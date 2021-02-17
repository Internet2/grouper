package edu.internet2.middleware.grouper.app.loader.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

/**
 * 
 * @author mchyzer
 *
 */
public class LdapLookup {

  /**
   * count how many filters we run
   */
  static int test_filterCount = 0;
  
  /** if a value is this its missing */
  private static final String MISSING_ENTRY = "_m-I_s!S@i#N$g";

  /**
   * 
   * @param entry
   * @return the entry of the missing entry string
   */
  private static String massageEntryToCache(String entry) {
    if (StringUtils.isBlank(entry)) {
      return MISSING_ENTRY;
    }
    return entry;
  }
  
  /**
   * 
   * @param entry
   * @return the entry or convert missing entry string into null
   */
  private static String massageEntryFromCache(String entry) {
    if (StringUtils.equals(entry, MISSING_ENTRY)) {
      return null;
    }
    return entry;
  }
  
  public LdapLookup() {
  }

  /**
   * configId to use
   */
  private String ldapConfigId;

  /**
   * ldap config id
   * @param theLdapConfigId
   * @return this for chaining
   */
  public LdapLookup assignLdapConfigId(String theLdapConfigId) {
    this.ldapConfigId = theLdapConfigId;
    return this;
  }
  
  /**
   * where to search, could use %TERM%
   */
  private String searchDn;

  /**
   * search dn
   * @param theSearchDn
   * @return this for chaining
   */
  public LdapLookup assignSearchDn(String theSearchDn) {
    this.searchDn = theSearchDn;
    return this;
  }
  
  /**
   * scope to search, OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
   */
  private Object searchScope;

  /**
   * search scope, OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
   * @param theSearchScope
   * @return this for chaining
   */
  public LdapLookup assignSearchScope(String theSearchScope) {
    this.searchScope = theSearchScope;
    return this;
  }
  
  /**
   * filter to search on substitute: %TERM%
   */
  private String filter;

  /**
   * 
   * @param theFilter
   * @return this for chaining
   */
  public LdapLookup assignFilter(String theFilter) {
    this.filter = theFilter;
    return this;
  }
  
  /**
   * attribute to use in the result
   */
  private String attributeNameResult;

  /**
   * attribute name result
   * @param theAttributeNameResult
   * @return this for chaining
   */
  public LdapLookup assignAttributeNameResult(String theAttributeNameResult) {
    this.attributeNameResult = theAttributeNameResult;
    return this;
  }
  
  /**
   * attribute to use to query
   */
  private String attributeNameQuery;

  
  /**
   * attribute name query
   * @param theAttributeNameQuery
   * @return this for chaining
   */
  public LdapLookup assignAttributeNameQuery(String theAttributeNameQuery) {
    this.attributeNameQuery = theAttributeNameQuery;
    return this;
  }

  /**
   * term to search for
   */
  private String term;
  
  /**
   * assign term
   * @param theTerm
   * @return this for chaining
   */
  public LdapLookup assignTerm(String theTerm) {
    this.term = theTerm;
    return this;
  }

  /**
   * cache results for minutes
   */
  private int cacheForMinutes = -1;

  /**
   * assign cache for minutes
   * @param theCacheForMinutes
   * @return this
   */
  public LdapLookup assignCacheForMinutes(int theCacheForMinutes) {
    this.cacheForMinutes = theCacheForMinutes;
    return this;
  }

  /**
   * cache key to single cache
   */
  private static Map<MultiKey, ExpirableCache<String, String>> cacheKeyToSingleCache 
    = new HashMap<MultiKey, ExpirableCache<String, String>>();
  
  /**
   * cache key to multi cache
   */
  private static Map<MultiKey, ExpirableCache<Boolean, Map<String, String>>> cacheKeyToMultiCache 
    = new HashMap<MultiKey, ExpirableCache<Boolean, Map<String, String>>>();
  
  
  /**
   * if this is a bulk lookup
   */
  private Boolean bulkLookup;
  
  /**
   * if this is a bulk lookup
   * @param theBulkLookup
   * @return this for training
   */
  public LdapLookup assignBulkLookup(Boolean theBulkLookup) {
    this.bulkLookup = theBulkLookup;
    return this;
  }
  
