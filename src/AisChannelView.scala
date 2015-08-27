 
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

import java.util._
import java.io._
import scala.xml._
import org.simpleframework.transport.connect.Connection
import org.simpleframework.transport.connect.SocketConnection
import org.simpleframework.http._
import uk.me.jstott.jcoord._
import no.polaric.aprsd._
import no.polaric.aprsd.http._
import org.xnap.commons.i18n._
import no.polaric.webconfig._



/**
 * Web view for Aprs Channels.
 */
package no.polaric.ais
{
   
   class AisChannelView 
      ( override val api: ServerAPI, override val model: AisChannel, override val req: Request) 
             extends ChannelView(api, model, req) with ConfigUtils
   {        
         
         protected def aistraffic: NodeSeq = 
             simpleLabel("nmsgs", "leftlab", 
                I.tr("AIS messages")+":", TXT(""+model.heardMsgs())) ++   
             simpleLabel("nvessels", "leftlab", I.tr("Added vessels")+":", TXT(""+model.heardVessels())) ++ br     
             ;
         

              
             
         override def fields(req : Request): NodeSeq =   
              state ++
              aistraffic ++
              activate ++ 
              inetaddr ++
              br ++
              visibility
              ;
         
         
         
         override def action(req : Request): NodeSeq = 
              br ++ br ++
              getField(req, "item1", chp+".on", BOOLEAN) ++
              action_inetaddr(chp) ++
              getField(req, "item10", chp+".restrict", BOOLEAN) ++
              getField(req, "item11", chp+".style", NAME) ++ 
              action_activate
              ; 
             
   }
  
}