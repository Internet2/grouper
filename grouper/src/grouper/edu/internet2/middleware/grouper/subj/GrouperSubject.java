/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.subj;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

/** 
 * {@link Subject} returned by the {@link GrouperSourceAdapter}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.8 2009-04-13 16:53:08 mchyzer Exp $
 */
public class GrouperSubject implements Subject {
  
  /**
   * lazy load map that doesnt do any queries until it really needs to
   * @param <K> 
   * @param <V> 
   */
  private class GrouperSubjectAttributeMap<K,V> implements Map<K,V> {
    
    /** underlying datastore */
    private Map<K,V> attrs   = new HashMap<K,V>();

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
      this.attrs.clear();
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
      if (this.attrs.containsKey(key)) {
        return true;
      }
      GrouperSubject.this._populateAttributes();
      GrouperSubject.this._populateCreateModifyTime();
      return this.attrs.containsKey(key);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
      if (this.attrs.containsValue(value)) {
        return true;
      }
      GrouperSubject.this._populateAttributes();
      GrouperSubject.this._populateCreateModifyTime();
      return this.attrs.containsValue(value);
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<K, V>> entrySet() {
      GrouperSubject.this._populateAttributes();
      GrouperSubject.this._populateCreateModifyTime();
      return this.attrs.entrySet();
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public V get(Object key) {
      if (!this.attrs.containsKey(key)) {
        GrouperSubject.this._populateAttributes();
        GrouperSubject.this._populateCreateModifyTime();
      }
      return (V)this.attrs.get(key);
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
      if (this.attrs.isEmpty()) {
        GrouperSubject.this._populateAttributes();
        GrouperSubject.this._populateCreateModifyTime();
      }
      return this.attrs.isEmpty();
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set<K> keySet() {
      GrouperSubject.this._populateAttributes();
      GrouperSubject.this._populateCreateModifyTime();
      return (Set<K>)this.attrs.keySet();
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public V put(K key, V value) {
      return this.put(key, value, true);
    }
    /**
     * @param key 
     * @param value 
     * @param populateAttributesBefore 
     * @return value
     */
    public V put(K key, V value, boolean populateAttributesBefore) {
      if (populateAttributesBefore) {
        GrouperSubject.this._populateAttributes();
        GrouperSubject.this._populateCreateModifyTime();
      }
      return this.attrs.put(key, value);
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends K, ? extends V> t) {
      GrouperSubject.this._populateAttributes();
      GrouperSubject.this._populateCreateModifyTime();
      this.attrs.putAll(t);
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public V remove(Object key) {
      GrouperSubject.this._populateAttributes();
      GrouperSubject.this._populateCreateModifyTime();
      return this.attrs.remove(key);
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {
      GrouperSubject.this._populateAttributes();
      GrouperSubject.this._populateCreateModifyTime();
      return this.attrs.size();
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection<V> values() {
      GrouperSubject.this._populateAttributes();
      GrouperSubject.this._populateCreateModifyTime();
      return this.attrs.values();
    }
  }
  
  /** */
  private GrouperSourceAdapter  adapter = null;
  /** */
  private GrouperSubjectAttributeMap<String, Set<String>>   attrs   = new GrouperSubjectAttributeMap<String, Set<String>>();
  /** */
  private String                id      = null;
  /** */
  private String                name    = null;
  /** */
  private SubjectType           type    = SubjectTypeEnum.valueOf("group");

  /** lazy load the map attributes of group attributes */
  private boolean loadedGroupAttributes = false;
  
  /** lazy load the modify and create subjects */
  private boolean loadedModifyCreateSubjects = false;
  
  /**
   * 
   * @param ifIgnore
   */
  public static void ignoreGroupAttributeSecurityOnNewSubject(boolean ifIgnore) {
    ignoreGroupAttributeSecurityOnNewSubject.set(ifIgnore);
  }

  /**
   * if loaded group attributes
   * @return true/false
   */
  public boolean isLoadedGroupAttributes() {
    return this.loadedGroupAttributes;
  }

  /**
   * if loaded group attributes
   * @return true/false
   */
  public boolean isLoadedModifyCreateSubjects() {
    return this.loadedModifyCreateSubjects;
  }

  /**
   * @param g
   * @throws SourceUnavailableException
   */
  public GrouperSubject(Group g) 
    throws  SourceUnavailableException {
    this.id       = g.getUuid();
    this.name     = g.getName();
    this.adapter  = (GrouperSourceAdapter) SubjectFinder.internal_getGSA();

    this.attrs.put( "name",   GrouperUtil.toSet(g.getName()), false);
    this.attrs.put( "displayName",   GrouperUtil.toSet(g.getDisplayName()), false);
    this.attrs.put( "alternateName", g.getAlternateNames(), false);
    this.attrs.put( "extension",   GrouperUtil.toSet(g.getExtension()), false);
    this.attrs.put( "displayExtension",   GrouperUtil.toSet(g.getDisplayExtension()), false );
    this.attrs.put( "description",   GrouperUtil.toSet(g.getDescription()), false);
    this.attrs.put( "modifyTime",        GrouperUtil.toSet(g.getModifyTime().toString()), false ); 
    this.attrs.put( "createTime",        GrouperUtil.toSet(g.getCreateTime().toString()), false ); 

  }

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    return SubjectHelper.eq(this, other);
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.Subject#getAttributes()
   */
  public Map<String, Set<String>> getAttributes() {
    return this.attrs;
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String)
   */
  public String getAttributeValue(String name) {
    if ( this.attrs.containsKey(name) ) {
      Set<String> values = this.attrs.get(name);
      return values == null ? null : this.attrs.get(name).iterator().next();
    }
    return GrouperConfig.EMPTY_STRING;
  }
  
  /**
   * 
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
   */
  public Set getAttributeValues(String name) {
  	//https://bugs.internet2.edu/jira/browse/GRP-40
  	//2007-10-18: Gary Brown
  	//Simply put value in a Set, however, would
  	//need to revisit if Grouper had multi-value String attributes
  	Set values = new LinkedHashSet();
  	String value = this.getAttributeValue(name);
  	if(!GrouperConfig.EMPTY_STRING.equals(value)) {
  		values.add(value);
  	}
    return values;
  }
  
  /**
   * 
   * @see edu.internet2.middleware.subject.Subject#getDescription()
   */
  public String getDescription() {
    return this.getAttributeValue("description");
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.Subject#getId()
   */
  public String getId() {
    return this.id;
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.Subject#getName()
   */
  public String getName() {
    return this.name;
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.Subject#getSource()
   */
  public Source getSource() {
    return this.adapter;
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.Subject#getType()
   */
  public SubjectType getType() {
    return this.type;
  }

  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append(  this.getId()              )
      .append(  this.getSource().getId()  )
      .append(  this.getType().getName()  )
      .toHashCode()
      ;
  }

  /**
   * 
   */
  private void _populateCreateModifyTime() {
    if (this.loadedModifyCreateSubjects) {
      return;
    }
    Group g = null;
    try {
      g = GrouperDAOFactory.getFactory().getGroup().findByUuid( this.getId(), true ) ;
    } catch (GroupNotFoundException eGNF) {
      LOG.error("unable to retrieve group attributes: " + this.getId() + ", " 
          + this.name + ", " + eGNF.getMessage() );
      return;
    }

    try {
      // Don't bother with any of the create* attrs unless we can find
      // the creating subject
      Subject creator = g.getCreateSubject();
      this.attrs.put( "createSubjectId",   GrouperUtil.toSet(creator.getId()), false);
      this.attrs.put( "createSubjectType", GrouperUtil.toSet(creator.getType().getName()), false);
    }
    catch (SubjectNotFoundException eSNF0) {
      LOG.error(E.GSUBJ_NOCREATOR + eSNF0.getMessage());
    }
    try {
      // Don't bother with any of the modify* attrs unless we can find
      // the modifying subject
      Subject modifier = g.getModifySubject();
      this.attrs.put( "modifySubjectId",   GrouperUtil.toSet(modifier.getId()), false);
      this.attrs.put( "modifySubjectType", GrouperUtil.toSet(modifier.getType().getName()), false);
    }
    catch (SubjectNotFoundException eSNF1) {
      // No modifier
    }
    this.loadedModifyCreateSubjects = true;

  }
  
  /**
   * 
   */
  private void _populateAttributes() {
    if (this.loadedGroupAttributes) {
      return;
    }

    Group g = null;
    Map.Entry<String,Attribute> kv;
    try {
      g = GrouperDAOFactory.getFactory().getGroup().findByUuid( this.getId(), true ) ;
    } catch (GroupNotFoundException eGNF) {
      LOG.error("unable to retrieve group attributes: " + this.getId() + ", " 
          + this.name + ", " + eGNF.getMessage() );
      return;
    }
    Iterator<Map.Entry<String, Attribute>>  it  = g.getAttributesMap(true).entrySet().iterator();
    int count=0;
    while (it.hasNext()) {
      kv = it.next();
      this.attrs.put( kv.getKey(), GrouperUtil.toSet(kv.getValue().getValue()), false );
      count++;
    }
    this.loadedGroupAttributes = true;
    LOG.debug("[" + this.name + "] attached " + count +  " new attributes: " + this.attrs.attrs.size() );
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperSubject.class);


}

