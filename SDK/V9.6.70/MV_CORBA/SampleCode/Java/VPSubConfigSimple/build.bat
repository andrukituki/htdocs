@echo off
REM (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
REM **************************************************************************/
REM This batch file builds the VPSubConfigSimple example Corba application.  */
REM It must be run before the VPSubConfigSimple example application can be   */
REM run.                                                                     */
REM **************************************************************************/

REM **************************************************************************/
REM First of all build the Corba Interface files.                            */
REM **************************************************************************/

pushd ..
call bldCommon.bat
popd

REM **************************************************************************/
REM Now build the VPSubConfigSimple application.                             */
REM                                                                          */
REM Class files will be written to the "SampleCode\classes" directory        */
REM **************************************************************************/

javac -classpath ..\classes;..\classes\* -d ..\classes src\*.java
