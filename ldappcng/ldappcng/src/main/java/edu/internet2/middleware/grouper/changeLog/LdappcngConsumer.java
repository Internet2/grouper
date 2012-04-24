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
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.changeLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.opensaml.util.resource.ResourceException;
import org.openspml.v2.msg.spml.CapabilityData;
import org.openspml.v2.msg.spml.DeleteRequest;
import org.openspml.v2.msg.spml.DeleteResponse;
import org.openspml.v2.msg.spml.ErrorCode;
import org.openspml.v2.msg.spml.LookupRequest;
import org.openspml.v2.msg.spml.LookupResponse;
import org.openspml.v2.msg.spml.Modification;
import org.openspml.v2.msg.spml.ModificationMode;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spml.ModifyResponse;
import org.openspml.v2.msg.spml.PSO;
import org.openspml.v2.msg.spml.Response;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.StatusCode;
import org.openspml.v2.msg.spmlref.HasReference;
import org.openspml.v2.msg.spmlref.Reference;
import org.openspml.v2.util.Spml2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.ChangeLogDataConnector;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.PSP;
import edu.internet2.middleware.ldappc.spml.PSPOptions;
import edu.internet2.middleware.ldappc.spml.definitions.PSODefinition;
import edu.internet2.middleware.ldappc.spml.request.BulkSyncRequest;
import edu.internet2.middleware.ldappc.spml.request.BulkSyncResponse;
import edu.internet2.middleware.ldappc.spml.request.CalcRequest;
import edu.internet2.middleware.ldappc.spml.request.CalcResponse;
import edu.internet2.middleware.ldappc.spml.request.SyncRequest;
import edu.internet2.middleware.ldappc.spml.request.SyncResponse;
import edu.internet2.middleware.ldappc.util.PSPUtil;

/**
 * A {@link ChangeLogConsumer} which provisions via ldappcng.
 * 
 * Extensions should override the process methods to change provisioning behavior.
 */
public class LdappcngConsumer extends ChangeLogConsumerBase {

  /** Maps change log entry category and action (change log type) to methods. */
  enum LdappcngEventType {

    /** Process the add group change log entry type. */
    group__addGroup {
      public void process(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
        consumer.processGroupAdd(consumer, changeLogEntry);
      }
    },

    /** Process the delete group change log entry type. */
    group__deleteGroup {
      public void process(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
        consumer.processGroupDelete(consumer, changeLogEntry);
      }
    },

    /** Process the update group change log entry type. */
    group__updateGroup {
      public void process(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
        consumer.processGroupUpdate(consumer, changeLogEntry);
      }
    },

    /** Process the add membership change log entry type. */
    membership__addMembership {
      public void process(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
        consumer.processMembershipAdd(consumer, changeLogEntry);
      }
    },

    /** Process the delete membership change log entry type. */
    membership__deleteMembership {
      public void process(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
        consumer.processMembershipDelete(consumer, changeLogEntry);
      }
    },

    /** Process the add stem change log entry type. */
    stem__addStem {
      public void process(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
        consumer.processStemAdd(consumer, changeLogEntry);
      }
    },

    /** Process the delete stem change log entry type. */
    stem__deleteStem {
      public void process(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
        consumer.processStemDelete(consumer, changeLogEntry);
      }
    },

    /** Process the update stem change log entry type. */
    stem__updateStem {
      public void process(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
        consumer.processStemUpdate(consumer, changeLogEntry);
      }
    },

    ;

    /** Process the change log entry. */
    public abstract void process(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception;

  }

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(LdappcngConsumer.class);

  /** The Provisioning Service Provider. */
  private static PSP psp;

  /** The change log consumer name from the processor metadata. */
  private String name;

  /** Boolean used to delay change log processing when a full sync is running. */
  private static boolean fullSyncIsRunning;

  /** LDAP error returned when a stem/ou is renamed and the DSA does not support subtree renaming. */
  public static final String ERROR_SUBTREE_RENAME_NOT_SUPPORTED = "[LDAP: error code 66 - subtree rename not supported]";

  /**
   * 
   * Constructor. Initializes the underlying {@link PSP}.
   * 
   * @throws ResourceException
   */
  public LdappcngConsumer() throws ResourceException {
    initialize();
  }

  /**
   * Return the {@link PSP}.
   * 
   * @return the provisioning service provider
   */
  public PSP getPsp() {
    return psp;
  }

