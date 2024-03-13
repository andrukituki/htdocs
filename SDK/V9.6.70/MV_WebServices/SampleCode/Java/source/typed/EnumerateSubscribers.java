//-----------------------------------------------------------------------------
// EnumerateSubscribers
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material.
//
// Sample application demonstrating the SOAP Sh interface to the MetaView
// Server.  This application sends in an Sh-Pull request to fetch a list of
// subscribers underneath a specified Business Group. If a department is
// specified, it will make a request filtered by the department.  It uses
// Apache Axis and the "typed" WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------

import com.MetaSwitch.EMS.SOAP.ShPull;
import com.MetaSwitch.EMS.SOAP.ShPullResponse;
import com.MetaSwitch.EMS.SOAP.ShService;
import com.MetaSwitch.EMS.SOAP.ShServiceStub;
import com.MetaSwitch.EMS.SOAP.TUserData;
import com.MetaSwitch.EMS.SOAP.TMetaBusinessGroupChildrenListSubscriber;

public class EnumerateSubscribers
{
  private final static String USAGE = "Usage: EnumerateSubscribers " +
                                      "-metaswitch <MetaSwitch name> " +
                                      "-businessgroup <Business Group name> " +
                                      "[-department <department name>]";

  private static ShTypedUtilities utilities = new ShTypedUtilities();

  public static void main(String[] args)
  {
    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      //-----------------------------------------------------------------------
      SubscriberListInfo subscriberListInfo = getParameters(args);

      String[] identifiers = new String[]
                             {subscriberListInfo.metaSwitchName.toString(),
                              subscriberListInfo.businessGroupName.toString()};

      String userIdentity = utilities.getUserIdentity(identifiers);

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
      ShService shService = new ShServiceStub();

      //-----------------------------------------------------------------------
      // Make the request to the MetaView Server.
      //-----------------------------------------------------------------------

      ShPull shPullRequest = utilities.createPullRequest(userIdentity,
                                                         0,
                                                         "Meta_BusinessGroup_ChildrenList_Subscriber",
                                                         utilities.ORIGIN_HOST);
      ShPullResponse shPullResponse = shService.shPull(shPullRequest);

      utilities.checkResultCode(shPullResponse.getResultCode(),
                                shPullResponse.getExtendedResult(),
                                shPullResponse.getUserData());

      //-----------------------------------------------------------------------
      // Display the list of subscribers.
      //-----------------------------------------------------------------------
      TMetaBusinessGroupChildrenListSubscriber subscribers =
                   shPullResponse.getUserData()
                                 .getShData()
                                 .getRepositoryData()
                                 .getServiceData()
                                 .getMetaSwitchData()
                                 .getMetaBusinessGroupChildrenListSubscriber();

      System.out.println("The MetaView Server returned "
                         + subscribers.getSubscriber().size()
                         + " subscribers.");

      for(TMetaBusinessGroupChildrenListSubscriber.Subscriber eachSubscriber
                                                                 : subscribers.getSubscriber())
      {
        //---------------------------------------------------------------------
        // Display this subscriber.
        //---------------------------------------------------------------------
        String subscriberDetails = eachSubscriber.getSubscriberType()
                                   + " "
                                   + eachSubscriber.getDirectoryNumber();

        if (eachSubscriber.getIntercomDialingCode() != null)
        {
          //-------------------------------------------------------------------
          // The subscriber has an intercom code, so display that too.
          //-------------------------------------------------------------------
          subscriberDetails += " ("
                               + eachSubscriber.getIntercomDialingCode()
                               + ")";
        }

        System.out.println(subscriberDetails);
      }
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
   * Parse the command line arguments and extract the necessary information.
   * See the associated EnumerateSubscribers.bat script for parameter details.
   *
   * @returns Information about the Business Group, and optionally department,
   *          under which subscribers should be listed.
   *
   * @param args
   *          IN The arguments provided at the command line.
   *
   * @exception WrongParametersException
   */
  private static SubscriberListInfo getParameters(String[] args)
    throws WrongParametersException
  {
    SubscriberListInfo fields = new SubscriberListInfo();
    String currentArg = "";
    for (String arg : args)
    {
      //-----------------------------------------------------------------------
      // Parse each parameter one by one (ignores anything before the first
      // flag).
      //-----------------------------------------------------------------------
      if (arg.startsWith("-"))
      {
        currentArg = arg.toLowerCase();
      }
      else
      {
        if (currentArg.equals("-metaswitch"))
        {
          fields.metaSwitchName.append(arg);
        }
        else if(currentArg.equals("-businessgroup"))
        {
          fields.businessGroupName.append(arg);
        }
        else if(currentArg.equals("-department"))
        {
          fields.department.append(arg);
        }
        else
        {
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
  static class SubscriberListInfo
  {
    Parameter metaSwitchName = new Parameter(null);
    Parameter businessGroupName = new Parameter(null);
    Parameter department = new Parameter(null);
  }
}
