//-----------------------------------------------------------------------------
// UtilitiesSample.java
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
//-----------------------------------------------------------------------------
package dcl.wsd.test.fv.sample;

import org.apache.axis2.client.Options;

import com.metaswitch.www.sdp.soap.sh.ShPull;
import com.metaswitch.www.sdp.soap.sh.ShPullResponse;
import com.metaswitch.www.sdp.soap.sh.ShServiceStub;
import com.metaswitch.www.sdp.soap.sh.ShUpdate;
import com.metaswitch.www.sdp.soap.sh.ShUpdateResponse;
import com.metaswitch.www.sdp.soap.sh.TDataReference;
import com.metaswitch.www.sdp.soap.sh.TUserIdentity;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMetaSphereData;
import com.metaswitch.www.sdp.soap.sh.servicedata.TServiceData;
import com.metaswitch.www.sdp.soap.sh.userdata.TSequenceNumber;
import com.metaswitch.www.sdp.soap.sh.userdata.TShData;
import com.metaswitch.www.sdp.soap.sh.userdata.TString;
import com.metaswitch.www.sdp.soap.sh.userdata.TTransparentData;
import com.metaswitch.www.sdp.soap.sh.userdata.TUserData;

import dcl.wsd.test.AbstractTestBase;

/**
 * Code sample: Utility functions for sample code
 *
 * The utility methods in this class contain code to handle the building up and
 * sending of Sh-Updates and Sh-Pulls. These methods are suitable for use by
 * any WSD client code.
 *
 * This sample client was built using the Axis2 (version 1.4.1) wsdl2java
 * utility, ADB databinding and the --unpack-classes option.
 *
 * It is not guaranteed to work with other data bindings, client framework
 * utilities or client stub code generated using other versions of Axis2.
 */
public class UtilitiesSample
{
  /**
   * Utility to create a ShUpdate object for provisioning operations.
   *
   * @param xiUserID the primary phone number or other ID of the target object
   *
   * @return the newly created ShUpdate operation.
   */
  static ShUpdate buildShUpdate(String xiUserID)
  {
    //-------------------------------------------------------------------------
    // Define the parameters for this ShUpdate request
    //-------------------------------------------------------------------------
    // Data reference must always be 0
    int dataReference = 0;

    // Origin host contains domain information, client version, administrator
    // credentials, and an instruction to ignore sequence numbers (these are
    // irrelevant when creating objects).
    String originHost = "server@domain" +
                        "?clientVersion=1.0" +
                        "&adminName=defaultGroupAdmin" +
                        "&password="+AbstractTestBase.ADMIN_PASSWORD +
                        "&ignoreSequenceNumber=true";

    // User identity is the phone number of the subscriber to add.
    String userIdentity = xiUserID;

    //-------------------------------------------------------------------------
    // Build up the basic update using the parameters defined above
    //-------------------------------------------------------------------------
    // Create update object
    ShUpdate update = new ShUpdate();

    // Data reference
    TDataReference ref = new TDataReference();
    ref.setTDataReference(dataReference);
    update.setDataReference(ref);

    // Origin host
    update.setOriginHost(originHost);

    // User identity
    TUserIdentity user = new TUserIdentity();
    user.setTUserIdentity(userIdentity);
    update.setUserIdentity(user);

    return update;
  }

  /**
   * Utility to create a ShPull object for provisioning operations.
   *
   * @param xiUserID the primary phone number or other ID of the target object.
   * @param xiIndication the indication to pull
   *
   * @return the newly created ShPull operation.
   */
  static ShPull buildShPull(String xiUserID, String xiIndication)
  {
    //-------------------------------------------------------------------------
    // Define the parameters for this ShPull request
    //-------------------------------------------------------------------------
    // Data reference must always be 0
    int dataReference = 0;

    // Origin host contains domain information, client version, administrator
    // credentials, and an instruction to ignore sequence numbers (these are
    // irrelevant when creating objects).
    String originHost = "server@domain" +
                        "?clientVersion=1.0" +
                        "&adminName=defaultGroupAdmin" +
                        "&password="+AbstractTestBase.ADMIN_PASSWORD+
                        "&ignoreSequenceNumber=true";

    // User identity is the phone number of the subscriber to add.
    String userIdentity = xiUserID;

    //-------------------------------------------------------------------------
    // Build up the basic update using the paramters defined above
    //-------------------------------------------------------------------------
    // Create update object
    ShPull pull = new ShPull();

    // Data reference
    TDataReference ref = new TDataReference();
    ref.setTDataReference(dataReference);
    pull.setDataReference(ref);

    // Origin host
    pull.setOriginHost(originHost);

    // User identity
    TUserIdentity user = new TUserIdentity();
    user.setTUserIdentity(userIdentity);
    pull.setUserIdentity(user);

    // Indication
    pull.setServiceIndication(xiIndication);

    return pull;
  }

