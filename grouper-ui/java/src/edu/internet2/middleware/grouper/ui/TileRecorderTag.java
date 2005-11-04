/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Bristol
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Bristol nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Bristol, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Bristol, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

/**
 * Used in conjunction with ELTileRecorderTag to print a comment in current page
 * output which shows which template was rendered. If a dynamic template it
 * indicates the object type, view, selected key and template name
 * 
 * @author Gary Brown.
 * @version $Id: TileRecorderTag.java,v 1.2 2005-11-04 11:02:02 isgwb Exp $
 */

public class TileRecorderTag extends TagSupport {
	private Stack stack = new Stack();

	private String view = null;

	private String type = null;

	private String key = null;

	private String tile = null;

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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	 */
	public int doEndTag() throws JspException {
		printEnd();
		List parent = (List) stack.pop();
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
		if (view != null && !"".equals(view))
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
}