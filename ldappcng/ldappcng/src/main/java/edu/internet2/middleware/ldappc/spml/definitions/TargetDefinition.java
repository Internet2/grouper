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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openspml.v2.msg.OCEtoMarshallableAdapter;
import org.openspml.v2.msg.spml.CapabilitiesList;
import org.openspml.v2.msg.spml.Capability;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.Schema;
import org.openspml.v2.msg.spml.SchemaEntityRef;
import org.openspml.v2.msg.spml.Target;
import org.openspml.v2.msg.spmlref.ReferenceDefinition;
import org.openspml.v2.profiles.DSMLProfileRegistrar;
import org.openspml.v2.profiles.spmldsml.AttributeDefinition;
import org.openspml.v2.profiles.spmldsml.AttributeDefinitionReference;
import org.openspml.v2.profiles.spmldsml.AttributeDefinitionReferences;
import org.openspml.v2.profiles.spmldsml.DSMLSchema;
import org.openspml.v2.profiles.spmldsml.ObjectClassDefinition;
import org.openspml.v2.util.Spml2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.ldappc.spml.provider.SpmlTargetProvider;

public class TargetDefinition {

  private static final Logger LOG = LoggerFactory.getLogger(TargetDefinition.class);

  private String id;

  private SpmlTargetProvider provider;

  private List<PSODefinition> psoDefinitions;

  private Target target;

  private boolean bundleModifications;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public SpmlTargetProvider getProvider() {
    return provider;
  }

  public void setProvider(SpmlTargetProvider provider) {
    this.provider = provider;
  }

  /**
   * 
   * @param id
   * @return the PSO definition with the given id or null
   */
  public PSODefinition getPSODefinition(String id) {
    for (PSODefinition psoDefinition : psoDefinitions) {
      if (psoDefinition.getId().equals(id)) {
        return psoDefinition;
      }
    }
    return null;
  }

  public List<PSODefinition> getPsoDefinitions() {
    return psoDefinitions;
  }

  public void setPsoDefinitions(List<PSODefinition> psoDefinitions) {
    this.psoDefinitions = psoDefinitions;
  }

  public Set<String> getAttributeIds() {
    return null;
  }

  public Set<String> getNames(ReturnData returnData) {
    Set<String> names = new LinkedHashSet<String>();
    for (PSODefinition psoDefinition : psoDefinitions) {
      IdentifyingAttribute ia = psoDefinition.getPsoIdentifierDefinition().getIdentifyingAttribute();
      if (ia != null) {
        names.add(ia.getName());
      }
      if (returnData.equals(ReturnData.DATA) || returnData.equals(ReturnData.EVERYTHING)) {
        names.addAll(psoDefinition.getAttributeNames());
      }
      if (returnData.equals(ReturnData.EVERYTHING)) {
        names.addAll(psoDefinition.getReferenceNames());
      }
    }
    return names;
  }

  public Target getTarget() throws Spml2Exception {

    if (target != null) {
      return target;
    }

    target = new Target();
    target.setTargetID(getId());
    // FUTURE support XSD ?
    target.setProfile(new DSMLProfileRegistrar().getProfileURI());

    Schema schema = new Schema();

    // FUTURE support other schemas ?
    DSMLSchema dsmlSchema = new DSMLSchema();

    CapabilitiesList cl = new CapabilitiesList();

    LinkedHashMap<String, SchemaEntityRef> schemaEntityRefMap = new LinkedHashMap<String, SchemaEntityRef>();

    for (PSODefinition psoDefinition : this.getPsoDefinitions()) {
      SchemaEntityRef entity = new SchemaEntityRef();
      entity.setEntityName(psoDefinition.getId());
      entity.setTargetID(getId());
      schemaEntityRefMap.put(entity.getEntityName(), entity);

      schema.addSupportedSchemaEntity(entity);

      ObjectClassDefinition objectClassDef = new ObjectClassDefinition();
      objectClassDef.setName(psoDefinition.getId());

      AttributeDefinitionReferences attrRefs = new AttributeDefinitionReferences();

      for (PSOAttributeDefinition psoAttributeDefinition : psoDefinition.getAttributeDefinitions()) {
        AttributeDefinition attrDef = new AttributeDefinition();
        attrDef.setName(psoAttributeDefinition.getName());
        dsmlSchema.addAttributeDefinition(attrDef);

        AttributeDefinitionReference attrDefRef = new AttributeDefinitionReference();
        attrDefRef.setName(psoAttributeDefinition.getName());
        // FUTURE attrRef.setRequired(required);
        attrRefs.addAttributeDefinitionReference(attrDefRef);
      }

      objectClassDef.setMemberAttributes(attrRefs);
      dsmlSchema.addObjectClassDefinition(objectClassDef);
    }

    for (PSODefinition psoDefinition : this.getPsoDefinitions()) {
      for (PSOReferencesDefinition psoReferencesDefinition : psoDefinition.getReferenceDefinitions()) {
        for (PSOReferenceDefinition psoReferenceDefinition : psoReferencesDefinition.getPsoReferenceDefinitions()) {
          SchemaEntityRef fromEntity = schemaEntityRefMap.get(psoDefinition.getId());
          SchemaEntityRef toEntity = schemaEntityRefMap.get(psoReferenceDefinition.getToPSODefinition().getId());

          Capability capability = new Capability();
          capability.setNamespaceURI(PSOReferencesDefinition.REFERENCE_URI);
          capability.addAppliesTo(fromEntity);
          cl.addCapability(capability);

          ReferenceDefinition rd = new ReferenceDefinition();
          rd.setTypeOfReference(psoReferencesDefinition.getName());
          rd.setSchemaEntity(fromEntity);
          rd.addCanReferTo(toEntity);

          OCEtoMarshallableAdapter oce = new OCEtoMarshallableAdapter(rd);
          capability.addOpenContentElement(oce);
        }
      }
    }

    target.setCapabilities(cl);
    schema.addOpenContentElement(dsmlSchema);
    target.addSchema(schema);

    return target;
  }

  public boolean isBundleModifications() {
    return bundleModifications;
  }

  public void setBundleModifications(boolean bundleModifications) {
    LOG.debug("setting bundleModifications {}", bundleModifications);
    this.bundleModifications = bundleModifications;
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("id", id);
    toStringBuilder.append("bundleModifications", bundleModifications);
    return toStringBuilder.toString();
  }
}
