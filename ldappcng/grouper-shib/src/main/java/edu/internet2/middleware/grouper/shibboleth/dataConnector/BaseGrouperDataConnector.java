/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.BaseField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.GroupsField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.MembersField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.PrivilegeField;
import edu.internet2.middleware.grouper.shibboleth.filter.AbstractSetOperationFilter;
import edu.internet2.middleware.grouper.shibboleth.filter.Filter;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.grouper.shibboleth.util.SubjectIdentifier;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.BaseDataConnector;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.DataConnector;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/** A {@link DataConnector} which returns Grouper objects. */
public abstract class BaseGrouperDataConnector<T> extends BaseDataConnector {

  /** logger */
  private static final Logger LOG = LoggerFactory.getLogger(BaseGrouperDataConnector.class);

  /** the grouper session, initialized for each grouper data connector */
  private GrouperSession grouperSession;

  /** the attributes which should be returned by this data connector */
  private List<AttributeIdentifier> attributeIdentifiers;

  /** The subject identifier used to start a <code>GrouperSession</code>. */
  private SubjectIdentifier subjectIdentifier;

  /** The query which filters the objects returned by this data connector. */
  private Filter<T> filter;

  /** the groups to return as attributes */
  private ArrayList<GroupsField> groupsFields = new ArrayList<GroupsField>();

  /** the members to return as attributes */
  private ArrayList<MembersField> membersFields = new ArrayList<MembersField>();

  /** the privileges to return as attributes */
  private ArrayList<PrivilegeField> privilegeFields = new ArrayList<PrivilegeField>();

  /** a set of valid names for the first element of an attribute identifier */
  private Set<String> validFirstIdElements = new HashSet<String>();

  /** The possibly empty set of attribute definition names defined in the resolver configuration. */
  private Set<String> attributeDefNames = new LinkedHashSet<String>();
  
  /** The principal name prefix required for processing of a change log entry. A terrible hack. */
  public static final String CHANGELOG_PRINCIPAL_NAME_PREFIX = "change_log_sequence_number:";

  /**
   * Constructor.
   */
  public BaseGrouperDataConnector() {
    super();
  }

  /**
   * Make sure that the attributes to return as specified in the data connector configuration are valid, and initialize
   * the necessary objects.
   * 
   * @throws GrouperException
   */
  // FUTURE probably should use spring for this configuration
  public void initialize() throws GrouperException {

    // make sure the session can be instantiated
    this.getGrouperSession();
    LOG.debug("Grouper data connector '{}' - Started grouper session '{}'", getId(), getGrouperSession());

    validFirstIdElements.add(GroupsField.NAME);
    validFirstIdElements.add(MembersField.NAME);
    validFirstIdElements.addAll(AccessPrivilege.getAllPrivilegeNames());
    validFirstIdElements.addAll(getAllAttributeDefNames());

    for (AttributeIdentifier fieldIdentifier : attributeIdentifiers) {

      LOG.debug("Grouper data connector '{}' - Attribute identifier '{}'", getId(), fieldIdentifier);

      // internal grouper fields
      if (fieldIdentifier.getSource().equals(SubjectFinder.internal_getGSA().getId())) {

        BaseField bf = new BaseField(fieldIdentifier.getId());

        if (!validFirstIdElements.contains(bf.getFirstIdElement())
            && !validFirstIdElements.contains(fieldIdentifier.getId())) {
          LOG.error("Grouper data connector '{}' - Invalid identifer '{}' should start with one of {}", new Object[] {
              getId(), fieldIdentifier.getId(), validFirstIdElements });
          throw new GrouperException("Invalid identifer '" + fieldIdentifier.getId() + "', should start with one of "
              + validFirstIdElements);
        }

        if (bf.getFirstIdElement().equals(GroupsField.NAME)) {

          groupsFields.add(new GroupsField(fieldIdentifier.getId()));

        } else if (bf.getFirstIdElement().equals(MembersField.NAME)) {

          membersFields.add(new MembersField(fieldIdentifier.getId()));

        } else if (AccessPrivilege.getAllPrivilegeNames().contains(fieldIdentifier.getId())) {

          privilegeFields.add(new PrivilegeField(fieldIdentifier.getId(), getGrouperSession().getAccessResolver()));

        } else if (getAllAttributeDefNames().contains(fieldIdentifier.getId())) {

          attributeDefNames.add(fieldIdentifier.getId());

        } else {
          LOG.error("Grouper data connector '{}' - Unknown field identifier '{}'", getId(), fieldIdentifier.getId());
          throw new GrouperException("Unknown field identifier " + fieldIdentifier.getId());
        }
      } else {
        // make sure the source is defined and available
        SubjectFinder.getSource(fieldIdentifier.getSource());
      }
    }

    privilegeFields.trimToSize();
    membersFields.trimToSize();
    groupsFields.trimToSize();

    // FUTURE improve session handling
    initGrouperSessionRecursively(filter);
  }

