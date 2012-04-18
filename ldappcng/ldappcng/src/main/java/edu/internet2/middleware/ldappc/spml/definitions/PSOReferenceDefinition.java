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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openspml.v2.msg.spml.ErrorCode;
import org.openspml.v2.msg.spml.PSO;
import org.openspml.v2.msg.spml.Request;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.SchemaEntityRef;
import org.openspml.v2.msg.spml.StatusCode;
import org.openspml.v2.msg.spmlref.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.shibboleth.util.OnNotFound;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.PSPContext;
import edu.internet2.middleware.ldappc.spml.request.CalcRequest;
import edu.internet2.middleware.ldappc.spml.request.CalcResponse;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;

public class PSOReferenceDefinition {

  private static final Logger LOG = LoggerFactory.getLogger(PSOReferenceDefinition.class);

  private String ref;

  private PSODefinition toPSODefinition;

  private OnNotFound onNotFound;

  // TODO correct ?
  public static String REFERENCE_NOT_FOUND = "REFERENCE_NOT_FOUND";

  /**
   * Allow multiple provisioned objects on a target for a single subject.
   */
  private boolean multipleResults;

  public static String ERROR_MULTIPLE_RESULTS = "ERROR_MULTIPLE_RESULTS";

  public boolean isMultipleResults() {
    return multipleResults;
  }

  public void setMultipleResults(boolean multipleResults) {
    this.multipleResults = multipleResults;
  }

  public OnNotFound getOnNotFound() {
    return onNotFound;
  }

  public void setOnNotFound(OnNotFound onNotFound) {
    this.onNotFound = onNotFound;
  }

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

    Map<String, BaseAttribute<?>> attributes = context.getAttributes();

    if (!attributes.containsKey(ref)) {
      LOG.debug("{} source attribute does not exist", msg);
      return references;
    }

    // resolve identifiers
    BaseAttribute<?> referenceAttribute = attributes.get(ref);
    for (Object id : referenceAttribute.getValues()) {
      CalcRequest calcRequest = new CalcRequest();
      calcRequest.setReturnData(ReturnData.IDENTIFIER);
      calcRequest.setId(id.toString());
      SchemaEntityRef schemaEntityRef = new SchemaEntityRef();
      schemaEntityRef.setTargetID(this.getToPSODefinition().getPsoIdentifierDefinition().getTargetDefinition().getId());
      schemaEntityRef.setEntityName(this.getToPSODefinition().getId());
      calcRequest.addSchemaEntity(schemaEntityRef);
      
      CalcResponse calcResponse = (CalcResponse) context.getProvisioningServiceProvider()
          .execute((Request) calcRequest);

      List<PSO> psos = calcResponse.getPSOs();

      if (calcResponse.getStatus().equals(StatusCode.FAILURE)
          || (calcResponse.getStatus().equals(StatusCode.SUCCESS) && psos.isEmpty())) {
        if (onNotFound.equals(OnNotFound.warn)) {
          LOG.warn("{} unable to resolve identifier '{}'", msg, id);
        } else if (onNotFound.equals(OnNotFound.fail)) {
          LOG.error("{} unable to resolve identifier '{}'", msg, id);
          throw new LdappcException(REFERENCE_NOT_FOUND);
        }
      }

      if (calcResponse.getStatus().equals(StatusCode.SUCCESS)) {

        // TODO correct handling of multiple results ?
        if (!multipleResults && psos.size() > 1) {
          LOG.error("Unable to resolve {} : {} results found", msg, psos.size());
          throw new LdappcException(ERROR_MULTIPLE_RESULTS);
        }

        for (PSO pso : psos) {
          Reference reference = new Reference();
          reference.setToPsoID(pso.getPsoID());
          reference.setTypeOfReference(typeOfReference);
          references.add(reference);
        }
      }

      // TODO correct handling of status=pending ?
      if (calcResponse.getStatus().equals(StatusCode.PENDING)) {
        LOG.error("Unable to resolve {} identifier {} " + ErrorCode.UNSUPPORTED_EXECUTION_MODE, msg, id);
        throw new LdappcException(ErrorCode.UNSUPPORTED_EXECUTION_MODE.toString());
      }
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("{} found {} references", msg, references.size());
      for (Reference reference : references) {
        LOG.debug("{} reference : '{}'", msg, PSPUtil.getString(reference));
      }
    }

    return references;
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("ref", ref);
    toStringBuilder.append("toPSODefinition", toPSODefinition.getId());
    toStringBuilder.append("onNotFound", onNotFound);
    toStringBuilder.append("multipleResults", multipleResults);
    return toStringBuilder.toString();
  }
}
