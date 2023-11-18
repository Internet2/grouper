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
 * $Id: WsCustomAuthentication.java,v 1.1 2008-04-01 08:38:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.security;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;


/**
 * <pre>
 * implement this interface and provide the class to the classpath and grouper-ws.properties
 * to override the default of httpServletRequest.getUserPrincipal();
 * for non-Rampart authentication
 * 
 * if user is not found, throw a runtime exception.  Could be WsInvalidQueryException
 * which is a type of runtime exception (experiment and see what you want the response to 
 * look like)
 * 
 * </pre>
 */
public interface WsCustomAuthentication {
  
  /**
   * retrieve the current username (subjectId) from the request object.
   * @param httpServletRequest
   * @return the logged in username (subjectId)
   * @throws WsInvalidQueryException if there is a problem
   */
  public String retrieveLoggedInSubjectId(HttpServletRequest httpServletRequest)
    throws WsInvalidQueryException;
  
}
