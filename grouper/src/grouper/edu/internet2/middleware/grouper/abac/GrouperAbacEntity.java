package edu.internet2.middleware.grouper.abac;

import java.util.Map;
import java.util.Set;

public class GrouperAbacEntity {

  private Map<String, String> singleValuedGroupExtensionInFolder = null;

  public Map<String, String> getSingleValuedGroupExtensionInFolder() {
    return singleValuedGroupExtensionInFolder;
  }

  
  
  
  // ${ entity.singleValuedEntityAttribute('personLdap', 'activeFlag') == 'T' }
  
  
  
  
  
  public void setSingleValuedGroupExtensionInFolder(Map<String, String> singleValuedGroupExtensionInFolder) {
    this.singleValuedGroupExtensionInFolder = singleValuedGroupExtensionInFolder;
  }

  
  // basis:affiliation:staff
  // basis:affiliation:student
  
  // ${ entity.multiValuedGroupExtensionInFolder('basis:affiliation').containsRegex('^(stu)|(fac).*$') }
  
  private Map<String, Set<String>> multiValuedGroupExtensionInFolder = null;

  public Map<String, Set<String>> multiValuedGroupExtensionInFolder(String folderName) {
    return multiValuedGroupExtensionInFolder;
  }

  public void setMultiValuedGroupExtensionInFolder(Map<String, Set<String>> multiValuedGroupExtensionInFolder) {
    this.multiValuedGroupExtensionInFolder = multiValuedGroupExtensionInFolder;
  }
  
  private Set<String> memberOfGroupNames;
  
  
  
  public void setMemberOfGroupNames(Set<String> memberOfGroupNames) {
    this.memberOfGroupNames = memberOfGroupNames;
  }

  public boolean memberOf(String groupName) {
    return this.memberOfGroupNames.contains(groupName);
  }
}
