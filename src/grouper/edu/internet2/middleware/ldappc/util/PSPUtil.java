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

package edu.internet2.middleware.ldappc.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.Resource;
import org.opensaml.util.resource.ResourceException;
import org.openspml.v2.msg.OCEtoMarshallableAdapter;
import org.openspml.v2.msg.spml.CapabilityData;
import org.openspml.v2.msg.spml.PSO;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spmlref.Reference;
import org.openspml.v2.util.Spml2Exception;
import org.springframework.context.support.GenericApplicationContext;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.spml.definitions.PSOReferencesDefinition;
import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;
import edu.internet2.middleware.subject.Subject;

public class PSPUtil {

  public static GenericApplicationContext createSpringContext(String... configs) throws ResourceException {

    ArrayList<Resource> resources = new ArrayList<Resource>();
    if (configs != null) {
      for (String config : configs) {
        resources.add(new ClasspathResource(config));
      }
    }

    return createSpringContext(resources);
  }

  public static GenericApplicationContext createSpringContext(List<Resource> resources) throws ResourceException {

    GenericApplicationContext gContext = new GenericApplicationContext();
    SpringConfigurationUtils.populateRegistry(gContext, resources);
    gContext.refresh();
    gContext.registerShutdownHook();

    return gContext;
  }

  public static String getString(Object object) {

    if (object == null) {
      return null;
    }

    if (object instanceof String) {
      return (String) object;
    }

    if (object instanceof Subject) {
      return GrouperUtil.subjectToString((Subject) object);
    }

    if (object instanceof PSOIdentifier) {

      PSOIdentifier psoIdentifier = (PSOIdentifier) object;

      String containerId = (psoIdentifier.getContainerID() == null ? null : psoIdentifier.getContainerID().getID());
      return "id='" + psoIdentifier.getID() + "' targetID='" + psoIdentifier.getTargetID() + "' containerID='"
          + containerId + "'";
    }

    if (object instanceof Reference) {
      Reference reference = (Reference) object;
      return "toPsoID='" + PSPUtil.getString(reference.getToPsoID()) + "' type='" + reference.getTypeOfReference()
          + "'";
    }

    return object.toString();
  }

  public static CapabilityData setReferences(PSO pso, Collection<Reference> references) throws Spml2Exception {

    CapabilityData capabilityData = PSPUtil.fromReferences(references);
    if (capabilityData != null) {
      pso.addCapabilityData(capabilityData);
    }

    return capabilityData;
  }

  public static CapabilityData fromReferences(Collection<Reference> references) throws Spml2Exception {
    if (!references.isEmpty()) {
      CapabilityData referenceCapabilityData = new CapabilityData(true, PSOReferencesDefinition.REFERENCE_URI);
      for (Reference reference : references) {
        OCEtoMarshallableAdapter oce = new OCEtoMarshallableAdapter(reference);
        referenceCapabilityData.addOpenContentElement(oce);
      }

      return referenceCapabilityData;
    }

    return null;
  }

}
