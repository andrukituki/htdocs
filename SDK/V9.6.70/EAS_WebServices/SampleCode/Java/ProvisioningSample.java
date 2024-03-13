//-----------------------------------------------------------------------------
// ProvisioningSample.java
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
//-----------------------------------------------------------------------------
package dcl.wsd.test.fv.sample;

import com.metaswitch.www.sdp.soap.sh.ShUpdate;
import com.metaswitch.www.sdp.soap.sh.ShUpdateResponse;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMetaSphereData;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Device_BaseInformation;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_BaseInformation;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_BaseInformation_Device;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_BaseInformation_Device_DeviceType;
import com.metaswitch.www.sdp.soap.sh.servicedata.TSwitchableDefaultString;

/**
 * Code sample: Provisioning operations
 *
 * These methods can be used to create new subscribers, delete subscribers,
 * add or remove a cellular number for an existing subscriber or change the
 * telephone number of an existing subscriber.
 *
 * This sample client was built using the Axis2 (version 1.4.1) wsdl2java
 * utility, ADB databinding and the --unpack-classes option.
 *
 * It is not guaranteed to work with other data bindings, client framework
 * utilities or client stub code generated using other versions of Axis2.
 */
public class ProvisioningSample
{
  /**
   * This sample code provisions a MetaSphere subscriber.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiPhone the primary telephone number for this subscriber
   * @param xiPIN the PIN for this subscriber
   * @param xiPassword the Password for this subscriber
   * @param xiClassOfServiceID the ID of the class of service to use for this
   *        subscriber, e.g. "ECM_Prem".  This ID must match that used to
   *        administer the class of service using craft.
   * @param xiGroupName the name of the customer group to which the new
   *        subscriber should be added.
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse provisionSubscriber(String xiServer,
                                              String xiPhone,
                                              String xiPIN,
                                              String xiPassword,
                                              String xiClassOfServiceID,
                                              String xiGroupName)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Populate the base information about this subscriber
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();

    TMsph_Subscriber_BaseInformation subBaseInfo =
      new TMsph_Subscriber_BaseInformation();
    metaData.setMsph_Subscriber_BaseInformation(subBaseInfo);

    subBaseInfo.setPrimaryPhoneNumber(xiPhone);
    subBaseInfo.setCoSID(xiClassOfServiceID);
    subBaseInfo.setGroupname(xiGroupName);
    subBaseInfo.setPIN(xiPIN);
    subBaseInfo.setPassword(xiPassword);
    subBaseInfo.setSurname("Doe");
    subBaseInfo.setGivenName("John");
    subBaseInfo.setDisplayName("John Doe");
    subBaseInfo.setAction("apply");

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_BaseInformation");

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
   * This sample code deletes a MetaSphere subscriber.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiPhone the primary telephone number for this subscriber
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse deleteSubscriber(String xiServer, String xiPhone)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Create a minimal base information object, containing just the phone
    // number and the "delete" action.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();

    TMsph_Subscriber_BaseInformation subBaseInfo =
      new TMsph_Subscriber_BaseInformation();
    metaData.setMsph_Subscriber_BaseInformation(subBaseInfo);

    subBaseInfo.setPrimaryPhoneNumber(xiPhone);
    subBaseInfo.setAction("delete");

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_BaseInformation");

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
   * This sample code provisions a new cellular number for an existing
   * subscriber.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiPhone the primary telephone number for this subscriber
   * @param xiCellularNumber the new cellular number
   * @param xiHostSwitch the telephony switch that hosts the specified
   *        cellular number.
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse provisionCellular(String xiServer,
                                            String xiPhone,
                                            String xiCellularNumber,
                                            String xiHostSwitch)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Create a subscriber base information object, with a new device attached
    // representing the new cellular device
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();

    TMsph_Subscriber_BaseInformation subBaseInfo =
      new TMsph_Subscriber_BaseInformation();
    metaData.setMsph_Subscriber_BaseInformation(subBaseInfo);
    subBaseInfo.setAction("apply");

    TMsph_Subscriber_BaseInformation_Device dev =
      new TMsph_Subscriber_BaseInformation_Device();

    dev.setDeviceType(
                  TMsph_Subscriber_BaseInformation_Device_DeviceType.cellular);
    dev.setPhoneNumber(xiCellularNumber);

    TSwitchableDefaultString hostSwitch = new TSwitchableDefaultString();
    hostSwitch.setValue(xiHostSwitch);
    dev.setHostSwitch(hostSwitch);

    subBaseInfo.setDevice(new TMsph_Subscriber_BaseInformation_Device[]{dev});

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_BaseInformation");

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
   * This sample code deletes the specified cellular number from an existing
   * subscriber.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiCellularNumber the cellular number to delete
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse deleteCellular(String xiServer,
                                         String xiCellularNumber)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiCellularNumber);

    //-------------------------------------------------------------------------
    // Create a device info object representing the cellular device
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();
    TMsph_Device_BaseInformation devInfo = new TMsph_Device_BaseInformation();
    metaData.setMsph_Device_BaseInformation(devInfo);

    devInfo.setAction("delete");

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Device_BaseInformation");

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
   * This sample code changes a subscriber's telephone number.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiOldNumber the current telephone number
   * @param xiNewNumber the new telephone number with which to replace it.
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse changeTelephoneNumber(String xiServer,
                                                String xiOldNumber,
                                                String xiNewNumber)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiOldNumber);

    //-------------------------------------------------------------------------
    // Create a device info object representing the cellular device
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();
    TMsph_Device_BaseInformation devInfo = new TMsph_Device_BaseInformation();
    metaData.setMsph_Device_BaseInformation(devInfo);

    devInfo.setAction("apply");
    devInfo.setDeviceNumber(xiNewNumber);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Device_BaseInformation");

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
}