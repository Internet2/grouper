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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.BaseField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.GroupsField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.MembersField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.PrivilegeField;
import edu.internet2.middleware.grouper.shibboleth.filter.ConditionalMatchQueryFilter;
import edu.internet2.middleware.grouper.shibboleth.filter.MatchQueryFilter;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.BaseDataConnector;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.DataConnector;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * A {@link DataConnector} which returns objects from Grouper.
 */
public abstract class BaseGrouperDataConnector extends BaseDataConnector implements SourceDataConnector {

  /** logger */
  private static final Logger LOG = LoggerFactory.getLogger(BaseGrouperDataConnector.class);

  /** the name of the attribute whose values are {@link GroupType}s */
  public static final String GROUP_TYPE_ATTR = "groupType";

  /** the name of the attribute whose value is the name of the parent stem */
  public static final String PARENT_STEM_NAME_ATTR = "parentStemName";

  /** the grouper session, initialized for each grouper data connector */
  private GrouperSession grouperSession;

  /** the attributes which should be returned by this data connector */
  private List<AttributeIdentifier> fieldIdentifiers;

  /** the groups to return as attributes */
  private ArrayList<GroupsField> groupsFields = new ArrayList<GroupsField>();

  /** the members to return as attributes */
  private ArrayList<MembersField> membersFields = new ArrayList<MembersField>();

  /** the privileges to return as attributes */
  private ArrayList<PrivilegeField> privilegeFields = new ArrayList<PrivilegeField>();

  /** the subject attributes which should be returned by this data connector */
  private List<AttributeIdentifier> subjectAttributeIdentifiers = new ArrayList<AttributeIdentifier>();

  /** the ids of all sources */
  private Set<String> sourceIds;

  /** the query which filters the objects returned by this data connector */
  private MatchQueryFilter matchQueryFilter;

  /** a set of valid names for the first element of an attribute identifier */
  private Set<String> validFirstIdElements = new HashSet<String>();

  /** The subject identifier used to start a <code>GrouperSession</code>. */
  private AttributeIdentifier subjectIdentifier;

  /** The possibly empty set of attribute definition names defined in the resolver configuration. */
  private Set<String> attributeDefNames = new LinkedHashSet<String>();

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
    LOG.info("started grouper session '" + this.getGrouperSession() + "' for '" + this.getId() + "'");

    validFirstIdElements.add(GroupsField.NAME);
    validFirstIdElements.add(MembersField.NAME);
    validFirstIdElements.addAll(AccessPrivilege.getAllPrivilegeNames());
    validFirstIdElements.addAll(getAllAttributeDefNames());

    for (AttributeIdentifier fieldIdentifier : fieldIdentifiers) {

      LOG.debug("attribute identifier '{}' for dc '{}'", fieldIdentifier, this.getId());

      // internal grouper fields
      if (fieldIdentifier.getSource().equals(SubjectFinder.internal_getGSA().getId())) {

        BaseField bf = new BaseField(fieldIdentifier.getId());

        if (!validFirstIdElements.contains(bf.getFirstIdElement())
            && !validFirstIdElements.contains(fieldIdentifier.getId())) {
          LOG.error("Invalid identifer '" + fieldIdentifier.getId() + "', should start with one of "
              + validFirstIdElements);
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
          LOG.error("Unknown field identifier {}", fieldIdentifier.getId());
          throw new GrouperException("Unknown field identifier " + fieldIdentifier.getId());
        }
      } else {
        // subject source attributes
        if (!this.getSourceIds().contains(fieldIdentifier.getSource())) {
          LOG.error("Unknown source '" + fieldIdentifier.getSource() + "'");
          throw new GrouperException("Unknown source '" + fieldIdentifier.getSource() + "'");
        }
        subjectAttributeIdentifiers.add(fieldIdentifier);
      }
    }

    privilegeFields.trimToSize();
    membersFields.trimToSize();
    groupsFields.trimToSize();

    // FUTURE improve session handling
    if (matchQueryFilter != null) {
      matchQueryFilter.setGrouperSession(grouperSession);
      if (matchQueryFilter instanceof ConditionalMatchQueryFilter) {
        ((ConditionalMatchQueryFilter) matchQueryFilter).getFilter0().setGrouperSession(grouperSession);
        ((ConditionalMatchQueryFilter) matchQueryFilter).getFilter1().setGrouperSession(grouperSession);
      }
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
      LOG.debug("started grouper session {}", grouperSession);
    }

