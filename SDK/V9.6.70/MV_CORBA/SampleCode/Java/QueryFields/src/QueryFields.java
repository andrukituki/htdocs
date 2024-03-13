/**
 * Title: QueryFields
 *
 * Description: Sample CORBA application which reads a list of queries from an
 * input file, performs them by connecting to the MetaView Server in secure
 * mode, and then writes the results to an output file.
 *
 * (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
 *
 * @version 1.0
 */

//-----------------------------------------------------------------------------
// This example application performs a sequence of queries, listed in an input
// file, on the specified MetaView Server as follows:
//
// -  Start a local ORB and log into the MetaView Server, using the CorbaHelper
//    utility methods.  Either a user login or an application login may be
//    performed depending on the command line arguments supplied.  Store the
//    returned reference to the new Client Session that the MetaView Server will
//    have instantiated, and start a thread to poll this connection in order
//    to keep it alive while the queries are performed.
//
// -  Request and store a reference to the MetaView Server's System Element (SE)
//    Access Factory using the new Client Session.
//
// -  Request and store a reference to a new SE Access using the SE Access
//    Factory.  This unattached SE can be considering as being at the root of
//    the SE tree.  A recursive child-finding routine, in which a new SE Access
//    object is obtained for a child of the current SE, is then used to
//    navigate the SE tree.
//
// -  In the input file SEs may be listed with or without indices.  If no
//    indices are given, the application will attach to each child of the
//    specified type in turn.
//
// -  Once the relevant SE for the current query is found, the values of the
//    fields listed in the input file are obtained, and the result of the query
//    is written to an output file.
//
// The application uses a input file that lists the fields to be queried.  Each
// line in the file defines a set of fields to query, with a string specifying
// the object to the left and one or more field names to the right.  The field
// names are separated from the element name, and from each other, with space
// or tab delimiters (either will work).
//
// The string specifying the object is made up of '/' delimited segments.  Each
// segment defines the name of the "O_*" object type constant in omlapi.idl
// (e.g.  "O_CFS___UMG___IS___MVD_CONNECTION"), and optionally a set of fixed
// indices delimited by dots.
//
// If the indices are omitted, the corresponding objects will be enumerated
// with the specified fields being queried for each object in turn.  Note that
// each element type segment must be given with either a complete set of
// indices or no indices at all - giving a partial set of indices will result
// in an error.
//
// The field names are given as the names of "F_*" constants in omlapi.idl
// (e.g. "F_LOCALE").
//
// An example configuration file line for illustration might be:
//
// O_CFS___UMG___IS___MVD_CONNECTION/O_CFS___UMG___IS___MVD.default/
//     O_CALL_FEATURE_SERVER.1/
//     O_SUBSCRIBER_CONFIGURATION/O_BUSINESS_GROUPS/O_BUSINESS_GROUP F_LOCALE
//     F_BILLING_TYPE                                (written all on one line).
//
// This line requests output for the Locale field in all Business Groups below
// all CFS / UMG / IS / MVD Connections.  Note that in this example the node
// and Call Feature Server indices are specified.  This is to illustrate how
// indices are specified when required - there is no real reason why these
// would need to be specified since there is only one node and one Call Feature
// Server in reality.
//
// If any of the indices to be included in the input file contain the '/'
// character, this must be preceded by a backslash ('\') as an escape
// character.  Similarly, in writing the output file, if any field values
// contain a '/' character, QueryFields will precede them with '\'.
//
// The output file contains a tab delimited line for each field queried on each
// object that indicates:
// -  whether the query was successful
// -  the fully qualified object display name
// -  the field name
// -  the field value (display string).
//
// If a valid value it retrieved, it will be output in the format:
//
// OK<TAB><Object display names (delimited with "/")><TAB><Field display name>
//     <TAB><Result>                                 (written all on one line).
//
// If the field does not exist in the object being queried, the application
// will output:
//
// Failed<TAB><Object display names><TAB><Field display name><TAB>
//     field not found                               (written all on one line).
//
// If whilst traversing the tree, no object of the required type and indexing
// is located, the application will output:
//
// Failed<TAB><Object display names, as far as we've got through the tree><TAB>
//     <Internal object type, including specified indices><TAB>object not found
//
// If one of the constant names listed in the input file does not exist, an
// error message will simply be written to the console - no line will be
// written to the output file.
//
// Example output for the configuration line illustrated earlier might be:
//
// OK Connection to VP2510 Integrated Softswitch "Nigel"/VP2510 Integrated
//     Softswitch "Nigel"/Call Feature Server/Subscriber Configuration/
//     Business Groups/Business Group "Hall"
//     Locale English (US)
// OK Connection to VP2510 Integrated Softswitch "Nigel"/VP2510 Integrated
//     Softswitch "Nigel"/Call Feature Server/Subscriber Configuration/
//     Business Groups/Business Group "Hall"
//     Billing type Flat rate
// OK Connection to VP2510 Integrated Softswitch "Nigel"/VP2510 Integrated
//     Softswitch "Nigel"/Call Feature Server/Subscriber Configuration/
//     Business Groups/Business Group "Kennedy"
//     Locale English (UK)
// OK Connection to VP2510 Integrated Softswitch "Nigel"/VP2510 Integrated
//     Softswitch "Nigel"/Call Feature Server/Subscriber Configuration/
//     Business Groups/Business Group "Kennedy"
//     Billing type Message rate
// OK Connection to UX9020 Call Feature Server "Helen"/UX9020 Call Feature
//     Server "Helen"/Call Feature Server/Subscriber Configuration/
//     Business Groups/Business Group "Vaigent"
//     Locale English (US)
// OK Connection to UX9020 Call Feature Server "Helen"/UX9020 Call Feature
//     Server "Helen"/Call Feature Server/Subscriber Configuration/
//     Business Groups/Business Group "Vaigent"
//     Billing type Message rate
// Failed Connection (at 1.2.3.4) CallFeatureServer.1
//     object not found
// Failed Connection to UX9024 Call Feature Server "Franco"/
//     UX9024 Call Feature Server "Franco"/Call Feature Server/
//     Subscriber Configuration/Business Groups BOOBusinessGroupSE
//     object not found
//
// Note that each output relating to a field or failed query will in fact be
// written on a single line.
//
// The Jacorb ORB is used as, unlike the Sun ORB used in the sample app
// GetValue, it provides the ability to make a secure connection.  The MetaView
// server must be set up to accept secure connections.
//-----------------------------------------------------------------------------
import java.io.*;
import java.util.*;

import org.omg.CORBA.*;

import com.Metaswitch.MVS.Corba.*;
import com.Metaswitch.MVS.Utils.*;
import com.Metaswitch.MVS.UtilsSecure.*;
import java.lang.reflect.*;

