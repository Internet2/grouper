/*
 * @author mchyzer $Id: GrouperWsLitePutGroup.java,v 1.1 2008-03-24 20:19:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite.group;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.lite.GrouperLiteInvalidRequest;
import edu.internet2.middleware.grouper.ws.lite.GrouperServiceLite;
import edu.internet2.middleware.grouper.ws.lite.WsRequestBean;
import edu.internet2.middleware.grouper.ws.lite.WsResponseBean;

/**
 * all first level resources on a get request
 */
public enum GrouperWsLitePutGroup {

  /** group get requests for members */
  members {

    /**
     * handle the incoming request based on HTTP method PUT and group as resource, and members as subresource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesLite/xhtml/xhtml/v3_0_000/group/a:b/members
     * the urlStrings would be size three: {"group", "a:b", "members"}
     * @param groupName in url
     * @param requestObject is the request body converted to object
     * @return the return object
     */
    @Override
    public WsResponseBean service(
        GrouperWsVersion clientVersion, String groupName, List<String> urlStrings,
        WsRequestBean requestObject) {

      //maybe putting all members (url strings size 3)
      //url should be: /v1_3_000/group/aStem:aGroup/members
      if (urlStrings.size() == 3 && (!(requestObject instanceof WsLiteAddMemberSimpleRequest))) {
        
        WsLiteAddMemberRequest wsLiteAddMembersRequest = GrouperUtil.typeCast(
            requestObject, WsLiteAddMemberRequest.class);
        
        return GrouperServiceLite.addMember(clientVersion, groupName, wsLiteAddMembersRequest);
        
      }
      
      //make sure right type
      WsLiteAddMemberSimpleRequest wsLiteAddMemberSimpleRequest = GrouperUtil.typeCast(
          requestObject, WsLiteAddMemberSimpleRequest.class);
      
      //url should be: /v1_3_000/group/aStem:aGroup/members/123412345
      //TODO make this generic
      String subjectId = null;
      String sourceId = null;
      if (urlStrings.size() == 4) {
        subjectId = urlStrings.get(3);
      } else {
        //url should be: /v1_3_000/group/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
        if (urlStrings.size() == 7) {
          subjectId = urlStrings.get(6);
          sourceId = urlStrings.get(4);
        }
      }
      
      return GrouperServiceLite.addMemberSimple(clientVersion, groupName, subjectId, sourceId,
          wsLiteAddMemberSimpleRequest);

    }

  };

  /**
     * handle the incoming request based on HTTP method GET and group as resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesLite/xhtml/v3_0_000/group/a:b/members
     * the urlStrings would be size three: {"group", "a:b", "members"}
     * @param groupName in url
     * @param requestObject is the request body converted to object
     * @return the return object
   */
  public abstract WsResponseBean service(
      GrouperWsVersion clientVersion, String groupName, List<String> urlStrings,
      WsRequestBean requestObject);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true if exception should be thrown on not found
   * @return the enum or null or exception if not found
   * @throws GrouperLiteInvalidRequest if there is a problem
   */
  public static GrouperWsLitePutGroup valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperLiteInvalidRequest {
    if (!exceptionOnNotFound && StringUtils.isBlank(string)) {
      return null;
    }
    for (GrouperWsLitePutGroup grouperWsLiteGetGroup : GrouperWsLitePutGroup.values()) {
      if (StringUtils.equalsIgnoreCase(string, grouperWsLiteGetGroup.name())) {
        return grouperWsLiteGetGroup;
      }
    }
    StringBuilder error = new StringBuilder(
        "Cant find GrouperWsLiteGetGroup from string: '").append(string);
    error.append("', expecting one of: ");
    for (GrouperWsLitePutGroup grouperWsLiteGetGroup : GrouperWsLitePutGroup.values()) {
      error.append(grouperWsLiteGetGroup.name()).append(", ");
    }
    throw new GrouperLiteInvalidRequest(error.toString());
  }

}
