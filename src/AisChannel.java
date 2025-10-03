/* 
 * Copyright (C) 2015-23 by LA7ECA, Øyvind Hanssen (ohanssen@acm.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
 
package no.polaric.ais;
import java.io.*;
import java.util.*;
import no.polaric.core.*;
import no.polaric.core.httpd.*;
import no.polaric.aprsd.*;
import no.polaric.aprsd.point.*;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.reader.*;
import java.util.function.Consumer;
import dk.dma.ais.message.*;
import dk.dma.ais.sentence.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonSubTypes.*;


/**
 * AIS Channel. Use AisTcpReader to get messages over a TCP connection.
 */
 
public class AisChannel extends Channel
{
    private   String    _host; 
    private   int       _port;
    
    transient private   AisTcpReader  reader;
    transient private   AprsServerConfig _conf;
    transient private   int       _chno;
    private static int _next_chno = 0;
    transient private Logfile log = AisPlugin.log;
    transient private  long _vessels   = 0;
    transient private  long _messages  = 0; 
    
        
    /* Register subtypes for deserialization */
    public static void classInit() { 
        ServerBase.addSubtype(AisChannel.JsConfig.class, "AIS-TCP");
    }

        
    public AisChannel(AprsServerConfig conf, String id) 
    {
        _init(conf, "channel", id);
        _conf= conf;        
        _chno = _next_chno;
        _next_chno++;
        _state = State.OFF;
    }
   
       
    /* 
     * Information about config to be exchanged in REST API
     */
    
    @JsonTypeName("AIS-TCP")
    public static class JsConfig extends Channel.JsConfig {
        public long messages, vessels;
        public int port; 
        public String host;
    }
       
       
    public JsConfig getJsConfig() {
        var cnf = new JsConfig();
        cnf.messages = _messages;
        cnf.vessels = _vessels; 
        cnf.type  = "AIS-TCP";
        cnf.host  = _api.getProperty("channel."+getIdent()+".host", "localhost");
        cnf.port  = _api.getIntProperty("channel."+getIdent()+".port", 21);
        return cnf;
    }
    
    
    public void setJsConfig(Channel.JsConfig ccnf) {
        var cnf = (JsConfig) ccnf;
        var props = _conf.config();
        props.setProperty("channel."+getIdent()+".host", cnf.host);
        props.setProperty("channel."+getIdent()+".port", ""+cnf.port);
    }
    
       
       
       
    public long heardVessels()
       { return _vessels; }
       
    public long heardMsgs()
       { return _messages; }
       
   
    /**
     * Load/reload configuration parameters. Called each time channel is activated. 
     */
    protected void getConfig()
    {      
        String id = getIdent();
        _host = _conf.getProperty("channel."+id+".host", "localhost");
        _port = _conf.getIntProperty("channel."+id+".port", 4030);
    }
   
 
 
    // Reusable Calendar instance to reduce object allocation
    private final ThreadLocal<Calendar> calendarCache = ThreadLocal.withInitial(Calendar::getInstance);
    
