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
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.ldappc.spml.request;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openspml.v2.msg.Marshallable;
import org.openspml.v2.msg.OpenContentElement;
import org.openspml.v2.msg.PrefixAndNamespaceTuple;
import org.openspml.v2.msg.XMLMarshaller;
import org.openspml.v2.msg.spml.Identifier;
import org.openspml.v2.msg.spml.PSO;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.profiles.dsml.DSMLProfileException;
import org.openspml.v2.util.Spml2Exception;
import org.openspml.v2.util.xml.XmlBuffer;

/**
 * An alternate identifier similar to a {@link PSOIdentifier}. An alternate identifier might represent the previous (or
 * old) identifier of a {@link PSO} after the {@link PSOIdentifier} has been changed.
 * 
 * <ldappc:altID xmlns:ldappc='http://grouper.internet2.edu/ldappc' ID='altID' targetID='targetID'/>
 */
public class AlternateIdentifier extends Identifier implements OpenContentElement, Marshallable {

  /** The target ID. */
  private String m_targetID = null;

  // TODO ?
  // private PSOIdentifier m_containerID = null;

  /**
   * Get the target ID.
   * 
   * @return the target ID.
   */
  public String getTargetID() {
    return m_targetID;
  }

  /**
   * Set the target ID.
   * 
   * @param targetID the target ID
   */
  public void setTargetID(String targetID) {
    m_targetID = targetID;
  }

  /** {@inheritDoc} */
  public PrefixAndNamespaceTuple[] getNamespacesInfo() {
    return LdappcMarshallableCreator.staticGetNamespacesInfo();
  }

  /**
   * Create a {@link PSOIdentifier} with this object's ID and targetID.
   * 
   * @return a {@link PSOIdentifier} representing this object
   */
  public PSOIdentifier getPSOIdentifier() {
    return new PSOIdentifier(getID(), null, getTargetID());
  }

  // TODO
  public void setPSOIdentifier(PSOIdentifier psoID) {
    setID(psoID.getID());
    setTargetID(psoID.getTargetID());
  }
  
  /** {@inheritDoc} */
  public String toXML() throws Spml2Exception {
    return toXML(0);
  }

  /** {@inheritDoc} */
  public String toXML(int indent) throws Spml2Exception {
    XmlBuffer buffer = new XmlBuffer();

    try {
      buffer.setNamespace(new URI(LdappcMarshallableCreator.URI));
    } catch (URISyntaxException e) {
      throw new DSMLProfileException(e.getMessage(), e);
    }

    buffer.setPrefix(LdappcMarshallableCreator.PREFIX);

    buffer.setIndent(indent);

    buffer.addOpenStartTag("alternateIdentifier");

    buffer.addAttribute("ID", getID());

    if (m_targetID != null) {
      buffer.addAttribute("targetID", m_targetID);
    }

    buffer.closeEmptyElement();

    return buffer.toString();
  }

  /** {@inheritDoc} */
  public String toXML(XMLMarshaller m) throws Spml2Exception {
    return m.marshall(this);
  }

  /** {@inheritDoc} */
  public int hashCode() {
    int result = super.hashCode();
    result = 29 * result + (m_targetID != null ? m_targetID.hashCode() : 0);
    return result;
  }

  /** {@inheritDoc} */
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AlternateIdentifier)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    final AlternateIdentifier that = (AlternateIdentifier) o;

    if (m_targetID != null ? !m_targetID.equals(that.m_targetID) : that.m_targetID != null) {
      return false;
    }

    return true;
  }

  /** {@inheritDoc} */
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("ID", getID());
    toStringBuilder.append("targetID", getTargetID());
    return toStringBuilder.toString();
  }

}
