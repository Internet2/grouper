package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.dataField.GrouperDataField;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldConfiguration;
import edu.internet2.middleware.grouper.dataField.GrouperDataProvider;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderConfiguration;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryConfiguration;
import edu.internet2.middleware.grouper.dataField.GrouperDataRow;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowConfiguration;
import edu.internet2.middleware.grouper.dataField.GrouperPrivacyRealmConfiguration;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class EntityDataFieldsContainer {
  
  private int privacyRealmNumberOfConfigs;
  
  private int dataFieldsNumberOfConfigs;
  
  private int dataRowsNumberOfConfigs;
  
  private int dataProvidersNumberOfConfigs;
  
  private int dataProviderQueriesNumberOfConfigs;
  
  private List<GrouperDataField> grouperDataFields;

  private List<GrouperDataRow> grouperDataRows;
  
  private List<GrouperDataProvider> grouperDataProviders;
  
//  private List<GrouperDataProviderQuery> grouperDataProviderQueries;
  
  private GuiDataFieldConfiguration guiDataFieldConfiguration;
  
  private GuiPrivacyRealmConfiguration guiPrivacyRealmConfiguration;

  private GuiDataProviderConfiguration guiDataProviderConfiguration;

  private GuiDataProviderQueryConfiguration guiDataProviderQueryConfiguration;

  private GuiDataRowConfiguration guiDataRowConfiguration;
  
  private List<GuiPrivacyRealmConfiguration> guiPrivacyRealmConfigurations = new ArrayList<>();

  private List<GuiDataProviderConfiguration> guiDataProviderConfigurations = new ArrayList<>();

  private List<GuiDataProviderQueryConfiguration> guiDataProviderQueryConfigurations = new ArrayList<>();
  
  private List<GuiDataFieldConfiguration> guiDataFieldConfigurations = new ArrayList<>();

  private List<GuiDataRowConfiguration> guiDataRowConfigurations = new ArrayList<>();
  
  
  /**
   * @return true if can operate on entity data fields
   */
  public boolean isCanOperateOnEntityDataFieldConfigs() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    return false;
  }
  
  
  public GuiDataProviderQueryConfiguration getGuiDataProviderQueryConfiguration() {
    return guiDataProviderQueryConfiguration;
  }



  
  public void setGuiDataProviderQueryConfiguration(
      GuiDataProviderQueryConfiguration guiDataProviderQueryConfiguration) {
    this.guiDataProviderQueryConfiguration = guiDataProviderQueryConfiguration;
  }



  
  public List<GuiDataProviderQueryConfiguration> getGuiDataProviderQueryConfigurations() {
    return guiDataProviderQueryConfigurations;
  }



  
  public void setGuiDataProviderQueryConfigurations(
      List<GuiDataProviderQueryConfiguration> guiDataProviderQueryConfigurations) {
    this.guiDataProviderQueryConfigurations = guiDataProviderQueryConfigurations;
  }



  public GuiDataProviderConfiguration getGuiDataProviderConfiguration() {
    return guiDataProviderConfiguration;
  }


  
  public void setGuiDataProviderConfiguration(
      GuiDataProviderConfiguration guiDataProviderConfiguration) {
    this.guiDataProviderConfiguration = guiDataProviderConfiguration;
  }


  
  public List<GuiDataProviderConfiguration> getGuiDataProviderConfigurations() {
    return guiDataProviderConfigurations;
  }


  
  public void setGuiDataProviderConfigurations(
      List<GuiDataProviderConfiguration> guiDataProviderConfigurations) {
    this.guiDataProviderConfigurations = guiDataProviderConfigurations;
  }


  public GuiDataRowConfiguration getGuiDataRowConfiguration() {
    return guiDataRowConfiguration;
  }

  
  public void setGuiDataRowConfiguration(GuiDataRowConfiguration guiDataRowConfiguration) {
    this.guiDataRowConfiguration = guiDataRowConfiguration;
  }


  
  public List<GuiDataRowConfiguration> getGuiDataRowConfigurations() {
    return guiDataRowConfigurations;
  }


  
  public void setGuiDataRowConfigurations(
      List<GuiDataRowConfiguration> guiDataRowConfigurations) {
    this.guiDataRowConfigurations = guiDataRowConfigurations;
  }


  public List<GuiDataFieldConfiguration> getGuiDataFieldConfigurations() {
    return guiDataFieldConfigurations;
  }

  
  public void setGuiDataFieldConfigurations(List<GuiDataFieldConfiguration> guiDataFieldConfigurations) {
    this.guiDataFieldConfigurations = guiDataFieldConfigurations;
  }



  public List<GuiPrivacyRealmConfiguration> getGuiPrivacyRealmConfigurations() {
    return guiPrivacyRealmConfigurations;
  }


  
  public void setGuiPrivacyRealmConfigurations(
      List<GuiPrivacyRealmConfiguration> guiPrivacyRealmConfigurations) {
    this.guiPrivacyRealmConfigurations = guiPrivacyRealmConfigurations;
  }


  public GuiPrivacyRealmConfiguration getGuiPrivacyRealmConfiguration() {
    return guiPrivacyRealmConfiguration;
  }

  
  public void setGuiPrivacyRealmConfiguration(
      GuiPrivacyRealmConfiguration guiPrivacyRealmConfiguration) {
    this.guiPrivacyRealmConfiguration = guiPrivacyRealmConfiguration;
  }

  public int getPrivacyRealmNumberOfConfigs() {
    return privacyRealmNumberOfConfigs;
  }

  public void setPrivacyRealmNumberOfConfigs(int privacyRealmNumberOfConfigs) {
    this.privacyRealmNumberOfConfigs = privacyRealmNumberOfConfigs;
  }


  public GuiDataFieldConfiguration getGuiDataFieldConfiguration() {
    return guiDataFieldConfiguration;
  }

  
  public void setGuiDataFieldConfiguration(
      GuiDataFieldConfiguration guiDataFieldConfiguration) {
    this.guiDataFieldConfiguration = guiDataFieldConfiguration;
  }

  public int getDataProvidersNumberOfConfigs() {
    return dataProvidersNumberOfConfigs;
  }

  public void setDataProvidersNumberOfConfigs(int dataProvidersNumberOfConfigs) {
    this.dataProvidersNumberOfConfigs = dataProvidersNumberOfConfigs;
  }
  
  public int getDataProviderQueriesNumberOfConfigs() {
    return dataProviderQueriesNumberOfConfigs;
  }
  
  public void setDataProviderQueriesNumberOfConfigs(
      int dataProviderQueriesNumberOfConfigs) {
    this.dataProviderQueriesNumberOfConfigs = dataProviderQueriesNumberOfConfigs;
  }

  public int getDataRowsNumberOfConfigs() {
    return dataRowsNumberOfConfigs;
  }

  public void setDataRowsNumberOfConfigs(int dataRowsNumberOfConfigs) {
    this.dataRowsNumberOfConfigs = dataRowsNumberOfConfigs;
  }

  public int getDataFieldsNumberOfConfigs() {
    return dataFieldsNumberOfConfigs;
  }
  
  public void setDataFieldsNumberOfConfigs(int dataFieldsNumberOfConfigs) {
    this.dataFieldsNumberOfConfigs = dataFieldsNumberOfConfigs;
  }

  
  public List<GrouperDataField> getGrouperDataFields() {
    return grouperDataFields;
  }

  
  public void setGrouperDataFields(List<GrouperDataField> grouperDataFields) {
    this.grouperDataFields = grouperDataFields;
  }

  
  public List<GrouperDataRow> getGrouperDataRows() {
    return grouperDataRows;
  }

  
  public void setGrouperDataRows(List<GrouperDataRow> grouperDataRows) {
    this.grouperDataRows = grouperDataRows;
  }

  
  public List<GrouperDataProvider> getGrouperDataProviders() {
    return grouperDataProviders;
  }

  
  public void setGrouperDataProviders(List<GrouperDataProvider> grouperDataProviders) {
    this.grouperDataProviders = grouperDataProviders;
  }

  
//  public List<GrouperDataProviderQuery> getGrouperDataProviderQueries() {
//    return grouperDataProviderQueries;
//  }
//
//  
//  public void setGrouperDataProviderQueries(List<GrouperDataProviderQuery> grouperDataProviderQueries) {
//    this.grouperDataProviderQueries = grouperDataProviderQueries;
//  }
  
  public List<GrouperDataFieldConfiguration> getAllDataFieldTypes() {
    return Arrays.asList(new GrouperDataFieldConfiguration());
  }

  public List<GrouperDataRowConfiguration> getAllDataRowTypes() {
    return Arrays.asList(new GrouperDataRowConfiguration());
  }
  
  public List<GrouperDataProviderConfiguration> getAllDataProviderTypes() {
    return Arrays.asList(new GrouperDataProviderConfiguration());
  }

  public List<GrouperDataProviderQueryConfiguration> getAllDataProviderQueryTypes() {
    return Arrays.asList(new GrouperDataProviderQueryConfiguration());
  }
  
  public List<GrouperPrivacyRealmConfiguration> getAllPrivacyRealmTypes() {
    return Arrays.asList(new GrouperPrivacyRealmConfiguration());
  }
  
}
