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

package edu.internet2.middleware.grouper.shibboleth.dataConnector.config;

import java.util.List;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.BaseGrouperDataConnector;
import edu.internet2.middleware.grouper.shibboleth.filter.Filter;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.grouper.shibboleth.util.SubjectIdentifier;
import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.dataConnector.BaseDataConnectorFactoryBean;

/** Spring factory bean for {@link BaseGrouperDataConnector}s. */
public abstract class BaseGrouperDataConnectorFactoryBean extends BaseDataConnectorFactoryBean {

  /** The identifiers of attributes along with their source id. */
  private List<AttributeIdentifier> attributeIdentifiers;

  /** The matcher. */
  private Filter matcher;

  /** The subject and source identifier used to start a {@link GrouperSession}. */
  private SubjectIdentifier subjectIdentifier;

  /**
   * Gets the {@link AttributeIdentifer}s.
   * 
   * @return the {@link AttributeIdentifer}s.
   */
  public List<AttributeIdentifier> getAttributeIdentifiers() {
    return attributeIdentifiers;
  }

  /**
   * Sets the the {@link AttributeIdentifer}s.
   * 
   * @param attributeIdentifiers
   */
  public void setAttributeIdentifiers(List<AttributeIdentifier> attributeIdentifiers) {
    this.attributeIdentifiers = attributeIdentifiers;
  }

  /**
   * Gets the {@link Filter}.
   * 
   * @return the {@link Filter}.
   */
  public Filter getFilter() {
    return matcher;
  }

  /**
   * Sets the {@link Filter}.
   * 
   * @param Filter
   */
  public void setFilter(Filter filter) {
    this.matcher = filter;
  }

  /**
   * @return Returns the subjectIdentifier.
   */
  public SubjectIdentifier getSubjectIdentifier() {
    return subjectIdentifier;
  }

  /**
   * @param subjectIdentifier The subjectIdentifier to set.
   */
  public void setSubjectIdentifier(SubjectIdentifier subjectIdentifier) {
    this.subjectIdentifier = subjectIdentifier;
  }

  /**
   * {@inheritDoc}
   */
  protected void populateDataConnector(BaseGrouperDataConnector connector) {
    super.populateDataConnector(connector);
    connector.setFilter(getFilter());
    connector.setAttributeIdentifiers(getAttributeIdentifiers());
    connector.setSubjectIdentifier(getSubjectIdentifier());
    connector.initialize();
  }

}
