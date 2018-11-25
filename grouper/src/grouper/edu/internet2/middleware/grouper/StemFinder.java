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

package edu.internet2.middleware.grouper;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Find stems within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: StemFinder.java,v 1.54 2009-11-18 17:03:50 mchyzer Exp $
 */
public class StemFinder {

  /**
   * remove all caches
   */
  public static void stemCacheClear() {
    stemFlashCache.clear();
  }

  /**
   * remove this from all caches
   * @param id
   */
  public static void stemCacheRemoveById(String id) {
    if (id == null) {
      return;
    }
    Stem stem = stemFlashCacheRetrieve(id, null);
    stemCacheRemove(stem);
  }

  /**
   * remove this from all caches
   * @param stem
   */
  public static void stemCacheRemove(Stem stem) {
    if (stem == null) {
      return;
    }
    stemFlashCache.remove(stem.getUuid());
    stemFlashCache.remove(stem.getName());
    stemFlashCache.remove(stem.getIdIndex());
    
    {
      Stem dbVersion = stem.dbVersion();
      if (dbVersion != null && dbVersion != stem) {
        stemCacheRemove(dbVersion);
        return;
      }
    }

  }

  /**
   * find stems where the user has these fields in a group
   */
  private Collection<Field> userHasInGroupFields;
  
  /**
   * find stems where the user has these fields in a group
   * @param theUserHasInGroupField
   * @return this for chaining
   */
  public StemFinder addUserHasInGroupField(Field theUserHasInGroupField) {
    if (this.userHasInGroupFields == null) {
      this.userHasInGroupFields = new HashSet<Field>();
    }
    this.userHasInGroupFields.add(theUserHasInGroupField);
    return this;
  }

  /**
   * find stems where the user has these fields in an attribute
   */
  private Collection<Field> userHasInAttributeFields;

  /**
   * find stems where the user has these fields in an attribute
   * @param theUserHasInAttributeField
   * @return this for chaining
   */
  public StemFinder addUserHasInAttributeField(Field theUserHasInAttributeField) {
    if (this.userHasInAttributeFields == null) {
      this.userHasInAttributeFields = new HashSet<Field>();
    }
    this.userHasInAttributeFields.add(theUserHasInAttributeField);
    return this;
  }

  /**
   * find stems where the user has these fields in an attribute
   * @param theUserHasInGroupFields
   * @return this for chaining
   */
  public StemFinder assignUserHasInGroupField(Collection<Field> theUserHasInGroupFields) {
    this.userHasInGroupFields = theUserHasInGroupFields;
    return this;
  }

  /**
   * find stems where the user has these fields in an attribute
   * @param theUserHasInAttributeFields
   * @return this for chaining
   */
  public StemFinder assignUserHasInAttributeField(Collection<Field> theUserHasInAttributeFields) {
    this.userHasInAttributeFields = theUserHasInAttributeFields;
    return this;
  }
  
  /**
   * Find stem by name.
   * <pre class="eg">
   * try {
   *   Stem stem = StemFinder.findByName(s, name);
   * }
   * catch (StemNotFoundException e) {
   *   // Stem not found
   * }
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @param   name  Find stem with this name.
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   * @deprecated see overload
   */
  @Deprecated
  public static Stem findByName(GrouperSession s, String name) throws StemNotFoundException {
    return findByName(s, name, true);
  }

