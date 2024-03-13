//-----------------------------------------------------------------------------
// MigrationSample.java
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
//-----------------------------------------------------------------------------
package dcl.wsd.test.fv.sample;

import java.io.File;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.w3.www._2005._05.xmlmime.Base64Binary;
import org.w3.www._2005._05.xmlmime.ContentType_type0;

import com.metaswitch.www.sdp.soap.sh.ShUpdate;
import com.metaswitch.www.sdp.soap.sh.ShUpdateResponse;
import com.metaswitch.www.sdp.soap.sh.servicedata.TBinaryData;
import com.metaswitch.www.sdp.soap.sh.servicedata.TCalendar;
import com.metaswitch.www.sdp.soap.sh.servicedata.TCalendar_Service;
import com.metaswitch.www.sdp.soap.sh.servicedata.TGreetingType;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMetaSphereData;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_BaseInformation;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_ContactGroup;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_ContactGroup_ContactGroup;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_Contacts;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_Contacts_Contacts;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_DCLTUI;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_Greetings;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_Greetings_Data;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_Greetings_Data_Greeting;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_Greetings_Device;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_Greetings_Device_Greeting;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_Messaging_Data;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_Messaging_Data_Message;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_Messaging_Data_Message_Voicemail;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_Schedules;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_Schedules_Device;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_VPIM;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_VPIM_Device;
import com.metaswitch.www.sdp.soap.sh.servicedata.TPartDay;
import com.metaswitch.www.sdp.soap.sh.servicedata.TPartDay_day;
import com.metaswitch.www.sdp.soap.sh.servicedata.TPriority;
import com.metaswitch.www.sdp.soap.sh.servicedata.TSchedule;
import com.metaswitch.www.sdp.soap.sh.servicedata.TScheduleEntry;
import com.metaswitch.www.sdp.soap.sh.servicedata.TTrueFalse;

/**
 * Code sample: Migration operations
 *
 * These methods can be used as follows to migrate a subscriber from a legacy
 * system to the MetaSphere deployment.
 *
 * Starting with a legacy subscriber, communicating with MetaSphere via VPIM:
 * - Call migrateSubscriber to create the subscriber on the deployment, and
 *   remove their VPIM details from the ERT. At this stage the subscriber is
 *   marked as 'disabled for migration'; this means they cannot yet use their
 *   account.
 *
 * Assuming this is successful:
 * - Call enableSubscriber to enable the account. The subscriber is then free
 *   to use the account. At this stage, the subscriber can be safely removed
 *   from the legacy system.
 *
 * If the migration is unsuccessful, the changes can be backed out by calling
 * backoutMigratedSubscriber. This deletes the subscriber information from the
 * deployment, and reconfigures the VPIM address in the ERT.
 *
 * This sample client was built using the Axis2 (version 1.4.1) wsdl2java
 * utility, ADB databinding and the --unpack-classes option.
 *
 * It is not guaranteed to work with other data bindings, client framework
 * utilities or client stub code generated using other versions of Axis2.
 */
public class MigrationSample
{
  /**
   * This sample code migrates a subscriber from a legacy system.
   *
   * To illustrate this process, it provisions the subscriber with one
   * voicemail and one greeting.
   *
   * This assumes that a VPIM address has already been set up in the ERT (so
   * the deployment knows that the subscriber exists on the legacy system,
   * and communicates with the subscriber using the VPIM address in the ERT).
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiPhone the primary phone number for this subscriber.
   * @param xiPIN the PIN for this subscriber.
   * @param xiPassword the Password for this subscriber.
   * @param xiClassOfServiceID the ID of the class of service to use for this
   *        subscriber, e.g. "ECM_Prem".  This ID must match that used to
   *        administer the class of service using craft.
   * @param xiGroupName the name of the customer group to which the new
   *        subscriber should be added.
   * @param xiFolderName the folder to upload the voicemail into.
   * @param xiMessageFile the data file to use for the voicemail to upload.
   * @param xiGreetingType the type of greeting to set.
   * @param xiGreetingFile the file to use as the data source for the greeting
   *                       to set.
   * @return the response from the ShUpdate
   * @throws Exception
   */
  public ShUpdateResponse migrateSubscriber(String xiServer,
                                            String xiPhone,
                                            String xiPIN,
                                            String xiPassword,
                                            String xiClassOfServiceID,
                                            String xiGroupName,
                                            String xiFolderName,
                                            String xiMessageFile,
                                            TGreetingType xiGreetingType,
                                            String xiGreetingFile)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // We want to create the subscriber and migrate all their data in a single
    // step.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Populate the base information about this subscriber
    //-------------------------------------------------------------------------
    TMetaSphereData baseMetaData = new TMetaSphereData();
    TMsph_Subscriber_BaseInformation subBaseInfo =
      new TMsph_Subscriber_BaseInformation();

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
    // The subscriber needs to be in the "disabled for migration" state when
    // added.
    //-------------------------------------------------------------------------
    subBaseInfo.setDisabledforMigration(TTrueFalse.True);

