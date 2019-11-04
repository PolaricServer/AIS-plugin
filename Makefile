##########################################################################
## Change macros below according to your environment and your needs
##
## CLASSDIR if you want to compile to a class directory instead of generating
##          a jar, by using the 'test' target, you may set the directory here.
##
## CLASSPATH Specify where to find the servlet library and the java-cup
##           library. For Debian Linux platform you wont need to change
##           this.
##
## JAVAC: Java compiler
## JAR:   Jar archiver
##########################################################################
  CLASSDIR = classes
     SYSLIB= /usr/share/java
      ALIB = aprsd-lib
 CLASSPATH = $(SYSLIB)/gettext-commons.jar:$(ALIB)/spark-core-polaric.jar:$(ALIB)/jcoord-polaric.jar:lib/ais-lib-communication.jar:lib/ais-lib-messages.jar:lib/ais-lib-cli.jar:polaric-aprsd.jar
INSTALLDIR = /etc/polaric-aprsd/plugins
     JAVAC = javac -source 11 -target 11
       JAR = jar
       
       
# Review (and if necessary) change these if you are going to 
# install by using this makefile

   INSTALL_JAR = $(DESTDIR)/etc/polaric-aprsd/plugins
   INSTALL_WWW = $(DESTDIR)/etc/polaric-webapp/www/auto
   INSTALL_BIN = $(DESTDIR)/usr/bin
INSTALL_CONFIG = $(DESTDIR)/etc/polaric-aprsd
   INSTALL_LOG = $(DESTDIR)/var/log/polaric

   
   
##################################################
##  things below should not be changed
##
##################################################
    LIBDIR = _lib
 JAVAFLAGS =
 PACKAGES  = core i18n scala
 LANGUAGES = no

 

all: aprs

install: polaric-ais.jar
	install -d $(INSTALL_JAR)
	install -d $(INSTALL_CONFIG)/config.d
	install -m 644 polaric-ais.jar $(INSTALL_JAR)
	install -m 644 lib/*.jar $(INSTALL_JAR)
	install -m 644 ais.ini $(INSTALL_CONFIG)/config.d
	
$(INSTALLDIR)/polaric-ais.jar: polaric-ais.jar
	cp polaric-ais.jar $(INSTALLDIR)/polaric-ais.jar

	
aprs: $(LIBDIR)
	@make TDIR=$(LIBDIR) CLASSPATH=$(LIBDIR):$(CLASSPATH) compile     
	cd $(LIBDIR);jar cvf ../polaric-ais.jar *;cd ..


compile: $(PACKAGES)
	

$(CLASSDIR): 
	mkdir $(CLASSDIR)
	
		
$(LIBDIR):
	mkdir $(LIBDIR)

	
.PHONY : core
core: 
	$(JAVAC) -d $(TDIR) $(JAVAFLAGS) src/*.java 

	
.PHONY : i18n
i18n: 
	bash msg-compile.sh $(LANGUAGES)
	
	
.PHONY : scala
scala: core           
	scalac -d $(TDIR) -classpath $(LIBDIR):$(CLASSPATH) src/*.scala

clean:
	@if [ -e ${LIBDIR} ]; then \
		  rm -Rf $(LIBDIR); \
	fi 
	rm -f ./*~ src/*~
	
