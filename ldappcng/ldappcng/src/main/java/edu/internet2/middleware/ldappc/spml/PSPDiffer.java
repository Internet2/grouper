/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.CapabilityData;
import org.openspml.v2.msg.spml.ErrorCode;
import org.openspml.v2.msg.spml.LookupRequest;
import org.openspml.v2.msg.spml.LookupResponse;
import org.openspml.v2.msg.spml.Modification;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spml.PSO;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.StatusCode;
import org.openspml.v2.msg.spmlref.Reference;
import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.profiles.dsml.DSMLModification;
import org.openspml.v2.profiles.dsml.DSMLProfileException;
import org.openspml.v2.profiles.dsml.DSMLValue;
import org.openspml.v2.util.Spml2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.definitions.PSOAttributeDefinition;
import edu.internet2.middleware.ldappc.spml.definitions.PSODefinition;
import edu.internet2.middleware.ldappc.spml.definitions.TargetDefinition;
import edu.internet2.middleware.ldappc.spml.request.CalcRequest;
import edu.internet2.middleware.ldappc.spml.request.CalcResponse;
import edu.internet2.middleware.ldappc.spml.request.DiffRequest;
import edu.internet2.middleware.ldappc.spml.request.DiffResponse;
import edu.internet2.middleware.ldappc.spml.request.SynchronizedResponse;
import edu.internet2.middleware.ldappc.synchronize.AttributeModifier;
import edu.internet2.middleware.ldappc.util.PSPUtil;

/**
 * This class, which diffs SPML Provisioning Service Objects, suffers from a lack of a thoroughly considered design.
 * Most methods existed previously in the Provisioning Service Provider, but were moved into their own class once the
 * number of method arguments exceeded three. Easy to read diff logic is a must for a provisioner; this is an incomplete
 * attempt. Perhaps the objects should diff themselves, since only they know their data.
 */

// TODO Most of these methods assume an <code>PSODefinition.ENTITY_NAME_ATTRIBUTE</code>
// FUTURE DSMLModifications assumed

public class PSPDiffer {

  /** logger */
  private static final Logger LOG = LoggerFactory.getLogger(PSPDiffer.class);

  /** The Provisioning Service Provider */
  private PSP psp;

  /** The diff request being processed. */
  private DiffRequest diffRequest;

  /** The result of the processing of the diff request. */
  private DiffResponse diffResponse;

  /**
   * Calculate the changes necessary to provision an object specified in the given <code>DiffRequest</code>.
   * 
   * @param psp the <code>ProvisioningServiceProvider</code>
   * @param diffRequest the <code>DiffRequest</code> to be processed
   * @param diffResponse the result of the processing of the <code>DiffRequest</code>
   * 
   */
  public PSPDiffer(PSP psp, DiffRequest diffRequest, DiffResponse diffResponse) {
    this.psp = psp;
    this.diffRequest = diffRequest;
    this.diffResponse = diffResponse;
  }

  /**
   * Process the <code>DiffRequest</code>. The result is the <code>DiffResponse</code>.
   */
  public void diff() {

    diffResponse.setId(diffRequest.getId());

    // Calculate how the id should be provisioned.
    CalcRequest calcRequest = new CalcRequest();
    calcRequest.setId(diffRequest.getId());
    calcRequest.setRequestID(psp.generateRequestID());
    calcRequest.setReturnData(diffRequest.getReturnData());
    calcRequest.setSchemaEntities(diffRequest.getSchemaEntities());
    CalcResponse calcResponse = psp.execute(calcRequest);

    if (calcResponse.getStatus().equals(StatusCode.FAILURE)) {
      psp.fail(diffResponse, calcResponse.getError(), calcResponse.getErrorMessages());
      return;
    }

    for (PSO correctPSO : calcResponse.getPSOs()) {

      // Lookup a PSO Identifier to see how it is provisioned.
      LookupRequest lookupRequest = new LookupRequest();
      lookupRequest.setPsoID(correctPSO.getPsoID());
      lookupRequest.setRequestID(psp.generateRequestID());
      lookupRequest.setReturnData(diffRequest.getReturnData());

      LookupResponse lookupResponse = psp.execute(lookupRequest);

      try {
        if (PSP.doesIdentifierExist(lookupResponse)) {
          // if identifier exists, diff
          PSO currentPSO = lookupResponse.getPso();
          diff(correctPSO, currentPSO);

        } else {
          // if identifier does not exist, do we need to rename ?
          ModifyRequest modifyRequest = psp.renameRequest(correctPSO);

          // if modify request is not null, rename
          if (modifyRequest != null) {
            diffResponse.addRequest(modifyRequest);
          } else {
            // if not renaming, add
            AddRequest addRequest = psp.add(correctPSO, diffRequest.getReturnData());
            diffResponse.addRequest(addRequest);
          }
        }
      } catch (Spml2Exception e) {
        psp.fail(diffResponse, ErrorCode.CUSTOM_ERROR, e);
      } catch (LdappcException e) {
        psp.fail(diffResponse, ErrorCode.CUSTOM_ERROR, e);
      }
    }
  }

