/**
 * Copyright 2014 Internet2
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
 */
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader.ldap;

import java.text.Normalizer;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * loaded in EL context for loader el's
 */
public class LoaderLdapElUtils {

  /**
   * This takes a string of attribute=value and makes sure that special, dn-relevant characters
   * are escaped, particularly commas, pluses, etc
   * @param rdnString An RDN: attribute=value
   * @return
   */
  public static String ldapEscapeRdn(String rdnString) {
    return GrouperUtil.ldapEscapeRdn(rdnString);
  }

  /**
   * This takes a string of value and makes sure that special, dn-relevant characters
   * are escaped, particularly commas, pluses, etc
   * @param rdnString An RDN value: value
   * @return the escaped value
   */
  public static String ldapEscapeRdnValue(String rdnValue) {
    return GrouperUtil.ldapEscapeRdnValue(rdnValue);
  }

  /**
   * escape an ldap filter term
   * @param s
   * @return the filter
   */
  public static String ldapFilterEscape(String s) {
    return GrouperUtil.ldapFilterEscape(s);
  }


  public static void main(String[] args) {
    String someString = "abc&def";
    System.out.println(someString.replaceAll("[^a-zA-Z0-9_-]", "_"));
  }
  
  /**
   * take out accented chars e.g.
   * loaderLdapElUtils.normalize("NFD", groupAttribute).replaceAll("\\p{M}", "")
   * @param form
   * @param text
   * @return normalized text
   */
  public static String normalize(String form, String text) {
    if (text == null) {
      return text;
    }
    Normalizer.Form formEnum = Normalizer.Form.valueOf(form);
    return Normalizer.normalize(text, formEnum);

  }
  
  /**
   * convert from uid=someapp,ou=people,dc=myschool,dc=edu
   * to someapp
   * @param dn
   * @return the most specific value
   */
  public static String convertDnToSpecificValue(String dn) {
    return GrouperUtil.ldapConvertDnToSpecificValue(dn);
  }
  
  /**
   * convert from uid=someapp,ou=people,dc=myschool,dc=edu to someapp and allow groups
   * @param dn full dn
   * @param groupSuffix group suffix e.g. ,OU=Groups,DC=dev,DC=umontreal,DC=ca
   * @param groupPrefix group prefix e.g. umontreal:adgroups: 
   * if null then no prefix
   * @param createGroupIfNotThere if we should see if group exists and if not, create
   * @param idOrIdentifier true for Id (uuid), false for Identifier of group (name)
   * @return the subjectId or Identifier
   */
  public static String convertDnToSpecificValueOrGroup(String dn, String groupPrefix, 
      String groupSuffix, boolean createGroupIfNotThere, boolean idOrIdentifier) {

    //not sure why this would happen 
    if (dn == null) {
      return dn;
    }

    boolean isGroup = dn.toLowerCase().endsWith(groupSuffix.toLowerCase());
    
    if (isGroup) {
      String cn = dn.substring(0, dn.length() - groupSuffix.length());

      if (StringUtils.countMatches(cn, "=") != 1) {
        throw new RuntimeException("Why is there not 1 equals in this CN??? '" + cn + "'");
      }

      //this should be CN=groupName, convert to groupName 
      cn = GrouperUtil.prefixOrSuffix(cn, "=", false);

      String groupName = StringUtils.isBlank(groupPrefix) ? cn : (groupPrefix + cn);
      
      Group group = GrouperDAOFactory.getFactory().getGroup().findByName(groupName,
          false, null);

      if (createGroupIfNotThere) {
        if (group == null) {

          group = new GroupSave(GrouperSession.staticGrouperSession())
              .assignName(groupName).assignCreateParentStemsIfNotExist(true).save();
        }        
      }

      if (group == null) {
        
        LOG.error("Why is group null??? " + groupName);
        return null;
      }
      
      return idOrIdentifier ? group.getId() : group.getName();

    }
    //not a group 
    return LoaderLdapElUtils.convertDnToSpecificValue(dn);
  }
  
