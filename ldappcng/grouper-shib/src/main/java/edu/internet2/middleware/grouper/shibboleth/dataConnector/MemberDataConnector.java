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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.GroupsField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.PrivilegeField;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.grouper.shibboleth.util.SourceIdentifier;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.DataConnector;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;

/**
 * A {@link DataConnector} which returns {@link Member}s. The attributes of the returned members may be limited in order
 * to avoid unnecessary queries to the Grouper database.
 */
public class MemberDataConnector extends BaseGrouperDataConnector {

  /** logger */
  private static final Logger LOG = LoggerFactory.getLogger(MemberDataConnector.class);

  /** the name of the attribute representing a subject's id */
  public static final String ID_ATTRIBUTE_NAME = "id";

  /** the ids of sources for which members will be returned */
  private Set<SourceIdentifier> sourceIdentifiers;

  /** the String ids of sources for which members will be returned */
  private Set<String> stringSourceIdentifiers;

  /**
   * Get the ids of {@link Source}s for which {@link Member}s will be returned.
   * 
   * @return the set of source ids or null
   */
  public Set<SourceIdentifier> getSourceIdentifiers() {
    return sourceIdentifiers;
  }

  /**
   * Set the {@link Source} ids for which {@link Member}s will be returned.
   * 
   * @param sourceIdentifiers
   */
  public void setSourceIdentifiers(Set<SourceIdentifier> sourceIdentifiers) {

    this.sourceIdentifiers = sourceIdentifiers;
  }

  /**
   * Get {@link SourceIdentifier} ids as strings.
   * 
   * @return the set of strings
   */
  // FUTURE probably a poor way to use Spring
  private Set<String> getSourceIdentifiersAsStrings() {
    if (stringSourceIdentifiers == null && sourceIdentifiers != null && !sourceIdentifiers.isEmpty()) {
      stringSourceIdentifiers = new HashSet<String>();
      for (SourceIdentifier sourceIdentifier : sourceIdentifiers) {
        stringSourceIdentifiers.add(sourceIdentifier.getId());
      }
    }

    return stringSourceIdentifiers;
  }

  /**
   * If {@link SourceIdentifier}s are not null, verify that the {@link Source}s with the defined identifiers exist.
   * 
   * @see edu.internet2.middleware.grouper.shibboleth.dataConnector.BaseGrouperDataConnector#initialize()
   */
  public void initialize() {
    super.initialize();

    if (this.getSourceIdentifiersAsStrings() != null) {
      for (String sourceId : this.getSourceIdentifiersAsStrings()) {
        try {
          if (SubjectFinder.getSource(sourceId) == null) {
            LOG.error("Unknown source '" + sourceId + "'");
            throw new GrouperException("Unknown source '" + sourceId + "'");
          }
        } catch (SourceUnavailableException e) {
          LOG.error("Source unavailable '" + sourceId + "'", e);
          throw new GrouperException("Source unavailable '" + sourceId + "'", e);
        }
      }
    }
  }

