//-----------------------------------------------------------------------------
// CreateSubscriber
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
// Author: Alex Davies
//
// Sample application demonstrating the SOAP Sh interface to the MetaView
// Server.  This application sends in an Sh-Pull request to fetch a list of
// subscribers underneath a specified Business Group. If a department is
// specified, it will make a request filtered by the department.  It uses
// Microsoft .NET and the "typed" WSDL file.  It also demonstrates how you can
// connect to a MetaView Server other than the one specified in the WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------

using System;

public class EnumerateSubscribers
{
  private readonly static string USAGE = "Usage: EnumerateSubscribers " +
                                      "-metaswitch <MetaSwitch name> " +
                                      "-businessgroup <Business Group name> " +
                                      "[-department <department name>]";

  private static ShTypedUtilities utilities = new ShTypedUtilities();

  public static void Main(string[] args)
  {
    int resultCode;
    tExtendedResult extendedResult;
    tUserData userData;

    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      //-----------------------------------------------------------------------
      SubscriberListInfo subscriberListInfo = getParameters(args);

      string[] identifiers = new string[]{
							  subscriberListInfo.metaSwitchName.toString(),
                              subscriberListInfo.businessGroupName.toString()};

      string userIdentity = utilities.getUserIdentity(identifiers);

      if (subscriberListInfo.department.toString() != null)
      {
        //---------------------------------------------------------------------
        // A department has been specified. Add a department filter to the
        // UserIdentity of the request.
        //---------------------------------------------------------------------
        userIdentity += "?filterDepartment="
                        + subscriberListInfo.department.toString();
      }

      //-----------------------------------------------------------------------
      // Prepare to connect to the Sh service.
      //-----------------------------------------------------------------------
      ShService shInstance = new ShService();

      //-----------------------------------------------------------------------
      // Make the request to the MetaView Server.
      //-----------------------------------------------------------------------
      resultCode = shInstance.ShPull(
								  userIdentity,
								  0,
								  "Meta_BusinessGroup_ChildrenList_Subscriber",
								  ShUtilities.ORIGIN_HOST,
								  out extendedResult,
							      out userData);

      utilities.checkResultCode(resultCode, extendedResult, null);

      //-----------------------------------------------------------------------
      // Display the list of subscribers.
      //-----------------------------------------------------------------------
      tMetaSwitchData metaSwitchData = (tMetaSwitchData)userData.ShData
                                                                .RepositoryData
                                                                .ServiceData
                                                                .Item;
      tMeta_BusinessGroup_ChildrenList_Subscriber itemGroup =
              (tMeta_BusinessGroup_ChildrenList_Subscriber)metaSwitchData.Item;

      tMeta_BusinessGroup_ChildrenList_SubscriberSubscriber[] subscribers =
													      itemGroup.Subscriber;

      Console.WriteLine("The MetaView Server returned "
                         + subscribers.Length
                         + " subscribers.");

      foreach (tMeta_BusinessGroup_ChildrenList_SubscriberSubscriber
												 eachSubscriber in subscribers)
      {
        //---------------------------------------------------------------------
        // Display this subscriber.
        //---------------------------------------------------------------------
        String subscriberDetails = eachSubscriber.SubscriberType.ToString()
                                   + " "
                                   + eachSubscriber.DirectoryNumber;

        if (eachSubscriber.IntercomDialingCode != null)
        {
          //-------------------------------------------------------------------
          // The subscriber has an intercom code, so display that too.
          //-------------------------------------------------------------------
          subscriberDetails += " ("
                               + eachSubscriber.IntercomDialingCode
                               + ")";
        }

        Console.WriteLine(subscriberDetails);
      }
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
   * See the associated EnumerateSubscribers.bat script for parameter details.
   *
   * @returns           Information about the Business Group, and optionally
   *					department, under which subscribers should be listed.
   *
   * @param args        The arguments provided at the command line.
   */
  private static SubscriberListInfo getParameters(string[] args)
  {
    SubscriberListInfo fields = new SubscriberListInfo();
    string currentArg = "";

    foreach (string arg in args)
    {
      //-----------------------------------------------------------------------
      // Parse each parameter one by one (ignores anything before the first
      // flag).
      //-----------------------------------------------------------------------
      if (arg.StartsWith("-"))
      {
        currentArg = arg.ToLower();
      }
      else
      {
        switch(currentArg)
        {
          case "-metaswitch":
            fields.metaSwitchName.append(arg);
            break;
          case "-businessgroup":
            fields.businessGroupName.append(arg);
            break;
          case "-department":
            fields.department.append(arg);
            break;
          default:
            throw new WrongParametersException("Unrecognised argument: " +
                                               currentArg);
        }
      }
    }

    if (fields.metaSwitchName.toString() == null)
    {
      throw new WrongParametersException("No MetaSwitch name was provided");
    }

    if (fields.businessGroupName.toString() == null)
    {
      throw new WrongParametersException(
										"No Business Group name was provided");
    }

    return fields;
  }

  //---------------------------------------------------------------------------
  // INNER CLASS: SubscriberListInfo
  //
  // Holds information about the Business Group, and optionally department,
  // under which subscribers should be listed.
  //---------------------------------------------------------------------------
  public class SubscriberListInfo
  {
    public Parameter metaSwitchName = new Parameter(null);
    public Parameter businessGroupName = new Parameter(null);
    public Parameter department = new Parameter(null);
  }
}