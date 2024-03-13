/**
 * Title: VPSubConfig
 *
 * Description: Sample CORBA application which creates a VOIP subscriber
 * gateway, access device on it and individual line on the access device and
 * then optionally deletes them again
 *
 * (c) Microsoft Corporation. All rights reserved.
 * Highly Confidential Material
 *
 * @version 1.0
 */

import org.omg.CORBA.*;

import com.Metaswitch.MVS.Corba.*;
import com.Metaswitch.MVS.Utils.*;

public class VPSubConfig
{
  private SEAccessFactoryInterface mSEAccessFactory = null;
  private SEAccessInterface mTopLevelSEA = null;
  private ClientSessionInterface mClientSession = null;

  //---------------------------------------------------------------------------
  // IP address, username and password of the MetaView Server.
  //---------------------------------------------------------------------------
  private String mMetaViewServerIPv4Address;
  private String mMetaViewServerUsername;
  private String mMetaViewServerPassword;

  //---------------------------------------------------------------------------
  // Constants defining vPSubConfigOperation.
  //---------------------------------------------------------------------------
  private static final int ADD_VOIP_AD_AND_SUBSCR = 1;
  private static final int DELETE_VOIP_AD_AND_SUBSCR = 2;

  //---------------------------------------------------------------------------
  // Constants for the command line options.
  //---------------------------------------------------------------------------
  private static final String SERVER_IP_ADDR_OPTION   = "-E:";
  private static final String METAVIEW_SERVER_USERNAME_OPTION = "-U:";
  private static final String METAVIEW_SERVER_PASSWORD_OPTION = "-P:";
  private static final String CFS_NAME_OPTION  = "-M:";
  private static final String DN_OPTION               = "-T:";
  private static final String DELETE_OPTION           = "-D";
  private static final String IP_ADDRESS_OPTION       = "-I:";
  private static final String DOMAIN_NAME_OPTION      = "-N:";
  private static final String SUBSCR_GROUP_OPTION     = "-G:";
  private static final String MEDIA_GWAY_MODEL_OPTION = "-L:";

  //---------------------------------------------------------------------------
  // Constants determining whether to deactivate.
  //---------------------------------------------------------------------------
  private static final boolean NO_DEACTIVATE_REQD = false;
  private static final boolean DEACTIVATE_REQD = true;

  //---------------------------------------------------------------------------
  // Constants for the return codes.
  //---------------------------------------------------------------------------
  private static final int RC_NO_ERROR = 0;
  private static final int RC_INVALID_COMMAND_LINE = -1;
  private static final int RC_UNABLE_TO_CONTACT_MVS = -2;
  private static final int RC_NO_METASWITCH_CONN = -3;
  private static final int RC_METASWITCH_UNAVAILABLE = -4;
  private static final int RC_OP_INCOMPLETE = -5;
  private static final int RC_SUBSCR_GWAY_BAD_VALUE = -101;
  private static final int RC_ACCESS_DEVICE_BAD_VALUE = -102;
  private static final int RC_IND_LINE_BAD_VALUE = -103;
  private static final int RC_CALL_SERV_BAD_VALUE = -104;
  private static final int RC_DN_NOT_IN_NUMBER_RANGE = -105;
  private static final int RC_DN_ALREADY_IN_USE = -106;
  private static final int RC_IP_ADDR_IN_USE = -107;
  private static final int RC_DOMAIN_NAME_IN_USE = -108;
  private static final int RC_WEB_PASSWD_TOO_SHORT = -109;
  private static final int RC_WEB_PASSWD_TOO_LONG = -110;
  private static final int RC_NO_SUCH_DN = -201;
  private static final int RC_INTERNAL_ERROR = -301;

  //---------------------------------------------------------------------------
  // Access Device fields.
  //---------------------------------------------------------------------------
  private static final int sAccDevNum = 1;
  private static final int sAccDevMaxLineNum = 1;

  //---------------------------------------------------------------------------
  // Individual Line fields.
  //---------------------------------------------------------------------------
  private static final int sIndLineNumStatus = omlapi.V_NORMAL;
  private static final int sIndLineAccessLineNum = 1;
  private static final int sIndLineLocale = omlapi.V_ENGLISH__UK_;

  //---------------------------------------------------------------------------
  //  Hardcoded subscriber services fields.
  //---------------------------------------------------------------------------
  private static final int sFwdGpSubscrFwdServSubscr = omlapi.V_TRUE;
  private static final boolean sFwdGpSubscrFwdServSubscrUseDef = false;
  private static final int sFwdGpSubscrFwdServEnabled = omlapi.V_FALSE;
  private static final int sFwdGpSubscrBsyServSubscr = omlapi.V_TRUE;
  private static final boolean sFwdGpSubscrBsyServSubscrUseDef = false;
  private static final int sFwdGpSubscrBsyServEnabled = omlapi.V_FALSE;
  private static final int sFwdGpSubscrDlyServSubscr = omlapi.V_TRUE;
  private static final boolean sFwdGpSubscrDlyServSubscrUseDef = false;
  private static final int sFwdGpSubscrDlyServEnabled = omlapi.V_FALSE;
  private static final int sFwdGpSubscrRemServSubscr = omlapi.V_FALSE;
  private static final boolean sFwdGpSubscrRemServSubscrUseDef = true;
  private static final int sFwdGpSubscrSelServSubscr = omlapi.V_TRUE;
  private static final boolean sFwdGpSubscrSelServSubscrUseDef = false;
  private static final int sFwdGpSubscrSelServEnabled = omlapi.V_FALSE;
  private static final int sFwdGpSubscrHntServSubscr = omlapi.V_FALSE;
  private static final boolean sFwdGpSubscrHntServSubscrUseDef = true;
  private static final int sFwdGpSubscrUsSensBill = omlapi.V_FALSE;
  private static final boolean sFwdGpSubscrUsSensBillUseDef = true;
  private static final int sFwdGpSubscrSelUsSens = omlapi.V_FALSE;
  private static final boolean sFwdGpSubscrSelUsSensUseDef = true;
  private static final int sFwdGpSubscrNoRpyTime = 36;
  private static final boolean sFwdGpSubscrNoRpyTimeUseDef = true;
  private static final int sFwdGpSubscrHntNoRpyTime = 36;
  private static final boolean sFwdGpSubscrHntNoRpyTimeUseDef = true;
  private static final int sFwdGpSubscrNotifyCalling = omlapi.V_TRUE;
  private static final boolean sFwdGpSubscrNotifyCallingUseDef = false;
  private static final int sFwdGpSubscrReleaseNumber = omlapi.V_TRUE;
  private static final boolean sFwdGpSubscrReleaseNumberUseDef = true;
  private static final int sFwdGpSubscrHntArrangement = omlapi.V_REGULAR;
  private static final boolean sFwdGpSubscrHntArrangementUseDef = true;
  private static final int sFwdGpSubscrActConfirmTone = omlapi.V_TRUE;
  private static final boolean sFwdGpSubscrActConfirmToneUseDef = true;

  private static final int sTrnGpSubscrThrServSubscr = omlapi.V_TRUE;
  private static final boolean sTrnGpSubscrThrServSubscrUseDef = false;
  private static final int sTrnGpSubscrCwtServSubscr = omlapi.V_TRUE;
  private static final boolean sTrnGpSubscrCwtServSubscrUseDef = false;
  private static final int sTrnGpSubscrCwtServEnabled = omlapi.V_TRUE;
  private static final int sTrnGpSubscrWidServSubscr = omlapi.V_TRUE;
  private static final boolean sTrnGpSubscrWidServSubscrUseDef = false;
  private static final int sTrnGpSubscrTrnServSubscr = omlapi.V_FALSE;
  private static final boolean sTrnGpSubscrTrnServSubscrUseDef = true;

  private static final int sCidGpSubscrArServSubscr = omlapi.V_TRUE;
  private static final boolean sCidGpSubscrArServSubscrUseDef = true;
  private static final int sCidGpSubscrTrcServSubscr = omlapi.V_TRUE;
  private static final boolean sCidGpSubscrTrcServSubscrUseDef = false;
  private static final int sCidGpSubscrCidServSubscr = omlapi.V_TRUE;
  private static final boolean sCidGpSubscrCidServSubscrUseDef = false;
  private static final int sCidGpSubscrCidServEnabled = omlapi.V_TRUE;
  private static final int sCidGpSubscrPrsServSubscr = omlapi.V_TRUE;
  private static final boolean sCidGpSubscrPrsServSubscrUseDef = false;
  private static final int sCidGpSubscrPrsCndbServSubscr = omlapi.V_TRUE;
  private static final boolean sCidGpSubscrPrsCndbServSubscrUseDef = true;
  private static final int sCidGpSubscrWthldDirNum = omlapi.V_FALSE;
  private static final boolean sCidGpSubscrWthldDirNumUseDef = true;
  private static final int sCidGpSubscrPrsWithhold = omlapi.V_FALSE;
  private static final boolean sCidGpSubscrPrsWithholdUseDef = true;
  private static final int sCidGpSubscrPrsPresent = omlapi.V_FALSE;
  private static final boolean sCidGpSubscrPrsPresentUseDef = true;
  private static final int sCidGpSubscrCidUsSens = omlapi.V_FALSE;
  private static final boolean sCidGpSubscrCidUsSensUseDef = true;
  private static final int sCidGpSubscrTrcUsSens = omlapi.V_TRUE;
  private static final boolean sCidGpSubscrTrcUsSensUseDef = true;
  private static final int sCidGpSubscrPrsCndbUsSens = omlapi.V_FALSE;
  private static final boolean sCidGpSubscrPrsCndbUsSensUseDef = true;
  private static final int sCidGpSubscrArUsSens = omlapi.V_FALSE;
  private static final boolean sCidGpSubscrArUsSensUseDef = true;
  private static final int sCidGpSubscrCnamServSubscr = omlapi.V_FALSE;
  private static final boolean sCidGpSubscrCnamServSubscrUseDef = true;
  private static final int sCidGpSubscrCnamServEnabled = omlapi.V_TRUE;
  private static final int sCidGpSubscrCnamUsSens = omlapi.V_FALSE;
  private static final boolean sCidGpSubscrCnamUsSensUseDef = true;
  private static final int sCidGpSubscrAcServSubscr = omlapi.V_FALSE;
  private static final boolean sCidGpSubscrAcServSubscrUseDef = true;
  private static final int sCidGpSubscrAcUsSens = omlapi.V_FALSE;
  private static final boolean sCidGpSubscrAcUsSensUseDef = true;

