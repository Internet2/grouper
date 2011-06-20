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

import java.util.Set;

import edu.internet2.middleware.grouper.shibboleth.dataConnector.FindMemberBySubjectIdOrIdentifierDataConnector;
import edu.internet2.middleware.grouper.shibboleth.util.SourceIdentifier;

/** Spring bean factory that produces {@link FindMemberBySubjectIdOrIdentifierDataConnector}s. */
public class FindMemberBySubjectIdOrIdentifierDataConnectorFactoryBean extends BaseGrouperDataConnectorFactoryBean {

  /** The source identifiers. */
  private Set<SourceIdentifier> sourceIdentifiers;

  /**
   * Gets the source identifiers.
   * 
   * @return the source identifiers.
   */
  public Set<SourceIdentifier> getSourceIdentifiers() {
    return sourceIdentifiers;
  }

  /**
   * Sets the source identifiers.
   * 
   * @param sourceIdentifiers
   */
  public void setSourceIdentifiers(Set<SourceIdentifier> sourceIdentifiers) {
    this.sourceIdentifiers = sourceIdentifiers;
  }

  /** {@inheritDoc} */
  protected Object createInstance() throws Exception {
    FindMemberBySubjectIdOrIdentifierDataConnector connector = new FindMemberBySubjectIdOrIdentifierDataConnector();
    connector.setSourceIdentifiers(getSourceIdentifiers());
    populateDataConnector(connector);
    return connector;
  }

  /** {@inheritDoc} */
  public Class getObjectType() {
    return FindMemberBySubjectIdOrIdentifierDataConnector.class;
  }

}