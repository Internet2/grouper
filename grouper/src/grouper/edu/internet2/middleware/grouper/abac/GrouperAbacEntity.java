package edu.internet2.middleware.grouper.abac;

import java.util.Map;
import java.util.Set;

public class GrouperAbacEntity {

  private Map<String, String> singleValuedGroupExtensionInFolder = null;

  public Map<String, String> getSingleValuedGroupExtensionInFolder() {
    return singleValuedGroupExtensionInFolder;
  }

  public void setSingleValuedGroupExtensionInFolder(Map<String, String> singleValuedGroupExtensionInFolder) {
    this.singleValuedGroupExtensionInFolder = singleValuedGroupExtensionInFolder;
  }

  private Map<String, Set<String>> multiValuedGroupExtensionInFolder = null;

  public Map<String, Set<String>> getMultiValuedGroupExtensionInFolder() {
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
