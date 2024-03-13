//-----------------------------------------------------------------------------
// CreateAGCLine
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material.
//
// Sample application demonstrating the SOAP Sh interface to the MetaView
// Server.  This application sends in an Sh-Update request to create a new
// line on an AGC, and then sends an Sh-Pull request to fetch the new line's
// alarm state to ensure it was correctly configured.  It uses Apache Axis2 and
// the "typed" WSDL file.  It also demonstrates how you can connect to a
// MetaView Server other than the one specified in the WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------

import com.MetaSwitch.EMS.SOAP.ShPull;
import com.MetaSwitch.EMS.SOAP.ShPullResponse;
import com.MetaSwitch.EMS.SOAP.ShUpdate;
import com.MetaSwitch.EMS.SOAP.ShUpdateResponse;
import com.MetaSwitch.EMS.SOAP.ShService;
import com.MetaSwitch.EMS.SOAP.ShServiceStub;
import com.MetaSwitch.EMS.SOAP.TMetaAGCLineBaseInformation;
import com.MetaSwitch.EMS.SOAP.TMetaAGCLineBaseInformationSignalingType;
import com.MetaSwitch.EMS.SOAP.TAccessDeviceReference;
import com.MetaSwitch.EMS.SOAP.TServiceData;
import com.MetaSwitch.EMS.SOAP.TShData;
import com.MetaSwitch.EMS.SOAP.TTransparentData;
import com.MetaSwitch.EMS.SOAP.TUserData;
import com.MetaSwitch.EMS.SOAP.TMetaSwitchData;
import com.MetaSwitch.EMS.SOAP.TTrueFalse;
import com.MetaSwitch.EMS.SOAP.TMetaAGCLineAlarms;

public class CreateAGCLine
{
  private final static String USAGE = "Usage: CreateAGCLine [-mvs " +
                                      "<MetaView Server name>] -dn " +
                                      "<Directory number> -ne " +
                                      "<Network Element name> " +
                                      "-linegroup <Line Group> -gateway " +
                                      "<Gateway name> -accessDevice " +
                                      "<Access Device name> " +
                                      "-accessLineNumber " +
                                      "<Access Line Number> " +
                                      "[-username <SIP user name>] " +
                                      "[-domain <SIP domain name>] " +
                                      "[-password <SIP password>]";
  private static ShTypedUtilities utilities = new ShTypedUtilities();

