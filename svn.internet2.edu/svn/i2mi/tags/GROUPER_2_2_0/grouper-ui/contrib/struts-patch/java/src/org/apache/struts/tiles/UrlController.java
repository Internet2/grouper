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
 * $Header: /home/hagleyj/i2mi/grouper-ui/contrib/struts-patch/java/src/org/apache/struts/tiles/UrlController.java,v 1.1.1.1 2005-08-23 13:03:13 isgwb Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2005-08-23 13:03:13 $
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.struts.tiles;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.ModuleUtils;

/**
 * Tiles controller including a local URL.
 */
public class UrlController implements Controller {

	/** 
	 * URL associated with this controller. 
	 */
	protected String url = null;

	/**
	 * Constructor.
	 * @param url URL.
	 */
	public UrlController(String url) {
		this.url = url;
	}

	/**
	 * Method associated to a tile and called immediately before the tile 
	 * is included.  This implementation calls an <code>Action</code>. 
	 * No servlet is set by this method.
	 *
	 * @param tileContext Current tile context.
	 * @param request Current request.
	 * @param response Current response.
	 * @param servletContext Current servlet context.
	 */
	public void perform(
		ComponentContext tileContext,
		HttpServletRequest request,
		HttpServletResponse response,
		ServletContext servletContext)
		throws ServletException, IOException {
		String fixedUrl = fixUrl(url,tileContext,request,servletContext);
		RequestDispatcher rd = servletContext.getRequestDispatcher(fixedUrl);
		if (rd == null) {
			throw new ServletException(
				"Controller can't find url '" + fixedUrl + "'.");
		}

		rd.include(request, response);
	}

	/**
	 * @see org.apache.struts.tiles.Controller#execute(org.apache.struts.tiles.ComponentContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.ServletContext)
	 */
	public void execute(
		ComponentContext tileContext,
		HttpServletRequest request,
		HttpServletResponse response,
		ServletContext servletContext)
		throws Exception {
        String fixedUrl = fixUrl(url,tileContext,request,servletContext);    
		RequestDispatcher rd = servletContext.getRequestDispatcher(fixedUrl);
		if (rd == null) {
			throw new ServletException(
				"Controller can't find url '" + fixedUrl + "'.");
		}

		rd.include(request, response);

	}
	private String fixUrl(String url,ComponentContext tileContext,HttpServletRequest request,ServletContext servletContext) {
		Object obj = tileContext.getAttribute("maintainModule");
		if(obj==null) return url;
		if("y".equals(obj.toString().toLowerCase())) {
			ModuleConfig moduleConfig =
	           ModuleUtils.getInstance().getModuleConfig(request, servletContext);
			return moduleConfig.getPrefix() + url;

		}
		return url;
	}

}
