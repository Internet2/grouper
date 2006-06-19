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
import  net.sf.hibernate.type.*;

/**
 * Find stems within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: StemFinder.java,v 1.20 2006-06-19 15:17:40 blair Exp $
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
    Stem ns = null;
    try {
      if (name.equals(Stem.ROOT_EXT)) {
        name = Stem.ROOT_INT;
      }
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Stem as ns where ns.stem_name = :name"
      );
      qry.setCacheable(GrouperConfig.QRY_SF_FBN);
      qry.setCacheRegion(GrouperConfig.QCR_SF_FBN);
      qry.setString("name", name);
      List    l   = qry.list();
      if (l.size() == 1) {
        ns = (Stem) l.get(0);
        ns.setSession(s);
      } 
      hs.close();
    }
    catch (HibernateException eH) {
      throw new StemNotFoundException(
        "unable to find stem: " + eH.getMessage(), eH
      );
    }
    if (ns == null) {
      throw new StemNotFoundException(
        "unable to find stem"
      );
    }
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
      qry.setCacheable(GrouperConfig.QRY_SF_FBADE);
      qry.setCacheRegion(GrouperConfig.QCR_SF_FBADE);
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      Iterator  iter    = qry.iterate();
      while (iter.hasNext()) {
        Stem ns = (Stem) iter.next();
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
      qry.setCacheable(GrouperConfig.QRY_SF_FBADN);
      qry.setCacheRegion(GrouperConfig.QCR_SF_FBADN);
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      Iterator  iter    = qry.iterate();
      while (iter.hasNext()) {
        Stem ns = (Stem) iter.next();
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
      qry.setCacheable(GrouperConfig.QRY_SF_FBAE);
      qry.setCacheRegion(GrouperConfig.QCR_SF_FBAE);
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      Iterator  iter    = qry.iterate();
      while (iter.hasNext()) {
        Stem ns = (Stem) iter.next();
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
      qry.setCacheable(GrouperConfig.QRY_SF_FBAN);
      qry.setCacheRegion(GrouperConfig.QCR_SF_FBAN);
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      Iterator  iter    = qry.iterate();
      while (iter.hasNext()) {
        Stem ns = (Stem) iter.next();
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
      qry.setCacheable(GrouperConfig.QRY_SF_FBANA);
      qry.setCacheRegion(GrouperConfig.QCR_SF_FBANA);
      qry.setString("name", "%" + name.toLowerCase() + "%");
      Iterator  iter    = qry.iterate();
      while (iter.hasNext()) {
        Stem ns = (Stem) iter.next();
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
    List stems = new ArrayList();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Stem as ns where ns.create_time > :time"
      );
      qry.setCacheable(GrouperConfig.QRY_SF_FBCA);
      qry.setCacheRegion(GrouperConfig.QCR_SF_FBCA);
      qry.setLong("time", d.getTime());
      List    l   = qry.list();
      hs.close();
      Iterator  iter = l.iterator();
      while (iter.hasNext()) {
        Stem ns = (Stem) iter.next();
        ns.setSession(s);
        stems.add(ns);
      }
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding stems: " + eH.getMessage(), eH
      );  
    }
    return new LinkedHashSet(stems);
  } // protected static Set findByCreatedAfter(s, d)

  // @return  stems created before this date
  protected static Set findByCreatedBefore(GrouperSession s, Date d) 
    throws QueryException 
  {
    List stems = new ArrayList();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Stem as ns where ns.create_time < :time"
      );
      qry.setCacheable(GrouperConfig.QRY_SF_FBCB);
      qry.setCacheRegion(GrouperConfig.QCR_SF_FBCB);
      qry.setLong("time", d.getTime());
      List    l   = qry.list();
      hs.close();
      Iterator  iter = l.iterator();
      while (iter.hasNext()) {
        Stem ns = (Stem) iter.next();
        ns.setSession(s);
        stems.add(ns);
      }
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding stems: " + eH.getMessage(), eH
      );  
    }
    return new LinkedHashSet(stems);
  } // protected static Set findByCreatedBefore(s, d)

  protected static Stem findByUuid(String uuid)
    throws StemNotFoundException
  {
    try {
      Stem    ns    = null;
      Session hs    = HibernateHelper.getSession();
      Query   qry   = hs.createQuery(
        "from Stem as ns where ns.uuid = :uuid"
      );
      qry.setCacheable(GrouperConfig.QRY_SF_FBU);
      qry.setCacheRegion(GrouperConfig.QCR_SF_FBU);
      qry.setString("uuid", uuid);
      List    stems = qry.list();
      if (stems.size() == 1) {
        ns = (Stem) stems.get(0);
      }
      hs.close();
      if (ns == null) {
        throw new StemNotFoundException("stem not found");
      }
      return ns; 
    }
    catch (HibernateException eH) {
      throw new StemNotFoundException(
        "error finding stem: " + eH.getMessage(), eH
      );  
    }
  } // protected static Stem findByUuid(uuid)

  // TODO Is this the right location for this method?
  // TODO Would a top-down rather than bottom-up approach work better?
  protected static boolean isChild(Stem ns, Group g) {
    try {
      Stem parent = g.getParentStem();
      while (parent != null) {
        if (parent.equals(ns)) {
          return true;
        }
        parent = parent.getParentStem();
      }
    }
    catch (StemNotFoundException eSNF) {
      ErrorLog.error(StemFinder.class, E.STEMF_ISCHILDGROUP + eSNF.getMessage());
      // Nothing
    }
    return false;
  } // protected static boolean isChild(ns, g)

  // TODO Is this the right location for this method?
  // TODO Would a top-down rather than bottom-up approach work better?
  protected static boolean isChild(Stem ns, Stem stem) {
    try {
      Stem parent = stem.getParentStem();
      while (parent != null) {
        if (parent.equals(ns)) {
          return true;
        }
        parent = parent.getParentStem();
      }
    }
    catch (StemNotFoundException eSNF) {
      ErrorLog.error(StemFinder.class, E.STEMF_ISCHILDSTEM + eSNF.getMessage());
    }
    return false;
  } // protected static boolean isChild(ns, stem)

}

