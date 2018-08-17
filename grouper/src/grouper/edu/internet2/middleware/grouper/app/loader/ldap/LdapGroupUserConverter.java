/**
 * Copyright 2018 Internet2
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

package edu.internet2.middleware.grouper.app.loader.ldap;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * This is an example of a converter for DN to user or group.  see
 * 
 * https://bugs.internet2.edu/jira/browse/GRP-1354
 * 
 * Convert a DN to a subjectId if its a person or to a groupName in Grouper if a group
 */
public class LdapGroupUserConverter {

  /**
   * 
   */
  public LdapGroupUserConverter() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    String equalsValuePart = equalsValuePart("CN=ABCDEF");
    
    if (!"ABCDEF".equals(equalsValuePart)) {
      throw new RuntimeException("Expecting ABCDEF as the equals value part but got: '" + equalsValuePart + "'");
    }
    
    String subjectId = convertDntoSubjectIdOrIdentifier("CN=JNWHITWO,OU=Users,OU=ITS-23101,OU=UNIT-InformationTechnologyServices-2D031,OU=COLL-InformationTechnologyServices-02C01,OU=DIV-InformationTechnologyServices-DIV02,OU=FACSTAFF,DC=campus,DC=uncg,DC=edu");
    
    if (!"jnwhitwo".equals(subjectId)) {
      throw new RuntimeException("Expecting jnwhitwo but got: '" + subjectId + "'");
    }
    
    subjectId = convertDntoSubjectIdOrIdentifier("CN=ITS-23101-SYN-Identity_Architecture,OU=Groups,OU=ITS-23101,OU=UNIT-InformationTechnologyServices-2D031,OU=COLL-InformationTechnologyServices-02C01,OU=DIV-InformationTechnologyServices-DIV02,OU=FACSTAFF,DC=campus,DC=uncg,DC=edu");

    if (!"uncg:facstaff:ITS-23101:ITS-23101-SYN-Identity_Architecture".equals(subjectId)) {
      throw new RuntimeException("Expecting uncg:facstaff:ITS-23101:ITS-23101-SYN-Identity_Architecture but got: '" + subjectId + "'");
    }

    System.out.println("Success");
  }

  /** logger */
  final static Logger LOG = Logger.getLogger(LdapGroupUserConverter.class);
  
  /**
   * convert dn to subject identifier or group name, log the result
   * @param dn
   * @return the subject identifier or group name
   */
  public static String convertDntoSubjectIdOrIdentifier(String dn) {
    String result = convertDntoSubjectIdOrIdentifierHelper(dn);
    LOG.debug("Converting dn '" + dn + "' to: '" + result + "'");
    return result;
  }
  
  /**
   * convert dn to subject identifier or group name, log the result
   * @param dn
   * @return the subject identifier or group name
   */
  private static String convertDntoSubjectIdOrIdentifierHelper(String dn) {

    if (dn == null || "".equals(dn.trim())) {
      return dn;
    }
    String[] partsArray = dn.split(",");
    
    List<String> partsList = new ArrayList<String>();
    
    for (String string : partsArray) {
      partsList.add(string);
    }
    
    //get a user: CN=JNWHITWO,OU=Users
    if (partsList.size() >= 2 && "OU=Users".equals(partsList.get(1))) {
      String firstPart = partsList.get(0);
      if (firstPart != null && firstPart.startsWith("CN=")) {
        return equalsValuePart(firstPart).toLowerCase();
      }
    }

    //get a group:
    // From:  CN=ITS-23101-SYN-Identity_Architecture,OU=Groups,OU=ITS-23101,OU=UNIT-InformationTechnologyServices-2D031,OU=COLL-InformationTechnologyServices-02C01,OU=DIV-InformationTechnologyServices-DIV02,OU=FACSTAFF,DC=campus,DC=uncg,DC=edu

    //CN=ITS-23101-SYN-Identity_Architecture,
    //OU=Groups,
    //OU=ITS-23101,
    //OU=UNIT-InformationTechnologyServices-2D031,
    //OU=COLL-InformationTechnologyServices-02C01,
    //OU=DIV-InformationTechnologyServices-DIV02,
    //OU=FACSTAFF,
    //DC=campus,
    //DC=uncg,
    //DC=edu
    
    // To:    uncg:facstaff:ITS-23101:ITS-23101-SYN-Identity_Architecture
    if (partsList.size() >= 2 && "OU=Groups".equals(partsList.get(1))) {
      
      StringBuilder groupName = new StringBuilder();
      groupName.insert(0, equalsValuePart(partsList.get(0)));
      groupName.insert(0, ":");
      
      //remove extension
      partsList.remove(0);
      
      //remove ou=groups
      partsList.remove(0);
      
      if (partsList.size() > 0) {
        
        groupName.insert(0, equalsValuePart(partsList.get(0)));
        groupName.insert(0, ":");
        partsList.remove(0);
      }

      //go until facstaff
      while(true) {
        if (partsList.size() >= 2 && partsList.get(1) != null && "dc=campus".equals(partsList.get(1).toLowerCase()) ) {
          groupName.insert(0, equalsValuePart(partsList.get(0)).toLowerCase());
          groupName.insert(0, ":");
          partsList.remove(0);
          break;
        }
        partsList.remove(0);
      }
      
      if (partsList.size() > 2) {
        //get rid of campus
        partsList.remove(0);
  
        //we are left with uncg
        groupName.insert(0, equalsValuePart(partsList.get(0)).toLowerCase());
        
        return groupName.toString();
      }
      
    }

    return dn;
  }
  
  /**
   * convert AB=whatever  to   whatever
   * @param string
   * @return the "whatever" value
   */
  public static String equalsValuePart(String string) {
    
    if (string == null) {
      return string;
    }
    
    int equalsIndex = string.indexOf('=');
    
    if (equalsIndex < 0) {
      return string;
    }

    return string.substring(equalsIndex+1, string.length());
    
  }
  
}

