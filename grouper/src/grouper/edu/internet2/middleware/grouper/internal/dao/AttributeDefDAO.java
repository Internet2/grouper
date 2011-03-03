/*
 * @author mchyzer
 * $Id: AttributeDefDAO.java,v 1.6 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;

/**
 * attribute def data access methods
 */
public interface AttributeDefDAO extends GrouperDAO {
  
  /** 
   * insert or update an attribute def object 
   * @param attributeDef 
   */
  public void saveOrUpdate(AttributeDef attributeDef);
  
  /**
   * find by id.  This is a secure method, a grouperSession needs to be open
   * @param id
   * @param exceptionIfNotFound
   * @return the attribute def or null if not there
   */
  public AttributeDef findByIdSecure(String id, boolean exceptionIfNotFound);
  
  /**
   * find by id.  This is NOT a secure method, a grouperSession does not need to be open
   * @param id
   * @param exceptionIfNotFound
   * @return the attribute def or null if not there
   */
  public AttributeDef findById(String id, boolean exceptionIfNotFound);
  
  /**
   * find by attributeDefNameId.  This is a secure method, a grouperSession needs to be open
   * @param attributeDefNameId
   * @param exceptionIfNotFound
   * @return the attribute def or null if not there
   */
  public AttributeDef findByAttributeDefNameIdSecure(String attributeDefNameId, boolean exceptionIfNotFound);
  
  /**
   * find an attribute def by name.  this is a secure method, a grouperSession needs to be open
   * @param name 
   * @param exceptionIfNotFound 
   * @return attribute def
   * @throws GrouperDAOException 
   * @throws AttributeDefNotFoundException 
   */
  public AttributeDef findByNameSecure(String name, boolean exceptionIfNotFound) 
    throws GrouperDAOException, AttributeDefNotFoundException;
  
  /**
   * Find all that have the given stem id.
   * @param id
   * @return set of stems
   */
  public Set<AttributeDef> findByStem(String id);
  
  /**
   * delete the attribute def
   * @param attributeDef
   */
  public void delete(AttributeDef attributeDef);
  
  /**
   * search for an attribute def by id or name
   * @param id
   * @param name
   * @param exceptionIfNotFound
   * @return the attribute def or null
   */
  public AttributeDef findByUuidOrName(String id, String name, boolean exceptionIfNotFound);

  /**
   * save the update properties which are auto saved when business method is called
   * @param attributeDef
   */
  public void saveUpdateProperties(AttributeDef attributeDef);

  /**
   * get all attribute defs secure
   * @param grouperSession
   * @param subject
   * @param privileges
   * @param queryOptions
   * @return attribute defs
   */
  public Set<AttributeDef> getAllAttributeDefsSecure(GrouperSession grouperSession, 
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions);
  
  /**
   * get all attribute defs secure
   * @param scope
   * @param grouperSession
   * @param subject
   * @param privileges
   * @param queryOptions
   * @return set of attribute defs
   */
  public Set<AttributeDef> getAllAttributeDefsSecure(String scope, GrouperSession grouperSession, 
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions);
  
  /**
   * get all attribute defs secure, split the scope by whitespace
   * @param scope
   * @param grouperSession
   * @param subject
   * @param privileges
   * @param queryOptions
   * @return set of attribute defs
   */
  public Set<AttributeDef> getAllAttributeDefsSplitScopeSecure(String scope, GrouperSession grouperSession, 
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions);
  
  /**
   * see which attributeDefs do not have this privilege
   * @param grouperSession
   * @param stemId
   * @param scope
   * @param subject
   * @param privilege
   * @param queryOptions
   * @param considerAllSubject
   * @param sqlLikeString
   * @return the attributeDefs
   */
  public Set<AttributeDef> findAttributeDefsInStemWithoutPrivilege(GrouperSession grouperSession,
      String stemId, Scope scope, Subject subject, Privilege privilege, QueryOptions queryOptions, boolean considerAllSubject, 
      String sqlLikeString);


}
