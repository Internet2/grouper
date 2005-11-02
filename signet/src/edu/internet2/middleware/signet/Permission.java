/*--
$Id: Permission.java,v 1.10 2005-11-02 18:20:20 acohen Exp $
$Date: 2005-11-02 18:20:20 $

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
  * @return a short mnemonic id which will appear in XML
  *    documents and other documents used by analysts.
  */
 public String getId();
 
 /**
  * Gets the {@link Function}s associated with this <code>Permission</code>.
  * 
  * @return the <code>Function</code>s associated with this
  * <code>Permission</code>.
  */
 public Set getFunctions();
 
 /**
  * Adds a <code>Function</code> to the set of <code>Function</code>s
  * associated with this <code>Permission</code>.
  * 
  * @param function The <code>Function</code> to be associated with this
  *   <code>Permission</code>.
  */
 public void addFunction(Function function);
 
 /**
  * Adds a <code>Limit</code> to the set of <code>Limit</code>s associated with
  * this <code>Permission</code>.
  * 
  * @param limit The <code>Limit</code> to be associated with this
  * <code>Permission</code>.
  */
 public void addLimit(Limit limit);
 
 /**
  * Gets the {@link Limit}s associated with this <code>Permission</code>.
  * 
  * @return the <code>Limit</code>s associated with this
  * <code>Permission</code>.
  */
 public Set getLimits();
}
