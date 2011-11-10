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

package edu.internet2.middleware.ldappc.spml.provider;

import edu.internet2.middleware.ldappc.spml.PSP;
import edu.internet2.middleware.ldappc.spml.definitions.TargetDefinition;

/** Base class for a {@link SpmlTargetProvider}. */
public abstract class BaseSpmlTargetProvider extends BaseSpmlProvider implements SpmlTargetProvider {

  /** The target definition. */
  private TargetDefinition targetDefinition;

  /** The provisioning service provider. */
  private PSP psp;

  /** {@inheritDoc} */
  public PSP getPSP() {
    return psp;
  }

  /** {@inheritDoc} */
  public void setPSP(PSP psp) {
    this.psp = psp;
  }

  /** {@inheritDoc} */
  public TargetDefinition getTargetDefinition() {
    return targetDefinition;
  }

  /** {@inheritDoc} */
  public void setTargetDefinition(TargetDefinition targetDefinition) {
    this.targetDefinition = targetDefinition;
  }

}
