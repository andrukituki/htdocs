@echo off
REM (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
REM /************************************************************************/
REM /* This script runs the VPSubConfig application.  It's usage is as      */
REM /* follows:                                                             */
REM /*                                                                      */
REM /* run -E:<MetaView Server hostname or IPv4 address>                    */
REM /*     -U:<MetaView Server username>                                    */
REM /*     -P:<MetaView Server password>                                    */
REM /*     -M:<metaswitch name>                                             */
REM /*     -T:<DN of subscriber>                                            */
REM /*     [ -D |                                                           */
REM /*       -I:<dotted IPv4 address>                                       */
REM /*       -N:<domain name>                                               */
REM /*       -G:<suscriber group name>                                      */
REM /*       -L:<media gateway model name> ]                                */
REM /*                                                                      */
REM /* -  The -E, -U, -P,-M and -T parameters must always be supplied.      */
REM /* -  Supply the -I, -N, -G and -L parameters to add a VoIP AD and      */
REM /*    Individual Line.                                                  */
REM /* -  Supply the -D parameter to delete a VoIP AD and Individual Line.  */
REM /*                                                                      */
REM /* For details of the script, see the comments in the source code.      */
REM /*                                                                      */
REM /* Sun's ORB does not provide support for SSL, so the MetaView Server's */
REM /* security access must be changed to 'insecure' for this application   */
REM /* to be able to login.                                                 */
REM /************************************************************************/

@echo on
@java -classpath ..\classes;..\classes\* -Djava.util.logging.config.file=..\classes\logging.properties VPSubConfig %*
