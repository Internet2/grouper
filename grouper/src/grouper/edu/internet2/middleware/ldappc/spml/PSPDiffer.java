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

import edu.internet2.middleware.grouper.util.GrouperUtil;
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
 *
 */
public class PSPDiffer {

  private static final Logger LOG = GrouperUtil.getLogger(PSPDiffer.class);

  private PSP psp;

  private DiffRequest diffRequest;

  private DiffResponse diffResponse;

  public PSPDiffer(PSP psp, DiffRequest diffRequest, DiffResponse diffResponse) {
    this.psp = psp;
    this.diffRequest = diffRequest;
    this.diffResponse = diffResponse;
  }

  public void diff() {
    diffResponse.setId(diffRequest.getId());

    // calculate how the given id should be provisioned
    CalcRequest calcRequest = new CalcRequest();
    psp.setRequestId(calcRequest);
    calcRequest.setId(diffRequest.getId());
    calcRequest.setTargetIds(diffRequest.getTargetIds());
    calcRequest.setReturnData(diffRequest.getReturnData());

    CalcResponse calcResponse = psp.execute(calcRequest);

    if (calcResponse.getStatus().equals(StatusCode.FAILURE)) {
      psp.fail(diffResponse, calcResponse.getError(), calcResponse.getErrorMessages());
    } else {
      for (PSO pso : calcResponse.getPSOs()) {
        this.diff(pso);
      }
    }
  }

  private void diff(PSO correctPSO) {

    // lookup pso to see if and/or how it is provisioned
    LookupRequest lookupRequest = new LookupRequest();
    lookupRequest.setPsoID(correctPSO.getPsoID());
    lookupRequest.setReturnData(diffRequest.getReturnData());
    psp.setRequestId(lookupRequest);

    LookupResponse lookupResponse = psp.execute(lookupRequest);

    if (lookupResponse.getStatus().equals(StatusCode.FAILURE)) {
      if (lookupResponse.getError().equals(ErrorCode.NO_SUCH_IDENTIFIER)) {
        // pso should be added to target
        diffResponse.addRequest(psp.add(correctPSO, diffRequest.getReturnData()));
      } else {
        // any other error is a failure
        psp.fail(diffResponse, lookupResponse.getError(), "Lookup request failed.");
      }
      return;
    }

    try {
      PSO currentPSO = lookupResponse.getPso();
      List<ModifyRequest> modifyRequests = this.diff(correctPSO, currentPSO);
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
    } catch (LdappcException e) {
      psp.fail(diffResponse, ErrorCode.CUSTOM_ERROR, e);
    } catch (Spml2Exception e) {
      psp.fail(diffResponse, ErrorCode.CUSTOM_ERROR, e);
    }
  }

  private List<ModifyRequest> diff(PSO correctPSO, PSO currentPSO) throws LdappcException,
      Spml2Exception {

    List<ModifyRequest> modifyRequests = new ArrayList<ModifyRequest>();

    if (!correctPSO.getPsoID().equals(currentPSO.getPsoID())) {
      LOG.error("Unable to diff objects with different identifiers : '{}' and '{}'", PSPUtil.getString(correctPSO
                .getPsoID()), PSPUtil.toString(currentPSO.getPsoID()));
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
      psp.setRequestId(modifyRequest);
      modifyRequests.add(modifyRequest);
    } else {
      modifyRequests.addAll(this.unbundleDataModifications(dataMods, correctPSO.getPsoID(), correctEntityName));
      modifyRequests.addAll(this.unbundleReferenceModifications(referenceMods, correctPSO.getPsoID(),
                correctEntityName));
    }

    return modifyRequests;
  }

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

    // determine the schema entity, assume pso IDs are the same for each pso
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

  public List<ModifyRequest> unbundleDataModifications(List<Modification> dataMods, PSOIdentifier psoID,
      String entityName) throws Spml2Exception {
    List<ModifyRequest> unbundledModifyRequests = new ArrayList<ModifyRequest>();

    for (Modification modification : dataMods) {

      for (Object object : modification.getOpenContentElements(DSMLModification.class)) {
        DSMLModification dsmlModification = (DSMLModification) object;
        DSMLValue[] dsmlValues = dsmlModification.getValues();
        for (DSMLValue dsmlValue : dsmlValues) {
          ModifyRequest unbundledModifyRequest = new ModifyRequest();
          psp.setRequestId(unbundledModifyRequest);
          unbundledModifyRequest.setPsoID(psoID);
          if (entityName != null) {
            unbundledModifyRequest.addOpenContentAttr(PSODefinition.ENTITY_NAME_ATTRIBUTE, entityName);
          }
          DSMLModification dsmlMod = new DSMLModification(dsmlModification.getName(),
                      new DSMLValue[] { dsmlValue }, dsmlModification.getOperation());
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

  public List<ModifyRequest> unbundleReferenceModifications(List<Modification> referenceMods,
      PSOIdentifier psoID,
      String entityName) throws Spml2Exception {
    List<ModifyRequest> unbundledModifyRequests = new ArrayList<ModifyRequest>();

    for (Modification modification : referenceMods) {
      Map<String, List<Reference>> references = PSP.getReferences(modification.getCapabilityData());
      for (String typeOfReference : references.keySet()) {
        for (Reference reference : references.get(typeOfReference)) {
          ModifyRequest unbundledModifyRequest = new ModifyRequest();
          psp.setRequestId(unbundledModifyRequest);
          unbundledModifyRequest.setPsoID(psoID);
          if (entityName != null) {
            unbundledModifyRequest.addOpenContentAttr(PSODefinition.ENTITY_NAME_ATTRIBUTE, entityName);
          }
          CapabilityData capabilityData = PSPUtil
                      .fromReferences(Arrays.asList(new Reference[] { reference }));
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