  private static final int sIncGpSubscrScrServSubscr = omlapi.V_TRUE;
  private static final boolean sIncGpSubscrScrServSubscrUseDef = false;
  private static final int sIncGpSubscrScrServEnabled = omlapi.V_FALSE;
  private static final int sIncGpSubscrAnrServSubscr = omlapi.V_TRUE;
  private static final boolean sIncGpSubscrAnrServSubscrUseDef = false;
  private static final int sIncGpSubscrAnrServEnabled = omlapi.V_FALSE;
  private static final int sIncGpSubscrDrcwServSubscr = omlapi.V_TRUE;
  private static final boolean sIncGpSubscrDrcwServSubscrUseDef = false;
  private static final int sIncGpSubscrDrcwServEnabled = omlapi.V_FALSE;
  private static final int sIncGpSubscrAnrUsSens = omlapi.V_FALSE;
  private static final boolean sIncGpSubscrAnrUsSensUseDef = true;
  private static final int sIncGpSubscrScrUsSens = omlapi.V_FALSE;
  private static final boolean sIncGpSubscrScrUsSensUseDef = true;
  private static final int sIncGpSubscrDrcwUsSens = omlapi.V_FALSE;
  private static final boolean sIncGpSubscrDrcwUsSensUseDef = true;

  private static final int sMsgGpSubscrVoicemailServSubscr = omlapi.V_TRUE;
  private static final boolean sMsgGpSubscrVoicemailServSubscrUseDef = false;
  private static final int sMsgGpSubscrRemServSubscr = omlapi.V_TRUE;
  private static final boolean sMsgGpSubscrRemServSubscrUseDef = false;
  private static final int sMsgGpSubscrRrcServSubscr = omlapi.V_TRUE;
  private static final boolean sMsgGpSubscrRrcServSubscrUseDef = false;
  private static final int sMsgGpSubscrMsgDelayTime  = 21;
  private static final boolean sMsgGpSubscrMsgDelayTimeUseDef = false;
  private static final boolean sMsgGpSubscrMSRGroupUseDef = true;
  private static final int sMsgGpSubscrVMWISubscr = omlapi.V_FALSE;
  private static final boolean sMsgGpSubscrVMWISubscrUseDef = true;
  private static final int sMsgGpSubscrAMWISubscr = omlapi.V_FALSE;
  private static final boolean sMsgGpSubscrAMWISubscrUseDef = true;

  private static final int sOutGpSubscrSpdServSubscr = omlapi.V_TRUE;
  private static final boolean sOutGpSubscrSpdServSubscrUseDef = false;
  private static final int sOutGpSubscrBarServSubscr = omlapi.V_TRUE;
  private static final boolean sOutGpSubscrBarServSubscrUseDef = false;
  private static final int sOutGpSubscrSpdForm = omlapi.V_ONE_AND_TWO_DIGIT;
  private static final boolean sOutGpSubscrSpdFormUseDef = true;
  private static final int sOutGpSubscrSpdAccess = omlapi.V_TRUE;
  private static final boolean sOutGpSubscrSpdAccessUseDef = true;
  private static final int sOutGpSubscrBarCallTypes  = omlapi.V_INTERNATIONAL | omlapi.V_PREMIUM_3;
  private static final boolean sOutGpSubscrBarCallTypesUseDef = false;
  private static final int sOutGpSubscrBarUsSens = omlapi.V_FALSE;
  private static final boolean sOutGpSubscrBarUsSensUseDef = true;

  private static final int sGenGpSubscrPinServSubscr = omlapi.V_TRUE;
  private static final boolean sGenGpSubscrPinServSubscrUseDef = false;

  private static final int sWebGpSubscrWebServSubscr = omlapi.V_TRUE;

  public static void main(String[] args)
  {
    int vpSubConfigOperation = 0;
    StringHolder metaViewServerIPv4Address = new StringHolder();
    StringHolder metaViewServerUsername = new StringHolder();
    StringHolder metaViewServerPassword = new StringHolder();
    StringHolder metaSwitchName = new StringHolder();
    StringHolder dn = new StringHolder();
    StringHolder iPv4Address = new StringHolder();
    StringHolder domainName = new StringHolder();
    StringHolder subscrGroupName = new StringHolder();
    StringHolder mgModelName = new StringHolder();
    int rc = RC_NO_ERROR;

    try
    {
      vpSubConfigOperation = parseCommandLine(args,
                                              metaViewServerIPv4Address,
                                              metaViewServerUsername,
                                              metaViewServerPassword,
                                              metaSwitchName,
                                              dn,
                                              iPv4Address,
                                              domainName,
                                              subscrGroupName,
                                              mgModelName);
    }
    catch (VPSubConfigFailureException e)
    {
      System.exit(e.mRC);
    }

    //-------------------------------------------------------------------------
    // Start the ORB
    //-------------------------------------------------------------------------
    CorbaHelper.startORB();
    TraceHelper.trace("Started ORB");

    VPSubConfig vPSubConfig = new VPSubConfig(metaViewServerIPv4Address.value,
                                              metaViewServerUsername.value,
                                              metaViewServerPassword.value);

    try
    {
      if (vpSubConfigOperation == ADD_VOIP_AD_AND_SUBSCR)
      {
        vPSubConfig.addVoIPADandIndividualLine(metaSwitchName.value,
                                               dn.value,
                                               iPv4Address.value,
                                               domainName.value,
                                               subscrGroupName.value,
                                               mgModelName.value);
      }
      else if (vpSubConfigOperation == DELETE_VOIP_AD_AND_SUBSCR)
      {
        vPSubConfig.deleteVoIPADandIndividualLine(metaSwitchName.value,
                                                  dn.value);
      }
    }
    catch (VPSubConfigFailureException e)
    {
      CorbaHelper.stopORB();
      TraceHelper.trace("Stopped ORB");
      System.exit(e.mRC);
    }

    CorbaHelper.stopORB();
    TraceHelper.trace("Stopped ORB");

    System.exit(RC_NO_ERROR);
  }

  /**
   * Parse the command line options, check them for consistency, and work out
   * what operation the user wants to perform.
   *
   * @returns           Value indicating what operation to perform. One of
   *                    ADD_VOIP_AD_AND_SUBSCR, DELETE_VOIP_AD_AND_SUBSCR.
   *
   * @param args        The arguments passed to the program.
   * @param metaViewServerIPv4Address
   *                    (OUT) MetaView Server address.
   * @param metaViewServerUsername
   *                    (OUT) MetaView Server username.
   * @param metaViewServerPassword
   *                    (OUT) MetaView Server password.
   * @param cfsName
   *                    (OUT) CFS / IS name
   * @param dn          (OUT) Subscriber directory number.
   * @param iPv4Address
   *                    (OUT) IP address for the new VoIP AD. Only valid if
   *                    the return value is ADD_VOIP_AD_AND_SUBSCR.
   * @param domainName
   *                    (OUT) Domain name for the new VoIP AD. Only valid if
   *                    the return value is ADD_VOIP_AD_AND_SUBSCR.
   * @param subscrGroupName
   *                    (OUT) Subscriber group to add the new subscriber to.
   *                    Only valid if the return value is
   *                    ADD_VOIP_AD_AND_SUBSCR.
   * @param mgModelName
   *                    (OUT) Media Gateway model of the new VoIP AD. Only
   *                    valid if the return value is ADD_VOIP_AD_AND_SUBSCR.
   */
  private static int parseCommandLine(String[] args,
                                      StringHolder metaViewServerIPv4Address,
                                      StringHolder metaViewServerUsername,
                                      StringHolder metaViewServerPassword,
                                      StringHolder cfsName,
                                      StringHolder dn,
                                      StringHolder iPv4Address,
                                      StringHolder domainName,
                                      StringHolder subscrGroupName,
                                      StringHolder mgModelName)
    throws VPSubConfigFailureException
  {
    String usageMessage = "Usage: VPSubConfig -E:<MetaView Server hostname or IPv4 address> "
                           + "-U:<MetaView Server username> "
                           + "-P:<MetaView Server password> "
                           + "-M:<CFS / IS name> -T:<DN of subscriber> "
                           + "[ -D | -I:<IPv4 address> -N:<domain name> "
                           + "-G:<suscriber group name> "
                           + "-L:<media gateway model name> ]";

    //-------------------------------------------------------------------------
    // Initialize all of the StringHolder OUT parameters to null (we will rely
    // on this later when doing consistency checks).
    //-------------------------------------------------------------------------
    metaViewServerIPv4Address.value = null;
    metaViewServerUsername.value = null;
    metaViewServerPassword.value = null;
    cfsName.value = null;
    dn.value = null;
    iPv4Address.value = null;
    domainName.value = null;
    subscrGroupName.value = null;
    mgModelName.value = null;

    //-------------------------------------------------------------------------
    // Step through each of the arguments, parsing the values, and storing them
    // in the relevant StringHolder OUT parameters.
    //-------------------------------------------------------------------------
    boolean deleteSpecified = false;
    for (int ii=0 ;ii<args.length ; ii++)
    {
      if (args[ii].startsWith(SERVER_IP_ADDR_OPTION))
      {
        metaViewServerIPv4Address.value = args[ii].substring(SERVER_IP_ADDR_OPTION.length());
      }
      else if (args[ii].startsWith(METAVIEW_SERVER_USERNAME_OPTION))
      {
        metaViewServerUsername.value = args[ii].substring(METAVIEW_SERVER_USERNAME_OPTION.length());
      }
      else if (args[ii].startsWith(METAVIEW_SERVER_PASSWORD_OPTION))
      {
        metaViewServerPassword.value = args[ii].substring(METAVIEW_SERVER_PASSWORD_OPTION.length());
      }
      else if (args[ii].startsWith(CFS_NAME_OPTION))
      {
        cfsName.value = args[ii].substring(CFS_NAME_OPTION.length());
      }
      else if (args[ii].startsWith(DN_OPTION))
      {
        dn.value = args[ii].substring(DN_OPTION.length());
      }
      else if (args[ii].startsWith(IP_ADDRESS_OPTION))
      {
        iPv4Address.value = args[ii].substring(IP_ADDRESS_OPTION.length());
      }
      else if (args[ii].startsWith(DOMAIN_NAME_OPTION))
      {
        domainName.value = args[ii].substring(DOMAIN_NAME_OPTION.length());
      }
      else if (args[ii].startsWith(SUBSCR_GROUP_OPTION))
      {
        subscrGroupName.value = args[ii].substring(SUBSCR_GROUP_OPTION.length());
      }
      else if (args[ii].startsWith(MEDIA_GWAY_MODEL_OPTION))
      {
        mgModelName.value = args[ii].substring(MEDIA_GWAY_MODEL_OPTION.length());
      }
      else if (args[ii].startsWith(DELETE_OPTION))
      {
        deleteSpecified = true;
      }
      else
      {
        System.err.println("Unrecognized option: " + args[ii]);
        System.err.println(usageMessage);
        throw new VPSubConfigFailureException(RC_INVALID_COMMAND_LINE);
      }
    }

    //-------------------------------------------------------------------------
    // Now validate the parameters that we've found.  Here are all of the
    // checks that we've got to make...
    //
    // -  Make sure we've got all mandatory parameters.  These are
    //   -  MetaView Server IP address
    //   -  Metaview Server Username
    //   -  Metaview Server Password
    //   -  CFS / IS name
    //   -  Subscriber DN
    //
    // -  If the delete option has been specified, make sure that none of the
    //    extra "add" parameters have been specified.
    //
    // -  If the delete option _has not_ been specified, make sure that all of
    //    the extra "add" parameters have been specified.
    //-------------------------------------------------------------------------
    if ((metaViewServerIPv4Address.value == null) ||
        (metaViewServerUsername.value == null) ||
        (metaViewServerPassword.value == null) ||
        (cfsName.value == null) ||
        (dn.value == null))
    {
        System.err.println("One or more mandatory parameters missing");
        System.err.println(usageMessage);
        throw new VPSubConfigFailureException(RC_INVALID_COMMAND_LINE);
    }

    if (deleteSpecified)
    {
      if ((iPv4Address.value != null) ||
          (domainName.value != null) ||
          (subscrGroupName.value != null) ||
          (mgModelName.value != null))
      {
        System.err.println("One or more \"Add\" parameters specified along with the Delete option");
        System.err.println(usageMessage);
        throw new VPSubConfigFailureException(RC_INVALID_COMMAND_LINE);
      }
    }
    else
    {
      if ((iPv4Address.value == null) ||
          (domainName.value == null) ||
          (subscrGroupName.value == null) ||
          (mgModelName.value == null))
      {
        System.err.println("One or more \"Add\" parameters missing");
        System.err.println(usageMessage);
        throw new VPSubConfigFailureException(RC_INVALID_COMMAND_LINE);
      }
    }

    //-------------------------------------------------------------------------
    // Set up our return value indicating the operation to perform.
    //-------------------------------------------------------------------------
    int rc = deleteSpecified
           ? DELETE_VOIP_AD_AND_SUBSCR
           : ADD_VOIP_AD_AND_SUBSCR;

    return rc;
  }


