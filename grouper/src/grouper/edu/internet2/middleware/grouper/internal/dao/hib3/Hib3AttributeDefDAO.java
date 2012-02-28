package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
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
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
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
    return findByIdSecure(id, exceptionIfNotFound, null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByIdSecure(String, boolean, QueryOptions)
   */
  public AttributeDef findByIdSecure(String id, boolean exceptionIfNotFound, QueryOptions queryOptions) {
    AttributeDef attributeDef = findById(id, exceptionIfNotFound, queryOptions);

    //make sure grouper session can view the attribute def
    attributeDef = filterSecurity(attributeDef);
    
    if (attributeDef == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Not allowed to find AttributeDef by id: " + id);
    }
    
    return attributeDef;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findById(java.lang.String, boolean)
   */
  public AttributeDef findById(String id, boolean exceptionIfNotFound) {
    return findById(id, exceptionIfNotFound, null);
  }
  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findById(java.lang.String, boolean, QueryOptions)
   */
  public AttributeDef findById(String id, boolean exceptionIfNotFound, QueryOptions queryOptions) {
    AttributeDef attributeDef = HibernateSession.byHqlStatic()
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindById")
      .createQuery(
        "from AttributeDef where id = :theId")
      .setString("theId", id)
      .options(queryOptions).uniqueResult(AttributeDef.class);

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
    return findByNameSecure(name, exceptionIfNotFound, null);
  }
 
  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByNameSecure(java.lang.String, boolean)
   */
  public AttributeDef findByNameSecure(String name, boolean exceptionIfNotFound, QueryOptions queryOptions)
      throws GrouperDAOException, AttributeDefNotFoundException {
    AttributeDef attributeDef = HibernateSession.byHqlStatic()
      .createQuery("select a from AttributeDef as a where a.nameDb = :value")
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByName")
      .options(queryOptions)
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
      .setString("theAttributeDefNameId", attributeDefNameId)
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByAttributeDefNameIdSecure")
      .uniqueResult(AttributeDef.class);
    
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
        
        List<Membership> memberships = GrouperDAOFactory.getFactory().getMembership().findAllByAttrDefOwnerAsList(attributeDef.getId(), false);
        
        
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").delete(memberships);
        
        Set<AttributeAssignAction> attributeAssignActions = GrouperDAOFactory.getFactory().getAttributeAssignAction().findByAttributeDefId(attributeDef.getId());
        
        for (AttributeAssignAction attributeAssignAction : attributeAssignActions) {
          attributeAssignAction.delete();
        }
        
        GrouperDAOFactory.getFactory().getGroupSet().deleteSelfByOwnerAttrDef(attributeDef.getId());

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
    return getAllAttributeDefsSecureHelper(scope, grouperSession, subject, privileges, queryOptions, false, null, null);
  }

  /**
   * @param scope 
   * @param grouperSession 
   * @param subject 
   * @param privileges 
   * @param queryOptions 
   * @param splitScope 
   * @param attributeAssignType
   * @param attributeDefType
   * @return  attribute defs
   * 
   */
  private Set<AttributeDef> getAllAttributeDefsSecureHelper(String scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> privileges,
      QueryOptions queryOptions, boolean splitScope, AttributeAssignType attributeAssignType,
      AttributeDefType attributeDefType) {
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theAttributeDef.nameDb");
    }

    StringBuilder sql = new StringBuilder("select distinct theAttributeDef from AttributeDef theAttributeDef ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder whereClause = new StringBuilder();
    
    if (attributeDefType != null) {
      if (whereClause.length() > 0) {
        whereClause.append(" and ");
      }
      whereClause.append(" theAttributeDef.attributeDefTypeDb = :theAttributeDefType ");
      byHqlStatic.setString("theAttributeDefType", attributeDefType.name());
    }
    
    if (attributeAssignType != null) {
      if (whereClause.length() > 0) {
        whereClause.append(" and ");
      }
      switch (attributeAssignType) {
        case any_mem:
          whereClause.append(" theAttributeDef.assignToEffMembershipDb = 'T' ");
          break;
        case any_mem_asgn:
          whereClause.append(" theAttributeDef.assignToEffMembershipAssnDb = 'T' ");
          break;
        case attr_def:
          whereClause.append(" theAttributeDef.assignToAttributeDefDb = 'T' ");
          break;
        case attr_def_asgn:
          whereClause.append(" theAttributeDef.assignToAttributeDefAssnDb = 'T' ");
          break;
        case group:
          whereClause.append(" theAttributeDef.assignToGroupDb = 'T' ");
          break;
        case group_asgn:
          whereClause.append(" theAttributeDef.assignToGroupAssnDb = 'T' ");
          break;
        case imm_mem:
          whereClause.append(" theAttributeDef.assignToImmMembershipDb = 'T' ");
          break;
        case imm_mem_asgn:
          whereClause.append(" theAttributeDef.assignToImmMembershipAssnDb = 'T' ");
          break;
        case member:
          whereClause.append(" theAttributeDef.assignToMemberDb = 'T' ");
          break;
        case mem_asgn:
          whereClause.append(" theAttributeDef.assignToMemberAssnDb = 'T' ");
          break;
        case stem:
          whereClause.append(" theAttributeDef.assignToStemDb = 'T' ");
          break;
        case stem_asgn:
          whereClause.append(" theAttributeDef.assignToStemAssnDb = 'T' ");
          break;
        default:
          throw new RuntimeException("Not expecting attribute assign type: " + attributeAssignType);
      }
    }
    
    //see if there is a scope
    if (!StringUtils.isBlank(scope)) {
      
      String[] scopes = splitScope ? GrouperUtil.splitTrim(scope, " ") : new String[]{scope};
      int index = 0;
      boolean firstScope = true;
      for (String theScope : scopes) {
        if (whereClause.length() > 0) {
          whereClause.append(" and ");
        } 
        if (firstScope) {
          whereClause.append(" (( ");
        }
        firstScope = false;
        whereClause.append(" lower(theAttributeDef.nameDb) like :scope" + index + " ");
        if (splitScope) {
          theScope = "%" + theScope + "%";
        } else if (!theScope.endsWith("%")) {
          theScope += "%";
        }
        byHqlStatic.setString("scope" + index, theScope.toLowerCase());
        index++;
      }

      whereClause.append(" ) or ( theAttributeDef.id = :attributeId  )) ");
      byHqlStatic.setString("attributeId", scope);
    }

    //see if we are adding more to the query
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(subject, byHqlStatic,
        sql, whereClause, "theAttributeDef.id", privileges);

    if (whereClause.length() > 0) {
      sql.append(" where ").append(whereClause);
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
  
  /**
   * @see AttributeDefDAO#findAttributeDefsInStemWithoutPrivilege(GrouperSession, String, Scope, Subject, Privilege, QueryOptions, boolean, String)
   */
  public Set<AttributeDef> findAttributeDefsInStemWithoutPrivilege(GrouperSession grouperSession,
      String stemId, Scope scope, Subject subject, Privilege privilege, QueryOptions queryOptions, boolean considerAllSubject, 
      String sqlLikeString) {
    
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theAttributeDef.nameDb");
    }

    StringBuilder sqlTables = new StringBuilder("select distinct theAttributeDef from AttributeDef theAttributeDef ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder sqlWhereClause = new StringBuilder();

    switch (scope) {
      case ONE:
        
        sqlWhereClause.append(" theAttributeDef.stemId = :stemId ");
        byHqlStatic.setString("stemId", stemId);
        
        break;
        
      case SUB:
        
        Stem stem = StemFinder.findByUuid(grouperSession, stemId, true);
        sqlWhereClause.append(" theAttributeDef.nameDb like :stemPattern ");
        byHqlStatic.setString("stemPattern", stem.getName() + ":%");

        break;
        
      default:
        throw new RuntimeException("Need to pass in a scope, or its not implemented: " + scope);
    }
    

    
    //see if we are adding more to the query, note, this is for the ADMIN list since the user should be able to read privs
    Set<Privilege> adminSet = GrouperUtil.toSet(AttributeDefPrivilege.ATTR_ADMIN);

    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        sqlTables, sqlWhereClause, "theAttributeDef.id", adminSet);

    if (!StringUtils.isBlank(sqlLikeString)) {
      if (sqlWhereClause.length() > 0) {
        sqlWhereClause.append(" and ");
      }
      sqlWhereClause.append(" theAttributeDef.nameDb like :sqlLikeString ");
      byHqlStatic.setString("sqlLikeString", sqlLikeString);
    }
    

    StringBuilder sql = sqlTables.append(" where ").append(sqlWhereClause);

    boolean changedQueryNotWithPriv = grouperSession.getAttributeDefResolver().hqlFilterAttributeDefsNotWithPrivWhereClause(subject, byHqlStatic, 
        sql, "theAttributeDef.id", privilege, considerAllSubject);

    Set<AttributeDef> attributeDefs = byHqlStatic.createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAttributeDefsInStemWithoutPrivilege")
      .options(queryOptions)
      .listSet(AttributeDef.class);
          
    //if the hql didnt filter, this will
    Set<AttributeDef> filteredAttributeDefs = grouperSession.getAttributeDefResolver()
      .postHqlFilterAttrDefs(attributeDefs, grouperSession.getSubject(), adminSet);

    if (!changedQueryNotWithPriv) {
      
      //didnt do this in the query
      Set<AttributeDef> originalList = new LinkedHashSet<AttributeDef>(filteredAttributeDefs);
      filteredAttributeDefs = grouperSession.getAttributeDefResolver()
        .postHqlFilterAttrDefs(originalList, subject, GrouperUtil.toSet(privilege));
      
      //we want the ones in the original list not in the new list
      if (filteredAttributeDefs != null) {
        originalList.removeAll(filteredAttributeDefs);
      }
      filteredAttributeDefs = originalList;
    }
    
    return filteredAttributeDefs;
    
  }

  /**
   * @see AttributeDefDAO#getAllAttributeDefsSplitScopeSecure(String, GrouperSession, Subject, Set, QueryOptions, AttributeAssignType, AttributeDefType)
   * @Override
   */
  public Set<AttributeDef> getAllAttributeDefsSplitScopeSecure(String scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> privileges,
      QueryOptions queryOptions, AttributeAssignType attributeAssignType,
      AttributeDefType attributeDefType) {
    return getAllAttributeDefsSecureHelper(scope, grouperSession, subject, privileges, queryOptions, true, attributeAssignType, attributeDefType);
  }


} 

