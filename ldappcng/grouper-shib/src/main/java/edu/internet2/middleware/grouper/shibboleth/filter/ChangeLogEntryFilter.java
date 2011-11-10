/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.shibboleth.filter;

import java.util.Collections;
import java.util.Set;

import org.opensaml.xml.util.DatatypeHelper;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogType;
import edu.internet2.middleware.grouper.exception.QueryException;

/**
 * Matches a {@link ChangeLogEntry} by change log category and action name.
 */
public class ChangeLogEntryFilter extends AbstractFilter<ChangeLogEntry> {

  /** The change log category. */
  private String category;

  /** The change log action. */
  private String action;

  /**
   * Constructor.
   * 
   * @param category the change log category
   * @param action the change log action name
   */
  public ChangeLogEntryFilter(String category, String action) {
    this.category = category;
    this.action = action;
  }

  /**
   * Returns an empty set since this method is not applicable for change log entries.
   * 
   * {@inheritDoc}
   */
  @Override
  public Set<ChangeLogEntry> getResults(GrouperSession s) throws QueryException {
    return Collections.EMPTY_SET;
  }

  /**
   * Returns true if the change log entry has the configured change log category and action name.
   * 
   * {@inheritDoc}
   */
  public boolean matches(ChangeLogEntry changeLogEntry) {

    if (changeLogEntry == null) {
      return false;
    }

    ChangeLogType changeLogType = changeLogEntry.getChangeLogType();

    if (changeLogType == null) {
      return false;
    }

    boolean matchesCategory = false;
    if (DatatypeHelper.isEmpty(category) || category.equals(changeLogType.getChangeLogCategory())) {
      matchesCategory = true;
    }

    boolean matchesActionName = false;
    if (DatatypeHelper.isEmpty(action) || action.equals(changeLogType.getActionName())) {
      matchesActionName = true;
    }
    
    return matchesCategory && matchesActionName;
  }

}
