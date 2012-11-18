/**
 * 
 */
package edu.internet2.middleware.grouperStandardApi.interfaces;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.authzStandardApiServer.interfaces.AsasApiGroupInterface;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiGroup;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiGroupsSearchParam;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiGroupsSearchResult;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiQueryParams;
import edu.internet2.middleware.authzStandardApiServer.interfaces.entity.AsasApiEntityLookup;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouperStandardApi.utils.GrouperAuthzApiUtils;
import edu.internet2.middleware.subject.Subject;


/**
 * Implement the group interface
 * @author mchyzer
 *
 */
public class GaasGroupInterfaceImpl implements AsasApiGroupInterface {

  /**
   * @see edu.internet2.middleware.authzStandardApiServer.interfaces.AsasApiGroupInterface#search(edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiGroupsSearchParam)
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
      Set<Group> grouperGroups = GrouperDAOFactory.getFactory().getGroup().findAllByApproximateNameSecure(
          "%", null, queryOptions, TypeOfGroup.GROUP_OR_ROLE_SET);
      
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

}
