@echo off
REM **************************************************************************/
REM This batch file builds the ProvAPI example application.                  */
REM It must be run before the ProvAPI example application can be run.        */
REM **************************************************************************/

REM **************************************************************************/
REM build the ProvAPI application.                                           */
REM                                                                          */
REM Class files will be written to the "SampleCode\classes" directory        */
REM **************************************************************************/

mkdir classes 2>NUL

javac -classpath .\lib\*;.\classes -d .\classes src\ProvAPISampleApp\ProvApiRequestHandler.java
javac -classpath .\lib\*;.\classes -d .\classes src\ProvAPISampleApp\ProvAPISampleApp.java
