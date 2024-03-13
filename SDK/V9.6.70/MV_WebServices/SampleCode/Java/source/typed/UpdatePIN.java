//-----------------------------------------------------------------------------
// UpdatePIN
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material.
//
// Sample application demonstrating the SOAP Sh interface to the MetaView
// Server.  This application sends in an Sh-Pull request to find a subscriber's
// current PIN, and then an Sh-Update request to change it to a new value.  It
// uses Apache Axis and the "typed" WSDL file, and also demonstrates bypassing
// the sequence number.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------

import com.MetaSwitch.EMS.SOAP.ShPull;
import com.MetaSwitch.EMS.SOAP.ShPullResponse;
import com.MetaSwitch.EMS.SOAP.ShService;
import com.MetaSwitch.EMS.SOAP.ShServiceStub;
import com.MetaSwitch.EMS.SOAP.ShUpdate;
import com.MetaSwitch.EMS.SOAP.ShUpdateResponse;
import com.MetaSwitch.EMS.SOAP.TMetaSwitchData;
import com.MetaSwitch.EMS.SOAP.TMetaSubscriberBaseInformation;
import com.MetaSwitch.EMS.SOAP.TUserData;

public class UpdatePIN
{
  private final static String USAGE = "Usage: UpdatePIN " +
                                      "<Directory number> <New PIN>";
  private static ShTypedUtilities utilities = new ShTypedUtilities();

  public static void main(String[] args)
  {
    final String servInd = "Meta_Subscriber_BaseInformation";

    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      // There are 2 mandatory parameters: 
      //  - The subscriber's DN
      //  - The new PIN to set
      //-----------------------------------------------------------------------
      if (args.length != 2)
      {
        throw new WrongParametersException("Wrong number of parameters.");
      }

      String dn = args[0];
      String newPin = args[1];

      //-----------------------------------------------------------------------
      // Connect to the Sh service and send in a Pull request.
      //-----------------------------------------------------------------------
      final ShService shService = new ShServiceStub();
      final ShPull shPullRequest = utilities.createPullRequest(dn,
                                                               0,
                                                               servInd,
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
      
      String originHost = utilities.ORIGIN_HOST + utilities.IGNORE_SEQUENCE_NUMBER;

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
      utilities.handleMetaSwitchException(e, USAGE);
    }
    catch (Exception e)
    {
      utilities.handleUnexpectedException(e, USAGE, false);
    }
  }
}