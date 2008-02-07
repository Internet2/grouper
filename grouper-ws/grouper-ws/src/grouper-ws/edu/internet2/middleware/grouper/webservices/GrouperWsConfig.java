/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.PropertiesConfiguration;

/**
 * config constants for WS
 * @author mchyzer
 *
 */
public class GrouperWsConfig {

	/**
	 * cache the properties configuration
	 */
	private static PropertiesConfiguration propertiesConfiguration = null;

	/**
	 * lazy load and cache the properties configuration
	 * @return the properties configuration
	 */
	private static PropertiesConfiguration retrievePropertiesConfiguration() {
		if (propertiesConfiguration == null) {
			propertiesConfiguration = new PropertiesConfiguration("/grouper-ws.properties");
		}
		return propertiesConfiguration;

	}
	
	/**
	 * Get a Grouper configuration parameter.
	 * <pre class="eg">
	 * String wheel = GrouperConfig.getProperty("groups.wheel.group");
	 * </pre>
	 * @param property 
	 * @return  Value of configuration parameter or an empty string if
	 *   parameter is invalid.
	 * @since   1.1.0
	 */
	public static String getPropertyString(String property) {
	  return retrievePropertiesConfiguration().getProperty(property);
	}
	
	/**
	 * Get a Grouper configuration parameter an integer
	 * @param property 
	 * @param defaultValue 
	 * @return  Value of configuration parameter or null if
	 *   parameter isnt specified.  Exception is thrown if not formatted correcly
	 * @throws NumberFormatException if cannot convert the value to an Integer
	 */
	public static int getPropertyInt(String property, int defaultValue) {
	  String paramString = getPropertyString(property);
	  //see if not there
	  if (StringUtils.isEmpty(paramString)) {
		  return defaultValue;
	  }
	  //if there, convert to int
	  try {
		  int paramInteger = Integer.parseInt(paramString);
		  return paramInteger;
	  } catch (NumberFormatException nfe) {
		  throw new NumberFormatException(
				  "Cannot convert the grouper.properties param: " + property 
				  + " to an Integer.  Config value is '" + paramString + "' " + nfe);
	  } 
	}
	
	/**
	 * Get a Grouper configuration parameter as boolean (must be true|t|false|f case-insensitive)
	 * 
	 * @param property 
	 * @param defaultValue 
	 * @return  Value of configuration parameter or null if
	 *   parameter isnt specified.  Exception is thrown if not formatted correcly
	 * @throws NumberFormatException if cannot convert the value to an Integer
	 */
	public static boolean getPropertyBoolean(String property, boolean defaultValue) {
	  String paramString = getPropertyString(property);
	  //see if not there
	  if (StringUtils.isEmpty(paramString)) {
		  return defaultValue;
	  }
	  //if there, convert to boolean
	  try {
		  //note, cant be blank at this point, so default value doesnt matter
		  boolean paramBoolean = GrouperServiceUtils.booleanValue(property);
		  return paramBoolean;
	  } catch (NumberFormatException nfe) {
		  throw new NumberFormatException(
				  "Cannot convert the grouper.properties param: " + property 
				  + " to an Integer.  Config value is '" + paramString + "' " + nfe);
	  } 
	}

	/** 
	 * name of param for add member web service max, default is 1000000
	 * 
	 * # Max number of subjects to be able to pass to addMember service, default is 1000000
     * ws.add.member.subjects.max = 20000
	 *  
	 */
	public static final String WS_ADD_MEMBER_SUBJECTS_MAX = "ws.add.member.subjects.max";
	
	/** 
	 * name of param for group delete, max groups to be able to delete at once, default is 1000000
	 * 
	 * # Max number of subjects to be able to pass to addMember service, default is 1000000
     * ws.group.delete.max = 20000
	 *  
	 */
	public static final String WS_GROUP_DELETE_MAX = "ws.group.delete.max";

	/** 
	 * name of param for delete member web service max, default is 1000000
	 * 
	 * # Max number of subjects to be able to pass to deleteMember service, default is 1000000
     * ws.add.member.subjects.max = 20000
	 *  
	 */
	public static final String WS_DELETE_MEMBER_SUBJECTS_MAX = "ws.delete.member.subjects.max";
	
	/** 
	 * name of param for has member web service max, default is 1000000
	 * 
	 * # Max number of subjects to be able to pass to addMember service, default is 1000000
     * ws.has.member.subjects.max = 20000
	 */
	public static final String WS_HAS_MEMBER_SUBJECTS_MAX = "ws.has.member.subjects.max";
	
	/**
	 * name of param
	 * 
	 * # Web service users who are in the following group can use the actAs field to act as someone else
	 * ws.act.as.group = aStem:aGroup
	 */
	public static final String WS_ACT_AS_GROUP = "ws.act.as.group";
	
	/** to ship members attributes back to the web service client, put the subject attribute 0 name here, e.g. subject.netid */
	public static final String WS_GET_MEMBERS_ATTRIBUTE0 = "ws.get.members.attribute0";

	/** to ship members attributes back to the web service client, put the subject attribute 0 name here, e.g. subject.netid */
	public static final String WS_GET_MEMBERS_ATTRIBUTE1 = "ws.get.members.attribute1";

	/** to ship members attributes back to the web service client, put the subject attribute 0 name here, e.g. subject.netid */
	public static final String WS_GET_MEMBERS_ATTRIBUTE2 = "ws.get.members.attribute2";

	/** to ship memberships attributes back to the web service client, put the attribute 0 name here, e.g. subject.netid */
	public static final String WS_GET_MEMBERSHIPS_ATTRIBUTE0 = "ws.get.memberships.attribute0";

	/** to ship memberships attributes back to the web service client, put the attribute 0 name here, e.g. subject.netid */
	public static final String WS_GET_MEMBERSHIPS_ATTRIBUTE1 = "ws.get.memberships.attribute1";

	/** to ship memberships attributes back to the web service client, put the attribute 0 name here, e.g. subject.netid */
	public static final String WS_GET_MEMBERSHIPS_ATTRIBUTE2 = "ws.get.memberships.attribute2";

	
}
