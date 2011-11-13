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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.naming.directory.SearchControls;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.opensaml.util.resource.FilesystemResource;
import org.opensaml.util.resource.Resource;
import org.opensaml.util.resource.ResourceException;
import org.openspml.v2.msg.OCEtoMarshallableAdapter;
import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.AddResponse;
import org.openspml.v2.msg.spml.CapabilityData;
import org.openspml.v2.msg.spml.DeleteRequest;
import org.openspml.v2.msg.spml.LookupRequest;
import org.openspml.v2.msg.spml.LookupResponse;
import org.openspml.v2.msg.spml.Modification;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spml.ModifyResponse;
import org.openspml.v2.msg.spml.PSO;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.QueryClause;
import org.openspml.v2.msg.spml.Request;
import org.openspml.v2.msg.spml.Response;
import org.openspml.v2.msg.spml.SchemaEntityRef;
import org.openspml.v2.msg.spml.StatusCode;
import org.openspml.v2.msg.spmlref.Reference;
import org.openspml.v2.msg.spmlsearch.Query;
import org.openspml.v2.msg.spmlsearch.Scope;
import org.openspml.v2.msg.spmlsearch.SearchRequest;
import org.openspml.v2.msg.spmlsearch.SearchResponse;
import org.openspml.v2.profiles.dsml.DSMLModification;
import org.openspml.v2.util.Spml2Exception;
import org.springframework.context.support.GenericApplicationContext;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.spml.PSP;
import edu.internet2.middleware.ldappc.spml.definitions.PSOReferencesDefinition;
import edu.internet2.middleware.ldappc.spml.request.AlternateIdentifier;
import edu.internet2.middleware.ldappc.spml.request.LdapFilterQueryClause;
import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;
import edu.internet2.middleware.subject.Subject;

public class PSPUtil {

  /**
   * Time stamp part of default requestID format : yyyyMMdd HH:mm:ss.SSS
   */
  public static final String TIMESTAMP_FORMAT = "yyyy/MM/dd-HH:mm:ss.SSS";

