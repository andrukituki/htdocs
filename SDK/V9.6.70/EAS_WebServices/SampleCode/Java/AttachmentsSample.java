//-----------------------------------------------------------------------------
// AttachmentsSample.java
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
//-----------------------------------------------------------------------------
package dcl.wsd.test.fv.sample;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.w3.www._2005._05.xmlmime.Base64Binary;
import org.w3.www._2005._05.xmlmime.ContentType_type0;

import com.metaswitch.www.sdp.soap.sh.ShPull;
import com.metaswitch.www.sdp.soap.sh.ShPullResponse;
import com.metaswitch.www.sdp.soap.sh.ShUpdate;
import com.metaswitch.www.sdp.soap.sh.ShUpdateResponse;
import com.metaswitch.www.sdp.soap.sh.servicedata.TBinaryData;
import com.metaswitch.www.sdp.soap.sh.servicedata.TFileReference;
import com.metaswitch.www.sdp.soap.sh.servicedata.TGreetingType;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMetaSphereData;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_Greetings_Data;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_Greetings_Data_Greeting;

/**
 * Code sample: Operations with attachments
 *
 * This sample client was built using the Axis2 (version 1.4.1) wsdl2java
 * utility, ADB databinding and the --unpack-classes option.
 *
 * It is not guaranteed to work with other data bindings, client framework
 * utilities or client stub code generated using other versions of Axis2.
 *
 * This client additionally requires:
 *
 *  - the JavaBeans Activation Framework (activation-*.jar);
 *    see http://java.sun.com/products/javabeans/jaf/downloads/index.html
 */
public class AttachmentsSample
{
  /**
   * This sample code uploads binary data (for use as a greeting) to the
   * server, using a file reference to supply the data.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiPhone the primary telephone number for this subscriber
   * @param xiGrtType the type of the greeting to apply the data to
   * @param xiFileName the full name and path of the file to use as the data
   *        source
   * @param xiIsFileAccessible is the file accessible by the server? If it is,
   *        the operation will work; if not, the operation will return an
   *        error in its response. The boolean is used to assert that the
   *        return codes we get back from the server are correct.
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse uploadFileUsingRef(String xiServer,
                                             String xiPhone,
                                             TGreetingType xiGrtType,
                                             String xiFileName,
                                             boolean xiIsFileAccessible)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Set up the data objects to pass into the update.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();

    TMsph_Subscriber_Greetings_Data_Greeting[] greetings =
      new TMsph_Subscriber_Greetings_Data_Greeting[1];

    //-------------------------------------------------------------------------
    // Read out the specified file.
    //-------------------------------------------------------------------------
    // Creating a javax.activation.FileDataSource from the input file.
    FileDataSource fileDataSource = new FileDataSource(new File(xiFileName));
    DataHandler dataHandler = new DataHandler(fileDataSource);

    // Determine the content type from the file
    ContentType_type0 contentType = new ContentType_type0();
    contentType.setContentType_type0(dataHandler.getContentType());

    //-------------------------------------------------------------------------
    // Set up a file reference and use it in a binary data object.
    //-------------------------------------------------------------------------
    // Set up the file reference
    TFileReference fileRef = new TFileReference();
    fileRef.setContentType(contentType);
    fileRef.setPath(xiFileName);

    // Add into binary data.
    TBinaryData binData = new TBinaryData();
    binData.setFileReference(fileRef);

    //-------------------------------------------------------------------------
    // Set up the greeting object with the supplied name and the binary
    // data extracted above.
    //-------------------------------------------------------------------------
    greetings[0] = new TMsph_Subscriber_Greetings_Data_Greeting();
    greetings[0].setName(xiGrtType);
    greetings[0].setGreetingFile(binData);

    //-------------------------------------------------------------------------
    // Set up the greetings to send to the service and add to the
    // MetaSphereData.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_Greetings_Data grtData =
      new TMsph_Subscriber_Greetings_Data();

    grtData.setAction("apply");
    grtData.setGreeting(greetings);

    metaData.setMsph_Subscriber_Greetings_Data(grtData);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_Greetings_Data");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    ShUpdateResponse response = UtilitiesSample.doShUpdate(xiServer, update);

    if (xiIsFileAccessible)
    {
      //-----------------------------------------------------------------------
      // Confirm that the result is successful:
      // - result code is 2001 (IMS_SUCCESS)
      // - extended result is 100 (SUCCESS)
      //-----------------------------------------------------------------------
      assert(response.getResultCode().getTResultCode() == 2001);
      assert(response.getExtendedResult().getExtendedResultCode() == 100);
    }
    else
    {
      //-----------------------------------------------------------------------
      // Confirm that the server was unable to access the file:
      //  - result code is 5012 (IMS_UNABLE_TO_COMPLY)
      //  - extended result is 604 (ERROR_INVALID_REFERENCE)
      //-----------------------------------------------------------------------
      assert(response.getResultCode().getTResultCode() == 5012);
      assert(response.getExtendedResult().getExtendedResultCode() == 604);
    }

    return response;
  }

  /**
   * This sample code uploads binary data (for use as a greeting) to the
   * server, using in-line binary data as the data source.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiPhone the primary telephone number for this subscriber
   * @param xiGrtType the type of the greeting to apply the data
   * @param xiFileName the full name and path of the file to use as the data
   *        source (this only needs to be accessible by the client)
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse uploadFileUsingMTOM(String xiServer,
                                              String xiPhone,
                                              TGreetingType xiGrtType,
                                              String xiFileName)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Set up the data objects to pass into the update.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();

    TMsph_Subscriber_Greetings_Data_Greeting[] greetings =
      new TMsph_Subscriber_Greetings_Data_Greeting[1];

    //-------------------------------------------------------------------------
    // Read out the specified file.
    //-------------------------------------------------------------------------
    // Creating a javax.activation.FileDataSource from the input file.
    FileDataSource fileDataSource =
      new FileDataSource(new File(xiFileName));
    DataHandler dataHandler = new DataHandler(fileDataSource);

    // Determine the content type from the file
    ContentType_type0 contentType = new ContentType_type0();
    contentType.setContentType_type0(dataHandler.getContentType());

    //-------------------------------------------------------------------------
    // Set up in-line data and use it in the binary data object.
    //-------------------------------------------------------------------------
    // Set up in-line data.
    Base64Binary base64bin = new Base64Binary();
    base64bin.setBase64Binary(dataHandler);
    base64bin.setContentType(contentType);

    // Add into binary data object.
    TBinaryData binData = new TBinaryData();
    binData.setData(base64bin);

    //-------------------------------------------------------------------------
    // Set up the greeting object with the supplied name and the binary
    // data extracted above.
    //-------------------------------------------------------------------------
    greetings[0] = new TMsph_Subscriber_Greetings_Data_Greeting();
    greetings[0].setName(xiGrtType);
    greetings[0].setGreetingFile(binData);

    //-------------------------------------------------------------------------
    // Set up the greetings to send to the service and add to the
    // MetaSphereData.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_Greetings_Data grtData =
      new TMsph_Subscriber_Greetings_Data();
    grtData.setAction("apply");
    grtData.setGreeting(greetings);

    metaData.setMsph_Subscriber_Greetings_Data(grtData);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_Greetings_Data");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    ShUpdateResponse response = UtilitiesSample.doShUpdate(xiServer, update);

    //-------------------------------------------------------------------------
    // Confirm that the result is successful:
    // - result code is 2001 (IMS_SUCCESS)
    // - extended result is 100 (SUCCESS)
    //-------------------------------------------------------------------------
    assert(response.getResultCode().getTResultCode() == 2001);
    assert(response.getExtendedResult().getExtendedResultCode() == 100);

    return response;
  }

  /**
   * This sample code retrieves binary data (from a greeting) from the
   * server.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiPhone the primary telephone number for this subscriber
   * @param xiGrtType the type of the greeting to retrieve
   *
   * @return the result of the ShPull operation
   *
   * @throws Exception
   */
  public ShPullResponse retrieveFile(String xiServer,
                                     String xiPhone,
                                     TGreetingType xiGrtType)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShPull pull = UtilitiesSample.buildShPull(xiPhone, "Msph_Subscriber_Greetings_Data");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    ShPullResponse response = UtilitiesSample.doShPull(xiServer, pull);

