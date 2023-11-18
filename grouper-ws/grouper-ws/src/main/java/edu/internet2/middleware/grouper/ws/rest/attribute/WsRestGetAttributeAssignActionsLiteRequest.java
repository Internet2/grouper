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
package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * Bean for rest request to get attributes assign actions
 */
public class WsRestGetAttributeAssignActionsLiteRequest implements WsRequestBean {

	/** is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 */
    private String clientVersion;
	
	/** attribute definition id in the query */
	private String wsIdOfAttributeDef;
	
	/** name of attribute definition in the query */
	private String wsNameOfAttributeDef;
	
	/** id index of attribute definition in the query */
	private String wsIdIndexOfAttributeDef;
	
	/** action in the query */
	private String action;
	
	 /** if acting as another user */
	private String actAsSubjectId;
	
	 /** if acting as another user */
	private String actAsSubjectSourceId;
	
	 /** if acting as another user */
	private String actAsSubjectIdentifier;
	
	/** reserved for future use */
	private String paramName0;
	
	/** reserved for future use */
	private String paramValue0;
	
	/** reserved for future use */
	private String paramName1;
	
	/** reserved for future use */
	private String paramValue1;

	/**
	 * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
	 * @return client version
	*/
	public String getClientVersion() {
	  return this.clientVersion;
	}

	/**
	 * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
	 * @param clientVersion1
	 */
	public void setClientVersion(String clientVersion1) {
	  this.clientVersion = clientVersion1;
	}

	/**
	 * @return wsIdOfAttributeDef
	*/
	public String getWsIdOfAttributeDef() {
	  return this.wsIdOfAttributeDef;
	}

    /**
	 * 
	 * @param wsIdOfAttributeDef1
	*/
	public void setWsIdOfAttributeDef(String wsIdOfAttributeDef1) {
	  this.wsIdOfAttributeDef = wsIdOfAttributeDef1;
	}

	/**
	 * @return wsNameOfAttributeDef
	*/
	public String getWsNameOfAttributeDef() {
	  return this.wsNameOfAttributeDef;
	}

	/**
	 * @param wsNameOfAttributeDef1
	*/
	public void setWsNameOfAttributeDef(String wsNameOfAttributeDef1) {
	  this.wsNameOfAttributeDef = wsNameOfAttributeDef1;
	}

	/**
	 * @return wsAttributeDefIdIndex
	*/
	public String getWsIdIndexOfAttributeDef() {
	  return this.wsIdIndexOfAttributeDef;
	}

	/**
	 * @param wsIdIndexOfAttributeDef1
	 */
	public void setWsIdIndexOfAttributeDef(String wsIdIndexOfAttributeDef1) {
	  this.wsIdIndexOfAttributeDef = wsIdIndexOfAttributeDef1;
	}

	/**
	 * 
	 * @return action name in the attribute def
	*/
	public String getAction() {
	  return this.action;
	}

    /**
     * @param action1
    */
	public void setAction(String action1) {
      this.action = action1;
	}

	/**
	 * @return actAsSubjectId
	*/
	public String getActAsSubjectId() {
	  return this.actAsSubjectId;
	}

	/**
	 * @param actAsSubjectId1
	 */
	public void setActAsSubjectId(String actAsSubjectId1) {
	  this.actAsSubjectId = actAsSubjectId1;
	}

	/**
	 * @return actAsSubjectSourceId
	 */
	public String getActAsSubjectSourceId() {
	  return this.actAsSubjectSourceId;
	}

	/**
	 * @param actAsSubjectSourceId1
	*/
	public void setActAsSubjectSourceId(String actAsSubjectSourceId1) {
	  this.actAsSubjectSourceId = actAsSubjectSourceId1;
	}

	/**
	 * @return actAsSubjectIdentifier
	 */
	public String getActAsSubjectIdentifier() {
	  return this.actAsSubjectIdentifier;
	}

	/**
	 * @param actAsSubjectIdentifier1
	 */
	public void setActAsSubjectIdentifier(String actAsSubjectIdentifier1) {
	  this.actAsSubjectIdentifier = actAsSubjectIdentifier1;
	}

	/**
	 * reserved for future use
	 * @return paramName0
	 */
	public String getParamName0() {
	  return this.paramName0;
	}

	/**
	 * reserved for future use
	 * @param _paramName0
	*/
	public void setParamName0(String _paramName0) {
	  this.paramName0 = _paramName0;
	}

	/**
	 * reserved for future use
	 * @return paramValue0
	*/
	public String getParamValue0() {
	  return this.paramValue0;
	}

	/**
	 * reserved for future use
	 * @param _paramValue0
	*/
	public void setParamValue0(String _paramValue0) {
	  this.paramValue0 = _paramValue0;
	}

	/**
	 * reserved for future use
	 * @return paramName1
	 */
	public String getParamName1() {
	  return this.paramName1;
	}

	/**
	 * reserved for future use
	 * @param _paramName1
	 */
	public void setParamName1(String _paramName1) {
	  this.paramName1 = _paramName1;
	}

	/**
	 * reserved for future use
	 * @return paramValue1
	 */
	public String getParamValue1() {
	  return this.paramValue1;
	}

	/**
	 * reserved for future use
	 * @param _paramValue1
	 */
	public void setParamValue1(String _paramValue1) {
	  this.paramValue1 = _paramValue1;
	}

	/**
	 * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
	 */
	@Override
	public GrouperRestHttpMethod retrieveRestHttpMethod() {
	  return GrouperRestHttpMethod.GET;
	}

}
