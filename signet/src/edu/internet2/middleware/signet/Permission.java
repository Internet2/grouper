/*--
$Id: Permission.java,v 1.9 2005-11-02 18:05:51 acohen Exp $
$Date: 2005-11-02 18:05:51 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Set;

import edu.internet2.middleware.subject.Subject;

/**
* Permission describes an application-level action that a {@link Subject} may
* be allowed to perform.
* 
*/

public interface Permission
extends SubsystemPart
{  
 /**
  * Gets the ID of this entity.
  * 
  * @return Returns a short mnemonic id which will appear in XML
  *    documents and other documents used by analysts.
  */
 public String getId();
 
 /**
  * Gets the Functions associated with this Permission.
  * 
  * @return Returns the Functions associated with this Permission.
  */
 public Set getFunctions();
 
 /**
  * Adds a Function to the set of Functions associated with this Permission.
  * 
  * @param function
  */
 public void addFunction(Function function);
 
 /**
  * Adds a Limit to the set of Limits associated with this Permission.
  * 
  * @param limit
  */
 public void addLimit(Limit limit);
 
 /**
  * Gets the Limits associated with this Permission.
  * 
  * @return the Limits associated with this Permission.
  */
 public Set getLimits();
}
