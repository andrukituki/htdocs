//-----------------------------------------------------------------------------
// CallTreeSample.java
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
//-----------------------------------------------------------------------------
package dcl.wsd.test.fv.sample;

import java.io.File;
import java.util.HashMap;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.w3.www._2005._05.xmlmime.Base64Binary;
import org.w3.www._2005._05.xmlmime.ContentType_type0;

import com.metaswitch.www.sdp.soap.sh.ShUpdate;
import com.metaswitch.www.sdp.soap.sh.ShUpdateResponse;
import com.metaswitch.www.sdp.soap.sh.servicedata.TAction;
import com.metaswitch.www.sdp.soap.sh.servicedata.TAction_CallExtension;
import com.metaswitch.www.sdp.soap.sh.servicedata.TAction_CallTransfer;
import com.metaswitch.www.sdp.soap.sh.servicedata.TAction_HangUp;
import com.metaswitch.www.sdp.soap.sh.servicedata.TAction_MailboxTransfer;
import com.metaswitch.www.sdp.soap.sh.servicedata.TAction_OperatorTransfer;
import com.metaswitch.www.sdp.soap.sh.servicedata.TAction_PlayAnnouncement;
import com.metaswitch.www.sdp.soap.sh.servicedata.TAction_ReplayMenu;
import com.metaswitch.www.sdp.soap.sh.servicedata.TAction_ReturnFromMenu;
import com.metaswitch.www.sdp.soap.sh.servicedata.TBinaryData;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMetaSphereData;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_Announcements;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_AnnouncementsData;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_AnnouncementsData_AnnouncementData;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_Announcements_Announcement;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_BaseInformation;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_DialByExtensionList;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_DialByExtensionList_ExtensionListEntry;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_Nodes;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_Nodes_MenuNode;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_Nodes_MenuNode_Result;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_Nodes_MenuNode_ScheduleID;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_Settings;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_Settings_DefaultKeys;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_Settings_Errors;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_Settings_Errors_Error;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_Settings_Errors_Error_ErrorType;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_Settings_ScheduleEntryPoints;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_CallTree_Settings_ScheduleEntryPoints_ScheduleEntryPoint;
import com.metaswitch.www.sdp.soap.sh.servicedata.TTrueFalse;

/**
 * Code sample: Call tree operations
 * <p>
 * These methods can be used to create new call trees, and modify each part of
 * the call tree configuration.
 * <p>
 * The {@link #createSampleCallTree(String, String, String)} method
 * demonstrates how to use the entire functionality of the call tree
 * indications in a single request. The other methods are demonstrations of
 * more specific, targeted operations.
 *
 * This sample client was built using the Axis2 (version 1.4.1) wsdl2java
 * utility, ADB databinding and the --unpack-classes option.
 *
 * It is not guaranteed to work with other data bindings, client framework
 * utilities or client stub code generated using other versions of Axis2.
 */
public class CallTreeSample
{
  /**
   * This sample code creates a sample call tree that makes use of all
   * available features.
   * <p>
   * The call tree will have the following structure:
   * <ul>
   *   <li>It will be enabled, with the forwarding number set to 5566778899.
   *   <li>Two menu nodes:
   *   <ol>
   *     <li><em>Work hours menu</em>
   *     <ul>
   *       <li>Node ID: "Office Hours"
   *       <li>Announcement ID: "Office Hours Welcome"
   *       <li>Schedule ID: schedulesEABusinessHours
   *       <li>Results:
   *       <ol>
   *         <li>Play announcement with ID "Useful Information"
   *         <li>Transfer to mailbox (number determined by parameter xiMailbox)
   *         <li>Call extension
   *         <li><em>Unassigned</em>
   *         <li><em>Unassigned</em>
   *         <li><em>Unassigned</em>
   *         <li><em>Unassigned</em>
   *         <li><em>Unassigned</em>
   *         <li>Transfer call to 4455667788
   *       </ol>
   *     </ul>
   *     <li><em>Out of hours hours menu</em>
   *     <ul>
   *       <li>Node ID: "Out of Office Hours"
   *       <li>Announcement ID: "Office Closed"
   *       <li>Results:
   *       <ol>
   *         <li>Play announcement with ID "Useful Information"
   *         <li>Transfer to mailbox (number determined by parameter xiMailbox)
   *         <li>Play announcement with ID "Opening Hours"
   *       </ol>
   *     </ul>
   *   </ol>
   *   <li>Two additional (unused) announcements
   *   <ul>
   *     <li>Announcement with ID "Useful Information 2"
   *     <li>Announcement with ID "Public Holiday Opening Hours"
   *   </ul>
   *   <li>A dial list with the following entries: (Extension Number Name)
   *   <ul>
   *     <li>0       3344556677 Reception
   *     <li>404     2233445566 Sales (US)
   *     <li>405     1122334455 Sales (EU)
   *     <li>1223344 0011223344 Chairman
   *   </ul>
   * </ul>
   *
   * @param xiPhone   the call tree's number
   * @param xiServer  the web services server and port in the form
   *                  servername:port
   * @param xiMailbox the number of a valid mailbox.
   * @return the {@link ShUpdateResponse}
   * @throws Exception
   */
  public ShUpdateResponse createSampleCallTree(String xiPhone,
                                               String xiServer,
                                               String xiMailbox)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Set up the data objects to pass into the update.
    //
    // Begin with the base information indication
    // - set the forwarding number to 5566778899
    // - enable the call tree
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_BaseInformation ctBaseInfoIndication =
      new TMsph_Subscriber_CallTree_BaseInformation();
    ctBaseInfoIndication.setActive(TTrueFalse.True);
    ctBaseInfoIndication.setForwardingNumber("5566778899");

