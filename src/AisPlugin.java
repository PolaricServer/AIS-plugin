
package no.polaric.ais;
import no.polaric.core.*;
import no.polaric.core.auth.*;
import no.polaric.aprsd.*;
import java.util.*;



public class AisPlugin implements PluginManager.Plugin
{
      private AprsServerConfig _conf; 
      static Logfile log;
     
     
      /** Start the plugin  */
      public void activate(AprsServerConfig conf)
      {        
         /* It's ok, there is at most one instance of a plugin */
         log =  new Logfile(conf, "aisplugin", "ais.log");
        
         try {
           conf.log().info("AisPlugin", "Activate plugin...");
           _conf = conf;
           _conf.getChanManager().addClass("AIS-TCP", "no.polaric.ais.TcpAisChannel");
           _conf.getChanManager().addClass("AIS-SERIAL", "no.polaric.ais.SerialAisChannel");
           AisChannel.classInit();
           AuthInfo.addService("ais");
        }
        catch (Exception e) {
           _conf.log().error("AisPlugin", ""+e);
        } 
      }
      
      
            
      // FIXME
      public boolean isActive()
       { return true; }

       
       
      /**  Stop the plugin */ 
      public void deActivate() {
         _conf.log().info("AisPlugin", "Deactivate plugin");
      }
       
       
      private String[] _dep = {};
      
      
      
      /** Return an array of other component (class names) this plugin depends on */
      public String[] getDependencies() { return _dep; }
      
      
      
      public String getDescr() {
         return "AisPlugin"; 
      }

}
 
