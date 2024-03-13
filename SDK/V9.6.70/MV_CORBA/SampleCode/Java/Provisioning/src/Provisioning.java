/**
 * Title: Provisioning
 *
 * Description: Sample CORBA application which creates an IDT, an Access device
 * for that IDT, and several Individual Lines using that Access Device, then
 * optionally deletes them again.
 *
 * (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
 *
 * @version 1.0
 */

//-----------------------------------------------------------------------------
// This example application performs a typical subscriber provisioning
// operation on a specified MetaView Server as follows:
//
// -  Start a local ORB and log into the MetaView Server, using the CorbaHelper
//    utility methods.  Store the returned reference to the new Client Session
//    that the MetaView Server will have instantiated.
//
// -  Request and store a reference to the MetaView Server's Top Level System
//    Element Access (SEA) using the new Client Session.
//
// -  Request and store references to new SE Accesses using the top level SEA.
//    Create a new IDT, Access Device and Individual Lines by performing an
//    createElement on these SEAs.
//
// -  Values are set in mandatory fields and the object creation is completed
//    by performing an APPLY action.
//
// -  Delete the Individual Lines that we have just created.
//
// -  Deactivate, disable and delete the Access Device and IDT.  Deactivation
//    is an asynchronous operation so we register snapshotChangeListeners to
//    wait for the operations to complete.
//
// -  Note that this application only works on Integrated Systems.
//
// Sun's ORB does not provide support for SSL, so the MetaView Server's security
// access must be changed to 'insecure' for this application to be able to
// login.
//-----------------------------------------------------------------------------

import org.omg.CORBA.*;

import com.Metaswitch.MVS.Corba.*;
import com.Metaswitch.MVS.Utils.*;

public class Provisioning
{
  private String mIPAddress = null;
  private String mUserName = null;
  private String mPassword = null;
  private SEAccessInterface mTopLevelSEA = null;
  private SEAccessInterface mConnSEA = null;
  private ClientSessionInterface mClientSession = null;

  //---------------------------------------------------------------------------
  // The number of individual Lines we want to create, and whether we should
  // delete them at the end of the application.
  //---------------------------------------------------------------------------
  private static final int sNumIndividualLines = 32;
  private static final boolean sDeleteIndividualLines = true;

  //---------------------------------------------------------------------------
  // Whether we should delete the Access Device and IDR after the Individual
  // Lines have been deleted.  Note that if sDeleteIndividualLines is false,
  // setting sDeleteAccessDeviceAndIDT to true will result in the MetaView Server
  // rejecting our deletion attempts as it is not possible to delete an Access
  // Device while it has subscribers configured to use it.
  //---------------------------------------------------------------------------
  private static final boolean sDeleteAccessDeviceAndIDT = true;

  //---------------------------------------------------------------------------
  // Hardcoded values to use in the creation of the IDT, Access Device and
  // Individual Lines.
  //---------------------------------------------------------------------------
  //---------------------------------------------------------------------------
  // IDT fields.
  //---------------------------------------------------------------------------
  private static final String sIDTDescription = "Sample IDT";

  //---------------------------------------------------------------------------
  // Individual Line fields.
  //---------------------------------------------------------------------------
  private static final int sIndLineNumStatus = omlapi.V_NORMAL;
  private static final int sIndLineLocale = omlapi.V_ENGLISH__US_;

  public static void main(String[] args)
  {
    //-------------------------------------------------------------------------
    // Extract the IP address, username and password from the arguments
    // provided.
    //-------------------------------------------------------------------------
    if (args.length < 3)
    {
      System.err.println("Usage: Provisioning MetaViewServerIPAddress/HostName "
                         + "Username Password");
      System.exit(1);
    }

    String iPAddress = args[0];
    String userName = args[1];
    String password = args[2];

    CorbaHelper.startORB();

    TraceHelper.trace("Started ORB");

    Provisioning provisioning = new Provisioning(iPAddress,
                                                 userName,
                                                 password);
    provisioning.execute();

    CorbaHelper.stopORB();

    TraceHelper.trace("Stopped ORB");

    System.exit(0);
  }