    baseMetaData.setMsph_Subscriber_BaseInformation(subBaseInfo);

    //-------------------------------------------------------------------------
    // We also need the subscriber's messages.
    //-------------------------------------------------------------------------
    TMetaSphereData msgMetaData = new TMetaSphereData();
    TMsph_Subscriber_Messaging_Data msgData =
                                         new TMsph_Subscriber_Messaging_Data();
    msgData.setFolder(xiFolderName);

    TMsph_Subscriber_Messaging_Data_Message msgDataMsg =
      new TMsph_Subscriber_Messaging_Data_Message();

    TMsph_Subscriber_Messaging_Data_Message_Voicemail vmData =
      new TMsph_Subscriber_Messaging_Data_Message_Voicemail();

    // Populate basic voicemail information
    vmData.setCallerName("Tom");
    vmData.setCallerNumber("1800123456");

    msgDataMsg.setVoicemail(vmData);

    // Creating a javax.activation.FileDataSource from the input file.
    FileDataSource msgFileDataSource =
      new FileDataSource(new File(xiMessageFile));
    DataHandler msgDataHandler = new DataHandler(msgFileDataSource);

    // Determine the content type from the file
    ContentType_type0 msgContentType = new ContentType_type0();
    msgContentType.setContentType_type0(msgDataHandler.getContentType());

    // Set up in-line data
    Base64Binary msgBase64bin = new Base64Binary();
    msgBase64bin.setBase64Binary(msgDataHandler);
    msgBase64bin.setContentType(msgContentType);

    TBinaryData msgBinData = new TBinaryData();
    msgBinData.setData(msgBase64bin);

    vmData.setVoiceAttachment(msgBinData);

    msgData.addMessage(msgDataMsg);

    msgMetaData.setMsph_Subscriber_Messaging_Data(msgData);

    //-------------------------------------------------------------------------
    // Add a single migrated greeting.
    //-------------------------------------------------------------------------
    TMetaSphereData grtMetaData = new TMetaSphereData();
    TMsph_Subscriber_Greetings_Data_Greeting[] greetings =
      new TMsph_Subscriber_Greetings_Data_Greeting[1];

    // Creating a javax.activation.FileDataSource from the input file.
    FileDataSource grtFileDataSource =
      new FileDataSource(new File(xiGreetingFile));
    DataHandler grtDataHandler = new DataHandler(grtFileDataSource);

    // Determine the content type from the file
    ContentType_type0 grtContentType = new ContentType_type0();
    grtContentType.setContentType_type0(grtDataHandler.getContentType());

    //-------------------------------------------------------------------------
    // Set up in-line data and use it in the binary data object.
    //-------------------------------------------------------------------------
    // Set up in-line data.
    Base64Binary grtBase64bin = new Base64Binary();
    grtBase64bin.setBase64Binary(grtDataHandler);
    grtBase64bin.setContentType(grtContentType);

    // Add into binary data object.
    TBinaryData grtBinData = new TBinaryData();
    grtBinData.setData(grtBase64bin);

    //-------------------------------------------------------------------------
    // Set up the greeting object with the supplied name and the binary
    // data extracted above.
    //-------------------------------------------------------------------------
    greetings[0] = new TMsph_Subscriber_Greetings_Data_Greeting();
    greetings[0].setName(xiGreetingType);
    greetings[0].setGreetingFile(grtBinData);