  /** {@inheritDoc} */
  public Map<String, BaseAttribute> resolve(final ShibbolethResolutionContext resolutionContext)
      throws AttributeResolutionException {

    Map<String, BaseAttribute> attributes = (Map<String, BaseAttribute>) GrouperSession.callbackGrouperSession(
        getGrouperSession(),
        new GrouperSessionHandler() {

          public Map<String, BaseAttribute> callback(GrouperSession grouperSession) throws GrouperSessionException {

            String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();
            String msg = "'" + principalName + "' dc '" + getId() + "'";
            LOG.debug("resolve {}", msg);
            if (LOG.isTraceEnabled()) {
              LOG.trace("resolve {} requested attribute ids {}", msg, resolutionContext.getAttributeRequestContext()
                  .getRequestedAttributesIds());
              if (resolutionContext.getAttributeRequestContext().getRequestedAttributesIds() != null) {
                for (String attrId : resolutionContext.getAttributeRequestContext().getRequestedAttributesIds()) {
                  LOG.trace("resolve {} requested attribute '{}'", msg, attrId);
                }
              }
            }

            Map<String, BaseAttribute> attributes = new HashMap<String, BaseAttribute>();

            // find subject
            Subject subject = SubjectFinder.findByIdOrIdentifier(principalName, false);
            if (subject == null) {
              LOG.debug("resolve {} subject not found", msg);
              return attributes;
            }
            LOG.debug("resolve {} found subject '{}'", msg, GrouperUtil.subjectToString(subject));

            // don't return group objects, that's the GroupDataConnector
            if (subject.getSourceId().equals(SubjectFinder.internal_getGSA().getId())) {
              LOG.debug("resolve {} returning empty map for '{}' source", msg, SubjectFinder.internal_getGSA().getId());
              return attributes;
            }

            // only return subjects from configured sources
            if (getSourceIdentifiersAsStrings() != null) {
              if (!getSourceIdentifiersAsStrings().contains(subject.getSourceId())) {
                LOG.debug("resolve {} returning empty map for unknown source '{}'", msg, subject.getSourceId());
                return attributes;
              }
            }

            // find member
            Member member = MemberFinder.findBySubject(getGrouperSession(), subject, false);
            if (member == null) {
              LOG.debug("resolve {} member not found", msg);
              return attributes;
            }
            LOG.debug("resolve {} found member '{}'", msg, member);

            // id
            BasicAttribute<String> id = new BasicAttribute<String>(ID_ATTRIBUTE_NAME);
            id.setValues(Arrays.asList(new String[] { subject.getId() }));
            attributes.put(id.getId(), id);

            // defined subject attributes
            LOG.debug("resolve {} subjectIDs {}", msg, getSubjectAttributeIdentifiers());
            for (AttributeIdentifier attributeIdentifier : getSubjectAttributeIdentifiers()) {
              LOG.debug("resolve {} member {} field {}", new Object[] { msg, member, attributeIdentifier });
              if (subject.getSourceId().equals(attributeIdentifier.getSource())) {
                // name
                if (attributeIdentifier.getId().equals(GrouperConfig.ATTRIBUTE_NAME)) {
                  String name = subject.getName();
                  if (name != null) {
                    BasicAttribute<String> nameAttribute = new BasicAttribute<String>(GrouperConfig.ATTRIBUTE_NAME);
                    nameAttribute.setValues(GrouperUtil.toList(name));
                    attributes.put(nameAttribute.getId(), nameAttribute);
                  }
                  // description
                } else if (attributeIdentifier.getId().equals(GrouperConfig.ATTRIBUTE_DESCRIPTION)) {
                  String description = subject.getDescription();
                  if (description != null && !description.equals(GrouperConfig.EMPTY_STRING)) {
                    BasicAttribute<String> descriptionAttribute = new BasicAttribute<String>(
                        GrouperConfig.ATTRIBUTE_DESCRIPTION);
                    descriptionAttribute.setValues(GrouperUtil.toList(description));
                    attributes.put(descriptionAttribute.getId(), descriptionAttribute);
                  }
                  // other attributes
                } else {
                  Set<String> values = subject.getAttributeValues(attributeIdentifier.getId());
                  if (values != null && !values.isEmpty()) {
                    BasicAttribute<String> basicAttribute = new BasicAttribute<String>(attributeIdentifier.getId());
                    basicAttribute.setValues(values);
                    attributes.put(basicAttribute.getId(), basicAttribute);
                  }
                }
              }
            }

            // groups
            for (GroupsField groupsField : getGroupsFields()) {
              BaseAttribute<Group> attr = groupsField.getAttribute(member, getMatchQueryFilter());
              if (attr != null) {
                attributes.put(groupsField.getId(), attr);
              }
            }

            // privs
            for (PrivilegeField privilegeField : getPrivilegeFields()) {
              BaseAttribute<Group> attr = privilegeField.getAttribute(subject);
              if (attr != null) {
                attributes.put(privilegeField.getId(), attr);
              }
            }
            
            // attribute defs
            for (String attributeDefName : getAttributeDefNames()) {
              List<String> values = member.getAttributeValueDelegate().retrieveValuesString(attributeDefName);
              if (values != null && !values.isEmpty()) {
                BasicAttribute<String> basicAttribute = new BasicAttribute<String>(attributeDefName);
                basicAttribute.setValues(values);
                attributes.put(attributeDefName, basicAttribute);
              }
            }

            if (LOG.isDebugEnabled()) {
              LOG.debug("resolve {} attributes {}", msg, attributes.size());
              for (String key : attributes.keySet()) {
                for (Object value : attributes.get(key).getValues()) {
                  LOG.debug("resolve {} '{}' : {}", new Object[] { msg, key, value });
                }
              }
            }
            return attributes;
          }
        });

    return attributes;
  }

  public void validate() throws AttributeResolutionException {

  }

  /**
   * {@inheritDoc}
   * 
   * The identifiers of {@link Member}s which are also {@link Group}s are omitted. This
   * data connector considers only {@link Member} objects.
   */
  public Set<String> getAllIdentifiers() {
    return this.getAllIdentifiers(null);
  }

  /**
   * {@inheritDoc}
   * 
   * see {@link MemberDataConnector#getAllIdentifiers()}
   * 
   */
  public Set<String> getAllIdentifiers(Date updatedSince) {
    Set<String> identifiers = new TreeSet<String>();
    for (Group group : this.getGroups(updatedSince)) {
      for (Member member : this.getMembers(group)) {
        // don't return Groups
        if (!member.getSubjectSourceId().equals("g:gsa")) {
          identifiers.add(member.getSubjectId());
        }
      }
    }
    return identifiers;
  }

  /**
   * Get the members of a group using this object's grouper session callback.
   * 
   * @param group
   *          the {@link Group}
   * @return the <code>Set</code> of {@link Member}s
   */
  public Set<Member> getMembers(final Group group) {
    return (Set<Member>) GrouperSession.callbackGrouperSession(getGrouperSession(),
        new GrouperSessionHandler() {

          public Set<Member> callback(GrouperSession grouperSession) throws GrouperSessionException {
            return group.getMembers();
          }
        });
  }

}