  private void diff(PSO correctPSO, PSO currentPSO) throws LdappcException, Spml2Exception {

    List<ModifyRequest> modifyRequests = diff(correctPSO, currentPSO, true);

    if (modifyRequests.isEmpty()) {

      SynchronizedResponse synchronizedResponse = new SynchronizedResponse();
      synchronizedResponse.setPsoID(currentPSO.getPsoID());
      diffResponse.addResponse(synchronizedResponse);

    } else {

      for (ModifyRequest modifyRequest : modifyRequests) {
        modifyRequest.setReturnData(diffRequest.getReturnData());
        diffResponse.addRequest(modifyRequest);
      }
    }
  }

  /**
   * Diff the data and reference capability data of two Provisioning Service Objects.
   * 
   * @param correctPSO the representation of the PSO as it should be
   * @param currentPSO the representation of the PSO as it is
   * @return the <code>ModifyRequests</code> which would make the currentPSO identical to the correctPSO
   * @throws LdappcException if the Provisioning Service Objects do not have the same identifier or schema entity name,
   *           the latter requires a <code>PSODefinition.ENTITY_NAME_ATTRIBUTE</code>. This is not ideal.
   * @throws Spml2Exception
   */
  private List<ModifyRequest> diff(PSO correctPSO, PSO currentPSO, boolean psoIDMustMatch) throws LdappcException,
      Spml2Exception {

    List<ModifyRequest> modifyRequests = new ArrayList<ModifyRequest>();

    if (psoIDMustMatch && !correctPSO.getPsoID().equals(currentPSO.getPsoID())) {
      LOG.error("Unable to diff objects with different identifiers : '{}' and '{}'",
          PSPUtil.getString(correctPSO.getPsoID()), PSPUtil.toString(currentPSO.getPsoID()));
      throw new LdappcException("Unable to diff objects with different identifiers.");
    }

    // entityName
    String correctEntityName = correctPSO.findOpenContentAttrValueByName(PSODefinition.ENTITY_NAME_ATTRIBUTE);
    String currentEntityName = currentPSO.findOpenContentAttrValueByName(PSODefinition.ENTITY_NAME_ATTRIBUTE);
    if (!correctEntityName.equals(currentEntityName)) {
      LOG.error("Unable to diff objects with different entityNames : '{}' and '{}'", correctEntityName,
          currentEntityName);
      throw new LdappcException("Unable to diff objects with different entityNames.");
    }

    List<Modification> dataMods = this.diffData(correctPSO, currentPSO);

    List<Modification> referenceMods = this.diffReferences(correctPSO, currentPSO);

    if (dataMods.isEmpty() && referenceMods.isEmpty()) {
      return modifyRequests;
    }

    if (psp.getTargetDefinitions().get(correctPSO.getPsoID().getTargetID()).isBundleModifications()) {
      ModifyRequest modifyRequest = new ModifyRequest();
      modifyRequest.setRequestID(psp.generateRequestID());
      modifyRequest.setPsoID(correctPSO.getPsoID());
      if (correctEntityName != null) {
        modifyRequest.addOpenContentAttr(PSODefinition.ENTITY_NAME_ATTRIBUTE, correctEntityName);
      }
      for (Modification modification : dataMods) {
        modifyRequest.addModification(modification);
      }
      for (Modification modification : referenceMods) {
        modifyRequest.addModification(modification);
      }
      modifyRequests.add(modifyRequest);
    } else {
      modifyRequests.addAll(this.unbundleDataModifications(dataMods, correctPSO.getPsoID(), correctEntityName));
      modifyRequests
          .addAll(this.unbundleReferenceModifications(referenceMods, correctPSO.getPsoID(), correctEntityName));
    }

    return modifyRequests;
  }