    //-------------------------------------------------------------------------
    // Add the base information indication to the update.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();
    metaData.setMsph_Subscriber_CallTree_BaseInformation(ctBaseInfoIndication);
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                   "Msph_Subscriber_CallTree_BaseInformation");

    //-------------------------------------------------------------------------
    // Next create the dial list, with the following entries.
    // - 0       3344556677 Reception
    // - 404     2233445566 Sales (US)
    // - 405     1122334455 Sales (EU)
    // - 1223344 0011223344 Chairman
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_DialByExtensionList_ExtensionListEntry[] exts =
      new TMsph_Subscriber_CallTree_DialByExtensionList_ExtensionListEntry[4];

    exts[0] =
      new TMsph_Subscriber_CallTree_DialByExtensionList_ExtensionListEntry();
    exts[0].setExtension("0");
    exts[0].setNumber("3344556677");
    exts[0].setFirstName("Reception");

    exts[1] =
      new TMsph_Subscriber_CallTree_DialByExtensionList_ExtensionListEntry();
    exts[1].setExtension("404");
    exts[1].setNumber("2233445566");
    exts[1].setFirstName("Sales");
    exts[1].setFirstName("(US)");

    exts[2] =
      new TMsph_Subscriber_CallTree_DialByExtensionList_ExtensionListEntry();
    exts[2].setExtension("405");
    exts[2].setNumber("1122334455");
    exts[2].setFirstName("Sales");
    exts[2].setFirstName("(EU)");

    exts[3] =
      new TMsph_Subscriber_CallTree_DialByExtensionList_ExtensionListEntry();
    exts[3].setExtension("1223344");
    exts[3].setNumber("0011223344");
    exts[3].setFirstName("John");
    exts[3].setLastName("Doe");

    //-------------------------------------------------------------------------
    // Set up the dial by extension indication, and add this indication to the
    // MetaSphere data.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_DialByExtensionList ctExtsIndication =
      new TMsph_Subscriber_CallTree_DialByExtensionList();
    ctExtsIndication.setExtensionListEntry(exts);

    metaData = new TMetaSphereData();
    metaData.setMsph_Subscriber_CallTree_DialByExtensionList(ctExtsIndication);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                               "Msph_Subscriber_CallTree_DialByExtensionList");

    //-------------------------------------------------------------------------
    // Next create the two menus
    //
    // Work hours menu:
    //   Node ID: "Office Hours"
    //   Announcement ID: "Office Hours Welcome"
    //   Schedule ID: schedulesEABusinessHours
    //   Results:
    //     1: Play announcement with ID "Useful Information"
    //     2: Transfer to mailbox number xiMailbox
    //     3: Call extension
    //     9: Transfer call to 4455667788
    //
    // Out of hours hours menu:
    //   Node ID: "Out of Office Hours"
    //   Announcement ID: "Office Closed"
    //   Results:
    //     1: Play announcement with ID "Useful Information"
    //     2: Transfer to mailbox number xiMailbox
    //     3: Play announcement with ID "Opening Hours"
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Nodes_MenuNode[] nodes =
      new TMsph_Subscriber_CallTree_Nodes_MenuNode[2];

    //-------------------------------------------------------------------------
    // Begin by creating the office hours menu
    //-------------------------------------------------------------------------
    nodes[0] = new TMsph_Subscriber_CallTree_Nodes_MenuNode();
    nodes[0].setNodeID("Office Hours");
    nodes[0].setAnnouncementID("Office Hours Welcome");
    nodes[0].setScheduleID(TMsph_Subscriber_CallTree_Nodes_MenuNode_ScheduleID
                                                    .schedulesEABusinessHours);

    //-------------------------------------------------------------------------
    // Create the results for the office hours menu
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Nodes_MenuNode_Result[] officeHoursResults =
      new TMsph_Subscriber_CallTree_Nodes_MenuNode_Result[4];

    //-------------------------------------------------------------------------
    // Create a play announcement result ("Useful Information") for key 1
    //-------------------------------------------------------------------------
    officeHoursResults[0] =
      new TMsph_Subscriber_CallTree_Nodes_MenuNode_Result();
    officeHoursResults[0].setEvent("1");
    officeHoursResults[0].setAction(new TAction());
    officeHoursResults[0].getAction().setPlayAnnouncement(
                                               new TAction_PlayAnnouncement());
    officeHoursResults[0].getAction().getPlayAnnouncement()
                                      .setAnnouncementID("Useful Information");

    //-------------------------------------------------------------------------
    // Create a mailbox transfer result (to xiMailbox) for key 2
    //-------------------------------------------------------------------------
    officeHoursResults[1] =
      new TMsph_Subscriber_CallTree_Nodes_MenuNode_Result();
    officeHoursResults[1].setEvent("2");
    officeHoursResults[1].setAction(new TAction());
    officeHoursResults[1].getAction().setMailboxTransfer(
                                                new TAction_MailboxTransfer());
    officeHoursResults[1].getAction().getMailboxTransfer()
                                                .setMailboxTransfer(xiMailbox);

    //-------------------------------------------------------------------------
    // Create a call extension result for key 3
    //-------------------------------------------------------------------------
    officeHoursResults[2] =
      new TMsph_Subscriber_CallTree_Nodes_MenuNode_Result();
    officeHoursResults[2].setEvent("3");
    officeHoursResults[2].setAction(new TAction());
    officeHoursResults[2].getAction().setCallExtension(
                                                  new TAction_CallExtension());

    //-------------------------------------------------------------------------
    // Create a call transfer result (to 4455667788) for key 9
    //-------------------------------------------------------------------------
    officeHoursResults[3] =
      new TMsph_Subscriber_CallTree_Nodes_MenuNode_Result();
    officeHoursResults[3].setEvent("9");
    officeHoursResults[3].setAction(new TAction());
    officeHoursResults[3].getAction().setCallTransfer(
                                                   new TAction_CallTransfer());
    officeHoursResults[3].getAction().getCallTransfer()
                                          .setCallTransferNumber("4455667788");

    //-------------------------------------------------------------------------
    // Set these results for the office hours menu
    //-------------------------------------------------------------------------
    nodes[0].setResult(officeHoursResults);

    //-------------------------------------------------------------------------
    // Begin by creating the office hours menu
    //-------------------------------------------------------------------------
    nodes[1] = new TMsph_Subscriber_CallTree_Nodes_MenuNode();
    nodes[1].setNodeID("Out Of Office Hours");
    nodes[1].setAnnouncementID("Office Closed");

    //-------------------------------------------------------------------------
    // Next create the results for the out of office hours menu
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Nodes_MenuNode_Result[] outOfOfficeHoursResults =
      new TMsph_Subscriber_CallTree_Nodes_MenuNode_Result[3];

    //-------------------------------------------------------------------------
    // Create a play announcement result ("Useful Information") for key 1
    //-------------------------------------------------------------------------
    outOfOfficeHoursResults[0] =
      new TMsph_Subscriber_CallTree_Nodes_MenuNode_Result();
    outOfOfficeHoursResults[0].setEvent("1");
    outOfOfficeHoursResults[0].setAction(new TAction());
    outOfOfficeHoursResults[0].getAction().setPlayAnnouncement(
                                               new TAction_PlayAnnouncement());
    outOfOfficeHoursResults[0].getAction().getPlayAnnouncement()
                                      .setAnnouncementID("Useful Information");

    //-------------------------------------------------------------------------
    // Create a mailbox transfer result (to xiMailbox) for key 2
    //-------------------------------------------------------------------------
    outOfOfficeHoursResults[1] =
      new TMsph_Subscriber_CallTree_Nodes_MenuNode_Result();
    outOfOfficeHoursResults[1].setEvent("2");
    outOfOfficeHoursResults[1].setAction(new TAction());
    outOfOfficeHoursResults[1].getAction().setMailboxTransfer(
                                                new TAction_MailboxTransfer());
    outOfOfficeHoursResults[1].getAction().getMailboxTransfer()
                                                .setMailboxTransfer(xiMailbox);

    //-------------------------------------------------------------------------
    // Create a play announcement result ("Opening Hours") for key 3
    //-------------------------------------------------------------------------
    outOfOfficeHoursResults[2] =
      new TMsph_Subscriber_CallTree_Nodes_MenuNode_Result();
    outOfOfficeHoursResults[2].setEvent("3");
    outOfOfficeHoursResults[2].setAction(new TAction());
    outOfOfficeHoursResults[2].getAction().setPlayAnnouncement(
                                               new TAction_PlayAnnouncement());
    outOfOfficeHoursResults[2].getAction().getPlayAnnouncement()
                                           .setAnnouncementID("Opening Hours");

    //-------------------------------------------------------------------------
    // Set these results for the office hours menu
    //-------------------------------------------------------------------------
    nodes[1].setResult(outOfOfficeHoursResults);

    //-------------------------------------------------------------------------
    // Set up the nodes indication, and add this indication to the MetaSphere
    // data.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Nodes ctNodesIndication =
      new TMsph_Subscriber_CallTree_Nodes();
    ctNodesIndication.setMenuNode(nodes);

    metaData = new TMetaSphereData();
    metaData.setMsph_Subscriber_CallTree_Nodes(ctNodesIndication);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_CallTree_Nodes");

    //-------------------------------------------------------------------------
    // Create a couple of extra (unused) announcements
    // - "Useful Information 2"
    // - "Public Holiday Opening Hours"
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Announcements_Announcement[] anns =
      new TMsph_Subscriber_CallTree_Announcements_Announcement[2];

    anns[0] = new TMsph_Subscriber_CallTree_Announcements_Announcement();
    anns[0].setAnnouncementID("Useful Information 2");
    anns[0].setRecorded(TTrueFalse.False);

    anns[1] = new TMsph_Subscriber_CallTree_Announcements_Announcement();
    anns[1].setAnnouncementID("Public Holiday Opening Hours");
    anns[1].setRecorded(TTrueFalse.False);

    //-------------------------------------------------------------------------
    // Set up the announcement indication, and add this indication to the
    // MetaSphere data.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Announcements ctAnnsIndication =
      new TMsph_Subscriber_CallTree_Announcements();
    ctAnnsIndication.setAnnouncement(anns);

    metaData = new TMetaSphereData();
    metaData.setMsph_Subscriber_CallTree_Announcements(ctAnnsIndication);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_CallTree_Announcements");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    ShUpdateResponse response = UtilitiesSample.doShUpdate(xiServer, update);

    return response;
  }

  /**
   * This sample code creates a simple call tree for the specified subscriber.
   * <p>
   * The tree has a single menu, with the ID specified, which has an initial
   * announcement with the ID specified.
   * <p>
   * The specified results are added to the menu node. See
   * {@link #createPlayAnnouncementResult(String, String)} for an example of
   * how to create results.
   *
   * @param xiPhone              the call tree's number
   * @param xiServer             the web services server and port in the form
   *                             servername:port
   * @param xiMenuID             the ID to use for the menu node
   * @param xiMenuAnnouncementID the announcement ID to use for the menu's
   *                             initial announcement.
   * @param xiScheduled          whether the node is used for the work hours
   *                             schedule or not.<p>
   *                             When creating a second menu node, exactly one
   *                             of the two menu nodes must use a schedule.
   * @param results              The array of results to use for this node.
   * @return the {@link ShUpdateResponse}
   * @throws Exception
   */
  public ShUpdateResponse createMenuNode(String xiPhone,
                                         String xiServer,
                                         String xiMenuID,
                                         String xiMenuAnnouncementID,
                                         boolean xiScheduled,
                     TMsph_Subscriber_CallTree_Nodes_MenuNode_Result[] results)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Set up the data objects to pass into the update.
    //
    // Create a menu node for the call tree - set the node ID and announcement
    // ID passed in as parameters.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Nodes_MenuNode[] nodes =
      new TMsph_Subscriber_CallTree_Nodes_MenuNode[1];
    nodes[0] = new TMsph_Subscriber_CallTree_Nodes_MenuNode();
    nodes[0].setNodeID(xiMenuID);
    nodes[0].setAnnouncementID(xiMenuAnnouncementID);

    //-------------------------------------------------------------------------
    // Set the schedule ID if this node is scheduled
    //-------------------------------------------------------------------------
    if (xiScheduled)
    {
      nodes[0].setScheduleID(
        TMsph_Subscriber_CallTree_Nodes_MenuNode_ScheduleID
                                                    .schedulesEABusinessHours);
    }

    //-------------------------------------------------------------------------
    // Set the results for this node
    //-------------------------------------------------------------------------
    nodes[0].setResult(results);

    //-------------------------------------------------------------------------
    // Set up the nodes indication, and add this indication to the MetaSphere
    // data.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Nodes ctNodesIndication =
      new TMsph_Subscriber_CallTree_Nodes();
    ctNodesIndication.setMenuNode(nodes);

    TMetaSphereData metaData = new TMetaSphereData();
    metaData.setMsph_Subscriber_CallTree_Nodes(ctNodesIndication);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_CallTree_Nodes");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    ShUpdateResponse response = UtilitiesSample.doShUpdate(xiServer, update);

    return response;
  }

  /**
   * Sample code for creating a play announcement result.
   * <p>
   * This method can be simply enhanced for creating the other available types
   * of result too.
   *
   * @param xiEvent          the event for the play announcement result
   *                         (e.g. "2" for key 2)
   * @param xiAnnouncementID the announcement ID
   * @return the result
   */
  public TMsph_Subscriber_CallTree_Nodes_MenuNode_Result
    createPlayAnnouncementResult(String xiEvent, String xiAnnouncementID)
  {
    //-------------------------------------------------------------------------
    // Create a new result
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Nodes_MenuNode_Result result =
      new TMsph_Subscriber_CallTree_Nodes_MenuNode_Result();

    //-------------------------------------------------------------------------
    // Create a play announcement action for this result
    //-------------------------------------------------------------------------
    TAction action = new TAction();
    TAction_PlayAnnouncement annAction = new TAction_PlayAnnouncement();
    action.setPlayAnnouncement(annAction);

    //-------------------------------------------------------------------------
    // Set the announcement for the play announcement action
    //-------------------------------------------------------------------------
    annAction.setAnnouncementID(xiAnnouncementID);

    //-------------------------------------------------------------------------
    // Set the result to use this play announcement action
    //-------------------------------------------------------------------------
    result.setEvent(xiEvent);
    result.setAction(action);

    return result;
  }

  /**
   * Create an announcement on the call tree.
   * <p>
   * This will add it to the list of announcements on the tree.
   *
   * @param xiPhone          the call tree's number
   * @param xiServer         the web services server and port in the form
   *                         servername:port
   * @param xiAnnouncementID the announcement ID
   * @return the {@link ShUpdateResponse}
   * @throws Exception
   */
  public ShUpdateResponse createAnnouncement(String xiPhone,
                                             String xiServer,
                                             String xiAnnouncementID)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Set up the data objects to pass into the update.
    //
    // Create an announcement, with the ID specified.
    // Mark this announcement as not recorded, since we are only just creating
    // it now.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Announcements_Announcement[] anns =
      new TMsph_Subscriber_CallTree_Announcements_Announcement[1];
    anns[0] = new TMsph_Subscriber_CallTree_Announcements_Announcement();
    anns[0].setAnnouncementID(xiAnnouncementID);
    anns[0].setRecorded(TTrueFalse.False);

    //-------------------------------------------------------------------------
    // Set up the announcement indication, and add this indication to the
    // MetaSphere data.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Announcements ctAnnsIndication =
      new TMsph_Subscriber_CallTree_Announcements();
    ctAnnsIndication.setAnnouncement(anns);

    TMetaSphereData metaData = new TMetaSphereData();
    metaData.setMsph_Subscriber_CallTree_Announcements(ctAnnsIndication);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_CallTree_Announcements");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    ShUpdateResponse response = UtilitiesSample.doShUpdate(xiServer, update);

    return response;
  }

  /**
   * Create an announcement data for the call tree.
   * <p>
   * This will create the announcement with the specified ID, if it does not
   * exist already.
   * <p>
   * The specified announcement will now be marked as recorded.
   *
   * @param xiPhone          the call tree's number
   * @param xiServer         the web services server and port in the form
   *                         servername:port
   * @param xiAnnouncementID the announcement ID
   * @param xiWavFile        the wav file
   * @return the {@link ShUpdateResponse}
   * @throws Exception
   */
  public ShUpdateResponse createAnnouncementData(String xiPhone,
                                                 String xiServer,
                                                 String xiAnnouncementID,
                                                 File xiWavFile)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Set up the data objects to pass into the update.
    //
    // Create an announcement data, for the announcement ID specified.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_AnnouncementsData_AnnouncementData[] annData =
      new TMsph_Subscriber_CallTree_AnnouncementsData_AnnouncementData[1];
    annData[0] =
      new TMsph_Subscriber_CallTree_AnnouncementsData_AnnouncementData();
    annData[0].setAnnouncementID(xiAnnouncementID);

    //-------------------------------------------------------------------------
    // Set the binary data for the announcement data.
    //-------------------------------------------------------------------------
    TBinaryData binData = getInlineBinaryData(xiWavFile);
    annData[0].setRecordingFile(binData);

    //-------------------------------------------------------------------------
    // Set up the announcement data indication, and add this indication to the
    // MetaSphere data.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_AnnouncementsData ctAnnDataIndication =
      new TMsph_Subscriber_CallTree_AnnouncementsData();
    ctAnnDataIndication.setAnnouncementData(annData);

    TMetaSphereData metaData = new TMetaSphereData();
    metaData
           .setMsph_Subscriber_CallTree_AnnouncementsData(ctAnnDataIndication);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                 "Msph_Subscriber_CallTree_AnnouncementsData");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    ShUpdateResponse response = UtilitiesSample.doShUpdate(xiServer, update);

    return response;
  }

  /**
   * @param xiWavFile the wav file
   * @return a TBinaryData object with the specified file contained in line
   */
  private TBinaryData getInlineBinaryData(File xiWavFile)
  {
    // Creating a javax.activation.FileDataSource from the input file.
    FileDataSource fileDataSource = new FileDataSource(xiWavFile);
    DataHandler dataHandler = new DataHandler(fileDataSource);

    // Read the content type
    ContentType_type0 contentType = new ContentType_type0();
    contentType.setContentType_type0(dataHandler.getContentType());

    // Set up in-line data
    Base64Binary base64bin = new Base64Binary();
    base64bin.setBase64Binary(dataHandler);
    base64bin.setContentType(contentType);

    TBinaryData binData = new TBinaryData();
    binData.setData(base64bin);

    return binData;
  }

  /**
   * Create a dial by extension list for the call tree.
   * <p>
   * The list of names, numbers and extensions passed in is taken to be the
   * complete set, and any entries previously in the call tree will be
   * overwritten.
   *
   * @param xiPhone      the call tree's number
   * @param xiServer     the web services server and port in the form
   *                     servername:port
   * @param xiExtensions the list of extensions
   * @param xiNumbers    the list of numbers (corresponding entries must be at
   *                     the same index in the array as for the list of
   *                     extensions)
   * @param xiFirstNames the list of first names - as above, these must be at
   *                     the same index in the array as the corresponding
   *                     extension
   * @param xiLastNames  the list of first names - as above, these must be at
   *                     the same index in the array as the corresponding
   *                     extension
   * @return the {@link ShUpdateResponse}
   * @throws Exception
   */
  public ShUpdateResponse createDialByExtensionList(String xiPhone,
                                                    String xiServer,
                                                    String[] xiExtensions,
                                                    String[] xiNumbers,
                                                    String[] xiFirstNames,
                                                    String[] xiLastNames)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Set up the data objects to pass into the update.
    //
    // Check that there are the same number of extensions, numbers and names.
    //-------------------------------------------------------------------------
    int num = xiExtensions.length;
    assert(xiNumbers.length == num);
    assert(xiFirstNames.length == num);
    assert(xiLastNames.length == num);

    //-------------------------------------------------------------------------
    // Create an array of dial entries
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_DialByExtensionList_ExtensionListEntry[] exts =
     new TMsph_Subscriber_CallTree_DialByExtensionList_ExtensionListEntry[num];

    for (int ii = 0; ii < num; ii++)
    {
      exts[ii] =
        new TMsph_Subscriber_CallTree_DialByExtensionList_ExtensionListEntry();
      exts[ii].setExtension(xiExtensions[ii]);
      exts[ii].setNumber(xiNumbers[ii]);
      exts[ii].setFirstName(xiFirstNames[ii]);
      exts[ii].setLastName(xiLastNames[ii]);
    }

    //-------------------------------------------------------------------------
    // Set up the announcement data indication, and add this indication to the
    // MetaSphere data.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_DialByExtensionList ctExtsIndication =
      new TMsph_Subscriber_CallTree_DialByExtensionList();
    ctExtsIndication.setExtensionListEntry(exts);

    TMetaSphereData metaData = new TMetaSphereData();
    metaData.setMsph_Subscriber_CallTree_DialByExtensionList(ctExtsIndication);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                               "Msph_Subscriber_CallTree_DialByExtensionList");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    ShUpdateResponse response = UtilitiesSample.doShUpdate(xiServer, update);

    return response;
  }

  /**
   * Set the base information for the call tree.
   * <p>
   * When a call tree is not active, all calls are forwarded to the specified
   * forwarding number.
   *
   * @param xiPhone            the call tree's number
   * @param xiServer           the web services server and port in the form
   *                           servername:port
   * @param xiActive           whether the call tree is active
   * @param xiForwardingNumber the call tree's forwarding number, to use when
   *                           the tree is not active,
   * @return the {@link ShUpdateResponse}
   * @throws Exception
   */
  public ShUpdateResponse setBaseInformation(String xiPhone,
                                             String xiServer,
                                             boolean xiActive,
                                             String xiForwardingNumber)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Set up the base information indication, set the active state and the
    // forwarding number, and add this indication to the MetaSphere data.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_BaseInformation ctBaseInfoIndication =
      new TMsph_Subscriber_CallTree_BaseInformation();
    ctBaseInfoIndication.setActive(xiActive ? TTrueFalse.True :
                                               TTrueFalse.False);
    ctBaseInfoIndication.setForwardingNumber(xiForwardingNumber);

    TMetaSphereData metaData = new TMetaSphereData();
    metaData.setMsph_Subscriber_CallTree_BaseInformation(ctBaseInfoIndication);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                   "Msph_Subscriber_CallTree_BaseInformation");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    ShUpdateResponse response = UtilitiesSample.doShUpdate(xiServer, update);

    return response;
  }

  /**
   * Delete a node from a call tree.
   *
   * @param xiPhone  the number of the call tree
   * @param xiServer the web services server and port in the form
   *                 servername:port
   * @param xiMenuID the ID of the menu node to delete
   * @return the {@link ShUpdateResponse}
   * @throws Exception
   */
  public ShUpdateResponse deleteNode(String xiPhone,
                                     String xiServer,
                                     String xiMenuID)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Set up the data objects to pass into the update.
    //
    // Create a menu node for the call tree - set the node ID and announcement
    // ID passed in as parameters.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Nodes_MenuNode[] nodes =
      new TMsph_Subscriber_CallTree_Nodes_MenuNode[1];
    nodes[0] = new TMsph_Subscriber_CallTree_Nodes_MenuNode();
    nodes[0].setNodeID(xiMenuID);

    //-------------------------------------------------------------------------
    // The announcement ID must be set, but it must not necessarily be correct,
    // since this is a delete operation.
    //-------------------------------------------------------------------------
    nodes[0].setAnnouncementID("");

    //-------------------------------------------------------------------------
    // Set up the nodes indication, and add this indication to the MetaSphere
    // data.
    //
    // Set the nodes indication to perform a delete action.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Nodes ctNodesIndication =
      new TMsph_Subscriber_CallTree_Nodes();
    ctNodesIndication.setMenuNode(nodes);
    ctNodesIndication.setAction("delete");

    TMetaSphereData metaData = new TMetaSphereData();
    metaData.setMsph_Subscriber_CallTree_Nodes(ctNodesIndication);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_CallTree_Nodes");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    ShUpdateResponse response = UtilitiesSample.doShUpdate(xiServer, update);

    return response;
  }

  /**
   * Delete an announcement from a call tree.
   * <p>
   * This will also delete the announcement data for the specified
   * announcement.
   *
   * @param xiPhone          the number of the call tree
   * @param xiServer         the web services server and port in the form
   *                         servername:port
   * @param xiAnnouncementID the ID of the announcement to delete
   * @return the {@link ShUpdateResponse}
   * @throws Exception
   */
  public ShUpdateResponse deleteAnnouncement(String xiPhone,
                                             String xiServer,
                                             String xiAnnouncementID)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Set up the data objects to pass into the update.
    //
    // Create an announcement, with the ID specified.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Announcements_Announcement[] anns =
      new TMsph_Subscriber_CallTree_Announcements_Announcement[1];
    anns[0] = new TMsph_Subscriber_CallTree_Announcements_Announcement();
    anns[0].setAnnouncementID(xiAnnouncementID);

    //-------------------------------------------------------------------------
    // Set up the announcement indication, and add this indication to the
    // MetaSphere data.
    //
    // Set the announcement indication to perform a delete action.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Announcements ctAnnsIndication =
      new TMsph_Subscriber_CallTree_Announcements();
    ctAnnsIndication.setAnnouncement(anns);
    ctAnnsIndication.setAction("delete");

    TMetaSphereData metaData = new TMetaSphereData();
    metaData.setMsph_Subscriber_CallTree_Announcements(ctAnnsIndication);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_CallTree_Announcements");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    ShUpdateResponse response = UtilitiesSample.doShUpdate(xiServer, update);

    return response;
  }

  /**
   * Delete an announcement data from a call tree.
   *
   * @param xiPhone          the number of the call tree
   * @param xiServer         the web services server and port in the form
   *                         servername:port
   * @param xiAnnouncementID the ID of the announcement to delete the data of
   * @return the {@link ShUpdateResponse}
   * @throws Exception
   */
  public ShUpdateResponse deleteAnnouncementData(String xiPhone,
                                                 String xiServer,
                                                 String xiAnnouncementID)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Set up the data objects to pass into the update.
    //
    // Create an announcement, with the ID specified.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Announcements_Announcement[] anns =
      new TMsph_Subscriber_CallTree_Announcements_Announcement[1];
    anns[0] = new TMsph_Subscriber_CallTree_Announcements_Announcement();
    anns[0].setAnnouncementID(xiAnnouncementID);

    //-------------------------------------------------------------------------
    // Set up the data objects to pass into the update.
    //
    // Create an announcement data, for the announcement ID specified.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_AnnouncementsData_AnnouncementData[] annData =
      new TMsph_Subscriber_CallTree_AnnouncementsData_AnnouncementData[1];
    annData[0] =
      new TMsph_Subscriber_CallTree_AnnouncementsData_AnnouncementData();
    annData[0].setAnnouncementID(xiAnnouncementID);

    //-------------------------------------------------------------------------
    // Set up the announcement data indication, and add this indication to the
    // MetaSphere data.
    //
    // Set the announcement data indication to perform a delete action.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_AnnouncementsData ctAnnDataIndication =
      new TMsph_Subscriber_CallTree_AnnouncementsData();
    ctAnnDataIndication.setAnnouncementData(annData);
    ctAnnDataIndication.setAction("delete");

    TMetaSphereData metaData = new TMetaSphereData();
    metaData
           .setMsph_Subscriber_CallTree_AnnouncementsData(ctAnnDataIndication);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                 "Msph_Subscriber_CallTree_AnnouncementsData");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    ShUpdateResponse response = UtilitiesSample.doShUpdate(xiServer, update);

    return response;
  }

  /**
   * Delete the entire dial list from a call tree.
   *
   * @param xiPhone  the number of the call tree
   * @param xiServer the web services server and port in the form
   *                 servername:port
   * @return the {@link ShUpdateResponse}
   * @throws Exception
   */
  public ShUpdateResponse deleteDialList(String xiPhone,
                                         String xiServer)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Deleting the entire dial list for a call tree is done by setting an
    // empty dial list.
    //-------------------------------------------------------------------------
    return createDialByExtensionList(xiPhone,
                                     xiServer,
                                     new String[0],
                                     new String[0],
                                     new String[0],
                                     new String[0]);
  }

  /**
   * Configure the schedule entry points for a call tree.
   *
   * @param xiPhone       The number of the call tree.
   * @param xiServer      The web services server and port in the form
   *                      <code>servername:port</code>
   * @param xiEntryPoints A Map of schedule IDs and node IDs to set as the
   *                      entry points. Each entry in the map corresponds to
   *                      a single entry point, using the schedule ID as the
   *                      key and the node ID as the value in the map.
   *
   * @return The {@link ShUpdateResponse} from the update.
   *
   * @throws Exception
   */
  public ShUpdateResponse configureScheduleEntryPoints(
                                         String xiPhone,
                                         String xiServer,
                                         HashMap<String, String> xiEntryPoints)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Set up the data objects to pass into the update.
    //
    // We want to set all the supplied entry points in the subscribers calltree
    // settings.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Settings_ScheduleEntryPoints entryPoints =
      new TMsph_Subscriber_CallTree_Settings_ScheduleEntryPoints();

    for (String scheduleID : xiEntryPoints.keySet())
    {
      String nodeID = xiEntryPoints.get(scheduleID);
      TMsph_Subscriber_CallTree_Settings_ScheduleEntryPoints_ScheduleEntryPoint point =
        new TMsph_Subscriber_CallTree_Settings_ScheduleEntryPoints_ScheduleEntryPoint();
      point.setScheduleID(scheduleID);
      point.setNodeID(nodeID);
      entryPoints.addScheduleEntryPoint(point);
    }

    //-------------------------------------------------------------------------
    // Set up the settings indication, and add this indication to the
    // MetaSphere data.
    //
    // Set the settings indication to perform an update action.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Settings settings =
      new TMsph_Subscriber_CallTree_Settings();
    settings.setScheduleEntryPoints(entryPoints);
    settings.setAction("update");

    TMetaSphereData metaData = new TMetaSphereData();
    metaData.setMsph_Subscriber_CallTree_Settings(settings);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_CallTree_Settings");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    ShUpdateResponse response = UtilitiesSample.doShUpdate(xiServer, update);

    return response;
  }

  /**
   * Configure the error scenarios and actions to take for a call tree.
   * <p>
   * This method configures some illustrative error handling for the specified
   * call tree:
   * <ul>
   * <li> Hang up after 2 failed call transfers
   * <li> Replay the current menu after 3 incorrect extensions have been
   *      entered by the caller
   * <li> Return to the previous menu after 5 timeouts
   * <li> Replay the current menu after 4 invalid actions have been entered by
   *      the caller
   * </ul>
   *
   * @param xiPhone  The number of the call tree.
   * @param xiServer The web services server and port in the form
   *                 <code>servername:port</code>
   *
   * @return The {@link ShUpdateResponse} from the update.
   *
   * @throws Exception
   */
  public ShUpdateResponse configureCallTreeErrors(String xiPhone,
                                                  String xiServer)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Set up the data objects to pass into the update.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Settings_Errors errors =
      new TMsph_Subscriber_CallTree_Settings_Errors();

    //-------------------------------------------------------------------------
    // Failed call transfers: if we hit this error twice, hang up the call.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Settings_Errors_Error error =
      new TMsph_Subscriber_CallTree_Settings_Errors_Error();
    error.setErrorType(
      TMsph_Subscriber_CallTree_Settings_Errors_Error_ErrorType.CallTransferFailed);
    error.setErrorCount(2);

    TAction action = new TAction();
    action.setHangUp(new TAction_HangUp());

    error.setAction(action);
    errors.addError(error);

    //-------------------------------------------------------------------------
    // Invalid extension: after 3 occurrences, replay the menu.
    //-------------------------------------------------------------------------
    error = new TMsph_Subscriber_CallTree_Settings_Errors_Error();
    error.setErrorType(
      TMsph_Subscriber_CallTree_Settings_Errors_Error_ErrorType.InvalidExtension);
    error.setErrorCount(3);
    action = new TAction();
    action.setReplayMenu(new TAction_ReplayMenu());
    error.setAction(action);
    errors.addError(error);

    //-------------------------------------------------------------------------
    // Timeout: after 5 occurrences, return to the previous menu.
    //-------------------------------------------------------------------------
    error = new TMsph_Subscriber_CallTree_Settings_Errors_Error();
    error.setErrorType(
      TMsph_Subscriber_CallTree_Settings_Errors_Error_ErrorType.Timeout);
    error.setErrorCount(5);
    action = new TAction();
    action.setReturnFromMenu(new TAction_ReturnFromMenu());
    error.setAction(action);
    errors.addError(error);

    //-------------------------------------------------------------------------
    // Unknown input from caller: after 4 occurrences, replay the menu.
    //-------------------------------------------------------------------------
    error = new TMsph_Subscriber_CallTree_Settings_Errors_Error();
    error.setErrorType(
      TMsph_Subscriber_CallTree_Settings_Errors_Error_ErrorType.UnknownInput);
    error.setErrorCount(4);
    action = new TAction();
    action.setReplayMenu(new TAction_ReplayMenu());
    error.setAction(action);
    errors.addError(error);

    //-------------------------------------------------------------------------
    // Set up the settings indication, and add this indication to the
    // MetaSphere data.
    //
    // Set the settings indication to perform an update action.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Settings settings =
      new TMsph_Subscriber_CallTree_Settings();
    settings.setErrors(errors);
    settings.setAction("update");

    TMetaSphereData metaData = new TMetaSphereData();
    metaData.setMsph_Subscriber_CallTree_Settings(settings);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_CallTree_Settings");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    ShUpdateResponse response = UtilitiesSample.doShUpdate(xiServer, update);

    return response;
  }

  /**
   * Configure the default keys and actions for a call tree.
   * <p>
   * This method configures some illustrative default keys for the specified
   * call tree:
   * <ul>
   * <li> Default 0 to 'transfer to operator'.
   * <li> Default * to 'go to previous menu'.
   * </ul>
   *
   * @param xiPhone  The number of the call tree.
   * @param xiServer The web services server and port in the form
   *                 <code>servername:port</code>
   *
   * @return The {@link ShUpdateResponse} from the update.
   *
   * @throws Exception
   */
  public ShUpdateResponse configureDefaultKeys(String xiPhone,
                                               String xiServer)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiPhone);

    //-------------------------------------------------------------------------
    // Set up the data objects to pass into the update.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Settings_DefaultKeys defaults =
      new TMsph_Subscriber_CallTree_Settings_DefaultKeys();

    //-------------------------------------------------------------------------
    // Default '0' to 'transfer to operator'.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Nodes_MenuNode_Result result =
      new TMsph_Subscriber_CallTree_Nodes_MenuNode_Result();
    TAction action = new TAction();

    action.setOperatorTransfer(new TAction_OperatorTransfer());
    result.setAction(action);
    result.setEvent("0");

    defaults.addDefaultKey(result);

    //-------------------------------------------------------------------------
    // Default '*' to 'go to previous menu'.
    //-------------------------------------------------------------------------
    result = new TMsph_Subscriber_CallTree_Nodes_MenuNode_Result();
    action = new TAction();

    action.setReturnFromMenu(new TAction_ReturnFromMenu());
    result.setAction(action);
    result.setEvent("*");

    defaults.addDefaultKey(result);

    //-------------------------------------------------------------------------
    // Set up the settings indication, and add this indication to the
    // MetaSphere data.
    //
    // Set the settings indication to perform an update action.
    //-------------------------------------------------------------------------
    TMsph_Subscriber_CallTree_Settings settings =
      new TMsph_Subscriber_CallTree_Settings();
    settings.setDefaultKeys(defaults);
    settings.setAction("update");

    TMetaSphereData metaData = new TMetaSphereData();
    metaData.setMsph_Subscriber_CallTree_Settings(settings);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_CallTree_Settings");

    //-------------------------------------------------------------------------
    // Issue the request
    //-------------------------------------------------------------------------
    ShUpdateResponse response = UtilitiesSample.doShUpdate(xiServer, update);

    return response;
  }
}