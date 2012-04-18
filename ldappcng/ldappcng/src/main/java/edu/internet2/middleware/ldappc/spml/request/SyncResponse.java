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
import org.openspml.v2.msg.spml.AddResponse;
import org.openspml.v2.msg.spml.DeleteResponse;
import org.openspml.v2.msg.spml.ModifyResponse;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.Response;
import org.openspml.v2.util.Spml2Exception;

import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.util.PSPUtil;

public class SyncResponse extends ProvisioningResponse {

  public void addResponse(AddResponse addResponse) {
    try {
      this.addOpenContentElement(new OCEtoMarshallableAdapter(addResponse));
    } catch (Spml2Exception e) {
      // shouldn't happen
      throw new LdappcException("An SPML2 error occurred.", e);
    }
  }

  public void addResponse(ModifyResponse modifyResponse) {
    try {
      this.addOpenContentElement(new OCEtoMarshallableAdapter(modifyResponse));
    } catch (Spml2Exception e) {
      // shouldn't happen
      throw new LdappcException("An SPML2 error occurred.", e);
    }
  }

  public void addResponse(DeleteResponse deleteResponse) {
    try {
      this.addOpenContentElement(new OCEtoMarshallableAdapter(deleteResponse));
    } catch (Spml2Exception e) {
      // shouldn't happen
      throw new LdappcException("An SPML2 error occurred.", e);
    }
  }

  public void addResponse(Response response) throws LdappcException {
    if (response instanceof AddResponse) {
      this.addResponse((AddResponse) response);
    } else if (response instanceof DeleteResponse) {
      this.addResponse((DeleteResponse) response);
    } else if (response instanceof ModifyResponse) {
      this.addResponse((ModifyResponse) response);
    } else if (response instanceof SynchronizedResponse) {
      this.addResponse((SynchronizedResponse) response);
    } else {
      throw new LdappcException("Response " + response.getClass() + " is not supported.");
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

  public List<AddResponse> getAddResponses() {
    List<AddResponse> requests = new ArrayList<AddResponse>();
    for (Object oce : this.getOpenContentElements(OCEtoMarshallableAdapter.class)) {
      Object o = ((OCEtoMarshallableAdapter) oce).getAdaptedObject();
      if (o instanceof AddResponse) {
        requests.add((AddResponse) o);
      }
    }

    return requests;
  }

  public List<DeleteResponse> getDeleteResponses() {
    List<DeleteResponse> requests = new ArrayList<DeleteResponse>();
    for (Object oce : this.getOpenContentElements(OCEtoMarshallableAdapter.class)) {
      Object o = ((OCEtoMarshallableAdapter) oce).getAdaptedObject();
      if (o instanceof DeleteResponse) {
        requests.add((DeleteResponse) o);
      }
    }

    return requests;
  }

  public List<ModifyResponse> getModifyResponses() {
    List<ModifyResponse> requests = new ArrayList<ModifyResponse>();
    for (Object oce : this.getOpenContentElements(OCEtoMarshallableAdapter.class)) {
      Object o = ((OCEtoMarshallableAdapter) oce).getAdaptedObject();
      if (o instanceof ModifyResponse) {
        requests.add((ModifyResponse) o);
      }
    }

    return requests;
  }

  public List<Response> getAddDeleteModifyResponses() {
    List<Response> responses = new ArrayList<Response>();
    responses.addAll(this.getAddResponses());
    responses.addAll(this.getDeleteResponses());
    responses.addAll(this.getModifyResponses());
    return responses;
  }

  public Map<PSOIdentifier, Response> getResponseMap() {
    Map<PSOIdentifier, Response> map = new HashMap<PSOIdentifier, Response>();
    for (AddResponse response : this.getAddResponses()) {
      map.put(response.getPso().getPsoID(), response);
    }
    for (ModifyResponse response : this.getModifyResponses()) {
      map.put(response.getPso().getPsoID(), response);
    }
    // TODO deleteResponses with no psoID ?
    return map;
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

  public int hashCode() {
    int result = 1;
    result = 29 * result + (this.getId() != null ? this.getId().hashCode() : 0);
    result = 29 * result + (this.getError() != null ? this.getError().hashCode() : 0);
    result = 29 * result + (this.getErrorMessages() != null ? Arrays.asList(this.getErrorMessages()).hashCode() : 0);
    result = 29 * result + (this.getRequestID() != null ? this.getRequestID().hashCode() : 0);
    result = 29 * result + (this.getStatus() != null ? this.getStatus().hashCode() : 0);
    result = 29 * result
        + (this.getOpenContentAttrs() != null ? Arrays.asList(this.getOpenContentAttrs()).hashCode() : 0);
    for (Response response : this.getAddDeleteModifyResponses()) {
      result = 29 * result + response.hashCode();
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
    if (!(o instanceof SyncResponse)) {
      return false;
    }

    // TODO calling super.equals() fails for some reason, e.g. OCEtoMarshallableAdapters

    final SyncResponse that = (SyncResponse) o;

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

    if (this.getDeleteResponses().isEmpty()) {
      if (!that.getDeleteResponses().isEmpty()) {
        return false;
      }
    } else {
      if (!this.getDeleteResponses().equals(that.getDeleteResponses())) {
        return false;
      }
    }

    // TODO synchronized responses ?

    return true;
  }

  @Override
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.appendSuper(super.toString());
    for (AddResponse response : this.getAddResponses()) {
      toStringBuilder.append(PSPUtil.toString(response));
    }
    for (ModifyResponse response : this.getModifyResponses()) {
      toStringBuilder.append(PSPUtil.toString(response));
    }
    for (DeleteResponse response : this.getDeleteResponses()) {
      toStringBuilder.append(PSPUtil.toString(response));
    }
    for (SynchronizedResponse response : this.getSynchronizedResponses()) {
      toStringBuilder.append(response);
    }
    return toStringBuilder.toString();
  }
}