  /**
   * Diff the data of two Provisioning Service Objects. @see #diff(PSO, PSO)
   * 
   * @param correctPSO the representation of the PSO as it should be
   * @param currentPSO the representation of the PSO as it is
   * @return the <code>ModifyRequests</code> which would make the currentPSO identical to the correctPSO
   * @throws DSMLProfileException if an error occurs determining the <code>ModifyRequest</code>s
   * @throws LdappcException if the Provisioning Service Objects do not have an
   *           <code>PSODefinition.ENTITY_NAME_ATTRIBUTE</code>
   */
  private List<Modification> diffData(PSO correctPSO, PSO currentPSO) throws DSMLProfileException {
    List<Modification> modifications = new ArrayList<Modification>();

    if (!(diffRequest.getReturnData().equals(ReturnData.DATA) || diffRequest.getReturnData().equals(
        ReturnData.EVERYTHING))) {
      return modifications;
    }

    Map<String, DSMLAttr> currentDsmlAttrs = PSP.getDSMLAttrMap(currentPSO.getData());
    Map<String, DSMLAttr> correctDsmlAttrs = PSP.getDSMLAttrMap(correctPSO.getData());

    Set<String> attrNames = new LinkedHashSet<String>();
    attrNames.addAll(correctDsmlAttrs.keySet());
    attrNames.addAll(currentDsmlAttrs.keySet());

    // determine the schema entity, assume pso IDs are the same for each pso, where do we
    // check this ?
    String targetId = currentPSO.getPsoID().getTargetID();
    TargetDefinition targetDefinition = psp.getTargetDefinitions().get(targetId);
    String entityName = currentPSO.findOpenContentAttrValueByName(PSODefinition.ENTITY_NAME_ATTRIBUTE);
    PSODefinition psoDefinition = targetDefinition.getPSODefinition(entityName);

    for (String attrName : attrNames) {
      PSOAttributeDefinition psoAttributeDefinition = psoDefinition.getAttributeDefinition(attrName);
      if (psoAttributeDefinition == null) {
        LOG.error("Unknown psoAttributeDefinition '" + attrName + "'");
        throw new LdappcException("Unknown psoAttributeDefinition '" + attrName + "'");
      }
    }

    for (String attrName : attrNames) {
      DSMLAttr currentDsmlAttr = currentDsmlAttrs.get(attrName);
      DSMLAttr correctDsmlAttr = correctDsmlAttrs.get(attrName);

      AttributeModifier attributeModifier = new AttributeModifier(attrName, true);

      if (currentDsmlAttr != null) {
        attributeModifier.initDSML(currentDsmlAttr.getValues());

        PSOAttributeDefinition psoAttributeDefinition = psoDefinition.getAttributeDefinition(attrName);
        if (psoAttributeDefinition.isRetainAll()) {
          attributeModifier.retainAll();
        }
      }

      if (correctDsmlAttr != null) {
        attributeModifier.store(correctDsmlAttr.getValues());
      }

      modifications.addAll(attributeModifier.getDSMLModification());
    }

    return modifications;
  }