  /**
   * do the lookup
   * @return the result
   */
  public String doLookup() {

    if (!StringUtils.isBlank(this.term)) {
      if (!StringUtils.isBlank(this.searchDn)) {
        if (StringUtils.equals("%TERM%", this.searchDn)) {
          this.searchDn = this.term;
        } else {
          this.searchDn = GrouperUtil.replace(this.searchDn, "%TERM%", GrouperUtil.ldapEscapeRdnValue(this.term));
        }
      }
      if (!StringUtils.isBlank(this.filter)) {
        if (StringUtils.equals("%TERM%", this.filter)) {
          this.filter = this.term;
        } else {
          this.filter = GrouperUtil.replace(this.filter, "%TERM%", GrouperUtil.ldapFilterEscape(this.term));
        }
      }
    }

    boolean singleFilter = StringUtils.isBlank(this.attributeNameQuery);
    
    if (this.bulkLookup == null) {
      this.bulkLookup = !singleFilter;
    } else {
      if (this.bulkLookup == singleFilter) {
        if (this.bulkLookup) {
          throw new RuntimeException("bulk lookups must pass in attributeNameQuery! " + this);
        } else {
          throw new RuntimeException("non-bulk lookups must not pass in attributeNameQuery! " + this);
        }
      } 
    }
    
    boolean needsCache = this.cacheForMinutes > 0;

    if (!needsCache & !singleFilter) {
      this.cacheForMinutes = 1;
      needsCache = true;
    }

    MultiKey cacheKey = needsCache ? new MultiKey(new Object[] {
        this.attributeNameQuery, 
        this.attributeNameResult,
        this.cacheForMinutes,
        this.filter,
        this.ldapConfigId,
        this.searchDn,
        this.searchScope}) : null;

    // if we arent caching then do the filter
    if (needsCache) {
      if (StringUtils.isBlank(this.term)) {
        throw new RuntimeException("term is a required field for cached filters! " + this);
      }

      if (singleFilter) {

        // if we dont need cache and single filter, then see if in cache
        ExpirableCache<String, String> cache = cacheKeyToSingleCache.get(cacheKey);
  
        // initialize
        if (cache == null) {
          cache = new ExpirableCache<String, String>(this.cacheForMinutes);
          cacheKeyToSingleCache.put(cacheKey, cache);
        }
        
        String result = cache.get(this.term);
        if (!StringUtils.isBlank(result)) {
          return massageEntryFromCache(result);
        }
  
      } else {
        // if multi filter
        ExpirableCache<Boolean, Map<String, String>> cache = cacheKeyToMultiCache.get(cacheKey);
  
        // initialize
        if (cache == null) {
          cache = new ExpirableCache<Boolean, Map<String, String>>(this.cacheForMinutes);
          cacheKeyToMultiCache.put(cacheKey, cache);
        }
  
        Map<String, String> termToSubjectIdOrIdentifier = cache.get(Boolean.TRUE);
        
        if (termToSubjectIdOrIdentifier != null) {
          return massageEntryFromCache(termToSubjectIdOrIdentifier.get(this.term));
        }
        
      }
    }
    
    LdapSearchScope ldapSearchScope = null;
    if (this.searchScope instanceof LdapSearchScope) {
      ldapSearchScope = (LdapSearchScope)this.searchScope;
    } else if (this.searchScope instanceof String) {
      ldapSearchScope = LdapSearchScope.valueOfIgnoreCase((String)this.searchScope, true);
    } else {
      
      // if theres no filter then we are simply looking up by dn
      if (!StringUtils.isBlank(this.filter)) {
        throw new RuntimeException("Search scope needs to be OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE: " + this);
      }
    }
    
    List<String> attributeNameList = new ArrayList<String>();
    
    if (!StringUtils.isBlank(this.attributeNameQuery) && !StringUtils.equalsIgnoreCase("dn", this.attributeNameQuery)) {
      attributeNameList.add(this.attributeNameQuery);
    }
    if (!StringUtils.isBlank(this.attributeNameResult) && !StringUtils.equalsIgnoreCase("dn", this.attributeNameResult)) {
      attributeNameList.add(this.attributeNameResult);
    }
    
    String[] attributeNames = null;
    if (attributeNameList.size() > 0) {
      attributeNames = GrouperUtil.toArray(attributeNameList, String.class);
    }
    
    // need filter
    List<LdapEntry> ldapEntries = null;
    if (StringUtils.isBlank(this.filter)) {
      ldapEntries = LdapSessionUtils.ldapSession().read(this.ldapConfigId, this.searchDn, GrouperUtil.toList(this.searchDn), attributeNames);
    } else {
      ldapEntries = LdapSessionUtils.ldapSession().list(this.ldapConfigId,this.searchDn, ldapSearchScope, this.filter, attributeNames, null);
    }

    test_filterCount++;
    
    if (singleFilter) {
      
      if (ldapEntries.size() > 1) {
        throw new RuntimeException("single filter returned multiple results! (" + ldapEntries.size() + ") " + this);
      }
      
      String result = null;
      if (ldapEntries.size() == 1) {
        result = retrieveResult(ldapEntries.get(0), this.attributeNameResult);
      }
      
      // not cacheable
      if (needsCache) {
        cacheKeyToSingleCache.get(cacheKey).put(this.term, massageEntryToCache(result));
      }
      
      return massageEntryFromCache(result);
      
    }
    
    // multiple results
    Map<String, String> termToSubjectIdOrIdentifier = new HashMap<String, String>();
    for (LdapEntry ldapEntry : GrouperUtil.nonNull(ldapEntries)) {
      
      String query = retrieveResult(ldapEntry, this.attributeNameQuery);
      String result = retrieveResult(ldapEntry, this.attributeNameResult);
      if (!StringUtils.isBlank(query)) {
        termToSubjectIdOrIdentifier.put(query, massageEntryToCache(result));
      }
    }

    if (needsCache) {
      // multi is always cached
      cacheKeyToMultiCache.get(cacheKey).put(Boolean.TRUE, termToSubjectIdOrIdentifier);
    }
    
    return massageEntryFromCache(termToSubjectIdOrIdentifier.get(this.term));
    
  }
  
