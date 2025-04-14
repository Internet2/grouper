package edu.internet2.middleware.grouper.app.provisioningExamples;


import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.app.gshTemplateProvisioner.GshTemplateProvisionerBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.*;
import groovy.transform.CompileStatic;

@CompileStatic
public class ExampleGshTemplateProvisioner extends GshTemplateV2 {

  public class ExampleProvisionerTargetDao extends GrouperProvisionerTargetDaoBase {

    @Override
    public void registerGrouperProvisionerDaoCapabilities(
        GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
      grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
      grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
      grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
      grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
      grouperProvisionerDaoCapabilities.setCanInsertGroups(true);
      grouperProvisionerDaoCapabilities.setCanInsertMembership(true);
      grouperProvisionerDaoCapabilities.setCanRetrieveAllData(true);
      grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
      grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
      grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
      grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
      grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByEntity(true);
      grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
      grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
      
    }

    @Override
    public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(
        TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
      System.out.println("retrieveAllGroups");
      return new TargetDaoRetrieveAllGroupsResponse();
    }

    @Override
    public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(
        TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
      System.out.println("retrieveAllEntities");
      return new TargetDaoRetrieveAllEntitiesResponse();
    }

    @Override
    public TargetDaoDeleteGroupResponse deleteGroup(
        TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {
      System.out.println("deleteGroup");
      return new TargetDaoDeleteGroupResponse();
    }

    @Override
    public TargetDaoRetrieveAllDataResponse retrieveAllData(
        TargetDaoRetrieveAllDataRequest targetDaoRetrieveAllDataRequest) {
      System.out.println("retrieveAllData");
      return new TargetDaoRetrieveAllDataResponse();
    }

    @Override
    public TargetDaoRetrieveMembershipsByEntityResponse retrieveMembershipsByEntity(
        TargetDaoRetrieveMembershipsByEntityRequest targetDaoRetrieveMembershipsByEntityRequest) {
      System.out.println("retrieveMembershipsByEntity");
      return new TargetDaoRetrieveMembershipsByEntityResponse();
    }

    @Override
    public TargetDaoRetrieveGroupResponse retrieveGroup(
        TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {
      System.out.println("retrieveGroup");
      return new TargetDaoRetrieveGroupResponse();
    }

    @Override
    public TargetDaoRetrieveEntityResponse retrieveEntity(
        TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {
      System.out.println("retrieveEntity");
      return new TargetDaoRetrieveEntityResponse();
    }

    @Override
    public TargetDaoUpdateGroupResponse updateGroup(
        TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
      System.out.println("updateGroup");
      return new TargetDaoUpdateGroupResponse();
    }

    @Override
    public TargetDaoInsertGroupsResponse insertGroups(
        TargetDaoInsertGroupsRequest targetDaoInsertGroupsRequest) {
      System.out.println("insertGroups");
      return new TargetDaoInsertGroupsResponse();
    }

    @Override
    public TargetDaoDeleteEntityResponse deleteEntity(
        TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {
      System.out.println("deleteEntity");
      return new TargetDaoDeleteEntityResponse();
    }

    @Override
    public TargetDaoInsertEntityResponse insertEntity(
        TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {
      System.out.println("insertEntity");
      return new TargetDaoInsertEntityResponse();
    }

    @Override
    public TargetDaoUpdateEntityResponse updateEntity(
        TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
      System.out.println("updateEntity");
      return new TargetDaoUpdateEntityResponse();
    }

    @Override
    public TargetDaoDeleteMembershipResponse deleteMembership(
        TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
      System.out.println("deleteMembership");
      return new TargetDaoDeleteMembershipResponse();
    }

    @Override
    public TargetDaoInsertMembershipResponse insertMembership(
        TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
      System.out.println("insertMembership");
      return new TargetDaoInsertMembershipResponse();
    }


  }
  
  public class ExampleProvisionerGshTemplate extends GshTemplateProvisionerBase {

    @Override
    protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
      return ExampleProvisionerTargetDao.class;
    }

    @Override
    protected GrouperProvisionerTargetDaoBase grouperTargetDaoInstance() {
      return new ExampleProvisionerTargetDao();
    }

  }
  
  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
      GshTemplateV2output gshTemplateV2output) {

    gshTemplateV2output.getGsh_builtin_gshTemplateOutput().assignGrouperProvisioner(new ExampleProvisionerGshTemplate());
    
  }

}
