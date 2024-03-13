/**
 * Title: Command Line Parser
 *
 * Description: Utility method to parse arguments from the command line
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

public class CommandLineParser
{
  public static void parse(String[] args, CommandLineParam[] parameters)
  throws ParamBadFormat123Exception
  {
    int jj = 0;
    boolean found = false;
    //-------------------------------------------------------------------------
    // Loop around args setting up the command line parameters
    //-------------------------------------------------------------------------
    for (int ii = 0; ii < args.length; ii++)
    {
      while (jj < parameters.length && !found)
      {
        if (args[ii].charAt(1) == parameters[jj].mCommandLineOption)
        {
          parameters[jj].parseParam(args[ii]);
          found = true;
        }
        jj++;
      }
      if (!found)
      {
        throw new ParamBadFormat123Exception();
      }
      found = false;
    }
    return;
  }

  public static String parseParam(String option, String[] args)
    throws BadParamException
  {
    String param = null;

    for (int ii=0; ii<args.length; ii++)
    {
      if (args[ii].startsWith(option))
      {
        param = args[ii].substring(option.length());
        break;
      }
    }

    if (param == null)
    {
      throw new BadParamException("Missing parameter " + option);
    }

    return param;
  }
}





