/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  java.util.*;
import  net.sf.hibernate.*;

/**
 * Find stems within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: StemFinder.java,v 1.31 2006-10-18 15:22:12 blair Exp $
 */
public class StemFinder {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = StemFinder.class.getName();
  
  
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
    GrouperSessionValidator.validate(s);
    Stem ns = findByName(name);
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
    GrouperSessionValidator.validate(s);
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
    GrouperSessionValidator.validate(s);
    Stem ns = findByUuid(uuid);
    ns.setSession(s);
    return ns;
  } // public static Stem findByUuid(s, uuid)


  // PROTECTED CLASS METHODS //
  protected static Set findByApproximateDisplayExtension(GrouperSession s, String val) 
    throws  QueryException
  {
    Set stems = new LinkedHashSet();
    try {
      Session   hs      = HibernateHelper.getSession();
      Query     qry     = hs.createQuery(
        "from Stem as ns where ns.display_extension like :value"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByApproximateDisplayExtension");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      Stem      ns;
      Iterator  iter    = qry.iterate();
      while (iter.hasNext()) {
        ns = (Stem) iter.next();
        ns.setSession(s);
        stems.add(ns);
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding stems: " + eH.getMessage(), eH
      );
    }
    return stems;
  } // protected static Set findByApproximateDisplayExtension(s, val)

  protected static Set findByApproximateDisplayName(GrouperSession s, String val) 
    throws  QueryException
  {
    Set stems = new LinkedHashSet();
    try {
      Session   hs      = HibernateHelper.getSession();
      Query     qry     = hs.createQuery(
        "from Stem as ns where ns.display_name like :value"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByApproximateDisplayName");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      Stem      ns;
      Iterator  iter    = qry.iterate();
      while (iter.hasNext()) {
        ns = (Stem) iter.next();
        ns.setSession(s);
        stems.add(ns);
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding stems: " + eH.getMessage(), eH
      );
    }
    return stems;
  } // protected static Set findByApproximateDisplayName(s, val)

  protected static Set findByApproximateExtension(GrouperSession s, String val) 
    throws  QueryException
  {
    Set stems = new LinkedHashSet();
    try {
      Session   hs      = HibernateHelper.getSession();
      Query     qry     = hs.createQuery(
        "from Stem as ns where ns.stem_extension like :value"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByApproximateExtension");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      Stem      ns;
      Iterator  iter    = qry.iterate();
      while (iter.hasNext()) {
        ns = (Stem) iter.next();
        ns.setSession(s);
        stems.add(ns);
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding stems: " + eH.getMessage(), eH
      );
    }
    return stems;
  } // protected static Set findByApproximateExtension(s, val)

  protected static Set findByApproximateName(GrouperSession s, String val) 
    throws  QueryException
  {
    Set stems = new LinkedHashSet();
    try {
      Session   hs      = HibernateHelper.getSession();
      Query     qry     = hs.createQuery(
        "from Stem as ns where ns.stem_name like :value"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByApproximateName");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      Stem      ns;
      Iterator  iter    = qry.iterate();
      while (iter.hasNext()) {
        ns = (Stem) iter.next();
        ns.setSession(s);
        stems.add(ns);
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding stems: " + eH.getMessage(), eH
      );
    }
    return stems;
  } // protected static Set findByApproximateName(s, val)

  protected static Set findByApproximateNameAny(GrouperSession s, String name) 
    throws  QueryException
  {
    Set stems = new LinkedHashSet();
    try {
      Session   hs      = HibernateHelper.getSession();
      Query     qry     = hs.createQuery(
        "from Stem as ns where "
        + "   lower(ns.stem_name)         like :name "
        + "or lower(ns.display_name)      like :name "
        + "or lower(ns.stem_extension)    like :name "
        + "or lower(ns.display_extension) like :name" 
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByApproximateNameAny");
      qry.setString("name", "%" + name.toLowerCase() + "%");
      Stem      ns;
      Iterator  iter    = qry.iterate();
      while (iter.hasNext()) {
        ns = (Stem) iter.next();
        ns.setSession(s);
        stems.add(ns);
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding stems: " + eH.getMessage(), eH
      );
    }
    return stems;
  } // protected static Set findByApproximateNameAny(s, name)

  // @return  stems created after this date
  protected static Set findByCreatedAfter(GrouperSession s, Date d) 
    throws QueryException 
  {
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from Stem as ns where ns.create_time > :time");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByCreatedAfter");
      return _findByDate(s, hs, qry, d);
    }
    catch (HibernateException eH) {
      throw new QueryException("error finding stems: " + eH.getMessage(), eH);  
    }
  } // protected static Set findByCreatedAfter(s, d)

  // @return  stems created before this date
  protected static Set findByCreatedBefore(GrouperSession s, Date d) 
    throws QueryException 
  {
    try {
      Session   hs    = HibernateHelper.getSession();
      Query     qry   = hs.createQuery("from Stem as ns where ns.create_time < :time");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByCreatedBefore");
      return _findByDate(s, hs, qry, d);
    }
    catch (HibernateException eH) {
      throw new QueryException("error finding stems: " + eH.getMessage(), eH);  
    }
  } // protected static Set findByCreatedBefore(s, d)

  // @since   1.1.0
  protected static Stem findByName(String name) 
    throws  StemNotFoundException
  {
    Stem ns = null;
    try {
      if (name.equals(Stem.ROOT_EXT)) {
        name = Stem.ROOT_INT;
      }
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from Stem as ns where ns.stem_name = :name");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByName");
      qry.setString("name", name);
      ns = (Stem) qry.uniqueResult();
      hs.close();
      if (ns == null) {
        throw new StemNotFoundException(E.NO_STEM + " by name: " + U.q(name));
      }
      return ns;
    }
    catch (HibernateException eH) {
      throw new StemNotFoundException(E.NO_STEM + ": " + eH.getMessage(), eH);
    }
  } // protected static Stem findByName(name)

  protected static Stem findByUuid(String uuid)
    throws StemNotFoundException
  {
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from Stem as ns where ns.uuid = :uuid");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      Stem ns = (Stem) qry.uniqueResult();
      hs.close();
      if (ns == null) {
        throw new StemNotFoundException(E.NO_STEM + " by uuid: " + U.q(uuid));
      }
      return ns; 
    }
    catch (HibernateException eH) {
      throw new StemNotFoundException(E.NO_STEM + ": " + eH.getMessage(), eH);
    }
  } // protected static Stem findByUuid(uuid)

  // TODO 20061018 Is this the right location?  And should it be top-down?
  protected static boolean isChild(Stem ns, Group g) {
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
      String msg = E.STEMF_ISCHILDGROUP + U.q(parent.getName()) + " " + eSNF.getMessage();
      ErrorLog.error(StemFinder.class, msg);
    }
    return false;
  } // protected static boolean isChild(ns, g)

  // TODO 20061018 Is this the right location?  And should it be top-down?
  protected static boolean isChild(Stem ns, Stem stem) {
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
        msg += U.q(parent.getName());
      }
      msg += " start=" + U.q(stem.getName());
      msg += " " + eSNF.getMessage();
      ErrorLog.error(StemFinder.class, msg);
    }
    return false;
  } // protected static boolean isChild(ns, stem)


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static Set _findByDate(GrouperSession s, Session hs, Query qry, Date d)
    throws  HibernateException
  {
    qry.setLong( "time", d.getTime() );
    List        l     = qry.list();
    hs.close();
    Stem        ns;
    Set         stems = new LinkedHashSet();
    Iterator    it    = l.iterator();
    while (it.hasNext()) {
      ns = (Stem) it.next();
      ns.setSession(s);
      stems.add(ns);
    }
    return stems;
  } // private static Set _findByDate(s, hs, qry, d)

} // public class StemFinder

