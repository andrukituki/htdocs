//-----------------------------------------------------------------------------
// AdminSample.java
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
//-----------------------------------------------------------------------------
package dcl.wsd.test.fv.sample;

import com.metaswitch.www.sdp.soap.sh.ShUpdate;
import com.metaswitch.www.sdp.soap.sh.ShUpdateResponse;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMetaSphereData;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_ClassOfService;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_ClassOfService_CallRouterRole;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_ClassOfService_Categorization;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_ClassOfService_SubscriberRole;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_CustomerGroup;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_CustomerGroup_Administrator_Type;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_CustomerGroup_Administrators;
import com.metaswitch.www.sdp.soap.sh.servicedata.TMsph_CustomerGroup_Administrators_Administrator;
import com.metaswitch.www.sdp.soap.sh.servicedata.TSwitchableDefaultMultiValuedEmailDomain;
import com.metaswitch.www.sdp.soap.sh.servicedata.TTrueFalse;

/**
 * Code sample: Administrative operations
 *
 * These methods can be used to create new customer groups and classes of
 * service.
 *
 * This sample client was built using the Axis2 (version 1.4.1) wsdl2java
 * utility, ADB databinding and the --unpack-classes option.
 *
 * It is not guaranteed to work with other data bindings, client framework
 * utilities or client stub code generated using other versions of Axis2.
 */
