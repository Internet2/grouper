package edu.internet2.middleware.grouper.app.upgradeTasks;

import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.app.usdu.UsduSettings;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.misc.GrouperVersion;

public class UpgradeTaskV2 implements UpgradeTasksInterface {

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      
    AttributeDefName deletedMembersAttr = AttributeDefNameFinder.findByName(UsduSettings.usduStemName() + ":subjectResolutionDeleted", false);

    if (deletedMembersAttr != null) {
      Set<Member> deletedMembers = new MemberFinder()
          .assignAttributeCheckReadOnAttributeDef(false)
          .assignNameOfAttributeDefName(UsduSettings.usduStemName() + ":subjectResolutionDeleted")
          .addAttributeValuesOnAssignment("true")
          .findMembers();
      
      for (Member deletedMember : deletedMembers) {
        deletedMember.setSubjectResolutionDeleted(true);
        deletedMember.setSubjectResolutionResolvable(false);
        deletedMember.store();
      }
      
      deletedMembersAttr.delete();
    }
    
    AttributeDefName resolvableMembersAttr = AttributeDefNameFinder.findByName(UsduSettings.usduStemName() + ":subjectResolutionResolvable", false);

    if (resolvableMembersAttr != null) {
      Set<Member> unresolvableMembers = new MemberFinder()
          .assignAttributeCheckReadOnAttributeDef(false)
          .assignNameOfAttributeDefName(UsduSettings.usduStemName() + ":subjectResolutionResolvable")
          .addAttributeValuesOnAssignment("false")
          .findMembers();
      
      for (Member unresolvableMember : unresolvableMembers) {
        unresolvableMember.setSubjectResolutionResolvable(false);
        unresolvableMember.store();
      }
      
      resolvableMembersAttr.delete();
    }
    
  }

  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("4.0.0");
  }

}
