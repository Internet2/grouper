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
Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2005 The University Of Bristol

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package edu.internet2.middleware.grouper.customqs.ui;

import javax.servlet.http.HttpServletRequestWrapper;

import javax.servlet.http.HttpSession;


/**
 * Used with EasyLoginFilter to provide a value for request.getRemoteUser()
 * based on the easyAuthUser session attribute
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: EasyLoginHttpServletRequest.java,v 1.1 2005-12-14 15:24:11 isgwb Exp $
 */

 
public class EasyLoginHttpServletRequest extends HttpServletRequestWrapper 
{
  
  public EasyLoginHttpServletRequest(javax.servlet.http.HttpServletRequest request) 
  {
    super(request);
  }

  /**
     * Tries to return the RemoteUser from the easyAuthUser session attribute
     */
  public java.lang.String getRemoteUser() 
  {
   HttpSession session = getSession();
   if(session!=null) return  (String)session.getAttribute("easyAuthUser");
   return null;
  }
}
