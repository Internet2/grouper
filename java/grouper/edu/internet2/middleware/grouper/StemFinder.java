/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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
 * <p />
 * @author  blair christensen.
 * @version $Id: StemFinder.java,v 1.2 2005-11-11 18:32:07 blair Exp $
 */
public class StemFinder {

  // Public Class Methods

  /**
   * Find root stem of the Groups Registry.
   * <pre class="eg">
   * // Find the root stem.
   * Stem rootStem = StemFinder.findRootStem(s);
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Stem findRootStem(GrouperSession s) 
    throws StemNotFoundException
  {
    // TODO Should this ever throw a SNFE?
    // TODO This is *obviously* not right
    Stem root = new Stem(s);
    try {
      HibernateHelper.save(root);
      return root;
    }
    catch (HibernateException e) {
      throw new StemNotFoundException(
        "root stem not found: " + e.getMessage()
      );
    }
    //return new Stem();
  } // public static Stem findRootStem(s)

  /**
   * Get stem by name.
   * <pre class="eg">
   * // Get the specified stem by name.
   * try {
   *   Stem stem = StemFinder.getByName(s, name);
   * }
   * catch (StemNotFoundException e) {
   *   // Stem not found
   * }
   * </pre>
   * @param   s     Search within this {@link GrouperSession} context
   * @param   name  Get stem with this name.
   * @return  A {@link Stem} object
   * @throws  StemNotFoundException
   */
  public static Stem getByName(GrouperSession s, String name) 
    throws StemNotFoundException
  {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get stem by uuid.
   * <pre class="eg">
   * // Get the specified stem by uuid.
   * try {
   *   Stem stem = StemFinder.getByUuid(s, uuid);
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
  public static Stem getByUuid(GrouperSession s, String uuid) 
    throws StemNotFoundException
  {
    Stem ns = findByUuid(uuid);
    ns.setSession(s);
    return ns;
  } // public static Stem getByUuid(s, uuid)


  // Protected Class Methods

  // @return  stems created after this date
  protected static Set findByCreatedAfter(GrouperSession s, Date d) 
    throws QueryException 
  {
    List stems = new ArrayList();
    try {
      Session hs  = HibernateHelper.getSession();
      List    l   = hs.find(
                      "from Stem as ns where  "
                      + "ns.create_time > ?   ",
                      new Long(d.getTime()),
                      Hibernate.LONG
                    )
                    ;
      hs.close();
      stems.addAll( Stem.setSession(s, l) );
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding stems: " + eH.getMessage()
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
      List    l   = hs.find(
                      "from Stem as ns where  "
                      + "ns.create_time < ?   ",
                      new Long(d.getTime()),
                      Hibernate.LONG
                      )
                      ;
      hs.close();
      stems.addAll( Stem.setSession(s, l) );
    }
    catch (HibernateException eH) {
      throw new QueryException(
        "error finding stems: " + eH.getMessage()
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
      List    stems = hs.find(
                        "from Stem as ns where  "
                        + "ns.stem_id = ?       ",
                        uuid,
                        Hibernate.STRING
                      )
                      ;
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
        "error finding stem: " + eH.getMessage()
      );  
    }
  } // protected static Stem findByUuid(s, uuid)

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
      // Nothing
    }
    return false;
  } // protected static boolean isChild(ns, stem)

}

