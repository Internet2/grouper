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

package edu.internet2.middleware.grouper.grouperUi.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;

/**
 * Initialises HttpSession after login. <p/>Should probably make an interface
 * and allow site specific initialisation
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: SessionInitialiser.java,v 1.1 2009-09-09 15:10:03 mchyzer Exp $
 */

public class SessionInitialiser {
  
  /**
   * 
   */
  public static final String RESOURCE_BUNDLE_KEY = "resourceBundleKey";

  /** logger */
	protected static Log LOG = LogFactory.getLog(SessionInitialiser.class);

	/** cache the locale and resource bundles.  the multikey is the module, and the locale.
	 * this gets the BundleBean, one for nav resource bundle, one for MapBundleWrapper, one for MapBundleWrapper (null), 
	 * media, media wrapper, media wrapper null */
  private static GrouperCache<MultiKey, BundleBean> resourceBundleCache = 
    new GrouperCache<MultiKey, BundleBean>(SessionInitialiser.class.getName(), 500, false, 120, 120, false);

  /**
   * get a resource bundle based on multikey
   * @param multiKey
   * @param isNav true for nav, false for media
   * @return the resource bundle
   */
  public static LocalizationContext retrieveLocalizationContext(MultiKey multiKey, boolean isNav) {
    if (isNav) {
      return resourceBundles(multiKey).getNav();
    } 
    return resourceBundles(multiKey).getMedia();
  }
  
