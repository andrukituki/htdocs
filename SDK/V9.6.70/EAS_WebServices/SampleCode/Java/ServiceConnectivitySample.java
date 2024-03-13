//-----------------------------------------------------------------------------
// ServiceConnectivitySampleTest.java
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
//-----------------------------------------------------------------------------
package dcl.wsd.test.fv.sample;

import com.metaswitch.www.sdp.soap.sh.ShPull;
import com.metaswitch.www.sdp.soap.sh.ShPullResponse;
import com.metaswitch.www.sdp.soap.sh.ShServiceStub;
import com.metaswitch.www.sdp.soap.sh.TDataReference;
import com.metaswitch.www.sdp.soap.sh.TUserIdentity;


/**
 * Code sample: Service connectivity test
 *
 * This sample client was built using the Axis2 (version 1.4.1) wsdl2java
 * utility, ADB databinding and the --unpack-classes option.
 *
 * It is not guaranteed to work with other data bindings, client framework
 * utilities or client stub code generated using other versions of Axis2.
 */
public class ServiceConnectivitySample
{
  /**
   * This sample code issues an ShPull request with an empty set of
   * indications.  The server responds to this with an empty success result;
   * as such it provides a useful test of basic connectivity between client
   * and server.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   *
   * @return the result of the pull operation
   *
   * @throws Exception
   */
  public ShPullResponse pingServer(String xiServer)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Define the parameters for this ShPull request
    //-------------------------------------------------------------------------

    // Data reference must always be 0
    int dataReference = 0;

    // Origin host in this case contains domain information and client version.
    String originHost = "server@domain" +
                        "?clientVersion=1.0" ;

    // User identity can be blank in this case, since we're not asking for
    // any indications
    String userIdentity = "";

    // No indications requested
    String indications = "";

    //-------------------------------------------------------------------------
    // Build up the pull
    //-------------------------------------------------------------------------
    // Create pull object
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

    // Service indication
    pull.setServiceIndication(indications);

    //-------------------------------------------------------------------------
    // Get service and issue request
    //-------------------------------------------------------------------------
    String endpoint = "http://" + xiServer + "/wsd/services/ShService";
    ShServiceStub stub = new ShServiceStub(endpoint);

    ShPullResponse response = stub.shPull(pull);

    stub.cleanup();

    //-------------------------------------------------------------------------
    // Confirm that the result is successful:
    // - result code is 2001 (IMS_SUCCESS)
    // - extended result is 100 (SUCCESS)
    //-------------------------------------------------------------------------
    assert(response.getResultCode().getTResultCode() == 2001);
    assert(response.getExtendedResult().getExtendedResultCode() == 100);

    return response;
  }
}