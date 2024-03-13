//-----------------------------------------------------------------------------
// SimplePull
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material.
//
// Sample application demonstrating the Network Wide Web Services API.
//
// This application sends in an Sh-Pull request for part of an object's
// configuration and prints out the result.  It uses Apache Axis2 and the
// "untyped" WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------
import com.MetaSwitch.SRB.SOAP.ShPull;
import com.MetaSwitch.SRB.SOAP.ShPullResponse;
import com.MetaSwitch.SRB.SOAP.ShServiceStub;
import com.MetaSwitch.SRB.SOAP.TExtendedResult;
import com.MetaSwitch.SRB.SOAP.TUserData;
import com.MetaSwitch.SRB.SOAP.TUserIdentity;
import com.MetaSwitch.SRB.SOAP.TDataReference;

public class SimplePull
{
  private static final String USAGE = "Usage: SimplePull " +
                                      "<NWSAP username> " +
                                      "<NWSAP password> " +
                                      "<User identity> [<Service " +
                                      "indication(s)>]";
  private static ShUntypedUtilities utilities = new ShUntypedUtilities();

  public static void main(String[] args)
  {
    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      //-----------------------------------------------------------------------
      if ((args.length < 3) || (args.length > 4))
      {
        throw new WrongParametersException("The wrong number of parameters was" +
                                           " provided");
      }

      String nwsapUsername = args[0];
      String nwsapPassword = args[1];
      String userIdentity = args[2];
      String servInd =
              (args.length == 4 ? args[3] : "Meta_Subscriber_BaseInformation");

      //-----------------------------------------------------------------------
      // Connect to the Sh service and send in a Pull request.
      //-----------------------------------------------------------------------
      ShServiceStub shService = new ShServiceStub();

      utilities.authenticate(shService,
                             nwsapUsername,
                             nwsapPassword);

      ShPull shPullRequest = utilities.createPullRequest(
                                             userIdentity, 
                                             0,
                                             servInd,
                                             utilities.ORIGIN_HOST);
      
      ShPullResponse shPullResponse = shService.shPull(shPullRequest);
      TUserData userData = shPullResponse.getUserData();

      //-----------------------------------------------------------------------
      // Check whether the request succeeded and display the value of each of
      // the fields.
      //-----------------------------------------------------------------------
      utilities.checkResultCode(shPullResponse.getResultCode().getTResultCode(),
                                shPullResponse.getExtendedResult(),
                                userData);

      utilities.displayFields(userData);
    }
    catch (MetaSwitchShInterfaceException e)
    {
      System.err.println(e.getMessage());
      System.err.println(USAGE);
    }
    catch (Exception e)
    {
      System.err.println("Unexpected error \"" + e
                         + "\" in retrieving data");
      e.printStackTrace(System.err);
      System.err.println(USAGE);
    }
  }
}
