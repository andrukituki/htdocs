
/**
 * Title: Int Command Line Param
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

public class IntCommandLineParam extends CommandLineParam
{
  public int mDefaultValue;
  public int mValue;

  //---------------------------------------------------------------------------
  // Constructor
  //---------------------------------------------------------------------------
  public IntCommandLineParam(char commandLineOption,
                                boolean isNoValueParam,
                                boolean hasDefault,
                                int defaultValue)
  {
    super(commandLineOption, isNoValueParam, hasDefault);
    mDefaultValue = defaultValue;

    if (mHasDefault)
    {
      mFound = true;
      mValue = mDefaultValue;
    }
  }

  public void parseParam(String arg)  throws ParamBadFormat123Exception
  {
    //-------------------------------------------------------------------------
    // Set up mValue
    //-------------------------------------------------------------------------
    int optionLength = 3;

    if (!mIsNoValueParam)
    {
      String sub = arg.substring(2,3);
      if (sub.equals(":"))
      {
        mFound = true;
        mValue = Integer.parseInt(arg.substring(optionLength));
      }
      else
      {
        throw new ParamBadFormat123Exception();
      }
    }
    else
    {
      throw new ParamBadFormat123Exception();
    }
    return;
  }
}
