package edu.internet2.middleware.grouper.app.gsh.template;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Subject;

public class GshTemplateConfig {
  
  private static final Log LOG = GrouperUtil.getLog(GshTemplateConfig.class);
  
  private String configId;
  
  private String templateName;
  
  private String templateNameExternalizedTextKey;
  
  private String templateDescription;
  
  private String templateDescriptionExternalizedTextKey;
  
  
  public String getTemplateNameExternalizedTextKey() {
    return templateNameExternalizedTextKey;
  }


  
  public String getTemplateDescriptionExternalizedTextKey() {
    return templateDescriptionExternalizedTextKey;
  }


  public String getTemplateDescription() {
    return templateDescription;
  }

  private GshTemplateRunAsType gshTemplateRunAsType;
  
  
  public void setGshTemplateRunAsType(GshTemplateRunAsType gshTemplateRunAsType) {
    this.gshTemplateRunAsType = gshTemplateRunAsType;
  }

  private boolean enabled;
  
  private boolean useIndividualAudits;
  
  private boolean showOnGroups;
  
  private GshTemplateGroupShowType gshTemplateGroupShowType;
  
  private Set<Group> groupsToShow = new HashSet<Group>();
  
  private GshTemplateGroupShowOnDescendants gshTemplateGroupShowOnDescendants;
  
  private boolean allowWsFromNoOwner;
  
  
  public boolean isAllowWsFromNoOwner() {
    return allowWsFromNoOwner;
  }

  private boolean showOnFolders;
  
  private GshTemplateFolderShowType gshTemplateFolderShowType;
  
  /** V1 or V2 */
  private String templateVersion;
  
  /**
   * V1 or V2
   * @return
   */
  public String getTemplateVersion() {
    return templateVersion;
  }

  private Set<Stem> foldersToShow = new HashSet<Stem>();
  
  private Stem folderForGroupsInFolder;
  
  private GshTemplateType gshTemplateType;


  public GshTemplateType getGshTemplateType() {
    return gshTemplateType;
  }

  /**
   * Execution mode — interpreted (legacy Groovy engine) or compiled
   * (new Java compile-on-save path via GshTemplateClassLoaderRegistry).
   * Defaults to interpreted for backward compat. GRP-7011.
   */
  private GshTemplateMode gshTemplateMode;

  /**
   * @return execution mode (interpreted or compiled). Never null after
   *   config load; defaults to interpreted for legacy rows.
   */
  public GshTemplateMode getGshTemplateMode() {
    return this.gshTemplateMode;
  }

  /**
   * @param gshTemplateMode
   */
  public void setGshTemplateMode(GshTemplateMode gshTemplateMode) {
    this.gshTemplateMode = gshTemplateMode;
  }

  /**
   * @return true if this template runs in compiled-Java mode
   *   (templateMode=compiled); false for the legacy interpreted Groovy path
   */
  public boolean isCompiledMode() {
    return this.gshTemplateMode == GshTemplateMode.compiled;
  }

  private GshTemplateFolderShowOnDescendants gshTemplateFolderShowOnDescendants;
  
  private GshTemplateSecurityRunType gshTemplateSecurityRunType;
  
  private Group groupThatCanRun;
  
  private boolean useExternalizedText;
  
  private String moreActionsLabelExternalizedTextKey;
  
  private String moreActionsLabel;
  
  private GshTemplateRequireFolderPrivilege gshTemplateRequireFolderPrivilege;
  
  private GshTemplateRequireGroupPrivilege gshTemplateRequireGroupPrivilege;
  
  private String runAsSpecifiedSubjectSourceId;
  
  private String runAsSpecifiedSubjectId;
  
  private boolean runGshInTransaction = true;
  
  private String gshTemplate;
  
  private String actAsGroupUUID;
  
  private boolean showInMoreActions;
  
  private boolean displayErrorOutput;

  private boolean mcpEnabled;

  private boolean mcpReadonly;

  private List<GshTemplateInputConfig> gshTemplateInputConfigs = new ArrayList<GshTemplateInputConfig>();
  
  private String gshTemplateSourceType;
  
  private String gshTemplateFileName;
  

  public GshTemplateConfig(String configId) {
    this.configId = configId;
  }
  
  
  public String getConfigId() {
    return configId;
  }
  
  
  public String getGshTemplateSourceType() {
    return gshTemplateSourceType;
  }

  
  public String getGshTemplateFileName() {
    return gshTemplateFileName;
  }


  public String getTemplateName() {
    return templateName;
  }

  public String getTemplateNameForUi() {
    if (!useExternalizedText) {
      return this.templateName;
    } else {
      return StringUtils.defaultString(GrouperTextContainer.textOrNull(templateNameExternalizedTextKey), templateNameExternalizedTextKey);
    }
  }

  public String getTemplateDescriptionForUi() {
    if (!useExternalizedText) {
      return this.templateDescription;
    } else {
      return StringUtils.defaultString(GrouperTextContainer.textOrNull(templateDescriptionExternalizedTextKey), templateDescriptionExternalizedTextKey);
    }
  }

  public String getMoreActionsLabelForUi() {
    if (!useExternalizedText) {
      return this.moreActionsLabel;
    } else {
      return StringUtils.defaultString(GrouperTextContainer.textOrNull(moreActionsLabelExternalizedTextKey), moreActionsLabelExternalizedTextKey);
    }
  }

  public boolean isUseExternalizedText() {
    return useExternalizedText;
  }


