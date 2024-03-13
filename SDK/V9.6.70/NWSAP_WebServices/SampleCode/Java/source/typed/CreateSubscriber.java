//-----------------------------------------------------------------------------
// CreateSubscriber
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material.
//
// Sample application demonstrating the Network Wide Web Services API.
//
// This application sends in an Sh-Update request to create a new subscriber,
// and then sends an Sh-Pull request to fetch the new subscriber's alarm state
// to ensure it was correctly configured.  It uses Apache Axis2 and the "typed"
// WSDL file.  It also demonstrates how you can connect to a MetaView Server
// other than the one specified in the WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------
import com.MetaSwitch.EMS.SOAP.TMetaSwitchData;
import com.MetaSwitch.EMS.SOAP.TMetaSubscriberBaseInformation;
import com.MetaSwitch.EMS.SOAP.TMetaSubscriberBaseInformationNumberStatus;
import com.MetaSwitch.EMS.SOAP.TMetaSubscriberBaseInformationSignalingType;
import com.MetaSwitch.EMS.SOAP.TMetaSubscriberAlarms;
import com.MetaSwitch.EMS.SOAP.TTrueFalse;
import com.MetaSwitch.EMS.SOAP.TSwitchableDefaultString;

import com.MetaSwitch.SRB.SOAP.ShService;
import com.MetaSwitch.SRB.SOAP.ShServiceStub;
import com.MetaSwitch.SRB.SOAP.ShPull;
import com.MetaSwitch.SRB.SOAP.ShPullResponse;
import com.MetaSwitch.SRB.SOAP.ShUpdate;
import com.MetaSwitch.SRB.SOAP.ShUpdateResponse;
import com.MetaSwitch.SRB.SOAP.TServiceData;
import com.MetaSwitch.SRB.SOAP.TShData;
import com.MetaSwitch.SRB.SOAP.TUserData;
import com.MetaSwitch.SRB.SOAP.TTransparentData;

public class CreateSubscriber
{
  private final static String USAGE = "Usage: CreateSubscriber " +
                                      "-nwsapusername " +
                                      "<NWSAP authentication username> " +
                                      "-nwsappassword " +
                                      "<NWSAP authentication password> [-mvs " +
                                      "<MetaView Server name>] -dn <Directory " +
                                      "number> -metaswitch <MetaSwitch " +
                                      "name> (-businessgroup <Business " +
                                      "Group name> | [-subscribergroup " +
                                      "<Subscriber Group>] [-profile " +
                                      "<Persistent Profile>] [-username " +
                                      "<SIP user name>] -domain <SIP domain " +
                                      "name> [-password <SIP password>]";
  private static ShTypedUtilities utilities = new ShTypedUtilities();

  public static void main(String[] args)
  {
    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      //-----------------------------------------------------------------------
      SubscriberInfo subscriberInfo = getParameters(args);

      String dn = subscriberInfo.dn.toString();
      String nwsapUsername = subscriberInfo.nwsapUsername.toString();
      String nwsapPassword = subscriberInfo.nwsapPassword.toString();

      //-----------------------------------------------------------------------
      // Prepare to connect to the Sh service.
      //-----------------------------------------------------------------------
      ShServiceStub shService;

      if (subscriberInfo.mvsAddress.toString() != null)
      {
        shService = new ShServiceStub(subscriberInfo.mvsAddress.toString());
      }
      else
      {
        shService = new ShServiceStub();
      }

      utilities.authenticate(shService,
                             nwsapUsername,
                             nwsapPassword);


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
                        TMetaSubscriberBaseInformationSignalingType
                                                            .fromValue("SIP"));
      baseInformation.setNumberStatus(
               TMetaSubscriberBaseInformationNumberStatus.fromValue("Normal"));

