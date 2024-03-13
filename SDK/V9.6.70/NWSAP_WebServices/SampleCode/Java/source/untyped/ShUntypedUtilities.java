//-----------------------------------------------------------------------------
// ShUntypedUtilities
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material 
//
// This file provides utilities to those example applications that use the
// "untyped" WSDL file and therefore deal with user data as XML.
//-----------------------------------------------------------------------------

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.axis2.util.XMLUtils;
import org.apache.axiom.om.OMElement;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import com.MetaSwitch.SRB.SOAP.TUserData;
import com.MetaSwitch.SRB.SOAP.TSubResult;
import com.MetaSwitch.SRB.SOAP.TDataReference;
import com.MetaSwitch.SRB.SOAP.TUserIdentity;
import com.MetaSwitch.SRB.SOAP.TSubResult;
import com.MetaSwitch.SRB.SOAP.ShPull;
import com.MetaSwitch.SRB.SOAP.ShUpdate;
import com.MetaSwitch.SRB.SOAP.TExtendedResult;
import com.MetaSwitch.SRB.SOAP.TResultSystem;

public class ShUntypedUtilities extends ShUtilities
{
  //---------------------------------------------------------------------------
  // Define QNames identifying elements within a user data XML document.
  //---------------------------------------------------------------------------
  static final String USER_DATA_NAMESPACE_URI =
                              "http://www.metaswitch.com/srb/soap/sh/userdata";
  static final String SERVICE_DATA_NAMESPACE_URI =
                           "http://www.metaswitch.com/ems/soap/sh/servicedata";

  static final QName REPOSITORY_DATA = new QName(USER_DATA_NAMESPACE_URI,
                                                 "RepositoryData");
  static final QName SEQUENCE_NUMBER = new QName(USER_DATA_NAMESPACE_URI,
                                                 "SequenceNumber");
  static final QName SERVICE_DATA = new QName(USER_DATA_NAMESPACE_URI,
                                              "ServiceData");
  static final QName METASWITCH_DATA = new QName(SERVICE_DATA_NAMESPACE_URI,
                                                 "MetaSwitchData");
  static final QName VALUE = new QName(SERVICE_DATA_NAMESPACE_URI,
                                       "Value");
  static final QName SUBSCRIBED = new QName(SERVICE_DATA_NAMESPACE_URI,
                                            "Subscribed");
  static final QName USE_DEFAULT = new QName(SERVICE_DATA_NAMESPACE_URI,
                                             "UseDefault");

  //---------------------------------------------------------------------------
  // Set up an object to handle XPath queries.
  //---------------------------------------------------------------------------
  public final XPath xPath;

  public ShUntypedUtilities()
  {
    xPath = XPathFactory.newInstance().newXPath();
    NamespaceContext namespaceContext = new MetaSwitchNamespaceContext();
    xPath.setNamespaceContext(namespaceContext);
  }

  /**
   * Print to the console a representation of each field in user data.
   *
   * @param userData    A set of user data such as that returned on an Sh-Pull
   *                    response.
   */
  public void displayFields(TUserData userData)
  {
    StringBuilder displayFieldsBuilder = new StringBuilder();

    //-------------------------------------------------------------------------
    // Go down to the field group elements contained within the user data.
    //-------------------------------------------------------------------------
    OMElement[] fieldGroupElements = getFieldGroupElements(userData);

    //-------------------------------------------------------------------------
    // Go through each field group and display its contents.
    //-------------------------------------------------------------------------
    for (OMElement eachFieldGroup : fieldGroupElements)
    {
      displayFieldsBuilder.append("\nService indication " +
                                  eachFieldGroup.getQName().getLocalPart() + "\n\n");

      //-----------------------------------------------------------------------
      // Go through each field under this element and display its name and
      // value.
      //-----------------------------------------------------------------------
      Iterator<OMElement> iterChildren = eachFieldGroup.getChildElements();
      while (iterChildren.hasNext())
      {
        OMElement eachField = iterChildren.next();
        displayFieldsBuilder.append(eachField.getQName().getLocalPart() + ": " 
                                  + getValue(eachField) + "\n");
      }
    }

  System.out.println(displayFieldsBuilder.toString());
  }


  /**
   * Extract the field group elements from user data.
   *
   * @returns           The field group element contained in the xml.
   *
   * @param userData    A set of user data such as that returned on an Sh-Pull
   *                    response.
   */
  public OMElement[] getFieldGroupElements(TUserData userData)
  {
    //-------------------------------------------------------------------------
    // Fetch all the RepositoryData elements.
    //-------------------------------------------------------------------------
    Iterator<OMElement> repositoryDatas = 
                                 userData.getExtraElement()
                                         .getChildrenWithName(REPOSITORY_DATA);

    List<OMElement> fieldGroupList = new ArrayList<OMElement>();

    while (repositoryDatas.hasNext())
    {
      OMElement eachRepData = repositoryDatas.next();

      //-----------------------------------------------------------------------
      // Get all the field group elements and add them to the list.
      //-----------------------------------------------------------------------
      OMElement fieldGroupElement = 
                             eachRepData.getFirstChildWithName(SERVICE_DATA)
                                        .getFirstChildWithName(METASWITCH_DATA)
                                        .getChildElements()
                                        .next();
      fieldGroupList.add(fieldGroupElement);
    }

    return fieldGroupList.toArray(new OMElement[]{});
  }

