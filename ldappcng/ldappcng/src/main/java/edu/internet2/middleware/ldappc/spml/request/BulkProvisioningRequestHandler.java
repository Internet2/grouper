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

package edu.internet2.middleware.ldappc.spml.request;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openspml.v2.msg.spml.SchemaEntityRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.shibboleth.dataConnector.SourceDataConnector;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.PSP;
import edu.internet2.middleware.ldappc.spml.definitions.PSODefinition;
import edu.internet2.middleware.ldappc.spml.definitions.PSOIdentifierDefinition;
import edu.internet2.middleware.ldappc.spml.definitions.TargetDefinition;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ResolutionPlugIn;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethAttributeResolver;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition.AttributeDefinition;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.DataConnector;

/**
 *
 */
public class BulkProvisioningRequestHandler {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(BulkProvisioningRequestHandler.class);

  private PSP psp;

  private BulkProvisioningRequest request;

  /**
   * 
   * Constructor
   * 
   * 
   * @param psp
   * @param request
   */
  public BulkProvisioningRequestHandler(PSP psp, BulkProvisioningRequest request) {
    this.psp = psp;
    this.request = request;
  }

  public Set<String> getAllIdentifiers() throws LdappcException {
    return getAllIdentifiers(false);
  }

  public Set<String> getAllIdentifiers(boolean updatedSince) throws LdappcException {
    String msg = "get all identifers";
    if (updatedSince) {
      msg += " updated since " + request.getUpdatedSince();
    }
    LOG.debug(msg);

    Set<String> identifiers = new LinkedHashSet<String>();

    Map<String, SourceDataConnector> dataConnectors = new LinkedHashMap<String, SourceDataConnector>();

    if (request.getSchemaEntities().isEmpty()) {
      dataConnectors.putAll(this.getAllSourceDataConnectors(null));
    } else {
      for (SchemaEntityRef schemaEntityRef : request.getSchemaEntities()) {
        dataConnectors.putAll(this.getAllSourceDataConnectors(schemaEntityRef));
      }
    }

    for (SourceDataConnector sourceDataConnector : dataConnectors.values()) {
      if (updatedSince && request.getUpdatedSinceAsDate() != null) {
        identifiers.addAll(sourceDataConnector.getAllIdentifiers(request.getUpdatedSinceAsDate()));
      } else {
        identifiers.addAll(sourceDataConnector.getAllIdentifiers());
      }
    }

    LOG.debug("{} found {} identifiers", msg, identifiers.size());
    return identifiers;
  }

  protected Map<String, SourceDataConnector> getAllSourceDataConnectors(SchemaEntityRef schemaEntityRef)
      throws LdappcException {
    String msg = "get all source data connectors for " + PSPUtil.toString(schemaEntityRef);
    LOG.debug(msg);

    ShibbolethAttributeResolver attributeResolver = null;
    for (String beanName : psp.getApplicationContext().getBeanNamesForType(ShibbolethAttributeResolver.class)) {
      if (attributeResolver != null) {
        LOG.error("Only one ShibbolethAttributeResolver was expected.");
        throw new LdappcException("Only one ShibbolethAttributeResolver was expected.");
      }
      attributeResolver = (ShibbolethAttributeResolver) psp.getApplicationContext().getBean(beanName);
    }

    Map<String, SourceDataConnector> dataConnectors = new LinkedHashMap<String, SourceDataConnector>();

    Map<TargetDefinition, List<PSODefinition>> map = psp.getTargetAndObjectDefinitions(schemaEntityRef);

    for (TargetDefinition targetDefinition : map.keySet()) {
      for (PSODefinition psoDefinition : map.get(targetDefinition)) {

        PSOIdentifierDefinition psoIdentifierDefinition = psoDefinition.getPsoIdentifierDefinition();
        String ref = psoIdentifierDefinition.getRef();

        AttributeDefinition attributeDef = attributeResolver.getAttributeDefinitions().get(ref);
        if (attributeDef == null) {
          LOG.error("Attribute definition for '" + ref + "' does not exist.");
          throw new LdappcException("Attribute definition for '" + ref + "' does not exist.");
        }

        dataConnectors.putAll(this.getDependentSourceDataConnectors(attributeResolver, attributeDef));
      }
    }

    LOG.debug("{} found {}", msg, dataConnectors);
    return dataConnectors;
  }

  protected Map<String, SourceDataConnector> getDependentSourceDataConnectors(ShibbolethAttributeResolver sar,
      ResolutionPlugIn<?> plugIn) {
    String msg = "get dependent source data connectors for attribute resolver '" + sar.getId() + "' plugIn '"
        + plugIn.getId() + "'";
    LOG.debug(msg);

    Map<String, SourceDataConnector> dataConnectors = new LinkedHashMap<String, SourceDataConnector>();

    for (String dependencyId : plugIn.getDependencyIds()) {
      AttributeDefinition ad = sar.getAttributeDefinitions().get(dependencyId);
      if (ad != null) {
        dataConnectors.putAll(this.getDependentSourceDataConnectors(sar, ad));
      }
      DataConnector dc = sar.getDataConnectors().get(dependencyId);
      if (dc != null) {
        if (dc instanceof SourceDataConnector) {
          dataConnectors.put(dc.getId(), (SourceDataConnector) dc);
        }
        dataConnectors.putAll(this.getDependentSourceDataConnectors(sar, dc));
      }
    }

    LOG.debug("{} found {}", msg, dataConnectors);
    return dataConnectors;
  }

}
