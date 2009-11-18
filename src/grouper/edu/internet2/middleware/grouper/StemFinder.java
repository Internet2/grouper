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
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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

}

