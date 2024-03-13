/**
 * (c) Microsoft Corporation. All rights reserved.
 * Highly Confidential Material
 */
final public class VPSubConfigFailureException extends Exception
{
  public int    mRC = 0;

  public VPSubConfigFailureException(int rc)
  {
    mRC = rc;
  }
}

