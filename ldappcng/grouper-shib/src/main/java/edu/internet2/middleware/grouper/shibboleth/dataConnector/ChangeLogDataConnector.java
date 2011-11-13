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

package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.shibboleth.filter.Filter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.DataConnector;

/** A {@link DataConnector} which returns {@link ChangeLogEntrty} attributes. */
public class ChangeLogDataConnector extends BaseGrouperDataConnector<ChangeLogEntry> {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(ChangeLogDataConnector.class);

  /** The principal name prefix required for processing of a change log entry. A terrible hack. */
  public static final String PRINCIPAL_NAME_PREFIX = "change_log_sequence_number:";

  /** The pattern for parsing principal names. */
  private static final Pattern pattern = Pattern.compile(PRINCIPAL_NAME_PREFIX);

  /** {@inheritDoc} */
  public Map<String, BaseAttribute> resolve(ShibbolethResolutionContext resolutionContext)
      throws AttributeResolutionException {

    String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();

    LOG.debug("ChangeLog data connector '{}' - Resolve principal '{}'", getId(), principalName);
    LOG.trace("ChangeLog data connector '{}' - Resolve principal '{}' requested attributes {}", new Object[] { getId(),
        principalName, resolutionContext.getAttributeRequestContext().getRequestedAttributesIds() });

    if (DatatypeHelper.isEmpty(principalName)) {
      LOG.debug("ChangeLog data Connector '{}' - No principal name", getId());
      return Collections.EMPTY_MAP;
    }

    // parse the change log entry sequence number from the principal name
    long sequenceNumber = sequenceNumber(principalName);

    if (sequenceNumber == -1) {
      LOG.debug("ChangeLog data connector '{}' - Principal name '{}' does not match prefix", getId(), principalName);
      return Collections.EMPTY_MAP;
    }

    // query for the change log entry
    ChangeLogEntry changeLogEntry = GrouperDAOFactory.getFactory().getChangeLogEntry()
        .findBySequenceNumber(sequenceNumber, false);

    if (changeLogEntry == null) {
      LOG.debug("ChangeLog data connector '{}' - Changelog sequence '{}' not found", getId(), principalName);
      return Collections.EMPTY_MAP;
    }

    // matcher
    Filter<ChangeLogEntry> matcher = getFilter();
    if (!matcher.matches(changeLogEntry)) {
      LOG.debug("ChangeLog data connector '{}' - Ignoring changelog '{}'", getId(), toString(changeLogEntry));
      return Collections.EMPTY_MAP;
    }

    LOG.debug("ChangeLog data connector '{}' - Found change log entry '{}'", getId(), toString(changeLogEntry));
    LOG.trace("ChangeLog data connector '{}' - Found change log entry '{}'", getId(), changeLogEntry.toStringDeep());

    Map<String, BaseAttribute> attributes = buildAttributes(changeLogEntry);

    LOG.debug("ChangeLog data connector '{}' - Change log entry {} returning {}", new Object[] { getId(),
        toString(changeLogEntry), attributes });

    if (LOG.isTraceEnabled()) {
      for (String key : attributes.keySet()) {
        for (Object value : attributes.get(key).getValues()) {
          LOG.trace("ChangeLog data connector '{}' - Change log entry {} attribute {} : {}", new Object[] { getId(),
              toString(changeLogEntry), key, value });
        }
      }
    }

    return attributes;
  }

  /**
   * Return attributes for the given {@link ChangeLogEntry}.
   * 
   * @param changeLogEntry the changeLogEntry
   * @return the attributes
   */
  protected Map<String, BaseAttribute> buildAttributes(ChangeLogEntry changeLogEntry) {

    Map<String, BaseAttribute> attributes = new LinkedHashMap<String, BaseAttribute>();

    ChangeLogType changeLogType = changeLogEntry.getChangeLogType();

    for (String label : changeLogType.labels()) {
      String fieldName = changeLogType.retrieveChangeLogEntryFieldForLabel(label);
      if (!DatatypeHelper.isEmpty(fieldName)) {
        Object value = GrouperUtil.fieldValue(changeLogEntry, fieldName);
        String valueString = DatatypeHelper.safeTrimOrNullString(GrouperUtil.stringValue(value));
        if (!DatatypeHelper.isEmpty(valueString)) {
          BasicAttribute<String> attribute = new BasicAttribute<String>();
          attribute.setId(label);
          attribute.getValues().add(valueString);
          attributes.put(attribute.getId(), attribute);
        }
      }
    }

    return attributes;
  }

