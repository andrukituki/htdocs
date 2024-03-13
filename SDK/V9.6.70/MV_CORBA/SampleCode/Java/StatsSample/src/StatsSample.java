/**
 * Title: StatsSample
 *
 * Description: Sample CORBA application which retrieves a value from a
 * particular Call Agent Statistics object.
 *
 * (c) Microsoft Corporation. All rights reserved.
 * Highly Confidential Material
 *
 * @version 1.0
 */

//-----------------------------------------------------------------------------
// This example application retrieves a Call Agent statistics value from a
// specified MetaView Server as follows:
//
// -  Start a local ORB and log into the MetaView Server, using the CorbaHelper
//    utility methods.  Store the returned reference to the new Client Session
//    that the MetaView Server will have instantiated.
//
// -  Request and store a reference to the MetaView Server's top level SEA using the
//    new Client Session.
//
// -  Attach to the required Call Agent Statistics SE, specifying the required
//    indices (as defined in omlapi) .
//
// -  Register to receive snapshot change notifications for the statistics
//    object.
//
// -  Get the latest settings of the Call Agent Statistics object, extract the
//    required field value and output it to System.out.
//
// -  When a new snapshot is available, get the latest settings and extract and
//    output the required field value.
//
// -  Poll the server often enough that it doesn't log us out while we are
//    waiting for snapshot change notifications.
//
// Sun's ORB does not provide support for SSL, so the MetaView Server's security
// access must be changed to 'insecure' for this application to be able to
// login.
//
// We do not use the CorbaHelper.handleUnexpectedUserException() method in this
// example application, for illustrative purposes - instead we explain whenever
// an unexpected exception is caught the reason why it is unexpected, and throw
// an IllegalStateException containing text describing the problem.
//-----------------------------------------------------------------------------

import org.omg.CORBA.*;
import java.util.Date;

import com.Metaswitch.MVS.Corba.*;
import com.Metaswitch.MVS.Utils.*;

public class StatsSample
{
  private String mIPAddress = null;
  private String mUserName = null;
  private String mPassword = null;
  private SEAccessInterface mTopLevelSEA = null;
  private ClientSessionInterface mClientSession = null;
  private long mPollingPeriod = 0;

  //---------------------------------------------------------------------------
  // The mLoggedIn boolean tracks whether we have an open Client Session with
  // the MetaView Server.  The mWasLoggedIn boolean is true if we have ever managed
  // to establish a connection with the server.
  //---------------------------------------------------------------------------
  private boolean mLoggedIn = false;
  private boolean mWasLoggedIn = false;

  public static void main(String[] args)
  {
    //-------------------------------------------------------------------------
    // Extract the IP address, username and password from the arguments
    // provided.
    //-------------------------------------------------------------------------
    if (args.length < 3)
    {
      System.err.println("Usage: StatsSample MetaViewServerIPAddress/HostName "
                         + "Username Password");
      System.exit(1);
    }

    String iPAddress = args[0];
    String userName = args[1];
    String password = args[2];

    CorbaHelper.startORB();

    StatsSample statsSample = new StatsSample(iPAddress, userName, password);
    statsSample.execute();

    CorbaHelper.stopORB();

    System.exit(0);
  }

  public StatsSample(String iPAddress, String userName, String password)
  {
    mIPAddress = iPAddress;
    mUserName = userName;
    mPassword = password;
  }

