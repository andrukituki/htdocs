/**
 * Title: GetValueSecure
 *
 * Description: Sample CORBA application which gets the value of a field in
 * secure mode, and prints it out.
 *
 * (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
 *
 * @version 1.0
 */

//-----------------------------------------------------------------------------
// This example application performs a simple "get value" operation on the
// specified MetaView Server as follows:
//
// -  Start a local ORB and log into the MetaView Server, using the CorbaHelper
//    utility methods.  Either a user login or an application login may be
//    performed depending on the command line arguments supplied.  Store the
//    returned reference to the new Client Session that the MetaView Server
//    will have instantiated.
//
// -  Request and store a reference to the MetaView Server's System Element
//    (SE) Access Factory using the new Client Session.
//
// -  Request and store a reference to a new SE Access using the SE Access
//    Factory.  Attach to the CFS / UMG / IS / MVD object, specifying the
//    required indices (as defined in omlapi) and read out the current system
//    time.
//
// The Jacorb ORB is used as, unlike the Sun ORB used in the sample app
// GetValue, it provides the ability to make a secure connection.  The MetaView
// server must be set up to accept secure connections.
//-----------------------------------------------------------------------------
import org.omg.CORBA.*;

import com.Metaswitch.MVS.Corba.*;
import com.Metaswitch.MVS.Utils.*;
import com.Metaswitch.MVS.UtilsSecure.*;

public class GetValueSecure
{
  //---------------------------------------------------------------------------
  // Member variables to store the details required to login to the MetaView
  // Server.  These are set using command line arguments - although username
  // and password will not be set if an application login is being performed.
  // The mIsUserLogin boolean indicates the login type - true for a user login,
  // false for an application login.
  //---------------------------------------------------------------------------
  private String mEMSAddress = null;
  private String mUsername = null;
  private String mPassword = null;
  private boolean mIsUserLogin = false;

  //---------------------------------------------------------------------------
  // Objects used in accessing data in MetaView.  The ClientSessionInterface is
  // used to generate the SEAccessFactoryInterface, which in turn generates the
  // SEAccessInterface which is used to query the MetaView Server.
  //---------------------------------------------------------------------------
  private ClientSessionInterface mClientSession = null;
  private SEAccessInterface mSEAccess = null;
  private SEAccessInterface mTopLevelSEA = null;

