/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.group.LockoutGroup;
import edu.internet2.middleware.grouper.group.RequireGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author vsachdeva
 *
 */
public class GrouperTemplatePolicyGroupLogic extends GrouperTemplateLogicBase {
  
  /**
   * @see edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperTemplateLogicBase#initScreen()
   */
  @Override
  public void initScreen() {
    super.initScreen();
    
    StemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemTemplateContainer();
    
    templateContainer.setShowInThisFolderCheckbox(false);
    
  }
  
  /**
   * @see edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperTemplateLogicBase#validate(java.util.List)
   */
  @Override
  public boolean validate(List<ServiceAction> selectedServiceActions) {
    
    // TODO validate service actions
    
    return super.validate(selectedServiceActions);
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperTemplateLogicBase#postCreateSelectedActions(java.util.List)
   */
  @Override
  public String postCreateSelectedActions(List<ServiceAction> selectedServiceActions) {
    
    if (GrouperUtil.length(selectedServiceActions) == 0) {
      return null;
    }
    
    // lets get a list of require groups
    List<ServiceAction> requireGroupsActions = new ArrayList<ServiceAction>();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group overallGroup = null;
    Group allowGroup = null;
    Group denyGroup = null;

    Set<String> requireGroupExtensionsUsed = new HashSet<String>();
    
    for (ServiceAction serviceAction : GrouperUtil.nonNull(selectedServiceActions)) {
      if (serviceAction.getId().startsWith("policyGroupRequireGroup_")) {
        requireGroupsActions.add(serviceAction);
      }
      if (StringUtils.equals(serviceAction.getId(), "policyGroupCreate")) {
        overallGroup = GroupFinder.findByName(grouperSession, serviceAction.getArgMap().get("groupName"), true);
      }
      if (StringUtils.equals(serviceAction.getId(), "policyGroupAllowGroupCreate")) {
        allowGroup = GroupFinder.findByName(grouperSession, serviceAction.getArgMap().get("groupName"), true);
      }
      if (StringUtils.equals(serviceAction.getId(), "policyGroupDenyGroupCreate")) {
        denyGroup = GroupFinder.findByName(grouperSession, serviceAction.getArgMap().get("groupName"), true);
      }
    }
    
    final Group ALLOW_GROUP = allowGroup;
    
    // now we have the list, in order of require group service actions
    
    // lets do some cases
    if (overallGroup == null) {
      return null;
    }
    
    if (denyGroup != null && allowGroup == null) {
      return "policyGroupDenyNeedsAllow";
    }      
        
    //lets make the require helpers
    boolean needsLastLevelHelper = allowGroup != null && denyGroup != null;

    List<Group> compositeGroupList = new ArrayList<Group>();
    
    // overall group is at the top
    compositeGroupList.add(overallGroup);
    
    Map<Group, ServiceAction> mapFromGroupToAction = new HashMap();
    
    ServiceAction lastServiceActionDoesntNeedHelper = null;
    
    // lets find or create these groups
    for (int i = 0; i<requireGroupsActions.size();i++) {
      
      ServiceAction serviceAction = requireGroupsActions.get(i);
      boolean isLast = (i == requireGroupsActions.size()-1);
      if (isLast && !needsLastLevelHelper) {
        lastServiceActionDoesntNeedHelper = serviceAction;
        break;
      }
      
      Map<String, String> argMap = serviceAction.getArgMap();
      // find a valid group extension
      String groupName = argMap.get("groupName");
      String groupDisplayName = argMap.get("groupDisplayName");
      String overallGroupDisplayName = argMap.get("overallGroupDisplayName");
      String overallGroupName = argMap.get("overallGroupName");
      String compositeGroupDescription = argMap.get("compositeGroupDescription");
      String groupExtension = GrouperUtil.extensionFromName(groupName);
      String groupDisplayExtension = GrouperUtil.extensionFromName(groupDisplayName);

      if (requireGroupExtensionsUsed.contains(groupExtension)) {
        for (int j=0;j<100;j++) {
          String newExtension = groupExtension + "_" + j;
          if (!requireGroupExtensionsUsed.contains(newExtension)) {
            groupDisplayExtension = groupDisplayExtension + "_" + j;
            groupExtension = newExtension;
            break;
          }
        }
      }
      
      Group requireGroup = new GroupSave(grouperSession).assignName(overallGroupName + "_preRequire_" + groupExtension)
          .assignDisplayName(overallGroupDisplayName + "_preRequire_" + groupDisplayExtension).assignDescription(compositeGroupDescription).save();
      compositeGroupList.add(requireGroup);
      mapFromGroupToAction.put(requireGroup, serviceAction);
      requireGroupExtensionsUsed.add(groupExtension);

    }

    for (int i=0;i<compositeGroupList.size();i++) {
      final Group currentGroup = compositeGroupList.get(i);
      
      //is last
      boolean isLast = (i == compositeGroupList.size() - 1);
      
      // then we have a composite
      if (!isLast) {
        final Group nextGroup = compositeGroupList.get(i+1);
        ServiceAction nextServiceAction = mapFromGroupToAction.get(nextGroup);
        
        if (groupMemberListSize(currentGroup.getName()) > 0)  {
          return "policyGroupCompositeGroupHasMembers";
        }
        
        final String groupNameOfIntersection = nextServiceAction.getArgMap().get("groupName");
        
          GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            Group groupOfIntersection = GroupFinder.findByName(grouperSession, groupNameOfIntersection, true);
            currentGroup.assignCompositeMember(CompositeType.INTERSECTION, nextGroup, groupOfIntersection);
            return null;
          }
        });
        continue;
      }
      if (isLast && lastServiceActionDoesntNeedHelper != null) {
        if (groupMemberListSize(currentGroup.getName()) > 0)  {
          return "policyGroupCompositeGroupHasMembers";
        }
        final String groupNameOfIntersection = lastServiceActionDoesntNeedHelper.getArgMap().get("groupName");
        
        GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            Group groupOfIntersection = GroupFinder.findByName(grouperSession, groupNameOfIntersection, true);
            currentGroup.assignCompositeMember(CompositeType.INTERSECTION, ALLOW_GROUP, groupOfIntersection);
            return null;
          }
        });
        continue;
      }
      // now we are at the last group
      if (isLast && (allowGroup != null && denyGroup != null)) {
        currentGroup.assignCompositeMember(CompositeType.COMPLEMENT, allowGroup, denyGroup);
        // we done
        continue;
      }
      
      if (isLast && (allowGroup != null && denyGroup == null)) {
        currentGroup.addMember(allowGroup.toSubject());
      }
    }
    return null;
  }

  /**
   * get the number of members in a group
   * @param groupName
   * @return number of members
   */
  public static int groupMemberListSize(String groupName) {
    //lets see if it has members
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.retrieveCount(true);
    queryOptions.retrieveResults(false);
    new MembershipFinder().addField(Group.getDefaultList()).addGroup(groupName).assignQueryOptionsForMember(queryOptions).findMembershipResult();
    return queryOptions.getCount().intValue();
  }
  
  /**
    Do you want a "app:policyGroup" group created? (ID is "policyGroup", name is "Policy Group")
      Assign "policy" type to the "app:policyGroup" group?
    Do you want a "app:policyGroup_allow" group created?
    Do you want a "app:policyGroup_deny" group created?
      Do you want the "ref:lockOutGroup" added to the "app:policyGroup_deny" group?
    Do you want a "app:policyGroup_allow_adhoc" group created?
    Do you want a "app:policyGroup_deny_adhoc" group created?
    Do you want to require "ref:active" to the overall group?

    policyGroup is policyGroupPreRequireActive intersect active
    policyGroupPreRequireActive is policyGroup_allow minus policyGroup_deny

   */
  @Override
  public List<ServiceAction> getServiceActions() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    Stem stem = StemFinder.findByUuid(grouperSession, this.getStemId(), true);
    
    StemTemplateContainer templateContainer = this.getStemTemplateContainer();
    String baseGroup = templateContainer.getTemplateKey();
    String baseGroupFriendlyName = templateContainer.getTemplateFriendlyName();

    
    if (StringUtils.isBlank(baseGroupFriendlyName)) {
      baseGroupFriendlyName = baseGroup;
    }
    
    String baseGroupDescription = StringUtils.isBlank(templateContainer.getTemplateDescription()) ?
        TextContainer.retrieveFromRequest().getText().get("policyGroupTemplateBaseGroupDescription"): templateContainer.getTemplateDescription();
    
    String stemPrefix = "";
    String stemPrefixDisplayName = "";
    
    boolean addFirstNode = false;
    String optionalColon = "";
    
    List<ServiceAction> serviceActionsForStem = new ArrayList<ServiceAction>();

    if (StringUtils.isBlank(baseGroup)) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
          TextContainer.retrieveFromRequest().getText().get("policyGroupTemplateRootFolderError")));
      return serviceActionsForStem;
      
    }


    if (stem.isRootStem()) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
          TextContainer.retrieveFromRequest().getText().get("policyGroupTemplateRootFolderError")));
      return serviceActionsForStem;
    }
    
    stemPrefix = stem.getName()+":";
    stemPrefixDisplayName = stem.getDisplayName()+":";
    optionalColon = ":";
    addFirstNode = true;

    List<ServiceActionArgument> args = new ArrayList<ServiceActionArgument>();
  
    //Do you want a "app:policyGroup" group created? (ID is "policyGroup", name is "Policy Group")

    ServiceAction overallGroupAction = null;
    String overallGroupDisplayName = stemPrefixDisplayName + baseGroupFriendlyName;
    String overallGroupName = stemPrefix + baseGroup;
    {
    
      args.add(new ServiceActionArgument("groupName", overallGroupName));
      args.add(new ServiceActionArgument("groupDisplayName", overallGroupDisplayName));
      args.add(new ServiceActionArgument("groupDescription", baseGroupDescription));

      overallGroupAction = createNewServiceAction("policyGroupCreate", true, 0, "policyGroupOverallCreationLabel",
        ServiceActionType.group, args, null);
      serviceActionsForStem.add(overallGroupAction);
      //levelTwoServiceAction_Four.addChildServiceAction(levelThreeServiceAction_Three);

      {
        //Assign the "policy" type to the policy group?
        args = new ArrayList<ServiceActionArgument>();
        args.add(new ServiceActionArgument("groupName", overallGroupName));
        args.add(new ServiceActionArgument("groupDisplayName", overallGroupDisplayName));
        args.add(new ServiceActionArgument("type", GrouperObjectTypesSettings.POLICY));
        ServiceAction policyTypeAction = createNewServiceAction("policyGroupType", true, 1, "stemServiceGroupTypeConfirmation", ServiceActionType.grouperType, args, null);
        
        serviceActionsForStem.add(policyTypeAction);
        overallGroupAction.addChildServiceAction(policyTypeAction);
      }

    }
    
    {
      // Do you want a "app:policyGroup_allow" group created?
      String allowGroupName = stemPrefix + baseGroup + "_allow";
      String allowGroupDisplayName = stemPrefixDisplayName + baseGroupFriendlyName + "_allow";
    
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", allowGroupName));
      args.add(new ServiceActionArgument("groupDisplayName", allowGroupDisplayName));
      args.add(new ServiceActionArgument("overallGroupDisplayName", overallGroupDisplayName));
      ServiceAction allowGroupAction = createNewServiceAction("policyGroupAllowGroupCreate", true, 1, "policyGroupAllowCreationLabel",
          ServiceActionType.group, args, null);

      templateContainer.setCurrentServiceAction(allowGroupAction);
      
      allowGroupAction.getArgs().add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("policyGroupAllowDescription")));

      serviceActionsForStem.add(allowGroupAction);

      templateContainer.setCurrentServiceAction(null);

      overallGroupAction.addChildServiceAction(allowGroupAction);

      //  Do you want a "app:policyGroup_allow_adhoc" group created?
      {
        String allowAdhocGroupName = stemPrefix + baseGroup + "_allow_adhoc";
        String allowAdhocGroupDisplayName = stemPrefixDisplayName + baseGroupFriendlyName + "_allow_adhoc";
      
        args = new ArrayList<ServiceActionArgument>();
        args.add(new ServiceActionArgument("groupName", allowAdhocGroupName));
        args.add(new ServiceActionArgument("groupDisplayName", allowAdhocGroupDisplayName));
        args.add(new ServiceActionArgument("overallGroupDisplayName", overallGroupDisplayName));
        ServiceAction allowAdhocGroupAction = createNewServiceAction("policyGroupAllowAdhocGroupCreate", false, 2, "policyGroupAllowAdhocCreationLabel",
            ServiceActionType.group, args, null);

        templateContainer.setCurrentServiceAction(allowAdhocGroupAction);
        
        allowAdhocGroupAction.getArgs().add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("policyGroupAllowAdhocDescription")));

        serviceActionsForStem.add(allowAdhocGroupAction);

        templateContainer.setCurrentServiceAction(null);
        allowGroupAction.addChildServiceAction(allowAdhocGroupAction);

        {
          //    Do you want the "app:policyGroup_allow_adhoc" added to the "app:policyGroup_allow" group?
          
          args = new ArrayList<ServiceActionArgument>();
          args.add(new ServiceActionArgument("groupNameMembership", allowAdhocGroupName));
          args.add(new ServiceActionArgument("groupDisplayNameMembership", allowAdhocGroupDisplayName));
          args.add(new ServiceActionArgument("groupNameMembershipOf", allowGroupName));
          args.add(new ServiceActionArgument("groupDisplayNameMembershipOf", allowGroupDisplayName));

          ServiceAction addAdhocToAllowMembershipAction = createNewServiceAction("policyGroupAddAdhocToAllow", false, 3, "policyGroupMembershipsLabel",
              ServiceActionType.membership, args, null);
          
          serviceActionsForStem.add(addAdhocToAllowMembershipAction);
          
          allowAdhocGroupAction.addChildServiceAction(addAdhocToAllowMembershipAction);

        }
      }
      
    }
      
    {
      // Do you want a "app:policyGroup_deny" group created?
      String denyGroupName = stemPrefix + baseGroup + "_deny";
      String denyGroupDisplayName = stemPrefixDisplayName + baseGroupFriendlyName + "_deny";
    
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", denyGroupName));
      args.add(new ServiceActionArgument("groupDisplayName", denyGroupDisplayName));
      args.add(new ServiceActionArgument("overallGroupDisplayName", overallGroupDisplayName));
      ServiceAction denyGroupAction = createNewServiceAction("policyGroupDenyGroupCreate", true, 1, "policyGroupDenyCreationLabel",
          ServiceActionType.group, args, null);

      templateContainer.setCurrentServiceAction(denyGroupAction);
      
      denyGroupAction.getArgs().add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("policyGroupDenyDescription")));

      templateContainer.setCurrentServiceAction(null);

      serviceActionsForStem.add(denyGroupAction);

      overallGroupAction.addChildServiceAction(denyGroupAction);
      
      int i=0;
      for (LockoutGroup lockoutGroup : GrouperUtil.nonNull(LockoutGroup.retrieveAllLockoutGroups(grouperSession.getSubject()))){
        //    Do you want the "ref:lockOutGroup" added to the "app:policyGroup_deny" group?
      
        args = new ArrayList<ServiceActionArgument>();
        args.add(new ServiceActionArgument("groupNameMembership", lockoutGroup.getName()));
        args.add(new ServiceActionArgument("groupDisplayNameMembership", lockoutGroup.getLockoutGroup().getDisplayName()));
        args.add(new ServiceActionArgument("groupNameMembershipOf", denyGroupName));
        args.add(new ServiceActionArgument("groupDisplayNameMembershipOf", denyGroupName));

        ServiceAction denyMembershipAction = createNewServiceAction("policyGroupLockoutGroup_" + i, true, 2, "policyGroupMembershipsLabel",
            ServiceActionType.membership, args, null);
        
        serviceActionsForStem.add(denyMembershipAction);
        
        denyGroupAction.addChildServiceAction(denyMembershipAction);
        i++;
      }

      //  Do you want a "app:policyGroup_deny_adhoc" group created?
      {
        String denyAdhocGroupName = stemPrefix + baseGroup + "_deny_adhoc";
        String denyAdhocGroupDisplayName = stemPrefixDisplayName + baseGroupFriendlyName + "_deny_adhoc";
      
        args = new ArrayList<ServiceActionArgument>();
        args.add(new ServiceActionArgument("groupName", denyAdhocGroupName));
        args.add(new ServiceActionArgument("groupDisplayName", denyAdhocGroupDisplayName));
        args.add(new ServiceActionArgument("overallGroupDisplayName", overallGroupDisplayName));
        ServiceAction denyAdhocGroupAction = createNewServiceAction("policyGroupDenyAdhocGroupCreate", false, 2, "policyGroupDenyAdhocCreationLabel",
            ServiceActionType.group, args, null);

        templateContainer.setCurrentServiceAction(denyAdhocGroupAction);
        
        denyAdhocGroupAction.getArgs().add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("policyGroupDenyAdhocDescription")));

        serviceActionsForStem.add(denyAdhocGroupAction);

        templateContainer.setCurrentServiceAction(null);
        denyGroupAction.addChildServiceAction(denyAdhocGroupAction);

        {
          //    Do you want the "app:policyGroup_deny_adhoc" added to the "app:policyGroup_deny" group?
          
          args = new ArrayList<ServiceActionArgument>();
          args.add(new ServiceActionArgument("groupNameMembership", denyAdhocGroupName));
          args.add(new ServiceActionArgument("groupDisplayNameMembership", denyAdhocGroupDisplayName));
          args.add(new ServiceActionArgument("groupNameMembershipOf", denyGroupName));
          args.add(new ServiceActionArgument("groupDisplayNameMembershipOf", denyGroupDisplayName));

          ServiceAction addAdhocToDenyMembershipAction = createNewServiceAction("policyGroupAddAdhocToDeny", false, 3, "policyGroupMembershipsLabel",
              ServiceActionType.membership, args, null);
          
          serviceActionsForStem.add(addAdhocToDenyMembershipAction);
          
          denyAdhocGroupAction.addChildServiceAction(addAdhocToDenyMembershipAction);

        }
      }
      
      i=0;
      for (RequireGroup requireGroup : GrouperUtil.nonNull(RequireGroup.retrieveAllRequireGroups(grouperSession.getSubject()))){
        // Do you want to require "ref:active" to the overall group?
      
        args = new ArrayList<ServiceActionArgument>();
        args.add(new ServiceActionArgument("groupName", requireGroup.getName()));
        args.add(new ServiceActionArgument("groupDisplayName", requireGroup.getRequireGroup().getDisplayName()));
        args.add(new ServiceActionArgument("overallGroupDisplayName", overallGroupDisplayName));
        args.add(new ServiceActionArgument("overallGroupName", overallGroupName));

        ServiceAction requireGroupAction = createNewServiceAction("policyGroupRequireGroup_" + i, false, 1, "policyGroupRequireGroupLabel",
            ServiceActionType.noAction, args, null);
        
        templateContainer.setCurrentServiceAction(requireGroupAction);

        requireGroupAction.getArgs().add(new ServiceActionArgument("compositeGroupDescription", TextContainer.retrieveFromRequest().getText().get("policyGroupRequireGroupDescription")));

        serviceActionsForStem.add(requireGroupAction);
        
        templateContainer.setCurrentServiceAction(null);

        overallGroupAction.addChildServiceAction(requireGroupAction);
        i++;
      }

    }
        
    return serviceActionsForStem;
    
  }
  
  
  /**
   * external text property
   */
  public String getSelectLabelKey() {
    return "policyGroupTemplateTypeLabel";
  }

}
