/*
 * @author mchyzer
 * $Id: GrouperLoaderStatus.java,v 1.1 2008-06-01 21:27:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * status of a job
 */
public enum GrouperLoaderStatus {
  
  /** job was started */
  STARTED,

  /** job cant even start */
  CONFIG_ERROR,

  /** job finished but there were problems with one or more subjects (e.g. not found, duplicate, unresolvable) */
  SUBJECT_PROBLEMS,

  /** job finished with success */
  SUCCESS,

  /** job finished, but had problems.  Or maybe some subjobs ok, some not */
  WARNING,
  
  /** job didnt finish, it had problems */
  ERROR;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   */
  public static GrouperLoaderStatus valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperLoaderStatus.class, 
        string, exceptionOnNotFound);
  }

}
