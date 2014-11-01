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
 * @author mchyzer $Id: GrouperWsRestGet.java,v 1.9 2009-12-29 07:39:28 mchyzer Exp $
 * @author Andrea Biancini <andrea.biancini@gmail.com>
 */
package edu.internet2.middleware.grouperVoot.restLogic;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.grouperVoot.VootLogic;
import edu.internet2.middleware.grouperVoot.VootRestHttpMethod;
import edu.internet2.middleware.grouperVoot.beans.VootGroup;
import edu.internet2.middleware.grouperVoot.messages.VootErrorResponse;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * All first level resources on a get request.
 */
public enum VootWsRest {

	/** group get requests */
	groups {

		/**
		 * Handle the incoming request based on url.
		 * 
		 * @param urlStrings not including the app name or servlet. for
		 *        http://localhost/grouper-ws/voot/groups/a:b the urlStrings
		 *        would be size two: {"groups", "subjectid"}
		 * @return the result object.
		 */
		@Override
		public Object service(List<String> urlStrings, VootRestHttpMethod vootRestHttpMethod,
				Map<String, String[]> requestParameters) {
			
			if (vootRestHttpMethod != VootRestHttpMethod.GET) {
				return new VootErrorResponse("Wrong method", "Not expecting method: " + vootRestHttpMethod.name());
			}

			String userName = GrouperServiceUtils.popUrlString(urlStrings);
			String sortBy = null;
			if (requestParameters.get("sortBy") != null) {
				sortBy = requestParameters.get("sortBy")[0];
			}
			int start = this.getStart(requestParameters);
			int count = this.getCount(requestParameters);

			if (StringUtils.isBlank(userName)) {
				String[] searchTerm = requestParameters.get("search");
				return VootLogic.getGroups(StringUtils.join(searchTerm, "%"), sortBy, start, count);
			}

			if (GrouperUtil.length(urlStrings) > 0) {
				return new VootErrorResponse("Too many params", "Not expecting more URL strings: "
						+ GrouperUtil.toStringForLog(urlStrings));
			}

			if (StringUtils.equals("@me", userName)) {
				return VootLogic.getGroups(GrouperSession.staticGrouperSession().getSubject(), sortBy, start, count);
			} else {
				try {
					return VootLogic.getGroups(SubjectFinder.findById(userName, true), sortBy, start, count);
				} catch (SubjectNotFoundException e) {
					return new VootErrorResponse("Subject error",
							"Subject not found: " + userName + ", " + GrouperUtil.toStringForLog(urlStrings));
				} catch (SubjectNotUniqueException e) {
					return new VootErrorResponse("Subject error",
							"Subject not unique: " + userName + ", " + GrouperUtil.toStringForLog(urlStrings));
				}
			}
		}
	},

	/** people get requests */
	people {

		/**
		 * Handle the incoming request based on url.
		 * 
		 * @param urlStrings not including the app name or servlet. for
		 *        http://localhost/grouper-ws/voot/people/@me the urlStrings
		 *        would be size three: {"people", "subjectid", "a:b"}
		 * @return the result object.
		 */
		@Override
		public Object service(List<String> urlStrings, VootRestHttpMethod vootRestHttpMethod,
				Map<String, String[]> requestParameters) {

			if (vootRestHttpMethod != VootRestHttpMethod.GET) {
				throw new RuntimeException("Not expecting method: " + vootRestHttpMethod.name());
			}

			String personId = GrouperServiceUtils.popUrlString(urlStrings);
			String groupName = GrouperServiceUtils.popUrlString(urlStrings);
			String sortBy = null;
			if (requestParameters.get("sortBy") != null) {
				sortBy = requestParameters.get("sortBy")[0];
			}
			int start = this.getStart(requestParameters);
			int count = this.getCount(requestParameters);

			if (StringUtils.isBlank(personId)) {
				return new VootErrorResponse("No username",
						"Error: no userName passed, " + vootRestHttpMethod + ", " + GrouperUtil.toStringForLog(urlStrings));
			}

			if (StringUtils.isBlank(groupName)) {
				return new VootErrorResponse("No group name",
						"Error: no group name passed, " + vootRestHttpMethod + ", " + GrouperUtil.toStringForLog(urlStrings));
			}

			if (GrouperUtil.length(urlStrings) > 0) {
				return new VootErrorResponse("Too many params",
						"Not expecting more URL strings: " + GrouperUtil.toStringForLog(urlStrings));
			}

			// get the members of that group
			VootGroup vootGroup = new VootGroup();
			vootGroup.setId(groupName);

			try {
				if (StringUtils.equals("@me", personId)) {
					return VootLogic.getMembers(GrouperSession.staticGrouperSession().getSubject(), vootGroup, sortBy, start, count);
				} else {
					return VootLogic.getMembers(SubjectFinder.findById(personId, true), vootGroup, sortBy, start, count);
				}
			} catch (SubjectNotFoundException e) {
				return new VootErrorResponse("Subject error",
						"Subject not found: " + personId + ", " + GrouperUtil.toStringForLog(urlStrings));
			} catch (SubjectNotUniqueException e) {
				return new VootErrorResponse("Subject error",
						"Subject not unique: " + personId + ", " + GrouperUtil.toStringForLog(urlStrings));
			} catch (GroupNotFoundException e) {
				return new VootErrorResponse("Group not found", e.getMessage());
			}
		}
	},

