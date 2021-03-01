package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperProvisioningObjectMetadata {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningObjectMetadata.class);

  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * list of metadata items for this metadata object
   */
  private List<GrouperProvisioningObjectMetadataItem> grouperProvisioningObjectMetadataItems = new ArrayList<GrouperProvisioningObjectMetadataItem>();

  /**
   * cache patterns since there will only be a few of them, return groupExtension|groupName|folderExtension|folderName, boolean for if matches,  and regex
   */
  private static Map<String, MultiKey> regexConfigToComponentIfMatchesAndRegex = new HashMap<String, MultiKey>();
  
  /**
   * component, if matches, and the regex
   */
  private static Pattern regexConfigPattern = Pattern.compile("^\\s*(folderExtension|folderName|groupExtension|groupName)\\s+(matches|not\\s+matches)\\s+(.+)$");
  
  /**
   * If you want to filter for groups in this provisionable folder by a regex on its name, specify here.  If the regex matches then the group is provisionable.  
   * If using 'not matches' then it will filter groups.  e.g.<br />folderExtension matches ^.*_someExtension$<br />folderName not matches ^.*_someExtension$<br />
   * groupExtension matches ^.*_someExtension$<br />groupName not matches ^.*_someExtension$
   * @param groupName
   * @param whichNameIfMatchesRegex folderName not matches ^.*_someExtension$
   * @return true if matches, false if not
   */
  public static boolean groupNameMatchesRegex(String groupName, String whichNameIfMatchesRegex) {
    
    MultiKey componentIfMatchesAndRegex = regexConfigToComponentIfMatchesAndRegex.get(whichNameIfMatchesRegex);
    if (componentIfMatchesAndRegex == null) {
      String component = null;
      boolean matches = false;
      Pattern regex = null;
      
      Matcher matcher = regexConfigPattern.matcher(whichNameIfMatchesRegex);
      if (!matcher.matches()) {
        throw new RuntimeException("Cant parse regex string: '" + whichNameIfMatchesRegex + "', '" + regexConfigPattern.pattern() + "'");
      }
      component = matcher.group(1);
      matches = "matches".equals(matcher.group(2));
      try {
        regex = Pattern.compile(GrouperUtil.trim(matcher.group(3)));
      } catch (RuntimeException re) {
        GrouperUtil.injectInException(re, "Cant parse regex string: '" + whichNameIfMatchesRegex + "', '" + regexConfigPattern.pattern() + "'");
        throw re;
      }

      synchronized(GrouperProvisioningObjectMetadata.class) {
        componentIfMatchesAndRegex = regexConfigToComponentIfMatchesAndRegex.get(whichNameIfMatchesRegex);
        if (componentIfMatchesAndRegex == null) {
          
          componentIfMatchesAndRegex = new MultiKey(component, matches, regex);
          regexConfigToComponentIfMatchesAndRegex.put(whichNameIfMatchesRegex, componentIfMatchesAndRegex);
        }
      }
    }
    
    String componentName = (String)componentIfMatchesAndRegex.getKey(0);
    boolean ifMatches = (Boolean)componentIfMatchesAndRegex.getKey(1);
    Pattern regex = (Pattern)componentIfMatchesAndRegex.getKey(2);
    
    String componentValue = null;
    if ("groupName".equals(componentName)) {
      componentValue = groupName;
    } else if ("groupExtension".equals(componentName)) {
      componentValue = GrouperUtil.extensionFromName(groupName);
    } else if ("folderName".equals(componentName)) {
      componentValue = GrouperUtil.parentStemNameFromName(groupName, false);
    } else if ("folderExtension".equals(componentName)) {
      componentValue = GrouperUtil.extensionFromName(GrouperUtil.parentStemNameFromName(groupName, false));
    } else {
      throw new RuntimeException("Invalid component name '" + componentName + "', expecting: groupName, groupExtension, folderName, or folderExtension");
    }

    boolean matches = regex.matcher(componentValue).matches();
    return ifMatches == matches;
  }
  
  /**
   * see if a metadata item already exists
   * @param name
   * @return true if exists
   */
  public boolean containsMetadataItemByName(String name) {
    for (GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem : GrouperUtil.nonNull(this.grouperProvisioningObjectMetadataItems)) {
      if (StringUtils.equals(name, grouperProvisioningObjectMetadataItem.getName())) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * init built in metadata after the configuration and behaviors are set
   */
  public void initBuiltInMetadata() {
    
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isAllowPolicyGroupOverride() && !this.containsMetadataItemByName("md_grouper_allowPolicyGroupOverride")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();
      grouperProvisioningObjectMetadataItem.setDefaultValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isOnlyProvisionPolicyGroups() ? "true" : "false");
      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningObjectMetadataProvisionOnlyPolicyGroupsDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningObjectMetadataProvisionOnlyPolicyGroupsLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_allowPolicyGroupOverride");
      grouperProvisioningObjectMetadataItem.setShowForFolder(true);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.BOOLEAN);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.TEXT);
      this.grouperProvisioningObjectMetadataItems.add(grouperProvisioningObjectMetadataItem);
    }
    
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isAllowProvisionableRegexOverride() && !this.containsMetadataItemByName("md_grouper_allowProvisionableRegexOverride")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();
      grouperProvisioningObjectMetadataItem.setDefaultValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getProvisionableRegex());
      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningObjectMetadataProvisionableRegexDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningObjectMetadataProvisionableRegexLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_allowProvisionableRegexOverride");
      grouperProvisioningObjectMetadataItem.setShowForFolder(true);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.TEXT);
      this.grouperProvisioningObjectMetadataItems.add(grouperProvisioningObjectMetadataItem);
    }
            
  }
  
  /**
   * reference back up to the provisioner
   * @return the provisioner
   */
  public GrouperProvisioner getGrouperProvisioner() {
    return this.grouperProvisioner;
  }

  /**
   * reference back up to the provisioner
   * @param grouperProvisioner1
   */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }
  
  /**
   * list of metadata items for this metadata object
   * @return
   */
  public List<GrouperProvisioningObjectMetadataItem> getGrouperProvisioningObjectMetadataItems() {
    return grouperProvisioningObjectMetadataItems;
  }

  /**
   * append metadata items from config
   * @param grouperProvisioningObjectMetadataItems
   */
  public void appendMetadataItemsFromConfig(Collection<GrouperProvisioningObjectMetadataItem> grouperProvisioningObjectMetadataItems) {
    this.grouperProvisioningObjectMetadataItems.addAll(grouperProvisioningObjectMetadataItems);
  }
  
  /**
   * return an error message if the value is wrong
   * @param name
   * @param value
   * @return the name and error message
   */
  public Map<String, String> validateMetadataInputForFolder(Map<String, Object> nameToValueFromUsersInput) {
    
    Map<String, String> errorMessages = new LinkedHashMap<String, String>();

    if (nameToValueFromUsersInput != null) {
      
      String allowProvisionableRegexOverride = (String)nameToValueFromUsersInput.get("md_grouper_allowProvisionableRegexOverride");
      if (!StringUtils.isBlank(allowProvisionableRegexOverride)) {
        try {
          groupNameMatchesRegex("a:a", allowProvisionableRegexOverride);
          // good
        } catch (Exception e) {
          // bad
          errorMessages.put("md_grouper_allowProvisionableRegexOverride", GrouperTextContainer.textOrNull("grouperProvisioningObjectMetadataProvisionableRegexError"));
        }
      }
      
    }
    
    return errorMessages;
  }
  
  /**
   * return an error message if the value is wrong
   * @param name
   * @param value
   * @return the name and error message
   */
  public Map<String, String> validateMetadataInputForGroup(Map<String, Object> nameToValueFromUsersInput) {
    return null;
  }
  
  /**
   * return an error message if the value is wrong
   * @param name
   * @param value
   * @return the name and error message
   */
  public Map<String, String> validateMetadataInputForMember(Map<String, Object> nameToValueFromUsersInput) {
    return null;
  }
  
  /**
   * return an error message if the value is wrong
   * @param name
   * @param value
   * @return the name and error message
   */
  public Map<String, String> validateMetadataInputForMembership(Map<String, Object> nameToValueFromUsersInput) {
    return null;
  }
  
  
}