  /**
   * Find stem by name.
   * <pre class="eg">
   *   Stem stem = StemFinder.findByName(s, name, false);
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @param   name  Find stem with this name.
   * @param exceptionIfNotFound
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Stem findByName(GrouperSession s, String name, boolean exceptionIfNotFound)
      throws StemNotFoundException {
    return findByName(s, name, exceptionIfNotFound, null);
  }

  
  /**
   * Find stem by name.
   * <pre class="eg">
   *   Stem stem = StemFinder.findByName(s, name, false);
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @param   name  Find stem with this name.
   * @param exceptionIfNotFound
   * @param queryOptions
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Stem findByName(GrouperSession s, String name, boolean exceptionIfNotFound, QueryOptions queryOptions)
      throws StemNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    // TODO 20070314 bah.  should be in dao if it exists at all.
    if ( name.equals(Stem.ROOT_NAME) ) {
      name = Stem.ROOT_INT;
    }
    
    Stem ns = stemFlashCacheRetrieve(name, queryOptions);
    if (ns != null) {
      return ns;
    }      
    
    ns = GrouperDAOFactory.getFactory().getStem().findByName(name, exceptionIfNotFound, queryOptions) ;
    
    if (ns != null) {
      stemFlashCacheAddIfSupposedTo(ns);
      return ns;
    }
    if (!exceptionIfNotFound) {
      return null;
    }
    throw new StemNotFoundException("Cant find stem: '" + name + "'");
  }
  
  /**
   * Find stem by its alternate name.
   * <pre class="eg">
   *   Stem stem = StemFinder.findByAlternateName(s, name, false);
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @param   name  Find stem with this alternate name.
   * @param exceptionIfNotFound
   * @param queryOptions
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Stem findByAlternateName(GrouperSession s, String name, boolean exceptionIfNotFound, QueryOptions queryOptions)
      throws StemNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    // TODO 20070314 bah.  should be in dao if it exists at all.
    if ( name.equals(Stem.ROOT_NAME) ) {
      name = Stem.ROOT_INT;
    }
    Stem ns = GrouperDAOFactory.getFactory().getStem().findByAlternateName(name, exceptionIfNotFound, queryOptions) ;
    return ns;
  }
  
  /**
   * Find stem by its current name.
   * <pre class="eg">
   *   Stem stem = StemFinder.findByCurrentName(s, name, false);
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @param   name  Find stem with this name.
   * @param exceptionIfNotFound
   * @param queryOptions
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Stem findByCurrentName(GrouperSession s, String name, boolean exceptionIfNotFound, QueryOptions queryOptions)
      throws StemNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    // TODO 20070314 bah.  should be in dao if it exists at all.
    if ( name.equals(Stem.ROOT_NAME) ) {
      name = Stem.ROOT_INT;
    }
    Stem ns = GrouperDAOFactory.getFactory().getStem().findByCurrentName(name, exceptionIfNotFound, queryOptions) ;
    return ns;
  }

  /**
   * Find root stem of the Groups Registry.
   * <pre class="eg">
   * // Find the root stem.
   * Stem rootStem = StemFinder.findRootStem(s);
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @return  A {@link Stem} object
   * @throws  GrouperException
   */
  public static Stem findRootStem(GrouperSession s) 
    throws  StemNotFoundException  {
    GrouperStartup.startup();
    GrouperSession.validate(s);
    return StemFinder.findByName(s, Stem.ROOT_INT, true);
  } 

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(StemFinder.class);

  /**
   * Get stem by uuid.
   * <pre class="eg">
   * // Get the specified stem by uuid.
   * try {
   *   Stem stem = StemFinder.findByUuid(s, uuid);
   * }
   * catch (StemNotFoundException e) {
   *   // Stem not found
   * }
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @param   uuid  Get stem with this UUID.
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   * @deprecated see overload
   */
  @Deprecated
  public static Stem findByUuid(GrouperSession s, String uuid) 
    throws StemNotFoundException {
    return findByUuid(s, uuid, true);
  }

