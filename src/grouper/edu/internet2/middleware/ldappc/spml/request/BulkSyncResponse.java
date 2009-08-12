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

import org.openspml.v2.util.xml.ArrayListWithType;
import org.openspml.v2.util.xml.ListWithType;

public class BulkSyncResponse extends ProvisioningResponse {

  private ListWithType m_response = new ArrayListWithType(SyncResponse.class);

  public List<SyncResponse> getResponses() {
    return m_response;
  }

  public void addResponse(SyncResponse response) {
    m_response.add(response);
  }

  public int hashCode() {
    int result = super.hashCode();
    result = 29 * result + (m_response != null ? m_response.hashCode() : 0);
    return result;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BulkSyncResponse)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    final BulkSyncResponse that = (BulkSyncResponse) o;

    if (m_response != null ? !m_response.equals(that.m_response) : that.m_response != null) {
      return false;
    }

    return true;
  }
}
