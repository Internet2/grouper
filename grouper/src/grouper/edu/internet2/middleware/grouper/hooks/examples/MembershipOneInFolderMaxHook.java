/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks.examples;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Hook allows only one membership in a folder at a time
 */
public class MembershipOneInFolderMaxHook extends MembershipHooks {

  /**
   * 
   */
  public static final String HOOK_VETO_MEMBERSHIP_ONE_IN_FOLDER_CANT_DELETE_MEMBER = "hook.veto.membershipOneInFolder.cantDeleteMember";

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(MembershipOneInFolderMaxHook.class);

  /**
   * base stem for these attributes (just in hooks folder)
   * @return the stem name
   */
  public static String membershipOneFolderStemName() {
    return GrouperCheckConfig.attributeRootStemName() + ":hooks";
  }
  
  /**
   * see if initted
   */
  static boolean inittedOnce = false;
  
  /**
   * @param inCheckConfig
   */
  public static void initObjectsOnce(final boolean inCheckConfig) {
    if (inittedOnce) {
      return;
    }

    synchronized (MembershipOneInFolderMaxHook.class) {
      if (inittedOnce) {
        return;
      }
    
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          String membershipOneHookStemName = GrouperCheckConfig.attributeRootStemName() + ":hooks";
          Stem membershipOneHookStem = StemFinder.findByName(grouperSession, membershipOneHookStemName, false);
          if (membershipOneHookStem == null) {
            membershipOneHookStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
              .assignDescription("folder for hooks settings").assignName(membershipOneHookStemName)
              .save();
          }
          
          //see if attributeDef is there
          String membershipOneDefName = membershipOneHookStemName + ":membershipOneInFolderDef";
          AttributeDef membershipOneDef = new AttributeDefSave(grouperSession).assignName(membershipOneDefName)
            .assignAttributeDefPublic(false).assignAttributeDefType(AttributeDefType.attr)
            .assignMultiAssignable(false).assignMultiValued(false).assignToStem(true).assignValueType(AttributeDefValueType.marker).save();
          
      //    String membershipOneAssignmentDefName = membershipOneHookStemName + ":membershipOneInFolderAssignmentDef";
          
      //    AttributeDef membershipOneAssignmentDef = new AttributeDefSave(grouperSession).assignName(membershipOneAssignmentDefName)
      //      .assignAttributeDefPublic(false).assignAttributeDefType(AttributeDefType.attr)
      //      .assignMultiAssignable(false).assignMultiValued(false).assignToStemAssn(true).assignValueType(AttributeDefValueType.string).save();
      
          //this is publicly assignable and readable
          //membershipOneDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
          //membershipOneDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
          
          //add the only name
          GrouperCheckConfig.checkAttribute(
              membershipOneHookStem, membershipOneDef, MembershipOneInFolderMaxHook.membershipOneFolderExtensionOfAttributeDefName,
              MembershipOneInFolderMaxHook.membershipOneFolderExtensionOfAttributeDefName,
              "put this attribute on a folder to ensure there is one membership only for any group in folder", inCheckConfig);
      //    checkAttribute(
      //        membershipOneHookStem, membershipOneAssignmentDef, MembershipOneInFolderMaxHook.membershipOneFolderScopeExtensionOfAttributeDefName, 
      //        "put this attribute on the hookMembershipOneInFolder attribute assignment to specify if ONE or SUB for if the "
      //        + "membership is only for this folder or for all subfolders too", wasInCheckConfig);
          inittedOnce = true;
          return null;
        }
      });
    }
  }
  
  /**
   * put this attribute on a folder to ensure there is one membership only for any group in folder
   */
  public static final String membershipOneFolderExtensionOfAttributeDefName = "hookMembershipOneInFolder";
  