  public String getMoreActionsLabelExternalizedTextKey() {
    return moreActionsLabelExternalizedTextKey;
  }


  
  public String getMoreActionsLabel() {
    return moreActionsLabel;
  }

  public GshTemplateRunAsType getGshTemplateRunAsType() {
    return gshTemplateRunAsType;
  }


  
  public boolean isEnabled() {
    return enabled;
  }

  
  
  public boolean isUseIndividualAudits() {
    return useIndividualAudits;
  }


  public boolean isShowOnGroups() {
    return showOnGroups;
  }


  
  public GshTemplateGroupShowType getGshTemplateGroupShowType() {
    return gshTemplateGroupShowType;
  }


  
  
  public Set<Group> getGroupsToShow() {
    return groupsToShow;
  }


  
  public GshTemplateGroupShowOnDescendants getGshTemplateGroupShowOnDescendants() {
    return gshTemplateGroupShowOnDescendants;
  }


  
  public boolean isShowOnFolders() {
    return showOnFolders;
  }

  
  
  
  
  public Stem getFolderForGroupsInFolder() {
    return folderForGroupsInFolder;
  }



  
  public void setFolderForGroupsInFolder(Stem folderForGroupsInFolder) {
    this.folderForGroupsInFolder = folderForGroupsInFolder;
  }



  public boolean isShowInMoreActions() {
    return showInMoreActions;
  }


  public GshTemplateFolderShowType getGshTemplateFolderShowType() {
    return gshTemplateFolderShowType;
  }


  
  
  
  public Set<Stem> getFoldersToShow() {
    return foldersToShow;
  }



  public GshTemplateFolderShowOnDescendants getGshTemplateFolderShowOnDescendants() {
    return gshTemplateFolderShowOnDescendants;
  }


  
  public GshTemplateSecurityRunType getGshTemplateSecurityRunType() {
    return gshTemplateSecurityRunType;
  }


  
  public Group getGroupThatCanRun() {
    return groupThatCanRun;
  }


  
  public GshTemplateRequireFolderPrivilege getGshTemplateRequireFolderPrivilege() {
    return gshTemplateRequireFolderPrivilege;
  }


  
  public GshTemplateRequireGroupPrivilege getGshTemplateRequireGroupPrivilege() {
    return gshTemplateRequireGroupPrivilege;
  }


  
  public String getRunAsSpecifiedSubjectSourceId() {
    return runAsSpecifiedSubjectSourceId;
  }


  
  public String getRunAsSpecifiedSubjectId() {
    return runAsSpecifiedSubjectId;
  }

  private boolean simplifiedUi;
  
  public boolean isSimplifiedUi() {
    return simplifiedUi;
  }
  
  public void setSimplifiedUi(boolean simplifiedUi) {
    this.simplifiedUi = simplifiedUi;
  }



  public String getGshTemplate() {
    return gshTemplate;
  }

  /**
   * Read the template's source from its configured location — inline config
   * (gshTemplateSourceType=textArea, the default, returns the gshTemplate
   * property) or a container file (gshTemplateSourceType=file, reads the file
   * named by gshTemplateFileName on each call). Centralizes the
   * file-vs-inline branch so the registry and the compiled-Java dispatchers
   * do not duplicate it. GRP-7026.
   * @return the source string
   */
  public String readSource() {
    if (StringUtils.equals(this.gshTemplateSourceType, "file")) {
      String fileName = this.gshTemplateFileName;
      File file = new File(fileName);
      if (!file.exists()) {
        throw new RuntimeException("File '" + fileName + "' does not exist in container!!");
      }
      return GrouperUtil.readFileIntoString(file);
    }
    return this.gshTemplate;
  }

  public List<GshTemplateInputConfig> getGshTemplateInputConfigs() {
    return gshTemplateInputConfigs;
  }
  
  
  public boolean isRunGshInTransaction() {
    return runGshInTransaction;
  }
  

  
  public String getActAsGroupUUID() {
    return actAsGroupUUID;
  }

  public boolean isDisplayErrorOutput() {
    return displayErrorOutput;
  }

  public boolean isMcpEnabled() {
    return mcpEnabled;
  }

  public boolean isMcpReadonly() {
    return mcpReadonly;
  }

  /**
   * cache the groups and stems
   * first key is group or stem
   * second key is id or name
   * value is the grouper object or Void if not found
   */
  private static ExpirableCache<MultiKey, Object> grouperObjectTypeIdOrNameToGrouperObjectCache = new ExpirableCache<MultiKey, Object>(5);
  
  /**
   * clear cache
   */
  public static void clearGrouperObjectTypeIdOrNameToGrouperObjectCache() {
    grouperObjectTypeIdOrNameToGrouperObjectCache.clear();
  }
  
