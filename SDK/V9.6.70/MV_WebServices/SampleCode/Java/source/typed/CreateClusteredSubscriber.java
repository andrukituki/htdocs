//-----------------------------------------------------------------------------
// CreateClusteredSubscriber
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material.
//
// Sample application demonstrating the SOAP Sh interface to the MetaView
// Server.  This application sends in an Sh-Update request to create a new
// subscriber, and then sends an Sh-Pull request to fetch the new subscriber's
// alarm state to ensure it was correctly configured.  It uses Apache Axis2 and
// the "typed" WSDL file.  It also demonstrates how you can connect to a
// MetaView Server other than the one specified in the WSDL file.
//
// This application is only valid on MVSs in a clustered deployment.  It
// creates a subscriber, but doesn't specify which CFS to put it on.  The MVS
// will choose one from the cluster.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------

import com.MetaSwitch.EMS.SOAP.ShPull;
import com.MetaSwitch.EMS.SOAP.ShPullResponse;
import com.MetaSwitch.EMS.SOAP.ShUpdate;
import com.MetaSwitch.EMS.SOAP.ShUpdateResponse;
import com.MetaSwitch.EMS.SOAP.ShService;
import com.MetaSwitch.EMS.SOAP.ShServiceStub;
import com.MetaSwitch.EMS.SOAP.TMetaSubscriberBaseInformation;
import com.MetaSwitch.EMS.SOAP.TSwitchableDefaultString;
import com.MetaSwitch.EMS.SOAP.TMetaSubscriberBaseInformationSignalingType;
import com.MetaSwitch.EMS.SOAP.TMetaSubscriberAlarms;
import com.MetaSwitch.EMS.SOAP.TMetaSwitchData;
import com.MetaSwitch.EMS.SOAP.TUserData;
import com.MetaSwitch.EMS.SOAP.TServiceData;
import com.MetaSwitch.EMS.SOAP.TTransparentData;
import com.MetaSwitch.EMS.SOAP.TShData;
import com.MetaSwitch.EMS.SOAP.TTrueFalse;
import com.MetaSwitch.EMS.SOAP.TMetaSubscriberBaseInformationNumberStatus;

public class CreateClusteredSubscriber
{
  private final static String USAGE =
             "Usage: CreateClusteredSubscriber [-mvs <MetaView Server name>] " +
             " -dn <Directory number> [-businessgroup <Business group>]" +
             " [-subscribergroup <Subscriber Group>]" +
             " -preferredsite <Preferred Site>" +
             " [-profile <Persistent Profile>] [-username <SIP user name>]" +
             " -domain <SIP domain name> [-password <SIP password>]";

  private static ShTypedUtilities utilities = new ShTypedUtilities();