  public static void main(String[] args)
  {
    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      //-----------------------------------------------------------------------
      AGCLineInfo agcLineInfo = getParameters(args);
      
      String dn = agcLineInfo.dn;
      
      //-----------------------------------------------------------------------
      // Prepare to connect to the Sh service.
      //-----------------------------------------------------------------------
      ShService shService;
      
      if (agcLineInfo.mvsAddress != null)
      {
        shService = new ShServiceStub(agcLineInfo.mvsAddress);
      }
      else
      {
        shService = new ShServiceStub();
      }

      //-----------------------------------------------------------------------
      // Put together the user data and set the required fields.  In addition
      // to the parameters provided, set SignalingType to "Analog Line/T1 CAS"
      // There are constants within the auto-generated classes that would be
      // more efficient to use; however, their string values, and fromValue
      // methods are used here for readability.
      //-----------------------------------------------------------------------
      TMetaAGCLineBaseInformation baseInformation =
                                             new TMetaAGCLineBaseInformation();

      baseInformation.setSignalingType(
        TMetaAGCLineBaseInformationSignalingType
                                             .fromValue("Analog Line/T1 CAS"));

      baseInformation.setLineGroup(agcLineInfo.lineGroup);

      baseInformation.setNetworkElementName(agcLineInfo.networkElementName);

      TAccessDeviceReference accessDevice = new TAccessDeviceReference();
      accessDevice.setGatewayName(agcLineInfo.gateway);
      accessDevice.setAccessDeviceName(agcLineInfo.accessDevice);

      baseInformation.setAccessDevice(accessDevice);
      baseInformation.setAccessLineNumber(
                               Integer.parseInt(agcLineInfo.accessLineNumber));

      if (agcLineInfo.sipDomainName == null)
      {
        baseInformation.setUseDomainFromRegistrar(TTrueFalse.TRUE);
      }
      else
      {
        baseInformation.setUseDomainFromRegistrar(TTrueFalse.FALSE);
        baseInformation.setSIPDomainName(agcLineInfo.sipDomainName);
      }

      if (agcLineInfo.sipUserName == null)
      {
        baseInformation.setUseDNForIdentification(TTrueFalse.TRUE);
      }
      else
      {
        baseInformation.setUseDNForIdentification(TTrueFalse.FALSE);
        baseInformation.setSIPUserName(agcLineInfo.sipUserName);
      }

      if (agcLineInfo.sipPassword == null)
      {
        baseInformation.setSIPAuthenticationRequired(TTrueFalse.FALSE);
      }
      else
      {
        baseInformation.setSIPAuthenticationRequired(TTrueFalse.TRUE);
        baseInformation.setNewSIPPassword(agcLineInfo.sipPassword);
        baseInformation.setConfirmNewSIPPassword(agcLineInfo.sipPassword);
      }
      
      //-----------------------------------------------------------------------
      // Now that we have created the base information from the supplied
      // parameters, add it to the user data.
      //-----------------------------------------------------------------------
      TMetaSwitchData metaSwitchData = new TMetaSwitchData();
      metaSwitchData.setMetaAGCLineBaseInformation(baseInformation);

      TServiceData serviceData = new TServiceData();
      serviceData.setMetaSwitchData(metaSwitchData);

      TTransparentData repositoryData = new TTransparentData();
      repositoryData.setServiceIndication("Meta_AGCLine_BaseInformation");
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

      System.out.println("AGC Line successfully created.");

      //-----------------------------------------------------------------------
      // Send an ShPull to get the new line's alarm state.  Extract and
      // print the alarm state.
      //-----------------------------------------------------------------------
      ShPull shPullRequest = utilities.createPullRequest(dn,
                                                         0,
                                                         "Meta_AGCLine_Alarms",
                                                         utilities.ORIGIN_HOST);
      
      ShPullResponse shPullResponse = shService.shPull(shPullRequest);

      utilities.checkResultCode(shPullResponse.getResultCode(),
                                shPullResponse.getExtendedResult(),
                                shPullResponse.getUserData());

      TMetaAGCLineAlarms alarms = shPullResponse.getUserData()
                                                .getShData()
                                                .getRepositoryData()
                                                .getServiceData()
                                                .getMetaSwitchData()
                                                .getMetaAGCLineAlarms();

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
   * See the associated CreateAGCLine.bat script for parameter details.
   *
   * @returns          Information about the line to be created.
   *
   * @param args        IN  The arguments provided at the command line.
   *
   * @exception WrongParametersException
   */
  private static AGCLineInfo getParameters(String[] args)
    throws WrongParametersException
  {
    AGCLineInfo fields = new AGCLineInfo();
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
          
          case "-ne":
          {
            fields.networkElementName = arg;
          }
          break;
          
          case "-linegroup":
          {
            fields.lineGroup = arg;
          }
          break;
          
          case "-gateway":
          {
            fields.gateway = arg;
          }
          break;
          
          case "-accessdevice":
          {
            fields.accessDevice = arg;
          }
          break;
          
          case "-accesslinenumber":
          {
            fields.accessLineNumber = arg;
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

    return fields;
  }

  //---------------------------------------------------------------------------
  // INNER CLASS: AGCLineInfo
  //
  // Holds information about the AGC line to be created.
  //---------------------------------------------------------------------------
  static class AGCLineInfo
  {
    String mvsAddress = null;
    String dn = null;
    String networkElementName = null;
    String lineGroup = null;
    String gateway = null;
    String accessDevice = null;
    String accessLineNumber = null;
    String sipUserName = null;
    String sipDomainName = null;
    String sipPassword = null;
  }
}
