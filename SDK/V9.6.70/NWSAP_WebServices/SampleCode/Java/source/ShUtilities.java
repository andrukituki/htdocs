//-----------------------------------------------------------------------------
// ShUtilities
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
//
// This file provides utilities common to all SOAP example applications.
//-----------------------------------------------------------------------------
import org.apache.axis2.transport.http.impl.httpclient4.HttpTransportPropertiesImpl;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.axis2.client.Stub;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import com.MetaSwitch.SRB.SOAP.ShService;
import com.MetaSwitch.SRB.SOAP.ShServiceStub;
import com.MetaSwitch.SRB.SOAP.TExtendedResult;
import com.MetaSwitch.SRB.SOAP.TSubResult;
import com.MetaSwitch.SRB.SOAP.TResultSystem;
import com.MetaSwitch.SRB.SOAP.TUserData;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

public abstract class ShUtilities
{
  public final static String ORIGIN_HOST = "?clientVersion=9.6.70";
  public final static String ORIGIN_HOST_V9_3 = "?clientVersion=9.3";
  public final static String IGNORE_SEQUENCE_NUMBER =
                                                  "&ignoreSequenceNumber=true";
  public final static String USE_FULL_BACK_COMP =
                                         "&useFullBackwardsCompatibility=true";
  public final static String NO_USE_FULL_BACK_COMP =
                                        "&useFullBackwardsCompatibility=false";
  public final static String SYSTEM_NAME_ON_ERROR =
                                               "&returnSystemNameOnError=true";
  public final static String NO_SYSTEM_NAME_ON_ERROR =
                                              "&returnSystemNameOnError=false";

  /**
   * Authenticate with the service stub using basic authentication
   *
   * @param shService      The service stub to authenticate
   * @param username       The NWSAP username
   * @param password       The password of the supplied NWSAP user
   */
  public void authenticate(ShServiceStub shService,
                           String username,
                           String password)
    throws AuthenticationFailedException
  {
    //-------------------------------------------------------------------------
    // Add authorization headers to the HTTP request.
    // We are using basic authentication, for which the authorization header
    // contains base64 encoded credentials.
    //-------------------------------------------------------------------------
    String originalInput = username + ":" + password;
    String encodedString = Base64.getEncoder()
                                 .encodeToString(originalInput.getBytes());
    String basicAuthHeaderValue = new String("Basic " + encodedString);
    Map<String, String> headers = (Map<String, String>) shService._getServiceClient()
                                                                 .getOptions()
                                                                 .getProperty(HTTPConstants.HTTP_HEADERS);
    if (headers == null)
    {
      headers = new HashMap<String, String>();
    }

    //[SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine", Justification="Example file; the interface uses HTTP basic auth; and there's no hard-coded credential.")]
    headers.put("Authorization", basicAuthHeaderValue);
    shService._getServiceClient()
             .getOptions()
             .setProperty(HTTPConstants.HTTP_HEADERS,
                          headers);
  }

  /**
   * Check the result information returned by an Sh operation, and throw an
   * exception including result details if it does not indicate success.
   *
   * @param resultCode
   *                    The result code returned by the operation.
   * @param extendedResult
   *                    The extended result information returned by the
   *                    operation.
   * @param userData    The user data that was included in the operation.
   *
   * @exception RequestFailedException
   */
  public void checkResultCode(int resultCode,
                              TExtendedResult extendedResult,
                              TUserData userData)
    throws RequestFailedException
  {
    checkResultCode(resultCode, extendedResult, userData, true);
  }

  /**
   * Check the result information returned by an Sh operation, and maybe throw
   * an exception including result details if it does not indicate success.
   *
   * @param resultCode
   *          The result code returned by the operation.
   * @param extendedResult
   *          The extended result information returned by the operation.
   * @param userData
   *          The user data that was included in the operation.
   * @param throwExceptionOnError
   *          Whether to throw an exceptionon error or just trace out.
   * @exception RequestFailedException
   */
  public void checkResultCode(int resultCode,
                              TExtendedResult extendedResult,
                              TUserData userData,
                              boolean throwExceptionOnError)
    throws RequestFailedException
  {
    if (resultCode != 2001)
    {
      //-----------------------------------------------------------------------
      // The request was unsuccessful, so return error information.
      //-----------------------------------------------------------------------
      StringBuilder error = new StringBuilder();

      error.append("\n\nThe Sh operation was unsuccessful.\n");
      error.append("Result code: ");
      error.append(resultCode);
      error.append("\nExtended result code: ");
      error.append(extendedResult.getExtendedResultCode());
      error.append("\n\"" + extendedResult.getExtendedResultDetail() + "\"\n");

      System.out.println(error.toString());
    }
  }