  /**
   * Extract the first field group element from user data.
   *
   * @returns           The field group element.
   *
   * @param userData    A set of user data such as that returned on an Sh-Pull
   *                    response.
   */
  public OMElement getFieldGroupElement(TUserData userData)
  {
    //-------------------------------------------------------------------------
    // Get all the field group elements.
    //-------------------------------------------------------------------------
    OMElement[] allFieldGroupElements = getFieldGroupElements(userData);

    //-------------------------------------------------------------------------
    // Return the first of them.
    //-------------------------------------------------------------------------
    return allFieldGroupElements[0];
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
          (extendedResult.getExtendedResultSystem().length > 0))
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
              (subResult.getSubResultSystem().length > 0))
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
   * Create a string representation of the value an XML element.
   *
   * @returns           The value as a string. Where the element has child
   *                    elements, these are returned as name-value pairs.
   *
   * @param element     An XML element whose value to print.
   */
  public String getValue(OMElement element)
  {
    String getValue;

    String value = element.getText();
    if (value != null && !value.equals(""))
    {
      //-----------------------------------------------------------------------
      // This element has a text value - just return that.
      //-----------------------------------------------------------------------
      getValue = value;
    }
    else
    {
      //-----------------------------------------------------------------------
      // This element has no text value, so we'll display child elements.
      //-----------------------------------------------------------------------
      StringBuilder getValueBuilder = new StringBuilder();

      //-----------------------------------------------------------------------
      // If there is an element called "Value", display that first, and exclude
      // the word "Value".  This improves the presentation of switchable
      // default fields.
      //-----------------------------------------------------------------------
      OMElement childValue = element.getFirstChildWithName(VALUE);
      if (childValue != null)
      {
        getValueBuilder.append(getValue(childValue) + " ");
      }

      //-----------------------------------------------------------------------
      // Go through each child element.  Display a list of their names and
      // values.
      //-----------------------------------------------------------------------
      Iterator iterChildren = element.getChildElements();
      if (iterChildren.hasNext())
      {
        getValueBuilder.append("{");

        while (iterChildren.hasNext())
        {
          OMElement currentChild = (OMElement)iterChildren.next();
          
          if (currentChild != childValue)
          {
            getValueBuilder.append(currentChild.getQName().getLocalPart() + "=" +
                                   getValue(currentChild) + ", ");
          }
        }

        //---------------------------------------------------------------------
        // Knock off the last comma and space.
        //---------------------------------------------------------------------
        getValueBuilder.setLength(getValueBuilder.length() - 2);
        getValueBuilder.append("}");
      }

      getValue = getValueBuilder.toString();
    }

    return getValue;
  }

  /**
   * Finds the current sequence number within the user data and increments it,
   * wrapping if necessary, so that the server accepts the change.
   *
   * @param userData    IN/OUT The user data whose sequence number to update.
   */
  public void incrementSequenceNumber(TUserData userData)
  {
    //-------------------------------------------------------------------------
    // Dig down to the "SequenceNumber" element and extract its current value.
    //-------------------------------------------------------------------------
    OMElement shData = userData.getExtraElement();
    OMElement repositoryData = shData.getFirstChildWithName(ShUntypedUtilities.REPOSITORY_DATA);
    OMElement sequenceNumber = repositoryData.getFirstChildWithName(ShUntypedUtilities.SEQUENCE_NUMBER);
    int value = Integer.parseInt(sequenceNumber.getText());

    //-------------------------------------------------------------------------
    // Increment the value, wrapping back to 1 if it goes past the limit of
    // 65535.
    //-------------------------------------------------------------------------
    int newValue = value + 1;

    if (newValue > 65535)
    {
      newValue = 1;
    }
    sequenceNumber.setText(String.valueOf(newValue));
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

    TUserIdentity userIdentity = new TUserIdentity();
    userIdentity.setTUserIdentity(userIdString);
    shPullRequest.setUserIdentity(userIdentity); 

    TDataReference dataRef = new TDataReference();
    dataRef.setTDataReference(dataRefValue);
    shPullRequest.setDataReference(dataRef);

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

    TUserIdentity userIdentity = new TUserIdentity();
    userIdentity.setTUserIdentity(userIdString);
    shUpdateRequest.setUserIdentity(userIdentity); 
    
    TDataReference dataRef = new TDataReference();
    dataRef.setTDataReference(dataRefValue);
    shUpdateRequest.setDataReference(dataRef);

    shUpdateRequest.setOriginHost(originHost);

    shUpdateRequest.setUserData(userData);

    return shUpdateRequest;
  }

  //---------------------------------------------------------------------------
  // This inner class is used by XPath to resolve namespace prefixes to the
  // full namespace URIs.
  //---------------------------------------------------------------------------
  class MetaSwitchNamespaceContext implements NamespaceContext
  {
    /**
     * Return the namespace URI for the specified prefix.
     *
     * @returns           The user data namespace URI for prefix u, otherwise
     *                    the service data namespace URI.
     *
     * @param prefix      The prefix to check.
     */
    public String getNamespaceURI(String prefix)
    {
      if (prefix.equals("u"))
      {
        return USER_DATA_NAMESPACE_URI;
      }
      else
      {
        return SERVICE_DATA_NAMESPACE_URI;
      }
    }

    /**
     * Returns the prefix for the specified namespace URI.
     *
     * @returns           u for user data, s otherwise.
     *
     * @param namespace   The namespace to check.
     */
    public String getPrefix(String namespace)
    {
      if (namespace.equals(USER_DATA_NAMESPACE_URI))
      {
        return "u";
      }
      else
      {
        return "s";
      }
    }

    /**
     * We have to implement this method, but it isn't required for any of our
     * uses of XPath, so it does nothing.
     *
     * @returns           null.
     *
     * @param namespace   The namespace to check.
     */
    public Iterator getPrefixes(String namespace)
    {
      return null;
    }
  }
}
