//-----------------------------------------------------------------------------
// BusinessGroupSample.java
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
//-----------------------------------------------------------------------------
package dcl.wsd.test.fv.sample;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.metaswitch.www.sdp.soap.sh.ShPull;
import com.metaswitch.www.sdp.soap.sh.ShPullResponse;
import com.metaswitch.www.sdp.soap.sh.ShUpdate;
import com.metaswitch.www.sdp.soap.sh.ShUpdateResponse;
import com.metaswitch.www.sdp.soap.sh.servicedata.TBGCLSubscriber;
import com.metaswitch.www.sdp.soap.sh.servicedata.TIntercomCodeRange;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMetaSphereData;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_BusinessGroup_BaseInformation;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_BusinessGroup_ChildrenList_Subscriber;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_BusinessGroup_DialingPlan_IntercomCodeRangesList;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Department_BaseInformation;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_BusinessGroup;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_Subscriber_BusinessGroup_AccountType;
import com.metaswitch.www.sdp.soap.sh.servicedata.TSwitchableDefaultString;
import com.metaswitch.www.sdp.soap.sh.servicedata.TTrueFalse;

/**
 * Code sample: Business Group Provisioning operations
 *
 * These methods can be used to create new business group fragments, intercom
 * code ranges and departments. They can also be used to add subscribers to
 * business groups.
 *
 * This sample client was built using the Axis2 (version 1.4.1) wsdl2java
 * utility, ADB databinding and the --unpack-classes option.
 *
 * It is not guaranteed to work with other data bindings, client framework
 * utilities or client stub code generated using other versions of Axis2.
 */
