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

package edu.internet2.middleware.ldappc.spml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openspml.v2.msg.OCEtoMarshallableAdapter;
import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.AddResponse;
import org.openspml.v2.msg.spml.CapabilityData;
import org.openspml.v2.msg.spml.DeleteRequest;
import org.openspml.v2.msg.spml.DeleteResponse;
import org.openspml.v2.msg.spml.ErrorCode;
import org.openspml.v2.msg.spml.Extensible;
import org.openspml.v2.msg.spml.ListTargetsRequest;
import org.openspml.v2.msg.spml.ListTargetsResponse;
import org.openspml.v2.msg.spml.LookupRequest;
import org.openspml.v2.msg.spml.LookupResponse;
import org.openspml.v2.msg.spml.Modification;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spml.PSO;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.Request;
import org.openspml.v2.msg.spml.Response;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.StatusCode;
import org.openspml.v2.msg.spmlref.Reference;
import org.openspml.v2.msg.spmlsearch.Query;
import org.openspml.v2.msg.spmlsearch.SearchRequest;
import org.openspml.v2.msg.spmlsearch.SearchResponse;
import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.profiles.dsml.DSMLProfileException;
import org.openspml.v2.profiles.dsml.DSMLUnmarshaller;
import org.openspml.v2.util.Spml2Exception;
import org.openspml.v2.util.xml.ObjectFactory;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.GroupDataConnector;
import edu.internet2.middleware.grouper.shibboleth.filter.GroupQueryFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.definitions.IdentifyingAttribute;
import edu.internet2.middleware.ldappc.spml.definitions.PSODefinition;
import edu.internet2.middleware.ldappc.spml.definitions.PSOReferencesDefinition;
import edu.internet2.middleware.ldappc.spml.definitions.TargetDefinition;
import edu.internet2.middleware.ldappc.spml.provider.BaseSpmlProvider;
import edu.internet2.middleware.ldappc.spml.request.BulkCalcRequest;
import edu.internet2.middleware.ldappc.spml.request.BulkCalcResponse;
import edu.internet2.middleware.ldappc.spml.request.BulkDiffRequest;
import edu.internet2.middleware.ldappc.spml.request.BulkDiffResponse;
import edu.internet2.middleware.ldappc.spml.request.BulkSyncRequest;
import edu.internet2.middleware.ldappc.spml.request.BulkSyncResponse;
import edu.internet2.middleware.ldappc.spml.request.CalcRequest;
import edu.internet2.middleware.ldappc.spml.request.CalcResponse;
import edu.internet2.middleware.ldappc.spml.request.DiffRequest;
import edu.internet2.middleware.ldappc.spml.request.DiffResponse;
import edu.internet2.middleware.ldappc.spml.request.LdapFilterQueryClause;
import edu.internet2.middleware.ldappc.spml.request.LdappcMarshallableCreator;
import edu.internet2.middleware.ldappc.spml.request.ProvisioningRequest;
import edu.internet2.middleware.ldappc.spml.request.SyncRequest;
import edu.internet2.middleware.ldappc.spml.request.SyncResponse;
import edu.internet2.middleware.ldappc.spml.request.SynchronizedResponse;
import edu.internet2.middleware.ldappc.synchronize.AttributeModifier;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.AttributeAuthority;
import edu.internet2.middleware.shibboleth.common.attribute.AttributeRequestException;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethAttributeResolver;
import edu.internet2.middleware.shibboleth.common.profile.provider.BaseSAMLProfileRequestContext;
import edu.internet2.middleware.shibboleth.common.service.ServiceException;

/**
 * An SPML 2 Provisioning Service Provider
 * 
 */
public class PSP extends BaseSpmlProvider {

  private static final Logger LOG = GrouperUtil.getLogger(PSP.class);

  private String id;

  private AttributeAuthority attributeAuthority;

  private Map<String, TargetDefinition> targetDefinitions;

  private GrouperSession grouperSession;

  public PSP() {
    // spml toolkit marshallers/unmarshallers
    ObjectFactory.getInstance().addCreator(new LdappcMarshallableCreator());
    ObjectFactory.getInstance().addOCEUnmarshaller(new DSMLUnmarshaller());
  }

  public CalcResponse execute(CalcRequest calcRequest) {

    this.setRequestId(calcRequest);
    LOG.trace("calc request:\n{}", this.toXML(calcRequest));

    CalcResponse calcResponse = new CalcResponse();
    calcResponse.setStatus(StatusCode.SUCCESS);
    calcResponse.setRequestID(this.getOrGenerateRequestID(calcRequest));

    if (calcRequest.getId() == null) {
      fail(calcResponse, ErrorCode.MALFORMED_REQUEST, "An id is required.");
      LOG.trace("calc response:\n{}", this.toXML(calcResponse));
      return calcResponse;
    }

    calcResponse.setId(calcRequest.getId());

    try {
      PSPContext context = this.getProvisioningContext(calcRequest);

      for (TargetDefinition targetDefinition : context.getPsoTargetDefinitions()) {
        calcResponse.addPSO(targetDefinition.getPSO(context));
      }

      if (calcResponse.getPSOs().isEmpty()) {
        // TODO document this error response
        calcResponse.setStatus(StatusCode.FAILURE);
        calcResponse.setError(ErrorCode.NO_SUCH_IDENTIFIER);
      }

    } catch (LdappcException e) {
      LOG.error("An LDAPPC error occurred.", e);
      fail(calcResponse, ErrorCode.CUSTOM_ERROR, e);
    } catch (AttributeRequestException e) {
      LOG.error("An Attribute Resolver error occurred.", e);
      fail(calcResponse, ErrorCode.CUSTOM_ERROR, e);
    } catch (Spml2Exception e) {
      LOG.error("An SPML2 error occurred.", e);
      fail(calcResponse, ErrorCode.CUSTOM_ERROR, e);
    }

    LOG.trace("calc response:\n{}", this.toXML(calcResponse));
    return calcResponse;
  }

