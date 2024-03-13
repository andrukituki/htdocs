//-----------------------------------------------------------------------------
// CreateAGCLine
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material.
//
// Sample application demonstrating the Network Wide Web Services API.
//
// This application sends in an Sh-Update request to create a new line on an
// AGC, and then sends an Sh-Pull request to fetch the new line's alarm state
// to ensure it was correctly configured.  It uses Apache Axis2 and the "typed"
// WSDL file.  It also demonstrates how you can connect to a MetaView Server
// other than the one specified in the WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------

import com.MetaSwitch.EMS.SOAP.TMetaSwitchData;
import com.MetaSwitch.EMS.SOAP.TMetaAGCLineBaseInformation;
import com.MetaSwitch.EMS.SOAP.TMetaAGCLineBaseInformationSignalingType;
import com.MetaSwitch.EMS.SOAP.TAccessDeviceReference;
import com.MetaSwitch.EMS.SOAP.TMetaAGCLineAlarms;
import com.MetaSwitch.EMS.SOAP.TTrueFalse;

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

public class CreateAGCLine
{
  private final static String USAGE = "Usage: CreateAGCLine " +
                                      "-nwsapusername " +
                                      "<NWSAP authentication username> " +
                                      "-nwsappassword " +
                                      "<NWSAP authentication password> " +
                                      "[-mvs <MetaView Server name>] -dn " +
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
      
      String dn = agcLineInfo.dn.toString();
      String nwsapUsername = agcLineInfo.nwsapUsername.toString();
      String nwsapPassword = agcLineInfo.nwsapPassword.toString();

      //-----------------------------------------------------------------------
      // Prepare to connect to the Sh service.
      //-----------------------------------------------------------------------
      ShServiceStub shService;

      if (agcLineInfo.mvsAddress.toString() != null)
      {
        shService = new ShServiceStub(agcLineInfo.mvsAddress.toString());
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

      baseInformation.setLineGroup(agcLineInfo.lineGroup.toString());

      baseInformation.setNetworkElementName(
                                    agcLineInfo.networkElementName.toString());
      
      TAccessDeviceReference accessDevice = new TAccessDeviceReference();
      accessDevice.setGatewayName(agcLineInfo.gateway.toString());
      accessDevice.setAccessDeviceName(agcLineInfo.accessDevice.toString());

      baseInformation.setAccessDevice(accessDevice);

      baseInformation.setAccessLineNumber(
                    Integer.parseInt(agcLineInfo.accessLineNumber.toString()));

      if (agcLineInfo.sipDomainName.toString() == null)
      {
        baseInformation.setUseDomainFromRegistrar(TTrueFalse.TRUE);
      }
      else
      {
        baseInformation.setUseDomainFromRegistrar(TTrueFalse.FALSE);
        baseInformation.setSIPDomainName(agcLineInfo.sipDomainName.toString());
      }

      if (agcLineInfo.sipUserName.toString() == null)
      {
        baseInformation.setUseDNForIdentification(TTrueFalse.TRUE);
      }
      else
      {
        baseInformation.setUseDNForIdentification(TTrueFalse.FALSE);
        baseInformation.setSIPUserName(agcLineInfo.sipUserName.toString());
      }

      if (agcLineInfo.sipPassword.toString() == null)
      {
        baseInformation.setSIPAuthenticationRequired(TTrueFalse.FALSE);
      }
      else
      {
        baseInformation.setSIPAuthenticationRequired(TTrueFalse.TRUE);
        baseInformation.setNewSIPPassword(agcLineInfo.sipPassword.toString());
        baseInformation.setConfirmNewSIPPassword(
                                           agcLineInfo.sipPassword.toString());
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
      shData.getRepositoryData().add(repositoryData);

      TUserData userData = new TUserData();
      userData.setShData(shData);

      //-----------------------------------------------------------------------
      // Build the user identity using the metaswitch name and dn.
      //-----------------------------------------------------------------------
      String[] identifiers = new String[]
                         {agcLineInfo.networkElementName.toString(), 
                          dn };

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
                                                .get(0)
                                                .getServiceData()
                                                .getMetaSwitchData()
                                                .getMetaAGCLineAlarms();

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

          case "-ne":
          {
            fields.networkElementName.append(arg);
          }
          break;
 
          case "-linegroup":
          {
            fields.lineGroup.append(arg);
          }
          break;

          case "-gateway":
          {
            fields.gateway.append(arg);
          }
          break;

          case "-accessdevice":
          {
            fields.accessDevice.append(arg);
          }
          break;

          case "-accesslinenumber":
          {
            fields.accessLineNumber.append(arg); 
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

    if (fields.networkElementName.toString() == null)
    {
      throw new WrongParametersException("No NE name was provided");
    }
    
    if (fields.gateway.toString() == null)
    {
      throw new WrongParametersException("No gateway was provided");
    }
    
    if (fields.accessDevice.toString() == null)
    {
      throw new WrongParametersException("No access device was provided");
    }
    
    if (fields.accessLineNumber.toString() == null)
    {
      throw new WrongParametersException("No access line number was provided");
    }
    
    try
    {
      Integer.parseInt(fields.accessLineNumber.toString());
    }
    catch (NumberFormatException e)
    {
      throw new WrongParametersException("Access line number is not a number. "
                                       + "Access line number supplied: " 
                                       + fields.accessLineNumber.toString());
    }

    if (fields.lineGroup.toString() == null)
    {
      throw new WrongParametersException("No line group was provided");
    }

    return fields;
  }

  //---------------------------------------------------------------------------
  // INNER CLASS: AGCLineInfo
  //
  // Holds information about the line to be created.
  //---------------------------------------------------------------------------
  static class AGCLineInfo
  {
    Parameter nwsapUsername = new Parameter(null);
    Parameter nwsapPassword = new Parameter(null);
    Parameter dn = new Parameter(null);
    Parameter mvsAddress = new Parameter(null);
    Parameter networkElementName = new Parameter(null);
    Parameter lineGroup = new Parameter(null);
    Parameter gateway = new Parameter(null);
    Parameter accessDevice = new Parameter(null);
    Parameter accessLineNumber = new Parameter(null);
    Parameter sipUserName = new Parameter(null);
    Parameter sipDomainName = new Parameter(null);
    Parameter sipPassword = new Parameter(null);
  }
}
