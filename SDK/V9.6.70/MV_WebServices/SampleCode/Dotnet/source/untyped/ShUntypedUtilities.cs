//-----------------------------------------------------------------------------
// ShUntypedUtilities
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
//
// This file provides utilities to those example applications that use the
// "untyped" WSDL file and therefore deal with user data as XML.
//-----------------------------------------------------------------------------

using System;
using System.Text;
using System.Xml;
using System.Xml.XPath;

public class ShUntypedUtilities : ShUtilities
{
  public readonly XmlNamespaceManager namespaceManager =
                                      new XmlNamespaceManager(new NameTable());

  public ShUntypedUtilities()
  {
    namespaceManager.AddNamespace(
                        "u", "http://www.metaswitch.com/ems/soap/sh/userdata");
    namespaceManager.AddNamespace(
                     "s", "http://www.metaswitch.com/ems/soap/sh/servicedata");
  }

  /**
   * Build a string representation of each field in user data.
   *
   * @param userData    A set of user data such as that returned on an Sh-Pull
   *                    response.
   */
  public void displayFields(XmlElement userData)
  {
    StringBuilder displayFieldsBuilder = new StringBuilder();

    //-------------------------------------------------------------------------
    // Go down to the field group element contained within the user data.
    //-------------------------------------------------------------------------
    XmlNodeList fieldGroupElements = getFieldGroupElements(userData);

    //-------------------------------------------------------------------------
    // Go through each field group and display its contents.
    //-------------------------------------------------------------------------
    foreach (XmlNode fieldGroupElement in fieldGroupElements)
    {
      displayFieldsBuilder.Append("\nService indication " +
                                  fieldGroupElement.LocalName + "\n\n");

      //-----------------------------------------------------------------------
      // Go through each field under this element and display its name and
      // value.
      //-----------------------------------------------------------------------
      foreach (XmlElement currentChild in fieldGroupElement.ChildNodes)
      {
        displayFieldsBuilder.Append(currentChild.LocalName + ": " +
                                    getValue(currentChild) + "\n");
      }
    }

    Console.WriteLine(displayFieldsBuilder.ToString());
  }

  /**
   * Extract the field group elements from user data.
   *
   * @returns           The field group element contained in the xml.
   *
   * @param userData    A set of user data such as that returned on an Sh-Pull
   *                    response.
   */
  public XmlNodeList getFieldGroupElements(XmlElement userData)
  {
    //-------------------------------------------------------------------------
    // Fetch all the field group elements.
    //-------------------------------------------------------------------------
    XmlNodeList nodes = userData.SelectNodes("//s:MetaSwitchData/*",
                                             namespaceManager);

    return nodes;
  }

  /**
   * Extract the field group element from user data.
   *
   * @returns           The field group element.
   *
   * @param userData    A set of user data such as that returned on an Sh-Pull
   *                    response.
   */
  public XmlNode getFieldGroupElement(XmlElement userData)
  {
    XmlNode node = userData.SelectSingleNode("//s:MetaSwitchData/*[1]",
                                             namespaceManager);
    return node;
  }

