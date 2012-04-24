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

import java.net.URI;
import java.net.URISyntaxException;

import org.openspml.v2.msg.OpenContentElement;
import org.openspml.v2.msg.PrefixAndNamespaceTuple;
import org.openspml.v2.msg.spml.QueryClause;
import org.openspml.v2.profiles.dsml.DSMLProfileException;
import org.openspml.v2.util.Spml2Exception;
import org.openspml.v2.util.xml.XmlBuffer;

public class LdapFilterQueryClause extends QueryClause implements OpenContentElement {

  public static final String ELEMENT_NAME = "LdapFilterQueryClause";

  public static final String FILTER_ELEMENT_NAME = "filter";

  private String[] m_filter = new String[1];

  public String getFilter() {
    return m_filter[0];
  }

  public void setFilter(String filter) {
    this.m_filter[0] = filter;
  }

  public PrefixAndNamespaceTuple[] getNamespacesInfo() {
    return LdappcMarshallableCreator.staticGetNamespacesInfo();
  }

  public String toXML() throws Spml2Exception {
    return toXML(0);
  }

  public String toXML(int indent) throws Spml2Exception {

    XmlBuffer buffer = new XmlBuffer();

    try {
      buffer.setNamespace(new URI(LdappcMarshallableCreator.URI));
    } catch (URISyntaxException e) {
      throw new DSMLProfileException(e.getMessage(), e);
    }

    buffer.setPrefix(LdappcMarshallableCreator.PREFIX);

    buffer.setIndent(indent);

    buffer.addStartTag(ELEMENT_NAME);

    if (this.getFilter() != null && this.getFilter().length() > 0) {
      buffer.incIndent();
      buffer.addStartTag(FILTER_ELEMENT_NAME, false);
      buffer.addContent(this.getFilter());
      buffer.addEndTag(FILTER_ELEMENT_NAME, false);
      buffer.decIndent();
    }

    buffer.addEndTag(ELEMENT_NAME);

    return buffer.toString();
  }

  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof LdapFilterQueryClause))
      return false;

    final LdapFilterQueryClause qc = (LdapFilterQueryClause) o;

    if (m_filter[0] != null ? !m_filter[0].equals(qc.m_filter[0]) : qc.m_filter[0] != null)
      return false;

    return true;
  }

  public int hashCode() {
    return (m_filter[0] != null ? m_filter[0].hashCode() : 0);
  }
}
