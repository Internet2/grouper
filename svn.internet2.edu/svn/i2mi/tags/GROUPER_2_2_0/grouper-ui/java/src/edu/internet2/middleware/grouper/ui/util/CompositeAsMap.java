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
package edu.internet2.middleware.grouper.ui.util;

import org.apache.commons.beanutils.WrapDynaBean;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Composite;

/**
 * Wraps a Composite - allows non persistent values to be stored for the UI and
 * works well with JSTL
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: CompositeAsMap.java,v 1.7 2008-07-21 04:43:47 mchyzer Exp $
 */
public class CompositeAsMap extends ObjectAsMap {
	protected Composite composite = null;

	protected final static String objType = "Composite";

	private GrouperSession grouperSession = null;
	
	protected CompositeAsMap() {}

	/**
	 * @param stem Stem to wrap
	 * @param s GrouperSession for authenticated user
	 */
	public CompositeAsMap(Composite composite) {
		super();
		init(composite);
	}
	
	protected void init(Composite c) {
		dynaBean = new WrapDynaBean(c);
		super.objType = objType;
		if (c == null)
			throw new NullPointerException(
					"Cannot create CompositeAsMap with a null composite");
		this.composite = c;
		wrappedObject = c;
	}
}
