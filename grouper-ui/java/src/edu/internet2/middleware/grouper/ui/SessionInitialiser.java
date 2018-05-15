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

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ui.util.ChainedResourceBundle;
import edu.internet2.middleware.grouper.ui.util.DOMHelper;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.MapBundleWrapper;
import edu.internet2.middleware.grouper.ui.util.MembershipExporter;
import edu.internet2.middleware.grouper.ui.util.MembershipImportManager;

/**
 * Initialises HttpSession after login. <p/>Should probably make an interface
 * and allow site specific initialisation
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: SessionInitialiser.java,v 1.24 2009-10-16 08:06:26 isgwb Exp $
 */

public class SessionInitialiser {
  
  /**
   * 
   */
  public static final String RESOURCE_BUNDLE_KEY = "resourceBundleKey";

  /** logger */
  protected static Log LOG = LogFactory.getLog(SessionInitialiser.class);

  private static Group debuggers;
  private static boolean attemptedDebuggers=false;

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
          
          ResourceBundle grouperBundle = new GrouperNavResourceBundle(localeObj);
          ResourceBundle grouperMediaBundle = ResourceBundle.getBundle(
              "resources.grouper.media", localeObj);
          
          ChainedResourceBundle chainedMediaBundle = null;
          ChainedResourceBundle chainedBundle = new ChainedResourceBundle(grouperBundle,
              "navResource");

          if (module.equals("i2mi") || module.equals("grouper")) {
            chainedMediaBundle = new ChainedResourceBundle(grouperMediaBundle,
                "mediaResource");
          } else {
            ResourceBundle moduleMediaBundle = ResourceBundle.getBundle(
                "resources." + module + ".media", localeObj);

            chainedMediaBundle = new ChainedResourceBundle(moduleMediaBundle,
                "mediaResource");
            chainedMediaBundle.addBundle(grouperMediaBundle);
          }
          
          MapBundleWrapper navBundleWrapperNull = new MapBundleWrapper(chainedBundle, true);

          addIncludeExcludeDefaults(chainedBundle, navBundleWrapperNull);
          
          //add in grouper-ui.properties for media.properties
          GrouperUiConfig grouperUiConfig = GrouperUiConfig.retrieveConfig();
          for (String key : grouperUiConfig.propertyNames()) {
            chainedMediaBundle.addToCache(key, grouperUiConfig.propertyValueString(key, ""));
          }
          
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
      module = defaultInit.getString("default.module");
    }
    ResourceBundle moduleInit = ResourceBundle.getBundle("/resources/"
        + module + "/init");
    if (locale == null || locale.equals("")) {
      locale = moduleInit.getString("default.locale");
    }
    Locale localeObj = createLocale(locale);
        
    MultiKey resourceBundlesKey = new MultiKey(module, localeObj);
    
    session.setAttribute(RESOURCE_BUNDLE_KEY, resourceBundlesKey);
    
    ResourceBundle chainedMediaBundle = retrieveLocalizationContext(resourceBundlesKey, false).getResourceBundle();
    
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
    
    initDebugging(session);
    GrouperSession s = getGrouperSession(session);
    
    try {
    	if(GrouperHelper.isRoot(s) && "true".equals(chainedMediaBundle.getString("act-as-admin.default"))) {
    		session.setAttribute("activeWheelGroupMember",true);
    	}
    }catch(Exception e) {
    	
    }
    
    
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
    initThread(session);
    if(getAuthUser(session)!=null) session.setAttribute("sessionInited", Boolean.TRUE);
		try{
			session.setAttribute("fieldList",GrouperHelper.getFieldsAsMap());
		}catch(Exception e) {
			LOG.error("Error retrieving Fields: " + e.getMessage());
		}
		session.setAttribute("MembershipExporter",new MembershipExporter());
		session.setAttribute("MembershipImportManager",new MembershipImportManager());
		Document doc = null;
		try {
			doc = DOMHelper.getDomFromResourceOnClassPath("resources/grouper/ui-permissions.xml");
		}catch(Exception e){
		  LOG.info("resources/grouper/ui-permissions.xml not found. Default permissions apply.");
		}
		if(doc==null) {
			doc=DOMHelper.newDocument();
		}
		if(s==null) return;
		UiPermissions uip = new UiPermissions(s,doc);
		session.setAttribute("uiPermissions", uip);
		Set menuFilters = new LinkedHashSet();
		session.setAttribute("menuFilters",menuFilters);
		String mFilters = null;
		try {
			mFilters=chainedMediaBundle.getString("menu.filters");
			String[] parts = mFilters.split(" ");
			Class claz;
			MenuFilter filter;
			for(int i=0;i<parts.length;i++) {
				try {
					claz=Class.forName(parts[i]);
					filter=(MenuFilter)claz.newInstance();
					menuFilters.add(filter);
				}catch(Exception e){
					LOG.error("Unable to add menu filter [" + parts[i] + "]. " + e.getMessage());
				}
			}
		}catch(MissingResourceException mre){
			LOG.info("No menu.filters set in media.properties");
		}

	}
	private static void initDebugging(HttpSession session) {
		boolean debugEnable = false;
		String debugGroup=null;
		Properties mediaProperties = GrouperUiFilter.retrieveMediaProperties();
		try {
			debugEnable = !"true".equals(mediaProperties.getProperty("browser.debug.enable"));
		}catch(Exception e) {
			LOG.error("Error processing browser.debug.enable. Disabling.", e);
		}
		if(debugEnable){
			session.setAttribute("debugMessage", "debug.error.disabled");
			return;
		}
		if(debuggers==null) {
			try {
				debugGroup = mediaProperties.getProperty("browser.debug.group");
			}catch(Exception e) {
				LOG.info("browser.debug.group not set in media.properties");
			}
			if(!StringUtils.isBlank(debugGroup) && !debugGroup.matches("^@.*?@$") && !attemptedDebuggers) {
				try {
					attemptedDebuggers=true;
					GrouperSession root = GrouperSession.startRootSession();
					debuggers=GroupFinder.findByName(root, debugGroup, true);
				}catch(Exception e) {
					LOG.error("browser.debug.group:" + debugGroup + " does not exist",e);
				}
			}
		}
		GrouperSession gs = getGrouperSession(session);
		if(gs==null || (debuggers==null && attemptedDebuggers) || (debuggers !=null &&!debuggers.hasMember(gs.getSubject()))) {
			session.setAttribute("debugMessage", "debug.error.not-allowed");
			return;
		}
		boolean enableHtmlEditor=false;
		try {
			enableHtmlEditor="true".equals(mediaProperties.getProperty("browser.debug.group.enable-html-editor"));
		}catch(Exception e){
			LOG.info("browser.debug.group.enable-html-editor not set in media.properties");
		}
		session.setAttribute("enableHtmlEditor", enableHtmlEditor);

    
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
	 * Proper way to get UiPermissions from HttpSession
	 * 
	 * @param session
	 *            current HttpSession
	 * @return the current UiPermissions
	 */
	public static UiPermissions getUiPermissions(HttpSession session) {
		UiPermissions uip = (UiPermissions)session.getAttribute("uiPermissions");
		return uip;
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
