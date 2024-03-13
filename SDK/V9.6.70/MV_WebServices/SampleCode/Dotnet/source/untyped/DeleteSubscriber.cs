//-----------------------------------------------------------------------------
// DeleteSubscriber
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
// Author: Alex Davies
//
// Sample application demonstrating the SOAP Sh interface to the MetaView
// Server.  This application uses Sh-Pull to get a subscriber's current base
// settings, and then sends an Sh-Update with an instruction to delete the
// subscriber.  It uses Microsoft .NET and the "untyped" WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------

using System;
using System.Xml;

public class DeleteSubscriber
{
  private static readonly String USAGE = "Usage: DeleteSubscriber " +
                                         "<Directory number>";
  private static ShUntypedUtilities utilities = new ShUntypedUtilities();

  public static void Main(String[] args)
  {
    String dN;

    int resultCode;
    tExtendedResult extendedResult;
    XmlElement userData;

    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      //-----------------------------------------------------------------------
      getParameters(args, out dN);

      //-----------------------------------------------------------------------
      // Connect to the Sh service and send in a Pull request.  Check that it
      // succeeds.
      //-----------------------------------------------------------------------
      ShService shInstance = new ShService();

      resultCode = shInstance.ShPull(dN,
                                     0,
                                     "Meta_Subscriber_BaseInformation",
                                     ShUtilities.ORIGIN_HOST,
                                     out extendedResult,
                                     out userData);

      utilities.checkResultCode(resultCode, extendedResult, userData);

      //-----------------------------------------------------------------------
      // Update the user data:
      // -  Update the sequence number.
      // -  Remove all the fields.
      // -  Add the action attribute to the field group element.
      //-----------------------------------------------------------------------
      utilities.incrementSequenceNumber(userData);

      XmlElement fieldGroup =
                          (XmlElement)utilities.getFieldGroupElement(userData);

      fieldGroup.RemoveAll();
      XmlAttribute action = fieldGroup.SetAttributeNode("Action", "");
      action.Value = "delete";

      //-----------------------------------------------------------------------
      // Make the update request and check it succeeds.
      //-----------------------------------------------------------------------
      resultCode = shInstance.ShUpdate(dN,
                                       0,
                                       userData,
                                       ShUtilities.ORIGIN_HOST,
                                       out extendedResult);

      utilities.checkResultCode(resultCode, extendedResult, userData);

      Console.WriteLine("Subscriber " + dN + " successfully deleted.");
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
   * The only argument is the subscriber's directory number, which is required.
   *
   * @param args        IN  The arguments provided at the command line.
   * @param dN          OUT The directory number of the subscriber to delete.
   */
  private static void getParameters(String[] args, out String dN)
  {
    if (args.Length != 1)
    {
      throw new WrongParametersException("Wrong number of parameters.");
    }

    dN = args[0];
  }
}
