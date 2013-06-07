/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.simpleMembershipUpdate;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperHtmlFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * get text in external URL (if applicable), and from nav.properties
 */
public class SimpleMembershipUpdateText {

  /** singleton */
  private static SimpleMembershipUpdateText simpleMembershipUpdateText = new SimpleMembershipUpdateText();
  
  /**
   * get singleton
   * @return singleton
   */
  public static SimpleMembershipUpdateText retrieveSingleton() {
    return simpleMembershipUpdateText;
  }

  /** grouper html filter */
  private static GrouperHtmlFilter grouperHtmlFilter;
  
  /** if found grouper html filter found */
  private static boolean grouperHtmlFilterFound = false;
  
  /**
   * cache this
   * @return grouper html filter
   */
  @SuppressWarnings("unchecked")
  private static GrouperHtmlFilter grouperHtmlFilter() {
    if (!grouperHtmlFilterFound) {
      String grouperHtmlFilterString = GrouperUiConfig.retrieveConfig().propertyValueStringRequired("simpleMembershipUpdate.externalUrlTextProperties.grouperHtmlFilter");
      Class<GrouperHtmlFilter> grouperHtmlFilterClass = GrouperUtil.forName(grouperHtmlFilterString);
      grouperHtmlFilter = GrouperUtil.newInstance(grouperHtmlFilterClass);
      grouperHtmlFilterFound = true;
    }
    return grouperHtmlFilter;
  }
  
  /**
   * get text based on key
   * @param key
   * @return text
   */
  public String text(String key) {
    
    //first try from URL external
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    String textUrl = simpleMembershipUpdateContainer.configValue("simpleMembershipUpdate.textFromUrl", false);
    
    if (!StringUtils.isBlank(textUrl)) {
      
      Properties properties = GrouperUtil.propertiesFromUrl(textUrl, true, true, grouperHtmlFilter());
      
      if (properties.containsKey(key)) {
        return properties.getProperty(key);
      }
      
    }
    
    //then try from name of simplemembership updater
    String name = simpleMembershipUpdateContainer.getMembershipLiteName();
    if (!StringUtils.isBlank(name)) {
      String namedKey = "membershipLiteName." + name + "." + key;
      if (TagUtils.navResourceContainsKey(namedKey)) {
        return TagUtils.navResourceString(namedKey);
      }
    }

    //finally, just go to nav.propreties
    return TagUtils.navResourceString(key);
    
  }
  
  /**
   * title of update screen
   * @return title
   */
  public String getUpdateTitle() {
    return text("simpleMembershipUpdate.updateTitle");
  }
  
  /**
   * infodot of update screen
   * @return infordot
   */
  public String getUpdateTitleInfodot() {
    return text("infodot.title.simpleMembershipUpdate.updateTitle");
  }
  
  /**
   * 
   * @return the label for the breadcrumb
   */
  public String getBreadcrumbLabel() {
    return text("simpleMembershipUpdate.find.browse.here");
  }
  
  /**
   * @return the label
   */
  public String getViewInAdminUi() {
    return text("simpleMembershipUpdate.viewInAdminUi");
  }
  
  /**
   * @return the label
   */
  public String getViewInAdminUiTooltip() {
    return text("tooltipTargetted.simpleMembershipUpdate.viewInAdminUi");
  }

  /**
   * @return the label
   */
  public String getGroupSubtitle() {
    return text("simpleMembershipUpdate.groupSubtitle");
  }
  
  /**
   * @return the label
   */
  public String getChangeLocation() {
    return text("simpleMembershipUpdate.changeLocation");
  }
  
  /**
   * @return the label
   */
  public String getChangeLocationTooltip() {
    return text("tooltipTargetted.simpleMembershipUpdate.changeLocation");
  }
  
  /**
   * @return the label
   */
  public String getAddMemberSubtitle() {
    return text("simpleMembershipUpdate.addMemberSubtitle");
  }
  
  /**
   * @return the label
   */
  public String getAddMemberButton() {
    return text("simpleMembershipUpdate.addMemberButton");
  }
  
  /**
   * @return the label
   */
  public String getMembershipListSubtitle() {
    return text("simpleMembershipUpdate.membershipListSubtitle");
  }
  
  /**
   * @return the label
   */
  public String getPagingLabelPrefix() {
    return text("simpleMembershipUpdate.pagingLabelPrefix");
  }
  
  /**
   * @return the label
   */
  public String getNoMembersFound() {
    return text("simpleMembershipUpdate.noMembersFound");
  }
  
