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
Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2006 The University Of Bristol

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

import javax.servlet.jsp.JspException;

import org.apache.strutsel.taglib.utils.EvalHelper;

/**
 * Used in conjunction with TileRecorderTag to print a comment in current page output 
 * which shows which template was rendered. If a dynamic template it indicates the object type,
 * view, selected key and template name
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: ELTileRecorderTag.java,v 1.4 2006-07-17 10:01:28 isgwb Exp $
 */
public class ELTileRecorderTag extends TileRecorderTag {
	private String view = null;

	private String type = null;

	private String key = null;

	private String tile = null;
	
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

	/**
	 * Constructor
	 */
	public ELTileRecorderTag() {
		super();
		// TODO Auto-generated constructor stub
	}

	
	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.Tag#doStartTag()
	 */
	public int doStartTag() throws JspException {
		evaluateExpressions();
		return (super.doStartTag());
	}

	/**
	 * Processes all attribute values which use the JSTL expression evaluation
	 * engine to determine their values.
	 * 
	 * @exception JspException
	 *                if a JSP exception has occurred
	 */
	private void evaluateExpressions() throws JspException {
		String string = null;

		if ((string = EvalHelper.evalString("view", getView(), this,
				pageContext)) != null)
			setView(string);
		if ((string = EvalHelper.evalString("type", getType(), this,
				pageContext)) != null)
			setType(string);
		if ((string = EvalHelper.evalString("key", getKey(), this, pageContext)) != null)
			setKey(string);
		if ((string = EvalHelper.evalString("page", getTile(), this,
				pageContext)) != null)
			setTile(string);
		if ((string = EvalHelper.evalString("silent", getSilent(), this,
				pageContext)) != null)
			setSilent(string);
	}

	/**
	 * Resets attribute values for tag reuse.
	 */
	public void release() {
		super.release();
		setView(null);
		setType(null);
		setKey(null);
		setTile(null);
	}

}