  public static void main(String[] args)
  {
    //-------------------------------------------------------------------------
    // Variables to hold the command line input.  The emsAddress is either the
    // IP address or the hostname of the MetaView Server.
    //-------------------------------------------------------------------------
    String emsAddress = null;
    String username = null;
    String password = null;
    String caCertFilename = null;
    String clientCertFilename = null;
    String clientKeyFilename = null;

    //-------------------------------------------------------------------------
    // There are three different possible ways of logging into a MetaView Server:
    //
    // - An insecure user login.  The MetaView Server will need to be set up to
    // accept insecure connections.
    // - A secure user login.  The user will need to provide a username and
    // password.  The default client certificates will be used, since the
    // security in this case comes from the username and password rather than
    // the certificates.
    // - A secure application login.  The user needs to supply secure
    // certificates.  No username and password are required.
    //
    // This app will perform either of the secure connections.  Which one it
    // will perform depends on the command line arguments it is passed.
    // Therefore the first step is to test the number of command line arguments
    // provided.  The user should provide either:
    // a) MetaView Server address, username and password - 3 arguments, indicating a
    // user login, or
    // b) MetaView Server address, Certificate Authority (CA) certificate filename,
    // client certificate filename and client key filename - 4 arguments,
    // indicating an application login.
    // Therefore the number of arguments indicates the type of login to
    // perform.  If the number is not either 3 or 4, the syntax is incorrect.
    //-------------------------------------------------------------------------
    boolean isUserLogin = false;

    TraceHelper.trace(args.length + " arguments received.");

    if (args.length == 3)
    {
      isUserLogin = true;
    }
    else if (args.length == 4)
    {
      isUserLogin = false;
    }
    else
    {
      //-----------------------------------------------------------------------
      // The number of command line arguments wasn't 3 or 4, and so must have
      // been wrong.  Display the correct usage and exit.
      //-----------------------------------------------------------------------
      System.err.println("Usage: GetValueSecure "
                       + "MetaViewServerIPAddress/HostName\n"
                       + "                      (Username Password) | \n"
                       + "                      (CACertificateFilename "
                       + "ClientCertificateFilename\n"
                       + "                       ClientKeyFilename)");
      System.exit(1);
    }

    //-------------------------------------------------------------------------
    // Assign the values of the MetaView Server address, username, password and
    // filename variables as appropriate.  The MetaView Server address is
    // either the IP address or the hostname of the MetaView Server.
    //-------------------------------------------------------------------------
    emsAddress = args[0];

    if (isUserLogin)
    {
      //-----------------------------------------------------------------------
      // User login - username and password are provided.
      //-----------------------------------------------------------------------
      username = args[1];
      password = args[2];
    }
    else
    {
      //-----------------------------------------------------------------------
      // Application login - CA certificate, client certificate and client key
      // filenames provided.
      //-----------------------------------------------------------------------
      caCertFilename = args[1];
      clientCertFilename = args[2];
      clientKeyFilename = args[3];
    }

    //-------------------------------------------------------------------------
    // Start the ORB in secure mode.  The startORB method is overloaded, and
    // will perform a user login or an application login depending on the
    // number of parameters it is passed.
    //-------------------------------------------------------------------------
    if (isUserLogin)
    {
      //-----------------------------------------------------------------------
      // Start ORB, preparing for user login.  The 'true' parameter indicates
      // that the ORB should be started in secure mode.
      //-----------------------------------------------------------------------
      CorbaHelperSecure.startORB(true);
    }
    else
    {
      //-----------------------------------------------------------------------
      // Start ORB, preparing for application login.
      //-----------------------------------------------------------------------
      try
      {
        CorbaHelperSecure.startORB(caCertFilename,
                                   clientCertFilename,
                                   clientKeyFilename);
      }
      catch (IllegalStateException e)
      {
        //---------------------------------------------------------------------
        // If we receive an IllegalStateException from startORB, display the
        // message and rethrow the exception.
        //---------------------------------------------------------------------
        System.err.println(e.getMessage());

        throw e;
      }
    }

    TraceHelper.trace("Started ORB");

    GetValueSecure getValueSecure = new GetValueSecure(emsAddress,
                                                       isUserLogin,
                                                       username,
                                                       password);
    getValueSecure.execute();

    CorbaHelper.stopORB();

    TraceHelper.trace("Stopped ORB");

    System.exit(0);
  }

  /**
   * This constructor sets up the MetaView Server IP address/hostname, username
   * and password member variables based on the command line input.
   *
   * @param iPAddress   IP address.
   * @param isUserLogin
   *                    Indicates whether a user login (if true) or an
   *                    application login (if false) is to be performed.
   * @param userName    Username.
   * @param password    Password - all three originally obtained from the
   *                    command line parameters.
   */
  public GetValueSecure(String emsAddress,
                        boolean isUserLogin,
                        String username,
                        String password)
  {
    mEMSAddress = emsAddress;
    mIsUserLogin = isUserLogin;
    mUsername = username;
    mPassword = password;
  }

