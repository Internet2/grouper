package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class TargetDaoRetrieveMembershipsByTargetGroupEntityMembershipRequest {

  public TargetDaoRetrieveMembershipsByTargetGroupEntityMembershipRequest() {

  }

  private List<MultiKey> targetGroupsMembersMemberships;

  
  public List<MultiKey> getTargetGroupsMembersMemberships() {
    return targetGroupsMembersMemberships;
  }

  
  public void setTargetGroupsMembersMemberships(
      List<MultiKey> targetGroupsMembersMemberships) {
    this.targetGroupsMembersMemberships = targetGroupsMembersMemberships;
  }


  public TargetDaoRetrieveMembershipsByTargetGroupEntityMembershipRequest(
      List<MultiKey> targetGroupsMembersMemberships) {
    super();
    this.targetGroupsMembersMemberships = targetGroupsMembersMemberships;
  }
  
  
}
