package ProvAPISampleApp;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import ProvAPISampleApp.ProvApiRequestHandler.Operation;

public class ProvAPISampleApp extends Thread
{
  ProvApiRequestHandler mHandler;
  BigInteger mDN;
  LinkedList<String> mTemplateList;
  String mCFS;
  public boolean mFinished = false;

  public ProvAPISampleApp(String mvs,
                          String cfs,
                          String username,
                          String password,
                          BigInteger dn,
                          LinkedList<String> templateList)
  {
    mHandler = new ProvApiRequestHandler(mvs, username, password);
    mDN = dn;
    mCFS = cfs;
    mTemplateList = templateList;
  }

  private static void printUsage()
  {
    String usageStr = "";
    usageStr += "Usage is provapi <mvs> <cfs> <username> <password> <dn> [templates]\n";
    usageStr += "<mvs> - the MVS to send provisioning requests to.\n";
    usageStr += "<cfs> - the CFS to provision subscribers on.\n";
    usageStr += "<username> - the user on MetaView Web.\n";
    usageStr += "<password> - password for the user on MetaView Web.\n";
    usageStr += "<dn> - directory number to provision.\n";
    usageStr += "[templates] - templates list to use (optional).\n";
    System.out.print(usageStr);
  }

  public static void main(String[] args)
  {
    /*
     * Parse the command line arguments.
     */
    String mvs = "";
    String cfs = "";
    String username = "";
    String password = "";
    String dn = "";
    BigInteger directoryNumber;
    LinkedList<String> templateList = new LinkedList<String>();

    if (args.length < 5)
    {
      printUsage();
      System.out.print(Arrays.toString(args));
      return;
    }
    else
    {
      mvs = args[0];
      cfs = args[1];
      username = args[2];
      password = args[3];
      dn = args[4];

      try
      {
        directoryNumber = new BigInteger(dn);
      }
      catch (NumberFormatException e)
      {
        printUsage();
        System.out.print(Arrays.toString(args));
        return;
      }

      for (int ii = 5; ii < args.length; ii++)
      {
        templateList.add(args[ii]);
      }
    }

    ProvAPISampleApp app = new ProvAPISampleApp(mvs,
                                                cfs,
                                                username,
                                                password,
                                                directoryNumber,
                                                templateList);
    long totalStartTime = System.currentTimeMillis();
    app.start();

    while (!app.mFinished)
    {
      try
      {
        Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }

    long totalEndTime = System.currentTimeMillis();

    System.out.println("Total time " + (totalStartTime - totalEndTime) + "ms");
  }

  public void run()
  {
    HashMap<String, Object> valueMap = new HashMap<String, Object>();

    valueMap.put("MetaSphere CFS", mCFS);

    if (mTemplateList.isEmpty())
    {
      valueMap.put("CFS Persistent Profile", "None");
      valueMap.put("Call Feature Server signaling type", "SIP");
      valueMap.put("Use phone number for SIP user name", true);
      valueMap.put("SIP authentication required", false);
      valueMap.put("SIP domain name", "1.2.3.4");
      valueMap.put("CFS Subscriber Group", "Payphones in Guernsey, NJ");
      valueMap.put("CFS Number status", "Normal");
    }

    try
    {
      Thread.sleep(1000);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }

    long startTime = System.currentTimeMillis();
    try
    {
      mHandler.processRequest(mDN.toString(),
                              Operation.CREATE,
                              mTemplateList,
                              valueMap);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    try
    {
      mHandler.processRequest(mDN.toString(),
                              Operation.UPDATE,
                              mTemplateList,
                              valueMap);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    //System.out.println("Total time " + (totalStartTime - totalEndTime) + "ms");
    mFinished = true;
  }
}
