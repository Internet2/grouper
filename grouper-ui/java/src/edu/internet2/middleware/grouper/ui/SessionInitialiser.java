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
 * @version $Id: SessionInitialiser.java,v 1.1.1.1 2005-08-23 13:04:14 isgwb Exp $
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
				GrouperHelper.createIfAbsentPersonalStem(s,
						personalStemInstance);
			}
		} catch (Exception e) {

		}
		session.setAttribute("sessionInited", Boolean.TRUE);
	}

	/**
	 * Proper way to get GrouperSession from HttpSession
	 * 
	 * @param session
	 *            current HttpSession
	 * @return
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
	 * @return
	 */
	public static String getAuthUser(HttpSession session) {
		String authUser = (String) session.getAttribute("authUser");
		return authUser;
	}
}