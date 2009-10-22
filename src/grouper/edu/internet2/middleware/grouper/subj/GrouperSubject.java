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
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.provider.SubjectImpl;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

/** 
 * {@link Subject} returned by the {@link GrouperSourceAdapter}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.12 2009-10-22 14:03:18 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class GrouperSubject extends SubjectImpl {
  
  /**
   * lazy load map that doesnt do any queries until it really needs to
   * @param <K> 
   * @param <V> 
   */
  @SuppressWarnings("serial")
  private class GrouperSubjectAttributeMap<K,V> implements Map<K,V>, Serializable {
    
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
  
  /** lazy load the map attributes of group attributes */
  private boolean loadedGroupAttributes = false;
  
  /** lazy load the modify and create subjects */
  private boolean loadedModifyCreateSubjects = false;
  
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
    
    super(g.getUuid(), g.getName(), null, SubjectTypeEnum.GROUP.getName(), 
        SubjectFinder.internal_getGSA().getId(), null);
    
    /** attributes (refresh if not found) */
    GrouperSubjectAttributeMap<String, Set<String>> attrs 
      = new GrouperSubjectAttributeMap<String, Set<String>>();

    attrs.put( "name",   GrouperUtil.toSet(g.getName()), false);
    attrs.put( "displayName",   GrouperUtil.toSet(g.getDisplayName()), false);
    attrs.put( "alternateName", g.getAlternateNames(), false);
    attrs.put( "extension",   GrouperUtil.toSet(g.getExtension()), false);
    attrs.put( "displayExtension",   GrouperUtil.toSet(g.getDisplayExtension()), false );
    attrs.put( "description",   GrouperUtil.toSet(g.getDescription()), false);
    attrs.put( "modifyTime",        GrouperUtil.toSet(g.getModifyTime().toString()), false ); 
    attrs.put( "createTime",        GrouperUtil.toSet(g.getCreateTime().toString()), false ); 
    super.setAttributes(attrs);
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String)
   */
  public String getAttributeValue(String name) {
    return StringUtils.defaultString(super.getAttributeValue(name));
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
          + this.getName() + ", " + eGNF.getMessage() );
      return;
    }

    try {
      // Don't bother with any of the create* attrs unless we can find
      // the creating subject
      Subject creator = g.getCreateSubject();
      ((GrouperSubjectAttributeMap)this.getAttributes()).put( "createSubjectId",   GrouperUtil.toSet(creator.getId()), false);
      ((GrouperSubjectAttributeMap)this.getAttributes()).put( "createSubjectType", GrouperUtil.toSet(creator.getType().getName()), false);
    }
    catch (SubjectNotFoundException eSNF0) {
      LOG.error(E.GSUBJ_NOCREATOR + eSNF0.getMessage());
    }
    try {
      // Don't bother with any of the modify* attrs unless we can find
      // the modifying subject
      Subject modifier = g.getModifySubject();
      ((GrouperSubjectAttributeMap)this.getAttributes()).put( "modifySubjectId",   
          GrouperUtil.toSet(modifier.getId()), false);
      ((GrouperSubjectAttributeMap)this.getAttributes()).put( "modifySubjectType", 
          GrouperUtil.toSet(modifier.getType().getName()), false);
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
          + this.getName() + ", " + eGNF.getMessage() );
      return;
    }
    Iterator<Map.Entry<String, Attribute>>  it  = g.getAttributesMap(true).entrySet().iterator();
    int count=0;
    while (it.hasNext()) {
      kv = it.next();
      ((GrouperSubjectAttributeMap)this.getAttributes()).put( kv.getKey(), GrouperUtil.toSet(kv.getValue().getValue()), false );
      count++;
    }
    this.loadedGroupAttributes = true;
    LOG.debug("[" + this.getName() + "] attached " + count +  " new attributes: " 
        + ((GrouperSubjectAttributeMap)GrouperUtil.nonNull(this.getAttributes())).attrs.size() );
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperSubject.class);


}