	/** blank or other request */
	VOOT_BLANK {

		/**
		 * Returns an error for wrong method called.
		 * @return the error object. 
		 */
		@Override
		public Object service(List<String> urlStrings, VootRestHttpMethod vootRestHttpMethod,
				Map<String, String[]> requestParameters) {
			
			if (vootRestHttpMethod != VootRestHttpMethod.GET) {
				return new VootErrorResponse("Wrong params",  "Not expecting method: " + vootRestHttpMethod.name());
			}
			
			return new VootErrorResponse("Wrong params", "Pass in a url string: groups or people");
		}

	};

	/**
	 * Utility function to retrieve from the query string the startIndex parameter
	 * (if present).
	 * @return the integer value of the parameter passed. 
	 */
	protected int getStart(Map<String, String[]> requestParameters) {
		if (requestParameters.containsKey("startIndex")) {
			return Integer.parseInt(requestParameters.get("startIndex")[0]);
		}
		return 0;
	}

	/**
	 * Utility function to retrieve from the query string the count parameter
	 * (if present).
	 * @return the integer value of the parameter passed. 
	 */
	protected int getCount(Map<String, String[]> requestParameters) {
		if (requestParameters.containsKey("count")) {
			return Integer.parseInt(requestParameters.get("count")[0]);
		}
		return -1;
	}

	/**
	 * Handle the incoming request based on HTTP method.
	 * 
	 * @param urlStrings not including the app name or servlet. for
	 *        http://localhost/grouper-ws/servicesRest/groups/a:b the
	 *        urlStrings would be size two: {"group", "a:b"}
	 * @param vootRestHttpMethod is the method of the call.
	 * @param requestParameters the parameters passed to the request.
	 * @return the result object.
	 */
	public abstract Object service(List<String> urlStrings, VootRestHttpMethod vootRestHttpMethod,
			Map<String, String[]> requestParameters);

	/**
	 * Do a case-insensitive matching.
	 * 
	 * @param string the first commant to VOOT from query string.
	 * @param exceptionOnNotFound true if exception should be thrown on not found
	 * @return the enum or null or exception if not found.
	 * @throws RuntimeException if empty string passed.
	 * @throws GrouperRestInvalidRequest if there is a problem.
	 */
	public static VootWsRest valueOfIgnoreCase(String string, boolean exceptionOnNotFound)
			throws GrouperRestInvalidRequest {

		try { 
			if (StringUtils.equals(VOOT_BLANK.name(), string)) {
				throw new RuntimeException(VootWsRest.class.toString() + " cannot be " + VOOT_BLANK.name());
			}
	
			// if blank, use the blank
			if (StringUtils.isBlank(string)) {
				string = VOOT_BLANK.name();
			}
	
			return GrouperUtil.enumValueOfIgnoreCase(VootWsRest.class, string, exceptionOnNotFound);
		} catch (RuntimeException e) {
			if (exceptionOnNotFound) throw e;
			return GrouperUtil.enumValueOfIgnoreCase(VootWsRest.class, VOOT_BLANK.name(), exceptionOnNotFound);
		}
	}
}
