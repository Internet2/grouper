package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * Data Access Object for attribute def
 * @author  mchyzer
 * @version $Id: Hib3AttributeDefDAO.java,v 1.6 2009-10-26 02:26:07 mchyzer Exp $
 */
public class Hib3AttributeDefDAO extends Hib3DAO implements AttributeDefDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeDefDAO.class.getName();

  /**
   * reset the attribute defs
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeDef").executeUpdate();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByIdSecure(java.lang.String, boolean)
   */
  public AttributeDef findByIdSecure(String id, boolean exceptionIfNotFound) {
    AttributeDef attributeDef = HibernateSession.byHqlStatic().createQuery(
        "from AttributeDef where id = :theId")
      .setString("theId", id).uniqueResult(AttributeDef.class);

    //make sure grouper session can view the attribute def
    attributeDef = filterSecurity(attributeDef);
    
    if (attributeDef == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Cant find (or not allowed to find) AttributeDef by id: " + id);
    }
    
    return attributeDef;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findById(java.lang.String, boolean)
   */
  public AttributeDef findById(String id, boolean exceptionIfNotFound) {
    AttributeDef attributeDef = HibernateSession.byHqlStatic()
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindById")
      .createQuery(
        "from AttributeDef where id = :theId")
      .setString("theId", id).uniqueResult(AttributeDef.class);

    if (attributeDef == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Cant find AttributeDef by id: " + id);
    }
    
    return attributeDef;
  }

  /**
   * make sure grouper session can view the attribute def
   * @param attributeDefs
   * @return the set of attribute defs
   */
  static Set<AttributeDef> filterSecurity(Set<AttributeDef> attributeDefs) {
    Set<AttributeDef> result = new LinkedHashSet<AttributeDef>();
    if (attributeDefs != null) {
      for (AttributeDef attributeDef : attributeDefs) {
        attributeDef = filterSecurity(attributeDef);
        if (attributeDef != null) {
          result.add(attributeDef);
        }
      }
    }
    return result;
  }
  
  /**
   * make sure grouper session can view the attribute def
   * @param attributeDef
   * @return the attributeDef or null
   */
  static AttributeDef filterSecurity(AttributeDef attributeDef) {
    if (attributeDef == null) {
      return null;
    }
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    if ( PrivilegeHelper.canAttrView( grouperSession.internal_getRootSession(), attributeDef, grouperSession.getSubject() ) ) {
      return attributeDef;
    }
    return null;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#saveOrUpdate(edu.internet2.middleware.grouper.attr.AttributeDef)
   */
  public void saveOrUpdate(AttributeDef attributeDef) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeDef);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByNameSecure(java.lang.String, boolean)
   */
  public AttributeDef findByNameSecure(String name, boolean exceptionIfNotFound)
      throws GrouperDAOException, AttributeDefNotFoundException {
    AttributeDef attributeDef = HibernateSession.byHqlStatic()
      .createQuery("select a from AttributeDef as a where a.nameDb = :value")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByName")
      .setString("value", name).uniqueResult(AttributeDef.class);

    //make sure grouper session can view the attribute def
    attributeDef = filterSecurity(attributeDef);
    
    //handle exceptions out of data access method...
    if (attributeDef == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Cannot find (or not allowed to find) attribute def with name: '" + name + "'");
    }
    return attributeDef;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByAttributeDefNameIdSecure(java.lang.String, boolean)
   */
  public AttributeDef findByAttributeDefNameIdSecure(String attributeDefNameId,
      boolean exceptionIfNotFound) {
    AttributeDef attributeDef = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeDef from AttributeDef as theAttributeDef, " +
        "AttributeDefName as theAttributeDefName where theAttributeDefName.id = :theAttributeDefNameId" +
        " and theAttributeDef.id = theAttributeDefName.attributeDefId")
      .setString("theAttributeDefNameId", attributeDefNameId).uniqueResult(AttributeDef.class);
    
    //make sure grouper session can view the attribute def
    attributeDef = filterSecurity(attributeDef);
    
    if (attributeDef == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Cant find (or not allowed to find) AttributeDef " +
      		"by attributeDefNameId: " + attributeDefNameId);
    }
    
    return attributeDef;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByStem(java.lang.String)
   */
  public Set<AttributeDef> findByStem(String id) {
    Set<AttributeDef> attributeDefs = HibernateSession.byHqlStatic()
        .createQuery("from AttributeDef where stemId = :id")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByStem")
        .setString("id", id)
        .listSet(AttributeDef.class);
    
    return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByUuidOrName(java.lang.String, java.lang.String, boolean)
   */
  public AttributeDef findByUuidOrName(String id, String name,
      boolean exceptionIfNotFound) {
    try {
      AttributeDef attributeDef = HibernateSession.byHqlStatic()
        .createQuery("from AttributeDef as theAttributeDef where theAttributeDef.id = :theId or theAttributeDef.nameDb = :theName")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrName")
        .setString("theId", id)
        .setString("theName", name)
        .uniqueResult(AttributeDef.class);
      if (attributeDef == null && exceptionIfNotFound) {
        throw new GroupNotFoundException("Can't find attributeDef by id: '" + id + "' or name '" + name + "'");
      }
      return attributeDef;
    }
    catch (GrouperDAOException e) {
      String error = "Problem finding attributeDef by id: '" 
        + id + "' or name '" + name + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#saveUpdateProperties(edu.internet2.middleware.grouper.attr.AttributeDef)
   */
  public void saveUpdateProperties(AttributeDef attributeDef) {
    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update AttributeDef " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "creatorId = :theCreatorId, " +
        "createdOnDb = :theCreatedOnDb " +
        "where id = :theId")
        .setLong("theHibernateVersionNumber", attributeDef.getHibernateVersionNumber())
        .setString("theCreatorId", attributeDef.getCreatorId())
        .setLong("theCreatedOnDb", attributeDef.getCreatedOnDb())
        .setString("theContextId", attributeDef.getContextId())
        .setString("theId", attributeDef.getId()).executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#delete(edu.internet2.middleware.grouper.attr.AttributeDef)
   */
  public void delete(final AttributeDef attributeDef) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        
        List<Membership> memberships = GrouperDAOFactory.getFactory().getMembership().findAllImmediateByAttrDefOwnerAsList(attributeDef.getId(), false);
        
        
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").delete(memberships);
        
        Set<AttributeAssignAction> attributeAssignActions = GrouperDAOFactory.getFactory().getAttributeAssignAction().findByAttributeDefId(attributeDef.getId());
        
        for (AttributeAssignAction attributeAssignAction : attributeAssignActions) {
          attributeAssignAction.delete();
        }
        
        GrouperDAOFactory.getFactory().getGroupSet().deleteSelfByOwnerAttrDef(attributeDef.getId());
        
        // remove foreign keys in flat table
        GrouperDAOFactory.getFactory().getFlatAttributeDef().removeAttributeDefForeignKey(attributeDef.getId());
        
        hibernateHandlerBean.getHibernateSession().byObject().delete(attributeDef);
        return null;
      }
    });
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#getAllAttributeDefsSecure(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<AttributeDef> getAllAttributeDefsSecure(GrouperSession grouperSession,
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions) {
    return getAllAttributeDefsSecure(null, grouperSession, subject, privileges, queryOptions);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#getAllAttributeDefsSecure(java.lang.String, edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<AttributeDef> getAllAttributeDefsSecure(String scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> privileges,
      QueryOptions queryOptions) {
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theAttributeDef.nameDb");
    }

    StringBuilder sql = new StringBuilder("select distinct theAttributeDef from AttributeDef theAttributeDef ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder whereClause = new StringBuilder();
    
    //see if there is a scope
    if (!StringUtils.isBlank(scope)) {
      whereClause.append(" theStem.nameDb like :scope ");
      byHqlStatic.setString("scope", scope + "%");
    }
    

    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(subject, byHqlStatic,
        sql, whereClause, "theAttributeDef.id", privileges);

    if (whereClause.length() > 0) {
      if (!changedQuery) {
        sql.append(" where ");
      } else {
        sql.append(" and ");
      }
      sql.append(whereClause);
    }    
    
    Set<AttributeDef> attributeDefs = byHqlStatic.createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".GetAllAttributeDefsSecure")
      .options(queryOptions)
      .listSet(AttributeDef.class);
    
    //if the hql didnt filter, this will
    Set<AttributeDef> filteredAttributeDefs = grouperSession.getAttributeDefResolver()
      .postHqlFilterAttrDefs(attributeDefs, subject, privileges);

    return filteredAttributeDefs;

  }

} 

