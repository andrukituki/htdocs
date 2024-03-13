@echo off
REM Copyright (c) Microsoft Corporation. All rights reserved.
REM Highly Confidential Material.
echo /************************************************************************/
echo /* This script runs the UpdatePIN application, which changes a          */
echo /* subscriber's PIN.                                                    */
echo /*                                                                      */
echo /* It requires four parameters:                                         */
echo /* -  the NWSAP authentication username.                                */
echo /* -  the NWSAP authentication password.                                */
echo /* -  the directory number of the subscriber to look up                 */
echo /* -  the new PIN to set for this subscriber.                           */
echo /*                                                                      */
echo /* For details of the script, see the comments in the source code.      */
echo /************************************************************************/
set AXIS_JARS=axis2\*

java -classpath %AXIS_JARS%;classes\typed;classes UpdatePIN %*