  public Provisioning(String iPAddress, String userName, String password)
  {
    mIPAddress = iPAddress;
    mUserName = userName;
    mPassword = password;
  }

  public synchronized void execute()
  {
    try
    {
      //-----------------------------------------------------------------------
      // Login to the MetaView Server.
      //-----------------------------------------------------------------------
      mClientSession = CorbaHelper.login(mIPAddress, mUserName, mPassword);

      //-----------------------------------------------------------------------
      // Get an unattached SEA using the client session.  This behaves as if it
      // were attached to the root of the SE object tree.
      //-----------------------------------------------------------------------
      mTopLevelSEA = mClientSession.createSEAccess();

      TraceHelper.trace("Logged in");

      //-----------------------------------------------------------------------
      // Get an SEA attached to the first CFS / UMG / IS / MVD Connection
      // object.  This method will return null if no CFS / UMG / IS / MVD
      // Connection can be found.  We do this (rather than just using the top
      // level SEA) so that everything we create will be on this CFS / UMG / IS
      // / MVD even if another CFS / UMG / IS / MVD Connection is added before
      // the first one.  Note that for this application to work the first
      // connection must be an IS.
      //-----------------------------------------------------------------------
      mConnSEA = mTopLevelSEA.findElement(omlapi.O_CFS___UMG___IS___MVD);

      if (mConnSEA != null)
      {
        TraceHelper.trace("Got Connection SEA.");

        //---------------------------------------------------------------------
        // Create an IDT.
        //---------------------------------------------------------------------
        DualString idtReference = createIDT();

        TraceHelper.trace("Created IDT");

        //---------------------------------------------------------------------
        // Create an Access Device using the IDT we've just created.
        //---------------------------------------------------------------------
        DualString accDevReference = createAccessDeviceForIDT(idtReference);

        TraceHelper.trace("Created Access Device");

        //---------------------------------------------------------------------
        // Create several Individual Lines using the Access Device we've just
        // created.
        //---------------------------------------------------------------------
        DualString[] indLineReferences = new DualString[sNumIndividualLines];

        for (int ii = 0; ii < sNumIndividualLines; ii++)
        {
          indLineReferences[ii] =
                           createIndividualLineOnAccessDevice(accDevReference);
        }

        TraceHelper.trace("Created Individual Lines");

        if (sDeleteIndividualLines)
        {
          //-------------------------------------------------------------------
          // Delete the Individual Lines we've just created.
          //-------------------------------------------------------------------
          for (int ii = 0; ii < sNumIndividualLines; ii++)
          {
            deleteIndividualLine(indLineReferences[ii]);
          }

          TraceHelper.trace("Deleted Individual Lines");
        }

        if (sDeleteAccessDeviceAndIDT)
        {
          //-------------------------------------------------------------------
          // Delete the Access Device and IDT.
          //-------------------------------------------------------------------
          TraceHelper.trace("Deactivating and deleting Access Device");

          deleteAccessDeviceOrIDT(accDevReference);

          TraceHelper.trace("Deleted Access Device");
          TraceHelper.trace("Deactivating and deleting IDT");

          deleteAccessDeviceOrIDT(idtReference);

          TraceHelper.trace("Deleted IDT");
        }
      }
      else
      {
        TraceHelper.trace("Failed to find Connection Index.\n"
                          + "This is probably because there is no "
                          + "Connection object configured "
                          + "on the MetaView Server.");
      }
    }
    catch (ElementUnavailableException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if an object we are working with has
      // become unavailable.  If this occurs we will just exit.
      //-----------------------------------------------------------------------
      TraceHelper.trace("Caught ElementUnavailableException - exiting");
    }
    catch (org.omg.CORBA.COMM_FAILURE e)
    {
      System.err.println("Unable to login to the MetaView Server.");
      System.err.println("This may be because it is not running, or it is "
                         + "running in secure mode.");
    }
    catch (org.omg.CORBA.TRANSIENT e)
    {
      System.err.println("Transient communication failure with the MetaView "
                         + "Server.\nThis may be because it is not running, "
                         + "or it is running in secure mode,\nor there are "
                         + "problems with the network.");
    }
    catch (LoginFailedException e)
    {
      System.err.println("Unable to login to the MetaView Server.");
      System.err.println("Make sure that:\n"
                         + " - your MetaView Server is running in insecure mode\n"
                         + " - your username and password are correct.");
    }
    catch (IllegalStateException e)
    {
      e.printStackTrace();
      System.err.println(e.getMessage());
      System.exit(1);
    }
    catch (Exception e)
    {
      System.err.println("Unexpected exception: " + e.toString());
      e.printStackTrace();
      System.err.println(e.getMessage());
      System.exit(1);
    }
    finally
    {
      //-----------------------------------------------------------------------
      // Destroy the SEAccess.
      //-----------------------------------------------------------------------
      if (mTopLevelSEA != null)
      {
        mTopLevelSEA.destroy();
      }

      if (mConnSEA != null)
      {
        mConnSEA.destroy();
      }

      //-----------------------------------------------------------------------
      // Log out of the MetaView Server if we managed to log in above.
      //-----------------------------------------------------------------------
      mClientSession.logout();
      mClientSession = null;
      TraceHelper.trace("Logged out");
    }
  }

