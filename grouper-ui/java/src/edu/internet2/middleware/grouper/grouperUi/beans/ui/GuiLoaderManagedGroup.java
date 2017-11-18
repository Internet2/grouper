package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import static edu.internet2.middleware.grouper.misc.GrouperCheckConfig.loaderMetadataStemName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author vsachdeva
 *
 */
public class GuiLoaderManagedGroup {
  
  /** logger */
  //private static final Log LOG = GrouperUtil.getLog(GuiLoaderManagedGroup.class);
  
  private boolean grouperLoaderMetadataLoaded;
  private String grouperLoaderMetadataGroupId;
  private String grouperLoaderMetadataLastFullMillisSince1970;
  private String grouperLoaderMetadataLastIncrementalMillisSince1970;
  private String grouperLoaderMetadataLastSummary;
  private GuiGroup guiGroup;
  
  public GuiLoaderManagedGroup(GuiGroup guiGroup, boolean grouperLoaderMetadataLoaded, String grouperLoaderMetadataGroupId, 
      String grouperLoaderMetadataLastFullMillisSince1970, String grouperLoaderMetadataLastIncrementalMillisSince1970, 
      String grouperLoaderMetadataLastSummary) {
    
    this.guiGroup = guiGroup;
    this.grouperLoaderMetadataLoaded = grouperLoaderMetadataLoaded;
    this.grouperLoaderMetadataGroupId = grouperLoaderMetadataGroupId;
    this.grouperLoaderMetadataLastFullMillisSince1970 = grouperLoaderMetadataLastFullMillisSince1970;
    this.grouperLoaderMetadataLastIncrementalMillisSince1970 = grouperLoaderMetadataLastIncrementalMillisSince1970;
    this.grouperLoaderMetadataLastSummary = grouperLoaderMetadataLastSummary;
    
  }
  
  /**
   * convert groups into gui loader managed groups
   * @param groups
   * @param attributeAssignValueFinderResult
   * @return the list of gui loader managed groups
   */
  public static List<GuiLoaderManagedGroup> convertGroupIntoGuiAttestation(
      Set<Group> groups,
      AttributeAssignValueFinderResult attributeAssignValueFinderResult) {
    
    List<GuiLoaderManagedGroup> guiLoaderManagedGroups = new ArrayList<GuiLoaderManagedGroup>();
    
    //now we have groups, assignments, assignments on assignments, and values
    for (Group group : groups) {
      
      Map<String, String> attributes = attributeAssignValueFinderResult.retrieveAttributeDefNamesAndValueStrings(group.getId());
      
      String lastFullMillisKey = loaderMetadataStemName()+":"+GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_LAST_FULL_MILLIS;
      String lastIncrementalMillisKey = loaderMetadataStemName()+":"+GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_LAST_INCREMENTAL_MILLIS;
      String summaryKey = loaderMetadataStemName()+":"+GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_LAST_SUMMARY;
      String groupIdKey = loaderMetadataStemName()+":"+GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_GROUP_ID;
      String grouperLoaderMetadataLoadedKey = loaderMetadataStemName()+":"+GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_LAODED;
      
      String summaryLoaderPerGroup = attributes.get(summaryKey);
      Long lastFullMillisLoaded = GrouperUtil.longObjectValue(attributes.get(lastFullMillisKey), true);
      Long lastIncrementalMillisLoaded = GrouperUtil.longObjectValue(attributes.get(lastIncrementalMillisKey), true);
      String groupId = attributes.get(groupIdKey);
      String grouperLoaderMetadataLoaded = attributes.get(grouperLoaderMetadataLoadedKey);
      
      GuiLoaderManagedGroup guiLoaderManagedGroup = new GuiLoaderManagedGroup(new GuiGroup(group), GrouperUtil.booleanObjectValue(grouperLoaderMetadataLoaded), 
          groupId,
          lastFullMillisLoaded == null ? null: new Date(lastFullMillisLoaded).toString(),
          lastIncrementalMillisLoaded == null ? null: new Date(lastIncrementalMillisLoaded).toString(),
          summaryLoaderPerGroup);
  
      guiLoaderManagedGroups.add(guiLoaderManagedGroup);
      
    }
    return guiLoaderManagedGroups;
  }

  public boolean isGrouperLoaderMetadataLoaded() {
    return grouperLoaderMetadataLoaded;
  }
  
  public String getGrouperLoaderMetadataGroupId() {
    return grouperLoaderMetadataGroupId;
  }

  public String getGrouperLoaderMetadataLastFullMillisSince1970() {
    return grouperLoaderMetadataLastFullMillisSince1970;
  }

  public String getGrouperLoaderMetadataLastIncrementalMillisSince1970() {
    return grouperLoaderMetadataLastIncrementalMillisSince1970;
  }

  public String getGrouperLoaderMetadataLastSummary() {
    return grouperLoaderMetadataLastSummary;
  }

  public GuiGroup getGuiGroup() {
    return guiGroup;
  }
  
}