  /**
   * Create a string representation of the value an XML element.
   *
   * @returns           The value as a string. Where the element has child
   *                    elements, these are returned as name-value pairs.
   *
   * @param element     An XML element whose value to print.
   */
  public String getValue(XmlNode element)
  {
    String getValueReturn;

    String value = element.Value;
    if (value != null)
    {
      //-----------------------------------------------------------------------
      // This element has a text value - just return that.
      //-----------------------------------------------------------------------
      getValueReturn = value;
    }
    else
    {
      XmlNode firstChild = element.FirstChild;
      if ((firstChild != null) && (firstChild.NodeType == XmlNodeType.Text))
      {
        //---------------------------------------------------------------------
        // There's just text here - output that.
        //---------------------------------------------------------------------
        getValueReturn = getValue(firstChild);
      }
      else
      {
        //---------------------------------------------------------------------
        // This element has no text value, so we'll display child elements.
        //---------------------------------------------------------------------
        StringBuilder getValueBuilder = new StringBuilder();

        //---------------------------------------------------------------------
        // If there is an element called "Value", display that first, and
        // exclude the word "Value".  This improves the presentation of
        // switchable default fields.
        //---------------------------------------------------------------------
        XmlNode childValue = element.SelectSingleNode("s:Value",
                                                      namespaceManager);
        if (childValue != null)
        {
          getValueBuilder.Append(getValue(childValue));
        }

        //---------------------------------------------------------------------
        // Go through each child element.  Display a list of their names and
        // values.
        //---------------------------------------------------------------------
        if (element.HasChildNodes)
        {
          getValueBuilder.Append("{");

          foreach (XmlNode currentChild in element.ChildNodes)
          {
            if (currentChild != childValue)
            {
              getValueBuilder.Append(currentChild.LocalName + "=" +
                                     getValue(currentChild) + ", ");
            }
          }

          //-------------------------------------------------------------------
          // Knock off the last comma and space.
          //-------------------------------------------------------------------
          getValueBuilder.Length = getValueBuilder.Length - 2;
          getValueBuilder.Append("}");
        }

        getValueReturn = getValueBuilder.ToString();
      }
    }

    return getValueReturn;
  }

  /**
   * Finds the current sequence number within the user data and increments it,
   * wrapping if necessary, so that the server accepts the change.
   *
   * @param userData    The user data whose sequence number to update.
   */
  public void incrementSequenceNumber(XmlNode userData)
  {
    //-------------------------------------------------------------------------
    // Dig down to the "SequenceNumber" element and extract its current value.
    //-------------------------------------------------------------------------
    XmlNode sequenceNumber = userData.SelectSingleNode("//u:SequenceNumber" +
                                                       "/text()",
                                                       namespaceManager);

    int value = Int32.Parse(sequenceNumber.Value);

    //-------------------------------------------------------------------------
    // Increment the value, wrapping back to 1 if it goes past the limit of
    // 65535.
    //-------------------------------------------------------------------------
    int newValue = value + 1;

    if (newValue > 65535)
    {
      newValue = 1;
    }
    sequenceNumber.Value = newValue.ToString();
  }

  /**
   * Get the name of the item identified by the SubResultSource in a particular
   * SubResult.  Overrides the implementation in ShUtilities.
   *
   * @returns           The name of the source item that caused a problem.
   *
   * @param subResult   The result containing the source string.
   * @param userData    The user data that was sent in.
   */
  public override String getSourceItem(tSubResult subResult,
                                       XmlElement userData)
  {
    //-------------------------------------------------------------------------
    // This overriding method tries to apply the sub-result's source as an
    // XPath query, resorting to string manipulation only if that doesn't work.
    //-------------------------------------------------------------------------
    String itemName = null;
    String source = subResult.SubResultSource;

    if ((userData != null) && (source != null) && (source.Length > 0))
    {
      XmlNode node = userData.SelectSingleNode(source, namespaceManager);

      if (node != null)
      {
        //---------------------------------------------------------------------
        // Having resolved the source to a part of the user data we sent in,
        // we'll just take the name of the element we found.  Normally an
        // application might do more than this, such as highlight the element
        // somehow in a user interface, but this serves as a demonstration.
        //---------------------------------------------------------------------
        itemName = node.LocalName;
      }
    }

    if (itemName == null)
    {
      //-----------------------------------------------------------------------
      // Either we don't have any source info, or we don't have any user data
      // (e.g. this was an Sh-Pull operation), or we couldn't resolve the
      // source to something within the user data.  We'll resort to working
      // with the source as a string instead.
      //-----------------------------------------------------------------------
      itemName = base.getSourceItem(subResult, userData);
    }

    return itemName;
  }
}