  /**
   * Creates an IDT on the IS whose connection has the index provided.
   *
   * @returns           A DualString containing a reference to the IDT that has
   *                    been created.
   *
   * @exception ElementUnavailableException
   */
  public DualString createIDT()
    throws ElementUnavailableException
  {
    SEAccessInterface idtSEA = null;
    DualString idtReference = null;

    try
    {
      idtSEA = mConnSEA.createElement(omlapi.O_IDT);

      //-----------------------------------------------------------------------
      // Obtain the settings.
      //-----------------------------------------------------------------------
      SettingsUserInterface settings = idtSEA.getSnapshot_Settings();

      //-----------------------------------------------------------------------
      // Setup the mandatory fields in the IDT Settings.
      //-----------------------------------------------------------------------

      //-----------------------------------------------------------------------
      // Access Device Number - use the first value returned by the PVP.
      //-----------------------------------------------------------------------
      DualString accessDeviceNumber =
           CorbaHelper.getFirstValueForPVPField(settings,
                                                omlapi.F_ACCESS_DEVICE_NUMBER);

      if (accessDeviceNumber == null)
      {
        //---------------------------------------------------------------------
        // If we've been returned null, then there are no valid values
        // available.  Throw an IllegalStateException to indicate that
        // something unexpected has occured.
        //---------------------------------------------------------------------
        throw new IllegalStateException("No possible values for Access Device "
                                        + "Number");
      }

      settings.setFieldAsStringByName(omlapi.F_ACCESS_DEVICE_NUMBER,
                                      accessDeviceNumber.internal);

      //-----------------------------------------------------------------------
      // Description - use the hardcoded values at the top of this class.
      //-----------------------------------------------------------------------
      settings.setFieldAsStringByName(omlapi.F_DESCRIPTION, sIDTDescription);

      //-----------------------------------------------------------------------
      // Apply the changes.  This results in the IDT object actually being
      // created.
      //-----------------------------------------------------------------------
      idtSEA.doAction(omlapi.A_APPLY);

      //-----------------------------------------------------------------------
      // Perform a final getSnapshot to get the element name and display name
      // of the IDT.
      //-----------------------------------------------------------------------
      idtReference = idtSEA.getSnapshot_Element();
    }
    //-------------------------------------------------------------------------
    // All CORBA UserExceptions are unexpected in this case (apart from
    // ElementUnavailableException).
    //-------------------------------------------------------------------------
    catch (NameUnknownException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (InvalidElementTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (CreationFailedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
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
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldNoRegisteredPVPException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadValueException e)
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
    finally
    {
      //-----------------------------------------------------------------------
      // Destroy the SEAccess.
      //-----------------------------------------------------------------------
      if (idtSEA != null)
      {
        idtSEA.destroy();
      }
    }

    return idtReference;
  }

  /**
   * Create an Access Device to use the specified IDT.
   *
   * @returns           A reference to the Access Device that has been created.
   *
   * @param idtReference
   *                    A reference to the IDT that we want to create an Access
   *                    Device for.
   *
   * @exception ElementUnavailableException
   */
  private DualString createAccessDeviceForIDT(DualString idtReference)
    throws ElementUnavailableException
  {
    SEAccessInterface accessDeviceSEA = null;
    DualString accDevReference = null;

    try
    {
      accessDeviceSEA = mConnSEA.createElement(
                                omlapi.O_TRUNK___ACCESS_GATEWAY_ACCESS_DEVICE);

      //-----------------------------------------------------------------------
      // None of the holders filled in by getSnaphot will contain useful values
      // until the object is successfully created, so just get the settings.
      //-----------------------------------------------------------------------
      SettingsUserInterface settings = accessDeviceSEA.getSnapshot_Settings();

      //-----------------------------------------------------------------------
      // Setup the mandatory fields in the Access Device Settings.
      //-----------------------------------------------------------------------

      //-----------------------------------------------------------------------
      // Access Hardware - use the reference that's been supplied.
      //-----------------------------------------------------------------------
      settings.setFieldAsReferenceByName(omlapi.F_ACCESS_HARDWARE,
                                         idtReference);

      //-----------------------------------------------------------------------
      // Apply the changes.
      //-----------------------------------------------------------------------
      accessDeviceSEA.doAction(omlapi.A_APPLY);

      //-----------------------------------------------------------------------
      // Perform a final getSnapshot to get the element name and display name
      // of the Access Device.
      //-----------------------------------------------------------------------
      accDevReference = accessDeviceSEA.getSnapshot_Element();
    }
    //-------------------------------------------------------------------------
    // All CORBA UserExceptions are unexpected in this case (apart from
    // ElementUnavailableException).
    //-------------------------------------------------------------------------
    catch (NameUnknownException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (InvalidElementTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (CreationFailedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
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
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadValueException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadTypeException e)
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
    finally
    {
      //-----------------------------------------------------------------------
      // Destroy the SEAccess.
      //-----------------------------------------------------------------------
      if (accessDeviceSEA != null)
      {
        accessDeviceSEA.destroy();
      }
    }

    return accDevReference;
  }

  /**
   * Creates an Individual Line on the specified Access Device.
   *
   * @returns           A DualString containing a reference to the Individual
   *                    Line that has been created.
   * @param accDevReference
   *                    A reference to the Access Device we want to create an
   *                    Individual Line on.
   *
   * @exception ElementUnavailableException
   */
  private DualString
                 createIndividualLineOnAccessDevice(DualString accDevReference)
    throws ElementUnavailableException
  {
    SEAccessInterface indLineSEA = null;
    DualString indLineReference = null;

    try
    {
      //-----------------------------------------------------------------------
      // Create a new Individual Line under our IS.
      //-----------------------------------------------------------------------
      indLineSEA = mConnSEA.createElement(omlapi.O_INDIVIDUAL_LINE);

      //-----------------------------------------------------------------------
      // None of the holders filled in by getSnaphot will contain useful values
      // until the object is successfully created, so just get the settings.
      //-----------------------------------------------------------------------
      SettingsUserInterface settings = indLineSEA.getSnapshot_Settings();

      //-----------------------------------------------------------------------
      // Setup the mandatory fields in the Individual Line Settings.
      //-----------------------------------------------------------------------

      //-----------------------------------------------------------------------
      // Directory Number - use the first value returned by the PVP.  The value
      // returned by this PVP is not a reference, but rather the first free dn
      // available on the IS, so we only need the internal value of the
      // reference when setting the Directory Number field.
      //-----------------------------------------------------------------------
      DualString directoryNumber =
               CorbaHelper.getFirstValueForPVPField(settings,
                                                    omlapi.F_DIRECTORY_NUMBER);

      if (directoryNumber == null)
      {
        //---------------------------------------------------------------------
        // If we've been returned null, then there are no valid values
        // available.  Throw an IllegalStateException to indicate that
        // something unexpected has occured.
        //---------------------------------------------------------------------
        throw new IllegalStateException("No possible values for Directory "
                                        + "Number");
      }

      settings.setFieldAsStringByName(omlapi.F_DIRECTORY_NUMBER,
                                      directoryNumber.internal);

      //-----------------------------------------------------------------------
      // Subscriber Group - Use the first value returned by the PVP.
      //-----------------------------------------------------------------------
      DualString subscrGroup =
               CorbaHelper.getFirstValueForPVPField(settings,
                                                    omlapi.F_SUBSCRIBER_GROUP);

      if (subscrGroup == null)
      {
        //---------------------------------------------------------------------
        // If we've been returned null, then there are no valid values
        // available.  Throw an IllegalStateException to indicate that
        // something unexpected has occured.
        //---------------------------------------------------------------------
        throw new IllegalStateException("No possible values for Subscriber "
                                        + "Group");
      }

      settings.setFieldAsReferenceByName(omlapi.F_SUBSCRIBER_GROUP,
                                         subscrGroup);

      //-----------------------------------------------------------------------
      // Number Status - use the hardcoded values at the top of this class.
      //-----------------------------------------------------------------------
      settings.setFieldAsIntByName(omlapi.F_NUMBER_STATUS, sIndLineNumStatus);

      //-----------------------------------------------------------------------
      // Access Device - Use the reference that's been supplied.
      //-----------------------------------------------------------------------
      settings.setFieldAsReferenceByName(omlapi.F_ACCESS_DEVICE,
                                         accDevReference);

      //-----------------------------------------------------------------------
      // Access Line number - Use the first value returned by the PVP.
      //-----------------------------------------------------------------------
      DualString accLineNumber =
             CorbaHelper.getFirstValueForPVPField(settings,
                                                  omlapi.F_ACCESS_LINE_NUMBER);

      if (accLineNumber == null)
      {
        //---------------------------------------------------------------------
        // If we've been returned null, then there are no valid values
        // available.  Throw an IllegalStateException to indicate that
        // something unexpected has occured.
        //---------------------------------------------------------------------
        throw new IllegalStateException("No possible values for Access Line "
                                        + "Number");
      }

      settings.setFieldAsStringByName(omlapi.F_ACCESS_LINE_NUMBER,
                                      accLineNumber.internal);

      //-----------------------------------------------------------------------
      // Locale - use the hardcoded values at the top of this class.
      //-----------------------------------------------------------------------
      settings.setFieldAsIntByName(omlapi.F_LOCALE, sIndLineLocale);

      //-----------------------------------------------------------------------
      // Apply the changes.
      //-----------------------------------------------------------------------
      indLineSEA.doAction(omlapi.A_APPLY);

      //-----------------------------------------------------------------------
      // Perform a final getSnapshot to get the element name and display name
      // of the Individual Line.
      //-----------------------------------------------------------------------
      indLineReference = indLineSEA.getSnapshot_Element();
    }
    //-------------------------------------------------------------------------
    // All CORBA UserExceptions are unexpected in this case (apart from
    // ElementUnavailableException).
    //-------------------------------------------------------------------------
    catch (NameUnknownException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (InvalidElementTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (CreationFailedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
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
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldNoRegisteredPVPException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadValueException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadTypeException e)
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
    finally
    {
      //-----------------------------------------------------------------------
      // Destroy the SEAccess.
      //-----------------------------------------------------------------------
      if (indLineSEA != null)
      {
        indLineSEA.destroy();
      }
    }

    return indLineReference;
  }

  /**
   * Deletes the specified Individual Line.
   *
   * @param indLineReference
   *                    A reference to the Individual Line that is to be
   *                    deleted.
   *
   * @exception ElementUnavailableException
   */
  private void deleteIndividualLine(DualString indLineReference)
    throws ElementUnavailableException
  {
    SEAccessInterface seAccess = null;

    try
    {
      //-----------------------------------------------------------------------
      // Create an SEAccess to attach to the Individual Line.
      //-----------------------------------------------------------------------
      seAccess = mClientSession.createSEAccess();

      //-----------------------------------------------------------------------
      // Attach to the Individual Line.
      //-----------------------------------------------------------------------
      seAccess.attachTo(indLineReference.internal);

      //-----------------------------------------------------------------------
      // Disable and delete the element.
      //-----------------------------------------------------------------------
      disableAndDeleteElement(seAccess);
    }
    catch (ElementDeletedException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if the Individual Line does not exist.
      // If this occurs, there is no problem, because we're trying to delete it
      // anyway.
      //-----------------------------------------------------------------------
      TraceHelper.trace("Element already deleted: "
                        + indLineReference.internal);
    }
    //-------------------------------------------------------------------------
    // All other CORBA UserExceptions are unexpected in this case (apart from
    // ElementUnavailableException).
    //-------------------------------------------------------------------------
    catch (InvalidNameException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (AlreadyAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementOperationFailedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    finally
    {
      //-----------------------------------------------------------------------
      // Destroy the SEAccess.
      //-----------------------------------------------------------------------
      if (seAccess != null)
      {
        seAccess.destroy();
      }
    }
  }

  /**
   * Deletes the specified Access Device or IDT.
   *
   * @param accDevOrIDTReference
   *                    A reference to the Access Device or IDT that is to be
   *                    deleted.
   *
   * @exception ElementUnavailableException
   */
  private void deleteAccessDeviceOrIDT(DualString accDevOrIDTReference)
    throws ElementUnavailableException
  {
    SEAccessInterface seAccess = null;

    try
    {
      //-----------------------------------------------------------------------
      // Create an SEAccess.
      //-----------------------------------------------------------------------
      seAccess = mClientSession.createSEAccess();

      //-----------------------------------------------------------------------
      // Attach to the Access Device or IDT.
      //-----------------------------------------------------------------------
      seAccess.attachTo(accDevOrIDTReference.internal);

      //-----------------------------------------------------------------------
      // Deactivate the Access Device or IDT.  Note that deactivation is an
      // asynchronous operation, and this method will block until deactivation
      // completes.
      //-----------------------------------------------------------------------
      TraceHelper.trace("  Deactivating element");

      deactivateElement(seAccess);

      TraceHelper.trace("  Deactivated element");

      //-----------------------------------------------------------------------
      // Disable and delete the Access Device or IDT.
      //-----------------------------------------------------------------------
      disableAndDeleteElement(seAccess);

      TraceHelper.trace("  Deleted element");
    }
    catch (ElementDeletedException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if the element does not exist.  If this
      // occurs, there is no problem, because we're trying to delete it anyway.
      //-----------------------------------------------------------------------
      TraceHelper.trace("  Element already deleted: "
                        + accDevOrIDTReference.internal);
    }
    //-------------------------------------------------------------------------
    // All other CORBA UserExceptions are unexpected in this case (apart from
    // ElementUnavailableException).
    //-------------------------------------------------------------------------
    catch (AlreadyAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (InvalidNameException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementOperationFailedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    finally
    {
      //-----------------------------------------------------------------------
      // Destroy the SEAccess.
      //-----------------------------------------------------------------------
      if (seAccess != null)
      {
        seAccess.destroy();
      }
    }
  }

  /**
   * Disables and deletes the specified element.
   *
   * @param seAccess    An SEAccess that is attached to the Individual Line,
   *                    Access Device or IDT that should be deleted.
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
      seAccess.getSnapshot(new SequenceOfIntegersHolder(),
                           new StringHolder(),
                           new StringHolder(),
                           new SequenceOfReferencesHolder());

      //-----------------------------------------------------------------------
      // The element has to be disabled before it can be deleted (if required).
      //-----------------------------------------------------------------------
      if (isActualStatusEnabled(seAccess) ||
          isActualStatusInactive(seAccess))
      {
        seAccess.doAction(omlapi.A_DISABLE);
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
   * @param seAccess    An SEAccess that is attached to the Access Device or
   *                    IDT that should be deactivated.
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
      // Deactivate the object (if required).
      //-----------------------------------------------------------------------
      if (isActualStatusActive(seAccess))
      {
        seAccess.doAction(omlapi.A_DEACTIVATE);

        //---------------------------------------------------------------------
        // Check whether the action has completed immediately - if it has, we
        // can return immediately.  Otherwise, we need to wait until the
        // snapshot change listener interrupts us.  The Deactivate action is
        // complete when the Disable action becomes available.
        //---------------------------------------------------------------------
        boolean actualStatusInactive = isActualStatusInactive(seAccess);
        if (!actualStatusInactive)
        {
          //-------------------------------------------------------------------
          // The deactivation did not complete immediately, so wait for the
          // snapshot change listener to interrupt us, then return.  If the
          // operation takes more than 30 seconds, then something has probably
          // gone wrong.
          //-------------------------------------------------------------------
          try
          {
            TraceHelper.trace("    Waiting for deactivation to complete...");
            Thread.currentThread().sleep(30000);
            TraceHelper.trace("    Deactivation failed to complete in 30 "
                              + "seconds!");
          }
          catch (InterruptedException e)
          {
            //-----------------------------------------------------------------
            // We assume that the only thread that will interrupt us is the one
            // from the snapshot change notification.
            //-----------------------------------------------------------------
            TraceHelper.trace("    Deactivation complete.");
          }
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
    boolean actualStatusInactive = isActualStatus(seAccess,
                                                  omlapi.V_INACTIVE_2);

    return actualStatusInactive;
  }

  /**
   * Perform a getSnapshot on the SEAccess provided, and check whether the
   * value of the Actual status field is Active.
   *
   * @returns           true if the Actual status is Active, false otherwise.
   *
   * @param seAccess    The SEAccessInterface to call getSnapshot on.
   *
   * @exception ElementUnavailableException
   */
  private boolean isActualStatusActive(SEAccessInterface seAccess)
    throws ElementUnavailableException
  {
    boolean actualStatusActive = isActualStatus(seAccess,
                                                omlapi.V_ACTIVE);

    return actualStatusActive;
  }

  /**
   * Perform a getSnapshot on the SEAccess provided, and check whether the
   * value of the Actual status field is Enabled.
   *
   * @returns           true if the Actual status is Enabled, false otherwise.
   *
   * @param seAccess    The SEAccessInterface to call getSnapshot on.
   *
   * @exception ElementUnavailableException
   */
  private boolean isActualStatusEnabled(SEAccessInterface seAccess)
    throws ElementUnavailableException
  {
    boolean actualStatusEnabled = isActualStatus(seAccess,
                                                 omlapi.V_ENABLED_2);

    return actualStatusEnabled;
  }

  /**
   * Perform a getSnapshot on the SEAccess provided, and check whether the
   * value of the Actual status field matches.
   *
   * @return            true if the Actual status matches, false otherwise.
   *
   * @param seAccess    The SEAccessInterface to call getSnapshot on.
   * @param status      The required actual status.
   *
   * @exception ElementUnavailableException
   */
  private boolean isActualStatus(SEAccessInterface seAccess, int status)
    throws ElementUnavailableException
  {
    boolean actualStatusMatches = false;

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
      actualStatusMatches = (isAssigned.value &&
                             (actualStatus == status));
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

    return actualStatusMatches;
  }
}
