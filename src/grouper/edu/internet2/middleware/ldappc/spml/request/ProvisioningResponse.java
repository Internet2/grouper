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

import org.openspml.v2.msg.BasicOpenContentAttr;
import org.openspml.v2.msg.PrefixAndNamespaceTuple;
import org.openspml.v2.msg.spml.Response;

public abstract class ProvisioningResponse extends Response {

  private ID m_id;

  public String getId() {
    return m_id.getID();
  }

  public void setId(String id) {
    this.m_id = new ID(id);
    setID(id);
  }

  public ID getIdentifier() {
    return m_id;
  }

  public void setIdentifier(ID id) {
    this.m_id = id;
    setID(id.getID());
  }

  protected void setID(String id) {
    String oldId = this.findOpenContentAttrValueByName("ID");
    if (oldId == null) {
      this.addOpenContentAttr("ID", id);
    } else {
      this.removeOpenContentAttr(new BasicOpenContentAttr("ID", oldId));
      this.addOpenContentAttr("ID", id);
    }
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

}
