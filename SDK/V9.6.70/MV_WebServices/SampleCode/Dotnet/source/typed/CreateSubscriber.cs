//-----------------------------------------------------------------------------
// CreateSubscriber
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
// Author: Alex Davies
//
// Sample application demonstrating the SOAP Sh interface to the MetaView
// Server.  This application sends in an Sh-Update request to create a new
// subscriber, and then sends an Sh-Pull request to fetch the new subscriber's
// alarm state to ensure it was correctly configured.  It uses Microsoft .NET
// and the "typed" WSDL file.  It also demonstrates how you can connect to a
// MetaView Server other than the one specified in the WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------

using System;

public class CreateSubscriber
{
  private readonly static string USAGE = "Usage: CreateSubscriber " +
                                             "[-ems <MetaView Server name>] -dn " +
                                             "<Directory number> -metaswitch" +
                                             " <MetaSwitch name> (-business" +
                                             "group <Business Group name> | " +
                                             "[-subscribergroup <Subscriber " +
                                             "Group>] [-profile <Persistent " +
                                             "Profile>] [-username <SIP " +
                                             "user name>] -domain <SIP " +
                                             "domain name> [-password <SIP " +
                                             "password>]";
  private static ShTypedUtilities utilities = new ShTypedUtilities();

  public static void Main(string[] args)
  {
    String emsAddress;
    String dN;

    int resultCode;
    tExtendedResult extendedResult;

    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      //-----------------------------------------------------------------------
      SubscriberInfo subscriberInfo = getParameters(args,
                                                    out emsAddress,
                                                    out dN);

      //-----------------------------------------------------------------------
      // Prepare to connect to the Sh service.
      //-----------------------------------------------------------------------
      ShService shInstance = new ShService();
      if (emsAddress != null)
      {
        shInstance.Url = emsAddress;
      }

      //-----------------------------------------------------------------------
      // Put together the user data and set the required fields.  In addition
      // to the parameters provided, set:
      // -  SignalingType to "SIP"
      // -  NumberStatus to "Normal"
      // -  Locale to "English (US)"
      //-----------------------------------------------------------------------
      tMeta_Subscriber_BaseInformation baseInformation =
                                        new tMeta_Subscriber_BaseInformation();

      baseInformation.SignalingType =
                   tMeta_Subscriber_BaseInformation_SignalingType.SIP;
      baseInformation.SignalingTypeSpecified = true;
      baseInformation.NumberStatus =
                          tMeta_Subscriber_BaseInformation_NumberStatus.Normal;
      baseInformation.NumberStatusSpecified = true;

      if (subscriberInfo.businessGroupName.toString() == null)
      {
        baseInformation.Locale =
                             tMeta_Subscriber_BaseInformation_Locale.EnglishUS;
        baseInformation.LocaleSpecified = true;
        baseInformation.SubscriberGroup =
                                     subscriberInfo.subscriberGroup.toString();
      }
      else
      {
        tSwitchableDefaultString subGp = new tSwitchableDefaultString();
        subGp.Default = null;
        subGp.UseDefaultSpecified = true;

        String requestedSubscriberGroup =
                                     subscriberInfo.subscriberGroup.toString();

        if (requestedSubscriberGroup == null)
        {
          subGp.UseDefault = tTrueFalse.True;
        }
        else
        {
          subGp.UseDefault = tTrueFalse.False;
          subGp.Value = requestedSubscriberGroup;
        }

        baseInformation.SubscriberGroupBusLine = subGp;
      }

      baseInformation.MetaSwitchName =
                                      subscriberInfo.metaSwitchName.toString();
      baseInformation.BusinessGroupName =
                                   subscriberInfo.businessGroupName.toString();
      baseInformation.PersistentProfile =
                                   subscriberInfo.persistentProfile.toString();
      baseInformation.SIPDomainName = subscriberInfo.sipDomainName.toString();

      if (subscriberInfo.sipUserName.toString() == null)
      {
        baseInformation.UseDNForIdentification = tTrueFalse.True;
      }
      else
      {
        baseInformation.UseDNForIdentification = tTrueFalse.False;
        baseInformation.SIPUserName = subscriberInfo.sipUserName.toString();
      }
      baseInformation.UseDNForIdentificationSpecified = true;

      if (subscriberInfo.sipPassword.toString() == null)
      {
        baseInformation.SIPAuthenticationRequired = tTrueFalse.False;
      }
      else
      {
        baseInformation.SIPAuthenticationRequired = tTrueFalse.True;
        baseInformation.NewSIPPassword =
                                         subscriberInfo.sipPassword.toString();
        baseInformation.ConfirmNewSIPPassword =
                                         subscriberInfo.sipPassword.toString();
      }
      baseInformation.SIPAuthenticationRequiredSpecified = true;

      tMetaSwitchData metaSwitchData = new tMetaSwitchData();
      metaSwitchData.Item = baseInformation;
      metaSwitchData.ItemElementName =
                                ItemChoiceType.Meta_Subscriber_BaseInformation;

      tServiceData serviceData = new tServiceData();
      serviceData.Item = metaSwitchData;

      tTransparentData repositoryData = new tTransparentData();
      repositoryData.ServiceIndication = "Meta_Subscriber_BaseInformation";
      repositoryData.SequenceNumber = 0;
      repositoryData.ServiceData = serviceData;

      tShData shData = new tShData();
      shData.RepositoryData = repositoryData;

      tUserData userData = new tUserData();
      userData.ShData = shData;

      //-----------------------------------------------------------------------
      // Send it in as an Sh-Update request, and make sure it succeeded.
      //-----------------------------------------------------------------------
      resultCode = shInstance.ShUpdate(dN,
                                       0,
                                       userData,
                                       ShUtilities.ORIGIN_HOST,
                                       out extendedResult);

      utilities.checkResultCode(resultCode, extendedResult, null);

      Console.WriteLine("Subscriber successfully created");

      //-----------------------------------------------------------------------
      // Send an ShPull to get the new subscriber's alarm state.  Extract and
      // print the alarm state.
      //-----------------------------------------------------------------------
      resultCode = shInstance.ShPull(dN,
                                     0,
                                     "Meta_Subscriber_Alarms",
                                     ShUtilities.ORIGIN_HOST,
                                     out extendedResult,
                                     out userData);

      utilities.checkResultCode(resultCode, extendedResult, null);

      Object alarmsObject = ((tMetaSwitchData)userData.ShData
                                                      .RepositoryData
                                                      .ServiceData
                                                      .Item).Item;

      tMeta_Subscriber_Alarms alarms = (tMeta_Subscriber_Alarms)alarmsObject;

      Console.WriteLine("Alarm state: \"" +
                        utilities.ConvertToString(alarms.AlarmState) + "\"");
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
   * See the associated CreateSubscriber.bat script for parameter details.
   *
   * @returns           Information about the subscriber to be created.
   *
   * @param args        The arguments provided at the command line.
   * @param emsAddress
   *                    The URL to which to send SOAP messages.
   * @param dN          The directory number to update.
   */
  private static SubscriberInfo getParameters(string[] args,
                                              out String emsAddress,
                                              out String dN)
  {
    Parameter emsAddressParameter = new Parameter(null);
    Parameter dNParameter = new Parameter(null);

    SubscriberInfo fields = new SubscriberInfo();
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
          case "-ems":
            emsAddressParameter.append(arg);
            break;
          case "-dn":
            dNParameter.append(arg);
            break;
          case "-metaswitch":
            fields.metaSwitchName.append(arg);
            break;
          case "-businessgroup":
            fields.businessGroupName.append(arg);
            break;
          case "-subscribergroup":
            fields.subscriberGroup.append(arg);
            break;
          case "-profile":
            fields.persistentProfile.append(arg);
            break;
          case "-username":
            fields.sipUserName.append(arg);
            break;
          case "-domain":
            fields.sipDomainName.append(arg);
            break;
          case "-password":
            fields.sipPassword.append(arg);
            break;
          default:
            throw new WrongParametersException("Unrecognised argument: " +
                                               currentArg);
        }
      }
    }

    if (emsAddressParameter.toString() == null)
    {
      emsAddress = null;
    }
    else
    {
      emsAddress = "http://" + emsAddressParameter.toString() +
                   ":8080/services/ShService";
    }

    if (dNParameter.toString() == null)
    {
      throw new WrongParametersException("No directory number was provided");
    }
    else
    {
      dN = dNParameter.toString();
    }

    return fields;
  }

  //---------------------------------------------------------------------------
  // INNER CLASS: SubscriberInfo
  //
  // Holds information about the subscriber to be created.
  //---------------------------------------------------------------------------
  public class SubscriberInfo
  {
    public Parameter metaSwitchName = new Parameter(null);
    public Parameter businessGroupName = new Parameter(null);
    public Parameter subscriberGroup = new Parameter(null);
    public Parameter persistentProfile = new Parameter("None");
    public Parameter sipUserName = new Parameter(null);
    public Parameter sipDomainName = new Parameter(null);
    public Parameter sipPassword = new Parameter(null);
  }
}