#!/usr/bin/env bash
# Copyright (c) Microsoft Corporation. All rights reserved.
# Highly Confidential Material

###############################################################################
# This script is used to build the SOAP example applications that             #
# demonstrate the Sh interface.                                               #
#                                                                             #
# It assumes that you have a java JDK installed, and have javac (the          #
# Java compiler) and java (the Java interpreter) in your PATH.  You should    #
# also have both WSDL files (ShService.wsdl and ShServiceTyped.wsdl)          #
# located two directories above here.                                         #
###############################################################################
AXIS_JARS="axis2/*"

###############################################################################
# Name:       list_java_files                                                 #
# Operation:  List all java files located in the passed in directory          #
#             parameter and save this list in the passed in file name.        #
#                                                                             #
# Parameters: $1 The directory containing the java files to be listed.        #
#             $2 The output file.                                             #
###############################################################################
list_java_files()
{
  # Delete the output file
  rm -f "$2" > /dev/null 2>&1

 # List the java files
  for file in "$1"/*.java
  do
    echo $file >> "$2"
  done
}

if [ ! -d soapstub ]
then
  #############################################################################
  # If we are re-generating the soapstub code, then any of the classes        # 
  # which have been compiled are no longer required. The soapstub and any     #
  # example apps will be rebuilt, so these can be deleted, so as not to       #
  # have any out of date classes remaining.                                   #
  #############################################################################
  rm -rf classes

  #############################################################################
  # Use Apache Axis2 WSDL2Java tool to create stub code for accessing the     #
  # SOAP service.                                                             #
  #                                                                           #
  # Stub code for the "untyped" style will be written to the                  #
  # "soapstub/untyped" directory.  Code for the "typed" style will be         #
  # written to "soapstub/typed".                                              #
  # We are using different databindings for the typed and untyped             #
  # applications. This is due to a bug in the ADB databinding which           #
  # means we can't compile our typed soapstub with the ADB databinding        #
  #############################################################################

  echo Generating Java code.

  java -classpath "$AXIS_JARS" org.apache.axis2.wsdl.WSDL2Java -uri ../../Definition/ShService.wsdl -u -o soapstub/untyped --noBuildXML --noMessageReceiver --ns2p http://www.metaswitch.com/ems/soap/sh=com.MetaSwitch.EMS.SOAP
  java -classpath "$AXIS_JARS" org.apache.axis2.wsdl.WSDL2Java -uri ../../Definition/ShServiceTyped.wsdl -g -u -d jaxbri -o soapstub/typed --noBuildXML --noMessageReceiver --namespace2package http://www.metaswitch.com/ems/soap/sh=com.MetaSwitch.EMS.SOAP,http://www.metaswitch.com/ems/soap/sh/userdata=com.MetaSwitch.EMS.SOAP,http://www.metaswitch.com/ems/soap/sh/servicedata=com.MetaSwitch.EMS.SOAP -Djavax.xml.accessExternalSchema=all

  echo Java code generated from schema.

else
  echo SOAP stub code exists - compiling.
  echo ""
  echo If you have changed the MetaView Server name in ShService.wsdl or you have
  echo recently upgraded MetaView to a new version and need to use its new schema,
  echo please delete the directory 'soapstub' and run build.bat again.
  echo ""
fi

###############################################################################
# Create binary directories and compile the stub code.                        #
#                                                                             #
# Class files will be written to the "classes" directory.                     #
###############################################################################

echo Compiling SOAP stub

mkdir classes 2> /dev/null
mkdir classes/untyped 2> /dev/null
mkdir classes/typed 2> /dev/null

list_java_files soapstub/untyped/src/com/MetaSwitch/EMS/SOAP untypedjavafiles
list_java_files soapstub/typed/src/com/MetaSwitch/EMS/SOAP typedjavafiles

javac -classpath "$AXIS_JARS":classes/untyped -sourcepath soapstub/untyped -d classes/untyped @untypedjavafiles -J-Xms256m -J-Xmx256m 2> /dev/null
javac -classpath "$AXIS_JARS":classes/typed -sourcepath soapstub/typed -d classes/typed @typedjavafiles -J-Xms512m -J-Xmx512m 2> /dev/null

###############################################################################
# Compile the example applications.                                           #
#                                                                             #
# Class files will be written to the "classes" directory.                     #
###############################################################################

javac -classpath "$AXIS_JARS":classes/untyped:classes -d classes source/ShUtilities.java
javac -classpath "$AXIS_JARS":classes/untyped:classes -d classes source/untyped/ShUntypedUtilities.java
javac -classpath "$AXIS_JARS":classes/untyped:classes -d classes source/untyped/SimplePull.java
javac -classpath "$AXIS_JARS":classes/untyped:classes -d classes source/untyped/GlobalSearch.java
javac -classpath "$AXIS_JARS":classes/untyped:classes -d classes source/untyped/UpdateSubscription.java
javac -classpath "$AXIS_JARS":classes/untyped:classes -d classes source/untyped/DeleteSubscriber.java
javac -classpath "$AXIS_JARS":classes/untyped:classes -d classes source/untyped/DeleteAGCLine.java
javac -classpath "$AXIS_JARS":classes/typed:classes -d classes source/typed/ShTypedUtilities.java
javac -classpath "$AXIS_JARS":classes/typed:classes -d classes source/typed/UpdatePIN.java
javac -classpath "$AXIS_JARS":classes/typed:classes -d classes source/typed/CreateSubscriber.java
javac -classpath "$AXIS_JARS":classes/typed:classes -d classes source/typed/CreateClusteredSubscriber.java
javac -classpath "$AXIS_JARS":classes/typed:classes -d classes source/typed/CreateAGCLine.java
javac -classpath "$AXIS_JARS":classes/typed:classes -d classes source/typed/AddMandatoryAccountCode.java
javac -classpath "$AXIS_JARS":classes/typed:classes -d classes source/typed/EnumerateSubscribers.java

echo Build completed.
