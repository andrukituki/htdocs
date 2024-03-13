@echo off
REM (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
REM **************************************************************************/
REM This batch file builds the Provisioning example Corba application.       */
REM It must be run before the Provisioning example application can be run.   */
REM **************************************************************************/

REM **************************************************************************/
REM First of all build the Corba Interface files.                            */
REM **************************************************************************/

pushd ..
call bldCommon.bat
popd

REM **************************************************************************/
REM Now build the Provisioning application.                                  */
REM                                                                          */
REM Class files will be written to the "SampleCode\classes" directory          */
REM **************************************************************************/

javac -classpath ..\classes;..\classes\* -d ..\classes src\*.java
