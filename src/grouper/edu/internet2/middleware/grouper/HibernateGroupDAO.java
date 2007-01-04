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
 * Stub Hibernate {@link Group} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateGroupDAO.java,v 1.8 2007-01-04 19:24:09 blair Exp $
 * @since   1.2.0
 */
class HibernateGroupDAO extends HibernateDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateGroupDAO.class.getName();


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Set findAllByAnyApproximateAttr(String val) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Attribute as a where lower(a.value) like :value");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByAnyApproximateAttr");
      qry.setString( "value", "%" + val.toLowerCase() + "%" );
      Iterator it = qry.iterate();
      while (it.hasNext()) {
        groups.add( ( (Attribute) it.next() ).getGroup() );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } // protected static Set findAllByAnyApproximateAttr(val)

  // @since   1.2.0
  protected static Set findAllByApproximateAttr(String attr, String val) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Attribute as a where a.field.name = :field and lower(a.value) like :value"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByApproximateAttr");
      qry.setString("field", attr);
      qry.setString( "value", "%" + val.toLowerCase() + "%" );
      Iterator it = qry.iterate();
      while (it.hasNext()) {
        groups.add( ( (Attribute) it.next() ).getGroup() );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } // protected static Set findAllByApproximateAttr(attr, val)

  // @since   1.2.0
  protected static Set findAllByApproximateName(String name) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Attribute as a where "
        + "   (a.field.name = 'name'              and lower(a.value) like :value) "
        + "or (a.field.name = 'displayName'       and lower(a.value) like :value) "
        + "or (a.field.name = 'extension'         and lower(a.value) like :value) "
        + "or (a.field.name = 'displayExtension'  and lower(a.value) like :value) "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByApproximateName");
      qry.setString( "value", "%" + name.toLowerCase() + "%" );
      Iterator it = qry.iterate();
      while (it.hasNext()) {
        groups.add( ( (Attribute) it.next() ).getGroup() );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } // protected static Set internal_findAllByApproximateName(name)

  // @since   1.2.0
  protected static Set findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Group as g where g.create_time > :time");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedAfter");
      qry.setLong( "time", d.getTime() );
      groups.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } // protected static Set findAllByCreatedAfter(d)

  // @since   1.2.0
  protected static Set findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Group as g where g.create_time < :time");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedBefore");
      qry.setLong( "time", d.getTime() );
      groups.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } // protected static Set findAllByCreatedBefore(d)

  // @since   1.2.0
  protected static Set findAllByModifiedAfter(Date d) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Group as g where g.modify_time > :time");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByModifiedAfter");
      qry.setLong( "time", d.getTime() );
      groups.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } // protected static Set findAllByModifiedAfter(d)

  // @since   1.2.0
  protected static Set findAllByModifiedBefore(Date d) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Group as g where g.modify_time < :time");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByModifiedBefore");
      qry.setLong( "time", d.getTime() );
      groups.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } // protected static Set findAllByModifiedBefore(d)

  // TODO 20061127 can i use a variant of this query in `GroupType.delete()`?
  // @since   1.2.0
  protected static Set findAllByType(GroupType type) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Group as g where :type in elements(g.group_types)");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByType");
      qry.setParameter("type", type);
      groups.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } // protected static Set findAllByType(s, type)

  // @return  {@link Group} or <code>null</code>
  // @since   1.2.0
  protected static Group findByAttribute(String attr, String val) 
    throws  GrouperDAOException
  {
    Group g = null;
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Attribute as a where a.field.name = :field and a.value like :value");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByAttribute");
      qry.setString("field", attr);
      qry.setString("value", val);
      Attribute a = (Attribute) qry.uniqueResult();
      hs.close();
      if (a != null) {
        g = a.getGroup();
      }
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return g;
  } // protected static Group findByAttribute(attr, val)

  // @return  {@link Group} or <code>null</code>
  // @since   1.2.0
  protected static Group findByName(String name) 
    throws  GrouperDAOException
  {
    Group g = null;
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Attribute as a where a.field.name = 'name' and a.value = :value");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByName");
      qry.setString("value", name);
      Attribute a = (Attribute) qry.uniqueResult();
      hs.close();
      if (a != null) {
        g = a.getGroup();
      }
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return g;
  } // protected static Group findByName(name)

  // @return  {@link Group} or <code>null</code>
  // @since   1.2.0
  protected static Group findByUuid(String uuid) 
    throws  GrouperDAOException
  {
    Group g = null;
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Group as g where g.uuid = :uuid");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      g = (Group) qry.uniqueResult();
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return g; 
  } // private static Group _findByUuid(uuid)

  // @since   1.2.0
  protected static void revokePriv(Group g, MemberOf mof)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = mof.getDeletes().iterator();
        while (it.hasNext()) {
          hs.delete( it.next() );
        }
        it = mof.getSaves().iterator();
        while (it.hasNext()) {
          hs.saveOrUpdate( it.next() );
        }
        hs.update(g);
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
  } // protected static void revokePriv(g, mof)

  // @since   1.2.0
  protected static void revokePriv(Group g, Set toDelete)
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
        hs.update(g);
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
  } // protected static void revokePriv(g, toDelete)

  // @since   1.2.0
  protected static void updateMemberships(MemberOf mof)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = mof.getDeletes().iterator();
        while (it.hasNext()) {
          hs.delete( it.next() );
        }
        it = mof.getSaves().iterator();
        while (it.hasNext()) {
          hs.saveOrUpdate( it.next() );
        }
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
  } // protected static void updateMemberships(mof)

} // class HibernateGroupDAO extends HibernateDAO

