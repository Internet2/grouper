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
 * @author mchyzer
 * $Id: GrouperAntProject.java,v 1.3 2008-09-29 04:42:19 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * make a subclass so we can log the output from java
 */
public class GrouperAntProject extends Project {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperAntProject.class);

  /**
   * @see org.apache.tools.ant.Project#log(java.lang.String, int)
   */
  @Override
  public void log(String message, int msgLevel) {
    this.log(message, null, msgLevel);
  }

  /**
   * @see org.apache.tools.ant.Project#log(java.lang.String, java.lang.Throwable, int)
   */
  public void log(String message, Throwable throwable, int msgLevel) {
    message = StringUtils.trimToEmpty(message);
    if (StringUtils.isBlank(message) && throwable == null) {
      return;
    }
    switch(msgLevel) {
      case MSG_DEBUG:
        LOG.debug(message, throwable);
        break;
      case MSG_VERBOSE:
      case MSG_INFO:
      case MSG_WARN:
        //always log or print to screen
        if (LOG.isWarnEnabled()) {
          LOG.warn(message, throwable);
        } else {
          System.err.println(message + (throwable == null ? "" : ("\n" + ExceptionUtils.getFullStackTrace(throwable))));
        }
        break;
      case MSG_ERR:
        //always log or print to screen
        if (LOG.isErrorEnabled()) {
          LOG.error(message, throwable);
        } else {
          System.err.println(message  + (throwable == null ? "" : ("\n" + ExceptionUtils.getFullStackTrace(throwable))));
        }
        break;
        
    }
  }

  /**
   * @see org.apache.tools.ant.Project#log(java.lang.String)
   */
  @Override
  public void log(String message) {
    this.log(message, Project.MSG_INFO);
  }

  /**
   * @see org.apache.tools.ant.Project#log(org.apache.tools.ant.Target, java.lang.String, int)
   */
  @Override
  public void log(Target target, String message, int msgLevel) {
    this.log(message, null, msgLevel);
  }

  /**
   * @see org.apache.tools.ant.Project#log(org.apache.tools.ant.Target, java.lang.String, java.lang.Throwable, int)
   */
  public void log(Target target, String message, Throwable throwable, int msgLevel) {
    this.log(message, throwable, msgLevel);
  }

  /**
   * @see org.apache.tools.ant.Project#log(org.apache.tools.ant.Task, java.lang.String, int)
   */
  @Override
  public void log(Task task, String message, int msgLevel) {
    this.log(message, null, msgLevel);
  }

  /**
   * @see org.apache.tools.ant.Project#log(org.apache.tools.ant.Task, java.lang.String, java.lang.Throwable, int)
   */
  public void log(Task task, String message, Throwable throwable, int msgLevel) {
    this.log(message, throwable, msgLevel);
  }

  /**
   * 
   */
  public GrouperAntProject() {
  }

}
