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
* scala-library version 2.11 or later. You will also need scala-xml
  and scala-parser-combinators packages. 
* polaric-aprsd, polaric-webapp and polaric-webconfig-plugin installed.

## Installation

We provide a deb package (Debian Buster or later) For information on getting 
started on a Debian platform (or derivative) please see: 
http://aprs.no/dokuwiki?id=install.dev

If doing manual installation, you may need to add the following to the 
server.ini file and restart: 

plugins = no.polaric.ais.AisPlugin


## Building from source 

Build from the source is done by a plain old makefile. Yes I know :)
Maybe I move to something else a little later. Setup for generating Debian
packages is included. You may use the 'debuild' command.

You will need JDK (Oracle or OpenJDK) version 11 or later, the Scala
programming language version 2.11 or later (scala and scala-library). 

Note. To compile this package you need to symlink or copy the following
files into the directory where makefile is located. You find them in the polaric-aprsd package.
* AIS-lib from Danish Maritime Authority (https://github.com/dma-ais/AisLib)
* polaric-aprsd.jar
* jcoord.jar
* simple.jar
