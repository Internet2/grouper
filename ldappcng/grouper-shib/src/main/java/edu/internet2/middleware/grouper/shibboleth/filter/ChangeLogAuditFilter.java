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
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Restrictions;
import org.opensaml.xml.util.DatatypeHelper;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.audit.UserAuditQuery;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.exception.QueryException;

/**
 * Matches a {@link ChangeLogEntry} by audit category and action name.
 */
public class ChangeLogAuditFilter extends AbstractFilter<ChangeLogEntry> {

  /** The audit category. */
  private String category;

  /** The audit action. */
  private String action;

  /**
   * Constructor.
   * 
   * @param category the audit category
   * @param action the audit action name
   */
  public ChangeLogAuditFilter(String category, String action) {
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
   * Returns true if any {@link AuditEntry} for the given {@link ChangeLogEntry} matches the defined category and action
   * name.
   * 
   * {@inheritDoc}
   */
  public boolean matches(ChangeLogEntry changeLogEntry) {

    if (changeLogEntry == null) {
      return false;
    }

    String contextId = changeLogEntry.getContextId();

    if (contextId == null) {
      return false;
    }

    List<AuditEntry> auditEntries = new UserAuditQuery().setExtraCriterion(
        Restrictions.eq(AuditEntry.FIELD_CONTEXT_ID, changeLogEntry.getContextId())).execute();

    if (auditEntries == null || auditEntries.isEmpty()) {
      return false;
    }

    for (AuditEntry auditEntry : auditEntries) {

      AuditType auditType = auditEntry.getAuditType();

      if (auditType == null) {
        continue;
      }

      boolean matchesCategory = false;
      if (DatatypeHelper.isEmpty(category) || category.equals(auditType.getAuditCategory())) {
        matchesCategory = true;
      }

      boolean matchesActionName = false;
      if (DatatypeHelper.isEmpty(action) || action.equals(auditType.getActionName())) {
        matchesActionName = true;
      }

      if (matchesCategory && matchesActionName) {
        return true;
      }

    }

    return false;
  }

}
