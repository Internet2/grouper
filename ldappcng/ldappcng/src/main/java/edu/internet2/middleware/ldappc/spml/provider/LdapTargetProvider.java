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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.InvalidNameException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.AddResponse;
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
import org.openspml.v2.msg.spml.Response;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.StatusCode;
import org.openspml.v2.msg.spmlref.HasReference;
import org.openspml.v2.msg.spmlref.Reference;
import org.openspml.v2.msg.spmlsearch.Query;
import org.openspml.v2.msg.spmlsearch.Scope;
import org.openspml.v2.msg.spmlsearch.SearchRequest;
import org.openspml.v2.msg.spmlsearch.SearchResponse;
import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.profiles.dsml.DSMLModification;
import org.openspml.v2.profiles.dsml.DSMLProfileException;
import org.openspml.v2.profiles.dsml.DSMLValue;
import org.openspml.v2.util.Spml2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.PSP;
import edu.internet2.middleware.ldappc.spml.PSPConstants;
import edu.internet2.middleware.ldappc.spml.definitions.PSODefinition;
import edu.internet2.middleware.ldappc.spml.definitions.PSOReferencesDefinition;
import edu.internet2.middleware.ldappc.spml.request.LdapFilterQueryClause;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.service.ServiceException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.bean.LdapAttribute;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.bean.LdapResult;
import edu.vt.middleware.ldap.bean.OrderedLdapBeanFactory;
import edu.vt.middleware.ldap.bean.SortedLdapBeanFactory;
import edu.vt.middleware.ldap.ldif.Ldif;
import edu.vt.middleware.ldap.ldif.LdifResultConverter;
import edu.vt.middleware.ldap.pool.LdapPool;
import edu.vt.middleware.ldap.pool.LdapPoolException;

public class LdapTargetProvider extends BaseSpmlTargetProvider {

  private static final Logger LOG = LoggerFactory.getLogger(LdapTargetProvider.class);

  private String ldapPoolId;

  private LdapPool<Ldap> ldapPool;

  private boolean logLdif;

  private boolean logSpml;

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

  /**
   * @return Returns the logLdif.
   */
  public boolean isLogLdif() {
    return logLdif;
  }

  /**
   * @param logLdif
   *          The logLdif to set.
   */
  public void setLogLdif(boolean logLdif) {
    this.logLdif = logLdif;
  }

  /**
   * @return Returns the logSpml.
   */
  public boolean isLogSpml() {
    return logSpml;
  }

  /**
   * @param logSpml
   *          The logSpml to set.
   */
  public void setLogSpml(boolean logSpml) {
    this.logSpml = logSpml;
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

    String msg = PSPUtil.toString(addRequest);
    LOG.info("{}", msg);
    if (this.isLogSpml()) LOG.info("\n{}", this.toXML(addRequest));

    AddResponse addResponse = new AddResponse();
    addResponse.setRequestID(this.getOrGenerateRequestID(addRequest));

    if (!this.getPSP().isValid(addRequest, addResponse)) {
      LOG.error(PSPUtil.toString(addResponse));
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(addResponse));
      return addResponse;
    }

