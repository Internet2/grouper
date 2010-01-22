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

import org.openspml.v2.msg.spml.PSO;
import org.openspml.v2.msg.spml.Response;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.StatusCode;
import org.openspml.v2.msg.spmlref.Reference;
import org.slf4j.Logger;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.PSPContext;
import edu.internet2.middleware.ldappc.spml.request.CalcRequest;
import edu.internet2.middleware.ldappc.spml.request.CalcResponse;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;

public class PSOReferenceDefinition {

  private static final Logger LOG = GrouperUtil.getLogger(PSOReferenceDefinition.class);

  private String ref;

  private PSODefinition toPSODefinition;

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

  public PSODefinition getToPSODefinition() {
    return toPSODefinition;
  }

  public void setToPSODefinition(PSODefinition toPSODefinition) {
    this.toPSODefinition = toPSODefinition;
  }

  public List<Reference> getReferences(PSPContext context, String typeOfReference) throws LdappcException {

    String msg = "get references for '" + context.getProvisioningRequest().getId() + "' ref '" + ref + "'";
    LOG.debug("{}", msg);

    ArrayList<Reference> references = new ArrayList<Reference>();

    Map<String, BaseAttribute> attributes = context.getAttributes();

    if (!attributes.containsKey(ref)) {
      LOG.debug("{} source attribute does not exist", msg);
      return references;
    }

    // resolve identifiers
    BaseAttribute<String> referenceAttribute = attributes.get(ref);
    for (String id : referenceAttribute.getValues()) {

      CalcRequest calcRequest = new CalcRequest();
      calcRequest.setReturnData(ReturnData.IDENTIFIER);
      calcRequest.setId(id);
      calcRequest.addTargetId(this.getToPSODefinition().getPsoIdentifierDefinition().getTargetDefinition().getId());

      Response response = context.getProvisioningServiceProvider().execute(calcRequest);

      if (response.getStatus().equals(StatusCode.SUCCESS)) {

        CalcResponse calcResponse = (CalcResponse) response;

        List<PSO> psos = calcResponse.getPSOs();

        if (psos.isEmpty()) {
          // TODO is logging sufficient ?
          LOG.warn("{} unable to resolve identifier '{}'", msg, id);
        }

        if (psos.size() > 1) {
          LOG.error("{} more than one PSO returned for id '{}'", msg, id);
          throw new LdappcException("More than one PSO returned.");
        }

        if (psos.size() == 1) {
          PSO pso = psos.get(0);
          Reference reference = new Reference();
          reference.setToPsoID(pso.getPsoID());
          reference.setTypeOfReference(typeOfReference);
          references.add(reference);
        }
      }
    }

    if (LOG.isDebugEnabled()) {
      for (Reference reference : references) {
        LOG.debug("{} reference : '{}'", msg, PSPUtil.getString(reference));
      }
    }

    return references;
  }
}
