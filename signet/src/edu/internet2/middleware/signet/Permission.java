/*--
$Id: Permission.java,v 1.11 2006-02-09 10:22:43 lmcrae Exp $
$Date: 2006-02-09 10:22:43 $

Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