  /**
   * get a single string attribute value out of an ldap entry
   * @param ldapEntry
   * @param attributeName
   * @return
   */
  private String retrieveResult(LdapEntry ldapEntry, String attributeName) {
    
    if (ldapEntry == null) {
      return null;
    }
    if (StringUtils.equalsIgnoreCase("dn", attributeName)) {
      return ldapEntry.getDn();
    }
    LdapAttribute ldapAttribute = ldapEntry.getAttribute(attributeName);
    if (ldapAttribute == null) {
      return null;
    }
    Collection<String> values = ldapAttribute.getStringValues();
    if (GrouperUtil.length(values) == 0) {
      return null;
    }
    if (GrouperUtil.length(values) == 1) {
      return values.iterator().next();
    }
    throw new RuntimeException("Looking for attribute '" 
        + this.attributeNameResult + "' and it returned multiple results! (" + GrouperUtil.length(values) + ") " + this);
  }
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this);
    // print things which arent null
    if (!StringUtils.isBlank(this.attributeNameQuery)) {
      toStringBuilder.append( "attributeNameQuery", this.attributeNameQuery);
    }
    if (!StringUtils.isBlank(this.attributeNameResult)) {
      toStringBuilder.append( "attributeNameResult", this.attributeNameResult);
    }
    if (this.cacheForMinutes >=0) {
      toStringBuilder.append( "cacheForMinutes", this.cacheForMinutes);
    }
    if (!StringUtils.isBlank(this.filter)) {
      toStringBuilder.append( "filter", this.filter);
    }
    if (!StringUtils.isBlank(this.ldapConfigId)) {
      toStringBuilder.append( "ldapConfigId", this.ldapConfigId);
    }
    if (!StringUtils.isBlank(this.searchDn)) {
      toStringBuilder.append( "searchDn", this.searchDn);
    }
    if (this.searchScope!=null) {
      toStringBuilder.append( "searchScope", this.searchScope);
    }
    if (!StringUtils.isBlank(this.term)) {
      toStringBuilder.append( "term", this.term);
    }
    return toStringBuilder.toString();
  }
  
}
