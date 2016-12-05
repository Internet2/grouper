package edu.internet2.middleware.grouper.pspng;

/*******************************************************************************
 * Copyright 2015 Internet2
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
 ******************************************************************************/

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchFilter;
import org.ldaptive.provider.unboundid.UnboundIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;


/**
 * This is a wrapper around ldaptive's LdapEntry. It adds the following capabilities:
 *   1) It keeps track of what attributes were fetched when the object was read from the ldap server.
 *   This will be used to raise an error if the fetching code didn't ask for an attribute
 *   that was later read from the object.
 *   2) It will convert an Ldaptive LdapEntry into an UnboundId Entry, typically for 
 *   the purposes of using UnboundId's ability to evaluate filters in memory.
 *   3) It stores when the object was fetched to help with caching
 *   4) It allows "dn" to be treated like a normal attribute
 *   
 * @author Bert Bee-Lindgren
 *
 */
public class LdapObject {
  /**
   * logger 
   */
  private static final Logger LOG = LoggerFactory.getLogger(LdapObject.class);
  
  // When we do a search, or create an LdapObject, it is easiest for us to have a 
  // java array of attributes that were fetched. We want, however, to keep
  // Sets around for fast lookup into the list of attributes that were fetched.
  // HOWEVER, we don't want to keep millions of Sets around, but just keep one
  // for each combination of attributes.
  // This allows us to cache a (case-insensitive) Set for each List of attributes
  private static final ConcurrentHashMap<List<String>, Set<String>> attributeSetCache
    = new ConcurrentHashMap<List<String>, Set<String>>();

  protected final Set<String> attributesRequested;
  private static final UnboundIDUtils unboundIdUtils = new UnboundIDUtils();
  Date fetchTime = new Date();
  LdapEntry ldapEntry;
  
  Entry unboundidEntry;
  
  
  private static Set<String> getStringSetForStringArray(String values[]) {
    List<String> valueList = Arrays.asList(values);
    if ( !attributeSetCache.contains(valueList) ) {
      Set<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      set.addAll(valueList);
      attributeSetCache.put(valueList, set);
    }
    
    return attributeSetCache.get(valueList);
  }
  
  public LdapObject(LdapEntry ldapEntry, String attributesRequested[]) {
    this.ldapEntry = ldapEntry;
    if ( attributesRequested == null || attributesRequested.length == 0 )
      attributesRequested=new String[] {"dn"};
    
    this.attributesRequested = getStringSetForStringArray(attributesRequested);
  }
  
  
  public Entry getUnboundIdEntry() {
    if ( unboundidEntry == null )
    {
      Entry result = new Entry(getDn());
      
      for ( LdapAttribute attribute : ldapEntry.getAttributes() ) 
        result.addAttribute(unboundIdUtils.fromLdapAttribute(attribute));
      unboundidEntry = result;
    }
    return unboundidEntry;
  }
  
  /**
   * Get the LdapAttribute from the stored entry. This first checks whether the
   * attribute requested was actually requested from the ldap server. If it was,
   * then the LdapAttribute is simply returned; if the attribute was not requested,
   * then this throws an IllegalStateException.
   * 
   * @param attributeName
   * @return
   */
  public LdapAttribute getAttribute(String attributeName) {
    if ( attributeName.equalsIgnoreCase("dn") || attributeName.equalsIgnoreCase("distinguishedName") )
      return new LdapAttribute(attributeName, ldapEntry.getDn());
    
    if ( attributesRequested.contains(attributeName) )
      return ldapEntry.getAttribute(attributeName);
    
    throw new IllegalStateException(String.format("Attribute was not requested from ldap server: %s", attributeName));
  }
  
  

  /** 
   * Get the distinguished name of the ldap entry. This is always lower-cased
   * so it is easy to do case-insensitive comparisons or Map storage
   * @return Lower-cased distinguished name
   */
  public String getDn() {
    return ldapEntry.getDn().toLowerCase();
  }
  

  public LdapAttribute getSingleValueAttribute(String attributeName)  {
    LdapAttribute attribute = getAttribute(attributeName);
    if ( attribute == null )
      return null;
    else if ( attribute.size() > 1 )
      throw new IllegalArgumentException(String.format("Expected a single-valued attribute, but attribute %s has %d values",
          attributeName, attribute.size()));
    
    return attribute;
  }
  
