//-----------------------------------------------------------------------------
// UpdateSubscription
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material 
//
// Sample application demonstrating the SOAP Sh interface to the MetaView
// Server.  This application uses Sh-Pull to display a subscriber's current
// settings for a particular call service, and then sends an Sh-Update to
// change whether the subscriber is subscribed to that service, using the
// sequence number to avoid conflicting updates.  Finally it makes a second
// Sh-Pull request to display the new configuration.  It uses Apache Axis2 and
// the "untyped" WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------
 
import javax.xml.soap.SOAPException;

import com.MetaSwitch.EMS.SOAP.ShService;
import com.MetaSwitch.EMS.SOAP.ShServiceStub;
import com.MetaSwitch.EMS.SOAP.ShPull;
import com.MetaSwitch.EMS.SOAP.ShPullResponse;
import com.MetaSwitch.EMS.SOAP.ShUpdate;
import com.MetaSwitch.EMS.SOAP.ShUpdateResponse;
import com.MetaSwitch.EMS.SOAP.TUserData;
import org.apache.axiom.om.OMElement;

public class UpdateSubscription
{
  private static final String USAGE = "Usage: UpdateSubscription " +
                                      "<Directory number> " +
                                      "<Service indication> <subscribed | " +
                                      "unsubscribed | default>";
  private static final String TRUE = "True";
  private static final String FALSE = "False";

  private static ShUntypedUtilities utilities = new ShUntypedUtilities();

  public static void main(String[] args)
  {
    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      // Ther are three mandatory parameters:
      // -  the subscriber's DN
      // -  the call service
      // -  the new value for the subscription
      //-----------------------------------------------------------------------
      if (args.length != 3)
      {
        throw new WrongParametersException("Wrong number of parameters.");
      }

      String dn = args[0];
      String serviceIndication = "Meta_Subscriber_" + args[1];
      String subscriptionStatus = args[2].toLowerCase();

      if (!(subscriptionStatus.equals("subscribed")) &&
          !(subscriptionStatus.equals("unsubscribed")) &&
          !(subscriptionStatus.equals("default")))
      {
        throw new WrongParametersException("SubscribedField may only be one " +
                                           "of subscribed, unsubscribed or " +
                                           "default.");
      }

      //-----------------------------------------------------------------------
      // Connect to the Sh service and send in a Pull request.
      //-----------------------------------------------------------------------
      ShService shService = new ShServiceStub();

      ShPull shPullRequest = utilities.createPullRequest(dn,
                                                         0,
                                                         serviceIndication,
                                                         utilities.ORIGIN_HOST);
 
      ShPullResponse shPullResponse = shService.shPull(shPullRequest);
      TUserData userData = shPullResponse.getUserData();

      //-----------------------------------------------------------------------
      // Check whether the request succeeded and display the "before update"
      // value of each of the fields.
      //-----------------------------------------------------------------------
      utilities.checkResultCode(shPullResponse.getResultCode().getTResultCode(),
                                shPullResponse.getExtendedResult(),
                                userData);

      System.out.println("Old settings:");
      utilities.displayFields(userData);

      //-----------------------------------------------------------------------
      // Update the user data and send it back.  Check that the request
      // succeeded.
      //-----------------------------------------------------------------------
      utilities.incrementSequenceNumber(userData);

      updateSubscribedField(userData, subscriptionStatus);

      ShUpdate shUpdateRequest = utilities.createUpdateRequest(dn,
                                                               0,
                                                               userData,
                                                               utilities.ORIGIN_HOST);

      ShUpdateResponse shUpdateResponse = shService.shUpdate(shUpdateRequest);

      utilities.checkResultCode(shUpdateResponse.getResultCode().getTResultCode(),
                                shUpdateResponse.getExtendedResult(),
                                userData);

      //-----------------------------------------------------------------------
      // Finally, call ShPull again and output the "after update" field values
      // to show the change has been successful.
      //-----------------------------------------------------------------------
      shPullResponse = shService.shPull(shPullRequest);
      utilities.checkResultCode(shPullResponse.getResultCode().getTResultCode(),
                                shPullResponse.getExtendedResult(),
                                shPullResponse.getUserData());

      System.out.println("New settings:");
      utilities.displayFields(shPullResponse.getUserData());
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

  /**
   * Finds the "Subscribed" field within the user data and sets it to a new
   * value.
   *
   * @param userData    IN/OUT The user data to update.
   * @param subscription
   *                    IN     Whether to subscribe or unsubscribe from the
   *                           service.
   *
   * @exception NoSubscribedFieldException
   */
  public static void updateSubscribedField(TUserData userData,
                                           String subscription)
    throws NoSubscribedFieldException,
           SOAPException
  {
    //-------------------------------------------------------------------------
    // Dig down to the "Subscribed" field and fetch the elements corresponding
    // to its value and whether it is inheriting the default value.
    //-------------------------------------------------------------------------
    OMElement fieldGroupElement = utilities.getFieldGroupElement(userData);
    OMElement subscribedElement = fieldGroupElement.getFirstChildWithName(ShUntypedUtilities.SUBSCRIBED);

    //-------------------------------------------------------------------------
    // Ensure that this is call service data by checking that it actually has a
    // "Subscribed" field.
    //-------------------------------------------------------------------------
    if (subscribedElement == null)
    {
       throw new NoSubscribedFieldException("There is no \"Subscribed\" " +
                                            "field in the requested Service " +
                                            "Indication");
    }

    //-------------------------------------------------------------------------
    // Continue digging down to contents of "Subscribed" field.
    //-------------------------------------------------------------------------
    OMElement subscribedValueElement =  subscribedElement.getFirstChildWithName(ShUntypedUtilities.VALUE);
    OMElement subscribedUseDefaultElement = subscribedElement.getFirstChildWithName(ShUntypedUtilities.USE_DEFAULT);

    //-------------------------------------------------------------------------
    // Determine what change is needed to this call service and update the
    // "Subscribed" field.
    //-------------------------------------------------------------------------
    if (subscription.equals("subscribed"))
     {
       subscribedValueElement.setText(TRUE);
       subscribedUseDefaultElement.setText(FALSE);
     }
     else if (subscription.equals("unsubscribed"))
     {
      subscribedValueElement.setText(FALSE);
      subscribedUseDefaultElement.setText(FALSE);
     }
     else if (subscription.equals("default"))
     {
      subscribedUseDefaultElement.setText(TRUE);
   }

    //-------------------------------------------------------------------------
    // Remove all the other field elements from the user data.
    //-------------------------------------------------------------------------
     fieldGroupElement.removeChildren();
     fieldGroupElement.addChild(subscribedElement);
  }
}

//-----------------------------------------------------------------------------
// NoSubscribedFieldException
//
// Indicates that user data did not contain a "Subscribed" field, suggesting
// that it was not call service information.
//-----------------------------------------------------------------------------
class NoSubscribedFieldException extends MetaSwitchShInterfaceException
{
  NoSubscribedFieldException(String s)
  {
    super(s);
  }
}