  private final static SimpleDateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);

  public static GenericApplicationContext createSpringContext(List<Resource> resources) throws ResourceException {

    GenericApplicationContext gContext = new GenericApplicationContext();
    SpringConfigurationUtils.populateRegistry(gContext, resources);
    gContext.refresh();
    gContext.registerShutdownHook();

    return gContext;
  }

  public static GenericApplicationContext createSpringContext(String... configs) throws ResourceException {

    return createSpringContext(getResources(null, configs));
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

  /**
   * Returns a possible empty list of {@link AlternateIdentifier} elements of the given {@link Modification}.
   * 
   * @param modification the spml modification
   * @return the possibly empty list of alternate identifiers
   */
  public static List<AlternateIdentifier> getAlternateIdentifiers(Modification modification) {
    return modification.getOpenContentElements(AlternateIdentifier.class);
  }

  /**
   * Returns {@link Resource}s with the given names. If the path is {@code null}, resources will be found using the
   * classpath.
   * 
   * @param path the directory containing resources
   * @param resourceNames the names of the resource files
   * @return the resources
   * @throws ResourceException if an error occurs loading the resource
   * @throws IllegalArgumentException if the resources are not files or are not readable
   */
  public static List<Resource> getResources(String path, String... resourceNames) throws ResourceException {
    ArrayList<Resource> resources = new ArrayList<Resource>();
    for (String resourceName : resourceNames) {
      File file = null;
      if (path == null) {
        file = GrouperUtil.fileFromResourceName(resourceName);
      } else {
        file = new File(path + System.getProperty("file.separator") + resourceName);
      }
      if (file == null) {
        throw new IllegalArgumentException("Unable to find file '" + resourceName + "'.");
      }
      if (!file.isFile() || !file.canRead()) {
        throw new IllegalArgumentException("Unable to read file '" + resourceName + "'.");
      }
      resources.add(new FilesystemResource(file.getAbsolutePath()));
    }
    return resources;
  }

  /**
   * Return <code>SearchControls</code> search scope from an SPML <code>Scope</code>.
   * 
   * @param scope the SPML scope
   * @return the javax.naming.directory search scope as an int
   */
  public static int getScope(Scope scope) {

    if (scope.equals(Scope.ONELEVEL)) {
      return SearchControls.OBJECT_SCOPE;
    } else if (scope.equals(Scope.SUBTREE)) {
      return SearchControls.SUBTREE_SCOPE;
    } else if (scope.equals(Scope.PSO)) {
      return SearchControls.OBJECT_SCOPE;
    }

    throw new IllegalArgumentException("Unknow scope " + scope);
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
      return toString((PSOIdentifier) object);
    }

    if (object instanceof Reference) {
      return toString((Reference) object);
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

  public static String toString(AddRequest addRequest) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(addRequest, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("psoID", PSPUtil.toString(addRequest.getPsoID()));
    toStringBuilder.append("targetID", addRequest.getTargetId());
    toStringBuilder.append("returnData", addRequest.getReturnData());
    toStringBuilder.appendSuper(PSPUtil.toString((Request) addRequest));
    return toStringBuilder.toString();
  }

  public static String toString(AddResponse addResponse) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(addResponse, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("pso", PSPUtil.toString(addResponse.getPso()));
    toStringBuilder.appendSuper(PSPUtil.toString((Response) addResponse));
    return toStringBuilder.toString();
  }

  public static String toString(DeleteRequest deleteRequest) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(deleteRequest, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("psoID", PSPUtil.toString(deleteRequest.getPsoID()));
    toStringBuilder.append("recursive", deleteRequest.isRecursive());
    toStringBuilder.appendSuper(PSPUtil.toString((Request) deleteRequest));
    return toStringBuilder.toString();
  }

  public static String toString(DSMLModification dsmlModification) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(dsmlModification, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("name", dsmlModification.getName());
    toStringBuilder.append("op", dsmlModification.getOperation());
    return toStringBuilder.toString();
  }

  public static String toString(LookupRequest lookupRequest) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(lookupRequest, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("psoID", PSPUtil.toString(lookupRequest.getPsoID()));
    toStringBuilder.append("returnData", lookupRequest.getReturnData());
    toStringBuilder.append("requestID", lookupRequest.getRequestID());
    return toStringBuilder.toString();
  }

  public static String toString(LookupResponse lookupResponse) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(lookupResponse, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("pso", PSPUtil.toString(lookupResponse.getPso()));
    toStringBuilder.appendSuper(PSPUtil.toString((Response) lookupResponse));
    return toStringBuilder.toString();
  }

  public static String toString(ModifyRequest modifyRequest) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(modifyRequest, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("psoID", PSPUtil.toString(modifyRequest.getPsoID()));
    for (Modification modification : modifyRequest.getModifications()) {
      for (Object object : modification.getOpenContentElements(DSMLModification.class)) {
        toStringBuilder.append("mod", PSPUtil.toString((DSMLModification) object));
      }
      Map<String, List<Reference>> references = PSP.getReferences(modification.getCapabilityData());
      for (String typeOfReference : references.keySet()) {
        toStringBuilder.append("typeOfReference", typeOfReference);
      }
    }
    toStringBuilder.append("returnData", modifyRequest.getReturnData());
    toStringBuilder.appendSuper(PSPUtil.toString((Request) modifyRequest));
    return toStringBuilder.toString();
  }

  public static String toString(ModifyResponse modifyResponse) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(modifyResponse, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("pso", PSPUtil.toString(modifyResponse.getPso()));
    toStringBuilder.appendSuper(PSPUtil.toString((Response) modifyResponse));
    return toStringBuilder.toString();
  }

  public static String toString(PSO pso) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(pso, ToStringStyle.SHORT_PREFIX_STYLE);
    if (pso != null) {
      toStringBuilder.append("psoID", PSPUtil.toString(pso.getPsoID()));
      // TODO data ? or leave for trace xml
      // TODO capability ?
      List<AlternateIdentifier> altIds = pso.getOpenContentElements(AlternateIdentifier.class);
      if (!altIds.isEmpty()) {
        toStringBuilder.append("alternateIdentifiers", altIds);
      }
    }
    return toStringBuilder.toString();
  }

  public static String toString(PSOIdentifier psoIdentifier) {
    if (psoIdentifier == null) {
      return null;
    }
    ToStringBuilder toStringBuilder = new ToStringBuilder(psoIdentifier, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("id", "'" + psoIdentifier.getID() + "'");
    toStringBuilder.append("targetID", psoIdentifier.getTargetID());
    toStringBuilder.append("containerID", PSPUtil.getString(psoIdentifier.getContainerID()));
    return toStringBuilder.toString();
  }

  public static String toString(Query query) {
    if (query == null) {
      return null;
    }
    ToStringBuilder toStringBuilder = new ToStringBuilder(query, ToStringStyle.SHORT_PREFIX_STYLE);
    for (QueryClause queryClause : query.getQueryClauses()) {
      if (queryClause instanceof LdapFilterQueryClause) {
        toStringBuilder.append("filter", ((LdapFilterQueryClause) queryClause).getFilter());
      }
    }
    toStringBuilder.append("basePsoID", PSPUtil.toString(query.getBasePsoID()));
    toStringBuilder.append("scope", query.getScope());
    toStringBuilder.append("targetID", query.getTargetID());
    return toStringBuilder.toString();
  }

  public static String toString(Reference reference) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(reference, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("toPsoID", PSPUtil.toString(reference.getToPsoID()));
    toStringBuilder.append("type", reference.getTypeOfReference());
    return toStringBuilder.toString();
  }

  public static String toString(Request request) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(request, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("requestID", request.getRequestID());
    return toStringBuilder.toString();
  }

  public static String toString(Response response) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(response, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("status", response.getStatus());
    if (response.getStatus() != null && response.getStatus().equals(StatusCode.FAILURE)) {
      toStringBuilder.append("error", response.getError());
      toStringBuilder.append("errorMessages", response.getErrorMessages());
    }
    toStringBuilder.append("requestID", response.getRequestID());
    return toStringBuilder.toString();
  }

  public static String toString(SearchRequest searchRequest) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(searchRequest, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("query", PSPUtil.toString(searchRequest.getQuery()));
    toStringBuilder.append("returnData", searchRequest.getReturnData());
    toStringBuilder.append("maxSelect", searchRequest.getMaxSelect());
    toStringBuilder.appendSuper(PSPUtil.toString((Request) searchRequest));
    return toStringBuilder.toString();
  }

  public static String toString(SearchResponse searchResponse) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(searchResponse, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("psos", searchResponse.getPSOs().length);
    toStringBuilder.appendSuper(PSPUtil.toString((Response) searchResponse));
    return toStringBuilder.toString();
  }

  public static String toString(SchemaEntityRef schemaEntityRef) {
    ToStringBuilder toStringBuilder = new ToStringBuilder(schemaEntityRef, ToStringStyle.SHORT_PREFIX_STYLE);
    if (schemaEntityRef != null) {
      toStringBuilder.append("targetID", schemaEntityRef.getTargetID());
      toStringBuilder.append("entityName", schemaEntityRef.getEntityName());
      toStringBuilder.append("isContainer", schemaEntityRef.isContainer());
    }
    return toStringBuilder.toString();
  }

  /**
   * Get a reasonably unique string for use as an SPML requestID.
   * 
   * see {@link GrouperUtil#uniqueId()}
   * 
   * @return String of the form yyyy/MM/dd-HH:mm:ss.SSS_XXXXXXXX
   */
  public static String uniqueRequestId() {
    return dateFormat.format(new Date()) + "_" + GrouperUtil.uniqueId();
  }

}
