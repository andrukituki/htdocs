/**
 * Title: CorbaHelper
 *
 * Description: A utility class providing methods to
 *              -  login to a MetaView Server
 *              -  traverse the MetaView tree
 *
 * (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
 *
 * @version 1.0
 */
package com.Metaswitch.MVS.Utils;

import java.lang.reflect.*;

import org.jacorbOverwrite.orb.JacorbClientProfileSelector;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import com.Metaswitch.MVS.Corba.*;

public class CorbaHelper
{
  //---------------------------------------------------------------------------
  // The ports to use when logging into the MetaView Server in insecure mode
  // and secure mode.
  //---------------------------------------------------------------------------
  private static final String INSECURE_PORT = "8004";
  private static final String BIDIR_PORT = "8005";
  private static final String SECURE_PORT = "8006";

  //---------------------------------------------------------------------------
  // Fixed value for the empty filter, for use with getPossibleValues.
  //---------------------------------------------------------------------------
  private static final String S_EMPTY_FILTER = "";

  //---------------------------------------------------------------------------
  // Flag to indicate whether we will log in at a single version or a range of
  // versions, and the corresponding supported versions format identifier.
  //---------------------------------------------------------------------------
  private static final boolean sSupportRangeOfVersions = true;
  private static final int sSupportedVersionsFormat =
    (sSupportRangeOfVersions ? SUPPORT_FWD_COMPATIBLE_RANGE_OF_VERSIONS.value :
                               SUPPORT_FWD_COMPATIBLE_SINGLE_VERSION.value);

  //---------------------------------------------------------------------------
  // The version or range of versions that we will log in at.
  //---------------------------------------------------------------------------
  private static final int sLowestVersion  =
                                    ClientSessionManagerInterface.VERSION_1_04;
  private static final int sHighestVersion =
                                  ClientSessionManagerInterface.VERSION_9_6_70;
  private static final int[] sSupportedVersions =
       (sSupportRangeOfVersions ? new int[] {sLowestVersion, sHighestVersion} :
                                  new int[] {sHighestVersion});

  //---------------------------------------------------------------------------
  // The ORB that we will use for communicating with the server over CORBA.
  //---------------------------------------------------------------------------
  protected static ORB sOrb;

  //---------------------------------------------------------------------------
  // A flag to track whether the ORB is running.
  //---------------------------------------------------------------------------
  protected static boolean sOrbRunning = false;

  //---------------------------------------------------------------------------
  // An object to synchronize on when accessing the sOrb object or the
  // sOrbRunning flag.
  //---------------------------------------------------------------------------
  protected static java.lang.Object sOrbLock = new java.lang.Object();

  //---------------------------------------------------------------------------
  // Booleans to indicate whether we are running in secure mode and whether we
  // will perform a user login or an application login.
  //
  // For insecure applications, sSecure will always be false and sIsUserLogin
  // will always be true.  Secure applications, using the Jacorb ORB, use the
  // subclass CorbaHelperSecure.  However the subclass accesses these variables
  // and they are used in this superclass, so it is necessary to have them
  // here.
  //---------------------------------------------------------------------------
  protected static boolean sSecure = false;
  protected static boolean sIsUserLogin = false;

  //---------------------------------------------------------------------------
  // A string to hold either the IP address or the hostname of the MetaView
  // Server.
  //---------------------------------------------------------------------------
  private static String sEMSAddress;

  //---------------------------------------------------------------------------
  // A ClientSessionManagerInterface object, initialized during login.
  //---------------------------------------------------------------------------
  private static ClientSessionManagerInterface sClientSessionManager = null;

  //---------------------------------------------------------------------------
  // A Thread that regularly polls the MetaView Server to keep our connection
  // alive.  This is created by startPolling() and destroyed by stopPolling().
  //---------------------------------------------------------------------------
  private static Thread sPollingThread = null;

