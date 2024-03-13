//-----------------------------------------------------------------------------
// AddMandatoryAccountCode
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
// Author: Alex Davies
//
// Sample application demonstrating the SOAP Sh interface to the MetaView
// Server.  This application sends in an Sh-Pull request to get the Mandatory
// Account Codes configured for a Business Group, and an Sh-Update request to
// add a new one.  It uses Microsoft .NET and the "typed" WSDL file and also
// demonstrates use of the sequence number.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------

using System;
using System.Text;

public class AddMandatoryAccountCode
{
  private static readonly String USAGE = "Usage: AddMandatoryAccountCode " +
                                         "<MetaSwitch name> <Business Group " +
                                         "name> <New account code> " +
                                         "[<Description>]";
  private static ShTypedUtilities utilities = new ShTypedUtilities();

  public static void Main(String[] args)
  {
    String metaSwitch;
    String businessGroup;
    String code;
    String description;

    int resultCode;
    tExtendedResult extendedResult;
    tUserData userData;

    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      //-----------------------------------------------------------------------
      getParameters(args,
                    out metaSwitch,
                    out businessGroup,
                    out code,
                    out description);

      String[] identifiers = new String[] { metaSwitch, businessGroup };
      String userIdentity = utilities.getUserIdentity(identifiers);

      //-----------------------------------------------------------------------
      // Connect to the Sh service, and retrieve and display the current
      // account codes.
      //-----------------------------------------------------------------------
      ShService shInstance = new ShService();

      resultCode = shInstance.ShPull(
                                    userIdentity,
                                    0,
                                    "Meta_BusinessGroup_MandatoryAccountCodes",
                                    ShUtilities.ORIGIN_HOST,
                                    out extendedResult,
                                    out userData);

      utilities.checkResultCode(resultCode, extendedResult, null);

      Console.WriteLine("Current Mandatory Account Codes:");

      tMetaSwitchData metaSwitchData = (tMetaSwitchData)userData.ShData
                                                                .RepositoryData
                                                                .ServiceData
                                                                .Item;
      tMeta_BusinessGroup_MandatoryAccountCodes macFieldGroup =
                (tMeta_BusinessGroup_MandatoryAccountCodes)metaSwitchData.Item;

      tValidAccountCodesAccountCode[] currentCodes =
                                               macFieldGroup.ValidAccountCodes;
      Console.WriteLine(tabulateCodes(currentCodes));

      //-----------------------------------------------------------------------
      // Update the userData with the incremented sequence number and add the
      // extra account code.
      //-----------------------------------------------------------------------
      utilities.incrementSequenceNumber(userData);

      tValidAccountCodesAccountCode newCode =
                                           new tValidAccountCodesAccountCode();
      newCode.Code = code;
      newCode.Description = description;

      tValidAccountCodesAccountCode[] newCodes =
                    new tValidAccountCodesAccountCode[currentCodes.Length + 1];
      System.Array.Copy(currentCodes, newCodes, currentCodes.Length);
      newCodes[newCodes.Length - 1] = newCode;

      macFieldGroup.ValidAccountCodes = newCodes;

      //-----------------------------------------------------------------------
      // Send it in as an Sh-Update request, and make sure it succeeded.
      //-----------------------------------------------------------------------
      resultCode = shInstance.ShUpdate(userIdentity,
                                       0,
                                       userData,
                                       ShUtilities.ORIGIN_HOST,
                                       out extendedResult);

      utilities.checkResultCode(resultCode, extendedResult, null);

      //-----------------------------------------------------------------------
      // Get and show the current account codes again to confirm they have
      // changed.
      //-----------------------------------------------------------------------
      resultCode = shInstance.ShPull(
                                    userIdentity,
                                    0,
                                    "Meta_BusinessGroup_MandatoryAccountCodes",
                                    ShUtilities.ORIGIN_HOST,
                                    out extendedResult,
                                    out userData);

      utilities.checkResultCode(resultCode, extendedResult, null);

      Console.WriteLine("New Mandatory Account Codes:");

      metaSwitchData = (tMetaSwitchData)userData.ShData
                                                .RepositoryData
                                                .ServiceData
                                                .Item;
      macFieldGroup =
                (tMeta_BusinessGroup_MandatoryAccountCodes)metaSwitchData.Item;
      currentCodes = macFieldGroup.ValidAccountCodes;
      Console.WriteLine(tabulateCodes(currentCodes));
    }
    catch (MetaSwitchShInterfaceException e)
    {
      Console.WriteLine(e.Message);
      Console.WriteLine(USAGE);
    }
    catch (Exception e)
    {
      Console.WriteLine("Unexpected error \"" + e.GetType().Name
                         + "\":\"" + e + "\" in retrieving data");
      Console.WriteLine(USAGE);
    }
  }

  /**
   * Parse the command line arguments and extract the necessary information.
   *
   * @param args        IN  The arguments provided at the command line.
   * @param metaSwitch
   *                    OUT The name of the MetaSwitch on which the BG resides.
   * @param businessGroup
   *                    OUT The name of the Business Group to update.
   * @param code        OUT The new code to create.
   * @param description
   *                    OUT The description of this new code.
   */
  private static void getParameters(String[] args,
                                    out String metaSwitch,
                                    out String businessGroup,
                                    out String code,
                                    out String description)
  {
    if ((args.Length < 3) || (args.Length > 4))
    {
      throw new WrongParametersException("Wrong number of parameters.");
    }

    metaSwitch = args[0];
    businessGroup = args[1];
    code = args[2];
    description = (args.Length == 4 ? args[3] : null);
  }

  /**
   * Produce a human-readable representation of the mandatory account codes.
   *
   * @returns           A string representation of the account codes.
   *
   * @param codes       A set of Mandatory Account Codes.
   */
  private static String tabulateCodes(tValidAccountCodesAccountCode[] codes)
  {
    StringBuilder tabulatedCodesBuilder = new StringBuilder();

    foreach (tValidAccountCodesAccountCode code in codes)
    {
      //-----------------------------------------------------------------------
      // Add this account code to the string representation of the Mandatory
      // Account Codes table.
      //-----------------------------------------------------------------------
      tabulatedCodesBuilder.Append(code.Code);
      if (code.Description != null)
      {
        //---------------------------------------------------------------------
        // There is a description, so add it to the table.
        //---------------------------------------------------------------------
        tabulatedCodesBuilder.Append(" ");
        tabulatedCodesBuilder.Append(code.Description);
      }
      tabulatedCodesBuilder.Append("\n");
    }

    return tabulatedCodesBuilder.ToString();
  }
}