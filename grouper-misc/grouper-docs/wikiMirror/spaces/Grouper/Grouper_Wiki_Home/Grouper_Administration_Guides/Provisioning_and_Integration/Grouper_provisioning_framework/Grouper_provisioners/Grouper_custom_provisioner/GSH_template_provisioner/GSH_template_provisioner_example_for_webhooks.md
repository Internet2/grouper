---
title: "GSH template provisioner example for webhooks"
space: Grouper
pageId: 28564224
version: 2
lastUpdated: 2026-07-01T05:35:29.655Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564224/GSH+template+provisioner+example+for+webhooks
---

This is a GSH template provisioner which calls webhooks when flattened memberships are added or removed from provisionable groups. A flattened membership change is when a person was not previously in the group, and now is in the group. Or a user who is in the group, and now is no longer in the group. If a current group member now has a membership from another path, then that is not a change.

Here is an example message:

action will be "membershipAdd" or "membershipRemove"

```
POST https://sonar.server.whatever/webhook-test/9e2123-234-345-456-56789
Grouper2SONAR_Token: abc123

{
  "pennId": "10021368",
  "groupName": "penn:nursing:etc:nursingTest",
  "action": "membershipAdd",
  "description": "Chris Hyzer (mchyzer, 10021368) (active) Staff - Isc-tech Services-network Operations - Application Architect (also: Alumni)",
  "name": "Chris Hyzer",
  "pennname": "mchyzer",
  "email_public": "mchyzer@example.com",
  "name_last_public": "Hyzer",
  "name_public": "Chris Hyzer",
  "name_first_public": "Chris",
  "eppn": "mchyzer@example.com",
  "preferred_first_name": "Chris",
  "last_name": "Hyzer",
  "first_name": "Chris"
}
```

The authentication is name/value in HTTP header.

## Assigning groups to be webhooks provisionable

People in this group can assign provisioning for the Nursing webhooks provisioner: etc:provisioning:nursing_webhook:nursingWebhookProvisionerUpdaters. The Nursing PennGroups admins group is in this group.

The Nursing webhooks provisioners are:

- nursing_webhook_test
- nursing_webhook_prod

Both of these are similar though point to different endpoints with different credentials.

## External system

The endpoint and credentials have a config ID which must be the same as the provisioner ID.

## Webhook GSH template

This custom template has the code to send messages to the webhook endpoint