  /**
   * If the underlying {@link PSP} has not been initialized, instantiate the {@link PSP}. Use the configuration
   * directory from the 'changeLog.consumer.ldappcng.confDir' property. If this property has not been set, then
   * configuration resources on the classpath will be used.
   * 
   * @throws ResourceException
   */
  public void initialize() throws ResourceException {

    if (psp == null) {
      PSPOptions pspOptions = new PSPOptions(null);
      String confDir = GrouperLoaderConfig.getPropertyString("changeLog.consumer.ldappcng.confDir");
      if (!confDir.isEmpty()) {
        LOG.info("Configuration directory {} set via property changeLog.consumer.ldappcng.confDir", confDir);
        pspOptions.setConfDir(confDir);
      }
      psp = PSP.getPSP(pspOptions);
    }
  }

  /**
   * Execute each {@link ModifyRequest}. If an error occurs executing a request, continue to execute requests, but throw
   * an exception upon completion.
   * 
   * @param consumer the change log consumer
   * @param changeLogEntry the change log entry
   * @param modifyRequests the modify requests
   * @throws LdappcException if an error occurs processing any modify request
   */
  public void executeModifyRequests(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry,
      Collection<ModifyRequest> modifyRequests) throws LdappcException {

    boolean isError = false;

    for (ModifyRequest modifyRequest : modifyRequests) {

      ModifyResponse modifyResponse = consumer.getPsp().execute(modifyRequest);

      if (modifyResponse.getStatus().equals(StatusCode.SUCCESS)) {
        LOG.info("Ldappcng Consumer '{}' - Change log entry '{}' Modify '{}'", new Object[] { name,
            toString(changeLogEntry), PSPUtil.toString(modifyResponse) });
      } else {
        LOG.error("Ldappcng Consumer '{}' - Change log entry '{}' Modify failed '{}'", new Object[] { name,
            toString(changeLogEntry), PSPUtil.toString(modifyResponse) });
        isError = true;
      }
    }

    if (isError) {
      String message = "Ldappcng Consumer '" + name + "' - Change log entry '" + toString(changeLogEntry)
          + "' Modify failed";
      LOG.error(message);
      throw new LdappcException(message);
    }
  }

  /**
   * Create and execute a {@link SyncRequest}. The request identifier is retrieved from the {@link ChangeLogEntry} using
   * the {@link ChangeLogLabel}.
   * 
   * @param consumer the change log consumer
   * @param changeLogEntry the change log entry
   * @param changeLogLabel the change log label used to determine the sync request identifier
   * @throws LdappcException if an error occurs
   */
  public void executeSync(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry, ChangeLogLabel changeLogLabel)
      throws LdappcException {

    // will throw a RuntimeException on error
    String principalName = changeLogEntry.retrieveValueForLabel(changeLogLabel);

    SyncRequest syncRequest = new SyncRequest();
    syncRequest.setId(principalName);
    syncRequest.setRequestID(PSPUtil.uniqueRequestId());

    LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Will attempt to sync '{}'", new Object[] { name,
        toString(changeLogEntry), PSPUtil.toString(syncRequest) });

    SyncResponse syncResponse = consumer.getPsp().execute(syncRequest);

