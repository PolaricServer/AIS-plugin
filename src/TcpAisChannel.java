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
 * AIS Channel using TCP connection.
 */
 
public class TcpAisChannel extends AisChannel
{
    private   String    _host; 
    private   int       _port;
    
    transient private   AisTcpReader  reader;
    
        
    public TcpAisChannel(AprsServerConfig conf, String id) 
    {
        super(conf, id);
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
       
       
    @Override
    public JsConfig getJsConfig() {
        var cnf = new JsConfig();
        cnf.messages = _messages;
        cnf.vessels = _vessels; 
        cnf.type  = "AIS-TCP";
        cnf.host  = _api.getProperty("channel."+getIdent()+".host", "localhost");
        cnf.port  = _api.getIntProperty("channel."+getIdent()+".port", 21);
        return cnf;
    }
    
    
    @Override
    public void setJsConfig(Channel.JsConfig ccnf) {
        var cnf = (JsConfig) ccnf;
        var props = _conf.config();
        props.setProperty("channel."+getIdent()+".host", cnf.host);
        props.setProperty("channel."+getIdent()+".port", ""+cnf.port);
    }
    
       
   
    /**
     * Load/reload configuration parameters. Called each time channel is activated. 
     */
    @Override
    protected void getConfig()
    {      
        String id = getIdent();
        _host = _conf.getProperty("channel."+id+".host", "localhost");
        _port = _conf.getIntProperty("channel."+id+".port", 4030);
    }
   
 

 
    /** Start the service */
    @Override
    public void activate(AprsServerConfig a) {
        try {
            getConfig();
            _conf.log().info("AisChannel", chId()+"Activating AIS channel: "+getIdent()+" ("+_host+":"+_port+")");
            reader = AisReaders.createReader(_host, _port);
            reader.setReconnectInterval(10000);
            reader.registerPacketHandler(new Consumer<AisPacket>() {
               @Override
               public void accept(AisPacket packet) {
                   handlePacket(packet);
               }
            });
            
            reader.start();
            _state = State.STARTING;
        } catch (Exception e) {
            _state = State.OFF;
            _conf.log().error("AisChannel", chId()+"Failed to activate AIS channel: "+getIdent()+" - "+e);
            e.printStackTrace(System.out);
            // Clean up reader if it was created but activation failed
            if (reader != null) {
                try {
                    reader.stopReader();
                    reader = null;
                } catch (Exception cleanupEx) {
                    _conf.log().warn("AisChannel", chId()+"Error cleaning up reader during failed activation: "+cleanupEx);
                }
            }
            throw new RuntimeException("Failed to activate AIS channel: "+getIdent(), e);
        }
    }
    

    
    /** Stop the service */
    @Override
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
        catch (InterruptedException e) {
            _conf.log().warn("AisChannel", chId()+"Interrupted while stopping AIS channel: "+getIdent());
            Thread.currentThread().interrupt(); // Restore interrupted status
            _state = State.OFF;
        }
    }
    
    
    
    @Override
    public String toString() { return "AIS TCP Channel"; }

}

