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
import uk.me.jstott.jcoord._
import no.polaric.aprsd._
import no.polaric.aprsd.http._
import org.xnap.commons.i18n._
import spark.Request;
import spark.Response;



   
package no.polaric.ais 
{
   
   class AisVesselView 
      ( override val api: ServerAPI, override val model:AisVessel, override val canUpdate: Boolean, override val req: Request) 
            extends TrackerPointView(api, model, canUpdate, req) with ServerUtils
   { 
       val II = getI18n(req, "no.polaric.ais.AisPlugin");
   
   
       /** AIS ship info. */
       protected def aisinfo(req : Request): NodeSeq =  
          { if (model.hasCallsign())
              simpleLabel("callsign", "leftlab", II.tr("Callsign")+":", TXT(model.getCallsign())) else EMPTY } ++
          { if (model.getName() != null)
              simpleLabel("shipname", "leftlab", II.tr("Name")+":", TXT(model.getName())) else EMPTY } ++
          { if (model.getType() != 0) 
              simpleLabel("shiptype", "leftlab", II.tr("Type")+":", TXT(model.getTypeText()+" ("+model.getType()+")")) 
            else EMPTY } ++
          { if (model.getNavStatus() != -1)
              simpleLabel("navstatus", "leftlab", II.tr("Nav status")+":", 
              TXT(model.getNavStatusText()+" ("+model.getNavStatus()+")" )) else EMPTY }
          ;
       
       
       /** Show altitude and course */                    
       protected def speedcourse(req: Request): NodeSeq =
            { if (model.getSpeed() > 0)
                  simpleLabel("cspeed", "leftlab", II.tr("Movement")+":", 
                  { TXT( Math.round(model.getSpeed()*0.539956803*10)/10 + " "+II.tr("knots")+" ") ++  
                     _directionIcon(model.getCourse(),fprefix(req))}) 
               else EMPTY }
            ;
                
                
       /** Show info about a trail point. */  
       override def trailpoint(req: Request, tp: Trail.Item): NodeSeq = 
            tp_prefix(tp) ++
            { if (tp.speed >= 0) simpleLabel("tp_speed", "lleftlab", II.tr("Speed")+":", 
                    TXT(Math.round(tp.speed * 0.539956803*10)/10 + " "+II.tr("knots")) )
                else EMPTY } ++
              simpleLabel("tp_dir",   "lleftlab", II.tr("Heading")+":", _directionIcon(tp.course, fprefix(req)))  
             ;
             
            
       override def fields(req : Request): NodeSeq = 
           ident(req) ++
           alias(req) ++
           aisinfo(req) ++
           descr(req) ++
           position(req) ++
           speedcourse(req) ++ 
           basicSettings(req)
       
   }
  
}
