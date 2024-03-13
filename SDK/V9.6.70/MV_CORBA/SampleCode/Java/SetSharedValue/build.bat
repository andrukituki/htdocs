@echo off
REM (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
REM **MOD+*********************************************************************
REM *                                                                         *
REM * Name:      build.bat                                                    *
REM *                                                                         *
REM * Purpose:   This batch file builds the SetSharedValue example Corba      *
REM *            application.  It must be run before the SetSharedValue       *
REM *            example application can be run.                              *
REM *                                                                         *
REM **MOD-*********************************************************************

REM **************************************************************************/
REM First of all build the Corba Interface files.                            */
REM **************************************************************************/

pushd ..
call bldCommon.bat
popd

REM **************************************************************************/
REM Now build the SetSharedValue application.                                */
REM                                                                          */
REM Class files will be written to the "SampleCode\classes" directory        */
REM **************************************************************************/

javac -classpath ..\classes;..\classes\* -d ..\classes src\SetSharedValue.java
