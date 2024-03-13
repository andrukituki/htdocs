@echo off
REM Copyright (c) Microsoft Corporation. All rights reserved.
REM Highly Confidential Material.
echo /************************************************************************/
echo /* This script runs the SimplePull application, which displays part of  */
echo /* an object's configuration.                                           */
echo /*                                                                      */
echo /* It requires two parameters:                                          */
echo /* -  the user identity of the object to look up, e.g. a subscriber's   */
echo /*    directory number.  Where an object with more than one identifier  */
echo /*    is requested, separate the identifiers with slashes.              */
echo /* -  optionally, the service indications to fetch.  If not provided,   */
echo /*    this defaults to "Meta_Subscriber_BaseInformation".  This may be  */
echo /*    a comma separated list, or Meta_^<object type^>_* to get all        */
echo /*    configuration.                                                    */
echo /*                                                                      */
echo /* For details of the script, see the comments in the source code.      */
echo /************************************************************************/
set AXIS_JARS=.\axis2\*

java -classpath %AXIS_JARS%;classes\untyped;classes SimplePull %*
