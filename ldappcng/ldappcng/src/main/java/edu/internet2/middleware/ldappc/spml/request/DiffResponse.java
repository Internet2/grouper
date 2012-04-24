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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openspml.v2.msg.OCEtoMarshallableAdapter;
import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.DeleteRequest;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.Request;
import org.openspml.v2.msg.spml.Response;
import org.openspml.v2.util.Spml2Exception;

import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.util.PSPUtil;

public class DiffResponse extends ProvisioningResponse {

  // cannot have more than one ArrayListWithType, or one ArrayListWithType and OCEs

  public void addRequest(AddRequest addRequest) {
    try {
      this.addOpenContentElement(new OCEtoMarshallableAdapter(addRequest));
    } catch (Spml2Exception e) {
      // shouldn't happen
      throw new LdappcException("An SPML2 error occurred.", e);
    }
  }

  public void addRequest(DeleteRequest deleteRequest) {
    try {
      this.addOpenContentElement(new OCEtoMarshallableAdapter(deleteRequest));
    } catch (Spml2Exception e) {
      // shouldn't happen
      throw new LdappcException("An SPML2 error occurred.", e);
    }
  }

  public void addRequest(ModifyRequest modifyRequest) {
    try {
      this.addOpenContentElement(new OCEtoMarshallableAdapter(modifyRequest));
    } catch (Spml2Exception e) {
      // shouldn't happen
      throw new LdappcException("An SPML2 error occurred.", e);
    }
  }

  public void addResponse(SynchronizedResponse synchronizedResponse) {

    try {
      this.addOpenContentElement(new OCEtoMarshallableAdapter(synchronizedResponse));
    } catch (Spml2Exception e) {
      // shouldn't happen
      throw new LdappcException("An SPML2 error occurred.", e);
    }
  }

  public List<AddRequest> getAddRequests() {
    List<AddRequest> requests = new ArrayList<AddRequest>();
    for (Object oce : this.getOpenContentElements(OCEtoMarshallableAdapter.class)) {
      Object o = ((OCEtoMarshallableAdapter) oce).getAdaptedObject();
      if (o instanceof AddRequest) {
        requests.add((AddRequest) o);
      }
    }

    return requests;
  }

  public List<DeleteRequest> getDeleteRequests() {
    List<DeleteRequest> requests = new ArrayList<DeleteRequest>();
    for (Object oce : this.getOpenContentElements(OCEtoMarshallableAdapter.class)) {
      Object o = ((OCEtoMarshallableAdapter) oce).getAdaptedObject();
      if (o instanceof DeleteRequest) {
        requests.add((DeleteRequest) o);
      }
    }

    return requests;
  }

  public List<ModifyRequest> getModifyRequests() {
    List<ModifyRequest> requests = new ArrayList<ModifyRequest>();
    for (Object oce : this.getOpenContentElements(OCEtoMarshallableAdapter.class)) {
      Object o = ((OCEtoMarshallableAdapter) oce).getAdaptedObject();
      if (o instanceof ModifyRequest) {
        requests.add((ModifyRequest) o);
      }
    }

    return requests;
  }

  public List<Request> getRequests() {
    List<Request> requests = new ArrayList<Request>();
    requests.addAll(this.getAddRequests());
    requests.addAll(this.getDeleteRequests());
    requests.addAll(this.getModifyRequests());
    return requests;
  }

  public List<SynchronizedResponse> getSynchronizedResponses() {
    List<SynchronizedResponse> responses = new ArrayList<SynchronizedResponse>();
    for (Object oce : this.getOpenContentElements(OCEtoMarshallableAdapter.class)) {
      Object o = ((OCEtoMarshallableAdapter) oce).getAdaptedObject();
      if (o instanceof SynchronizedResponse) {
        responses.add((SynchronizedResponse) o);
      }
    }
    return responses;
  }

  public List<PSOIdentifier> getPsoIds() {
    List<PSOIdentifier> psoIds = new ArrayList<PSOIdentifier>();
    for (AddRequest addRequest : this.getAddRequests()) {
      psoIds.add(addRequest.getPsoID());
    }
    for (ModifyRequest modifyRequest : this.getModifyRequests()) {
      psoIds.add(modifyRequest.getPsoID());
    }
    // TODO what about delete ?
    for (SynchronizedResponse synchronizedResponse : this.getSynchronizedResponses()) {
      psoIds.add(synchronizedResponse.getPsoID());
    }
    return psoIds;
  }

  public Map<PSOIdentifier, Request> getRequestMap() {
    // TODO more than one value for the same psoID ?
    Map<PSOIdentifier, Request> map = new HashMap<PSOIdentifier, Request>();
    for (AddRequest request : this.getAddRequests()) {
      map.put(request.getPsoID(), request);
    }
    for (DeleteRequest request : this.getDeleteRequests()) {
      map.put(request.getPsoID(), request);
    }
    for (ModifyRequest request : this.getModifyRequests()) {
      map.put(request.getPsoID(), request);
    }
    return map;
  }

