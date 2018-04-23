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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import edu.internet2.middleware.grouper.GrouperSession;

/**
 * Factory class for resolving correct implementations of RepositoryBrowsers given a browse mode.
 * repository.browser.<browseMode>.class=<class name>
 *
 * @author Gary Brown.
 * @version $Id: RepositoryBrowserFactory.java,v 1.4 2006-10-05 09:00:36 isgwb Exp $
 */
public class RepositoryBrowserFactory {

	public static RepositoryBrowser getInstance(String browseMode,GrouperSession s,ResourceBundle navBundle,ResourceBundle mediaBundle) {
		String className = null;
		if("".equals(browseMode)) browseMode="My";
		try {
			className = mediaBundle.getString("repository.browser." + browseMode.toLowerCase() + ".class");
		}catch(MissingResourceException e) {
			throw new IllegalArgumentException("No RepositoryBrowser specified for " + browseMode);
		}
		Class rbClass = null;
		try {
			rbClass = Class.forName(className);
		}catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Cannot find " + className);
		}
		RepositoryBrowser browser = null;
		try {
			browser=(RepositoryBrowser)rbClass.newInstance();
		}catch(Exception e) {
			throw new RuntimeException(e); 
		}
		browser.init(s,navBundle,mediaBundle);
		return browser;
	}

}