    //-------------------------------------------------------------------------
    // Confirm that the result is successful:
    // - result code is 2001 (IMS_SUCCESS)
    // - extended result is 100 (SUCCESS)
    //-------------------------------------------------------------------------
    assert(response.getResultCode().getTResultCode() == 2001);
    assert(response.getExtendedResult().getExtendedResultCode() == 100);

    //-------------------------------------------------------------------------
    // Extract the greetings data from the returned data.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_Greetings_Data_Greeting[] rc =
      response.getUserData().getShData().getRepositoryData()[0].
      getServiceData().getMetaSphereData().getMsph_Subscriber_Greetings_Data().
      getGreeting();

    //-------------------------------------------------------------------------
    // Search for the specified greeting.
    //-------------------------------------------------------------------------
    for (TMsph_Subscriber_Greetings_Data_Greeting greeting : rc)
    {
      if (greeting.getName() == xiGrtType)
      {
        //---------------------------------------------------------------------
        // Found the greeting we're looking for.
        // Write the data out to disk.
        //---------------------------------------------------------------------
        DataHandler dh =
          greeting.getGreetingFile().getData().getBase64Binary();
        InputStream in =
          new BufferedInputStream(dh.getDataSource().getInputStream());

        File outFile = new File(xiGrtType.toString() + ".wav");
        OutputStream out =
          new BufferedOutputStream(new FileOutputStream(outFile));

        //---------------------------------------------------------------------
        // Copy the file from the input stream to the output stream.
        //---------------------------------------------------------------------
        byte buffer[] = new byte[1024];
        int bytesRead;

        while ((bytesRead = in.read(buffer)) != -1)
        {
          out.write(buffer, 0, bytesRead);
        }

        //---------------------------------------------------------------------
        // Done copying - flush and close streams.
        //---------------------------------------------------------------------
        in.close();
        out.flush();
        out.close();
      }
    }

    return response;
  }
}