@echo off
REM Copyright (c) Microsoft Corporation. All rights reserved.
REM Highly Confidential Material.
echo /************************************************************************/
echo /* This script runs the UpdateSubscription application, which updates   */
echo /* whether a subscriber is subscribed to a particular call service.     */
echo /*                                                                      */
echo /* It requires three parameters:                                        */
echo /* -  the directory number of the subscriber to look up                 */
echo /* -  the call service to which to subscribe or unsubscribe             */
echo /*      (this is without the "Meta_Subscriber_" prefix)                 */
echo /* -  what action to perform on this call service.  This can take one   */
echo /*    of three values:                                                  */
echo /*    -  "subscribed", to subscribe this DN to this service             */
echo /*    -  "unsubscribed", to unsubscribe this DN from this service       */
echo /*    -  "default", to return this subscriber to the default defined    */
echo /*       in its Persistent Profile for subscription to this service.    */
echo /*                                                                      */
echo /* For details of the script, see the comments in the source code.      */
echo /************************************************************************/
set AXIS_JARS=.\axis2\*

java -classpath %AXIS_JARS%;classes\untyped;classes UpdateSubscription %*
