@echo off
REM Copyright (c) Microsoft Corporation. All rights reserved.
REM Highly Confidential Material.
echo /************************************************************************/
echo /* This script runs the DeleteAGCLine application, which deletes        */
echo /* the specified line.                                                  */
echo /*                                                                      */
echo /* It requires one parameter: the directory number of the line to       */
echo /* delete.                                                              */
echo /*                                                                      */
echo /* For details of the script, see the comments in the source code.      */
echo /************************************************************************/
set AXIS_JARS=.\axis2\*

java -classpath %AXIS_JARS%;classes\untyped;classes DeleteAGCLine %*