public class QueryFields
{
  //---------------------------------------------------------------------------
  // Member variables to store the details required to login to the MetaView
  // Server.  These are set using command line arguments - although username
  // and password will only be set if a user login is being performed.  The
  // mIsUserLogin boolean indicates the login type - true for a user login,
  // false for an application login.
  //---------------------------------------------------------------------------
  private String mEMSAddress = null;
  private String mUsername = null;
  private String mPassword = null;
  private boolean mIsUserLogin = false;

  //---------------------------------------------------------------------------
  // Objects used in accessing data in the MetaView Server.  The
  // ClientSessionInterface is used to generate the SEAccessFactoryInterface,
  // which in turn generates the SEAccessInterface which is used to query the
  // MetaView Server.
  //---------------------------------------------------------------------------
  private ClientSessionInterface mClientSession = null;

  //---------------------------------------------------------------------------
  // An SEA which will remain unattached.  An unattached SEA can be considered
  // to be at the root of the SE object tree, and so this is used to begin the
  // process of navigating the tree.
  //---------------------------------------------------------------------------
  private SEAccessInterface mRootSE = null;

  //---------------------------------------------------------------------------
  // A FileWriter object, to output query results.
  //---------------------------------------------------------------------------
  private PrintWriter mWriter = null;

  //---------------------------------------------------------------------------
  // The names of the input and output files.  The input file lists the queries
  // to perform, and the results are saved in the output file.
  //---------------------------------------------------------------------------
  private String mInputFile = null;
  private String mOutputFile = null;

  //---------------------------------------------------------------------------
  // An ArrayList in which to store the queries read from the input file.
  //---------------------------------------------------------------------------
  private ArrayList<Query> mQueries = new ArrayList<Query>();

  //---------------------------------------------------------------------------
  // A Class object, which will be used to obtain the values of constants from
  // omlapi using reflection.
  //---------------------------------------------------------------------------
  private Class mOmlapiClass = null;

  /**
   * The Query class will be used to store details of individual queries - the
   * path of element types and the set of fields queried.
   */
  private static class Query
  {
    //-------------------------------------------------------------------------
    // Within an individual query, the list of element types will be stored in
    // the 'elementTypesWithIndices' ArrayList, and the set of fields queried
    // will be stored in the fields ArrayList. e.g. for the following query:
    //
    // NetworkElementConn/NetworkElement.default/CallFeatureServer.1/
    //                              SubscriberConfigContainer/
    //                              BusinessGroupContainer/BusinessGroup Locale
    //
    // the 'mElementTypesWithIndices' ArrayList would contain the following
    // strings:
    //
    // "NetworkElementConn"
    // "NetworkElement.default"
    // "CallFeatureServer.1"
    // "SubscriberConfigContainer".
    //
    // The 'fields' ArrayList would just contain "Locale".
    //
    // Note that we're directly accessing the member variables of this class
    // for simplicity, rather than using accessor methods, which would be
    // cumbersome.
    //
    // The mQueryNumber member integer indicates from which line of the input
    // file the query originates.
    //-------------------------------------------------------------------------
    ArrayList<String[]> mElementTypesWithIndices;
    ArrayList<String> mFields;
    int mQueryNumber;

    /**
     * Constructor - initializes the member variables.
     */
    public Query()
    {
      mElementTypesWithIndices = new ArrayList<String[]>();
      mFields = new ArrayList<String>();
    }
  }

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
    String inputFile = null;
    String outputFile = null;

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
    // a) MetaView Server address, username, password, input file and output file - 5
    // arguments, indicating a user login, or
    // b) MetaView Server address, Certificate Authority (CA) certificate filename, client
    // certificate filename, client key filename, input file and output file -
    // 6 arguments, indicating an application login.
    // Therefore the number of arguments indicates the type of login to
    // perform.  If the number is not either 5 or 6, the syntax is incorrect.
    //-------------------------------------------------------------------------
    boolean isUserLogin = false;

