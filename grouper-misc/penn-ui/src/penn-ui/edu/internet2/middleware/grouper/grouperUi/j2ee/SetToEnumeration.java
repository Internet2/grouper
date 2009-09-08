/*
 * $Id: SetToEnumeration.java,v 1.2 2009-09-08 18:53:31 mchyzer Exp $
 * 
 * Copyright University of Pennsylvania 2004
 */
/*
 * Created on Nov 25, 2003
 *  
 */
package edu.internet2.middleware.grouper.grouperUi.j2ee;

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