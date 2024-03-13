//-----------------------------------------------------------------------------
// UpdatePIN
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
// Author: Alex Davies
//
// Sample application demonstrating the SOAP Sh interface to the MetaView
// Server.  This application sends in an Sh-Pull request to find a subscriber's
// current PIN, and then an Sh-Update request to change it to a new value.  It
// uses Microsoft .NET and the "typed" WSDL file, and also demonstrates
// bypassing the sequence number.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------

using System;

public class UpdatePIN
{
  private readonly static String USAGE = "Usage: UpdatePIN" +
                                         "<Directory number> <New PIN>";
  private static ShTypedUtilities utilities = new ShTypedUtilities();

  public static void Main(String[] args)
  {
    String dN;
    String pin;

    int resultCode;
    tExtendedResult extendedResult;
    tUserData userData;

    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      //-----------------------------------------------------------------------
      getParameters(args, out dN, out pin);

      //-----------------------------------------------------------------------
      // Connect to the Sh service and send in a Pull request.
      //-----------------------------------------------------------------------
      ShService shInstance = new ShService();

      resultCode = shInstance.ShPull(dN,
                                     0,
                                     "Meta_Subscriber_BaseInformation",
                                     ShUtilities.ORIGIN_HOST,
                                     out extendedResult,
                                     out userData);

      //-----------------------------------------------------------------------
      // Check that the request succeeded and extract and display the current
      // PIN.
      //-----------------------------------------------------------------------
      utilities.checkResultCode(resultCode, extendedResult, null);

      tMetaSwitchData metaSwitchData = (tMetaSwitchData)userData.ShData
                                                                .RepositoryData
                                                                .ServiceData
                                                                .Item;

      tMeta_Subscriber_BaseInformation baseInformation =
                         (tMeta_Subscriber_BaseInformation)metaSwitchData.Item;

      Console.WriteLine("Old PIN: " + baseInformation.PIN);

      //-----------------------------------------------------------------------
      // Update the user data with the new PIN, then send in the Update request
      // and check it succeeded.
      //-----------------------------------------------------------------------
      baseInformation.PIN = pin;

      resultCode = shInstance.ShUpdate(
                  dN,
                  0,
                  userData,
                  ShUtilities.ORIGIN_HOST + ShUtilities.IGNORE_SEQUENCE_NUMBER,
                  out extendedResult);

      utilities.checkResultCode(resultCode, extendedResult, null);

      Console.WriteLine("PIN successfully changed to " + pin);
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
   * Both parameters are required:
   * -  the subscriber's DN
   * -  the new PIN to set.
   *
   * @param args        The arguments provided at the command line.
   * @param dN          The directory number to update.
   * @param pin         The new PIN to use for this subscriber.
   */
  private static void getParameters(String[] args,
                                    out String dN,
                                    out String pin)
  {
    if (args.Length != 2)
    {
      throw new WrongParametersException("Wrong number of parameters");
    }

    dN = args[0];
    pin = args[1];
  }
}