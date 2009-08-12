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

import org.openspml.v2.msg.spml.PSO;
import org.openspml.v2.util.xml.ArrayListWithType;
import org.openspml.v2.util.xml.ListWithType;

public class CalcResponse extends ProvisioningResponse {

  private ListWithType m_pso = new ArrayListWithType(PSO.class);

  public List<PSO> getPSOs() {
    return m_pso;
  }

  public void addPSO(PSO pso) {
    if (pso != null) {
      m_pso.add(pso);
    }
  }

  public void setPSOs(List<PSO> pso) {
    if (pso != null) {
      m_pso.addAll(pso);
    }
  }
}
