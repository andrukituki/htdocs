//-----------------------------------------------------------------------------
// DeleteAGCLine
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
//
// Sample application demonstrating the SOAP Sh interface to the MetaView
// Server.  This application uses Sh-Pull to get an AGC line's current base
// settings, and then sends an Sh-Update with an instruction to delete the
// line.  It uses Apache Axis2 and the "untyped" WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------

import javax.xml.soap.SOAPException;

import org.apache.axiom.om.OMElement;

import com.MetaSwitch.EMS.SOAP.ShPull;
import com.MetaSwitch.EMS.SOAP.ShPullResponse;
import com.MetaSwitch.EMS.SOAP.ShUpdate;
import com.MetaSwitch.EMS.SOAP.ShUpdateResponse;
import com.MetaSwitch.EMS.SOAP.ShService;
import com.MetaSwitch.EMS.SOAP.ShServiceStub;
import com.MetaSwitch.EMS.SOAP.TUserData;

public class DeleteAGCLine
{
  private static final String USAGE = "Usage: DeleteAGCLine " +
                                      "<Directory number>";

  private static ShUntypedUtilities utilities = new ShUntypedUtilities();

  public static void main(String[] args)
  {
    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      // The only argument is the AGC line's directory number - which is
      // required.
      //-----------------------------------------------------------------------
      if (args.length != 1)
      {
        throw new WrongParametersException("Wrong number of parameters.");
      }

      String dn = args[0];

      //-----------------------------------------------------------------------
      // Connect to the Sh service and send in a Pull request.  Check that it
      // succeeds.
      //-----------------------------------------------------------------------
      ShService shService = new ShServiceStub();
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
      utilities.handleMetaSwitchException(e, USAGE);
    }
    catch (Exception e)
    {
      utilities.handleUnexpectedException(e, USAGE, false);
    }
  }
}
