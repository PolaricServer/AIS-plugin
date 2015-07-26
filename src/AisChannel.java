/* 
 * Copyright (C) 2015 by LA7ECA, Øyvind Hanssen (ohanssen@acm.org)
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
import no.polaric.aprsd.*;

import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.reader.*;
import java.util.function.Consumer;
import dk.dma.ais.message.*;
import dk.dma.ais.sentence.*;
import uk.me.jstott.jcoord.*;



/**
 * Sound modem radio channel
 */
 
public class AisChannel extends Channel
{
    private   String    _host; 
    private   int       _port;
    
    transient private   AisReader  reader;
    transient private   ServerAPI _api;
    transient private   int       _chno;
    private static int _next_chno = 0;
    transient private Logfile log = AisPlugin.log;
        
        
    public AisChannel(ServerAPI api, String id) 
    {
        _init(api, "channel", id);
        _api = api;        
        _chno = _next_chno;
        _next_chno++;
        _state = State.OFF;
    }
   
   
    /**
     * Load/reload configuration parameters. Called each time channel is activated. 
     */
    protected void getConfig()
    {      
        String id = getIdent();
        _host = _api.getProperty("channel."+id+".host", "localhost");
        _port = _api.getIntProperty("channel."+id+".port", 4030);
    }
   
 
    /**
     * Update position.
     */
    protected void updatePos(AisVessel st, IVesselPositionMessage msg) {

       if (msg.isPositionValid()) {
          AisPosition pos = msg.getPos();
          double lat = pos.getLatitudeDouble();
          double lon = pos.getLongitudeDouble();
         
          int speed = (msg.isSogValid() ? (int) Math.round(msg.getSog() * 0.1852) : -1);
          int course = (msg.isHeadingValid() ? msg.getTrueHeading() : -1);
  
          /* Adjust timestamp */
          Calendar ts = Calendar.getInstance();
          ts.setTimeInMillis((new Date()).getTime()) ;
          int zsec = msg.getUtcSec();
          if (zsec < 60) {
             int sec = ts.get(Calendar.SECOND);
          
             if (zsec > 55 && sec < 5)
                ts.roll(Calendar.MINUTE, -1); 
             ts.set(Calendar.SECOND, zsec);
          }                
          if ( st.saveToTrail(ts.getTime(), new LatLng(lat, lon), speed, course, "AIS") );
          st.updatePosition(ts.getTime(), new LatLng(lat, lon));
          st.setSpeed(speed);
          st.setCourse(course);
       }
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
       
       st.setName(name);
       st.setCallsign(callsign);
       st.setLabelHidden(false);  
       st.setType(type);  
       log.log(" STATIC MSG: uid="+msg.getUserId() + ", type="+type+", callsign="+callsign+", name=" + name);  
    }
 
 
    /** 
     * Get Point object for AIS message.
     */
    protected AisVessel getStn(AisMessage msg) {
        long id = msg.getUserId();
        AisVessel v = (AisVessel) _api.getDB().getItem("MSSI:"+id, null);
        if (v == null) {
           v = new AisVessel(null, id);
           _api.getDB().addPoint(v);
           v.setLabelHidden(true);
        }
        v.setSource(this);        
        return v;
    } 
 
 

 
    /** Start the service */
    public void activate(ServerAPI a) {
        getConfig();
        log.log(" Activating AIS channel: "+getIdent()+" ("+_host+":"+_port+")");
        reader = AisReaders.createReader(_host, _port);
        reader.registerPacketHandler(new Consumer<AisPacket>() {
           @Override
           public void accept(AisPacket packet) {
               try {
                   AisMessage msg = packet.getAisMessage();
                   _state = State.RUNNING;
                   AisVessel st = getStn(msg);
                
                   if (msg.getMsgId() == 1 || msg.getMsgId() == 2 || msg.getMsgId() == 3)
                       /* Position */
                       updatePosExtra(st, (AisPositionMessage) msg);
                   else if (msg.getMsgId() == 5 || msg.getMsgId() == 24)
                       /* Static */
                       updateStatic(st, (AisStaticCommon) msg);
                   else if (msg.getMsgId() == 18)
                       /* Simple position */
                       updatePos(st, (IVesselPositionMessage) msg);
                   else if (msg.getMsgId() == 19 ) {
                       /* Extended position */
                       updateStatic(st, (AisStaticCommon) msg);
                       updatePos(st, (IVesselPositionMessage)msg);
                   }
               } catch (Throwable e) {
                    log.log(" WARNING: cannot parse ais message: "+e);
                    return;
               }
           }
        });
        
        reader.start();
        _state = State.STARTING; 
    }
    

    
    /** Stop the service */
    public void deActivate() {
        log.log(" Dectivating AIS channel: "+getIdent());
        try {
           reader.stopReader();
           reader.join();
           reader = null;
           _state = State.OFF;
        } 
        catch (InterruptedException e) {}
    }
    
    
    
    @Override public String getShortDescr()
       { return "ais"+_chno; }
 
         

    public String toString() { return "AIS Channel"; }

}

