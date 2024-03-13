//-----------------------------------------------------------------------------
// GlobalSearch.java
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material.
//
// Sample application demonstrating the Network Wide Web Services API.
//
// It uses Apache Axis2 and the "untyped" WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.MetaSwitch.SRB.SOAP.ShPull;
import com.MetaSwitch.SRB.SOAP.ShPullResponse;
import com.MetaSwitch.SRB.SOAP.ShService;
import com.MetaSwitch.SRB.SOAP.ShServiceStub;
import com.MetaSwitch.SRB.SOAP.TExtendedResult;
import com.MetaSwitch.SRB.SOAP.TUserData;

/**
 * Global search sample application
 *
 * Performs a Meta_Global_Search ShPull on the MetaView Server as configured in
 * ShService.wsdl/ShServiceTyped.wsdl.
 *
 * Takes parameters as follows:
 * <object type>
 * <comma-separated list of output fields>
 * [optional SQL where clause]
 *
 * See the documentation for more details.
 */
public class GlobalSearch
{
  private static final String USAGE = "Usage: GlobalSearch " +
                                      "<NWSAP username> " +
                                      "<NWSAP password> " +
                                      "<objectType> " +
                                      "<outputField1,outputField2,...> " +
                                      "[where clause]";

  private static final String META_GLOBAL_SEARCH_SI = "Meta_Global_Search";

  private static ShUntypedUtilities utilities = new ShUntypedUtilities();

  /**
   * Extract the arguments, construct the user identity and perform the ShPull.
   *
   * @param args - the command line arguments.
   *
   * @throws WrongParametersException
   * @throws UnsupportedEncodingException
   */
  public static void main(String[] args)
  {
    String nwsapUsername = "";
    String nwsapPassword = "";
    String userIdentity = "";

    try
    {
      if (args.length == 4)
      {
        //---------------------------------------------------------------------
        // Four arguments - the optional where clause was not provided.
        //---------------------------------------------------------------------
        nwsapUsername = args[0];
        nwsapPassword = args[1];
        userIdentity = getUserIdentity(args[2].trim(),
                                       args[3].trim().split(","),
                                       null);
      }
      else if (args.length == 5)
      {
        //---------------------------------------------------------------------
        // five arguments - the optional where clause was provided.
        //---------------------------------------------------------------------
        nwsapUsername = args[0];
        nwsapPassword = args[1];
        userIdentity = getUserIdentity(args[2].trim(),
                                       args[3].trim().split(","),
                                       args[4].trim());
      }
      else
      {
        //---------------------------------------------------------------------
        // Wrong number of arguments were specified.
        //---------------------------------------------------------------------
        throw new WrongParametersException("The wrong number of parameters " +
                                           "were provided");
      }
  
      System.out.println("The user identity for this search is: " +
                         userIdentity);

      //-----------------------------------------------------------------------
      // Connect to the Sh service and send in a Pull request.
      //-----------------------------------------------------------------------
      ShServiceStub shService = new ShServiceStub();

      utilities.authenticate(shService,
                             nwsapUsername,
                             nwsapPassword);

      ShPull shPullRequest = utilities.createPullRequest(
                                                        userIdentity, 
                                                        0,
                                                        META_GLOBAL_SEARCH_SI,
                                                        utilities.ORIGIN_HOST);
      
      ShPullResponse shPullResponse = shService.shPull(shPullRequest);
      TUserData userData = shPullResponse.getUserData();

      //-----------------------------------------------------------------------
      // Check whether the request succeeded and display the value of each of
      // the fields.
      //-----------------------------------------------------------------------
      utilities.checkResultCode(shPullResponse.getResultCode().getTResultCode(),
                                shPullResponse.getExtendedResult(),
                                userData);

      utilities.displayFields(userData);
    }
    catch (MetaSwitchShInterfaceException e)
    {
      System.err.println(e.getMessage());
      System.err.println(USAGE);
    }
    catch (Exception e)
    {
      System.err.println("Unexpected error \"" + e + "\" in retrieving data");
      e.printStackTrace(System.err);
      System.err.println(USAGE);
    }
  }

  /**
   * Get the user identity for the Meta_Global_Search ShPull from the command
   * line arguments.
   *
   * When performing an ShPull operation for the public-exposed
   * Meta_Global_Search service indication, the UserIdentity field defines the
   * search to be performed as follows:
   *
   * <UserIdentity>
   *     ?searchObjectType=[URL-encoded objectType]
   *     &searchOutputFields=[URL-encoded list of output fields]
   *     &searchWhere=[URL-encoded filter query]
   * </UserIdentity>
   *
   * The search query to perform is defined as follows:
   * -   The first character must be a "?".
   * -   The object type to be searched is specified as the searchObjectType
   *     parameter. The value of this parameter should be URL encoded and must
   *     match a table name in the MetaView Shadow Configuration Database.
   *     This parameter is mandatory.
   * -   The fields to be retrieved are specified as the searchOutputFields
   *     parameter, separated by the "+" character. Each field name must be
   *     URL encoded and must match a column name in the table specified by the
   *     objectType parameter, in the MetaView Shadow Configuration Database.
   *     The whole list of fields (including "+" separators) should then be
   *     URL-encoded as well. This parameter is mandatory.
   * -   A filter query is specified as the searchWhere parameter.
   *     The value of this parameter should be URL-encoded.  When decoded, this
   *     value is used as the WHERE clause in a single SQL statement, and
   *     therefore must conform to standard SQL syntax.  This parameter is
   *     optional.
   *
   * @param objectType   - the object type for the search query.
   * @param outputFields - the fields to be outputted in the result of the
   *                       search query.
   * @param where        - the SQL where clause for the search query.
   *
   * @return       User identity string as defined in documentation, including
   *               URL encoding.
   *
   * @throws UnsupportedEncodingException
   * @throws WrongParametersException
   */
  private static String getUserIdentity(String objectType,
                                        String[] outputFields,
                                        String where)
  throws UnsupportedEncodingException
  {    
    String userIdentity = "?";

    String outputFieldString = outputFields[0];

    for (int ii = 1; ii < outputFields.length; ii++)
    {
      //-----------------------------------------------------------------------
      // Each individual output field is URL encoded and then placed in a '+'
      // separated string.
      //-----------------------------------------------------------------------
      outputFieldString += "+" +
      URLEncoder.encode(outputFields[ii], "UTF-8");
    }

    userIdentity += "searchObjectType=" +
    URLEncoder.encode(objectType, "UTF-8");
    userIdentity += "&searchOutputFields=" +
    URLEncoder.encode(outputFieldString, "UTF-8");

    if (where != null)
    {
      //-----------------------------------------------------------------------
      // The where clause is an optional parameter so only add this to the
      // query if there was a third parameter representing it.
      //-----------------------------------------------------------------------
      userIdentity += "&searchWhere=" + URLEncoder.encode(where, "UTF-8");
    }

    return userIdentity;
  }
}