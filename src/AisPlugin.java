
package no.polaric.ais;
import no.polaric.aprsd.*;
import java.util.*;



public class AisPlugin implements PluginManager.Plugin
{
      private ServerAPI _api; 
     
     
      /** Start the plugin  */
      public void activate(ServerAPI api)
      {
         try {
           System.out.println("*** AisPlugin.activate");
           _api = api;
           _api.getChanManager().addClass("AIS-TCP", "no.polaric.ais.AisChannel");
           
        }
        catch (Exception e) {} // FIXME: 
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
 