  public VPSubConfig(String metaViewServerIPv4Address,
                     String metaViewServerUsername,
                     String metaViewServerPassword)
  {
    mMetaViewServerIPv4Address = metaViewServerIPv4Address;
    mMetaViewServerUsername = metaViewServerUsername;
    mMetaViewServerPassword = metaViewServerPassword;
  }

  /**
   * Add VOIP subscriber gateway, access device and individual line
   *
   * @returns           Nothing
   *
   * @param cfsName
   *                    The name of the CFS / IS to add the objects to.
   * @param dn          Directory number of Individual Line to add.
   * @param iPv4Address
   *                    IP address of subscriber gateway to add.
   * @param domainName
   *                    Domain name of subscriber gateway
   * @param subscrGroupName
   *                    Name of subscriber group to create line in
   * @param mgModelName
   *                    Media gateway model name
   * @exception VPSubConfigFailureException
   */
  private void addVoIPADandIndividualLine(String cfsName,
                                          String dn,
                                          String iPv4Address,
                                          String domainName,
                                          String subscrGroupName,
                                          String mgModelName)
    throws VPSubConfigFailureException
  {
    boolean loggedIn = false;
    SEAccessInterface connSEA = null;
    SEAccessInterface mgModelSEA = null;
    SEAccessInterface subGSEA = null;
    SEAccessInterface accessDeviceSEA = null;
    SEAccessInterface subGroupSEA = null;
    SEAccessInterface indLineSEA = null;
    try
    {
      //-----------------------------------------------------------------------
      // Login to the MetaView Server.
      //-----------------------------------------------------------------------
      mClientSession = CorbaHelper.login(mMetaViewServerIPv4Address,
                                         mMetaViewServerUsername,
                                         mMetaViewServerPassword);
      TraceHelper.trace("Logged in");
      loggedIn = true;

      //-----------------------------------------------------------------------
      // Get an unattached SEA using the client session.  This behaves as if it
      // were attached to the root of the SE object tree.
      //-----------------------------------------------------------------------
      mTopLevelSEA = mClientSession.createSEAccess();

      //-----------------------------------------------------------------------
      // Get hold of the CFS / IS connection we want.
      //-----------------------------------------------------------------------
      connSEA = mTopLevelSEA.findElementWithStringField(
                                                omlapi.O_CFS___UMG___IS___MVD_CONNECTION,
                                                omlapi.F_CFS___UMG___IS___MVD_NAME,
                                                cfsName);
      if (connSEA != null)
      {
        //---------------------------------------------------------------------
        // Create a new Subscriber Gateway, fill in the fields, and apply the
        // settings.
        //---------------------------------------------------------------------
        mgModelSEA = connSEA.findElementWithStringField(
                                           omlapi.O_REMOTE_MEDIA_GATEWAY_MODEL,
                                           omlapi.F_MODEL_NAME,
                                           mgModelName);

        if (mgModelSEA == null)
        {
          TraceHelper.trace("Unable to find MG Model with name " + mgModelName);
          throw new VPSubConfigFailureException(RC_SUBSCR_GWAY_BAD_VALUE);
        }

        subGSEA = connSEA.createElement(omlapi.O_SUBSCRIBER_GATEWAY);
        SettingsUserInterface subGSettings = subGSEA.getSnapshot_Settings();
        subGSettings.setFieldAsStringByName(omlapi.F_DESCRIPTION, dn);
        subGSettings.setFieldAsStringByName(omlapi.F_IP_ADDRESS, iPv4Address);
        subGSettings.setFieldAsStringByName(omlapi.F_DOMAIN_NAME, domainName);
        subGSettings.setFieldAsReferenceByName(
               omlapi.F_MEDIA_GATEWAY_MODEL, mgModelSEA.getSnapshot_Element());
        subGSEA.doAction(omlapi.A_APPLY);

        TraceHelper.trace("  Created Subscriber Gateway: " + dn);

        //---------------------------------------------------------------------
        // Create an access device (number 1, with 1 access line) on the
        // Subscriber Gateway.
        //---------------------------------------------------------------------
        accessDeviceSEA = subGSEA.createElement(
                                    omlapi.O_SUBSCRIBER_GATEWAY_ACCESS_DEVICE);
        SettingsUserInterface adSettings =
                                        accessDeviceSEA.getSnapshot_Settings();
        adSettings.setFieldAsReferenceByName(omlapi.F_SUBSCRIBER_GATEWAY,
                                             subGSEA.getSnapshot_Element());
        adSettings.setFieldAsStringByName(omlapi.F_DESCRIPTION, dn);
        adSettings.setFieldAsIntByName(omlapi.F_ACCESS_DEVICE_NUMBER, 1);
        adSettings.setFieldAsIntByName(omlapi.F_MAXIMUM_LINE_NUMBER, 1);
        accessDeviceSEA.doAction(omlapi.A_APPLY);

        TraceHelper.trace("  Created Access Device: " + dn);

        //---------------------------------------------------------------------
        // Create a Subscriber (Individual Line) set up to use the access
        // device.
        //---------------------------------------------------------------------
        subGroupSEA = connSEA.findElementWithStringField(
                                                     omlapi.O_SUBSCRIBER_GROUP,
                                                     omlapi.F_GROUP_NAME,
                                                     subscrGroupName);
        if (subGroupSEA == null)
        {
          TraceHelper.trace("Unable to find Subscriber Group with name "
                            + subscrGroupName);
          throw new VPSubConfigFailureException(RC_IND_LINE_BAD_VALUE);
        }

        indLineSEA = connSEA.createElement(omlapi.O_INDIVIDUAL_LINE);
        SettingsUserInterface indSettings = indLineSEA.getSnapshot_Settings();
        indSettings.setFieldAsStringByName(omlapi.F_DIRECTORY_NUMBER, dn);
        indSettings.setFieldAsIntByName(omlapi.F_NUMBER_STATUS, omlapi.V_NORMAL);
        indSettings.setFieldAsReferenceByName(
                                        omlapi.F_ACCESS_DEVICE,
                                        accessDeviceSEA.getSnapshot_Element());
        indSettings.setFieldAsIntByName(omlapi.F_ACCESS_LINE_NUMBER, 1);
        indSettings.setFieldAsReferenceByName(omlapi.F_SUBSCRIBER_GROUP,
                                              subGroupSEA.getSnapshot_Element());
        indSettings.setFieldAsIntByName(omlapi.F_LOCALE, omlapi.V_ENGLISH__UK_);
        indLineSEA.doAction(omlapi.A_APPLY);

        TraceHelper.trace("  Created Individual Line: " + dn);

        //---------------------------------------------------------------------
        // Configure the call services for the new Individual Line.
        //---------------------------------------------------------------------
        updateSubscriberServices(indLineSEA);
      }
    }
    catch (VPSubConfigFailureException e)
    {
      //-----------------------------------------------------------------------
      // Catch and throw the VPSubConfigFailureException to prevent it being
      // caught in the general Exception branch below.
      //-----------------------------------------------------------------------
      throw e;
    }
    catch (org.omg.CORBA.COMM_FAILURE e)
    {
      System.err.println("Unable to login to the MetaView Server.");
      System.err.println("This may be because it is not running, or it is "
                         + "running in secure mode.");
      throw new VPSubConfigFailureException(RC_UNABLE_TO_CONTACT_MVS);
    }
    catch (org.omg.CORBA.TRANSIENT e)
    {
      System.err.println("Transient communication failure with the MetaView "
                         + "Server.\nThis may be because it is not running, "
                         + "or it is running in secure mode,\nor there are "
                         + "problems with the network.");
      throw new VPSubConfigFailureException(RC_UNABLE_TO_CONTACT_MVS);
    }
    catch (LoginFailedException e)
    {
      System.err.println("Unable to login to the MetaView Server.");
      System.err.println("Make sure that:\n"
                         + " - your MetaView Server is running in insecure mode\n"
                         + " - your username and password are correct.");
      throw new VPSubConfigFailureException(RC_UNABLE_TO_CONTACT_MVS);
    }
    catch (IllegalStateException e)
    {
      //-----------------------------------------------------------------------
      // We use IllegalStateException to indicate an assertion failure.  We
      // will already have done a stack trace etc., so just throw a
      // VPSubConfigFailureException to get the suitable return code up to the
      // top level.
      //-----------------------------------------------------------------------
      throw new VPSubConfigFailureException(RC_INTERNAL_ERROR);
    }
    catch (Exception e)
    {
      System.err.println("Unexpected exception: " + e.toString());
      e.printStackTrace();
      System.err.println(e.getMessage());
      throw new VPSubConfigFailureException(RC_INTERNAL_ERROR);
    }
    finally
    {
      //-----------------------------------------------------------------------
      // Destroy all the SEAs
      //-----------------------------------------------------------------------
      if (mTopLevelSEA != null)
      {
        mTopLevelSEA.destroy();
      }
      if (connSEA != null)
      {
        connSEA.destroy();
      }
      if (mgModelSEA != null)
      {
        mgModelSEA.destroy();
      }
      if (subGroupSEA != null)
      {
        subGroupSEA.destroy();
      }
      if (subGSEA != null)
      {
        subGSEA.destroy();
      }
      if (accessDeviceSEA != null)
      {
        accessDeviceSEA.destroy();
      }
      if (indLineSEA != null)
      {
        indLineSEA.destroy();
      }

      //-----------------------------------------------------------------------
      // Log out of the MetaView Server if we managed to log in above.
      //-----------------------------------------------------------------------
      if (loggedIn)
      {
        mClientSession.logout();
        TraceHelper.trace("Logged out");
      }
      mClientSession = null;
    }
  }

