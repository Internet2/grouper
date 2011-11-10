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
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.Response;

import edu.internet2.middleware.ldappc.util.PSPUtil;

// TODO there is a bug here with regards to xml format, see tests

public class SynchronizedResponse extends Response {

  private PSOIdentifier m_psoID = null;

  public PSOIdentifier getPsoID() {
    return m_psoID;
  }

  public void setPsoID(PSOIdentifier psoID) {
    m_psoID = psoID;
  }

  public PrefixAndNamespaceTuple[] getNamespacesInfo() {
    return LdappcMarshallableCreator.staticGetNamespacesInfo();
  }

  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof SynchronizedResponse))
      return false;
    if (!super.equals(o))
      return false;

    final SynchronizedResponse that = (SynchronizedResponse) o;

    if (m_psoID != null ? !m_psoID.equals(that.m_psoID) : that.m_psoID != null) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result = super.hashCode();
    result = 29 * result + (m_psoID != null ? m_psoID.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("psoID", PSPUtil.toString(this.getPsoID()));
    toStringBuilder.appendSuper(super.toString());
    return toStringBuilder.toString();
  }
}
