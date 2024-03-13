/**
 * Title: TraceHelper
 *
 * Description: A utility class providing methods to output time-stamped
 * strings to the standard output stream.
 *
 * (c) Microsoft Corporation. All rights reserved - Highly Confidential Material
 *
 * @version 1.0
 */
package com.Metaswitch.MVS.Utils;

import java.util.Date;
import java.text.SimpleDateFormat;

public class TraceHelper
{
  //---------------------------------------------------------------------------
  // Create a SimpleDateFormat object to format time strings for us.
  //---------------------------------------------------------------------------
  private static final SimpleDateFormat sDateFormat =
                               new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  /**
   * Output a line to stdout consisting of the current time and the specified
   * text.
   *
   * @param text        The text to output.
   */
  public static void trace(String text)
  {
    //-------------------------------------------------------------------------
    // Get the current time as a string.
    //-------------------------------------------------------------------------
    String currentTime = currentTimeAsString();

    //-------------------------------------------------------------------------
    // Insert spaces into the text string after all the carriage returns, so
    // that the output text is lined up nicely.
    //-------------------------------------------------------------------------
    text = text.replaceAll("\n", "\n                        ");

    System.out.println(currentTime + " " + text);
  }

  /**
   * Returns the current time as a nicely formatted String.
   *
   * @returns           The current time.
   */
  public static synchronized String currentTimeAsString()
  {
    return sDateFormat.format(new Date());
  }
}