  /**
   * Get the ORB that we are using, for use when generating CORBA references.
   *
   * @returns           A reference to the ORB.
   */
  public static ORB getORB()
  {
    synchronized (sOrbLock)
    {
      return sOrb;
    }
  }

  /**
   * Start the ORB in preparation for an insecure user login.  No CORBA request
   * will work until an ORB is started.
   */
  public static void startORB()
  {
    sIsUserLogin = true;

    try
    {
      //-----------------------------------------------------------------------
      // Initialise the ORB in insecure mode.
      //-----------------------------------------------------------------------
      sOrb = ORB.init((String[])null, null);

      POA rootPOA =
                  POAHelper.narrow(sOrb.resolve_initial_references("RootPOA"));

      POAManager rootPOAManager = rootPOA.the_POAManager();

      rootPOAManager.activate();

      sOrbRunning = true;
    }
    catch (Exception e)
    {
      //-----------------------------------------------------------------------
      // Any exceptions not caught elsewhere in the code above are caught here.
      //-----------------------------------------------------------------------
      e.printStackTrace();
      throw new IllegalStateException("ORB Initialization failed");
    }
  }

  /**
   * Stop the ORB.
   */
  public static void stopORB()
  {
    synchronized (sOrbLock)
    {
      if (sOrbRunning)
      {
        try
        {
          //-------------------------------------------------------------------
          // Shut down and destroy the ORB.
          //-------------------------------------------------------------------
          sOrb.destroy();
          sOrb = null;
          sOrbRunning = false;
        }
        catch (Exception e)
        {
          e.printStackTrace();
          System.err.println("ORB shutdown failed");
          System.exit(1);
        }
      }
    }
  }

  /**
   * Used to perform an application login to the MetaView Server.  Calls the main
   * method below, with username and password set to null.  Whether a user
   * login or an application login is actually performed depends on the value
   * of the sIsUserLogin variable, which is set in the startORB method.
   * However, if this login method is used to perform a user login an exception
   * will be thrown, as no username and password are provided.
   *
   * @returns           A ClientSession to the MetaView Server.
   *
   * @param emsAddress
   *                    The IP address / hostname of the MetaView Server to login
   *                    to.
   *
   * @exception LoginFailedException
   */
  public static ClientSessionInterface login(String emsAddress)
    throws LoginFailedException
  {
    return login(emsAddress, null, null);
  }

  /**
   * Login to the MetaView Server and get a reference to a Client Session.
   * Either a user login or an application login may be performed, depending on
   * the value of sIsUserLogin, which is set by the startORB method.
   *
   * @returns           A ClientSession to the MetaView Server.
   *
   * @param emsAddress
   *                    The IP address / hostname of the MetaView Server to
   *                    login to.
   * @param username    Username.
   * @param password    Password.
   *
   * @exception LoginFailedException
   */
  public static ClientSessionInterface login(String emsAddress,
                                             String username,
                                             String password)
    throws LoginFailedException
  {
    //-------------------------------------------------------------------------
    // Initialize the ClientSessionInterface object.
    //-------------------------------------------------------------------------
    ClientSessionInterface clientSession = null;

    //-------------------------------------------------------------------------
    // Set the static EMS address variable from the passed parameter.
    //-------------------------------------------------------------------------
    sEMSAddress = emsAddress;

    try
    {
      //-----------------------------------------------------------------------
      // Store the ClientSessionManagerInterface object for later use.
      //-----------------------------------------------------------------------
      sClientSessionManager = getClientSessionManagerInterface();

      //-----------------------------------------------------------------------
      // Perform either a user login (using the username and password
      // specified) or an application login, at the version(s) defined above,
      // using the Client Session Manager.  Store the returned reference to the
      // new Client Session.
      //-----------------------------------------------------------------------

      if (sIsUserLogin)
      {
        if ((username == null) || (password == null))
        {
          //-------------------------------------------------------------------
          // User is attempting to perform a user login without providing a
          // username or password - this won't work, so throw an exception.
          //-------------------------------------------------------------------
          throw new IllegalStateException(
            "Error - a username and password must be provided to perform a "
              + "user login. Either:\n"
              + " - Provide a username and password, or\n"
              + " - Perform an application login, providing appropriate "
              + "certificates.");

        }

        clientSession = sClientSessionManager.userLogin(
                                                      username,
                                                      password,
                                                      sSupportedVersionsFormat,
                                                      sSupportedVersions);
      }
      else
      {
        clientSession = sClientSessionManager.applicationLogin(
                            ClientSessionManagerInterface.USER_TYPE_SUPER_USER,
                            sSupportedVersionsFormat,
                            sSupportedVersions);
      }
    }
    catch (VersionNotSupportedException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if we have successfully communicated
      // with the MetaView Server, but it does not support the version we have
      // passed.  If this occurs throw an IllegalStateException.
      //-----------------------------------------------------------------------
      throw new IllegalStateException(
                                 "Version not supported by MetaView Server at "
                                 + sEMSAddress);
    }
    catch (VersionFormatException e)
    {
      //-----------------------------------------------------------------------
      // This is exception will be thrown if the format of the version
      // parameter specified in the User Login call is incorrectly formatted.
      // If this occurs throw an IllegalStateException to indicate that
      // something unexpected has occurred.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("Version Format Exception thrown!");
    }

    return clientSession;
  }

