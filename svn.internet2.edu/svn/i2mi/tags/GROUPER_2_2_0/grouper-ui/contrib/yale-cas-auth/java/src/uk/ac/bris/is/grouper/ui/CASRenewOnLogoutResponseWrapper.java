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

package uk.ac.bris.is.grouper.ui;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Designed to allow a Filter which precedes the CAS Filter to
 * control whether renew=true is passed to CAS to force a 
 * new login. The CAS filter only take an initialisation parameter
 * which sets renew for all requests
 * 
 * @author Gary Brown.
 * @version $Id: CASRenewOnLogoutResponseWrapper.java,v 1.2 2006-03-01 16:19:28 isgwb Exp $
 */

public class CASRenewOnLogoutResponseWrapper extends HttpServletResponseWrapper {


	/**
	 * Wraps response
	 * @param response
	 */
	public CASRenewOnLogoutResponseWrapper(HttpServletResponse response) {
		super(response);
	}
	
	/**
	 * If _grouper_loggedOut header set and url includes service=
	 * then add renew=true
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String url) throws IOException{
		if(containsHeader("_grouper_loggedOut") && url.indexOf("service=") > -1) {
			url = url + "&renew=true";
			//url = url.replaceAll("&renew=false","renew=true");
		}
		super.sendRedirect(url);
	}
}
