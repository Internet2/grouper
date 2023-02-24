/*******************************************************************************
 * Copyright 2023 Internet2
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
package edu.internet2.middleware.grouper.ws.security;

import java.security.Principal;
import java.util.List;
import java.util.Vector;

import org.apache.axis2.context.MessageContext;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 */
public class RampartUtil {

  public static String getUserIdLoggedIn() {
    String userIdLoggedIn = null;
    
    MessageContext msgCtx = MessageContext.getCurrentMessageContext();
    Vector results = null;
    if ((results = (Vector) msgCtx.getProperty(WSHandlerConstants.RECV_RESULTS)) == null) {
      throw new RuntimeException("No Rampart security results!");
    }
    OUTER: for (int i = 0; i < results.size(); i++) {
      WSHandlerResult rResult = (WSHandlerResult) results.get(i);
      List<WSSecurityEngineResult> wsSecEngineResults = rResult.getResults();

      for (int j = 0; j < wsSecEngineResults.size(); j++) {
        WSSecurityEngineResult wser = wsSecEngineResults
            .get(j);
        if (GrouperUtil.equals(wser.get(WSSecurityEngineResult.TAG_ACTION), WSConstants.UT) && wser.get(WSSecurityEngineResult.TAG_PRINCIPAL) != null) {

          //Extract the principal
          Principal principal = (Principal) wser.get(WSSecurityEngineResult.TAG_PRINCIPAL);

          //Get user
          userIdLoggedIn = principal.getName();
          break OUTER;
        }
      }
    }
    return userIdLoggedIn;
  }
}