  /**
   * Start a thread that regularly polls the MetaView Server to keep the
   * connection alive.  Call stopPolling() to stop this thread.
   *
   * @param clientSession
   *                    The ClientSessionInterface to poll.
   */
  public static void startPolling(final ClientSessionInterface clientSession)
  {
    //-------------------------------------------------------------------------
    // We spawn a new Thread that simply calls the keepAlive() method of the
    // ClientSessionInterface, sleeps for a while, then repeats.
    //
    // The time after which we will be logged out by the MetaView Server if we
    // don't call the keepAlive() method is returned by getTimeoutPeriod(), so
    // call this and use 4/5 of the returned value as the sleep time.
    //-------------------------------------------------------------------------
    final long sleepTime = clientSession.getTimeoutPeriod() * 4 / 5;

    sPollingThread = new Thread()
      {
        public void run()
        {
          //-------------------------------------------------------------------
          // Poll the MetaView Server until this thread is interrupted or the
          // keepAlive() method throws an exception.
          //-------------------------------------------------------------------
          boolean keepPolling = true;

          while (keepPolling)
          {
            try
            {
              //---------------------------------------------------------------
              // Do the poll.
              //---------------------------------------------------------------
              clientSession.keepAlive();

              //---------------------------------------------------------------
              // Sleep for the required time.
              //---------------------------------------------------------------
              sleep(sleepTime);
            }
            catch (InterruptedException e)
            {
              //---------------------------------------------------------------
              // If we are interrupted, this means that the stopPolling()
              // method has been called and we should stop polling.
              //---------------------------------------------------------------
              keepPolling = false;
            }
            catch (ElementOperationFailedException e)
            {
              //---------------------------------------------------------------
              // The keepAlive() method throws this exception if we are not
              // logged in.  This is unrecoverable, as logging in again will
              // return a new ClientSession, so stop the thread.
              //---------------------------------------------------------------
              keepPolling = false;
            }
          }
        }
      };

    //-------------------------------------------------------------------------
    // Start the thread.
    //-------------------------------------------------------------------------
    sPollingThread.start();
  }

  /**
   * Stop the thread that regularly polls the MetaView Server to keep the connection
   * alive.  startPolling() must have been called previously.
   */
  public static void stopPolling()
  {
    //-------------------------------------------------------------------------
    // Interrupt the polling thread, which will cause it to stop running.
    //-------------------------------------------------------------------------
    sPollingThread.interrupt();

    //-------------------------------------------------------------------------
    // Wait for the polling thread to die properly.
    //-------------------------------------------------------------------------
    while (sPollingThread.isAlive())
    {
      try
      {
        Thread.currentThread().sleep(10);
      }
      catch (InterruptedException e)
      {
      }
    }

    sPollingThread = null;
  }

