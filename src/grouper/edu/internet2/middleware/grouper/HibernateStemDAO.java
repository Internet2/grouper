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
 * @version $Id: HibernateStemDAO.java,v 1.11 2007-02-08 16:25:25 blair Exp $
 * @since   1.2.0
 */
class HibernateStemDAO extends HibernateDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateStemDAO.class.getName();


  // PRIVATE INSTANCE VARIABLES //
  private String  createSource;
  private long    createTime;
  private String  creatorUUID;
  private String  description;
  private String  displayExtension;
  private String  displayName;
  private String  extension;
  private String  id;
  private String  name;
  private String  modifierUUID;
  private String  modifySource;
  private long    modifyTime;
  private String  parentUUID;
  private String  uuid;


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static String createChildGroup(StemDTO parent, GroupDTO child, MemberDTO m)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = HibernateDAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      HibernateDAO  dao = Rosetta.getDAO(child);
      try {
        hs.save(dao);
        // TODO 20070207 add group-type tuples
        Iterator it = child.getTypes().iterator();
        while (it.hasNext()) {
          GroupTypeDTO                dto = (GroupTypeDTO) it.next();
          HibernateGroupTypeTupleDAO  gtt = new HibernateGroupTypeTupleDAO();
          gtt.setGroupUuid( child.getUuid() );
          gtt.setTypeUuid( dto.getTypeUuid() );
          hs.save(gtt); // new group-type tuple
        }
        hs.update( Rosetta.getDAO(parent) );
        hs.save( Rosetta.getDAO(m) );
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      }
      return dao.getId();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static String createChildGroup(parent, child, m)


  // @since   1.2.0
  protected static String createChildStem(StemDTO parent, StemDTO child)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = HibernateDAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      HibernateDAO  dao = Rosetta.getDAO(child);
      try {
        hs.save(dao);
        hs.update( Rosetta.getDAO(parent) );
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      }
      return dao.getId();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static String createChildStem(parent, child)

  // @since   1.2.0
  protected static String createRootStem(StemDTO root)
    throws  GrouperDAOException
  {
    try {
      Session           hs  = HibernateDAO.getSession();
      Transaction       tx  = hs.beginTransaction();
      HibernateStemDAO  dao = root.getDAO();
      try {
        hs.save(dao);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      }
      return dao.getId();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static String createRootStem(root)

  // @since   1.2.0
  protected static Set findAllByApproximateDisplayExtension(String val) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where ns.displayExtension like :value");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByApproximateDisplayExtension");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      stems.addAll( StemDTO.getDTO( qry.list() ) );
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
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where ns.displayName like :value");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByApproximateDisplayName");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      stems.addAll( StemDTO.getDTO( qry.list() ) );
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
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where ns.extension like :value");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByApproximateExtension");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      stems.addAll( StemDTO.getDTO( qry.list() ) );
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
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where ns.name like :value");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByApproximateName");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      stems.addAll( StemDTO.getDTO( qry.list() ) );
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
        "from HibernateStemDAO as ns where "
        + "   lower(ns.name)              like :name "
        + "or lower(ns.displayName)       like :name "
        + "or lower(ns.extension)         like :name "
        + "or lower(ns.displayExtension)  like :name" 
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByApproximateNameAny");
      qry.setString("name", "%" + name.toLowerCase() + "%");
      stems.addAll( StemDTO.getDTO( qry.list() ) );
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
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where ns.createTime > :time");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedAfter");
      qry.setLong( "time", d.getTime() );
      stems.addAll( StemDTO.getDTO( qry.list() ) );
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
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where ns.createTime < :time");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedBefore");
      qry.setLong( "time", d.getTime() );
      stems.addAll( StemDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } // protected static Set findAllByCreatedBefore(d)

  // @since   1.2.0
  protected static StemDTO findByName(String name) 
    throws  GrouperDAOException,
            StemNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where ns.name = :name");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByName");
      qry.setString("name", name);
      HibernateStemDAO dao = (HibernateStemDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        throw new StemNotFoundException(); // TODO 20070104 null or ex?
      }
      return StemDTO.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static StemDTO findByName(name)

  // @since   1.2.0
  protected static StemDTO findByUuid(String uuid)
    throws  GrouperDAOException,
            StemNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where ns.uuid = :uuid");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      HibernateStemDAO dao = (HibernateStemDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        throw new StemNotFoundException(); // TODO 20070104 null or ex?
      }
      return StemDTO.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static StemDTO findByUuid(uuid)

  // @since   1.2.0
  protected static Set findChildGroups(Stem ns) // TODO 20061219 rename
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupDAO as g where g.parentUuid = :parent");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindChildGroups");
      qry.setString( "parent", ns.getUuid() );
      groups.addAll( GroupDTO.getDTO( qry.list() ) );
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
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where ns.parentUuid = :parent");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindChildStems");
      qry.setString( "parent", ns.getUuid() );
      stems.addAll( StemDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } // protected sdtatic Set findChildStems(ns)

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
          hs.update( Rosetta.getDAO( it.next() ) );
        }
        hs.update( Rosetta.getDAO(ns) );
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
  protected static void revokePriv(StemDTO ns, MemberOf mof)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = mof.internal_getDeletes().iterator();
        while (it.hasNext()) {
          hs.delete( Rosetta.getDAO( it.next() ) );
        }
        it = mof.internal_getSaves().iterator();
        while (it.hasNext()) {
          hs.saveOrUpdate( it.next() );
        }
        hs.update( ns.getDAO() );
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
  protected static void revokePriv(StemDTO ns, Set toDelete)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = toDelete.iterator();
        while (it.hasNext()) {
          hs.delete( Rosetta.getDAO( it.next() ) );
        }
        hs.update( Rosetta.getDAO(ns) );
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


  // GETTERS //

  protected String getCreateSource() {
    return this.createSource;
  }
  protected long getCreateTime() {
    return this.createTime;
  }
  protected String getCreatorUuid() {
    return this.creatorUUID;
  }
  protected String getDescription() {
    return this.description;
  }
  protected String getDisplayExtension() {
    return this.displayExtension;
  }
  protected String getDisplayName() {
    return this.displayName;
  }
  protected String getExtension() {
    return this.extension;
  }
  protected String getId() {
    return this.id;
  }
  protected String getModifierUuid() {
    return this.modifierUUID;
  }
  protected String getModifySource() {
    return this.modifySource;
  }
  protected long getModifyTime() {
    return this.modifyTime;
  }
  protected String getName() {
    return this.name;
  }
  protected String getParentUuid() {
    return this.parentUUID;
  }
  protected String getUuid() {
    return this.uuid;
  }


  // SETTERS //

  protected void setCreateSource(String createSource) {
    this.createSource = createSource;
  }
  protected void setCreateTime(long createTime) {
    this.createTime = createTime;
  }
  protected void setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
  }
  protected void setDescription(String description) {
    this.description = description;
  }
  protected void setDisplayExtension(String displayExtension) {
    this.displayExtension = displayExtension;
  }
  protected void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
  protected void setExtension(String extension) {
    this.extension = extension;
  }
  protected void setId(String id) {
    this.id = id;
  }
  protected void setModifierUuid(String modifierUUID) {
    this.modifierUUID = modifierUUID;
  }
  protected void setModifySource(String modifySource) {
    this.modifySource = modifySource;
  }
  protected void setModifyTime(long modifyTime) {
    this.modifyTime = modifyTime;
  }
  protected void setName(String name) {
    this.name = name;
  }
  protected void setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
  }
  protected void setUuid(String uuid) {
    this.uuid = uuid;
  }

} // class HibernateStemDAO extends HibernateDAO 