  /**
   * Get stem by uuid.
   * <pre class="eg">
   * // Get the specified stem by uuid.
   * try {
   *   Stem stem = StemFinder.findByUuid(s, uuid);
   * }
   * catch (StemNotFoundException e) {
   *   // Stem not found
   * }
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @param   uuid  Get stem with this UUID.
   * @param exceptionIfNotFound
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Stem findByUuid(GrouperSession s, String uuid, boolean exceptionIfNotFound) 
    throws StemNotFoundException {
    return findByUuid(s, uuid, exceptionIfNotFound, null);
  }
  
  /**
   * Get stem by uuid.
   * <pre class="eg">
   * // Get the specified stem by uuid.
   * try {
   *   Stem stem = StemFinder.findByUuid(s, uuid);
   * }
   * catch (StemNotFoundException e) {
   *   // Stem not found
   * }
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @param   uuid  Get stem with this UUID.
   * @param exceptionIfNotFound
   * @param queryOptions
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Stem findByUuid(GrouperSession s, String uuid, boolean exceptionIfNotFound, QueryOptions queryOptions) 
    throws StemNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Stem ns = stemFlashCacheRetrieve(uuid, queryOptions);
    if (ns != null) {
      return ns;
    }      
    
    ns = GrouperDAOFactory.getFactory().getStem().findByUuid(uuid, exceptionIfNotFound, queryOptions) ;
    
    if (ns != null) {
      stemFlashCacheAddIfSupposedTo(ns);
      return ns;
    }
    if (!exceptionIfNotFound) {
      return null;
    }
    throw new StemNotFoundException("Cant find stem: '" + uuid + "'");

  } // public static Stem findByUuid(s, uuid)

  /**
   * Get stems by uuids.
   * <pre class="eg">
   * // Get the specified stems by uuids.
   * try {
   *   Set<Stem> stems = StemFinder.findByUuids(s, uuids, null);
   * }
   * catch (StemNotFoundException e) {
   *   // Stem not found
   * }
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @param   uuids  Get stem with this UUID.
   * @param queryOptions
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Set<Stem> findByUuids(GrouperSession s, Collection<String> uuids, QueryOptions queryOptions) 
    throws StemNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set<Stem> ns = GrouperDAOFactory.getFactory().getStem().findByUuids(uuids, queryOptions) ;
    
    for (Stem stem : GrouperUtil.nonNull(ns)) {
      stemFlashCacheAddIfSupposedTo(stem);
    }
    return ns;
  } // public static Stem findByUuid(s, uuid)

  // @since   1.2.0
  public static Set internal_findAllByApproximateDisplayExtension(GrouperSession s, String val) 
    throws  QueryException
  {
    //note, no need for GrouperSession inverse of control
    // @session true
    Set       stems = new LinkedHashSet();
    Stem      ns;
    Iterator  it    = GrouperDAOFactory.getFactory().getStem().findAllByApproximateDisplayExtension(val).iterator();
    while (it.hasNext()) {
      ns = (Stem) it.next() ;
      stems.add(ns);
    }
    return stems;
  } // protected static Set internal_findAllByApproximateDisplayExtension(s, val)

  // @since   1.2.0
  public static Set internal_findAllByApproximateDisplayName(GrouperSession s, String val) 
    throws  QueryException
  {
    //note, no need for GrouperSession inverse of control
    // @session true
    Set       stems = new LinkedHashSet();
    Stem      ns;
    Iterator  it    = GrouperDAOFactory.getFactory().getStem().findAllByApproximateDisplayName(val).iterator();
    while (it.hasNext()) {
      ns = (Stem) it.next() ;
      stems.add(ns);
    }
    return stems;
  } // public static Set internal_findAllByApproximateDisplayExtension(s, val)

  // @since   1.2.0
  public static Set internal_findAllByApproximateExtension(GrouperSession s, String val) 
    throws  QueryException
  {
    //note, no need for GrouperSession inverse of control
    // @session true
    Set       stems = new LinkedHashSet();
    Stem      ns;
    Iterator  it    = GrouperDAOFactory.getFactory().getStem().findAllByApproximateExtension(val).iterator();
    while (it.hasNext()) {
      ns = (Stem) it.next() ;
      stems.add(ns);
    }
    return stems;
  } // public static Set internal_findAllByApproximateExtension(s, val)

  // @since   1.2.0
  public static Set internal_findAllByApproximateName(GrouperSession s, String val) 
    throws  QueryException
  {
    //note, no need for GrouperSession inverse of control
    // @session true
    Set       stems = new LinkedHashSet();
    Stem      ns;
    Iterator  it    = GrouperDAOFactory.getFactory().getStem().findAllByApproximateName(val).iterator();
    while (it.hasNext()) {
      ns = (Stem) it.next() ;
      stems.add(ns);
    }
    return stems;
  } // public static Set internal_findAllByApproximateName(s, val)

  // @since   1.2.0
  public static Set internal_findAllByApproximateNameAny(GrouperSession s, String val) 
    throws  QueryException
  {
    //note, no need for GrouperSession inverse of control
    // @session true
    Set       stems = new LinkedHashSet();
    Stem      ns;
    Iterator  it    = GrouperDAOFactory.getFactory().getStem().findAllByApproximateNameAny(val).iterator();
    while (it.hasNext()) {
      ns = (Stem) it.next() ;
      stems.add(ns);
    }
    return stems;
  } // public static Set internal_findAllByApproximateNameAny(s, val)

  // @since   1.2.0
  public static Set internal_findAllByCreatedAfter(GrouperSession s, Date d) 
    throws  QueryException
  {
    //note, no need for GrouperSession inverse of control
    // @session true
    Set       stems = new LinkedHashSet();
    Stem      ns;
    Iterator  it    = GrouperDAOFactory.getFactory().getStem().findAllByCreatedAfter(d).iterator();
    while (it.hasNext()) {
      ns = (Stem) it.next() ;
      stems.add(ns);
    }
    return stems;
  } // public static Set internal_findAllByCreatedAfter(s, d)

  // @since   1.2.0
  public static Set internal_findAllByCreatedBefore(GrouperSession s, Date d) 
    throws  QueryException
  {
    //note, no need for GrouperSession inverse of control
    // @session true
    Set       stems = new LinkedHashSet();
    Stem      ns;
    Iterator  it    = GrouperDAOFactory.getFactory().getStem().findAllByCreatedBefore(d).iterator();
    while (it.hasNext()) {
      ns = (Stem) it.next() ;
      stems.add(ns);
    }
    return stems;
  } // public static Set internal_findAllByCreatedBefore(s, d)

  /**
   * 
   * @param name
   * @param exceptionIfNotFound
   * @return the stem
   * @throws StemNotFoundException
   */
  public static Stem internal_findByName(String name, boolean exceptionIfNotFound) 
    throws  StemNotFoundException {
    // @session false
    if (name.equals(Stem.ROOT_NAME)) {
      name = Stem.ROOT_INT;
    }
    return GrouperDAOFactory.getFactory().getStem().findByName(name, exceptionIfNotFound);
  } // public static StemDTO internal_findByName(name)

