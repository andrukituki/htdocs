//-----------------------------------------------------------------------------
// SimplePull
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material 
//
// Sample application demonstrating the SOAP Sh interface to the MetaView
// Server.  This application sends in an Sh-Pull request for part of an object's
// configuration and prints out the result.  It uses Apache Axis2 and the
// "untyped" WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------

import com.MetaSwitch.EMS.SOAP.ShPull;
import com.MetaSwitch.EMS.SOAP.ShPullResponse;
import com.MetaSwitch.EMS.SOAP.ShServiceStub;
import com.MetaSwitch.EMS.SOAP.TExtendedResult;
import com.MetaSwitch.EMS.SOAP.TUserData;
import com.MetaSwitch.EMS.SOAP.TUserIdentity;
import com.MetaSwitch.EMS.SOAP.TDataReference;

public class SimplePull
{
  private static final String USAGE = "Usage: SimplePull " +
                                      "<User identity> [<Service " +
                                      "indication(s)>]";
  private static ShUntypedUtilities utilities = new ShUntypedUtilities();

  public static void main(String[] args)
  {
    
    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      // The first parameter is the subscriber's DN.  The second is the service
      // indication and is optional; it defaults to #
      // "Meta_Subscriber_BaseInformation" if not specified.
      //-----------------------------------------------------------------------
      if ((args.length < 1) || (args.length > 2))
      {
        throw new WrongParametersException("The wrong number of parameters was" +
                                           " provided");
      }

      String userIdentity = args[0];
      String serviceIndication = (args.length == 2 ? args[1] : "Meta_Subscriber_BaseInformation");

      //-----------------------------------------------------------------------
      // Connect to the Sh service and send in a Pull request.
      //-----------------------------------------------------------------------      
      ShServiceStub shService = new ShServiceStub();
      ShPull shPullRequest = utilities.createPullRequest(userIdentity,
                                                         0,
                                                         serviceIndication,
                                                         utilities.ORIGIN_HOST);

      ShPullResponse shPullResponse = shService.shPull(shPullRequest);

      //-----------------------------------------------------------------------
      // Check whether the request succeeded and display the value of each of
      // the fields.
      //-----------------------------------------------------------------------

      TUserData userData = shPullResponse.getUserData();

      utilities.checkResultCode(shPullResponse.getResultCode().getTResultCode(),
                                shPullResponse.getExtendedResult(),
                                userData);

      utilities.displayFields(userData);
    }
    catch (MetaSwitchShInterfaceException e)
    {
      utilities.handleMetaSwitchException(e, USAGE);
    }
    catch (Exception e)
    {
      utilities.handleUnexpectedException(e, USAGE, false);
    }
  }
}
