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
 * $Id: GrouperWssecAuthentication.java,v 1.1 2008-04-01 08:38:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.security;

import java.io.IOException;

import org.apache.ws.security.WSPasswordCallback;


/**
 * Implement this for rampart security.  See GrouperWssecSample
 * for an example
 */
public interface GrouperWssecAuthentication {

  /**
   * <pre>
   * authenticate the user, and find the subject and return.
   * See GrouperWssecSample for an example
   * </pre>
   * @param wsPasswordCallback
   * @return true if that callback type is supported, false if not
   * @throws IOException if there is a problem or if user is not authenticated correctly
   */
  public boolean authenticate(WSPasswordCallback wsPasswordCallback) throws IOException;
  

}