  public synchronized void execute()
  {
    //-------------------------------------------------------------------------
    // Any exception that is thrown all the way up to this try/catch will
    // result in the application exiting.
    //-------------------------------------------------------------------------
    try
    {
      //-----------------------------------------------------------------------
      // This while loop will try to log in to the server, and if successful,
      // it will keep trying to log in again if it is logged out for any
      // reason.
      //-----------------------------------------------------------------------
      while (true)
      {
        //---------------------------------------------------------------------
        // An exception caught in this try/catch will result in us attempting
        // to re-login to the server.
        //---------------------------------------------------------------------
        try
        {
          //-------------------------------------------------------------------
          // Login to the MetaView Server.
          //-------------------------------------------------------------------
          mClientSession = CorbaHelper.login(mIPAddress, mUserName, mPassword);
          mLoggedIn = true;
          mWasLoggedIn = true;

          //-------------------------------------------------------------------
          // Get an unattached SEA using the client session.  This behaves as if
          // it were attached to the root of the SE object tree.
          //-------------------------------------------------------------------
          mTopLevelSEA = mClientSession.createSEAccess();

          TraceHelper.trace("Logged in");

          //-------------------------------------------------------------------
          // Get the timeout period from the client session - we will need to
          // poll the server more often that this in order to keep our
          // connection alive.
          //-------------------------------------------------------------------
          long timeoutPeriod = mClientSession.getTimeoutPeriod();

          //-------------------------------------------------------------------
          // We poll the server 30 seconds earlier than it would log us out.
          //-------------------------------------------------------------------
          mPollingPeriod = timeoutPeriod - 30 * 1000;

          //-------------------------------------------------------------------
          // If the timeout period is less than 30 seconds, we default to
          // polling every second.  We assume that the timeout period will not
          // be less than a second!
          //-------------------------------------------------------------------
          if (mPollingPeriod < 1000)
          {
            mPollingPeriod = 1000;
          }

          TraceHelper.trace("Polling server every " + mPollingPeriod / 1000
                            + " seconds");

          //-------------------------------------------------------------------
          // This while loop will try to attach to the 'Previous Hour' Call
          // Agent Stats object until we get logged out of the server for any
          // reason.  If the object is temporarily unavailable, we will keep
          // trying to attach to it every minute.
          //-------------------------------------------------------------------
          while (mLoggedIn)
          {
            //-----------------------------------------------------------------
            // An exception caught in this try/catch will result in us
            // attempting to re-attach to the Call Agent Stats object.
            //-----------------------------------------------------------------
            try
            {
              //---------------------------------------------------------------
              // Attach to the 'Previous Hour' Call Agent Stats object.
              //---------------------------------------------------------------
              SEAccessInterface previousHourCallAgentStatsSEA =
                                         getPreviousHourCallAgentStatsObject();

              if (previousHourCallAgentStatsSEA != null)
              {
                //-------------------------------------------------------------
                // Create a snapshot change listener object to register with
                // the SE.  The newSnapshotAvailable method will be called
                // every time the settings of the object change.  When this
                // happens, we output the new value of the statistics.
                //-------------------------------------------------------------
                SnapshotChangeListenerInterfacePOA snapshotChangeListener =
                  new SnapshotChangeListenerInterfacePOA()
                  {
                    public void newSnapshotAvailable(SEAccessInterface seAccess,
                                                     boolean fundamental)
                    {
                      try
                      {
                        //-----------------------------------------------------
                        // Print out the latest statistics.
                        //-----------------------------------------------------
                        getAndPrintStats(seAccess);
                      }
                      catch (ElementUnavailableException e)
                      {
                        //-----------------------------------------------------
                        // If the element has become unavailable when we try to
                        // get the snapshot, give up and wait for the next
                        // snapshot change notification.
                        //-----------------------------------------------------
                      }
                      catch (NotAttachedException e)
                      {
                        //-----------------------------------------------------
                        // This exception will have been thrown if the SEA we
                        // are passing to getAndPrintStats is not attached to
                        // an SE.  However, it is an SEA that was passed to us
                        // by the server on a newSnapshotAvailable call, so
                        // throw an IllegalStateException to indicate that
                        // something unexpected has occurred.
                        //-----------------------------------------------------
                        throw new IllegalStateException("SEA passed into "
                                    + "newSnapshotAvailable is not attached!");
                      }
                      catch (LockTimeoutException e)
                      {
                        //-----------------------------------------------------
                        // This exception may be thrown if we have explicitly
                        // locked an SE and that lock has expired after a fixed
                        // period (30 seconds).  However, we haven't explicitly
                        // locked an SE, so throw an IllegalStateException to
                        // indicate that something unexpected has occurred.
                        //-----------------------------------------------------
                        throw new IllegalStateException("Lock Timeout "
                                                        + "Exception thrown!");
                      }
                    }
                  };

                //-------------------------------------------------------------
                // Get a CORBA reference for our snapshot change listener and
                // register it with the SE to receive snapshot notifications.
                //-------------------------------------------------------------
                SnapshotChangeListenerInterface
                  snapshotChangeListenerCorbaRef =
                            snapshotChangeListener._this(CorbaHelper.getORB());

                previousHourCallAgentStatsSEA.addSnapshotChangeListener(
                                               snapshotChangeListenerCorbaRef);

                //-------------------------------------------------------------
                // Print the current value of the statistics.  We will wait
                // until the statistics change before outputting any more
                // values.
                //-------------------------------------------------------------
                getAndPrintStats(previousHourCallAgentStatsSEA);

                //-------------------------------------------------------------
                // This while loop will poll the MetaView Server every
                // mPollingPeriod milliseconds to prevent it from logging us
                // out while we wait for snapshot change notifications.  If we
                // do get logged out for any reason, we will exit this loop and
                // the containing loop, and try to log in again.
                //-------------------------------------------------------------
                while (mLoggedIn)
                {
                  try
                  {
                    //---------------------------------------------------------
                    // Poll the MetaView Server once a minute to keep our Client
                    // Session alive.  This call will throw
                    // ElementOperationFailedException if we have been logged
                    // out by the server for any reason, so we will catch this
                    // below and try to log in again.
                    //---------------------------------------------------------
                    mClientSession.keepAlive();

                    //---------------------------------------------------------
                    // Wait for the polling period calculated earlier before
                    // polling the MetaView Server again.
                    //---------------------------------------------------------
                    wait(mPollingPeriod);
                  }
                  catch (InterruptedException e)
                  {
                    //---------------------------------------------------------
                    // If this exception is hit, then our wait has been
                    // interrupted.  No matter, just poll the server again
                    // sooner than we expected.
                    //---------------------------------------------------------
                  }
                  catch (ElementOperationFailedException e)
                  {
                    //---------------------------------------------------------
                    // We have been logged out by the server, so try to log in
                    // again.
                    //---------------------------------------------------------
                    TraceHelper.trace("Logged out by server, retrying login");
                    mLoggedIn = false;
                  }
                }
              }
              else
              {
                //-------------------------------------------------------------
                // If the statistics object could not be found, wait a minute
                // then try to get it again.
                //-------------------------------------------------------------
                TraceHelper.trace("Failed to find statistics object, retrying"
                                  + "\nin 1 minute");
                try
                {
                  wait(60 * 1000);
                }
                catch (InterruptedException ex)
                {
                  //-----------------------------------------------------------
                  // If this exception is hit, then our wait has been
                  // interrupted.  No matter, just get the 'Previous Hour' Call
                  // Agent Statistics object sooner than we expected.
                  //-----------------------------------------------------------
                }
              }
            }
            catch (ElementUnavailableException e)
            {
              //---------------------------------------------------------------
              // This exception may be thrown if any of the obejcts attached to
              // during the call to getPreviousHourCallAgentStats are currently
              // unavailable.  Wait a minute before trying to get the 'Previous
              // Hour' Call Agent Statistics object again.
              //---------------------------------------------------------------
              TraceHelper.trace("Element unavailable, retrying in 1 minute");
              try
              {
                wait(60 * 1000);
              }
              catch (InterruptedException ex)
              {
                //-------------------------------------------------------------
                // If this exception is hit, then our wait has been
                // interrupted.  No matter, just get the 'Previous Hour' Call
                // Agent Statistics object sooner than we expected.
                //-------------------------------------------------------------
              }
            }
          }
        }
        catch (org.omg.CORBA.COMM_FAILURE e)
        {
          //-------------------------------------------------------------------
          // We have lost connectivity to the MetaView Server.  If we have ever
          // managed to log in, then the server must have been running at some
          // point, so we wait a minute then retry.  If we never managed to log
          // in, the server may not be running so we give up.
          //-------------------------------------------------------------------
          if (mWasLoggedIn)
          {
            TraceHelper.trace("Lost connection to server, retrying login in\n"
                              + "1 minute");
            mLoggedIn = false;
            try
            {
              wait(60 * 1000);
            }
            catch (InterruptedException ex)
            {
              //---------------------------------------------------------------
              // If this exception is hit, then our wait has been interrupted.
              // No matter, just try to log in again sooner than we expected.
              //---------------------------------------------------------------
            }
          }
          else
          {
            //-----------------------------------------------------------------
            // If we have never managed to log in to the server, re-throw the
            // COMM_FAILURE exception so that it will be caught below and we
            // will exit.
            //-----------------------------------------------------------------
            throw e;
          }
        }
        catch (org.omg.CORBA.TRANSIENT e)
        {
          //-------------------------------------------------------------------
          // We have temporarily lost connectivity to the MetaView Server.  A
          // TRANSIENT error should not be a permanent condition, so we wait a
          // minute then retry.
          //-------------------------------------------------------------------
          TraceHelper.trace("Transient error in connection to server,\n"
                            + "retrying login in 1 minute");
          mLoggedIn = false;
          try
          {
            wait(60 * 1000);
          }
          catch (InterruptedException ex)
          {
            //-----------------------------------------------------------------
            // If this exception is hit, then our wait has been interrupted.
            // No matter, just try to log in again sooner than we expected.
            //-----------------------------------------------------------------
          }
        }
        catch (org.omg.CORBA.OBJECT_NOT_EXIST e)
        {
          //-------------------------------------------------------------------
          // A CORBA object that we have tried to reference does not exist.
          // This probably means that the ORB on the MetaView Server has restarted
          // for some reason.  This should not be a permanent condition, so we
          // wait a minute then retry.
          //-------------------------------------------------------------------
          TraceHelper.trace("Referenced CORBA object does not exist,\n"
                            + "retrying login in 1 minute");
          mLoggedIn = false;
          try
          {
            wait(60 * 1000);
          }
          catch (InterruptedException ex)
          {
            //-----------------------------------------------------------------
            // If this exception is hit, then our wait has been interrupted.
            // No matter, just try to log in again sooner than we expected.
            //-----------------------------------------------------------------
          }
        }
      }
    }
    catch (org.omg.CORBA.COMM_FAILURE e)
    {
      System.err.println("Unable to login to the MetaView Server.");
      System.err.println("This may be because it is not running, or it is "
                         + "running in secure mode.");
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
  }

  /**
   * Attach to the 'Previous Hour' Call Agent Statistics object.
   *
   * @returns           An SEAccessInterface which is attached to the 'Previous
   *                    Hour' Call Agent Statistics object, or null if the
   *                    object could not be found.
   *
   * @exception ElementUnavailableException
   */
  private SEAccessInterface getPreviousHourCallAgentStatsObject()
    throws ElementUnavailableException
  {
    SEAccessInterface previousHourCallAgentStatsSEA = null;

    try
    {
      if (mTopLevelSEA != null)
      {
        //---------------------------------------------------------------------
        // Attach to the first Call Agent Stats object for the previous hour
        // under the top level SEA.
        //---------------------------------------------------------------------
        previousHourCallAgentStatsSEA =
                   mTopLevelSEA.findElementWithIntField(
                               omlapi.O__SUMMARY_PERIOD__CALL_FEATURE_SERVER_STATISTICS,
                               omlapi.F_SUMMARY_PERIOD,
                               omlapi.V_SUMMARY_PERIOD_7___PREVIOUS_HOUR);
      }
    }
    catch (FieldNameOrIndexNotFoundException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown by findElement or
      // findElementWithIntField if we supply an invalid field name.  However,
      // we have supplied an field name defined in omlapi, which should be
      // correct, so throw an IllegalStateException to indicate that something
      // unexpected has occurred.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("Call Agent Stats field name is "
                                      + "invalid!");
    }
    catch (FieldBadTypeException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown by findElement or
      // findElementWithIntField if we supply an invalid field name.  However,
      // we have supplied an field name defined in omlapi, which should be
      // correct, so throw an IllegalStateException to indicate that something
      // unexpected has occurred.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("Call Agent Stats field type is "
                                      + "invalid!");
    }
    catch (NameUnknownException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown by findElement or
      // findElementWithIntField if the Call Agent Stats object does not
      // completely exist.  However, this should never happen, which should be
      // correct, so throw an IllegalStateException to indicate that something
      // unexpected has occurred.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("Call Agent Stats name is unknown");
    }
    catch (ElementDeletedException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if the Call Agent Stats SE does not
      // exist.  This should not happen (because it is not deletable), so throw
      // an IllegalStateException to indicate that something unexpected has
      // occurred.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("Element Deleted Exception thrown!");
    }
    catch (ElementOperationFailedException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown by the MetaView Server if our request has
      // failed for an unknown reason.  Throw an IllegalStateException to
      // indicate that something unexpected has occurred.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("Element Operation Failed Exception "
                                      + "thrown!");
    }

    return previousHourCallAgentStatsSEA;
  }

  /**
   * Given an SE Access to the 'Previous Hour' Call Agent Statistics object,
   * get the Settings and output the value of the Count of call attempts field.
   *
   * @param seAccess    An SE Access that is attached to the 'Previous Hour'
   *                    Call Agent Statistics object.
   *
   * @exception NotAttachedException
   * @exception ElementUnavailableException
   * @exception LockTimeoutException
   */
  private void getAndPrintStats(SEAccessInterface seAccess)
    throws NotAttachedException,
           ElementUnavailableException,
           LockTimeoutException
  {
    try
    {
      //-----------------------------------------------------------------------
      // Perform a getSnapshot to retrieve the settings from the server.  We
      // can discard all other information returned in the holders because we
      // don't need it.
      //-----------------------------------------------------------------------
      SettingsUserInterface settings =
                        seAccess.getSnapshot(new SequenceOfIntegersHolder(),
                                             new StringHolder(),
                                             new StringHolder(),
                                             new SequenceOfReferencesHolder());

      //-----------------------------------------------------------------------
      // Get the value of the 'Count of Call Attempts' field, and output it.
      //-----------------------------------------------------------------------
      BooleanHolder isAssigned = new BooleanHolder();
      String currentCalls =
               settings.getFieldAsStringByName(omlapi.F_CALL_ATTEMPTS___SUBSCRIBER_ORIGINATED_CALLS,
                                               isAssigned);

      Date currentDate = new Date();

      //-----------------------------------------------------------------------
      // If the value of the Count of Call Attempts field is not assigned, then
      // output an appropriate error message.
      //-----------------------------------------------------------------------
      if (!isAssigned.value)
      {
        throw new IllegalStateException("Count of Call Attempts field is not "
                                        + "assigned!");
      }

      System.out.println("Statistics for                        : "
                         + currentDate.toString());
      System.out.println("Count of call attempts (previous hour): "
                         + currentCalls);
    }
    catch (FieldNameOrIndexNotFoundException e)
    {
      //-----------------------------------------------------------------------
      // This exception is thrown if we've been unable to find a particular
      // field an object.  However, we know that the Count of Call Attempts
      // field is in the Call Agent Statistics object, so throw an
      // IllegalStateException to indicate that something unexpected has
      // occurred.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("Unable to locate 'Count of Call "
                                      + "Attempts' field in 'Summary Period' "
                                      + "Call Agent Statistics object!");
    }
    catch (ElementBrokenException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if any of the SEs attached to are broken
      // - i.e.  if they are missing one of their subcomponents.  This should
      // not happen for any of the SEs we are interested in, so throw an
      // IllegalStateException to indicate that somethig unexpected has
      // occurred.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("Element Broken Exception thrown!");
    }
    catch (ElementDeletedException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if the Previous Hour Call Agent
      // Statistics object has been deleted since we attached to it.  However,
      // the Call Agent Stats object is not deleteable, so throw an
      // IllegalStateException to indicate that something unexpected has
      // occurred.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("Element Deleted Exception thrown!");
    }
  }
}
