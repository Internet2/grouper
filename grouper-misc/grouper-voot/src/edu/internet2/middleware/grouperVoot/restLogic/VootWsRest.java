/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer $Id: GrouperWsRestGet.java,v 1.9 2009-12-29 07:39:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouperVoot.restLogic;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouperVoot.VootLogic;
import edu.internet2.middleware.grouperVoot.VootRestHttpMethod;
import edu.internet2.middleware.grouperVoot.beans.VootGroup;

/**
 * all first level resources on a get request
 */
public enum VootWsRest {

  /** group get requests */
  groups {

    /**
     * handle the incoming request based on url
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/voot/groups/a:b
     * the urlStrings would be size two: {"groups", "a:b"}
     * @return the result object
     */
    @Override
    public Object service(List<String> urlStrings, VootRestHttpMethod vootRestHttpMethod) {

      if (vootRestHttpMethod != VootRestHttpMethod.GET) {
        throw new RuntimeException("Not expecting method: " + vootRestHttpMethod.name());
      }
      
      String groupName = GrouperServiceUtils.popUrlString(urlStrings);
      
      if (StringUtils.isBlank(groupName)) {
        
        return VootLogic.getGroups();
        
      }
      
      if (GrouperUtil.length(urlStrings) > 0) {
        throw new RuntimeException("Not expecting more URL strings: " + GrouperUtil.toStringForLog(urlStrings));
      }
      
      if (StringUtils.equals("@me", groupName)) {
        
        return VootLogic.getGroups(GrouperSession.staticGrouperSession().getSubject());
        
      }
      
      throw new RuntimeException("Not implemented: " + groupName + ", " + vootRestHttpMethod + ", " + GrouperUtil.toStringForLog(urlStrings));
    }

  }, 
  
  /** people get requests */
  people {

    /**
     * handle the incoming request based on url
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/voot/people/@me
     * the urlStrings would be size two: {"people", "@me"}
     * @return the result object
     */
    @Override
    public Object service(List<String> urlStrings, VootRestHttpMethod vootRestHttpMethod) {

      if (vootRestHttpMethod != VootRestHttpMethod.GET) {
        throw new RuntimeException("Not expecting method: " + vootRestHttpMethod.name());
      }

      String personId = GrouperServiceUtils.popUrlString(urlStrings);
      
      if (StringUtils.isBlank(personId)) {
        return VootLogic.getSubject(GrouperSession.staticGrouperSession().getSubject());
      }
      
      if (StringUtils.equals("@me", personId)) {
        
        String groupName = GrouperServiceUtils.popUrlString(urlStrings);
        
        if (GrouperUtil.length(urlStrings) > 0) {
          throw new RuntimeException("Not expecting more URL strings: " + GrouperUtil.toStringForLog(urlStrings));
        }

        if (StringUtils.isBlank(groupName)) {
        
          return VootLogic.getSubject(GrouperSession.staticGrouperSession().getSubject());
        }
        
        //get the members of that group
        VootGroup vootGroup = new VootGroup();
        vootGroup.setId(groupName);
        return VootLogic.getMembers(vootGroup);
      }
      
      throw new RuntimeException("Not implemented: " + personId + ", " + vootRestHttpMethod + ", " + GrouperUtil.toStringForLog(urlStrings));
    }

  }, 

  VOOT_BLANK {

    /**
     * 
     */
    @Override
    public Object service(List<String> urlStrings, VootRestHttpMethod vootRestHttpMethod) {
      if (vootRestHttpMethod != VootRestHttpMethod.GET) {
        throw new RuntimeException("Not expecting method: " + vootRestHttpMethod.name());
      }
      throw new RuntimeException("Pass in a url string: groups or people");
    }
    
  };

  /**
   * handle the incoming request based on HTTP method
   * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesRest/groups/a:b
   * the urlStrings would be size two: {"group", "a:b"}
   * @param vootRestHttpMethod is the method of the call
   * @return the result object
   */
  public abstract Object service(List<String> urlStrings, VootRestHttpMethod vootRestHttpMethod);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true if exception should be thrown on not found
   * @return the enum or null or exception if not found
   * @throws GrouperRestInvalidRequest if there is a problem
   */
  public static VootWsRest valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperRestInvalidRequest {
    
    if (StringUtils.equals(VOOT_BLANK.name(), string)) {
      throw new RuntimeException(VootWsRest.class.toString() + " cannot be " + VOOT_BLANK.name());
    }
    
    //if blank, use the blank
    if (StringUtils.isBlank(string)) {
      string = VOOT_BLANK.name();
    }
    
    return GrouperUtil.enumValueOfIgnoreCase(VootWsRest.class, 
        string, exceptionOnNotFound);
  }
}
