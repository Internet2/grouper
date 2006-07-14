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

import java.util.*;
import javax.servlet.http.*;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.ui.util.*;

/**
 * Initialises HttpSession after login. <p/>Should probably make an interface
 * and allow site specific initialisation
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: SessionInitialiser.java,v 1.5 2006-07-14 11:04:11 isgwb Exp $
 */

public class SessionInitialiser {

	/**
	 * Sets locale and calls any module specific initialisation
	 * 
	 * @param request
	 *            current HttpServletRequest
	 * @throws Exception
	 */
	public static void init(HttpServletRequest request) throws Exception {

		String localeStr = request.getParameter("lang");
		HttpSession session = request.getSession();
		Locale locale = null;
		if (localeStr == null) {
			locale = Locale.getDefault();
		} else {
			locale = new Locale(localeStr);
		}
		session.setAttribute("org.apache.struts.action.LOCALE", locale);

		org.apache.struts.config.ModuleConfig configx = (org.apache.struts.config.ModuleConfig) request
				.getAttribute("org.apache.struts.action.MODULE");
		String module = "";
		if (configx != null)
			module = configx.getPrefix();
		SessionInitialiser.init(module, locale.toString(), session);
		session.setAttribute("javax.servlet.jsp.jstl.fmt.locale", locale);

		session.setAttribute("sessionInited", localeStr);

	}

	/**
	 * Module specific initialisation with no locale specified
	 * 
	 * @param module
	 *            Struts's module
	 * @param session
	 *            current HttpSession
	 * @throws Exception
	 */
	public static void init(String module, HttpSession session)
			throws Exception {
		init(module, null, session);
	}

	/**
	 * Module and locale specific initialisation
	 * 
	 * @param module
	 *            Strut's module
	 * @param locale
	 *            selected locale
	 * @param session
	 *            current HttpSession
	 * @throws Exception
	 */
	public static void init(String module, String locale, HttpSession session)
			throws Exception {
		if (module != null)
			module = module.replaceAll("^/", "");
		ResourceBundle defaultInit = ResourceBundle
				.getBundle("/resources/init");
		if (module == null || module.equals("")) {
			module = defaultInit.getString("default.module");
		}
		ResourceBundle moduleInit = ResourceBundle.getBundle("/resources/"
				+ module + "/init");
		if (locale == null || locale.equals("")) {
			locale = moduleInit.getString("default.locale");
		}
		Locale localeObj = new Locale(locale);
		ResourceBundle grouperBundle = ResourceBundle.getBundle(
				"resources.grouper.nav", localeObj);
		ResourceBundle grouperMediaBundle = ResourceBundle.getBundle(
				"resources.grouper.media", localeObj);

		ChainedResourceBundle chainedBundle = null;
		ChainedResourceBundle chainedMediaBundle = null;

		if (module.equals("i2mi") || module.equals("grouper")) {
			chainedBundle = new ChainedResourceBundle(grouperBundle,
					"navResource");
			chainedMediaBundle = new ChainedResourceBundle(grouperMediaBundle,
					"mediaResource");
		} else {
			ResourceBundle moduleBundle = ResourceBundle.getBundle("resources."
					+ module + ".nav", localeObj);
			ResourceBundle moduleMediaBundle = ResourceBundle.getBundle(
					"resources." + module + ".media", localeObj);
			chainedBundle = new ChainedResourceBundle(moduleBundle,
					"navResource");
			chainedBundle.addBundle(grouperBundle);
			chainedMediaBundle = new ChainedResourceBundle(moduleMediaBundle,
					"mediaResource");
			chainedMediaBundle.addBundle(grouperMediaBundle);
		}
		session.setAttribute("nav",
				new javax.servlet.jsp.jstl.fmt.LocalizationContext(
						chainedBundle));
		session.setAttribute("navMap", new MapBundleWrapper(chainedBundle));

		session.setAttribute("media",
				new javax.servlet.jsp.jstl.fmt.LocalizationContext(
						chainedMediaBundle));
		session.setAttribute("mediaMap", new MapBundleWrapper(
				chainedMediaBundle));
		String pageSizes = chainedMediaBundle
				.getString("pager.pagesize.selection");

		String[] pageSizeSelections = pageSizes.split(" ");
		session.setAttribute("pageSizeSelections", pageSizeSelections);
		session.setAttribute("stemSeparator", GrouperHelper.HIER_DELIM);
		try {
			String initialStems = chainedMediaBundle
					.getString("plugin.initialstems");
			if (initialStems != null && !"".equals(initialStems))
				session.setAttribute("isQuickLinks", Boolean.TRUE);
		} catch (Exception e) {

		}
		GrouperSession s = getGrouperSession(session);
		//@TODO: should we split the personalStemRoot and create
		//any stems which are missing

		try {
			String personalStem = chainedMediaBundle
					.getString("plugin.personalstem");
			if (personalStem != null && !"".equals(personalStem)) {
				PersonalStem personalStemInstance = (PersonalStem) Class
						.forName(personalStem).newInstance();
				GrouperHelper.createIfAbsentPersonalStem(s,personalStemInstance);
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		session.setAttribute("sessionInited", Boolean.TRUE);
	}

	/**
	 * Proper way to get GrouperSession from HttpSession
	 * 
	 * @param session
	 *            current HttpSession
	 * @return the current GrouperSession
	 */
	public static GrouperSession getGrouperSession(HttpSession session) {

		GrouperSession s = (GrouperSession) session
				.getAttribute("edu.intenet2.middleware.grouper.ui.GrouperSession");
		return s;
	}

	/**
	 * Proper way of getting the underlying HttpSession attribute value for the
	 * currently logged in user.
	 * 
	 * @param session
	 * @return the id of the currently authenticated user
	 */
	public static String getAuthUser(HttpSession session) {
		String authUser = (String) session.getAttribute("authUser");
		return authUser;
	}
}