  public DiffResponse execute(DiffRequest diffRequest) {

    this.setRequestId(diffRequest);
    LOG.trace("diff request:\n{}", this.toXML(diffRequest));

    DiffResponse diffResponse = new DiffResponse();
    diffResponse.setStatus(StatusCode.SUCCESS);
    diffResponse.setRequestID(this.getOrGenerateRequestID(diffRequest));

    if (diffRequest.getId() == null) {
      fail(diffResponse, ErrorCode.MALFORMED_REQUEST, "An id is required.");
      LOG.trace("diff response:\n{}", this.toXML(diffResponse));
      return diffResponse;
    }

    diffResponse.setId(diffRequest.getId());

    // calculate how the given id should be provisioned
    CalcRequest calcRequest = new CalcRequest();
    calcRequest.setId(diffRequest.getId());
    calcRequest.setTargetIds(diffRequest.getTargetIds());
    calcRequest.setReturnData(diffRequest.getReturnData());

    CalcResponse calcResponse = this.execute(calcRequest);

    if (calcResponse.getStatus().equals(StatusCode.FAILURE)) {
      if (calcResponse.getError() == ErrorCode.NO_SUCH_IDENTIFIER) {
        // if found on any target, delete
        for (TargetDefinition targetDefinition : targetDefinitions.values()) {

          LookupRequest lookupRequest = new LookupRequest();
          PSOIdentifier psoID = new PSOIdentifier();
          psoID.setID(diffRequest.getId());
          psoID.setTargetID(targetDefinition.getId());
          lookupRequest.setPsoID(psoID);
          lookupRequest.setReturnData(ReturnData.IDENTIFIER);

          Response lookupResponse = targetDefinition.getProvider().execute(lookupRequest);

          if (!(lookupResponse instanceof LookupResponse)) {
            fail(diffResponse, ErrorCode.CUSTOM_ERROR, "Lookup request did not return a lookup response.");
            LOG.trace("diff response:\n{}", this.toXML(diffResponse));
            return diffResponse;
          }

          if (lookupResponse.getStatus() == StatusCode.SUCCESS) {
            DeleteRequest deleteRequest = new DeleteRequest();
            deleteRequest.setPsoID(((LookupResponse) lookupResponse).getPso().getPsoID());
            diffResponse.addRequest(deleteRequest);
          }
        }
      } else {
        fail(diffResponse, calcResponse.getError(), "Unable to calculate response.");
        LOG.trace("diff response:\n{}", this.toXML(diffResponse));
        return diffResponse;
      }
    }

    List<PSO> psos = calcResponse.getPSOs();
    for (PSO pso : psos) {
      // lookup pso to see if and/or how it is provisioned
      String targetId = pso.getPsoID().getTargetID();
      LookupRequest lookupRequest = new LookupRequest();
      lookupRequest.setPsoID(pso.getPsoID());
      lookupRequest.setRequestID(this.generateRequestID(lookupRequest));
      lookupRequest.setReturnData(diffRequest.getReturnData());

      TargetDefinition targetDefinition = targetDefinitions.get(targetId);
      if (targetDefinition == null) {
        LOG.error("Unknown target id '{}'", targetId);
        fail(diffResponse, ErrorCode.NO_SUCH_IDENTIFIER);
        LOG.trace("diff response:\n{}", this.toXML(diffResponse));
        return diffResponse;
      }

      Response lookupResponse = targetDefinition.getProvider().execute(lookupRequest);

      if (!(lookupResponse instanceof LookupResponse)) {
        fail(diffResponse, ErrorCode.CUSTOM_ERROR, "Lookup request did not return a lookup response.");
        LOG.trace("diff response:\n{}", this.toXML(diffResponse));
        return diffResponse;
      }

      if (lookupResponse.getStatus().equals(StatusCode.FAILURE)) {
        if (lookupResponse.getError().equals(ErrorCode.NO_SUCH_IDENTIFIER)) {
          // pso should be added to target
          AddRequest addRequest = PSP.add(pso, diffRequest.getReturnData());
          diffResponse.addRequest(addRequest);
        } else {
          // any other error is a failure
          fail(diffResponse, lookupResponse.getError(), "Lookup request failed.");
          LOG.trace("diff response:\n{}", this.toXML(diffResponse));
          return diffResponse;
        }
      } else {
        // already exists
        // TODO delete if pso has no data besides identifier ?
        if (pso.getData() == null && pso.getCapabilityData().length == 0) {
          DeleteRequest deleteRequest = new DeleteRequest();
          deleteRequest.setPsoID(pso.getPsoID());
          diffResponse.addRequest(deleteRequest);
        } else {
          // modify if appropriate
          PSO currentPSO = ((LookupResponse) lookupResponse).getPso();
          try {
            ModifyRequest modifyRequest = PSP.diff(pso, currentPSO);
            if (modifyRequest.getModifications().length > 0) {
              diffResponse.addRequest(modifyRequest);
            } else {
              SynchronizedResponse synchronizedResponse = new SynchronizedResponse();
              synchronizedResponse.setPsoID(currentPSO.getPsoID());
              LOG.debug("adding s response\n{}", this.toXML(synchronizedResponse));
              diffResponse.addResponse(synchronizedResponse);
            }
          } catch (Spml2Exception e) {
            LOG.error("An SPML2 error occurred.", e);
            fail(diffResponse, ErrorCode.CUSTOM_ERROR, "Unable to diff objects.");
            LOG.trace("diff response:\n{}", this.toXML(diffResponse));
            return diffResponse;
          }
        }
      }
    }

    LOG.trace("diff response:\n{}", this.toXML(diffResponse));
    return diffResponse;
  }

