
/**
 * Title: Command Line Parser
 *
 * Description: Utility method to set up command line parameters
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

public class CommandLineParam
{
  //---------------------------------------------------------------------------
  // Letter to look for in the args
  //---------------------------------------------------------------------------
  public char mCommandLineOption;

  //---------------------------------------------------------------------------
  // Set to true if there is no value associated with the param (i.e.  it
  // appears in the args as "-L" rather than "-L:blah", for example)
  //---------------------------------------------------------------------------
  public boolean mIsNoValueParam;

  //---------------------------------------------------------------------------
  // Does this parameter have a default value if not present in the args?
  // Should be false if isNoValueParam is set to true.
  //---------------------------------------------------------------------------
  public boolean mHasDefault;

  //---------------------------------------------------------------------------
  // Has the param been found in the args (or been defaulted)?
  //---------------------------------------------------------------------------
  public boolean mFound;

  //---------------------------------------------------------------------------
  // Constructor
  //---------------------------------------------------------------------------
  public CommandLineParam(char commandLineOption,
                          boolean isNoValueParam,
                          boolean hasDefault)
  {
    //-------------------------------------------------------------------------
    // Set up member variables
    //-------------------------------------------------------------------------
    mCommandLineOption = commandLineOption;
    mIsNoValueParam = isNoValueParam;
    mHasDefault = hasDefault;
    mFound = false;
  }
  public void parseParam(String arg) throws ParamBadFormat123Exception
  {

    if (mIsNoValueParam)
    {
      mFound = true;
    }
    else
    {
      throw new ParamBadFormat123Exception();
    }
    return;
  }
}
