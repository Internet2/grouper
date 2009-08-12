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

import edu.internet2.middleware.grouper.shibboleth.dataConnector.GroupDataConnector;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.MemberDataConnector;

/**
 * Spring bean factory that produces {@link GroupDataConnector}s.
 */
public class MemberDataConnectorFactoryBean extends BaseGrouperDataConnectorFactoryBean {

  protected Object createInstance() throws Exception {
    MemberDataConnector connector = new MemberDataConnector();
    populateDataConnector(connector);
    connector.setFieldIdentifiers(getFieldIdentifiers());
    connector.initialize();
    return connector;
  }

  public Class getObjectType() {
    return MemberDataConnector.class;
  }

}