  public SyncResponse execute(SyncRequest syncRequest) {

    this.setRequestId(syncRequest);
    LOG.trace("sync request:\n{}", this.toXML(syncRequest));

    SyncResponse syncResponse = new SyncResponse();
    syncResponse.setStatus(StatusCode.SUCCESS);
    syncResponse.setRequestID(this.getOrGenerateRequestID(syncRequest));

    if (syncRequest.getId() == null) {
      fail(syncResponse, ErrorCode.MALFORMED_REQUEST, "An id is required.");
      LOG.trace("sync response:\n{}", this.toXML(syncResponse));
      return syncResponse;
    }

    syncResponse.setId(syncRequest.getId());

    // first, calculate the diff
    DiffRequest diffRequest = new DiffRequest();
    diffRequest.setId(syncRequest.getId());
    diffRequest.setTargetIds(syncRequest.getTargetIds());
    diffRequest.setReturnData(syncRequest.getReturnData());

    DiffResponse diffResponse = this.execute(diffRequest);

    if (diffResponse.getStatus().equals(StatusCode.FAILURE)) {
      fail(syncResponse, diffResponse.getError());
      LOG.trace("sync response:\n{}", this.toXML(syncResponse));
      return syncResponse;
    }

    for (Request request : diffResponse.getRequests()) {
      String targetId = null;
      if (request instanceof AddRequest) {
        targetId = ((AddRequest) request).getTargetId();
      } else if (request instanceof ModifyRequest) {
        targetId = ((ModifyRequest) request).getPsoID().getTargetID();
      } else if (request instanceof DeleteRequest) {
        targetId = ((DeleteRequest) request).getPsoID().getTargetID();
      } else {
        LOG.error("Request " + request.getClass() + " is not supported.");
        fail(syncResponse, ErrorCode.UNSUPPORTED_OPERATION, "Request " + request.getClass() + " is not supported.");
        LOG.trace("sync response:\n{}", this.toXML(syncResponse));
        return syncResponse;
      }

      // execute each request
      TargetDefinition targetDefinition = targetDefinitions.get(targetId);
      if (targetDefinition == null) {
        LOG.error("Unknown target id '{}'", targetId);
        fail(syncResponse, ErrorCode.NO_SUCH_IDENTIFIER);
        LOG.trace("sync response:\n{}", this.toXML(syncResponse));
        return syncResponse;
      }

      Response response = targetDefinition.getProvider().execute(request);

      if (response.getStatus().equals(StatusCode.FAILURE)) {
        fail(syncResponse, response.getError(), response.getErrorMessages());
        LOG.trace("sync response:\n{}", this.toXML(syncResponse));
        return syncResponse;
      }

      try {
        syncResponse.addResponse(response);
      } catch (LdappcException e) {
        LOG.error("Response " + response.getClass() + " is not supported.");
        fail(syncResponse, ErrorCode.UNSUPPORTED_OPERATION, "Response " + response.getClass() + " is not supported.");
        LOG.trace("sync response:\n{}", this.toXML(syncResponse));
        return syncResponse;
      }
    }

    LOG.trace("sync response:\n{}", this.toXML(syncResponse));
    return syncResponse;
  }

  public LookupResponse execute(LookupRequest lookupRequest) {

    this.setRequestId(lookupRequest);
    LOG.trace("lookup request:\n{}", this.toXML(lookupRequest));

    // default response used during error conditions
    LookupResponse response = new LookupResponse();
    response.setStatus(StatusCode.SUCCESS);
    response.setRequestID(this.getOrGenerateRequestID(lookupRequest));

    PSOIdentifier psoID = lookupRequest.getPsoID();
    if (psoID == null) {
      LOG.error(ERROR_NULL_PSO_ID);
      fail(response, ErrorCode.MALFORMED_REQUEST, ERROR_NULL_PSO_ID);
      LOG.trace("lookup response:\n{}", this.toXML(response));
      return response;
    }

    String targetId = psoID.getTargetID();
    TargetDefinition targetDefinition = targetDefinitions.get(targetId);
    if (targetDefinition == null) {
      LOG.error("Unknown target id '{}'", targetId);
      fail(response, ErrorCode.NO_SUCH_IDENTIFIER);
      LOG.trace("lookup response:\n{}", this.toXML(response));
      return response;
    }

    // TODO lookup entityName only ?

    Response targetProviderResponse = targetDefinition.getProvider().execute(lookupRequest);

    if (!(targetProviderResponse instanceof LookupResponse)) {
      fail(response, ErrorCode.CUSTOM_ERROR, "Target did not return a lookup response.");
      LOG.trace("lookup response:\n{}", this.toXML(response));
      return response;
    }

    if (targetProviderResponse.getRequestID() == null) {
      targetProviderResponse.setRequestID(this.getOrGenerateRequestID(lookupRequest));
    }

    LOG.trace("lookup response:\n{}", this.toXML(targetProviderResponse));
    return (LookupResponse) targetProviderResponse;
  }