  /**
   * Attach to the child of the specified type, and with the specified indices,
   * below the specified parent.
   *
   * @returns           An SEA attached to the required child, or null if no
   *                    such child exists.
   *
   * @param clientSession
   *                    A clientSessionInterface which can be used to create
   *                    new SEAs.
   * @param parentSEA   An SEA attached to the parent of the SE we want to
   *                    attach to.
   * @param childTypeWithIndices
   *                    A string array. The first entry in the array contains
   *                    the required child type. The subsequent entries contain
   *                    the indices of that child.
   */
  public static SEAccessInterface getChildOfTypeWithIndices(
                                          ClientSessionInterface clientSession,
                                          SEAccessInterface parentSEA,
                                          String[] childTypeWithIndices)
    throws ElementOperationFailedException,
           BadIndicesException,
           InvalidElementTypeException,
           ElementDeletedException
  {
    //-------------------------------------------------------------------------
    // Obtain the parent's indices.
    //-------------------------------------------------------------------------
    String[] parentIndices = parentSEA.getIndices();

    //-------------------------------------------------------------------------
    // We want to create a larger array containing the indices of both the
    // parent and the child - this will be used to attach to the child using
    // attachToWithIndices.  Therefore we need first to make an array of the
    // appropriate size, and then to copy in data.
    //
    // Note that the childTypeWithIndices array has the element type in entry
    // [0], and indices in subsequent entries.
    //-------------------------------------------------------------------------
    String[] completeIndices = new String[parentIndices.length +
                                              childTypeWithIndices.length - 1];

    System.arraycopy(parentIndices,
                     0,
                     completeIndices,
                     0,
                     parentIndices.length);

    System.arraycopy(childTypeWithIndices,
                     1,
                     completeIndices,
                     parentIndices.length,
                     childTypeWithIndices.length - 1);

    //-------------------------------------------------------------------------
    // Initialize a new SEA, which will be attached the the child SE.
    //-------------------------------------------------------------------------
    SEAccessInterface childSEA = null;

    try
    {
      //-----------------------------------------------------------------------
      // Create the new SEA using the SEA factory object.
      //-----------------------------------------------------------------------
      childSEA = clientSession.createSEAccess();

      //-----------------------------------------------------------------------
      // Attach the new SEA to the child.
      //-----------------------------------------------------------------------
      childSEA.attachToWithIndices(completeIndices, childTypeWithIndices[0]);
    }
    catch (AlreadyAttachedException e)
    {
      //-----------------------------------------------------------------------
      // This exception is thrown during the attachToWithIndices call if the
      // childSEA is already attached.  However, we've only just created the
      // SEAccessInterface, so throw an IllegalStateException to indicate that
      // something unexpected has occurred.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("Newly created SEAccessInterface is "
                                      + "already attached!");
    }

