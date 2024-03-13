/**
 * Title: CorbaHelperSecure
 *
 * Description: A utility class providing methods to start an Jacorb ORB in
 * secure mode.
 *
 * (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
 *
 * @version 1.1
 */
package com.Metaswitch.MVS.UtilsSecure;

import org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE;
import org.omg.BiDirPolicy.BOTH;
import org.omg.BiDirPolicy.BidirectionalPolicyValueHelper;
import com.Metaswitch.MVS.Corba.*;
import com.Metaswitch.MVS.Utils.*;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.*;
import org.omg.CORBA.*;

import iaik.x509.X509Certificate;

import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;

public class CorbaHelperSecure extends CorbaHelper
{
  //---------------------------------------------------------------------------
  // The filenames of the security certificates which will be used by default,
  // if no certificate and key filenames are specified by the user.  There is
  // no CA key filename because the CA key is never used.
  //---------------------------------------------------------------------------
  private static final String CLIENT_CERT_FILENAME = "MVSClient.der";
  private static final String CA_CERT_FILENAME = "MVSCA.der";
  private static final String CLIENT_KEY_FILENAME = "MVSClient.key";

  //---------------------------------------------------------------------------
  // A comma-separated list of TLS protocols which the client will offer.
  // Valid values are:
  //   - SSLv3
  //   - TLSv1
  //   - TLSv1.1
  //   - TLSv1.2
  //---------------------------------------------------------------------------
  private static final String TLS_PROTOCOLS_SUPPORTED = "TLSv1.2";

  //---------------------------------------------------------------------------
  // The MVS client entry name.
  //---------------------------------------------------------------------------
  private static final String MVS_CLIENT_ENTRY = "MVSClientEntry";

  //---------------------------------------------------------------------------
  // The client key pass phrase.
  // If using the default MVE certificates, then retrieve this from a secure
  // craft administrator.
  // If using custom certificates - this is the key used to encrypt your
  // private key file.
  //---------------------------------------------------------------------------
  private static final String CLIENT_KEY_PASS_PHRASE = "<CHANGE_THIS>";

  //---------------------------------------------------------------------------
  // The MVS Client key store file.
  //---------------------------------------------------------------------------
  private static final String MVS_CLIENT_KEYSTORE = "ClientMVS";

  //---------------------------------------------------------------------------
  // There are two different possible ways of logging into an MVS, both of
  // which are supported by this utility class:
  //
  // -  A secure user login.  The user will need to provide a username and
  //    password.  CorbaHelperSecure will use the default client certificates,
  //    since the security in this case comes from the username and password
  //    rather than the certificates.
  // -  A secure application login.  The user needs to supply secure
  //    certificates.  No username and password are required.
  //
  // The overloaded startORB method below can be used to make either of these
  // connection types - the comments with each method declaration indicate what
  // type of connection will be made.
  //
  // To perform an insecure user login, the startORB method in the CorbaHelper
  // superclass should be called.
  //
  // Note that insecure application logins are not possible.
  //---------------------------------------------------------------------------

  /**
   * Start the ORB in preparation for a user login, either secure or insecure.
   * Calls the main startOrb method below, setting the 'secure' parameter as
   * appropriate, and setting 'isUserLogin' to true.  All other parameters on
   * the main method are irrelevant.
   *
   * @param secure      Indicates whether to prepare for a secure user login
   *                    (if set to true), or an insecure login.
   */
  public static void startORB(boolean secure)
  {
    startORB(secure, true, null, null, null);
  }

  /**
   * Start the ORB in preparation for an application login.  The fact that
   * three parameters are passed indicates that an application login is
   * desired.  Calls the main startORB method below, setting 'secure' to true
   * and 'isUserLogin' to false, and passing the certificate and key filenames.
   *
   * @param caCertFilename
   *                    CA certificate filename.
   * @param clientCertFilename
   *                    Client certificate filename.
   * @param clientKeyFilename
   *                    Client key filename.
   */
  public static void startORB(String caCertFilename,
                              String clientCertFilename,
                              String clientKeyFilename)
  {
    startORB(true,
             false,
             caCertFilename,
             clientCertFilename,
             clientKeyFilename);
  }

