@echo off
REM Copyright (c) Microsoft Corporation. All rights reserved.
REM Highly Confidential Material.
echo /************************************************************************/
echo /* This script runs the CreateSubscriber application, which creates a   */
echo /* new subscriber record.                                               */
echo /*                                                                      */
echo /* Its usage is as follows:                                             */
echo /*    CreateSubscriber [-mvs ^<mvs^>] -dn ^<dn^>                            */
echo /*    -metaswitch ^<MetaSwitch name^>                                     */
echo /*    [-businessgroup ^<Business Group name^>]                            */
echo /*    [-subscribergroup ^<Subscriber Group^>]                             */
echo /*    [-profile ^<Persistent Profile^>] [-username ^<SIP user name^>]       */
echo /*    -domain ^<SIP domain name^> [-password ^<SIP password^>]              */
echo /*                                                                      */
echo /* where the parameters are as follows:                                 */
echo /* -mvs: the hostname or IP address of the MetaView Server              */
echo /* -dn:  the new subscriber's directory number                          */
echo /* -metaswitch:                                                         */
echo /*       the name of the MetaSwitch on which to create the subscriber   */
echo /* -businessgroup:                                                      */
echo /*       the name of the Business Group in which to create the          */
echo /*       subscriber.  If omitted, the new subscriber will be an         */
echo /*       Individual Line                                                */
echo /* -subscribergroup:                                                    */
echo /*       the new subscriber's Subscriber Group.  This is required if    */
echo /*       creating an Individual Line, but can be omitted if creating a  */
echo /*       Business Group line, in which case the default value will be   */
echo /*       used.                                                          */
echo /* -profile:                                                            */
echo /*       the Persistent Profile that the new subscriber should use      */
echo /* -username:                                                           */
echo /*       the SIP user name.  If omitted, the subscriber will use its DN */
echo /*       for identification                                             */
echo /* -domain:                                                             */
echo /*       the SIP domain name                                            */
echo /* -password:                                                           */
echo /*       the SIP password.  If omitted, the subscriber will not use SIP */
echo /*       authentication.                                                */
echo /*                                                                      */
echo /* For details of the script, see the comments in the source code.      */
echo /************************************************************************/
set AXIS_JARS=.\axis2\*

java -classpath %AXIS_JARS%;classes;classes\typed CreateSubscriber %*
