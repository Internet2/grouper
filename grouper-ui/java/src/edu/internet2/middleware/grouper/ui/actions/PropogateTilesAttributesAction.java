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

package edu.internet2.middleware.grouper.ui.actions;

import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.tiles.ComponentContext;
import edu.internet2.middleware.grouper.GrouperSession;


/**
 * Low level Strut's actiopn which acts as a controller for dynamic tiles. It 
 * attempts to add some level of inheritance between tiles - making attributes from 
 * 'parent' tiles available to 'subtiles'. 
 * 
 * 
 * @author Gary Brown.
 * @version $Id: PropogateTilesAttributesAction.java,v 1.3 2005-12-08 15:30:52 isgwb Exp $
 */
public class PropogateTilesAttributesAction extends LowLevelGrouperCapableAction {

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {

		//Get tiles attributes set using 'put' tags
		Map tilesAttributes = getTilesAttributes(request); //from
														   // GrouperCapableAction
		ComponentContext context = ComponentContext.getContext(request);
		
		//parentTilesContext is set in dynamicTile.jsp
		ComponentContext parent = (ComponentContext) context
				.getAttribute("parentTilesContext");
		if (parent == null)
			return null;
		Iterator it = parent.getAttributeNames();
		String name;
		Object value;
		while (it.hasNext()) {
			name = (String) it.next();
			value = parent.getAttribute(name);
			if (!tilesAttributes.containsKey(name)) {
				context.putAttribute(name, value);
			}
		}

		return null;
	}
}