  /**
   * Find a stem within the registry by ID index.
   * @param idIndex id index of stem to find.
   * @param exceptionIfNotFound true if exception if not found
   * @param queryOptions 
   * @return  A {@link Stem}
   * @throws StemNotFoundException if not found an exceptionIfNotFound is true
   */
  public static Stem findByIdIndex(Long idIndex, boolean exceptionIfNotFound,  QueryOptions queryOptions) 
      throws StemNotFoundException {
    
    Stem ns = stemFlashCacheRetrieve(idIndex, queryOptions);
    if (ns != null) {
      return ns;
    }      
    
    ns = GrouperDAOFactory.getFactory().getStem().findByIdIndex(idIndex, exceptionIfNotFound, queryOptions);
    
    if (ns != null) {
      stemFlashCacheAddIfSupposedTo(ns);
      return ns;
    }
    if (!exceptionIfNotFound) {
      return null;
    }
    throw new StemNotFoundException("Cant find stem: '" + idIndex + "'");

  }

  /**
   * find stems where the static grouper session has certain privileges on the results
   */
  private Set<Privilege> privileges;

  /**
   * if sorting or paging
   */
  private QueryOptions queryOptions;
  
  /**
   * scope to look for stems Wildcards will be appended or percent is the wildcard
   */
  private String scope;