    if (args.length == 5)
    {
      isUserLogin = true;
    }
    else if (args.length == 6)
    {
      isUserLogin = false;
    }
    else
    {
      //-----------------------------------------------------------------------
      // The number of command line arguments wasn't 5 or 6, and so must have
      // been wrong.  Display the correct usage and exit.
      //-----------------------------------------------------------------------
      System.err.println("Usage: QueryFields "
                       + "MetaViewServerIPAddress/HostName\n"
                       + "                   (Username Password) | \n"
                       + "                   (CACertificateFilename "
                       + "ClientCertificateFilename\n"
                       + "                    ClientKeyFilename)\n"
                       + "                   InputFile OutputFile");
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
      inputFile = args[3];
      outputFile = args[4];
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
      inputFile = args[4];
      outputFile = args[5];
    }
    try
    {
      //-----------------------------------------------------------------------
      // Start the ORB in secure mode.  The startORB method is overloaded, and
      // will perform a user login or an application login depending on the
      // number of parameters it is passed.
      //-----------------------------------------------------------------------
      if (isUserLogin)
      {
        //---------------------------------------------------------------------
        // Start ORB, preparing for user login.  The 'true' parameter indicates
        // that the ORB should be started in secure mode.
        //---------------------------------------------------------------------
        CorbaHelperSecure.startORB(true);
      }
      else
      {
        //---------------------------------------------------------------------
        // Start ORB, preparing for application login.  An application login is
        // automatically in secure mode.
        //---------------------------------------------------------------------
        CorbaHelperSecure.startORB(caCertFilename,
                                   clientCertFilename,
                                   clientKeyFilename);
      }

      QueryFields queryFields = new QueryFields(emsAddress,
                                                isUserLogin,
                                                username,
                                                password,
                                                inputFile,
                                                outputFile);
      queryFields.execute();
    }
    catch (IllegalStateException e)
    {
      System.out.println(e.getMessage());
    }
    finally
    {
      //-----------------------------------------------------------------------
      // Finally call stopORB.
      //-----------------------------------------------------------------------
      CorbaHelper.stopORB();

      System.exit(0);
    }
  }

  /**
   * This constructor sets up member variables using the command line input -
   * username and password, and input and output filenames.
   *
   * @param iPAddress   IP address.
   * @param isUserLogin
   *                    Indicates whether a user login (if true) or an
   *                    application login (if false) is to be performed.
   * @param userName    Username.
   * @param password    Password - all three originally obtained from the
   *                    command line parameters.
   */
  public QueryFields(String emsAddress,
                     boolean isUserLogin,
                     String username,
                     String password,
                     String inputFile,
                     String outputFile)
  {
    mEMSAddress = emsAddress;
    mIsUserLogin = isUserLogin;
    mUsername = username;
    mPassword = password;
    mInputFile = inputFile;
    mOutputFile = outputFile;
  }

  /**
   * This method first reads the list of queries to be performed from the input
   * file.  It then logs in to the MetaView Server, navigates the element tree
   * and performs all of the requested queries.
   */
  public synchronized void execute()
  {
    try
    {
      //-----------------------------------------------------------------------
      // Write a message to the console to indicate we are starting.
      //-----------------------------------------------------------------------
      System.out.println("Starting...");

      //-----------------------------------------------------------------------
      // Read the requested queries from the input file, and open the output
      // file.
      //-----------------------------------------------------------------------
      readFile(mInputFile);

      mWriter = new PrintWriter(new BufferedWriter(
                                                 new FileWriter(mOutputFile)));

      //-----------------------------------------------------------------------
      // The displayQueries method can be used for debugging, to display all of
      // the queries read from the input file on the console.  To use it, just
      // uncomment the next line.
      //-----------------------------------------------------------------------
      //displayQueries();

      //-----------------------------------------------------------------------
      // Login to the MetaView Server.
      //-----------------------------------------------------------------------
      if (mIsUserLogin)
      {
        mClientSession = CorbaHelper.login(mEMSAddress,
                                           mUsername,
                                           mPassword);
      }
      else
      {
        mClientSession = CorbaHelper.login(mEMSAddress);
      }

      //-----------------------------------------------------------------------
      // Start a thread to poll the MetaView Server regularly and keep our
      // connection alive.
      //-----------------------------------------------------------------------
      CorbaHelper.startPolling(mClientSession);

      //-----------------------------------------------------------------------
      // Obtain the requested fields - loop through each of the queries in
      // turn.  An unattached SEA is required to start the process - an
      // unattached SEA can be considered to be at the root of the SE object
      // tree.
      //-----------------------------------------------------------------------
      mRootSE = mClientSession.createSEAccess();

      for (int ii = 0; ii < mQueries.size(); ii++)
      {
        descendTreeAndPerformQuery(mRootSE,
                                   "",
                                   -1,
                                   mQueries.get(ii));
      }

      //-----------------------------------------------------------------------
      // Stop polling the MetaView Server.
      //-----------------------------------------------------------------------
      CorbaHelper.stopPolling();

      //-----------------------------------------------------------------------
      // Write a message to the console to indicate that we've finished.
      //-----------------------------------------------------------------------
      System.out.println("Finished.");
    }
    catch (IOException e)
    {
      //-----------------------------------------------------------------------
      // This error is thrown by readFile, and indicates an unspecified I/O
      // error.
      //-----------------------------------------------------------------------
      System.out.println(
                   "Error opening output file - this may be because the file\n"
                 + " - exists but is read-only\n"
                 + " - exists but is in use by another program\n"
                 + " - does not exist but cannot be created\n"
                 + " - is a folder rather than a file.");
    }
    catch (ElementOperationFailedException e)
    {
      //-----------------------------------------------------------------------
      // An element operation, e.g.  creating an SEA or an SEA factory, failed.
      // CorbaHelper contains appropriate code for dealing with the exception.
      //-----------------------------------------------------------------------
      CorbaHelper.handleUnexpectedUserException(e);
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
      System.err.println("No permission to access MetaView Server.\nThis may be because "
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
    //-------------------------------------------------------------------------
    // We catch IllegalStateExceptions and rethrow them so that they are caught
    // and handled nicely in main().  If we didn't do this they would be caught
    // as unexpected RuntimeExceptions below.
    //-------------------------------------------------------------------------
    catch (IllegalStateException e)
    {
      throw e;
    }
    //-------------------------------------------------------------------------
    // This catch is intended to catch any unexpected errors.
    //-------------------------------------------------------------------------
    catch (RuntimeException e)
    {
      System.err.println("Unexpected exception: " + e.toString());
      e.printStackTrace();
      System.err.println(e.getMessage());
    }
    finally
    {
      //-----------------------------------------------------------------------
      // Whetever happens elsewhere in the code, it is important that the
      // output file is closed - otherwise all output will have been lost.
      //-----------------------------------------------------------------------
      if (mWriter != null)
      {
        mWriter.flush();
        mWriter.close();
      }

      //-----------------------------------------------------------------------
      // Destroy the root SEA to free memory on the MetaView Server.
      //-----------------------------------------------------------------------
      if (mRootSE != null)
      {
        mRootSE.destroy();
      }

      //-----------------------------------------------------------------------
      // Log out of the server, if we managed to log in above.
      //-----------------------------------------------------------------------
      if (mClientSession != null)
      {
        mClientSession.logout();
        mClientSession = null;
      }
    }
  }

 /**
  * Reads the input file line by line, storing the contents of each line as a
  * Query object.  The set of element types (separated by "/" in the input
  * file) is placed in an ArrayList called mElementTypesWithIndices in the
  * Query object.  The set of fields (separated by spaces) is placed in an
  * ArrayList called mFields.
  *
  * The set of Query objects is stored in the ArrayList mQueries.
  *
  * @param filename    The filename of the input file.
  */
  private void readFile(String filename)
  {
    try
    {
      //-----------------------------------------------------------------------
      // Initialise a counter.  This is only used if a line in the input is
      // incorrectly formatted, so that we can print which line it was to the
      // console.
      //-----------------------------------------------------------------------
      int lineCounter = 0;

      //-----------------------------------------------------------------------
      // Later on we will need to obtain the values of constants defined in
      // omlapi.  We will do this by reflection, using a Class object for
      // omlapi, which we create here.
      //-----------------------------------------------------------------------
      try
      {
        mOmlapiClass = Class.forName("com.Metaswitch.MVS.Corba.omlapi");
      }
      catch (ClassNotFoundException e)
      {
        //---------------------------------------------------------------------
        // The omlapi class file was not found.  This is an unexpected error,
        // so throw an IllegalStateException.
        //---------------------------------------------------------------------
        throw new IllegalStateException("com.Metaswitch.MVS.Corba.omlapi not "
                                      + "found!");
      }

      //-----------------------------------------------------------------------
      // Create a BufferedReader object.
      //-----------------------------------------------------------------------
      FileReader fileReader = new FileReader(filename);
      BufferedReader bufferedReader = new BufferedReader(fileReader);

      //-----------------------------------------------------------------------
      // Read the first line from the file.
      //-----------------------------------------------------------------------
      String currentLine = bufferedReader.readLine();

      if (currentLine == null)
      {
        System.out.println("Warning! The input file is empty.");
      }

      //-----------------------------------------------------------------------
      // Start a loop.  On each cycle, the next line of the file is read.  When
      // the whole file has been read the loop terminates.
      //-----------------------------------------------------------------------
      while (currentLine != null)
      {
        //---------------------------------------------------------------------
        // Set queryStillValid to 'true'.  It may be set to false later if we
        // find that one of the omlapi constant names given in the input file
        // is invalid.
        //---------------------------------------------------------------------
        boolean queryStillValid = true;

        //---------------------------------------------------------------------
        // Increment the line counter.
        //---------------------------------------------------------------------
        lineCounter++;

        //---------------------------------------------------------------------
        // Check the line isn't just a blank line before we even start looking
        // at it - if it is, simply ignore it.  We also check that the line
        // doesn't start with a space - if it does, it is badly formatted since
        // this gives an element name of "", and we will have to display an
        // error message and move on.
        //---------------------------------------------------------------------
        if ((currentLine.length() > 0) && !(currentLine.startsWith(" ")))
        {
          //-------------------------------------------------------------------
          // Parse the last line read.
          //-------------------------------------------------------------------
          String[] objectAddressAndFieldList = currentLine.split("[ ,\t]");

          //-------------------------------------------------------------------
          // objectAddressAndFieldList should have a length of at least 2 -
          // otherwise the line is not correctly formatted, in which case an
          // error message will be written to the console.
          //-------------------------------------------------------------------
          if (objectAddressAndFieldList.length > 1)
          {
            //-----------------------------------------------------------------
            // The first entry in objectAddressAndFieldList will contain the
            // complete system element address.  Therefore this address should
            // be extracted and parsed.  Thereafter the first entry in
            // objectAddressAndFieldList will be ignored.
            //-----------------------------------------------------------------
            String[] elementList = splitWithEscapeCharacter(
                                                  objectAddressAndFieldList[0],
                                                  '/',
                                                  '\\');

            //-----------------------------------------------------------------
            // Create a new Query object, and fill up its
            // mElementTypesWithIndices and mFields ArrayLists.  Then add it to
            // the mQueries ArrayList.
            //-----------------------------------------------------------------
            Query currentQuery = new Query();

            //-----------------------------------------------------------------
            // Loop through all of the element type strings, process them and
            // add them to the current query.
            //-----------------------------------------------------------------
            for (int ii = 0;
                 queryStillValid && (ii < elementList.length);
                 ii++)
            {
              //---------------------------------------------------------------
              // Each element type string is split, using "." as a delimiter,
              // before being stored in currentQuery.  Hence what is added is
              // an array, with the first entry being the element type, and
              // subsequent entries containing indices.  Because "." is a
              // reserved character, we have to use "\\.", with the slashes
              // acting as escape characters.
              //
              // The '-1' parameter indicates to split() that we want it to
              // include empty indices, i.e. between adjacent dots.
              //---------------------------------------------------------------
              String[] elementArray = elementList[ii].split("\\.", -1);

              //---------------------------------------------------------------
              // The element name we are reading from the input file is the
              // name of a constant string defined in omlapi.  This constant in
              // turn gives the internal name of the element, which can be used
              // in a query.  We therefore need to obtain the value of this
              // constant before a query can be performed.
              //---------------------------------------------------------------
              elementArray[0] = obtainNameFromOmlapi(elementArray[0],
                                                     lineCounter);

              //---------------------------------------------------------------
              // If the constant name read from the input file was invalid,
              // then obtainNameFromOmlapi() will have returned null.  We can't
              // allow this query to proceed, as attempting to access an
              // invalid object type may cause an internal error in the
              // MetaView Server.  Instead, we declare that this query is no
              // longer valid.  This will result in us skipping the rest of the
              // processing for this query and moving on to the next one.
              //---------------------------------------------------------------
              if (elementArray[0] == null)
              {
                queryStillValid = false;
              }
              else
              {
                //-------------------------------------------------------------
                // Store the array in currentQuery.
                //-------------------------------------------------------------
                currentQuery.mElementTypesWithIndices.add(elementArray);
              }
            }

            //-----------------------------------------------------------------
            // We only want to continue processing the query if it is valid,
            // i.e.  does not contain any invalid omlapi constant names.
            //-----------------------------------------------------------------
            if (queryStillValid)
            {
              //---------------------------------------------------------------
              // We want to keep a count of the number of valid field names in
              // the current query.  If there are no valid field names then the
              // query should be ignored.
              //---------------------------------------------------------------
              int numberOfValidFields = 0;

              //---------------------------------------------------------------
              // Add the requested fields to currentQuery.
              //---------------------------------------------------------------
              for (int ii = 1; ii < objectAddressAndFieldList.length; ii++)
              {
                //-------------------------------------------------------------
                // The field names given in the input file are the names of
                // constants in omlapi, the values of which are the internal
                // names of the fields.  We need to use these internal names
                // for the query, so we should obtain them from omlapi.
                //-------------------------------------------------------------
                String fieldName = obtainNameFromOmlapi(
                                                 objectAddressAndFieldList[ii],
                                                 lineCounter);

                //-------------------------------------------------------------
                // If the omlapi constant name for the field was valid, we
                // should add this field to the current query.  If it wasn't,
                // fieldName will be null - we should ignore this field and
                // move on.  An error message will have been printed to the
                // console by obtainNameFromOmlapi().
                //-------------------------------------------------------------
                if (fieldName != null)
                {
                  currentQuery.mFields.add(fieldName);

                  numberOfValidFields++;
                }
              }

              //---------------------------------------------------------------
              // Before we save this query, check that it contained some valid
              // fields.
              //---------------------------------------------------------------
              if (numberOfValidFields > 0)
              {
                //-------------------------------------------------------------
                // Record from which line the query originated.
                //-------------------------------------------------------------
                currentQuery.mQueryNumber = lineCounter;

                //-------------------------------------------------------------
                // Add the completed query to the ArrayList of queries.
                //-------------------------------------------------------------
                mQueries.add(currentQuery);
              }
            }
          }
          else
          {
            //-----------------------------------------------------------------
            // Line was not correctly formatted - print an error message to the
            // console and move on.
            //-----------------------------------------------------------------
            System.out.println("Warning - incorrect formatting on input file "
                             + "line " + lineCounter + "\n"
                             + "- Line contained too few parameters.");
          }
        }
        else if (currentLine.startsWith(" "))
        {
          //-------------------------------------------------------------------
          // Line began with a space - display an error message and move on.
          //-------------------------------------------------------------------
          System.out.println("Warning - incorrect formatting on input file "
                           + "line " + lineCounter + "\n"
                           + "- Line began with a space and will be ignored.");
        }

        //---------------------------------------------------------------------
        // Read the next line from the file.
        //---------------------------------------------------------------------
        currentLine = bufferedReader.readLine();
      }
    }
    catch (java.io.FileNotFoundException e)
    {
      //-----------------------------------------------------------------------
      // File not found error.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("Error - input file not found: "
                                                                   + filename);
    }
    catch (java.io.IOException e)
    {
      //-----------------------------------------------------------------------
      // I/O error.
      //-----------------------------------------------------------------------
      throw new IllegalStateException("I/O error - unable to read from the "
                                                  + "input file: " + filename);
    }
  }

  /**
   * Descend the tree according to the element types defined in the specified
   * query, from the specified parent (which is at the specified depth).  Once
   * the element specified in the query is reached, perform the query.
   *
   * @returns           The element name of the parentSEA.
   *
   * @param parentSEA   The SEA to descend from.
   * @param parentDisplayNames
   *                    A concatenation of all of the parent display names.
   * @param depth       The depth our parentSEA is at.
   * @param query       The query to perform.
   */
  private String descendTreeAndPerformQuery(SEAccessInterface parentSEA,
                                            String parentDisplayNames,
                                            int depth,
                                            Query query)
  {
    //-------------------------------------------------------------------------
    // Create objects to accept data when the getSnapshot method of the
    // parentSEA is called.
    //-------------------------------------------------------------------------
    SettingsUserInterface settings = null;
    StringHolder elementName = new StringHolder();
    StringHolder displayName = new StringHolder();

    //-------------------------------------------------------------------------
    // Get the details of the requested child's type and indices from the
    // query.  It is important to extract this early, as the information is
    // used in writing error messages to the output file.  We shouldn't do this
    // though if we've reached the destination depth, as there are no more
    // children.
    //-------------------------------------------------------------------------
    String[] childTypeWithIndices = null;

    //-------------------------------------------------------------------------
    // Check if the depth is equal to the number of element types in the
    // mElementTypesWithIndices ArrayList in the current query.  If it is,
    // the current element will have no children.
    //-------------------------------------------------------------------------
    boolean hasChildren =
                        (depth != (query.mElementTypesWithIndices.size() - 1));

    if (hasChildren)
    {
      childTypeWithIndices = (query.mElementTypesWithIndices.get(depth + 1));
    }

    try
    {
      //-----------------------------------------------------------------------
      // A depth of -1 indicates that this is the first time this recursive
      // method is being run, and our parentSEA is unattached.  Attempting to
      // run the getSnapshot method will cause an error, and so we should avoid
      // the next section of code.
      //-----------------------------------------------------------------------
      if (depth != -1)
      {
        //---------------------------------------------------------------------
        // Get a snapshot of parentSEA, in order to provide a
        // SettingsUserInterface object from which to perform queries, as well
        // as the element name and display name.  The display name is
        // concatenated with parentDisplayNames to give a complete path to the
        // current element.
        //---------------------------------------------------------------------
        settings = parentSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                         elementName,
                                         displayName,
                                         new SequenceOfReferencesHolder());

        //---------------------------------------------------------------------
        // The display name is translated to human readable text.  Should the
        // translation fail there is nothing we can do about it, so in effect
        // we do not care whether or not it was successful - hence a throw-away
        // BooleanHolder is passed to nlsTranslate.
        //---------------------------------------------------------------------
        String childDisplayName = CorbaHelper.nlsTranslate(
                                                          displayName.value,
                                                          new BooleanHolder());

        //---------------------------------------------------------------------
        // If there are any "/" characters in the display name then we should
        // add a "\" escape character before them.  This is because "/" is the
        // character used to delimit element names in the output file.  If
        // another application parses the output then the presence of
        // additional "/" characters could cause problems, so we use an escape
        // character to indicate that they are not present as delimiters.
        //
        // Note that we have to use "\\" to add a "\" character, because "\" is
        // a Java escape character as well.
        //---------------------------------------------------------------------
        childDisplayName.replaceAll("/", "\\/");

        //---------------------------------------------------------------------
        // The child's display name is then concatenated with
        // parentDisplayNames to give a complete path to the current element.
        //---------------------------------------------------------------------
        parentDisplayNames += childDisplayName;
      }

      //-----------------------------------------------------------------------
      // If the current element has no children then we've reached the object
      // we're looking for - we should therefore perform a query.  If not, we
      // need to keep on navigating the tree.
      //-----------------------------------------------------------------------
      if (!hasChildren)
      {
        //---------------------------------------------------------------------
        // Query each of the fields specified for this query in the input file.
        //---------------------------------------------------------------------
        for (int ii = 0; ii < query.mFields.size(); ii++)
        {
          performQuery(settings,
                       parentDisplayNames,
                       query.mFields.get(ii));
        }
      }
      else
      {
        //---------------------------------------------------------------------
        // We know we've still got another level to go, so we can put a slash
        // in the parentDisplayNames string, which will later be followed by
        // another element type name.  We don't want to this before we've put
        // in any type names at all though - so check depth isn't -1.
        //---------------------------------------------------------------------
        if (depth > -1)
        {
          parentDisplayNames += "/";
        }

        //---------------------------------------------------------------------
        // We need to continue navigating the tree, and so should move on to
        // the children.  The input file will specify the type of child to look
        // at, and may specify indices.  If it does, then we should simply
        // attach to the child with those indices.  If it doesn't, then the
        // query is requesting information on all children of the specified
        // type, regardless of indices.  There may be several, and we should
        // look at each in turn.
        //---------------------------------------------------------------------

        //---------------------------------------------------------------------
        // Initialize an SEA to attach to the child SE.  Also we need a string
        // to hold the name of the previous child if we are looping through a
        // number of children.
        //---------------------------------------------------------------------
        SEAccessInterface childSEA = null;
        String previousChildName = null;

        boolean more = true;

        //---------------------------------------------------------------------
        // Begin a loop.  If there is only one child, or a child is specified
        // by indices, this loop will only run once.
        //---------------------------------------------------------------------
        while (more)
        {
          try
          {
            //-----------------------------------------------------------------
            // Check the length of the array containing the child type.  The
            // first entry contains the type, and subsequent entries contain
            // indices.  Therefore a length greater than 1 indicates the
            // presence of indices.
            //-----------------------------------------------------------------

            if (childTypeWithIndices.length > 1)
            {
              //---------------------------------------------------------------
              // Child has indices specified, and so we should attach to it
              // directly.
              //---------------------------------------------------------------
              childSEA = CorbaHelper.getChildOfTypeWithIndices(
                                                         mClientSession,
                                                         parentSEA,
                                                         childTypeWithIndices);

              //---------------------------------------------------------------
              // We don't want to do this again, as we've found the single
              // child specified by the indices - therefore set 'more' to
              // false, to prevent looping.
              //---------------------------------------------------------------
              more = false;
            }
            else
            {
              //---------------------------------------------------------------
              // Child does not have indices specified, and so we should
              // enumerate all children of the correct type.  If we haven't yet
              // found any children, get the first child.  Otherwise, get the
              // next child.
              //---------------------------------------------------------------
              if (previousChildName == null)
              {
                childSEA = parentSEA.findElement(childTypeWithIndices[0]);
              }
              else
              {
                childSEA = parentSEA.findNextElement(childTypeWithIndices[0],
                                                     previousChildName);
              }
            }
          }

          //-------------------------------------------------------------------
          // The following exceptions come from getChildOfTypeWithIndices,
          // getFirstChildOfType and getNextChildOfType.  These are similar
          // methods and so have many exceptions in common.  In each case an
          // error message is written to the console, and previousChildName is
          // set to null.
          //
          // If these errors have occurred then childSEA will also be null and
          // so, within the 'if' clause below these exceptions, an 'object not
          // found' line will be written to the output file.  Also, 'more' is
          // set to false, so that we do not attempt to find any more children
          // in this branch.  The application will then move on to the next SE.
          //-------------------------------------------------------------------
          catch (ElementOperationFailedException e)
          {
            //-----------------------------------------------------------------
            // An unknown error occurred while attempting
            // getChildOfTypeWithIndices.
            //-----------------------------------------------------------------
            System.out.println("An unexpected error occurred while attempting"
                             + " to find a system element in query "
                             + query.mQueryNumber + ".");
            previousChildName = null;
          }
          catch (BadIndicesException e)
          {
            //-----------------------------------------------------------------
            // The indices used in getChildOfTypeWithIndices were invalid.
            //-----------------------------------------------------------------
            System.out.println("Supplied indices invalid in query "
                             + query.mQueryNumber + " - object not "
                             + "found.");
            previousChildName = null;
          }
          catch (InvalidElementTypeException e)
          {
            //-----------------------------------------------------------------
            // The element type used in getChildOfTypeWithIndices was invalid.
            //-----------------------------------------------------------------
            System.out.println("Supplied element type invalid in query "
                             + query.mQueryNumber + " - object not "
                             + "found.");
            previousChildName = null;
          }
          catch (ElementDeletedException e)
          {
            //-----------------------------------------------------------------
            // The element requested in the query does not exist.
            //-----------------------------------------------------------------
            System.out.println("A requested element of type "
                             + childTypeWithIndices[0] + " in query "
                             + query.mQueryNumber + " does not exist.");
            previousChildName = null;
          }
          catch (NameUnknownException e)
          {
            //-----------------------------------------------------------------
            // This exception should only be thrown when the CORBA app is
            // trying to create a new SE but hasn't yet applied the settings.
            // That isn't what we're doing here, so this exception is
            // unexpected.
            //-----------------------------------------------------------------
            System.out.println ("Unexpected NameUnknownException.");
            previousChildName = null;
          }
          catch (ElementUnavailableException e)
          {
            //-----------------------------------------------------------------
            // The requested element is for some reason unavailable - this is
            // usually because the connection between the MetaView Server and
            // the NE has gone down.
            //-----------------------------------------------------------------
            System.out.println ("A requested element is unavailable in query "
                              + query.mQueryNumber);
            previousChildName = null;
          }
          catch (IllegalStateException e)
          {
            //-----------------------------------------------------------------
            // An unexpected exception was thrown in getChildOfTypeWithIndices
            // or descendTreeAndPerformQuery.  The child SE will be found to be
            // null below, and an error line will be written to the output
            // file.
            //-----------------------------------------------------------------
            System.out.println("Unexpected error while navigating system "
                                                            + "element tree.");
            previousChildName = null;
          }

          //-------------------------------------------------------------------
          // Check if the next child existed.  If it didn't, stop looping.  If
          // it did, then run this same routine on the child, to recurse to a
          // lower level.
          //-------------------------------------------------------------------
          if (childSEA == null)
          {
            more = false;

            //-----------------------------------------------------------------
            // Check if this was the first child.  If it was, that indicates
            // that no children at all were found.  Write an 'object not found'
            // line to the output file and exit the method.
            //-----------------------------------------------------------------
            if (previousChildName == null)
            {
              writeLineToOutputFile(parentDisplayNames, childTypeWithIndices);
            }
          }
          else
          {
            //-----------------------------------------------------------------
            // Recurse this method, using the child as the starting point.
            //-----------------------------------------------------------------
            try
            {
              previousChildName = descendTreeAndPerformQuery(
                                                            childSEA,
                                                            parentDisplayNames,
                                                            depth + 1,
                                                            query);

              //---------------------------------------------------------------
              // If null was returned, that indicates an error so do not
              // continue the query at this level.
              //---------------------------------------------------------------
              if (previousChildName == null)
              {
                more = false;
              }
            }
            catch (IllegalStateException e)
            {
              //---------------------------------------------------------------
              // The only exception that the descendTreeAndPerformQuery method
              // can throw is an IllegalStateException, and this is unlikely -
              // see comment where it is thrown below.  We should just write an
              // 'object not found' line to the output file and exit this
              // branch.
              //---------------------------------------------------------------
              System.out.println("Unexpected error while navigating system "
                                                            + "element tree.");
              writeLineToOutputFile(parentDisplayNames, childTypeWithIndices);
              more = false;
            }

            //-----------------------------------------------------------------
            // Important - destroy our SE access to the child to free memory on
            // the MetaView Server.  Then set childSEA back to null so that the
            // above exception handling works properly the next time around.
            //-----------------------------------------------------------------
            childSEA.destroy();
            childSEA = null;
          }
        }
      }
    }

    //-------------------------------------------------------------------------
    // These last errors result from a failure in the getSnapshot method.  An
    // 'object not found' line is written to the output file, and the method
    // exits.
    //-------------------------------------------------------------------------
    catch (ElementDeletedException e)
    {
      System.out.println("Error - unable to take snapshot - element deleted.");
      writeLineToOutputFile(parentDisplayNames, childTypeWithIndices);
    }
    catch (NotAttachedException e)
    {
      System.out.println("Error - unable to take snapshot - SEA not "
                                                                + "attached.");
      writeLineToOutputFile(parentDisplayNames, childTypeWithIndices);
    }
    catch (ElementUnavailableException e)
    {
      System.out.println("Error - unable to take snapshot - element "
                                                             + "unavailable.");
      writeLineToOutputFile(parentDisplayNames, childTypeWithIndices);
    }
    catch (ElementBrokenException e)
    {
      System.out.println("Error - unable to take snapshot - element broken.");
      writeLineToOutputFile(parentDisplayNames, childTypeWithIndices);
    }

    //-------------------------------------------------------------------------
    // Return the name of the element.
    //-------------------------------------------------------------------------
    return elementName.value;
  }

  /**
   * Queries a field (one of those specified in a query) on the specified
   * element, and writes the result to file.
   *
   * @param settings    The SettingsUserInterface object which will actually
   *                    obtain the queried values.
   * @param displayNames
   *                    A concatenation of all the display names of the element
   *                    types in the path.
   * @param field       The field to find the value of.
   */
  private void performQuery(SettingsUserInterface settings,
                            String displayNames,
                            String field)
  {
    //-------------------------------------------------------------------------
    // We should first check that the field string is not empty - this would
    // generally indicate an excess space in the input file, and we should just
    // ignore this query.
    //-------------------------------------------------------------------------
    if (field.length() > 0)
    {
      //-----------------------------------------------------------------------
      // Create a boolean to record whether the query was successful or not,
      // and a BooleanHolder to pass into the query request.  Also create a
      // string to hold the result and a string to hold the field's display
      // name.
      //
      // We initialise fieldDisplayName to the internal name of the field.  If
      // we fail to obtain the display name, because the field doesn't exist in
      // the current object, we can at least still use the internal name to
      // write an error line to the output file.
      //-----------------------------------------------------------------------
      boolean querySuccessful = false;
      BooleanHolder isAssigned = new BooleanHolder();
      String queryResult = null;
      String fieldDisplayName = field;

      try
      {
        //---------------------------------------------------------------------
        // Perform the query, and also get the display name of the field.
        //---------------------------------------------------------------------
        queryResult = settings.getFieldAsStringByName(field, isAssigned);
        fieldDisplayName = settings.getDisplayNameByName(field);

        //---------------------------------------------------------------------
        // If we have got this far without an exception being thrown, the query
        // must have been successful.
        //---------------------------------------------------------------------
        querySuccessful = true;

        //---------------------------------------------------------------------
        // Translate the result from an NLS string into human readable text.
        // If the translation wasn't successful we can't do anything about it,
        // so in effect we don't care whether or not it was successful - we
        // therefore pass in a throw-away BooleanHolder.  We do the same thing
        // for the field display name.
        //---------------------------------------------------------------------
        if (isAssigned.value)
        {
          queryResult = CorbaHelper.nlsTranslate(queryResult,
                                                 new BooleanHolder());
        }
        else
        {
          //-------------------------------------------------------------------
          // If the field was not assigned, set the result to an empty string.
          //-------------------------------------------------------------------
          queryResult = "";
        }

        fieldDisplayName = CorbaHelper.nlsTranslate(fieldDisplayName,
                                                    new BooleanHolder());
      }
      catch (FieldNameOrIndexNotFoundException e)
      {
        //---------------------------------------------------------------------
        // The field name could not be found in the specified element.  The
        // fact that the query was unsuccessful will be recorded in the output
        // file, so no other action needs to be taken.
        //---------------------------------------------------------------------
      }

      //-----------------------------------------------------------------------
      // Write the result to file.  The querySuccessful boolean indicates
      // whether the line written to the output file should be an 'OK' line or
      // a 'field not found' line.
      //-----------------------------------------------------------------------
      writeLineToOutputFile(true,
                            querySuccessful,
                            displayNames,
                            fieldDisplayName,
                            queryResult);
    }
  }

  /**
   * Writes an 'object not found' error to the output file.  Calls the main
   * method below, passing 'false' for foundElement and foundField.
   *
   * @param displayNames
   *                    The complete display name of the element in question,
   *                    including all parent display names, delimited by "/".
   * @param elementNameWithIndices
   *                    A string array containing the name of the element that
   *                    could not be found in the first position, and specified
   *                    indices in subsequent positions.
   */
  private void writeLineToOutputFile(String displayNames,
                                     String[] elementNameWithIndices)
  {
    //-------------------------------------------------------------------------
    // Because of the way the displayNames string is put together, it will have
    // an erroneous "/" at the end - another element name was expected to
    // follow.  Therefore this should be removed.
    //
    // The only way this "/" could fail to be present is if the query failed
    // before finding even the first object, in which case it will be a zero
    // length string.  We check for this before removing the "/".
    //-------------------------------------------------------------------------
    if (displayNames.length() > 0)
    {
      displayNames = displayNames.substring(0, displayNames.length() - 1);
    }

    //-------------------------------------------------------------------------
    // Create a single string which is a concatenation of the string array,
    // containing the name of the object that could not be found followed by
    // its indices, with each index separated by a ".".
    //-------------------------------------------------------------------------
    String concatenatedNameWithIndices = elementNameWithIndices[0];

    for (int ii = 1; ii < elementNameWithIndices.length; ii++)
    {
      concatenatedNameWithIndices += "." + elementNameWithIndices[ii];
    }

    writeLineToOutputFile(false,
                          false,
                          displayNames,
                          concatenatedNameWithIndices,
                          "");
  }

  /**
   * Writes a result to the output file.  The result line indicates whether the
   * test was successful, the path of the element whose field was being
   * queried, the field name, and the result.  Lines may also be written to
   * indicate that the field was not found, or that the object itself was not
   * found.
   *
   * @param foundElement
   *                    Indicates whether or not the element whose field was to
   *                    be queried was found.
   * @param foundField
   *                    Indicates whether the field requested in the query was
   *                    found.
   * @param displayNames
   *                    The complete display name of the element in question,
   *                    including all parent display names, delimited by "/".
   * @param fieldOrElementName
   *                    The display name of the field to be queried. If the
   *                    element in question was not found, the element name,
   *                    including indices, will be displayed in the equivalent
   *                    position in the output line instead. Hence where this
   *                    method is called to indicate an 'object not found'
   *                    error, the name of the element will be passed in as
   *                    this parameter.
   * @param result      The value of the queried field.
   */
  private void writeLineToOutputFile(boolean foundElement,
                                     boolean foundField,
                                     String displayNames,
                                     String fieldOrElementName,
                                     String result)
  {
    //-------------------------------------------------------------------------
    // Initialize a string which will be used to build up the line to be
    // written to the file.
    //-------------------------------------------------------------------------
    String outputString = null;

    //-------------------------------------------------------------------------
    // Firstly, indicate in the output line whether the query was successful.
    // Individual entries in the output line will be separated by tabs, hence
    // the "\t".
    //-------------------------------------------------------------------------
    if (foundField)
    {
      outputString = "OK\t";
    }
    else
    {
      outputString = "Failed\t";
    }

    //-------------------------------------------------------------------------
    // Then give the full display name of the SE path and the field name.  Note
    // that if an 'object not found' error line is being written to the file,
    // the 'field' string will actually contain the name of the object rather
    // than the field.
    //-------------------------------------------------------------------------
    outputString += displayNames + "\t" + fieldOrElementName + "\t";

    //-------------------------------------------------------------------------
    // Finally either give the result, or indicate that the field was not
    // found, or even that the object wasn't found.
    //-------------------------------------------------------------------------
    if (foundField)
    {
      outputString += result;
    }
    else if (foundElement)
    {
      outputString += "field not found";
    }
    else
    {
      outputString += "object not found";
    }

    //-------------------------------------------------------------------------
    // Now write the output line to the file.
    //-------------------------------------------------------------------------
    mWriter.println(outputString);
  }

  /**
   * Display the contents of the mQueries ArrayList - for debugging purposes.
   */
  private void displayQueries()
  {
    if (mQueries.size() == 0)
    {
      System.out.println("No queries have been saved.");
    }
    else
    {
      //-----------------------------------------------------------------------
      // Loop through all of the queries in mQueries.
      //-----------------------------------------------------------------------
      for (int ii = 0; ii < mQueries.size(); ii++)
      {
        System.out.println("\nQuery no. " + ii + ":");

        Query currentQuery = (Query)mQueries.get(ii);

        //---------------------------------------------------------------------
        // Display all of the element types in the query.
        //---------------------------------------------------------------------
        System.out.println("\nElement types:");

        for (int jj = 0; jj < currentQuery.mElementTypesWithIndices.size();
                                                                          jj++)
        {
          String[] elementTypeWithIndices =
                                 currentQuery.mElementTypesWithIndices.get(jj);

          String currentElementType = elementTypeWithIndices[0];

          System.out.println(currentElementType);
        }

        //---------------------------------------------------------------------
        // Display all of the fields in the query.
        //---------------------------------------------------------------------
        System.out.println("\nFields:");

        for (int jj = 0; jj < currentQuery.mFields.size(); jj++)
        {
          String currentField = currentQuery.mFields.get(jj);

          System.out.println(currentField);
        }
      }
    }
  }

  /**
   * Obtains an internal name which is defined as a constant in omlapi, using
   * the name of the constant (which will have been given in the input file).
   *
   * @param constantName
   *                    The name of the constant to look up in omlapi.
   * @param lineCounter
   *                    The line of the input file we are currently on - used
   *                    for error messages.
   *
   * @returns           The value of the named constant.
   */
  private String obtainNameFromOmlapi(String constantName, int lineCounter)
  {
    String returnName = null;

    try
    {
      //-----------------------------------------------------------------------
      // Get the value of the constant using reflection, with the mOmlapiClass
      // object which was assigned to the omlapi class in readFile().  Note
      // that the get() method has to be passed null when requesting the value
      // of a static variable.
      //-----------------------------------------------------------------------
      returnName = (String)mOmlapiClass.getField(constantName).get(null);
    }
    catch (NoSuchFieldException e)
    {
      //-----------------------------------------------------------------------
      // The name given in the input file does not correspond to any constant
      // in omlapi.  Display an error message here, and leave returnName as
      // null.  readFile() will detect the null value, and the current query
      // will be declared invalid.
      //-----------------------------------------------------------------------
      System.out.println("Warning - name not valid:");
      System.out.println(" - Line " + lineCounter + ": '" + constantName
                                                                        + "'");
    }
    catch (IllegalAccessException e)
    {
      //-----------------------------------------------------------------------
      // This indicates that we do not have access to the constant value in
      // omlapi - this is unexpected, so throw an IllegalStateException.
      //-----------------------------------------------------------------------
      throw new IllegalStateException ("Error! No access to "
                                     + "com.Metaswitch.MVS.Corba.omlapi!");
    }

    return returnName;
  }

  /**
   * Performs essentially the same function as the String split() method -
   * divides a string into a string array using a specified delimiter.
   * However, this method allows an escape character to be placed before the
   * delimiter so that the delimiter is ignored.  The escape character will
   * then be left out when the string array is created.
   *
   * @param inputString
   *                    The string to be split.
   * @param delimiter   The delimiter with which to split the string.
   * @param escapeCharacter
   *                    The escape character which will cause the delimiter to
   *                    be ignored.
   */
  private String[] splitWithEscapeCharacter(String inputString,
                                            char delimiter,
                                            char escapeCharacter)
  {
    //-------------------------------------------------------------------------
    // An ArrayList in which to store the substrings as they are generated.
    //-------------------------------------------------------------------------
    ArrayList<String> outputList = new ArrayList<String>();

    //-------------------------------------------------------------------------
    // A string to hold the current substring and a char to hold each character
    // as it is read from the input string.
    //-------------------------------------------------------------------------
    StringBuffer currentSubstring = new StringBuffer();
    char currentChar;

    //-------------------------------------------------------------------------
    // Loop through the input string, checking its characters one at a time.
    //-------------------------------------------------------------------------
    for (int currentPositionInString = 0;
         currentPositionInString < inputString.length();
         currentPositionInString++)
    {
      //-----------------------------------------------------------------------
      // Get the current character.
      //-----------------------------------------------------------------------
      currentChar = inputString.charAt(currentPositionInString);

      //-----------------------------------------------------------------------
      // Check what it is.
      //-----------------------------------------------------------------------
      if (currentChar == delimiter)
      {
        //---------------------------------------------------------------------
        // Since we've come to a delimiter, we should save the current string
        // in the ArrayList and then reset the substring holder.
        //---------------------------------------------------------------------
        outputList.add(currentSubstring.toString());

        currentSubstring = new StringBuffer();
      }
      else if (currentChar == escapeCharacter)
      {
        //---------------------------------------------------------------------
        // We've come to an escape character.  If it's followed by a delimiter
        // then we should add the delimiter to the current substring, and then
        // move forward an extra character so that the delimiter is not found
        // on the next cycle.
        //---------------------------------------------------------------------
        if (inputString.charAt(currentPositionInString + 1) == delimiter)
        {
          currentSubstring.append(delimiter);
          currentPositionInString++;
        }
      }
      else
      {
        //---------------------------------------------------------------------
        // The current character is neither a delimiter or an escape character
        // - simply concatenate it with the current substring and move on.
        //---------------------------------------------------------------------
        currentSubstring.append(currentChar);
      }
    }

    //-------------------------------------------------------------------------
    // Add the final substring to the ArrayList.
    //-------------------------------------------------------------------------
    outputList.add(currentSubstring.toString());

    //-------------------------------------------------------------------------
    // Convert the ArrayList to a string array and return it.
    //-------------------------------------------------------------------------
    return (String[])outputList.toArray(new String[outputList.size()]);
  }
}
