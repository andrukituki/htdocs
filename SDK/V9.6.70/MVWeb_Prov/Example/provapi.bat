@echo off
echo /************************************************************************/
echo /* This script runs the ProvAPI application.  It requires five          */
echo /* parameters:                                                          */
echo /*  - the MetaView Server to send provisioning requests to.             */
echo /*  - the CFS to provision subscribers on.                              */
echo /*  - the user on MetaView Web.                                         */
echo /*  - password for the user on MetaView Web                             */
echo /*  - directory number to provision.                                    */
echo /*                                                                      */
echo /* and has one optional parameter:                                      */
echo /* [templates] - templates list to use (optional)                       */
echo /*                                                                      */
echo /* For details of the script, see the comments in the source code.      */
echo /************************************************************************/

@echo on
@java -classpath .\lib\*;.\classes ProvAPISampleApp/ProvAPISampleApp %1 %2 %3 %4 %5 %6