```

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.app.gshTemplateProvisioner.GshTemplateProvisionerBase;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipResponse;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import groovy.transform.CompileStatic;

@CompileStatic
public class Test137nursingWebhooksProvisioner extends GshTemplateV2 {

  // add logger
  private static final Log LOG = GrouperUtil.getLog(GshTemplateV2.class);
  
  public class NursingWebhooksProvisionerTargetDao extends GrouperProvisionerTargetDaoBase {

    @Override
    public boolean loggingStart() {
      return GrouperHttpClient.logStart(new GrouperHttpClientLog());
    }

    @Override
    public String loggingStop() {
      return GrouperHttpClient.logEnd();
    }

    private String gshTemplateConfigId = null;
    
    public NursingWebhooksProvisionerTargetDao(String theGshTemplateConfigId) {
      super();
      this.gshTemplateConfigId = theGshTemplateConfigId;
    }
    
    public NursingWebhooksProvisionerTargetDao() {
      super();
    }

    @Override
    public void registerGrouperProvisionerDaoCapabilities(
        GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
      grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
      grouperProvisionerDaoCapabilities.setCanInsertMembership(true);
    }

    public void addAttribute(ObjectNode objectNode, Subject subject, String attributeName) {
      
      String attributeValue = subject.getAttributeValue(attributeName);
      
      if (!GrouperUtil.isBlank(attributeValue)) {
        objectNode.put(attributeName, attributeValue);
      }
    }
    
    public void processMembership(ProvisioningMembership targetMembership, String action) {

      String jsonString = "";
      try {
        
        String pennId = targetMembership.getProvisioningEntityId();
        
        Subject subject = SubjectFinder.findByIdAndSource(pennId, "pennperson", true);

        // null if a group was added or a service principal
        if (subject != null) {
          GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
          grouperHttpClient.assignDoNotLogHeaders(GrouperUtil.toSet("Grouper2SONAR_Token"));
          WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient, this.gshTemplateConfigId);

          grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
          grouperHttpClient.assignUrl(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken." + this.gshTemplateConfigId + ".endpoint"));
          grouperHttpClient.addHeader("Content-Type", "application/json");
          
          String groupName = targetMembership.getProvisioningGroupId();

          ObjectNode objectNode = GrouperUtil.jsonJacksonNode();
          objectNode.put("pennId", pennId);
          objectNode.put("groupName", groupName);
          objectNode.put("action", action);

          if (!StringUtils.isBlank(subject.getDescription())) {
            objectNode.put("description", subject.getDescription());
          }
          
          // add name
          if (!StringUtils.isBlank(subject.getName())) {
            objectNode.put("name", subject.getName());
          }

          List<String> attributes = GrouperUtil.toList("pennname", "email_public", "name_middle_public", "name_last_public", 
              "name_public", "name_first_public", "eppn", "preferred_first_name", 
              "last_name", "first_name");
          
          for (String attr : attributes) {
            addAttribute(objectNode, subject, attr);
          }
          
          jsonString = GrouperUtil.jsonJacksonToString(objectNode);
          
          grouperHttpClient.assignBody(jsonString);
          grouperHttpClient.assignAssertResponseCode(200);
          grouperHttpClient.executeRequest();

        }
        
        
        targetMembership.setProvisioned(true);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(true);
        }
        
      } catch (RuntimeException re) {
        
        GrouperUtil.injectInException(re, "Error provisioning membership: " + targetMembership.getProvisioningEntityId() + ", " + targetMembership.getProvisioningGroupId() + ", jsonString: " + jsonString + ", ");
        
        targetMembership.setProvisioned(false);
        targetMembership.setException(re);
        
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }
        
        if (LOG.isErrorEnabled()) {
          LOG.error("Error", re);
        }
        
      }

    }
    
    @Override
    public TargetDaoDeleteMembershipResponse deleteMembership(
        TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {

      ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest.getTargetMembership();
      
      processMembership(targetMembership, "membershipRemove");
      
      return new TargetDaoDeleteMembershipResponse();
    }

    @Override
    public TargetDaoInsertMembershipResponse insertMembership(
        TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {

      ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();
      
      processMembership(targetMembership, "membershipAdd");
      return new TargetDaoInsertMembershipResponse();
    }

  }
  
  public class NursingWebhooksProvisionerGshTemplate extends GshTemplateProvisionerBase {

    private String gshTemplateConfigId = null;
    
    public NursingWebhooksProvisionerGshTemplate(String theGshTemplateConfigId) {
      super();
      this.gshTemplateConfigId = theGshTemplateConfigId;
    }
    
    public NursingWebhooksProvisionerGshTemplate() {
      super();
    }
    
    @Override
    protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
      return NursingWebhooksProvisionerTargetDao.class;
    }

    @Override
    protected GrouperProvisionerTargetDaoBase grouperTargetDaoInstance() {
      return new NursingWebhooksProvisionerTargetDao(this.gshTemplateConfigId);
    }

  }
  
  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
      GshTemplateV2output gshTemplateV2output) {

    // nursing_webhook_test, nursing_webhook_prod
    String templateConfigId = gshTemplateV2input.getGsh_builtin_gshTemplateRuntime().getTemplateConfigId();
    
    gshTemplateV2output.getGsh_builtin_gshTemplateOutput().assignGrouperProvisioner(new NursingWebhooksProvisionerGshTemplate(templateConfigId));
    
  }

}

```

## Provisioner configuration