  /**
   * get a resource bundle based on multikey
   * @param multiKey
   * @param isNav true for nav, false for media
   * @param returnNullsIfNotFound false if normal, true, is nulls if not found (e.g. mediaNullMap)
   * @return the resource bundle
   */
  public static MapBundleWrapper retrieveMapBundleWrapper(MultiKey multiKey, boolean isNav, boolean returnNullsIfNotFound) {
    
    //  session.setAttribute("nav",
    //      new javax.servlet.jsp.jstl.fmt.LocalizationContext(
    //          chainedBundle));
    //  session.setAttribute("navMap", new MapBundleWrapper(chainedBundle, false));
    //  session.setAttribute("navNullMap", navBundleWrapperNull);

    //  session.setAttribute("mediaMap", new MapBundleWrapper(
    //      chainedMediaBundle, false));
    //  //returns null if not there, not question marks
    //  session.setAttribute("mediaNullMap", new MapBundleWrapper(
    //      chainedMediaBundle, true));
    
    if (isNav) {
      if (!returnNullsIfNotFound) {
        return resourceBundles(multiKey).getNavMap();
      }
      return resourceBundles(multiKey).getNavMapNull();
    }
    if (!returnNullsIfNotFound) {
      return resourceBundles(multiKey).getMediaMap();
    }
    return resourceBundles(multiKey).getMediaMapNull();
  }
  
  

    
  /**
   * get the resource bundle by name and local
   * @param multiKey is the module, name (nav or media), and locale
   * @return the resource bundle
   */
  public static BundleBean resourceBundles( MultiKey multiKey) {
    //new MultiKey(module, name, locale);
    BundleBean resourceBundles = resourceBundleCache.get(multiKey);
    if (resourceBundles == null) {
      synchronized(SessionInitialiser.class) {
        resourceBundles = resourceBundleCache.get(multiKey);
        if (resourceBundles == null) {
          String module = (String)multiKey.getKey(0);
          Locale localeObj = (Locale)multiKey.getKey(1);
          
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
          
          MapBundleWrapper navBundleWrapperNull = new MapBundleWrapper(chainedBundle, true);

          addIncludeExcludeDefaults(chainedBundle, navBundleWrapperNull);

          resourceBundles = new BundleBean();
          resourceBundles.setNav(
              new javax.servlet.jsp.jstl.fmt.LocalizationContext(chainedBundle));
          resourceBundles.setNavMap(
              new MapBundleWrapper(chainedBundle, false));
          resourceBundles.setNavMapNull(
              navBundleWrapperNull);
          resourceBundles.setMedia(
              new javax.servlet.jsp.jstl.fmt.LocalizationContext(chainedMediaBundle));
          resourceBundles.setMediaMap(
              new MapBundleWrapper(chainedMediaBundle, false));
          resourceBundles.setMediaMapNull(
              new MapBundleWrapper(chainedMediaBundle, true));
          
          resourceBundleCache.put(multiKey, resourceBundles);
        }
      }
    }
    return resourceBundles;
  }
  
  
	/**
	 * Sets locale and calls any module specific initialisation
	 * 
	 * @param request
	 *            current HttpServletRequest
	 */
	public static void init(HttpServletRequest request) {

	  try {
  		String localeStr = request.getParameter("lang");
  		HttpSession session = request.getSession();
  		Locale locale = null;
  		
  		if(localeStr!=null && !"".equals(localeStr)) {
  			locale=createLocale(localeStr);
  		}
  		
  		session.setAttribute("org.apache.struts.action.LOCALE", locale);
      session.setAttribute("locale", locale);
  
      SessionInitialiser.init(null, session);

  		if(locale!=null) {
  			SessionInitialiser.init(null, locale.toString(), session);
  		}else{
  			SessionInitialiser.init(null, null, session);
  		}
      session.setAttribute("javax.servlet.jsp.jstl.fmt.locale", locale);
  
  		//session.setAttribute("sessionInited", localeStr);
	  } catch (Exception e) {
	    throw new RuntimeException(e);
	  }
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
		if(Boolean.TRUE.equals(session.getAttribute("sessionInited"))) return;
		if (module != null)
			module = module.replaceAll("^/", "");
		ResourceBundle defaultInit = ResourceBundle
				.getBundle("/resources/init");
		if (module == null || module.equals("")) {
			LOG.debug("Selecting default module");
			module = defaultInit.getString("default.module");
		}
    if (module != null){
      module = module.replaceAll("^/", "");
    }
		LOG.debug("module="+module);
		
		
		ResourceBundle moduleInit = ResourceBundle.getBundle("/resources/"
				+ module + "/init");
		if (locale == null || locale.equals("")) {
			locale = moduleInit.getString("default.locale");
			LOG.debug("Selecting default locale");
		}
		LOG.debug("locale=" + locale);
		Locale localeObj = createLocale(locale);
		    
		MultiKey resourceBundlesKey = new MultiKey(module, localeObj);
		
    session.setAttribute(RESOURCE_BUNDLE_KEY, resourceBundlesKey);
    
    ResourceBundle chainedMediaBundle = retrieveLocalizationContext(resourceBundlesKey, false).getResourceBundle();
    
		String pageSizes = chainedMediaBundle
				.getString("pager.pagesize.selection");

		String[] pageSizeSelections = pageSizes.split(" ");
		session.setAttribute("pageSizeSelections", pageSizeSelections);
		session.setAttribute("stemSeparator", ":");
		try {
			String initialStems = chainedMediaBundle
					.getString("plugin.initialstems");
			if (initialStems != null && !"".equals(initialStems))
				session.setAttribute("isQuickLinks", Boolean.TRUE);
		} catch (Exception e) {

		}
		getGrouperSession(session);
		//@TODO: should we split the personalStemRoot and create
		//any stems which are missing

		initThread(session);
		if(getAuthUser(session)!=null) session.setAttribute("sessionInited", Boolean.TRUE);


		
	}

