/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import edu.internet2.middleware.subject.*;


/** 
 * Implementation of the I2MI {{@link SubjectTypeAdapter}} interface.
 *
 * @author  blair christensen.
 * @version $Id: SubjectTypeAdapterPersonImpl.java,v 1.1 2004-11-02 19:40:22 blair Exp $
 */
public class GrouperSubjectTypeAdapterImpl
	extends     AbstractSubjectTypeAdapter
	implements  SubjectTypeAdapter
{

  public GrouperSubjectTypeAdapterImpl() {
    super();
  }
  
  /*
   * PUBLIC INSTANCE METHODS
   */

  public void destroy() { 
    // XXX Nothing -- Yet
  }

  public Subject getSubject(SubjectType type, String id) {
    return null;
  }

  public Subject getSubjectByDisplayId(SubjectType type, String displayId) {
    return null;
  }
 
  public Subject[] getSubjects(SubjectType type) {
    return null;
  }

  public void init() {
    // XXX Nothing -- Yet
  }

  public boolean isModifiable() {
    return false;
  }

  public Subject newSubject(SubjectType type, 
                            String id, 
                            String name, 
                            String description, 
                            String displayId) 
  {
    return null;
  }

  public Subject quickSearch(String searchValue) {
    return null;
  }

  public Subject[] searchByIdentifier(SubjectType type, String id) {
    return null;
  }

}
