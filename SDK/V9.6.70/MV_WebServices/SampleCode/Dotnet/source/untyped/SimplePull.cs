//-----------------------------------------------------------------------------
// SimplePull
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
// Author: Alex Davies
//
// Sample application demonstrating the SOAP Sh interface to the MetaView
// Server.  This application sends in an Sh-Pull request for part of an object's
// configuration and prints out the result.  It uses Microsoft .NET and the
// "untyped" WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------

using System;
using System.Xml;

public class SimplePull
{
  private static readonly string USAGE = "Usage: SimplePull " +
                                         "<User identity> [<service" +
                                         " indication(s)>]";
  private static ShUntypedUtilities utilities = new ShUntypedUtilities();

  public static void Main(string[] args)
  {
    String servInd;
    String userIdentity;

    int resultCode;
    tExtendedResult extendedResult;
    XmlElement userData;

    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      //-----------------------------------------------------------------------
      getParameters(args, out userIdentity, out servInd);

      //-----------------------------------------------------------------------
      // Connect to the Sh service and send in a Pull request.
      //-----------------------------------------------------------------------
      ShService shInstance = new ShService();

      resultCode = shInstance.ShPull(userIdentity,
                                     0,
                                     servInd,
                                     ShUtilities.ORIGIN_HOST,
                                     out extendedResult,
                                     out userData);

      //-----------------------------------------------------------------------
      // Check whether the request succeeded and display the value of each of
      // the fields.
      //-----------------------------------------------------------------------
      utilities.checkResultCode(resultCode, extendedResult, userData);

      utilities.displayFields(userData);
    }
    catch (MetaSwitchShInterfaceException e)
    {
      Console.WriteLine(e.Message);
      Console.WriteLine(USAGE);
    }
    catch (Exception e)
    {
      Console.WriteLine("Unexpected error \"" + e.GetType().Name +
                        "\":\"" + e + "\" in retrieving data");
      Console.WriteLine(USAGE);
    }
  }

  /**
   * Parse the command line arguments and extract the necessary information.
   *
   * The first parameter is the object's user identity.  The second is the
   * service indication and is optional; it defaults to
   * "Meta_Subscriber_BaseInformation" if not specified.
   *
   * @param args        IN  The arguments provided at the command line.
   * @param userIdentity
   *                    OUT The user identity of the object to request.
   * @param servInd     OUT The service indication to request.
   */
  private static void getParameters(string[] args,
                                    out string userIdentity,
                                    out string servInd)
  {
    if ((args.Length < 1) || (args.Length > 2))
    {
      throw new WrongParametersException("Wrong number of parameters");
    }

    userIdentity = args[0];

    if (args.Length == 2)
    {
      servInd = args[1];
    }
    else
    {
      servInd = "Meta_Subscriber_BaseInformation";
    }
  }
}