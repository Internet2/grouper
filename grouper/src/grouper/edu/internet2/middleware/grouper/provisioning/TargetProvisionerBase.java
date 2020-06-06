package edu.internet2.middleware.grouper.provisioning;

/**
 * 
 * @author mchyzer
 *
 */
public abstract class TargetProvisionerBase {

  public TargetGroup retrieveGroup(TargetProvisionerRetrieveGroupParam targetProvisionerRetrieveGroupParam) {
    throw new UnsupportedOperationException();
  }

  public TargetGroup retrieveGroup(TargetProvisionerRetrieveGroupsParam targetProvisionerRetrieveGroupsParam) {
    throw new UnsupportedOperationException();
  }

  public Object translateTargetGroupToGrouper(TargetGroup targetGroup) {
    throw new UnsupportedOperationException();
  }

  public TargetTranslationToGrouperGroupType translateTargetGroupType() {
    throw new UnsupportedOperationException();
  }

}