  /**
   * Deletes a VOIP individual line, and the access device and subscriber
   * gateway that it's on
   *
   * @returns           Nothing
   *
   * @param cfsName
   *                    The name of the CFS / IS to delete the objects from.
   * @param dn          Directory number of Individual Line to delete.
   *
   * @exception VPSubConfigFailureException
   */
  private void deleteVoIPADandIndividualLine(String cfsName,
                                             String dn)
    throws VPSubConfigFailureException
  {
    boolean loggedIn = false;
    try
    {
      //-----------------------------------------------------------------------
      // Login to the MetaView Server.
      //-----------------------------------------------------------------------
      mClientSession = CorbaHelper.login(mMetaViewServerIPv4Address,
                                         mMetaViewServerUsername,
                                         mMetaViewServerPassword);

      TraceHelper.trace("Logged in");

      mTopLevelSEA = mClientSession.createSEAccess();

      //-----------------------------------------------------------------------
      // Get hold of the CFS / IS connection we want.
      //-----------------------------------------------------------------------
      SEAccessInterface connSEA =
                         mTopLevelSEA.findElementWithStringField(
                                                omlapi.O_CFS___UMG___IS___MVD_CONNECTION,
                                                omlapi.F_CFS___UMG___IS___MVD_NAME,
                                                cfsName);

      if (connSEA != null)
      {
        //---------------------------------------------------------------------
        // Now get the metaswitch index.
        //---------------------------------------------------------------------
        String metaSwitchConnIndex = (connSEA.getIndices())[0];

        String errorMessage = "Failure deleting individual line "
                               + dn;

        //---------------------------------------------------------------------
        // Delete the Individual Line first.
        //---------------------------------------------------------------------
        SEAccessInterface indLineSEA = null;
        DualString accDevReference = null;
        try
        {
          //-------------------------------------------------------------------
          // We have the dn so can use this to build the indices of the
          // Individual Line object, attach to it, and get the Settings.  We
          // can then read out the value of the Access Device from the
          // Settings.  We don't use findElement here because there are likely
          // to be a large number of individual lines on a CFS / ISand
          // trying to find one by DN would be inefficient.
          //-------------------------------------------------------------------
          TraceHelper.trace("Delete Individual Line " + dn + "...");
          indLineSEA = mClientSession.createSEAccess();
          String[] indLineIndices = new String[]{metaSwitchConnIndex,
                                                 omlapi.FIV_NETWORK_ELEMENT,
                                                 omlapi.FIV_CALL_FEATURE_SERVER_INDEX,
                                                 dn};
          indLineSEA.attachToWithIndices(indLineIndices,
                                         omlapi.O_INDIVIDUAL_LINE);

          SettingsUserInterface settings = indLineSEA.getSnapshot_Settings();

          BooleanHolder isAssigned = new BooleanHolder();
          accDevReference =
                     settings.getFieldAsReferenceByName(omlapi.F_ACCESS_DEVICE,
                                                        isAssigned);

          //-------------------------------------------------------------------
          // Do the actual delete on the Individual Line.
          //-------------------------------------------------------------------
          deleteSE(indLineSEA, NO_DEACTIVATE_REQD);
          TraceHelper.trace("Deleted Individual Line " + dn);
        }
        catch (ElementUnavailableException e)
        {
          outputException(errorMessage, e);
          throw new VPSubConfigFailureException(RC_METASWITCH_UNAVAILABLE);
        }
        catch (ElementDeletedException e)
        {
          //-------------------------------------------------------------------
          // An ElementDeletedException here indicates that the object doesn't
          // exist.
          //-------------------------------------------------------------------
          TraceHelper.trace("No Individual Line with DN " + dn);
          throw new VPSubConfigFailureException(RC_NO_SUCH_DN);
        }
        catch (ElementBrokenException e)
        {
          outputException(errorMessage, e);
          throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
        }
        catch (ElementOperationFailedException e)
        {
          outputException(errorMessage, e);
          throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
        }
        //---------------------------------------------------------------------
        // None of the remaining exceptions should happen - these indicate
        // programming errors.
        //---------------------------------------------------------------------
        catch (Exception e)
        {
          System.err.println("Unexpected exception: " + e.toString());
          e.printStackTrace();
          System.err.println(e.getMessage());
          throw new VPSubConfigFailureException(RC_INTERNAL_ERROR);
        }
        finally
        {
          //-------------------------------------------------------------------
          // Destroy any SEA that we've created.
          //-------------------------------------------------------------------
          if (indLineSEA != null)
          {
            indLineSEA.destroy();
          }
        }

        //---------------------------------------------------------------------
        // Delete the Access Device next.
        //---------------------------------------------------------------------
        SEAccessInterface accDevSEA = null;
        SEAccessInterface subGSEA = null;
        errorMessage = "Failure deleting access device" + dn;
        try
        {
          //-------------------------------------------------------------------
          // We set up the access device reference earlier.  Check whether it
          // is valid now.
          //-------------------------------------------------------------------
          TraceHelper.trace("Delete Access Device " + dn + "...");
          String accDevElementName = accDevReference.internal;
          if (accDevReference.internal == null)
          {
            TraceHelper.trace("Individual Line "
                              + dn
                              + " has no reference to an Access Device");
            throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
          }

          //-------------------------------------------------------------------
          // Attach to the access device
          //-------------------------------------------------------------------
          accDevSEA = mClientSession.createSEAccess();
          accDevSEA.attachTo(accDevElementName);

          //-------------------------------------------------------------------
          // The Access Device is a child of the Subscriber Gateway that we
          // want to delete in a moment.  We can therefore do a findElement to
          // get the parent SEA.
          //-------------------------------------------------------------------
          subGSEA = accDevSEA.findElement(omlapi.O_SUBSCRIBER_GATEWAY);

          deleteSE(accDevSEA, DEACTIVATE_REQD);
          TraceHelper.trace("Deleted Access Device " + dn);
        }
        catch (ElementDeletedException e)
        {
          outputException(errorMessage, e);
          throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
        }
        catch (ElementOperationFailedException e)
        {
          outputException(errorMessage, e);
          throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
        }
        //---------------------------------------------------------------------
        // None of the remaining exceptions should happen - these indicate
        // programming errors.
        //---------------------------------------------------------------------
        catch (Exception e)
        {
          System.err.println("Unexpected exception: " + e.toString());
          e.printStackTrace();
          System.err.println(e.getMessage());
          throw new VPSubConfigFailureException(RC_INTERNAL_ERROR);
        }
        finally
        {
          //-------------------------------------------------------------------
          // Destroy any SEA that we've created.
          //-------------------------------------------------------------------
          if (accDevSEA != null)
          {
            accDevSEA.destroy();
          }
        }

        //---------------------------------------------------------------------
        // Finally, delete the Subscriber Gateway.
        //---------------------------------------------------------------------
        errorMessage = "Failure deleting subscriber gateway" + dn;
        try
        {
          TraceHelper.trace("Delete Subscriber Gateway " + dn + "...");

          deleteSE(subGSEA, DEACTIVATE_REQD);
          TraceHelper.trace("Deleted Subscriber Gateway " + dn);
        }
        //---------------------------------------------------------------------
        // None of the remaining exceptions should happen - these indicate
        // programming errors.
        //---------------------------------------------------------------------
        catch (Exception e)
        {
          System.err.println("Unexpected exception: " + e.toString());
          e.printStackTrace();
          System.err.println(e.getMessage());
          throw new VPSubConfigFailureException(RC_INTERNAL_ERROR);
        }
        finally
        {
          //-------------------------------------------------------------------
          // Destroy any SEA that we've created.
          //-------------------------------------------------------------------
          if (subGSEA != null)
          {
            subGSEA.destroy();
          }
        }
      }
    }
    catch (VPSubConfigFailureException e)
    {
      //-----------------------------------------------------------------------
      // Catch and throw the VPSubConfigFailureException to prevent it being
      // caught in the general Exception branch below.
      //-----------------------------------------------------------------------
      throw e;
    }
    catch (org.omg.CORBA.COMM_FAILURE e)
    {
      System.err.println("Unable to login to the MetaView Server.");
      System.err.println("This may be because it is not running, or it is "
                         + "running in secure mode.");
      throw new VPSubConfigFailureException(RC_UNABLE_TO_CONTACT_MVS);
    }
    catch (org.omg.CORBA.TRANSIENT e)
    {
      System.err.println("Transient communication failure with the MetaView "
                         + "Server.\nThis may be because it is not running, "
                         + "or it is running in secure mode,\nor there are "
                         + "problems with the network.");
      throw new VPSubConfigFailureException(RC_UNABLE_TO_CONTACT_MVS);
    }
    catch (LoginFailedException e)
    {
      System.err.println("Unable to login to the MetaView Server.");
      System.err.println("Make sure that:\n"
                         + " - your MetaView Server is running in insecure mode\n"
                         + " - your username and password are correct.");
      throw new VPSubConfigFailureException(RC_UNABLE_TO_CONTACT_MVS);
    }
    catch (IllegalStateException e)
    {
      //-----------------------------------------------------------------------
      // We use IllegalStateException to indicate an assertion failure.  We
      // will already have done a stack trace etc., so just throw a
      // VPSubConfigFailureException to get the suitable return code up to the
      // top level.
      //-----------------------------------------------------------------------
      throw new VPSubConfigFailureException(RC_INTERNAL_ERROR);
    }
    catch (Exception e)
    {
      System.err.println("Unexpected exception: " + e.toString());
      e.printStackTrace();
      System.err.println(e.getMessage());
      throw new VPSubConfigFailureException(RC_INTERNAL_ERROR);
    }
    finally
    {
      //-----------------------------------------------------------------------
      // Log out of the MetaView Server if we managed to log in above.
      //-----------------------------------------------------------------------
      if (loggedIn)
      {
        mClientSession.logout();
        TraceHelper.trace("Logged out");
      }
      mClientSession = null;
    }
  }