  /**
   * @return the label
   */
  public String getDeleteConfirm() {
    return text("simpleMembershipUpdate.deleteConfirm");
  }
  
  /**
   * @return the label
   */
  public String getPagingResultPrefix() {
    return text("simpleMembershipUpdate.pagingResultPrefix");
  }

  /**
   * @return the label
   */
  public String getErrorNotEnoughSubjectChars() {
    return text("simpleMembershipUpdate.errorNotEnoughSubjectChars");
  }

  /**
   * @return the label
   */
  public String getErrorNotEnoughFilterChars() {
    return text("simpleMembershipUpdate.errorNotEnoughFilterChars");
  }

  /**
   * @return the label
   */
  public String getErrorNotEnoughFilterCharsAlert() {
    return text("simpleMembershipUpdate.errorNotEnoughFilterCharsAlert");
  }

  /**
   * @param memberDescription 
   * @return the label
   */
  public String getSuccessMemberDeleted(String memberDescription) {
    String theText = text("simpleMembershipUpdate.successMemberDeleted");
    memberDescription = GrouperUiUtils.escapeHtml(memberDescription, true, false);
    theText = StringUtils.replace(theText, "{0}", memberDescription);
    return theText;
  }

  /**
   * @return the label
   */
  public String getErrorUserSearchNothingEntered() {
    return text("simpleMembershipUpdate.errorUserSearchNothingEntered");
  }

  /**
   * @param memberDescription 
   * @return the label
   */
  public String getWarningSubjectAlreadyMember(String memberDescription) {
    String theText = text("simpleMembershipUpdate.warningSubjectAlreadyMember");
    memberDescription = GrouperUiUtils.escapeHtml(memberDescription, true, false);
    theText = StringUtils.replace(theText, "{0}", memberDescription);
    return theText;
  }

  /**
   * @param memberDescription 
   * @return the label
   */
  public String getSuccessMemberAdded(String memberDescription) {
    
    String theText = text("simpleMembershipUpdate.successMemberAdded");
    memberDescription = GrouperUiUtils.escapeHtml(memberDescription, true, false);
    theText = StringUtils.replace(theText, "{0}", memberDescription);
    return theText;
  }

  /**
   * @param memberDescription 
   * @return the label
   */
  public String getErrorSubjectNotFound(String memberDescription) {
    String theText = text("simpleMembershipUpdate.errorSubjectNotFound");
    memberDescription = GrouperUiUtils.escapeHtml(memberDescription, true, false);
    theText = StringUtils.replace(theText, "{0}", memberDescription);
    return theText;
  }

  /**
   * @param memberDescription 
   * @return the label
   */
  public String getErrorSubjectNotUnique(String memberDescription) {
    String theText = text("simpleMembershipUpdate.errorSubjectNotUnique");
    memberDescription = GrouperUiUtils.escapeHtml(memberDescription, true, false);
    theText = StringUtils.replace(theText, "{0}", memberDescription);
    return theText;
  }

  /**
   * @return the label
   */
  public String getErrorSourceUnavailable() {
    return text("simpleMembershipUpdate.errorSourceUnavailable");
  }

  /**
   * @return the label
   */
  public String getErrorUserSearchTooManyResults() {
    return text("simpleMembershipUpdate.errorUserSearchTooManyResults");
  }

  /**
   * @return the label
   */
  public String getErrorUserSearchNoResults() {
    return text("simpleMembershipUpdate.errorUserSearchNoResults");
  }

  /**
   * @return the label
   */
  public String getDeleteImageAlt() {
    return GrouperUiUtils.escapeHtml(text("simpleMembershipUpdate.deleteImageAlt"), true, true);
  }

