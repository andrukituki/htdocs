//-----------------------------------------------------------------------------
// DeleteAGCLine
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material.
//
// Sample application demonstrating the Network Wide Web Services API.
//
// This application uses Sh-Pull to get an AGC line's current base settings,
// and then sends an Sh-Update with an instruction to delete the line.  It uses
// Apache Axis2 and the "untyped" WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------
import org.apache.axiom.om.OMElement;

import com.MetaSwitch.SRB.SOAP.ShPull;
import com.MetaSwitch.SRB.SOAP.ShPullResponse;
import com.MetaSwitch.SRB.SOAP.ShUpdate;
import com.MetaSwitch.SRB.SOAP.ShUpdateResponse;
import com.MetaSwitch.SRB.SOAP.ShService;
import com.MetaSwitch.SRB.SOAP.ShServiceStub;
import com.MetaSwitch.SRB.SOAP.TUserData;

public class DeleteAGCLine
{
  private static final String USAGE = "Usage: DeleteAGCLine " +
                                      "<NWSAP username> " +
                                      "<NWSAP password> " +
                                      "<Directory number>";

  private static ShUntypedUtilities utilities = new ShUntypedUtilities();

  public static void main(String[] args)
  {
    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      //-----------------------------------------------------------------------
      if (args.length != 3)
      {
        throw new WrongParametersException("Wrong number of parameters.");
      }

      String nwsapUsername = args[0];
      String nwsapPassword = args[1];
      String dn = args[2];

      //-----------------------------------------------------------------------
      // Connect to the Sh service and send in a Pull request.  Check that it
      // succeeds.
      //-----------------------------------------------------------------------
      ShServiceStub shService = new ShServiceStub();

      utilities.authenticate(shService,
                             nwsapUsername,
                             nwsapPassword);

      ShPull shPullRequest = utilities.createPullRequest(
                                                dn, 
                                                0,
                                                "Meta_AGCLine_BaseInformation",
                                                utilities.ORIGIN_HOST);
      
      ShPullResponse shPullResponse = shService.shPull(shPullRequest);
      TUserData userData = shPullResponse.getUserData();

      utilities.checkResultCode(shPullResponse.getResultCode().getTResultCode(),
                                shPullResponse.getExtendedResult(),
                                userData);

      //-----------------------------------------------------------------------
      // Update the user data:
      // -  Update the sequence number.
      // -  Remove all the fields.
      // -  Add the action attribute to the field group element.
      //-----------------------------------------------------------------------
      utilities.incrementSequenceNumber(userData);

      OMElement fieldGroup = utilities.getFieldGroupElement(userData);
      fieldGroup.removeChildren();
      fieldGroup.addAttribute("Action", "delete", null);

      //-----------------------------------------------------------------------
      // Make the update request and check it succeeds.
      //-----------------------------------------------------------------------
      ShUpdate shUpdateRequest = utilities.createUpdateRequest(
                                                        dn,
                                                        0,
                                                        userData,
                                                        utilities.ORIGIN_HOST);
      
      ShUpdateResponse shUpdateResponse = shService.shUpdate(shUpdateRequest);
      utilities.checkResultCode(shUpdateResponse.getResultCode().getTResultCode(),
                                shUpdateResponse.getExtendedResult(),
                                userData);

      System.out.println("AGC line " + dn + " successfully deleted.");
    }
    catch (MetaSwitchShInterfaceException e)
    {
      System.err.println(e.getMessage());
      System.err.println(USAGE);
    }
    catch (Exception e)
    {
      System.err.println("Unexpected error \"" + e
                         + "\" in retrieving data");
      e.printStackTrace(System.err);
      System.err.println(USAGE);
    }
  }
}
