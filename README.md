# AIS-plugin for Polaric Server

The "Polaric Server" is mainly a web based service to present (APRS) 
tracking information on maps and where the information is updated in real-
time. It is originally targeted for use by radio amateurs in voluntary search
and rescue service in Norway. It consists of a web application and a server 
program (APRS daemon). 
 
This is a plugin that can read AIS data from a TCP stream and and present 
AIS vessels as moving points on the map. It is used on aprs.no with data from 
the Norwegian Maritime Authority. 
 
More documentation on the project can be found here: 
http://aprs.no/polaricserver

## System requirements

Linux/Java platform (tested with Debian/Ubuntu) with
* Java Runtime environment version 11 or later.  
* polaric-aprsd, polaric-webapp and polaric-webconfig-plugin installed.

## Installation

We provide a deb package. For information on getting 
started on a Debian platform (or derivative) please see: 
http://aprs.no/polaricserver

If doing manual installation, you may need to add the following to the 
server.ini file and restart: 

plugins = no.polaric.ais.AisPlugin


## Building from source 

Build from the source is done by maven. Setup for generating Debian
packages is included. You may use the 'debuild' command to build a deb package.

You will need JDK (Oracle or OpenJDK) version 17 or later.

The plugin depends on the AIS-lib from Danish Maritime Authority (https://github.com/dma-ais/AisLib)