  public static Map<MultiKey, GrouperObject> gshTemplateObjectCache() {

    List<GshTemplateConfiguration> gshTemplateConfigs = GshTemplateConfiguration.retrieveAllGshTemplateConfigs();
    
    // resolve all the stems and groups here to avoid doing it in the loop
    Map<MultiKey, GrouperObject> grouperObjectTypeIdOrNameToGrouperObject = new HashMap<MultiKey, GrouperObject>();

    final Set<String> groupIdsToFind = new HashSet<>();
    final Set<String> groupNamesToFind = new HashSet<>();
    final Set<String> stemIdsToFind = new HashSet<>();
    final Set<String> stemNamesToFind = new HashSet<>();
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    
    for (GshTemplateConfiguration gshTemplateConfiguration: gshTemplateConfigs) {
      if (!gshTemplateConfiguration.isEnabled()) {
        continue;
      }

      String configPrefix = "grouperGshTemplate."+gshTemplateConfiguration.getConfigId()+".";

      // multiple
      String groupUuidsToShow = grouperConfig.propertyValueString(configPrefix+"groupUuidsToShow");
      
      if (!StringUtils.isBlank(groupUuidsToShow)) {
        String[] groupUuidsOrNames = GrouperUtil.splitTrim(groupUuidsToShow, ",");
        for (String groupUuidOrName: groupUuidsOrNames) {
          if (groupUuidOrName.contains(":")) {
            groupNamesToFind.add(groupUuidOrName);
          } else {
            groupIdsToFind.add(groupUuidOrName);
          }
        }
      }

      String folderUuidForGroupsInFolder = grouperConfig.propertyValueString(configPrefix+"folderUuidForGroupsInFolder");
      
      if (!StringUtils.isBlank(folderUuidForGroupsInFolder)) {
        String[] folderUuidsOrNames = GrouperUtil.splitTrim(folderUuidForGroupsInFolder, ",");
        for (String folderUuidOrName: folderUuidsOrNames) {
          stemNamesToFind.add(folderUuidOrName);
          stemIdsToFind.add(folderUuidOrName);
        }
      }

      // multiple
      String folderUuidsOrNamesToShow = grouperConfig.propertyValueString(configPrefix+"folderUuidToShow");
      
      if (!StringUtils.isBlank(folderUuidsOrNamesToShow)) {
        String[] folderUuidsOrNames = GrouperUtil.splitTrim(folderUuidsOrNamesToShow, ",");
        for (String folderUuidOrName: folderUuidsOrNames) {
          stemNamesToFind.add(folderUuidOrName);
          if (!folderUuidOrName.contains(":")) {
            stemIdsToFind.add(folderUuidOrName);
          }
        }
      }

      String groupUuidOrNameCanRun = grouperConfig.propertyValueString(configPrefix+"groupUuidCanRun");

      if (!StringUtils.isBlank(groupUuidOrNameCanRun)) {
        if (groupUuidOrNameCanRun.contains(":")) {
          groupNamesToFind.add(groupUuidOrNameCanRun);
        } else {
          groupIdsToFind.add(groupUuidOrNameCanRun);
        }
      }
      
    }
    
    Iterator<String> groupIdIterator = groupIdsToFind.iterator();
    while (groupIdIterator.hasNext()) {
      String groupId = groupIdIterator.next();
      MultiKey multiKey = new MultiKey("group", groupId);
      Object object = grouperObjectTypeIdOrNameToGrouperObjectCache.get(multiKey);
      if (object == null) {
        continue;
      }
      if (object == Void.TYPE) {
        if (!grouperObjectTypeIdOrNameToGrouperObject.containsKey(multiKey)) {
          grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, null);
        }
      } else {
        grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, (GrouperObject)object);
      }
    }
    
    Iterator<String> groupNameIterator = groupNamesToFind.iterator();
    while (groupNameIterator.hasNext()) {
      String groupName = groupNameIterator.next();
      MultiKey multiKey = new MultiKey("group", groupName);
      Object object = grouperObjectTypeIdOrNameToGrouperObjectCache.get(multiKey);
      if (object == null) {
        continue;
      }
      if (object == Void.TYPE) {
        if (!grouperObjectTypeIdOrNameToGrouperObject.containsKey(multiKey)) {
          grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, null);
        }
      } else {
        grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, (GrouperObject)object);
      }
    }
    
    Iterator<String> stemIdIterator = stemIdsToFind.iterator();
    while (stemIdIterator.hasNext()) {
      String stemId = stemIdIterator.next();
      MultiKey multiKey = new MultiKey("stem", stemId);
      Object object = grouperObjectTypeIdOrNameToGrouperObjectCache.get(multiKey);
      if (object == null) {
        continue;
      }
      if (object == Void.TYPE) {
        if (!grouperObjectTypeIdOrNameToGrouperObject.containsKey(multiKey)) {
          grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, null);
        }
      } else {
        grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, (GrouperObject)object);
      }
    }
    
    Iterator<String> stemNameIterator = stemNamesToFind.iterator();
    while (stemNameIterator.hasNext()) {
      String stemName = stemNameIterator.next();
      MultiKey multiKey = new MultiKey("stem", stemName);
      Object object = grouperObjectTypeIdOrNameToGrouperObjectCache.get(multiKey);
      if (object == null) {
        continue;
      }
      if (object == Void.TYPE) {
        if (!grouperObjectTypeIdOrNameToGrouperObject.containsKey(multiKey)) {
          grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, null);
        }
      } else {
        grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, (GrouperObject)object);
      }
    }

    // if there are ids or names of groups or stems to find, find them now
    if (stemIdsToFind.size() > 0 || stemNamesToFind.size() > 0 || groupIdsToFind.size() > 0 || groupNamesToFind.size() > 0) {
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          // find groups by id
          if (groupIdsToFind.size() > 0) {
            Set<Group> groups = new GroupFinder().assignGroupIds(groupIdsToFind).findGroups();
            for (Group theGroup : groups) {
              MultiKey multiKey = new MultiKey("group", theGroup.getUuid());
              grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, theGroup);
              grouperObjectTypeIdOrNameToGrouperObjectCache.put(multiKey, theGroup);
              groupIdsToFind.remove(theGroup.getUuid());
            }
            // not found
            for (String groupIdNotFound : groupIdsToFind) {
              MultiKey multiKey = new MultiKey("group", groupIdNotFound);
              if (!grouperObjectTypeIdOrNameToGrouperObject.containsKey(multiKey)) {
                grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, null);
                grouperObjectTypeIdOrNameToGrouperObjectCache.put(multiKey, Void.TYPE);
              }
            }
          }
          
          // find groups by name
          if (groupNamesToFind.size() > 0) {
            Set<Group> groups = new GroupFinder().assignGroupNames(groupNamesToFind).findGroups();
            for (Group theGroup : groups) {
              MultiKey multiKey = new MultiKey("group", theGroup.getName());
              grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, theGroup);
              grouperObjectTypeIdOrNameToGrouperObjectCache.put(multiKey, theGroup);
              groupNamesToFind.remove(theGroup.getName());
            }
            // not found
            for (String groupNameNotFound : groupNamesToFind) {
              MultiKey multiKey = new MultiKey("group", groupNameNotFound);
              if (!grouperObjectTypeIdOrNameToGrouperObject.containsKey(multiKey)) {
                grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, null);
                grouperObjectTypeIdOrNameToGrouperObjectCache.put(multiKey, Void.TYPE);
              }
            }
          }
          
          // find stems by id
          if (stemIdsToFind.size() > 0) {
            Set<Stem> stems = new StemFinder().assignStemIds(stemIdsToFind).findStems();
            for (Stem theStem : stems) {
              MultiKey multiKey = new MultiKey("stem", theStem.getUuid());
              grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, theStem);
              grouperObjectTypeIdOrNameToGrouperObjectCache.put(multiKey, theStem);
              stemIdsToFind.remove(theStem.getUuid());
            }
            // not found
            for (String stemIdNotFound : stemIdsToFind) {
              MultiKey multiKey = new MultiKey("stem", stemIdNotFound);
              if (!grouperObjectTypeIdOrNameToGrouperObject.containsKey(multiKey)) {
                grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, null);
                grouperObjectTypeIdOrNameToGrouperObjectCache.put(multiKey, Void.TYPE);
              }
            }
          }
          
          // find stems by name
          if (stemNamesToFind.size() > 0) {
            Set<Stem> stems = new StemFinder().assignStemNames(stemNamesToFind).findStems();
            for (Stem theStem : stems) {
              MultiKey multiKey = new MultiKey("stem", theStem.getName());
              grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, theStem);
              grouperObjectTypeIdOrNameToGrouperObjectCache.put(multiKey, theStem);
              stemNamesToFind.remove(theStem.getName());
            }
            // not found
            for (String stemNameNotFound : stemNamesToFind) {
              MultiKey multiKey = new MultiKey("stem", stemNameNotFound);
              if (!grouperObjectTypeIdOrNameToGrouperObject.containsKey(multiKey)) {
                grouperObjectTypeIdOrNameToGrouperObject.put(multiKey, null);
                grouperObjectTypeIdOrNameToGrouperObjectCache.put(multiKey, Void.TYPE);
              }
            }
          }
          
          return null;
        }
      });
    }
    return grouperObjectTypeIdOrNameToGrouperObject;
  }
  
  public void populateConfiguration() {
    populateConfiguration(null);
  }
  
  public void populateConfiguration(Map<MultiKey, GrouperObject> grouperObjectTypeIdOrNameToGrouperObject) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        String configPrefix = "grouperGshTemplate."+configId+".";
        
        GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
        gshTemplateType = GshTemplateType.valueOfIgnoreCase(GrouperUtil.defaultIfBlank(grouperConfig.propertyValueString(configPrefix+"templateType"), "gsh"), true);

        // GRP-7011: templateMode — interpreted (legacy default) or compiled
        gshTemplateMode = GshTemplateMode.valueOfIgnoreCase(
            GrouperUtil.defaultIfBlank(grouperConfig.propertyValueString(configPrefix+"templateMode"), "interpreted"),
            true);

        enabled = grouperConfig.propertyValueBoolean(configPrefix+"enabled", true);

        templateVersion = grouperConfig.propertyValueString(configPrefix+"templateVersion", "V1");

        simplifiedUi = grouperConfig.propertyValueBoolean(configPrefix+"simplifiedUi", false);

        useIndividualAudits = grouperConfig.propertyValueBoolean(configPrefix+"useIndividualAudits", true);
        
        useExternalizedText = grouperConfig.propertyValueBoolean(configPrefix+"externalizedText", false);
        
        showInMoreActions = grouperConfig.propertyValueBoolean(configPrefix+"showInMoreActions", false);
        
        if (useExternalizedText) {
          
          if (showInMoreActions) {
            moreActionsLabelExternalizedTextKey = grouperConfig.propertyValueStringRequired(configPrefix+"moreActionsLabelExternalizedTextKey");
          }
          templateNameExternalizedTextKey = grouperConfig.propertyValueStringRequired(configPrefix+"templateNameExternalizedTextKey");
          templateDescriptionExternalizedTextKey = grouperConfig.propertyValueStringRequired(configPrefix+"templateDescriptionExternalizedTextKey");
          
        } else {
          if (showInMoreActions) {
            moreActionsLabel = grouperConfig.propertyValueStringRequired(configPrefix+"moreActionsLabel");
          }
          templateName = grouperConfig.propertyValueStringRequired(configPrefix+"templateName");
          templateDescription = grouperConfig.propertyValueStringRequired(configPrefix+"templateDescription");
        }

        displayErrorOutput = grouperConfig.propertyValueBoolean(configPrefix+"displayErrorOutput", false);
        
        actAsGroupUUID = grouperConfig.propertyValueString(configPrefix+"actAsGroupUUID", null);
        
        if (gshTemplateType != GshTemplateType.provisioner) {
          String runAsType = grouperConfig.propertyValueStringRequired(configPrefix+"runAsType");
          gshTemplateRunAsType = GshTemplateRunAsType.valueOfIgnoreCase(runAsType, true);
        } else {
          gshTemplateRunAsType = GshTemplateRunAsType.GrouperSystem;
        }
        
        if (gshTemplateRunAsType == GshTemplateRunAsType.specifiedSubject) {
          runAsSpecifiedSubjectSourceId = grouperConfig.propertyValueStringRequired(configPrefix+"runAsSpecifiedSubjectSourceId");
          runAsSpecifiedSubjectId = grouperConfig.propertyValueStringRequired(configPrefix+"runAsSpecifiedSubjectId");
        }
        
        showOnGroups = grouperConfig.propertyValueBoolean(configPrefix+"showOnGroups", false);
        
        if (showOnGroups) {
          gshTemplateGroupShowType = GshTemplateGroupShowType.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(configPrefix+"groupShowType"), true);
          
          if (gshTemplateGroupShowType == GshTemplateGroupShowType.certainGroups) {
            String groupUuidsToShow = grouperConfig.propertyValueStringRequired(configPrefix+"groupUuidsToShow");
            
            String[] groupUuidsOrNames = GrouperUtil.splitTrim(groupUuidsToShow, ",");
            for (String groupUuidOrName: groupUuidsOrNames) {
              Group groupToShow = null;
              if (grouperObjectTypeIdOrNameToGrouperObject != null) {
                MultiKey multiKeyUuid = new MultiKey("group", groupUuidOrName);
                groupToShow = (Group)grouperObjectTypeIdOrNameToGrouperObject.get(multiKeyUuid);
              } else {
                groupToShow = GroupFinder.findByUuid(grouperSession, groupUuidOrName, false);
                if (groupToShow == null) {
                  groupToShow = GroupFinder.findByName(grouperSession, groupUuidOrName, false);
                }
              }
              GrouperUtil.assertion(groupToShow != null, "could not find group for groupUuidOrName: "+groupUuidOrName);
              groupsToShow.add(groupToShow);
            }
            
          } else if (gshTemplateGroupShowType == GshTemplateGroupShowType.groupsInFolder) {
            
            String folderUuidForGroupsInFolder = grouperConfig.propertyValueStringRequired(configPrefix+"folderUuidForGroupsInFolder");
            folderForGroupsInFolder = null;
            if (grouperObjectTypeIdOrNameToGrouperObject != null) {
              MultiKey multiKeyUuid = new MultiKey("stem", folderUuidForGroupsInFolder);
              folderForGroupsInFolder = (Stem)grouperObjectTypeIdOrNameToGrouperObject.get(multiKeyUuid);
            } else {
              folderForGroupsInFolder = StemFinder.findByUuid(grouperSession, folderUuidForGroupsInFolder, false);
              if (folderForGroupsInFolder == null) {
                folderForGroupsInFolder = StemFinder.findByName(grouperSession, folderUuidForGroupsInFolder, false);
              }
            }
            GrouperUtil.assertion(folderForGroupsInFolder != null, "could not find folder for folderUuidForGroupsInFolder: "+folderUuidForGroupsInFolder);
            gshTemplateGroupShowOnDescendants = GshTemplateGroupShowOnDescendants.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(configPrefix+"groupShowOnDescendants"), true);
          }
          
        }
        
        allowWsFromNoOwner = grouperConfig.propertyValueBoolean(configPrefix+"allowWsFromNoOwner", false);
        
        if (gshTemplateType == GshTemplateType.provisioner) {
          allowWsFromNoOwner = true;
        }

        showOnFolders = grouperConfig.propertyValueBoolean(configPrefix+"showOnFolders", false);
        
        if (showOnFolders) {
          gshTemplateFolderShowType = GshTemplateFolderShowType.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(configPrefix+"folderShowType"), true);
          
          if(gshTemplateFolderShowType == GshTemplateFolderShowType.certainFolders) {
            String folderUuidsOrNamesToShow = grouperConfig.propertyValueStringRequired(configPrefix+"folderUuidToShow");
            
            String[] folderUuidsOrNames = GrouperUtil.splitTrim(folderUuidsOrNamesToShow, ",");
            for (String folderUuidOrName: folderUuidsOrNames) {
              Stem folderToShow = null;
              if (grouperObjectTypeIdOrNameToGrouperObject != null) {
                MultiKey multiKeyUuid = new MultiKey("stem", folderUuidOrName);
                folderToShow = (Stem)grouperObjectTypeIdOrNameToGrouperObject.get(multiKeyUuid);
              } else {
                folderToShow = StemFinder.findByUuid(grouperSession, folderUuidOrName, false);
                if (folderToShow == null) {
                  folderToShow = StemFinder.findByName(grouperSession, folderUuidOrName, false);
                }
              }
              GrouperUtil.assertion(folderToShow != null, "could not find folder for folderUuidToShow: "+folderUuidOrName);
              foldersToShow.add(folderToShow);
            }
            
            gshTemplateFolderShowOnDescendants = GshTemplateFolderShowOnDescendants.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(configPrefix+"folderShowOnDescendants"), true);
          }
          
        }
        
        if (gshTemplateType != GshTemplateType.provisioner) {
          gshTemplateSecurityRunType = GshTemplateSecurityRunType.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(configPrefix+"securityRunType"), true);
        }
        
        if (gshTemplateSecurityRunType == GshTemplateSecurityRunType.specifiedGroup) {
          String groupUuidOrNameCanRun = grouperConfig.propertyValueStringRequired(configPrefix+"groupUuidCanRun");
          if (grouperObjectTypeIdOrNameToGrouperObject != null) {
            MultiKey multiKeyUuid = new MultiKey("group", groupUuidOrNameCanRun);
            groupThatCanRun = (Group)grouperObjectTypeIdOrNameToGrouperObject.get(multiKeyUuid);
          } else {
            groupThatCanRun = GroupFinder.findByUuid(grouperSession, groupUuidOrNameCanRun, false);
            if (groupThatCanRun == null) {
              groupThatCanRun = GroupFinder.findByName(grouperSession, groupUuidOrNameCanRun, false);
            }
          }
          GrouperUtil.assertion(groupThatCanRun != null, "could not find group for groupUuidOrNameCanRun: "+groupUuidOrNameCanRun);
        }

        mcpEnabled = grouperConfig.propertyValueBoolean(configPrefix+"mcpEnabled", false);

        if (mcpEnabled) {
          mcpReadonly = grouperConfig.propertyValueBoolean(configPrefix+"mcpReadonly", false);
        }

        if (gshTemplateSecurityRunType == GshTemplateSecurityRunType.privilegeOnObject && showOnGroups) {
          gshTemplateRequireGroupPrivilege =  GshTemplateRequireGroupPrivilege.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(configPrefix+"requireGroupPrivilege"), true);
        }
        
        if (gshTemplateSecurityRunType == GshTemplateSecurityRunType.privilegeOnObject && showOnFolders) {
          gshTemplateRequireFolderPrivilege =  GshTemplateRequireFolderPrivilege.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(configPrefix+"requireFolderPrivilege"), true);
        }
        
        gshTemplateSourceType = grouperConfig.propertyValueString(configPrefix+"gshTemplateSourceType", "textArea");
        
        if (StringUtils.equals(gshTemplateSourceType, "file")) {
          gshTemplateFileName = grouperConfig.propertyValueString(configPrefix+"gshTemplateFileName");
        } else if (StringUtils.equals(gshTemplateSourceType, "textArea")) {
          gshTemplate = grouperConfig.propertyValueStringRequired(configPrefix+"gshTemplate");
        } else {
          throw new RuntimeException("Invalid gshTemplateSourceType: '"+gshTemplateSourceType+"'");
        }

        gshLightweight = grouperConfig.propertyValueBoolean(configPrefix+"gshLightweight", false);

        runGshInTransaction = grouperConfig.propertyValueBoolean(configPrefix+"runGshInTransaction", true);

        int numberOfInputs = grouperConfig.propertyValueInt(configPrefix+"numberOfInputs", 0);
        
        for (int i=0; i<numberOfInputs; i++) {
          
          String inputPrefix = configPrefix + "input." + i + ".";
          
          String inputName = grouperConfig.propertyValueStringRequired(inputPrefix + "name");
          
          GshTemplateInputConfig gshTemplateInputConfig = new GshTemplateInputConfig();
          
          gshTemplateInputConfig.setGshTemplateConfig(GshTemplateConfig.this);
          
          gshTemplateInputConfig.setName(inputName);
          
          String valueType = grouperConfig.propertyValueString(inputPrefix + "type", "string");
          GshTemplateInputType gshTemplateInputType = GshTemplateInputType.valueOfIgnoreCase(valueType, true);
          
          gshTemplateInputConfig.setUseExternalizedText(GshTemplateConfig.this.useExternalizedText);
          
          if (useExternalizedText) {
            gshTemplateInputConfig.setLabelExternalizedTextKey(grouperConfig.propertyValueStringRequired(inputPrefix + "labelExternalizedTextKey"));
            gshTemplateInputConfig.setDescriptionExternalizedTextKey(grouperConfig.propertyValueStringRequired(inputPrefix + "descriptionExternalizedTextKey"));
          } else {
            gshTemplateInputConfig.setLabel(grouperConfig.propertyValueStringRequired(inputPrefix + "label"));
            gshTemplateInputConfig.setDescription(grouperConfig.propertyValueStringRequired(inputPrefix + "description"));
          }
          
          gshTemplateInputConfig.setGshTemplateInputType(gshTemplateInputType);
          
          if (gshTemplateInputType == GshTemplateInputType.BOOLEAN) {
            String booleanFormElementType = grouperConfig.propertyValueString(inputPrefix + "formElementTypeForBoolean", "radio");
            if (StringUtils.equalsIgnoreCase("checkbox", booleanFormElementType)) {
              gshTemplateInputConfig.setConfigItemFormElement(ConfigItemFormElement.BOOLEANCHECKBOX);
              gshTemplateInputConfig.setCheckboxLabel(grouperConfig.propertyValueString(inputPrefix + "checkboxLabel", null));
            } else {
              gshTemplateInputConfig.setConfigItemFormElement(ConfigItemFormElement.RADIOBUTTON);
            }
          } else {
            ConfigItemFormElement configItemFormElement = ConfigItemFormElement.valueOfIgnoreCase(grouperConfig.propertyValueString(inputPrefix + "formElementType", "text"), true);
            gshTemplateInputConfig.setConfigItemFormElement(configItemFormElement);
          }
          
          if ((gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.TEXT || gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.TEXTAREA) && gshTemplateInputType != GshTemplateInputType.BOOLEAN) {
            GshTemplateInputValidationType gshTemplateInputValidationType = GshTemplateInputValidationType.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(inputPrefix + "validationType"), true);
            gshTemplateInputConfig.setGshTemplateInputValidationType(gshTemplateInputValidationType);
            
            String validationMessage = grouperConfig.propertyValueString(inputPrefix + "validationMessage");
            gshTemplateInputConfig.setValidationMessage(validationMessage);
            
            String validationMessageExternalizedTextKey = grouperConfig.propertyValueString(inputPrefix + "validationMessageExternalizedTextKey");
            gshTemplateInputConfig.setValidationMessageExternalizedTextKey(validationMessageExternalizedTextKey);
            
            if (gshTemplateInputValidationType == GshTemplateInputValidationType.regex) {
              String validationRegex = grouperConfig.propertyValueStringRequired(inputPrefix + "validationRegex");
              gshTemplateInputConfig.setValidationRegex(validationRegex);
            } else if (gshTemplateInputValidationType == GshTemplateInputValidationType.jexl) {
              String validationJexl = grouperConfig.propertyValueStringRequired(inputPrefix + "validationJexl");
              gshTemplateInputConfig.setValidationJexl(validationJexl);
            } else if (gshTemplateInputValidationType == GshTemplateInputValidationType.builtin) {
              String validationBuiltinTypeString = grouperConfig.propertyValueStringRequired(inputPrefix + "validationBuiltin");
              ValidationBuiltinType validationBuiltinType = ValidationBuiltinType.valueOfIgnoreCase(validationBuiltinTypeString, true);
              gshTemplateInputConfig.setValidationBuiltinType(validationBuiltinType);
            }
          }
          
          boolean required = grouperConfig.propertyValueBoolean(inputPrefix+"required", false);
          gshTemplateInputConfig.setRequired(required);
          
          if (!required) {
            String defaultValue = grouperConfig.propertyValueString(inputPrefix + "defaultValue", null);
            gshTemplateInputConfig.setDefaultValue(defaultValue);
          }
          
          gshTemplateInputConfig.setTrimWhitespace(grouperConfig.propertyValueBoolean(inputPrefix+"trimWhitespace", true));
          gshTemplateInputConfig.setShowEl(grouperConfig.propertyValueString(inputPrefix+"showEl", null));
          gshTemplateInputConfig.setIndex(grouperConfig.propertyValueInt(inputPrefix+"index", 0));
          
          if (gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.DROPDOWN) {
            
            GshTemplateDropdownValueFormatType gshTemplateDropdownValueFormatType = GshTemplateDropdownValueFormatType.valueOfIgnoreCase(grouperConfig.propertyValueString(inputPrefix + "dropdownValueFormat", "csv"), true);
            gshTemplateInputConfig.setGshTemplateDropdownValueFormatType(gshTemplateDropdownValueFormatType);
            
            if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.csv) {
             String dropdownCsvValue = grouperConfig.propertyValueStringRequired(inputPrefix + "dropdownCsvValue");
             gshTemplateInputConfig.setDropdownCsvValue(dropdownCsvValue);
            } else if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.json) {
              String dropdownJsonValue = grouperConfig.propertyValueStringRequired(inputPrefix + "dropdownJsonValue");
              gshTemplateInputConfig.setDropdownJsonValue(dropdownJsonValue);
            } else if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.dynamicFromTemplate) {
              // let this happen
            } else if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.javaclass) {
              String dropdownJavaClassValue = grouperConfig.propertyValueStringRequired(inputPrefix + "dropdownJavaClassValue");
              gshTemplateInputConfig.setDropdownJavaClassValue(dropdownJavaClassValue);
            } else if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.sql) {
              String dropdownSqlDatabase = grouperConfig.propertyValueStringRequired(inputPrefix + "dropdownSqlDatabase");
              gshTemplateInputConfig.setDropdownSqlDatabase(dropdownSqlDatabase);
              String dropdownSqlValue = grouperConfig.propertyValueStringRequired(inputPrefix + "dropdownSqlValue");
              gshTemplateInputConfig.setDropdownSqlValue(dropdownSqlValue);
              int dropdownSqlCacheForMinutes = grouperConfig.propertyValueInt(inputPrefix + "dropdownSqlCacheForMinutes", 2);
              gshTemplateInputConfig.setDropdownSqlCacheForMinutes(dropdownSqlCacheForMinutes);
            } else {
              throw new RuntimeException("Not expecting drop down value format type: " + gshTemplateInputConfig.getGshTemplateDropdownValueFormatType());
            }
          } else {
            int maxLength = grouperConfig.propertyValueInt(inputPrefix + "maxLength", 500);
            gshTemplateInputConfig.setMaxLength(maxLength);
          }
          
          
          // MCP scope type for this input (only applicable when mcpEnabled and not mcpReadonly)
          if (mcpEnabled && !mcpReadonly) {
            String mcpScopeType = grouperConfig.propertyValueString(inputPrefix + "mcpScopeType", null);
            gshTemplateInputConfig.setMcpScopeType(mcpScopeType);
          }

          gshTemplateInputConfigs.add(gshTemplateInputConfig);

        }

        return null;
      }
    });
    
  }

  /**
   * this will not have imports built in, so have imports in script or fully qualify classes.  Saves 3 seconds of execution
   */
  private boolean gshLightweight = false;

  /**
   * this will not have imports built in, so have imports in script or fully qualify classes.  Saves 3 seconds of execution
   * @return
   */
  public boolean isGshLightweight() {
    return gshLightweight;
  }
  
  /**
   * check if the given folder can run this gsh template
   * @param folder
   * @return
   */
  public boolean canFolderRunTemplate(Stem folder) {
    
    if (!isShowOnFolders()) {
      return false;
    }
    
    if (this.getGshTemplateFolderShowType() == GshTemplateFolderShowType.allFolders) {
      return true;
    }
    
    Set<Stem> foldersToShow = getFoldersToShow();
    if (GrouperUtil.nonNull(foldersToShow).size() == 0) {
      LOG.error("foldersToShow is not configured correctly for template with config id: "+getConfigId());
      return false;
    }
    
    Set<String> foldersToShowUuids = new HashSet<String>();

    for (Stem folderToShow: foldersToShow) {
      foldersToShowUuids.add(folderToShow.getUuid());
    }
    
    GshTemplateFolderShowOnDescendants gshTemplateFolderShowOnDescendants = getGshTemplateFolderShowOnDescendants();
    if (GshTemplateFolderShowOnDescendants.certainFolders == gshTemplateFolderShowOnDescendants) {
      
      if (foldersToShowUuids.contains(folder.getUuid())) {
        return true;
      }
      
      return false;
    } else if (GshTemplateFolderShowOnDescendants.oneChildLevel == gshTemplateFolderShowOnDescendants) {
      
      for (Stem folderToShow: foldersToShow) {
        if (StringUtils.equals(GrouperUtil.parentStemNameFromName(folder.getName(), false), folderToShow.getName())) {
          return true;
        }
      }
      
      return false;
    } else if (GshTemplateFolderShowOnDescendants.certainFoldersAndOneChildLevel == gshTemplateFolderShowOnDescendants) {
      
      if (foldersToShowUuids.contains(folder.getUuid())) {            
        return true;
      } 
      
      for (Stem folderToShow: foldersToShow) {
        if (StringUtils.equals(GrouperUtil.parentStemNameFromName(folder.getName(), false), folderToShow.getName())) {
          return true;
        }
      }
      
      return false;
    } else if (GshTemplateFolderShowOnDescendants.descendants == gshTemplateFolderShowOnDescendants) {
      
      for (Stem folderToShow: foldersToShow) {
        if (folder.getName().startsWith(folderToShow.getName()+":")) {
          return true;
        }
      }
      
      return false;
    } else if (GshTemplateFolderShowOnDescendants.certainFoldersAndDescendants == gshTemplateFolderShowOnDescendants) {
      
      for (Stem folderToShow: foldersToShow) {
        if (folder.getName().startsWith(folderToShow.getName()+":")) {
          return true;
        }
      }
      
      if (foldersToShowUuids.contains(folder.getUuid())) {
        return true;
      }
      
      return false;
      
    } else {
      throw new RuntimeException("Invalid gshTemplateFolderShowOnDescendants: "+gshTemplateFolderShowOnDescendants);
    }
    
  }
  
  /**
   * check if the given group can run this gsh template
   * @param folder
   * @return
   */
  public boolean canGroupRunTemplate(Group group) {
    
    if (!isShowOnGroups()) {
      return false;
    }
    
    if (this.getGshTemplateGroupShowType() == GshTemplateGroupShowType.allGroups) {
      return true;
    } else if (this.getGshTemplateGroupShowType() == GshTemplateGroupShowType.certainGroups) {
      Set<Group> groupsToShow = getGroupsToShow();
      if (groupsToShow.contains(group)) {
        return true;
      }
    } else if (this.getGshTemplateGroupShowType() == GshTemplateGroupShowType.groupsInFolder) {
      
      Stem folderForGroupsInFolder = getFolderForGroupsInFolder();
      
      GshTemplateGroupShowOnDescendants templateGroupShowOnDescendants = getGshTemplateGroupShowOnDescendants();
      
      if (GshTemplateGroupShowOnDescendants.descendants == templateGroupShowOnDescendants) {
        return folderForGroupsInFolder.isChildGroup(group);
      } else if (GshTemplateGroupShowOnDescendants.oneChildLevel == templateGroupShowOnDescendants) {
        return folderForGroupsInFolder.getChildGroups(Scope.ONE).contains(group);
      }
      
    }
    
    return false;
    
  }

  /**
   * some controls might depend on the logged in subject
   */
  private Subject currentUser = null;
  
  /**
   * some controls might depend on the logged in subject
   * @param loggedInSubject
   */
  public void setCurrentUser(Subject loggedInSubject) {
    this.currentUser = loggedInSubject;
  }

  /**
   * some controls might depend on the logged in subject
   * @return current user
   */
  public Subject getCurrentUser() {
    return currentUser;
  }



  public GshTemplateInputConfig retrieveGshTemplateInputConfig(String gshInputName) {
    for (GshTemplateInputConfig gshTemplateInputConfig : GrouperUtil.nonNull(this.getGshTemplateInputConfigs())) {
      if (StringUtils.equals(gshInputName, gshTemplateInputConfig.getName())) {
        return gshTemplateInputConfig;
      }
    }
    throw new RuntimeException("Cannot find config for input: '" + gshInputName + "'");
  }

}
