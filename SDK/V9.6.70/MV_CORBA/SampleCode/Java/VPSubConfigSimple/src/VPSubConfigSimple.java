/**
 * Title: VPSubConfigSimple
 *
 * Description: Sample CORBA application which creates a VOIP subscriber
 * gateway, access device on it and individual line on the access device, and
 * configures Caller ID.
 *
 * This example application performs a typical VOIP subscriber provisioning
 * operation on a specified MetaView Server as follows:
 *
 * -  Start a local ORB and log into the MetaView Server, using the CorbaHelper
 *    utility methods.  Store the returned reference to the new Client Session
 *    that the MetaView Server will have instantiated.
 *
 * -  Request and store a reference to the MetaView Server's Top Level System
 *    Element Access (SEA) using the new Client Session.
 *
 * -  Request and store references to the CFS / IS Connection SE Access
 *    needed using find element on the top level SEA.
 *
 * -  Create a new Subscriber Gateway, Access Device and Individual Line by
 *    performing a createElement on these SEAs.
 *
 * -  Configure the Caller ID Services by finding the Caller ID SEA under the
 *    Individual Line SEA and modifying its fields.
 *
 * -  Values are set in mandatory fields and the object creation is completed
 *    by performing an APPLY action.
 *
 * Sun's ORB does not provide support for SSL, so the MetaView Server's security
 * access must be changed to 'insecure' for this application to be able to
 * login.
 *
 * (c) Microsoft Corporation. All rights reserved.
 * Highly Confidential Material
 *
 * @version 1.0
 */
import org.omg.CORBA.*;
import com.Metaswitch.MVS.Corba.*;
import com.Metaswitch.MVS.Utils.*;

public class VPSubConfigSimple
{
  private static ClientSessionInterface clientSession = null;
  private static SEAccessInterface topLevelSEA = null;

