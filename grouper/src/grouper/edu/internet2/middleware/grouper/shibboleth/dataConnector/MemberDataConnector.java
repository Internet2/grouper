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
import edu.internet2.middleware.grouper.shibboleth.GroupsField;
import edu.internet2.middleware.grouper.shibboleth.PrivilegeField;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.subject.Subject;

public class MemberDataConnector extends BaseGrouperDataConnector {

  private static final Logger LOG = GrouperUtil.getLogger(MemberDataConnector.class);

  // TODO source identifiers
  private Set<String> sourceIdentifiers;

  public Map<String, BaseAttribute> resolve(ShibbolethResolutionContext resolutionContext)
      throws AttributeResolutionException {

    String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();
    String msg = "'" + principalName + "' dc '" + this.getId() + "'";
    LOG.debug("resolve {}", msg);
    if (LOG.isDebugEnabled()) {
      for (String attrId : resolutionContext.getAttributeRequestContext().getRequestedAttributesIds()) {
        LOG.trace("resolve {} requested attribute '{}'", msg, attrId);
      }
    }

    Map<String, BaseAttribute> attributes = new HashMap<String, BaseAttribute>();

    Subject subject = SubjectFinder.findByIdOrIdentifier(principalName, false);
    if (subject == null) {
      LOG.debug("resolve {} subject not found", msg);
      return attributes;
    }
    LOG.debug("resolve {} found subject '{}'", msg, GrouperUtil.subjectToString(subject));

    // TODO
    if (subject.getSource().getId().equals("g:gsa")) {
      LOG.debug("resolve {} returning empty map for g:gsa", msg);
      return attributes;
    }

    Member member = MemberFinder.findBySubject(grouperSession, subject, false);
    if (member == null) {
      LOG.debug("resolve {} member not found", msg);
      return attributes;
    }
    LOG.debug("resolve {} found member '{}'", msg, member);

    // groups
    for (GroupsField groupsField : groupsFields) {
      BaseAttribute<Group> attr = groupsField.getAttribute(member);
      if (attr != null) {
        // TODO does group match
        attributes.put(groupsField.getId(), attr);
      }
    }

    // privs
    for (PrivilegeField privilegeField : privilegeFields) {
      BaseAttribute<Group> attr = privilegeField.getAttribute(subject);
      if (attr != null) {
        attributes.put(privilegeField.getId(), attr);
      }
    }

    // internal attributes
    BasicAttribute<String> id = new BasicAttribute<String>("id");
    id.setValues(Arrays.asList(new String[] { subject.getId() }));
    attributes.put(id.getId(), id);

    BasicAttribute<String> name = new BasicAttribute<String>("name");
    name.setValues(Arrays.asList(new String[] { subject.getName() }));
    attributes.put(name.getId(), name);

    BasicAttribute<String> description = new BasicAttribute<String>("description");
    description.setValues(Arrays.asList(new String[] { subject.getDescription() }));
    attributes.put(description.getId(), description);

    // TODO expensive to get all subject attributes ? maybe use <attribute />
    Map<String, Set<String>> subjectAttributes = subject.getAttributes();
    for (String attributeName : subjectAttributes.keySet()) {
      BasicAttribute<String> basicAttribute = new BasicAttribute<String>(attributeName);
      basicAttribute.setValues(subjectAttributes.get(attributeName));
      attributes.put(basicAttribute.getId(), basicAttribute);
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
