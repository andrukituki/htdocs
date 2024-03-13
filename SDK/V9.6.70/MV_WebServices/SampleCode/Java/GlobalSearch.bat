@echo off
REM Copyright (c) Microsoft Corporation. All rights reserved.
REM Highly Confidential Material.
echo /************************************************************************/
echo /* This script runs the GlobalSearch application, which performs a      */
echo /* search of the MetaView Shadow Configuration Database.                */
echo /*                                                                      */
echo /* It requires two mandatory  parameters:                               */
echo /* -  the object type to look up e.g. Meta_Subscriber.                  */
echo /* -  a comma-separated list of output fields e.g.                      */
echo /*    baseinformation_directorynumber                                   */
echo /* and one optional parameter                                           */
echo /* - a where clause corresponding to an SQL query e.g.                  */
echo /*   baseinformation_directorynumber='2012030000' (note that if you use */
echo /*   spaces in this clause, it must be surrounded by "".                */
echo /*                                                                      */
echo /* For details of the script, see the comments in the source code.      */
echo /************************************************************************/
set AXIS_JARS=.\axis2\*

java -classpath %AXIS_JARS%;classes\untyped;classes GlobalSearch %*
