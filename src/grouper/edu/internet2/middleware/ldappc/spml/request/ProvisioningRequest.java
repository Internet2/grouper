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

import java.util.ArrayList;
import java.util.List;

import org.openspml.v2.msg.BasicOpenContentAttr;
import org.openspml.v2.msg.PrefixAndNamespaceTuple;
import org.openspml.v2.msg.spml.BatchableRequest;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.util.xml.ArrayListWithType;

public abstract class ProvisioningRequest extends BatchableRequest {

  // private String[] m_id = new String[1];
  private ID m_id;

  private ReturnData m_returnData = ReturnData.EVERYTHING;

  // private List<String> m_targetId = new ArrayList<String>();
  private ArrayListWithType m_targetId = new ArrayListWithType(ID.class);

  public ProvisioningRequest() {
  };

  public PrefixAndNamespaceTuple[] getNamespacesInfo() {
    return LdappcMarshallableCreator.staticGetNamespacesInfo();
  }

  public String getId() {
    return m_id.getID();
  }

  public ID getIdentifier() {
    return m_id;
  }

  public void setId(String id) {
    this.m_id = new ID(id);
    setID(id);
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

  public ReturnData getReturnData() {
    return m_returnData;
  }

  public void setReturnData(ReturnData data) {
    m_returnData = data;
  }

  public List<ID> getTargetIdentifiers() {
    return m_targetId;
  }

  public List<String> getTargetIds() {
    ArrayList<String> ids = new ArrayList<String>();
    for (ID id : getTargetIdentifiers()) {
      ids.add(id.getID());
    }
    return ids;
  }

  public void addTargetId(String id) {
    if (id != null) {
      m_targetId.add(new ID(id));
    }
  }

  public void addTargetIdentifier(ID id) {
    if (id != null) {
      m_targetId.add(id);
    }
  }

  public void setTargetIds(List<String> targetIds) {
    for (String targetId : targetIds) {
      addTargetId(targetId);
    }
  }

  public int hashCode() {
    int result = super.hashCode();
    result = 29 * result + (m_id != null ? m_id.hashCode() : 0);
    result = 29 * result + (m_targetId != null ? m_targetId.hashCode() : 0);
    result = 29 * result + (m_returnData != null ? m_returnData.hashCode() : 0);
    return result;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ProvisioningRequest)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    final ProvisioningRequest that = (ProvisioningRequest) o;

    if (!m_id.equals(that.m_id)) {
      return false;
    }
    if (m_targetId != null ? !m_targetId.equals(that.m_targetId) : that.m_targetId != null) {
      return false;
    }
    if (m_returnData != null ? !m_returnData.equals(that.m_returnData) : that.m_returnData != null) {
      return false;
    }

    return true;
  }

  public boolean isValid() {
    // TODO better validity checking
    if (m_id != null) {
      return true;
    }
    return false;
  }

  public String toString() {
    return "id='" + m_id.getID() + "' returnData='" + m_returnData + "' targetIds='" + getTargetIds() + "'";
  }
}
