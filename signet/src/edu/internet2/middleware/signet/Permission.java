/*--
  $Id: Permission.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import edu.internet2.middleware.subject.Subject;

/**
 * Permission describes an application-level action that a {@link Subject} may
 * be allowed to perform.
 * 
 */

public interface Permission
{
  /**
   * Gets the ID of this Permission.
   * 
   * @return Returns a short mnemonic id which will appear in XML
   * 		documents and other documents used by analysts.
   */
  public String getId();

  /**
   * Sets the ID of this Permission. Should probably be made less public.
   * 
   * @param id A short mnemonic id which will appear in XML
   * 		documents and other documents used by analysts.
   */
  public void setId(String id);
  
  /**
   * Gets the Functions associated with this Permission.
   * 
   * @return Returns the Functions associated with this Permission.
   */
  public Function[] getFunctionsArray();
  
  /**
   * Sets the Functions associated with this Permission.
   * 
   * @param functions The Functions associated with this Permission.
   */
  void setFunctionsArray(Function[] functions);
  
  /**
   * Adds a Function to the set of Functions associated with this Permission.
   * 
   * @param function
   */
  void addFunction(Function function);
  
  /**
   * Gets the Subsystem associated with this Permission.
   * 
   * @return the Subsystem associated with this Permission.
   */
  public Subsystem getSubsystem();
}
