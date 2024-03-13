@echo off
REM (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
REM /************************************************************************/
REM /* This script runs the Provisioning application.  It requires three    */
REM /* parameters:                                                          */
REM /* -  the hostname/IP address of the MetaView Server                    */
REM /* -  the username                                                      */
REM /* -  the password                                                      */
REM /*                                                                      */
REM /* For details of the script, see the comments in the source code.      */
REM /*                                                                      */
REM /* Sun's ORB does not provide support for SSL, so the MetaView Server's */
REM /* security access must be changed to 'insecure' for this application   */
REM /* to be able to login.                                                 */
REM /************************************************************************/

@echo on
@java -classpath ..\classes;..\classes\* -Djava.util.logging.config.file=..\classes\logging.properties Provisioning %1 %2 %3
