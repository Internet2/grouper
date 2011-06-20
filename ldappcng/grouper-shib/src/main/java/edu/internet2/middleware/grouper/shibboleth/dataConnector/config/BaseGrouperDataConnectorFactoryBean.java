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

import edu.internet2.middleware.grouper.shibboleth.dataConnector.BaseGrouperDataConnector;
import edu.internet2.middleware.grouper.shibboleth.filter.MatchQueryFilter;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.dataConnector.BaseDataConnectorFactoryBean;

/** Spring factory bean for {@link BaseGrouperDataConnector}s. */
public abstract class BaseGrouperDataConnectorFactoryBean extends BaseDataConnectorFactoryBean {

  /** Attribute identifiers. */
  private List<AttributeIdentifier> fieldIdentifiers;

  /** The match query filter. */
  private MatchQueryFilter matchQueryFilter;

  /** The subject identifier used to start a <code>GrouperSession</code>. */
  private AttributeIdentifier subjectIdentifier;

  /**
   * Gets the {@link AttributeIdentifer}s.
   * 
   * @return the {@link AttributeIdentifer}s.
   */
  public List<AttributeIdentifier> getFieldIdentifiers() {
    return fieldIdentifiers;
  }

  /**
   * Sets the the {@link AttributeIdentifer}s.
   * 
   * @param fieldIdentifiers
   */
  public void setFieldIdentifiers(List<AttributeIdentifier> fieldIdentifiers) {
    this.fieldIdentifiers = fieldIdentifiers;
  }

  /**
   * Gets the {@link MatchQueryFilter}.
   * 
   * @return the {@link MatchQueryFilter}.
   */
  public MatchQueryFilter getMatchQueryFilter() {
    return matchQueryFilter;
  }

  /**
   * Sets the {@link MatchQueryFilter}.
   * 
   * @param matchQueryFilter
   */
  public void setMatchQueryFilter(MatchQueryFilter matchQueryFilter) {
    this.matchQueryFilter = matchQueryFilter;
  }

  /**
   * @return Returns the subjectIdentifier.
   */
  public AttributeIdentifier getSubjectIdentifier() {
    return subjectIdentifier;
  }

  /**
   * @param subjectIdentifier The subjectIdentifier to set.
   */
  public void setSubjectIdentifier(AttributeIdentifier subjectIdentifier) {
    this.subjectIdentifier = subjectIdentifier;
  }

  /**
   * {@inheritDoc}
   */
  protected void populateDataConnector(BaseGrouperDataConnector connector) {
    super.populateDataConnector(connector);
    connector.setMatchQueryFilter(this.getMatchQueryFilter());
    connector.setFieldIdentifiers(this.getFieldIdentifiers());
    connector.setSubjectIdentifier(this.getSubjectIdentifier());
    connector.initialize();
  }

}