  /**
   * Set the {@link GrouperSession} for any configured {@link Filter} recursively.
   * 
   * @param filter the filter
   */
  protected void initGrouperSessionRecursively(Filter filter) {
    if (filter == null) {
      return;
    }
    filter.setGrouperSession(grouperSession);
    if (filter instanceof AbstractSetOperationFilter) {
      initGrouperSessionRecursively(((AbstractSetOperationFilter) filter).getFilter0());
      initGrouperSessionRecursively(((AbstractSetOperationFilter) filter).getFilter1());
    }
  }

  /**
   * Start a new session if necessary, otherwise reuse existing session. The session is started using the
   * {@link Subject} identified by the configured subject identifier. This session is not added to the threadlocal.
   * 
   * @return the <code>GrouperSession</code>
   * @throws SubjectNotFoundException if the {@link Subject} identified by the subjectId cannot be found
   */
  public GrouperSession getGrouperSession() throws SubjectNotFoundException {
    if (grouperSession == null) {
      if (subjectIdentifier == null) {
        grouperSession = GrouperSession.startRootSession(false);
      } else {
        Subject subject = SubjectFinder.findByIdAndSource(subjectIdentifier.getId(), subjectIdentifier.getSource(),
            true);
        grouperSession = GrouperSession.start(subject, false);
      }
      LOG.debug("Grouper data connector '{}' - Started grouper session '{}'", getId(), grouperSession);
    }

    return grouperSession;
  }

  /**
   * Get the filter which determines the objects which will be considered by this data connector.
   * 
   * @return the filter or <tt>null</tt> if all groups should be considered
   */
  public Filter<T> getFilter() {
    return filter;
  }

  /**
   * Set the match query filter.
   * 
   * @param filter the Filter
   */
  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  /**
   * Set the identifiers of the attributes to return.
   * 
   * @param attributeIdentifiers
   */
  public void setAttributeIdentifiers(List<AttributeIdentifier> attributeIdentifiers) {
    this.attributeIdentifiers = attributeIdentifiers;
  }

  /**
   * Return the identifiers of the attributes to return.
   * 
   * @return the attribute identifiers
   */
  public List<AttributeIdentifier> getAttributeIdentifiers() {
    return attributeIdentifiers;
  }

  /**
   * Get the subject and source identifier used to start {@link GrouperSession}s.
   * 
   * @return Returns the source and subject identifier.
   */
  public SubjectIdentifier getSubjectIdentifier() {
    return subjectIdentifier;
  }

  /**
   * Set the subject and source identifier used to start {@link GrouperSession}s.
   * 
   * @param subjectIdentifier The source and subject identifier to set.
   */
  public void setSubjectIdentifier(SubjectIdentifier subjectIdentifier) {
    this.subjectIdentifier = subjectIdentifier;
  }

  /**
   * The representation of the attributes which return groups.
   * 
   * @return the groups fields
   */
  public List<GroupsField> getGroupsFields() {
    return groupsFields;
  }

  /**
   * The representation of the attributes which return members.
   * 
   * @return the members fields
   */
  public List<MembersField> getMembersFields() {
    return membersFields;
  }

  /**
   * The representation of the attributes which return privileges.
   * 
   * @return the privileges fields
   */
  public List<PrivilegeField> getPrivilegeFields() {
    return privilegeFields;
  }

  /**
   * Returns the possibly empty set of attribute definition names defined in the attribute resolver configuration.
   * 
   * @return the possibly empty set of attribute definition names
   */
  protected Set<String> getAttributeDefNames() {
    return attributeDefNames;
  }

  /**
   * Return all attribute definition names.
   * 
   * @return all attribute definition names.
   */
  protected Set<String> getAllAttributeDefNames() {
    Set<String> allAttributeDefNames = new HashSet<String>();

    Set<AttributeDefName> attributeDefNames = GrouperDAOFactory
        .getFactory()
        .getAttributeDefName()
        .findAllAttributeNamesSplitScopeSecure(null, getGrouperSession(), null, SubjectFinder.findRootSubject(), null,
            null, null, null);

    for (AttributeDefName attributeDefName : attributeDefNames) {
      allAttributeDefNames.add(attributeDefName.getName());
    }
    return allAttributeDefNames;
  }
}
