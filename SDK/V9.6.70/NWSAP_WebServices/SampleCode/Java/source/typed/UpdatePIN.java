//-----------------------------------------------------------------------------
// UpdatePIN
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material.
//
// Sample application demonstrating the Network Wide Web Services API.
//
// This application sends in an Sh-Pull request to find a subscriber's current
// PIN, and then an Sh-Update request to change it to a new value.  It uses
// Apache Axis2 and the "typed" WSDL file, and also demonstrates bypassing the
// sequence number.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------
import com.MetaSwitch.EMS.SOAP.TMetaSwitchData;
import com.MetaSwitch.EMS.SOAP.TMetaSubscriberBaseInformation;


import com.MetaSwitch.SRB.SOAP.ShPull;
import com.MetaSwitch.SRB.SOAP.ShPullResponse;
import com.MetaSwitch.SRB.SOAP.ShService;
import com.MetaSwitch.SRB.SOAP.ShServiceStub;
import com.MetaSwitch.SRB.SOAP.ShUpdate;
import com.MetaSwitch.SRB.SOAP.ShUpdateResponse;
import com.MetaSwitch.SRB.SOAP.TUserData;

public class UpdatePIN
{
  private final static String USAGE = "Usage: UpdatePIN " +
                                      "<NWSAP username> " +
                                      "<NWSAP password> " +
                                      "<Directory number> <New PIN>";
  private static ShTypedUtilities utilities = new ShTypedUtilities();

  public static void main(String[] args)
  {
    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      // There are 4 mandatory parameters: 
      //  - The username with which to authenticate with NWSAP
      //  - The password with which to authenticate with NWSAP
      //  - The subscriber's DN
      //  - The new PIN to set
      //-----------------------------------------------------------------------
      if (args.length != 4)
      {
        throw new WrongParametersException("Wrong number of parameters.");
      }

      String nwsapUsername = args[0];
      String nwsapPassword = args[1];
      String dn = args[2];
      String newPin = args[3];

      //-----------------------------------------------------------------------
      // Connect to the Sh service.
      //-----------------------------------------------------------------------
      ShServiceStub shService = new ShServiceStub();

      utilities.authenticate(shService,
                             nwsapUsername,
                             nwsapPassword);

      //-----------------------------------------------------------------------
      // Make the request to the MetaView Server.
      //-----------------------------------------------------------------------
      ShPull shPullRequest = utilities.createPullRequest(
                                  dn,
                                  0,
                                  "Meta_Subscriber_BaseInformation",
                                  utilities.ORIGIN_HOST);

      ShPullResponse shPullResponse = shService.shPull(shPullRequest);
      TUserData userData = shPullResponse.getUserData();

      //-----------------------------------------------------------------------
      // Check that the request succeeded and extract and display the current
      // PIN.
      //-----------------------------------------------------------------------     
      utilities.checkResultCode(shPullResponse.getResultCode(),
                                shPullResponse.getExtendedResult(),
                                userData);

      TMetaSwitchData metaSwitchData = userData.getShData()
                                               .getRepositoryData()
                                               .get(0)
                                               .getServiceData()
                                               .getMetaSwitchData();

      TMetaSubscriberBaseInformation baseInformation =
                             metaSwitchData.getMetaSubscriberBaseInformation();

      System.out.println("Old PIN: " + baseInformation.getPIN());

      //-----------------------------------------------------------------------
      // Update the user data with the new PIN, then send in the Update request
      // and check it succeeded.
      //-----------------------------------------------------------------------
      baseInformation.setPIN(newPin);
      
      String originHost = utilities.ORIGIN_HOST +
                          utilities.IGNORE_SEQUENCE_NUMBER;

      final ShUpdate shUpdateRequest = utilities.createUpdateRequest(dn,
                                                                     0,
                                                                     userData,
                                                                     originHost);
                               

      ShUpdateResponse shUpdateResponse = shService.shUpdate(shUpdateRequest);

      utilities.checkResultCode(shUpdateResponse.getResultCode(),
                                shUpdateResponse.getExtendedResult(),
                                userData);

      System.out.println("PIN successfully changed to " + newPin);
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
