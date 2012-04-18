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
 * @author mchyzer
 * $Id: WsGrouperDefaultAuthentication.java,v 1.1 2008-04-01 08:38:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.security;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;


/**
 * default authentication for grouper if a custom one isnt specified in
 * grouper-ws.properties for non-rampart requests
 */
public class WsGrouperDefaultAuthentication implements WsCustomAuthentication {

  /**
   * 
   * @see edu.internet2.middleware.grouper.ws.security.WsCustomAuthentication#retrieveLoggedInSubjectId(javax.servlet.http.HttpServletRequest)
   */
  public String retrieveLoggedInSubjectId(HttpServletRequest httpServletRequest)
      throws RuntimeException {
    
    // use this to be the user connected, or the user act-as
    String userIdLoggedIn = GrouperServiceJ2ee.retrieveUserPrincipalNameFromRequest();

    return userIdLoggedIn;
  }

}
