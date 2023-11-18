/**
 * Copyright 2014 Internet2
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
 */
package edu.internet2.middleware.grouper.ldap.ldaptive;

import org.ldaptive.CompareConnectionValidator;

/**
 * Custom connection validator that performs an LDAP compare operation. This class exposes properties to allow for
 * configuration wiring in {@link LdaptiveConfiguration}.
 */
public class LdaptiveConnectionValidator extends CompareConnectionValidator {

  public String getDn() {
    return super.getCompareRequest().getDn();
  }

  public void setDn(String dn) {
    super.getCompareRequest().setDn(dn);
  }

  public String getName() {
    return super.getCompareRequest().getName();
  }

  public void setName(String name) {
    super.getCompareRequest().setName(name);
  }

  public String getValue() {
    return super.getCompareRequest().getValue();
  }

  public void setValue(String value) {
    super.getCompareRequest().setValue(value);
  }
}
