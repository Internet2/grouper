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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.SubjectAttributeDefinition;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.GroupsField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.PrivilegeField;
import edu.internet2.middleware.grouper.shibboleth.filter.Filter;
import edu.internet2.middleware.grouper.shibboleth.filter.MemberSourceFilter;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.DataConnector;
import edu.internet2.middleware.subject.Subject;

/** A {@link DataConnector} which returns {@link Member} attributes. */
public class MemberDataConnector extends BaseGrouperDataConnector<Member> {

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(MemberDataConnector.class);

  /** {@inheritDoc} */
  public Map<String, BaseAttribute> resolve(final ShibbolethResolutionContext resolutionContext)
      throws AttributeResolutionException {

    Map<String, BaseAttribute> attributes = (Map<String, BaseAttribute>) GrouperSession.callbackGrouperSession(
        getGrouperSession(), new GrouperSessionHandler() {

          public Map<String, BaseAttribute> callback(GrouperSession grouperSession) throws GrouperSessionException {

            String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();

            LOG.debug("Member data connector '{}' - Resolve principal '{}'", getId(), principalName);
            LOG.trace("Member data connector '{}' - Resolve principal '{}' requested attributes {}", new Object[] {
                getId(), principalName, resolutionContext.getAttributeRequestContext().getRequestedAttributesIds() });

            if (principalName.startsWith(CHANGELOG_PRINCIPAL_NAME_PREFIX)) {
              LOG.debug("Group data connector '{}' - Ignoring principal name '{}'", getId(), principalName);
              return Collections.EMPTY_MAP;
            }

            // find subject
            Subject subject = null;

            // match filter
            Filter<Member> filter = getFilter();

            // use the configured source id if present to shortcut search
            if (filter != null && filter instanceof MemberSourceFilter) {
              String sourceId = ((MemberSourceFilter) filter).getSourceId();
              subject = SubjectFinder.findByIdOrIdentifierAndSource(principalName, sourceId, false);
            } else {
              subject = SubjectFinder.findByIdOrIdentifier(principalName, false);
            }

            if (subject == null) {
              LOG.debug("Member data connector '{}' - Resolve principal '{}' unable to find subject.", getId(),
                  principalName);
              return Collections.EMPTY_MAP;
            }

            // don't return group objects, that's the GroupDataConnector
            if (isInternal(subject)) {
              LOG.debug("Member data connector '{}' - Resolve principal '{}' subject is internal to grouper.", getId(),
                  principalName);
              return Collections.EMPTY_MAP;
            }

            LOG.debug("Member data connector '{}' - Resolve principal '{}' found subject '{}'", new Object[] { getId(),
                principalName, GrouperUtil.subjectToString(subject) });

            // find member
            Member member = MemberFinder.findBySubject(getGrouperSession(), subject, false);
            if (member == null) {
              LOG.debug("Member data connector '{}' - Resolve principal '{}' member not found for subject '{}",
                  new Object[] { getId(), principalName, GrouperUtil.subjectToString(subject) });
              return Collections.EMPTY_MAP;
            }
            LOG.debug("Member data connector '{}' - Resolve principal '{}' found member '{}'", new Object[] { getId(),
                principalName, member });

            // match filter
            if (filter != null && !filter.matches(member)) {
              LOG.debug("Member data connector '{}' - Resolve principal '{}' member '{}' does not match filter.",
                  new Object[] { getId(), principalName, member });
              return Collections.EMPTY_MAP;
            }

            // build attributes
            Map<String, BaseAttribute> attributes = buildAttributes(member);

            LOG.debug("Member data connector '{}' - Resolve principal '{}' attributes {}", new Object[] { getId(),
                principalName, attributes });

            if (LOG.isTraceEnabled()) {
              for (String key : attributes.keySet()) {
                for (Object value : attributes.get(key).getValues()) {
                  LOG.trace("Member data connector '{}' - Resolve principal '{}' attribute {} : '{}'", new Object[] {
                      getId(), principalName, key, value });
                }
              }
            }

            return attributes;
          }
        });

    return attributes;
  }

  /**
   * Return attributes for the given {@link Member}.
   * 
   * @param member the member
   * @return the attributes
   */
  protected Map<String, BaseAttribute> buildAttributes(Member member) {

    Map<String, BaseAttribute> attributes = new LinkedHashMap<String, BaseAttribute>();

    Subject subject = member.getSubject();

    // id
    BasicAttribute<String> id = new BasicAttribute<String>("id");
    id.setValues(Arrays.asList(new String[] { subject.getId() }));
    attributes.put(id.getId(), id);

    // defined subject attributes
    for (AttributeIdentifier attributeIdentifier : getAttributeIdentifiers()) {
      Set<String> values = SubjectAttributeDefinition.getValues(subject, attributeIdentifier.getId());
      if (!values.isEmpty()) {
        BasicAttribute<String> attribute = new BasicAttribute<String>(attributeIdentifier.getId());
        attribute.setValues(values);
        attributes.put(attribute.getId(), attribute);
      }
    }

    // groups
    for (GroupsField groupsField : getGroupsFields()) {
      BaseAttribute<Group> attr = groupsField.getAttribute(member, getFilter());
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

    return attributes;
  }

  public void validate() throws AttributeResolutionException {

  }

  /**
   * Returns true if the subject is internal to Grouper, in either the {@link InternalSourceAdapter} for the
   * GrouperSystem and GrouperAll subjects or the {@link GrouperSourceAdapter} for {@link Group}s.
   * 
   * @param subject the subject
   * @return true if the subject is internal to Grouper
   */
  protected boolean isInternal(Subject subject) {

    if (subject.getSourceId().equals(SubjectFinder.internal_getGSA().getId())) {
      return true;
    }

    if (subject.getSourceId().equals(InternalSourceAdapter.ID)) {
      return true;
    }

    return false;
  }
}
