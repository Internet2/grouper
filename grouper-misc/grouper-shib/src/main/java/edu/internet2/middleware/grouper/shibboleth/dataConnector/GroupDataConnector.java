/**
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
 */
/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.GroupsField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.MembersField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.PrivilegeField;
import edu.internet2.middleware.grouper.shibboleth.filter.Filter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.DataConnector;
import edu.internet2.middleware.subject.Subject;

/** A {@link DataConnector} which returns {@link Group} attributes. */
public class GroupDataConnector extends BaseGrouperDataConnector<Group> implements DataConnector {

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(GroupDataConnector.class);

  /** The name of the attribute whose values are {@link GroupType}s.s */
  public static final String GROUP_TYPE_ATTR = "groupType";

  /** The name of the attribute whose values are alternate names. */
  public static final String ALTERNATE_NAME_ATTR = "alternateName";

  /** {@inheritDoc} */
  public Map<String, BaseAttribute> resolve(final ShibbolethResolutionContext resolutionContext)
      throws AttributeResolutionException {

    Map<String, BaseAttribute> attributes = (Map<String, BaseAttribute>) GrouperSession.callbackGrouperSession(
        getGrouperSession(), new GrouperSessionHandler() {

          public Map<String, BaseAttribute> callback(GrouperSession grouperSession) throws GrouperSessionException {

            String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();

            LOG.debug("Group data connector '{}' - Resolve principal '{}'", getId(), principalName);
            LOG.trace("Group data connector '{}' - Resolve principal '{}' requested attributes {}", new Object[] {
                getId(), principalName, resolutionContext.getAttributeRequestContext().getRequestedAttributesIds() });

            if (principalName.startsWith(CHANGELOG_PRINCIPAL_NAME_PREFIX)) {
              LOG.debug("Group data connector '{}' - Ignoring principal name '{}'", getId(), principalName);
              return Collections.EMPTY_MAP;
            }

            // find group
            Group group = GroupFinder.findByName(getGrouperSession(), principalName, false);
            if (group == null) {
              LOG.debug("Group data connector '{}' - Resolve principal '{}' unable to find group.", getId(),
                  principalName);
              return Collections.EMPTY_MAP;
            }
            LOG.debug("Group data connector '{}' - Resolve principal '{}' found group '{}'", new Object[] { getId(),
                principalName, group });

            // match filter
            Filter<Group> matchQueryFilter = getFilter();
            if (matchQueryFilter != null && !matchQueryFilter.matches(group)) {
              LOG.debug("Group data connector '{}' - Resolve principal '{}' group '{}' does not match filter.",
                  new Object[] { getId(), principalName, group });
              return Collections.EMPTY_MAP;
            }

            // build attributes
            Map<String, BaseAttribute> attributes = buildAttributes(group);

            LOG.debug("Group data connector '{}' - Resolve principal '{}' attributes {}", new Object[] { getId(),
                principalName, attributes });

            if (LOG.isTraceEnabled()) {
              for (String key : attributes.keySet()) {
                for (Object value : attributes.get(key).getValues()) {
                  LOG.trace("Group data connector '{}' - Resolve principal '{}' attribute {} : '{}'", new Object[] {
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
   * Return attributes for the given {@link Group}.
   * 
   * @param group the group
   * @return the attributes
   */
  protected Map<String, BaseAttribute> buildAttributes(Group group) {

    Map<String, BaseAttribute> attributes = new LinkedHashMap<String, BaseAttribute>();

    // internal attributes
    for (String attributeName : Group.INTERNAL_FIELD_ATTRIBUTES) {
      String value = (String) GrouperUtil.fieldValue(group, attributeName);
      if (value != null) {
        BasicAttribute<String> basicAttribute = new BasicAttribute<String>(attributeName);
        basicAttribute.setValues(Arrays.asList(new String[] { value }));
        attributes.put(attributeName, basicAttribute);
      }
    }

    // attribute defs
    for (String attributeDefName : getAttributeDefNames()) {
      List<String> values = group.getAttributeValueDelegate().retrieveValuesString(attributeDefName);
      if (values != null && !values.isEmpty()) {
        BasicAttribute<String> basicAttribute = new BasicAttribute<String>(attributeDefName);
        basicAttribute.setValues(values);
        attributes.put(attributeDefName, basicAttribute);
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

    // groupType
    BasicAttribute<GroupType> groupTypes = new BasicAttribute<GroupType>(GROUP_TYPE_ATTR);
    groupTypes.setValues(group.getTypes());
    attributes.put(groupTypes.getId(), groupTypes);

    // alternate names
    Set<String> alternateNames = group.getAlternateNames();
    if (alternateNames != null && !alternateNames.isEmpty()) {
      BasicAttribute<String> basicAttribute = new BasicAttribute<String>(ALTERNATE_NAME_ATTR);
      basicAttribute.setValues(alternateNames);
      attributes.put(basicAttribute.getId(), basicAttribute);
    }

    return attributes;
  }

  /** {@inheritDoc} */
  public void validate() throws AttributeResolutionException {

  }
}
