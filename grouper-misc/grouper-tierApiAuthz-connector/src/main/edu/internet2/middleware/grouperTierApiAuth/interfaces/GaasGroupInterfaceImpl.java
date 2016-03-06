/**
 * 
 */
package edu.internet2.middleware.grouperTierApiAuth.interfaces;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.tierApiAuthzServer.interfaces.AsasApiGroupInterface;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.AsasApiQueryParams;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroup;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsSearchParam;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsSearchResult;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.entity.AsasApiEntityLookup;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouperTierApiAuth.utils.GrouperAuthzApiUtils;
import edu.internet2.middleware.subject.Subject;


/**
 * Implement the group interface
 * @author mchyzer
 *
 */
public class GaasGroupInterfaceImpl implements AsasApiGroupInterface {

  /**
   * @see edu.internet2.middleware.tierApiAuthzServer.interfaces.AsasApiGroupInterface#search(edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsSearchParam)
   */
  @Override
  public AsasApiGroupsSearchResult search(AsasApiEntityLookup authenticatedSubject,
      AsasApiGroupsSearchParam asasApiGroupsSearchParam) {
    
    if (asasApiGroupsSearchParam == null) {
      throw new NullPointerException();
    }
    
    Subject loggedInSubject = GrouperAuthzApiUtils.loggedInSubject(authenticatedSubject);
    
    //start a session
    GrouperSession grouperSession = GrouperSession.start(loggedInSubject);
    
    try {
    
      AsasApiGroupsSearchResult result = new AsasApiGroupsSearchResult();
      
      AsasApiQueryParams queryParams = asasApiGroupsSearchParam.getQueryParams();
      QueryOptions queryOptions = GrouperAuthzApiUtils.convertToQueryOptions(queryParams);
      
      // do a search...
      //Set<Group> grouperGroups = GrouperDAOFactory.getFactory().getGroup().findAllByApproximateNameSecure(
      //    "%", null, queryOptions, TypeOfGroup.GROUP_OR_ROLE_SET);
      Set<Group> grouperGroups = findAllByApproximateNameSecureHelper(
          "%", null, queryOptions);
      
      // convert the groups
      List<AsasApiGroup> asasApiGroups = GrouperAuthzApiUtils.convertToGroups(grouperGroups);
      result.setGroups(asasApiGroups);
      
      queryParams = GrouperAuthzApiUtils.convertToQueryParams(queryOptions);
      result.setQueryParams(queryParams);
      
      return result;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Helper for find by approximate name queries
   * @param name
   * @param scope
   * @param currentNames
   * @param alternateNames
   * @param queryOptions 
   * @param typeOfGroups
   * @return set
   * @throws GrouperDAOException
   * @throws IllegalStateException
   */
  private Set<Group> findAllByApproximateNameSecureHelper(final String name, final String scope,
      final QueryOptions queryOptions) throws GrouperDAOException {
    @SuppressWarnings("unchecked")
    Set<Group> resultGroups = (Set<Group>)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {
  
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            StringBuilder hql = new StringBuilder("select distinct theGroup from Group theGroup ");
      
            ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
          
            GrouperSession grouperSession = GrouperSession.staticGrouperSession();
            
            //see if we are adding more to the query
            boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
                grouperSession.getSubject(), byHqlStatic, 
                hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);
          
            if (!changedQuery) {
              hql.append(" where ");
            } else {
              hql.append(" and ");
            }
            String lowerName = StringUtils.defaultString(name).toLowerCase();
            hql.append(" ( ");
            hql.append(" lower(theGroup.nameDb) like :theName or lower(theGroup.displayNameDb) like :theDisplayName ");
            byHqlStatic.setString("theName", "%" + lowerName + "%");
            byHqlStatic.setString("theDisplayName", "%" + lowerName + "%");
            
            hql.append(" ) ");
            
            if (scope != null) {
              hql.append(" and theGroup.nameDb like :theStemScope ");
              byHqlStatic.setString("theStemScope", scope + "%");
            }

            //reset sorting
            if (queryOptions != null) {
              
              byHqlStatic.options(queryOptions);
            }
            
            byHqlStatic.createQuery(hql.toString());
            Set<Group> groups = byHqlStatic.listSet(Group.class);
            
            return groups;
          }
    });
    return resultGroups;
  }

  
}
