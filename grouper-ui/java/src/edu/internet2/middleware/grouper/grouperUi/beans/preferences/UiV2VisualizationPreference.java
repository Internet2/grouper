/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.preferences;


import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.lang.StringUtils;

/**
 * bean to serialize options from stored preferences
 */
public class UiV2VisualizationPreference {

  private String drawModule = "d3";
  private String drawObjectNameType = "displayExtension";
  private long drawNumParentsLevels = -1;
  private long drawNumChildrenLevels = -1;
  private long drawMaxSiblings = 50;
  private boolean drawShowStems = true;
  private boolean drawShowLoaders = true;
  private boolean drawShowProvisioners = true;
  private boolean drawShowAllMemberCounts = true;
  private boolean drawShowDirectMemberCounts = true;
  private boolean drawShowObjectTypes = false;
  private boolean drawIncludeGroupsInMemberCounts = false;
  private boolean drawShowLegend = true;

  public UiV2VisualizationPreference() {
  }

  public String getDrawModule() {
    return drawModule;
  }

  public void setDrawModule(String drawModule) {
    this.drawModule = drawModule;
  }

  public String getDrawObjectNameType() {
    return drawObjectNameType;
  }

  public void setDrawObjectNameType(String drawObjectNameType) {
    this.drawObjectNameType = drawObjectNameType;
  }

  public long getDrawNumParentsLevels() {
    return drawNumParentsLevels;
  }

  public void setDrawNumParentsLevels(long drawNumParentsLevels) {
    this.drawNumParentsLevels = drawNumParentsLevels;
  }

  public long getDrawNumChildrenLevels() {
    return drawNumChildrenLevels;
  }

  public void setDrawNumChildrenLevels(long drawNumChildrenLevels) {
    this.drawNumChildrenLevels = drawNumChildrenLevels;
  }

  public long getDrawMaxSiblings() {
    return drawMaxSiblings;
  }

  public void setDrawMaxSiblings(long drawMaxSiblings) {
    this.drawMaxSiblings = drawMaxSiblings;
  }

  public boolean isDrawShowStems() {
    return drawShowStems;
  }

  public void setDrawShowStems(boolean drawShowStems) {
    this.drawShowStems = drawShowStems;
  }

  public boolean isDrawShowLoaders() {
    return drawShowLoaders;
  }

  public void setDrawShowLoaders(boolean drawShowLoaders) {
    this.drawShowLoaders = drawShowLoaders;
  }

  public boolean isDrawShowProvisioners() {
    return drawShowProvisioners;
  }

  public void setDrawShowProvisioners(boolean drawShowProvisioners) {
    this.drawShowProvisioners = drawShowProvisioners;
  }

  public boolean isDrawShowAllMemberCounts() {
    return drawShowAllMemberCounts;
  }

  public void setDrawShowAllMemberCounts(boolean drawShowAllMemberCounts) {
    this.drawShowAllMemberCounts = drawShowAllMemberCounts;
  }

  public boolean isDrawShowDirectMemberCounts() {
    return drawShowDirectMemberCounts;
  }

  public void setDrawShowDirectMemberCounts(boolean drawShowDirectMemberCounts) {
    this.drawShowDirectMemberCounts = drawShowDirectMemberCounts;
  }

  public boolean isDrawShowObjectTypes() {
    return drawShowObjectTypes;
  }

  public void setDrawShowObjectTypes(boolean drawShowObjectTypes) {
    this.drawShowObjectTypes = drawShowObjectTypes;
  }

  public boolean isDrawIncludeGroupsInMemberCounts() {
    return drawIncludeGroupsInMemberCounts;
  }

  public void setDrawIncludeGroupsInMemberCounts(boolean drawIncludeGroupsInMemberCounts) {
    this.drawIncludeGroupsInMemberCounts = drawIncludeGroupsInMemberCounts;
  }

  public boolean isDrawShowLegend() {
    return drawShowLegend;
  }

  public void setDrawShowLegend(boolean drawShowLegend) {
    this.drawShowLegend = drawShowLegend;
  }

  /**
   *
   * @param jsonString
   * @return
   */
  public static UiV2VisualizationPreference jsonMarshalFrom(String jsonString) {
    if (StringUtils.isBlank(jsonString)) {
      return null;
    }
    return GrouperUtil.jsonConvertFrom(jsonString, UiV2VisualizationPreference.class);
  }

  /**
   * convert the object to json
   * @return the string
   */
  public String jsonConvertTo() {
    return GrouperUtil.jsonConvertTo(this, false);
  }

}
