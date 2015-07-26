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



   
package no.polaric.ais 
{
   
   class AisVesselView 
      ( override val api: ServerAPI, override val model:AisVessel, override val canUpdate: Boolean, override val req: Request) 
            extends TrackerPointView(api, model, canUpdate, req) with ServerUtils
   {
       /** AIS ship info. */
       protected def aisinfo(req : Request): NodeSeq = 
          { if (model.getCallsign() != null)
              simpleLabel("callsign", "leftlab", I.tr("Callsign")+":", TXT(model.getCallsign())) else <span></span> } ++
          { if (model.getName() != null)
              simpleLabel("shipname", "leftlab", I.tr("Name")+":", TXT(model.getName())) else <span></span> } ++
          simpleLabel("shiptype", "leftlab", I.tr("Type")+":", TXT(model.getTypeText()+" ("+model.getType()+")")) ++
          { if (model.getNavStatus() != -1)
              simpleLabel("navstatus", "leftlab", I.tr("Nav status")+":", 
              TXT(model.getNavStatusText()+" ("+model.getNavStatus()+")" )) else <span></span> }
          ;
       
       
       override def fields(req : Request): NodeSeq = 
           ident(req) ++
           alias(req) ++
           aisinfo(req) ++
           descr(req) ++
           position(req) ++
           basicSettings(req)
       
   }
  
}