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
import java.util.TreeSet;

import org.opensaml.util.resource.ResourceException;
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
import org.openspml.v2.msg.spml.ModificationMode;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spml.ModifyResponse;
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
import org.openspml.v2.profiles.dsml.DSMLUnmarshaller;
import org.openspml.v2.util.Spml2Exception;
import org.openspml.v2.util.xml.ObjectFactory;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
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
import edu.internet2.middleware.ldappc.spml.request.ProvisioningResponse;
import edu.internet2.middleware.ldappc.spml.request.SyncRequest;
import edu.internet2.middleware.ldappc.spml.request.SyncResponse;
import edu.internet2.middleware.ldappc.spml.request.SynchronizedResponse;
import edu.internet2.middleware.ldappc.util.MDCHelper;
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

  /** Configuration xml node name. */
  public static final String DEFAULT_BEAN_NAME = "ldappc";

  /** Configuration files. */
  private static String[] CONFIG_FILES = { "ldappc-internal.xml", "ldappc-services.xml", };

  private String id;

  private AttributeAuthority attributeAuthority;

  private Map<String, TargetDefinition> targetDefinitions;

  private GrouperSession grouperSession;

  private PSPOptions pspOptions;

  /**
   * Default constructor. Must exist for Spring configuration.
   */
  public PSP() {
  }

  public void initialize() throws ServiceException {

    super.initialize();

    // spml toolkit marshallers/unmarshallers
    ObjectFactory.getInstance().addCreator(new LdappcMarshallableCreator());
    ObjectFactory.getInstance().addOCEUnmarshaller(new DSMLUnmarshaller());
  }

  public static PSP getPSP(PSPOptions pspOptions) throws ResourceException {
    String confDir = pspOptions != null ? pspOptions.getConfDir() : null;
    String beanName = pspOptions.getBeanName() != null ? pspOptions.getBeanName() : DEFAULT_BEAN_NAME;
    LOG.info("Loading PSP from configuration directory '{}' with bean name '{}'", confDir, beanName);
    ApplicationContext context = PSPUtil.createSpringContext(PSPUtil.getResources(confDir, CONFIG_FILES));
    PSP psp = (PSP) context.getBean(beanName);
    psp.setPspOptions(pspOptions);
    return psp;
  }

  /**
   * @return Returns the pspOptions.
   */
  public PSPOptions getPspOptions() {
    return pspOptions;
  }

  /**
   * @param pspOptions
   *          The pspOptions to set.
   */
  public void setPspOptions(PSPOptions pspOptions) {
    this.pspOptions = pspOptions;
  }

  public CalcResponse execute(CalcRequest calcRequest) {

    MDCHelper mdc = new MDCHelper(calcRequest).start();
    LOG.info("{}", calcRequest);
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(calcRequest));

    CalcResponse calcResponse = new CalcResponse();
    calcResponse.setStatus(StatusCode.SUCCESS);
    calcResponse.setRequestID(this.getOrGenerateRequestID(calcRequest));

    if (this.isValid(calcRequest, calcResponse)) {
      calcResponse.setId(calcRequest.getId());

      try {
        PSPContext context = this.getProvisioningContext(calcRequest);

        for (TargetDefinition targetDefinition : context.getPsoTargetDefinitions()) {
          for (PSO pso : targetDefinition.getPSO(context)) {
            calcResponse.addPSO(pso);
          }
        }

        if (calcResponse.getPSOs().isEmpty()) {
          // TODO document this error response
          fail(calcResponse, ErrorCode.NO_SUCH_IDENTIFIER, PSPConstants.ERROR_NO_OBJECT_IDENTIFIER);
        }

      } catch (LdappcException e) {
        fail(calcResponse, ErrorCode.CUSTOM_ERROR, e);
      } catch (AttributeRequestException e) {
        fail(calcResponse, ErrorCode.CUSTOM_ERROR, e);
      } catch (Spml2Exception e) {
        fail(calcResponse, ErrorCode.CUSTOM_ERROR, e);
      }
    }

    if (calcResponse.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.info(PSPUtil.toString(calcResponse));
    } else {
      LOG.error(PSPUtil.toString(calcResponse));
    }
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(calcResponse));
    mdc.stop();
    return calcResponse;
  }

  public DiffResponse execute(DiffRequest diffRequest) {

    MDCHelper mdc = new MDCHelper(diffRequest).start();
    LOG.info("{}", diffRequest);
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(diffRequest));

    DiffResponse diffResponse = new DiffResponse();
    diffResponse.setStatus(StatusCode.SUCCESS);
    diffResponse.setRequestID(this.getOrGenerateRequestID(diffRequest));

    if (this.isValid(diffRequest, diffResponse)) {
      new PSPDiffer(this, diffRequest, diffResponse).diff();
    }

    if (diffResponse.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.info(PSPUtil.toString(diffResponse));
    } else {
      LOG.error(PSPUtil.toString(diffResponse));
    }
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(diffResponse));
    mdc.stop();
    return diffResponse;
  }

  public SyncResponse execute(SyncRequest syncRequest) {

    this.setRequestId(syncRequest);
    MDCHelper mdc = new MDCHelper(syncRequest).start();
    LOG.info("{}", syncRequest);
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(syncRequest));

    SyncResponse syncResponse = new SyncResponse();
    syncResponse.setRequestID(this.getOrGenerateRequestID(syncRequest));

    if (GrouperUtil.isBlank(syncRequest.getId())) {
      fail(syncResponse, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_NULL_ID);
      LOG.error("{}", syncResponse);
      if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(syncResponse));
      mdc.stop();
      return syncResponse;
    }
    syncResponse.setId(syncRequest.getId());

    // first, calculate the diff
    DiffRequest diffRequest = new DiffRequest();
    this.setRequestId(diffRequest);
    diffRequest.setId(syncRequest.getId());
    diffRequest.setTargetIds(syncRequest.getTargetIds());
    diffRequest.setReturnData(syncRequest.getReturnData());

    DiffResponse diffResponse = this.execute(diffRequest);

    if (diffResponse.getStatus().equals(StatusCode.FAILURE)) {
      fail(syncResponse, diffResponse.getError(), diffResponse.getErrorMessages());
      LOG.error("{}", syncResponse);
      if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(syncResponse));
      mdc.stop();
      return syncResponse;
    }

    for (Request request : diffResponse.getRequests()) {

      Response response = this.execute(request);

      if (response.getStatus().equals(StatusCode.FAILURE)) {
        fail(syncResponse, response.getError(), response.getErrorMessages());
        LOG.error("{}", syncResponse);
        if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(syncResponse));
        mdc.stop();
        return syncResponse;
      }

      try {
        syncResponse.addResponse(response);
      } catch (LdappcException e) {
        fail(syncResponse, ErrorCode.CUSTOM_ERROR, e);
        LOG.error("{}", syncResponse);
        if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(syncResponse));
        mdc.stop();
        return syncResponse;
      }
    }

    syncResponse.setStatus(StatusCode.SUCCESS);
    LOG.info("{}", syncResponse);
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(syncResponse));
    mdc.stop();
    return syncResponse;
  }

  public LookupResponse execute(LookupRequest lookupRequest) {

    MDCHelper mdc = new MDCHelper(lookupRequest).start();
    LOG.info(PSPUtil.toString(lookupRequest));
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(lookupRequest));

    LookupResponse lookupResponse = new LookupResponse();
    lookupResponse.setStatus(StatusCode.SUCCESS);
    lookupResponse.setRequestID(this.getOrGenerateRequestID(lookupRequest));

    if (this.isValid(lookupRequest.getPsoID(), lookupResponse)) {
      TargetDefinition targetDefinition = targetDefinitions.get(lookupRequest.getPsoID().getTargetID());

      // TODO lookup entityName only ?
      Response targetProviderResponse = targetDefinition.getProvider().execute(lookupRequest);

      if (!(targetProviderResponse instanceof LookupResponse)) {
        fail(lookupResponse, ErrorCode.CUSTOM_ERROR, "Target did not return a lookup response.");
      } else {
        lookupResponse = (LookupResponse) targetProviderResponse;
      }
    }

    this.isValid(lookupResponse);

    if (lookupResponse.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.info(PSPUtil.toString(lookupResponse));
    } else {
      LOG.error(PSPUtil.toString(lookupResponse));
    }
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(lookupResponse));
    mdc.stop();
    return lookupResponse;
  }

  public SearchResponse execute(SearchRequest searchRequest) {

    MDCHelper mdc = new MDCHelper(searchRequest).start();
    LOG.info("{}", PSPUtil.toString(searchRequest));
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(searchRequest));

    SearchResponse searchResponse = new SearchResponse();
    searchResponse.setStatus(StatusCode.SUCCESS);
    searchResponse.setRequestID(this.getOrGenerateRequestID(searchRequest));

    if (this.isValid(searchRequest, searchResponse)) {
      // default return data to everything
      if (searchRequest.getReturnData() == null) {
        searchRequest.setReturnData(ReturnData.EVERYTHING);
      }
      TargetDefinition targetDefinition = targetDefinitions.get(searchRequest.getQuery().getTargetID());
      // TODO entityName ?
      Response targetProviderResponse = targetDefinition.getProvider().execute(searchRequest);

      if (!(targetProviderResponse instanceof SearchResponse)) {
        fail(searchResponse, ErrorCode.CUSTOM_ERROR, "Target did not return a SearchResponse.");
      } else {
        searchResponse = (SearchResponse) targetProviderResponse;
      }
    }

    if (searchResponse.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.info(PSPUtil.toString(searchResponse));
    } else {
      LOG.error(PSPUtil.toString(searchResponse));
    }
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(searchResponse));
    mdc.stop();
    return searchResponse;
  }

  public AddResponse execute(AddRequest addRequest) {

    MDCHelper mdc = new MDCHelper(addRequest).start();
    LOG.info(PSPUtil.toString(addRequest));
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(addRequest));

    AddResponse addResponse = new AddResponse();
    addResponse.setRequestID(this.getOrGenerateRequestID(addRequest));

    if (this.isValid(addRequest, addResponse)) {
      TargetDefinition targetDefinition = targetDefinitions.get(addRequest.getPsoID().getTargetID());

      Response targetProviderResponse = targetDefinition.getProvider().execute(addRequest);

      if (!(targetProviderResponse instanceof AddResponse)) {
        fail(addResponse, ErrorCode.CUSTOM_ERROR, "Target did not return a AddResponse.");
      } else {
        addResponse = (AddResponse) targetProviderResponse;
      }
    }

    this.isValid(addResponse);

    if (addResponse.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.info(PSPUtil.toString(addResponse));
    } else {
      LOG.error(PSPUtil.toString(addResponse));
    }
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(addResponse));
    mdc.stop();
    return addResponse;
  }

  public DeleteResponse execute(DeleteRequest deleteRequest) {

    MDCHelper mdc = new MDCHelper(deleteRequest).start();
    LOG.info(PSPUtil.toString(deleteRequest));
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(deleteRequest));

    // default response used during error conditions
    DeleteResponse deleteResponse = new DeleteResponse();
    deleteResponse.setRequestID(this.getOrGenerateRequestID(deleteRequest));

    if (this.isValid(deleteRequest.getPsoID(), deleteResponse)) {
      TargetDefinition targetDefinition = targetDefinitions.get(deleteRequest.getPsoID().getTargetID());

      Response targetProviderResponse = targetDefinition.getProvider().execute(deleteRequest);

      if (!(targetProviderResponse instanceof DeleteResponse)) {
        fail(deleteResponse, ErrorCode.CUSTOM_ERROR, "Target did not return a DeleteResponse.");
      } else {
        deleteResponse = (DeleteResponse) targetProviderResponse;
      }
    }

    this.isValid(deleteResponse);

    if (deleteResponse.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.info(PSPUtil.toString(deleteResponse));
    } else {
      LOG.error(PSPUtil.toString(deleteResponse));
    }
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(deleteResponse));
    mdc.stop();
    return deleteResponse;
  }

  public ModifyResponse execute(ModifyRequest modifyRequest) {

    MDCHelper mdc = new MDCHelper(modifyRequest).start();
    LOG.info(PSPUtil.toString(modifyRequest));
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(modifyRequest));

    ModifyResponse modifyResponse = new ModifyResponse();
    modifyResponse.setRequestID(this.getOrGenerateRequestID(modifyRequest));

    if (this.isValid(modifyRequest, modifyResponse)) {
      TargetDefinition targetDefinition = targetDefinitions.get(modifyRequest.getPsoID().getTargetID());

      Response targetProviderResponse = targetDefinition.getProvider().execute(modifyRequest);

      if (!(targetProviderResponse instanceof ModifyResponse)) {
        fail(modifyResponse, ErrorCode.CUSTOM_ERROR, "Target did not return a ModifyResponse.");
      } else {
        modifyResponse = (ModifyResponse) targetProviderResponse;
      }
    }

    this.isValid(modifyResponse);

    if (modifyResponse.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.info(PSPUtil.toString(modifyResponse));
    } else {
      LOG.error(PSPUtil.toString(modifyResponse));
    }
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(modifyResponse));
    mdc.stop();
    return modifyResponse;
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

  /* specific to grouper */
  public BulkCalcResponse execute(BulkCalcRequest bulkCalcRequest) {

    MDCHelper mdc = new MDCHelper(bulkCalcRequest).start();
    LOG.info("{}", bulkCalcRequest);
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(bulkCalcRequest));

    BulkCalcResponse bulkCalcResponse = new BulkCalcResponse();
    bulkCalcResponse.setStatus(StatusCode.SUCCESS);
    bulkCalcResponse.setRequestID(this.getOrGenerateRequestID(bulkCalcRequest));

    Set<String> identifiers = this.getAllIdentifiers();

    // calc
    for (String identifier : identifiers) {
      CalcRequest calcRequest = new CalcRequest();
      calcRequest.setId(identifier);
      calcRequest.setTargetIds(bulkCalcRequest.getTargetIds());
      calcRequest.setReturnData(bulkCalcRequest.getReturnData());
      this.setRequestId(calcRequest);

      CalcResponse calcResponse = this.execute(calcRequest);

      // first failure encountered
      if (calcResponse.getStatus() != StatusCode.SUCCESS && bulkCalcResponse.getStatus() != StatusCode.FAILURE) {
        bulkCalcResponse.setStatus(StatusCode.FAILURE);
        // FUTURE break on error toggle ?
      }

      bulkCalcResponse.addResponse(calcResponse);
    }

    LOG.info("{}", bulkCalcResponse);
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(bulkCalcResponse));
    mdc.stop();
    return bulkCalcResponse;
  }

  public BulkDiffResponse execute(BulkDiffRequest bulkDiffRequest) {

    MDCHelper mdc = new MDCHelper(bulkDiffRequest).start();
    LOG.info("{}", bulkDiffRequest);
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(bulkDiffRequest));

    BulkDiffResponse bulkDiffResponse = new BulkDiffResponse();
    bulkDiffResponse.setStatus(StatusCode.SUCCESS);
    bulkDiffResponse.setRequestID(this.getOrGenerateRequestID(bulkDiffRequest));

    Map<String, List<PSOIdentifier>> correctMap = new HashMap<String, List<PSOIdentifier>>();

    Set<String> identifiers = this.getAllIdentifiers();

    for (String identifier : identifiers) {
      DiffRequest diffRequest = new DiffRequest();
      diffRequest.setId(identifier);
      diffRequest.setTargetIds(bulkDiffRequest.getTargetIds());
      diffRequest.setReturnData(bulkDiffRequest.getReturnData());
      this.setRequestId(diffRequest);

      DiffResponse diffResponse = this.execute(diffRequest);

      if (diffResponse.getStatus() != StatusCode.SUCCESS && bulkDiffResponse.getStatus() != StatusCode.FAILURE) {
        bulkDiffResponse.setStatus(StatusCode.FAILURE);
        // FUTURE break on error toggle ?
      }

      bulkDiffResponse.addResponse(diffResponse);

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
    }

    // map of all existing ids
    Map<String, List<PSOIdentifier>> currentMap = new HashMap<String, List<PSOIdentifier>>();
    List<String> targetIds = bulkDiffRequest.getTargetIds();
    if (targetIds.isEmpty()) {
      targetIds = new ArrayList<String>(this.getTargetDefinitions().keySet());
    }

    for (String targetId : targetIds) {
      TargetDefinition targetDefinition = targetDefinitions.get(targetId);

      if (GrouperUtil.isBlank(targetDefinition)) {
        fail(bulkDiffResponse, ErrorCode.NO_SUCH_IDENTIFIER);
        LOG.error("{}", bulkDiffResponse);
        if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(bulkDiffResponse));
        mdc.stop();
        return bulkDiffResponse;
      }

      List<PSOIdentifier> psoIds = this.searchForPsoIds(targetDefinition);
      if (psoIds == null) {
        fail(bulkDiffResponse, ErrorCode.CUSTOM_ERROR, "An error occured while searching.");
        LOG.error("{}", bulkDiffResponse);
        if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(bulkDiffResponse));
        mdc.stop();
        return bulkDiffResponse;
      }

      currentMap.put(targetId, psoIds);
    }

    for (String targetId : currentMap.keySet()) {
      for (PSOIdentifier psoID : currentMap.get(targetId)) {
        if (correctMap.get(targetId) == null || !correctMap.get(targetId).contains(psoID)) {
          DeleteRequest deleteRequest = new DeleteRequest();
          deleteRequest.setPsoID(psoID);
          this.setRequestId(deleteRequest);
          DiffResponse diffResponse = new DiffResponse();
          diffResponse.setId(psoID.getID());
          diffResponse.addRequest(deleteRequest);
          bulkDiffResponse.addResponse(diffResponse);
        }
      }
    }

    LOG.info("{}", bulkDiffResponse);
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(bulkDiffResponse));
    mdc.stop();
    return bulkDiffResponse;
  }

  public BulkSyncResponse execute(BulkSyncRequest bulkSyncRequest) {

    MDCHelper mdc = new MDCHelper(bulkSyncRequest).start();
    LOG.info("{}", bulkSyncRequest);
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(bulkSyncRequest));

    BulkSyncResponse bulkSyncResponse = new BulkSyncResponse();
    bulkSyncResponse.setStatus(StatusCode.SUCCESS);
    bulkSyncResponse.setRequestID(this.getOrGenerateRequestID(bulkSyncRequest));

    BulkDiffRequest bulkDiffRequest = new BulkDiffRequest();
    bulkDiffRequest.setTargetIds(bulkSyncRequest.getTargetIds());
    bulkDiffRequest.setReturnData(bulkSyncRequest.getReturnData());
    this.setRequestId(bulkDiffRequest);

    BulkDiffResponse bulkDiffResponse = this.execute(bulkDiffRequest);

    if (bulkDiffResponse.getStatus() != StatusCode.SUCCESS) {
      fail(bulkSyncResponse, bulkDiffResponse.getError(), bulkDiffResponse.getErrorMessages());
      LOG.error("{}", bulkSyncResponse);
      if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(bulkSyncResponse));
      mdc.stop();
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
          fail(bulkSyncResponse, ErrorCode.UNSUPPORTED_OPERATION, "Unsupported request " + request.getClass());
          LOG.error("{}", bulkSyncResponse);
          if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(bulkSyncResponse));
          mdc.stop();
          return bulkSyncResponse;
        }

        // execute each request
        TargetDefinition targetDefinition = targetDefinitions.get(targetId);
        if (GrouperUtil.isBlank(targetDefinition)) {
          fail(bulkSyncResponse, ErrorCode.NO_SUCH_IDENTIFIER, "Unknown target id '" + targetId + "'");
          LOG.error("{}", bulkSyncResponse);
          if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(bulkSyncResponse));
          mdc.stop();
          return bulkSyncResponse;
        }

        Response response = targetDefinition.getProvider().execute(request);

        if (response.getStatus() != StatusCode.SUCCESS && bulkSyncResponse.getStatus() != StatusCode.FAILURE) {
          bulkSyncResponse.setStatus(StatusCode.FAILURE);
          // FUTURE break on error toggle ?
        }

        try {
          SyncResponse syncResponse = new SyncResponse();
          syncResponse.setId(diffResponse.getId());
          syncResponse.addResponse(response);
          bulkSyncResponse.addResponse(syncResponse);
        } catch (LdappcException e) {
          fail(bulkSyncResponse, ErrorCode.CUSTOM_ERROR, e);
          LOG.error("{}", bulkSyncResponse);
          if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(bulkSyncResponse));
          mdc.stop();
          return bulkSyncResponse;
        }
      }

      for (SynchronizedResponse synchronizedResponse : diffResponse.getSynchronizedResponses()) {
        SyncResponse syncResponse = new SyncResponse();
        syncResponse.setId(diffResponse.getId());
        syncResponse.addResponse(synchronizedResponse);
        bulkSyncResponse.addResponse(syncResponse);
      }
    }

    LOG.info("{}", bulkSyncResponse);
    if (pspOptions.isLogSpml()) LOG.info("\n{}", this.toXML(bulkSyncResponse));
    mdc.stop();
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
    LOG.debug("{} resolving attributes {}", provisioningRequest, attributeIds);
    Map<String, BaseAttribute<?>> attributes = getAttributeAuthority().getAttributes(attributeRequestContext);
    LOG.debug("{} resolved attributes {}", provisioningRequest, attributes.keySet());
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

  public static Map<String, List<Reference>> getReferences(CapabilityData[] capabilityDataArray)
            throws LdappcException {

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

  public AddRequest add(PSO pso, ReturnData returnData) {

    AddRequest addRequest = new AddRequest();
    this.setRequestId(addRequest);
    addRequest.setReturnData(returnData);

    String entityName = pso.findOpenContentAttrValueByName(PSODefinition.ENTITY_NAME_ATTRIBUTE);
    if (entityName != null) {
      addRequest.addOpenContentAttr(PSODefinition.ENTITY_NAME_ATTRIBUTE, entityName);
    }

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

    return addRequest;
  }

  public Set<Group> getAllGroups() {

    Set<Group> groups = new TreeSet<Group>();

    String[] attrResolverBeans = this.getApplicationContext()
                .getBeanNamesForType(ShibbolethAttributeResolver.class);
    for (String attrResolverBean : attrResolverBeans) {
      ShibbolethAttributeResolver attributeResolver = (ShibbolethAttributeResolver) this.getApplicationContext()
                    .getBean(attrResolverBean);

      String[] groupDataConnBeans = attributeResolver.getServiceContext().getBeanNamesForType(
                    GroupDataConnector.class);
      for (String groupDataConnBean : groupDataConnBeans) {
        GroupDataConnector groupDataConnector = (GroupDataConnector) attributeResolver.getServiceContext()
                        .getBean(groupDataConnBean);

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

  public Set<String> getAllIdentifiers() {
    Set<String> identifiers = new LinkedHashSet<String>();

    // FUTURE perhaps implement GrouperDataConnector.getAll();
    Set<Group> groups = this.getAllGroups();

    // stems
    Set<String> stemNames = GrouperUtil.findParentStemNames(groups);
    for (String stemName : new TreeSet<String>(stemNames)) {
      // omit root
      if (stemName.equals(Stem.DELIM)) {
        continue;
      }
      identifiers.add(stemName);
    }

    // groups
    for (Group group : groups) {
      identifiers.add(group.getName());
    }

    // members
    Set<Member> members = new TreeSet<Member>();
    for (Group group : groups) {
      for (Member member : group.getMembers()) {
        // only provision groups once
        if (member.getSubjectSourceId().equals("g:gsa")) {
          continue;
        }
        members.add(member);
      }
    }
    for (Member member : members) {
      identifiers.add(member.getSubjectId());
    }

    return identifiers;
  }

  public List<PSOIdentifier> searchForPsoIds(TargetDefinition targetDefinition) {
    List<PSOIdentifier> psoIds = new ArrayList<PSOIdentifier>();

    for (PSODefinition psoDef : targetDefinition.getPsoDefinitions()) {
      // TODO remove
      LOG.debug("psoDef authoritative {}", psoDef.isAuthoritative());
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
      query.setScope(org.openspml.v2.msg.spmlsearch.Scope.SUBTREE);

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

    LOG.debug("found {} pso ids", psoIds.size());

    return psoIds;
  }

  private GrouperSession getGrouperSession() {
    if (grouperSession == null) {
      // TODO make user configurable
      grouperSession = GrouperSession.startRootSession();
    }
    return grouperSession;
  }

  public boolean isValid(PSOIdentifier psoID, Response response) {

    if (GrouperUtil.isBlank(psoID) || GrouperUtil.isBlank(psoID.getID())) {
      fail(response, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_NULL_PSO_ID);
      return false;
    }

    if (GrouperUtil.isBlank(psoID.getTargetID())) {
      fail(response, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_NULL_TARGET_ID);
      return false;
    }

    TargetDefinition targetDefinition = targetDefinitions.get(psoID.getTargetID());
    if (targetDefinition == null) {
      fail(response, ErrorCode.NO_SUCH_IDENTIFIER, "Unknown target id '" + psoID.getTargetID() + "'");
      return false;
    }

    return true;
  }

  public boolean isValid(AddRequest addRequest, AddResponse addResponse) {

    if (!this.isValid(addRequest.getPsoID(), addResponse)) {
      return false;
    }

    if (addRequest.getData() == null) {
      fail(addResponse, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_NO_DATA);
      return false;
    }

    String entityName = addRequest.findOpenContentAttrValueByName(PSODefinition.ENTITY_NAME_ATTRIBUTE);
    if (GrouperUtil.isBlank(entityName)) {
      fail(addResponse, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_INVALID_ENTITY_NAME);
      return false;
    }

    TargetDefinition targetDefinition = targetDefinitions.get(addRequest.getPsoID().getTargetID());
    PSODefinition psoDefinition = targetDefinition.getPSODefinition(entityName);
    if (psoDefinition == null) {
      fail(addResponse, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_INVALID_ENTITY_NAME);
      return false;
    }

    Map<String, DSMLAttr> dsmlAttrs = PSP.getDSMLAttrMap(addRequest.getData());
    for (String attrName : dsmlAttrs.keySet()) {
      if (psoDefinition.getAttributeDefinition(attrName) == null) {
        fail(addResponse, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_INVALID_ATTRIBUTE);
        return false;
      }
    }

    return true;

  }

  public boolean isValid(ModifyRequest modifyRequest, ModifyResponse modifyResponse) {

    if (!this.isValid(modifyRequest.getPsoID(), modifyResponse)) {
      return false;
    }

    if (modifyRequest.getModifications().length == 0) {
      fail(modifyResponse, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_NULL_MODIFICATIONS);
      return false;
    }

    for (Modification modification : modifyRequest.getModifications()) {
      if (GrouperUtil.isBlank(modification.getModificationMode())) {
        fail(modifyResponse, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_NULL_MODIFICATION_MODE);
        return false;
      }
      if (!(modification.getModificationMode().equals(ModificationMode.ADD)
          || modification.getModificationMode().equals(ModificationMode.DELETE) || modification.getModificationMode()
          .equals(ModificationMode.REPLACE))) {
        fail(modifyResponse, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_UNSUPPORTED_MODIFICATION_MODE);
        return false;
      }
    }

    return true;
  }

  public boolean isValid(SearchRequest searchRequest, SearchResponse searchResponse) {

    Query query = searchRequest.getQuery();
    if (query == null) {
      fail(searchResponse, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_NULL_QUERY);
      return false;
    }

    String targetId = query.getTargetID();
    if (GrouperUtil.isBlank(targetId)) {
      fail(searchResponse, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_NULL_TARGET_ID);
      return false;
    }

    TargetDefinition targetDefinition = targetDefinitions.get(targetId);
    if (targetDefinition == null) {
      fail(searchResponse, ErrorCode.NO_SUCH_IDENTIFIER, "Unknown target id '" + targetId + "'");
      return false;
    }

    return true;
  }

  public boolean isValid(Response response) {

    if (response.getStatus() == null) {
      fail(response, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_NULL_STATUS);
      return false;
    }

    if (!(response.getStatus().equals(StatusCode.SUCCESS)
        || response.getStatus().equals(StatusCode.FAILURE) || response.getStatus().equals(StatusCode.PENDING))) {
      fail(response, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_UNSUPPORTED_STATUS);
      return false;
    }

    return true;
  }

  public boolean isValid(ProvisioningRequest provisioningRequest, ProvisioningResponse provisioningResponse) {

    if (GrouperUtil.isBlank(provisioningRequest.getId())) {
      fail(provisioningResponse, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_NULL_ID);
      return false;
    }

    return true;
  }
}