public class AdminSample
{
  /**
   * This sample code creates a Customer Group.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiDomain email domain(s) for the group
   * @param xiGroupName name for the new group
   * @param xiMasterAdminName master administrator.  This level of
   *        authentication is required to manipulate customer groups.
   * @param xiMasterAdminPassword password
   * @return the result of the ShUpdate operation
   * @throws Exception
   */
  public ShUpdateResponse createCustomerGroup(
                             String xiServer,
                             String[] xiDomain,
                             String xiGroupName,
                             String xiMasterAdminName,
                             String xiMasterAdminPassword)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiGroupName);

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
    // Populate the customer group indication.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();

    TMsph_CustomerGroup custGroup = new TMsph_CustomerGroup();
    metaData.setMsph_CustomerGroup(custGroup);

    TSwitchableDefaultMultiValuedEmailDomain domain =
                                new TSwitchableDefaultMultiValuedEmailDomain();
    domain.setValue(xiDomain);
    custGroup.setEmailDomain(domain);
    custGroup.setGroupName(xiGroupName);
    custGroup.setAction("apply");

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_CustomerGroup");

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
   * This sample code deletes a Customer Group.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiGroupName the name of the customer group to remove.
   * @param xiMasterAdminName master administrator
   * @param xiMasterAdminPassword password
   * @param xiDomain email domain
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse deleteCustomerGroup(String xiServer,
                                              String[] xiDomain,
                                              String xiGroupName,
                                              String xiMasterAdminName,
                                              String xiMasterAdminPassword)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiGroupName);

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
    // Populate the customer group indication.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();

    TMsph_CustomerGroup custGroup = new TMsph_CustomerGroup();
    metaData.setMsph_CustomerGroup(custGroup);

    TSwitchableDefaultMultiValuedEmailDomain domain =
      new TSwitchableDefaultMultiValuedEmailDomain();
    domain.setValue(xiDomain);
    custGroup.setEmailDomain(domain);
    custGroup.setGroupName(xiGroupName);
    custGroup.setAction("delete");

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_CustomerGroup");

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
   * This sample code creates a Customer Group Administrator.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiName name of the administrator
   * @param xiPassword password for the administrator
   * @param xiGroupName the name of the customer group to which the new
   *        administrator should be added.
   * @param xiMasterAdminName master administrator
   * @param xiMasterAdminPassword password
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse createCustomerGroupAdmin(
                            String xiServer,
                            String xiName,
                            String xiPassword,
                            String xiGroupName,
                            String xiMasterAdminName,
                            String xiMasterAdminPassword)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiGroupName);

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
    // Populate the customer group administrator indication.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();

    TMsph_CustomerGroup_Administrators custGroupAdmin =
                                      new TMsph_CustomerGroup_Administrators();

    TMsph_CustomerGroup_Administrators_Administrator admin =
                        new TMsph_CustomerGroup_Administrators_Administrator();

    admin.setAdministratorGroup(xiGroupName);
    admin.setAdministratorName(xiName);
    admin.setAdministratorPassword(xiPassword);
    admin.setAdministratorType(TMsph_CustomerGroup_Administrator_Type.Group);

    metaData.setMsph_CustomerGroup_Administrators(custGroupAdmin);

    custGroupAdmin.addAdministrator(admin);
    custGroupAdmin.setAction("apply");

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_CustomerGroup_Administrators");

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
   * This sample code deletes a Customer Group Administrator.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiName name of the administrator
   * @param xiGroupName the name of the customer group to which the new
   *        administrator should be added.
   * @param xiMasterAdminName master administrator
   * @param xiMasterAdminPassword password
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse deleteCustomerGroupAdmin(
                                                  String xiServer,
                                                  String xiName,
                                                  String xiGroupName,
                                                  String xiMasterAdminName,
                                                  String xiMasterAdminPassword)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiGroupName);

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
    // Populate the customer group administrator indication.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();

    TMsph_CustomerGroup_Administrators custGroupAdmin =
                                      new TMsph_CustomerGroup_Administrators();

    TMsph_CustomerGroup_Administrators_Administrator admin =
                        new TMsph_CustomerGroup_Administrators_Administrator();

    admin.setAdministratorGroup(xiGroupName);
    admin.setAdministratorName(xiName);

    metaData.setMsph_CustomerGroup_Administrators(custGroupAdmin);

    custGroupAdmin.addAdministrator(admin);
    custGroupAdmin.setAction("delete");

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_CustomerGroup_Administrators");

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
   * This sample code creates a Master Administrator.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiName name of the administrator
   * @param xiPassword password for the administrator
   * @param xiMasterAdminName master administrator
   * @param xiMasterAdminPassword password
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse createMasterAdministrator(
                            String xiServer,
                            String xiName,
                            String xiPassword,
                            String xiMasterAdminName,
                            String xiMasterAdminPassword)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation, using the special group value of
    // <masteradmin> to indicate that this is an operation on a master
    // administrator.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate("<masteradmin>");

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
    // Populate the customer group administrator indication.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();

    TMsph_CustomerGroup_Administrators custGroupAdmin =
                                      new TMsph_CustomerGroup_Administrators();

    TMsph_CustomerGroup_Administrators_Administrator admin =
                        new TMsph_CustomerGroup_Administrators_Administrator();

    //-------------------------------------------------------------------------
    // Again, the group name must take the special master admin value.
    //-------------------------------------------------------------------------
    admin.setAdministratorGroup("<masteradmin>");
    admin.setAdministratorName(xiName);
    admin.setAdministratorPassword(xiPassword);
    admin.setAdministratorType(TMsph_CustomerGroup_Administrator_Type.Master);

    metaData.setMsph_CustomerGroup_Administrators(custGroupAdmin);

    custGroupAdmin.addAdministrator(admin);
    custGroupAdmin.setAction("apply");

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_CustomerGroup_Administrators");

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
   * This sample code deletes a Master Administrator.
   *
   * Note that the original masteradmin account can never be deleted.  Also,
   * a master administrator cannot delete itself.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiName name of the administrator to delete
   * @param xiMasterAdminName master administrator credentials
   * @param xiMasterAdminPassword password
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse deleteMasterAdministrator(
                                                  String xiServer,
                                                  String xiName,
                                                  String xiMasterAdminName,
                                                  String xiMasterAdminPassword)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation, indicating that this is a special master
    // admin operation with no group.
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate("<masteradmin>");

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
    // Populate the customer group administrator indication.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();

    TMsph_CustomerGroup_Administrators custGroupAdmin =
                                      new TMsph_CustomerGroup_Administrators();

    TMsph_CustomerGroup_Administrators_Administrator admin =
                        new TMsph_CustomerGroup_Administrators_Administrator();

    //-------------------------------------------------------------------------
    // Group name must match the user identity field.
    //-------------------------------------------------------------------------
    admin.setAdministratorGroup("<masteradmin>");
    admin.setAdministratorName(xiName);

    metaData.setMsph_CustomerGroup_Administrators(custGroupAdmin);

    custGroupAdmin.addAdministrator(admin);
    custGroupAdmin.setAction("delete");

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_CustomerGroup_Administrators");

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
   * This sample code creates a Class of Service.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiName the name of the Class of Service to be added.
   * @param xiDescription descriptive name for the CoS
   * @param xiBusiness whether to set categorization to business rather than
   *                   home.
   * @param xiCallRouterRole call router role for the CoS
   * @param xiSubscriberRole subscriber role for the CoS. Must be one of:
   *                  primary, secondary, primaryorsecondary, individual.
   * @param xiCommPortalAllowed whether to enable CommPortal
   * @param xiMasterAdminName master administrator
   * @param xiMasterAdminPassword password
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse createClassOfService(
                          String xiServer,
                          boolean xiBusiness,
                          String xiCallRouterRole,
                          String xiDescription,
                          String xiSubscriberRole,
                          boolean xiCommPortalAllowed,
                          String xiName,
                          String xiMasterAdminName,
                          String xiMasterAdminPassword)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiName);

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
    // Populate the class of service indication.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();

    TMsph_ClassOfService cos = new TMsph_ClassOfService();
    metaData.setMsph_ClassOfService(cos);
    cos.setDescription(xiDescription);
    cos.setName(xiName);

    //-------------------------------------------------------------------------
    // Set categorization.
    //-------------------------------------------------------------------------
    if (xiBusiness)
    {
      cos.setCategorization(TMsph_ClassOfService_Categorization.business);
    }
    else
    {
      cos.setCategorization(TMsph_ClassOfService_Categorization.home);
    }

    //-------------------------------------------------------------------------
    // Set call router role, if a valid value is supplied.
    //-------------------------------------------------------------------------
    if (xiCallRouterRole.equalsIgnoreCase("none"))
    {
      cos.setCallRouterRole(TMsph_ClassOfService_CallRouterRole.none);
    }
    else if (xiCallRouterRole.equalsIgnoreCase("md"))
    {
      cos.setCallRouterRole(TMsph_ClassOfService_CallRouterRole.md);
    }
    else if (xiCallRouterRole.equalsIgnoreCase("linkable"))
    {
      cos.setCallRouterRole(TMsph_ClassOfService_CallRouterRole.linkable);
    }

    //-------------------------------------------------------------------------
    // Set subscriber role, if a valid value is supplied.
    //-------------------------------------------------------------------------
    if (xiSubscriberRole.equalsIgnoreCase("individual"))
    {
      cos.setSubscriberRole(TMsph_ClassOfService_SubscriberRole.individual);
    }
    else if (xiSubscriberRole.equalsIgnoreCase("primary"))
    {
      cos.setSubscriberRole(TMsph_ClassOfService_SubscriberRole.primary);
    }
    else if (xiSubscriberRole.equalsIgnoreCase("secondary"))
    {
      cos.setSubscriberRole(TMsph_ClassOfService_SubscriberRole.secondary);
    }
    else if (xiSubscriberRole.equalsIgnoreCase("primaryorsecondary"))
    {
      cos.setSubscriberRole(
                       TMsph_ClassOfService_SubscriberRole.primaryorsecondary);
    }

    //-------------------------------------------------------------------------
    // CommPortalAllowed is TTrueFalse.
    //-------------------------------------------------------------------------
    if (xiCommPortalAllowed)
    {
      cos.setCommPortalAllowed(TTrueFalse.True);
    }
    else
    {
      cos.setCommPortalAllowed(TTrueFalse.False);
    }
    cos.setAction("apply");

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_ClassOfService");

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
   * This sample code deletes a Class of Service.
   *
   * @param xiServer the web services server and port in the form
   *        servername:port
   * @param xiName the name of the Class of Service to be added.
   * @param xiMasterAdminName master administrator
   * @param xiMasterAdminPassword password
   *
   * @return the result of the ShUpdate operation
   *
   * @throws Exception
   */
  public ShUpdateResponse deleteClassOfService(String xiServer,
                                               String xiName,
                                               String xiMasterAdminName,
                                               String xiMasterAdminPassword)
    throws Exception
  {
    //-------------------------------------------------------------------------
    // Create the update operation
    //-------------------------------------------------------------------------
    ShUpdate update = UtilitiesSample.buildShUpdate(xiName);

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
    // Populate the class of service indication.
    //-------------------------------------------------------------------------
    TMetaSphereData metaData = new TMetaSphereData();

    TMsph_ClassOfService cos = new TMsph_ClassOfService();
    metaData.setMsph_ClassOfService(cos);

    cos.setName(xiName);
    cos.setAction("delete");

    //-------------------------------------------------------------------------
    // Add the MetaSphere data to the update operation under construction
    //-------------------------------------------------------------------------
    UtilitiesSample.addDataToUpdate(update,
                                    metaData,
                                    "Msph_ClassOfService");

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
