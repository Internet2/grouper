/*******************************************************************************
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
 ******************************************************************************/
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

package edu.internet2.middleware.ldappc.spml;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.ldappc.spml.definitions.PSODefinition;
import edu.internet2.middleware.ldappc.spml.definitions.TargetDefinition;
import edu.internet2.middleware.ldappc.spml.request.ProvisioningRequest;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;

public class PSPContext {

  private Map<String, BaseAttribute<?>> attributes;

  private ProvisioningRequest provisioningRequest;

  private PSP provisioningServiceProvider;

  private Map<TargetDefinition, List<PSODefinition>> targetAndObjectDefinitions;

  public Map<String, BaseAttribute<?>> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, BaseAttribute<?>> attributes) {
    this.attributes = attributes;
  }

  public ProvisioningRequest getProvisioningRequest() {
    return provisioningRequest;
  }

  public void setProvisioningRequest(ProvisioningRequest provisioningRequest) {
    this.provisioningRequest = provisioningRequest;
  }

  public PSP getProvisioningServiceProvider() {
    return provisioningServiceProvider;
  }

  public void setProvisioningServiceProvider(PSP provisioningServiceProvider) {
    this.provisioningServiceProvider = provisioningServiceProvider;
  }

  /**
   * @return Returns the targetAndObjectDefinitions.
   */
  public Map<TargetDefinition, List<PSODefinition>> getTargetAndObjectDefinitions() {
    return targetAndObjectDefinitions;
  }

  /**
   * @param targetAndObjectDefinitions
   *          The targetAndObjectDefinitions to set.
   */
  public void setTargetAndObjectDefinitions(Map<TargetDefinition, List<PSODefinition>> targetAndObjectDefinitions) {
    this.targetAndObjectDefinitions = targetAndObjectDefinitions;
  }
}