  public static void main(String[] args)
  {
    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      //-----------------------------------------------------------------------
      SubscriberInfo subscriberInfo = getParameters(args);

      String dn = subscriberInfo.dn;

      //-----------------------------------------------------------------------
      // Prepare to connect to the Sh service.
      //-----------------------------------------------------------------------
      ShService shService;

      if (subscriberInfo.mvsAddress != null)
      {
        shService = new ShServiceStub(subscriberInfo.mvsAddress);
      }
      else
      {
        shService = new ShServiceStub();
      }

      //-----------------------------------------------------------------------
      // Put together the user data and set the required fields.  In addition
      // to the parameters provided, set:
      // -  SignalingType to "SIP"
      // -  NumberStatus to "Normal"
      // -  Locale to "English (US)"
      // There are constants within the auto-generated classes that would be
      // more efficient to use; however, their string values, and fromValue
      // methods are used here for readability.
      //-----------------------------------------------------------------------
      TMetaSubscriberBaseInformation baseInformation =
                                          new TMetaSubscriberBaseInformation();
      baseInformation.setSignalingType(
                 TMetaSubscriberBaseInformationSignalingType.fromValue("SIP"));
      baseInformation.setNumberStatus(
               TMetaSubscriberBaseInformationNumberStatus.fromValue("Normal"));

      if (subscriberInfo.businessGroupName == null)
      {
        baseInformation.setLocale("English (US)");
        baseInformation.setSubscriberGroup(subscriberInfo.subscriberGroup);
      }
      else
      {
        baseInformation.setBusinessGroupName(subscriberInfo.businessGroupName);
        TSwitchableDefaultString subGp;
        String requestedSubscriberGroup = subscriberInfo.subscriberGroup;

        if (requestedSubscriberGroup == null)
        {
          subGp = new TSwitchableDefaultString();
          subGp.setUseDefault(TTrueFalse.TRUE);
          subGp.setValue(null);
          subGp.setDefault(null);
        }
        else
        {
          subGp = new TSwitchableDefaultString();
          subGp.setUseDefault(TTrueFalse.FALSE);
          subGp.setValue(requestedSubscriberGroup);
          subGp.setDefault(null);
        }

        baseInformation.setSubscriberGroupBusLine(subGp);
      }

      baseInformation.setPreferredSite(subscriberInfo.preferredSite);
      baseInformation.setPersistentProfile(subscriberInfo.persistentProfile);
      baseInformation.setSIPDomainName(subscriberInfo.sipDomainName);

      if (subscriberInfo.sipUserName == null)
      {
        baseInformation.setUseDNForIdentification(TTrueFalse.TRUE);
      }
      else
      {
        baseInformation.setUseDNForIdentification(TTrueFalse.FALSE);
        baseInformation.setSIPUserName(subscriberInfo.sipUserName);
      }

      if (subscriberInfo.sipPassword == null)
      {
        baseInformation.setSIPAuthenticationRequired(TTrueFalse.FALSE);
      }
      else
      {
        baseInformation.setSIPAuthenticationRequired(TTrueFalse.TRUE);
        baseInformation.setNewSIPPassword(subscriberInfo.sipPassword);
        baseInformation.setConfirmNewSIPPassword(subscriberInfo.sipPassword);
      }

      //-----------------------------------------------------------------------
      // Now that we have created the base information from the supplied
      // parameters, add it to the user data.
      //-----------------------------------------------------------------------
      TMetaSwitchData metaSwitchData = new TMetaSwitchData();
      metaSwitchData.setMetaSubscriberBaseInformation(baseInformation);

      TServiceData serviceData = new TServiceData();
      serviceData.setMetaSwitchData(metaSwitchData);

      TTransparentData repositoryData = new TTransparentData();
      repositoryData.setServiceIndication("Meta_Subscriber_BaseInformation");
      repositoryData.setSequenceNumber(0);
      repositoryData.setServiceData(serviceData);

      TShData shData = new TShData();
      shData.setRepositoryData(repositoryData);

      TUserData userData = new TUserData();
      userData.setShData(shData);

      //-----------------------------------------------------------------------
      // Send it in as an ShUpdate request, and make sure it succeeded.
      //-----------------------------------------------------------------------
      ShUpdate shUpdateRequest = utilities.createUpdateRequest(
                                                        dn,
                                                        0,
                                                        userData,
                                                        utilities.ORIGIN_HOST);
                                                          
      ShUpdateResponse shUpdateResponse = shService.shUpdate(shUpdateRequest);

      utilities.checkResultCode(shUpdateResponse.getResultCode(),
                                shUpdateResponse.getExtendedResult(),
                                userData);

      System.out.println("Subscriber successfully created.");

      //-----------------------------------------------------------------------
      // Send an ShPull to get the new subscriber's alarm state.  Extract and
      // print the alarm state.
      //-----------------------------------------------------------------------
      ShPull shPullRequest = utilities.createPullRequest(dn,
                                                         0,
                                                         "Meta_Subscriber_Alarms",
                                                         utilities.ORIGIN_HOST);

      ShPullResponse shPullResponse = shService.shPull(shPullRequest);

      utilities.checkResultCode(shPullResponse.getResultCode(),
                                shPullResponse.getExtendedResult(),
                                shPullResponse.getUserData());

      TMetaSubscriberAlarms  alarms = shPullResponse.getUserData()
                                                    .getShData()
                                                    .getRepositoryData()
                                                    .getServiceData()
                                                    .getMetaSwitchData()
                                                    .getMetaSubscriberAlarms();

      System.out.println("Alarm state: \"" + alarms.getAlarmState() + "\"");
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
   * See the associated CreateClusteredSubscriber.bat script for parameter details.
   *
   * @returns          Information about the subscriber to be created.
   *
   * @param args        IN  The arguments provided at the command line.
   *
   * @exception WrongParametersException
   */
  private static SubscriberInfo getParameters(String[] args)
    throws WrongParametersException
  {
    SubscriberInfo fields = new SubscriberInfo();
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
          case "-mvs":
          {
            fields.mvsAddress = "http://" + arg + ":8080/services/ShService";
          }
          break;
          
          case "-dn":
          {
            fields.dn = arg;
          }
          break;
          
          case "-subscribergroup":
          {
            fields.subscriberGroup = arg;
          }
          break;
          
          case "-preferredsite":
          {
            fields.preferredSite = arg;
          }
          break;
          
          case "-profile":
          {
            fields.persistentProfile = arg;
          }
          break;
          
          case "-username":
          {
            fields.sipUserName = arg;
          }
          break;
          
          case "-domain":
          {
            fields.sipDomainName = arg;
          }
          break;
          
          case "-password":
          {
            fields.sipPassword = arg;
          }
          break;
          
          case "-businessgroup":
          {
            fields.businessGroupName = arg;
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

    if (fields.dn == null)
    {
      throw new WrongParametersException("No directory number was provided");
    }

    if (fields.businessGroupName == null && fields.subscriberGroup == null)
    {
      throw new WrongParametersException("No Subscriber Group was provided " + 
                                         "and subscriber is an individual line");
    }

    if (fields.preferredSite == null)
    {
      throw new WrongParametersException("No Preferred Site was provided");
    }

    return fields;
  }

  //---------------------------------------------------------------------------
  // INNER CLASS: SubscriberInfo
  //
  // Holds information about the subscriber to be created.
  //---------------------------------------------------------------------------
  static class SubscriberInfo
  {
    String mvsAddress = null;
    String dn = null;
    String metaSwitchName = null;
    String businessGroupName = null;
    String subscriberGroup = null;
    String preferredSite = null;
    String persistentProfile = "None";
    String sipUserName = null;
    String sipDomainName = null;
    String sipPassword = null;
  }
}
