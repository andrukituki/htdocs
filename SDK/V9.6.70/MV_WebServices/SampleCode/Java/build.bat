@echo off
REM Copyright (c) Microsoft Corporation. All rights reserved.
REM Highly Confidential Material.
REM **************************************************************************/
REM This batch file is used to build the SOAP example applications that      */
REM demonstrate the Sh interface.                                            */
REM                                                                          */
REM It assumes that you have a java JDK installed and have javac (the        */
REM Java compiler) and java (the Java interpreter) in your PATH.  You should */
REM also have both WSDL files (ShService.wsdl and ShServiceTyped.wsdl)       */
REM located two directories above here.                                      */
REM **************************************************************************/

SETLOCAL ENABLEDELAYEDEXPANSION

set AXIS_JARS=.\axis2\*

if not exist soapstub (
  REM ***********************************************************************/ 
  REM If we are re-generating the soapstub code, then any of the classes    */ 
  REM which have been compiled are no longer required. The soapstub and any */
  REM example apps will be rebuilt, so these can be deleted, so as not to   */
  REM have any out of date classes remaining.                               */
  REM ***********************************************************************/
  
  if exist classes rmdir /s /q classes

  REM ***********************************************************************/
  REM Use Apache Axis2 WSDL2Java tool to create stub code for accessing the */
  REM SOAP service.                                                         */
  REM                                                                       */
  REM Stub code for the "untyped" style will be written to the              */
  REM "soapstub\untyped" directory.  Code for the "typed" style will be     */
  REM written to "soapstub\typed".                                          */
  REM We are using different databindings for the typed and untyped        */
  REM applications. This is due to a bug in the ADB databinding which      */
  REM means we can't compile our typed soapstub with the ADB databinding    */
  REM ***********************************************************************/
  
  echo Generating Java code.

  java -classpath %AXIS_JARS% org.apache.axis2.wsdl.WSDL2Java -uri ..\..\Definition\ShService.wsdl -u -o soapstub\untyped --noBuildXML --noMessageReceiver --ns2p http://www.metaswitch.com/ems/soap/sh=com.MetaSwitch.EMS.SOAP
  java -classpath %AXIS_JARS% org.apache.axis2.wsdl.WSDL2Java -uri ..\..\Definition\ShServiceTyped.wsdl -g -u -d jaxbri -o soapstub\typed --noBuildXML --noMessageReceiver --namespace2package http://www.metaswitch.com/ems/soap/sh=com.MetaSwitch.EMS.SOAP,http://www.metaswitch.com/ems/soap/sh/userdata=com.MetaSwitch.EMS.SOAP,http://www.metaswitch.com/ems/soap/sh/servicedata=com.MetaSwitch.EMS.SOAP -Djavax.xml.accessExternalSchema=all
 
  
  echo Java code generated from schema.
  echo.
) else (
  echo SOAP stub code exists - compiling.
  echo.
  echo If you have changed the MetaView Server name in ShService.wsdl or you have
  echo recently upgraded MetaView to a new version and need to use its new schema,
  echo please delete the directory 'soapstub' and run build.bat again.
  echo.
)

REM ***********************************************************************/
REM Create binary directories and compile the stub code.                  */
REM                                                                       */
REM Class files will be written to the "classes" directory.               */
REM ***********************************************************************/
echo Compiling SOAP stub.

mkdir classes 2>NUL
mkdir classes\untyped 2>NUL
mkdir classes\typed 2>NUL

CALL :LIST_JAVA_FILES soapstub\untyped\src\com\MetaSwitch\EMS\SOAP untypedjavafiles
CALL :LIST_JAVA_FILES soapstub\typed\src\com\MetaSwitch\EMS\SOAP typedjavafiles

javac -classpath %AXIS_JARS%;classes\untyped -sourcepath soapstub\untyped -d classes\untyped @untypedjavafiles -J-Xms256m -J-Xmx256m
javac -classpath %AXIS_JARS%;classes\typed -sourcepath soapstub\typed -d classes\typed @typedjavafiles -J-Xms512m -J-Xmx512m

REM ********************************************************************/
REM Compile the example applications.                                  */
REM                                                                    */
REM Class files will be written to the "classes" directory.            */
REM ********************************************************************/

javac -classpath %AXIS_JARS%;classes\untyped;classes -d classes source\ShUtilities.java
javac -classpath %AXIS_JARS%;classes\untyped;classes -d classes source\untyped\ShUntypedUtilities.java
javac -classpath %AXIS_JARS%;classes\untyped;classes -d classes source\untyped\SimplePull.java
javac -classpath %AXIS_JARS%;classes\untyped;classes -d classes source\untyped\GlobalSearch.java
javac -classpath %AXIS_JARS%;classes\untyped;classes -d classes source\untyped\UpdateSubscription.java
javac -classpath %AXIS_JARS%;classes\untyped;classes -d classes source\untyped\DeleteSubscriber.java
javac -classpath %AXIS_JARS%;classes\untyped;classes -d classes source\untyped\DeleteAGCLine.java
javac -classpath %AXIS_JARS%;classes\typed;classes -d classes source\typed\ShTypedUtilities.java
javac -classpath %AXIS_JARS%;classes\typed;classes -d classes source\typed\UpdatePIN.java
javac -classpath %AXIS_JARS%;classes\typed;classes -d classes source\typed\CreateSubscriber.java
javac -classpath %AXIS_JARS%;classes\typed;classes -d classes source\typed\CreateClusteredSubscriber.java
javac -classpath %AXIS_JARS%;classes\typed;classes -d classes source\typed\CreateAGCLine.java
javac -classpath %AXIS_JARS%;classes\typed;classes -d classes source\typed\AddMandatoryAccountCode.java
javac -classpath %AXIS_JARS%;classes\typed;classes -d classes source\typed\EnumerateSubscribers.java

echo Build completed.
   
REM **************************************************************************/
REM Quits from build script.                                                 */
REM **************************************************************************/
ENDLOCAL
GOTO :EOF

REM **************************************************************************/
REM List all java files located in the passed in directory parameter and     */
REM save this list in the passed in file name.                               */
REM                                                                          */
REM Parameters:                                                              */
REM   %1:  The directory containing the java files to be listed.             */
REM   %2:  The output file.                                                  */
REM **************************************************************************/
:LIST_JAVA_FILES
(
  REM ************************************************************************/
  REM Delete the output file.                                                */
  REM ************************************************************************/
  DEL/S %2 >NUL 2>&1

  REM ************************************************************************/
  REM Lists the java files.                                                  */
  REM ************************************************************************/
  FOR /F %%I IN ('DIR/B %1\*.java') DO (
    ECHO %1\%%I >> %2
  )

  REM ************************************************************************/
  REM Return from internal function.                                         */
  REM ************************************************************************/
  GOTO :EOF
)
