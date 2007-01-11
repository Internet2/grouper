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
import  net.sf.hibernate.*;

/**
 * Stub Hibernate {@link Stem} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateStemDAO.java,v 1.10 2007-01-11 14:22:06 blair Exp $
 * @since   1.2.0
 */
class HibernateStemDAO extends HibernateDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateStemDAO.class.getName();


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Group createChildGroup(Stem parent, Group child, Member m)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.save(child);
        hs.update(parent);
        hs.save(m);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      }
      return child;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static Group createChildGroup(parent, child, m)

  // @since   1.2.0
  protected static Stem createChildStem(Stem parent, Stem child)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.save(child);
        hs.update(parent);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      }
      return child;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static Stem createChildStem(parent, child)

  // @since   1.2.0
  protected static Set findAllByApproximateDisplayExtension(String val) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Stem as ns where ns.display_extension like :value");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByApproximateDisplayExtension");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      stems.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } // protected static Set findAllByApproximateDisplayExtension(val)

  // @since   1.2.0
  protected static Set findAllByApproximateDisplayName(String val) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Stem as ns where ns.display_name like :value");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByApproximateDisplayName");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      stems.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } // protected static Set findAllByApproximateDisplayName(val)

  // @since   1.2.0
  protected static Set findAllByApproximateExtension(String val) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Stem as ns where ns.stem_extension like :value");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByApproximateExtension");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      stems.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } // protected static Set findAllByApproximateExtension(val)

  // @since   1.2.0
  protected static Set findAllByApproximateName(String val) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Stem as ns where ns.stem_name like :value");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByApproximateName");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      stems.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } // protected static Set findAllByApproximateName(val)

  // @since   1.2.0
  protected static Set findAllByApproximateNameAny(String name) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Stem as ns where "
        + "   lower(ns.stem_name)         like :name "
        + "or lower(ns.display_name)      like :name "
        + "or lower(ns.stem_extension)    like :name "
        + "or lower(ns.display_extension) like :name" 
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByApproximateNameAny");
      qry.setString("name", "%" + name.toLowerCase() + "%");
      stems.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } // protected static Set findAllByApproximateNameAny(name)

  // @since   1.2.0
  protected static Set findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Stem as ns where ns.create_time > :time");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedAfter");
      qry.setLong( "time", d.getTime() );
      stems.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } // protected static Set findAllByCreatedAfter(d)

  // @since   1.2.0
  protected static Set findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Stem as ns where ns.create_time < :time");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedBefore");
      qry.setLong( "time", d.getTime() );
      stems.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } // protected static Set findAllByCreatedBefore(d)

  // @since   1.2.0
  protected static Stem findByName(String name) 
    throws  GrouperDAOException,
            StemNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Stem as ns where ns.stem_name = :name");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByName");
      qry.setString("name", name);
      Stem ns = (Stem) qry.uniqueResult();
      hs.close();
      if (ns == null) {
        throw new StemNotFoundException(); // TODO 20070104 null or ex?
      }
      return ns;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static Stem findByName(name)

  // @since   1.2.0
  protected static Stem findByUuid(String uuid)
    throws  GrouperDAOException,
            StemNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Stem as ns where ns.uuid = :uuid");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      Stem ns = (Stem) qry.uniqueResult();
      hs.close();
      if (ns == null) {
        throw new StemNotFoundException(); // TODO 20070104 null or ex?
      }
      return ns; 
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static Stem findByUuid(uuid)

  // @since   1.2.0
  protected static Set findChildGroups(Stem ns) // TODO 20061219 rename
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Group as g where g.parent_stem = :id");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindChildGroups");
      qry.setString( "id", ns.getId() );
      groups.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } // protected sdtatic Set findChildGroups(ns)

  // @since   1.2.0
  protected static Set findChildStems(Stem ns) // TODO 20601219 rename
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Stem as ns where ns.parent_stem = :id");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindChildStems");
      qry.setString( "id", ns.getId() );
      stems.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } // protected sdtatic Set findChildStems(ns)

  // @since   1.2.0
  protected static void revokePriv(Stem ns, MemberOf mof)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = mof.internal_getDeletes().iterator();
        while (it.hasNext()) {
          hs.delete( it.next() );
        }
        it            = mof.internal_getSaves().iterator();
        while (it.hasNext()) {
          hs.saveOrUpdate( it.next() );
        }
        hs.update(ns);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close(); 
      }
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static void revokePriv(ns, mof)

  // @since   1.2.0
  protected static void renameStemAndChildren(Stem ns, Set children)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = children.iterator();
        while (it.hasNext()) {
          hs.update( it.next() );
        }
        hs.update(ns);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();  
        throw eH;
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static void renameStemAndChildren(ns, children)
  
  // @since   1.2.0
  protected static void revokePriv(Stem ns, Set toDelete)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = toDelete.iterator();
        while (it.hasNext()) {
          hs.delete( it.next() );
        }
        hs.update(ns);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close(); 
      }
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static void revokePriv(ns, toDelete)

} // class HibernateStemDAO extends HibernateDAO

