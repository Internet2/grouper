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
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.GroupsField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.MembersField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.PrivilegeField;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.DataConnector;
import edu.internet2.middleware.subject.Subject;

/**
 * A {@link DataConnector} which returns {@link Group}s. The attributes of the returned groups may be limited in order
 * to avoid unnecessary queries to the Grouper database.
 */
public class GroupDataConnector extends BaseGrouperDataConnector {

  /** logger */
  private static final Logger LOG = GrouperUtil.getLogger(GroupDataConnector.class);

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

    Map<String, BaseAttribute> attributes = new LinkedHashMap<String, BaseAttribute>();

    // find group
    Group group = GroupFinder.findByName(getGrouperSession(), principalName, false);
    if (group == null) {
      LOG.debug("resolve {} group not found", msg);
      return attributes;
    }
    LOG.debug("resolve {} found group '{}'", msg, group);

    // TODO does group match

    // internal attributes
    for (String attributeName : Group.INTERNAL_FIELD_ATTRIBUTES) {
      String value = (String) GrouperUtil.fieldValue(group, attributeName);
      if (value != null) {
        BasicAttribute<String> basicAttribute = new BasicAttribute<String>(attributeName);
        basicAttribute.setValues(Arrays.asList(new String[] { value }));
        attributes.put(attributeName, basicAttribute);
      }
    }

    // custom attributes
    Map<String, Attribute> customAttributes = group.getAttributesMap(false);
    for (String attributeName : customAttributes.keySet()) {
      String value = customAttributes.get(attributeName).getValue();
      if (value != null) {
        BasicAttribute<String> basicAttribute = new BasicAttribute<String>(attributeName);
        basicAttribute.setValues(Arrays.asList(new String[] { value }));
        attributes.put(attributeName, basicAttribute);
      }
    }

    // members
    for (MembersField membersField : getMembersFields()) {
      BaseAttribute<Member> attr = membersField.getAttribute(group);
      if (attr != null) {
        attributes.put(membersField.getId(), attr);
      }
    }

    // groups
    for (GroupsField groupsField : getGroupsFields()) {
      BaseAttribute<Group> attr = groupsField.getAttribute(group.toMember());
      if (attr != null) {
        attributes.put(groupsField.getId(), attr);
      }
    }

    // privs
    for (PrivilegeField privilegeField : getPrivilegeFields()) {
      BaseAttribute<Subject> attr = privilegeField.getAttribute(group);
      if (attr != null) {
        attributes.put(privilegeField.getId(), attr);
      }
    }

    // stem attribute
    BasicAttribute<String> stem = new BasicAttribute<String>(PARENT_STEM_NAME_ATTR);
    stem.setValues(Arrays.asList(new String[] { group.getParentStemName() }));
    attributes.put(stem.getId(), stem);

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

  /** {@inheritDoc} */
  public void validate() throws AttributeResolutionException {

  }
}
