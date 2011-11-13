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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.openspml.v2.msg.spml.SchemaEntityRef;
import org.openspml.v2.msg.spml.StatusCode;
import org.openspml.v2.msg.spmlbatch.OnError;
import org.openspml.v2.msg.spmlref.HasReference;
import org.openspml.v2.msg.spmlref.Reference;
import org.openspml.v2.msg.spmlsearch.Query;
import org.openspml.v2.msg.spmlsearch.Scope;
import org.openspml.v2.msg.spmlsearch.SearchRequest;
import org.openspml.v2.msg.spmlsearch.SearchResponse;
import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.profiles.dsml.DSMLUnmarshaller;
import org.openspml.v2.util.Spml2Exception;
import org.openspml.v2.util.xml.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.definitions.PSODefinition;
import edu.internet2.middleware.ldappc.spml.definitions.PSOReferencesDefinition;
import edu.internet2.middleware.ldappc.spml.definitions.TargetDefinition;
import edu.internet2.middleware.ldappc.spml.provider.BaseSpmlProvider;
import edu.internet2.middleware.ldappc.spml.request.AlternateIdentifier;
import edu.internet2.middleware.ldappc.spml.request.BulkCalcRequest;
import edu.internet2.middleware.ldappc.spml.request.BulkCalcResponse;
import edu.internet2.middleware.ldappc.spml.request.BulkDiffRequest;
import edu.internet2.middleware.ldappc.spml.request.BulkDiffResponse;
import edu.internet2.middleware.ldappc.spml.request.BulkProvisioningRequest;
import edu.internet2.middleware.ldappc.spml.request.BulkProvisioningRequestHandler;
import edu.internet2.middleware.ldappc.spml.request.BulkSyncRequest;
import edu.internet2.middleware.ldappc.spml.request.BulkSyncResponse;
import edu.internet2.middleware.ldappc.spml.request.CalcRequest;
import edu.internet2.middleware.ldappc.spml.request.CalcResponse;
import edu.internet2.middleware.ldappc.spml.request.DiffRequest;
import edu.internet2.middleware.ldappc.spml.request.DiffResponse;
import edu.internet2.middleware.ldappc.spml.request.LdappcMarshallableCreator;
import edu.internet2.middleware.ldappc.spml.request.ProvisioningRequest;
import edu.internet2.middleware.ldappc.spml.request.ProvisioningResponse;
import edu.internet2.middleware.ldappc.spml.request.SearchRequestWithQueryClauseNamespaces;
import edu.internet2.middleware.ldappc.spml.request.SyncRequest;
import edu.internet2.middleware.ldappc.spml.request.SyncResponse;
import edu.internet2.middleware.ldappc.spml.request.SynchronizedResponse;
import edu.internet2.middleware.ldappc.util.MDCHelper;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.AttributeAuthority;
import edu.internet2.middleware.shibboleth.common.attribute.AttributeRequestException;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.profile.provider.BaseSAMLProfileRequestContext;
import edu.internet2.middleware.shibboleth.common.service.ServiceException;

/**
 * An incomplete SPML 2 Provisioning Service Provider.
 */
