@echo off
REM Copyright (c) Microsoft Corporation. All rights reserved.
REM Highly Confidential Material.
echo /************************************************************************/
echo /* This script runs the CreateClusteredSubscriber application, which    */
echo /* creates a new subscriber record.                                     */
echo /*                                                                      */
echo /* Its usage is as follows:                                             */
echo /*    CreateSubscriber -nwsapusername ^<NWSAP authentication username^>   */
echo /*    -nwsappassword ^<NWSAP authentication password^>                    */
echo /*    [-mvs ^<mvs^>] -ne ^<ne^> -dn ^<dn^>                                    */
echo /*    [-businessgroup ^<business group name^>]                            */
echo /*    [-subscribergroup ^<Subscriber Group^>]                             */
echo /*    -preferredsite ^<Preferred site^>                                   */
echo /*    [-profile ^<Persistent Profile^>] [-username ^<SIP user name^>]       */
echo /*    -domain ^<SIP domain name^> [-password ^<SIP password^>]              */
echo /*                                                                      */
echo /* where the parameters are as follows:                                 */
echo /* -nwsapusername:                                                      */
echo /*       the NWSAP authentication username.                             */
echo /* -nwsappassword:                                                      */
echo /*       the NWSAP authentication password.                             */
echo /* -mvs: the hostname or IP address of the MetaView Server              */
echo /* -ne:  the name of the NE on which to create the subscriber           */
echo /* -dn:  the new subscriber's directory number                          */
echo /* -businessgroup:                                                      */
echo /*       the subscriber's business group. If omitted, the               */
echo /*       subscriber will not be in a business group                     */
echo /* -subscribergroup:                                                    */
echo /*        the new subscriber's Subscriber Group.  This is required if   */
echo /*       creating an Individual Line, but can be omitted if creating a  */
echo /*       Business Group line, in which case the default value will be   */
echo /*       used.                                                          */
echo /* -preferredsite                                                       */
echo /*       the new subscriber's Preferred Site                            */
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
set AXIS_JARS=axis2\*

java -classpath %AXIS_JARS%;classes\typed;classes CreateClusteredSubscriber %*
