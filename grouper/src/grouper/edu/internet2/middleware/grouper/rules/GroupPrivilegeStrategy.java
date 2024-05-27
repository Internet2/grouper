package edu.internet2.middleware.grouper.rules;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.subject.Subject;

public enum GroupPrivilegeStrategy {
  
  read {

    @Override
    public boolean canSubjectViewGroup(Subject subject, Group group) {
      return group.canHavePrivilege(subject, AccessPrivilege.READ.getName(), false);
    }
    
  };
  
  public abstract boolean canSubjectViewGroup(Subject subject, Group group);

}