  public SearchResponse execute(SearchRequest searchRequest) {

    this.setRequestId(searchRequest);
    LOG.trace("search request:\n{}", this.toXML(searchRequest));

    // default response used during error conditions
    SearchResponse response = new SearchResponse();
    response.setStatus(StatusCode.SUCCESS);
    response.setRequestID(this.getOrGenerateRequestID(searchRequest));

    Query query = searchRequest.getQuery();
    if (query == null) {
      fail(response, ErrorCode.MALFORMED_REQUEST, "A query is required");
      LOG.trace("search response:\n{}", this.toXML(response));
      return response;
    }

    String targetId = query.getTargetID();
    if (targetId == null) {
      fail(response, ErrorCode.MALFORMED_REQUEST, ERROR_NULL_TARGET_ID);
      LOG.trace("search response:\n{}", this.toXML(response));
      return response;
    }

    TargetDefinition targetDefinition = targetDefinitions.get(targetId);
    if (targetDefinition == null) {
      LOG.error("Unknown target id '{}'", targetId);
      fail(response, ErrorCode.NO_SUCH_IDENTIFIER);
      LOG.trace("search response:\n{}", this.toXML(response));
      return response;
    }

    // TODO entityName

    Response targetProviderResponse = targetDefinition.getProvider().execute(searchRequest);

    if (!(targetProviderResponse instanceof SearchResponse)) {
      fail(response, ErrorCode.CUSTOM_ERROR, "Target did not return a SearchResponse.");
      LOG.trace("search response:\n{}", this.toXML(response));
      return response;
    }

    if (targetProviderResponse.getRequestID() == null) {
      targetProviderResponse.setRequestID(this.getOrGenerateRequestID(searchRequest));
    }

    LOG.trace("search response:\n{}", this.toXML(targetProviderResponse));
    return (SearchResponse) targetProviderResponse;
  }

  public AddResponse execute(AddRequest addRequest) {

    this.setRequestId(addRequest);
    LOG.trace("add request:\n{}", this.toXML(addRequest));

    AddResponse addResponse = new AddResponse();
    addResponse.setStatus(StatusCode.SUCCESS);
    addResponse.setRequestID(this.getOrGenerateRequestID(addRequest));

    if (addRequest.getPsoID() == null) {
      fail(addResponse, ErrorCode.MALFORMED_REQUEST, ERROR_NULL_PSO_ID);
      LOG.trace("add response:\n{}", this.toXML(addResponse));
      return addResponse;
    }

    String targetId = addRequest.getPsoID().getTargetID();
    if (targetId == null) {
      fail(addResponse, ErrorCode.MALFORMED_REQUEST, ERROR_NULL_TARGET_ID);
      LOG.trace("add response:\n{}", this.toXML(addResponse));
      return addResponse;
    }

    TargetDefinition targetDefinition = targetDefinitions.get(targetId);
    if (targetDefinition == null) {
      fail(addResponse, ErrorCode.NO_SUCH_IDENTIFIER);
      LOG.trace("add response:\n{}", this.toXML(addResponse));
      return addResponse;
    }

    Response targetProviderResponse = targetDefinition.getProvider().execute(addRequest);

    if (!(targetProviderResponse instanceof AddResponse)) {
      fail(addResponse, ErrorCode.CUSTOM_ERROR, "Target did not return a AddResponse.");
      LOG.trace("add response:\n{}", this.toXML(addResponse));
      return addResponse;
    }

    LOG.trace("add response:\n{}", this.toXML(targetProviderResponse));
    return (AddResponse) targetProviderResponse;
  }

  public DeleteResponse execute(DeleteRequest deleteRequest) {

    this.setRequestId(deleteRequest);
    LOG.trace("delete request:\n{}", this.toXML(deleteRequest));

    // default response used during error conditions
    DeleteResponse response = new DeleteResponse();
    response.setStatus(StatusCode.SUCCESS);
    response.setRequestID(this.getOrGenerateRequestID(deleteRequest));

    PSOIdentifier psoID = deleteRequest.getPsoID();
    if (psoID == null) {
      fail(response, ErrorCode.MALFORMED_REQUEST, ERROR_NULL_PSO_ID);
      LOG.trace("delete response:\n{}", this.toXML(response));
      return response;
    }

    String targetId = psoID.getTargetID();
    if (targetId == null) {
      fail(response, ErrorCode.MALFORMED_REQUEST, ERROR_NULL_TARGET_ID);
      LOG.trace("delete response:\n{}", this.toXML(response));
      return response;
    }

    TargetDefinition targetDefinition = targetDefinitions.get(targetId);
    if (targetDefinition == null) {
      fail(response, ErrorCode.NO_SUCH_IDENTIFIER);
      LOG.trace("delete response:\n{}", this.toXML(response));
      return response;
    }

    // TODO entityName

    Response targetProviderResponse = targetDefinition.getProvider().execute(deleteRequest);

    if (!(targetProviderResponse instanceof DeleteResponse)) {
      fail(response, ErrorCode.CUSTOM_ERROR, "Target did not return a DeleteResponse.");
      LOG.trace("delete response:\n{}", this.toXML(response));
      return response;
    }

    LOG.trace("delete response:\n{}", this.toXML(targetProviderResponse));
    return (DeleteResponse) targetProviderResponse;
  }

