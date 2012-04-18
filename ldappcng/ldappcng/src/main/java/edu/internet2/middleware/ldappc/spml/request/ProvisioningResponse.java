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

package edu.internet2.middleware.ldappc.spml.request;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openspml.v2.msg.PrefixAndNamespaceTuple;
import org.openspml.v2.msg.spml.Response;

import edu.internet2.middleware.ldappc.util.PSPUtil;

public abstract class ProvisioningResponse extends Response {

  /** The identifier of the object to be provisioned. */
  private ID m_id;

  public String getId() {
    return m_id == null ? null : m_id.getID();
  }

  public void setId(String id) {
    this.m_id = new ID(id);
  }

  public PrefixAndNamespaceTuple[] getNamespacesInfo() {
    return LdappcMarshallableCreator.staticGetNamespacesInfo();
  }

  public int hashCode() {
    int result = super.hashCode();
    result = 29 * result + (m_id != null ? m_id.hashCode() : 0);
    return result;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ProvisioningResponse)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    final ProvisioningResponse that = (ProvisioningResponse) o;

    if (m_id != null ? !m_id.equals(that.m_id) : that.m_id != null) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("id", this.getId());
    toStringBuilder.appendSuper(PSPUtil.toString((Response) this));
    return toStringBuilder.toString();
  }
}
