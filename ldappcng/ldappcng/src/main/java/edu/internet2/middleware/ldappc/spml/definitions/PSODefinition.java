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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openspml.v2.msg.spml.Extensible;
import org.openspml.v2.msg.spml.PSO;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spmlref.Reference;
import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.util.Spml2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.PSPContext;
import edu.internet2.middleware.ldappc.spml.request.AlternateIdentifier;
import edu.internet2.middleware.ldappc.util.PSPUtil;

public class PSODefinition {

  private static final Logger LOG = LoggerFactory.getLogger(PSODefinition.class);

  private String id;

  private List<PSOAttributeDefinition> psoAttributeDefinitions;

  private PSOIdentifierDefinition psoIdentifierDefinition;

  private List<PSOReferencesDefinition> psoReferencesDefinitions;

  private boolean authoritative;

  public static final String ENTITY_NAME_ATTRIBUTE = "entityName";

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isAuthoritative() {
    return authoritative;
  }

  public void setAuthoritative(boolean authoritative) {
    this.authoritative = authoritative;
  }

  /**
   * 
   * @param name
   * @return the PSO attribute definition with the given name or null
   */
  public PSOAttributeDefinition getAttributeDefinition(String name) {
    for (PSOAttributeDefinition psoAttributeDefinition : psoAttributeDefinitions) {
      if (psoAttributeDefinition.getName().equals(name)) {
        return psoAttributeDefinition;
      }
    }
    return null;
  }

  public List<PSOAttributeDefinition> getAttributeDefinitions() {
    return psoAttributeDefinitions;
  }

  public void setAttributeDefinitions(List<PSOAttributeDefinition> attributeDefinitions) {
    this.psoAttributeDefinitions = attributeDefinitions;
  }

  public PSOIdentifierDefinition getPsoIdentifierDefinition() {
    return psoIdentifierDefinition;
  }

  public void setPsoIdentifierDefinition(PSOIdentifierDefinition identifierDefinition) {
    this.psoIdentifierDefinition = identifierDefinition;

  }

  // TODO complete
  public PSOReferencesDefinition getReferencesDefinition(String name) {
    for (PSOReferencesDefinition r : psoReferencesDefinitions) {
      if (r.getName().endsWith(name)) {
        return r;
      }
    }
    return null;
  }

  public List<PSOReferencesDefinition> getReferenceDefinitions() {
    return psoReferencesDefinitions;
  }

  public void setReferenceDefinitions(List<PSOReferencesDefinition> referenceDefinitions) {
    this.psoReferencesDefinitions = referenceDefinitions;
  }

  public Set<String> getAttributeNames() {

    Set<String> names = new LinkedHashSet<String>();

    for (PSOAttributeDefinition psoAttributeDefinition : psoAttributeDefinitions) {
      names.add(psoAttributeDefinition.getName());
    }

    return names;
  }

  public Set<String> getAttributeSourceIds() {

    Set<String> ids = new LinkedHashSet<String>();

    for (PSOAttributeDefinition psoAttributeDefinition : psoAttributeDefinitions) {
      ids.add(psoAttributeDefinition.getRef());
    }

    return ids;
  }

  public Set<String> getReferenceNames() {

    Set<String> names = new LinkedHashSet<String>();

    for (PSOReferencesDefinition psoReferencesDefinition : psoReferencesDefinitions) {
      names.add(psoReferencesDefinition.getName());
    }

    return names;
  }

  public Set<String> getReferenceSourceIds() {

    Set<String> ids = new LinkedHashSet<String>();

    for (PSOReferencesDefinition psoReferencesDefinition : psoReferencesDefinitions) {
      for (PSOReferenceDefinition psoReferenceDefinition : psoReferencesDefinition.getPsoReferenceDefinitions()) {
        ids.add(psoReferenceDefinition.getRef());
      }
    }

    return ids;
  }

  public Set<String> getSourceIds(ReturnData returnData) {
    Set<String> set = new LinkedHashSet<String>();
    set.add(this.getPsoIdentifierDefinition().getRef());
    // TODO should alternate identifier be IDENTIFIER or DATA ... not sure
    if (getPsoIdentifierDefinition().getAlternateIdentifierDefinitions() != null) {
      for (AlternateIdentifierDefinition altIdDef : getPsoIdentifierDefinition().getAlternateIdentifierDefinitions()) {
        if (altIdDef.getRef() != null) {
          set.add(altIdDef.getRef());
        }
      }
    }
    if (returnData.equals(ReturnData.DATA) || returnData.equals(ReturnData.EVERYTHING)) {
      set.addAll(this.getAttributeSourceIds());
    }
    if (returnData.equals(ReturnData.EVERYTHING)) {
      set.addAll(this.getReferenceSourceIds());
    }
    return set;
  }

  public List<PSO> getPSO(PSPContext context) throws LdappcException, Spml2Exception {

    String msg = "get pso '" + context.getProvisioningRequest().getId() + "' object '" + id + "' return '"
        + context.getProvisioningRequest().getReturnData() + "'";
    LOG.debug("{}", msg);

    ArrayList<PSO> psos = new ArrayList<PSO>();

    // must have an identifier
    List<PSOIdentifier> psoIdentifiers = this.getPsoIdentifierDefinition().getPSOIdentifier(context);
    if (psoIdentifiers.isEmpty()) {
      // TODO logging
      LOG.debug("{} identifier is empty", msg);
      return psos;
    }

    // TODO

    // data
    Extensible data = null;
    ReturnData returnData = context.getProvisioningRequest().getReturnData();
    if (returnData.equals(ReturnData.DATA) || returnData.equals(ReturnData.EVERYTHING)) {
      for (PSOAttributeDefinition psoAttributeDefinition : this.getAttributeDefinitions()) {
        DSMLAttr dsmlAttr = psoAttributeDefinition.getAttribute(context.getAttributes());
        if (dsmlAttr != null) {
          if (data == null) {
            data = new Extensible();
          }
          data.addOpenContentElement(dsmlAttr);
        }
      }
    }

    // references
    List<Reference> references = null;
    if (returnData.equals(ReturnData.EVERYTHING)) {
      references = new ArrayList<Reference>();
      for (PSOReferencesDefinition psoReferenceDefinition : this.getReferenceDefinitions()) {
        references.addAll(psoReferenceDefinition.getReferences(context));
      }
    }

    // alternate identifier
    List<AlternateIdentifier> alternateIdentifiers = this.getPsoIdentifierDefinition().getAlternateIdentifier(context);

    for (PSOIdentifier psoIdentifier : psoIdentifiers) {
      // pso
      PSO pso = new PSO();
      pso.setPsoID(psoIdentifier);

      pso.addOpenContentAttr(ENTITY_NAME_ATTRIBUTE, id);

      if (data != null) {
        pso.setData(data);
      }

      if (references != null && !references.isEmpty()) {
        PSPUtil.setReferences(pso, references);
      }

      for (AlternateIdentifier alternateIdentifier : alternateIdentifiers) {
        pso.addOpenContentElement(alternateIdentifier);
      }

      psos.add(pso);
    }

    LOG.debug("{} returned {}", msg, psos.size());
    return psos;
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("id", id);
    toStringBuilder.append("authoritative", authoritative);
    return toStringBuilder.toString();
  }
}
