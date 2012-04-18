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

package edu.internet2.middleware.ldappc.spml.definitions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spmlref.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.PSPContext;

public class PSOReferencesDefinition {

  private static final Logger LOG = LoggerFactory.getLogger(PSOReferencesDefinition.class);

  private String name;

  private String emptyValue = null;

  private List<PSOReferenceDefinition> psoReferenceDefinitions;

  public static final String REFERENCE_URI_STRING = "urn:oasis:names:tc:SPML:2:0:reference";

  public static final URI REFERENCE_URI;

  // public static final String EMPTY_STRING = "_EMPTY_STRING_";

  static {
    try {
      REFERENCE_URI = new URI(REFERENCE_URI_STRING);
    } catch (URISyntaxException e) {
      LOG.error("Unable to parse URI", e);
      throw new LdappcException("Unable to parse URI", e);
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmptyValue() {
    return emptyValue;
  }

  public void setEmptyValue(String emptyValue) {
    this.emptyValue = emptyValue;
  }

  // TODO complete
  public PSOReferenceDefinition getReferenceDefinition(String ref) {
    for (PSOReferenceDefinition r : psoReferenceDefinitions) {
      if (r.getRef().equals(ref)) {
        return r;
      }
    }
    return null;
  }

  public List<PSOReferenceDefinition> getPsoReferenceDefinitions() {
    return psoReferenceDefinitions;
  }

  public void setPsoReferenceDefinitions(List<PSOReferenceDefinition> psoReferenceDefinitions) {
    this.psoReferenceDefinitions = psoReferenceDefinitions;
  }

  public List<Reference> getReferences(PSPContext context) throws LdappcException {

    String msg = "get references for '" + context.getProvisioningRequest().getId() + "' name '" + name + "'";
    LOG.debug("{}", msg);

    List<Reference> references = new ArrayList<Reference>();

    for (PSOReferenceDefinition psoReferenceDefinition : psoReferenceDefinitions) {
      references.addAll(psoReferenceDefinition.getReferences(context, name));
    }

    if (emptyValue != null && references.isEmpty()) {
      references.addAll(getEmptyReferences());
    }

    LOG.debug("{} returned {}", msg, references.size());
    return references;
  }

  public List<Reference> getEmptyReferences() {
    List<Reference> references = new ArrayList<Reference>();

    Set<String> targetIds = new LinkedHashSet<String>();
    for (PSOReferenceDefinition psoReferenceDefinition : psoReferenceDefinitions) {
      targetIds.add(psoReferenceDefinition.getToPSODefinition().getPsoIdentifierDefinition().getTargetDefinition()
          .getId());
    }

    for (String targetId : targetIds) {
      PSOIdentifier psoID = new PSOIdentifier();
      psoID.setTargetID(targetId);
      psoID.setID(emptyValue);

      Reference reference = new Reference();
      reference.setToPsoID(psoID);
      reference.setTypeOfReference(name);
      references.add(reference);
    }

    return references;
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("name", name);
    toStringBuilder.append("emptyValue", emptyValue);
    return toStringBuilder.toString();
  }
}