  /**
   * @return the label
   */
  public String getErrorTooManyBrowsers() {
    return text("simpleMembershipUpdate.errorTooManyBrowsers");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuDeleteMultiple() {
    return text("simpleMembershipUpdate.advancedMenuDeleteMultiple");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuDeleteMultipleTooltip() {
    return text("simpleMembershipUpdate.advancedMenuDeleteMultipleTooltip");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuShowGroupDetails() {
    return text("simpleMembershipUpdate.advancedMenuShowGroupDetails");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuShowGroupDetailsTooltip() {
    return text("simpleMembershipUpdate.advancedMenuShowGroupDetailsTooltip");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuShowMemberFilter() {
    return text("simpleMembershipUpdate.advancedMenuShowMemberFilter");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuShowMemberFilterTooltip() {
    return text("simpleMembershipUpdate.advancedMenuShowMemberFilterTooltip");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuImportExport() {
    return text("simpleMembershipUpdate.advancedMenuImportExport");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuImportExportTooltip() {
    return text("simpleMembershipUpdate.advancedMenuImportExportTooltip");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuExport() {
    return text("simpleMembershipUpdate.advancedMenuExport");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuExportTooltip() {
    return text("simpleMembershipUpdate.advancedMenuExportTooltip");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuExportSubjectIds() {
    return text("simpleMembershipUpdate.advancedMenuExportSubjectIds");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuExportSubjectIdsTooltip() {
    return text("simpleMembershipUpdate.advancedMenuExportSubjectIdsTooltip");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuExportAll() {
    return text("simpleMembershipUpdate.advancedMenuExportAll");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuExportAllTooltip() {
    return text("simpleMembershipUpdate.advancedMenuExportAllTooltip");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuImport() {
    return text("simpleMembershipUpdate.advancedMenuImport");
  }

  /**
   * @return the label
   */
  public String getAdvancedMenuImportTooltip() {
    return text("simpleMembershipUpdate.advancedMenuImportTooltip");
  }
  
  /**
   * @return the label
   */
  public String getMemberMenuDetailsLabel() {
    return text("simpleMembershipUpdate.memberMenuDetailsLabel");
  }
  
  /**
   * @return the label
   */
  public String getMemberMenuDetailsTooltip() {
    return text("simpleMembershipUpdate.memberMenuDetailsTooltip");
  }
  
  /**
   * @return the label
   */
  public String getMemberMenuEnabledDisabled() {
    return text("simpleMembershipUpdate.memberMenuEnabledDisabled");
  }
  
  /**
   * @return the label
   */
  public String getMemberMenuEnabledDisabledTooltip() {
    return text("simpleMembershipUpdate.memberMenuEnabledDisabledTooltip");
  }
  
  /**
   * @return the label
   */
  public String getDeleteMultipleButton() {
    return text("simpleMembershipUpdate.deleteMultipleButton");
  }

  /**
   * @return the label
   */
  public String getDeleteMultipleTooltip() {
    return text("simpleMembershipUpdate.deleteMultipleTooltip");
  }

  /**
   * @return the label
   */
  public String getDeleteAllButton() {
    return text("simpleMembershipUpdate.deleteAllButton");
  }

  /**
   * @return the label
   */
  public String getDeleteAllTooltip() {
    return text("simpleMembershipUpdate.deleteAllTooltip");
  }

  /**
   * @return the label
   */
  public String getErrorDeleteCheckboxRequired() {
    return text("simpleMembershipUpdate.errorDeleteCheckboxRequired");
  }

  /**
   * @param memberCountDeleted 
   * @return the label
   */
  public String getSuccessMembersDeleted(int memberCountDeleted) {
    String theText = text("simpleMembershipUpdate.successMembersDeleted");
    theText = StringUtils.replace(theText, "{0}", Integer.toString(memberCountDeleted));
    return theText;
  }

  /**
   * @param memberCountDeleted 
   * @return the label
   */
  public String getSuccessAllMembersDeleted(int memberCountDeleted) {
    String theText = text("simpleMembershipUpdate.successAllMembersDeleted");
    theText = StringUtils.replace(theText, "{0}", Integer.toString(memberCountDeleted));
    return theText;
  }

  /**
   * @return the label
   */
  public String getAdvancedButton() {
    return text("simpleMembershipUpdate.advancedButton");
  }

  /**
   * @return the label
   */
  public String getAddMemberCombohint() {
    return text("simpleMembershipUpdate.addMemberCombohint");
  }

  /**
   * @return the label
   */
  public String getFilterMemberCombohint() {
    return text("simpleMembershipUpdate.filterMemberCombohint");
  }

  /**
   * @return the label
   */
  public String getDownloadSubjectIdsLabel() {
    return text("simpleMembershipUpdate.downloadSubjectIdsLabel");
  }

  /**
   * @return the label
   */
  public String getDownloadAllLabel() {
    return text("simpleMembershipUpdate.downloadAllLabel");
  }

  /**
   * @return the label
   */
  public String getImportLabel() {
    return text("simpleMembershipUpdate.importLabel");
  }

  /**
   * @return the label
   */
  public String getImportSubtitle() {
    return text("simpleMembershipUpdate.importSubtitle");
  }

  /**
   * @return the label
   */
  public String getImportSubtitleInfodot() {
    return text("infodot.subtitle.simpleMembershipUpdate.importSubtitle");
  }

  /**
   * @return the label
   */
  public String getImportAvailableSourceIds() {
    return text("simpleMembershipUpdate.importAvailableSourceIds");
  }

  /**
   * @return the label
   */
  public String getImportReplaceExistingMembers() {
    return text("simpleMembershipUpdate.importReplaceExistingMembers");
  }

  /**
   * @return the label
   */
  public String getImportCommaSeparatedValuesFile() {
    return text("simpleMembershipUpdate.importCommaSeparatedValuesFile");
  }

  /**
   * @return the label
   */
  public String getImportCancelButton() {
    return text("simpleMembershipUpdate.importCancelButton");
  }

  /**
   * @return the label
   */
  public String getImportButton() {
    return text("simpleMembershipUpdate.importButton");
  }

  /**
   * @return the label
   */
  public String getImportErrorNoWrongFile() {
    return text("simpleMembershipUpdate.importErrorNoWrongFile");
  }

  /**
   * @return the label
   */
  public String getImportErrorBlankTextarea() {
    return text("simpleMembershipUpdate.importErrorBlankTextarea");
  }

  /**
   * @return the label
   */
  public String getImportErrorNoId() {
    return text("simpleMembershipUpdate.importErrorNoId");
  }

  /**
   * @return the label
   */
  public String getImportErrorNoIdCol() {
    return text("simpleMembershipUpdate.importErrorNoIdCol");
  }

  /**
   * @return the label
   */
  public String getImportErrorSubjectProblems() {
    return text("simpleMembershipUpdate.importErrorSubjectProblems");
  }

  /**
   * @return the label
   */
  public String getImportSuccessSummary() {
    return text("simpleMembershipUpdate.importSuccessSummary");
  }

  /**
   * @param errorCount 
   * @return the label
   */
  public String getImportErrorSummary(int errorCount) {
    String theText = text("simpleMembershipUpdate.importErrorSummary");
    theText = StringUtils.replace(theText, "{0}", Integer.toString(errorCount));
    return theText;

  }

  /**
   * @param oldSize 
   * @param newSize 
   * @return the label
   */
  public String getImportSizeSummary(int oldSize, int newSize) {
    String theText = text("simpleMembershipUpdate.importSizeSummary");
    theText = StringUtils.replace(theText, "{0}", Integer.toString(oldSize));
    theText = StringUtils.replace(theText, "{1}", Integer.toString(newSize));
    return theText;
  }

  /**
   * @param adds 
   * @param deletes 
   * @return the label
   */
  public String getImportAddsDeletesSummary(int adds, int deletes) {
    String theText = text("simpleMembershipUpdate.importAddsDeletesSummary");
    theText = StringUtils.replace(theText, "{0}", Integer.toString(adds));
    theText = StringUtils.replace(theText, "{1}", Integer.toString(deletes));
    return theText;
  }

  /**
   * @return the label
   */
  public String getImportSubjectErrorsLabel() {
    return text("simpleMembershipUpdate.importSubjectErrorsLabel");
  }

  /**
   * @return the label
   */
  public String getImportAddErrorsLabel() {
    return text("simpleMembershipUpdate.importAddErrorsLabel");
  }

  /**
   * @return the label
   */
  public String getImportRemoveErrorsLabel() {
    return text("simpleMembershipUpdate.importRemoveErrorsLabel");
  }

  /**
   * @return the label
   */
  public String getMembershipLiteImportFileButton() {
    return text("simpleMembershipUpdate.membershipLiteImportFileButton");
  }

  /**
   * @return the label
   */
  public String getMembershipLiteImportTextfieldButton() {
    return text("simpleMembershipUpdate.membershipLiteImportTextfieldButton");
  }

  /**
   * @return the label
   */
  public String getImportDirectInput() {
    return text("simpleMembershipUpdate.importDirectInput");
  }

  /**
   * @return the label
   */
  public String getMemberMenuAlt() {
    return text("simpleMembershipUpdate.memberMenuAlt");
  }

  /**
   * @return the label
   */
  public String getMemberDetailsSubtitle() {
    return text("simpleMembershipUpdate.memberDetailsSubtitle");
  }

  /**
   * @return the label
   */
  public String getEnabledDisableSubtitle() {
    return text("simpleMembershipUpdate.enabledDisableSubtitle");
  }

  /**
   * @return the label
   */
  public String getEnabledDisableGroupPath() {
    return text("simpleMembershipUpdate.enabledDisableGroupPath");
  }

  /**
   * @return the label
   */
  public String getEnabledDisableEntity() {
    return text("simpleMembershipUpdate.enabledDisableEntity");
  }

  /**
   * @return the label
   */
  public String getEnabledDisableEntityId() {
    return text("simpleMembershipUpdate.enabledDisableEntityId");
  }

  /**
   * @return the label
   */
  public String getEnabledDisableEntitySource() {
    return text("simpleMembershipUpdate.enabledDisableEntitySource");
  }

  /**
   * @return the label
   */
  public String getEnabledDisableStartDate() {
    return text("simpleMembershipUpdate.enabledDisableStartDate");
  }

  /**
   * @return the label
   */
  public String getEnabledDisableEndDate() {
    return text("simpleMembershipUpdate.enabledDisableEndDate");
  }

  /**
   * @return the label
   */
  public String getEnabledDisableOkButton() {
    return text("simpleMembershipUpdate.enabledDisableOkButton");
  }

  /**
   * @return the label
   */
  public String getEnabledDisableCancelButton() {
    return text("simpleMembershipUpdate.enabledDisableCancelButton");
  }

  /**
   * @return the label
   */
  public String getEnabledDisableDateMask() {
    return text("simpleMembershipUpdate.enabledDisableDateMask");
  }

  /**
   * @return the label
   */
  public String getEnabledDisabledSuccess() {
    return text("simpleMembershipUpdate.enabledDisabledSuccess");
  }

  /**
   * @return the label
   */
  public String getFilterMemberButton() {
    return text("simpleMembershipUpdate.filterMemberButton");
  }
  /**
   * @return the label
   */
  public String getFilterLabel() {
    return text("simpleMembershipUpdate.filterLabel");
  }
  /**
   * @return the label
   */
  public String getClearFilterButton() {
    return text("simpleMembershipUpdate.clearFilterButton");
  }
  /**
   * @return the label
   */
  public String getErrorMemberFilterTooManyResults() {
    return text("simpleMembershipUpdate.errorMemberFilterTooManyResults");
  }
  /**
   * @return the label
   */
  public String getDisabledPrefix() {
    return text("simpleMembershipUpdate.disabledPrefix");
  }

  /**
   * @return the label
   */
  public String getGroupDisplayExtension() {
    return text("simpleMembershipUpdate.groups.summary.display-extension");
  }
  /**
   * @return the label
   */
  public String getGroupDisplayName() {
    return text("simpleMembershipUpdate.groups.summary.display-name");
  }
  /**
   * @return the label
   */
  public String getGroupDescription() {
    return text("simpleMembershipUpdate.groups.summary.description");
  }
  /**
   * @return the label
   */
  public String getGroupExtension() {
    return text("simpleMembershipUpdate.groups.summary.extension");
  }
  /**
   * @return the label
   */
  public String getGroupName() {
    return text("simpleMembershipUpdate.groups.summary.name");
  }
  /**
   * @return the label
   */
  public String getGroupId() {
    return text("simpleMembershipUpdate.groups.summary.id");
  }
  /**
   * @return the label
   */
  public String getGroupDisplayExtensionTooltip() {
    return text("simpleMembershipUpdate.tooltipTargetted.field.displayName.displayExtension");
  }
  /**
   * @return the label
   */
  public String getGroupDisplayNameTooltip() {
    return text("simpleMembershipUpdate.tooltipTargetted.field.displayName.displayName");
  }
  /**
   * @return the label
   */
  public String getGroupDescriptionTooltip() {
    return text("simpleMembershipUpdate.tooltipTargetted.field.displayName.description");
  }
  /**
   * @return the label
   */
  public String getGroupExtensionTooltip() {
    return text("simpleMembershipUpdate.tooltipTargetted.field.displayName.extension");
  }
  /**
   * @return the label
   */
  public String getGroupNameTooltip() {
    return text("simpleMembershipUpdate.tooltipTargetted.field.displayName.name");
  }
  /**
   * @return the label
   */
  public String getGroupIdTooltip() {
    return text("simpleMembershipUpdate.tooltipTargetted.groups.summary.id");
  }
  /**
   * @return the label
   */
  public String getGroupAlternateName() {
    return text("simpleMembershipUpdate.field.displayName.alternateName");
  }
  /**
   * @return the label
   */
  public String getGroupAlternateNameTooltip() {
    return text("simpleMembershipUpdate.tooltipTargetted.field.displayName.alternateName");
  }
  
  /**
   * @return the label
   */
  public String getSortBy() {
    return text("simpleMembershipUpdate.sortBy");
  }
}
