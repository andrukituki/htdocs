//-----------------------------------------------------------------------------
// EnumerateSubscribers
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material.
//
// Sample application demonstrating the Network Wide Web Services API.
//
// This application sends in an Sh-Pull request to fetch a list of subscribers
// underneath a specified Business Group. If a department is specified, it will
// make a request filtered by the department.  It uses Apache Axis2 and the
// "typed" WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------
import java.util.List;

import com.MetaSwitch.EMS.SOAP.TMetaBusinessGroupChildrenListSubscriber;

import com.MetaSwitch.SRB.SOAP.ShPull;
import com.MetaSwitch.SRB.SOAP.ShPullResponse;
import com.MetaSwitch.SRB.SOAP.ShService;
import com.MetaSwitch.SRB.SOAP.ShServiceStub;
import com.MetaSwitch.SRB.SOAP.TUserData;

public class EnumerateSubscribers
{
  private final static String USAGE = "Usage: EnumerateSubscribers " +
                                      "-nwsapusername <NWSAP authentication username> " +
                                      "-nwsappassword <NWSAP authentication password> " +
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
      ShServiceStub shService = new ShServiceStub();

      utilities.authenticate(shService,
                             subscriberListInfo.nwsapUsername.toString(),
                             subscriberListInfo.nwsapPassword.toString());

      //-----------------------------------------------------------------------
      // Make the request to the MetaView Server.
      //-----------------------------------------------------------------------
      
      ShPull shPullRequest = utilities.createPullRequest(
                                  userIdentity,
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
      List<TMetaBusinessGroupChildrenListSubscriber.Subscriber> subscribers =
                    shPullResponse.getUserData()
                                  .getShData()
                                  .getRepositoryData()
                                  .get(0)
                                  .getServiceData()
                                  .getMetaSwitchData()
                                  .getMetaBusinessGroupChildrenListSubscriber()
                                  .getSubscriber();

      System.out.println("The MetaView Server returned "
                         + subscribers.size()
                         + " subscribers.");

      for(TMetaBusinessGroupChildrenListSubscriber.Subscriber eachSubscriber
                                                                 : subscribers)
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
        switch (currentArg)
        {
          case "-nwsapusername":
          {
            fields.nwsapUsername.append(arg);
          }
          break;

          case "-nwsappassword":
          {
            fields.nwsapPassword.append(arg);
          }
          break;

          case "-metaswitch":
          {
            fields.metaSwitchName.append(arg);
          }
          break;

          case "-businessgroup":
          {
            fields.businessGroupName.append(arg);
          }
          break;

          case "-department":
          {
            fields.department.append(arg);
          }
          break;
          
          default:
          {
            throw new WrongParametersException("Unrecognised argument: " +
                                               currentArg);
          }
        }
      }
    }

    if (fields.nwsapUsername.toString() == null)
    {
      throw new WrongParametersException("No NWSAP username was provided");
    }

    if (fields.nwsapPassword.toString() == null)
    {
      throw new WrongParametersException("No NWSAP password was provided");
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
    Parameter nwsapUsername = new Parameter(null);
    Parameter nwsapPassword = new Parameter(null);
    Parameter dn = new Parameter(null);
    Parameter metaSwitchName = new Parameter(null);
    Parameter businessGroupName = new Parameter(null);
    Parameter department = new Parameter(null);
  }
}
