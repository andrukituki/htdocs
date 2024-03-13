@echo off
REM Copyright (c) Microsoft Corporation. All rights reserved.
REM Highly Confidential Material.
echo /************************************************************************/
echo /* This script runs the AddMandatoryAccountCode application, which      */
echo /* creates a new Mandatory Account Code for a Business Group.           */
echo /*                                                                      */
echo /* It requires five mandatory parameters:                               */
echo /* -  the NWSAP authentication username.                                */
echo /* -  the NWSAP authentication password.                                */
echo /* -  the name of the MetaSwitch on which the Business Group resides    */
echo /* -  the name of the Business Group to update                          */
echo /* -  the new code to create                                            */
echo /* and one optional parameter:                                          */
echo /* -  a description for the new code.                                   */
echo /*                                                                      */
echo /* For details of the script, see the comments in the source code.      */
echo /************************************************************************/
set AXIS_JARS=axis2\*

java -classpath %AXIS_JARS%;classes\typed;classes AddMandatoryAccountCode %*
