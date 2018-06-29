#
# This file is part of "The High Voltage System".
#
# This is free software; you can redistribute it and/or modify
# it.
#

#JAVA_PATH = /apps/java/PRO/bin
#JAVA_PATH = /apps/java/jdk1.6.0_03/bin
JAVA_PATH =/usr/bin
OPT	= 
DEBUG	= -deprecation
JAVA	= $(JAVA_PATH)/java
JAVAC	= $(JAVA_PATH)/javac
JAR     = $(JAVA_PATH)/jar
JAVADOC = $(JAVA_PATH)/javadoc
APPV	= $(JAVA_PATH)/appletviewer

.SUFFIXES: .java .class .html .jar

.java.class:
	$(JAVAC) $(OPT) $(DEBUG) $<

.class.html:	
	$(APPLV) $@

.jar:
	$(JAR)  $<



HELP:
	@echo 'To compile "The Java High Voltage System" do one of the following:'
	@echo '------------------------------------------------------------'
	@echo 'make all          - compile all files to classes'
	@echo 'make jar          - compile all files to classes and crate jar file'
#	@echo 'make test         - compile applet and run appletviewer'
#	@echo 'make telnet       - compile standalone and run telnet'
#	@echo 'make chartest     - compile character display test and run'
	@echo 'make doc          - create documentation'
	@echo 'make clean        - delete backup files'
	@echo 'make realclean    - make clean and delete .class files'

all: HVS.class HVmainMenu.class DBAdapter.class \
     hvtools hvframe hvmap hvserver socket
	@echo All classes created.


doc: 
	$(JAVADOC) -d Doc/Source -author -version \
		hvtools hvframe hvserver hvmap socket \
	        HVS.java HVmainMenu.java DBAdapter.java
    
tar:	
	rm -f ../hvs.tgz 
	(cd ..; tar cf - hv | gzip - > hvs.tgz)

jar: 
	$(JAR) cvmf HVS.mf HVS.jar *.class *.wav hvframe/*.class hvmap/*.class \
		images hvserver/*.class hvtools/*.class socket/*.class


revision: 
	grep @version *.java */*.java|awk '{split($$6,rev,".");printf("%-26.26s %2.2s.%-2.2s (%s)\n",$$1,rev[1],rev[2],$$7);}' > REVISION
	rcs2log *.java */*.java > CHANGES
	sed -e"s/package:.*$$/package: `date +%c`/" index.html > index.html.x
	mv index.html.x index.html

dist:	realclean all doc tar bin-tar
	@echo DONE.

clean:	
	rm -f *~ */*~

realclean:	clean
	rm -f *.class */*.class

# dependencies