  public static void main(String[] args)
  {
    try
    {
      //-----------------------------------------------------------------------
      // Parse the arguments.
      //-----------------------------------------------------------------------
      String argEMSAddress = CommandLineParser.parseParam("-E:", args);
      String argUserName = CommandLineParser.parseParam("-U:", args);
      String argPassword = CommandLineParser.parseParam("-P:", args);
      String argCfsName = CommandLineParser.parseParam("-M:", args);
      String argMGModelName = CommandLineParser.parseParam("-L:", args);
      String argDescription = CommandLineParser.parseParam("-D:", args);
      String argSubGAddress = CommandLineParser.parseParam("-I:", args);
      String argDN = CommandLineParser.parseParam("-T:", args);
      String argGroupName = CommandLineParser.parseParam("-G:", args);

      //-----------------------------------------------------------------------
      // Start the ORB, log in, and get the top level objects.
      //-----------------------------------------------------------------------
      CorbaHelper.startORB();
      clientSession = CorbaHelper.login(argEMSAddress, argUserName, argPassword);
      topLevelSEA = clientSession.createSEAccess();

      //-----------------------------------------------------------------------
      // Get hold of the CFS / IS connection we want.
      //-----------------------------------------------------------------------
      SEAccessInterface connSEA =
          topLevelSEA.findElementWithStringField(
              omlapi.O_CFS___UMG___IS___MVD_CONNECTION,
              omlapi.F_CFS___UMG___IS___MVD_NAME,
              argCfsName);

      //-----------------------------------------------------------------------
      // Create a new Subscriber Gateway, fill in the fields, and apply the
      // settings.
      //-----------------------------------------------------------------------
      SEAccessInterface mgModelSEA =
          connSEA.findElementWithStringField(
              omlapi.O_REMOTE_MEDIA_GATEWAY_MODEL,
              omlapi.F_MODEL_NAME,
              argMGModelName);
      SEAccessInterface subGSEA =
                  connSEA.createElement(omlapi.O_SUBSCRIBER_GATEWAY);
      SettingsUserInterface subGSettings = subGSEA.getSnapshot_Settings();
      subGSettings.setFieldAsStringByName(omlapi.F_DESCRIPTION, argDescription);
      subGSettings.setFieldAsStringByName(omlapi.F_IP_ADDRESS, argSubGAddress);
      subGSettings.setFieldAsReferenceByName(
               omlapi.F_MEDIA_GATEWAY_MODEL, mgModelSEA.getSnapshot_Element());
      subGSEA.doAction(omlapi.A_APPLY);

      //-----------------------------------------------------------------------
      // Create an access device (number 1, with 1 access line) on the
      // Subscriber Gateway.
      //-----------------------------------------------------------------------
      SEAccessInterface accessDeviceSEA = subGSEA.createElement(
          omlapi.O_SUBSCRIBER_GATEWAY_ACCESS_DEVICE);
      SettingsUserInterface adSettings = accessDeviceSEA.getSnapshot_Settings();
      adSettings.setFieldAsReferenceByName(
                   omlapi.F_SUBSCRIBER_GATEWAY, subGSEA.getSnapshot_Element());
      adSettings.setFieldAsStringByName(omlapi.F_DESCRIPTION, argDescription);
      adSettings.setFieldAsIntByName(omlapi.F_ACCESS_DEVICE_NUMBER, 1);
      adSettings.setFieldAsIntByName(omlapi.F_MAXIMUM_LINE_NUMBER, 1);
      accessDeviceSEA.doAction(omlapi.A_APPLY);

      //-----------------------------------------------------------------------
      // Create a Subscriber (Individual Line) set up to use the access device.
      //-----------------------------------------------------------------------
      SEAccessInterface subGroupSEA =
          connSEA.findElementWithStringField(
              omlapi.O_SUBSCRIBER_GROUP, omlapi.F_GROUP_NAME, argGroupName);
      SEAccessInterface indLineSEA =
                     connSEA.createElement(omlapi.O_INDIVIDUAL_LINE);
      SettingsUserInterface indSettings = indLineSEA.getSnapshot_Settings();
      indSettings.setFieldAsStringByName(omlapi.F_DIRECTORY_NUMBER, argDN);
      indSettings.setFieldAsIntByName(omlapi.F_NUMBER_STATUS, omlapi.V_NORMAL);
      indSettings.setFieldAsReferenceByName(
                omlapi.F_ACCESS_DEVICE, accessDeviceSEA.getSnapshot_Element());
      indSettings.setFieldAsIntByName(omlapi.F_ACCESS_LINE_NUMBER, 1);
      indSettings.setFieldAsReferenceByName(
                 omlapi.F_SUBSCRIBER_GROUP, subGroupSEA.getSnapshot_Element());
      indSettings.setFieldAsIntByName(omlapi.F_LOCALE, omlapi.V_ENGLISH__UK_);
      indLineSEA.doAction(omlapi.A_APPLY);

      //-----------------------------------------------------------------------
      // Configure the Individual Line to use Calling Number delivery.
      //-----------------------------------------------------------------------
      SEAccessInterface callerIDSEA =
          indLineSEA.findElement(omlapi.O_INDIVIDUAL_LINE_CALLER_ID_SERVICES);
      SettingsUserInterface cidSettings = callerIDSEA.getSnapshot_Settings();
      cidSettings.setUseDefaultFlagByName(
          omlapi.F_CALLING_NUMBER_DELIVERY, false);
      cidSettings.setSpecificValueAsIntByName(
          omlapi.F_CALLING_NUMBER_DELIVERY, omlapi.V_TRUE);
      cidSettings.setFieldAsIntByName(
          omlapi.F_CALLING_NUMBER_DELIVERY___ENABLED, omlapi.V_TRUE);
      callerIDSEA.doAction(omlapi.A_APPLY);
    }
    catch (BadParamException e)
    {
      System.out.println("Bad parameters: " + e);
    }
    catch (org.omg.CORBA.UserException e)
    {
      System.out.println("Corba exception caught: " + e);
      e.printStackTrace();
    }
    catch (Exception e)
    {
      System.out.println("Non-Corba exception caught: " + e);
      e.printStackTrace();
    }
  }
}