  /**
   * if the scope has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   */
  private boolean splitScope;

  /**
   * this is the subject that has certain privileges
   */
  private Subject subject;

  /**
   * parent or ancestor stem of the group
   */
  private String parentStemId;

  /**
   * if we are looking up a stem, only look by uuid or name
   */
  private boolean findByUuidOrName;
  
  /**
   * if we are looking up a stem, only look by uuid or name
   * @param theFindByUuidOrName
   * @return the stem finder
   */
  public StemFinder assignFindByUuidOrName(boolean theFindByUuidOrName) {
    
    this.findByUuidOrName = theFindByUuidOrName;
    
    return this;
  }
  
  /**
   * if passing in a stem, this is the stem scope...
   */
  private Scope stemScope;

  /**
   * stem ids to find
   */
  private Collection<String> stemIds;

  /**
   * find stems that have this attribute def name id
   */
  private String attributeDefNameId;
  
  /**
   * find stems with this value
   */
  private Object attributeValue;
  
  /**
   * check read on attribute def when checking attribute def name
   */
  private Boolean attributeCheckReadOnAttributeDef;

  /**
   * if looking for an attribute value on an assignment, could be multiple values
   */
  private Set<Object> attributeValuesOnAssignment;

  /**
   * find stems that have this attribute def name id, note could be an assignment on an assignment
   */
  private String attributeDefNameId2;

  /**
   * if looking for an attribute value on an assignment, could be multiple values
   */
  private Set<Object> attributeValuesOnAssignment2;

  /**
   * find groups with this value
   */
  private Object attributeValue2;

  /**
   * config key for caching
   */
  private static final String GROUPER_FLASHCACHE_STEMS_IN_FINDER = "grouper.flashcache.stems.in.finder";

  /**
   * cache stuff in stems by name, uuid, idIndex
   */
  private static GrouperCache<Object, Stem> stemFlashCache = new GrouperCache(
      "edu.internet2.middleware.grouper.StemFinder.stemFlashCache", 10000, false, 5, 5, false);

  /**
   * check read on attribute def when checking attribute def name
   * @param theAttributeCheckReadOnAttributeDef
   * @return this for chaining
   */
  public StemFinder assignAttributeCheckReadOnAttributeDef(boolean theAttributeCheckReadOnAttributeDef) {
    this.attributeCheckReadOnAttributeDef = theAttributeCheckReadOnAttributeDef;
    return this;
  }
  
  /**
   * find objects with this value
   * @param theValue
   * @return this for chaining
   */
  public StemFinder assignAttributeValue(Object theValue) {
    if (theValue == null) {
      throw new RuntimeException("Cant look for a null value");
    }
    this.attributeValue = theValue;
    return this;
  }
  
  /**
   * add a privilege to filter by that the subject has on the stem
   * @param privilege should be AccessPrivilege
   * @return this for chaining
   */
  public StemFinder addPrivilege(Privilege privilege) {
    
    if (this.privileges == null) {
      this.privileges = new HashSet<Privilege>();
    }
    
    this.privileges.add(privilege);
    
    return this;
  }

  /**
   * assign privileges to filter by that the subject has on the stem
   * @param theStems
   * @return this for chaining
   */
  public StemFinder assignPrivileges(Set<Privilege> theStems) {
    this.privileges = theStems;
    return this;
  }

  /**
   * if sorting, paging, caching, etc
   * @param theQueryOptions
   * @return this for chaining
   */
  public StemFinder assignQueryOptions(QueryOptions theQueryOptions) {
    this.queryOptions = theQueryOptions;
    return this;
  }