//  /**
//   * put this attribute on the hookMembershipOneInFolder attribute assignment to specify if ONE or SUB
//   * for if the membership is only for this folder or for all subfolders too
//   */
//  public static final String membershipOneFolderScopeExtensionOfAttributeDefName = "hookMembershipOneInFolderScope";

  /**
   * cache if stem name has the membership one attribute
   */
  private static GrouperCache<String, Boolean> stemHasMembershipOneAttribute = new GrouperCache(
      MembershipOneInFolderMaxHook.class.getName() + ".membershipOneAttribute", 5000, false, 60, 60, false);
  //TODO remove defaults in 2.3+
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPreAddMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean)
   */
  @Override
  public void membershipPreAddMember(HooksContext hooksContext,
      final HooksMembershipChangeBean preAddMemberBean) {

    if (!FieldType.LIST.equals(preAddMemberBean.getField().getType())) {
      return;
    }

    if (GrouperCheckConfig.inCheckConfig || !GrouperStartup.isFinishedStartupSuccessfully()) {
      return;
    }

    initObjectsOnce(false);

    final Set<Group> problemGroups = new HashSet<Group>();

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        
        //lets see if we are limiting for this membership
        Group group = preAddMemberBean.getGroup();
        
        String parentStemName = group.getParentStemName();
        
        Boolean membershipOne = stemHasMembershipOneAttribute.get(parentStemName);

        //if checking, see what memberships are in this folder
        Stem parentStem = null;

        if (membershipOne == null) {

          if (parentStem == null) {
            parentStem = group.getParentStem();
          }
          
          //if not in cache that we check this stem, add it
          AttributeDefName membershipOneAttributeDefName = membershipOneInFolderAttributeDefName();
          
          membershipOne = parentStem.getAttributeDelegate().hasAttribute(membershipOneAttributeDefName);

          stemHasMembershipOneAttribute.put(parentStemName, membershipOne);
        }
          
        if (membershipOne) {

          if (parentStem == null) {
            parentStem = group.getParentStem();
          }

          Set<MembershipSubjectContainer> membershipSubjectContainers = new MembershipFinder()
            .assignStem(parentStem).assignStemScope(Scope.ONE).assignField(Group.getDefaultList())
            .assignMemberIds(GrouperUtil.toSet(preAddMemberBean.getMember().getId()))
            .assignEnabled(true).findMembershipResult().getMembershipSubjectContainers();
          
          for (MembershipSubjectContainer membershipSubjectContainer : GrouperUtil.nonNull(membershipSubjectContainers)) {

            Group currentGroup = membershipSubjectContainer.getGroupOwner();
            if (!StringUtils.equals(currentGroup.getName(), group.getName())) {
              problemGroups.add(currentGroup);
            }
            
          }
          
        }
        return null;
      }
    });
    

    //now we are out of the GrouperSystem, and we need to remove memberships
    //do this with the privileges of the calling user.  if they cant, throw a veto
    for (Group currentGroup : problemGroups) {
      try {
        currentGroup.deleteMember(preAddMemberBean.getMember());
      } catch (Exception e) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Problem auto removing from " + currentGroup.getName() + ", " + GrouperUtil.subjectToString(GrouperSession.staticGrouperSession().getSubject()), e);
        }
        throw new HookVeto(HOOK_VETO_MEMBERSHIP_ONE_IN_FOLDER_CANT_DELETE_MEMBER, "This folder only allows one membership in any group, "
            + "but there was a problem removing from " + currentGroup.getName() + ", do you have privileges to remove memberships there?");
      }
    }
    
  }

  /**
   * @return attribute def name for this hook
   */
  public static AttributeDefName membershipOneInFolderAttributeDefName() {
    initObjectsOnce(false);
    return AttributeDefNameFinder.findByNameCache(membershipOneFolderStemName() + ":" + membershipOneFolderExtensionOfAttributeDefName, true);
  }

  /**
   * pass in the stem and assign attribute and clear cache
   * @param stem
   */
  public static void assignMembershipOneInFolderAttributeDefName(Stem stem) {
    
    stem.getAttributeDelegate().assignAttribute(MembershipOneInFolderMaxHook.membershipOneInFolderAttributeDefName());
    stemHasMembershipOneAttribute.clear();
  }


}
