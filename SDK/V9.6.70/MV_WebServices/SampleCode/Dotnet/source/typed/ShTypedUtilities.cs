//-----------------------------------------------------------------------------
// ShTypedUtilities
//
// Copyright (c) Microsoft Corporation. All rights reserved.
// Highly Confidential Material
//
// This file provides utilities to those example applications that use the
// "typed" WSDL file and therefore deal with user data as .NET classes.
//-----------------------------------------------------------------------------

using System;

public class ShTypedUtilities : ShUtilities
{
  /**
   * Finds the current sequence number within the user data and increments it,
   * wrapping if necessary, so that the server accepts the change.
   *
   * @param userData    IN/OUT The user data whose sequence number to update.
   */
  public void incrementSequenceNumber(tUserData userData)
  {
    tTransparentData repositoryData = userData.ShData.RepositoryData;

    int newSequenceNumber = repositoryData.SequenceNumber + 1;

    if (newSequenceNumber > 65535)
    {
      //-----------------------------------------------------------------------
      // The sequence number needs to wrap to 1, not 0: 0 is used to create
      // new objects.
      //-----------------------------------------------------------------------
      newSequenceNumber = 1;
    }

    repositoryData.SequenceNumber = newSequenceNumber;
  }
}
