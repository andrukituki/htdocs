@echo off
REM (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
REM /************************************************************************/
REM /* This script runs the GetLog application.  It requires five or six    */
REM /* parameters:                                                          */
REM /* -  the hostname/IP address of the MetaView Server                    */
REM /* -  the username                                                      */
REM /* -  the password                                                      */
REM /* -  the UID of the node on which the Log was raised                   */
REM /* -  the 16-digit log correlator (without spaces, or in quotes with    */
REM /*    spaces)                                                           */
REM /* -  optionally, the time (in seconds) to wait for the log collector   */
REM /*    to find the log, if it is not immediately available               */
REM /*                                                                      */
REM /* For details of the script, see the comments in the source code.      */
REM /*                                                                      */
REM /* Sun's ORB does not provide support for SSL, so the MetaView Server's */
REM /* security access must be changed to 'insecure' for this application   */
REM /* to be able to login.                                                 */
REM /************************************************************************/
REM

@echo on
@java -classpath ..\classes;..\classes\* -Djava.util.logging.config.file=..\classes\logging.properties GetLog %1 %2 %3 %4 %5 %6
