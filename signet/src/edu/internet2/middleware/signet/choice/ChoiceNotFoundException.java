/*
 * Created on Jan 18, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
