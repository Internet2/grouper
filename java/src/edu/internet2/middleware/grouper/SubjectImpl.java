/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.subject.*;


/** 
 * Implementation of the I2MI {{@link Subject}} interface.
 *
 * @author  blair christensen.
 * @version $Id: SubjectImpl.java,v 1.2 2004-11-12 17:36:03 blair Exp $
 */
public class SubjectImpl implements Subject {

  public SubjectImpl() {
    super();
  }

  /*
   * PUBLIC INSTANCE METHODS
   */

  public void addAttribute(String name, String value) {
    // XXX Nothing -- Yet
  }

  public String[] getAttributeArray(String name) {
    return null; 
  }

  public String getDescription() {
    return null;
  }

  public String getDisplayId() {
    return null; 
  }

  public String getId() {
    return null;
  }

  public String getName() {
    return null; 
  }

  public SubjectType getSubjectType()  {
    return null;
  }

}
