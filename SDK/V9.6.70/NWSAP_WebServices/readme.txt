Copyright (c) Microsoft Corporation. All rights reserved.
Highly Confidential Material.

=================================
Network Wide Web Services API SDK
=================================

This directory contains the resources needed to develop SOAP/XML applications
for use with the Network Wide Web Services API.  For information about using
this interface please see the
"MetaSphere EAS, MetaView and N-Series Web Services Programmers Guide".

=================
QUICK START GUIDE
=================

To get started with Web Services, firstly enable the API on your MetaView
Server as detailed in the manual.  Remember that the server must be restarted
for the change to take effect.

Open the two WSDL files in this directory, and check that in each one the line
containing "wsdlsoap:address" also contains the name or IP address of your
MetaView Server.  If not, replace "&&METAVIEW_SERVER_NAME&&" with your
MetaView Server.

Run the build.bat batch file in either the "java" example applications, and
then the batch files (for Java) to run the applications.  If you encounter
problems, see the more detailed instructions below.

==================
DIRECTORY CONTENTS
==================

Definition
=============

This directory contains the WSDL and XML Schema files that define the Sh
interface:

 ShService.wsdl       The "untyped" WSDL file, for working with user data as
                      XML.
 ShServiceTyped.wsdl  The "typed" WSDL file, for working with user data as
                      strongly-typed classes.
 userData.xsd         The NWSAP version of the IMS schema defining the valid
                      format of user data.
 serviceDataMeta.xsd/serviceDataMsph.xsd/serviceDataNsrs.xsd
                      The MetaSwitch/Metasphere/NSeries XML Schema defining the
                      valid format of service data.  These pull in the
                      serviceData.xsd from MV_WebServices, EAS_WebServices and
                      NSeries_WebServices respectively.

See the manual for more information on the WSDL and schema files.

SampleCode
=============

This is the parent directory for NWSAP example applications that demonstrate
the Sh interface.  Your MetaView Server must have SOAP enabled
before you can use this interface: see the manual for instructions.

If you have extracted the installation CD to your MetaView Server and installed
it, the WSDL files will have been prepared for you.  Otherwise, before you can
use the example applications, you must edit both WSDL files to replace
"&&METAVIEW_SERVER_NAME&&" with the name or IP address of your MetaView Server.

SampleCode\Java
==================

This directory contains Java sample applications that use Apache Axis2.

To use the Sh interface from Java, use Apache Axis2' WSDL2Java tool to build
"stub" code from the WSDL file.  You will need the Axis2 JARs in your classpath
when you compile and run your applications, although the batch files provided
with the sample applications (see below) will set up the classpath for you.
See the Axis2 documentation for more information.

The directory contains batch files to demonstrate compiling and running.

Before you run these scripts, you must set up your system: see build.bat for
details. The scripts must be run from their directory.

The first time you run build.bat, it will generate the stub code for both
WSDL files. It will then compile the SOAP stub code and example applications.
If the stub code already exists, when you run build.bat it will not be
re-generated.

If you later change the MetaView Server name in ShService.wsdl or you upgrade
MetaView to a new version and need to use its new schema, you will need to
delete the directory 'soapstub' and again follow the procedure of running
build.bat.

The other batch files will run the sample applications.

Each of the batch jobs for running the applications contains instructions on
how to use them.

SampleCode\Java\axis2
=======================

This directory contains distributions of:
- Apache Axis2 1.8.0

See the license files in this directory for license details.

SampleCode\Java\source
=========================

This directory and its subdirectories contain the source for the Java example
applications.

