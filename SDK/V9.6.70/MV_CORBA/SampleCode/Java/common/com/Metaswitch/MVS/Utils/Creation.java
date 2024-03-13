/**
 * Title: Creation
 *
 * Description: Utility methods to create and delete objects
 *
 * (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
 *
 * @version 1.0
 */
package com.Metaswitch.MVS.Utils;

import java.lang.reflect.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;
import org.omg.PortableServer.POAManagerPackage.*;

import com.Metaswitch.MVS.Corba.*;

public class Creation
{
  //---------------------------------------------------------------------------
  // Versions
  //---------------------------------------------------------------------------
  private static final int V1_04   = 0x01040000;
  private static final int V2_0    = 0x02000000;
  private static final int V2_1    = 0x02010000;
  private static final int V2_2    = 0x02020000;
  private static final int V2_3    = 0x02030000;
  private static final int V2_3_01 = 0x02030100;
  private static final int V3_0    = 0x03000000;
  private static final int V3_0_01 = 0x03000100;
  private static final int V3_0_02 = 0x03000200;
  private static final int V3_1    = 0x03010000;
  private static final int V3_1_01 = 0x03010100;
  private static final int V3_2    = 0x03020000;
  private static final int V4_0    = 0x04000000;
  private static final int V4_0_01 = 0x04000100;
  private static final int V4_1    = 0x04010000;
  private static final int V4_1_01 = 0x04010100;
  private static final int V4_1_02 = 0x04010200;
  private static final int V4_2    = 0x04020000;
  private static final int V5_0    = 0x05000000;
  private static final int V5_0_02 = 0x05000200;
  private static final int V5_1    = 0x05010000;
  private static final int V6_0    = 0x06000000;
  private static final int V6_1    = 0x06010000;
  private static final int V7_0    = 0x07000000;
  private static final int V7_0_01 = 0x07000100;
  private static final int V7_1    = 0x07010000;
  private static final int V7_1_01 = 0x07010100;
  private static final int V7_2    = 0x07020000;
  private static final int V7_3    = 0x07030000;
  private static final int V7_3_01 = 0x07030100;
  private static final int V7_4    = 0x07040000;
  private static final int V7_4_01 = 0x07040100;
  private static final int V8_0    = 0x08000000;
  private static final int V8_0_02 = 0x08000200;
  private static final int V8_0_03 = 0x08000300;
  private static final int V8_1    = 0x08010000;
  private static final int V8_1_01 = 0x08010100;
  private static final int V8_1_02 = 0x08010200;
  private static final int V8_2    = 0x08020000;
  private static final int V8_2_02 = 0x08020200;
  private static final int V8_3    = 0x08030000;
  private static final int V8_3_01 = 0x08030100;
  private static final int V8_3_03 = 0x08030300;
  private static final int V8_3_04 = 0x08030400;
  private static final int V9_0    = 0x09000000;
  private static final int V9_0_01 = 0x09000100;
  private static final int V9_0_03 = 0x09000300;
  private static final int V9_0_10 = 0x09000A00;
  private static final int V9_1    = 0x09010000;
  private static final int V9_1_10 = 0x09010A00;
  private static final int V9_1_15 = 0x09010F00;
  private static final int V9_2    = 0x09020000;
  private static final int V9_2_10 = 0x09020A00;
  private static final int V9_3    = 0x09030000;
  private static final int V9_3_10 = 0x09030A00;
  private static final int V9_3_20 = 0x09031400;
  private static final int V9_4    = 0x09040000;
  private static final int V9_4_10 = 0x09040A00;
  private static final int V9_4_20 = 0x09041400;
  private static final int V9_4_30 = 0x09041E00;
  private static final int V9_5    = 0x09050000;
  private static final int V9_5_10 = 0x09050A00;
  private static final int V9_5_20 = 0x09051400;
  private static final int V9_5_30 = 0x09051E00;
  private static final int V9_5_40 = 0x09052800;
  private static final int V9_6    = 0x09060000;
  private static final int V9_6_10 = 0x09060A00;
  private static final int V9_6_20 = 0x09061400;
  private static final int V9_6_30 = 0x09061E00;
  private static final int V9_6_40 = 0x09062800;
  private static final int V9_6_50 = 0x09063200;
  private static final int V9_6_60 = 0x09063C00;
  private static final int V9_6_70 = 0x09064600;
  
  //---------------------------------------------------------------------------
  // Access Device fields.
  //---------------------------------------------------------------------------
  private static final int sAccDevProtocol = omlapi.V_BLES;

  //---------------------------------------------------------------------------
  // Individual Line fields.
  //---------------------------------------------------------------------------
  private static final int sIndLineNumStatus = omlapi.V_NORMAL;
  private static final int sIndLineLocale = omlapi.V_ENGLISH__US_;

  //---------------------------------------------------------------------------
  //  Hardcoded subscriber services fields for initialising call services.
  //---------------------------------------------------------------------------
  private static final int iFwdGpSubscrFwdServSubscr = omlapi.V_TRUE;
  private static final boolean iFwdGpSubscrFwdServSubscrUseDef = false;
  private static final boolean iFwdGpSubscrBsyServSubscrUseDef = true;
  private static final boolean iFwdGpSubscrDlyServSubscrUseDef = true;
  private static final boolean iFwdGpSubscrRemServSubscrUseDef = true;
  private static final boolean iFwdGpSubscrSelServSubscrUseDef = true;
  private static final boolean iFwdGpSubscrHntServSubscrUseDef = true;
  private static final boolean iFwdGpSubscrUsSensBillUseDef = true;
  private static final boolean iFwdGpSubscrSelUsSensUseDef = true;
  private static final boolean iFwdGpSubscrNoRpyTimeUseDef = true;
  private static final boolean iFwdGpSubscrHntNoRpyTimeUseDef = true;
  private static final boolean iFwdGpSubscrNotifyCallingUseDef = true;
  private static final boolean iFwdGpSubscrReleaseNumberUseDef = true;
  private static final boolean iFwdGpSubscrHntArrangementUseDef = true;
  private static final boolean iFwdGpSubscrActConfirmToneUseDef = true;

  private static final int iTrnGpSubscrThrServSubscr = omlapi.V_TRUE;
  private static final boolean iTrnGpSubscrThrServSubscrUseDef = false;
  private static final boolean iTrnGpSubscrCwtServSubscrUseDef = true;
  private static final boolean iTrnGpSubscrWidServSubscrUseDef = true;
  private static final boolean iTrnGpSubscrTrnServSubscrUseDef = true;
  private static final boolean iTrnHicServSubscrUseDef = true;
  private static final boolean iTrnHicDrServSubscrUseDef = true;

  private static final int iCidGpSubscrArServSubscr = omlapi.V_TRUE;
  private static final boolean iCidGpSubscrArServSubscrUseDef = false;
  private static final boolean iCidGpSubscrTrcServSubscrUseDef = true;
  private static final boolean iCidGpSubscrCidServSubscrUseDef = true;
  private static final boolean iCidGpSubscrPrsServSubscrUseDef = true;
  private static final boolean iCidGpSubscrPrsCndbServSubscrUseDef = true;
  private static final boolean iCidGpSubscrPrsWithholdUseDef = true;
  private static final boolean iCidGpSubscrLceServSubscrUseDef = true;

  private static final int iIncGpSubscrScrServSubscr = omlapi.V_TRUE;
  private static final boolean iIncGpSubscrScrServSubscrUseDef = false;
  private static final boolean iIncGpSubscrAnrServSubscrUseDef = true;
  private static final boolean iIncGpSubscrDrcwServSubscrUseDef = true;

