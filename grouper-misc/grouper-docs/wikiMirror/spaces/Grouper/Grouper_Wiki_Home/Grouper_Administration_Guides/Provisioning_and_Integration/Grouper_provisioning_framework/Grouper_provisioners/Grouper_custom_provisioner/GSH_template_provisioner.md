---
title: "GSH template provisioner"
space: Grouper
pageId: 28560403
version: 5
lastUpdated: 2026-07-01T05:35:37.712Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560403/GSH+template+provisioner
---

This is in Grouper v4.17.8+ and v5.17.3+

A GSH template type of "provisioner" can be linked to a provisioner in the provisioning framework. If there is a lightweight provisioning requirement this is the lowest barrier to entry.

## Advantages and disadvantages

You can write a simple custom provisioner in a GSH template

| Topic | Rating | Description |
| --- | --- | --- |
| Does not need compiled java/jar |  | You can paste the provisioner code in the UI |
| If your institution has configs in Docker image |  | If you are putting code in the image you could easily just compile it into a jar |
| Simple provisioners |  | If the provisioner is simple, the complexity could fit in one GSH template source file |
| Complex provisioners |  | Since the source is in one file, it might be easier to manage in separate source files.  If you need extra jars, those need to go in the Docker image.  If there are configuration additions to the provisioning wizard, those need to go in the Docker image. |
| Can use Java |  | GSH templates can be written in Java, and if so, could take advantage of the performance annotation @CompileStatic |
| Can use Groovy |  | GSH templates can be written in Groovy |

## Configure the GSH template

Make a new GSH template of type "provisioner". This must be a V2 template. Since this is not run from the UI like traditional GSH templates, many options you typically see on this screen are not shown.

## GSH template provisioner source

The source is in the GSH template, explained below.

There are a lot of details here and things are a little different than a normal Java compiled provisioner.

Full example basic source file that just prints out and does not actually provision

```
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.app.gshTemplateProvisioner.GshTemplateProvisionerBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.*;
import groovy.transform.CompileStatic;

// if this is java (not groovy) and there are performance issues, 
// you can try adding the @CompileStatic annotation.  Your mileage may vary
@CompileStatic
public class Test122gshTemplateProvisioner extends GshTemplateV2 {

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

```

GSH template method. This just registers the provisioner after the template is run. The provisioner should be in its own class in the GSH template source.

```
  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
      GshTemplateV2output gshTemplateV2output) {

    gshTemplateV2output.getGsh_builtin_gshTemplateOutput().assignGrouperProvisioner(new ExampleProvisionerGshTemplate());
    
  }

```

The GSH template runtime is in a different classloader. Class objects from a GSH template cannot be instantiated dynamically from the provisioning framework. Methods in the provisioner which return classes representing overrides for various parts of the provisioning framework need to explicitly return instances and not classes. You need to return the class object just like a compiled provisioner even though they are not instantiated. This is because these are logged and need to be set to what is being overridden. But for GSH template provisioners (different from compiled provisioners), you also need to implement sibling methods to return an instance of these (due to classloader). The GrouperProvisioner subclass just registered which classes are overridden from the base provisioner. There is a GshTemplateProvisionerBase class that you can subclass for your provisioner (instead of GrouperProvisioner) which removed the need to have an empty configuration implementation. You must at least implement a DAO.

```
  public class ExampleProvisionerGshTemplate extends GshTemplateProvisionerBase {

    @Override
    protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {

      // you must return the class object for each provisioning framework functional override
      return ExampleProvisionerTargetDao.class;

    }

    @Override
    protected GrouperProvisionerTargetDaoBase grouperTargetDaoInstance() {

      // you must also implement an instance override for each functional override
      // since GSH templates run in their own classloader
      return new ExampleProvisionerTargetDao();

    }

  }

```

As with any provisioner, the main code is in the DAO (data access object) which does CRUD (create, read, update, delete) operations against the target (whichever non redundant operations you can implement preferring batched operations). It also tells the framework which methods are implemented and available to use (register capabilities).

```
  public class ExampleProvisionerTargetDao extends GrouperProvisionerTargetDaoBase {

    @Override
    public void registerGrouperProvisionerDaoCapabilities(GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
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

...
```

## Configure the provisioner

Start by selection GSH template provisioner type

Then pick the GSH template which has the code

Configure the provisioner as you normally would.
