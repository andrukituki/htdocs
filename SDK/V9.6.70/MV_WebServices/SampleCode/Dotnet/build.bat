rem Copyright (c) Microsoft Corporation. All rights reserved.
rem Highly Confidential Material
@echo off
REM **************************************************************************/
REM This batch file is used to build the SOAP example applications that      */
REM demonstrate the Sh interface.                                            */
REM                                                                          */
REM It assumes that you have Microsoft .NET SDK installed, and have its bin  */
REM folder, and the .NET framework folder containing csc.exe (for example    */
REM C:\WINDOWS\Microsoft.NET\Framework\v2.0.xxxxx) in your PATH.             */
REM **************************************************************************/

REM **************************************************************************/
REM Use Microsoft .NET's wsdl.exe tool to create stub code for accessing the */
REM SOAP service.                                                            */
REM                                                                          */
REM Stub code will be written to the "soapstub" directory.                   */
REM **************************************************************************/

mkdir soapstub 2>NUL

wsdl.exe ..\..\Definition\ShService.wsdl /o:soapstub\ShService.cs
wsdl.exe ..\..\Definition\ShServiceTyped.wsdl ..\..\Definition\servicedata.xsd ..\..\Definition\userdata.xsd /o:soapstub\ShServiceTyped.cs

REM **************************************************************************/
REM Compile the libraries.                                                   */
REM **************************************************************************/

csc /t:library /out:Untyped.dll soapstub\ShService.cs source\ShUtilities.cs source\untyped\ShUntypedUtilities.cs
csc /t:library /out:Typed.dll soapstub\ShServiceTyped.cs source\ShUtilities.cs source\typed\ShTypedUtilities.cs

REM **************************************************************************/
REM Compile the example applications.                                        */
REM **************************************************************************/

csc /out:SimplePull.exe /r:Untyped.dll source\untyped\SimplePull.cs
csc /out:UpdateSubscription.exe /r:Untyped.dll source\untyped\UpdateSubscription.cs
csc /out:Deletesubscriber.exe /r:Untyped.dll source\untyped\DeleteSubscriber.cs
csc /out:UpdatePIN.exe /r:Typed.dll source\typed\UpdatePIN.cs
csc /out:CreateSubscriber.exe /r:Typed.dll source\typed\CreateSubscriber.cs
csc /out:AddMandatoryAccountCode.exe /r:Typed.dll source\typed\AddMandatoryAccountcode.cs
csc /out:EnumerateSubscribers.exe /r:Typed.dll source\typed\EnumerateSubscribers.cs

echo Build completed.
