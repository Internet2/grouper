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

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openspml.v2.msg.PrefixAndNamespaceTuple;
import org.openspml.v2.msg.spml.BatchableRequest;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.SchemaEntityRef;
import org.openspml.v2.util.xml.ArrayListWithType;
import org.openspml.v2.util.xml.ListWithType;

import edu.internet2.middleware.ldappc.util.PSPUtil;

public abstract class ProvisioningRequest extends BatchableRequest {

  /** The identifier of the object to be provisioned. */
  private ID m_id;

  /** The return data. */
  private ReturnData m_returnData = ReturnData.EVERYTHING;

  /** The schema entities to be considered. */
  private ListWithType m_schemaEntity = new ArrayListWithType(SchemaEntityRef.class);

  public ProvisioningRequest() {
  };

  public PrefixAndNamespaceTuple[] getNamespacesInfo() {
    return LdappcMarshallableCreator.staticGetNamespacesInfo();
  }

  public String getId() {
    return m_id == null ? null : m_id.getID();
  }

  public void setId(String id) {
    this.m_id = new ID(id);
  }

  public ReturnData getReturnData() {
    return m_returnData;
  }

  public void setReturnData(ReturnData data) {
    m_returnData = data;
  }

  public void addSchemaEntity(SchemaEntityRef se) {
    if (se != null) {
      m_schemaEntity.add(se);
    }
  }

  public void setSchemaEntities(List<SchemaEntityRef> se) {
    m_schemaEntity.clear();
    for (SchemaEntityRef s : se) {
      this.addSchemaEntity(s);
    }
  }

  public List<SchemaEntityRef> getSchemaEntities() {
    return m_schemaEntity;
  }

  public int hashCode() {
    int result = super.hashCode();
    result = 29 * result + (m_id != null ? m_id.hashCode() : 0);
    result = 29 * result + (m_returnData != null ? m_returnData.hashCode() : 0);
    result = 29 * result + (m_schemaEntity != null ? m_schemaEntity.hashCode() : 0);
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
    if (m_returnData != null ? !m_returnData.equals(that.m_returnData) : that.m_returnData != null) {
      return false;
    }
    if (m_schemaEntity != null ? !m_schemaEntity.equals(that.m_schemaEntity) : that.m_schemaEntity != null) {
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

  @Override
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("id", this.getId());
    toStringBuilder.append("requestID", this.getRequestID());
    toStringBuilder.append("returnData", this.getReturnData());
    for (SchemaEntityRef s : this.getSchemaEntities()) {
      toStringBuilder.append("schemaEntityRef", PSPUtil.toString(s));
    }
    return toStringBuilder.toString();
  }
}
