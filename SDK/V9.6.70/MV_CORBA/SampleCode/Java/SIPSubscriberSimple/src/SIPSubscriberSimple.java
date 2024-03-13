/**
 * Title: SIPSubscriberSimple
 *
 * Description: Sample CORBA application which creates a SIP subscriber and
 * configures Caller ID and deletes the subscriber.
 *
 * This example application performs a typical SIP subscriber provisioning
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
 * -  Create a new Individual Line by performing a createElement on these SEAs.
 *
 * -  Configure the Caller ID Services by finding the Caller ID SEA under the
 *    Individual Line SEA and modifying its fields.
 *
 * -  Values are set in mandatory fields and the object creation is completed
 *    by performing an APPLY action.
 * 
 * -  Disable and delete the subscriber and stop the ORB.
 *
 * Sun's ORB does not provide support for SSL, so the MetaView Server's security
 * access must be changed to 'insecure' for this application to be able to
 * login.
 *
 * (c) Microsoft Corporation. All rights reserved.
 * Highly Confidential Material
 *
 */
import org.omg.CORBA.*;
import com.Metaswitch.MVS.Corba.*;
import com.Metaswitch.MVS.Utils.*;

public class SIPSubscriberSimple
{
  private static ClientSessionInterface clientSession = null;

  public static void main(String[] args)
  {
    SEAccessInterface mTopLevelSEA = null;
    SEAccessInterface mConnSEA = null;
    SEAccessInterface mSubGroupSEA = null;
    SEAccessInterface mIndLineSEA = null;
    SEAccessInterface mCallerIDSEA = null;
    
    try
    {
      //-----------------------------------------------------------------------
      // Parse the arguments.
      //-----------------------------------------------------------------------
      String argEMSAddress = CommandLineParser.parseParam("-E:", args);
      String argUserName = CommandLineParser.parseParam("-U:", args);
      String argPassword = CommandLineParser.parseParam("-P:", args);
      String argCfsName = CommandLineParser.parseParam("-M:", args);
      String argDN = CommandLineParser.parseParam("-T:", args);
      String argGroupName = CommandLineParser.parseParam("-G:", args);
      String argDomainName = CommandLineParser.parseParam("-N:", args);  

      //-----------------------------------------------------------------------
      // Start the ORB, log in, and get the top level objects.
      //-----------------------------------------------------------------------
      CorbaHelper.startORB();
      TraceHelper.trace("Started ORB");

      clientSession = CorbaHelper.login(argEMSAddress, argUserName, argPassword);
      TraceHelper.trace("Logged in");

      mTopLevelSEA = clientSession.createSEAccess();

      //-----------------------------------------------------------------------
      // Get hold of the CFS / IS connection we want.
      //-----------------------------------------------------------------------
      mConnSEA = mTopLevelSEA.findElementWithStringField(
              omlapi.O_CFS___UMG___IS___MVD_CONNECTION,
              omlapi.F_CFS___UMG___IS___MVD_NAME,
              argCfsName);
      
      TraceHelper.trace("Found CFS: " + argCfsName);        

      //-----------------------------------------------------------------------
      // Create a Subscriber (Individual Line) set up to use the access device.
      //-----------------------------------------------------------------------
      mSubGroupSEA = mConnSEA.findElementWithStringField(
              omlapi.O_SUBSCRIBER_GROUP, omlapi.F_GROUP_NAME, argGroupName);
      mIndLineSEA = mConnSEA.createElement(omlapi.O_INDIVIDUAL_LINE);
      SettingsUserInterface indSettings = mIndLineSEA.getSnapshot_Settings();
      indSettings.setFieldAsStringByName(omlapi.F_DIRECTORY_NUMBER, argDN);
      indSettings.setFieldAsIntByName(omlapi.F_NUMBER_STATUS, omlapi.V_NORMAL);
      indSettings.setFieldAsIntByName(omlapi.F_SIGNALING_TYPE, omlapi.V_SIP_2);
      indSettings.setFieldAsIntByName(omlapi.F_USE_DN_FOR_IDENTIFICATION, 
                                      omlapi.V_TRUE);
      indSettings.setFieldAsStringByName(omlapi.F_SIP_DOMAIN_NAME, argDomainName);
      indSettings.setFieldAsIntByName(omlapi.F_SIP_AUTHENTICATION_REQUIRED, 
                                      omlapi.V_FALSE);
      indSettings.setFieldAsReferenceByName(
                 omlapi.F_SUBSCRIBER_GROUP, mSubGroupSEA.getSnapshot_Element());
      indSettings.setFieldAsIntByName(omlapi.F_LOCALE, omlapi.V_ENGLISH__US_);
      mIndLineSEA.doAction(omlapi.A_APPLY);

      TraceHelper.trace("SIP subscriber created, DN: " + argDN);

      //-----------------------------------------------------------------------
      // Configure the Individual Line to use Calling Number delivery.
      //-----------------------------------------------------------------------
      mCallerIDSEA =
          mIndLineSEA.findElement(omlapi.O_INDIVIDUAL_LINE_CALLER_ID_SERVICES);
      SettingsUserInterface cidSettings = mCallerIDSEA.getSnapshot_Settings();
      cidSettings.setUseDefaultFlagByName(
          omlapi.F_CALLING_NUMBER_DELIVERY, false);
      cidSettings.setSpecificValueAsIntByName(
          omlapi.F_CALLING_NUMBER_DELIVERY, omlapi.V_TRUE);
      cidSettings.setFieldAsIntByName(
          omlapi.F_CALLING_NUMBER_DELIVERY___ENABLED, omlapi.V_TRUE);
      mCallerIDSEA.doAction(omlapi.A_APPLY);

      TraceHelper.trace("Calling Number delivery enabled");

      //-----------------------------------------------------------------------
      // Disable and delete the individual line.
      //-----------------------------------------------------------------------
      disableAndDeleteElement(mIndLineSEA);
      TraceHelper.trace("Subscriber deleted.");
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
    finally
    {
      //-----------------------------------------------------------------------
      // Destroy all the SEAs
      //-----------------------------------------------------------------------
      if (mTopLevelSEA != null)
      {
        mTopLevelSEA.destroy();
      }
      if (mConnSEA != null)
      {
        mConnSEA.destroy();
      }
      if (mSubGroupSEA != null)
      {
        mSubGroupSEA.destroy();
      }
      if (mIndLineSEA != null)
      {
        mIndLineSEA.destroy();
      }
      if (mCallerIDSEA != null)
      {
        mCallerIDSEA.destroy();
      }

      CorbaHelper.stopORB();
      TraceHelper.trace("Stopped ORB");
      System.exit(0);
    }
  }

  /**
   * Disables and deletes the specified element.
   *
   * @param seAccess    An SEAccess that is attached to the element 
   *                    that should be deleted.
   *
   * @exception ElementUnavailableException
   */
  private static void disableAndDeleteElement(SEAccessInterface seAccess)
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
}