  /** {@inheritDoc} */
  public void validate() throws AttributeResolutionException {

  }

  /**
   * A hack. Return a principal name suitable for processing based on a change log sequence number.
   * 
   * @param changeLogSequenceNumber the change log sequence number
   * @return the prefixed principal name
   */
  public static String principalName(long changeLogSequenceNumber) {
    return PRINCIPAL_NAME_PREFIX + Long.toString(changeLogSequenceNumber);
  }

  /**
   * A hack. Get the sequence number from a prefixed principal name. Returns -1 if the principal name does not match the
   * prefix or an error occurred parsing the sequence number.
   * 
   * @param principalName the prefixed principal name
   * @return the sequence number
   */
  public static long sequenceNumber(String principalName) {
    String toks[] = pattern.split(principalName);
    if (toks.length == 2) {
      try {
        return Long.parseLong(toks[1]);
      } catch (NumberFormatException e) {
        LOG.error("ChangeLog data connector - Unable to parse principal name '{}'", principalName, e);
        return -1;
      }
    }
    return -1;
  }

  /**
   * Return a string representing an {@AuditEntry}.
   * 
   * Returned fields include timestamp, category, actionname, and description.
   * 
   * Uses {@ToStringBuilder}.
   * 
   * @param auditEntry the audit entry
   * @return the string representing the audit entry
   */
  public static String toString(AuditEntry auditEntry) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(auditEntry, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("timestamp", auditEntry.getCreatedOn());
    toStringBuilder.append("category", auditEntry.getAuditType().getAuditCategory());
    toStringBuilder.append("actionname", auditEntry.getAuditType().getActionName());
    toStringBuilder.append("description", auditEntry.getDescription());
    return toStringBuilder.toString();
  }

  /**
   * Return a string representing an {ChangeLogEntry}.
   * 
   * Returned fields include timestamp, category, actionname, and description.
   * 
   * Uses {@ToStringBuilder}.
   * 
   * @param changeLogEntry the change log entry
   * @return the string representing the change log entry
   */
  public static String toString(ChangeLogEntry changeLogEntry) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(changeLogEntry, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("timestamp", changeLogEntry.getCreatedOn());
    toStringBuilder.append("sequence", changeLogEntry.getSequenceNumber());
    toStringBuilder.append("category", changeLogEntry.getChangeLogType().getChangeLogCategory());
    toStringBuilder.append("actionname", changeLogEntry.getChangeLogType().getActionName());
    return toStringBuilder.toString();
  }

  /**
   * Return a string representing an {ChangeLogEntry}.
   * 
   * Returns all labels (attributes).
   * 
   * Uses {@ToStringBuilder}.
   * 
   * @param changeLogEntry the change log entry
   * @return the string representing the entire change log entry
   */
  public static String toStringDeep(ChangeLogEntry changeLogEntry) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(changeLogEntry, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("timestamp", changeLogEntry.getCreatedOn());
    toStringBuilder.append("sequence", changeLogEntry.getSequenceNumber());
    toStringBuilder.append("category", changeLogEntry.getChangeLogType().getChangeLogCategory());
    toStringBuilder.append("actionname", changeLogEntry.getChangeLogType().getActionName());

    ChangeLogType changeLogType = changeLogEntry.getChangeLogType();

    for (String label : changeLogType.labels()) {
      String fieldName = changeLogType.retrieveChangeLogEntryFieldForLabel(label);
      if (!DatatypeHelper.isEmpty(fieldName)) {
        Object value = GrouperUtil.fieldValue(changeLogEntry, fieldName);
        String valueString = DatatypeHelper.safeTrimOrNullString(GrouperUtil.stringValue(value));
        toStringBuilder.append(label, valueString);
      }
    }

    return toStringBuilder.toString();
  }

}
