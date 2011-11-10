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

/** Spring bean factory that produces {@link LdapDnPSOIdentifierAttributeDefinition}s. */
public class LdapDnPSOIdentifierAttributeDefinitionFactoryBean extends BaseAttributeDefinitionFactoryBean {

  /** The LDAP DN base. */
  private String base;

  /** The LDAP RDN attribute name. */
  private String rdnAttributeName;

  /** The Grouper DN structure. */
  private GroupDNStructure structure;

  /**
   * Get the LDAP DN base.
   * 
   * @return the base DN
   */
  public String getBase() {
    return base;
  }

  /**
   * Set the LDAP DN base.
   * 
   * @param base the base DN
   */
  public void setBase(String base) {
    this.base = base;
  }

  /**
   * Get the LDAP RDN attribute name.
   * 
   * @return the RDN attribute name
   */
  public String getRdnAttributeName() {
    return rdnAttributeName;
  }

  /**
   * Set the LDAP RDN attribute name.
   * 
   * @param rdnAttributeName the RDN attribute name
   */
  public void setRdnAttributeName(String rdnAttributeName) {
    this.rdnAttributeName = rdnAttributeName;
  }

  /**
   * Get the Grouper DN structure.
   * 
   * @return the DN structure
   */
  public GroupDNStructure getStructure() {
    return structure;
  }

  /**
   * Set the Grouper DN structure.
   * 
   * @param structure the DN structure
   */
  public void setStructure(GroupDNStructure structure) {
    this.structure = structure;
  }

  /** {@inheritDoc} */
  protected Object createInstance() throws Exception {
    LdapDnPSOIdentifierAttributeDefinition definition = new LdapDnPSOIdentifierAttributeDefinition();
    populateAttributeDefinition(definition);
    definition.setBase(base);
    definition.setStructure(structure);
    definition.setRdnAttributeName(rdnAttributeName);
    return definition;
  }

  /** {@inheritDoc} */
  public Class getObjectType() {
    return LdapDnPSOIdentifierAttributeDefinition.class;
  }

}