  private static final int iMsgGpSubscrVoicemailServSubscr = omlapi.V_TRUE;
  private static final boolean iMsgGpSubscrVoicemailServSubscrUseDef = false;
  private static final boolean iMsgGpSubscrRemServSubscrUseDef = true;
  private static final boolean iMsgGpSubscrRrcServSubscrUseDef = true;
  private static final boolean iMsgGpSubscrMsgDelayTimeUseDef = true;
  private static final boolean iMsgGpSubscrMSRGroupUseDef = true;

  private static final int iOutGpSubscrSpdServSubscr = omlapi.V_TRUE;
  private static final boolean iOutGpSubscrSpdServSubscrUseDef = false;
  private static final boolean iOutGpSubscrBarServSubscrUseDef = true;
  private static final boolean iOutGpSubscrSpdFormUseDef = true;
  private static final boolean iOutGpSubscrSpdAccessUseDef = true;
  private static final boolean iOutGpSubscrMacServSubscrUseDef = true;

  private static final int iGenGpSubscrPinServSubscr = omlapi.V_TRUE;
  private static final boolean iGenGpSubscrPinServSubscrUseDef = false;

  private static final int iWebGpWebServSubscr = omlapi.V_FALSE;

  //---------------------------------------------------------------------------
  //  Hardcoded subscriber services fields for updating call services.
  //---------------------------------------------------------------------------
  private static final int uFwdGpSubscrBsyServSubscr = omlapi.V_TRUE;
  private static final boolean uFwdGpSubscrBsyServSubscrUseDef = false;

  private static final int uTrnGpSubscrCwtServSubscr = omlapi.V_TRUE;
  private static final boolean uTrnGpSubscrCwtServSubscrUseDef = false;

  private static final int uCidGpSubscrTrcServSubscr = omlapi.V_TRUE;
  private static final boolean uCidGpSubscrTrcServSubscrUseDef = false;

  private static final int uIncGpSubscrAnrServSubscr = omlapi.V_TRUE;
  private static final boolean uIncGpSubscrAnrServSubscrUseDef = false;

  private static final int uMsgGpSubscrRemServSubscr = omlapi.V_TRUE;
  private static final boolean uMsgGpSubscrRemServSubscrUseDef = false;

  private static final int uOutGpSubscrBarServSubscr = omlapi.V_TRUE;
  private static final boolean uOutGpSubscrBarServSubscrUseDef = false;

  private static final int uGenGpSubscrPinServSubscr = omlapi.V_TRUE;
  private static final boolean uGenGpSubscrPinServSubscrUseDef = false;

  private static final int uWebGpWebServSubscr = omlapi.V_FALSE;

