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

package edu.internet2.middleware.grouper.shibboleth.attributeDefinition.config;

import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.LdapDnPSOIdentifierAttributeDefinition;
import edu.internet2.middleware.ldappc.LdappcConfig.GroupDNStructure;
import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.attributeDefinition.BaseAttributeDefinitionFactoryBean;

public class LdapDnPSOIdentifierAttributeDefinitionFactoryBean extends BaseAttributeDefinitionFactoryBean {

  private String base;

  private GroupDNStructure structure;

  private String rdnAttributeName;

  public String getRdnAttributeName() {
    return rdnAttributeName;
  }

  public void setRdnAttributeName(String rdnAttributeName) {
    this.rdnAttributeName = rdnAttributeName;
  }

  public String getBase() {
    return base;
  }

  public void setBase(String base) {
    this.base = base;
  }

  public GroupDNStructure getStructure() {
    return structure;
  }

  public void setStructure(GroupDNStructure structure) {
    this.structure = structure;
  }

  protected Object createInstance() throws Exception {
    LdapDnPSOIdentifierAttributeDefinition definition = new LdapDnPSOIdentifierAttributeDefinition();
    populateAttributeDefinition(definition);
    definition.setBase(base);
    definition.setStructure(structure);
    definition.setRdnAttributeName(rdnAttributeName);
    return definition;
  }

  public Class getObjectType() {
    return LdapDnPSOIdentifierAttributeDefinition.class;
  }

}
