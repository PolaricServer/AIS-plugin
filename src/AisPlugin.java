
package no.polaric.ais;
import no.polaric.aprsd.*;
import no.polaric.aprsd.http.*;
import java.util.*;



public class AisPlugin implements PluginManager.Plugin
{
      private ServerAPI _api; 
      static Logfile log;
     
     
      /** Start the plugin  */
      public void activate(ServerAPI api)
      {        
         /* It's ok, there is at most one instance of a plugin */
         log =  new Logfile(api, "aisplugin", "ais.log");
        
         try {
           System.out.println("*** AisPlugin.activate");
           _api = api;
           _api.getChanManager().addClass("AIS-TCP", "no.polaric.ais.AisChannel");
           _api.addHttpHandlerCls("no.polaric.ais.Webserver", null);
        }
        catch (Exception e) {
           log.log(" AisPlugin: ERROR: "+e);
        } 
      }
      
      
            
      // FIXME
      public boolean isActive()
       { return true; }

       
       
      /**  Stop the plugin */ 
      public void deActivate() {
         System.out.println("*** AisPlugin.deactivate");
      }
       
       
      private String[] _dep = {};
      
      
      
      /** Return an array of other component (class names) this plugin depends on */
      public String[] getDependencies() { return _dep; }
      
      
      
      public String getDescr() {
         return "AisPlugin"; 
      }

}
 