    //-------------------------------------------------------------------------
    // Set up the greetings to send to the service and add to the
    // MetaSphereData.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_Greetings_Data grtData =
      new TMsph_Subscriber_Greetings_Data();
    grtData.setAction("apply");
    grtData.setGreeting(greetings);

    grtMetaData.setMsph_Subscriber_Greetings_Data(grtData);

    //-------------------------------------------------------------------------
    // Add greeting settings to enable the greeting.
    //-------------------------------------------------------------------------
    TMetaSphereData grtSettingsMetaData = new TMetaSphereData();
    TMsph_Subscriber_Greetings_Device_Greeting[] greetingSettings =
                             new TMsph_Subscriber_Greetings_Device_Greeting[1];

    //-------------------------------------------------------------------------
    // Set up the greeting object with the supplied name and the binary
    // data extracted above.
    //-------------------------------------------------------------------------
    greetingSettings[0] = new TMsph_Subscriber_Greetings_Device_Greeting();
    greetingSettings[0].setEnabled(TTrueFalse.True);
    greetingSettings[0].setGreetingName(xiGreetingType);

    //-------------------------------------------------------------------------
    // Set up the greetings to send to the service and add to the
    // MetaSphereData.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_Greetings_Device dev =
                                       new TMsph_Subscriber_Greetings_Device();
    dev.setPhoneNumber(xiPhone);
    dev.addGreeting(greetingSettings[0]);

    TMsph_Subscriber_Greetings grtSettingsData =
                                              new TMsph_Subscriber_Greetings();
    grtSettingsData.setAction("apply");

    grtSettingsData.addDevice(dev);
    grtSettingsMetaData.setMsph_Subscriber_Greetings(grtSettingsData);

    //-------------------------------------------------------------------------
    // Set up the initialisation state for the TUI, to prevent the subscriber
    // from needing to re-initialise on their first login.
    //-------------------------------------------------------------------------
    TMetaSphereData tuiMetaData = new TMetaSphereData();

    TMsph_Subscriber_DCLTUI dclTUI = new TMsph_Subscriber_DCLTUI();

    dclTUI.setAction("apply");
    dclTUI.setInitializedPIN(TTrueFalse.True);

    tuiMetaData.setMsph_Subscriber_DCLTUI(dclTUI);

    //-------------------------------------------------------------------------
    // Add a contact to the contacts list
    //-------------------------------------------------------------------------
    TMetaSphereData contactsMetaData = new TMetaSphereData();
    TMsph_Subscriber_Contacts_Contacts contacts =
                                    new TMsph_Subscriber_Contacts_Contacts();
    // Mandatory element
    contacts.setDisplayName("Customer Support");
    // Optional element
    contacts.setHomePhone("1800123123");

    TMsph_Subscriber_Contacts contactsData = new TMsph_Subscriber_Contacts();
    contactsData.addContacts(contacts);
    contactsData.setAction("apply");

    contactsMetaData.setMsph_Subscriber_Contacts(contactsData);

    //-------------------------------------------------------------------------
    // Add a contact group
    //-------------------------------------------------------------------------
    TMetaSphereData contactGroupMetaData = new TMetaSphereData();
    TMsph_Subscriber_ContactGroup_ContactGroup contactGroup =
                              new TMsph_Subscriber_ContactGroup_ContactGroup();
    // Optional elements
    contactGroup.setTUID("3");
    contactGroup.addContactPhoneNumber("1800123123");

    TMsph_Subscriber_ContactGroup contactGroupData =
                                           new TMsph_Subscriber_ContactGroup();
    contactGroupData.addContactGroup(contactGroup);
    contactGroupData.setAction("apply");

    contactGroupMetaData.setMsph_Subscriber_ContactGroup(contactGroupData);

    //-------------------------------------------------------------------------
    // Add an out-of-hours device calendar
    //-------------------------------------------------------------------------
    TMetaSphereData schedData = new TMetaSphereData();
    TMsph_Subscriber_Schedules scheds = new TMsph_Subscriber_Schedules();
    TMsph_Subscriber_Schedules_Device devSched =
                                       new TMsph_Subscriber_Schedules_Device();
    scheds.setAction("apply");
    TPriority prio = new TPriority();
    TPartDay part = new TPartDay();
    TScheduleEntry schedEntPart = new TScheduleEntry();
    TSchedule partTimeSched = new TSchedule();
    TCalendar calendar = new TCalendar();

