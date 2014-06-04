/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * $Id: SetToEnumeration.java,v 1.1 2009-10-11 22:04:17 mchyzer Exp $
 * 
 * Copyright University of Pennsylvania 2004
 */
/*
 * Created on Nov 25, 2003
 *  
 */
package edu.internet2.middleware.grouper.j2ee;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * helper class to convert set to enumeration
 */
public class SetToEnumeration implements Enumeration {

  /**
   * Field set.
   */
  HashSet<Set> set = null;

  /**
   * Field iterator.
   */
  Iterator iterator = null;

  /**
   * Constructor for SetToEnumeration.
   * @param theSet Set
   */
  @SuppressWarnings("unchecked")
  public SetToEnumeration(Set theSet) {
    this.set = new HashSet(theSet);
    this.iterator = this.set.iterator();
  }

  /**
   * Method hasMoreElements.
   * @return boolean
   * @see java.util.Enumeration#hasMoreElements()
   */
  public boolean hasMoreElements() {
    return this.iterator.hasNext();
  }

  /**
   * Method nextElement.
   * @return Object
   * @see java.util.Enumeration#nextElement()
   */
  public Object nextElement() {
    return this.iterator.next();
  }
}