  /**
   * Utility to add the supplied transparent data to the supplied update
   * object.
   *
   * @param xiUpdate the update under construction
   * @param xiData the transparent data to add to it.
   * @param xiIndication the indication being added
   */
  static void addDataToUpdate(ShUpdate        xiUpdate,
                              TMetaSphereData xiData,
                              String          xiIndication)
  {
    //-------------------------------------------------------------------------
    // Construct a UserData element containing the supplied MetaSphereData.
    // The underlying XML takes a form as follows:
    //
    // <UserData>
    //   <Sh-Data xmlns="http://www.metaswitch.com/sdp/soap/sh/userdata">
    //     <RepositoryData>
    //       <ServiceIndication>...xiIndication...</ServiceIndication>
    //       <SequenceNumber>0</SequenceNumber>
    //       <ServiceData>
    //         <MetaSphereData xmlns="http://www.metaswitch.com/sdp/soap/sh/servicedata">
    //           ...
    //         </MetaSphereData>
    //       </ServiceData>
    //     </RepositoryData>
    //   </Sh-Data>
    // </UserData>
    //
    //-------------------------------------------------------------------------
    TServiceData serviceData = new TServiceData();
    serviceData.setMetaSphereData(xiData);

    TTransparentData transparentData = new TTransparentData();
    transparentData.setServiceData(serviceData);

    TSequenceNumber sequenceNumber = new TSequenceNumber();
    sequenceNumber.setTSequenceNumber(0);
    transparentData.setSequenceNumber(sequenceNumber);

    TString indication = new TString();
    indication.setTString(xiIndication);
    transparentData.setServiceIndication(indication);

    //-------------------------------------------------------------------------
    // If this is the first time this utility has been called, we need to
    // create the UserData and attach it to the request.
    //-------------------------------------------------------------------------
    TUserData userData = xiUpdate.getUserData();
    TShData shData;

    if (userData == null)
    {
      userData = new TUserData();
      shData = new TShData();
      userData.setShData(shData);
      xiUpdate.setUserData(userData);
    }
    else
    {
      shData = userData.getShData();
    }

    shData.addRepositoryData(transparentData);
  }

  /**
   * Utility to issue the supplied update request to the specified web services
   * server.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiRequest the update request
   *
   * @return the update response
   *
   * @throws Exception
   */
  static ShUpdateResponse doShUpdate(String xiServer, ShUpdate xiRequest)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Get service and issue update request
    //-------------------------------------------------------------------------
    String endpoint = "http://" + xiServer + "/wsd/services/ShService";
    ShServiceStub stub = new ShServiceStub(endpoint);

    //-------------------------------------------------------------------------
    // Set default client options.
    //-------------------------------------------------------------------------
    Options options = new Options();

    // Increase the time out when sending large attachments
    options.setTimeOutInMilliSeconds(30000);

    // Apply override options
    stub._getServiceClient().setOverrideOptions(options);

    ShUpdateResponse response = stub.shUpdate(xiRequest);

    stub.cleanup();

    return response;
  }

  /**
   * Utility to issue the supplied pull request to the specified web services
   * server.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiRequest the pull request
   *
   * @return the update response
   *
   * @throws Exception
   */
  static ShPullResponse doShPull(String xiServer, ShPull xiRequest)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Get service and issue pull request
    //-------------------------------------------------------------------------
    String endpoint = "http://" + xiServer + "/wsd/services/ShService";
    ShServiceStub stub = new ShServiceStub(endpoint);

    ShPullResponse response = stub.shPull(xiRequest);

    stub.cleanup();

    return response;
  }
}