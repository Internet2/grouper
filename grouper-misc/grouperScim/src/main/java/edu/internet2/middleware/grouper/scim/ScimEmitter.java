/**
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
 */
package edu.internet2.middleware.grouper.scim;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import org.apache.log4j.Logger;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientWebException;
import org.apache.wink.client.handlers.ClientHandler;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.exceptions.BadRequestException;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.charon.samples.utils.CharonResponseHandler;
import org.wso2.charon.utils.authentication.BasicAuthHandler;
import org.wso2.charon.utils.authentication.BasicAuthInfo;

//TODO -- should we be using groupID or groupName()?
/**
 * Emits SCIM.  
 * @author David Langenberg <davel@uchicago.edu>
 */
public class ScimEmitter {

  protected static Logger log = Logger.getLogger(ScimEmitter.class);

  protected SCIMClient scimClient;
  protected CharonResponseHandler crh;
  protected RestClient restClient;
  
  protected GrouperConfig gConf;

  private BasicAuthInfo encodedBasicAuthInfo;


  public ScimEmitter() {
	gConf = GrouperConfig.retrieveConfig();
	  
    ClientConfig config = new ClientConfig();
    crh = new CharonResponseHandler();
    config.handlers(new ClientHandler[]{crh});
    restClient = new RestClient(config);

    //create a apache wink ClientHandler to intercept and identify response messages  
    CharonResponseHandler responseHandler = new CharonResponseHandler();
    responseHandler.setSCIMClient(scimClient);
    //set the handler in wink client config  
    ClientConfig clientConfig = new ClientConfig();
    clientConfig.handlers(new ClientHandler[]{responseHandler});
    //create a wink rest client with the above config  
    restClient = new RestClient(clientConfig);
    //create resource endpoint to access group resource  
    Resource groupResource = restClient.resource(gConf.propertyValueString("scim.endpoint"));
	
    BasicAuthInfo basicAuthInfo = new BasicAuthInfo();
    //set creds
    basicAuthInfo.setUserName(gConf.propertyValueString("scim.user"));
    basicAuthInfo.setPassword(gConf.propertyValueString("scim.password"));

    BasicAuthHandler basicAuthHandler = new BasicAuthHandler();
    encodedBasicAuthInfo = (BasicAuthInfo) basicAuthHandler.getAuthenticationToken(basicAuthInfo);

    scimClient = new SCIMClient();
  }

  /**
   * creates a SCIM Group out of a grouper group
   * @param group
   * @return 
   */
  public String createGroup(Group group) {
    try {
      
      org.wso2.charon.core.objects.Group scimGroup = scimClient.createGroup();
      
      scimGroup.setDisplayName(group.getDisplayName());
      scimGroup.setExternalId(group.getName());
      
      for(Member m : group.getMembers()){
        scimGroup.setMember(m.getSubjectId());
      }
      
      //create resource endpoint to access group resource  
      //no groupid appended to this one because that's somethign the server will handle
      Resource groupResource = restClient.resource(gConf.propertyValueString("scim.endpoint"));
      
      return groupResource.
              header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
              contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON).
              post(String.class, scimClient.encodeSCIMObject(scimGroup, SCIMConstants.JSON));
    } catch (CharonException e) {
      throw new RuntimeException(e);
    } catch (ClientWebException e){
		System.err.println(e.getMessage());
		System.err.println(e.getResponse().getMessage());
		log.error(e.getMessage(),e);
		throw new RuntimeException(e);
	}
  }
  
  /**
   * This updates a group which already exists in the SCIM endpoint.  According to the
   * SURFNet use-case, we can only send full-groups, not updates, so update will
   * for now be a delete & create operation
   * @param group
   * @return 
   */
  public String updateGroup(Group group){
    deleteGroup(group);
	return createGroup(group);
  }
  
  /**
   * Retrieves the SCIM server's group representation of the Grouper group.
   * @param group grouper group to query the SCIM server for
   * @return 
   */
  public org.wso2.charon.core.objects.Group getGroup(Group group) {
    //todo -- should this be protected or private?
    //create resource endpoint to access a known user resource.
    Resource groupResource = restClient.resource(gConf.propertyValueString("scim.endpoint") + group.getName());
    String response = groupResource.
            header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
            contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
            .get(String.class);
    try {
      org.wso2.charon.core.objects.Group scimGroup = (org.wso2.charon.core.objects.Group) scimClient.decodeSCIMResponse(response, SCIMConstants.JSON, 2);
      return scimGroup;
    } catch (BadRequestException e) {
      //TODO decide what to do
      throw new RuntimeException(e);
    } catch (CharonException e) {
      //TODO decide what to do
      throw new RuntimeException(e);
    }

  }
    
  public String deleteGroup(Group group) {

    //create resource endpoint to access group resource  
    //no groupid appended to this one because that's somethign the server will handle
    Resource groupResource = restClient.resource(gConf.propertyValueString("scim.endpoint") + group.getName());

    return groupResource.
            header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
            accept(SCIMConstants.APPLICATION_JSON).
            delete(String.class);

  }

}
