/*******************************************************************************
 * Copyright 2012 Internet2
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

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
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
   * @param exceptionOnNotFound
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Stem findByName(GrouperSession s, String name, boolean exceptionOnNotFound)
      throws StemNotFoundException {
    return findByName(s, name, exceptionOnNotFound, null);
  }

  
  /**
   * Find stem by name.
   * <pre class="eg">
   *   Stem stem = StemFinder.findByName(s, name, false);
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @param   name  Find stem with this name.
   * @param exceptionOnNotFound
   * @param queryOptions
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Stem findByName(GrouperSession s, String name, boolean exceptionOnNotFound, QueryOptions queryOptions)
      throws StemNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    // TODO 20070314 bah.  should be in dao if it exists at all.
    if ( name.equals(Stem.ROOT_NAME) ) {
      name = Stem.ROOT_INT;
    }
    Stem ns = GrouperDAOFactory.getFactory().getStem().findByName(name, exceptionOnNotFound, queryOptions) ;
    return ns;
  }
  
  /**
   * Find stem by its alternate name.
   * <pre class="eg">
   *   Stem stem = StemFinder.findByAlternateName(s, name, false);
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @param   name  Find stem with this alternate name.
   * @param exceptionOnNotFound
   * @param queryOptions
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Stem findByAlternateName(GrouperSession s, String name, boolean exceptionOnNotFound, QueryOptions queryOptions)
      throws StemNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    // TODO 20070314 bah.  should be in dao if it exists at all.
    if ( name.equals(Stem.ROOT_NAME) ) {
      name = Stem.ROOT_INT;
    }
    Stem ns = GrouperDAOFactory.getFactory().getStem().findByAlternateName(name, exceptionOnNotFound, queryOptions) ;
    return ns;
  }
  
  /**
   * Find stem by its current name.
   * <pre class="eg">
   *   Stem stem = StemFinder.findByCurrentName(s, name, false);
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @param   name  Find stem with this name.
   * @param exceptionOnNotFound
   * @param queryOptions
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Stem findByCurrentName(GrouperSession s, String name, boolean exceptionOnNotFound, QueryOptions queryOptions)
      throws StemNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    // TODO 20070314 bah.  should be in dao if it exists at all.
    if ( name.equals(Stem.ROOT_NAME) ) {
      name = Stem.ROOT_INT;
    }
    Stem ns = GrouperDAOFactory.getFactory().getStem().findByCurrentName(name, exceptionOnNotFound, queryOptions) ;
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
   * @param exceptionIfNull
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Stem findByUuid(GrouperSession s, String uuid, boolean exceptionIfNull) 
    throws StemNotFoundException {
    return findByUuid(s, uuid, exceptionIfNull, null);
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
   * @param exceptionIfNull
   * @param queryOptions
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Stem findByUuid(GrouperSession s, String uuid, boolean exceptionIfNull, QueryOptions queryOptions) 
    throws StemNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Stem ns = GrouperDAOFactory.getFactory().getStem().findByUuid(uuid, exceptionIfNull, queryOptions) ;
    return ns;
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
   * @param   uuid  Get stem with this UUID.
   * @param exceptionIfNull
   * @param queryOptions
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Set<Stem> findByUuids(GrouperSession s, Collection<String> uuids, QueryOptions queryOptions) 
    throws StemNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set<Stem> ns = GrouperDAOFactory.getFactory().getStem().findByUuids(uuids, queryOptions) ;
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
   * @param exceptionIfNull
   * @return
   * @throws StemNotFoundException
   */
  public static Stem internal_findByName(String name, boolean exceptionIfNull) 
    throws  StemNotFoundException {
    // @session false
    if (name.equals(Stem.ROOT_NAME)) {
      name = Stem.ROOT_INT;
    }
    return GrouperDAOFactory.getFactory().getStem().findByName(name, exceptionIfNull);
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
    Stem s = GrouperDAOFactory.getFactory().getStem().findByIdIndex(idIndex, exceptionIfNotFound, queryOptions);
    return s;
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
   * find all the stem
   * @return the set of stems or the empty set if none found
   */
  public Stem findStem() {
    Set<Stem> stems = this.findStems();
    return GrouperUtil.setPopOne(stems);
  }

  /**
   * find all the stems
   * @return the set of stemss or the empty set if none found
   */
  public Set<Stem> findStems() {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
  
    return GrouperDAOFactory.getFactory().getStem()
        .getAllStemsSecure(this.scope, grouperSession, this.subject, this.privileges, 
            this.queryOptions, this.splitScope, this.parentStemId, this.stemScope, this.findByUuidOrName);
    
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

}

