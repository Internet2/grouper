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

import edu.internet2.middleware.grouper.shibboleth.Attribute;
import edu.internet2.middleware.grouper.shibboleth.filter.GroupQueryFilter;
import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.dataConnector.BaseDataConnectorFactoryBean;

public abstract class BaseGrouperDataConnectorFactoryBean extends BaseDataConnectorFactoryBean {

  private List<Attribute> fieldIdentifiers;

  private GroupQueryFilter groupQueryFilter;

  public List<Attribute> getFieldIdentifiers() {
    return fieldIdentifiers;
  }

  public void setFieldIdentifiers(List<Attribute> fieldIdentifiers) {
    this.fieldIdentifiers = fieldIdentifiers;
  }

  public GroupQueryFilter getGroupQueryFilter() {
    return groupQueryFilter;
  }

  public void setGroupQueryFilter(GroupQueryFilter groupQueryFilter) {
    this.groupQueryFilter = groupQueryFilter;
  }

}
