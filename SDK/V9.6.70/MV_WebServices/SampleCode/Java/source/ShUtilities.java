//-----------------------------------------------------------------------------
// ShUtilities
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material 
//
// This file provides utilities common to all SOAP example applications.
//-----------------------------------------------------------------------------

import com.MetaSwitch.EMS.SOAP.TExtendedResult;
import com.MetaSwitch.EMS.SOAP.TSubResult;
import com.MetaSwitch.EMS.SOAP.TUserData;

import com.MetaSwitch.EMS.SOAP.TExtendedSubResults;

public abstract class ShUtilities
{
  public final static String ORIGIN_HOST = "?clientVersion=9.6.70";
  public final static String IGNORE_SEQUENCE_NUMBER =
                                                  "&ignoreSequenceNumber=true";

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
    if (resultCode != 2001)
    {
      //-----------------------------------------------------------------------
      // The request was unsuccessful, so return error information.
      //-----------------------------------------------------------------------
      StringBuilder error = new StringBuilder();

      error.append("The Sh operation was unsuccessful.\n");
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
   * Handle a MetaSwitch exception
   * 
   * @param e                   The exception to handle
   */
  public void handleMetaSwitchException(Exception e, String usage)
  {
    System.err.println(e.getMessage());
    System.err.println(usage);
  }
  
  /**
   * Handle an unexpected exception
   * 
   * @param e                   The exception to handle
   * @param printFullStack      whether to print the full exception stack trace
   *                            to stderr.
   */
  public void handleUnexpectedException(Exception e,
                                        String usage,
                                        boolean printFullStack)
  {
    System.err.println("Unexpected error \"" + e + "\" in retrieving data");
    
    if (printFullStack)
    {
      e.printStackTrace(System.err);
    }
    
    System.err.println(usage);
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