  public ListTargetsResponse execute(ListTargetsRequest listTargetsRequest) {

    this.setRequestId(listTargetsRequest);
    LOG.trace("list targets request:\n{}", this.toXML(listTargetsRequest));

    ListTargetsResponse listTargetsResponse = new ListTargetsResponse();
    listTargetsResponse.setStatus(StatusCode.SUCCESS);
    listTargetsResponse.setRequestID(this.getOrGenerateRequestID(listTargetsRequest));

    try {
      for (TargetDefinition psoTargetDefinition : targetDefinitions.values()) {
        listTargetsResponse.addTarget(psoTargetDefinition.getTarget());
      }
    } catch (Spml2Exception e) {
      // TODO UNSUPPORTED_PROFILE instead of CUSTOM_ERROR as appropriate
      LOG.error("An SPML2 error occurred.", e);
      fail(listTargetsResponse, ErrorCode.CUSTOM_ERROR, e);
    }

    LOG.trace("list targets response:\n{}", this.toXML(listTargetsResponse));
    return listTargetsResponse;
  }

  public BulkCalcResponse execute(BulkCalcRequest bulkCalcRequest) {

    this.setRequestId(bulkCalcRequest);
    LOG.trace("bulk calc request:\n{}", this.toXML(bulkCalcRequest));

    BulkCalcResponse bulkCalcResponse = new BulkCalcResponse();
    bulkCalcResponse.setStatus(StatusCode.SUCCESS);
    bulkCalcResponse.setRequestID(this.getOrGenerateRequestID(bulkCalcRequest));

    Set<Group> groups = this.getAllGroups();
    for (Group group : groups) {
      CalcRequest calcRequest = new CalcRequest();
      calcRequest.setId(group.getName());
      calcRequest.setTargetIds(bulkCalcRequest.getTargetIds());
      calcRequest.setReturnData(bulkCalcRequest.getReturnData());

      CalcResponse calcResponse = this.execute(calcRequest);
      // TODO break on error ?
      if (calcResponse.getStatus() != StatusCode.SUCCESS && bulkCalcResponse.getStatus() != StatusCode.FAILURE) {
        bulkCalcResponse.setStatus(StatusCode.FAILURE);
      }
      bulkCalcResponse.addResponse(calcResponse);
    }

    LOG.trace("bulk calc response:\n{}", this.toXML(bulkCalcResponse));
    return bulkCalcResponse;
  }

  public BulkDiffResponse execute(BulkDiffRequest bulkDiffRequest) {

    this.setRequestId(bulkDiffRequest);
    LOG.trace("bulk diff request:\n{}", this.toXML(bulkDiffRequest));

    BulkDiffResponse bulkDiffResponse = new BulkDiffResponse();
    bulkDiffResponse.setStatus(StatusCode.SUCCESS);
    bulkDiffResponse.setRequestID(this.getOrGenerateRequestID(bulkDiffRequest));

    Map<String, List<PSOIdentifier>> correctMap = new HashMap<String, List<PSOIdentifier>>();

    Set<Group> groups = this.getAllGroups();
    for (Group group : groups) {
      DiffRequest diffRequest = new DiffRequest();
      diffRequest.setId(group.getName());
      diffRequest.setTargetIds(bulkDiffRequest.getTargetIds());
      diffRequest.setReturnData(bulkDiffRequest.getReturnData());

      DiffResponse diffResponse = this.execute(diffRequest);
      // TODO break on error ?
      if (diffResponse.getStatus() != StatusCode.SUCCESS && bulkDiffResponse.getStatus() != StatusCode.FAILURE) {
        bulkDiffResponse.setStatus(StatusCode.FAILURE);
      }
      // build map of correct ids
      if (diffResponse.getStatus() == StatusCode.SUCCESS) {
        for (PSOIdentifier psoID : diffResponse.getPsoIds()) {
          String targetId = psoID.getTargetID();
          if (!correctMap.containsKey(targetId)) {
            correctMap.put(targetId, new ArrayList<PSOIdentifier>());
          }
          correctMap.get(targetId).add(psoID);
        }
      }
      bulkDiffResponse.addResponse(diffResponse);
    }

    // map of all existing ids
    Map<String, List<PSOIdentifier>> currentMap = new HashMap<String, List<PSOIdentifier>>();
    for (String targetId : bulkDiffRequest.getTargetIds()) {
      TargetDefinition targetDefinition = targetDefinitions.get(targetId);
      if (targetDefinition == null) {
        fail(bulkDiffResponse, ErrorCode.NO_SUCH_IDENTIFIER);
        LOG.trace("bulk diff response:\n{}", this.toXML(bulkDiffResponse));
        return bulkDiffResponse;
      }
      List<PSOIdentifier> psoIds = this.searchForPsoIds(targetDefinition);
      if (psoIds == null) {
        fail(bulkDiffResponse, ErrorCode.CUSTOM_ERROR, "An error occured while searching.");
        LOG.trace("bulk diff response:\n{}", this.toXML(bulkDiffResponse));
        return bulkDiffResponse;
      }
      currentMap.put(targetId, psoIds);
    }

    for (String targetId : currentMap.keySet()) {
      for (PSOIdentifier psoID : currentMap.get(targetId)) {
        if (correctMap.get(targetId) == null || !correctMap.get(targetId).contains(psoID)) {
          DeleteRequest deleteRequest = new DeleteRequest();
          deleteRequest.setPsoID(psoID);
          DiffResponse diffResponse = new DiffResponse();
          diffResponse.setId(psoID.getID());
          diffResponse.addRequest(deleteRequest);
          bulkDiffResponse.addResponse(diffResponse);
        }
      }
    }

    LOG.trace("bulk diff response:\n{}", this.toXML(bulkDiffResponse));
    return bulkDiffResponse;
  }

