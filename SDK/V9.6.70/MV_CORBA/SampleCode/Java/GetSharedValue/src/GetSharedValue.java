/**
 * Title: GetSharedValue
 *
 * Description: Sample CORBA application which gets the value of a field on a
 *              shared config object on a clustered deployment.
 *
 * (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
 */

//-----------------------------------------------------------------------------
// This example application performs a simple "get shared value" operation on
// the specified MetaView Server as follows:
//
// -  Start a local ORB and log into the MetaView Server, using the CorbaHelper
//    utility methods.  Store the returned reference to the new Client Session
//    that the MetaView Server will have instantiated.
//
// -  Request and store a reference to the MetaView Server's System Element
//    (SE) Access Factory using the new Client Session.
//
// -  Request and store a reference to a new SE Access using the SE Access
//    Factory.  Attach to the shared config object, then connect to the Trunk
//    Routing and Policy Services object and read out a value.
//
// Sun's ORB does not provide support for SSL, so the MetaView Server's
// security access must be changed to 'insecure' for this application to be
// able to login.
//-----------------------------------------------------------------------------

import org.omg.CORBA.*;

import com.Metaswitch.MVS.Corba.*;
import com.Metaswitch.MVS.Utils.*;

public class GetSharedValue
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

    GetSharedValue GetSharedValue = new GetSharedValue(emsAddress,
                                                       userName,
                                                       password);
    GetSharedValue.execute();


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
  public GetSharedValue(String emsAddress, String userName, String password)
  {
    mEMSAddress = emsAddress;
    mUserName = userName;
    mPassword = password;
  }

  public synchronized void execute()
  {
    SEAccessInterface rpsSEA = null;

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
      mTopLevelSEA.attachTo(omlapi.O_METASWITCH_CONNECTION + "." +
                            omlapi.FIV_SHARED_CONN_INDEX);
      mSEAccess = mTopLevelSEA;

      if (mSEAccess != null)
      {
        TraceHelper.trace("Attached to shared config.");

        rpsSEA = mSEAccess.findElement(
                                   omlapi.O_TRUNK_ROUTING_AND_POLICY_SERVICES);

        String activeConfigSet = getActiveConfigSetValue(rpsSEA);

        TraceHelper.trace("Got maximum number of routing requests: " +
                          activeConfigSet);
      }
      else
      {
        TraceHelper.trace("Failed to find shared config.\n");
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
      if (rpsSEA != null)
      {
        rpsSEA.destroy();
        rpsSEA = null;
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
   * Get the value of the "Trunk Routing Config Set" field from the SE that the
   * given SEAccess is attached to.
   *
   * @return            The value of the "TrunkRoutingConfigSet" field, or null
   *                    if it is not assigned.
   *
   * @param rpsSEA     An SEAccessInterface that is attached to an RPS SE.
   *
   * @exception ElementUnavailableException
   */
  public String getActiveConfigSetValue(SEAccessInterface rpsSEA)
    throws ElementUnavailableException
  {
    String configSet = null;

    try
    {
      //-----------------------------------------------------------------------
      // Call takeSnapshot to make sure that we have access to the latest
      // settings.
      //-----------------------------------------------------------------------
      rpsSEA.takeSnapshot();

      //-----------------------------------------------------------------------
      // Obtain the settings.
      //-----------------------------------------------------------------------
      SettingsUserInterface settings = rpsSEA.getSnapshot_Settings();

      BooleanHolder isAssigned = new BooleanHolder();

      configSet = settings.getFieldAsStringByName(
                                   omlapi.F_MAXIMUM_NUMBER_OF_ROUTING_REQUESTS,
                                   isAssigned);

      //-----------------------------------------------------------------------
      // If the isAssigned BooleanHolder has been filled in with the value
      // false, the value of the configSet variable is undefined so set it back
      // to null.
      //-----------------------------------------------------------------------
      if (!isAssigned.value)
      {
        configSet = null;
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

    return configSet;
  }

}
