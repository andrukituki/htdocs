@echo off
REM Copyright (c) Microsoft Corporation. All rights reserved.
REM Highly Confidential Material.
echo /************************************************************************/
echo /* This script runs the CreateAGCLine application, which creates a      */
echo /* new line on an AGC.                                                  */
echo /*                                                                      */
echo /* Its usage is as follows:                                             */
echo /*    CreateAGCLine -nwsapusername ^<NWSAP authentication username^>      */
echo /*    -nwsappassword ^<NWSAP authentication password^>                    */
echo /*    [-mvs ^<mvs^>]                                                      */
echo /*    -dn ^<dn^>                                                          */
echo /*    -ne ^<Network Element name^>                                        */
echo /*    -linegroup ^<Line Group^>                                           */
echo /*    -gateway ^<Gateway name^>                                           */
echo /*    -accessDevice ^<Access Device name^>                                */
echo /*    -accessLineNumber ^<Access Line Number^>                            */
echo /*    [-username ^<SIP user name^>]                                       */
echo /*    [-domain ^<SIP domain name^>]                                       */
echo /*    [-password ^<SIP password^>]                                        */
echo /*                                                                      */
echo /* where the parameters are as follows:                                 */
echo /* -nwsapusername:                                                      */
echo /*       the NWSAP authentication username.                             */
echo /* -nwsappassword:                                                      */
echo /*       the NWSAP authentication password.                             */
echo /* -mvs: the hostname or IP address of the MetaView Server              */
echo /* -dn:  the new line's directory number                                */
echo /* -ne:  the name of the Network Element on which to create the line.   */
echo /*       This must be an Access Gateway Controller (AGC).               */
echo /* -linegroup:                                                          */
echo /*       the new Line Group which the new line should use.              */
echo /* -gateway:                                                            */
echo /*       the Gateway that the new line should use                       */
echo /* -accessDevice:                                                       */
echo /*       the Access Device that the new line should use                 */
echo /* -accessLineNumber:                                                   */
echo /*       the line number on the Access Device that the new line should  */
echo /*       use.                                                           */
echo /* -username:                                                           */
echo /*       the SIP user name.  If omitted, the line will use its DN for   */
echo /*       identification.                                                */
echo /* -domain:                                                             */
echo /*       the SIP domain name. If omitted, the line will use the domain  */
echo /*       name from its SIP Registrar.                                   */
echo /* -password:                                                           */
echo /*       the SIP password.  If omitted, the line will not use SIP       */
echo /*       authentication.                                                */
echo /*                                                                      */
echo /* For details of the script, see the comments in the source code.      */
echo /************************************************************************/
set AXIS_JARS=axis2\*

java -classpath %AXIS_JARS%;classes\typed;classes CreateAGCLine %*
