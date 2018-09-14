package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.rules.RuleApi;
import edu.internet2.middleware.subject.Subject;

public enum ServiceActionType {
  
  stem {
    
    public void createTemplateItem(ServiceAction serviceAction) {
     
      new StemSave(GrouperSession.staticGrouperSession())
      .assignName(serviceAction.getArgMap().get("stemName"))
      .assignDisplayName(serviceAction.getArgMap().get("stemDisplayName"))
      .assignDescription(serviceAction.getArgMap().get("stemDescription"))
      .save();
      
    }
    
  },
  group {
    
    public void createTemplateItem(ServiceAction serviceAction) {
      
      new GroupSave(GrouperSession.staticGrouperSession())
      .assignName(serviceAction.getArgMap().get("groupName"))
      .assignDisplayName(serviceAction.getArgMap().get("groupDisplayName"))
      .assignDescription(serviceAction.getArgMap().get("groupDescription"))
      .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .save();
      
    }
    
  },
  
  inheritedPrivilege {
    
    public void createTemplateItem(ServiceAction serviceAction) {
      
      String groupName = serviceAction.getArgMap().get("groupName");
      String parentStemName = serviceAction.getArgMap().get("parentStemName");
      String privilegeName = serviceAction.getArgMap().get("internalPrivilegeName");
      String templateItemType = serviceAction.getArgMap().get("templateItemType");
      
      final GrouperSession session = GrouperSession.staticGrouperSession();
      
      final Stem stem = StemFinder.findByName(session, parentStemName, true);
      
      Group group = GroupFinder.findByName(session, groupName, true);
      final Subject sub = SubjectFinder.findById(group.getId(), true);
      
      Privilege priv = Privilege.getInstance(privilegeName);
      
      final Set<Privilege> privs = new HashSet<Privilege>();
      privs.add(priv);
      
      if (templateItemType.equals("Folders")) {
        RuleApi.inheritFolderPrivileges(session.getSubject(), stem, Scope.SUB, sub, privs);
      } else if (templateItemType.equals("Groups")) {
        RuleApi.inheritGroupPrivileges(session.getSubject(), stem, Scope.SUB, sub, privs);
      } else if (templateItemType.equals("Attributes")) {
        RuleApi.inheritAttributeDefPrivileges(session.getSubject(), stem, Scope.SUB, sub, privs);
      }
      
    }
    
  },

 membership {
    
    public void createTemplateItem(ServiceAction serviceAction) {
      
      String groupName = serviceAction.getArgMap().get("groupNameMembership");
      String memberOf = serviceAction.getArgMap().get("groupNameMembershipOf");
      
      Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, true);
      
      Group groupMemberOf = GroupFinder.findByName(GrouperSession.staticGrouperSession(), memberOf, true);
      Subject sub = SubjectFinder.findByIdOrIdentifier(group.getId(), true);
      groupMemberOf.addMember(sub, false);
      
    }
    
  };
  
  
  public abstract void createTemplateItem(ServiceAction serviceAction);

}
