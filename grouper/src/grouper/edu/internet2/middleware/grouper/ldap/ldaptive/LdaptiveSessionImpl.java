/**
 * Copyright 2014 Internet2
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
 */
package edu.internet2.middleware.grouper.ldap.ldaptive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AddResponse;
import org.ldaptive.AttributeModification;
import org.ldaptive.BindOperation;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionInitializer;
import org.ldaptive.ConnectionValidator;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DeleteResponse;
import org.ldaptive.FilterTemplate;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyDnOperation;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyDnResponse;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ModifyResponse;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.Result;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchScope;
import org.ldaptive.SimpleBindRequest;
import org.ldaptive.control.util.PagedResultsClient;
import org.ldaptive.dn.Dn;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.ResultPredicate;
import org.ldaptive.handler.SearchResultHandler;
import org.ldaptive.props.SearchRequestPropertySource;
import org.ldaptive.referral.DefaultReferralConnectionFactory;
import org.ldaptive.referral.FollowAddReferralHandler;
import org.ldaptive.referral.FollowDeleteReferralHandler;
import org.ldaptive.referral.FollowModifyDnReferralHandler;
import org.ldaptive.referral.FollowModifyReferralHandler;
import org.ldaptive.referral.FollowSearchReferralHandler;
import org.ldaptive.referral.FollowSearchResultReferenceHandler;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.ldap.LdapConfiguration;
import edu.internet2.middleware.grouper.ldap.LdapHandler;
import edu.internet2.middleware.grouper.ldap.LdapHandlerBean;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;

/**
 * will handle the ldap config, and inverse of control for pooling
 * 
 * @author mchyzer
 */
public class LdaptiveSessionImpl implements LdapSession {

  /** class logger. */
  private static final Log LOG = GrouperUtil.getLog(LdaptiveSessionImpl.class);

  /** map of connection name to pool */
  private static final Map<String, PooledConnectionFactory> poolMap = new HashMap<>();

  /** pools that need to be cleaned up */
  private static final List<PooledConnectionFactory> poolsNeedingCleanup = new ArrayList<>();

  private static boolean hasWarnedAboutMissingDnAttributeForSearches = false;

  /**
   * debug log where lines are separated by newlines
   */
  private StringBuilder debugLog = null;

  /**
   * debug log where lines are separated by newlines
   * @return
   */
  public StringBuilder getDebugLog() {
    return debugLog;
  }

  /**
   * if we are debugging
   */
  private boolean debug = false;
  
  /**
   * if we are debugging
   * @return
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * if we should capture debug info
   * @param isDebug
   */
  public void assignDebug(boolean isDebug, StringBuilder theDebugLog) {
    this.debug = isDebug;
    if (isDebug) {
      this.debugLog = theDebugLog;
    } else {
      this.debugLog = null;
    }
  }

  /**
   * if we should capture debug info
   * @param isDebug
   */
  public void assignDebug(boolean isDebug) {
    assignDebug(isDebug, new StringBuilder());
  }

  /**
   * get or create the pool based on the server id
   * @param ldapServerId
   * @return the pool
   */
  @SuppressWarnings("unchecked")
  private static PooledConnectionFactory getPooledConnectionFactory(String ldapServerId) {
    synchronized (poolMap) {
      PooledConnectionFactory blockingLdapPool = poolMap.get(ldapServerId);
      if (blockingLdapPool == null) {
        blockingLdapPool = LdaptiveConfiguration.createPooledConnectionFactory(ldapServerId);
        poolMap.put(ldapServerId, blockingLdapPool);
      }
      return blockingLdapPool;
    }
  }

