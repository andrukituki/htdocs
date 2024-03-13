/**
 * Title: GetLog.java
 *
 * Description: Sample CORBA application which gets a specific log from the Log
 * dtatabase and prints it to screen.
 *
 * (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
 *
 * @version 1.0
 */

//-----------------------------------------------------------------------------
// This example application performs a simple "get log" operation on the
// specified MetaView Server as follows:
//
// -  Start a local ORB and log into the MetaView Server, using the CorbaHelper
//    utility methods.  Store the returned reference to the new Client Session
//    that the MetaView Server will have instantiated.
//
// -  Make a request for a specific Log using the Client session, and print the
//    results to screen.
//
// Sun's ORB does not provide support for SSL, so the MetaView Server's
// security access must be changed to 'insecure' for this application to be
// able to login.
//-----------------------------------------------------------------------------

import org.omg.CORBA.*;

import com.Metaswitch.MVS.Corba.*;
import com.Metaswitch.MVS.Utils.*;
import java.math.*;

public class GetLog
{
  private String mEMSAddress       = null;
  private String mUserName         = null;
  private String mPassword         = null;
  private String mUID              = null;
  private long   mCorrelator       = 0;
  private String mCorrelatorString = "0";
  private int    mTimeToWait       = 0;
  private ClientSessionInterface mClientSession = null;
  private SEAccessInterface mSEAccess = null;
  private SEAccessInterface mTopLevelSEA = null;

  public static void main(String[] args)
  {
    //-------------------------------------------------------------------------
    // Extract the IP address, username and password from the arguments
    // provided.
    //-------------------------------------------------------------------------
    if ((args.length != 5) && (args.length != 6))
    {
      System.err.println("Expected 5 or 6 parameters, got " + args.length);
      System.err.println("Usage: MetaViewServerIPAddress/HostName MetaViewUsername " +
                         "MetaViewPassword SystemUID LogCorrelator <timeToWait>");
      System.exit(1);
    }

    String emsAddress = args[0];
    String userName = args[1];
    String password = args[2];
    String metaSwitch = args[3];
    String correlator = args[4];
    String timeToWait = "0";
    if (args.length == 6)
    {
      timeToWait = args[5];
    }

    CorbaHelper.startORB();

    TraceHelper.trace("Started ORB");

    GetLog getLog = new GetLog(emsAddress,
                               userName,
                               password,
                               metaSwitch,
                               correlator,
                               timeToWait);

    getLog.execute();

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
   * @param UID         The unique identifier for the CFS / UMG / IS / MVD or
   *                    MetaView Server on which the log was raised. The value
   *                    to use is: the "System unique identifier" in the CFS /
   *                    UMG / IS / MVD element, for elements on the CFS / UMG /
   *                    IS / MVD; or the "MetaView Server unique identifier" in
   *                    the MetaView Server element, for elements on the
   *                    MetaView Server.
   * @param logCorrelator
   *                    The log correlator, which identifies the log on the
   *                    specified CFS / UMG / IS / MVD or MetaView Server.
   * @param timeToWait
   *                    How long (in seconds) to wait for the log collector to
   *                    find the log if it's not instantly available. May be
   *                    zero.
   */
  public GetLog(String emsAddress,
                String userName,
                String password,
                String UID,
                String correlator,
                String timeToWait)
  {
    mEMSAddress = emsAddress;
    mUserName   = userName;
    mPassword   = password;
    mUID        = UID;

    //-------------------------------------------------------------------------
    // Need to convert the correlator to a long.  Note that this method doesn't
    // care whether the correlator contains spaces or not, it can handle
    // either.  Store both the String and the long.
    //-------------------------------------------------------------------------
    mCorrelatorString = correlator;
    mCorrelator = convertCorrelator(correlator);

    //-------------------------------------------------------------------------
    // Convert the time to wait from String to int and store it.
    //-------------------------------------------------------------------------
    mTimeToWait = Integer.parseInt(timeToWait);
  }

  public synchronized void execute()
  {
    try
    {
      //-----------------------------------------------------------------------
      // Login to the MetaView Server.
      //-----------------------------------------------------------------------
      mClientSession = CorbaHelper.login(mEMSAddress, mUserName, mPassword);

      TraceHelper.trace("Logged in");

      //-----------------------------------------------------------------------
      // Do the query.
      //-----------------------------------------------------------------------
      if (mTimeToWait > 0)
      {
        TraceHelper.trace("Will wait up to " + mTimeToWait + "s for log");
      }

      LogInformation logInfo = mClientSession.getLogInformation(mUID,
                                                                mCorrelator,
                                                                mTimeToWait);

      //-----------------------------------------------------------------------
      // Trace the log info out to the screen.
      //-----------------------------------------------------------------------
      TraceHelper.trace("********** START LOG **********"
                        + "\nnode      :     " + logInfo.node
                        + "\ncorrelator:     " + mCorrelatorString
                        + "\ntime:           " + logInfo.time
                        + "\nseverity:       " + logInfo.severity
                        + "\nacknowledged:   " + logInfo.acknowledged
                        + "\nlogSource:      " + logInfo.logSource
                        + "\nobjectName:     " + logInfo.objectName
                        + "\nrelatedObjects: " + logInfo.relatedObjects
                        + "\n"
                        + "\n  Description: \n" + logInfo.description
                        + "\n"
                        + "\nCause: " + logInfo.cause
                        + "\nEffect: " + logInfo.effect
                        + "\n"
                        + "\nAction: " + logInfo.action
                        + "\n"
                        + "\nUserComments: " + logInfo.userComments
                        + "\n"
                        + "\n"
                        + "associatedLogCorrelator:    "
                        + logInfo.associatedLogCorrelator
                        + "\nassociatedLogNode:  "
                        + logInfo.associatedNodeName
                        + "\nassociatedLogNodeUID: "
                        + logInfo.associatedNodeUID
                        + "\n*********** END LOG ***********\n");
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
   * Converts the passed hex value display String to a long.
   *
   * @return long       A long with the numerical value represented by the
   *                    display string.
   *
   * @param value       String displaying a 16-character hex value with or
   *                    without spaces, e.g. "0123456789abcdef" or "0123 4567
   *                    89ab cdef".
   *
   * @exception NumberFormatException
   *                    - thrown if the passed String cannot be converted to an
   *                    Unsigned64BitNumber.
   */
  static public long convertCorrelator(String value)
      throws NumberFormatException
  {
    //-------------------------------------------------------------------------
    // 1.  Remove any spaces.
    //-------------------------------------------------------------------------
    value = value.trim();

    int index = value.indexOf(" ");

    while (index != -1)
    {
      value = value.substring(0, index) + value.substring(index + 1);

      index = value.indexOf(" ");
    }

    //-------------------------------------------------------------------------
    // 2.  Interpret as hex value - this may only have a maximum of 16
    // hexadecimal characters otherwise it will overflow a 64 bit quantity.
    //-------------------------------------------------------------------------
    if (value.length() > 16)
    {
      throw new NumberFormatException("Failed to parse " + value);
    }

    long result;

    try
    {
      result = new BigInteger(value, 16).longValue();
    }
    catch (Exception ex)
    {
      throw new NumberFormatException("Failed to parse " + value);
    }

    return result;
  }
}
