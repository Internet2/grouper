/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperTierApiAuth.interfaces;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.subj.GrouperSubject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperTierApiAuth.utils.GrouperAuthzApiUtils;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.tierApiAuthzServer.config.TaasWsClientConfig;
import edu.internet2.middleware.tierApiAuthzServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.AsasApiGroupsMemberInterface;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroup;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupLookup;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsMemberSearchParam;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsMemberSearchResult;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiName;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiUser;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.entity.AsasApiEntityLookup;
import edu.internet2.middleware.tierApiAuthzServer.logging.TaasRequestLog;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.lang.StringUtils;


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

    TaasRequestLog.logToRequestLog("operation", "groupHasMemberGet");

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

      Group group = GrouperAuthzApiUtils.groupLookupConvertToGroup(grouperSession, asasApiGroupLookup, false);
      
      if (group == null) {
        throw new AsasRestInvalidRequest("404", "ERROR_GROUP_NOT_FOUND");
      }
      
      TaasRequestLog.logToRequestLog("foundGroup", group.getName());
      
      Subject subject = GrouperAuthzApiUtils.entityLookupConvertToSubject(grouperSession, asasApiEntityLookup, false);

      if (subject == null) {
        throw new AsasRestInvalidRequest("404", "ERROR_SUBJECT_NOT_FOUND");
      }
      
      TaasRequestLog.logToRequestLog("foundSubject", GrouperUtil.subjectToString(subject));

      boolean hasMember = false;

      TaasRequestLog.logToRequestLog("membershipType", asasApiGroupsMemberSearchParam.getMembershipType());

      switch (asasApiGroupsMemberSearchParam.getMembershipType()) {
        case all:
          
          hasMember = group.hasMember(subject);
          break;
          
        case direct:
          
          hasMember = group.hasImmediateMember(subject);
          break;
          
        case indirect:
          
          hasMember = group.hasNonImmediateMember(subject);
          break;
          
        default: 
          throw new RuntimeException("Not expecting " + asasApiGroupsMemberSearchParam.getMembershipType());
      }
      

      TaasRequestLog.logToRequestLog("hasMember", hasMember);

      if (hasMember) {
        if (StringUtils.equals(subject.getSourceId(), "g:gsa" ) && subject instanceof GrouperSubject) {
          Group subjectGroup = ((GrouperSubject)subject).internal_getGroup();
          AsasApiGroup asasApiGroup = new AsasApiGroup();
          asasApiGroup.setId(subjectGroup.getId());
          if (!StringUtils.isBlank(subjectGroup.getDescription())) {
            asasApiGroup.setDescription(subjectGroup.getDescription());
          }
          asasApiGroup.setDisplayName(subjectGroup.getDisplayName());
          asasApiGroup.setName(subjectGroup.getName());
          result.setAsasApiGroup(asasApiGroup);
        } else {
          AsasApiUser asasApiUser = new AsasApiUser();
          asasApiUser.setId(subject.getId());
          if (TaasWsClientConfig.retrieveClientConfigForLoggedInUser().propertyValueBoolean(true, 
              "tierClient.getGroupMember.showName", "tierClient.generic.showName")) {
            AsasApiName asasApiName = new AsasApiName();
            asasApiName.setFormatted(subject.getName());
            asasApiUser.setName(asasApiName);
          }
          result.setAsasApiUser(asasApiUser);
        }
      }

      return result;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
