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
Copyright 2004-2007 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2007 The University Of Bristol

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

import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface for resolving an object and a view to a template. This is a
 * plugable interface. To use your own implementation set the key
 * <i>edu.internet2.middleware.grouper.ui.TemplateResolver </i> in
 * resources/media.resources to the name of a class which implements this
 * interface. If none is specified <i>DefaultTemplateREsolverImpl </i> is used.
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: TemplateResolver.java,v 1.4 2007-04-11 08:19:24 isgwb Exp $
 */
public interface TemplateResolver {
	/**
	 * Given an Object determine its type. The type is not necessarily the Java
	 * type - it can be an entity type e.g. GroupAsMap has an object type of
	 * GrouperGroup
	 * 
	 * @param obj
	 *            object to determine type of
	 * @return generic type of obj
	 */
	public String getObjectType(Object obj);

	/**
	 * Given an Object and the name of a view, use the ResourceBundle to
	 * determine which JSP should render the view. request is redundant at the
	 * moment.
	 * 
	 * @param obj
	 *            object to be rendered
	 * @param view
	 *            name of template
	 * @param bundle
	 *            ResourceBundle containing keys which can be maped to template
	 *            names
	 * @param request
	 *            current HttpServletRequest
	 * @return name of template to use to render obj
	 */
	public String getTemplateName(Object obj, String view,
			ResourceBundle bundle, HttpServletRequest request);
}