    return childSEA;
  }

  /**
   * Get the first (non-filter) value from a PVP's possible values.
   *
   * @returns           A DualString defining the first non-filter value, or
   *                    null if there are no possible values.
   *
   * @param settings    The settings that the PVP field is contained in.
   * @param fieldName   The name of the field that a value is required for.
   *
   * @exception FieldNameOrIndexNotFoundException
   * @exception FieldNoRegisteredPVPException
   * @exception ElementDeletedException
   * @exception ElementOperationFailedException
   * @exception LockTimeoutException
   * @exception ElementUnavailableException
   * @exception ElementBrokenException
   */
  public static DualString
                       getFirstValueForPVPField(SettingsUserInterface settings,
                                                String fieldName)
    throws FieldNameOrIndexNotFoundException,
           FieldNoRegisteredPVPException,
           ElementDeletedException,
           ElementOperationFailedException,
           LockTimeoutException,
           ElementUnavailableException,
           ElementBrokenException
  {
    BooleanHolder hasFilters = new BooleanHolder(true);
    BooleanHolder moreValues = new BooleanHolder();
    IntHolder numEntries = new IntHolder();
    String filter = S_EMPTY_FILTER;
    DualString firstValue = null;

    try
    {
      while (hasFilters.value)
      {
        DualString[] possibleValues =
                 settings.getPossibleValuesAsStringsByName(fieldName,
                                                           filter,
                                                           1,
                                                           moreValues,
                                                           hasFilters,
                                                           numEntries,
                                                           new StringHolder());

        if ((numEntries.value > 0))
        {
          if (hasFilters.value)
          {
            //-----------------------------------------------------------------
            // If we've been returned filters, then get the internal name of
            // the first one and put it back into our call to
            // getPossibleValues.
            //-----------------------------------------------------------------
            filter = possibleValues[0].internal;
          }
          else
          {

            //-----------------------------------------------------------------
            // If we've been returned real values, then save the first one off.
            //-----------------------------------------------------------------
            firstValue = possibleValues[0];
          }
        }
        else
        {
          //-------------------------------------------------------------------
          // Set hasFilters to false so that we'll exit the loop and return
          // null.
          //-------------------------------------------------------------------
          hasFilters.value = false;
        }
      }
    }
    catch (InvalidFilterException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if the filter we are supplying to
      // getPossibleValues is not valid.  However, either it is an empty
      // string, which we know is valid, or it is a filter which we have just
      // been returned.  In either case, throw an IllegalStateException to
      // indicate that something unexpected has occurred.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("Invalid Filter Exception thrown!");
    }
    catch (InvalidNameException e)
    {
      //-----------------------------------------------------------------------
      // There is currently no reason why this exception should be thrown by
      // the MetaView Server in this scenario, so throw an
      // IllegalStateException to indicate that something unexpected has
      // occurred.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("Invalid Name Exception thrown!");
    }

    return firstValue;
  }

  /**
   * Translates a supplied NLS string into human readable text.
   *
   * @returns           The human readable translation of the NLS string. If
   *                    the translation is unsuccessful, the original string
   *                    will be returned instead.
   *
   * @param nlsString   The NLS string to translate.
   * @param translationSuccessful
   *                    This boolean holder will be returned with a value
   *                    indicating whether the translation was successful.
   */
  public static String nlsTranslate(String nlsString,
                                    BooleanHolder translationSuccessful)
  {
    NLSLocale locale = null;

    //-------------------------------------------------------------------------
    // Create an NLSSupportInterface object, which will perform the
    // translations.
    //-------------------------------------------------------------------------
    NLSSupportInterface nLSSupport = sClientSessionManager.getNLSSupport();

    //-------------------------------------------------------------------------
    // Retrieve the locale information, so that translation can be performed
    // in the correct language.
    //-------------------------------------------------------------------------
    try
    {
      locale = nLSSupport.getLocale("en", "US");
    }
    catch (UnsupportedLocaleException e)
    {
      //-----------------------------------------------------------------------
      // The locale provided is unsupported.  This is an unexpected error, as
      // the language "en" (English) and the country "US" are always supported.
      // Therefore throw an IllegalStateException.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("United States locale not supported!");
    }

    //-------------------------------------------------------------------------
    // Produce the human-readable text from the supplied NLS string.
    //-------------------------------------------------------------------------
    String translatedText = nLSSupport.makeDisplayText(nlsString,
                                                       locale,
                                                       translationSuccessful);

    //-------------------------------------------------------------------------
    // Return the human readable text.
    //-------------------------------------------------------------------------
    return translatedText;
  }

  /**
   * Handle a CORBA UserException that is not expected.  This method prints the
   * stack trace of the exception to the standard error stream, then throws an
   * IllegalStateException containing text describing the problem that has
   * occurred.  All CORBA UserExceptions that can be thrown by MetaView CORBA
   * calls are converted into IllegalStateException.  All other CORBA
   * UserExceptions are also converted to IllegalStateException, with a generic
   * piece of text explaining that these exceptions should be handled
   * separately.
   *
   * NOTE THAT ANY EXCEPTIONS THAT CAN BE THROWN FOR LEGITIMATE REASONS, FOR
   * EXAMPLE ElementUnavailableException, SHOULD NOT BE PASSED TO THIS METHOD
   * FOR HANDLING.
   *
   * @param exception   The unexpected exception to handle.
   */
  public static void handleUnexpectedUserException(UserException exception)
  {
    try
    {
      //-----------------------------------------------------------------------
      // Print the stack trace of the exception to the standard error stream.
      //-----------------------------------------------------------------------
      System.err.println("An unexpected CORBA UserException has occurred: ["
                         + exception.toString()
                         + "].  Original stack trace follows.");
      exception.printStackTrace();

      //-----------------------------------------------------------------------
      // Throw the exception that we have been passed, and handle it below.  If
      // the exception is expected to occur for any reason, it should not be
      // passed to this method.
      //-----------------------------------------------------------------------
      throw exception;
    }
    //-------------------------------------------------------------------------
    // Element exceptions.  Note that we do not handle
    // ElementUnavailableException here, because it is always an "expected"
    // exception - i.e.  we can never guarantee that elements will be
    // available.
    //-------------------------------------------------------------------------
    catch (ElementDeletedException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if an object that is being attached to
      // or an action is being performed on does not exist.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("ElementDeletedException thrown - an "
             + "element does not exist."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
  }
    catch (ElementBrokenException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if an element is broken.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("ElementBrokenException thrown - an "
             + "element is broken."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    catch (ElementChangedException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown by doAction if the object we are
      // performing the action on has been changed by another SEA since a
      // snapshot was taken.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("ElementChangedException thrown - "
             + "another SEAccess has changed the element that we were "
             + "trying to change."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    catch (ElementOperationFailedException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown by the MetaView Server if a request has
      // failed for any reason that is not better described by another element
      // exception.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("ElementOperationFailedException thrown "
             + "- a request has failed."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    catch (CreationFailedException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if the creation or an object has failed
      // for some reason.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("CreationFailedException thrown - the "
             + "creation of an object has failed."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    catch (InvalidElementTypeException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if the element type we've supplied is
      // invalid.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("InvalidElementTypeException thrown - "
             + "we have supplied an invalid element "
             + "type."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    //-------------------------------------------------------------------------
    // Element name and indices exceptions.
    //-------------------------------------------------------------------------
    catch (InvalidNameException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if the name we've supplied for an object
      // is incorrect (e.g.  wrong type).
      //-----------------------------------------------------------------------
      throw new IllegalStateException("InvalidNameException thrown - we have "
             + "supplied an invalid name for an "
             + "object."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    catch (BadIndicesException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if the indices we've supplied for an
      // object are incorrect (e.g.  wrong length).
      //-----------------------------------------------------------------------
      throw new IllegalStateException("BadIndicesException thrown - we have "
             + "supplied invalid indices for an "
             + "object."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    //-------------------------------------------------------------------------
    // Attachment exceptions.
    //-------------------------------------------------------------------------
    catch (AlreadyAttachedException e)
    {
      //-----------------------------------------------------------------------
      // This exception is thrown during an attachTo or attachToNew call if the
      // SEAccess is already attached.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("AlreadyAttachedException thrown - an "
             + "SEAccessInterface that we are trying to attach to an SE is "
             + "already attached."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    catch (NotAttachedException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown by doAction if the SEA we are performing
      // doAction on is not attached to an object.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("NotAttachedException thrown - the "
             + "SEAccess that we are performing an action on is not "
             + "attached to an SE."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    //-------------------------------------------------------------------------
    // Locking exceptions.
    //-------------------------------------------------------------------------
    catch (ElementAlreadyLockedException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if we try to lock an element more than
      // once.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("ElementAlreadyLockedException thrown - "
             + "the element was already locked when we tried to lock it."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    catch (LockTimeoutException e)
    {
      //-----------------------------------------------------------------------
      // This exception may be thrown if we have explicitly locked an SE and
      // that lock has expired after a fixed period (30 seconds).
      //-----------------------------------------------------------------------
      throw new IllegalStateException("LockTimeoutException thrown - our lock "
             + "on an SE has expired."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    //-------------------------------------------------------------------------
    // Settings field exceptions.
    //-------------------------------------------------------------------------
    catch (SettingsFieldException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if one or more of the fields in the
      // Settings being updated contains an invalid value.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("SettingsFieldException thrown for "
             + "field " + e.erroredField + " - the value we have tried to set "
             + "is not valid."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    catch (FieldNameOrIndexNotFoundException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if any of the settings operations we've
      // performed have been on fields that are not in the object we are
      // operating on.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("FieldNameOrIndexNotFoundException "
             + "thrown - the field we are trying to operate on does not exist."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    catch (FieldBadValueException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if any of the values we have set in
      // settings fields are invalid.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("FieldBadValueException thrown - we "
             + "have tried to set an invalid field value."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    catch (FieldBadTypeException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if we are trying to set a field with a
      // value of the wrong type.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("FieldBadTypeException thrown - we have "
             + "tried to set a field with a value of the wrong type."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    catch (FieldNoRegisteredPVPException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown by a call to getPossibleValues if the
      // field we have supplied does not have a Possible Values Provider.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("FieldNoRegisteredPVPException thrown - "
             + "we have tried to get the possible values for a field with no "
             + "possible values provider."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    catch (FieldNoRegisteredAPException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown by a call to getAdjustments if the field
      // we have supplied does not have an Adjustments Provider.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("FieldNoRegisteredAPException thrown - "
             + "we have tried to get the adjustments for a field with no "
             + "adjustments provider."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    //-------------------------------------------------------------------------
    // Action exceptions.
    //-------------------------------------------------------------------------
    catch (UnknownActionException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if an action that we are trying to
      // perform is not supported by the object.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("UnknownActionException thrown - we "
             + "have tried to perform an action that the object does not "
             + "support."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    catch (ActionNotEnabledException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if an action that we are attempting is
      // not enabled.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("ActionNotEnabledException thrown - we "
             + "have tried to perform an action that is not currently "
             + "enabled."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    //-------------------------------------------------------------------------
    // Snapshot change listener exceptions.
    //-------------------------------------------------------------------------
    catch (TooManySnapshotChangeListenersException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if we try to register more than one
      // snapshot change listener on an SEAccess.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("TooManySnapshotChangeListenersException"
             + " thrown - we have tried to register more than one snapshot "
             + "change listener on an SEAccess."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    catch (NoSnapshotChangeListenersException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if we try to un-register a snapshot
      // change listener when there are no registered snapshot change
      // listeners.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("NoSnapshotChangeListenersException "
             + "thrown - we have tried to un-register a snapshot change "
             + "listener when there are none registered."
             + "  NLS text: " + e.nlsText
             + ", translation: " + nlsTranslate(e.nlsText, new BooleanHolder())
             + ", message: " + e.getMessage());
    }
    //-------------------------------------------------------------------------
    // Other CORBA UserExceptions.
    //-------------------------------------------------------------------------
    catch (UserException e)
    {
      //-----------------------------------------------------------------------
      // If we get here, this indicates an internal error because we should
      // have handled the exception earlier.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("Unknown UserException thrown - "
                                      + "this indicates an internal error.  "
                                      + "All the UserException types that can "
                                      + "be thrown should be handled "
                                      + "separately."
                                      + "  Exception: " + e.toString()
                                      + ", message: " + e.getMessage());
    }
  }

  //---------------------------------------------------------------------------
  //
  // Internal helper methods
  //
  //---------------------------------------------------------------------------

  /**
   * Returns a reference to a ClientSessionManagerInterface object on an
   * MetaView Server.  The server used is determined by the static variable
   * sEMSAddress, set in the login method.  The connection may be either secure
   * or insecure - this is determined by sSecure, which is set in startORB.
   *
   * @returns           A new ClientSessionManagerInterface object.
   */
  private static ClientSessionManagerInterface
                                             getClientSessionManagerInterface()
  {
    //-------------------------------------------------------------------------
    // Request a reference to the Client Session Manager interface on the
    // MetaView Server.  The protocol used depends on whether the connection is
    // secure or insecure.
    //-------------------------------------------------------------------------
    String corbastring = null;

    if (sSecure)
    {
      //-----------------------------------------------------------------------
      // The JacorbClientProfileSelector is needed to avoid the creation of
      // multiples sockets in a bidirectional connection.
      // JacorbClientProfileSelector.setup(ProtocolType (1-secure, 2-bidir,
      // 3-insecure), sslPort, bidirPort, insecurePort).
      // Note: The JacorbClientProfileSelector needs to be set in your orb
      // properties in conjunction with Jacorb in order to work e.g:
      // props.setProperty("jacorb.transport.client.selector",
      // "org.jacorbOverwrite.orb.JacorbClientProfileSelector");
      //-----------------------------------------------------------------------
      JacorbClientProfileSelector.setup(1,
                                        Integer.parseInt(SECURE_PORT),
                                        Integer.parseInt(BIDIR_PORT),
                                        Integer.parseInt(INSECURE_PORT));
      corbastring = "corbaloc:ssliop:1.2@" + sEMSAddress + ":"
                              + SECURE_PORT + "/ClientSessionManagerInterface";
    }
    else
    {
      corbastring = "corbaloc::" + sEMSAddress + ":"
                            + INSECURE_PORT + "/ClientSessionManagerInterface";
    }

    org.omg.CORBA.Object obj = sOrb.string_to_object(corbastring);
    ClientSessionManagerInterface clientSessionManager =
                               ClientSessionManagerInterfaceHelper.narrow(obj);

    return clientSessionManager;
  }

  /**
   * Determine whether the specified int field in a supplied Settings object
   * has a given value.
   *
   * @returns           True if the values are equal, false otherwise.
   *
   * @param settings    The Settings object to get the field value from.
   * @param fieldName   The name of the field. This must be an int field.
   * @param checkValue
   *                    An object of type Integer containing the value to check
   *                    against.
   */
  private static Boolean intFieldEquals(SettingsUserInterface settings,
                                        String fieldName,
                                        java.lang.Object checkValue)
    throws FieldNameOrIndexNotFoundException,
           FieldBadTypeException
  {
    Boolean isEqualReturn = Boolean.valueOf(false);
    BooleanHolder isAssigned = new BooleanHolder();

    int actualFieldValue = settings.getFieldAsIntByName(fieldName,
                                                        isAssigned);
    if (isAssigned.value)
    {
      boolean isEqual = (actualFieldValue == ((Integer)checkValue).intValue());
      isEqualReturn = Boolean.valueOf(isEqual);
    }

    return isEqualReturn;
  }

  /**
   * Determine whether the specified String field in a supplied Settings object
   * has a given value.
   *
   * @returns           True if the values are equal, false otherwise.
   *
   * @param settings    The Settings object to get the field value from.
   * @param fieldName   The name of the field. This must be a String field.
   * @param checkValue
   *                    An object of type String containing the value to check
   *                    against.
   */
  private static Boolean stringFieldEquals(SettingsUserInterface settings,
                                           String fieldName,
                                           java.lang.Object checkValue)
    throws FieldNameOrIndexNotFoundException,
           FieldBadTypeException
  {
    Boolean isEqualReturn = Boolean.valueOf(false);
    BooleanHolder isAssigned = new BooleanHolder();

    String actualFieldValue = settings.getFieldAsStringByName(fieldName,
                                                              isAssigned);
    if (isAssigned.value)
    {
      boolean isEqual = ((String)checkValue).equals(actualFieldValue);
      isEqualReturn = Boolean.valueOf(isEqual);
    }

    return isEqualReturn;
  }
}
