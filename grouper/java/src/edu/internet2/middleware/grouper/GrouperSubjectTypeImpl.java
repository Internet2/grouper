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
 * Implementation of the I2MI {{@link SubjectType}} interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSubjectTypeImpl.java,v 1.1 2004-11-02 19:40:22 blair Exp $
 */
public class GrouperSubjectTypeImpl implements SubjectType {

  public GrouperSubjectTypeImpl() {
    super();
  }

  /*
   * PUBLIC INSTANCE METHODS
   */

  public SubjectTypeAdapter getAdapter() {
    return null;
  }

  public String getId() {
    return null;
  }

  public String getName() {
    return null;
  }
 
}