    /**
     * Update position.
     */
    protected void updatePos(AisVessel st, IPositionMessage msg) {

        AisPosition pos = msg.getPos();
        double lat = pos.getLatitudeDouble();
        double lon = pos.getLongitudeDouble();
        int speed=-1, course=-1;
         
        // Reuse Calendar instance instead of creating new one
        Calendar ts = calendarCache.get();
        long currentTimeMillis = System.currentTimeMillis();
        ts.setTimeInMillis(currentTimeMillis);
            
        if (msg instanceof IVesselPositionMessage) {
            var mm = (IVesselPositionMessage) msg;
            if (!mm.isPositionValid())
                return;
                
            speed = (mm.isSogValid() ? (int) Math.round(mm.getSog() * 0.1852) : -1);
            course = (mm.isHeadingValid() ? mm.getTrueHeading() : -1);
                
            /* Adjust timestamp */
            int zsec = mm.getUtcSec();
            if (zsec < 60) {
                ts.set(Calendar.SECOND, zsec);
                // Cache the time limit calculation
                long timeLimit = currentTimeMillis + 3000;
                while (ts.getTimeInMillis() > timeLimit)
                    ts.roll(Calendar.MINUTE, -1); 
            }
        }
        
        if (lat>90 || lat<-90) {
            log.debug(null, chId()+"Latitude out of bounds ("+st.getIdent()+") "+lat);
            return;
        }
        LatLng prevpos = (st.getPosition()==null ? null : st.getPosition());
        if ( st.saveToTrail(ts.getTime(), new LatLng(lat, lon), speed, course, 
                (msg instanceof AisMessage27 ? "AISLONG" : "AIS"))) 
        {
            st.updatePosition(ts.getTime(), new LatLng(lat, lon));    
            _conf.getDB().updateItem(st, prevpos);
        }
        st.setSpeed(speed);
        st.setCourse(course);
    }
   

   
    protected void updatePosExtra(AisVessel st, AisPositionMessage msg) {
        updatePos(st, msg);
        st.setNavStatus(msg.getNavStatus());
    }
    
   
   /**
    * Process static AIS message.
    */
    protected void updateStatic(AisVessel st, AisStaticCommon msg) {    
       int type = msg.getShipType();
       String callsign = AisMessage.trimText(msg.getCallsign());
       String name = AisMessage.trimText(msg.getName());
       
       if (name != null && name.length() > 0)
          st.setName(name);
       if (callsign != null && callsign.length() > 0)
          st.setCallsign(callsign);
       if (type != 0) {
          st.setType(type);
          if (type == 51)
             st.setTag("AIS.SAR");
          if (type == 55)
             st.setTag("AIS.law");
          if (type == 58)
             st.setTag("AIS.medical");
          if (type / 10 == 4 || type / 10 == 6)
             st.setTag("AIS.passenger");
          if (type / 10 == 5)
             st.setTag("AIS.special");
          if (type / 10 == 7)
             st.setTag("AIS.cargo");
          if (type / 10 == 8)
             st.setTag("AIS.tanker");
       }
       st.setLabelHidden(false); 
       st.autoTag(); 
    }
 
 
    /** 
     * Get Point object for AIS message.
     */
    protected AisVessel getStn(AisMessage msg) {
        long id = msg.getUserId();
        AisVessel v = (AisVessel) _conf.getDB().getItem("MMSI:"+id, null);
        if (v == null) {
            v = new AisVessel(null, id);
            v.setLabelHidden(true);
            v.setTag("AIS");
            if (getTag() != null && !getTag().equals(""))
                v.setTag(getTag());
            _conf.getDB().addItem(v);
            _vessels++;
        }
        v.setSource(this);        
        return v;
    } 
 
 

 
    /** Start the service */
    private long prev_log_time = System.currentTimeMillis();
    private static final long LOG_INTERVAL_MS = 120000; // 2 minutes
    
    public void activate(AprsServerConfig a) {
        getConfig();
        _conf.log().info("AisChannel", chId()+"Activating AIS channel: "+getIdent()+" ("+_host+":"+_port+")");
        reader = AisReaders.createReader(_host, _port);
        reader.setReconnectInterval(10000);
        reader.registerPacketHandler(new Consumer<AisPacket>() {
           @Override
           public void accept(AisPacket packet) {
               try {
                   AisMessage msg = packet.getAisMessage();
                   _state = State.RUNNING;
                   AisVessel st = getStn(msg);
                   _messages++;
                
                   int msgId = msg.getMsgId();
                   if (msgId == 1 || msgId == 2 || msgId == 3)
                       /* Position */
                       updatePosExtra(st, (AisPositionMessage) msg);
                   else if (msgId == 5 || msgId == 24)
                       /* Static */
                       updateStatic(st, (AisStaticCommon) msg);
                   else if (msgId == 18)
                       /* Simple position */
                       updatePos(st, (IVesselPositionMessage) msg);
                   else if (msgId == 19) {
                       /* Extended position */
                       updateStatic(st, (AisStaticCommon) msg);
                       updatePos(st, (IVesselPositionMessage)msg);
                   }
                   else if (msgId == 27) {
                       /* Long range */
                       updatePos(st, (IPositionMessage) msg);
                   }
                   
                   // Periodic logging - check with minimal overhead
                   long currentTime = System.currentTimeMillis();
                   if (currentTime - prev_log_time >= LOG_INTERVAL_MS) {
                      prev_log_time = currentTime; 
                      log.info(null, chId()+"Received "+_messages+" messsages, "+_vessels+" vessels");
                   }
                   
               } catch (Throwable e) {
                    log.warn(null, chId()+"Cannot parse ais message: "+e);
                    e.printStackTrace(System.out);
                    return;
               }
           }
        });
        
        reader.start();
        _state = State.STARTING; 
    }
    

    
    /** Stop the service */
    public void deActivate() {
        _conf.log().info("AisChannel", chId()+"Dectivating AIS channel: "+getIdent());
        try {
            if (reader!=null) {
                reader.stopReader();
                reader.join();
                reader = null;
            }
           _state = State.OFF;
        } 
        catch (InterruptedException e) {}
    }
    
    
    
    public String getShortDescr()
       { return "ais"+_chno; }
 
         

    public String toString() { return "AIS Channel"; }

}