  public BulkSyncResponse execute(BulkSyncRequest bulkSyncRequest) {

    this.setRequestId(bulkSyncRequest);
    LOG.trace("bulk sync request:\n{}", this.toXML(bulkSyncRequest));

    BulkSyncResponse bulkSyncResponse = new BulkSyncResponse();
    bulkSyncResponse.setStatus(StatusCode.SUCCESS);
    bulkSyncResponse.setRequestID(this.getOrGenerateRequestID(bulkSyncRequest));

    BulkDiffRequest bulkDiffRequest = new BulkDiffRequest();
    bulkDiffRequest.setTargetIds(bulkSyncRequest.getTargetIds());
    bulkDiffRequest.setReturnData(bulkSyncRequest.getReturnData());

    BulkDiffResponse bulkDiffResponse = this.execute(bulkDiffRequest);

    if (bulkDiffResponse.getStatus() != StatusCode.SUCCESS) {
      fail(bulkSyncResponse, bulkDiffResponse.getError());
      LOG.trace("bulk sync response:\n{}", this.toXML(bulkSyncResponse));
      return bulkSyncResponse;
    }

    for (DiffResponse diffResponse : bulkDiffResponse.getResponses()) {
      for (Request request : diffResponse.getRequests()) {
        String targetId = null;
        if (request instanceof AddRequest) {
          targetId = ((AddRequest) request).getTargetId();
        } else if (request instanceof ModifyRequest) {
          targetId = ((ModifyRequest) request).getPsoID().getTargetID();
        } else if (request instanceof DeleteRequest) {
          targetId = ((DeleteRequest) request).getPsoID().getTargetID();
        } else {
          LOG.error("Request " + request.getClass() + " is not supported.");
          fail(bulkSyncResponse, ErrorCode.UNSUPPORTED_OPERATION, "Request " + request.getClass()
              + " is not supported.");
          LOG.trace("bulk sync response:\n{}", this.toXML(bulkSyncResponse));
          return bulkSyncResponse;
        }

        // execute each request
        TargetDefinition targetDefinition = targetDefinitions.get(targetId);
        if (targetDefinition == null) {
          LOG.error("Unknown target id '{}'", targetId);
          fail(bulkSyncResponse, ErrorCode.NO_SUCH_IDENTIFIER);
          LOG.trace("bulk sync response:\n{}", this.toXML(bulkSyncResponse));
          return bulkSyncResponse;
        }

        Response response = targetDefinition.getProvider().execute(request);

        if (response.getStatus().equals(StatusCode.FAILURE)) {
          fail(bulkSyncResponse, response.getError(), response.getErrorMessages());
          LOG.trace("bulk sync response:\n{}", this.toXML(bulkSyncResponse));
          return bulkSyncResponse;
        }

        try {
          SyncResponse syncResponse = new SyncResponse();
          syncResponse.setId(diffResponse.getId());
          syncResponse.addResponse(response);
          bulkSyncResponse.addResponse(syncResponse);
        } catch (LdappcException e) {
          LOG.error("Response " + response.getClass() + " is not supported.");
          fail(bulkSyncResponse, ErrorCode.UNSUPPORTED_OPERATION, "Response " + response.getClass()
              + " is not supported.");
          LOG.trace("bulk sync response:\n{}", this.toXML(bulkSyncResponse));
          return bulkSyncResponse;
        }
      }
    }

    LOG.trace("bulk sync response:\n{}", this.toXML(bulkSyncResponse));
    return bulkSyncResponse;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public AttributeAuthority getAttributeAuthority() {
    return attributeAuthority;
  }

  public void setAttributeAuthority(AttributeAuthority attributeAuthority) {
    this.attributeAuthority = attributeAuthority;
  }

  public PSPContext getProvisioningContext(ProvisioningRequest provisioningRequest) throws AttributeRequestException,
      Spml2Exception, LdappcException {

    // provisioning context
    PSPContext provContext = new PSPContext();
    provContext.setProvisioningServiceProvider(this);
    provContext.setProvisioningRequest(provisioningRequest);

    // attribute request context
    BaseSAMLProfileRequestContext attributeRequestContext = new BaseSAMLProfileRequestContext();
    attributeRequestContext.setPrincipalName(provisioningRequest.getId());

    // get targets specified in request before building the context
    List<TargetDefinition> psoTargetDefinitions = this.getTargetDefinitions(provisioningRequest);
    provContext.setPsoTargetDefinitions(psoTargetDefinitions);

    // determine attribute resolver requested attributes
    LinkedHashSet<String> attributeIds = new LinkedHashSet<String>();
    for (TargetDefinition psoTargetDefinition : psoTargetDefinitions) {
      attributeIds.addAll(psoTargetDefinition.getSourceIds(provisioningRequest.getReturnData()));
    }
    attributeRequestContext.setRequestedAttributes(attributeIds);

    // resolve attributes
    Map<String, BaseAttribute> attributes = getAttributeAuthority().getAttributes(attributeRequestContext);
    provContext.setAttributes(attributes);

    return provContext;
  }

  public Map<String, TargetDefinition> getTargetDefinitions() {
    return targetDefinitions;
  }

  public List<TargetDefinition> getTargetDefinitions(ProvisioningRequest provisioningRequest) throws LdappcException {

    ArrayList<TargetDefinition> defs = new ArrayList<TargetDefinition>();

    if (provisioningRequest.getTargetIds().isEmpty()) {
      defs.addAll(targetDefinitions.values());
    } else {
      for (String targetId : provisioningRequest.getTargetIds()) {
        TargetDefinition targetDefinition = targetDefinitions.get(targetId);
        if (targetDefinition == null) {
          LOG.error("Unknown target id '{}", targetId);
          throw new LdappcException("Unknown target id " + targetId);
        }
        defs.add(targetDefinition);
      }
    }

    if (defs.isEmpty()) {
      LOG.error("Unknown target ids " + provisioningRequest.getTargetIds());
      throw new LdappcException("Unknown target ids " + provisioningRequest.getTargetIds());
    }

    return defs;
  }

  @Override
  protected void onNewContextCreated(ApplicationContext newServiceContext) throws ServiceException {

    Map<String, TargetDefinition> oldTargetDefinitions = targetDefinitions;

    try {
      String[] beanNames = newServiceContext.getBeanNamesForType(TargetDefinition.class);
      LOG.debug("Loading {} target definitions {}", beanNames.length, Arrays.asList(beanNames));
      targetDefinitions = new LinkedHashMap<String, TargetDefinition>(beanNames.length);
      for (String beanName : beanNames) {
        TargetDefinition targetDefinition = (TargetDefinition) newServiceContext.getBean(beanName);
        targetDefinitions.put(targetDefinition.getId(), targetDefinition);
        // populate target's provider
        targetDefinition.getProvider().setTargetDefinition(targetDefinition);
        targetDefinition.getProvider().setPSP(this);
      }
    } catch (Exception e) {
      // TODO really catch all exceptions ?
      targetDefinitions = oldTargetDefinitions;
      LOG.error(getId() + " configuration is not valid, retaining old configuration", e);
      throw new ServiceException(getId() + " configuration is not valid, retaining old configuration", e);
    }
  }

  public static Map<String, DSMLAttr> getDSMLAttrMap(Extensible data) {

    Map<String, DSMLAttr> dsmlAttrs = new LinkedHashMap<String, DSMLAttr>();

    if (data == null) {
      return dsmlAttrs;
    }

    for (Object object : data.getOpenContentElements(DSMLAttr.class)) {
      DSMLAttr dsmlAttr = (DSMLAttr) object;
      dsmlAttrs.put(dsmlAttr.getName(), dsmlAttr);
    }
    return dsmlAttrs;
  }

  public static Map<String, List<Reference>> getReferences(CapabilityData[] capabilityDataArray) throws LdappcException {

    Map<String, List<Reference>> references = new LinkedHashMap<String, List<Reference>>();

    for (CapabilityData capabilityData : capabilityDataArray) {
      if (capabilityData.getCapabilityURI().equals(PSOReferencesDefinition.REFERENCE_URI)) {
        for (Object object : capabilityData.getOpenContentElements(OCEtoMarshallableAdapter.class)) {
          Object adaptedObject = ((OCEtoMarshallableAdapter) object).getAdaptedObject();
          if (adaptedObject instanceof Reference) {
            Reference reference = (Reference) adaptedObject;
            if (!references.containsKey(reference.getTypeOfReference())) {
              references.put(reference.getTypeOfReference(), new ArrayList<Reference>());
            }
            references.get(reference.getTypeOfReference()).add(reference);
          }
        }
      } else {
        LOG.warn("Encountered unhandled capability data '{}'", capabilityData.getCapabilityURI());
        if (capabilityData.isMustUnderstand()) {
          LOG.error("Encountered unhandled capability data '{}' which must be understood.", capabilityData
              .getCapabilityURI());
          throw new LdappcException("Encountered unhandled capability data which must be understood.");
        }
      }
    }

    return references;
  }

  public static AddRequest add(PSO pso, ReturnData returnData) {

    AddRequest addRequest = new AddRequest();

    // identifier
    addRequest.setPsoID(pso.getPsoID());
    addRequest.setTargetId(pso.getPsoID().getTargetID());
    if (pso.getPsoID().getContainerID() != null) {
      addRequest.setContainerID(pso.getPsoID().getContainerID());
    }

    // data
    if (returnData.equals(ReturnData.DATA) || returnData.equals(ReturnData.EVERYTHING)) {
      addRequest.setData(pso.getData());
    }

    // everything
    if (returnData.equals(ReturnData.EVERYTHING)) {
      for (CapabilityData capabilityData : pso.getCapabilityData()) {
        addRequest.addCapabilityData(capabilityData);
      }
    }
    addRequest.setReturnData(returnData);

    return addRequest;
  }

  public static ModifyRequest diff(PSO correctPSO, PSO currentPSO) throws LdappcException, Spml2Exception {

    ModifyRequest modifyRequest = new ModifyRequest();

    if (!correctPSO.getPsoID().equals(currentPSO.getPsoID())) {
      LOG.error("Unable to diff objects with different identifiers : '{}' and '{}'", PSPUtil.getString(correctPSO
          .getPsoID()), currentPSO.getPsoID());
      throw new LdappcException("Unable to diff objects with different identifiers.");
    }

    modifyRequest.setPsoID(correctPSO.getPsoID());

    List<Modification> dataMods = PSP.diffData(correctPSO, currentPSO);
    for (Modification modification : dataMods) {
      modifyRequest.addModification(modification);
    }

    List<Modification> referenceMods = PSP.diffReferences(correctPSO, currentPSO);
    for (Modification modification : referenceMods) {
      modifyRequest.addModification(modification);
    }

    return modifyRequest;
  }

  public static List<Modification> diffData(PSO correctPSO, PSO currentPSO) throws DSMLProfileException {
    List<Modification> modifications = new ArrayList<Modification>();

    Map<String, DSMLAttr> currentDsmlAttrs = PSP.getDSMLAttrMap(currentPSO.getData());
    Map<String, DSMLAttr> correctDsmlAttrs = PSP.getDSMLAttrMap(correctPSO.getData());

    Set<String> attrNames = new LinkedHashSet<String>();
    attrNames.addAll(correctDsmlAttrs.keySet());
    attrNames.addAll(currentDsmlAttrs.keySet());

    for (String attrName : attrNames) {
      DSMLAttr currentDsmlAttr = currentDsmlAttrs.get(attrName);
      DSMLAttr correctDsmlAttr = correctDsmlAttrs.get(attrName);

      AttributeModifier attributeModifier = new AttributeModifier(attrName);

      if (currentDsmlAttr != null) {
        attributeModifier.initDSML(currentDsmlAttr.getValues());
      }
      if (correctDsmlAttr != null) {
        attributeModifier.store(correctDsmlAttr.getValues());
      }

      modifications.addAll(attributeModifier.getDSMLModification());
    }

    return modifications;
  }

  public static List<Modification> diffReferences(PSO correctPSO, PSO currentPSO) throws Spml2Exception {
    List<Modification> modifications = new ArrayList<Modification>();

    Map<String, List<Reference>> correctReferenceMap = PSP.getReferences(correctPSO.getCapabilityData());
    Map<String, List<Reference>> currentReferenceMap = PSP.getReferences(currentPSO.getCapabilityData());

    Set<String> typeOfReferences = new LinkedHashSet<String>();
    typeOfReferences.addAll(correctReferenceMap.keySet());
    typeOfReferences.addAll(currentReferenceMap.keySet());

    for (String typeOfReference : typeOfReferences) {
      List<Reference> currentReferences = currentReferenceMap.get(typeOfReference);
      List<Reference> correctReferences = correctReferenceMap.get(typeOfReference);

      AttributeModifier attributeModifier = new AttributeModifier(typeOfReference);

      if (currentReferences != null) {
        attributeModifier.initReference(currentReferences);
      }
      if (correctReferences != null) {
        attributeModifier.store(correctReferences);
      }

      modifications.addAll(attributeModifier.getReferenceModification());
    }

    return modifications;
  }

  public Set<Group> getAllGroups() {

    Set<Group> groups = new LinkedHashSet<Group>();

    String[] attrResolverBeans = this.getApplicationContext().getBeanNamesForType(ShibbolethAttributeResolver.class);
    for (String attrResolverBean : attrResolverBeans) {
      ShibbolethAttributeResolver attributeResolver = (ShibbolethAttributeResolver) this.getApplicationContext()
          .getBean(attrResolverBean);

      String[] groupDataConnBeans = attributeResolver.getServiceContext().getBeanNamesForType(GroupDataConnector.class);
      for (String groupDataConnBean : groupDataConnBeans) {
        GroupDataConnector groupDataConnector = (GroupDataConnector) attributeResolver.getServiceContext().getBean(
            groupDataConnBean);

        GroupQueryFilter filter = groupDataConnector.getGroupQueryFilter();
        if (filter == null) {
          Stem root = StemFinder.findRootStem(this.getGrouperSession());
          groups.addAll(root.getChildGroups(Scope.SUB));
        } else {
          groups.addAll(groupDataConnector.getGroupQueryFilter().getResults(this.getGrouperSession()));
        }
      }
    }

    LOG.debug("found {} groups", groups);
    return groups;
  }

  public List<PSOIdentifier> searchForPsoIds(TargetDefinition targetDefinition) {
    List<PSOIdentifier> psoIds = new ArrayList<PSOIdentifier>();

    for (PSODefinition psoDef : targetDefinition.getPsoDefinitions()) {

      if (!psoDef.isAuthoritative()) {
        continue;
      }

      PSOIdentifier basePsoId = new PSOIdentifier();
      basePsoId.setID(psoDef.getPsoIdentifierDefinition().getBaseId());

      // TODO custom filter
      IdentifyingAttribute ia = psoDef.getPsoIdentifierDefinition().getIdentifyingAttribute();
      LdapFilterQueryClause filterQueryClause = new LdapFilterQueryClause();
      String filter = ia.getName() + "=" + ia.getValue();
      filterQueryClause.setFilter(filter);

      Query query = new Query();
      query.setTargetID(targetDefinition.getId());
      query.setBasePsoID(basePsoId);
      query.addQueryClause(filterQueryClause);

      SearchRequest searchRequest = new SearchRequest();
      searchRequest.setReturnData(ReturnData.IDENTIFIER);
      searchRequest.setQuery(query);

      SearchResponse response = this.execute(searchRequest);

      if (response.getStatus() == StatusCode.SUCCESS) {
        for (PSO pso : response.getPSOs()) {
          psoIds.add(pso.getPsoID());
        }
      } else {
        LOG.error("An error occurred while searching.");
        return null;
      }
    }

    return psoIds;
  }

  private GrouperSession getGrouperSession() {
    if (grouperSession == null) {
      // TODO make user configurable
      grouperSession = GrouperSession.startRootSession();
    }
    return grouperSession;
  }
}
