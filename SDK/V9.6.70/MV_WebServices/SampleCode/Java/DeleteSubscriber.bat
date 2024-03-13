@echo off
REM Copyright (c) Microsoft Corporation. All rights reserved.
REM Highly Confidential Material.
echo /************************************************************************/
echo /* This script runs the DeleteSubscriber application, which deletes     */
echo /* the specified subscriber.                                            */
echo /*                                                                      */
echo /* It requires one parameter: the directory number of the subscriber to */
echo /* delete.                                                              */
echo /*                                                                      */
echo /* For details of the script, see the comments in the source code.      */
echo /************************************************************************/
set AXIS_JARS=.\axis2\*

java -classpath %AXIS_JARS%;classes\untyped;classes DeleteSubscriber %*
