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
Copyright 2004-2008 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2008 The University Of Bristol

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

package edu.internet2.middleware.grouper.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.TryCatchFinally;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used in conjunction with ELTileRecorderTag to print a comment in current page
 * output which shows which template was rendered. If a dynamic template it
 * indicates the object type, view, selected key and template name
 * 
 * @author Gary Brown.
 * @version $Id: TileRecorderTag.java,v 1.7 2008-04-09 14:21:08 isgwb Exp $
 */

public class TileRecorderTag extends TagSupport implements TryCatchFinally{
	protected Log LOG = LogFactory.getLog(TileRecorderTag.class);
	public void doCatch(Throwable ex) throws Throwable {
		LOG.error(ex);
		try {
			Map navMap = (Map)pageContext.findAttribute("navMap");
			Object errMsg=navMap.get("jsp.error");
			String output="<span class=\"jspError\">" + errMsg + "</span>\n" + 
			"<!-- " + ex.getClass().getSimpleName() + ":" + ex.getMessage()+" -->";
			pageContext.getOut().println(output);
			List parent = (List) stack.peek();
			HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
			Map thisTemplate = (Map) parent.get(parent.size()-1);
			thisTemplate.put("jspErr", ex);
			
		} catch (Exception e) {
			LOG.error("Error handling doCatch.",e);
		}
		
	}
	private Stack stack = new Stack();

	private String view = null;

	private String type = null;

	private String key = null;

	public void doFinally() {
		// TODO Auto-generated method stub
		
	}
	private String tile = null;
	
	private String silent = null;

	/**
	 * @return Returns the key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            The key to set.
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return Returns the tile.
	 */
	public String getTile() {
		return tile;
	}

	/**
	 * @param tile
	 *            The tile to set.
	 */
	public void setTile(String tile) {
		this.tile = tile;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return Returns the view.
	 */
	public String getView() {
		return view;
	}

	/**
	 * @param view
	 *            The view to set.
	 */
	public void setView(String view) {
		this.view = view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.Tag#release()
	 */
	public void release() {
		// TODO Auto-generated method stub
		stack.clear();
		super.release();
		setView(null);
		setType(null);
		setKey(null);
		setTile(null);
		setSilent(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	 */
	public int doEndTag() throws JspException {
		printEnd();
		List parent = (List) stack.pop();
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		Throwable t = (Throwable)request.getAttribute("uiException");
		if(t!=null) {
			LOG.error(t);
			request.removeAttribute("uiException");
			Map thisTemplate = (Map) parent.get(0);
			thisTemplate.put("jspErr", t);
		}
		UIThreadLocal.replace("dynamicTiles", parent);
		return super.doEndTag();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.Tag#doStartTag()
	 */
	public int doStartTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest) pageContext
				.getRequest();

		Map thisTemplate = new HashMap();
		thisTemplate.put("view", getView());
		thisTemplate.put("type", getType());
		thisTemplate.put("key", getKey());
		thisTemplate.put("tile", getTile());
		List children = new ArrayList();
		thisTemplate.put("children", children);
		List parent = (List) UIThreadLocal.get("dynamicTiles");
		if (parent == null)
			parent = new ArrayList();
		parent.add(thisTemplate);
		UIThreadLocal.replace("dynamicTiles", children);
		stack.push(parent);
		printStart();
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Constructor 
	 */
	public TileRecorderTag() {
		super();
		// TODO Auto-generated constructor stub
	}

	private void printStart() {
		doPrint(true);
	}

	private void printEnd() {
		doPrint(false);
	}

	private void doPrint(boolean isStart) {
		if (view != null && !"".equals(view) || (getSilent()!=null && !getSilent().equals("")))
			return;
		String end = "end:";
		if (isStart)
			end = "start:";
		int indent = stack.size();
		StringBuffer padding = new StringBuffer();
		for (int i = 0; i < indent; i++) {
			padding.append("  ");
		}
		try {
			pageContext.getOut().println(
					padding + "<!--" + end + getTile() + "-->");
		} catch (Exception e) {
		}
	}
	/**
	 * @return Returns the silent.
	 */
	public String getSilent() {
		return silent;
	}
	/**
	 * @param silent The silent to set.
	 */
	public void setSilent(String silent) {
		this.silent = silent;
	}
}
