@echo off
REM Copyright (c) Microsoft Corporation. All rights reserved.
REM Highly Confidential Material.
echo /************************************************************************/
echo /* This script runs the EnumerateSubscribers application, which         */
echo /* retrieves and displays a list of subscribers under a Business Group, */
echo /* or under a department of a Business Group.  The list displayed shows */
echo /* the type of subscriber, its directory number, and its intercom       */
echo /* dialing code if present.                                             */
echo /*                                                                      */
echo /* Its usage is as follows:                                             */
echo /*    EnumerateSubscribers                                              */
echo /*    -metaswitch ^<MetaSwitch name^>                                     */
echo /*    -businessgroup ^<Business Group name^>                              */
echo /*    [-department ^<department name^>]                                   */
echo /*                                                                      */
echo /* where the parameters are as follows:                                 */
echo /* -metaswitch:                                                         */
echo /*       the name of the MetaSwitch on which the Business Group exists  */
echo /* -businessgroup:                                                      */
echo /*       the name of the Business Group under which to list subscribers */
echo /* -department:                                                         */
echo /*       the department under which to list subscribers. If omitted,    */
echo /*       all subscribers in the Business Group will be listed.          */
echo /*                                                                      */
echo /* For details of the script, see the comments in the source code.      */
echo /************************************************************************/
set AXIS_JARS=.\axis2\*

java -classpath %AXIS_JARS%;classes\typed;classes EnumerateSubscribers %*
