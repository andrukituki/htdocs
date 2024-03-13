//-----------------------------------------------------------------------------
// AddMandatoryAccountCode
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material.
//
// Sample application demonstrating the Network Wide Web Services API.
//
// This application sends in an Sh-Pull request to get the Mandatory Account
// Codes configured for a Business Group, and an Sh-Update request to add a new
// one.  It uses Apache Axis2 and the "typed" WSDL file and also demonstrates
// use of the sequence number.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------
import java.util.List;

import com.MetaSwitch.EMS.SOAP.TValidAccountCodes;
import com.MetaSwitch.EMS.SOAP.TMetaBusinessGroupMandatoryAccountCodes;

import com.MetaSwitch.SRB.SOAP.ShService;
import com.MetaSwitch.SRB.SOAP.ShServiceStub;
import com.MetaSwitch.SRB.SOAP.ShPull;
import com.MetaSwitch.SRB.SOAP.ShPullResponse;
import com.MetaSwitch.SRB.SOAP.ShUpdate;
import com.MetaSwitch.SRB.SOAP.ShUpdateResponse;
import com.MetaSwitch.SRB.SOAP.TUserData;


public class AddMandatoryAccountCode
{
  private final static String USAGE = "Usage: AddMandatoryAccountCode " +
                                      "<NWSAP username> " +
                                      "<NWSAP password> " +
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

      if ((args.length < 5) || (args.length > 6))
      {
        throw new WrongParametersException("Wrong number of parameters.");
      }

      String nwsapUsername = args[0];
      String nwsapPassword = args[1];
      String metaSwitchName = args[2];
      String businessGroup = args[3];
      String accountCode = args[4];
      String description = (args.length == 6 ? args[5] : null);
      
      String[] identifiers = new String[] { metaSwitchName, businessGroup};
      String userIdentity = utilities.getUserIdentity(identifiers);

      //-----------------------------------------------------------------------
      // Connect to the Sh service, and retrieve and display the current
      // account codes.
      //-----------------------------------------------------------------------
      ShServiceStub shService = new ShServiceStub();

      utilities.authenticate(shService,
                             nwsapUsername,
                             nwsapPassword);

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
                                  .get(0)
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
                                   .get(0)
                                   .getServiceData()
                                   .getMetaSwitchData()
                                   .getMetaBusinessGroupMandatoryAccountCodes()
                                   .getValidAccountCodes()
                                   .getAccountCode();

      System.out.println(tabulateCodes(currentCodes));
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
