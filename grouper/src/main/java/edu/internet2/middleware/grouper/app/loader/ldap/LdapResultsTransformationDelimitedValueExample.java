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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.ldap.LdapResultsTransformationBase;
import edu.internet2.middleware.grouper.app.loader.ldap.LdapResultsTransformationInput;
import edu.internet2.middleware.grouper.app.loader.ldap.LdapResultsTransformationOutput;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Example where LDAP stores values that are delimited by a character for group names.
 * @author shilen
 */
public class LdapResultsTransformationDelimitedValueExample extends LdapResultsTransformationBase {

  @Override
  public LdapResultsTransformationOutput transformResults(LdapResultsTransformationInput ldapResultsTransformationInput) {
    Map<String, List<String>> newMembershipResults = new LinkedHashMap<String, List<String>>();
    Map<String, String> newGroupNameToDisplayName = new LinkedHashMap<String, String>();
    Map<String, String> newGroupNameToDescription = new LinkedHashMap<String, String>();

    String delimiter = GrouperLoaderConfig.retrieveConfig().propertyValueString("loader.ldap.resultsTransformationDelimitedValueExampleDelimiter", "-");
    
    for (String originalGroupName : ldapResultsTransformationInput.getMembershipResults().keySet()) {
      String stemName = GrouperUtil.parentStemNameFromName(originalGroupName);
      String oldGroupExtension = GrouperUtil.extensionFromName(originalGroupName);
      
      for (String newGroupExtension : oldGroupExtension.split(Pattern.quote(delimiter))) {
        String newGroupName = stemName + ":" + newGroupExtension;
        if (!newMembershipResults.containsKey(newGroupName)) {
          newMembershipResults.put(newGroupName, new ArrayList<String>());
          
          //example to manage description and display name
          //newGroupNameToDisplayName.put(newGroupName, "My displayExtension - " + newGroupExtension.toUpperCase());
          //newGroupNameToDescription.put(newGroupName, "My description - " + newGroupExtension.toUpperCase());
        }
        
        newMembershipResults.get(newGroupName).addAll(ldapResultsTransformationInput.getMembershipResults().get(originalGroupName));
      }
    }   
    
    LdapResultsTransformationOutput ldapResultsTransformationOutput = new LdapResultsTransformationOutput()
        .setGroupNameToDescription(newGroupNameToDescription)
        .setGroupNameToDisplayName(newGroupNameToDisplayName)
        .setMembershipResults(newMembershipResults);
    
    return ldapResultsTransformationOutput;
  }
}