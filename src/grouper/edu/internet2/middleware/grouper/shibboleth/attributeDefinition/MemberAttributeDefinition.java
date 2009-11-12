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

package edu.internet2.middleware.grouper.shibboleth.attributeDefinition;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition.BaseAttributeDefinition;
import edu.internet2.middleware.subject.Subject;

public class MemberAttributeDefinition extends BaseAttributeDefinition {

  private static Logger LOG = LoggerFactory.getLogger(MemberAttributeDefinition.class);

  private List<AttributeIdentifier> attributes;

  public void setAttributes(List<AttributeIdentifier> attributes) {
    this.attributes = attributes;
  }

  protected BaseAttribute doResolve(ShibbolethResolutionContext resolutionContext) throws AttributeResolutionException {

    String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();
    String msg = "resolve '" + principalName + "' ad '" + this.getId() + "'";
    LOG.debug("{}", msg);

    BasicAttribute<String> attribute = new BasicAttribute<String>(this.getId());

    Collection<?> values = this.getValuesFromAllDependencies(resolutionContext);
    if (LOG.isDebugEnabled()) {
      for (Object value : values) {
        LOG.debug("{} values from dependencies '{}'", msg, value);
      }
    }

    if (values == null) {
      LOG.debug("{} no dependency values", msg);
      return attribute;
    }

    for (Object value : values) {
      if (!(value instanceof Member)) {
        LOG.error("{} Unable to resolve attribute, dependency value is not a Member : {}", msg, value.getClass());
        throw new AttributeResolutionException("Unable to resolve attribute, dependency value is not a Member");
      }

      Member member = (Member) value;
      Subject subject = member.getSubject();

      for (AttributeIdentifier attr : attributes) {
        if (member.getSubjectSourceId().equals(attr.getSource())) {
          Set<String> subjectValues = null;
          // TODO this should be fixed
          if (attr.getId().equalsIgnoreCase("id")) {
            subjectValues = new HashSet<String>();
            subjectValues.add(subject.getId());
          } else if (attr.getId().equalsIgnoreCase("name")) {
            subjectValues = new HashSet<String>();
            subjectValues.add(subject.getName());
          } else if (attr.getId().equalsIgnoreCase("description")) {
            subjectValues = new HashSet<String>();
            subjectValues.add(subject.getDescription());
          } else {
            subjectValues = subject.getAttributeValues(attr.getId());
          }
          if (subjectValues != null) {
            attribute.getValues().addAll(subjectValues);
          }
        }
      }
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("{} values {}", msg, attribute.getValues().size());
      for (Object value : attribute.getValues()) {
        LOG.debug("{} value '{}'", msg, PSPUtil.getString(value));
      }
    }

    return attribute;
  }

  public void validate() throws AttributeResolutionException {

  }
}