    return grouperSession;
  }

  /**
   * Get the filter which determines the objects which will be considered by this data connector.
   * 
   * @return the {@link MatchQueryFilter} or <tt>null</tt> if all objects should be considered
   */
  public MatchQueryFilter getMatchQueryFilter() {
    return matchQueryFilter;
  }

  /**
   * Set the match query filter
   * 
   * @param groupQueryFilter the {@link MatchQueryFilter}
   */
  public void setMatchQueryFilter(MatchQueryFilter groupQueryFilter) {
    this.matchQueryFilter = groupQueryFilter;
  }

  /**
   * Set the identifiers of the attributes to return.
   * 
   * @param fieldIdentifiers
   */
  public void setFieldIdentifiers(List<AttributeIdentifier> fieldIdentifiers) {
    this.fieldIdentifiers = fieldIdentifiers;
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
   * Get all source ids.
   * 
   * @return the ids of all sources
   */
  private Set<String> getSourceIds() {
    if (sourceIds == null) {
      sourceIds = new HashSet<String>();
      for (Source source : SubjectFinder.getSources()) {
        sourceIds.add(source.getId());
      }
    }

    return sourceIds;
  }

  /**
   * The {@link AttributeIdentifier}s for subject attributes.
   * 
   * @return the attribute identifiers
   */
  public List<AttributeIdentifier> getSubjectAttributeIdentifiers() {
    return subjectAttributeIdentifiers;
  }

  /**
   * Get the subject and source identifier used to start {@link GrouperSession}s.
   * 
   * @return Returns the source and subject identifier.
   */
  public AttributeIdentifier getSubjectIdentifier() {
    return subjectIdentifier;
  }

  /**
   * Set the subject and source identifier used to start {@link GrouperSession}s.
   * 
   * @param subjectIdentifier The source and subject identifier to set.
   */
  public void setSubjectIdentifier(AttributeIdentifier subjectIdentifier) {
    this.subjectIdentifier = subjectIdentifier;
  }

  /**
   * Query for all groups matching the group query filter. If no group query filter has been configured, then return all
   * groups.
   * 
   * @return the groups returned from the group query filter or all groups
   */
  public Set<Group> getGroups(final Date lastModifyTime) {

    Set<Group> groups = (Set<Group>) GrouperSession.callbackGrouperSession(getGrouperSession(),
        new GrouperSessionHandler() {

          public Set<Group> callback(GrouperSession grouperSession) throws GrouperSessionException {
            String msg = "get groups since '" + lastModifyTime + "' for '" + getId() + "'";
            LOG.debug(msg);

            Set<Group> groups = new TreeSet<Group>();
            MatchQueryFilter filter = getMatchQueryFilter();
            if (filter == null) {
              Stem root = StemFinder.findRootStem(grouperSession);
              groups.addAll(root.getChildGroups(Scope.SUB));
            } else {
              groups.addAll(getMatchQueryFilter().getResults(grouperSession));
            }

            LOG.debug("{} found {} before filtering", msg, groups.size());
            // filter by lastModifyTime
            if (lastModifyTime != null) {
              Iterator<Group> iterator = groups.iterator();
              while (iterator.hasNext()) {
                Group group = iterator.next();
                if (group.getCreateTime().after(lastModifyTime))
                  continue;
                if (group.getModifyTime().after(lastModifyTime))
                  continue;
                if (group.getLastMembershipChange() != null) {
                  Date memberModifyTime = new Date(group.getLastMembershipChange().getTime());
                  if (memberModifyTime.after(lastModifyTime)) {
                    continue;
                  }
                }
                // remove from selection
                iterator.remove();
              }
            }
            LOG.debug("{} found {}", msg, groups.size());
            return groups;
          }
        });

    return groups;
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
            null);

    for (AttributeDefName attributeDefName : attributeDefNames) {
      allAttributeDefNames.add(attributeDefName.getName());
    }
    return allAttributeDefNames;
  }
}
