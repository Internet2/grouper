package edu.internet2.middleware.grouper.userLifecycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataField;
import edu.internet2.middleware.grouper.dataField.GrouperDataRow;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class UserLifecycleEngine {

  /**
   * Build the JEXL variable map exposed to user lifecycle event natural-language
   * templates. Any argument may be null; only non-null inputs contribute keys.
   * The keys here are the single source of truth shared by the daemon
   * (UserLifecycleFullDaemon) and the pre-save validator
   * (UserLifecycleEventConfiguration.validatePreSave) so they cannot drift.
   */
  public static Map<String, Object> buildJexlVariables(
      Group group, Stem stem,
      GrouperDataField dataField, Object dataFieldValue,
      GrouperDataRow dataRow) {

    Map<String, Object> vars = new HashMap<>();

    if (group != null) {
      vars.put("groupName",             group.getName());
      vars.put("groupDisplayName",      group.getDisplayName());
      vars.put("groupExtension",        group.getExtension());
      vars.put("groupDisplayExtension", group.getDisplayExtension());
      vars.put("groupDescription",      group.getDescription());
    }

    if (stem != null) {
      vars.put("stemName",             stem.getName());
      vars.put("stemDisplayName",      stem.getDisplayName());
      vars.put("stemExtension",        stem.getExtension());
      vars.put("stemDisplayExtension", stem.getDisplayExtension());
      vars.put("stemDescription",      stem.getDescription());
    }

    if (dataField != null) {
      vars.put("configId", dataField.getConfigId());
    }
    if (dataFieldValue != null) {
      vars.put("value", dataFieldValue);
    }

    if (dataRow != null) {
      vars.put("configId", dataRow.getConfigId());
    }

    return vars;
  }

  /**
   * Evaluate a user lifecycle natural-language template (template-mode JEXL —
   * literal text is preserved, ${...} blocks are evaluated). Daemon callers pass
   * lenient=true (undefined variables render as empty, don't crash production);
   * the pre-save validator passes lenient=false (undefined variables throw, so
   * typos surface as field-level errors at save time).
   */
  public static String evaluateLifecycleJexl(
      String jexlTemplate,
      Group group, Stem stem,
      GrouperDataField dataField, Object dataFieldValue,
      GrouperDataRow dataRow,
      boolean lenient) {

    Map<String, Object> vars = buildJexlVariables(group, stem, dataField, dataFieldValue, dataRow);
    // Use the script-template variant so each ${...} block is evaluated as a JEXL
    // script (multi-statement, var declarations) rather than a single expression.
    return GrouperUtil.substituteExpressionLanguageScriptsNotExpressions(jexlTemplate, vars, true, false, lenient);
  }


  /**
   * take data fields and make sure the have an internal id
   */
  public static void syncUserLifecycleEventConfigs(GrouperConfig grouperConfig) {
    
    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(GrouperLifecycleEventConfig.lifecycleEventConfigIds));

    
    List<GrouperLifecycleEventConfig> grouperLifecycleEventConfigsInDb = GrouperUtil.nonNull(UserLifecycleEventConfigDao.selectAll());

    Map<String, GrouperLifecycleEventConfig> configIdToGrouperLifecycleEventConfigInDb = new HashMap<String, GrouperLifecycleEventConfig>();
    Map<Long, GrouperLifecycleEventConfig> internalIdToGrouperLifecycleEventConfigInDb = new HashMap<Long, GrouperLifecycleEventConfig>();

    for (GrouperLifecycleEventConfig grouperLifecycleEventConfig : grouperLifecycleEventConfigsInDb) {
      configIdToGrouperLifecycleEventConfigInDb.put(grouperLifecycleEventConfig.getConfigId(), grouperLifecycleEventConfig);
      internalIdToGrouperLifecycleEventConfigInDb.put(grouperLifecycleEventConfig.getInternalId(), grouperLifecycleEventConfig);
    }
    
    // additions
    Set<String> configIdsToInsert = new HashSet<String>(configIdsInConfig);
    configIdsToInsert.removeAll(configIdToGrouperLifecycleEventConfigInDb.keySet());
    UserLifecycleEventConfigDao.insertMissingConfigIds(configIdsToInsert);
    
    // deletions
    Set<String> configIdsToDelete = new HashSet<String>(configIdToGrouperLifecycleEventConfigInDb.keySet());
    configIdsToDelete.removeAll(configIdsInConfig);
    
    List<GrouperLifecycleEventConfig> eventConfigsToDelete = new ArrayList<>();
    
    for (String configIdToDelete : configIdsToDelete) {
      GrouperLifecycleEventConfig grouperLifecycleEventConfig = configIdToGrouperLifecycleEventConfigInDb.get(configIdToDelete);
      eventConfigsToDelete.add(grouperLifecycleEventConfig);
    }
    UserLifecycleEventConfigDao.delete(eventConfigsToDelete);
    
    // updates
    
    // grouperLifecycleEventConfigsInDb is in the db (lifecycle event table) and configIdsInConfig is what's in the grouper_config table 
    // configIdsToInsert are already taken care of and configIdsToDelete are also taken care of
    
    Set<String> potentialConfigIdsToUpdate = new HashSet<String>(configIdsInConfig);
    potentialConfigIdsToUpdate.removeAll(configIdsToInsert);
    potentialConfigIdsToUpdate.removeAll(configIdsToDelete);
    
    for (String configIdToUpdate : potentialConfigIdsToUpdate) {
      GrouperLifecycleEventConfig grouperLifecycleEventConfig = configIdToGrouperLifecycleEventConfigInDb.get(configIdToUpdate);
      UserLifecycleEventConfigDao.updateEventLifecycleConfig(grouperLifecycleEventConfig, configIdToUpdate);
    }
    
  }

}
