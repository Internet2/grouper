/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperTierApiAuth.interfaces;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouperTierApiAuth.utils.GrouperAuthzApiUtils;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.AsasApiGroupsMemberInterface;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupLookup;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsMemberSearchParam;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsMemberSearchResult;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiName;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiUser;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.entity.AsasApiEntityLookup;


/**
 *
 */
public class GtaasGroupsMemberInterfaceImpl implements AsasApiGroupsMemberInterface {

  /**
   * 
   */
  public GtaasGroupsMemberInterfaceImpl() {
    GrouperStartup.startup();
  }

  /**
   * @see edu.internet2.middleware.tierApiAuthzServer.interfaces.AsasApiGroupsMemberInterface#search(edu.internet2.middleware.tierApiAuthzServer.interfaces.entity.AsasApiEntityLookup, edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsMemberSearchParam)
   */
  public AsasApiGroupsMemberSearchResult search(AsasApiEntityLookup authenticatedSubject,
      AsasApiGroupsMemberSearchParam asasApiGroupsMemberSearchParam) {
    if (asasApiGroupsMemberSearchParam == null) {
      throw new NullPointerException();
    }
    
    Subject loggedInSubject = GrouperAuthzApiUtils.loggedInSubject(authenticatedSubject);
    
    //start a session
    GrouperSession grouperSession = GrouperSession.start(loggedInSubject);
    
    try {
    
      AsasApiGroupsMemberSearchResult result = new AsasApiGroupsMemberSearchResult();
      
      AsasApiGroupLookup asasApiGroupLookup = asasApiGroupsMemberSearchParam.getAsasApiGroupLookup();
      AsasApiEntityLookup asasApiEntityLookup = asasApiGroupsMemberSearchParam.getAsasApiEntityLookup();

      Group group = GrouperAuthzApiUtils.groupLookupConvertToGroup(grouperSession, asasApiGroupLookup, true);
      
      Subject subject = GrouperAuthzApiUtils.entityLookupConvertToSubject(grouperSession, asasApiEntityLookup, true);
      
      boolean hasMember = group.hasMember(subject);
      
      if (hasMember) {
        AsasApiUser asasApiUser = new AsasApiUser();
        asasApiUser.setId(subject.getId());
        AsasApiName asasApiName = new AsasApiName();
        asasApiName.setFormatted(subject.getName());
        asasApiUser.setName(asasApiName);
        result.setAsasApiUser(asasApiUser);
      }

      return result;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