  public String getStringValue(String attributeName) {
    LdapAttribute attribute = getSingleValueAttribute(attributeName);
    if ( attribute == null )
      return null;
    
    return attribute.getStringValue();
  }
  
  public Collection<String> getStringValues(String attributeName) {
    LdapAttribute attribute = getAttribute(attributeName);
    if ( attribute == null )
      return Collections.EMPTY_LIST;
    
    return attribute.getStringValues();
  }
  
  public Long getLongValue(String attributeName) {
    String s = getStringValue(attributeName);
    if ( s == null )
      return null;
    
    return Long.parseLong(s);
  }
  
  public Integer getIntegerValue(String attributeName) {
    String s = getStringValue(attributeName);
    if ( s == null )
      return null;
    
    return Integer.parseInt(s);
  }
  
  public Map<String, Collection<String>> getMap() {
    Map<String, Collection<String>> result = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
    
    result.put("dn", Arrays.asList(getDn()));
    
    for ( LdapAttribute attribute : ldapEntry.getAttributes())
      result.put(attribute.getName(), attribute.getStringValues());
    
    return result;
  }
  
  /**
   * Add this LdapObject to a (single-valued) index of ldap objects based on the
   * specified attribute's value.
   * @param indexName Name to use when logging problems with idnex
   * @param index Map of (string) attribute values to their LdapObjects
   * @param attributeName What attribute should be key of the requested index
   */
  public void addToStringIndex(String indexName, Map<String, LdapObject> index, String attributeName)
  {
    String attributeValue = getStringValue(attributeName);
        
    if ( index.containsKey(attributeValue) )
      throw new IllegalStateException(
          String.format("Single-valued index %s cannot store multiple values for value %s (%s,%s)",
          indexName, attributeValue, getDn(), index.get(attributeValue).getDn()));
    
    index.put(attributeValue, this);
  }
  
  
  /**
   * Add this LdapObject to a (single-valued) index of ldap objects based on the specified
   * attribute's value.
   * 
   * @param indexName Name to use when logging problems with idnex
   * @param index Map of (string) attribute values to their LdapObjects
   * @param attributeName What attribute should be key of the requested index
   */
  public void addToLongIndex(String indexName, Map<Long, LdapObject> index, String attributeName)
  {
    Long attributeValue = getLongValue(attributeName);
        
    if ( index.containsKey(attributeValue) )
      throw new IllegalStateException(
          String.format("Single-valued index %s cannot store multiple values for value %s (%s,%s)",
          indexName, attributeValue, getDn(), index.get(attributeValue).getDn()));
    
    index.put(attributeValue, this);
  }

  /**
   * Add this LdapObject to an index of ldap objects based on the specified attribute's value.
   *
   * @param indexName
   * @param index
   * @param attributeName
   */
  public void addToIndex(String indexName, MultiMap index, String attributeName)
  {
    String attributeValue = getStringValue(attributeName);
        
    index.put(attributeValue, this);
  }
  
  public boolean matchesLdapFilter(SearchFilter filter) throws PspException {
    try {
      Filter unboundidFilter = Filter.create(filter.format());
      boolean result = unboundidFilter.matchesEntry(getUnboundIdEntry());
      
      return result;
    } catch (LDAPException e) {
      LOG.error("Problem checking ldap filter in memory: {}", filter, e);
      throw new PspException("Problem checking ldap filter {}: %s", filter, e.getMessage());
    }
    
  }
  
  @Override
  public int hashCode() {
    return getDn().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if ( obj instanceof LdapObject )
      return getDn().equals(((LdapObject) obj).getDn());
    return false;
  }
  
  @Override
  public String toString() {
    ToStringBuilder result = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    
    if ( attributesRequested.contains("cn") )
      result.append("cn", getStringValues("cn"));

    if ( attributesRequested.contains("uid") && StringUtils.isNotEmpty(getStringValue("uid")) )
      result.append("uid", getStringValues("uid"));
    else if ( attributesRequested.contains("samAccountName") && StringUtils.isNotEmpty(getStringValue("samaccountname")) )
      result.append("samAccountName", getStringValue("samAccountName"));

    result.append("dn", getDn());

    return result.toString();
  }
}
