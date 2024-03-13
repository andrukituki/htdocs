//-----------------------------------------------------------------------------
// ShTypedUtilities
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material 
//
// This file provides utilities to those example applications that use the
// "typed" WSDL file and therefore deal with user data as Java classes.
//-----------------------------------------------------------------------------
import com.MetaSwitch.SRB.SOAP.ShService;
import com.MetaSwitch.SRB.SOAP.ShServiceStub;
import com.MetaSwitch.SRB.SOAP.TTransparentData;
import com.MetaSwitch.SRB.SOAP.TUserData;
import com.MetaSwitch.SRB.SOAP.ShPull;
import com.MetaSwitch.SRB.SOAP.ShUpdate;
import com.MetaSwitch.SRB.SOAP.TExtendedResult;
import com.MetaSwitch.SRB.SOAP.TResultSystem;
import com.MetaSwitch.SRB.SOAP.TSubResult;

public class ShTypedUtilities extends ShUtilities
{
  /**
   * Finds the current sequence number within the user data and increments it,
   * wrapping if necessary, so that the server accepts the change.
   *
   * @param userData    IN/OUT The user data whose sequence number to update.
   */
  public void incrementSequenceNumber(TUserData userData)
  {
    TTransparentData repositoryData = userData.getShData().getRepositoryData().get(0);

    int newSequenceNumber = repositoryData.getSequenceNumber() + 1;

    if (newSequenceNumber > 65535)
    {
      //-----------------------------------------------------------------------
      // The sequence number needs to wrap to 1, not 0: 0 is used to create
      // new objects.
      //-----------------------------------------------------------------------
      newSequenceNumber = 1;
    }

    repositoryData.setSequenceNumber(newSequenceNumber);
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
    super.checkResultCode(resultCode,
                          extendedResult,
                          userData,
                          throwExceptionOnError);
    
    if (resultCode != 2001)
    {
      StringBuilder error = new StringBuilder();
      
      //-----------------------------------------------------------------------
      // Include each of the extended result systems, if any.
      //-----------------------------------------------------------------------
      if ((extendedResult.getExtendedResultSystem() != null) &&
          (extendedResult.getExtendedResultSystem().size() > 0))
      {
        error.append("\nExtended result system: ");
        for (TResultSystem resultSystem : extendedResult.getExtendedResultSystem())
        {
          error.append("\n  Type: " + resultSystem.getType());
          error.append(" " + resultSystem.toString());
        }
        error.append("\n");
      }

      if ((extendedResult.getExtendedSubResults() != null) &&
          (extendedResult.getExtendedSubResults().getSubResult() != null))
      {
        //-----------------------------------------------------------------------
        // Include each of the sub-results.
        //-----------------------------------------------------------------------
        for (TSubResult subResult : extendedResult.getExtendedSubResults().getSubResult())
        {
          error.append("\nSub-result code: ");
          error.append(subResult.getSubResultCode());
          error.append("\n\"" + subResult.getSubResultDetail() + "\"\n");
          error.append("Source: " + getSourceItem(subResult, userData) + "\n");
  
          //---------------------------------------------------------------------
          // Include each of the sub result systems, if any.
          //---------------------------------------------------------------------
          if ((subResult.getSubResultSystem() != null) &&
              (subResult.getSubResultSystem().size() > 0))
          {
            error.append("\nSub result system: ");
  
            for (TResultSystem resultSystem : subResult.getSubResultSystem())
            {
              error.append("\n  Type: " + resultSystem.getType());
              error.append(" " + resultSystem.toString());
            }
            error.append("\n");
          }
        }
      }
      
      if (throwExceptionOnError)
      {
        throw new RequestFailedException(error.toString());
      }
      else
      {
        System.err.println(error.toString());
      }
    }
  }

  /**
   * Create an ShPull request with the specified values. 
   * 
   * @param userIdString A string representing the userIdentity
   * @param dataRefValue the data reference
   * @param serviceIndication The service indication
   * @param originHost The origin host
   * @return an ShPull Request with the specified fields. 
   */
  public ShPull createPullRequest(String userIdString,
                                  int dataRefValue,
                                  String serviceIndication,
                                  String originHost)
  {
    ShPull shPullRequest = new ShPull();

    shPullRequest.setServiceIndication(serviceIndication);
    shPullRequest.setUserIdentity(userIdString);
    shPullRequest.setDataReference(dataRefValue);
    shPullRequest.setOriginHost(originHost);

    return shPullRequest;
  }

  /**
   * Create an ShUpdate request with the specified values
   * 
   * @param userIdString A string representing the userIdentity
   * @param dataRefValue the data reference
   * @param userData The user data
   * @param originHost The origin host
   * @return an ShUpdate request with the specified fields.
   */
  public ShUpdate createUpdateRequest(String userIdString,
                                      int dataRefValue,
                                      TUserData userData,
                                      String originHost)
  {
    ShUpdate shUpdateRequest = new ShUpdate();

    shUpdateRequest.setUserIdentity(userIdString);
    shUpdateRequest.setDataReference(dataRefValue);
    shUpdateRequest.setOriginHost(originHost);
    shUpdateRequest.setUserData(userData);

    return shUpdateRequest;
  }
}