  /**
  * This method logs in to the MetaView Server, obtains the current time value
  * from the first NE managed by the MetaView Server, and displays it.
  */
  public synchronized void execute()
  {
    try
    {
      //-----------------------------------------------------------------------
      // Login to the MetaView Server.
      //-----------------------------------------------------------------------
      if (mIsUserLogin)
      {
        TraceHelper.trace("Attempt user login");
        mClientSession = CorbaHelper.login(mEMSAddress,
                                           mUsername,
                                           mPassword);
      }
      else
      {
        TraceHelper.trace("Attempt application login");
        mClientSession = CorbaHelper.login(mEMSAddress);
      }

      //-----------------------------------------------------------------------
      // Get an unattached SEA using the client session.  This behaves as if it
      // were attached to the root of the SE object tree.
      //-----------------------------------------------------------------------
      mTopLevelSEA = mClientSession.createSEAccess();

      TraceHelper.trace("Logged in");

      //-----------------------------------------------------------------------
      // Obtain an SE Access that is attached to the first CFS / UMG / IS / MVD
      // object.
      //-----------------------------------------------------------------------
      mSEAccess = mTopLevelSEA.findElement(omlapi.O_CFS___UMG___IS___MVD);

      if (mSEAccess != null)
      {
        TraceHelper.trace("Attached to first CFS / UMG / IS / MVD.");

        //---------------------------------------------------------------------
        // Get the value of the "Time" field.
        //---------------------------------------------------------------------
        String time = getTimeValue(mSEAccess);

        TraceHelper.trace("Got time value: " + time);
      }
      else
      {
        TraceHelper.trace("Failed to find CFS / UMG / IS / MVD.\n"
                          + "This is probably because there is no "
                          + "CFS / UMG / IS / MVD\nConnection object configured "
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
                         + "running in insecure mode.");
    }
    catch (org.omg.CORBA.TRANSIENT e)
    {
      System.err.println("Transient communication failure with the MetaView "
                         + "Server.\nThis may be because it is not running, "
                         + "or it is running in insecure mode,\nor there are "
                         + "problems with the network.");
    }
    catch (org.omg.CORBA.NO_PERMISSION e)
    {
      System.err.println("No permission to access MetaView.\nThis may be because "
                        +  "the certificates or keys being used are invalid.");
    }
    catch (LoginFailedException e)
    {
      System.err.println("Unable to login to the MetaView Server.");

      //-----------------------------------------------------------------------
      // Give detailed suggestions depending on whether a user login or an
      // application login is being performed.
      //-----------------------------------------------------------------------
      if (mIsUserLogin)
      {
        System.err.println("Make sure that:\n"
                           + " - your MetaView Server is running in secure mode\n"
                           + " - your username and password are correct.");
      }
      else
      {
        System.err.println("Make sure that:\n"
                           + " - your MetaView Server is running in secure mode\n"
                           + " - the certificate filenames provided are "
                                                                 + "correct.\n"
                           + " - your certificate files are valid to log in "
                                                          + "to this server.");
      }
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
      // Destroy the SEAccess we created above, if we got that far.
      //-----------------------------------------------------------------------
      if (mSEAccess != null)
      {
        mSEAccess.destroy();
        mSEAccess = null;
      }

      //-----------------------------------------------------------------------
      // Log out of the server, if we managed to log in above.
      //-----------------------------------------------------------------------
      if (mClientSession != null)
      {
        mClientSession.logout();
        mClientSession = null;
        TraceHelper.trace("Logged out");
      }
    }
  }

  /**
   * Get the value of the "Time" field from the SE that the given SEAccess is
   * attached to.
   *
   * @return            The value of the "Time" field, or null if it is not
   *                    assigned.
   *
   * @param connSEA     An SEAccessInterface that is attached to a CFS / UMG /
   *                    IS / MVD SE.
   *
   * @exception ElementUnavailableException
   */

  public String getTimeValue(SEAccessInterface connSEA)
    throws ElementUnavailableException
  {
    String time = null;

    try
    {
      //-----------------------------------------------------------------------
      // Call takeSnapshot to make sure that we have access to the latest
      // settings.
      //-----------------------------------------------------------------------
      connSEA.takeSnapshot();

      //-----------------------------------------------------------------------
      // Obtain the settings.
      //-----------------------------------------------------------------------
      SettingsUserInterface settings = connSEA.getSnapshot_Settings();

      BooleanHolder isAssigned = new BooleanHolder();

      time = settings.getFieldAsStringByName(omlapi.F_TIME, isAssigned);

      //-----------------------------------------------------------------------
      // If the isAssigned BooleanHolder has been filled in with the value
      // false, the value of the time variable is undefined so set it back to
      // null.
      //-----------------------------------------------------------------------
      if (!isAssigned.value)
      {
        time = null;
      }
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
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }

    return time;
  }
}
