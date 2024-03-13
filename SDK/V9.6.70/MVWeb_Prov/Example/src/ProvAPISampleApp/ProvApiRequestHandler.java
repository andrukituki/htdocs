// ----------------------------------------------------------------------------
// ProvApiRequestHandler.java
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
// ----------------------------------------------------------------------------
package ProvAPISampleApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minidev.json.JSONArray;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * Class to handle Provisioning Requests.
 */
public class ProvApiRequestHandler
{
  /**
   * Class logger.
   */
  private static Logger logger = LogManager.getLogger(ProvApiRequestHandler.class);

  /**
   * Member variables.
   */
  private String mProvServer;
  private String mProvUser;
  private String mProvPassword;
  private HttpClient mClient;
  
  /**
   * Constructor.
   */
  public ProvApiRequestHandler(String provServer, String provUser, String provPassword)
  {
    mProvServer = provServer;
    mProvUser = provUser;
    mProvPassword = provPassword;
    mClient = new HttpClient();
  }
  
  /**
   * Constants defining JSON keys.
   */
  private static final String JSON_ACTION = "action";
  private static final String JSON_TEMPLATE = "template";
  private static final String JSON_VALUES = "values";
  
  /**
   * Operation types.
   */
  public enum Operation
  {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete");
    
    private String mDisplayText;
    
    private Operation(String displayText)
    {
      mDisplayText = displayText;    
    }
    
    public String toString()
    {
      return mDisplayText;
    }
  }
  
  /**
   * Create and send a request to the provisioning server.
   * 
   * @param dn            The directory number to provision
   * @param opType        The type of operation to perform 
   * @param templateList  A list of templates
   * @param keyValueMap   A map of key value pairs 
   */
  public void processRequest(String dn,
                             Operation opType,
                             List<String> templateList,                             
                             Map<String, Object> keyValueMap) 
    throws Exception
  {
    //--------------------------------------------------------------------------
    // Get the URL and JSON body for the request.
    //-------------------------------------------------------------------------- 
    String requestUrl = "http://" + mProvServer + ":8087/api/subscriber/" + dn;  
    String requestBody = getRequestBody(opType, templateList, keyValueMap);
    
    //--------------------------------------------------------------------------
    // Send the PUT request.
    //--------------------------------------------------------------------------
    sendPUTRequest(requestUrl, requestBody);
  }
  
  /**
   * Send a HTTP PUT request. 
   * 
   * @param url The URL to send the request to
   * @param dataToSend The data to send in the PUT request
   */
  private void sendPUTRequest(String url, String dataToSend)
    throws Exception
  {   
    //--------------------------------------------------------------------------
    // Create an HTTP client and setup a PUT method.
    //--------------------------------------------------------------------------
    PutMethod method = new PutMethod(url);
    
    try
    {
      //------------------------------------------------------------------------
      // Set the data type.
      //------------------------------------------------------------------------
      method.setRequestEntity(new StringRequestEntity(dataToSend, 
                                                      "application/json",
                                                      "UTF-8"));
      
      //------------------------------------------------------------------------
      // Add the authorization credentials.
      //------------------------------------------------------------------------
      String authCredentials = mProvUser + ":" + mProvPassword;
      String authEncoded = new String(Base64.encodeBase64(authCredentials.getBytes()));
      method.setRequestHeader("Authorization", "Basic " + authEncoded);
  
      //------------------------------------------------------------------------
      // Send the PUT request.
      //------------------------------------------------------------------------
      int statusCode = mClient.executeMethod(method);
  
      if (statusCode != 200)
      {
        //----------------------------------------------------------------------
        // Provisioning failed.
        //----------------------------------------------------------------------
        logger.debug("Provisioning failed");
        logger.debug("Request Data:\n" + dataToSend);
        logger.debug("Status Code: " + statusCode);
        logger.debug("Status Text: " + HttpStatus.getStatusText(statusCode));
        logger.debug(method.getResponseBodyAsString());    
        
        throw new Exception(method.getResponseBodyAsString());
      }
    }
    finally
    {
      //------------------------------------------------------------------------
      // Make sure we close the HTTP connection.
      //------------------------------------------------------------------------
      method.releaseConnection();
    }
  } 
  
  /**
   * Internal methods to construct the request body.
   */  
  