  /**
   * scope to look for stems  Wildcards will be appended or percent is the wildcard
   * @param theScope
   * @return this for chaining
   */
  public StemFinder assignScope(String theScope) {
    this.scope = theScope;
    return this;
  }

  /**
   * if the scope has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   * @param theSplitScope
   * @return this for chaining
   */
  public StemFinder assignSplitScope(boolean theSplitScope) {
    this.splitScope = theSplitScope;
    return this;
  }

  /**
   * this is the subject that has certain privileges or is in the query
   * @param theSubject
   * @return this for chaining
   */
  public StemFinder assignSubject(Subject theSubject) {
    this.subject = theSubject;
    return this;
  }

  /**
   * find the stem
   * @return the stem or null
   */
  public Stem findStem() {
    Set<Stem> stems = this.findStems();
    
    Stem stem = GrouperUtil.setPopOne(stems);

    if (stem != null) {
      stemFlashCacheAddIfSupposedTo(stem);
    }
    
    return stem;
  }

  /**
   * find all the stems
   * @return the set of stems or the empty set if none found
   */
  public Set<Stem> findStems() {
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.emptySetOfLookupsReturnsNoResults", true)) {
    
      // if passed in empty set of stem ids and no names, then no stems found
      // uncomment this if we can search by stem names
      if (this.stemIds != null && this.stemIds.size() == 0 /* && GrouperUtil.length(this.stemNames) == 0 */ ) {
        return new HashSet<Stem>();
      }
    }    

    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
  
    Set<Stem> stems = GrouperDAOFactory.getFactory().getStem()
        .getAllStemsSecure(this.scope, grouperSession, this.subject, this.privileges, 
            this.queryOptions, this.splitScope, this.parentStemId, this.stemScope, 
            this.findByUuidOrName, this.userHasInGroupFields,
            this.userHasInAttributeFields, this.stemIds, 
            this.attributeDefNameId, this.attributeValue, this.attributeCheckReadOnAttributeDef,
            this.attributeValuesOnAssignment, 
            this.attributeDefNameId2, this.attributeValue2, this.attributeValuesOnAssignment2);
   
    for (Stem stem : GrouperUtil.nonNull(stems)) {
      stemFlashCacheAddIfSupposedTo(stem);
    }
    