      if (subscriberInfo.businessGroupName.toString() == null)
      {
        baseInformation.setLocale("English (US)");
        baseInformation.setSubscriberGroup(
                                    subscriberInfo.subscriberGroup.toString());
      }
      else
      {
        TSwitchableDefaultString subGp;
        String requestedSubscriberGroup =
                                     subscriberInfo.subscriberGroup.toString();

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

      baseInformation.setMetaSwitchName(
                                     subscriberInfo.metaSwitchName.toString());
      baseInformation.setBusinessGroupName(
                                  subscriberInfo.businessGroupName.toString());
      baseInformation.setPersistentProfile(
                                  subscriberInfo.persistentProfile.toString());
      baseInformation.setSIPDomainName(
                                      subscriberInfo.sipDomainName.toString());

      if (subscriberInfo.sipUserName.toString() == null)
      {
        baseInformation.setUseDNForIdentification(TTrueFalse.TRUE);
      }
      else
      {
        baseInformation.setUseDNForIdentification(TTrueFalse.FALSE);
        baseInformation.setSIPUserName(subscriberInfo.sipUserName.toString());
      }

      if (subscriberInfo.sipPassword.toString() == null)
      {
        baseInformation.setSIPAuthenticationRequired(TTrueFalse.FALSE);
      }
      else
      {
        baseInformation.setSIPAuthenticationRequired(TTrueFalse.TRUE);
        baseInformation.setNewSIPPassword(
                                        subscriberInfo.sipPassword.toString());
        baseInformation.setConfirmNewSIPPassword(
                                        subscriberInfo.sipPassword.toString());
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
      shData.getRepositoryData().add(repositoryData);

      TUserData userData = new TUserData();
      userData.setShData(shData);

      //-----------------------------------------------------------------------
      // Build the user identity using the metaswitch name, bg name 
      // (if supplied) and dn.
      //-----------------------------------------------------------------------
      String[] identifiers = null;
      if (subscriberInfo.businessGroupName.toString() != null)
      {
        identifiers = new String[]
                         {subscriberInfo.metaSwitchName.toString(), 
                          subscriberInfo.businessGroupName.toString(),
                          dn};
      }
      else
      {
        identifiers = new String[]
                         {subscriberInfo.metaSwitchName.toString(), 
                          dn};
      }

      String userIdentity = utilities.getUserIdentity(identifiers);                   
      
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

      System.out.println("Subscriber successfully created.");
      
      //-----------------------------------------------------------------------
      // Send an ShPull to get the new line's alarm state.  Extract and
      // print the alarm state.
      //-----------------------------------------------------------------------
      ShPull shPullRequest = utilities.createPullRequest(
                                                      dn,
                                                      0,
                                                      "Meta_Subscriber_Alarms",
                                                      utilities.ORIGIN_HOST);
     
      ShPullResponse shPullResponse = shService.shPull(shPullRequest);

      utilities.checkResultCode(shPullResponse.getResultCode(),
                                shPullResponse.getExtendedResult(),
                                shPullResponse.getUserData());

      TMetaSubscriberAlarms alarms = shPullResponse.getUserData()
                                                   .getShData()
                                                   .getRepositoryData()
                                                   .get(0)
                                                   .getServiceData()
                                                   .getMetaSwitchData()
                                                   .getMetaSubscriberAlarms();

      System.out.println("Alarm state: \"" + alarms.getAlarmState() + "\"");
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
   * See the associated CreateSubscriber.bat script for parameter details.
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

          case "-mvs":
          {
            fields.mvsAddress.append("http://" + arg + 
                                     ":8087/mvweb/services/ShService");
          }
          break;

          case "-dn":
          {
            fields.dn.append(arg);
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

          case "-subscribergroup":
          {
            fields.subscriberGroup.append(arg);
          }
          break;

          case "-profile":
          {
            fields.persistentProfile.append(arg);
          }
          break;

          case "-username":
          {
            fields.sipUserName.append(arg);
          }
          break;

          case "-domain":
          {
            fields.sipDomainName.append(arg);
          }
          break;

          case "-password":
          {
            fields.sipPassword.append(arg);
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

    if (fields.dn.toString() == null)
    {
      throw new WrongParametersException("No directory number was provided");
    }

    if (fields.metaSwitchName.toString() == null)
    {
      throw new WrongParametersException("No metaswitch name was provided");
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
    Parameter nwsapUsername = new Parameter(null);
    Parameter nwsapPassword = new Parameter(null);
    Parameter dn = new Parameter(null);
    Parameter mvsAddress = new Parameter(null);
    Parameter metaSwitchName = new Parameter(null);
    Parameter businessGroupName = new Parameter(null);
    Parameter subscriberGroup = new Parameter(null);
    Parameter persistentProfile = new Parameter("None");
    Parameter sipUserName = new Parameter(null);
    Parameter sipDomainName = new Parameter(null);
    Parameter sipPassword = new Parameter(null);
  }
}