  /**
   * add in exclude and include tooltips if not already in there (from grouper.properties)
   * @param chainedBundle
   * @param navBundleWrapperNull
   */
  private static void addIncludeExcludeDefaults(ChainedResourceBundle chainedBundle,
      MapBundleWrapper navBundleWrapperNull) {
    
    boolean useIncludeExclude = GrouperConfig.getPropertyBoolean("grouperIncludeExclude.use", false);
    boolean useRequireGroups = GrouperConfig.getPropertyBoolean("grouperIncludeExclude.requireGroups.use", false);

    String navPropertiesKey = null;
    if (useIncludeExclude) {
      navPropertiesKey = "tooltipTargetted.groupTypes." + GrouperConfig.getProperty("grouperIncludeExclude.type.name");
      if (navBundleWrapperNull.get(navPropertiesKey) == null) {
        chainedBundle.addToCache(navPropertiesKey, GrouperConfig.getProperty("grouperIncludeExclude.tooltip"));
      }
    }
    
    String requireGroupsTypeName = GrouperConfig.getProperty("grouperIncludeExclude.requireGroups.type.name");
    String tooltip = GrouperConfig.getProperty("grouperIncludeExclude.requireGroups.tooltip");
    if (useRequireGroups && !StringUtils.isBlank(requireGroupsTypeName) && !StringUtils.isBlank(tooltip)) {
      navPropertiesKey = "tooltipTargetted.groupTypes." + requireGroupsTypeName;
      if (navBundleWrapperNull.get(navPropertiesKey) == null) {
        chainedBundle.addToCache(navPropertiesKey, tooltip);
      }
    }
    
    //built in attribute
    String attributeName = GrouperConfig.getProperty("grouperIncludeExclude.requireGroups.attributeName");
    tooltip = GrouperConfig.getProperty("grouperIncludeExclude.requireGroups.attribute.tooltip");
    if (useRequireGroups && !StringUtils.isBlank(attributeName) && !StringUtils.isBlank(tooltip)) {
      navPropertiesKey = "tooltipTargetted.groupFields." + attributeName;
      if (navBundleWrapperNull.get(navPropertiesKey) == null) {
        chainedBundle.addToCache(navPropertiesKey, tooltip);
      }
    }
    
    //loop through custom types and attributes
    int i=0;
    while(true) {
      
      //#grouperIncludeExclude.requireGroup.name.0 = requireActiveEmployee
      //#grouperIncludeExclude.requireGroup.attributeOrType.0 = type
      //#grouperIncludeExclude.requireGroup.description.0 = If value is true, members of the overall group must be an active employee (in the school:community:activeEmployee group).  Otherwise, leave this value not filled in.
      String name = GrouperConfig.getProperty("grouperIncludeExclude.requireGroup.name." + i);
      if (StringUtils.isBlank(name)) {
        break;
      }
      String attributeOrTypeName = "grouperIncludeExclude.requireGroup.attributeOrType." + i;
      String type = GrouperConfig.getProperty(attributeOrTypeName);
      boolean isAttribute = false;
      if (StringUtils.equalsIgnoreCase("attribute", type)) {
        isAttribute = true;
      } else if (!StringUtils.equalsIgnoreCase("type", type)) {
        throw new RuntimeException("Invalid type: '" + type + "' for grouper.properties entry: " + attributeOrTypeName);
      }
      String description = GrouperConfig.getProperty("grouperIncludeExclude.requireGroup.description." + i);
      
      String key = "tooltipTargetted.group" + (isAttribute ? "Field" : "Type") + "s." + name;
      if (navBundleWrapperNull.get(key) == null) {
        chainedBundle.addToCache(key, description);
      }
      i++;
    }
    
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
	 * Proper way to get MenuFilters from HttpSession
	 * 
	 * @param session
	 *            current HttpSession
	 * @return the current MenuFilters
	 */
	public static Set getMenuFilters(HttpSession session) {
		Set mf = (Set)session.getAttribute("menuFilters");
		return mf;
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
	
	public static Locale createLocale(String localeStr) {
		if(localeStr==null || localeStr.equals("")) return Locale.getDefault();
		String[] parts = localeStr.split("_");
		Locale locale=null;
		switch (parts.length) {
		case 1:
			locale = new Locale(parts[0]);
			break;
		case 2:
			locale = new Locale(parts[0],parts[1]);
			break;
		case 3:
			locale = new Locale(parts[0],parts[1],parts[2]);
			break;

		default:
			throw new IllegalArgumentException("Wrong number of parts for locale: " + localeStr);
			
		}
		return locale;
	}
	
	public static void initThread(HttpSession session) {
		LocalizationContext lc = (LocalizationContext) session.getAttribute("media");
		
		if (lc == null) return;
		
		ResourceBundle mediaBundle = lc.getResourceBundle();
		UIThreadLocal.put("mediaBundle", mediaBundle);
	}

}