    return stems;
  }

  /**
   * parent or ancestor stem of the stem
   * @param theParentStemId
   * @return this for chaining
   */
  public StemFinder assignParentStemId(String theParentStemId) {
    this.parentStemId = theParentStemId;
    return this;
  }

  /**
   * if passing in a stem, this is the stem scope...
   * @param theStemScope
   * @return this for chaining
   */
  public StemFinder assignStemScope(Scope theStemScope) {
    this.stemScope = theStemScope;
    return this;
  }

  /**
   * add a stem id to search for
   * @param stemId
   * @return this for chaining
   */
  public StemFinder addStemId(String stemId) {
    if (this.stemIds == null) {
      this.stemIds = new HashSet<String>();
    }
    this.stemIds.add(stemId);
    return this;
  }

  /**
   * assign stem ids to search for
   * @param theStemIds
   * @return this for chaining
   */
  public StemFinder assignStemIds(Collection<String> theStemIds) {
    this.stemIds = theStemIds;
    return this;
  }

  /**
   * if looking for an attribute value on an assignment, could be multiple values
   * @param value
   * @return this for chaining
   */
  public StemFinder addAttributeValuesOnAssignment(Object value) {
    if (this.attributeValuesOnAssignment == null) {
      this.attributeValuesOnAssignment = new HashSet<Object>();
    }
    this.attributeValuesOnAssignment.add(value);
    return this;
  }

  /**
   * if looking for an attribute value on an assignment2, could be multiple values
   * @param value
   * @return this for chaining
   */
  public StemFinder addAttributeValuesOnAssignment2(Object value) {
    if (this.attributeValuesOnAssignment2 == null) {
      this.attributeValuesOnAssignment2 = new HashSet<Object>();
    }
    this.attributeValuesOnAssignment2.add(value);
    return this;
  }

  /**
   * if looking for an attribute value on an assignment, could be multiple values
   * @param theValues
   * @return this for chaining
   */
  public StemFinder assignAttributeValuesOnAssignment(Set<Object> theValues) {
    this.attributeValuesOnAssignment = theValues;
    return this;
  }

  /**
   * if looking for an attribute value on an assignment2, could be multiple values
   * @param theValues
   * @return this for chaining
   */
  public StemFinder assignAttributeValuesOnAssignment2(Set<Object> theValues) {
    this.attributeValuesOnAssignment2 = theValues;
    return this;
  }

  /**
   * find stems that have this attribute def name id, note could be an assignment on an assignment
   * @param theAttributeDefNameId
   * @return this for chaining
   */
  public StemFinder assignIdOfAttributeDefName(String theAttributeDefNameId) {
    this.attributeDefNameId = theAttributeDefNameId;
    return this;
  }

  /**
   * find stems that have this attribute def name id, note could be an assignment on an assignment
   * @param theAttributeDefNameId
   * @return this for chaining
   */
  public StemFinder assignIdOfAttributeDefName2(String theAttributeDefNameId) {
    this.attributeDefNameId2 = theAttributeDefNameId;
    return this;
  }

  /**
   * find stems that have this attribute assigned
   * @param theNameOfAttributeDefName
   * @return this for chaining
   */
  public StemFinder assignNameOfAttributeDefName(String theNameOfAttributeDefName) {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(theNameOfAttributeDefName, true);
    
    this.attributeDefNameId = attributeDefName.getId();
    return this;
  }

  /**
   * find stems that have this attribute assigned
   * @param theNameOfAttributeDefName
   * @return this for chaining
   */
  public StemFinder assignNameOfAttributeDefName2(String theNameOfAttributeDefName) {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(theNameOfAttributeDefName, true);
    
    this.attributeDefNameId2 = attributeDefName.getId();
    return this;
  }

  /**
   * see if this is cacheable
   * @param id
   * @param queryOptions 
   * @return if cacheable
   */
  private static boolean stemFlashCacheable(Object id, QueryOptions queryOptions) {
  
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_STEMS_IN_FINDER, true)) {
      return false;
    }
  
    if (id == null || ((id instanceof String) && StringUtils.isBlank((String)id))) {
      return false;
    }
  
    if (!HibUtils.secondLevelCaching(true, queryOptions)) {
      return false;
    }
    
    return true;
  }

  /**
   * add stem to cache if not null
   * @param stem
   */
  private static void stemFlashCacheAddIfSupposedTo(Stem stem) {
    if (stem == null || !GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_STEMS_IN_FINDER, true)) {
      return;
    }
    
    for (Object id : new Object[]{stem.getUuid(), stem.getName(), stem.getIdIndex()}) {
      if (id == null) {
        continue;
      }
      stemFlashCache.put(id, stem);
    }
    
  }

  /**
   * multikey
   * @param id
   * @return if cacheable
   */
  private static Object stemFlashCacheMultikey(Object id) {
    
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_STEMS_IN_FINDER, true)) {
      return null;
    }
  
    return id;
  }

  /**
   * get a stem fom flash cache
   * @param id
   * @param queryOptions
   * @return the stem or null
   */
  private static Stem stemFlashCacheRetrieve(Object id, QueryOptions queryOptions) {
    if (stemFlashCacheable(id, queryOptions)) {
      Object stemFlashKey = stemFlashCacheMultikey(id);
      //see if its already in the cache
      Stem stem = stemFlashCache.get(stemFlashKey);
      if (stem != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("retrieving from stem flash cache by id: " + stem.getName());
        }
        return stem;
      }
    }
    return null;
  }

}

