@echo off
REM (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
REM **************************************************************************/
REM This batch file is used to generate and build the interface files from   */
REM the IDL definitions in the idl directory, as well as the common code in  */
REM the common directory.                                                    */
REM                                                                          */
REM It uses the idlj.jar as an idl compiler. This Jar is located             */
REM at ..\..\SampleCode\Java\classes and should be used for all MV versions  */
REM                                                                          */
REM **************************************************************************/

REM **************************************************************************/
REM Go to the location of the .idl interface definition files.               */
REM **************************************************************************/

cd ..\..\Definition\idl

REM **************************************************************************/
REM Generate java ORB-supporting code using idlj.                            */
REM                                                                          */
REM The interfaces defined in these idl files will be put into the           */
REM   com.Metaswitch.MVS.Corba package                                       */
REM due to the idl.config file in the "idl" directory.                       */
REM                                                                          */
REM For more information on this file see the idlj tool help at:             */
REM   http://java.sun.com/j2se/1.4.1/docs/tooldocs/tools.html                */
REM                                                                          */
REM Output files will be written to the "SampleCode" directory.              */
REM **************************************************************************/

java -jar ..\..\SampleCode\Java\classes\idlj.jar -td ..\..\SampleCode\Java\common NLSSupportInterface.idl
java -jar ..\..\SampleCode\Java\classes\idlj.jar -td ..\..\SampleCode\Java\common SEAccessInterface.idl
java -jar ..\..\SampleCode\Java\classes\idlj.jar -td ..\..\SampleCode\Java\common SEExceptions.idl
java -jar ..\..\SampleCode\Java\classes\idlj.jar -td ..\..\SampleCode\Java\common SettingsUserInterface.idl
java -jar ..\..\SampleCode\Java\classes\idlj.jar -td ..\..\SampleCode\Java\common SEAccessFactoryInterface.idl
java -jar ..\..\SampleCode\Java\classes\idlj.jar -td ..\..\SampleCode\Java\common ClientSessionInterface.idl
java -jar ..\..\SampleCode\Java\classes\idlj.jar -td ..\..\SampleCode\Java\common ClientSessionManagerInterface.idl
java -jar ..\..\SampleCode\Java\classes\idlj.jar -fall -td ..\..\SampleCode\Java\common SnapshotChangeListenerInterface.idl
java -jar ..\..\SampleCode\Java\classes\idlj.jar -td ..\..\SampleCode\Java\common omlapi.idl

cd ..\..\SampleCode\Java\common

if not exist ..\classes\com\nul md ..\classes\com

if not exist ..\classes\logging.properties (
  echo # Console Handler>> ..\classes\logging.properties
  echo handlers= java.util.logging.ConsoleHandler>> ..\classes\logging.properties
  echo.>> ..\classes\logging.properties
  echo # Global logging level.>> ..\classes\logging.properties
  echo .level= OFF>> ..\classes\logging.properties
  echo.>> ..\classes\logging.properties
  echo # Console handler specific configuration>> ..\classes\logging.properties
  echo java.util.logging.ConsoleHandler.level = OFF>> ..\classes\logging.properties
  echo java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter>> ..\classes\logging.properties
)

REM **************************************************************************/
REM Compile the Corba Interface files that have just been generated.         */
REM                                                                          */
REM Class files will be written to the "SampleCode\classes" directory        */
REM **************************************************************************/
javac -classpath ..\classes;..\classes\* -d ..\classes com\Metaswitch\MVS\Corba\*.java

REM **************************************************************************/
REM Compile the Common helper files.                                         */
REM                                                                          */
REM Class files will be written to the "SampleCode\classes" directory        */
REM **************************************************************************/
javac -classpath ..\classes;..\classes\jacorb.jar;..\classes\jacorb-omgapi.jar;..\classes\jacorb-services.jar;..\classes\iaik_ssl.jar;..\classes\iaik_jce.jar;..\classes\slf4j-api.jar;..\classes\slf4j-jdk14.jar;..\classes\avalon-framework.jar;..\classes\jboss-rmi-api_1.0_spec;..\classes\jacorbOverwriteSecurity.jar -d ..\classes com\Metaswitch\MVS\Utils\*.java

cd ..