  /**
   * Creates a JSON body of the form:
   * 
   * {
   *  "action": "create",
   *  "template": ["Gold", "Silver"],
   *  "values":
   *  {
   *   "Name": "John Smith",
   *   "PIN": "1234",
   *   "New SIP Password": "sippass",
   *   "EAS Password": "easpass"
   *  }
   * }
   *
   * @return The constructed request body
   */
  private String getRequestBody(Operation opType,
                                List<String> templateList,                               
                                Map<String, Object> keyValueMap) 
    throws Exception
  {
    StringBuilder jsonBody = new StringBuilder();  
    int indent = 0;
       
    //--------------------------------------------------------------------------
    // Build the JSON body...
    //--------------------------------------------------------------------------
    indent = appendOpeningBrace(jsonBody, indent);
    
    //--------------------------------------------------------------------------
    // Add the action
    //--------------------------------------------------------------------------
    appendKeyValuePair(JSON_ACTION, opType.toString(), jsonBody, indent, true);
    jsonBody.append(",\r\n");
    
    //--------------------------------------------------------------------------
    // Add the list of templates
    //--------------------------------------------------------------------------
    appendKeyListPair(JSON_TEMPLATE, templateList, jsonBody, indent);
    
    //--------------------------------------------------------------------------
    // Add "values" key
    //--------------------------------------------------------------------------
    jsonBody.append(",\r\n");
    appendIndent(jsonBody, indent);
    jsonBody.append('\"' + JSON_VALUES + "\":\r\n");
    indent = appendOpeningBrace(jsonBody, indent);
   
    //--------------------------------------------------------------------------
    // Append the key value pairs.
    //--------------------------------------------------------------------------
    boolean isFirst = true;
    
    for (Entry<String, Object> entry : keyValueMap.entrySet())
    {
       appendKeyValuePair(entry.getKey(),
                          entry.getValue(),
                          jsonBody,               
                          indent,
                          isFirst);      
       isFirst= false;
    }

    jsonBody.append("\r\n");
    indent = appendClosingBrace(jsonBody, indent);
    indent = appendClosingBrace(jsonBody, indent);   

    return jsonBody.toString();
  }
   
  /**
   * Append an opening brace to the passed JSON body and increase the indent
   *
   * @param jsonBody The JSON body to append the opening brace to
   * @param indent The current indent
   * @return The new indent
   */  
  private int appendOpeningBrace(StringBuilder jsonBody,
                                 int indent)
  {
    appendIndent(jsonBody, indent);
    jsonBody.append("{\r\n");
    ++indent;
    
    return indent;
  }   
  
  /**
   * Decrease the indent and append a closing brace to the passed JSON body
   *
   * @param jsonBody The JSON body to append the closing brace to
   * @param indent The current indent
   * @return The new indent
   */  
  private int appendClosingBrace(StringBuilder jsonBody,
                                 int indent)
  {
    --indent;
    appendIndent(jsonBody, indent);
    jsonBody.append("}\r\n");
    
    return indent;
  }     
  
  /**
   * Append an indent to the passed JSON body
   *
   * @param jsonBody The JSON body to append the indent to
   * @param indent The indent to append
   */  
  private void appendIndent(StringBuilder jsonBody,
                            int indent)
  {
    while (indent > 0)
    {
      jsonBody.append(' ');
      --indent;
    }
  }        
    
  /**
   * Append the passed key / value pair to the passed JSON body
   *
   * @param key The key to append
   * @param value The value to append 
   * @param jsonBody The JSON body to append the key to
   * @param indent The indent to append
   * @param isFirst Whether this is the first key / value pair
   */  
  private void appendKeyValuePair(String key, 
                                  Object value,
                                  StringBuilder jsonBody,
                                  int indent,
                                  boolean isFirst)
  {
    if (!isFirst)
    {
      jsonBody.append(",\r\n");
    }
    
    appendIndent(jsonBody, indent);  
    
    if (value instanceof String)
    {
      jsonBody.append('\"' + key + "\": \"" + value + '\"');
    }
    else if (value instanceof Boolean)
    {
      jsonBody.append('\"' + key + "\": " + Boolean.toString((Boolean)value));
    }
    else if (value instanceof Integer)
    {
      jsonBody.append('\"' + key + "\": " + Integer.toString((Integer)value));
    }   
  }
    
  /**
   * Append the passed key / list pair to the passed JSON body
   *
   * @param key The key to append
   * @param list The list of values to append 
   * @param jsonBody The JSON body to append the key to
   * @param indent The indent to append
   */  
  private void appendKeyListPair(String key, 
                                 List<String> list,
                                 StringBuilder jsonBody,
                                 int indent)
  {
    appendIndent(jsonBody, indent);
    jsonBody.append('\"' + key + "\": [");
    
    boolean isFirst = true;    
    
    for (String element : list)
    {
      if (!isFirst)
      {
        jsonBody.append(',');
      }
      
      jsonBody.append('\"' + element + '\"');
      isFirst = false;
    }
    
    jsonBody.append(']');
  }

}