  /**
   * Start the ORB.  No CORBA request will work until an ORB is started.
   *
   * @param secure      Indicates whether a secure connection should be made,
   *                    using the Jacorb ORB, or an insecure connection using
   *                    the Sun ORB.
   * @param isUserLogin
   *                    Indicates that a user login rather than an application
   *                    login will be performed. If false, the certificate and
   *                    key filenames will be used - if true they are ignored.
   * @param caCertFilename
   *                    The filename of the Certificate Authority (CA)
   *                    certificate
   * @param clientCertFilename
   *                    The filename of the client certificate
   * @param clientKeyFilename
   *                    The filename of the client key file
   */
  public static void startORB(boolean secure,
                              boolean isUserLogin,
                              String caCertFilename,
                              String clientCertFilename,
                              String clientKeyFilename)
  {
    //-------------------------------------------------------------------------
    // Set static variables using the supplied parameters, to make them
    // available to all other methods.
    //-------------------------------------------------------------------------
    CorbaHelper.sSecure = secure;
    CorbaHelper.sIsUserLogin = isUserLogin;

    synchronized (CorbaHelper.sOrbLock)
    {
      if (!CorbaHelper.sOrbRunning)
      {
        //---------------------------------------------------------------------
        // Use different initialising code depending on whether we are in
        // secure or insecure mode.
        //---------------------------------------------------------------------
        try
        {
          if (CorbaHelper.sSecure)
          {
            //-----------------------------------------------------------------
            // The process of setting up a secure ORB involves a number of
            // stages:
            //
            // -  Initialise in secure mode, using the Jacorb ORB.  See
            //    Jacorb documentation for details, but the initialisation has
            //    three phases:
            //   -  Load the security certificate files, and create a chaing of
            //      SSL certificated.
            //   -  Call ORB.init() with properties specifying various
            //      behaviour options including the required communication
            //      protocols.  We use the bidirectional SSL inter-ORB
            //      protocol.
            //   -  Create and activate the POA manager.
            // -  When this is complete, the ORB will be ready to use to
            //    resolve remote interfaces, and it will communicate securely
            //    with the remote host.
            //-----------------------------------------------------------------

            //-----------------------------------------------------------------
            // Initialise in secure mode, using the Jacorb ORB.
            // First set up the ORB properties - see Jacorb documentation for
            // details.
            //-----------------------------------------------------------------
            java.util.Properties props = System.getProperties();
            props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.setProperty("org.omg.CORBA.ORBSingletonClass",
                              "org.jacorb.orb.ORBSingleton");
            props.setProperty("jacorb.security.support_ssl", "on");
            props.setProperty("jacorb.security.ssl.client.supported_options",
                              "60");
            props.setProperty("jacorb.security.iaik_debug", "off");
            props.setProperty("jacorb.ssl.socket_factory",
                              "org.jacorbOverwrite.iaik.OvwrSSLSocketFactory");
            props.setProperty(
                          "jacorb.ssl.server_socket_factory",
                          "org.jacorb.orb.factory.DefaultServerSocketFactory");
            props.setProperty("jacorb.security.default_user",
                              MVS_CLIENT_ENTRY);
            props.setProperty("jacorb.security.default_password",
                              CLIENT_KEY_PASS_PHRASE);
            props.setProperty(
                  "jacorb.security.principal_authenticator",
                  "org.jacorbOverwrite.level2.OvwrPrincipalAuthenticatorImpl");
            props.setProperty(
                             "jacorb.transport.factories",
                             "org.jacorbOverwrite.orb.iiop.OvwrIIOPFactories");
            props.setProperty("jacorb.security.ssl.client.protocols",
                              TLS_PROTOCOLS_SUPPORTED);
            props.setProperty("jacorb.native_wchar_codeset", "UCS2");
            props.setProperty("jacorb.poa.thread_pool_max", "10");
            props.setProperty(
                "org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
                "org.jacorbOverwrite.orb.iiop.OvwrBiDirConnectionInitializer");
            props.setProperty(
                        "jacorb.transport.client.selector",
                        "org.jacorbOverwrite.orb.JacorbClientProfileSelector");

            //-----------------------------------------------------------------
            // Create the POA Manager object.
            //-----------------------------------------------------------------
            org.omg.PortableServer.POAManager poaManager = null;

            //-----------------------------------------------------------------
            // Set any unset filenames to their defaults.  The CA key filename
            // is not set as it is never actually used.
            //-----------------------------------------------------------------
            if (caCertFilename == null)
            {
              caCertFilename = CA_CERT_FILENAME;
            }

            if (clientCertFilename == null)
            {
              clientCertFilename = CLIENT_CERT_FILENAME;
            }

            if (clientKeyFilename == null)
            {
              clientKeyFilename = CLIENT_KEY_FILENAME;
            }

            //-----------------------------------------------------------------
            // Load the client certificate.
            //-----------------------------------------------------------------
            KeyStore keyStore = null;
            if(Security.getProvider("IAIK") == null)
            {
              Provider provider = new iaik.security.provider.IAIK();
              Security.addProvider(provider);
            }

            props.setProperty("jacorb.security.keystore_password",
                              CLIENT_KEY_PASS_PHRASE);
            props.setProperty("jacorb.security.keystore", MVS_CLIENT_KEYSTORE);

            CertificateFactory certificateFactory = null;

            certificateFactory = CertificateFactory.getInstance("X509");
            X509Certificate clientCert = null;
            FileOutputStream fos = new FileOutputStream(MVS_CLIENT_KEYSTORE);

            keyStore = KeyStore.getInstance("IAIKKeyStore", "IAIK");
            keyStore.load(null, CLIENT_KEY_PASS_PHRASE.toCharArray());

            try (FileInputStream clientCertInput = 
                                       new FileInputStream(clientCertFilename))
            {
              clientCert = new X509Certificate(
                            certificateFactory.generateCertificate(clientCertInput)
                                              .getEncoded());
            }
            catch (BAD_PARAM e)
            {
              //---------------------------------------------------------------
              // Couldn't find the client certificate file.
              //---------------------------------------------------------------
              throw new IllegalStateException(
                                "Error while opening client certificate file '"
                                + clientCertFilename + "'\n"
                                + " - check that the filename is valid.");
            }
            catch (IOException e)
            {
              throw new IllegalStateException(
                                    "Failed to close client certificate file '"
                                    + clientCertFilename + "'");
            }
              

            //-----------------------------------------------------------------
            // Load the CA certificate.
            //-----------------------------------------------------------------
            X509Certificate caCert = null;

            try (FileInputStream caCertInput = new FileInputStream(caCertFilename))
            {
              caCert = new X509Certificate(
                            certificateFactory.generateCertificate(caCertInput)
                                              .getEncoded());
            }
            catch (BAD_PARAM e)
            {
              //---------------------------------------------------------------
              // Couldn't find the CA certificate file.
              //---------------------------------------------------------------
              throw new IllegalStateException(
                                    "Error while opening CA certificate file '"
                                    + caCertFilename + "'\n"
                                    + " - check that the filename is valid.");
            }
            catch (IOException e)
            {
              throw new IllegalStateException(
                                        "Failed to close CA certificate file '"
                                        + caCertFilename + "'");
            }

            //-----------------------------------------------------------------
            // Create the certificate chain.  The secure Jacorb ORB requires
            // at least two certificates in this chain in order to communicate
            // with the server.
            //-----------------------------------------------------------------
            Certificate[] chain = new Certificate[2];
            chain[0] = clientCert;
            chain[1] = caCert;

            //-----------------------------------------------------------------
            // A hard-coded, easily guessable pass-phrase is used,
            // because a pass-phrase is required by the FSSL package but does
            // not add any security.
            //-----------------------------------------------------------------
            try
            {
              keyStore.setKeyEntry(MVS_CLIENT_ENTRY,
                                   loadFileAsByteArray(clientKeyFilename),
                                   chain);
              keyStore.store(fos, CLIENT_KEY_PASS_PHRASE.toCharArray());
              KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                                      KeyManagerFactory.getDefaultAlgorithm());
              kmf.init(keyStore, CLIENT_KEY_PASS_PHRASE.toCharArray());
              TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(
                                    TrustManagerFactory.getDefaultAlgorithm());
              tmf.init(keyStore);

              fos.close();
            }
            catch (org.omg.CORBA.BAD_PARAM e)
            {
              //---------------------------------------------------------------
              // This error is thrown if any of the filenames given are
              // incorrect.  However, if either of the certificate filenames
              // was incorrect an exception would have been thrown earlier.
              // Reaching this exception therefore indicates that the key
              // filename is incorrect.
              //---------------------------------------------------------------
              throw new IllegalStateException("The security key filename is "
                                            + "invalid: " + clientKeyFilename);
            }

            //-----------------------------------------------------------------
            // Initialise the ORB.
            //-----------------------------------------------------------------
            CorbaHelper.sOrb = ORB.init(new String[]{}, props);

            //-----------------------------------------------------------------
            // Create the POA Manager.
            //-----------------------------------------------------------------
            POA rootPOA =
              POAHelper.narrow(
                       CorbaHelper.sOrb.resolve_initial_references("RootPOA"));

            Any any = CorbaHelper.sOrb.create_any();
            BidirectionalPolicyValueHelper.insert(any, BOTH.value);
            Policy[] policies = new Policy[4];
            policies[0] = rootPOA.create_lifespan_policy(
                                                LifespanPolicyValue.TRANSIENT);
            policies[1] = rootPOA.create_id_assignment_policy(
                                            IdAssignmentPolicyValue.SYSTEM_ID);
            policies[2] = rootPOA.create_implicit_activation_policy(
                            ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);
            policies[3] = CorbaHelper.sOrb.create_policy(
                                               BIDIRECTIONAL_POLICY_TYPE.value,
                                               any);
            POA bidir_poa = rootPOA.create_POA("BiDirPOA",
                                               rootPOA.the_POAManager(),
                                               policies);
            poaManager = bidir_poa.the_POAManager();

            try
            {
              //---------------------------------------------------------------
              // Activate the Root POA manager.
              //---------------------------------------------------------------
              poaManager.activate();
            }
            catch (org.omg.PortableServer.POAManagerPackage.AdapterInactive e)
            {
              //---------------------------------------------------------------
              // Error - adapter inactive.
              //---------------------------------------------------------------
              throw new IllegalStateException("Adapter inactive");
            }
          }
          else
          {
            //-----------------------------------------------------------------
            // Insecure mode.
            //-----------------------------------------------------------------
            if (!isUserLogin)
            {
              //---------------------------------------------------------------
              // User is attempting to perform an application login in insecure
              // mode - this won't work, so throw an exception.
              //---------------------------------------------------------------
              throw new IllegalStateException(
                "Error - an application login cannot be performed in insecure "
                  + "mode. Either:\n"
                  + " - Perform a user login, supplying a username and "
                  + "password, or\n"
                  + " - Login in secure mode");
            }

            //-----------------------------------------------------------------
            // Call the insecure method in the superclass, CorbaHelper.
            //-----------------------------------------------------------------
            CorbaHelper.startORB();
          }
        }
        catch (Exception e)
        {
          //-------------------------------------------------------------------
          // Any exceptions not caught elsewhere in the code above are caught
          // here.  If it is an IllegalStateException then we should throw it
          // again - if not, it is an unexpected error, so we should print the
          // stack trace.
          //-------------------------------------------------------------------
          if (e instanceof IllegalStateException)
          {
            throw (IllegalStateException)e;
          }
          else
          {
            e.printStackTrace();
            throw new IllegalStateException("ORB Initialization failed");
          }
        }
      }
    }
  }

  /**
   * Loads a file as a byte array.
   *
   * @param fileName  The file name to be opened.
   *
   * @return  The array bytes with the file content.
   *
   * @throws IOException  Whether we get any error reading the file.
   */
  private static byte[] loadFileAsByteArray(String fileName) throws IOException
  {
    RandomAccessFile file = new RandomAccessFile(fileName, "r");
    byte[] data = new byte[(int) file.length()];
    file.readFully(data);
    file.close();
    return data;
  }
}
