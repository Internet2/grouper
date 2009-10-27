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

package edu.internet2.middleware.ldappc.spml.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InvalidNameException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.AddResponse;
import org.openspml.v2.msg.spml.CapabilityData;
import org.openspml.v2.msg.spml.DeleteRequest;
import org.openspml.v2.msg.spml.DeleteResponse;
import org.openspml.v2.msg.spml.ErrorCode;
import org.openspml.v2.msg.spml.Extensible;
import org.openspml.v2.msg.spml.LookupRequest;
import org.openspml.v2.msg.spml.LookupResponse;
import org.openspml.v2.msg.spml.Modification;
import org.openspml.v2.msg.spml.ModificationMode;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spml.ModifyResponse;
import org.openspml.v2.msg.spml.PSO;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.QueryClause;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.StatusCode;
import org.openspml.v2.msg.spmlref.Reference;
import org.openspml.v2.msg.spmlsearch.Query;
import org.openspml.v2.msg.spmlsearch.SearchRequest;
import org.openspml.v2.msg.spmlsearch.SearchResponse;
import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.profiles.dsml.DSMLModification;
import org.openspml.v2.profiles.dsml.DSMLProfileException;
import org.openspml.v2.profiles.dsml.DSMLValue;
import org.openspml.v2.util.Spml2Exception;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.PSP;
import edu.internet2.middleware.ldappc.spml.definitions.PSODefinition;
import edu.internet2.middleware.ldappc.spml.definitions.PSOReferencesDefinition;
import edu.internet2.middleware.ldappc.spml.request.LdapFilterQueryClause;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.service.ServiceException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.bean.LdapAttribute;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.bean.LdapResult;
import edu.vt.middleware.ldap.ldif.LdifResult;
import edu.vt.middleware.ldap.pool.LdapPool;
import edu.vt.middleware.ldap.pool.LdapPoolException;

public class LdapTargetProvider extends BaseSpmlTargetProvider {

  private static final Logger LOG = GrouperUtil.getLogger(LdapTargetProvider.class);

  private String ldapPoolId;

  private LdapPool<Ldap> ldapPool;

  public LdapTargetProvider() {
  }

  public String getLdapPoolId() {
    return ldapPoolId;
  }

  public void setLdapPoolId(String ldapPoolId) {
    this.ldapPoolId = ldapPoolId;
  }

  public LdapPool<Ldap> getLdapPool() {
    return ldapPool;
  }

  protected void onNewContextCreated(ApplicationContext newServiceContext) throws ServiceException {
    LdapPool<Ldap> oldPool = ldapPool;
    try {
      LOG.debug("Loading ldap pool '{}'", getLdapPoolId());
      ldapPool = (LdapPool<Ldap>) newServiceContext.getBean(getLdapPoolId());
    } catch (Exception e) {
      ldapPool = oldPool;
      LOG.error(getId() + " configuration is not valid, retaining old configuration", e);
      throw new ServiceException(getId() + " configuration is not valid, retaining old configuration", e);
    }
  }