  /**
   * call this to send a callback for the ldap session object.
   * @param ldapServerId is the config id from the grouper-loader.properties
   * @param ldapHandler is the logic of the ldap calls
   * @return the result of the handler
   */
  private <R> R callbackLdapSession(String ldapServerId, LdapHandler<ConnectionFactory, R> ldapHandler) {
    try {
      PooledConnectionFactory blockingLdapPool = getPooledConnectionFactory(ldapServerId);
      if (LOG.isDebugEnabled()) {
        LOG.debug("checkout: ldap id: " + ldapServerId + ", pool active: " + blockingLdapPool.activeCount() + ", available: " + blockingLdapPool.availableCount());
      }
      LdapHandlerBean<ConnectionFactory> ldapHandlerBean = new LdapHandlerBean<>();
      ldapHandlerBean.setLdap(blockingLdapPool);
      return ldapHandler.callback(ldapHandlerBean);
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "Problem with ldap connection: " + ldapServerId);
      throw re;
    } catch (Exception e) {
      throw new RuntimeException("Problem with ldap connection: " + ldapServerId, e);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.ldap.LdapSession#list(java.lang.Class, java.lang.String, java.lang.String, edu.internet2.middleware.grouper.ldap.LdapSearchScope, java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public <R> List<R> list(final Class<R> returnType, final String ldapServerId, 
      final String searchDn, final LdapSearchScope ldapSearchScope, final String filter, final String attributeName) {
    
    try {
      
      return callbackLdapSession(ldapServerId, ldapHandlerBean -> {

        ConnectionFactory ldap = ldapHandlerBean.getLdap();

        List<R> result = new ArrayList<>();
        processSearchRequest(ldapServerId, ldap, searchDn, ldapSearchScope, filter, new String[]{attributeName}, null, entry -> {
          LdapAttribute attribute = entry.getAttribute(attributeName);
          if (attribute == null && StringUtils.equals("dn", attributeName)) {
            String nameInNamespace = entry.getDn();
            R attributeValue = GrouperUtil.typeCast(nameInNamespace, returnType);
            result.add(attributeValue);
          } else {
            if (attribute != null) {
              for (Object attributeValue : attribute.getStringValues()) {
                attributeValue = GrouperUtil.typeCast(attributeValue, returnType);
                if (attributeValue != null) {
                  result.add((R)attributeValue);
                }
              }
            }
          }
          return null;
        });

        if (LOG.isDebugEnabled()) {
          LOG.debug("Found " + result.size() + " results for serverId: " + ldapServerId + ", searchDn: " + searchDn
            + ", filter: '" + filter + "', returning attribute: "
            + attributeName + ", some results: " + GrouperUtil.toStringForLog(result, 100) );
        }

        return result;
      });
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "Error querying ldap server id: " + ldapServerId + ", searchDn: " + searchDn
          + ", filter: '" + filter + "', returning attribute: " + attributeName);
      throw re;
    }
    
  }

  /**
   * @see edu.internet2.middleware.grouper.ldap.LdapSession#listInObjects(java.lang.Class, java.lang.String, java.lang.String, edu.internet2.middleware.grouper.ldap.LdapSearchScope, java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public <R> Map<String, List<R>> listInObjects(final Class<R> returnType, final String ldapServerId, 
      final String searchDn, final LdapSearchScope ldapSearchScope, final String filter, final String attributeName) {
    
    try {
      
      return callbackLdapSession(ldapServerId, ldapHandlerBean -> {

        ConnectionFactory ldap = ldapHandlerBean.getLdap();

        Map<String, List<R>> result = new HashMap<>();
        AtomicInteger subObjectCount = new AtomicInteger();
        processSearchRequest(ldapServerId, ldap, searchDn, ldapSearchScope, filter, new String[]{attributeName}, null, entry -> {
          List<R> valueResults = new ArrayList<>();
          String nameInNamespace = entry.getDn();

          result.put(nameInNamespace, valueResults);

          LdapAttribute attribute = entry.getAttribute(attributeName);
          if (attribute != null) {
            for (Object attributeValue : attribute.getStringValues()) {
              attributeValue = GrouperUtil.typeCast(attributeValue, returnType);
              if (attributeValue != null) {
                subObjectCount.incrementAndGet();
                valueResults.add((R)attributeValue);
              }
            }
          }
          return null;
        });

        if (LOG.isDebugEnabled()) {
          LOG.debug("Found " + result.size() + " results, (" + subObjectCount + " sub-results) for serverId: " + ldapServerId + ", searchDn: " + searchDn
            + ", filter: '" + filter + "', returning attribute: "
            + attributeName + ", some results: " + GrouperUtil.toStringForLog(result, 100) );
        }

        return result;
      });
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "Error querying ldap server id: " + ldapServerId + ", searchDn: " + searchDn
          + ", filter: '" + filter + "', returning attribute: " + attributeName);
      throw re;
    }
    
  }

  /**
   * @see edu.internet2.middleware.grouper.ldap.LdapSession#list(java.lang.String, java.lang.String, edu.internet2.middleware.grouper.ldap.LdapSearchScope, java.lang.String, java.lang.String[], java.lang.Integer)
   */
  @SuppressWarnings("unchecked")
  public List<edu.internet2.middleware.grouper.ldap.LdapEntry> list(final String ldapServerId, final String searchDn,
      final LdapSearchScope ldapSearchScope, final String filter, final String[] attributeNames, final Integer sizeLimit) {

    try {
      
      return callbackLdapSession(ldapServerId, ldapHandlerBean -> {

        List<edu.internet2.middleware.grouper.ldap.LdapEntry> entries = new ArrayList<>();
        ConnectionFactory ldap = ldapHandlerBean.getLdap();
        processSearchRequest(ldapServerId, ldap, searchDn, ldapSearchScope, filter, attributeNames, sizeLimit, e -> {
          entries.add(getLdapEntryFromSearchResult(e, attributeNames));
          return null;
        });
        return entries;
      });
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "Error querying ldap server id: " + ldapServerId + ", searchDn: " + searchDn
          + ", filter: '" + filter + "', returning attributes: " + StringUtils.join(attributeNames, ", "));
      throw re;
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public List<edu.internet2.middleware.grouper.ldap.LdapEntry> read(String ldapServerId, String searchDn, List<String> dnList, String[] attributeNames) {
    try {
      return callbackLdapSession(ldapServerId, ldapHandlerBean -> {

        ConnectionFactory ldap = ldapHandlerBean.getLdap();

        List<edu.internet2.middleware.grouper.ldap.LdapEntry> results = new ArrayList<>();

        LdapConfiguration config = LdapConfiguration.getConfig(ldapServerId);
        int batchSize = config.getQueryBatchSize();

        if (StringUtils.isEmpty(config.getDnAttributeForSearches()) && !hasWarnedAboutMissingDnAttributeForSearches) {
          LOG.warn("Performance impact due to missing config: ldap." + ldapServerId + ".dnAttributeForSearches");
          hasWarnedAboutMissingDnAttributeForSearches = true;
        }

        if (!StringUtils.isEmpty(config.getDnAttributeForSearches()) && batchSize > 1) {
          int numberOfBatches = GrouperUtil.batchNumberOfBatches(GrouperUtil.length(dnList), batchSize);
          for (int i = 0; i < numberOfBatches; i++) {
            List<String> currentBatch = GrouperUtil.batchList(dnList, batchSize, i);
            StringBuilder builder = new StringBuilder();
            for (String dn : currentBatch) {
              builder.append("(").append(config.getDnAttributeForSearches()).append("=").append(FilterTemplate.encodeValue(dn)).append(")");
            }

            String filter = "(|" + builder + ")";
            processSearchRequest(ldapServerId, ldap, searchDn, LdapSearchScope.SUBTREE_SCOPE, filter, attributeNames, null, e -> {
              results.add(getLdapEntryFromSearchResult(e, attributeNames));
              return null;
            });
          }
        } else {
          for (String dn : dnList) {
            processSearchRequest(ldapServerId, ldap, dn, LdapSearchScope.OBJECT_SCOPE, "(objectclass=*)", attributeNames, null, e -> {
              results.add(getLdapEntryFromSearchResult(e, attributeNames));
              return null;
            });
          }
        }

        return results;
      });
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "Error querying ldap server id: " + ldapServerId + ", dnList size: " + dnList.size()
          + ", returning attributes: " + StringUtils.join(attributeNames, ", "));
      throw re;
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.ldap.LdapSession#authenticate(java.lang.String, java.lang.String, java.lang.String)
   */
  public void authenticate(final String ldapServerId, final String userDn, final String password) {
          
      callbackLdapSession(ldapServerId, ldapHandlerBean -> {

        ConnectionFactory ldap = ldapHandlerBean.getLdap();
        ConnectionConfig connectionConfig = ConnectionConfig.copy(ldap.getConnectionConfig());
        connectionConfig.setConnectionInitializers((ConnectionInitializer[]) null);
        ConnectionFactory ldap2 = new DefaultConnectionFactory(connectionConfig);
        BindOperation bind = new BindOperation(ldap2);
        bind.setThrowCondition(result -> !result.getResultCode().equals(ResultCode.SUCCESS));
        bind.execute(new SimpleBindRequest(userDn, new Credential(password)));
        return null;
      });

  }
  
  private SearchScope translateScope(LdapSearchScope scope) {
    if (scope == null) {
      return null;
    }
    
    SearchScope ldaptiveScope;
    if (scope == LdapSearchScope.OBJECT_SCOPE) {
      ldaptiveScope = SearchScope.OBJECT;
    } else if (scope == LdapSearchScope.ONELEVEL_SCOPE) {
      ldaptiveScope = SearchScope.ONELEVEL;
    } else if (scope == LdapSearchScope.SUBTREE_SCOPE) {
      ldaptiveScope = SearchScope.SUBTREE;
    } else {
      throw new RuntimeException("Unexpected scope " + scope);
    }
    
    return ldaptiveScope;
  }
  
  private AttributeModification.Type translateModificationType(LdapModificationType modificationType) {
    if (modificationType == null) {
      return null;
    }
    
    AttributeModification.Type ldaptiveModificationType;
    if (modificationType == LdapModificationType.ADD_ATTRIBUTE) {
      ldaptiveModificationType = AttributeModification.Type.ADD;
    } else if (modificationType == LdapModificationType.REMOVE_ATTRIBUTE) {
      ldaptiveModificationType = AttributeModification.Type.DELETE;
    } else if (modificationType == LdapModificationType.REPLACE_ATTRIBUTE) {
      ldaptiveModificationType = AttributeModification.Type.REPLACE;
    } else {
      throw new RuntimeException("Unexpected modification type " + modificationType);
    }
    
    return ldaptiveModificationType;
  }
  
  private SearchResponse processSearchRequest(
    String ldapServerId, ConnectionFactory ldap, String searchDn, LdapSearchScope ldapSearchScope, String filter, String[] attributeNames, Integer sizeLimit, LdapEntryHandler entryHandler) throws LdapException {

    SearchRequest searchRequest = new SearchRequest();

    if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("ldap." + ldapServerId + ".isActiveDirectory", false)) {
      searchRequest.setBinaryAttributes("objectSid", "objectGUID");
    }
    
    if (filter != null) {
      filter = filter.trim();
      if (filter.startsWith("${") && filter.endsWith("}")) {
        
        if (this.debug) {
          this.debugLog.append("Ldaptive filterJexl '").append(filter).append("'\n");
        }
        filter = StringUtils.replace(filter, "$newline$", "\n");
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("grouperUtil", new GrouperUtil());
        filter = (String)GrouperUtil.substituteExpressionLanguageScript(filter, variableMap, true, false, false);
      }
    }
    
    searchRequest.setFilter(new FilterTemplate(filter));
    searchRequest.setReturnAttributes(attributeNames);

    SearchRequestPropertySource srSource = new SearchRequestPropertySource(
      searchRequest, LdaptiveConfiguration.getConfig(ldapServerId).getProperties());
    srSource.initialize();
    
    // add this after the properties get initialized so that this would override if needed
    // note that the searchDn here is relative
    if (StringUtils.isNotBlank(searchDn)) {
      searchRequest.setBaseDn(searchDn);
    }
    
    if (sizeLimit != null) {
      searchRequest.setSizeLimit(sizeLimit);
    }

    if (ldapSearchScope != null) {
      searchRequest.setSearchScope(translateScope(ldapSearchScope));
    }
    
    SearchResponse response;
    LdapConfiguration ldapConfig = LdapConfiguration.getConfig(ldapServerId);

    Integer pageSize = ldapConfig.getPageSize();
    if (pageSize != null) {
      if (pageSize < 0) {
        pageSize = null;
      }
    } else if (ldapConfig.isActiveDirectory()) {
      pageSize = getDefaultActiveDirectoryPageSize(ldapServerId, ldap);
    }
    
    if (this.debug) {
      this.debugLog.append("Ldaptive searchRequest (").append(ldapServerId).append("): ").append(StringUtils.abbreviate(searchRequest.toString(), 2000)).append("\n");
    }
    List<LdapEntryHandler> entryHandlers = Arrays.stream(
      Optional.ofNullable(LdaptiveConfiguration.getConfig(ldapServerId).getLdapEntryHandlers())
        .orElse(new LdapEntryHandler[0]))
      .collect(Collectors.toList());
    if (entryHandler != null) {
      entryHandlers.add(entryHandler);
    }
    List<SearchResultHandler> resultHandlers = new ArrayList<>();
    if (LdaptiveConfiguration.getConfig(ldapServerId).getSearchResultHandlers() != null) {
      resultHandlers.addAll(Arrays.asList(LdaptiveConfiguration.getConfig(ldapServerId).getSearchResultHandlers()));
    }

    final Set<ResultCode> ignoreResultCodes = ldapConfig.getSearchIgnoreResultCodes();
    if ("follow".equals(GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".referral"))) {
      // referrals should be followed before any other search result handlers
      // the boolean parameter indicates that failure to follow referrals should raise an exception
      resultHandlers.add(0, new FollowSearchReferralHandler(new DefaultReferralConnectionFactory(ldap.getConnectionConfig()), true));
      resultHandlers.add(1, new FollowSearchResultReferenceHandler(new DefaultReferralConnectionFactory(ldap.getConnectionConfig()), true));
    }
    if (pageSize == null) {
      SearchOperation search = new SearchOperation(ldap);
      search.setThrowCondition(new IgnoreResultCodePredicate(ignoreResultCodes));
      if (!entryHandlers.isEmpty()) {
        search.setEntryHandlers(entryHandlers.toArray(new LdapEntryHandler[0]));
      }
      if (!resultHandlers.isEmpty()) {
        search.setSearchResultHandlers(resultHandlers.toArray(new SearchResultHandler[0]));
      }
      response = search.execute(searchRequest);
    } else {
      PagedResultsClient client = new PagedResultsClient(ldap, pageSize);
      client.setThrowCondition(new IgnoreResultCodePredicate(ignoreResultCodes));
      if (!entryHandlers.isEmpty()) {
        client.setEntryHandlers(entryHandlers.toArray(new LdapEntryHandler[0]));
      }
      if (!resultHandlers.isEmpty()) {
        client.setSearchResultHandlers(resultHandlers.toArray(new SearchResultHandler[0]));
      }
      response = client.executeToCompletion(searchRequest);
    }
    if (this.debug) {
      this.debugLog.append("Ldaptive searchResponse (").append(ldapServerId).append("): ").append(StringUtils.abbreviate(response.toString(), 2000)).append("\n");
    }
    return response;
  }
  
  private synchronized Integer getDefaultActiveDirectoryPageSize(String ldapServerId, ConnectionFactory ldap) {
    int pageSize = 1000;
    
    try {
      LdapEntry rootLdapEntry;

      LdapConfiguration ldapConfig = LdapConfiguration.getConfig(ldapServerId);
      final Set<ResultCode> ignoreResultCodes = ldapConfig.getSearchIgnoreResultCodes();

      SearchOperation search = new SearchOperation(ldap);
      if ("follow".equals(GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".referral"))) {
        search.setSearchResultHandlers(
          new FollowSearchReferralHandler(new DefaultReferralConnectionFactory(ldap.getConnectionConfig())),
          new FollowSearchResultReferenceHandler(new DefaultReferralConnectionFactory(ldap.getConnectionConfig())));
      }
      search.setThrowCondition(new IgnoreResultCodePredicate(ldapConfig.getSearchIgnoreResultCodes()));

      SearchResponse response = search.execute(SearchRequest.builder()
        .dn("")
        .scope(SearchScope.OBJECT)
        .filter(new FilterTemplate("(objectClass=*)"))
        .returnAttributes("configurationNamingContext")
        .build());
      rootLdapEntry = response.getEntry();
      if (rootLdapEntry != null && rootLdapEntry.getAttribute("configurationNamingContext") != null && !GrouperUtil.isEmpty(rootLdapEntry.getAttribute("configurationNamingContext").getStringValue())) {
        String configurationDn = rootLdapEntry.getAttribute("configurationNamingContext").getStringValue();
        
        response = search.execute(SearchRequest.builder()
          .dn(configurationDn)
          .scope(SearchScope.SUBTREE)
          .filter(new FilterTemplate("(&(objectClass=queryPolicy)(cn=Default Query Policy))"))
          .returnAttributes("lDAPAdminLimits")
          .build());
        LdapEntry queryPolicyLdapEntry = response.getEntry();
        if (queryPolicyLdapEntry != null && queryPolicyLdapEntry.getAttribute("lDAPAdminLimits") != null) {
          for (String adminLimit : GrouperUtil.nonNull(queryPolicyLdapEntry.getAttribute("lDAPAdminLimits").getStringValues())) {
            if (adminLimit != null && adminLimit.startsWith("MaxPageSize=")) {
              String pageSizeString = adminLimit.substring("MaxPageSize=".length());
              pageSize = Integer.parseInt(pageSizeString);
              LOG.warn("Using pagedResultsSize from " + queryPolicyLdapEntry.getDn());
              break;
            }
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Exception trying to determine default Active Directory page size", e);
    }
    
    LOG.warn("pagedResultsSize is not set for '" + ldapServerId + "' even though it is usually required with Active Directory. Set to -1 to force no paging. Defaulting to " + pageSize + ".");
    
    LdapConfiguration.getConfig(ldapServerId).setPageSize(pageSize);
    
    return pageSize;
  }

  private edu.internet2.middleware.grouper.ldap.LdapEntry getLdapEntryFromSearchResult(LdapEntry searchResult, String[] attributeNames) {
    String nameInNamespace = searchResult.getDn();

    edu.internet2.middleware.grouper.ldap.LdapEntry entry = new edu.internet2.middleware.grouper.ldap.LdapEntry(nameInNamespace);

    boolean useAttributeNamesFromResult = false;

    if (attributeNames == null) {
      useAttributeNamesFromResult = true;
    } else {
      // ReturnAttribute could be all user or all operational or both
      for (String attributeName : attributeNames) {
        if (StringUtils.equals("*", attributeName)) {
          useAttributeNamesFromResult = true;
        } else if (StringUtils.equals("+", attributeName)) {
          useAttributeNamesFromResult = true;
        }
      }
    }

    if (useAttributeNamesFromResult) {
      attributeNames = searchResult.getAttributeNames();
    }
    for (String attributeName : attributeNames) {
      edu.internet2.middleware.grouper.ldap.LdapAttribute attribute = new edu.internet2.middleware.grouper.ldap.LdapAttribute(attributeName);

      LdapAttribute sourceAttribute = searchResult.getAttribute(attributeName);
      if (sourceAttribute != null) {
        if (sourceAttribute.isBinary()) {
          attribute.setBinaryValues(sourceAttribute.getBinaryValues());
        } else {
          attribute.setStringValues(sourceAttribute.getStringValues());
        }
      }

      entry.addAttribute(attribute);
    }

    return entry;
  }

  @Override
  public void delete(final String ldapServerId, final String dn) {

    try {
      
      if (GrouperUtil.isEmpty(dn)) {
        throw new RuntimeException("No dn!");
      }
      
      callbackLdapSession(ldapServerId, ldapHandlerBean -> {

        ConnectionFactory ldap = ldapHandlerBean.getLdap();

        DeleteOperation delete = new DeleteOperation(ldap);
        delete.setThrowCondition(result -> !result.getResultCode().equals(ResultCode.SUCCESS));
        DeleteRequest deleteRequest = new DeleteRequest(dn);

        if ("follow".equals(GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".referral"))) {
          delete.setReferralResultHandler(new FollowDeleteReferralHandler(new DefaultReferralConnectionFactory(ldap.getConnectionConfig()), true));
        }
        if (debug) {
          debugLog.append("Ldaptive deleteRequest (").append(ldapServerId).append("): ").append(StringUtils.abbreviate(deleteRequest.toString(), 2000)).append("\n");
        }

        try {
          DeleteResponse response = delete.execute(deleteRequest);
          if (debug) {
            debugLog.append("Ldaptive deleteResponse (").append(ldapServerId).append("): ").append(StringUtils.abbreviate(response.toString(), 2000)).append("\n");
          }
          return null;
        } catch (LdapException e) {
          // note that this only happens if an intermediate context does not exist
          if (e.getResultCode() == ResultCode.NO_SUCH_OBJECT) {
            if (debug) {
              debugLog.append("Ldaptive deleteResultCode (").append(ldapServerId).append("): NO_SUCH_OBJECT\n");
            }
            return null;
          }

          if (debug) {
            debugLog.append("Ldaptive delete error (").append(ldapServerId).append("): ").append(GrouperUtil.getFullStackTrace(e)).append("\n");
          }

          // TODO should we re-query just to be sure?
          throw e;
        }
      });
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "Error deleting entry server id: " + ldapServerId + ", dn: " + dn);
      throw re;
    }
  }
  
  @Override
  public boolean create(final String ldapServerId, final edu.internet2.middleware.grouper.ldap.LdapEntry ldapEntry) {
    
    // if create failed because object is there, then do an update with the attributes that were given
    // some attributes given may have no values and therefore clear those attributes
    // true if created, false if updated

    try {
      if (GrouperUtil.isEmpty(ldapEntry.getDn())) {
        throw new RuntimeException("No dn!");
      }
      
      return callbackLdapSession(ldapServerId, ldapHandlerBean -> {

        ConnectionFactory ldap = ldapHandlerBean.getLdap();

        List<LdapAttribute> ldaptiveAttributes = new ArrayList<>(); // if doing create
        List<AttributeModification> ldaptiveModifications = new ArrayList<>(); // if doing modify

        for (edu.internet2.middleware.grouper.ldap.LdapAttribute grouperLdapAttribute : ldapEntry.getAttributes()) {
          LdapAttribute ldaptiveAttribute = new LdapAttribute(grouperLdapAttribute.getName());
          if (grouperLdapAttribute.getStringValues().size() > 0) {
            ldaptiveAttribute.addStringValues(grouperLdapAttribute.getStringValues());
          } else if (grouperLdapAttribute.getBinaryValues().size() > 0) {
            ldaptiveAttribute.addBinaryValues(grouperLdapAttribute.getBinaryValues());
          }

          if (ldaptiveAttribute.size() > 0) {
            ldaptiveAttributes.add(ldaptiveAttribute);
          }

          ldaptiveModifications.add(new AttributeModification(AttributeModification.Type.REPLACE, ldaptiveAttribute));
        }

        AddOperation add = new AddOperation(ldap);
        add.setThrowCondition(result -> !result.getResultCode().equals(ResultCode.SUCCESS));
        AddRequest addRequest = new AddRequest(ldapEntry.getDn(), ldaptiveAttributes);

        if ("follow".equals(GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".referral"))) {
          add.setReferralResultHandler(new FollowAddReferralHandler(new DefaultReferralConnectionFactory(ldap.getConnectionConfig()), true));
        }
        if (debug) {
          debugLog.append("Ldaptive addRequest (").append(ldapServerId).append("): ").append(StringUtils.abbreviate(addRequest.toString(), 2000)).append("\n");
        }

        try {
          AddResponse response = add.execute(addRequest);
          if (debug) {
            debugLog.append("Ldaptive addResponse (").append(ldapServerId).append("): ").append(StringUtils.abbreviate(response.toString(), 2000)).append("\n");
          }
          return true;
        } catch (LdapException e) {

          // update attributes instead
          if (e.getResultCode() == ResultCode.ENTRY_ALREADY_EXISTS) {
            if (debug) {
              debugLog.append("Ldaptive addResponse (").append(ldapServerId).append("): ENTRY_ALREADY_EXISTS\n");
            }
            ModifyOperation modify = new ModifyOperation(ldap);
            modify.setThrowCondition(result -> !result.getResultCode().equals(ResultCode.SUCCESS));
            ModifyRequest modifyRequest = new ModifyRequest(ldapEntry.getDn(), ldaptiveModifications.toArray(new AttributeModification[] { }));

            if ("follow".equals(GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".referral"))) {
              modify.setReferralResultHandler(new FollowModifyReferralHandler(new DefaultReferralConnectionFactory(ldap.getConnectionConfig()), true));
            }
            if (debug) {
              debugLog.append("Ldaptive addModifyRequest (").append(ldapServerId).append("): ").append(StringUtils.abbreviate(modifyRequest.toString(), 2000)).append("\n");
            }

            ModifyResponse response = modify.execute(modifyRequest);
            if (debug) {
              debugLog.append("Ldaptive addModifyResponse (").append(ldapServerId).append("): ").append(StringUtils.abbreviate(response.toString(), 2000)).append("\n");
            }
            return false;
          }
          if (debug) {
            debugLog.append("Ldaptive add error (").append(ldapServerId).append("): ").append(GrouperUtil.getFullStackTrace(e)).append("\n");
          }
          throw e;
        }
      });
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "Error creating entry server id: " + ldapServerId + ", dn: " + ldapEntry.getDn());
      throw re;
    }
  }

  @Override
  public boolean move(final String ldapServerId, final String oldDn, final String newDn) {
    // return true if moved
    // return false if newDn exists and oldDn doesn't
    try {
      
      if (GrouperUtil.isEmpty(oldDn)) {
        throw new RuntimeException("No oldDn!");
      }
      
      if (GrouperUtil.isEmpty(newDn)) {
        throw new RuntimeException("No newDn!");
      }

      //if (!new Dn(oldDn).getParent().isSame(new Dn(newDn).getParent())) {
      //  throw new RuntimeException("oldDn and newDn must have the same parent DN");
      //}

      return callbackLdapSession(ldapServerId, ldapHandlerBean -> {

        ConnectionFactory ldap = ldapHandlerBean.getLdap();

        String parentDnIfDifferent = null;
        if (!new Dn(oldDn).getParent().isSame(new Dn(newDn).getParent())) {
          parentDnIfDifferent = new Dn(newDn).getParent().format(null);
        }
        
        ModifyDnOperation modifyDn = new ModifyDnOperation(ldap);
        modifyDn.setThrowCondition(result -> !result.getResultCode().equals(ResultCode.SUCCESS));
        ModifyDnRequest modifyDnRequest = new ModifyDnRequest(oldDn, new Dn(newDn).getRDn().format(null), true, parentDnIfDifferent);

        if ("follow".equals(GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".referral"))) {
          modifyDn.setReferralResultHandler(new FollowModifyDnReferralHandler(new DefaultReferralConnectionFactory(ldap.getConnectionConfig()), true));
        }
        if (debug) {
          debugLog.append("Ldaptive moveRequest (").append(ldapServerId).append("): ").append(StringUtils.abbreviate(modifyDnRequest.toString(), 2000)).append("\n");
        }
        try {
          ModifyDnResponse response = modifyDn.execute(modifyDnRequest);
          if (debug) {
            debugLog.append("Ldaptive moveResponse (").append(ldapServerId).append("): ").append(StringUtils.abbreviate(response.toString(), 2000)).append("\n");
          }
          return true;
        } catch (LdapException e) {
          if (e.getResultCode() == ResultCode.NO_SUCH_OBJECT) {

            if (debug) {
              debugLog.append("Ldaptive moveResponse (").append(ldapServerId).append("): NO_SUCH_OBJECT\n");
            }
            // old entry doesn't exist.  if the new one does, then let's assume it was already renamed and return false
            // note that this exception could also happen if the oldDn exists but the newDn is an invalid location - in that case we should still end up throwing the original exception below

            try {
              processSearchRequest(ldapServerId, ldap, newDn, LdapSearchScope.OBJECT_SCOPE, "(objectclass=*)", new String[] { "objectclass" }, null, null);
              return false;
            } catch (LdapException e2) {
              if (e2.getResultCode() == ResultCode.NO_SUCH_OBJECT) {
                // throw original exception
                throw e;
              }

              // something else went wrong so throw this
              throw e2;
            }
          }
          if (debug) {
            debugLog.append("Ldaptive move error (").append(ldapServerId).append("): ").append(GrouperUtil.getFullStackTrace(e)).append("\n");
          }
          throw e;
        }
      });
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "Error moving entry server id: " + ldapServerId + ", oldDn: " + oldDn + ", newDn: " + newDn);
      throw re;
    }
  }

  @Override
  public void internal_modifyHelper(final String ldapServerId, String dn, final List<LdapModificationItem> ldapModificationItems) {

    if (ldapModificationItems.size() == 0) {
      return;
    }
    
    try {
      
      if (GrouperUtil.isEmpty(dn)) {
        throw new RuntimeException("No dn!");
      }
      
      callbackLdapSession(ldapServerId, ldapHandlerBean -> {

        ConnectionFactory ldap = ldapHandlerBean.getLdap();

        List<AttributeModification> ldaptiveModifications = new ArrayList<>();

        for (LdapModificationItem ldapModificationItem : ldapModificationItems) {
          LdapAttribute ldaptiveAttribute = new LdapAttribute(ldapModificationItem.getAttribute().getName());
          if (ldapModificationItem.getAttribute().getStringValues().size() > 0) {
            ldaptiveAttribute.addStringValues(ldapModificationItem.getAttribute().getStringValues());
          } else if (ldapModificationItem.getAttribute().getBinaryValues().size() > 0) {
            ldaptiveAttribute.addBinaryValues(ldapModificationItem.getAttribute().getBinaryValues());
          }

          ldaptiveModifications.add(new AttributeModification(translateModificationType(ldapModificationItem.getLdapModificationType()), ldaptiveAttribute));
        }

        ModifyOperation modify = new ModifyOperation(ldap);
        modify.setThrowCondition(result -> !result.getResultCode().equals(ResultCode.SUCCESS));
        modify.setExceptionHandler(e -> {throw new RuntimeException(e);});
        ModifyRequest modifyRequest = new ModifyRequest(dn, ldaptiveModifications.toArray(new AttributeModification[] { }));

        if ("follow".equals(GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".referral"))) {
          modify.setReferralResultHandler(new FollowModifyReferralHandler(new DefaultReferralConnectionFactory(ldap.getConnectionConfig()), true));
        }
        if (debug) {
          debugLog.append("Ldaptive modifyRequest (").append(ldapServerId).append("): ").append(StringUtils.abbreviate(modifyRequest.toString(), 2000)).append("\n");
        }

        ModifyResponse response = modify.execute(modifyRequest);
        if (debug) {
          debugLog.append("Ldaptive modifyResponse (").append(ldapServerId).append("): ").append(StringUtils.abbreviate(response.toString(), 2000)).append("\n");
        }
        return null;
      });
    } catch (RuntimeException re) {
      if (debug) {
        debugLog.append("Ldaptive modify error (").append(ldapServerId).append("): ").append(GrouperUtil.getFullStackTrace(re)).append("\n");
      }
      GrouperUtil.injectInException(re, "Error modifying entry server id: " + ldapServerId + ", dn: " + dn);
      throw re;
    }
  }

  @Override
  public boolean testConnection(final String ldapServerId) {
    PooledConnectionFactory ldap = getPooledConnectionFactory(ldapServerId);
    ConnectionValidator validator = ldap.getValidator();
    boolean valid = false;
    if (validator != null) {
      valid = callbackLdapSession(ldapServerId, ldapHandlerBean -> {
        try (Connection conn = ldapHandlerBean.getLdap().getConnection()) {
          conn.open();
          return validator.apply(conn);
        }
      });
    }
    // if not valid, maybe this will throw a useful exception
    if (validator == null || !valid) {
      String user = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".user");
      String pass = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".pass");
      pass = Morph.decryptIfFile(pass);
      authenticate(ldapServerId, user, pass);
    }
    return valid;
  }
  
  public void refreshConnectionsIfNeeded(final String ldapServerId) {
    synchronized (poolMap) {
      Iterator<PooledConnectionFactory> poolsNeedingCleanupIter = poolsNeedingCleanup.iterator();
      while (poolsNeedingCleanupIter.hasNext()) {
        PooledConnectionFactory pool = poolsNeedingCleanupIter.next();
        if (pool.activeCount() == 0) {
          pool.close();
          poolsNeedingCleanupIter.remove();
          LOG.warn("Closed old LDAP pool after confirming not in use.");
        } else {
          LOG.warn("Unable to close old LDAP pool since it is being used.  Will check again later.");
        }
      }

      if (poolMap.containsKey(ldapServerId) && LdaptiveConfiguration.hasConfig(ldapServerId)) {
        LdaptiveConfiguration.Config currentConfig = LdaptiveConfiguration.getConfig(ldapServerId);
        LdaptiveConfiguration.Config newConfig = LdaptiveConfiguration.createConfig(ldapServerId);
        if (!currentConfig.equals(newConfig)) {
          PooledConnectionFactory pool = poolMap.remove(ldapServerId);
          poolsNeedingCleanup.add(pool);
          LdapConfiguration.removeConfig(ldapServerId);
          LdaptiveConfiguration.removeConfig(ldapServerId);
        }
      }
    }
  }
  
  /**
   * Used by unit tests
   */
  public static void internal_closeAllPools() {
    
    if (poolMap != null) {
      for (String id : new HashSet<String>(poolMap.keySet())) {
        PooledConnectionFactory pool = poolMap.get(id);
        
        try {
          pool.close();
        } catch (Exception e) {
          LOG.warn("Error closing pool " + id, e);
        } finally {
          poolMap.remove(id);
        }
      }
    }
  }

  /** Custom predicate for ignoring specific result codes. */
  private static class IgnoreResultCodePredicate implements ResultPredicate {

    private final Set<ResultCode> ignoreResultCodes;

    IgnoreResultCodePredicate(final Set<ResultCode> codes) {
      ignoreResultCodes = new HashSet<>(codes);
    }

    @Override
    public boolean test(Result result) {
      return !result.getResultCode().equals(ResultCode.SUCCESS) && !ignoreResultCodes.contains(result.getResultCode());
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "@" + hashCode() + "::" + "ignoreResultCodes=" + ignoreResultCodes;
    }
  }
}
