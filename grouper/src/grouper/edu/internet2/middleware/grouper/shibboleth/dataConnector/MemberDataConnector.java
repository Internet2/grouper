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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.GroupsField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.PrivilegeField;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.DataConnector;
import edu.internet2.middleware.subject.Subject;

/**
 * A {@link DataConnector} which returns {@link Member}s. The attributes of the returned members may be limited in order
 * to avoid unnecessary queries to the Grouper database.
 */
public class MemberDataConnector extends BaseGrouperDataConnector {

  /** logger */
  private static final Logger LOG = GrouperUtil.getLogger(MemberDataConnector.class);

  /** the name of the attribute representing a subject's id */
  public static final String ID_ATTRIBUTE_NAME = "id";

  /** the name of the attribute representing a subject's name */
  public static final String NAME_ATTRIBUTE_NAME = "name";

  /** the name of the attribute representing a subject's description */
  public static final String DESCRIPTION_ATTRIBUTE_NAME = "description";

  // TODO source identifiers
  private Set<String> sourceIdentifiers;

  /** {@inheritDoc} */
  public Map<String, BaseAttribute> resolve(ShibbolethResolutionContext resolutionContext)
      throws AttributeResolutionException {

    String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();
    String msg = "'" + principalName + "' dc '" + this.getId() + "'";
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
    if (subject.getSource().getId().equals(SubjectFinder.internal_getGSA().getId())) {
      LOG.debug("resolve {} returning empty map for '{}' source", msg, SubjectFinder.internal_getGSA().getId());
      return attributes;
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
    LOG.debug("resolve {} subjectIDs {}", msg, this.getSubjectAttributeIdentifiers());
    for (AttributeIdentifier attributeIdentifier : this.getSubjectAttributeIdentifiers()) {
      LOG.debug("resolve {} member {} field {}", new Object[] { msg, member, attributeIdentifier });
      if (subject.getSourceId().equals(attributeIdentifier.getSource())) {
        // name
        if (attributeIdentifier.getId().equals(NAME_ATTRIBUTE_NAME)) {
          String name = subject.getName();
          if (name != null) {
            BasicAttribute<String> nameAttribute = new BasicAttribute<String>(NAME_ATTRIBUTE_NAME);
            nameAttribute.setValues(GrouperUtil.toList(name));
            attributes.put(nameAttribute.getId(), nameAttribute);
          }
          // description
        } else if (attributeIdentifier.getId().equals(DESCRIPTION_ATTRIBUTE_NAME)) {
          String description = subject.getDescription();
          if (description != null) {
            BasicAttribute<String> descriptionAttribute = new BasicAttribute<String>(DESCRIPTION_ATTRIBUTE_NAME);
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
      BaseAttribute<Group> attr = groupsField.getAttribute(member);
      if (attr != null) {
        // TODO does group match
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

    if (LOG.isDebugEnabled()) {
      LOG.debug("resolve {} attributes {}", msg, attributes.size());
      for (String key : attributes.keySet()) {
        for (Object value : attributes.get(key).getValues()) {
          LOG.debug("resolve {} '{}' : {}", new Object[] { msg, key, PSPUtil.getString(value) });
        }
      }
    }

    return attributes;
  }

  public void validate() throws AttributeResolutionException {

  }

}
