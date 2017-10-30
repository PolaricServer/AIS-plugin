 
import java.util._
import java.io._
import scala.xml._
import scala.collection.JavaConversions._
import no.polaric.aprsd._
import no.polaric.aprsd.http._
import no.polaric.aprsd.http.ServerUtils
import no.polaric.aprsd.http.ServerBase
import org.xnap.commons.i18n._



package no.polaric.ais
{

  class Webserver 
      ( val api: ServerAPI ) extends ServerBase(api) with ServerUtils
  {
        PointView.addView(classOf[AisVessel], classOf[AisVesselView])
        ChannelView.addView(classOf[AisChannel],  classOf[AisChannelView])    
        ConfigUtils.addChanType("AIS-TCP")
  }

}
