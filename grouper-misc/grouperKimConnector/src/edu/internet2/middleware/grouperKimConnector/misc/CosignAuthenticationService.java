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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.misc;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.kuali.rice.kim.service.AuthenticationService;

import edu.internet2.middleware.grouperClient.api.GcHasMember;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimUtils;


/**
 * This authenticator will work with cosign or other authentication services
 * that put the netId in the request.attribute(REMOTE_USER)
 */
public class CosignAuthenticationService implements AuthenticationService {

  /**
   * logger
   */
  private static final Logger LOG = Logger.getLogger(CosignAuthenticationService.class);

  /**
   * @see org.kuali.rice.kim.service.AuthenticationService#getPrincipalName(javax.servlet.http.HttpServletRequest)
   */
  public String getPrincipalName(HttpServletRequest request) {
    String netId = (String)request.getAttribute("REMOTE_USER");
    if (GrouperClientUtils.isBlank(netId)) {
      netId = request.getRemoteUser();
    }
    if (GrouperClientUtils.isBlank(netId) && request.getUserPrincipal() != null) {
      netId = request.getUserPrincipal().getName();
    }
    LOG.debug("netId: " + netId);
    if (GrouperClientUtils.isBlank(netId)) {
      throw new RuntimeException("netId is null");
    }
    String requireGroup = GrouperClientUtils.propertiesValue("kuali.authn.require.group", false);
    
    if (!GrouperClientUtils.isBlank(requireGroup)) {
      
      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup(null, 
          GrouperKimUtils.subjectSourceId(), netId);
      
      WsHasMemberResults wsHasMemberResults = new GcHasMember().addSubjectLookup(wsSubjectLookup)
        .assignGroupName(requireGroup).execute();
      
      WsHasMemberResult wsHasMemberResult = wsHasMemberResults.getResults()[0];
      
      LOG.debug("checking group for user: " + netId + ", " + requireGroup + ": " + wsHasMemberResult.getResultMetadata().getResultCode());
      
      if (!GrouperClientUtils.equals("IS_MEMBER", wsHasMemberResult.getResultMetadata().getResultCode())) {
        throw new RuntimeException("User " + netId + " doesnt have access since is not in group: " + requireGroup); 
      }
      
    }
    
    return netId;
  }

}