  /**
   * convert from uid=someapp,ou=people,dc=myschool,dc=edu
   * baseDn is edu
   * searchDn is myschool
   * to people:someapp
   * @param dn
   * @param baseDn if there is one, take it off
   * @param searchDn if there is one after the baseDn is off, take it off
   * @return the subpath
   */
  public static String convertDnToSubPath(String dn, String baseDn, String searchDn) {
    return GrouperUtil.ldapConvertDnToSubPath(dn, baseDn, searchDn);
  }

  /**
   * test case showing a transformation where if the dn is the subjectIdToReturnGroup then make sure groupToCreateReturn
   * exists and return its ID
   * @param dn
   * @param subjectIdToReturnGroup
   * @param groupToCreateReturn
   * @return the subject id or group id
   */
  public static String convertDnToSpecificValueTest(String dn, String subjectIdToReturnGroup, String groupToCreateReturn) {
    
    //not sure why this would happen
    if (dn == null) {
      return dn;
    }
   
    String cn = LoaderLdapElUtils.convertDnToSpecificValue(dn);
    
    if (StringUtils.equals(cn, subjectIdToReturnGroup)) {
      
      Group group = GrouperDAOFactory.getFactory().getGroup().findByName(groupToCreateReturn, false, null);
      
      if (group == null) {
        group = new GroupSave(GrouperSession.staticGrouperSession()).assignName(groupToCreateReturn).assignCreateParentStemsIfNotExist(true).save();
      }

      return group.getId();
    }

    //not a group
    return cn;
  }

  /**
   * cache for if cn is person or group
   * cn -> is Person; true = person, false = group, store in memory for 3 hours
   */
  private static GrouperCache<String, Boolean> cacheIsPerson = new GrouperCache<String, Boolean>("loaderLdapElUtilsCacheIsPerson", 
      10000, false, 60 * 3 * 60, 60 * 3 * 60, false);

  /**
   * cache for DN to group name
   * dn -> group ID store for 3 hours
   */
  private static GrouperCache<String, String> cacheDnToGroupName = new GrouperCache<String, String>("loaderLdapElUtilsCacheDnToGroupName", 
      10000, false, 60 * 3 * 60, 60 * 3 * 60, false);
  
  /** 
   * Logging infos
   */
  private static final Log LOG = GrouperUtil.getLog(LoaderLdapElUtils.class);
  
