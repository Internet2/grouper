/*
 * @author mchyzer
 * $Id: GrouperLoaderStatus.java,v 1.1 2008-05-13 07:11:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader;


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
}
