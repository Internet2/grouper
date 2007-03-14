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
import  java.util.Date;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;

/**
 * Find stems within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: StemFinder.java,v 1.38 2007-03-14 19:54:08 blair Exp $
 */
public class StemFinder {

  // PUBLIC CLASS METHODS //

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
   */
  public static Stem findByName(GrouperSession s, String name) 
    throws StemNotFoundException
  {
    GrouperSession.validate(s);
    // TODO 20070314 bah.  should be in dao if it exists at all.
    if ( name.equals(Stem.ROOT_EXT) ) {
      name = Stem.ROOT_INT;
    }
    Stem ns = new Stem();
    ns.setDTO( HibernateStemDAO.findByName(name) );
    ns.setSession(s);
    return ns;
  } // public static Stem findByName(s, name)

  /**
   * Find root stem of the Groups Registry.
   * <pre class="eg">
   * // Find the root stem.
   * Stem rootStem = StemFinder.findRootStem(s);
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @return  A {@link Stem} object
   * @throws  GrouperRuntimeException
   */
  public static Stem findRootStem(GrouperSession s) 
    throws  GrouperRuntimeException
  {
    GrouperSession.validate(s);
    try {
      return StemFinder.findByName(s, Stem.ROOT_INT);
    }
    catch (StemNotFoundException eSNF) {
      String msg = E.STEM_ROOTNOTFOUND;
      ErrorLog.fatal(StemFinder.class, msg);
      throw new GrouperRuntimeException(msg, eSNF);
    }
  } // public static Stem findRootStem(s)

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
   */
  public static Stem findByUuid(GrouperSession s, String uuid) 
    throws StemNotFoundException
  {
    GrouperSession.validate(s);
    Stem ns = new Stem();
    ns.setDTO( HibernateStemDAO.findByUuid(uuid) );
    ns.setSession(s);
    return ns;
  } // public static Stem findByUuid(s, uuid)


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Set internal_findAllByApproximateDisplayExtension(GrouperSession s, String val) 
    throws  QueryException
  {
    // @session true
    Set       stems = new LinkedHashSet();
    Stem      ns;
    Iterator  it    = HibernateStemDAO.findAllByApproximateDisplayExtension(val).iterator();
    while (it.hasNext()) {
      ns = new Stem();
      ns.setDTO( (StemDTO) it.next() );
      ns.setSession(s);
      stems.add(ns);
    }
    return stems;
  } // protected static Set internal_findAllByApproximateDisplayExtension(s, val)

  // @since   1.2.0
  protected static Set internal_findAllByApproximateDisplayName(GrouperSession s, String val) 
    throws  QueryException
  {
    // @session true
    Set       stems = new LinkedHashSet();
    Stem      ns;
    Iterator  it    = HibernateStemDAO.findAllByApproximateDisplayName(val).iterator();
    while (it.hasNext()) {
      ns = new Stem();
      ns.setDTO( (StemDTO) it.next() );
      ns.setSession(s);
      stems.add(ns);
    }
    return stems;
  } // protected static Set internal_findAllByApproximateDisplayExtension(s, val)

  // @since   1.2.0
  protected static Set internal_findAllByApproximateExtension(GrouperSession s, String val) 
    throws  QueryException
  {
    // @session true
    Set       stems = new LinkedHashSet();
    Stem      ns;
    Iterator  it    = HibernateStemDAO.findAllByApproximateExtension(val).iterator();
    while (it.hasNext()) {
      ns = new Stem();
      ns.setDTO( (StemDTO) it.next() );
      ns.setSession(s);
      stems.add(ns);
    }
    return stems;
  } // protected static Set internal_findAllByApproximateExtension(s, val)

  // @since   1.2.0
  protected static Set internal_findAllByApproximateName(GrouperSession s, String val) 
    throws  QueryException
  {
    // @session true
    Set       stems = new LinkedHashSet();
    Stem      ns;
    Iterator  it    = HibernateStemDAO.findAllByApproximateName(val).iterator();
    while (it.hasNext()) {
      ns = new Stem();
      ns.setDTO( (StemDTO) it.next() );
      ns.setSession(s);
      stems.add(ns);
    }
    return stems;
  } // protected static Set internal_findAllByApproximateName(s, val)