public class BusinessGroupSample
{
  /**
   * This sample code creates a Business Group fragment and adds two intercom
   * code ranges (which are hardcoded in this example).
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiName of business group fragment to create.
   * @param xiOpNum the operator number to use for the entire business group
   *        fragment, can be overridden on a per-department basis.
   * @param xiExternalLineCode the external line code to use for this business
   *        group fragment.
   * @param xiNetworkID
   * @param xiRepNumByExt true if representing subscriber DNs as intercom
   *        codes.
   * @param xiRestrictSubsToSubs true if restricting subsriber to subscriber
   *        messaging to within the business group.
   * @param xiMasterAdminName master administrator.  This level of
   *        authentication is required to manipulate business groups.
   * @param xiMasterAdminPassword password
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse provisionBGFragment(String xiServer,
                                              String xiName,
                                              String xiOpNum,
                                              String xiExternalLineCode,
                                              String xiNetworkID,
                                              boolean xiRepNumByExt,
                                              boolean xiRestrictSubsToSubs,
                                              String xiMasterAdminName,
                                              String xiMasterAdminPassword)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // URL encode the BG name so that we can use it in the user identity field.
    //-------------------------------------------------------------------------
    String fullnameurl;

    try
    {
      fullnameurl = URLEncoder.encode(xiName, "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      //-----------------------------------------------------------------------
      // This won't happen unless Java stops recognising UTF-8 as a
      // valid encoding.
      //-----------------------------------------------------------------------
      throw new RuntimeException("URLDecoder doesn't support UTF-8");
    }

    //-------------------------------------------------------------------------
    // Create the update operation
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(fullnameurl);

    //-------------------------------------------------------------------------
    // This operation requires master administrator credentials - alter the
    // origin host accordingly.
    //-------------------------------------------------------------------------
    String originHost = "server@domain" +
                        "?clientVersion=1.0" +
                        "&adminName=" + xiMasterAdminName +
                        "&password=" + xiMasterAdminPassword +
                        "&ignoreSequenceNumber=true";
    update.setOriginHost(originHost);

    //-------------------------------------------------------------------------
    // Populate the business group information
    //-------------------------------------------------------------------------
    TMetaSphereData metaDataBG = new TMetaSphereData();
    TMsph_BusinessGroup_BaseInformation bgBaseInfo =
      new TMsph_BusinessGroup_BaseInformation();
    metaDataBG.setMsph_BusinessGroup_BaseInformation(bgBaseInfo);

    bgBaseInfo.setBusinessGroupName(xiName);
    bgBaseInfo.setOperatorNumber(xiOpNum);
    bgBaseInfo.setExternalLineCode(xiExternalLineCode);
    bgBaseInfo.setNetworkID(xiNetworkID);
    bgBaseInfo.setAction("apply");

    if (xiRepNumByExt)
    {
      bgBaseInfo.setRepresentCallingNumberByExtension(TTrueFalse.True);
    }
    else
    {
      bgBaseInfo.setRepresentCallingNumberByExtension(TTrueFalse.False);
    }

    if (xiRestrictSubsToSubs)
    {
      bgBaseInfo.setRestrictSubscriberToSubscriberMessaging(TTrueFalse.True);
    }
    else
    {
      bgBaseInfo.setRestrictSubscriberToSubscriberMessaging(TTrueFalse.False);
    }

    //-------------------------------------------------------------------------
    // Populate two intercom code ranges. Start with a MetaSphereData object.
    //-------------------------------------------------------------------------
    TMetaSphereData metaDataIC = new TMetaSphereData();
    TMsph_BusinessGroup_DialingPlan_IntercomCodeRangesList bgICInfo =
      new TMsph_BusinessGroup_DialingPlan_IntercomCodeRangesList();
    metaDataIC.
            setMsph_BusinessGroup_DialingPlan_IntercomCodeRangesList(bgICInfo);

    //-------------------------------------------------------------------------
    // Now create intercom code objects for each of the ranges and add them to
    // the MetaSphere data.
    //-------------------------------------------------------------------------
    TIntercomCodeRange range1 = new TIntercomCodeRange();
    range1.setFirstCode("100");
    range1.setLastCode("199");
    range1.setFirstDirectoryNumber("1000000100");

    TIntercomCodeRange range2 = new TIntercomCodeRange();
    range2.setFirstCode("200");
    range2.setLastCode("299");
    range2.setFirstDirectoryNumber("1000000200");

    bgICInfo.setAction("apply");
    bgICInfo.addIntercomCodeRange(range1);
    bgICInfo.addIntercomCodeRange(range2);

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaDataBG,
                                    "Msph_BusinessGroup_BaseInformation");
    UtilitiesSample.addDataToUpdate(update,
                                    metaDataIC,
                      "Msph_BusinessGroup_DialingPlan_IntercomCodeRangesList");

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
   * This sample code deletes a MetaSphere business group fragment. This will
   * also delete all intercom code ranges, departments and subscribers that
   * belong to this business group fragment.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiName the name of the business group fragment.
   * @param xiMasterAdminName master administrator.  This level of
   *        authentication is required to manipulate business groups.
   * @param xiMasterAdminPassword password
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse deleteBGFragment(String xiServer,
                                           String xiName,
                                           String xiMasterAdminName,
                                           String xiMasterAdminPassword)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // URL encode the BG name so that we can use it in the user identity field.
    //-------------------------------------------------------------------------
    String fullnameurl;

    try
    {
      fullnameurl = URLEncoder.encode(xiName, "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      //-----------------------------------------------------------------------
      // This won't happen unless Java stops recognising UTF-8 as a
      // valid encoding.
      //-----------------------------------------------------------------------
      throw new RuntimeException("URLDecoder doesn't support UTF-8");
    }

    //-------------------------------------------------------------------------
    // Create the update operation
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(fullnameurl);

    //-------------------------------------------------------------------------
    // This operation requires master administrator credentials - alter the
    // origin host accordingly.
    //-------------------------------------------------------------------------
    String originHost = "server@domain" +
                        "?clientVersion=1.0" +
                        "&adminName=" + xiMasterAdminName +
                        "&password=" + xiMasterAdminPassword +
                        "&ignoreSequenceNumber=true";
    update.setOriginHost(originHost);

    //-------------------------------------------------------------------------
    // Create a minimal base information object, containing just the bg
    // fragment name and the "delete" action.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();

    TMsph_BusinessGroup_BaseInformation bgBaseInfo =
                                     new TMsph_BusinessGroup_BaseInformation();
    metaData.setMsph_BusinessGroup_BaseInformation(bgBaseInfo);

    bgBaseInfo.setBusinessGroupName(fullnameurl);
    bgBaseInfo.setAction("delete");

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_BusinessGroup_BaseInformation");

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
   * This sample code provisions a new department for an existing
   * business group fragment.
   *
   * Departments are described by their name and the full name of their parent.
   * The full name of a parent at the top level is "None". The full name of a
   * department two levels below the top level may be
   * "Top Department / Child Department".
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiBGName the BG fragment the department will be added to
   * @param xiDeptName the name of the BG department
   * @param xiParentFullName the full name of the parent of the BG department,
   *        or "None".
   * @param xiOperatorNumber the operator number, or null.
   * @param xiMasterAdminName master administrator.  This level of
   *        authentication is required to manipulate business groups.
   * @param xiMasterAdminPassword password
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse provisionDepartment(String xiServer,
                                              String xiBGName,
                                              String xiDeptName,
                                              String xiParentFullName,
                                              String xiOperatorNumber,
                                              String xiMasterAdminName,
                                              String xiMasterAdminPassword)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the appropriate user id, consisting of the following:
    //   <URLencodedBGname>/<URLencodedFullDeptName>
    // Where FullDeptName consists of:
    //   <xiParentFullName> / <xiDeptName>
    // or if there is no parent:
    //   <xiDeptName>
    //-------------------------------------------------------------------------
    String lFullName = "";

    if (!xiParentFullName.equals("None"))
    {
      lFullName = xiParentFullName + "/";
    }

    lFullName += xiDeptName;

    String bgnameurl = null;
    String deptnameurl = null;

    try
    {
      bgnameurl = URLEncoder.encode(xiBGName, "UTF-8");
      deptnameurl = URLEncoder.encode(lFullName, "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      //-----------------------------------------------------------------------
      // This will only happen if Java stops recognising UTF-8 as a
      // valid encoding.
      //-----------------------------------------------------------------------
      throw new RuntimeException("URLDecoder doesn't support UTF-8");
    }

    //-------------------------------------------------------------------------
    // Create the update operation
    //-------------------------------------------------------------------------
    ShUpdate update =
                  UtilitiesSample.buildShUpdate(bgnameurl + "/" + deptnameurl);

    //-------------------------------------------------------------------------
    // This operation requires master administrator credentials - alter the
    // origin host accordingly.
    //-------------------------------------------------------------------------
    String originHost = "server@domain" +
                        "?clientVersion=1.0" +
                        "&adminName=" + xiMasterAdminName +
                        "&password=" + xiMasterAdminPassword +
                        "&ignoreSequenceNumber=true";
    update.setOriginHost(originHost);

    //-------------------------------------------------------------------------
    // Create a department base information object.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();
    TMsph_Department_BaseInformation dept =
                                        new TMsph_Department_BaseInformation();

    dept.setAction("apply");
    dept.setName(xiDeptName);
    dept.setParentDepartment(xiParentFullName);

    if (xiOperatorNumber != null)
    {
      TSwitchableDefaultString opnum = new TSwitchableDefaultString();
      opnum.setValue(xiOperatorNumber);
      dept.setOperatorNumber(opnum);
    }

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    metaData.setMsph_Department_BaseInformation(dept);
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Department_BaseInformation");

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
   * This sample code adds/removes an existing subscriber to/from an
   * existing business group fragment. Removing a subscriber from a BG leaves
   * them provisioned on the system.
   *
   * Departments are described by their full name, such as
   * "My Corp / My Parent Department / My Department".
   * Unless department name is "None" the department must already exist.
   *
   * Admin departments are also described by their full name. Unless
   * department name is "Whole Business Group" the department must already
   * exist. Admin department must either be the same as department, or a parent
   * of department. So given the department above, the following are valid
   * values for admin department:
   * "Whole Business Group"
   * "My Corp"
   * "My Corp / My Parent Department"
   * "My Corp / My Parent Department / My Department"
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiSubscriberDN the subscriber's directory number
   * @param xiBGName the BG fragment the department will be added to
   * @param xiFullDeptName the full department name or "None" if the subscriber
   *        is not a member of any department.
   * @param xiAdministrator true if subscriber is a business group
   *        administrator.
   * @param xiFullAdminDeptName name of the department the subscriber
   *        administers, or "Whole Business Group" if they administer the
   *        entire fragment.
   * @param xiMasterAdminName master administrator.  This level of
   *        authentication is required to manipulate business groups.
   * @param xiMasterAdminPassword password
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse provisionBGSubscriber(String xiServer,
                                                String xiSubscriberDN,
                                                String xiBGName,
                                                String xiFullDeptName,
                                                boolean xiAdministrator,
                                                String xiFullAdminDeptName,
                                                String xiMasterAdminName,
                                                String xiMasterAdminPassword)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiSubscriberDN);

    //-------------------------------------------------------------------------
    // This operation requires master administrator credentials - alter the
    // origin host accordingly.
    //-------------------------------------------------------------------------
    String originHost = "server@domain" +
                        "?clientVersion=1.0" +
                        "&adminName=" + xiMasterAdminName +
                        "&password=" + xiMasterAdminPassword +
                        "&ignoreSequenceNumber=true";
    update.setOriginHost(originHost);

    //-------------------------------------------------------------------------
    // Create a subscriber business group base information object.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();
    TMsph_Subscriber_BusinessGroup subs = new TMsph_Subscriber_BusinessGroup();

    subs.setAction("apply");
    subs.setBusinessGroup(xiBGName);

    if (!xiBGName.equals(""))
    {
      //-----------------------------------------------------------------------
      // We're not removing the subscriber from the BG fragment, so set up the
      // other fields too.
      //-----------------------------------------------------------------------
      subs.setDepartment(xiFullDeptName);

      if (xiAdministrator)
      {
        subs.setAccountType(
                     TMsph_Subscriber_BusinessGroup_AccountType.Administrator);
        subs.setAdministrationDepartment(xiFullAdminDeptName);
      }
      else
      {
        //---------------------------------------------------------------------
        // Admin Dept always defaults to "Whole Business Group", even if
        // subscriber is not an admin.
        //---------------------------------------------------------------------
        subs.setAccountType(TMsph_Subscriber_BusinessGroup_AccountType.Normal);
        subs.setAdministrationDepartment("Whole Business Group");
      }
    }

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    metaData.setMsph_Subscriber_BusinessGroup(subs);
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_Subscriber_BusinessGroup");

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
   * This sample code lists all the members of an existing MetaSphere business
   * group fragment.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiName the name of the business group fragment.
   * @param xiMasterAdminName master administrator.  This level of
   *        authentication is required to manipulate business groups.
   * @param xiMasterAdminPassword password
   *
   * @return the array of subscriber objects returned, or null if there were
   * none.
   *
   * @throws Exception
   */
  public TBGCLSubscriber[] listBGFragmentSubs(String xiServer,
                                              String xiName,
                                              String xiMasterAdminName,
                                              String xiMasterAdminPassword)
    throws Exception
  {
    String fullnameurl;

    try
    {
      fullnameurl = URLEncoder.encode(xiName, "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      //-----------------------------------------------------------------------
      // This will not happen unless Java stops recognising UTF-8 as a
      // valid encoding.
      //-----------------------------------------------------------------------
      throw new RuntimeException("URLDecoder doesn't support UTF-8");
    }

    //-------------------------------------------------------------------------
    // Create the pull operation
    //-------------------------------------------------------------------------
    ShPull pull = UtilitiesSample.buildShPull(fullnameurl,
                                 "Msph_BusinessGroup_ChildrenList_Subscriber");

    //-------------------------------------------------------------------------
    // This operation requires master administrator credentials - alter the
    // origin host accordingly.
    //-------------------------------------------------------------------------
    String originHost = "server@domain" +
                        "?clientVersion=1.0" +
                        "&adminName=" + xiMasterAdminName +
                        "&password=" + xiMasterAdminPassword +
                        "&ignoreSequenceNumber=true";
    pull.setOriginHost(originHost);

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
    // Extract the array of subscriber elements from the result. It is safe to
    // get repository data for the zeroth element because we know there was
    // only a single indication on this pull and the pull succeeded.
    //-------------------------------------------------------------------------
    TMsph_BusinessGroup_ChildrenList_Subscriber pulledData = response.
    getUserData().
    getShData().
    getRepositoryData()[0].
    getServiceData().
    getMetaSphereData().getMsph_BusinessGroup_ChildrenList_Subscriber();

    TBGCLSubscriber[] subs = pulledData.getSubscriber();

    return subs;
  }
}