/*--
$Id: ChoiceNotFoundException.java,v 1.3 2005-02-25 19:37:03 acohen Exp $
$Date: 2005-02-25 19:37:03 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.choice;

/**
 * Used to indicate that a requested Choice is not found in a
 * particular ChoiceSet.
 */
public class ChoiceNotFoundException extends Exception
{

  /**
   * 
   */
  public ChoiceNotFoundException()
  {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   */
  public ChoiceNotFoundException(String arg0)
  {
    super(arg0);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   * @param arg1
   */
  public ChoiceNotFoundException(String arg0, Throwable arg1)
  {
    super(arg0, arg1);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   */
  public ChoiceNotFoundException(Throwable arg0)
  {
    super(arg0);
    // TODO Auto-generated constructor stub
  }

}