  /**
   * Diff the reference capability data of two Provisioning Service Objects. @see #diff(PSO, PSO)
   * 
   * @param correctPSO the representation of the PSO as it should be
   * @param currentPSO the representation of the PSO as it is
   * @return the <code>ModifyRequests</code> which would make the currentPSO identical to the correctPSO
   * @throws Spml2Exception if an error occurs determining the <code>ModifyRequest</code>s
   */
  private List<Modification> diffReferences(PSO correctPSO, PSO currentPSO) throws Spml2Exception {
    List<Modification> modifications = new ArrayList<Modification>();

    if (!diffRequest.getReturnData().equals(ReturnData.EVERYTHING)) {
      return modifications;
    }

    Map<String, List<Reference>> correctReferenceMap = PSP.getReferences(correctPSO.getCapabilityData());
    Map<String, List<Reference>> currentReferenceMap = PSP.getReferences(currentPSO.getCapabilityData());

    Set<String> typeOfReferences = new LinkedHashSet<String>();
    typeOfReferences.addAll(correctReferenceMap.keySet());
    typeOfReferences.addAll(currentReferenceMap.keySet());

    for (String typeOfReference : typeOfReferences) {
      List<Reference> currentReferences = currentReferenceMap.get(typeOfReference);
      List<Reference> correctReferences = correctReferenceMap.get(typeOfReference);

      AttributeModifier attributeModifier = new AttributeModifier(typeOfReference, true);

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

  /**
   * Return a <code>ModifyRequest</code> for every data <code>Modification</code>.
   * 
   * @param dataMods the <code>Modification</code>s
   * @param psoID the PSO Identifier
   * @param entityName the schema entity name
   * @return the <code>ModifyRequest</code>s
   * @throws Spml2Exception if an error occurs creating the <code>DSMLModification</code>s
   * 
   */
  private List<ModifyRequest> unbundleDataModifications(List<Modification> dataMods, PSOIdentifier psoID,
      String entityName) throws Spml2Exception {
    List<ModifyRequest> unbundledModifyRequests = new ArrayList<ModifyRequest>();

    for (Modification modification : dataMods) {

      for (Object object : modification.getOpenContentElements(DSMLModification.class)) {
        DSMLModification dsmlModification = (DSMLModification) object;
        DSMLValue[] dsmlValues = dsmlModification.getValues();
        for (DSMLValue dsmlValue : dsmlValues) {
          ModifyRequest unbundledModifyRequest = new ModifyRequest();
          unbundledModifyRequest.setRequestID(psp.generateRequestID());
          unbundledModifyRequest.setPsoID(psoID);
          if (entityName != null) {
            unbundledModifyRequest.addOpenContentAttr(PSODefinition.ENTITY_NAME_ATTRIBUTE, entityName);
          }
          DSMLModification dsmlMod = new DSMLModification(dsmlModification.getName(), new DSMLValue[] { dsmlValue },
              dsmlModification.getOperation());
          Modification unbundledModification = new Modification();
          unbundledModification.setModificationMode(modification.getModificationMode());
          unbundledModification.addOpenContentElement(dsmlMod);
          unbundledModifyRequest.addModification(unbundledModification);
          unbundledModifyRequests.add(unbundledModifyRequest);
        }
      }
    }

    return unbundledModifyRequests;
  }

  /**
   * Return a <code>ModifyRequest</code> for every reference capability data <code>Modification</code>.
   * 
   * @param referenceMods the <code>Modification</code>s
   * @param psoID the PSO Identifier
   * @param the schema entity name
   * @return the <code>ModifyRequest</code>s
   * @throws Spml2Exception if an error occurs creating the <code>DSMLModification</code>s
   */
  private List<ModifyRequest> unbundleReferenceModifications(List<Modification> referenceMods, PSOIdentifier psoID,
      String entityName) throws Spml2Exception {
    List<ModifyRequest> unbundledModifyRequests = new ArrayList<ModifyRequest>();

    for (Modification modification : referenceMods) {
      Map<String, List<Reference>> references = PSP.getReferences(modification.getCapabilityData());
      for (String typeOfReference : references.keySet()) {
        for (Reference reference : references.get(typeOfReference)) {
          ModifyRequest unbundledModifyRequest = new ModifyRequest();
          unbundledModifyRequest.setRequestID(psp.generateRequestID());
          unbundledModifyRequest.setPsoID(psoID);
          if (entityName != null) {
            unbundledModifyRequest.addOpenContentAttr(PSODefinition.ENTITY_NAME_ATTRIBUTE, entityName);
          }
          CapabilityData capabilityData = PSPUtil.fromReferences(Arrays.asList(new Reference[] { reference }));
          Modification unbundledModification = new Modification();
          unbundledModification.addCapabilityData(capabilityData);
          unbundledModification.setModificationMode(modification.getModificationMode());
          unbundledModifyRequest.addModification(unbundledModification);
          unbundledModifyRequests.add(unbundledModifyRequest);
        }
      }
    }

    return unbundledModifyRequests;
  }
}