  public Map<PSOIdentifier, Response> getResponseMap() {
    Map<PSOIdentifier, Response> map = new HashMap<PSOIdentifier, Response>();
    for (SynchronizedResponse response : this.getSynchronizedResponses()) {
      map.put(response.getPsoID(), response);
    }
    return map;
  }

  public int hashCode() {
    int result = 1;
    result = 29 * result + (this.getId() != null ? this.getId().hashCode() : 0);
    result = 29 * result + (this.getError() != null ? this.getError().hashCode() : 0);
    result = 29 * result + (this.getErrorMessages() != null ? Arrays.asList(this.getErrorMessages()).hashCode() : 0);
    result = 29 * result + (this.getRequestID() != null ? this.getRequestID().hashCode() : 0);
    result = 29 * result + (this.getStatus() != null ? this.getStatus().hashCode() : 0);
    result = 29 * result
        + (this.getOpenContentAttrs() != null ? Arrays.asList(this.getOpenContentAttrs()).hashCode() : 0);
    for (Request request : this.getRequests()) {
      result = 29 * result + request.hashCode();
    }
    for (Response response : this.getSynchronizedResponses()) {
      result = 29 * result + response.hashCode();
    }
    return result;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DiffResponse)) {
      return false;
    }

    // TODO calling super.equals() fails for some reason, e.g. OCEtoMarshallableAdapters

    final DiffResponse that = (DiffResponse) o;

    // ProvisioningResponse
    if (this.getId() != null ? !this.getId().equals(that.getId()) : that.getId() != null) {
      return false;
    }

    // Response
    if (this.getError() != null ? !this.getError().equals(that.getError()) : that.getError() != null) {
      return false;
    }
    if (this.getErrorMessages() != null ? !Arrays.asList(this.getErrorMessages()).equals(
        Arrays.asList(that.getErrorMessages())) : that.getErrorMessages() != null) {
      return false;
    }
    if (this.getRequestID() != null ? !this.getRequestID().equals(that.getRequestID()) : that.getRequestID() != null) {
      return false;
    }
    if (this.getStatus() != null ? !this.getStatus().equals(that.getStatus()) : that.getStatus() != null) {
      return false;
    }

    if (this.getOpenContentAttrs() != null ? !Arrays.asList(this.getOpenContentAttrs()).equals(
        Arrays.asList(that.getOpenContentAttrs())) : that.getOpenContentAttrs() != null) {
      return false;
    }

    // custom OCE equality

    Map<PSOIdentifier, Request> thisRequestMap = this.getRequestMap();
    Map<PSOIdentifier, Request> thatRequestMap = that.getRequestMap();
    for (PSOIdentifier psoID : thisRequestMap.keySet()) {
      Request other = thatRequestMap.get(psoID);
      if (other == null) {
        return false;
      }
      if (!thisRequestMap.get(psoID).equals(other)) {
        return false;
      }
    }
    for (PSOIdentifier psoID : thatRequestMap.keySet()) {
      Request other = thisRequestMap.get(psoID);
      if (other == null) {
        return false;
      }
      if (!thatRequestMap.get(psoID).equals(other)) {
        return false;
      }
    }

    Map<PSOIdentifier, Response> thisResponseMap = this.getResponseMap();
    Map<PSOIdentifier, Response> thatResponseMap = that.getResponseMap();
    for (PSOIdentifier psoID : thisResponseMap.keySet()) {
      Response other = thatResponseMap.get(psoID);
      if (other == null) {
        return false;
      }
      if (!thisResponseMap.get(psoID).equals(other)) {
        return false;
      }
    }
    for (PSOIdentifier psoID : thatResponseMap.keySet()) {
      Response other = thisResponseMap.get(psoID);
      if (other == null) {
        return false;
      }
      if (!thatResponseMap.get(psoID).equals(other)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.appendSuper(super.toString());
    for (AddRequest request : this.getAddRequests()) {
      toStringBuilder.append("add", PSPUtil.toString(request));
    }
    for (ModifyRequest request : this.getModifyRequests()) {
      toStringBuilder.append("modify", PSPUtil.toString(request));
    }
    for (DeleteRequest request : this.getDeleteRequests()) {
      toStringBuilder.append("delete", PSPUtil.toString(request));
    }
    for (SynchronizedResponse response : this.getSynchronizedResponses()) {
      toStringBuilder.append("synchronized", response.toString());
    }
    return toStringBuilder.toString();
  }

  // TODO equality checking for OCEtoMarshallableAdapter
}