  /**
   * convert a user dn to a user CN, and a group dn to a group ID or Uuid
   * @param baseDn e.g. OU=People,DC=devsim,DC=umontreal,DC=ca
   * @param dn of group member
   * @param grouperBaseStem is the base stem where the groups go.  e.g. my:groups:
   * @param idOrIdentifier true for id (group id), false for identifier (group name)
   * @return CN for person, Uuid or Group name for a group
   */
  public static String convertAdMemberDnToSpecificValue(String dn, String baseDn, String grouperBaseStem, boolean idOrIdentifier) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Start conversion of DN '" + dn + "'");
    }
    
    if (StringUtils.isBlank(dn)) {
      return dn;
    }
    
    // see if the dn is in the space that is managed by grouper
    if (dn.indexOf(baseDn) < 0) {
      LOG.error("Group member isn't managed in Grouper : " + dn);
      return dn;  // this will cause an error since the subject cant be found...
    }
      
    // Get the CN/sAMAccountName from the DN
    String cn = LoaderLdapElUtils.convertDnToSpecificValue(dn);

    // see if it is a person or group
    Boolean isPerson = cacheIsPerson.get(cn);

    // ID is not cached, get the value from LDAP
    if (isPerson == null) {

      // Send a request to LDAP.  Note, in the future we can make this part of the original filter maybe
      List<String> results = LdapSessionUtils.ldapSession().list(String.class, "personLdap",
          "", null, "(&(sAMAccountName=" + cn +")(objectClass=person))","sAMAccountName");
      
      // If no results were found, it means it didn't pass through the (objectClass=person) filter
      isPerson = new Boolean(results.size() > 0);
      
      // Put the result in the cache
      cacheIsPerson.put(cn, isPerson);
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("Object not found in cache, result : " + cn + " = " + (isPerson ? "user" : "group"));
      }
      
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Object was found in cache, result : " + cn + " = " + (isPerson ? "user" : "group"));
      }
    }

    // if its a person return the cn (samaccountname)
    if (isPerson) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("DN: '" + dn + "' converted to CN '" + cn + "'");
      }      
      return cn;
    }
    
    // convert the DN to a grouper group
    String groupName = convertDnToGroupName(dn, baseDn, grouperBaseStem); 
    
    // see if the group exists
    Group group = GrouperDAOFactory.getFactory().getGroup().findByName(groupName, false, null) ;
    if (group == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Group doesnt exist, creating: '" + groupName +"'");
      }

      // If the group which is a member doesnt exist, create it
      //
      group = new GroupSave(GrouperSession.staticGrouperSession())
              .assignName(groupName)
              .assignCreateParentStemsIfNotExist(true)
              .save();
    }

  
    // return the ID of the group
    if (LOG.isDebugEnabled()) {
      LOG.debug("DN: '" + dn + "' converted to group: '" + group.getName() + "' and id: '" + group.getId() + "'");
    }
    
    return idOrIdentifier ? group.getId() : group.getName();
  }

  /**
   * convert a DN to a group name
   * @param dn 
   * @param baseDn e.g. OU=People,DC=devsim,DC=umontreal,DC=ca
   * @param grouperBaseStem is the base stem where the groups go.  e.g. my:groups:
   * @return the subject identifier (group name)
   */
  public static String convertDnToGroupName(String dn, String baseDn, String grouperBaseStem) {
    // see it is already in the cache
    String cachedGroupID = cacheDnToGroupName.get(dn);
    if (cachedGroupID != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("DN of groupe as in the cache cache, DN='" + dn + "' --> GroupID='" + cachedGroupID + "'");
      }
      return cachedGroupID;
    }
    
    // convert the dn of the grouper group to group name in grouper :
    // from : CN=dgtic-dev-deleg,OU=Groupes,OU=dgtic,OU=People,DC=devsim,DC=umontreal,DC=ca
    // to   : udem:dgtic:Groupes:dgtic-dev-deleg
    
    // take out the part of the name which is the baseDn and not part of grouper
    String partDn = dn.substring(0, dn.indexOf(baseDn));
    String[] splitDn = GrouperUtil.splitTrim(partDn, ",");

    // convert the rest of the bushy group part of dn to the group name
    StringBuilder groupName = new StringBuilder();
    for (String element : splitDn) {
      if (element.indexOf('=') >= 0) {  // Avoid empty elements (e.g. if you have a ',' at the end)
        groupName.insert(0, element.substring(element.indexOf('=') + 1)).insert(0,':');
      }
    }
    
    // add the base stem if it is there
    if (!StringUtils.isBlank(grouperBaseStem)) {
      groupName.insert(0, grouperBaseStem);
    } else {
      //if it starts with a colon, delete it
      if (groupName.charAt(0) == ':') {
        groupName.deleteCharAt(0);
      }
    }
    
    cacheDnToGroupName.put(dn, groupName.toString());
    return groupName.toString();
  }

  /**
   * convert a subject dn to a user RDN value, and a group dn to a group ID or Uuid
   * @param dn of group member . Normally this will be as subjectId
   * @param baseOu This is the full DN of the OU to load from LDAP e.g. OU=Some Example,DC=test,DC=school,DC=edu 
   * @param grouperBaseStem is the base stem where the groups are loaded into.  e.g. my:groups:Loaded_from_LDAP
   * @param serverId is the connection or server ID specified in grouper-loader.properties file. e.g: personLdap
   * @param uniqueAttribute attribute value to return from LDAP query, preferably the one storing the subjectId
   * @param idOrIdentifier true for id (group id), false for identifier (group name)
   * @return RDN value for subject (user object), Uuid or Group name for a group
   */
  public static String convertLDAPMemberDnToSubjectIdOrGroup(String dn, String baseOu, String grouperBaseStem, String serverId, String uniqueAttribute, boolean idOrIdentifier) {
    
    // Not really needed as dn shouldn't be blank, but it is here
    // to avoid having to go through the rest of the code in case blank
    // is ever provided as an argument
    if (StringUtils.isBlank(dn)) {
      return dn;
    }
  
    if (LOG.isDebugEnabled()) {
      LOG.debug("Start conversion of DN '" + dn + "'");
    }
  
    // get the domain suffix, RDN attribute and RDN value from the DN
    String domainSuffix = dn.substring(dn.toLowerCase().indexOf("dc="));
    String rdnAttribute = StringUtils.substringBefore(dn,"=");
    String rdnValue = LoaderLdapElUtils.convertDnToSpecificValue(dn);
    
    // searchBase is member DN minus suffix (domainSuffix) minus trailing comma
    // This has to be done since LdapSession.list appends LDAP suffix to search base values
    String searchBase = StringUtils.chop(StringUtils.removeEnd(dn.toLowerCase(),domainSuffix.toLowerCase()));
    
    // Forward slash is a special character in JNDI. In case the dn contains one, this addresses it.
    // This may no longer be needed with patched versions of grouper. 
    if (dn.indexOf('/') > 0 ) {
      searchBase = "" ;
    }

    // check if it is a person
    Boolean isPerson = cacheIsPerson.get(rdnValue);

    // ID is not cached, get the value from LDAP
    if (isPerson == null) {

      // Send a request to LDAP.  Note, in the future we can make this part of the original filter maybe
      List<String> results = LdapSessionUtils.ldapSession().list(String.class,serverId,searchBase, null,"(&(objectClass=person)(" + uniqueAttribute + "=*)(" + rdnAttribute + "=" + rdnValue + "))",uniqueAttribute);

      // Put it in cache if there is a result
      isPerson = new Boolean(results.size() > 0);
      cacheIsPerson.put(rdnValue, isPerson);
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("Object not found in cache, result : " + rdnValue + " = " + (isPerson ? "user" : "group"));
      }
      
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Object was found in cache, result : " + rdnValue + " = " + (isPerson ? "user" : "group"));
      }
    }

    // if it is a person AND has uniqueAttribute, return the rdn
    if (isPerson) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("DN: '" + dn + "' converted to RDN '" + rdnValue + "'");
      }      
      return rdnValue;
    }
    // dn is not a person or is a person object that does not have a uniqueAttribute value (e.g: computer, etc..)
    else {
      // perform another LDAP query to make sure it is actually a group. Return the RDN in query
      // "(|(objectClass=group)(objectclass=*group*))" filter should match grouperOfNames, grouperOfUniqueNames objectclasses
      // (objectclass=*group*) alone did not work with AD, thus the redundancy of the filter
      List<String> groupresults = LdapSessionUtils.ldapSession().list(String.class,serverId,searchBase, null,"(&(|(objectClass=group)(objectclass=*group*))(" + rdnAttribute + "=" + rdnValue + "))",rdnAttribute);
      Boolean isGroup = new Boolean(groupresults.size() > 0);
    
      if (isGroup) {
      // Check that the member object is within the baseOu space
      if ( dn.toLowerCase().indexOf(baseOu.toLowerCase()) > 0 ) {
        // convert the DN to a grouper group
        String groupName = convertDnToGroupName(dn, domainSuffix, grouperBaseStem); 
      
        // see if the group exists
        Group group = GrouperDAOFactory.getFactory().getGroup().findByName(groupName, false, null) ;
        if (group == null) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Group doesnt exist, creating: '" + groupName +"'");
          }
  
        // If the group which is a member doesnt exist, create it
        group = new GroupSave(GrouperSession.staticGrouperSession())
                .assignName(groupName)
                .assignCreateParentStemsIfNotExist(true)
                .save();
        }
  
        // return the ID of the group
        if (LOG.isDebugEnabled()) {
          LOG.debug("DN: '" + dn + "' converted to group: '" + group.getName() + "' and id: '" + group.getId() + "'");
        }
      
        return idOrIdentifier ? group.getId() : group.getName();
        
      } 
      else {
        // member object is not within the baseOu space.
        // Future code should perhaps address this but for now, return dn.
        return dn;
      }
      }
      // object is NOT a group OR is person object that does not have a uniqueAttribute value (e.g: computer). Return dn
      else {
        return dn;
      }
    }
  }
  
}