    prio.setTPriority(1);
    part.setDay(TPartDay_day.Tuesday);
    part.setIndex(prio);
    part.setStart("09:00");
    part.setEnd("17:00");
    schedEntPart.setPartDay(part);
    partTimeSched.setName("Part-time hours");
    partTimeSched.addEntry(schedEntPart);
    calendar.setService(TCalendar_Service.TUI);
    calendar.addSchedules(partTimeSched);
    devSched.setPhoneNumber(xiPhone);
    devSched.addCalendar(calendar);
    scheds.addDevice(devSched);

    schedData.setMsph_Subscriber_Schedules(scheds);

    //-------------------------------------------------------------------------
    // Add each MetaSphere data object to the update operation under
    // construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    baseMetaData,
                                    "Msph_Subscriber_BaseInformation");
    UtilitiesSample.addDataToUpdate(update,
                                    msgMetaData,
                                    "Msph_Subscriber_Messaging_Data");
    UtilitiesSample.addDataToUpdate(update,
                                    grtMetaData,
                                    "Msph_Subscriber_Greetings_Data");
    UtilitiesSample.addDataToUpdate(update,
                                    grtSettingsMetaData,
                                    "Msph_Subscriber_Greetings");
    UtilitiesSample.addDataToUpdate(update,
                                    tuiMetaData,
                                    "Msph_Subscriber_DCLTUI");
    UtilitiesSample.addDataToUpdate(update,
                                    contactsMetaData,
                                    "Msph_Subscriber_Contacts");
    UtilitiesSample.addDataToUpdate(update,
                                    contactGroupMetaData,
                                    "Msph_Subscriber_ContactGroup");
    UtilitiesSample.addDataToUpdate(update,
                                    schedData,
                                    "Msph_Subscriber_Schedules");

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
   * This sample code enables a subscriber that has been migrated from a legacy
   * system.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiPhone the primary phone number for this subscriber
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse enableMigratedSubscriber(String xiServer,
                                                   String xiPhone)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Add data to enable the specified subscriber.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();

    TMsph_Subscriber_BaseInformation subBaseInfo =
      new TMsph_Subscriber_BaseInformation();
    metaData.setMsph_Subscriber_BaseInformation(subBaseInfo);

    subBaseInfo.setDisabledforMigration(TTrueFalse.False);
    subBaseInfo.setAction("apply");

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction.
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_BaseInformation");

    //-------------------------------------------------------------------------
    // Issue the request.
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
   * This sample code backs out a subscriber who has been migrated from a
   * legacy system.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiPhone the primary telephone number for this subscriber
   * @param xiVPIMDomain the domain used for VPIM addresses
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse backoutMigratedSubscriber(String xiServer,
                                                    String xiPhone,
                                                    String xiVPIMDomain)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // We want to back out the migrated subscriber.
    // To do this, we need to delete the subscriber and re-instate their
    // VPIM data in the ERT.
    //
    // Add instructions to delete the subscriber.
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

    //-------------------------------------------------------------------------
    // Deleting a subscriber takes a couple of seconds to propagate to the ERT;
    // hence we need to wait before we can re-add the old VPIM address.
    //-------------------------------------------------------------------------
    Thread.sleep(3000);

    //-------------------------------------------------------------------------
    // Create a new update operation.
    //-------------------------------------------------------------------------
    update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Add information to re-add the VPIM data.
    //-------------------------------------------------------------------------
    metaData = new TMetaSphereData();

    TMsph_VPIM_Device vpimDevice = new TMsph_VPIM_Device();
    vpimDevice.setDomain(xiVPIMDomain);
    vpimDevice.setPhoneNumber(xiPhone);

    TMsph_VPIM vpimData = new TMsph_VPIM();
    vpimData.addDevice(vpimDevice);
    vpimData.setAction("apply");

    metaData.setMsph_VPIM(vpimData);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_VPIM");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    response = UtilitiesSample.doShUpdate(xiServer, update);

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