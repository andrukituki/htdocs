//-----------------------------------------------------------------------------
// UpdateSubscription
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
// Author: Alex Davies
//
// Sample application demonstrating the SOAP Sh interface to the MetaView
// Server.  This application uses Sh-Pull to display a subscriber's current
// settings for a particular call service, and then sends an Sh-Update to
// change whether the subscriber is subscribed to that service, using the
// sequence number to avoid conflicting updates.  Finally it makes a second
// Sh-Pull request to display the new configuration.  It uses Microsoft .NET
// and the "untyped" WSDL file.
//
// See the readme file for more information.
//-----------------------------------------------------------------------------

using System;
using System.Xml;

public class UpdateSubscription
{
  private static readonly string USAGE = "Usage: UpdateSubscription " +
                                         "<Directory number> " +
                                         "<Service indication> <subscribed |" +
                                         "unsubscribed | default>";
  private static ShUntypedUtilities utilities = new ShUntypedUtilities();

  public static void Main(string[] args)
  {
    String dN;
    String servInd;
    String subscribed;

    int resultCode;
    tExtendedResult extendedResult;
    XmlElement userData;

    try
    {
      //-----------------------------------------------------------------------
      // Extract information from the command line arguments.
      //-----------------------------------------------------------------------
      getParameters(args,
                    out dN,
                    out servInd,
                    out subscribed);

      //-----------------------------------------------------------------------
      // Connect to the Sh service and send in a Pull request.
      //-----------------------------------------------------------------------
      ShService shInstance = new ShService();

      resultCode = shInstance.ShPull(dN,
                                     0,
                                     servInd,
                                     ShUtilities.ORIGIN_HOST,
                                     out extendedResult,
                                     out userData);


      //-----------------------------------------------------------------------
      // Check whether the request succeeded and display the "before update"
      // value of each of the fields.
      //-----------------------------------------------------------------------
      utilities.checkResultCode(resultCode, extendedResult, userData);

      utilities.displayFields(userData);

      //-----------------------------------------------------------------------
      // Update the user data and send it back.  Check that the request
      // succeeded.
      //-----------------------------------------------------------------------
      utilities.incrementSequenceNumber(userData);

      updateSubscribedField(userData, subscribed);

      resultCode = shInstance.ShUpdate(dN,
                                       0,
                                       userData,
                                       ShUtilities.ORIGIN_HOST,
                                       out extendedResult);

      utilities.checkResultCode(resultCode, extendedResult, userData);

      //-----------------------------------------------------------------------
      // Finally, call ShPull again and output the "after update" field values
      // to show the change has been successful.
      //-----------------------------------------------------------------------
      resultCode = shInstance.ShPull(dN,
                                     0,
                                     servInd,
                                     ShUtilities.ORIGIN_HOST,
                                     out extendedResult,
                                     out userData);


      utilities.checkResultCode(resultCode, extendedResult, userData);

      Console.WriteLine("New settings:");
      utilities.displayFields(userData);
    }
    catch (MetaSwitchShInterfaceException e)
    {
      Console.WriteLine(e.Message);
      Console.WriteLine(USAGE);
    }
    catch (Exception e)
    {
      Console.WriteLine("Unexpected error \"" + e.GetType().Name
                         + "\":\"" + e + "\" in retrieving data");
      Console.WriteLine(USAGE);
    }
  }

  /**
   * Parse the command line arguments and extract the necessary information.
   *
   * All three parameters are required:
   * -  the subscriber's DN
   * -  the call service
   * -  the new value for the subscription.
   *
   * @param args        The arguments provided at the command line.
   * @param dN          The directory number to update.
   * @param servInd     The service indication to update.
   * @param subscription
   *                    Whether to subscribe to this service, unsubscribe from
   *                    the service or return to the default value.
   */
  private static void getParameters(String[] args,
                                    out String dN,
                                    out String servInd,
                                    out String subscription)
  {
    if (args.Length != 3)
    {
      throw new WrongParametersException("Wrong number of parameters.");
    }

    dN = args[0];
    servInd = "Meta_Subscriber_" + args[1];
    subscription = args[2].ToLower();

    if (!(subscription == "subscribed"  ) &&
        !(subscription == "unsubscribed") &&
        !(subscription == "default"     ) )
    {
      throw new WrongParametersException("SubscribedField may only be one " +
                                         "of subscribed, unsubscribed or " +
                                         "default");
    }
  }

  /**
   * Finds the "Subscribed" field within the user data and sets it to a new
   * value.
   *
   * @param userData    The user data to update.
   * @param subscription
   *                    Whether to subscribe or unsubscribe from the service.
   */
  private static void updateSubscribedField(XmlElement userData,
                                            String subscription)
  {
    //-------------------------------------------------------------------------
    // Dig down to the "Subscribed" field and fetch the elements corresponding
    // to its value and whether it is inheriting the default value.
    //-------------------------------------------------------------------------
    XmlNode fieldGroupElement = utilities.getFieldGroupElement(userData);

    XmlNode subscribedElement =
                fieldGroupElement.SelectSingleNode("s:Subscribed",
                                                   utilities.namespaceManager);
    if (subscribedElement == null) Console.WriteLine("argh");
    XmlNode subscribedValueElement =
                subscribedElement.SelectSingleNode("s:Value/text()",
                                                   utilities.namespaceManager);
    XmlNode subscribedUseDefaultElement =
                subscribedElement.SelectSingleNode("s:UseDefault/text()",
                                                   utilities.namespaceManager);

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
    // Determine what change is needed to this call service and update the
    // "Subscribed" field.
    //-------------------------------------------------------------------------
    if (subscription == "subscribed")
    {
      subscribedValueElement.Value = "True";
      subscribedUseDefaultElement.Value = "False";
    }
    else if (subscription  == "unsubscribed")
    {
      subscribedValueElement.Value = "False";
      subscribedUseDefaultElement.Value = "False";
    }
    else if (subscription == "default")
    {
      subscribedUseDefaultElement.Value = "True";
    }

    //-------------------------------------------------------------------------
    // Remove all the other field elements from the user data.
    //-------------------------------------------------------------------------
    fieldGroupElement.RemoveAll();
    fieldGroupElement.AppendChild(subscribedElement);
  }
}

//-----------------------------------------------------------------------------
// NoSubscribedFieldException
//
// Indicates that user data did not contain a "Subscribed" field, suggesting
// that it was not call service information.
//-----------------------------------------------------------------------------
public class NoSubscribedFieldException : MetaSwitchShInterfaceException
{
  public NoSubscribedFieldException(string s) : base(s)
  {
  }
}