/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperConfig;

/**
 * config constants for WS
 * @author mchyzer
 *
 */
public class GrouperWsConfig {

	/**
	 * Get a Grouper configuration parameter.
	 * <pre class="eg">
	 * String wheel = GrouperConfig.getProperty("groups.wheel.group");
	 * </pre>
	 * @return  Value of configuration parameter or an empty string if
	 *   parameter is invalid.
	 * @since   1.1.0
	 */
	public static String getPropertyString(String property) {
	  return GrouperConfig.getProperty(property);
	}
	
	/**
	 * Get a Grouper configuration parameter.
	 * <pre class="eg">
	 * String wheel = GrouperConfig.getProperty("groups.wheel.group");
	 * </pre>
	 * @return  Value of configuration parameter or null if
	 *   parameter isnt specified.  Exception is thrown if not formatted correcly
	 * @throws NumberFormatException if cannot convert the value to an Integer
	 * @since   1.1.0
	 */
	public static Integer getPropertyInteger(String property, Integer defaultValue) {
	  String paramString = GrouperConfig.getProperty(property);
	  //see if not there
	  if (StringUtils.isEmpty(paramString)) {
		  return defaultValue;
	  }
	  //if there, convert to int
	  try {
		  Integer paramInteger = Integer.parseInt(paramString);
		  return paramInteger;
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
     * webservice.addMember.maxSubjects = 20000
	 *  
	 */
	public static final String WS_ADD_MEMBER_SUBJECTS_MAX = "ws.add.member.subjects.max";
	
	/**
	 * name of param
	 * 
	 * # Web service users who are in the following group can use the actAs field to act as someone else
	 * ws.act.as.group = aStem:aGroup
	 */
	public static final String WS_ACT_AS_GROUP = "ws.act.as.group";
	

	
}