    if (!this.isValidTargetId(addRequest.getPsoID(), addResponse)) {
      LOG.error(PSPUtil.toString(addResponse));
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(addResponse));
      return addResponse;
    }

    // assume the psoID is a DN
    String dn = addRequest.getPsoID().getID();

    try {
      this.handleEmptyReferences(addRequest);
    } catch (DSMLProfileException e) {
      fail(addResponse, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(addResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(addResponse));
      return addResponse;
    }

    Ldap ldap = null;
    try {
      Extensible data = addRequest.getData();

      SortedLdapBeanFactory ldapBeanFactory = new SortedLdapBeanFactory();
      LdapAttributes ldapAttributes = ldapBeanFactory.newLdapAttributes();

      // data
      Map<String, DSMLAttr> dsmlAttrs = PSP.getDSMLAttrMap(data);
      for (DSMLAttr dsmlAttr : dsmlAttrs.values()) {
        BasicAttribute basicAttribute = new BasicAttribute(dsmlAttr.getName());
        for (DSMLValue dsmlValue : dsmlAttr.getValues()) {
          basicAttribute.add(dsmlValue.getValue());
        }
        LdapAttribute ldapAttribute = ldapBeanFactory.newLdapAttribute();
        ldapAttribute.setAttribute(basicAttribute);
        ldapAttributes.addAttribute(ldapAttribute);
      }

      // references
      Map<String, List<Reference>> references = PSP.getReferences(addRequest.getCapabilityData());
      for (String typeOfReference : references.keySet()) {
        BasicAttribute basicAttribute = new BasicAttribute(typeOfReference);
        for (Reference reference : references.get(typeOfReference)) {
          if (reference.getToPsoID().getTargetID().equals(this.getTargetDefinition().getId())) {
            String id = reference.getToPsoID().getID();
            // fake empty string since the spml toolkit ignores an empty string psoID
            if (id == null) {
              id = "";
            }
            basicAttribute.add(id);
          }
        }
        LdapAttribute ldapAttribute = ldapBeanFactory.newLdapAttribute();
        ldapAttribute.setAttribute(basicAttribute);
        ldapAttributes.addAttribute(ldapAttribute);
      }

      String escapedDn = LdapUtil.escapeForwardSlash(dn);
      LOG.debug("{} escaped dn '{}'", msg, escapedDn);

      // create
      ldap = ldapPool.checkOut();
      LOG.info("{} create", PSPUtil.toString(addRequest));
      ldap.create(escapedDn, ldapAttributes.toAttributes());

      if (this.isLogLdif()) {
        LdapEntry ldapEntry = ldapBeanFactory.newLdapEntry();
        ldapEntry.setDn(dn);
        ldapEntry.setLdapAttributes(ldapAttributes);
        LdapResult result = ldapBeanFactory.newLdapResult();
        result.addEntry(ldapEntry);
        Ldif ldif = new Ldif();
        LOG.info("{}:\n{}", msg, ldif.createLdif(result));
      }

      // response PSO
      if (addRequest.getReturnData().equals(ReturnData.IDENTIFIER)) {
        PSO responsePSO = new PSO();
        responsePSO.setPsoID(addRequest.getPsoID());
        addResponse.setPso(responsePSO);
      } else {
        LookupRequest lookupRequest = new LookupRequest();
        lookupRequest.setPsoID(addRequest.getPsoID());
        lookupRequest.setReturnData(addRequest.getReturnData());

        LookupResponse lookupResponse = this.execute(lookupRequest);
        if (lookupResponse.getStatus() == StatusCode.SUCCESS) {
          addResponse.setPso(lookupResponse.getPso());
        } else {
          fail(addResponse, lookupResponse.getError(), "Unable to lookup object after create.");
          LOG.error(PSPUtil.toString(addResponse));
          if (this.isLogSpml()) LOG.info("\n{}", this.toXML(addResponse));
          return addResponse;
        }
      }

      // TODO are all jndi exceptions caught correctly ?
    } catch (LdapPoolException e) {
      fail(addResponse, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(addResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(addResponse));
      return addResponse;
    } catch (NameAlreadyBoundException e) {
      fail(addResponse, ErrorCode.ALREADY_EXISTS, e);
      LOG.error(PSPUtil.toString(addResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(addResponse));
      return addResponse;
    } catch (NamingException e) {
      fail(addResponse, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(addResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(addResponse));
      return addResponse;
    } catch (LdappcException e) {
      // from PSO.getReferences, an unhandled capability data
      fail(addResponse, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(addResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(addResponse));
      return addResponse;
    } finally {
      ldapPool.checkIn(ldap);
    }

    addResponse.setStatus(StatusCode.SUCCESS);
    LOG.info(PSPUtil.toString(addResponse));
    if (this.isLogSpml()) LOG.info("\n{}", this.toXML(addResponse));
    return addResponse;
  }

  public DeleteResponse execute(DeleteRequest deleteRequest) {

    String msg = PSPUtil.toString(deleteRequest);
    LOG.info("{}", msg);
    if (this.isLogSpml()) LOG.info("\n{}", this.toXML(deleteRequest));

    DeleteResponse deleteResponse = new DeleteResponse();
    deleteResponse.setRequestID(this.getOrGenerateRequestID(deleteRequest));

    if (!this.isValidTargetId(deleteRequest.getPsoID(), deleteResponse)) {
      LOG.error(PSPUtil.toString(deleteResponse));
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(deleteResponse));
      return deleteResponse;
    }

    String dn = deleteRequest.getPsoID().getID();

    Ldap ldap = null;
    try {
      ldap = ldapPool.checkOut();
      LOG.info("{}", msg);
      String escapedDn = LdapUtil.escapeForwardSlash(dn);
      LOG.debug("{} escaped dn '{}'", msg, escapedDn);
      ldap.delete(escapedDn);

      // TODO are all jndi exceptions caught correctly ?
    } catch (LdapPoolException e) {
      fail(deleteResponse, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(deleteResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(deleteResponse));
      return deleteResponse;
    } catch (NameNotFoundException e) {
      fail(deleteResponse, ErrorCode.NO_SUCH_IDENTIFIER, e);
      LOG.error(PSPUtil.toString(deleteResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(deleteResponse));
      return deleteResponse;
    } catch (NamingException e) {
      fail(deleteResponse, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(deleteResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(deleteResponse));
      return deleteResponse;
    } finally {
      ldapPool.checkIn(ldap);
    }

    deleteResponse.setStatus(StatusCode.SUCCESS);
    if (this.isLogSpml()) LOG.info("\n{}", this.toXML(deleteResponse));
    return deleteResponse;
  }

  public LookupResponse execute(LookupRequest lookupRequest) {

    String msg = PSPUtil.toString(lookupRequest);
    LOG.info("{}", msg);
    if (this.isLogSpml()) LOG.info("\n{}", this.toXML(lookupRequest));

    LookupResponse lookupResponse = new LookupResponse();
    lookupResponse.setRequestID(this.getOrGenerateRequestID(lookupRequest));

    if (lookupRequest.getPsoID() == null || lookupRequest.getPsoID().getID() == null) {
      fail(lookupResponse, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_NULL_PSO_ID);
      LOG.error("{}", PSPUtil.toString(lookupResponse));
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(lookupResponse));
      return lookupResponse;
    }

    String targetId = lookupRequest.getPsoID().getTargetID();
    if (GrouperUtil.isBlank(targetId)) {
      fail(lookupResponse, ErrorCode.MALFORMED_REQUEST, PSPConstants.ERROR_NULL_TARGET_ID);
      LOG.error("{}", PSPUtil.toString(lookupResponse));
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(lookupResponse));
      return lookupResponse;
    }

    if (!targetId.equals(this.getTargetDefinition().getId())) {
      fail(lookupResponse, ErrorCode.INVALID_IDENTIFIER);
      LOG.error("{}", PSPUtil.toString(lookupResponse));
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(lookupResponse));
      return lookupResponse;
    }

    String dn = lookupRequest.getPsoID().getID();

    Ldap ldap = null;
    try {
      // will not return AD Range option attrs
      // Attributes attributes = ldap.getAttributes(escapedDn, retAttrs);

      SearchFilter sf = new SearchFilter();
      sf.setFilter("objectclass=*");
      SearchControls sc = new SearchControls();
      sc.setSearchScope(SearchControls.OBJECT_SCOPE);

      // This lookup requests attributes defined for *all* objects.
      // Perhaps there should be two searches, one for the identifier
      // and a second for attributes.
      String[] retAttrs = this.getTargetDefinition().getNames(lookupRequest.getReturnData()).toArray(new String[] {});
      sc.setReturningAttributes(retAttrs);

      // TODO logging
      String escapedDn = LdapUtil.escapeForwardSlash(dn);
      LOG.debug("{} dn '{}' attrs {}", new Object[] { msg, escapedDn, retAttrs });

      ldap = ldapPool.checkOut();
      Iterator<SearchResult> searchResults = ldap.search(escapedDn, sf, sc);

      if (!searchResults.hasNext()) {
        fail(lookupResponse, ErrorCode.NO_SUCH_IDENTIFIER);
        LOG.error("{}", PSPUtil.toString(lookupResponse));
        if (this.isLogSpml()) LOG.info("\n{}", this.toXML(lookupResponse));
        return lookupResponse;
      }

      SearchResult result = searchResults.next();

      if (searchResults.hasNext()) {
        fail(lookupResponse, ErrorCode.CUSTOM_ERROR, "More than one result found.");
        LOG.error("{}", PSPUtil.toString(lookupResponse));
        if (this.isLogSpml()) LOG.info("\n{}", this.toXML(lookupResponse));
        return lookupResponse;
      }
      Attributes attributes = result.getAttributes();

      // return attributes in order defined by config
      OrderedLdapBeanFactory orderedLdapBeanFactory = new OrderedLdapBeanFactory();
      // sort values
      SortedLdapBeanFactory sortedLdapBeanFactory = new SortedLdapBeanFactory();

      LdapAttributes ldapAttributes = orderedLdapBeanFactory.newLdapAttributes();
      for (String retAttr : retAttrs) {
        Attribute attr = attributes.get(retAttr);
        if (attr != null) {
          LdapAttribute ldapAttribute = sortedLdapBeanFactory.newLdapAttribute();
          ldapAttribute.setAttribute(attr);
          ldapAttributes.addAttribute(ldapAttribute);
        }
      }

      LdapEntry entry = sortedLdapBeanFactory.newLdapEntry();
      entry.setDn(dn);
      entry.setLdapAttributes(ldapAttributes);

      if (this.isLogLdif()) {
        LdapResult lr = sortedLdapBeanFactory.newLdapResult();
        lr.addEntry(entry);
        LdifResultConverter lrc = new LdifResultConverter();
        LOG.info("{}\n{}", msg, lrc.toLdif(lr));
      }

      // build pso
      lookupResponse.setPso(getPSO(entry, lookupRequest.getReturnData()));

    } catch (NameNotFoundException e) {
      fail(lookupResponse, ErrorCode.NO_SUCH_IDENTIFIER);
      LOG.error(PSPUtil.toString(lookupResponse));
      LOG.debug(PSPUtil.toString(lookupResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(lookupResponse));
      return lookupResponse;
    } catch (LdapPoolException e) {
      fail(lookupResponse, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(lookupResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(lookupResponse));
      return lookupResponse;
    } catch (InvalidNameException e) {
      fail(lookupResponse, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(lookupResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(lookupResponse));
      return lookupResponse;
    } catch (NamingException e) {
      fail(lookupResponse, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(lookupResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(lookupResponse));
      return lookupResponse;
    } catch (DSMLProfileException e) {
      fail(lookupResponse, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(lookupResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(lookupResponse));
      return lookupResponse;
    } catch (Spml2Exception e) {
      fail(lookupResponse, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(lookupResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(lookupResponse));
      return lookupResponse;
    } finally {
      ldapPool.checkIn(ldap);
    }

    lookupResponse.setStatus(StatusCode.SUCCESS);
    LOG.info("{}", PSPUtil.toString(lookupResponse));
    if (this.isLogSpml()) LOG.info("\n{}", this.toXML(lookupResponse));
    return lookupResponse;
  }

  public ModifyResponse execute(ModifyRequest modifyRequest) {

    String msg = PSPUtil.toString(modifyRequest);
    LOG.info("{}", msg);
    if (this.isLogSpml()) LOG.info("\n{}", this.toXML(modifyRequest));

    ModifyResponse response = new ModifyResponse();
    response.setRequestID(this.getOrGenerateRequestID(modifyRequest));

    if (!this.getPSP().isValid(modifyRequest, response)) {
      LOG.error(PSPUtil.toString(response));
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(response));
      return response;
    }

    if (!this.isValidTargetId(modifyRequest.getPsoID(), response)) {
      LOG.error(PSPUtil.toString(response));
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(response));
      return response;
    }

    String dn = modifyRequest.getPsoID().getID();

    Ldap ldap = null;
    try {
      List<ModificationItem> modificationItems = new ArrayList<ModificationItem>();
      for (Modification modification : modifyRequest.getModifications()) {
        modificationItems.addAll(this.getDsmlMods(modification));
        modificationItems.addAll(this.getReferenceMods(modification));
      }

      ldap = ldapPool.checkOut();

      LOG.debug("{} mods {}", msg, modificationItems);
      String escapedDn = LdapUtil.escapeForwardSlash(dn);
      LOG.debug("{} escaped dn '{}'", msg, escapedDn);
      ldap.modifyAttributes(escapedDn, modificationItems.toArray(new ModificationItem[] {}));

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
          fail(response, lookupResponse.getError());
          LOG.error(PSPUtil.toString(response));
          if (this.isLogSpml()) LOG.info("\n{}", this.toXML(response));
          return response;
        }
      }

    } catch (LdapPoolException e) {
      fail(response, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(response), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(response));
      return response;
    } catch (LdappcException e) {
      fail(response, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(response), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(response));
      return response;
    } catch (NamingException e) {
      fail(response, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(response), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(response));
      return response;
    } finally {
      ldapPool.checkIn(ldap);
    }

    response.setStatus(StatusCode.SUCCESS);
    LOG.info(PSPUtil.toString(response));
    if (this.isLogSpml()) LOG.info("\n{}", this.toXML(response));
    return response;
  }

  public SearchResponse execute(SearchRequest searchRequest) {

    String msg = PSPUtil.toString(searchRequest);
    LOG.info("{}", msg);
    if (this.isLogSpml()) LOG.info("\n{}", this.toXML(searchRequest));

    SearchResponse searchResponse = new SearchResponse();
    searchResponse.setRequestID(this.getOrGenerateRequestID(searchRequest));

    // query
    Query query = searchRequest.getQuery();
    if (GrouperUtil.isBlank(query)) {
      fail(searchResponse, ErrorCode.MALFORMED_REQUEST, "A query is required.");
      LOG.error("{}", searchResponse);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(searchResponse));
      return searchResponse;
    }

    // query target id
    if (query.getTargetID() != null && !query.getTargetID().equals(this.getTargetDefinition().getId())) {
      fail(searchResponse, ErrorCode.MALFORMED_REQUEST, "Target ID " + query.getTargetID()
          + " does not match this target " + this.getTargetDefinition().getId());
      LOG.error("{}", searchResponse);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(searchResponse));
      return searchResponse;
    }

    // query filter
    // TODO support QueryClause other than our own
    String filter = null;
    for (QueryClause queryClause : query.getQueryClauses()) {
      if (queryClause instanceof LdapFilterQueryClause) {
        filter = ((LdapFilterQueryClause) queryClause).getFilter();
      }
      if (queryClause instanceof HasReference) {
        HasReference hasReference = (HasReference) queryClause;
        if (hasReference.getTypeOfReference() != null && hasReference.getToPsoID() != null
            && hasReference.getToPsoID().getID() != null) {
          filter = "(" + hasReference.getTypeOfReference() + "=" + hasReference.getToPsoID().getID() + ")";
          // TODO what do we do with hasReference.getReferenceData(); ?
        }
      }
    }
    if (GrouperUtil.isBlank(filter)) {
      fail(searchResponse, ErrorCode.MALFORMED_REQUEST, "A filter is required.");
      LOG.error("{}", searchResponse);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(searchResponse));
      return searchResponse;
    }

    // query base
    if (query.getBasePsoID() == null || query.getBasePsoID().getID() == null) {
      fail(searchResponse, ErrorCode.MALFORMED_REQUEST, "A basePsoID is required.");
      LOG.error("{}", searchResponse);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(searchResponse));
      return searchResponse;
    }
    String base = query.getBasePsoID().getID();

    SearchControls searchControls = new SearchControls();

    // query scope
    Scope scope = query.getScope();
    if (scope != null) {
      searchControls.setSearchScope(PSPUtil.getScope(scope));
    }

    Ldap ldap = null;
    try {
      String[] retAttrs = this.getTargetDefinition().getNames(searchRequest.getReturnData()).toArray(new String[] {});
      searchControls.setReturningAttributes(retAttrs);

      ldap = ldapPool.checkOut();

      LOG.debug("{} retAttrs {}", msg, Arrays.asList(retAttrs));
      Iterator<SearchResult> searchResults = ldap.search(base, new SearchFilter(filter), searchControls);

      SortedLdapBeanFactory ldapBeanFactory = new SortedLdapBeanFactory();
      LdapResult ldapResult = ldapBeanFactory.newLdapResult();
      ldapResult.addEntries(searchResults);

      Collection<LdapEntry> entries = ldapResult.getEntries();
      LOG.debug("{} found {}", msg, entries.size());
      for (LdapEntry entry : entries) {
        searchResponse.addPSO(getPSO(entry, searchRequest.getReturnData()));
      }

      if (logLdif) {
        Ldif ldif = new Ldif();
        LOG.info("{}:\n{}", msg, ldif.createLdif(ldapResult));
      }

      // TODO are all jndi exceptions caught correctly ?
    } catch (NameNotFoundException e) {
      fail(searchResponse, ErrorCode.NO_SUCH_IDENTIFIER, e);
      LOG.error(PSPUtil.toString(searchResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(searchResponse));
      return searchResponse;
    } catch (NamingException e) {
      fail(searchResponse, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(searchResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(searchResponse));
      return searchResponse;
    } catch (LdapPoolException e) {
      fail(searchResponse, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(searchResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(searchResponse));
      return searchResponse;
    } catch (Spml2Exception e) {
      fail(searchResponse, ErrorCode.CUSTOM_ERROR, e);
      LOG.error(PSPUtil.toString(searchResponse), e);
      if (this.isLogSpml()) LOG.info("\n{}", this.toXML(searchResponse));
      return searchResponse;
    } finally {
      ldapPool.checkIn(ldap);
    }

    searchResponse.setStatus(StatusCode.SUCCESS);
    LOG.info("{}", PSPUtil.toString(searchResponse));
    if (this.isLogSpml()) LOG.info("\n{}", this.toXML(searchResponse));
    return searchResponse;
  }

  protected PSO getPSO(LdapEntry entry, ReturnData returnData) throws Spml2Exception {

    String msg = "get pso for '" + entry.getDn() + "' target '" + this.getTargetDefinition().getId() + "'";

    PSO pso = new PSO();

    // determine schema entity
    PSODefinition psoDefinition = this.getPSODefinition(entry);
    LOG.debug("{} schema entity '{}'", msg, psoDefinition.getId());
    pso.addOpenContentAttr(PSODefinition.ENTITY_NAME_ATTRIBUTE, psoDefinition.getId());

    PSOIdentifier psoID = new PSOIdentifier();
    psoID.setTargetID(this.getTargetDefinition().getId());

    try {
      psoID.setID(LdapUtil.canonicalizeDn(entry.getDn()));
    } catch (InvalidNameException e) {
      LOG.error(msg + " Unable to canonicalize entry dn.", e);
      throw new Spml2Exception(e);
    }

    // TODO skipping container id for now
    // String baseId = psoDefinition.getPsoIdentifierDefinition().getBaseId();
    // if (baseId != null) {
    // PSOIdentifier containerID = new PSOIdentifier();
    // containerID.setID(baseId);
    // containerID.setTargetID(this.getTargetDefinition().getId());
    // psoID.setContainerID(containerID);
    // }

    pso.setPsoID(psoID);

    LdapAttributes ldapAttributes = entry.getLdapAttributes();

    if (returnData.equals(ReturnData.DATA) || returnData.equals(ReturnData.EVERYTHING)) {
      // TODO this is ugly; ldap attribute names are case insensitive
      Map<String, String> attributeNameMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
      for (String attributeName : psoDefinition.getAttributeNames()) {
        attributeNameMap.put(attributeName, attributeName);
      }
      Map<String, String> referenceNameMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
      if (returnData.equals(ReturnData.EVERYTHING)) {
        for (String referenceName : psoDefinition.getReferenceNames()) {
          referenceNameMap.put(referenceName, referenceName);
        }
      }

      Extensible data = new Extensible();
      List<Reference> references = new ArrayList<Reference>();

      for (LdapAttribute ldapAttribute : ldapAttributes.getAttributes()) {
        if (attributeNameMap.containsKey(ldapAttribute.getName())) {
          data.addOpenContentElement(this.getDsmlAttr(attributeNameMap.get(ldapAttribute.getName()), ldapAttribute
              .getStringValues()));
        } else if (returnData.equals(ReturnData.EVERYTHING) && referenceNameMap.containsKey(ldapAttribute.getName())) {
          references.addAll(this
              .getReferences(referenceNameMap.get(ldapAttribute.getName()), ldapAttribute.getStringValues()));
        } else {
          LOG.trace("{} ignoring attribute '{}'", msg, ldapAttribute.getName());
        }

        if (data.getOpenContentElements().length > 0) {
          pso.setData(data);
        }      
      }      
      if (returnData.equals(ReturnData.EVERYTHING)) {
        PSPUtil.setReferences(pso, references);
      }
    }

    return pso;
  }

  /**
   * Determine the schema entity appropriate for the given <code>LdapEntry</code>.
   * 
   * @param entry
   *          the <code>LdapEntry</code>
   * @return the <code>PSODefintion</code>
   * @throws LdappcException
   *           if the schema entity cannot be determined.
   */
  protected PSODefinition getPSODefinition(LdapEntry entry) throws LdappcException {

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

  protected DSMLAttr getDsmlAttr(String name, Collection<String> values) throws DSMLProfileException {
    DSMLValue[] dsmlValues = null;
    DSMLAttr dsmlAttr = new DSMLAttr(name, dsmlValues);
    for (String value : values) {
      dsmlAttr.addValue(new DSMLValue(value));
    }
    return dsmlAttr;
  }

  protected List<Reference> getReferences(String name, Collection<String> values) throws Spml2Exception {
    try {
      List<Reference> references = new ArrayList<Reference>();
      for (String value : values) {
        Reference reference = new Reference();
        PSOIdentifier toPSOId = new PSOIdentifier();
        toPSOId.setID(LdapUtil.canonicalizeDn(value));
        toPSOId.setTargetID(this.getTargetDefinition().getId());

        // TODO containerID ?
        // PSOIdentifier containerID = new PSOIdentifier();
        // containerID.setID(LdapUtil.getParentDn(toPSOId.getID()));
        // containerID.setTargetID(this.getTargetDefinition().getId());
        // toPSOId.setContainerID(containerID);

        reference.setToPsoID(toPSOId);
        reference.setTypeOfReference(name);
        references.add(reference);
      }
      return references;
    } catch (InvalidNameException e) {
      LOG.error("Unable to canonicalize name", e);
      throw new Spml2Exception(e);
    }
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
          // if (id.equals(PSOReferencesDefinition.EMPTY_STRING)) {
          // id = "";
          // }
          if (id == null) {
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

  /**
   * 
   * @param addRequest
   * @throws DSMLProfileException
   */
  protected void handleEmptyReferences(AddRequest addRequest) throws DSMLProfileException {

    if (!addRequest.getReturnData().equals(ReturnData.DATA)) {
      return;
    }

    // TODO logging and errors

    LOG.trace("add request before:\n{}", this.toXML(addRequest));

    String entityName = addRequest.findOpenContentAttrValueByName(PSODefinition.ENTITY_NAME_ATTRIBUTE);
    if (entityName == null) {
      LOG.debug("TODO");
      return;
    }

    PSODefinition psoDefinition = this.getTargetDefinition().getPSODefinition(entityName);
    if (psoDefinition == null) {
      LOG.debug("TODO");
      return;
    }

    Map<String, DSMLAttr> dsmlAttrs = PSP.getDSMLAttrMap(addRequest.getData());

    for (PSOReferencesDefinition refsDef : psoDefinition.getReferenceDefinitions()) {
      String emptyValue = refsDef.getEmptyValue();
      if (emptyValue != null) {
        DSMLAttr member = dsmlAttrs.get(refsDef.getName());
        if (member == null || member.getValues().length == 0) {
          LOG.debug("TODO");
          addRequest.getData().addOpenContentElement(new DSMLAttr(refsDef.getName(), refsDef.getEmptyValue()));
        }
      }
    }

    LOG.trace("add request after:\n{}", this.toXML(addRequest));
  }

  public boolean isValidTargetId(PSOIdentifier psoID, Response response) {

    if (!psoID.getTargetID().equals(this.getTargetDefinition().getId())) {
      fail(response, ErrorCode.INVALID_IDENTIFIER);
      return false;
    }

    return true;
  }
}