  /**
   * Creates an IDT on the UMG / IS whose connection has the index provided.
   *
   * @returns           A DualString containing a reference to the IDT that has
   *                    been created.
   *
   * @param description
   *                    Name of the IDT
   * @param connSEA
   *
   * @exception ElementUnavailableException
   */
  public static DualString createIDT(String description,
                                     SEAccessInterface connSEA)
    throws ElementUnavailableException
  {
    SEAccessInterface idtSEA = null;
    DualString idtReference = null;

    try
    {
      idtSEA = connSEA.createElement(omlapi.O_IDT);

      //-----------------------------------------------------------------------
      // Obtain the settings, but throw away all other values returned by
      // getSnapshot since none of the holders filled in by getSnaphot will
      // contain useful values until the object is successfully created.
      //-----------------------------------------------------------------------
      SettingsUserInterface settings =
                          idtSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                             new StringHolder(),
                                             new StringHolder(),
                                             new SequenceOfReferencesHolder());

      //-----------------------------------------------------------------------
      // Setup the mandatory fields in the IDT Settings.
      //-----------------------------------------------------------------------

      //-----------------------------------------------------------------------
      // Access Device Number - use the first value returned by the PVP.
      //-----------------------------------------------------------------------
      DualString accessDeviceNumber =
           CorbaHelper.getFirstValueForPVPField(settings,
                                                omlapi.F_ACCESS_DEVICE_NUMBER);

      if (accessDeviceNumber == null)
      {
        //---------------------------------------------------------------------
        // If we've been returned null, then there are no valid values
        // available.  Throw an IllegalStateException to indicate that
        // something unexpected has occured.
        //---------------------------------------------------------------------
        throw new IllegalStateException("No possible values for Access Device "
                                        + "Number");
      }

      settings.setFieldAsStringByName(omlapi.F_ACCESS_DEVICE_NUMBER,
                                      accessDeviceNumber.internal);

      //-----------------------------------------------------------------------
      // Description.
      //-----------------------------------------------------------------------
      settings.setFieldAsStringByName(omlapi.F_DESCRIPTION, description);

      //-----------------------------------------------------------------------
      // Apply the changes.  This results in the IDT object actually being
      // created.
      //-----------------------------------------------------------------------
      idtSEA.doAction(omlapi.A_APPLY);

      //-----------------------------------------------------------------------
      // Perform a final getSnapshot to get the element name and display name
      // of the IDT.
      //-----------------------------------------------------------------------
      StringHolder idtElementName = new StringHolder();
      StringHolder idtDisplayName = new StringHolder();

      idtSEA.getSnapshot(new SequenceOfIntegersHolder(),
                         idtElementName,
                         idtDisplayName,
                         new SequenceOfReferencesHolder());

      idtReference = new DualString(idtElementName.value,
                                    idtDisplayName.value);
    }
    //-------------------------------------------------------------------------
    // All CORBA UserExceptions are unexpected in this case (apart from
    // ElementUnavailableException).
    //-------------------------------------------------------------------------
    catch (NameUnknownException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (InvalidElementTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (CreationFailedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementDeletedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementBrokenException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementOperationFailedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementChangedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementAlreadyLockedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (SettingsFieldException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldNoRegisteredPVPException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadValueException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (UnknownActionException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ActionNotEnabledException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    finally
    {
      //-----------------------------------------------------------------------
      // Destroy the SEAccess.
      //-----------------------------------------------------------------------
      if (idtSEA != null)
      {
        idtSEA.destroy();
      }
    }

    return idtReference;
  }
  /**
   * Create an Access Device to use the specified IDT.
   *
   * @returns           A reference to the Access Device that has been created.
   *
   * @param connSEA
   *                    An SEA attached to the CFS / IS that we want to
   *                    create the Access Device on.
   * @param idtReference
   *                    A reference to the IDT that we want to create an Access
   *                    Device for.
   *
   * @exception ElementUnavailableException
   */
  public static DualString createAccessDeviceForIDT(SEAccessInterface connSEA,
                                                    DualString idtReference)
    throws ElementUnavailableException
  {
    SEAccessInterface accessDeviceSEA = null;
    DualString accDevReference = null;

    try
    {
      accessDeviceSEA = connSEA.createElement(
                                omlapi.O_TRUNK___ACCESS_GATEWAY_ACCESS_DEVICE);

      //-----------------------------------------------------------------------
      // None of the holders filled in by getSnaphot will contain useful values
      // until the object is successfully created, so just throw the values
      // away.
      //-----------------------------------------------------------------------
      SettingsUserInterface settings =
                 accessDeviceSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                             new StringHolder(),
                                             new StringHolder(),
                                             new SequenceOfReferencesHolder());

      //-----------------------------------------------------------------------
      // Setup the mandatory fields in the Access Device Settings.
      //
      // These are set by filling in the Access Hardware field and then calling
      // perform adjustments on it.  This will get the values for the port
      // name, access protocol, description and no. of lines fields from the
      // referenced IDT and fill them in.
      //-----------------------------------------------------------------------

      //-----------------------------------------------------------------------
      // Access hardware - use the reference that's been supplied.
      //-----------------------------------------------------------------------
      settings.setFieldAsReferenceByName(omlapi.F_ACCESS_HARDWARE,
                                         idtReference);

      //-----------------------------------------------------------------------
      // Apply the changes.
      //-----------------------------------------------------------------------
      accessDeviceSEA.doAction(omlapi.A_APPLY);

      //-----------------------------------------------------------------------
      // Perform a final getSnapshot to get the element name and display name
      // of the Access Device.
      //-----------------------------------------------------------------------
      StringHolder accDevElementName = new StringHolder();
      StringHolder accDevDisplayName = new StringHolder();

      accessDeviceSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                  accDevElementName,
                                  accDevDisplayName,
                                  new SequenceOfReferencesHolder());

      accDevReference = new DualString(accDevElementName.value,
                                       accDevDisplayName.value);
    }
    //-------------------------------------------------------------------------
    // All CORBA UserExceptions are unexpected in this case (apart from
    // ElementUnavailableException).
    //-------------------------------------------------------------------------
    catch (NameUnknownException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (InvalidElementTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (CreationFailedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementDeletedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementBrokenException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementOperationFailedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementChangedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementAlreadyLockedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (SettingsFieldException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadValueException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (UnknownActionException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ActionNotEnabledException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    finally
    {
      //-----------------------------------------------------------------------
      // Destroy the SEAccess.
      //-----------------------------------------------------------------------
      if (accessDeviceSEA != null)
      {
        accessDeviceSEA.destroy();
      }
    }

    return accDevReference;
  }

  /**
   * Creates an Individual Line on the specified Access Device.
   *
   * @param connSEA
   *                    A SEA attached to the CFS / IS below which we want to
   *                    create an Individual Line.
   * @param accDevReference
   *                    A reference to the Access Device we want to create an
   *                    Individual Line on.
   * @param directoryNumber
   *                    Directory number of the line being created
   *
   * @exception ElementUnavailableException
   */
  public static DualString
                 createIndLineOnAccessDevice(SEAccessInterface connSEA,
                                             DualString accDevReference,
                                             DualStringHolder directoryNumber,
                                             int accessLineNumber)
    throws ElementUnavailableException
  {
    SEAccessInterface indLineSEA = null;
    DualString indLineReference = null;

    try
    {
      indLineSEA = connSEA.createElement(omlapi.O_INDIVIDUAL_LINE);

      //-----------------------------------------------------------------------
      // None of the holders filled in by getSnaphot will contain useful values
      // until the object is successfully created, so just throw the values
      // away.
      //-----------------------------------------------------------------------
      SettingsUserInterface settings =
                      indLineSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                             new StringHolder(),
                                             new StringHolder(),
                                             new SequenceOfReferencesHolder());

      //-----------------------------------------------------------------------
      // Directory Number - use the first value returned by the PVP.  The value
      // returned by this PVP is not a reference, but rather the first free dn
      // available on the CFS / IS, so we only need the internal value of the
      // reference when setting the Directory Number field.
      //-----------------------------------------------------------------------
      directoryNumber.value =
               CorbaHelper.getFirstValueForPVPField(settings,
                                                    omlapi.F_DIRECTORY_NUMBER);

      if (directoryNumber == null)
      {
        //---------------------------------------------------------------------
        // If we've been returned null, then there are no valid values
        // available.  Throw an IllegalStateException to indicate that
        // something unexpected has occured.
        //---------------------------------------------------------------------
        throw new IllegalStateException("No possible values for Directory "
                                        + "Number");
      }

      settings.setFieldAsStringByName(omlapi.F_DIRECTORY_NUMBER,
                                      directoryNumber.value.internal);

      //-----------------------------------------------------------------------
      // Subscriber Group - Use the first value returned by the PVP.
      //-----------------------------------------------------------------------
      DualString subscrGroup =
               CorbaHelper.getFirstValueForPVPField(settings,
                                                    omlapi.F_SUBSCRIBER_GROUP);

      if (subscrGroup == null)
      {
        //---------------------------------------------------------------------
        // If we've been returned null, then there are no valid values
        // available.  Throw an IllegalStateException to indicate that
        // something unexpected has occured.
        //---------------------------------------------------------------------
        throw new IllegalStateException("No possible values for Subscriber "
                                        + "Group");
      }

      settings.setFieldAsReferenceByName(omlapi.F_SUBSCRIBER_GROUP,
                                         subscrGroup);

      //-----------------------------------------------------------------------
      // Number Status - use the hardcoded values at the top of this class.
      //-----------------------------------------------------------------------
      settings.setFieldAsIntByName(omlapi.F_NUMBER_STATUS, sIndLineNumStatus);

      //-----------------------------------------------------------------------
      // Access Device - Use the reference that's been supplied.
      //-----------------------------------------------------------------------
      settings.setFieldAsReferenceByName(omlapi.F_ACCESS_DEVICE,
                                         accDevReference);

      settings.setFieldAsIntByName(omlapi.F_ACCESS_LINE_NUMBER,
                                   accessLineNumber);

      //-----------------------------------------------------------------------
      // Locale - use the hardcoded values at the top of this class.
      //-----------------------------------------------------------------------
      settings.setFieldAsIntByName(omlapi.F_LOCALE, sIndLineLocale);

      //-----------------------------------------------------------------------
      // Apply the changes.
      //-----------------------------------------------------------------------
      indLineSEA.doAction(omlapi.A_APPLY);

      //-----------------------------------------------------------------------
      // Perform a final getSnapshot to get the element name and display name
      // of the Individual Line.
      //-----------------------------------------------------------------------
      StringHolder indLineElementName = new StringHolder();
      StringHolder indLineDisplayName = new StringHolder();

      indLineSEA.getSnapshot(new SequenceOfIntegersHolder(),
                             indLineElementName,
                             indLineDisplayName,
                             new SequenceOfReferencesHolder());

      indLineReference =
            new DualString(indLineElementName.value, indLineDisplayName.value);
    }
    //-------------------------------------------------------------------------
    // All CORBA UserExceptions are unexpected in this case (apart from
    // ElementUnavailableException).
    //-------------------------------------------------------------------------
    catch (NameUnknownException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (InvalidElementTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (CreationFailedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementDeletedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementBrokenException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementOperationFailedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementChangedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementAlreadyLockedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (SettingsFieldException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldNoRegisteredPVPException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadValueException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (UnknownActionException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ActionNotEnabledException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    finally
    {
      //-----------------------------------------------------------------------
      // Destroy the SEAccess.
      //-----------------------------------------------------------------------
      if (indLineSEA != null)
      {
        indLineSEA.destroy();
      }
    }

    return indLineReference;
  }

  /**
   * Sets the subscriber services for the created subscriber to hardcoded
   * values
   *
   * @param indLineElementName
   *                    Name of the Individual Line to set services for.
   * @param clientSession
   *                    Client Session to use to get SEAs
   * @param version     Version of the NE we are making changes on.
   *
   */
  public static void initialiseSubscriberServices(String indLineElementName,
                                                  ClientSessionInterface clientSession,
                                                  int version)
  {


    String errorMessage = "Failure when creating call forwarding services for Ind Line "
                      + indLineElementName;

    SEAccessInterface subServSEA = null;
    SEAccessInterface indLineSEA = null;

    try
    {
      indLineSEA = clientSession.createSEAccess();
      indLineSEA.attachTo(indLineElementName);
    }
    catch (InvalidNameException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (AlreadyAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementDeletedException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementOperationFailedException e)
    {
      outputException(errorMessage, e);
    }

    try
    {
      subServSEA = indLineSEA.findElement(
                            omlapi.O_INDIVIDUAL_LINE_CALL_FORWARDING_SERVICES);

      SettingsUserInterface settings =
                      subServSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                            new StringHolder(),
                                            new StringHolder(),
                                            new SequenceOfReferencesHolder());

      settings.setUseDefaultFlagByName(omlapi.F_USER_NOTIFICATION_OF_CALL_DIVERSION,
                                       iFwdGpSubscrNotifyCallingUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_NUMBER_RELEASED_TO_DIVERTED_TO_USER,
                                       iFwdGpSubscrReleaseNumberUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_PLAY_CONFIRM_TONE,
                                       iFwdGpSubscrActConfirmToneUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_UNCONDITIONAL_CALL_FORWARDING___SUBSCRIBED,
                                       iFwdGpSubscrFwdServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_UNCONDITIONAL_CALL_FORWARDING___SUBSCRIBED,
                                           iFwdGpSubscrFwdServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_BUSY_CALL_FORWARDING___SUBSCRIBED,
                                       iFwdGpSubscrBsyServSubscrUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_DELAYED_CALL_FORWARDING___SUBSCRIBED,
                                       iFwdGpSubscrDlyServSubscrUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_DELAYED_CALL_FORWARDING___NO_REPLY_TIME,
                                       iFwdGpSubscrNoRpyTimeUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_UNCONDITIONAL__BUSY_OR_DELAYED_CALL_FORWARDING_USAGE_SENSITIVE_BILLING,
                                       iFwdGpSubscrUsSensBillUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_SELECTIVE_CALL_FORWARDING___SUBSCRIBED,
                                       iFwdGpSubscrSelServSubscrUseDef );
      settings.setUseDefaultFlagByName(omlapi.F_SELECTIVE_CALL_FORWARDING___USAGE_SENSITIVE_BILLING,
                                       iFwdGpSubscrSelUsSensUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_LINE_HUNTING___SUBSCRIBED,
                                       iFwdGpSubscrHntServSubscrUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_LINE_HUNTING___NO_REPLY_TIME,
                                       iFwdGpSubscrHntNoRpyTimeUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_LINE_HUNTING___ARRANGEMENT,
                                       iFwdGpSubscrHntArrangementUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_REMOTE_ACCESS_TO_CALL_FORWARDING___SUBSCRIBED,
                                       iFwdGpSubscrRemServSubscrUseDef);

      //-----------------------------------------------------------------------
      // Apply the changes.  This results in the object
      // actually being created.
      //-----------------------------------------------------------------------
      subServSEA.doAction(omlapi.A_APPLY);
      TraceHelper.trace("Created Call forwarding services for Ind Line " + indLineElementName);
    }
    catch (ElementUnavailableException e)
    {
      outputException(errorMessage, e);
    }
    catch (FieldBadValueException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementDeletedException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementBrokenException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementOperationFailedException e)
    {
      outputException(errorMessage, e);
    }
    catch (SettingsFieldException e)
    {
      outputException(errorMessage, e);
    }
    //-------------------------------------------------------------------------
    // None of the remaining exceptions should happen - these indicate
    // programming errors.
    //-------------------------------------------------------------------------
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NameUnknownException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementAlreadyLockedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementChangedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ActionNotEnabledException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (UnknownActionException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    finally
    {
      if (subServSEA != null)
      {
        subServSEA.destroy();
      }
    }

    errorMessage = "Failure when creating caller ID services for Ind Line "
                      + indLineElementName;
    try
    {
      subServSEA = indLineSEA.findElement(
                                 omlapi.O_INDIVIDUAL_LINE_CALLER_ID_SERVICES);

      SettingsUserInterface settings =
                     subServSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                            new StringHolder(),
                                            new StringHolder(),
                                            new SequenceOfReferencesHolder());

      settings.setUseDefaultFlagByName(omlapi.F_AUTOMATIC_RECALL___SUBSCRIBED,
                                       iCidGpSubscrArServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_AUTOMATIC_RECALL___SUBSCRIBED,
                                           iCidGpSubscrArServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_CALL_TRACE___SUBSCRIBED,
                                       iCidGpSubscrTrcServSubscrUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_CALLING_NUMBER_DELIVERY___SUBSCRIBED,
                                       iCidGpSubscrCidServSubscrUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_CALLER_ID_PRESENTATION___SUBSCRIBED,
                                       iCidGpSubscrPrsServSubscrUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_CALLER_ID_PRESENTATION___WITHHOLD_NUMBER_BY_DEFAULT,
                                       iCidGpSubscrPrsWithholdUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_CALLING_NUMBER_DELIVERY_BLOCKING___SUBSCRIBED,
                                       iCidGpSubscrPrsCndbServSubscrUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_LAST_CALLER_ID_ERASURE___SUBSCRIBED,
                                       iCidGpSubscrLceServSubscrUseDef);

      //-----------------------------------------------------------------------
      // Apply the changes.  This results in the object
      // actually being created.
      //-----------------------------------------------------------------------
      subServSEA.doAction(omlapi.A_APPLY);
      TraceHelper.trace("Created Caller ID services for Ind Line " + indLineElementName);
    }
    catch (FieldBadValueException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementUnavailableException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementDeletedException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementBrokenException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementOperationFailedException e)
    {
      outputException(errorMessage, e);
    }
    catch (SettingsFieldException e)
    {
      outputException(errorMessage, e);
    }
    //-------------------------------------------------------------------------
    // None of the remaining exceptions should happen - these indicate
    // programming errors.
    //-------------------------------------------------------------------------
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NameUnknownException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementAlreadyLockedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementChangedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ActionNotEnabledException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (UnknownActionException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    finally
    {
      if (subServSEA != null)
      {
        subServSEA.destroy();
      }
    }

    errorMessage = "Failure when creating General Call services for Ind Line "
                      + indLineElementName;
    try
    {
      subServSEA = indLineSEA.findElement(
                      omlapi.O_INDIVIDUAL_LINE_GENERAL_CALL_SERVICE_CONTROLS);

      SettingsUserInterface settings =
                     subServSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                            new StringHolder(),
                                            new StringHolder(),
                                            new SequenceOfReferencesHolder());

      settings.setUseDefaultFlagByName(omlapi.F_PIN_CHANGE___SUBSCRIBED,
                                       iGenGpSubscrPinServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_PIN_CHANGE___SUBSCRIBED,
                                           iGenGpSubscrPinServSubscr);

      //-----------------------------------------------------------------------
      // Apply the changes.  This results in the object
      // actually being created.
      //-----------------------------------------------------------------------
      subServSEA.doAction(omlapi.A_APPLY);
      TraceHelper.trace("Created General call services for Ind Line " + indLineElementName);
    }
    catch (FieldBadValueException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementUnavailableException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementDeletedException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementBrokenException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementOperationFailedException e)
    {
      outputException(errorMessage, e);
    }
    catch (SettingsFieldException e)
    {
      outputException(errorMessage, e);
    }
    //-------------------------------------------------------------------------
    // None of the remaining exceptions should happen - these indicate
    // programming errors.
    //-------------------------------------------------------------------------
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NameUnknownException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementAlreadyLockedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementChangedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ActionNotEnabledException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (UnknownActionException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    finally
    {
      if (subServSEA != null)
      {
        subServSEA.destroy();
      }
    }

    errorMessage = "Failure when creating Incoming Call services for Ind Line "
                      + indLineElementName;
    try
    {
      subServSEA = indLineSEA.findElement(
                              omlapi.O_INDIVIDUAL_LINE_INCOMING_CALL_SERVICES);

      SettingsUserInterface settings =
                     subServSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                            new StringHolder(),
                                            new StringHolder(),
                                            new SequenceOfReferencesHolder());

      settings.setUseDefaultFlagByName(omlapi.F_SELECTIVE_CALL_REJECTION___SUBSCRIBED,
                                       iIncGpSubscrScrServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_SELECTIVE_CALL_REJECTION___SUBSCRIBED,
                                           iIncGpSubscrScrServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_ANONYMOUS_CALL_REJECTION___SUBSCRIBED,
                                       iIncGpSubscrAnrServSubscrUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_PRIORITY_CALL___SUBSCRIBED,
                                       iIncGpSubscrDrcwServSubscrUseDef);

      //-----------------------------------------------------------------------
      // Apply the changes.  This results in the object
      // actually being created.
      //-----------------------------------------------------------------------
      subServSEA.doAction(omlapi.A_APPLY);
      TraceHelper.trace("Created Incoming call services for Ind Line " + indLineElementName);
    }
    catch (FieldBadValueException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementUnavailableException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementDeletedException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementBrokenException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementOperationFailedException e)
    {
      outputException(errorMessage, e);
    }
    catch (SettingsFieldException e)
    {
      outputException(errorMessage, e);
    }
    //-------------------------------------------------------------------------
    // None of the remaining exceptions should happen - these indicate
    // programming errors.
    //-------------------------------------------------------------------------
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NameUnknownException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementAlreadyLockedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementChangedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ActionNotEnabledException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (UnknownActionException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    finally
    {
      if (subServSEA != null)
      {
        subServSEA.destroy();
      }
    }
    errorMessage = "Failure when creating Message services for Ind Line "
                      + indLineElementName;
    try
    {
      subServSEA = indLineSEA.findElement(
                                    omlapi.O_INDIVIDUAL_LINE_MESSAGE_SERVICES);

      SettingsUserInterface settings =
                     subServSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                            new StringHolder(),
                                            new StringHolder(),
                                            new SequenceOfReferencesHolder());

      settings.setUseDefaultFlagByName(omlapi.F_VOICEMAIL___SUBSCRIBED,
                                       iMsgGpSubscrVoicemailServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_VOICEMAIL___SUBSCRIBED,
                                           iMsgGpSubscrVoicemailServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_VOICEMAIL___VOICEMAIL_SYSTEM_LINE_GROUP,
                                       iMsgGpSubscrMSRGroupUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_VOICEMAIL___CALL_TRANSFER_TIME,
                                       iMsgGpSubscrMsgDelayTimeUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_REMINDER_CALLS___SUBSCRIBED,
                                       iMsgGpSubscrRemServSubscrUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_REGULAR_REMINDER_CALLS___SUBSCRIBED,
                                       iMsgGpSubscrRrcServSubscrUseDef);

      //-----------------------------------------------------------------------
      // Apply the changes.  This results in the object
      // actually being created.
      //-----------------------------------------------------------------------
      subServSEA.doAction(omlapi.A_APPLY);
      TraceHelper.trace("Created Message services for Ind Line " + indLineElementName);
    }
    catch (FieldBadValueException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementUnavailableException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementDeletedException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementBrokenException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementOperationFailedException e)
    {
      outputException(errorMessage, e);
    }
    catch (SettingsFieldException e)
    {
      outputException(errorMessage, e);
    }
    //-------------------------------------------------------------------------
    // None of the remaining exceptions should happen - these indicate
    // programming errors.
    //-------------------------------------------------------------------------
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NameUnknownException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementAlreadyLockedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementChangedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ActionNotEnabledException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (UnknownActionException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    finally
    {
      if (subServSEA != null)
      {
        subServSEA.destroy();
      }
    }
    errorMessage = "Failure when creating Multi-Party call services for Ind Line "
                      + indLineElementName;
    try
    {
      subServSEA = indLineSEA.findElement(
                           omlapi.O_INDIVIDUAL_LINE_MULTI_PARTY_CALL_SERVICES);

      SettingsUserInterface settings =
                     subServSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                            new StringHolder(),
                                            new StringHolder(),
                                            new SequenceOfReferencesHolder());

      settings.setUseDefaultFlagByName(omlapi.F_3_WAY_CALLING___SUBSCRIBED,
                                       iTrnGpSubscrThrServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_3_WAY_CALLING___SUBSCRIBED,
                                           iTrnGpSubscrThrServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_CALL_WAITING___SUBSCRIBED,
                                       iTrnGpSubscrCwtServSubscrUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_CALL_WAITING_WITH_CALLER_ID___SUBSCRIBED,
                                       iTrnGpSubscrWidServSubscrUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_CALL_TRANSFER___SUBSCRIBED,
                                       iTrnGpSubscrTrnServSubscrUseDef);
      if (version == V3_0)
      {
        settings.setUseDefaultFlagByName(omlapi.F_HOME_INTERCOM___SUBSCRIBED,
                                         iTrnHicServSubscrUseDef);
        settings.setUseDefaultFlagByName(
                                    omlapi.F_HOME_INTERCOM_DISTINCTIVE_RINGING,
                                    iTrnHicDrServSubscrUseDef);
      }

      //-----------------------------------------------------------------------
      // Apply the changes.  This results in the object
      // actually being created.
      //-----------------------------------------------------------------------
      subServSEA.doAction(omlapi.A_APPLY);
      TraceHelper.trace("Created Multi-party call services for Ind Line " + indLineElementName);
    }
    catch (FieldBadValueException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementUnavailableException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementDeletedException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementBrokenException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementOperationFailedException e)
    {
      outputException(errorMessage, e);
    }
    catch (SettingsFieldException e)
    {
      outputException(errorMessage, e);
    }
    //-------------------------------------------------------------------------
    // None of the remaining exceptions should happen - these indicate
    // programming errors.
    //-------------------------------------------------------------------------
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NameUnknownException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementAlreadyLockedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementChangedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ActionNotEnabledException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (UnknownActionException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    finally
    {
      if (subServSEA != null)
      {
        subServSEA.destroy();
      }
    }
    errorMessage = "Failure when creating Outgoing call services for Ind Line "
                      + indLineElementName;
    try
    {
      subServSEA = indLineSEA.findElement(
                              omlapi.O_INDIVIDUAL_LINE_OUTGOING_CALL_SERVICES);

      SettingsUserInterface settings =
                     subServSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                            new StringHolder(),
                                            new StringHolder(),
                                            new SequenceOfReferencesHolder());

      settings.setUseDefaultFlagByName(omlapi.F_SPEED_CALLING___SUBSCRIBED,
                                       iOutGpSubscrSpdServSubscrUseDef);
      settings.setSpecificValueAsIntByName(omlapi.F_SPEED_CALLING___SUBSCRIBED,
                                           iOutGpSubscrSpdServSubscr);
      settings.setUseDefaultFlagByName(omlapi.F_SPEED_CALLING___ALLOWED_TYPES,
                                       iOutGpSubscrSpdFormUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_SPEED_CALLING___HANDSET_ACCESS_ALLOWED,
                                       iOutGpSubscrSpdAccessUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_CALL_BARRING___SUBSCRIBED,
                                       iOutGpSubscrBarServSubscrUseDef);
      settings.setUseDefaultFlagByName(omlapi.F_MANDATORY_ACCOUNT_CODES___SUBSCRIBED,
                                       iOutGpSubscrMacServSubscrUseDef);

      //-----------------------------------------------------------------------
      // Apply the changes.  This results in the object
      // actually being created.
      //-----------------------------------------------------------------------
      subServSEA.doAction(omlapi.A_APPLY);
      TraceHelper.trace("Created Outgoing call services for Ind Line " + indLineElementName);
    }
    catch (FieldBadValueException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementUnavailableException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementDeletedException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementBrokenException e)
    {
      outputException(errorMessage, e);
    }
    catch (ElementOperationFailedException e)
    {
      outputException(errorMessage, e);
    }
    catch (SettingsFieldException e)
    {
      outputException(errorMessage, e);
    }
    //-------------------------------------------------------------------------
    // None of the remaining exceptions should happen - these indicate
    // programming errors.
    //-------------------------------------------------------------------------
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NameUnknownException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementAlreadyLockedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementChangedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ActionNotEnabledException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (UnknownActionException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    finally
    {
      if (subServSEA != null)
      {
        subServSEA.destroy();
      }
    }
  }

  /**
   * Updates the subscriber services for the subscriber to hardcoded values
   *
   * @param indLineSEA
   *                    SEA attached to the individual line we want to update
   *                    services below.
   *
   */
  public static void updateSubscriberServices(SEAccessInterface indLineSEA)
  {


    String errorMessage = "Failure when updating call forwarding services for Ind Line.";

    SEAccessInterface subServSEA = null;

    try
    {
      try
      {
        subServSEA = indLineSEA.findElement(
                            omlapi.O_INDIVIDUAL_LINE_CALL_FORWARDING_SERVICES);
        StringHolder name = new StringHolder();

        SettingsUserInterface settings =
                      subServSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                             name,
                                             new StringHolder(),
                                             new SequenceOfReferencesHolder());
        settings.setUseDefaultFlagByName(omlapi.F_BUSY_CALL_FORWARDING___SUBSCRIBED,
                                         uFwdGpSubscrBsyServSubscrUseDef);
        settings.setSpecificValueAsIntByName(omlapi.F_BUSY_CALL_FORWARDING___SUBSCRIBED,
                                             uFwdGpSubscrBsyServSubscr);

        //---------------------------------------------------------------------
        // Apply the changes.  This results in the object actually being
        // updated.
        //---------------------------------------------------------------------
        subServSEA.doAction(omlapi.A_APPLY);
        TraceHelper.trace("Updated Call forwarding services for Ind Line.");
      }
      catch (ElementUnavailableException e)
      {
        outputException(errorMessage, e);
      }
      catch (FieldBadValueException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementDeletedException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementBrokenException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementOperationFailedException e)
      {
        outputException(errorMessage, e);
      }
      catch (SettingsFieldException e)
      {
        outputException(errorMessage, e);
      }
      finally
      {
        if (subServSEA != null)
        {
          subServSEA.destroy();
        }
      }

      errorMessage = "Failure when updating caller ID services for Ind Line.";
      try
      {
        subServSEA = indLineSEA.findElement(
                                  omlapi.O_INDIVIDUAL_LINE_CALLER_ID_SERVICES);

        SettingsUserInterface settings =
                      subServSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                             new StringHolder(),
                                             new StringHolder(),
                                             new SequenceOfReferencesHolder());

        settings.setUseDefaultFlagByName(omlapi.F_CALL_TRACE___SUBSCRIBED,
                                         uCidGpSubscrTrcServSubscrUseDef);
        settings.setSpecificValueAsIntByName(omlapi.F_CALL_TRACE,
                                             uCidGpSubscrTrcServSubscr);

        //---------------------------------------------------------------------
        // Apply the changes.  This results in the object actually being
        // updated.
        //---------------------------------------------------------------------
        subServSEA.doAction(omlapi.A_APPLY);
        TraceHelper.trace("Updated Caller ID services for Ind Line.");
      }
      catch (FieldBadValueException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementUnavailableException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementDeletedException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementBrokenException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementOperationFailedException e)
      {
        outputException(errorMessage, e);
      }
      catch (SettingsFieldException e)
      {
        outputException(errorMessage, e);
      }
      finally
      {
        if (subServSEA != null)
        {
          subServSEA.destroy();
        }
      }

      errorMessage = "Failure when updating General Call services for Ind Line.";
      try
      {
        subServSEA = indLineSEA.findElement(
                       omlapi.O_INDIVIDUAL_LINE_GENERAL_CALL_SERVICE_CONTROLS);

        SettingsUserInterface settings =
                      subServSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                             new StringHolder(),
                                             new StringHolder(),
                                             new SequenceOfReferencesHolder());

        settings.setUseDefaultFlagByName(omlapi.F_PIN_CHANGE___SUBSCRIBED,
                                         uGenGpSubscrPinServSubscrUseDef);
        settings.setSpecificValueAsIntByName(omlapi.F_PIN_CHANGE___SUBSCRIBED,
                                             uGenGpSubscrPinServSubscr);

        //---------------------------------------------------------------------
        // Apply the changes.  This results in the object actually being
        // updated.
        //---------------------------------------------------------------------
        subServSEA.doAction(omlapi.A_APPLY);
        TraceHelper.trace("Updated General call services for Ind Line.");
      }
      catch (FieldBadValueException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementUnavailableException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementDeletedException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementBrokenException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementOperationFailedException e)
      {
        outputException(errorMessage, e);
      }
      catch (SettingsFieldException e)
      {
        outputException(errorMessage, e);
      }
      finally
      {
        if (subServSEA != null)
        {
          subServSEA.destroy();
        }
      }

      errorMessage = "Failure when updating Web services for Ind Line.";
      try
      {
        subServSEA = indLineSEA.findElement(
                                        omlapi.O_INDIVIDUAL_LINE_WEB_SERVICES);

        SettingsUserInterface settings =
                      subServSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                             new StringHolder(),
                                             new StringHolder(),
                                             new SequenceOfReferencesHolder());

        //---------------------------------------------------------------------
        // Apply the changes.  This results in the object actually being
        // updated.
        //---------------------------------------------------------------------
        subServSEA.doAction(omlapi.A_APPLY);
        TraceHelper.trace("Updated Web services for Ind Line.");
      }
      catch (ElementUnavailableException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementDeletedException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementBrokenException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementOperationFailedException e)
      {
        outputException(errorMessage, e);
      }
      catch (SettingsFieldException e)
      {
        outputException(errorMessage, e);
      }
      finally
      {
        if (subServSEA != null)
        {
          subServSEA.destroy();
        }
      }

      errorMessage = "Failure when updating Incoming Call services for Ind Line.";
      try
      {
        subServSEA = indLineSEA.findElement(
                              omlapi.O_INDIVIDUAL_LINE_INCOMING_CALL_SERVICES);

        SettingsUserInterface settings =
                      subServSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                             new StringHolder(),
                                             new StringHolder(),
                                             new SequenceOfReferencesHolder());

        settings.setUseDefaultFlagByName(omlapi.F_ANONYMOUS_CALL_REJECTION___SUBSCRIBED,
                                         uIncGpSubscrAnrServSubscrUseDef);
        settings.setSpecificValueAsIntByName(omlapi.F_ANONYMOUS_CALL_REJECTION___SUBSCRIBED,
                                             uIncGpSubscrAnrServSubscr);

        //---------------------------------------------------------------------
        // Apply the changes.  This results in the object actually being
        // updated.
        //---------------------------------------------------------------------
        subServSEA.doAction(omlapi.A_APPLY);
        TraceHelper.trace("Updated Incoming call services for Ind Line.");
      }
      catch (FieldBadValueException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementUnavailableException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementDeletedException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementBrokenException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementOperationFailedException e)
      {
        outputException(errorMessage, e);
      }
      catch (SettingsFieldException e)
      {
        outputException(errorMessage, e);
      }
      finally
      {
        if (subServSEA != null)
        {
          subServSEA.destroy();
        }
      }
      errorMessage = "Failure when updating Message services for Ind Line.";
      try
      {
        subServSEA = indLineSEA.findElement(
                                    omlapi.O_INDIVIDUAL_LINE_MESSAGE_SERVICES);

        SettingsUserInterface settings =
                      subServSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                             new StringHolder(),
                                             new StringHolder(),
                                             new SequenceOfReferencesHolder());

        settings.setUseDefaultFlagByName(omlapi.F_REMINDER_CALLS___SUBSCRIBED,
                                         uMsgGpSubscrRemServSubscrUseDef);
        settings.setSpecificValueAsIntByName(omlapi.F_REMINDER_CALLS___SUBSCRIBED,
                                             uMsgGpSubscrRemServSubscr);

        //---------------------------------------------------------------------
        // Apply the changes.  This results in the object actually being
        // updated.
        //---------------------------------------------------------------------
        subServSEA.doAction(omlapi.A_APPLY);
        TraceHelper.trace("Updated Message services for Ind Line.");
      }
      catch (FieldBadValueException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementUnavailableException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementDeletedException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementBrokenException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementOperationFailedException e)
      {
        outputException(errorMessage, e);
      }
      catch (SettingsFieldException e)
      {
        outputException(errorMessage, e);
      }
      finally
      {
        if (subServSEA != null)
        {
          subServSEA.destroy();
        }
      }
      errorMessage = "Failure when updating Multi-Party call services for Ind Line.";
      try
      {
        subServSEA = indLineSEA.findElement(
                           omlapi.O_INDIVIDUAL_LINE_MULTI_PARTY_CALL_SERVICES);

        SettingsUserInterface settings =
                      subServSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                             new StringHolder(),
                                             new StringHolder(),
                                             new SequenceOfReferencesHolder());

        settings.setUseDefaultFlagByName(omlapi.F_CALL_WAITING___SUBSCRIBED,
                                         uTrnGpSubscrCwtServSubscrUseDef);
        settings.setSpecificValueAsIntByName(omlapi.F_CALL_WAITING___SUBSCRIBED,
                                             uTrnGpSubscrCwtServSubscr);

        //---------------------------------------------------------------------
        // Apply the changes.  This results in the object actually being
        // updated.
        //---------------------------------------------------------------------
        subServSEA.doAction(omlapi.A_APPLY);
        TraceHelper.trace("Updated Multi-party call services for Ind Line.");
      }
      catch (FieldBadValueException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementUnavailableException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementDeletedException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementBrokenException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementOperationFailedException e)
      {
        outputException(errorMessage, e);
      }
      catch (SettingsFieldException e)
      {
        outputException(errorMessage, e);
      }
      finally
      {
        if (subServSEA != null)
        {
          subServSEA.destroy();
        }
      }
      errorMessage = "Failure when updating Outgoing call services for Ind Line.";
      try
      {
        subServSEA = indLineSEA.findElement(
                              omlapi.O_INDIVIDUAL_LINE_OUTGOING_CALL_SERVICES);

        SettingsUserInterface settings =
                      subServSEA.getSnapshot(new SequenceOfIntegersHolder(),
                                             new StringHolder(),
                                             new StringHolder(),
                                             new SequenceOfReferencesHolder());

        settings.setUseDefaultFlagByName(omlapi.F_CALL_BARRING___SUBSCRIBED,
                                         uOutGpSubscrBarServSubscrUseDef);
        settings.setSpecificValueAsIntByName(omlapi.F_CALL_BARRING___SUBSCRIBED,
                                             uOutGpSubscrBarServSubscr);

        //---------------------------------------------------------------------
        // Apply the changes.  This results in the object actually being
        // updated.
        //---------------------------------------------------------------------
        subServSEA.doAction(omlapi.A_APPLY);
        TraceHelper.trace("Updated Outgoing call services for Ind Line.");
      }
      catch (FieldBadValueException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementUnavailableException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementDeletedException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementBrokenException e)
      {
        outputException(errorMessage, e);
      }
      catch (ElementOperationFailedException e)
      {
        outputException(errorMessage, e);
      }
      catch (SettingsFieldException e)
      {
        outputException(errorMessage, e);
      }
    //-------------------------------------------------------------------------
    // None of the remaining exceptions should happen - these indicate
    // programming errors.
    //-------------------------------------------------------------------------
    }
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NameUnknownException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementAlreadyLockedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementChangedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ActionNotEnabledException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (UnknownActionException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    finally
    {
      if (subServSEA != null)
      {
        subServSEA.destroy();
      }
    }
  }

  /**
   * Deletes a SE deactivating it first if required.  SE Access is deleted in
   * this function so must not be used after calling this function.
   *
   * @param seAccess    seAccess of object being deleted
   * @param deactivateReqd
   *                    whether to deactivate the object before deletion or not
   *
   * @exception ElementUnavailableException
   */
  public static void deleteSE(SEAccessInterface seAccess, boolean deactivateReqd)
  throws ElementUnavailableException
  {
    String errorMessage = "Failure when deleting an object";
    try
    {

      //-----------------------------------------------------------------------
      // Deactivate the Object.  Note that deactivation is an asynchronous
      // operation, and this method will block until deactivation completes.
      // Individual lines do not need to be deactivated before deletion
      //-----------------------------------------------------------------------
      if (deactivateReqd)
      {
        TraceHelper.trace("  Deactivating element");

        deactivateElement(seAccess);

        TraceHelper.trace("  Deactivated element");
      }
      //-----------------------------------------------------------------------
      // Disable and delete the Object.
      //-----------------------------------------------------------------------
      disableAndDeleteElement(seAccess);

      TraceHelper.trace("  Deleted element");
    }
    finally
    {
      //-----------------------------------------------------------------------
      // Destroy the SEAccess.
      //-----------------------------------------------------------------------
      if (seAccess != null)
      {
        seAccess.destroy();
      }
    }
  }

  /**
   * Disables and deletes the specified element.
   *
   * @param seAccess    An SEAccess that is attached to the Individual Line,
   *                    Access Device or IDT that should be deleted.
   *
   * @exception ElementUnavailableException
   */
  private static void disableAndDeleteElement(SEAccessInterface seAccess)
    throws ElementUnavailableException
  {
    try
    {
      //-----------------------------------------------------------------------
      // Get a snapshot of the element.  We must have the up-to-date Settings
      // for this SE before we perform an action on it (otherwise we'll get an
      // ElementChangedException).
      //-----------------------------------------------------------------------
      seAccess.getSnapshot(new SequenceOfIntegersHolder(),
                           new StringHolder(),
                           new StringHolder(),
                           new SequenceOfReferencesHolder());

      //-----------------------------------------------------------------------
      // The element has to be disabled before it can be deleted.
      //-----------------------------------------------------------------------
      seAccess.doAction(omlapi.A_DISABLE);

      //-----------------------------------------------------------------------
      // Get a snapshot of the element.  We must have the up-to-date Settings
      // for this SE before we perform an action on it (otherwise we'll get an
      // ElementChangedException).
      //-----------------------------------------------------------------------
      seAccess.getSnapshot(new SequenceOfIntegersHolder(),
                           new StringHolder(),
                           new StringHolder(),
                           new SequenceOfReferencesHolder());

      //-----------------------------------------------------------------------
      // Delete the element.
      //-----------------------------------------------------------------------
      seAccess.doAction(omlapi.A_DELETE);
    }
    catch (ElementDeletedException e)
    {
      //-----------------------------------------------------------------------
      // This exception will be thrown if the element does not exist.  If this
      // occurs, there is no problem, because we're trying to delete it anyway.
      //-----------------------------------------------------------------------
      TraceHelper.trace("Element already deleted!");
    }
    //-------------------------------------------------------------------------
    // All other CORBA UserExceptions are unexpected in this case (apart from
    // ElementUnavailableException).
    //-------------------------------------------------------------------------
    catch (ElementBrokenException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementOperationFailedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementChangedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementAlreadyLockedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (SettingsFieldException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (UnknownActionException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ActionNotEnabledException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
  }

  /**
   * Deactivates the specified element.  This method will block until the
   * element has become inactive, which may take some time as deactivation is
   * an asynchronous process.
   *
   * @param seAccess    An SEAccess that is attached to the Access Device or
   *                    IDT that should be deactivated.
   *
   * @exception ElementUnavailableException
   */
  private static void deactivateElement(SEAccessInterface seAccess)
    throws ElementUnavailableException
  {
    try
    {
      //-----------------------------------------------------------------------
      // Create a snapshot change listener object to register with the SE.  We
      // need to do this because the deactivate action that we are about to
      // perform may complete asynchronously, so we might need to wait until we
      // receive a snapshot that has the Disable action enabled (indicating
      // that the deactivation has completed) before returning.
      //-----------------------------------------------------------------------
      SnapshotChangeListenerInterfacePOA snapshotChangeListener =
        new SnapshotChangeListenerInterfacePOA()
        {
          Thread ownerThread = Thread.currentThread();

          public void newSnapshotAvailable(SEAccessInterface seAccess,
                                           boolean fundamental)
          {
            try
            {
              TraceHelper.trace("      [received snapshot change "
                                + "notification]");

              //---------------------------------------------------------------
              // When a new snapshot is available, we get the value of the
              // Actual status field and check that it is Inactive.  If it is,
              // we notify the thread that is waiting for the deactivation to
              // complete.
              //---------------------------------------------------------------
              boolean actualStatusInactive = isActualStatusInactive(seAccess);
              if (actualStatusInactive)
              {
                //-------------------------------------------------------------
                // Interrupt the thread that is waiting for the deactivation to
                // complete (see below).
                //-------------------------------------------------------------
                TraceHelper.trace("      [actual status inactive, finished "
                                  + "deactivation]");
                ownerThread.interrupt();
              }
              else
              {
                //-------------------------------------------------------------
                // Wait for the next snapshot change notification.
                //-------------------------------------------------------------
                TraceHelper.trace("      [actual status not inactive]");
              }
            }
            catch (ElementUnavailableException e)
            {
              //---------------------------------------------------------------
              // If the element has become unavailable when we try to get the
              // snapshot, give up and wait for the next snapshot change
              // notification.
              //---------------------------------------------------------------
            }
          }
        };

      //-----------------------------------------------------------------------
      // Get a CORBA reference for our snapshot change listener and register it
      // with the SE to receive snapshot notifications.
      //-----------------------------------------------------------------------
      SnapshotChangeListenerInterface snapshotChangeListenerCorbaRef =
                            snapshotChangeListener._this(CorbaHelper.getORB());
      seAccess.addSnapshotChangeListener(snapshotChangeListenerCorbaRef);

      //-----------------------------------------------------------------------
      // Get a snapshot of the object.  We must have the up-to-date Settings
      // for this SE before we perform an action on it (otherwise we'll get an
      // ElementChangedException).
      //-----------------------------------------------------------------------
      seAccess.getSnapshot(new SequenceOfIntegersHolder(),
                           new StringHolder(),
                           new StringHolder(),
                           new SequenceOfReferencesHolder());

      //-----------------------------------------------------------------------
      // Deactivate the object.
      //-----------------------------------------------------------------------
      seAccess.doAction(omlapi.A_DEACTIVATE);

      //-----------------------------------------------------------------------
      // Check whether the action has completed immediately - if it has, we can
      // return immediately.  Otherwise, we need to wait until the snapshot
      // change listener interrupts us.  The Deactivate action is complete when
      // the Disable action becomes available.
      //-----------------------------------------------------------------------
      boolean actualStatusInactive = isActualStatusInactive(seAccess);
      if (!actualStatusInactive)
      {
        //---------------------------------------------------------------------
        // The deactivation did not complete immediately, so wait for the
        // snapshot change listener to interrupt us, then return.  If the
        // operation takes more than 30 seconds, then something has probably
        // gone wrong.
        //---------------------------------------------------------------------
        try
        {
          TraceHelper.trace("    Waiting for deactivation to complete...");
          Thread.currentThread().sleep(30000);
          TraceHelper.trace("    Deactivation failed to complete in 30 "
                            + "seconds!");
        }
        catch (InterruptedException e)
        {
          //-------------------------------------------------------------------
          // We assume that the only thread that will interrupt us is the one
          // from the snapshot change notification.
          //-------------------------------------------------------------------
          TraceHelper.trace("    Deactivation complete.");
        }
      }

      //-----------------------------------------------------------------------
      // Remove the snapshot change listener, as we are no longer interested in
      // snapshot changes.
      //-----------------------------------------------------------------------
      seAccess.removeSnapshotChangeListener();
    }
    //-------------------------------------------------------------------------
    // All CORBA UserExceptions are unexpected in this case (apart from
    // ElementUnavailableException).
    //-------------------------------------------------------------------------
    catch (ElementDeletedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementBrokenException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementOperationFailedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementChangedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementAlreadyLockedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (LockTimeoutException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (SettingsFieldException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (UnknownActionException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ActionNotEnabledException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (TooManySnapshotChangeListenersException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NoSnapshotChangeListenersException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
  }

  /**
   * Perform a getSnapshot on the SEAccess provided, and check whether the
   * value of the Actual status field is Inactive.
   *
   * @returns           true if the Actual status is Inactive, false otherwise.
   *
   * @param seAccess    The SEAccessInterface to call getSnapshot on.
   *
   * @exception ElementUnavailableException
   */
  private static boolean isActualStatusInactive(SEAccessInterface seAccess)
    throws ElementUnavailableException
  {
    boolean actualStatusInactive = false;

    try
    {
      //-----------------------------------------------------------------------
      // Get the latest snapshot from the SEAccess.
      //-----------------------------------------------------------------------
      SettingsUserInterface settings =
                        seAccess.getSnapshot(new SequenceOfIntegersHolder(),
                                             new StringHolder(),
                                             new StringHolder(),
                                             new SequenceOfReferencesHolder());

      BooleanHolder isAssigned = new BooleanHolder();
      int actualStatus = settings.getFieldAsIntByName(omlapi.F_ACTUAL_STATUS,
                                                      isAssigned);
      actualStatusInactive = (isAssigned.value &&
                              (actualStatus == omlapi.V_INACTIVE_2));
    }
    //-------------------------------------------------------------------------
    // All CORBA UserExceptions are unexpected in this case (apart from
    // ElementUnavailableException).
    //-------------------------------------------------------------------------
    catch (ElementDeletedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (ElementBrokenException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (NotAttachedException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldNameOrIndexNotFoundException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }
    catch (FieldBadTypeException e)
    {
      CorbaHelper.handleUnexpectedUserException(e);
    }

    return actualStatusInactive;
  }

  /**
   * Trace out details of a normal "handled" exception (i.e.  one that can
   * validly be thrown).  For unexpected exceptions, call
   * CorbaHelper.handleUnexpectedUserException()
   *
   * @param errorText   Descriptive text explaining the scenario in which the
   *                    exception was caught.
   * @param e           The exception.
   */
  public static void outputException(String errorText, Exception e)
  {
    TraceHelper.trace(errorText
                      + "\nException details: "
                      + "\n  Exception: " + e.toString()
                      + "\n  Message: " + e.getMessage());
  }

}
