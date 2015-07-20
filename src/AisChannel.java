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
import uk.me.jstott.jcoord.*;



/**
 * Sound modem radio channel
 */
 
public class AisChannel extends Channel
{

    private   AisReader  reader;
    private   String    _host; 
    private   int       _port; 
    private   ServerAPI _api;
 

    
    
    public AisChannel(ServerAPI api, String id) 
    {
        _init(api, "channel", id);
        _api = api;
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
  
   //      _log = new Logfile(_api, id, "ais.log");
    }
   
 
    /**
     * Update position.
     */
    protected void updatePos(Station st, IVesselPositionMessage msg) {

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
          /* Update APRS "station" */
          AprsHandler.PosData pd = new AprsHandler.PosData (new LatLng(lat,lon), course, speed, 's', '/');
          st.update(ts.getTime(), pd, null, "AIS");
       }
    }
   
   
   /**
    * Process static AIS message.
    */
    protected void updateStatic(Station st, AisStaticCommon msg) {      
       int type = msg.getShipType();
       String callsign = AisMessage.trimText(msg.getCallsign());
       String name = AisMessage.trimText(msg.getName());
       
       if (name != null)
           st.setAlias("'"+name+"'");
       else if (callsign != null)
           st.setAlias(callsign);
       st.setLabelHidden(false);  
       st.setDescr((callsign!=null? "callsign="+callsign+", " : "") + (name!=null? "name="+name+", " : "")+
            "type "+type+"="+type2text(type));   
       System.out.println("AIS STATIC: uid="+msg.getUserId() + ", type="+type+", callsign="+callsign+", name=" + name);  
    }
 
 
    /** 
     * Get item for AIS message (currently an APRS station).
     */
    protected Station getStn(AisMessage msg) {
        String id = "MSSI:"+msg.getUserId();
        Station station = _api.getDB().getStation(id, null);
        if (station == null) {
           station = _api.getDB().newStation(id); 
           station.setLabelHidden(true);
        }
        station.setSource(this);        
        return station;
    } 
 
 
    /** 
     * Get text for ship type.
     */
    public String type2text(int type) {
        if (type / 10 == 2)
           return "WIG (US)"; 
        
        /* 3x = Engaged in */
        else if (type == 30)
           return "Fishing";
        else if (type == 31 || type == 32)
           return "Towing";
        else if (type == 33 || type == 34)
           return "Underwater ops";
        else if (type == 35)
           return "Military ops";
        else if (type == 36)
           return "Sailing"; 
        else if (type == 37)
           return "Recreational";

        /* 5x = Special */
        else if (type == 50)
           return "Pilot"; 
        else if (type == 51)
           return "Search & rescue";
        else if (type == 54)
           return "Commercial response";
        else if (type == 55)
           return "Law enforcement";
        else if (type == 56 || type == 57)
           return "Assignment.."; 
        else if (type == 58)
           return "Medical/public safety";
        else if (type / 10 == 5)
           return "Special..";
           
        else if (type == 41 || type == 61)
           return "Passenger < 12 pas";
        else if (type == 43 || type == 63)
           return "Ferry < 150 pas";
        else if (type == 44 || type == 64)
           return "Ferry >= 150 pas"; 
        else if (type / 10 == 4)
           return "HS passenger";
        else if (type / 10 == 6)
           return "Passenger";
        
        else if (type / 10 == 7)
           return "Cargo";
        else if (type / 10 == 8)
           return "Tanker";
        else if (type / 10 == 9)
           return "Other";
        else return "ERROR";
    }
 
 
 
    /** Start the service */
    public void activate(ServerAPI a) {
        getConfig();
        reader = AisReaders.createReader(_host, _port);
        reader.registerHandler(new Consumer<AisMessage>() {
            @Override
            public void accept(AisMessage msg) {       
                _state = State.RUNNING;
                Station st = getStn(msg);
                
                if (msg.getMsgId() == 1 || msg.getMsgId() == 2 || msg.getMsgId() == 3 || msg.getMsgId() == 18)
                    /* Position */
                    updatePos(st, (IVesselPositionMessage) msg);
                else if (msg.getMsgId() == 5 || msg.getMsgId() == 24)
                    /* Static */
                    updateStatic(st, (AisStaticCommon) msg);
                else if (msg.getMsgId() == 19 ) {
                    /* Extended position */
                    updateStatic(st, (AisStaticCommon) msg);
                    updatePos(st, (IVesselPositionMessage)msg);
                }
            }
        });
        
        reader.start();
        _state = State.STARTING; 
    }
    

    
    /** Stop the service */
    public void deActivate() {
        try {
           reader.stopReader();
           reader.join();
           reader = null;
           _state = State.OFF;
        } 
        catch (InterruptedException e) {}
    }
    
    
    
    @Override public String getShortDescr()
       { return "AIS"; }
 
         

    public String toString() { return "AIS Channel"; }

}

