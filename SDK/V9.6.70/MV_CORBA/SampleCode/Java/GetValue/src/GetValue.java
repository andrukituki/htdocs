/**
 * Title: GetValue
 *
 * Description: Sample CORBA application which gets the value of a field and
 * prints it out.
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
//    utility methods.  Store the returned reference to the new Client Session
//    that the MetaView Server will have instantiated.
//
// -  Request and store a reference to the MetaView Server's System Element
//    (SE) Access Factory using the new Client Session.
//
// -  Request and store a reference to a new SE Access using the SE Access
//    Factory.  Attach to the CFS / UMG / IS / MVD object, specifying the
//    required indices (as defined in omlapi) and read out the current system
//    time.
//
// Sun's ORB does not provide support for SSL, so the MetaView Server's
// security access must be changed to 'insecure' for this application to be
// able to login.
//-----------------------------------------------------------------------------

import org.omg.CORBA.*;

import com.Metaswitch.MVS.Corba.*;
import com.Metaswitch.MVS.Utils.*;

public class GetValue
{
  private String mEMSAddress = null;
  private String mUserName = null;
  private String mPassword = null;
  private ClientSessionInterface mClientSession = null;
  private SEAccessInterface mSEAccess = null;
  private SEAccessInterface mTopLevelSEA = null;

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

    String emsAddress = args[0];
    String userName = args[1];
    String password = args[2];

    CorbaHelper.startORB();

    TraceHelper.trace("Started ORB");

    GetValue getValue = new GetValue(emsAddress, userName, password);
    getValue.execute();


    CorbaHelper.stopORB();

    TraceHelper.trace("Stopped ORB");

    System.exit(0);
  }

  /**
   * Constructor which assigns member variables based on its parameters.
   *
   * @param emsAddress
   *                    The IP address or hostname of the MetaView Server.
   * @param userName    Username.
   * @param password    Password.
   */
  public GetValue(String emsAddress, String userName, String password)
  {
    mEMSAddress = emsAddress;
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
      mClientSession = CorbaHelper.login(mEMSAddress, mUserName, mPassword);

      //-----------------------------------------------------------------------
      // Get an unattached SEA using the client session.  This behaves as if it
      // were attached to the root of the SE object tree.
      //-----------------------------------------------------------------------
      mTopLevelSEA = mClientSession.createSEAccess();

      TraceHelper.trace("Logged in");

      //-----------------------------------------------------------------------
      // Obtain an SE Access that is attached to the first CFS / UMG / IS / MVD
      // connection object.
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
    catch (org.omg.CORBA.UserException e)
    {
      System.out.println("Corba exception caught: " + e);
      e.printStackTrace();
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
      // Destroy the SEAccess we created above, if we got that far.
      //-----------------------------------------------------------------------
      if (mTopLevelSEA != null)
      {
        mTopLevelSEA.destroy();
        mTopLevelSEA = null;
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
