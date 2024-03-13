//-----------------------------------------------------------------------------
// AddMandatoryAccountCode
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material.
//
// Sample application demonstrating the SOAP Sh interface to the MetaView
// Server.  This application sends in an Sh-Pull request to get the Mandatory
// Account Codes configured for a Business Group, and an Sh-Update request to
// add a new one.  It uses Apache Axis2 and the "typed" WSDL file and also
// demonstrates use of the sequence number.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------
import java.util.List;

import com.MetaSwitch.EMS.SOAP.ShPull;
import com.MetaSwitch.EMS.SOAP.ShPullResponse;
import com.MetaSwitch.EMS.SOAP.ShUpdate;
import com.MetaSwitch.EMS.SOAP.ShUpdateResponse;
import com.MetaSwitch.EMS.SOAP.ShService;
import com.MetaSwitch.EMS.SOAP.ShServiceStub;
import com.MetaSwitch.EMS.SOAP.TMetaBusinessGroupMandatoryAccountCodes;
import com.MetaSwitch.EMS.SOAP.TValidAccountCodes;
import com.MetaSwitch.EMS.SOAP.TUserData;

public class AddMandatoryAccountCode
{
  private final static String USAGE = "Usage: AddMandatoryAccountCode " +
                                      "<MetaSwitch name> <Business Group " +
                                      "name> <New account code> " +
                                      "[<Description>]";

  private static ShTypedUtilities utilities = new ShTypedUtilities();

  public static void main(String[] args)
  {
    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      //-----------------------------------------------------------------------
      if ((args.length < 3) || (args.length > 4))
      {
        throw new WrongParametersException("Wrong number of parameters.");
      }

      String metaSwitchName = args[0];
      String businessGroup = args[1];
      String accountCode = args[2];
      String description = (args.length == 4 ? args[3] : null);
      
      String[] identifiers = new String[] { metaSwitchName, businessGroup };
      String userIdentity = utilities.getUserIdentity(identifiers);

      //-----------------------------------------------------------------------
      // Connect to the Sh service, and retrieve and display the current
      // account codes.
      //-----------------------------------------------------------------------
      ShService shService = new ShServiceStub();

      ShPull shPullRequest = utilities.createPullRequest(
                                    userIdentity, 
                                    0,
                                    "Meta_BusinessGroup_MandatoryAccountCodes",
                                    utilities.ORIGIN_HOST);

      ShPullResponse shPullResponse = shService.shPull(shPullRequest);

      utilities.checkResultCode(shPullResponse.getResultCode(),
                                shPullResponse.getExtendedResult(),
                                shPullResponse.getUserData());

      TUserData userData = shPullResponse.getUserData();

      System.out.println("Current Mandatory Account Codes:");

      TMetaBusinessGroupMandatoryAccountCodes macFieldGroup =
                          userData.getShData()
                                  .getRepositoryData()
                                  .getServiceData()
                                  .getMetaSwitchData()
                                  .getMetaBusinessGroupMandatoryAccountCodes();

      List<TValidAccountCodes.AccountCode> currentCodes =
                         macFieldGroup.getValidAccountCodes().getAccountCode();

      System.out.println(tabulateCodes(currentCodes));

      //-----------------------------------------------------------------------
      // Update the userData with the incremented sequence number and add the
      // extra account code.
      //-----------------------------------------------------------------------
      utilities.incrementSequenceNumber(userData);

      TValidAccountCodes.AccountCode newAccountCode =
                                          new TValidAccountCodes.AccountCode();
      newAccountCode.setCode(accountCode);
      newAccountCode.setDescription(description);

      currentCodes.add(newAccountCode);
      
      TValidAccountCodes newCodes = macFieldGroup.getValidAccountCodes();

      macFieldGroup.setValidAccountCodes(newCodes);

      //-----------------------------------------------------------------------
      // Send it in as an Sh-Update request, and make sure it succeeded.
      //-----------------------------------------------------------------------
      ShUpdate shUpdateRequest = utilities.createUpdateRequest(
                                                        userIdentity,
                                                        0,
                                                        userData,
                                                        utilities.ORIGIN_HOST);
      ShUpdateResponse shUpdateResponse = shService.shUpdate(shUpdateRequest);

      utilities.checkResultCode(shUpdateResponse.getResultCode(),
                                shUpdateResponse.getExtendedResult(),
                                userData);

      //-----------------------------------------------------------------------
      // Get and show the current account codes again to confirm they have
      // changed.
      //-----------------------------------------------------------------------
      shPullResponse = shService.shPull(shPullRequest);

      utilities.checkResultCode(shPullResponse.getResultCode(),
                                shPullResponse.getExtendedResult(),
                                shPullResponse.getUserData());

      System.out.println("New Mandatory Account Codes:");

      currentCodes = shPullResponse.getUserData()
                                   .getShData()
                                   .getRepositoryData()
                                   .getServiceData()
                                   .getMetaSwitchData()
                                   .getMetaBusinessGroupMandatoryAccountCodes()
                                   .getValidAccountCodes()
                                   .getAccountCode();

      System.out.println(tabulateCodes(currentCodes));
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

  /**
   * Produce a human-readable representation of the mandatory account codes.
   *
   * @returns           A string representation of the account codes.
   *
   * @param codes       A set of Mandatory Account Codes.
   */
  private static String tabulateCodes(List<TValidAccountCodes.AccountCode> codes)
  {
    StringBuilder tabulatedCodesBuilder = new StringBuilder();

    for (TValidAccountCodes.AccountCode code : codes)
    {
      //-----------------------------------------------------------------------
      // Add this account code to the string representation of the Mandatory
      // Account Codes table.
      //-----------------------------------------------------------------------
      tabulatedCodesBuilder.append(code.getCode());
      
      if (code.getDescription() != null)
      {
        //---------------------------------------------------------------------
        // There is a description, so add it to the table.
        //---------------------------------------------------------------------
        tabulatedCodesBuilder.append(" ");
        tabulatedCodesBuilder.append(code.getDescription());
      }
      
      tabulatedCodesBuilder.append("\n");
    }

    return tabulatedCodesBuilder.toString();
  }
}
