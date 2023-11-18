package edu.internet2.middleware.grouper.app.provisioningExamples.exampleFileWriter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoReplaceGroupMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoReplaceGroupMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupsResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
// Code below here
/**
 * edu.internet2.middleware.grouper.app.provisioningExamples.exampleFileWriter.ProvisioningExampleFileWriter
 */
public class ProvisioningExampleFileWriter extends GrouperProvisionerTargetDaoBase {
    /** logger */
    private static final Log LOG = GrouperUtil.getLog(ProvisioningExampleFileWriter.class);
/**
   - canRetrieveAllGroups
    - canInsertGroups
    - canDeleteGroups
    (*canRetrieveMembershipsAllByGroups*) OR canRetrieveMembershipsWithGroup
    - canReplaceGroupMemberships
**/
    // The capability for this seems to have the word ALL in it, while these references do not.
    @Override
    public TargetDaoRetrieveMembershipsByGroupsResponse retrieveMembershipsByGroups(TargetDaoRetrieveMembershipsByGroupsRequest targetDaoRetrieveMembershipsByGroupsRequest) {

      TargetDaoRetrieveMembershipsByGroupsResponse targetDaoRetrieveMembershipsByGroupsResponse = new TargetDaoRetrieveMembershipsByGroupsResponse();
      
      List<ProvisioningGroup> targetGroups = targetDaoRetrieveMembershipsByGroupsRequest.getTargetGroups();

      List<ProvisioningMembership> targetMemberships = new ArrayList<>();
      
      for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetGroups)) {
        
        String fileName = provisioningGroup.retrieveAttributeValueString("fileName");

        MailListGroup mailListGroup = new MailListGroup(fileName);
        
        for (String email : mailListGroup.getEmailToName().keySet()) {
          String name = mailListGroup.getEmailToName().get(email);
          ProvisioningMembership targetMembership = new ProvisioningMembership();
          targetMembership.addAttributeValue("fileName", mailListGroup.getFileName());
          targetMembership.addAttributeValue("email", email);
          targetMembership.addAttributeValue("name", name);
          targetMemberships.add(targetMembership);
        }
        
        targetDaoRetrieveMembershipsByGroupsResponse.setTargetMemberships(targetMemberships);
        
      }
      
      return targetDaoRetrieveMembershipsByGroupsResponse;
      
      
    }
    
    @Override
public TargetDaoDeleteMembershipResponse deleteMembership(
    TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
  // TODO Auto-generated method stub
  return super.deleteMembership(targetDaoDeleteMembershipRequest);
}

@Override
public TargetDaoInsertMembershipResponse insertMembership(
    TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
  // TODO Auto-generated method stub
  return super.insertMembership(targetDaoInsertMembershipRequest);
}

    @Override
    public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
      File dir = new File("/Users/mchyzer/Documents/23/2310/provisioningFiles");
      File[] listFiles = dir.listFiles(new FilenameFilter() {
        
        @Override
        public boolean accept(File dir, String name) {
          
          return name.endsWith(".txt");
        }
      });
      TargetDaoRetrieveAllGroupsResponse targetDaoRetrieveAllGroupsResponse = new TargetDaoRetrieveAllGroupsResponse();
      List<ProvisioningGroup> targetGroups = new ArrayList<>();
      for (File file : GrouperUtil.nonNull(listFiles, File.class)) {
        ProvisioningGroup targetGroup = new ProvisioningGroup();
        targetGroup.assignAttributeValue("fileName", file.getName());
        targetGroups.add(targetGroup);
      }
      targetDaoRetrieveAllGroupsResponse.setTargetGroups(targetGroups );
      return targetDaoRetrieveAllGroupsResponse;
    }
    @Override
    public TargetDaoInsertGroupsResponse insertGroups(TargetDaoInsertGroupsRequest targetDaoInsertGroupsRequest) {
      
      for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetDaoInsertGroupsRequest.getTargetGroups())) {
        File dir = new File("/Users/mchyzer/Documents/23/2310/provisioningFiles");
        File[] listFiles = dir.listFiles(new FilenameFilter() {
          
          @Override
          public boolean accept(File dir, String name) {
            
            return name.endsWith(targetGroup.retrieveAttributeValueString("fileName"));
          }
        });
        
        if (GrouperUtil.nonNull(listFiles, File.class).length == 0) {
          GrouperUtil.fileCreateNewFile(new File("/Users/mchyzer/Documents/23/2310/provisioningFiles/" + targetGroup.retrieveAttributeValueString("fileName")));
        }
        targetGroup.setProvisioned(true);

        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(true);
        }

      }
      return new TargetDaoInsertGroupsResponse();
    }
    @Override
    public TargetDaoDeleteGroupsResponse deleteGroups(TargetDaoDeleteGroupsRequest targetDaoDeleteGroupsRequest ) {
      for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetDaoDeleteGroupsRequest.getTargetGroups())) {
        File dir = new File("/Users/mchyzer/Documents/23/2310/provisioningFiles");
        File[] listFiles = dir.listFiles(new FilenameFilter() {
          
          @Override
          public boolean accept(File dir, String name) {
            
            return name.endsWith(targetGroup.retrieveAttributeValueString("fileName"));
          }
        });
        
        if (GrouperUtil.nonNull(listFiles, File.class).length == 1) {
          GrouperUtil.deleteFile(new File("/Users/mchyzer/Documents/23/2310/provisioningFiles/" + targetGroup.retrieveAttributeValueString("fileName")));
        }
        targetGroup.setProvisioned(true);

        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(true);
        }

      }
      return new TargetDaoDeleteGroupsResponse();
    }
    // This is getting called from outside of this class.  It's the public hook.
    @Override
    public TargetDaoReplaceGroupMembershipsResponse replaceGroupMemberships(
            TargetDaoReplaceGroupMembershipsRequest targetDaoReplaceGroupMembershipsRequest) {
      ProvisioningGroup targetGroup = targetDaoReplaceGroupMembershipsRequest.getTargetGroup();
      
      MailListGroup mlg = new MailListGroup(targetGroup.retrieveAttributeValueString("fileName"));
      
      for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(targetDaoReplaceGroupMembershipsRequest.getTargetMemberships())) {
        mlg.add(provisioningMembership.retrieveAttributeValueString("email"), provisioningMembership.retrieveAttributeValueString("name"));
      }
      
      mlg.writeToDisk();
      
      targetGroup.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(targetDaoReplaceGroupMembershipsRequest.getTargetMemberships())) {
        provisioningMembership.setProvisioned(true);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(provisioningMembership.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(true);
        }
      }

      return new TargetDaoReplaceGroupMembershipsResponse();
    }
    @Override
    public void registerGrouperProvisionerDaoCapabilities(GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
        LOG.debug("registerGrouperProvisionerDaoCapabilities called.");
        grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroups(true);
        grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
        grouperProvisionerDaoCapabilities.setCanInsertGroups(true);
        grouperProvisionerDaoCapabilities.setCanDeleteGroups(true);
        grouperProvisionerDaoCapabilities.setCanReplaceGroupMemberships(true);
    }
}