public class PSP extends BaseSpmlProvider {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PSP.class);

  /** Configuration xml element name. */
  public static final String DEFAULT_BEAN_NAME = "ldappc";

  /** Required bootstrap configuration files. */
  private static String[] CONFIG_FILES = { "ldappc-internal.xml", "ldappc-services.xml", };

  /** Spring identifier. */
  private String id;

  /** The Shibboleth attribute authority. */
  private AttributeAuthority attributeAuthority;

  /** Map of target identifiers to target definitions. */
  private Map<String, TargetDefinition> targetDefinitions;

  /** Runtime configuration. */
  private PSPOptions pspOptions;

  /** Whether or not to log spml messages. */
  private boolean logSpml;

  /** Constructor */
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
   * @param pspOptions The pspOptions to set.
   */
  public void setPspOptions(PSPOptions pspOptions) {
    this.pspOptions = pspOptions;
  }

  /**
   * If true will log spml messages.
   * 
   * @return Returns whether or not to log spml messages.
   */
  public boolean isLogSpml() {
    return logSpml;
  }

  /**
   * If set to true will log spml messages.
   * 
   * @param logSpml whether or not to log spml messages.
   */
  public void setLogSpml(boolean logSpml) {
    this.logSpml = logSpml;
  }

  public CalcResponse execute(CalcRequest calcRequest) {

    MDCHelper mdc = new MDCHelper(calcRequest).start();
    LOG.info("{}", calcRequest);
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(calcRequest));
    }
    writeRequest(calcRequest);

    CalcResponse calcResponse = new CalcResponse();
    calcResponse.setStatus(StatusCode.SUCCESS);
    calcResponse.setRequestID(this.getOrGenerateRequestID(calcRequest));

    if (this.isValid(calcRequest, calcResponse)) {
      calcResponse.setId(calcRequest.getId());

      try {
        PSPContext context = this.getProvisioningContext(calcRequest);

        Map<TargetDefinition, List<PSODefinition>> map = context.getTargetAndObjectDefinitions();
        for (TargetDefinition targetDefinition : map.keySet()) {
          for (PSODefinition psoDefinition : map.get(targetDefinition)) {
            for (PSO pso : psoDefinition.getPSO(context)) {
              calcResponse.addPSO(pso);
            }
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
      LOG.info("{}", calcResponse);
    } else {
      LOG.error("{}", calcResponse);
    }
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(calcResponse));
    }
    mdc.stop();
    writeResponse(calcResponse);
    return calcResponse;
  }

  public DiffResponse execute(DiffRequest diffRequest) {

    MDCHelper mdc = new MDCHelper(diffRequest).start();
    LOG.info("{}", diffRequest);
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(diffRequest));
    }
    writeRequest(diffRequest);

    DiffResponse diffResponse = new DiffResponse();
    diffResponse.setStatus(StatusCode.SUCCESS);
    diffResponse.setRequestID(this.getOrGenerateRequestID(diffRequest));

    if (this.isValid(diffRequest, diffResponse)) {
      // TODO this is not great
      new PSPDiffer(this, diffRequest, diffResponse).diff();
    }

    if (diffResponse.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.info("{}", diffResponse);
    } else {
      LOG.error("{}", diffResponse);
    }
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(diffResponse));
    }
    mdc.stop();
    writeResponse(diffResponse);
    return diffResponse;
  }

  public SyncResponse execute(SyncRequest syncRequest) {

    MDCHelper mdc = new MDCHelper(syncRequest).start();
    LOG.info("{}", syncRequest);
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(syncRequest));
    }
    writeRequest(syncRequest);

    SyncResponse syncResponse = new SyncResponse();
    syncResponse.setStatus(StatusCode.SUCCESS);
    syncResponse.setRequestID(this.getOrGenerateRequestID(syncRequest));

    if (!this.isValid(syncRequest, syncResponse)) {
      fail(syncResponse, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_NULL_ID);
      LOG.error("{}", syncResponse);
      if (isLogSpml()) {
        LOG.info("\n{}", this.toXML(syncResponse));
      }
      mdc.stop();
      writeResponse(syncResponse);
      return syncResponse;
    }
    syncResponse.setId(syncRequest.getId());

    // first, calculate the diff
    DiffRequest diffRequest = new DiffRequest();
    diffRequest.setId(syncRequest.getId());
    diffRequest.setRequestID(this.generateRequestID());
    diffRequest.setReturnData(syncRequest.getReturnData());
    diffRequest.setSchemaEntities(syncRequest.getSchemaEntities());

    DiffResponse diffResponse = this.execute(diffRequest);

    if (diffResponse.getStatus().equals(StatusCode.FAILURE)) {
      fail(syncResponse, diffResponse.getError(), diffResponse.getErrorMessages());
      LOG.error("{}", syncResponse);
      if (isLogSpml()) {
        LOG.info("\n{}", this.toXML(syncResponse));
      }
      mdc.stop();
      writeResponse(syncResponse);
      return syncResponse;
    }

    for (Request request : diffResponse.getRequests()) {

      Response response = this.execute(request);
      syncResponse.addResponse(response);

      if (response.getStatus().equals(StatusCode.FAILURE)) {
        fail(syncResponse, response.getError(), response.getErrorMessages());
        LOG.error("{}", syncResponse);
        if (isLogSpml()) {
          LOG.info("\n{}", this.toXML(syncResponse));
        }
        mdc.stop();
        writeResponse(syncResponse);
        return syncResponse;
      }
    }

    if (syncResponse.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.info("{}", syncResponse);
    } else {
      LOG.error("{}", syncResponse);
    }
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(syncResponse));
    }
    mdc.stop();
    writeResponse(syncResponse);
    return syncResponse;
  }

  public LookupResponse execute(LookupRequest lookupRequest) {

    MDCHelper mdc = new MDCHelper(lookupRequest).start();
    LOG.info(PSPUtil.toString(lookupRequest));
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(lookupRequest));
    }
    writeRequest(lookupRequest);

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
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(lookupResponse));
    }
    mdc.stop();
    writeResponse(lookupResponse);
    return lookupResponse;
  }

  public SearchResponse execute(SearchRequest searchRequest) {

    MDCHelper mdc = new MDCHelper(searchRequest).start();
    LOG.info("{}", PSPUtil.toString(searchRequest));
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(searchRequest));
    }
    writeRequest(searchRequest);

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
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(searchResponse));
    }
    mdc.stop();
    writeResponse(searchResponse);
    return searchResponse;
  }

  public AddResponse execute(AddRequest addRequest) {

    MDCHelper mdc = new MDCHelper(addRequest).start();
    LOG.info(PSPUtil.toString(addRequest));
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(addRequest));
    }
    writeRequest(addRequest);

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
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(addResponse));
    }
    mdc.stop();
    writeResponse(addResponse);
    return addResponse;
  }

  public DeleteResponse execute(DeleteRequest deleteRequest) {

    MDCHelper mdc = new MDCHelper(deleteRequest).start();
    LOG.info(PSPUtil.toString(deleteRequest));
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(deleteRequest));
    }
    writeRequest(deleteRequest);

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
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(deleteResponse));
    }
    mdc.stop();
    writeResponse(deleteResponse);
    return deleteResponse;
  }

  public ModifyResponse execute(ModifyRequest modifyRequest) {

    MDCHelper mdc = new MDCHelper(modifyRequest).start();
    LOG.info(PSPUtil.toString(modifyRequest));
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(modifyRequest));
    }
    writeRequest(modifyRequest);

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
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(modifyResponse));
    }
    mdc.stop();
    writeResponse(modifyResponse);
    return modifyResponse;
  }

  public ListTargetsResponse execute(ListTargetsRequest listTargetsRequest) {

    MDCHelper mdc = new MDCHelper(listTargetsRequest).start();
    LOG.info("{}", listTargetsRequest);
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(listTargetsRequest));
    }
    writeRequest(listTargetsRequest);

    ListTargetsResponse listTargetsResponse = new ListTargetsResponse();
    listTargetsResponse.setStatus(StatusCode.SUCCESS);
    listTargetsResponse.setRequestID(this.getOrGenerateRequestID(listTargetsRequest));

    try {
      for (TargetDefinition psoTargetDefinition : targetDefinitions.values()) {
        listTargetsResponse.addTarget(psoTargetDefinition.getTarget());
      }
    } catch (Spml2Exception e) {
      // TODO UNSUPPORTED_PROFILE instead of CUSTOM_ERROR as appropriate
      fail(listTargetsResponse, ErrorCode.CUSTOM_ERROR, e);
    }

    if (listTargetsResponse.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.info(PSPUtil.toString(listTargetsResponse));
    } else {
      LOG.error(PSPUtil.toString(listTargetsResponse));
    }
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(listTargetsResponse));
    }
    mdc.stop();
    writeResponse(listTargetsResponse);
    return listTargetsResponse;
  }

  /* specific to grouper */
  public BulkCalcResponse execute(BulkCalcRequest bulkCalcRequest) {

    MDCHelper mdc = new MDCHelper(bulkCalcRequest).start();
    LOG.info("{}", bulkCalcRequest);
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(bulkCalcRequest));
    }
    writeRequest(bulkCalcRequest);

    BulkCalcResponse bulkCalcResponse = new BulkCalcResponse();
    bulkCalcResponse.setStatus(StatusCode.SUCCESS);
    bulkCalcResponse.setRequestID(this.getOrGenerateRequestID(bulkCalcRequest));

    if (!this.isValid(bulkCalcRequest, bulkCalcResponse)) {
      LOG.error("{}", bulkCalcResponse);
      if (isLogSpml()) {
        LOG.info("\n{}", this.toXML(bulkCalcResponse));
      }
      mdc.stop();
      writeResponse(bulkCalcResponse);
      return bulkCalcResponse;
    }

    // get all identifiers
    BulkProvisioningRequestHandler handler = new BulkProvisioningRequestHandler(this, bulkCalcRequest);
    Set<String> identifiers = handler.getAllIdentifiers();

    // new CalcRequest for each identifier
    for (String identifier : identifiers) {
      CalcRequest calcRequest = new CalcRequest();
      calcRequest.setId(identifier);
      calcRequest.setRequestID(this.generateRequestID());
      calcRequest.setReturnData(bulkCalcRequest.getReturnData());
      calcRequest.setSchemaEntities(bulkCalcRequest.getSchemaEntities());

      CalcResponse calcResponse = this.execute(calcRequest);
      bulkCalcResponse.addResponse(calcResponse);

      // first failure encountered, stop processing if OnError.EXIT
      if (calcResponse.getStatus() != StatusCode.SUCCESS && bulkCalcResponse.getStatus() != StatusCode.FAILURE) {
        bulkCalcResponse.setStatus(StatusCode.FAILURE);
        if (bulkCalcRequest.getOnError().equals(OnError.EXIT)) {
          LOG.error("{}", bulkCalcResponse);
          if (isLogSpml()) {
            LOG.info("\n{}", this.toXML(bulkCalcResponse));
          }
          mdc.stop();
          writeResponse(bulkCalcResponse);
          return bulkCalcResponse;
        }
      }
    }

    if (bulkCalcResponse.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.info("{}", bulkCalcResponse);
    } else {
      LOG.error("{}", bulkCalcResponse);
    }
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(bulkCalcResponse));
    }
    mdc.stop();
    writeResponse(bulkCalcResponse);
    return bulkCalcResponse;
  }

  public BulkDiffResponse execute(BulkDiffRequest bulkDiffRequest) {

    MDCHelper mdc = new MDCHelper(bulkDiffRequest).start();
    LOG.info("{}", bulkDiffRequest);
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(bulkDiffRequest));
    }
    writeRequest(bulkDiffRequest);

    BulkDiffResponse bulkDiffResponse = new BulkDiffResponse();
    bulkDiffResponse.setStatus(StatusCode.SUCCESS);
    bulkDiffResponse.setRequestID(this.getOrGenerateRequestID(bulkDiffRequest));

    if (!this.isValid(bulkDiffRequest, bulkDiffResponse)) {
      LOG.error("{}", bulkDiffResponse);
      if (isLogSpml()) {
        LOG.info("\n{}", this.toXML(bulkDiffResponse));
      }
      mdc.stop();
      writeResponse(bulkDiffResponse);
      return bulkDiffResponse;
    }

    // get all identifiers
    BulkProvisioningRequestHandler handler = new BulkProvisioningRequestHandler(this, bulkDiffRequest);

    // new DiffRequest for each identifier
    for (String identifier : handler.getAllIdentifiers()) {
      DiffRequest diffRequest = new DiffRequest();
      diffRequest.setId(identifier);
      diffRequest.setRequestID(this.generateRequestID());
      diffRequest.setReturnData(bulkDiffRequest.getReturnData());
      diffRequest.setSchemaEntities(bulkDiffRequest.getSchemaEntities());

      DiffResponse diffResponse = this.execute(diffRequest);
      bulkDiffResponse.addResponse(diffResponse);

      // first failure encountered, stop processing if OnError.EXIT
      if (diffResponse.getStatus() != StatusCode.SUCCESS && bulkDiffResponse.getStatus() != StatusCode.FAILURE) {
        bulkDiffResponse.setStatus(StatusCode.FAILURE);
        if (bulkDiffRequest.getOnError().equals(OnError.EXIT)) {
          LOG.error("{}", bulkDiffResponse);
          if (isLogSpml()) {
            LOG.info("\n{}", this.toXML(bulkDiffResponse));
          }
          mdc.stop();
          writeResponse(bulkDiffResponse);
          return bulkDiffResponse;
        }
      }
    }

    // reconciliation

    // PSOIdentifiers that should exist
    Set<PSOIdentifier> correctPsoIds = new LinkedHashSet<PSOIdentifier>();

    // PSOIdentifiers to be deleted
    Set<PSOIdentifier> psoIdsToBeDeleted = new LinkedHashSet<PSOIdentifier>();

    for (DiffResponse diffResponse : bulkDiffResponse.getResponses()) {
      for (AddRequest addRequest : diffResponse.getAddRequests()) {
        correctPsoIds.add(addRequest.getPsoID());
      }
      for (ModifyRequest modifyRequest : diffResponse.getModifyRequests()) {
        correctPsoIds.add(modifyRequest.getPsoID());
      }
      for (DeleteRequest deleteRequest : diffResponse.getDeleteRequests()) {
        psoIdsToBeDeleted.add(deleteRequest.getPsoID());
      }
      for (SynchronizedResponse synchronizedResponse : diffResponse.getSynchronizedResponses()) {
        correctPsoIds.add(synchronizedResponse.getPsoID());
      }
    }

    // get the PSOIdentifiers which currently exist
    Set<PSOIdentifier> currentPsoIds = new LinkedHashSet<PSOIdentifier>();

    try {
      Map<TargetDefinition, List<PSODefinition>> map = getTargetAndObjectDefinitions(bulkDiffRequest);
      for (TargetDefinition targetDefinition : map.keySet()) {
        for (PSODefinition psoDefinition : map.get(targetDefinition)) {
          // if authoritative, get the PSOIdentifiers from the target provider
          if (psoDefinition.isAuthoritative()) {
            Set<PSOIdentifier> targetPsoIds = targetDefinition.getProvider().getPsoIdentifiers(psoDefinition);
            Set<PSOIdentifier> orderedPsoIds = targetDefinition.getProvider().orderForDeletion(targetPsoIds);
            currentPsoIds.addAll(orderedPsoIds);
          }
        }
      }
    } catch (LdappcException e) {
      fail(bulkDiffResponse, ErrorCode.CUSTOM_ERROR, e);
      LOG.error("{}", bulkDiffResponse);
      if (isLogSpml()) {
        LOG.info("\n{}", this.toXML(bulkDiffResponse));
      }
      mdc.stop();
      writeResponse(bulkDiffResponse);
      return bulkDiffResponse;
    }

    // DeleteRequests for identifiers which exist but shouldn't, and which are not already being deleted
    for (PSOIdentifier psoId : currentPsoIds) {
      if (!correctPsoIds.contains(psoId) && !psoIdsToBeDeleted.contains(psoId)) {
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setPsoID(psoId);
        deleteRequest.setRequestID(this.generateRequestID());
        DiffResponse diffResponse = new DiffResponse();
        diffResponse.setId(psoId.getID());
        diffResponse.addRequest(deleteRequest);
        bulkDiffResponse.addResponse(diffResponse);
      }
    }

    if (bulkDiffResponse.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.info("{}", bulkDiffResponse);
    } else {
      LOG.error("{}", bulkDiffResponse);
    }
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(bulkDiffResponse));
    }
    mdc.stop();
    writeResponse(bulkDiffResponse);
    return bulkDiffResponse;
  }

  public BulkSyncResponse execute(BulkSyncRequest bulkSyncRequest) {

    MDCHelper mdc = new MDCHelper(bulkSyncRequest).start();
    LOG.info("{}", bulkSyncRequest);
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(bulkSyncRequest));
    }
    writeRequest(bulkSyncRequest);

    BulkSyncResponse bulkSyncResponse = new BulkSyncResponse();
    bulkSyncResponse.setStatus(StatusCode.SUCCESS);
    bulkSyncResponse.setRequestID(this.getOrGenerateRequestID(bulkSyncRequest));

    if (!this.isValid(bulkSyncRequest, bulkSyncResponse)) {
      LOG.error("{}", bulkSyncResponse);
      if (isLogSpml()) {
        LOG.info("\n{}", this.toXML(bulkSyncResponse));
      }
      mdc.stop();
      writeResponse(bulkSyncResponse);
      return bulkSyncResponse;
    }

    BulkDiffRequest bulkDiffRequest = new BulkDiffRequest();
    bulkDiffRequest.setOnError(bulkSyncRequest.getOnError());
    bulkDiffRequest.setRequestID(this.generateRequestID());
    bulkDiffRequest.setReturnData(bulkSyncRequest.getReturnData());
    bulkDiffRequest.setSchemaEntities(bulkSyncRequest.getSchemaEntities());
    bulkDiffRequest.setUpdatedSince(bulkSyncRequest.getUpdatedSinceAsDate());

    BulkDiffResponse bulkDiffResponse = this.execute(bulkDiffRequest);

    if (bulkDiffResponse.getStatus() != StatusCode.SUCCESS && bulkSyncRequest.getOnError().equals(OnError.EXIT)) {
      fail(bulkSyncResponse, bulkDiffResponse.getError(), bulkDiffResponse.getErrorMessages());
      LOG.error("{}", bulkSyncResponse);
      if (isLogSpml()) {
        LOG.info("\n{}", this.toXML(bulkSyncResponse));
      }
      mdc.stop();
      writeResponse(bulkSyncResponse);
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
          if (isLogSpml()) {
            LOG.info("\n{}", this.toXML(bulkSyncResponse));
          }
          mdc.stop();
          writeResponse(bulkSyncResponse);
          return bulkSyncResponse;
        }

        // execute each request
        TargetDefinition targetDefinition = targetDefinitions.get(targetId);
        if (GrouperUtil.isBlank(targetDefinition)) {
          fail(bulkSyncResponse, ErrorCode.NO_SUCH_IDENTIFIER, "Unknown target id '" + targetId + "'");
          LOG.error("{}", bulkSyncResponse);
          if (isLogSpml()) {
            LOG.info("\n{}", this.toXML(bulkSyncResponse));
          }
          mdc.stop();
          writeResponse(bulkSyncResponse);
          return bulkSyncResponse;
        }

        Response response = targetDefinition.getProvider().execute(request);

        SyncResponse syncResponse = new SyncResponse();
        syncResponse.setId(diffResponse.getId());
        syncResponse.addResponse(response);
        bulkSyncResponse.addResponse(syncResponse);

        // first failure encountered, stop processing if OnError.EXIT
        if (response.getStatus() != StatusCode.SUCCESS && bulkSyncResponse.getStatus() != StatusCode.FAILURE) {
          bulkSyncResponse.setStatus(StatusCode.FAILURE);
          if (bulkSyncRequest.getOnError().equals(OnError.EXIT)) {
            LOG.error("{}", bulkSyncResponse);
            if (isLogSpml()) {
              LOG.info("\n{}", this.toXML(bulkSyncResponse));
            }
            mdc.stop();
            writeResponse(bulkSyncResponse);
            return bulkSyncResponse;
          }
        }
      }

      // include synchronized responses
      for (SynchronizedResponse synchronizedResponse : diffResponse.getSynchronizedResponses()) {
        SyncResponse syncResponse = new SyncResponse();
        syncResponse.setId(diffResponse.getId());
        syncResponse.addResponse(synchronizedResponse);
        bulkSyncResponse.addResponse(syncResponse);
      }
    }

    if (bulkSyncResponse.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.info("{}", bulkSyncResponse);
    } else {
      LOG.error("{}", bulkSyncResponse);
    }
    if (isLogSpml()) {
      LOG.info("\n{}", this.toXML(bulkSyncResponse));
    }
    mdc.stop();
    writeResponse(bulkSyncResponse);
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
    Map<TargetDefinition, List<PSODefinition>> map = this.getTargetAndObjectDefinitions(provisioningRequest);
    provContext.setTargetAndObjectDefinitions(map);

    // determine attribute resolver requested attributes
    LinkedHashSet<String> attributeIds = new LinkedHashSet<String>();
    for (TargetDefinition psoTargetDefinition : map.keySet()) {
      for (PSODefinition psoDefinition : map.get(psoTargetDefinition)) {
        attributeIds.addAll(psoDefinition.getSourceIds(provisioningRequest.getReturnData()));
      }
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

  public Map<TargetDefinition, List<PSODefinition>> getTargetAndObjectDefinitions(ProvisioningRequest request)
      throws LdappcException {
    Map<TargetDefinition, List<PSODefinition>> map = new LinkedHashMap<TargetDefinition, List<PSODefinition>>();
    if (request.getSchemaEntities().isEmpty()) {
      map.putAll(this.getTargetAndObjectDefinitions(new SchemaEntityRef()));
    } else {
      for (SchemaEntityRef schemaEntityRef : request.getSchemaEntities()) {
        map.putAll(this.getTargetAndObjectDefinitions(schemaEntityRef));
      }
    }
    return map;
  }

  public Map<TargetDefinition, List<PSODefinition>> getTargetAndObjectDefinitions(SchemaEntityRef schemaEntityRef)
      throws LdappcException {
    String msg = "get target and object definitions for " + PSPUtil.toString(schemaEntityRef);
    LOG.trace(msg);

    Map<TargetDefinition, List<PSODefinition>> map = new LinkedHashMap<TargetDefinition, List<PSODefinition>>();

    String targetId = schemaEntityRef == null ? null : schemaEntityRef.getTargetID();
    String objectId = schemaEntityRef == null ? null : schemaEntityRef.getEntityName();

    if (GrouperUtil.isBlank(targetId) && GrouperUtil.isBlank(objectId)) {

      for (TargetDefinition targetDefinition : targetDefinitions.values()) {
        map.put(targetDefinition, targetDefinition.getPsoDefinitions());
      }

    } else if (GrouperUtil.isBlank(targetId)) {

      for (TargetDefinition targetDefinition : targetDefinitions.values()) {
        PSODefinition psoDefinition = targetDefinition.getPSODefinition(objectId);
        if (psoDefinition == null) {
          LOG.error("Unknown object id '" + objectId + "'");
          throw new LdappcException("Unknown object id '" + objectId + "'");
        }
        map.put(targetDefinition, new ArrayList<PSODefinition>());
        map.get(targetDefinition).add(psoDefinition);
      }

    } else if (GrouperUtil.isBlank(objectId)) {

      TargetDefinition targetDefinition = targetDefinitions.get(targetId);
      if (targetDefinition == null) {
        LOG.error("Unknown target id '" + targetId + "'");
        throw new LdappcException("Unknown target id '" + targetId + "'");
      }
      map.put(targetDefinition, targetDefinition.getPsoDefinitions());

    } else {

      TargetDefinition targetDefinition = targetDefinitions.get(targetId);
      if (targetDefinition == null) {
        LOG.error("Unknown target id '" + targetId + "'");
        throw new LdappcException("Unknown target id '" + targetId + "'");
      }
      PSODefinition psoDefinition = targetDefinition.getPSODefinition(objectId);
      if (psoDefinition == null) {
        LOG.error("Unknown object id '" + objectId + "'");
        throw new LdappcException("Unknown object id '" + objectId + "'");
      }
      map.put(targetDefinition, new ArrayList<PSODefinition>());
      map.get(targetDefinition).add(psoDefinition);

    }

    LOG.trace("{} found {}", msg, map);
    return map;
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
          LOG.error("Encountered unhandled capability data '{}' which must be understood.",
              capabilityData.getCapabilityURI());
          throw new LdappcException("Encountered unhandled capability data which must be understood.");
        }
      }
    }

    return references;
  }

  /**
   * Return true if the given {@link PSOIdentifier} has the given {@Reference}.
   * 
   * @param psoID the pso identifier
   * @param reference the reference
   * @return true if the pso identifier has the reference, false otherwise
   * @throws LdappcException if the spml search fails
   */
  public boolean hasReference(PSOIdentifier psoID, Reference reference) throws LdappcException {

    HasReference hasReference = new HasReference();
    hasReference.setToPsoID(reference.getToPsoID());
    hasReference.setTypeOfReference(reference.getTypeOfReference());
    hasReference.setReferenceData(reference.getReferenceData());

    Query query = new Query();
    query.setBasePsoID(psoID);
    query.setTargetID(psoID.getTargetID());
    query.addQueryClause(hasReference);
    query.setScope(Scope.PSO);

    // SPML library namespace marshalling issue
    // SearchRequest searchRequest = new SearchRequest();
    SearchRequest searchRequest = new SearchRequestWithQueryClauseNamespaces();

    searchRequest.setReturnData(ReturnData.EVERYTHING);
    searchRequest.setQuery(query);
    searchRequest.setRequestID(PSPUtil.uniqueRequestId());

    SearchResponse response = execute(searchRequest);

    if (!response.getStatus().equals(StatusCode.SUCCESS)) {
      String errorMessage = PSPUtil.toString(response);
      LOG.error(errorMessage);
      throw new LdappcException(errorMessage);
    }

    if (response.getPSOs().length > 0) {
      return true;
    }

    return false;
  }

  public AddRequest add(PSO pso, ReturnData returnData) {

    AddRequest addRequest = new AddRequest();
    addRequest.setRequestID(this.generateRequestID());
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

  public boolean isValid(BulkProvisioningRequest provisioningRequest, ProvisioningResponse provisioningResponse) {
    try {
      this.getTargetAndObjectDefinitions(provisioningRequest);
    } catch (LdappcException e) {
      fail(provisioningResponse, ErrorCode.NO_SUCH_IDENTIFIER, e.getMessage());
      return false;
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

    for (Modification modification : modifyRequest.getModifications()) {
      List<AlternateIdentifier> alternateIdentifiers = modification.getOpenContentElements(AlternateIdentifier.class);
      if (alternateIdentifiers.size() > 1) {
        fail(modifyResponse, ErrorCode.CUSTOM_ERROR, PSPConstants.ERROR_MULTIPLE_ALTERNATE_IDENTIFIERS);
      }
      if (alternateIdentifiers.size() == 1) {
        AlternateIdentifier alternateIdentifier = alternateIdentifiers.get(0);
        if (alternateIdentifier.getID() == null || alternateIdentifier.getID().length() == 0) {
          fail(modifyResponse, ErrorCode.CUSTOM_ERROR, PSPConstants.ERROR_NULL_ID);
        }
        if (alternateIdentifier.getTargetID() == null || alternateIdentifier.getTargetID().length() == 0) {
          fail(modifyResponse, ErrorCode.CUSTOM_ERROR, PSPConstants.ERROR_NULL_TARGET_ID);
        }
      }
    }

    return true;
  }

  public boolean isValid(ProvisioningRequest provisioningRequest, ProvisioningResponse provisioningResponse) {

    if (GrouperUtil.isBlank(provisioningRequest.getId())) {
      fail(provisioningResponse, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_NULL_ID);
      return false;
    }

    try {
      this.getTargetAndObjectDefinitions(provisioningRequest);
    } catch (LdappcException e) {
      fail(provisioningResponse, ErrorCode.NO_SUCH_IDENTIFIER, e.getMessage());
      return false;
    }

    return true;
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

  public boolean isValid(Response response) {

    if (response.getStatus() == null) {
      fail(response, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_NULL_STATUS);
      return false;
    }

    if (!(response.getStatus().equals(StatusCode.SUCCESS) || response.getStatus().equals(StatusCode.FAILURE) || response
        .getStatus().equals(StatusCode.PENDING))) {
      fail(response, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_UNSUPPORTED_STATUS);
      return false;
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

  /**
   * Returns true if the lookup response is successful and the identifier exists. Returns false if the lookup response
   * is successful and the identifier does not exist. Throw exception otherwise.
   * 
   * @param lookupResponse the lookup response
   * @return true if the identifier exists, false if the identifier does not exist
   * @throws LdappcException if the lookup response is not successfull
   */
  public static boolean doesIdentifierExist(LookupResponse lookupResponse) throws LdappcException {

    if (lookupResponse.getStatus().equals(StatusCode.SUCCESS)) {
      return true;
    }

    if (lookupResponse.getStatus().equals(StatusCode.FAILURE)
        && lookupResponse.getError().equals(ErrorCode.NO_SUCH_IDENTIFIER)) {
      return false;
    }

    LOG.error("The lookup response is not a success '{}'", PSPUtil.toString(lookupResponse));
    throw new LdappcException("The lookup response is not a success '" + PSPUtil.toString(lookupResponse) + "'");
  }

  /**
   * Lookup an identifier and return true if the lookup is successful and the identifier exists. Return false if the
   * lookup is successful and the identifier does not exist. Throw exception if the attempt to lookup the identifier
   * does not succeed.
   * 
   * @param psoID the provisioned object identifier
   * @return true if the identifier exists, false if the identifier does not exist
   * @throws LdappcException if an error occurs during when attempting to lookup the identifier
   */
  public boolean doesIdentifierExist(PSOIdentifier psoID) throws LdappcException {

    LookupRequest lookupRequest = new LookupRequest();
    lookupRequest.setPsoID(psoID);
    lookupRequest.setRequestID(PSPUtil.uniqueRequestId());
    lookupRequest.setReturnData(ReturnData.IDENTIFIER);

    LookupResponse lookupResponse = execute(lookupRequest);

    return doesIdentifierExist(lookupResponse);
  }

  /**
   * Returns a {@link ModifyRequest} suitable for renaming the {@PSOIdentifier} of the given {@link PSO}
   * . Returns null if the object can not or should not be renamed.
   * 
   * The modify request must contain one and only one {@link AlternateIdentifier}. This alternate identifier is the
   * "old" or "current" identifier.
   * 
   * The "new" identifier is the pso identifier of the given pso.
   * 
   * If the "new" identifier already exists, or the "old" identifier does not exist, then return null.
   * 
   * @param pso the provisioned object
   * @return the modify request or null if the object should not or can not be renamed
   */
  public ModifyRequest renameRequest(PSO pso) {

    LOG.debug("PSP '{}' - Rename {}", getId(), PSPUtil.toString(pso));

    // get alternate identifiers from correct pso
    List<AlternateIdentifier> oldIDs = pso.getOpenContentElements(AlternateIdentifier.class);

    // do not rename if there are no alternate identifiers
    if (oldIDs.isEmpty()) {
      LOG.debug("PSP '{}' - Rename {} Will not rename. One alternate identifier is required.", getId(),
          PSPUtil.toString(pso));
      return null;
    }

    // throw exception if more than one alternate identifier
    if (oldIDs.size() > 1) {
      LOG.warn("PSP '{}' - Rename {} Will not rename. Multiple alternate identifiers are not supported.", getId(),
          PSPUtil.toString(pso));
      return null;
    }

    PSOIdentifier newPSOID = pso.getPsoID();

    // Do nothing if identifier exists.
    if (doesIdentifierExist(newPSOID)) {
      LOG.warn("PSP '{}' - Rename {} Will not rename. New identifier already exists.", getId(), PSPUtil.toString(pso));
      return null;
    }

    // Lookup alternate identifiers to see if they exist.
    List<AlternateIdentifier> foundOldIDs = new ArrayList<AlternateIdentifier>();

    for (AlternateIdentifier oldID : oldIDs) {
      if (doesIdentifierExist(oldID.getPSOIdentifier())) {
        foundOldIDs.add(oldID);
      }
    }

    // Do nothing if old identifiers do not exist.
    if (foundOldIDs.isEmpty()) {
      LOG.warn("PSP '{}' - Rename {} Will not rename. Alternate identifier must exist on target.", getId(),
          PSPUtil.toString(pso));
      return null;
    }

    // Throw exception if more than one old identifier is found.
    if (foundOldIDs.size() > 1) {
      LOG.warn("PSP '{}' - Rename {} Will not rename. Multiple alternate identifiers exist on target.", getId(),
          PSPUtil.toString(pso));
      return null;
    }

    PSOIdentifier oldPSOID = foundOldIDs.get(0).getPSOIdentifier();

    // rename
    ModifyRequest modifyRequest = new ModifyRequest();
    modifyRequest.setPsoID(oldPSOID);
    modifyRequest.setRequestID(PSPUtil.uniqueRequestId());

    Modification modification = new Modification();
    AlternateIdentifier newPsoID = new AlternateIdentifier();
    newPsoID.setPSOIdentifier(newPSOID);
    modification.addOpenContentElement(newPsoID);
    modification.setModificationMode(ModificationMode.REPLACE);
    modifyRequest.addModification(modification);

    LOG.warn("PSP '{}' - Rename {} Will rename with modify request {}", new Object[] { getId(), PSPUtil.toString(pso),
        PSPUtil.toString(modifyRequest) });

    return modifyRequest;
  }

}
