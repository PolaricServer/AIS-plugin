 
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
import java.util.*;
import no.polaric.core.*;
import no.polaric.aprsd.point.*;
import java.io.Serializable;
import no.polaric.aprsd.*;
 

 
public class AisVessel extends TrackerPoint implements Serializable, Cloneable
{

           
    /* Class for Json encoding info about a point. This is subclassed in AprsPoint */
    public static class JsInfo extends TrackerPoint.JsInfo {
        public String name, callsign; 
        public String vtype, navstatus;
        
        public JsInfo(AisVessel p) {
            super(p);
            type = "AisVessel";
            name = p.getName();
            callsign = p.getCallsign();
            vtype = p.getTypeText();
            navstatus = p.getNavStatusText();
        }
    }
    
    
    public JsInfo getJsInfo() {
        return new JsInfo(this);
    }




     private long      _ident;
     private String    _name;
     private String    _callsign; 
     private int       _type = 0;
     private int       _navstatus = -1; 
     private String    _source;
     
     
     public AisVessel(LatLng p, long id)
       { super(p); _ident = id; }
       
     public boolean hasName()
       { return _name != null && _name.length() > 0; }
       
     public String getName()
       { return _name; }
     
     public void setName(String n) 
       { _name = n; }
     
     public boolean hasCallsign()
       { return _callsign != null && _callsign.length() > 0; }
       
     public String getCallsign()
       { return _callsign; }
       
     public void setCallsign(String cs)
       { _callsign = cs; }
           
     @Override public String getIdent()
        { return "MMSI:"+_ident; }
        
     @Override public String _getDisplayId() { 
         if (hasName()) 
            return _name;
         else if (hasCallsign()) 
            return _callsign; 
         else return getIdent();
      } 
   
     @Override public String getDescr() {
        if (hasDescr())
           return super.getDescr();
        else
           return (_callsign != null ? _callsign+", " : "") + getTypeText(); 
     }
     
     public int getType() 
        { return _type; }
        
     public void setType(int t)
        { _type = t; }
        
     public String getTypeText()
        { return type2text(_type); }
        
     public int getNavStatus() 
        { return _navstatus; }
      
     public String getNavStatusText()
        { return navstatus2text(_navstatus); }
        
     public void setNavStatus (int s)
        { _navstatus = s; }
        

    @Override public Source getSource()
       { return _api.getChanManager().get(_source); }
       
       
    @Override public String getSourceId()
       { return _source; }
       
    public void setSource(Source src)
       { _source = src.getIdent(); }
       
       
    @Override public String getIcon(boolean override) 
        { if (override && _icon != null)
            return _icon; 
          else return "boat.png"; }

    
    /** 
     * Get text for ship type.
     */
    public static String type2text(int type) {

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
        else return "Undefined";
    }
 
    public static String navstatus2text(int st) {
      switch (st) {
         case 0: return "Under way using engine"; 
         case 1: return "At anchor";
         case 2: return "Not under command";
         case 3: return "Restricted manoeuvrability";
         case 4: return "Constrained by her draught";
         case 5: return "Moore";
         case 6: return "Aground"; 
         case 7: return "Engaged in fishing";
         case 8: return "Under way sailing";
         case 14: return "AIS-SART (active)";
         default: return "Undefined";
      }
    } 
}
