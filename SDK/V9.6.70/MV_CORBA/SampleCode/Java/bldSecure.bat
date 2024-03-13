@echo off
REM (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
REM **************************************************************************/
REM This batch file is used to build CorbaHelperSecure.java, which is used   */
REM to start the Jacorb ORB in a secure connection with the Metaview Server. */
REM                                                                          */
REM See the readme.txt file for more details.                                */
REM **************************************************************************/

cd common

if not exist ..\classes\com\nul md ..\classes\com

javac -classpath ..\classes;..\classes\jacorb.jar;..\classes\jacorb-omgapi.jar;..\classes\jacorb-services.jar;..\classes\iaik_ssl.jar;..\classes\iaik_jce.jar;..\classes\slf4j-api.jar;..\classes\slf4j-jdk14.jar;..\classes\avalon-framework.jar;..\classes\jboss-rmi-api_1.0_spec.jar;..\classes\jacorbOverwriteSecurity.jar -d ..\classes com\Metaswitch\MVS\UtilsSecure\*.java

cd ..
