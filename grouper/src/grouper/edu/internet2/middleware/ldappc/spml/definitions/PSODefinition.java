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

import org.openspml.v2.msg.spml.Extensible;
import org.openspml.v2.msg.spml.PSO;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spmlref.Reference;
import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.profiles.dsml.DSMLValue;
import org.openspml.v2.util.Spml2Exception;
import org.slf4j.Logger;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.PSPContext;
import edu.internet2.middleware.ldappc.util.PSPUtil;

public class PSODefinition {

  private static final Logger LOG = GrouperUtil.getLogger(PSODefinition.class);

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

  public PSO getPSO(PSPContext context) throws LdappcException, Spml2Exception {

    String msg = "get pso '" + context.getProvisioningRequest().getId() + "' object '" + id + "' return "
        + context.getProvisioningRequest().getReturnData() + "'";
    LOG.debug("{}", msg);

    // must have an identifier
    PSOIdentifier psoIdentifier = this.getPsoIdentifierDefinition().getPSOIdentifier(context);
    if (psoIdentifier == null) {
      LOG.debug("{} identifier is null", msg);
      return null;
    }

    // pso
    PSO pso = new PSO();
    pso.setPsoID(psoIdentifier);

    pso.addOpenContentAttr(ENTITY_NAME_ATTRIBUTE, id);

    // data
    ReturnData returnData = context.getProvisioningRequest().getReturnData();
    if (returnData.equals(ReturnData.DATA) || returnData.equals(ReturnData.EVERYTHING)) {
      Extensible data = new Extensible();
      for (PSOAttributeDefinition psoAttributeDefinition : this.getAttributeDefinitions()) {
        DSMLAttr dsmlAttr = psoAttributeDefinition.getAttribute(context.getAttributes());
        if (dsmlAttr != null) {
          data.addOpenContentElement(dsmlAttr);
          if (LOG.isDebugEnabled()) {
            for (DSMLValue dsmlValue : dsmlAttr.getValues()) {
              LOG.debug("{} attr {}", new String[] { msg, dsmlAttr.getName(), dsmlValue.getValue() });
            }
          }
        }
      }
      pso.setData(data);
    }

    // references
    if (returnData.equals(ReturnData.EVERYTHING)) {
      List<Reference> references = new ArrayList<Reference>();
      for (PSOReferencesDefinition psoReferenceDefinition : this.getReferenceDefinitions()) {
        references.addAll(psoReferenceDefinition.getReferences(context));
      }
      PSPUtil.setReferences(pso, references);
      // if (!references.isEmpty()) {
      // CapabilityData referenceCapabilityData = new CapabilityData(true,
      // PSOReferencesDefinition.REFERENCE_URI);
      // for (Reference reference : references) {
      // OCEtoMarshallableAdapter oce = new OCEtoMarshallableAdapter(reference);
      // referenceCapabilityData.addOpenContentElement(oce);
      // if (LOG.isDebugEnabled()) {
      // LOG.debug("{} reference {}", LdappcUtil.getString(reference));
      // }
      // }
      // pso.addCapabilityData(referenceCapabilityData);
      // }
    }

    return pso;
  }
}