  /**
   * Get the name of the item identified by the SubResultSource in a particular
   * SubResult.
   *
   * @returns           The name of the source item that caused a problem.
   *
   * @param subResult   The result containing the source string.
   * @param userData    The user data that was sent in.
   */
  public String getSourceItem(TSubResult subResult, TUserData userData)
  {
    //-------------------------------------------------------------------------
    // This default method simply manipulates the SubResultSource as a string.
    // Typed applications would do this, but untyped applications have the
    // option of applying the SubResultSource directly as an XPath string: see
    // ShUntypedUtilities, where this is overloaded, for an example.
    //-------------------------------------------------------------------------
    String finalItem = "<none>";
    String source = subResult.getSubResultSource();

    if ((source != null) && (source.length() > 0))
    {
      //-----------------------------------------------------------------------
      // Each part of the source has a namespace prefix, e.g. "u:".  Get the
      // last part of the source, without the namespace prefix.
      //-----------------------------------------------------------------------
      finalItem = source.substring(source.lastIndexOf(':') + 1);
    }

    return finalItem;
  }

  /**
   * Creates a UserIdentity string from a set of identifies by URL-encoding the
   * individual identifiers and then concatenating them with / separators.
   *
   * @returns           A string suitable for use as a UserIdentity.
   *
   * @param identifiers
   *                    The identifiers of the requested object.
   */
  public String getUserIdentity(String[] identifiers)
  {
    StringBuilder userIdentityBuilder = new StringBuilder();

    //-------------------------------------------------------------------------
    // URL-encode each identifier, and add it to the UserIdentity.
    //-------------------------------------------------------------------------
    for (String identifier : identifiers)
    {
      String encodedIdent = null;
      try
      {
        encodedIdent = java.net.URLEncoder.encode(identifier, "UTF-8");
      }
      catch (java.io.UnsupportedEncodingException e)
      {
        //---------------------------------------------------------------------
        // This should never happen, but there's nothing we can do about it.
        //---------------------------------------------------------------------
      }

      userIdentityBuilder.append(encodedIdent);
      userIdentityBuilder.append("/");
    }

    //-------------------------------------------------------------------------
    // Remove the final separator.
    //-------------------------------------------------------------------------
    String userIdentity =
            userIdentityBuilder.substring(0, userIdentityBuilder.length() - 1);

    return userIdentity;
  }

  /**
   * Finds the current sequence number within the user data and increments it,
   * wrapping if necessary, so that the server accepts the change.
   *
   * @param userData    IN/OUT The user data whose sequence number to update.
   */
  public abstract void incrementSequenceNumber(TUserData userData);
}

//-----------------------------------------------------------------------------
// Parameter
//
// A class for dealing with command line parameters that may include spaces.
//-----------------------------------------------------------------------------
class Parameter
{
  private String value;
  private boolean touched = false;

  Parameter(String defaultValue)
  {
    value = defaultValue;
  }

  public String toString()
  {
    return value;
  }

  void append(String word)
  {
    if (touched)
    {
      value += " " + word;
    }
    else
    {
      value = word;
      touched = true;
    }
  }
}

//-----------------------------------------------------------------------------
// MetaSwitchShInterfaceException
//
// An umbrella exception for problems hit by SOAP example applications.
//-----------------------------------------------------------------------------
class MetaSwitchShInterfaceException extends Exception
{
  MetaSwitchShInterfaceException(String s)
  {
    super(s);
  }
}

//-----------------------------------------------------------------------------
// AuthenticationFailedException
//
// Indicates that authentication was not successful.
//-----------------------------------------------------------------------------
class AuthenticationFailedException extends MetaSwitchShInterfaceException
{
  AuthenticationFailedException(String s)
  {
    super(s);
  }
}

//-----------------------------------------------------------------------------
// RequestFailedException
//
// Indicates that a SOAP request was not successful.
//-----------------------------------------------------------------------------
class RequestFailedException extends MetaSwitchShInterfaceException
{
  RequestFailedException(String s)
  {
    super(s);
  }
}

//-----------------------------------------------------------------------------
// WrongParametersException
//
// Indicates that invalid parameters were passed on the command line to one of
// the example applications.
//-----------------------------------------------------------------------------
class WrongParametersException extends MetaSwitchShInterfaceException
{
  WrongParametersException(String s)
  {
    super(s);
  }
}
