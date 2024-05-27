package edu.internet2.middleware.grouper.rules;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeGroupInheritanceFinder;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.privs.PrivilegeStemInheritanceFinder;
import edu.internet2.middleware.subject.Subject;

public enum StemPrivilegeStrategy {
  
  adminOrInheritedStemAdmin {

    @Override
    public boolean canSubjectViewStem(Subject subject, Stem stem) {
      
      if (PrivilegeHelper.canStemAdmin(stem, subject)) {
        return true;
      }
      
      return new PrivilegeStemInheritanceFinder().assignStem(stem).assignSubject(subject)
      .assignPrivilege(NamingPrivilege.STEM_ADMIN).assignRunAsRoot(true).hasEffectivePrivilege();
      
    }
  },
  
  adminOrInheritedGroupAdmin {
    
    @Override
    public boolean canSubjectViewStem(Subject subject, Stem stem) {
      if (PrivilegeHelper.canStemAdmin(stem, subject)) {
        return true;
      }
      return new PrivilegeGroupInheritanceFinder().assignStem(stem).assignSubject(subject)
          .assignPrivilege(AccessPrivilege.READ).assignRunAsRoot(true).hasEffectivePrivilege();
    }
    
  },

  adminOrInheritedAttributeDefAdmin {
    
    @Override
    public boolean canSubjectViewStem(Subject subject, Stem stem) {
      if (PrivilegeHelper.canStemAdmin(stem, subject)) {
        return true;
      }
      
      return new PrivilegeGroupInheritanceFinder().assignStem(stem).assignSubject(subject)
          .assignPrivilege(AttributeDefPrivilege.ATTR_ADMIN)
          .assignRunAsRoot(true).hasEffectivePrivilege();
      
    }
    
  },
  
  inheritedRead {

    @Override
    public boolean canSubjectViewStem(Subject subject, Stem stem) {
      
      if (PrivilegeHelper.canStemAdmin(stem, subject)) {
        return true;
      }
      
      return new PrivilegeGroupInheritanceFinder().assignStem(stem).assignSubject(subject)
          .assignPrivilege(AccessPrivilege.READ).assignRunAsRoot(true).hasEffectivePrivilege();
    }
  };
  
  public abstract boolean canSubjectViewStem(Subject subject, Stem stem);

}