  public AddResponse execute(AddRequest addRequest) {
    if (addRequest.getRequestID() == null) {
      addRequest.setRequestID(this.generateRequestID(addRequest));
    }
    LOG.trace("add request:\n{}", this.toXML(addRequest));

    AddResponse addResponse = new AddResponse();
    addResponse.setStatus(StatusCode.SUCCESS);
    addResponse.setRequestID(this.getOrGenerateRequestID(addRequest));

    if (addRequest.getPsoID() == null || addRequest.getPsoID().getID() == null) {
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

    if (!targetId.equals(this.getTargetDefinition().getId())) {
      fail(addResponse, ErrorCode.INVALID_IDENTIFIER);
      LOG.trace("add response:\n{}", this.toXML(addResponse));
      return addResponse;
    }

    String dn = addRequest.getPsoID().getID();

    String msg = "add '" + dn + "' ldap '" + this.getId() + "'";

    Ldap ldap = null;
    try {
      // build ldap attributes
      LdapAttributes ldapAttributes = new LdapAttributes();
      Extensible data = addRequest.getData();
      if (data == null) {
        fail(addResponse, ErrorCode.MALFORMED_REQUEST, "Data is required.");
        LOG.trace("add response:\n{}", this.toXML(addResponse));
        return addResponse;
      }

      // data
      Map<String, DSMLAttr> dsmlAttrs = PSP.getDSMLAttrMap(data);
      for (DSMLAttr dsmlAttr : dsmlAttrs.values()) {
        List<String> values = new ArrayList<String>();
        for (DSMLValue dsmlValue : dsmlAttr.getValues()) {
          values.add(dsmlValue.getValue());
        }
        LdapAttribute ldapAttribute = new LdapAttribute(dsmlAttr.getName(), values);
        ldapAttributes.addAttribute(ldapAttribute);
      }

      // references
      Map<String, List<Reference>> references = PSP.getReferences(addRequest.getCapabilityData());
      for (String typeOfReference : references.keySet()) {
        List<String> ids = new ArrayList<String>();
        for (Reference reference : references.get(typeOfReference)) {
          if (reference.getToPsoID().getTargetID().equals(this.getTargetDefinition().getId())) {
            String id = reference.getToPsoID().getID();
            // fake empty string since the spml toolkit ignores an empty string psoID
            if (id.equals(PSOReferencesDefinition.EMPTY_STRING)) {
              id = "";
            }
            ids.add(id);
          }
        }
        LdapAttribute ldapAttribute = new LdapAttribute(typeOfReference, ids);
        ldapAttributes.addAttribute(ldapAttribute);
      }

      // TODO decide on logging, ldif or one-line
      if (LOG.isDebugEnabled()) {
        LOG.debug("{} entry '{}'", msg, ldapAttributes);
        if (LOG.isTraceEnabled()) {
          LdapEntry ldapEntry = new LdapEntry(dn, ldapAttributes);
          LdifResult ldifResult = new LdifResult(ldapEntry);
          LOG.trace("{} ldif:\n{}", msg, ldifResult.toLdif());
        }
      }

      ldap = ldapPool.checkOut();

      LOG.info("{}", msg);
      ldap.create(dn, ldapAttributes.toAttributes());

      // response PSO
      PSO responsePSO = new PSO();
      responsePSO.setPsoID(addRequest.getPsoID());
      if (addRequest.getReturnData().equals(ReturnData.DATA)
          || addRequest.getReturnData().equals(ReturnData.EVERYTHING)) {
        responsePSO.setData(addRequest.getData());
      }
      if (addRequest.getReturnData().equals(ReturnData.EVERYTHING)) {
        for (CapabilityData capabilityData : addRequest.getCapabilityData()) {
          responsePSO.addCapabilityData(capabilityData);
        }
      }
      addResponse.setPso(responsePSO);

      // TODO are all jndi exceptions caught correctly ?
    } catch (LdapPoolException e) {
      LOG.error(msg + " An LdapPool error occurred", e);
      fail(addResponse, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } catch (NameAlreadyBoundException e) {
      LOG.error(msg + " Already exists", e);
      fail(addResponse, ErrorCode.ALREADY_EXISTS);
    } catch (NamingException e) {
      LOG.error(msg + " An error occurred", e);
      fail(addResponse, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } catch (LdappcException e) {
      // from PSO.getReferences, an unhandled capability data
      LOG.error(msg + " An error occurred", e);
      fail(addResponse, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } finally {
      ldapPool.checkIn(ldap);
    }

    LOG.trace("add response:\n{}", this.toXML(addResponse));
    return addResponse;
  }

  public DeleteResponse execute(DeleteRequest deleteRequest) {
    if (deleteRequest.getRequestID() == null) {
      deleteRequest.setRequestID(this.generateRequestID(deleteRequest));
    }
    LOG.trace("delete request:\n{}", this.toXML(deleteRequest));

    DeleteResponse deleteResponse = new DeleteResponse();
    deleteResponse.setStatus(StatusCode.SUCCESS);
    deleteResponse.setRequestID(this.getOrGenerateRequestID(deleteRequest));

    if (deleteRequest.getPsoID() == null || deleteRequest.getPsoID().getID() == null) {
      fail(deleteResponse, ErrorCode.MALFORMED_REQUEST, ERROR_NULL_PSO_ID);
      LOG.trace("delete response:\n{}", this.toXML(deleteResponse));
      return deleteResponse;
    }

    String targetId = deleteRequest.getPsoID().getTargetID();
    if (targetId == null) {
      fail(deleteResponse, ErrorCode.MALFORMED_REQUEST, ERROR_NULL_TARGET_ID);
      LOG.trace("delete response:\n{}", this.toXML(deleteResponse));
      return deleteResponse;
    }

    if (!targetId.equals(this.getTargetDefinition().getId())) {
      fail(deleteResponse, ErrorCode.INVALID_IDENTIFIER);
      LOG.trace("delete response:\n{}", this.toXML(deleteResponse));
      return deleteResponse;
    }

    String dn = deleteRequest.getPsoID().getID();

    String msg = "delete '" + dn + "' ldap '" + this.getId() + "'";

    Ldap ldap = null;
    try {
      ldap = ldapPool.checkOut();
      LOG.info("{}", msg);
      ldap.delete(dn);

      // TODO are all jndi exceptions caught correctly ?
    } catch (LdapPoolException e) {
      LOG.error(msg + " An LdapPool error occurred", e);
      fail(deleteResponse, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } catch (NameNotFoundException e) {
      LOG.error(msg + " Not found", e);
      fail(deleteResponse, ErrorCode.NO_SUCH_IDENTIFIER);
    } catch (NamingException e) {
      LOG.error(msg + " An LdapPool error occurred", e);
      fail(deleteResponse, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } finally {
      ldapPool.checkIn(ldap);
    }

    LOG.trace("delete response:\n{}", this.toXML(deleteResponse));
    return deleteResponse;
  }

  public LookupResponse execute(LookupRequest lookupRequest) {
    if (lookupRequest.getRequestID() == null) {
      lookupRequest.setRequestID(this.generateRequestID(lookupRequest));
    }
    LOG.trace("lookup request:\n{}", this.toXML(lookupRequest));

    LookupResponse lookupResponse = new LookupResponse();
    lookupResponse.setStatus(StatusCode.SUCCESS);
    lookupResponse.setRequestID(this.getOrGenerateRequestID(lookupRequest));

    if (lookupRequest.getPsoID() == null || lookupRequest.getPsoID().getID() == null) {
      fail(lookupResponse, ErrorCode.MALFORMED_REQUEST, ERROR_NULL_PSO_ID);
      LOG.trace("lookup response:\n{}", this.toXML(lookupResponse));
      return lookupResponse;
    }

    String targetId = lookupRequest.getPsoID().getTargetID();
    if (targetId == null) {
      fail(lookupResponse, ErrorCode.MALFORMED_REQUEST, ERROR_NULL_TARGET_ID);
      LOG.trace("lookup response:\n{}", this.toXML(lookupResponse));
      return lookupResponse;
    }

    if (!targetId.equals(this.getTargetDefinition().getId())) {
      fail(lookupResponse, ErrorCode.INVALID_IDENTIFIER);
      LOG.trace("lookup response:\n{}", this.toXML(lookupResponse));
      return lookupResponse;
    }

    String dn = lookupRequest.getPsoID().getID();

    String msg = "lookup '" + dn + "' ldap '" + this.getId() + "' return '" + lookupRequest.getReturnData() + "'";

    Ldap ldap = null;
    try {

      // This lookup requests attributes defined for *all* objects.
      // Perhaps there should be two searches, one for the identifier
      // and a second for attributes.
      String[] retAttrs = this.getTargetDefinition().getNames(lookupRequest.getReturnData()).toArray(new String[] {});

      ldap = ldapPool.checkOut();

      LOG.debug("{} retAttrs {}", msg, Arrays.asList(retAttrs));
      Attributes attributes = ldap.getAttributes(dn, retAttrs);

      LdapEntry entry = new LdapEntry(dn, new LdapAttributes(attributes));
      // TODO debug or info logging or ?
      LOG.info("{} found {} attributes", msg, entry.getLdapAttributes().getAttributes().size());

      // build pso
      lookupResponse.setPso(getPSO(entry, lookupRequest.getReturnData()));

    } catch (NameNotFoundException e) {
      LOG.debug("{} not found", msg);
      fail(lookupResponse, ErrorCode.NO_SUCH_IDENTIFIER, e.getMessage());
    } catch (LdapPoolException e) {
      LOG.error("An error occurred", e);
      fail(lookupResponse, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } catch (InvalidNameException e) {
      LOG.error("An error occurred", e);
      fail(lookupResponse, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } catch (NamingException e) {
      LOG.error("An error occurred", e);
      fail(lookupResponse, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } catch (DSMLProfileException e) {
      LOG.error("An error occurred", e);
      fail(lookupResponse, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } catch (Spml2Exception e) {
      LOG.error("An error occurred", e);
      fail(lookupResponse, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } finally {
      ldapPool.checkIn(ldap);
    }

    LOG.trace("lookup response:\n{}", this.toXML(lookupResponse));
    return lookupResponse;
  }

  public ModifyResponse execute(ModifyRequest modifyRequest) {
    if (modifyRequest.getRequestID() == null) {
      modifyRequest.setRequestID(this.generateRequestID(modifyRequest));
    }
    LOG.trace("modify request:\n{}", this.toXML(modifyRequest));

    ModifyResponse response = new ModifyResponse();
    response.setStatus(StatusCode.SUCCESS);
    response.setRequestID(this.getOrGenerateRequestID(modifyRequest));

    if (modifyRequest.getPsoID() == null || modifyRequest.getPsoID().getID() == null) {
      fail(response, ErrorCode.MALFORMED_REQUEST, ERROR_NULL_PSO_ID);
      LOG.trace("modify response:\n{}", this.toXML(response));
      return response;
    }

    String targetId = modifyRequest.getPsoID().getTargetID();
    if (targetId == null) {
      fail(response, ErrorCode.MALFORMED_REQUEST, ERROR_NULL_TARGET_ID);
      LOG.trace("modify response:\n{}", this.toXML(response));
      return response;
    }

    if (!targetId.equals(this.getTargetDefinition().getId())) {
      fail(response, ErrorCode.INVALID_IDENTIFIER);
      LOG.trace("modify response:\n{}", this.toXML(response));
      return response;
    }

    String dn = modifyRequest.getPsoID().getID();

    String msg = "modify '" + dn + "' ldap '" + this.getId() + "'";

    Ldap ldap = null;
    try {
      List<ModificationItem> modificationItem = new ArrayList<ModificationItem>();
      for (Modification modification : modifyRequest.getModifications()) {
        modificationItem.addAll(this.getDsmlMods(modification));
        modificationItem.addAll(this.getReferenceMods(modification));
      }

      ldap = ldapPool.checkOut();

      LOG.debug("{} mods {}", msg, modificationItem);
      LOG.info("{}", msg);
      ldap.modifyAttributes(dn, modificationItem.toArray(new ModificationItem[] {}));

      // response PSO

      if (modifyRequest.getReturnData().equals(ReturnData.IDENTIFIER)) {
        PSO responsePSO = new PSO();
        responsePSO.setPsoID(modifyRequest.getPsoID());
        response.setPso(responsePSO);
      } else {
        LookupRequest lookupRequest = new LookupRequest();
        lookupRequest.setPsoID(modifyRequest.getPsoID());
        lookupRequest.setReturnData(modifyRequest.getReturnData());

        LookupResponse lookupResponse = this.execute(lookupRequest);
        if (lookupResponse.getStatus() == StatusCode.SUCCESS) {
          response.setPso(lookupResponse.getPso());
        } else {
          LOG.error("{} Unable to lookup object after modification.", msg);
          fail(response, lookupResponse.getError());
          LOG.trace("modify response:\n{}", this.toXML(response));
          return response;
        }
      }

      // TODO are all jndi exceptions caught correctly ?
    } catch (LdapPoolException e) {
      LOG.error("An error occurred", e);
      fail(response, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } catch (LdappcException e) {
      // from PSO.getReferences, an unhandled capability data
      LOG.error("An error occurred", e);
      fail(response, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } catch (NamingException e) {
      LOG.error("An error occurred", e);
      fail(response, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } finally {
      ldapPool.checkIn(ldap);
    }

    LOG.trace("modify response:\n{}", this.toXML(response));
    return response;
  }

  public SearchResponse execute(SearchRequest searchRequest) {
    if (searchRequest.getRequestID() == null) {
      searchRequest.setRequestID(this.generateRequestID(searchRequest));
    }
    LOG.trace("search request:\n{}", this.toXML(searchRequest));

    SearchResponse searchResponse = new SearchResponse();
    searchResponse.setStatus(StatusCode.SUCCESS);
    searchResponse.setRequestID(this.getOrGenerateRequestID(searchRequest));

    Query query = searchRequest.getQuery();
    if (query == null) {
      fail(searchResponse, ErrorCode.MALFORMED_REQUEST, "A query is required.");
      LOG.trace("search response:\n{}", this.toXML(searchResponse));
      return searchResponse;
    }

    String filter = null;
    for (QueryClause queryClause : query.getQueryClauses()) {
      if (queryClause instanceof LdapFilterQueryClause) {
        LdapFilterQueryClause ldapFilterQueryClause = (LdapFilterQueryClause) queryClause;
        filter = ldapFilterQueryClause.getFilter();
      }
    }
    if (filter == null) {
      fail(searchResponse, ErrorCode.MALFORMED_REQUEST, "A filter is required.");
      LOG.trace("search response:\n{}", this.toXML(searchResponse));
      return searchResponse;
    }

    if (query.getBasePsoID() == null || query.getBasePsoID().getID() == null) {
      fail(searchResponse, ErrorCode.MALFORMED_REQUEST, "A basePsoID is required.");
      LOG.trace("search response:\n{}", this.toXML(searchResponse));
      return searchResponse;
    }
    String base = query.getBasePsoID().getID();

    String msg = "search ldap '" + this.getId() + "' return '" + searchRequest.getReturnData() + "'";

    Ldap ldap = null;
    try {
      String[] retAttrs = this.getTargetDefinition().getNames(searchRequest.getReturnData()).toArray(new String[] {});

      ldap = ldapPool.checkOut();

      LOG.debug("{} filter '{}' base '{}' retAttrs {}", new Object[] { msg, filter, base, Arrays.asList(retAttrs) });
      Iterator<SearchResult> searchResults = ldap.search(base, new SearchFilter(filter), retAttrs);

      LdapResult ldapResult = new LdapResult(searchResults);
      Collection<LdapEntry> entries = ldapResult.getEntries();
      LOG.debug("{} found", entries.size());
      for (LdapEntry entry : entries) {
        searchResponse.addPSO(getPSO(entry, searchRequest.getReturnData()));
      }

      // TODO are all jndi exceptions caught correctly ?

    } catch (LdapPoolException e) {
      LOG.error("An error occurred", e);
      fail(searchResponse, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } catch (NameNotFoundException e) {
      LOG.debug("{} not found", msg);
      fail(searchResponse, ErrorCode.NO_SUCH_IDENTIFIER);
    } catch (NamingException e) {
      LOG.error("An error occurred", e);
      fail(searchResponse, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } catch (Spml2Exception e) {
      LOG.error("An error occurred", e);
      fail(searchResponse, ErrorCode.CUSTOM_ERROR, e.getMessage());
    } finally {
      ldapPool.checkIn(ldap);
    }

    LOG.trace("search response:\n{}", this.toXML(searchResponse));
    return searchResponse;
  }

  protected PSO getPSO(LdapEntry entry, ReturnData returnData) throws Spml2Exception {

    String msg = "get pso for '" + entry.getDn() + "' target '" + this.getTargetDefinition().getId() + "'";

    PSO pso = new PSO();
    PSOIdentifier psoID = new PSOIdentifier();
    psoID.setID(entry.getDn());
    psoID.setTargetID(this.getTargetDefinition().getId());
    pso.setPsoID(psoID);

    LdapAttributes ldapAttributes = entry.getLdapAttributes();

    // determine schema entity
    PSODefinition psoDefinition = this.getPSODefinition(entry);
    LOG.trace("{} object '{}'", msg, psoDefinition.getId());

    if (returnData.equals(ReturnData.DATA) || returnData.equals(ReturnData.EVERYTHING)) {
      // TODO this is ugly
      Set<String> attributeNames = new HashSet<String>();
      for (String attrName : psoDefinition.getAttributeNames()) {
        attributeNames.add(attrName.toLowerCase());
      }
      Set<String> referenceNames = new HashSet<String>();
      for (String refName : psoDefinition.getReferenceNames()) {
        referenceNames.add(refName.toLowerCase());
      }

      Extensible data = new Extensible();
      List<Reference> references = new ArrayList<Reference>();

      for (LdapAttribute ldapAttribute : ldapAttributes.getAttributes()) {
        if (attributeNames.contains(ldapAttribute.getName().toLowerCase())) {
          data.addOpenContentElement(this.getDsmlAttr(ldapAttribute));
        } else if (referenceNames.contains(ldapAttribute.getName().toLowerCase())) {
          if (returnData.equals(ReturnData.EVERYTHING)) {
            references.addAll(this.getReferences(ldapAttribute));
          }
        } else {
          // TODO logging ?
          LOG.trace("{} ignoring attribute '{}'", msg, ldapAttribute.getName());
        }

        if (data.getOpenContentElements().length > 0) {
          pso.setData(data);
        }
        if (returnData.equals(ReturnData.EVERYTHING)) {
          PSPUtil.setReferences(pso, references);
        }
      }
    }

    return pso;
  }

  protected PSODefinition getPSODefinition(LdapEntry entry) {

    Attributes attributes = entry.getLdapAttributes().toAttributes();

    PSODefinition definition = null;

    for (PSODefinition psoDefinition : this.getTargetDefinition().getPsoDefinitions()) {
      String idAttrName = psoDefinition.getPsoIdentifierDefinition().getIdentifyingAttribute().getName();
      String idAttrValue = psoDefinition.getPsoIdentifierDefinition().getIdentifyingAttribute().getValue();
      Attribute attribute = attributes.get(idAttrName);
      if (attribute != null && attribute.contains(idAttrValue)) {
        if (definition != null) {
          LOG.error("More than one schema entity found for " + entry.getDn());
          throw new LdappcException("More than one schema entity found for " + entry.getDn());
        }
        definition = psoDefinition;
      }
    }
    if (definition == null) {
      LOG.error("Unable to determine schema entity for " + entry.getDn());
      throw new LdappcException("Unable to determine schema entity for " + entry.getDn());
    }

    return definition;
  }

  protected DSMLAttr getDsmlAttr(LdapAttribute ldapAttribute) throws DSMLProfileException {
    DSMLValue[] dsmlValues = null;
    DSMLAttr dsmlAttr = new DSMLAttr(ldapAttribute.getName(), dsmlValues);
    for (String ldapAttributeValue : ldapAttribute.getStringValues()) {
      dsmlAttr.addValue(new DSMLValue(ldapAttributeValue));
    }
    return dsmlAttr;
  }

  protected List<Reference> getReferences(LdapAttribute ldapAttribute) {
    List<Reference> references = new ArrayList<Reference>();
    for (String value : ldapAttribute.getStringValues()) {
      Reference reference = new Reference();
      PSOIdentifier toPSOId = new PSOIdentifier();
      if (value.equals("")) {
        value = PSOReferencesDefinition.EMPTY_STRING;
      }
      toPSOId.setID(value);
      toPSOId.setTargetID(this.getTargetDefinition().getId());
      reference.setToPsoID(toPSOId);
      reference.setTypeOfReference(ldapAttribute.getName());
      references.add(reference);
    }
    return references;
  }

  protected List<ModificationItem> getDsmlMods(Modification modification) {
    List<ModificationItem> mods = new ArrayList<ModificationItem>();

    for (Object object : modification.getOpenContentElements(DSMLModification.class)) {
      DSMLModification dsmlModification = (DSMLModification) object;

      Attribute attribute = new BasicAttribute(dsmlModification.getName());

      DSMLValue[] dsmlValues = dsmlModification.getValues();
      for (DSMLValue dsmlValue : dsmlValues) {
        attribute.add(dsmlValue.getValue());
      }

      int op = -1;
      if (dsmlModification.getOperation().equals(ModificationMode.ADD)) {
        op = DirContext.ADD_ATTRIBUTE;
      } else if (dsmlModification.getOperation().equals(ModificationMode.DELETE)) {
        op = DirContext.REMOVE_ATTRIBUTE;
      } else if (dsmlModification.getOperation().equals(ModificationMode.REPLACE)) {
        op = DirContext.REPLACE_ATTRIBUTE;
      } else {
        throw new LdappcException("Unknown dsml modification operation : " + dsmlModification.getOperation());
      }

      mods.add(new ModificationItem(op, attribute));
    }

    return mods;
  }

  protected List<ModificationItem> getReferenceMods(Modification modification) {
    List<ModificationItem> mods = new ArrayList<ModificationItem>();

    Map<String, List<Reference>> references = PSP.getReferences(modification.getCapabilityData());

    if (references.isEmpty()) {
      return mods;
    }

    for (String typeOfReference : references.keySet()) {

      List<String> ids = new ArrayList<String>();
      for (Reference reference : references.get(typeOfReference)) {
        if (reference.getToPsoID().getTargetID().equals(this.getTargetDefinition().getId())) {
          String id = reference.getToPsoID().getID();
          // fake empty string since the spml toolkit ignores an empty string psoID
          if (id.equals(PSOReferencesDefinition.EMPTY_STRING)) {
            id = "";
          }
          ids.add(id);
        }
      }

      Attribute attribute = new BasicAttribute(typeOfReference);
      for (String id : ids) {
        attribute.add(id);
      }

      int op = -1;
      if (modification.getModificationMode().equals(ModificationMode.ADD)) {
        op = DirContext.ADD_ATTRIBUTE;
      } else if (modification.getModificationMode().equals(ModificationMode.DELETE)) {
        op = DirContext.REMOVE_ATTRIBUTE;
      } else if (modification.getModificationMode().equals(ModificationMode.REPLACE)) {
        op = DirContext.REPLACE_ATTRIBUTE;
      } else {
        throw new LdappcException("Unknown modification operation : " + modification.getModificationMode());
      }

      mods.add(new ModificationItem(op, attribute));
    }

    return mods;
  }
}