  /**
   * Sets the subscriber services for a given Individual Line to hardcoded
   * values
   *
   * @param indlineSEA
   *                    SEAccessInterface attached to the individual line we
   *                    want to update services for.
   * @exception VPSubConfigFailureException
   */
  private void updateSubscriberServices(SEAccessInterface indLineSEA)
  throws VPSubConfigFailureException
  {
    String errorMessage = "Failure when updating call forwarding services for Ind Line."
                        + "\nCheck service is configured on switch";

    SEAccessInterface subServSEA = null;

    //-------------------------------------------------------------------------
    // Call Forwarding
    //-------------------------------------------------------------------------
    try
    {
      subServSEA = indLineSEA.findElement(
                            omlapi.O_INDIVIDUAL_LINE_CALL_FORWARDING_SERVICES);

      if (subServSEA == null)
      {
        TraceHelper.trace(errorMessage);
        throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
      }

      SettingsUserInterface settings = subServSEA.getSnapshot_Settings();

      settings.setUseDefaultFlagByName(omlapi.F_USER_NOTIFICATION_OF_CALL_DIVERSION,
                                       sFwdGpSubscrNotifyCallingUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_USER_NOTIFICATION_OF_CALL_DIVERSION,
                                           sFwdGpSubscrNotifyCalling);
      settings.setUseDefaultFlagByName(omlapi.F_NUMBER_RELEASED_TO_DIVERTED_TO_USER,
                                       sFwdGpSubscrReleaseNumberUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_NUMBER_RELEASED_TO_DIVERTED_TO_USER,
                                           sFwdGpSubscrReleaseNumber);
      settings.setUseDefaultFlagByName(omlapi.F_PLAY_CONFIRM_TONE,
                                       sFwdGpSubscrActConfirmToneUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_PLAY_CONFIRM_TONE,
                                           sFwdGpSubscrActConfirmTone);
      settings.setUseDefaultFlagByName(omlapi.F_UNCONDITIONAL_CALL_FORWARDING___SUBSCRIBED,
                                       sFwdGpSubscrFwdServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_UNCONDITIONAL_CALL_FORWARDING___SUBSCRIBED,
                                           sFwdGpSubscrFwdServSubscr);
      settings.setFieldAsIntByName(omlapi.F_UNCONDITIONAL_CALL_FORWARDING___ENABLED,
                                   sFwdGpSubscrFwdServEnabled);
      settings.setUseDefaultFlagByName(omlapi.F_BUSY_CALL_FORWARDING___SUBSCRIBED,
                                       sFwdGpSubscrBsyServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_BUSY_CALL_FORWARDING___SUBSCRIBED,
                                           sFwdGpSubscrBsyServSubscr);
      settings.setFieldAsIntByName(omlapi.F_BUSY_CALL_FORWARDING___ENABLED,
                                   sFwdGpSubscrBsyServEnabled);
      settings.setUseDefaultFlagByName(omlapi.F_DELAYED_CALL_FORWARDING___SUBSCRIBED,
                                       sFwdGpSubscrDlyServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_DELAYED_CALL_FORWARDING___SUBSCRIBED,
                                           sFwdGpSubscrDlyServSubscr);
      settings.setFieldAsIntByName(omlapi.F_DELAYED_CALL_FORWARDING___ENABLED,
                                   sFwdGpSubscrDlyServEnabled);
      settings.setUseDefaultFlagByName(omlapi.F_DELAYED_CALL_FORWARDING___NO_REPLY_TIME,
                                       sFwdGpSubscrNoRpyTimeUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_DELAYED_CALL_FORWARDING___NO_REPLY_TIME,
                                           sFwdGpSubscrNoRpyTime);
      settings.setUseDefaultFlagByName(omlapi.F_UNCONDITIONAL__BUSY_OR_DELAYED_CALL_FORWARDING_USAGE_SENSITIVE_BILLING,
                                       sFwdGpSubscrUsSensBillUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_UNCONDITIONAL__BUSY_OR_DELAYED_CALL_FORWARDING_USAGE_SENSITIVE_BILLING,
                                           sFwdGpSubscrUsSensBill);
      settings.setUseDefaultFlagByName(omlapi.F_SELECTIVE_CALL_FORWARDING___SUBSCRIBED,
                                       sFwdGpSubscrSelServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_SELECTIVE_CALL_FORWARDING___SUBSCRIBED,
                                           sFwdGpSubscrSelServSubscr);
      settings.setFieldAsIntByName(omlapi.F_SELECTIVE_CALL_FORWARDING___ENABLED,
                                   sFwdGpSubscrSelServEnabled);
      settings.setUseDefaultFlagByName(omlapi.F_SELECTIVE_CALL_FORWARDING___USAGE_SENSITIVE_BILLING,
                                       sFwdGpSubscrSelUsSensUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_SELECTIVE_CALL_FORWARDING___USAGE_SENSITIVE_BILLING,
                                           sFwdGpSubscrSelUsSens);
      settings.setUseDefaultFlagByName(omlapi.F_LINE_HUNTING___SUBSCRIBED,
                                       sFwdGpSubscrHntServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_LINE_HUNTING___SUBSCRIBED,
                                           sFwdGpSubscrHntServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_LINE_HUNTING___NO_REPLY_TIME,
                                       sFwdGpSubscrHntNoRpyTimeUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_LINE_HUNTING___NO_REPLY_TIME,
                                           sFwdGpSubscrHntNoRpyTime);
      settings.setUseDefaultFlagByName(omlapi.F_LINE_HUNTING___ARRANGEMENT,
                                       sFwdGpSubscrHntArrangementUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_LINE_HUNTING___ARRANGEMENT,
                                           sFwdGpSubscrHntArrangement);
      settings.setUseDefaultFlagByName(omlapi.F_REMOTE_ACCESS_TO_CALL_FORWARDING___SUBSCRIBED,
                                       sFwdGpSubscrRemServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_REMOTE_ACCESS_TO_CALL_FORWARDING___SUBSCRIBED,
                                           sFwdGpSubscrRemServSubscr);

      //-----------------------------------------------------------------------
      // Apply the updates.
      //-----------------------------------------------------------------------
      subServSEA.doAction(omlapi.A_APPLY);
      TraceHelper.trace("  Updated Call forwarding services for Ind Line.");
    }
    catch (ElementUnavailableException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_METASWITCH_UNAVAILABLE);
    }
    catch (FieldBadValueException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
    }
    catch (ElementDeletedException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (ElementBrokenException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (ElementOperationFailedException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (SettingsFieldException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
    }
    //-------------------------------------------------------------------------
    // None of the remaining exceptions should happen - these indicate
    // programming errors.
    //-------------------------------------------------------------------------
    catch (Exception e)
    {
      System.err.println("Unexpected exception: " + e.toString());
      e.printStackTrace();
      System.err.println(e.getMessage());
      throw new VPSubConfigFailureException(RC_INTERNAL_ERROR);
    }
    finally
    {
      if (subServSEA != null)
      {
        subServSEA.destroy();
      }
    }

    //-------------------------------------------------------------------------
    // Caller ID
    //-------------------------------------------------------------------------
    errorMessage = "Failure when updating caller ID services for Ind Line."
                 + "\nCheck service is configured on switch";
    try
    {
      subServSEA = indLineSEA.findElement(
                                 omlapi.O_INDIVIDUAL_LINE_CALLER_ID_SERVICES);

      if (subServSEA == null)
      {
        TraceHelper.trace(errorMessage);
        throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
      }

      SettingsUserInterface settings = subServSEA.getSnapshot_Settings();

      settings.setUseDefaultFlagByName(omlapi.F_AUTOMATIC_RECALL___SUBSCRIBED,
                                       sCidGpSubscrArServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_AUTOMATIC_RECALL___SUBSCRIBED,
                                           sCidGpSubscrArServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_AUTOMATIC_RECALL___USAGE_SENSITIVE_BILLING,
                                       sCidGpSubscrArUsSensUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_AUTOMATIC_RECALL___USAGE_SENSITIVE_BILLING,
                                           sCidGpSubscrArUsSens);
      settings.setUseDefaultFlagByName(omlapi.F_AUTOMATIC_CALLBACK___SUBSCRIBED,
                                       sCidGpSubscrAcServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_AUTOMATIC_CALLBACK___SUBSCRIBED,
                                           sCidGpSubscrAcServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_AUTOMATIC_CALLBACK___USAGE_SENSITIVE_BILLING,
                                       sCidGpSubscrAcUsSensUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_AUTOMATIC_CALLBACK___USAGE_SENSITIVE_BILLING,
                                           sCidGpSubscrAcUsSens);
      settings.setUseDefaultFlagByName(omlapi.F_CALL_TRACE___SUBSCRIBED,
                                       sCidGpSubscrTrcServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_CALL_TRACE___SUBSCRIBED,
                                           sCidGpSubscrTrcServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_CALL_TRACE___USAGE_SENSITIVE_BILLING,
                                       sCidGpSubscrTrcUsSensUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_CALL_TRACE___USAGE_SENSITIVE_BILLING,
                                           sCidGpSubscrTrcUsSens);
      settings.setUseDefaultFlagByName(omlapi.F_CALLING_NUMBER_DELIVERY___SUBSCRIBED,
                                       sCidGpSubscrCidServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_CALLING_NUMBER_DELIVERY___SUBSCRIBED,
                                           sCidGpSubscrCidServSubscr);
      settings.setFieldAsIntByName(omlapi.F_CALLING_NUMBER_DELIVERY___ENABLED,
                                   sCidGpSubscrCidServEnabled);
      settings.setUseDefaultFlagByName(omlapi.F_CALLING_NUMBER_DELIVERY___USAGE_SENSITIVE_BILLING,
                                       sCidGpSubscrCidUsSensUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_CALLING_NUMBER_DELIVERY___USAGE_SENSITIVE_BILLING,
                                           sCidGpSubscrCidUsSens);
      settings.setUseDefaultFlagByName(omlapi.F_CALLER_ID_PRESENTATION___SUBSCRIBED,
                                       sCidGpSubscrPrsServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_CALLER_ID_PRESENTATION___SUBSCRIBED,
                                           sCidGpSubscrPrsServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_CALLER_ID_PRESENTATION___WITHHOLD_NUMBER_BY_DEFAULT,
                                       sCidGpSubscrPrsWithholdUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_CALLER_ID_PRESENTATION___WITHHOLD_NUMBER_BY_DEFAULT,
                                           sCidGpSubscrPrsWithhold);
      settings.setUseDefaultFlagByName(omlapi.F_CALLER_ID_PRESENTATION___PRESENT_NUMBER_BY_DEFAULT,
                                       sCidGpSubscrPrsPresentUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_CALLER_ID_PRESENTATION___PRESENT_NUMBER_BY_DEFAULT,
                                           sCidGpSubscrPrsPresent);
      settings.setUseDefaultFlagByName(omlapi.F_WITHHOLD_DIRECTORY_NUMBER,
                                       sCidGpSubscrWthldDirNumUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_WITHHOLD_DIRECTORY_NUMBER,
                                           sCidGpSubscrWthldDirNum);
      settings.setUseDefaultFlagByName(omlapi.F_CALLING_NUMBER_DELIVERY_BLOCKING___SUBSCRIBED,
                                       sCidGpSubscrPrsCndbServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_CALLING_NUMBER_DELIVERY_BLOCKING___SUBSCRIBED,
                                           sCidGpSubscrPrsCndbServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_CALLING_NUMBER_DELIVERY_BLOCKING___USAGE_SENSITIVE_BILLING,
                                       sCidGpSubscrPrsCndbUsSensUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_CALLING_NUMBER_DELIVERY_BLOCKING___USAGE_SENSITIVE_BILLING,
                                           sCidGpSubscrPrsCndbUsSens);
      settings.setUseDefaultFlagByName(omlapi.F_CALLING_NAME_DELIVERY___SUBSCRIBED,
                                       sCidGpSubscrCnamServSubscrUseDef );
      settings.setSpecificValueAsIntByName(omlapi.F_CALLING_NAME_DELIVERY___SUBSCRIBED,
                                           sCidGpSubscrCnamServSubscr);
      settings.setFieldAsIntByName(omlapi.F_CALLING_NAME_DELIVERY___ENABLED,
                                   sCidGpSubscrCnamServEnabled );
      settings.setUseDefaultFlagByName(omlapi.F_CALLING_NAME_DELIVERY___USAGE_SENSITIVE_BILLING,
                                       sCidGpSubscrCnamUsSensUseDef );
      settings.setSpecificValueAsIntByName(omlapi.F_CALLING_NAME_DELIVERY___USAGE_SENSITIVE_BILLING,
                                           sCidGpSubscrCnamUsSens );

      //-----------------------------------------------------------------------
      // Apply the updates.
      //-----------------------------------------------------------------------
      subServSEA.doAction(omlapi.A_APPLY);
      TraceHelper.trace("  Updated Caller ID services for Ind Line.");
    }
    catch (FieldBadValueException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
    }
    catch (ElementUnavailableException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_METASWITCH_UNAVAILABLE);
    }
    catch (ElementDeletedException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (ElementBrokenException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (ElementOperationFailedException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (SettingsFieldException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
    }
    //-------------------------------------------------------------------------
    // None of the remaining exceptions should happen - these indicate
    // programming errors.
    //-------------------------------------------------------------------------
    catch (Exception e)
    {
      System.err.println("Unexpected exception: " + e.toString());
      e.printStackTrace();
      System.err.println(e.getMessage());
      throw new VPSubConfigFailureException(RC_INTERNAL_ERROR);
    }
    finally
    {
      if (subServSEA != null)
      {
        subServSEA.destroy();
      }
    }

    //-------------------------------------------------------------------------
    // General Call Services
    //-------------------------------------------------------------------------
    errorMessage = "Failure when updating General Call services for Ind Line."
                 + "\nCheck service is configured on switch";
    try
    {
      subServSEA = indLineSEA.findElement(
                      omlapi.O_INDIVIDUAL_LINE_GENERAL_CALL_SERVICE_CONTROLS);

      if (subServSEA == null)
      {
        TraceHelper.trace(errorMessage);
        throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
      }

      SettingsUserInterface settings = subServSEA.getSnapshot_Settings();

      settings.setUseDefaultFlagByName(omlapi.F_PIN_CHANGE___SUBSCRIBED,
                                       sGenGpSubscrPinServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_PIN_CHANGE___SUBSCRIBED,
                                           sGenGpSubscrPinServSubscr);

      //-----------------------------------------------------------------------
      // Apply the updates.
      //-----------------------------------------------------------------------
      subServSEA.doAction(omlapi.A_APPLY);
      TraceHelper.trace("  Updated General call services for Ind Line.");
    }
    catch (FieldBadValueException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
    }
    catch (ElementUnavailableException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_METASWITCH_UNAVAILABLE);
    }
    catch (ElementDeletedException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (ElementBrokenException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (ElementOperationFailedException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (SettingsFieldException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
    }
    //-------------------------------------------------------------------------
    // None of the remaining exceptions should happen - these indicate
    // programming errors.
    //-------------------------------------------------------------------------
    catch (Exception e)
    {
      System.err.println("Unexpected exception: " + e.toString());
      e.printStackTrace();
      System.err.println(e.getMessage());
      throw new VPSubConfigFailureException(RC_INTERNAL_ERROR);
    }
    finally
    {
      if (subServSEA != null)
      {
        subServSEA.destroy();
      }
    }

    //-------------------------------------------------------------------------
    // Incoming Services
    //-------------------------------------------------------------------------
    errorMessage = "Failure when updating Incoming Call services for Ind Line."
                 + "\nCheck service is configured on switch";
    try
    {
      subServSEA = indLineSEA.findElement(
                              omlapi.O_INDIVIDUAL_LINE_INCOMING_CALL_SERVICES);

      if (subServSEA == null)
      {
        TraceHelper.trace(errorMessage);
        throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
      }

      SettingsUserInterface settings = subServSEA.getSnapshot_Settings();

      settings.setUseDefaultFlagByName(omlapi.F_SELECTIVE_CALL_REJECTION___SUBSCRIBED,
                                       sIncGpSubscrScrServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_SELECTIVE_CALL_REJECTION___SUBSCRIBED,
                                           sIncGpSubscrScrServSubscr);
      settings.setFieldAsIntByName(omlapi.F_SELECTIVE_CALL_REJECTION___ENABLED,
                                   sIncGpSubscrScrServEnabled);
      settings.setUseDefaultFlagByName(omlapi.F_SELECTIVE_CALL_REJECTION___USAGE_SENSITIVE_BILLING,
                                       sIncGpSubscrScrUsSensUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_SELECTIVE_CALL_REJECTION___USAGE_SENSITIVE_BILLING,
                                           sIncGpSubscrScrUsSens);
      settings.setUseDefaultFlagByName(omlapi.F_ANONYMOUS_CALL_REJECTION___SUBSCRIBED,
                                       sIncGpSubscrAnrServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_ANONYMOUS_CALL_REJECTION___SUBSCRIBED,
                                           sIncGpSubscrAnrServSubscr);
      settings.setFieldAsIntByName(omlapi.F_ANONYMOUS_CALL_REJECTION___ENABLED,
                                   sIncGpSubscrAnrServEnabled);
      settings.setUseDefaultFlagByName(omlapi.F_ANONYMOUS_CALL_REJECTION___USAGE_SENSITIVE_BILLING,
                                       sIncGpSubscrAnrUsSensUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_ANONYMOUS_CALL_REJECTION___USAGE_SENSITIVE_BILLING,
                                           sIncGpSubscrAnrUsSens);
      settings.setUseDefaultFlagByName(omlapi.F_PRIORITY_CALL___SUBSCRIBED,
                                       sIncGpSubscrDrcwServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_PRIORITY_CALL___SUBSCRIBED,
                                           sIncGpSubscrDrcwServSubscr);
      settings.setFieldAsIntByName(omlapi.F_PRIORITY_CALL___ENABLED,
                                   sIncGpSubscrDrcwServEnabled);
      settings.setSpecificValueAsIntByName(omlapi.F_PRIORITY_CALL___USAGE_SENSITIVE_BILLING,
                                           sIncGpSubscrDrcwUsSens);
      settings.setUseDefaultFlagByName(omlapi.F_PRIORITY_CALL___USAGE_SENSITIVE_BILLING,
                                       sIncGpSubscrDrcwUsSensUseDef);

      //-----------------------------------------------------------------------
      // Apply the updates.
      //-----------------------------------------------------------------------
      subServSEA.doAction(omlapi.A_APPLY);
      TraceHelper.trace("  Updated Incoming call services for Ind Line.");
    }
    catch (FieldBadValueException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
    }
    catch (ElementUnavailableException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_METASWITCH_UNAVAILABLE);
    }
    catch (ElementDeletedException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (ElementBrokenException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (ElementOperationFailedException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (SettingsFieldException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
    }
    //-------------------------------------------------------------------------
    // None of the remaining exceptions should happen - these indicate
    // programming errors.
    //-------------------------------------------------------------------------
    catch (Exception e)
    {
      System.err.println("Unexpected exception: " + e.toString());
      e.printStackTrace();
      System.err.println(e.getMessage());
      throw new VPSubConfigFailureException(RC_INTERNAL_ERROR);
    }
    finally
    {
      if (subServSEA != null)
      {
        subServSEA.destroy();
      }
    }

    //-------------------------------------------------------------------------
    // Message Services
    //-------------------------------------------------------------------------
    errorMessage = "Failure when updating Message services for Ind Line."
                 + "\nCheck service is configured on switch";
    try
    {
      subServSEA = indLineSEA.findElement(
                                    omlapi.O_INDIVIDUAL_LINE_MESSAGE_SERVICES);

      if (subServSEA == null)
      {
        TraceHelper.trace(errorMessage);
        throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
      }

      SettingsUserInterface settings = subServSEA.getSnapshot_Settings();

      settings.setUseDefaultFlagByName(omlapi.F_VOICEMAIL___SUBSCRIBED,
                                       sMsgGpSubscrVoicemailServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_VOICEMAIL___SUBSCRIBED,
                                           sMsgGpSubscrVoicemailServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_VOICEMAIL___SUBSCRIBED,
                                       sMsgGpSubscrVoicemailServSubscrUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_VOICEMAIL___VISUAL_MESSAGE_WAITING_INDICATOR,
                                       sMsgGpSubscrVMWISubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_VOICEMAIL___VISUAL_MESSAGE_WAITING_INDICATOR,
                                           sMsgGpSubscrVMWISubscr);
      settings.setUseDefaultFlagByName(omlapi.F_VOICEMAIL___AUDIBLE_MESSAGE_WAITING_INDICATOR,
                                       sMsgGpSubscrAMWISubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_VOICEMAIL___AUDIBLE_MESSAGE_WAITING_INDICATOR,
                                           sMsgGpSubscrAMWISubscr);
      settings.setUseDefaultFlagByName(omlapi.F_VOICEMAIL___CALL_TRANSFER_TIME,
                                       sMsgGpSubscrMsgDelayTimeUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_VOICEMAIL___CALL_TRANSFER_TIME,
                                           sMsgGpSubscrMsgDelayTime);
      settings.setUseDefaultFlagByName(omlapi.F_REMINDER_CALLS___SUBSCRIBED,
                                       sMsgGpSubscrRemServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_REMINDER_CALLS___SUBSCRIBED,
                                           sMsgGpSubscrRemServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_REGULAR_REMINDER_CALLS___SUBSCRIBED,
                                       sMsgGpSubscrRrcServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_REGULAR_REMINDER_CALLS___SUBSCRIBED,
                                           sMsgGpSubscrRrcServSubscr);

      //-----------------------------------------------------------------------
      // Apply the updates.
      //-----------------------------------------------------------------------
      subServSEA.doAction(omlapi.A_APPLY);
      TraceHelper.trace("  Updated Message services for Ind Line.");
    }
    catch (FieldBadValueException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
    }
    catch (ElementUnavailableException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_METASWITCH_UNAVAILABLE);
    }
    catch (ElementDeletedException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (ElementBrokenException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (ElementOperationFailedException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (SettingsFieldException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
    }
    //-------------------------------------------------------------------------
    // None of the remaining exceptions should happen - these indicate
    // programming errors.
    //-------------------------------------------------------------------------
    catch (Exception e)
    {
      System.err.println("Unexpected exception: " + e.toString());
      e.printStackTrace();
      System.err.println(e.getMessage());
      throw new VPSubConfigFailureException(RC_INTERNAL_ERROR);
    }
    finally
    {
      if (subServSEA != null)
      {
        subServSEA.destroy();
      }
    }

    //-------------------------------------------------------------------------
    // Multi-party Services
    //-------------------------------------------------------------------------
    errorMessage = "Failure when updating Multi-Party call services for Ind Line."
                 + "\nCheck service is configured on switch";
    try
    {
      subServSEA = indLineSEA.findElement(
                          omlapi.O_INDIVIDUAL_LINE_MULTI_PARTY_CALL_SERVICES);

      if (subServSEA == null)
      {
        TraceHelper.trace(errorMessage);
        throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
      }

      SettingsUserInterface settings = subServSEA.getSnapshot_Settings();

      settings.setUseDefaultFlagByName(omlapi.F_3_WAY_CALLING___SUBSCRIBED,
                                       sTrnGpSubscrThrServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_3_WAY_CALLING___SUBSCRIBED,
                                           sTrnGpSubscrThrServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_CALL_WAITING___SUBSCRIBED,
                                       sTrnGpSubscrCwtServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_CALL_WAITING___SUBSCRIBED,
                                           sTrnGpSubscrCwtServSubscr);
      settings.setFieldAsIntByName(omlapi.F_CALL_WAITING___ENABLED,
                                   sTrnGpSubscrCwtServEnabled);
      settings.setUseDefaultFlagByName(omlapi.F_CALL_WAITING_WITH_CALLER_ID,
                                       sTrnGpSubscrWidServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_CALL_WAITING_WITH_CALLER_ID,
                                           sTrnGpSubscrWidServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_CALL_TRANSFER___SUBSCRIBED,
                                       sTrnGpSubscrTrnServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_CALL_TRANSFER___SUBSCRIBED,
                                           sTrnGpSubscrTrnServSubscr);

      //-----------------------------------------------------------------------
      // Apply the updates.
      //-----------------------------------------------------------------------
      subServSEA.doAction(omlapi.A_APPLY);
      TraceHelper.trace("  Updated Multi-party call services for Ind Line.");
    }
    catch (FieldBadValueException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
    }
    catch (ElementUnavailableException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_METASWITCH_UNAVAILABLE);
    }
    catch (ElementDeletedException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (ElementBrokenException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (ElementOperationFailedException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (SettingsFieldException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
    }
    //-------------------------------------------------------------------------
    // None of the remaining exceptions should happen - these indicate
    // programming errors.
    //-------------------------------------------------------------------------
    catch (Exception e)
    {
      System.err.println("Unexpected exception: " + e.toString());
      e.printStackTrace();
      System.err.println(e.getMessage());
      throw new VPSubConfigFailureException(RC_INTERNAL_ERROR);
    }
    finally
    {
      if (subServSEA != null)
      {
        subServSEA.destroy();
      }
    }

    //-------------------------------------------------------------------------
    // Outgoing Services
    //-------------------------------------------------------------------------
    errorMessage = "Failure when updating Outgoing call services for Ind Line."
                 + "\nCheck service is configured on switch";
    try
    {
      subServSEA = indLineSEA.findElement(
                              omlapi.O_INDIVIDUAL_LINE_OUTGOING_CALL_SERVICES);

      if (subServSEA == null)
      {
        TraceHelper.trace(errorMessage);
        throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
      }

      SettingsUserInterface settings = subServSEA.getSnapshot_Settings();

      settings.setUseDefaultFlagByName(omlapi.F_SPEED_CALLING___SUBSCRIBED,
                                       sOutGpSubscrSpdServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_SPEED_CALLING___SUBSCRIBED,
                                           sOutGpSubscrSpdServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_SPEED_CALLING___ALLOWED_TYPES,
                                       sOutGpSubscrSpdFormUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_SPEED_CALLING___ALLOWED_TYPES,
                                           sOutGpSubscrSpdForm);
      settings.setUseDefaultFlagByName(omlapi.F_SPEED_CALLING___HANDSET_ACCESS_ALLOWED,
                                       sOutGpSubscrSpdAccessUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_SPEED_CALLING___HANDSET_ACCESS_ALLOWED,
                                           sOutGpSubscrSpdAccess);
      settings.setUseDefaultFlagByName(omlapi.F_CALL_BARRING___SUBSCRIBED,
                                       sOutGpSubscrBarServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_CALL_BARRING___SUBSCRIBED,
                                           sOutGpSubscrBarServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_CALL_BARRING___USAGE_SENSITIVE_BILLING,
                                       sOutGpSubscrBarUsSensUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_CALL_BARRING___USAGE_SENSITIVE_BILLING,
                                           sOutGpSubscrBarUsSens);
      settings.setUseDefaultFlagByName(omlapi.F_CALL_BARRING___CURRENT_BARRED_CALL_TYPES,
                                       sOutGpSubscrBarCallTypesUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_CALL_BARRING___CURRENT_BARRED_CALL_TYPES,
                                           sOutGpSubscrBarCallTypes);

      //-----------------------------------------------------------------------
      // Apply the updates.
      //-----------------------------------------------------------------------
      subServSEA.doAction(omlapi.A_APPLY);
      TraceHelper.trace("  Updated Outgoing call services for Ind Line.");
    }
    catch (FieldBadValueException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
    }
    catch (ElementUnavailableException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_METASWITCH_UNAVAILABLE);
    }
    catch (ElementDeletedException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (ElementBrokenException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (ElementOperationFailedException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_OP_INCOMPLETE);
    }
    catch (SettingsFieldException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_CALL_SERV_BAD_VALUE);
    }
    //-------------------------------------------------------------------------
    // None of the remaining exceptions should happen - these indicate
    // programming errors.
    //-------------------------------------------------------------------------
    catch (Exception e)
    {
      System.err.println("Unexpected exception: " + e.toString());
      e.printStackTrace();
      System.err.println(e.getMessage());
      throw new VPSubConfigFailureException(RC_INTERNAL_ERROR);
    }
    finally
    {
      if (subServSEA != null)
      {
        subServSEA.destroy();
      }
    }
  }

  /**
   * Deletes a subscriber gateway, access device or indivdual line depending on
   * the element name passed in.
   *
   * @param elementName
   * @param deactivateReqd
   *                    whether to deactivate the object before deletion or not
   *
   * @exception VPSubConfigFailureException
   */
  private void deleteSE(SEAccessInterface seAccess, boolean deactivateReqd)
    throws VPSubConfigFailureException
  {
    String errorMessage = "Failure when deleting an object";
    try
    {
      //-----------------------------------------------------------------------
      // Deactivate the Object.  Note that deactivation is an asynchronous
      // operation, and this method will block until deactivation completes.
      // Individual lines do not need to be deactivated before deletion
      //-----------------------------------------------------------------------
      if (deactivateReqd)
      {
        TraceHelper.trace("  Deactivating element");

        deactivateElement(seAccess);

        TraceHelper.trace("  Deactivated element");
      }
      //-----------------------------------------------------------------------
      // Disable and delete the Object.
      //-----------------------------------------------------------------------
      disableAndDeleteElement(seAccess);

      TraceHelper.trace("  Deleted element");
    }
    catch (ElementUnavailableException e)
    {
      outputException(errorMessage, e);
      throw new VPSubConfigFailureException(RC_METASWITCH_UNAVAILABLE);
    }
  }

  /**
   * Disables and deletes the specified element.
   *
   * @param seAccess    An SEAccess that is attached to the Individual Line,
   *                    Access Device that should be deleted.
   *
   * @exception ElementUnavailableException
   */
  private void disableAndDeleteElement(SEAccessInterface seAccess)
    throws ElementUnavailableException
  {
    try
    {
      //-----------------------------------------------------------------------
      // Get a snapshot of the element.  We must have the up-to-date Settings
      // for this SE before we perform an action on it (otherwise we'll get an
      // ElementChangedException).
      //-----------------------------------------------------------------------
      SequenceOfIntegersHolder enabledActions = new SequenceOfIntegersHolder();
      seAccess.getSnapshot(enabledActions,
                           new StringHolder(),
                           new StringHolder(),
                           new SequenceOfReferencesHolder());

      //-----------------------------------------------------------------------
      // The element has to be disabled before it can be deleted.
      //-----------------------------------------------------------------------
      boolean deleteEnabled = false;

      for (int ii = 0; ii < enabledActions.value.length; ii++)
      {
        if (enabledActions.value[ii] == omlapi.A_DISABLE)
        {
          deleteEnabled = true;
        }
      }

      if (deleteEnabled)
      {
        seAccess.doAction(omlapi.A_DISABLE);
        TraceHelper.trace("  Disabled element");
      }

      //-----------------------------------------------------------------------
      // Get a snapshot of the element.  We must have the up-to-date Settings
      // for this SE before we perform an action on it (otherwise we'll get an
      // ElementChangedException).
      //-----------------------------------------------------------------------
      seAccess.getSnapshot(new SequenceOfIntegersHolder(),
                           new StringHolder(),
                           new StringHolder(),
                           new SequenceOfReferencesHolder());

      //-----------------------------------------------------------------------
      // Delete the element.
      //-----------------------------------------------------------------------
      seAccess.doAction(omlapi.A_DELETE);
    }
    catch (ElementDeletedException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if the element does not exist.  If this
      // occurs, there is no problem, because we're trying to delete it anyway.
      //-----------------------------------------------------------------------
      TraceHelper.trace("Element already deleted!");
    }
    //-------------------------------------------------------------------------
    // All other CORBA UserExceptions are unexpected in this case (apart from
    // ElementUnavailableException).
    //-------------------------------------------------------------------------
    catch (ElementBrokenException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementOperationFailedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementChangedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementAlreadyLockedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (SettingsFieldException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (UnknownActionException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ActionNotEnabledException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
  }

  /**
   * Deactivates the specified element.  This method will block until the
   * element has become inactive, which may take some time as deactivation is
   * an asynchronous process.
   *
   * @param seAccess    An SEAccess that is attached to the Access Device that
   *                    should be deactivated.
   *
   * @exception ElementUnavailableException
   */
  private void deactivateElement(SEAccessInterface seAccess)
    throws ElementUnavailableException
  {
    try
    {
      //-----------------------------------------------------------------------
      // Create a snapshot change listener object to register with the SE.  We
      // need to do this because the deactivate action that we are about to
      // perform may complete asynchronously, so we might need to wait until we
      // receive a snapshot that has the Disable action enabled (indicating
      // that the deactivation has completed) before returning.
      //-----------------------------------------------------------------------
      SnapshotChangeListenerInterfacePOA snapshotChangeListener =
        new SnapshotChangeListenerInterfacePOA()
        {
          Thread ownerThread = Thread.currentThread();

          public void newSnapshotAvailable(SEAccessInterface seAccess,
                                           boolean fundamental)
          {
            try
            {
              TraceHelper.trace("      [received snapshot change "
                                + "notification]");

              //---------------------------------------------------------------
              // When a new snapshot is available, we get the value of the
              // Actual status field and check that it is Inactive.  If it is,
              // we notify the thread that is waiting for the deactivation to
              // complete.
              //---------------------------------------------------------------
              boolean actualStatusInactive = isActualStatusInactive(seAccess);
              if (actualStatusInactive)
              {
                //-------------------------------------------------------------
                // Interrupt the thread that is waiting for the deactivation to
                // complete (see below).
                //-------------------------------------------------------------
                TraceHelper.trace("      [actual status inactive, finished "
                                  + "deactivation]");
                ownerThread.interrupt();
              }
              else
              {
                //-------------------------------------------------------------
                // Wait for the next snapshot change notification.
                //-------------------------------------------------------------
                TraceHelper.trace("      [actual status not inactive]");
              }
            }
            catch (ElementUnavailableException e)
            {
              //---------------------------------------------------------------
              // If the element has become unavailable when we try to get the
              // snapshot, give up and wait for the next snapshot change
              // notification.
              //---------------------------------------------------------------
            }
          }
        };

      //-----------------------------------------------------------------------
      // Get a CORBA reference for our snapshot change listener and register it
      // with the SE to receive snapshot notifications.
      //-----------------------------------------------------------------------
      SnapshotChangeListenerInterface snapshotChangeListenerCorbaRef =
                            snapshotChangeListener._this(CorbaHelper.getORB());
      seAccess.addSnapshotChangeListener(snapshotChangeListenerCorbaRef);

      //-----------------------------------------------------------------------
      // Get a snapshot of the object.  We must have the up-to-date Settings
      // for this SE before we perform an action on it (otherwise we'll get an
      // ElementChangedException).
      //-----------------------------------------------------------------------
      seAccess.getSnapshot(new SequenceOfIntegersHolder(),
                           new StringHolder(),
                           new StringHolder(),
                           new SequenceOfReferencesHolder());

      //-----------------------------------------------------------------------
      // Deactivate the object.
      //-----------------------------------------------------------------------
      seAccess.doAction(omlapi.A_DEACTIVATE);

      //-----------------------------------------------------------------------
      // Check whether the action has completed immediately - if it has, we can
      // return immediately.  Otherwise, we need to wait until the snapshot
      // change listener interrupts us.  The Deactivate action is complete when
      // the Disable action becomes available.
      //-----------------------------------------------------------------------
      boolean actualStatusInactive = isActualStatusInactive(seAccess);
      if (!actualStatusInactive)
      {
        //---------------------------------------------------------------------
        // The deactivation did not complete immediately, so wait for the
        // snapshot change listener to interrupt us, then return.  If the
        // operation takes more than 30 seconds, then something has probably
        // gone wrong.
        //---------------------------------------------------------------------
        try
        {
          TraceHelper.trace("    Waiting for deactivation to complete...");
          Thread.currentThread().sleep(30000);
          TraceHelper.trace("    Deactivation failed to complete in 30 "
                            + "seconds!");
        }
        catch (InterruptedException e)
        {
          //-------------------------------------------------------------------
          // We assume that the only thread that will interrupt us is the one
          // from the snapshot change notification.
          //-------------------------------------------------------------------
          TraceHelper.trace("    Deactivation complete.");
        }
      }

      //-----------------------------------------------------------------------
      // Remove the snapshot change listener, as we are no longer interested in
      // snapshot changes.
      //-----------------------------------------------------------------------
      seAccess.removeSnapshotChangeListener();
    }
    //-------------------------------------------------------------------------
    // All CORBA UserExceptions are unexpected in this case (apart from
    // ElementUnavailableException).
    //-------------------------------------------------------------------------
    catch (ElementDeletedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementBrokenException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementOperationFailedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementChangedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementAlreadyLockedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (SettingsFieldException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (UnknownActionException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ActionNotEnabledException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (TooManySnapshotChangeListenersException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NoSnapshotChangeListenersException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
  }

  /**
   * Perform a getSnapshot on the SEAccess provided, and check whether the
   * value of the Actual status field is Inactive.
   *
   * @returns           true if the Actual status is Inactive, false otherwise.
   *
   * @param seAccess    The SEAccessInterface to call getSnapshot on.
   *
   * @exception ElementUnavailableException
   */
  private boolean isActualStatusInactive(SEAccessInterface seAccess)
    throws ElementUnavailableException
  {
    boolean actualStatusInactive = false;

    try
    {
      //-----------------------------------------------------------------------
      // Get the latest snapshot from the SEAccess.
      //-----------------------------------------------------------------------
      SettingsUserInterface settings =
                        seAccess.getSnapshot(new SequenceOfIntegersHolder(),
                                             new StringHolder(),
                                             new StringHolder(),
                                             new SequenceOfReferencesHolder());

      BooleanHolder isAssigned = new BooleanHolder();
      int actualStatus = settings.getFieldAsIntByName(omlapi.F_ACTUAL_STATUS,
                                                      isAssigned);
      actualStatusInactive = (isAssigned.value &&
                              (actualStatus == omlapi.V_INACTIVE_2));
    }
    //-------------------------------------------------------------------------
    // All CORBA UserExceptions are unexpected in this case (apart from
    // ElementUnavailableException).
    //-------------------------------------------------------------------------
    catch (ElementDeletedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementBrokenException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }

    return actualStatusInactive;
  }

  /**
   * Trace out details of a normal "handled" exception (i.e.  one that can
   * validly be thrown).  For unexpected exceptions, call
   * CorbaHelper.handleUnexpectedUserException()
   *
   * @param errorText   Descriptive text explaining the scenario in which the
   *                    exception was caught.
   * @param exception   The exception.
   */
  private static void outputException(String errorText,
                                      UserException exception)
  {
    String extraText = null;
    int mainRC = 0;
    int subRC = 0;
    String field = null;

    try
    {
      //-----------------------------------------------------------------------
      // Throw the exception again so we can pick out some extra information
      // from the specific exception types.
      //-----------------------------------------------------------------------
      throw exception;
    }
    catch (SettingsFieldException e)
    {
      mainRC = e.mainReturnCode;
      subRC = e.subReturnCode;
      field = e.erroredField;
    }
    catch (ElementOperationFailedException e)
    {
      mainRC = e.mainReturnCode;
      subRC = e.subReturnCode;
      field = e.erroredField;
    }
    catch (UserException e)
    {
      ;
    }

    //-------------------------------------------------------------------------
    // Form the extra text from the values that we picked out above.
    //-------------------------------------------------------------------------
    if ((subRC == SEExceptionsInterface.ES_VALUE_NOT_UNIQUE) &&
        (field != null))
    {
      extraText = "An object already exists with the same value for field \"" + field + "\"";
    }
    else if ((mainRC == SEExceptionsInterface.RC_WRONG_VALUE) &&
             (field != null))
    {
      extraText = "The value for field \"" + field + "\" is invalid";
    }
    else if ((mainRC == SEExceptionsInterface.RC_INCONSISTENT_VALUE) &&
             (field != null))
    {
      extraText = "The value for field \"" + field + "\" is inconsistent with some other value";
    }
    else
    {
      extraText = "No more information is available";
    }

    TraceHelper.trace(errorText
                      + "\nExtra details: " + extraText
                      + "\n");
  }
}
