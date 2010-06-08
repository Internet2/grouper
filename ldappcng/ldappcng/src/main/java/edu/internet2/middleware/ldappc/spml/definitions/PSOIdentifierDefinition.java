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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.PSPContext;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;

public class PSOIdentifierDefinition {

  private static final Logger LOG = LoggerFactory.getLogger(PSOIdentifierDefinition.class);

  private String baseId;

  private String ref;

  private TargetDefinition targetDefinition;

  private IdentifyingAttribute identifyingAttribute;

  public String getBaseId() {
    return baseId;
  }

  public void setBaseId(String baseId) {
    this.baseId = baseId;
  }

  public IdentifyingAttribute getIdentifyingAttribute() {
    return identifyingAttribute;
  }

  public void setIdentifyingAttribute(IdentifyingAttribute identifyingAttribute) {
    this.identifyingAttribute = identifyingAttribute;
  }

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

  public TargetDefinition getTargetDefinition() {
    return targetDefinition;
  }

  public void setTargetDefinition(TargetDefinition targetDefinition) {
    this.targetDefinition = targetDefinition;
  }

  public List<PSOIdentifier> getPSOIdentifier(PSPContext context) throws LdappcException {

    List<PSOIdentifier> psoIDs = new ArrayList<PSOIdentifier>();

    String msg = "get psoId for '" + context.getProvisioningRequest().getId() + "' ref '" + ref + "'";
    LOG.debug("{}", msg);

    Map<String, BaseAttribute<?>> attributes = context.getAttributes();

    if (!attributes.containsKey(ref)) {
      LOG.debug("{} source attribute does not exist", msg);
      return psoIDs;
    }

    BaseAttribute<?> attribute = attributes.get(ref);

    if (attribute.getValues().isEmpty()) {
      LOG.debug("{} no dependency values", msg);
      return psoIDs;
    }

    for (Object value : attribute.getValues()) {
      if (!(value instanceof PSOIdentifier)) {
        LOG.error("{} Unable to calculate identifier, returned object is not a " + PSOIdentifier.class + " : {}", msg,
            value.getClass());
        throw new LdappcException("Unable to calculate identifier, returned object is not a " + PSOIdentifier.class);
      }

      PSOIdentifier psoIdentifier = (PSOIdentifier) value;
      psoIdentifier.setTargetID(targetDefinition.getId());
      psoIDs.add(psoIdentifier);
      LOG.debug("{} returned '{}'", msg, PSPUtil.getString(psoIdentifier));
    }

    return psoIDs;
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("ref", ref);
    toStringBuilder.append("baseId", baseId);
    toStringBuilder.append("targetDefinitionID", targetDefinition.getId());
    toStringBuilder.append("identifyingAttribute", identifyingAttribute);
    return toStringBuilder.toString();
  }
}