  // @since   1.2.0
  protected static Set internal_findAllByApproximateNameAny(GrouperSession s, String val) 
    throws  QueryException
  {
    // @session true
    Set       stems = new LinkedHashSet();
    Stem      ns;
    Iterator  it    = HibernateStemDAO.findAllByApproximateNameAny(val).iterator();
    while (it.hasNext()) {
      ns = new Stem();
      ns.setDTO( (StemDTO) it.next() );
      ns.setSession(s);
      stems.add(ns);
    }
    return stems;
  } // protected static Set internal_findAllByApproximateNameAny(s, val)

  // @since   1.2.0
  protected static Set internal_findAllByCreatedAfter(GrouperSession s, Date d) 
    throws  QueryException
  {
    // @session true
    Set       stems = new LinkedHashSet();
    Stem      ns;
    Iterator  it    = HibernateStemDAO.findAllByCreatedAfter(d).iterator();
    while (it.hasNext()) {
      ns = new Stem();
      ns.setDTO( (StemDTO) it.next() );
      ns.setSession(s);
      stems.add(ns);
    }
    return stems;
  } // protected static Set internal_findAllByCreatedAfter(s, d)

  // @since   1.2.0
  protected static Set internal_findAllByCreatedBefore(GrouperSession s, Date d) 
    throws  QueryException
  {
    // @session true
    Set       stems = new LinkedHashSet();
    Stem      ns;
    Iterator  it    = HibernateStemDAO.findAllByCreatedBefore(d).iterator();
    while (it.hasNext()) {
      ns = new Stem();
      ns.setDTO( (StemDTO) it.next() );
      ns.setSession(s);
      stems.add(ns);
    }
    return stems;
  } // protected static Set internal_findAllByCreatedBefore(s, d)

  // @since   1.2.0
  protected static StemDTO internal_findByName(String name) 
    throws  StemNotFoundException
  {
    // @session false
    if (name.equals(Stem.ROOT_EXT)) {
      name = Stem.ROOT_INT;
    }
    return HibernateStemDAO.findByName(name);
  } // protected static StemDTO internal_findByName(name)

  // TODO 20061018 Is this the right location?  And should it be top-down?
  // @since   1.2.0
  protected static boolean internal_isChild(Stem ns, Group g) {
    Stem parent = g.getParentStem();
    try {
      while (parent != null) {
        if (parent.equals(ns)) {
          return true;
        }
        // parent is root.  don't bother searching further.
        if (parent.getName().equals(Stem.ROOT_EXT)) {
          return false;
        }
        parent = parent.getParentStem();
      }
    }
    catch (StemNotFoundException eSNF) {
      String msg = E.STEMF_ISCHILDGROUP + U.internal_q(parent.getName()) + " " + eSNF.getMessage();
      ErrorLog.error(StemFinder.class, msg);
    }
    return false;
  } // protected static boolean internal_isChild(ns, g)

  // TODO 20061018 Is this the right location?  And should it be top-down?
  // @since   1.2.0
  protected static boolean internal_isChild(Stem ns, Stem stem) {
    // our start stem is the root stem.  bail out immediately.
    if (stem.getName().equals(Stem.ROOT_EXT)) {
      return false;
    }
    Stem parent = null;
    try {
      parent = stem.getParentStem();
      while (parent != null) {
        if (parent.equals(ns)) {
          return true;
        }
        // parent is root.  don't bother searching further.
        if (parent.getName().equals(Stem.ROOT_EXT)) {
          return false;
        }
        parent = parent.getParentStem();
      }
    }
    catch (StemNotFoundException eSNF) {
      String msg = E.STEMF_ISCHILDSTEM;
      if (parent == null) {
        msg += "null";
      }
      else {
        msg += U.internal_q(parent.getName());
      }
      msg += " start=" + U.internal_q(stem.getName());
      msg += " " + eSNF.getMessage();
      ErrorLog.error(StemFinder.class, msg);
    }
    return false;
  } // protected static boolean internal_isChild(ns, stem)

} // public class StemFinder