    if (syncResponse.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.info("Ldappcng Consumer '{}' - Change log entry '{}' Sync was successfull '{}'", new Object[] { name,
          toString(changeLogEntry), PSPUtil.toString(syncResponse) });
    } else {
      LOG.error("Ldappcng Consumer '{}' - Change log entry '{}' Sync failed '{}'", new Object[] { name,
          toString(changeLogEntry), PSPUtil.toString(syncResponse) });
      throw new LdappcException(PSPUtil.toString(syncResponse));
    }
  }

  /**
   * Run a full synchronization by executing a {@link BulkSyncRequest}.
   * 
   * @return the response
   */
  public synchronized Response fullSync() {

    LOG.info("Ldappcng Consumer '{}' - Starting full sync", name);

    fullSyncIsRunning = true;

    BulkSyncResponse response = psp.execute(new BulkSyncRequest());

    fullSyncIsRunning = false;

    if (response.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.info("Ldappcng Consumer '{}' - Full sync was successfull '{}'", name, PSPUtil.toString(response));
    } else {
      LOG.error("Ldappcng Consumer '{}' - Full sync was not successfull '{}'", name, PSPUtil.toString(response));
    }

    LOG.info("Ldappcng Consumer '{}' - Finished full sync", name);

    return response;
  }

  /** {@inheritDoc} */
  public long processChangeLogEntries(final List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {

    long currentId = -1;

    // initialize this consumer's name from the change log metadata
    if (name == null) {
      name = changeLogProcessorMetadata.getConsumerName();
      LOG.trace("Ldappcng Consumer '{}' - Setting name.", name);
    }

    LOG.debug("Ldappcng Consumer '{}' - Processing change log entry list size '{}'", name, changeLogEntryList.size());

    // try catch so we can track that we made some progress
    try {
      for (ChangeLogEntry changeLogEntry : changeLogEntryList) {
        // increment the current ids
        currentId = changeLogEntry.getSequenceNumber();

        // if full sync is running, return the previous sequence number to process this entry on the next run
        if (fullSyncIsRunning) {
          LOG.info("Ldappcng Consumer '{}' - Full sync is running, returning sequence number '{}'", name, currentId - 1);
          return currentId - 1;
        }

        // process the change log entry
        processChangeLogEntry(changeLogEntry);
      }
    } catch (Exception e) {
      String message = "Ldappcng Consumer '" + name + "' - An error occurred processing sequence number " + currentId;
      LOG.error(message, e);
      changeLogProcessorMetadata.registerProblem(e, message, currentId);
      changeLogProcessorMetadata.setHadProblem(true);
      changeLogProcessorMetadata.setRecordException(e);
      changeLogProcessorMetadata.setRecordExceptionSequence(currentId);
      // don't retry change log entry, we could loop indefinitely on error
      return currentId;
    }
    if (currentId == -1) {
      LOG.error("Ldappcng Consumer '" + name + "' - Unable to process any records.");
      throw new RuntimeException("Ldappcng Consumer '" + name + "' - Unable to process any records.");
    }

    LOG.debug("Ldappcng Consumer '{}' - Finished processing change log entries. Last sequence number '{}'", name,
        currentId);

    return currentId;
  }

  /**
   * Call the method of the {@link LdappcngEventType} enum which matches the {@link ChangeLogEntry} category and action
   * (the {@link ChangeLogType}).
   * 
   * @param changeLogEntry the change log entry
   * @throws Exception if an error occurs processing the change log entry
   */
  public void processChangeLogEntry(ChangeLogEntry changeLogEntry) throws Exception {

    try {
      // find the method to run via the enum
      String enumKey = changeLogEntry.getChangeLogType().getChangeLogCategory() + "__"
          + changeLogEntry.getChangeLogType().getActionName();

      LdappcngEventType ldappcEventType = LdappcngEventType.valueOf(enumKey);

      if (ldappcEventType == null) {
        LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Unsupported category and action.", name,
            toString(changeLogEntry));
      } else {
        // process the change log event
        LOG.info("Ldappcng Consumer '{}' - Change log entry '{}'", name, toStringDeep(changeLogEntry));
        ldappcEventType.process(this, changeLogEntry);
        LOG.info("Ldappcng Consumer '{}' - Change log entry '{}' Finished processing.", name, toString(changeLogEntry));
      }
    } catch (IllegalArgumentException e) {
      LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Unsupported category and action.", name,
          toString(changeLogEntry));
    }
  }

  /**
   * Add a group.
   * 
   * @param consumer the change log consumer
   * @param changeLogEntry the change log entry
   */
  public void processGroupAdd(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) {

    LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Processing group add.", name, toString(changeLogEntry));

    executeSync(consumer, changeLogEntry, ChangeLogLabels.GROUP_ADD.name);
  }

  /**
   * Delete a group.
   * 
   * @param consumer the change log consumer
   * @param changeLogEntry the change log entry
   */
  public void processGroupDelete(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) {

    LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Processing group delete.", name, toString(changeLogEntry));

    processDelete(consumer, changeLogEntry);
  }

  /**
   * Update a group.
   * 
   * @param consumer the change log consumer
   * @param changeLogEntry the change log entry
   * @throws LdappcException if an error occurs
   */
  public void processGroupUpdate(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) throws LdappcException {

    LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Processing group update.", name, toString(changeLogEntry));

    processUpdate(consumer, changeLogEntry, ChangeLogLabels.GROUP_UPDATE.name);
  }

  /**
   * Add a membership.
   * 
   * @param consumer the change log consumer
   * @param changeLogEntry the change log entry
   * @throws Spml2Exception if an spml error occurs
   */
  public void processMembershipAdd(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) throws Spml2Exception {

    LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Processing membership add.", name,
        toString(changeLogEntry));

    List<ModifyRequest> modifyRequests = consumer.processMembershipChange(consumer, changeLogEntry,
        ModificationMode.ADD);

    executeModifyRequests(consumer, changeLogEntry, modifyRequests);
  }

  /**
   * Delete a membership.
   * 
   * @param consumer the change log consumer
   * @param changeLogEntry the change log entry
   * @throws Spml2Exception if an spml error occurs
   */
  public void processMembershipDelete(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) throws Spml2Exception {

    LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Processing membership delete.", name,
        toString(changeLogEntry));

    List<ModifyRequest> modifyRequests = consumer.processMembershipChange(consumer, changeLogEntry,
        ModificationMode.DELETE);

    executeModifyRequests(consumer, changeLogEntry, modifyRequests);
  }

  /**
   * Add a stem.
   * 
   * @param consumer the change log consumer
   * @param changeLogEntry the change log entry
   */
  public void processStemAdd(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) {

    LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Processing group add.", name, toString(changeLogEntry));

    executeSync(consumer, changeLogEntry, ChangeLogLabels.STEM_ADD.name);
  }

  /**
   * Delete a stem.
   * 
   * @param consumer the change log consumer
   * @param changeLogEntry the change log entry
   */
  public void processStemDelete(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) {

    LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Processing stem delete.", name, toString(changeLogEntry));

    processDelete(consumer, changeLogEntry);
  }

  /**
   * Update a stem.
   * 
   * @param consumer the change log consumer
   * @param changeLogEntry the change log entry
   * @throws LdappcException if an error occurs
   */
  public void processStemUpdate(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) throws LdappcException {

    LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Processing stem update.", name, toString(changeLogEntry));

    processUpdate(consumer, changeLogEntry, ChangeLogLabels.STEM_UPDATE.name);
  }

  /**
   * Delete an object. The object identifiers to be deleted are calculated from the change log entry. For every object
   * to be deleted, a lookup is performed on the object identifier to determine if the object exists. If the object
   * exists, it is deleted.
   * 
   * @param consumer the change log consumer
   * @param changeLogEntry the change log entry
   * @throws LdappcException if an error occurs deleting the object
   */
  public void processDelete(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry) throws LdappcException {

    // calculate the psoID to be deleted from the change log entry
    CalcRequest calcRequest = new CalcRequest();
    calcRequest.setId(ChangeLogDataConnector.principalName(changeLogEntry.getSequenceNumber()));
    calcRequest.setReturnData(ReturnData.IDENTIFIER);
    calcRequest.setRequestID(PSPUtil.uniqueRequestId());

    CalcResponse calcResponse = consumer.getPsp().execute(calcRequest);

    if (!calcResponse.getStatus().equals(StatusCode.SUCCESS)) {
      LOG.error("Ldappcng Consumer '{}' - Calc request '{}' failed {}", new Object[] { name, calcRequest.toString(),
          PSPUtil.toString(calcResponse) });
      throw new LdappcException(PSPUtil.toString(calcResponse));
    }

    List<PSO> psos = calcResponse.getPSOs();

    if (psos.isEmpty()) {
      LOG.warn("Ldappcng Consumer '{}' - Change log entry '{}' Unable to calculate identifier.", name,
          toString(changeLogEntry));
      return;
    }

    for (PSO pso : psos) {
      // lookup object to see if it exists
      LookupRequest lookupRequest = new LookupRequest();
      lookupRequest.setPsoID(pso.getPsoID());
      lookupRequest.setRequestID(PSPUtil.uniqueRequestId());
      lookupRequest.setReturnData(ReturnData.IDENTIFIER);
      LookupResponse lookupResponse = consumer.getPsp().execute(lookupRequest);

      if (!lookupResponse.getStatus().equals(StatusCode.SUCCESS)) {
        LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Identifier '{}' does not exist.", new Object[] {
            name, toString(changeLogEntry), PSPUtil.toString(pso.getPsoID()) });
        continue;
      }

      DeleteRequest deleteRequest = new DeleteRequest();
      deleteRequest.setPsoID(pso.getPsoID());
      deleteRequest.setRequestID(PSPUtil.uniqueRequestId());

      DeleteResponse deleteResponse = consumer.getPsp().execute(deleteRequest);

      if (deleteResponse.getStatus().equals(StatusCode.SUCCESS)) {
        LOG.info("Ldappcng Consumer '{}' - Change log entry '{}' Delete '{}'", new Object[] { name,
            toString(changeLogEntry), PSPUtil.toString(deleteResponse) });
      } else {
        LOG.error("Ldappcng Consumer '{}' - Change log entry '{}' Delete failed '{}'", new Object[] { name,
            toString(changeLogEntry), PSPUtil.toString(deleteResponse) });
        throw new LdappcException(PSPUtil.toString(deleteResponse));
      }
    }
  }

  /**
   * Return a {@link ModifyRequest} for every {@link PSO} whose references need to be modified. Each modify request has
   * a single {@link Modification} consisting of the {@link Reference}s which need to be added or deleted.
   * 
   * @param changeLogEntry the change log entry
   * @param modificationMode the modification mode, either add or delete
   * @return the possibly empty list of modify requests
   * @throws Spml2Exception if an SPML error occurs
   */
  public List<ModifyRequest> processMembershipChange(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry,
      ModificationMode modificationMode) throws Spml2Exception {

    List<ModifyRequest> modifyRequests = new ArrayList<ModifyRequest>();

    CalcRequest calcRequest = new CalcRequest();
    calcRequest.setId(ChangeLogDataConnector.principalName(changeLogEntry.getSequenceNumber()));
    calcRequest.setRequestID(PSPUtil.uniqueRequestId());

    CalcResponse calcResponse = consumer.getPsp().execute(calcRequest);

    for (PSO pso : calcResponse.getPSOs()) {

      List<Reference> references = processMembershipChangeReferences(pso, modificationMode);
      if (references.isEmpty()) {
        continue;
      }

      ModifyRequest modifyRequest = new ModifyRequest();
      modifyRequest.setRequestID(PSPUtil.uniqueRequestId());
      modifyRequest.setPsoID(pso.getPsoID());
      modifyRequest.addOpenContentAttr(PSODefinition.ENTITY_NAME_ATTRIBUTE,
          pso.findOpenContentAttrValueByName(PSODefinition.ENTITY_NAME_ATTRIBUTE));

      Modification modification = new Modification();
      modification.setModificationMode(modificationMode);
      CapabilityData capabilityData = PSPUtil.fromReferences(references);
      modification.addCapabilityData(capabilityData);
      modifyRequest.addModification(modification);

      modifyRequests.add(modifyRequest);
    }

    return modifyRequests;
  }

  /**
   * Return the {@link Reference}s which need to be added or deleted to the {@link PSO}.
   * 
   * A {@link HasReference} query is performed to determine if each {@link Reference} exists.
   * 
   * @param pso the provisioned object
   * @param modificationMode the modification mode, either add or delete
   * @return the possibly empty list of references
   * @throws Spml2Exception if an SPML error occurs
   */
  public List<Reference> processMembershipChangeReferences(PSO pso, ModificationMode modificationMode)
      throws Spml2Exception {

    // the references which need to be modified
    List<Reference> references = new ArrayList<Reference>();

    // references from the pso
    Map<String, List<Reference>> referenceMap = PSP.getReferences(pso.getCapabilityData());

    // for every type of reference, i.e. the attribute name
    for (String typeOfReference : referenceMap.keySet()) {
      // for every reference
      for (Reference reference : referenceMap.get(typeOfReference)) {

        // perform a search to determine if the reference exists
        boolean hasReference = psp.hasReference(pso.getPsoID(), reference);

        // if adding reference and reference does not exist, modify
        if (modificationMode.equals(ModificationMode.ADD) && !hasReference) {
          references.add(reference);
        }

        // if deleting reference and reference exists, modify
        if (modificationMode.equals(ModificationMode.DELETE) && hasReference) {
          references.add(reference);
        }
      }
    }

    return references;
  }

  /**
   * Process an object update. If the object should be renamed, attempt to rename, otherwise execute a
   * {@link SyncRequest}.
   * 
   * If the attempt to rename the object fails with the {@link ERROR_SUBTREE_RENAME_NOT_SUPPORTED} error, then attempt
   * to sync the object.
   * 
   * @param consumer the change log consumer
   * @param changeLogEntry the change log entry
   * @param principalNameLabel the change log label used to determine the sync request identifier
   * @throws LdappcException if an error occurs
   */
  public void processUpdate(LdappcngConsumer consumer, ChangeLogEntry changeLogEntry, ChangeLogLabel principalNameLabel)
      throws LdappcException {

    LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Processing object update.", name,
        toString(changeLogEntry));

    CalcRequest calcRequest = new CalcRequest();
    calcRequest.setId(ChangeLogDataConnector.principalName(changeLogEntry.getSequenceNumber()));
    calcRequest.setReturnData(ReturnData.EVERYTHING);
    calcRequest.setRequestID(PSPUtil.uniqueRequestId());

    CalcResponse calcResponse = consumer.getPsp().execute(calcRequest);

    if (!calcResponse.getStatus().equals(StatusCode.SUCCESS)) {
      if (calcResponse.getError().equals(ErrorCode.NO_SUCH_IDENTIFIER)) {
        // OK just could not calculate identifier;
        LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Could not calculate identifier '{}'", new Object[] {
            name, toString(changeLogEntry), PSPUtil.toString(calcResponse) });
        return;
      } else {
        LOG.error("Ldappcng Consumer '{}' - Change log entry '{}' Processing object update failed '{}'", new Object[] {
            name, toString(changeLogEntry), PSPUtil.toString(calcResponse) });
        throw new LdappcException(PSPUtil.toString(calcResponse));
      }
    }

    List<PSO> psos = calcResponse.getPSOs();

    if (psos.isEmpty()) {
      LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Nothing to provision.", name, toString(changeLogEntry));
      return;
    }

    boolean isError = false;

    for (PSO pso : psos) {

      // calculate rename
      ModifyRequest modifyRequest = consumer.getPsp().renameRequest(pso);

      if (modifyRequest == null) {
        // not renaming, attempt to sync
        try {
          executeSync(consumer, changeLogEntry, principalNameLabel);
        } catch (LdappcException e) {
          isError = true;
        }
      } else {
        // attempt to rename
        LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Will attempt to rename '{}'", new Object[] { name,
            toString(changeLogEntry), PSPUtil.toString(modifyRequest) });

        ModifyResponse modifyResponse = consumer.getPsp().execute(modifyRequest);

        if (modifyResponse.getStatus().equals(StatusCode.SUCCESS)) {
          LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Rename was successfull '{}'", new Object[] { name,
              toString(changeLogEntry), PSPUtil.toString(modifyResponse) });
        } else {
          LOG.error("Ldappcng Consumer '{}' - Change log entry '{}' Rename failed '{}'", new Object[] { name,
              toString(changeLogEntry), PSPUtil.toString(modifyResponse) });

          // if target (e.g. openldap with bdb backend) returns subtree rename not supported error, sync
          if (modifyResponse.getError().equals(ErrorCode.CUSTOM_ERROR)) {
            List<String> errorMessages = Arrays.asList(modifyResponse.getErrorMessages());
            if (errorMessages.size() == 1 && errorMessages.get(0).equals(ERROR_SUBTREE_RENAME_NOT_SUPPORTED)) {
              try {
                LOG.info("Ldappcng Consumer '{}' - Change log entry '{}' Rename failed, attempting to sync.", name,
                    toString(changeLogEntry));
                executeSync(consumer, changeLogEntry, principalNameLabel);
              } catch (LdappcException e) {
                isError = true;
              }
              continue;
            }
          }

          isError = true;
        }
      }
    }

    if (isError) {
      String message = "Ldappcng Consumer '" + name + "' - Change log entry '" + toString(changeLogEntry)
          + "' Update failed";
      LOG.error(message);
      throw new LdappcException(message);
    }

    LOG.debug("Ldappcng Consumer '{}' - Change log entry '{}' Processing object update was successfull.", name,
        toString(changeLogEntry));
  }

  /** @see ChangeLogDataConnector#toString(ChangeLogEntry) */
  public static String toString(ChangeLogEntry changeLogEntry) {
    return ChangeLogDataConnector.toString(changeLogEntry);
  }

  /** @see ChangeLogDataConnector#toStringDeep(ChangeLogEntry) */
  public static String toStringDeep(ChangeLogEntry changeLogEntry) {
    return ChangeLogDataConnector.toStringDeep(changeLogEntry);
  }
}
