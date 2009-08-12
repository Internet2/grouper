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

import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spmlref.Reference;
import org.slf4j.Logger;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.PSPContext;
import edu.internet2.middleware.ldappc.util.PSPUtil;

public class PSOReferencesDefinition {

  private static final Logger LOG = GrouperUtil.getLogger(PSOReferencesDefinition.class);

  private String name;

  private String emptyValue = null;

  private List<PSOReferenceDefinition> psoReferenceDefinitions;

  public static final String REFERENCE_URI_STRING = "urn:oasis:names:tc:SPML:2:0:reference";

  public static final URI REFERENCE_URI;

  public static final String EMPTY_STRING = "_EMPTY_STRING_";

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

    // new empty reference to each referenced target
    if (emptyValue != null && references.isEmpty()) {
      Set<String> targetIds = new LinkedHashSet<String>();
      for (PSOReferenceDefinition psoReferenceDefinition : psoReferenceDefinitions) {
        targetIds.add(psoReferenceDefinition.getToPSODefinition().getPsoIdentifierDefinition().getTargetDefinition()
            .getId());
      }
      for (String targetId : targetIds) {
        PSOIdentifier psoID = new PSOIdentifier();
        psoID.setTargetID(targetId);
        // fake empty string since the spml toolkit ignores an empty string psoID
        if (emptyValue.equals("")) {
          psoID.setID(EMPTY_STRING);
        } else {
          psoID.setID(emptyValue);
        }

        Reference reference = new Reference();
        reference.setToPsoID(psoID);
        reference.setTypeOfReference(name);

        references.add(reference);
        LOG.debug("{} reference with empty value {}", PSPUtil.getString(reference));
      }
    }

    LOG.debug("{} returned {} references", references.size());
    return references;
  }
}
