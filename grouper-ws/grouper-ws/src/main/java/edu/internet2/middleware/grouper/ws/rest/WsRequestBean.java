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
 * @author mchyzer $Id: WsRequestBean.java,v 1.2 2008-03-26 07:39:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest;

import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * Bean (used in Rest) as a request for web service
 */
public interface WsRequestBean {
  
  /** 
   * see which http method this is supposed to be associated with, and
   * override whatever was passed in
   * @return the method
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod();    
}
