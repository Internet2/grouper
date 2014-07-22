/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author mchyzer
 * $Id: GrouperLoaderStatus.java,v 1.3 2008-12-11 16:28:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * status of a job
 */
public enum GrouperLoaderStatus {
  
  /** job was started */
  STARTED("started"),

  /** job is running */
  RUNNING("running"),

  /** job cant even start */
  CONFIG_ERROR("config error"),

  /** job finished but there were problems with one or more subjects (e.g. not found, duplicate, unresolvable) */
  SUBJECT_PROBLEMS("subject problems"),

  /** job finished with success */
  SUCCESS("successes"),

  /** job finished, but had problems.  Or maybe some subjobs ok, some not */
  WARNING("warnings"),
  
  /** job didnt finish, it had problems */
  ERROR("errors");
  
  /** friendly string */
  private String friendlyString;
  
  /**
   * 
   * @param theFriendlyStatus
   */
  private GrouperLoaderStatus(String theFriendlyStatus) {
    this.friendlyString = theFriendlyStatus;
  }
  
  /**
   * friendly string e.g. for report
   * @return the friendly string
   */
  public String getFriendlyString() {
    return this.friendlyString;
  }
  
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
