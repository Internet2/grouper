/**
 * 
 */
package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogCommands;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembershipWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * wraps the dao so it can convert methods and see if things are available
 * @author mchyzer-local
 *
 */
public class GrouperProvisionerTargetDaoAdapter extends GrouperProvisionerTargetDaoBase {

  @Override
  public boolean loggingStart() {
    return this.wrappedDao.loggingStart();
  }

  @Override
  public String loggingStop() {
    return this.wrappedDao.loggingStop();
  }


  private GrouperProvisionerTargetDaoBase wrappedDao;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisionerTargetDaoAdapter.class);
  
  @Override
  public GrouperProvisionerDaoCapabilities getGrouperProvisionerDaoCapabilities() {
    return this.wrappedDao.getGrouperProvisionerDaoCapabilities();
  }

  @Override
  public void setGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    this.wrappedDao.setGrouperProvisionerDaoCapabilities(grouperProvisionerDaoCapabilities);
  }


  @Override
  public void addTargetDaoTimingInfo(TargetDaoTimingInfo targetDaoTimingInfo) {
    this.wrappedDao.addTargetDaoTimingInfo(targetDaoTimingInfo);
  }


  @Override
  public List<TargetDaoTimingInfo> getTargetDaoTimingInfos() {
    return this.wrappedDao.getTargetDaoTimingInfos();
  }


  @Override
  public void setTargetDaoTimingInfos(List<TargetDaoTimingInfo> targetDaoTimingInfos) {
    this.wrappedDao.setTargetDaoTimingInfos(targetDaoTimingInfos);
  }


  public GrouperProvisionerTargetDaoBase getWrappedDao() {
    return wrappedDao;
  }

  
  public void setWrappedDao(GrouperProvisionerTargetDaoBase wrappedDao) {
    this.wrappedDao = wrappedDao;
  }

  public GrouperProvisionerTargetDaoAdapter(GrouperProvisioner grouperProvisioner, GrouperProvisionerTargetDaoBase wrappedDao) {
    super();
    super.setGrouperProvisioner(grouperProvisioner);
    this.wrappedDao = wrappedDao;
  }

  /**
   * 
   */
  public GrouperProvisionerTargetDaoAdapter() {
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    // this probably would never be called here, but just delegate just in case
    this.wrappedDao.registerGrouperProvisionerDaoCapabilities(grouperProvisionerDaoCapabilities);
  }


  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(
      TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveAllGroups(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();
  
        TargetDaoRetrieveAllGroupsResponse targetDaoRetrieveAllGroupsResponse = this.wrappedDao.retrieveAllGroups(targetDaoRetrieveAllGroupsRequest);
        
        hasError = logGroups(targetDaoRetrieveAllGroupsResponse.getTargetGroups());
        return targetDaoRetrieveAllGroupsResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveAllGroups");
      }

    }
    throw new RuntimeException("Dao cannot retrieve all groups");
  }

  /**
   * finally block for command logging
   * @param hasError
   * @param method
   */
  private void commandLogFinallyBlock(boolean commandLogStarted, boolean hasError, String method) {
    if (!commandLogStarted) {
      return;
    }
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isLogCommandsAlways()
        || (hasError && this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isLogCommandsOnError())) {
      String debugInfo = this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "None implemented for this DAO");
      String theLog = "Command log for provisioner '" + this.getGrouperProvisioner().getConfigId() 
          + "' - '" + this.getGrouperProvisioner().getInstanceId() + "', " + method + ": " + debugInfo;
      if (hasError) {
        GrouperProvisioningLogCommands.errorLog(theLog);
      } else {
        GrouperProvisioningLogCommands.infoLog(theLog);
      }     
    }
  }


  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(
      TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveAllEntities(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoRetrieveAllEntitiesResponse targetDaoRetrieveAllEntitiesResponse = this.wrappedDao.retrieveAllEntities(targetDaoRetrieveAllEntitiesRequest);
        hasError = logEntities(targetDaoRetrieveAllEntitiesResponse.getTargetEntities());
        return targetDaoRetrieveAllEntitiesResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveAllEntities");
      }
        
    }
    
    throw new RuntimeException("Dao cannot retrieve all entities");
    
  }

  private boolean commandLogStartLoggingIfConfigured() {
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isLogCommandsAlways()
        || this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isLogCommandsOnError()) {
      return this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().loggingStart();
    }
    return false;
  }


  @Override
  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(
      TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveAllMemberships(), false)) {
      
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();
      
        TargetDaoRetrieveAllMembershipsResponse targetDaoRetrieveAllMembershipsResponse = this.wrappedDao.retrieveAllMemberships(targetDaoRetrieveAllMembershipsRequest);
        hasError = logMemberships(targetDaoRetrieveAllMembershipsResponse.getTargetMemberships());
        return targetDaoRetrieveAllMembershipsResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveAllMemberships");
      }

    }

    throw new RuntimeException("Dao cannot retrieve all memberships");
  }


  @Override
  public TargetDaoDeleteGroupResponse deleteGroup(
      TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {
    
    if (targetDaoDeleteGroupRequest.getTargetGroup() == null) {
      return new TargetDaoDeleteGroupResponse();
    }

    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest.getTargetGroup();
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteGroup(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();
        TargetDaoDeleteGroupResponse targetDaoDeleteGroupResponse = this.wrappedDao.deleteGroup(targetDaoDeleteGroupRequest);
        hasError = logGroup(targetGroup);
        if (targetGroup.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set deleted group as provisioned: " + this.wrappedDao);
        }
        return targetDaoDeleteGroupResponse;
      } catch (RuntimeException e) {
        hasError = true;
        if (targetGroup.getProvisioned() == null) {
          targetGroup.setProvisioned(false);
        }
        if (targetGroup.getException() == null) {
          GrouperUtil.injectInException(e, targetGroup.toString());
          targetGroup.setException(e);
        }
        logGroup(targetGroup);
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "deleteGroup");
      }
      return new TargetDaoDeleteGroupResponse();
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteGroups(), false)) {
      this.deleteGroups(new TargetDaoDeleteGroupsRequest(GrouperUtil.toList(targetGroup)));
      return new TargetDaoDeleteGroupResponse();
    }

    throw new RuntimeException("Dao cannot delete group or groups");
  }


  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    if (targetGroup == null) {
      return new TargetDaoInsertGroupResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertGroup(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoInsertGroupResponse targetDaoInsertGroupResponse = this.wrappedDao.insertGroup(targetDaoInsertGroupRequest);
        hasError = logGroup(targetGroup);
        if (targetGroup.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set inserted group as provisioned: " + this.wrappedDao);
        }
        return targetDaoInsertGroupResponse;
      } catch (RuntimeException e) {
        hasError = true;
        if (targetGroup.getProvisioned() == null) {
          targetGroup.setProvisioned(false);
        }
        if (targetGroup.getException() == null) {
          GrouperUtil.injectInException(e, targetGroup.toString());
          targetGroup.setException(e);
        }
        logGroup(targetGroup);
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "insertGroup");
      }
      return new TargetDaoInsertGroupResponse();
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertGroups(), false)) {
      this.insertGroups(new TargetDaoInsertGroupsRequest(GrouperUtil.toList(targetGroup)));
      return new TargetDaoInsertGroupResponse();
    }

    throw new RuntimeException("Dao cannot insert group or groups");

  }

  @Override
  public TargetDaoSendChangesToTargetResponse sendChangesToTarget(
      TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest) {
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanSendChangesToTarget(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        this.wrappedDao.sendChangesToTarget(targetDaoSendChangesToTargetRequest);
        if (logProvisioningLists(targetDaoSendChangesToTargetRequest.getTargetObjectInserts())) {
          hasError = true;
        }
        if (logProvisioningLists(targetDaoSendChangesToTargetRequest.getTargetObjectUpdates())) {
          hasError = true;
        }
        if (logProvisioningLists(targetDaoSendChangesToTargetRequest.getTargetObjectDeletes())) {
          hasError = true;
        }
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "sendChangesToTarget");
      }

    }
    {
      TargetDaoSendGroupChangesToTargetRequest targetDaoSendGroupChangesToTargetRequest = new TargetDaoSendGroupChangesToTargetRequest(
          targetDaoSendChangesToTargetRequest.getTargetObjectInserts().getProvisioningGroups(), 
          targetDaoSendChangesToTargetRequest.getTargetObjectUpdates().getProvisioningGroups(),
          targetDaoSendChangesToTargetRequest.getTargetObjectDeletes().getProvisioningGroups());
      sendGroupChangesToTarget(targetDaoSendGroupChangesToTargetRequest);
    }
    {
      TargetDaoSendEntityChangesToTargetRequest targetDaoSendEntityChangesToTargetRequest = new TargetDaoSendEntityChangesToTargetRequest(
          targetDaoSendChangesToTargetRequest.getTargetObjectInserts().getProvisioningEntities(), 
          targetDaoSendChangesToTargetRequest.getTargetObjectUpdates().getProvisioningEntities(),
          targetDaoSendChangesToTargetRequest.getTargetObjectDeletes().getProvisioningEntities());
      sendEntityChangesToTarget(targetDaoSendEntityChangesToTargetRequest);
    }
    {
      TargetDaoSendMembershipChangesToTargetRequest targetDaoSendMembershipChangesToTargetRequest = new TargetDaoSendMembershipChangesToTargetRequest(
          targetDaoSendChangesToTargetRequest.getTargetObjectInserts().getProvisioningMemberships(), 
          targetDaoSendChangesToTargetRequest.getTargetObjectUpdates().getProvisioningMemberships(),
          targetDaoSendChangesToTargetRequest.getTargetObjectDeletes().getProvisioningMemberships(),
          targetDaoSendChangesToTargetRequest.getTargetObjectReplaces().getProvisioningMemberships());
      sendMembershipChangesToTarget(targetDaoSendMembershipChangesToTargetRequest);
    }
    return null;

  }


  @Override
  public TargetDaoSendGroupChangesToTargetResponse sendGroupChangesToTarget(
      TargetDaoSendGroupChangesToTargetRequest targetDaoSendGroupChangesToTargetRequest) {

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanSendGroupChangesToTarget(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoSendGroupChangesToTargetResponse targetDaoSendGroupChangesToTargetResponse = this.wrappedDao.sendGroupChangesToTarget(targetDaoSendGroupChangesToTargetRequest);
        if (logGroups(targetDaoSendGroupChangesToTargetRequest.getTargetGroupInserts())) {
          hasError = true;
        }
        if (logGroups(targetDaoSendGroupChangesToTargetRequest.getTargetGroupUpdates())) {
          hasError = true;
        }
        if (logGroups(targetDaoSendGroupChangesToTargetRequest.getTargetGroupDeletes())) {
          hasError = true;
        }
        return targetDaoSendGroupChangesToTargetResponse;

      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "sendGroupChangesToTarget");
      }

    }
    if (GrouperUtil.length(targetDaoSendGroupChangesToTargetRequest.getTargetGroupDeletes()) > 0){
      List<ProvisioningGroup> targetGroupDeletes = targetDaoSendGroupChangesToTargetRequest.getTargetGroupDeletes();
      TargetDaoDeleteGroupsRequest targetDaoDeleteGroupsRequest = new TargetDaoDeleteGroupsRequest();
      targetDaoDeleteGroupsRequest.setTargetGroups(targetGroupDeletes);
      this.deleteGroups(targetDaoDeleteGroupsRequest);
    }

    if (GrouperUtil.length(targetDaoSendGroupChangesToTargetRequest.getTargetGroupInserts()) > 0){
      List<ProvisioningGroup> targetGroupInserts = targetDaoSendGroupChangesToTargetRequest.getTargetGroupInserts();
      
      this.insertGroups(new TargetDaoInsertGroupsRequest(targetGroupInserts));
    }
    if (GrouperUtil.length(targetDaoSendGroupChangesToTargetRequest.getTargetGroupUpdates()) > 0){
      List<ProvisioningGroup> targetGroupUpdates = targetDaoSendGroupChangesToTargetRequest.getTargetGroupUpdates();
      
      this.updateGroups(new TargetDaoUpdateGroupsRequest(targetGroupUpdates));
    }
    return null;

  }


  @Override
  public TargetDaoUpdateGroupsResponse updateGroups(
      TargetDaoUpdateGroupsRequest targetDaoUpdateGroupsRequest) {
    
    if (GrouperUtil.length(targetDaoUpdateGroupsRequest.getTargetGroups()) == 0) {
      return new TargetDaoUpdateGroupsResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateGroups(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoUpdateGroupsResponse targetDaoUpdateGroupsResponse = this.wrappedDao.updateGroups(targetDaoUpdateGroupsRequest);
        hasError = logGroups(targetDaoUpdateGroupsRequest.getTargetGroups());
        for (ProvisioningGroup provisioningGroup : targetDaoUpdateGroupsRequest.getTargetGroups()) { 
          if (provisioningGroup.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set updated group as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoUpdateGroupsResponse;
      } catch (RuntimeException e) {
        hasError = true;
        for (ProvisioningGroup targetGroup : targetDaoUpdateGroupsRequest.getTargetGroups()) { 
          
          if (targetGroup.getProvisioned() == null) {
            targetGroup.setProvisioned(false);
          }
          if (targetGroup.getException() == null) {
            GrouperUtil.injectInException(e, targetGroup.toString());
            targetGroup.setException(e);
          }
          setExceptionForMembershipsWhenGroupOrEntityAttributes(null, targetGroup, e);
        }
        logGroups(targetDaoUpdateGroupsRequest.getTargetGroups());
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "updateGroups");
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateGroup(), false)) {
      for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetDaoUpdateGroupsRequest.getTargetGroups())) {
        updateGroup(new TargetDaoUpdateGroupRequest(provisioningGroup));
      }
      return null;
    }

    throw new RuntimeException("Dao cannot update group or groups");
  }


  @Override
  public TargetDaoDeleteMembershipsResponse deleteMemberships(
      TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest) {

    if (GrouperUtil.length(targetDaoDeleteMembershipsRequest.getTargetMemberships()) == 0) {
      return new TargetDaoDeleteMembershipsResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteMemberships(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoDeleteMembershipsResponse targetDaoDeleteMembershipsResponse = this.wrappedDao.deleteMemberships(targetDaoDeleteMembershipsRequest);
        hasError = logMemberships(targetDaoDeleteMembershipsRequest.getTargetMemberships());
        for (ProvisioningMembership provisioningMembership : targetDaoDeleteMembershipsRequest.getTargetMemberships()) { 
          if (provisioningMembership.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set deleted membership as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoDeleteMembershipsResponse;
      } catch (RuntimeException e) {
        hasError = true;
        for (ProvisioningMembership targetMembership : targetDaoDeleteMembershipsRequest.getTargetMemberships()) { 
          
          if (targetMembership.getProvisioned() == null) {
            targetMembership.setProvisioned(false);
          }
          if (targetMembership.getException() == null) {
            GrouperUtil.injectInException(e, targetMembership.toString());
            targetMembership.setException(e);
          }
        }
        logMemberships(targetDaoDeleteMembershipsRequest.getTargetMemberships());
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "deleteMemberships");
      }
      return new TargetDaoDeleteMembershipsResponse();
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteMembership(), false)) {
      for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(targetDaoDeleteMembershipsRequest.getTargetMemberships())) {
        deleteMembership(new TargetDaoDeleteMembershipRequest(provisioningMembership));
      }
      return new TargetDaoDeleteMembershipsResponse();
    }

    throw new RuntimeException("Dao cannot delete membership or memberships");
  }


  @Override
  public TargetDaoRetrieveAllDataResponse retrieveAllData(
      TargetDaoRetrieveAllDataRequest targetDaoRetrieveAllDataRequest) {

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveAllData(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();
        TargetDaoRetrieveAllDataResponse targetDaoRetrieveAllDataResponse = this.wrappedDao.retrieveAllData(targetDaoRetrieveAllDataRequest);
        if (targetDaoRetrieveAllDataResponse.getTargetData()!=null) {
          if (logEntities(targetDaoRetrieveAllDataResponse.getTargetData().getProvisioningEntities())) {
            hasError = true;
          }
          if (logGroups(targetDaoRetrieveAllDataResponse.getTargetData().getProvisioningGroups())) {
            hasError = true;
          }
          if (logMemberships(targetDaoRetrieveAllDataResponse.getTargetData().getProvisioningMemberships())) {
            hasError = true;
          }
        }
        return targetDaoRetrieveAllDataResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveAllData");
      }
        
    }

    GrouperProvisioningLists targetObjects = new GrouperProvisioningLists();
    TargetDaoRetrieveAllDataResponse result = new TargetDaoRetrieveAllDataResponse(targetObjects);
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsAll()) {
      
      TargetDaoRetrieveAllGroupsResponse targetDaoRetrieveAllGroupsResponse = this.retrieveAllGroups(new TargetDaoRetrieveAllGroupsRequest(true));
      List<ProvisioningGroup> targetGroups = targetDaoRetrieveAllGroupsResponse == null ? null : targetDaoRetrieveAllGroupsResponse.getTargetGroups();
      targetObjects.setProvisioningGroups(targetGroups);

    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesAll()) {
      
      TargetDaoRetrieveAllEntitiesResponse targetDaoRetrieveAllEntitiesResponse = this.retrieveAllEntities(new TargetDaoRetrieveAllEntitiesRequest(true));
      List<ProvisioningEntity> targetEntities = targetDaoRetrieveAllEntitiesResponse == null ? null : targetDaoRetrieveAllEntitiesResponse.getTargetEntities();
      targetObjects.setProvisioningEntities(targetEntities);

    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsAll()) {
      
      TargetDaoRetrieveAllMembershipsResponse targetDaoRetrieveAllMembershipsResponse = this.retrieveAllMemberships(new TargetDaoRetrieveAllMembershipsRequest());
      List<ProvisioningMembership> targetMemberships = targetDaoRetrieveAllMembershipsResponse == null ? null : targetDaoRetrieveAllMembershipsResponse.getTargetMemberships();
      targetObjects.setProvisioningMemberships(targetMemberships);

    }
    return result;
  }


  @Override
  public TargetDaoRetrieveIncrementalDataResponse retrieveIncrementalData(
      TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncementalDataRequest) {
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveIncrementalData(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoRetrieveIncrementalDataResponse targetDaoRetrieveIncrementalDataResponse = this.wrappedDao.retrieveIncrementalData(targetDaoRetrieveIncementalDataRequest);
        if (logEntities(targetDaoRetrieveIncrementalDataResponse.getProvisioningEntities())) {
          hasError = true;
        }
        if (logGroups(targetDaoRetrieveIncrementalDataResponse.getProvisioningGroups())) {
          hasError = true;
        }
        if (logObjects(targetDaoRetrieveIncrementalDataResponse.getProvisioningMemberships())) {
          hasError = true;
        }
        return targetDaoRetrieveIncrementalDataResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveIncrementalData");
      }
        
    }
    TargetDaoRetrieveIncrementalDataResponse result = new TargetDaoRetrieveIncrementalDataResponse();
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroups()) {
      List<ProvisioningGroup> targetGroups = null;
      targetGroups = targetDaoRetrieveIncementalDataRequest.getTargetGroupsForGroupOnly();
      if (GrouperUtil.length(targetGroups) > 0) {
        // if there are groups then this must be implemented
        TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = this.retrieveGroups(
            new TargetDaoRetrieveGroupsRequest(targetGroups, false));
        List<ProvisioningGroup> targetGroupsResult = targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups();
        result.setProvisioningGroups(targetGroupsResult);
      }
    }
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntities()) {
      List<ProvisioningEntity> targetEntities = null;
      targetEntities = targetDaoRetrieveIncementalDataRequest.getTargetEntitiesForEntityOnly();
      if (GrouperUtil.length(targetEntities) > 0) {
        TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = this.retrieveEntities(
            new TargetDaoRetrieveEntitiesRequest(targetEntities, false));
        List<ProvisioningEntity> targetEntitiesResult = targetDaoRetrieveEntitiesResponse == null ?
            null : targetDaoRetrieveEntitiesResponse.getTargetEntities();
        result.setProvisioningEntities(targetEntitiesResult);
      }
    }
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMemberships()) {
      List<ProvisioningGroup> targetGroups = targetDaoRetrieveIncementalDataRequest.getTargetGroupsForGroupMembershipSync();
      List<ProvisioningEntity> targetEntities = targetDaoRetrieveIncementalDataRequest.getTargetEntitiesForEntityMembershipSync();
      List<Object> targetGroupsEntitiesMemberships = targetDaoRetrieveIncementalDataRequest.getTargetMembershipObjectsForMembershipSync();
      if (GrouperUtil.length(targetGroupsEntitiesMemberships) > 0 || GrouperUtil.length(targetGroups) > 0 || GrouperUtil.length(targetEntities) > 0) {
        TargetDaoRetrieveMembershipsBulkResponse retrieveMembershipsBulk = this.retrieveMembershipsBulk(new TargetDaoRetrieveMembershipsBulkRequest(targetGroups, targetEntities, targetGroupsEntitiesMemberships));
        List<Object> targetMemberships = retrieveMembershipsBulk == null ? null : retrieveMembershipsBulk.getTargetMemberships();
        result.setProvisioningMemberships(targetMemberships);
      }
    }
    return result;

  }


  @Override
  public TargetDaoRetrieveGroupsResponse retrieveGroups(
      TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest) {
    
    if (GrouperUtil.length(targetDaoRetrieveGroupsRequest.getTargetGroups()) == 0) {
      return new TargetDaoRetrieveGroupsResponse();
    }

    for (ProvisioningGroup provisioningGroup : targetDaoRetrieveGroupsRequest.getTargetGroups()) {
      provisioningGroup.assignSearchFilter();
    }
    
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveGroups(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();
        TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = this.wrappedDao.retrieveGroups(targetDaoRetrieveGroupsRequest);
        hasError = logGroups(targetDaoRetrieveGroupsResponse.getTargetGroups());
        return targetDaoRetrieveGroupsResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveGroups");
      }
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveGroup(), false)) {
      
      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
      
      for (ProvisioningGroup provisioningGroup : targetDaoRetrieveGroupsRequest.getTargetGroups()) {

        TargetDaoRetrieveGroupResponse targetDaoRetrieveGroupResponse = this.retrieveGroup(new TargetDaoRetrieveGroupRequest(provisioningGroup, targetDaoRetrieveGroupsRequest.isIncludeAllMembershipsIfApplicable()));
        if (targetDaoRetrieveGroupResponse != null && targetDaoRetrieveGroupResponse.getTargetGroup() != null) {
          results.add(targetDaoRetrieveGroupResponse.getTargetGroup());
        }
      }
      return new TargetDaoRetrieveGroupsResponse(results);
    }

    throw new RuntimeException("Dao cannot retrieve groups or group");

  }


  @Override
  public TargetDaoRetrieveMembershipsBulkResponse retrieveMembershipsBulk(
      TargetDaoRetrieveMembershipsBulkRequest targetDaoRetrieveMembershipsBulkRequest) {
    
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsBulk(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoRetrieveMembershipsBulkResponse targetDaoRetrieveMembershipsBulkResponse = this.wrappedDao.retrieveMembershipsBulk(targetDaoRetrieveMembershipsBulkRequest);
        hasError = logObjects(targetDaoRetrieveMembershipsBulkResponse.getTargetMemberships());
        return targetDaoRetrieveMembershipsBulkResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveMembershipsBulk");
      }

    }

    List<Object> targetMembershipsResults = new ArrayList<Object>();
    
    List<ProvisioningGroup> targetGroups = targetDaoRetrieveMembershipsBulkRequest == null ? null : targetDaoRetrieveMembershipsBulkRequest.getTargetGroupsForAllMemberships();
    
    if (GrouperUtil.length(targetGroups) > 0) {
      TargetDaoRetrieveMembershipsByGroupsResponse retrieveMembershipsByGroups = this.retrieveMembershipsByGroups(new TargetDaoRetrieveMembershipsByGroupsRequest(targetGroups));
      targetMembershipsResults.addAll(retrieveMembershipsByGroups == null ? null : retrieveMembershipsByGroups.getTargetMemberships());
    }
    
    List<ProvisioningEntity> targetEntities = targetDaoRetrieveMembershipsBulkRequest == null ? null : targetDaoRetrieveMembershipsBulkRequest.getTargetEntitiesForAllMemberships();
    if (GrouperUtil.length(targetEntities) > 0) {
      TargetDaoRetrieveMembershipsByEntitiesResponse targetDaoRetrieveMembershipsByEntitiesResponse =
          this.retrieveMembershipsByEntities(new TargetDaoRetrieveMembershipsByEntitiesRequest(targetEntities));
      List<Object> targetMembershipsResult = targetDaoRetrieveMembershipsByEntitiesResponse == null ? null : targetDaoRetrieveMembershipsByEntitiesResponse.getTargetMemberships();
      targetMembershipsResults.addAll(targetMembershipsResult);
    }
    
    List<Object> targetMembershipsInput = targetDaoRetrieveMembershipsBulkRequest == null ? 
        null : targetDaoRetrieveMembershipsBulkRequest.getTargetMemberships();
    if (GrouperUtil.length(targetMembershipsInput) > 0) {
      
      TargetDaoRetrieveMembershipsResponse retrieveMembershipsResponse 
        = this.retrieveMemberships(new TargetDaoRetrieveMembershipsRequest(
          targetMembershipsInput));
      targetMembershipsResults.addAll(retrieveMembershipsResponse == null ? null :
        retrieveMembershipsResponse.getTargetMemberships());
    }
    
    return new TargetDaoRetrieveMembershipsBulkResponse(targetMembershipsResults);
  }


  @Override
  public TargetDaoRetrieveMembershipsByGroupsResponse retrieveMembershipsByGroups(
      TargetDaoRetrieveMembershipsByGroupsRequest targetDaoRetrieveMembershipsByGroupsRequest) {
    
    if (GrouperUtil.length(targetDaoRetrieveMembershipsByGroupsRequest.getTargetGroups()) == 0) {
      return new TargetDaoRetrieveMembershipsByGroupsResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByGroups(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoRetrieveMembershipsByGroupsResponse targetDaoRetrieveMembershipsByGroupsResponse = this.wrappedDao.retrieveMembershipsByGroups(targetDaoRetrieveMembershipsByGroupsRequest);
        hasError = logObjects(targetDaoRetrieveMembershipsByGroupsResponse.getTargetMemberships());
        return targetDaoRetrieveMembershipsByGroupsResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveMembershipsByGroups");
      }
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByGroup(), false)) {
      
      List<Object> results = new ArrayList<Object>();
      
      for (ProvisioningGroup provisioningGroup : targetDaoRetrieveMembershipsByGroupsRequest.getTargetGroups()) {

        TargetDaoRetrieveMembershipsByGroupResponse targetDaoRetrieveMembershipsByGroupResponse = this.retrieveMembershipsByGroup(
            new TargetDaoRetrieveMembershipsByGroupRequest(provisioningGroup));
        results.addAll(GrouperUtil.nonNull(targetDaoRetrieveMembershipsByGroupResponse.getTargetMemberships()));
      }
      return new TargetDaoRetrieveMembershipsByGroupsResponse(results);

    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = this.retrieveGroups(
          new TargetDaoRetrieveGroupsRequest(targetDaoRetrieveMembershipsByGroupsRequest.getTargetGroups(), true));
      return new TargetDaoRetrieveMembershipsByGroupsResponse((List<Object>)(Object)targetDaoRetrieveGroupsResponse.getTargetGroups());
    }
    
    throw new RuntimeException("Dao cannot retrieve memberships by group or groups");
  }


  @Override
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(
      TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    
    if (targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup() == null) {
      return new TargetDaoRetrieveMembershipsByGroupResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByGroup(), false)) {

      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoRetrieveMembershipsByGroupResponse targetDaoRetrieveMembershipsByGroupResponse = this.wrappedDao.retrieveMembershipsByGroup(targetDaoRetrieveMembershipsByGroupRequest);
        hasError = logObjects(targetDaoRetrieveMembershipsByGroupResponse.getTargetMemberships());
        return targetDaoRetrieveMembershipsByGroupResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveMembershipsByGroup");
      }

    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByGroups(), false)) {

      TargetDaoRetrieveMembershipsByGroupsRequest targetDaoRetrieveMembershipsByGroupsRequest = 
          new TargetDaoRetrieveMembershipsByGroupsRequest(GrouperUtil.toList(targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup()));
      
      TargetDaoRetrieveMembershipsByGroupsResponse targetDaoRetrieveMembershipsByGroupsResponse 
        = this.retrieveMembershipsByGroups(targetDaoRetrieveMembershipsByGroupsRequest);

      return new TargetDaoRetrieveMembershipsByGroupResponse(targetDaoRetrieveMembershipsByGroupsResponse.getTargetMemberships());
    }

    throw new RuntimeException("Dao cannot retrieve memberships by group or groups");
  }


  @Override
  public TargetDaoRetrieveMembershipsByEntitiesResponse retrieveMembershipsByEntities(
      TargetDaoRetrieveMembershipsByEntitiesRequest targetDaoRetrieveMembershipsByEntitiesRequest) {
    
    if (GrouperUtil.length(targetDaoRetrieveMembershipsByEntitiesRequest.getTargetEntities()) == 0) {
      return new TargetDaoRetrieveMembershipsByEntitiesResponse();
    }
   
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByEntities(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoRetrieveMembershipsByEntitiesResponse targetDaoRetrieveMembershipsByEntitiesResponse = this.wrappedDao.retrieveMembershipsByEntities(targetDaoRetrieveMembershipsByEntitiesRequest);
        hasError = logObjects(targetDaoRetrieveMembershipsByEntitiesResponse.getTargetMemberships());
        return targetDaoRetrieveMembershipsByEntitiesResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveMembershipsByEntities");
      }
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByEntity(), false)) {
      
      List<Object> results = new ArrayList<Object>();
      
      for (ProvisioningEntity provisioningEntity : targetDaoRetrieveMembershipsByEntitiesRequest.getTargetEntities()) {

        TargetDaoRetrieveMembershipsByEntityResponse targetDaoRetrieveMembershipsByEntityResponse = this.retrieveMembershipsByEntity(
            new TargetDaoRetrieveMembershipsByEntityRequest(provisioningEntity));
        results.addAll(GrouperUtil.nonNull(targetDaoRetrieveMembershipsByEntityResponse.getTargetMemberships()));
      }
      return new TargetDaoRetrieveMembershipsByEntitiesResponse(results);

    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = this.retrieveEntities(
          new TargetDaoRetrieveEntitiesRequest(targetDaoRetrieveMembershipsByEntitiesRequest.getTargetEntities(), true));
      return new TargetDaoRetrieveMembershipsByEntitiesResponse((List<Object>)(Object)targetDaoRetrieveEntitiesResponse.getTargetEntities());
    }

    throw new RuntimeException("Dao cannot retrieve memberships by entity or entities");
  }


  @Override
  public TargetDaoRetrieveMembershipsByEntityResponse retrieveMembershipsByEntity(
      TargetDaoRetrieveMembershipsByEntityRequest targetDaoRetrieveMembershipsByEntityRequest) {
    
    if (targetDaoRetrieveMembershipsByEntityRequest.getTargetEntity() == null) {
      return new TargetDaoRetrieveMembershipsByEntityResponse();
    }
    
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByEntity(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoRetrieveMembershipsByEntityResponse targetDaoRetrieveMembershipsByEntityResponse = this.wrappedDao.retrieveMembershipsByEntity(targetDaoRetrieveMembershipsByEntityRequest);
        return targetDaoRetrieveMembershipsByEntityResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveMembershipsByEntity");
      }

    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByEntities(), false)) {

      TargetDaoRetrieveMembershipsByEntitiesRequest targetDaoRetrieveMembershipsByEntitiesRequest = 
          new TargetDaoRetrieveMembershipsByEntitiesRequest(GrouperUtil.toList(targetDaoRetrieveMembershipsByEntityRequest.getTargetEntity()));
      
      TargetDaoRetrieveMembershipsByEntitiesResponse targetDaoRetrieveMembershipsByEntitiesResponse 
        = this.retrieveMembershipsByEntities(targetDaoRetrieveMembershipsByEntitiesRequest);

      return new TargetDaoRetrieveMembershipsByEntityResponse(targetDaoRetrieveMembershipsByEntitiesResponse.getTargetMemberships());
    }

    throw new RuntimeException("Dao cannot retrieve memberships by entity or entities");
  }


  @Override
  public TargetDaoRetrieveMembershipsResponse retrieveMemberships(
      TargetDaoRetrieveMembershipsRequest targetDaoRetrieveMembershipsRequest) {
    
    if (GrouperUtil.length(targetDaoRetrieveMembershipsRequest.getTargetMemberships()) == 0) {
      return new TargetDaoRetrieveMembershipsResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMemberships(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoRetrieveMembershipsResponse targetDaoRetrieveMembershipsResponse = this.wrappedDao.retrieveMemberships(targetDaoRetrieveMembershipsRequest);
        hasError = logObjects(targetDaoRetrieveMembershipsResponse.getTargetMemberships());
        return targetDaoRetrieveMembershipsResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveMemberships");
      }
    }

    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembership(), false)) {
        
        List<Object> results = new ArrayList<Object>();
        
        for (Object provisioningMembership : targetDaoRetrieveMembershipsRequest.getTargetMemberships()) {

          TargetDaoRetrieveMembershipResponse targetDaoRetrieveMembershipResponse = this.retrieveMembership(new TargetDaoRetrieveMembershipRequest(provisioningMembership));
          if (targetDaoRetrieveMembershipResponse != null && targetDaoRetrieveMembershipResponse.getTargetMembership() != null) {
            results.add(targetDaoRetrieveMembershipResponse.getTargetMembership());
          }
        }
        return new TargetDaoRetrieveMembershipsResponse(results);
      }
    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembership(), false)) {
        
        List<Object> results = new ArrayList<Object>();
        
        String attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
        
        for (Object provisioningMembership : targetDaoRetrieveMembershipsRequest.getTargetMemberships()) {

          ProvisioningGroup provisioningGroup = (ProvisioningGroup) provisioningMembership;
          
          
          Set<Object> attributeValueSet = (Set<Object>) provisioningGroup.retrieveAttributeValueSet(attributeForMemberships);
          
          for (Object attributeValue: GrouperUtil.nonNull(attributeValueSet)) {
            
            ProvisioningGroup clonedProvisioningGroup = provisioningGroup.clone();
            ProvisioningAttribute provisioningAttribute = new ProvisioningAttribute();
            provisioningAttribute.setValue(GrouperUtil.toSet(attributeValue));
            provisioningAttribute.setName(attributeForMemberships);
            
            clonedProvisioningGroup.getAttributes().put(attributeForMemberships, provisioningAttribute);
            
            TargetDaoRetrieveMembershipResponse targetDaoRetrieveMembershipResponse = this.retrieveMembership(new TargetDaoRetrieveMembershipRequest(clonedProvisioningGroup));
            if (targetDaoRetrieveMembershipResponse != null && targetDaoRetrieveMembershipResponse.getTargetMembership() != null) {
              results.add(targetDaoRetrieveMembershipResponse.getTargetMembership());
            }
            
          }
        
        }
        return new TargetDaoRetrieveMembershipsResponse(results);
      }
    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembership(), false)) {
        
        List<Object> results = new ArrayList<Object>();
        
        String attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
        
        for (Object provisioningMembership : targetDaoRetrieveMembershipsRequest.getTargetMemberships()) {

          ProvisioningEntity provisioningEntity = (ProvisioningEntity) provisioningMembership;
          
          
          Set<Object> attributeValueSet = (Set<Object>) provisioningEntity.retrieveAttributeValueSet(attributeForMemberships);
          
          for (Object attributeValue: GrouperUtil.nonNull(attributeValueSet)) {
            
            ProvisioningEntity clonedProvisioningEntity = provisioningEntity.clone();
            ProvisioningAttribute provisioningAttribute = new ProvisioningAttribute();
            provisioningAttribute.setValue(GrouperUtil.toSet(attributeValue));
            provisioningAttribute.setName(attributeForMemberships);
            
            clonedProvisioningEntity.getAttributes().put(attributeForMemberships, provisioningAttribute);
            
            TargetDaoRetrieveMembershipResponse targetDaoRetrieveMembershipResponse = this.retrieveMembership(new TargetDaoRetrieveMembershipRequest(clonedProvisioningEntity));
            if (targetDaoRetrieveMembershipResponse != null && targetDaoRetrieveMembershipResponse.getTargetMembership() != null) {
              results.add(targetDaoRetrieveMembershipResponse.getTargetMembership());
            }
            
          }
        
        }
        return new TargetDaoRetrieveMembershipsResponse(results);
      }
    }
    
    throw new RuntimeException("Dao cannot retrieve memberships or membership");
  }


  @Override
  public TargetDaoRetrieveEntitiesResponse retrieveEntities(
      TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest) {
    
    if (GrouperUtil.length(targetDaoRetrieveEntitiesRequest.getTargetEntities()) == 0) {
      return new TargetDaoRetrieveEntitiesResponse();
    }

    for (ProvisioningEntity provisioningEntity : targetDaoRetrieveEntitiesRequest.getTargetEntities()) {
      provisioningEntity.assignSearchFilter();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveEntities(), false)) {
      
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = this.wrappedDao.retrieveEntities(targetDaoRetrieveEntitiesRequest);
        hasError = logEntities(targetDaoRetrieveEntitiesResponse.getTargetEntities());
        return targetDaoRetrieveEntitiesResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveEntities");
      }

    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveEntity(), false)) {
      
      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();
      
      for (ProvisioningEntity provisioningEntity : targetDaoRetrieveEntitiesRequest.getTargetEntities()) {

        TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = this.retrieveEntity(new TargetDaoRetrieveEntityRequest(provisioningEntity, targetDaoRetrieveEntitiesRequest.isIncludeAllMembershipsIfApplicable()));
        if (targetDaoRetrieveEntityResponse != null && targetDaoRetrieveEntityResponse.getTargetEntity() != null) {
          results.add(targetDaoRetrieveEntityResponse.getTargetEntity());
        }
      }
      return new TargetDaoRetrieveEntitiesResponse(results);
    }

    throw new RuntimeException("Dao cannot retrieve entities or entity");
    
  }


  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(
      TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {
    
    if (targetDaoRetrieveGroupRequest.getTargetGroup() == null) {
      return new TargetDaoRetrieveGroupResponse();
    }

    targetDaoRetrieveGroupRequest.getTargetGroup().assignSearchFilter();
    
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveGroup(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoRetrieveGroupResponse targetDaoRetrieveGroupResponse = this.wrappedDao.retrieveGroup(targetDaoRetrieveGroupRequest);
        hasError = logGroup(targetDaoRetrieveGroupRequest.getTargetGroup());
        return targetDaoRetrieveGroupResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveGroup");
      }

    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveGroups(), false)) {
      
      TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
          this.retrieveGroups(new TargetDaoRetrieveGroupsRequest(GrouperUtil.toList(targetDaoRetrieveGroupRequest.getTargetGroup()), 
          targetDaoRetrieveGroupRequest.isIncludeAllMembershipsIfApplicable()));

      return new TargetDaoRetrieveGroupResponse(targetDaoRetrieveGroupsResponse == null ? null :
        GrouperUtil.length(targetDaoRetrieveGroupsResponse.getTargetGroups()) == 0 ? null : 
            targetDaoRetrieveGroupsResponse.getTargetGroups().get(0));
    }

    throw new RuntimeException("Dao cannot retrieve groups or group");
  }


  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(
      TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {
    
    if (targetDaoRetrieveEntityRequest.getTargetEntity() == null) {
      return new TargetDaoRetrieveEntityResponse();
    }

    targetDaoRetrieveEntityRequest.getTargetEntity().assignSearchFilter();

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveEntity(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = this.wrappedDao.retrieveEntity(targetDaoRetrieveEntityRequest);
        hasError = logEntity(targetDaoRetrieveEntityResponse.getTargetEntity());
        return targetDaoRetrieveEntityResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveEntity");
      }
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveEntities(), false)) {
      
      TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = 
          this.retrieveEntities(new TargetDaoRetrieveEntitiesRequest(GrouperUtil.toList(targetDaoRetrieveEntityRequest.getTargetEntity()), 
          targetDaoRetrieveEntityRequest.isIncludeAllMembershipsIfApplicable()));

      return new TargetDaoRetrieveEntityResponse(targetDaoRetrieveEntitiesResponse == null ? null :
        GrouperUtil.length(targetDaoRetrieveEntitiesResponse.getTargetEntities()) == 0 ? null : 
            targetDaoRetrieveEntitiesResponse.getTargetEntities().get(0));
    }

    throw new RuntimeException("Dao cannot retrieve entities or entity");
  }


  @Override
  public TargetDaoRetrieveMembershipResponse retrieveMembership(
      TargetDaoRetrieveMembershipRequest targetDaoRetrieveMembershipRequest) {
    
    if (targetDaoRetrieveMembershipRequest.getTargetMembership() == null) {
      return new TargetDaoRetrieveMembershipResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembership(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoRetrieveMembershipResponse targetDaoRetrieveMembershipResponse = this.wrappedDao.retrieveMembership(targetDaoRetrieveMembershipRequest);
        hasError = logObject(targetDaoRetrieveMembershipResponse.getTargetMembership());
        return targetDaoRetrieveMembershipResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "retrieveMembership");
      }

    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMemberships(), false)) {
      
      TargetDaoRetrieveMembershipsResponse targetDaoRetrieveMembershipsResponse = 
          this.retrieveMemberships(new TargetDaoRetrieveMembershipsRequest(GrouperUtil.toList(targetDaoRetrieveMembershipRequest.getTargetMembership())));

      return new TargetDaoRetrieveMembershipResponse(targetDaoRetrieveMembershipsResponse == null ? null :
        GrouperUtil.length(targetDaoRetrieveMembershipsResponse.getTargetMemberships()) == 0 ? null : 
            targetDaoRetrieveMembershipsResponse.getTargetMemberships().get(0));
    }

    throw new RuntimeException("Dao cannot retrieve memberships or membership");
  }


  @Override
  public TargetDaoUpdateGroupResponse updateGroup(
      TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();
    
    if (targetGroup == null) {
      return new TargetDaoUpdateGroupResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateGroup(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoUpdateGroupResponse targetDaoUpdateGroupResponse = this.wrappedDao.updateGroup(targetDaoUpdateGroupRequest);
        hasError = logGroup(targetGroup);
        if (targetGroup.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set updated group as provisioned: " + this.wrappedDao);
        }
        
        return targetDaoUpdateGroupResponse;
      } catch (RuntimeException e) {
        hasError = true;

        if (targetGroup.getProvisioned() == null) {
          targetGroup.setProvisioned(false);
        }
        if (targetGroup.getException() == null) {
          GrouperUtil.injectInException(e, targetGroup.toString());
          targetGroup.setException(e);
        }
        
        setExceptionForMembershipsWhenGroupOrEntityAttributes(null, targetGroup, e);
        
        logGroup(targetGroup);
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "updateGroup");
      }
      return null;
      
      /**
       * 
       * if it's group attributes
       * get the results back
       * if there are exceptions in the object change
       * get the attribute value
       * based on the attribute value, get the membership object (there's a map of attribute value to membership wrapper)
       * on that membership object set exception, failure
       * 
       */
      
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateGroups(), false)) {
      this.updateGroups(new TargetDaoUpdateGroupsRequest(GrouperUtil.toList(targetGroup)));
      return null;
    }

    throw new RuntimeException("Dao cannot update group or groups");
  }


  @Override
  public TargetDaoInsertGroupsResponse insertGroups(
      TargetDaoInsertGroupsRequest targetDaoInsertGroupsRequest) {
    
    if (GrouperUtil.length(targetDaoInsertGroupsRequest.getTargetGroups()) == 0) {
      return new TargetDaoInsertGroupsResponse();
    }
    
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertGroups(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoInsertGroupsResponse targetDaoInsertGroupsResponse = this.wrappedDao.insertGroups(targetDaoInsertGroupsRequest);
        hasError = logGroups(targetDaoInsertGroupsRequest.getTargetGroups());
        for (ProvisioningGroup provisioningGroup : targetDaoInsertGroupsRequest.getTargetGroups()) { 
          if (provisioningGroup.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set inserted group as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoInsertGroupsResponse;
      } catch (RuntimeException e) {
        hasError = true;
        for (ProvisioningGroup targetGroup : targetDaoInsertGroupsRequest.getTargetGroups()) { 
          
          if (targetGroup.getProvisioned() == null) {
            targetGroup.setProvisioned(false);
          }
          if (targetGroup.getException() == null) {
            GrouperUtil.injectInException(e, targetGroup.toString());
            targetGroup.setException(e);
          }
        }
        logGroups(targetDaoInsertGroupsRequest.getTargetGroups());
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "insertGroups");
      }
      return new TargetDaoInsertGroupsResponse();
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertGroup(), false)) {
      for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetDaoInsertGroupsRequest.getTargetGroups())) {
        insertGroup(new TargetDaoInsertGroupRequest(provisioningGroup));
      }
      return new TargetDaoInsertGroupsResponse();
    }

    throw new RuntimeException("Dao cannot insert group or groups");
  }


  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(
      TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {
    
    if (targetDaoDeleteEntityRequest.getTargetEntity() == null) {
      return new TargetDaoDeleteEntityResponse();
    }

    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteEntity(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoDeleteEntityResponse targetDaoDeleteEntityResponse = this.wrappedDao.deleteEntity(targetDaoDeleteEntityRequest);
        hasError = logEntity(targetEntity);
        if (targetEntity.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set deleted entity as provisioned: " + this.wrappedDao);
        }
        return targetDaoDeleteEntityResponse;
      } catch (RuntimeException e) {
        hasError = true;

        if (targetEntity.getProvisioned() == null) {
          targetEntity.setProvisioned(false);
        }
        if (targetEntity.getException() == null) {
          GrouperUtil.injectInException(e, targetEntity.toString());
          targetEntity.setException(e);
        }
        logEntity(targetEntity);
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "deleteEntity");
      }
      return new TargetDaoDeleteEntityResponse();
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteEntities(), false)) {
      this.deleteEntities(new TargetDaoDeleteEntitiesRequest(GrouperUtil.toList(targetEntity)));
      return new TargetDaoDeleteEntityResponse();
    }

    throw new RuntimeException("Dao cannot insert entity or entities");

  }

  /**
   * log grouper provisioning lists
   * @param grouperProvisioningLists
   */
  private boolean logProvisioningLists(GrouperProvisioningLists grouperProvisioningLists) {
    boolean hasError = false;
    if (grouperProvisioningLists != null) {
      if (logGroups(grouperProvisioningLists.getProvisioningGroups())) {
        hasError = true;
      }
      if (logEntities(grouperProvisioningLists.getProvisioningEntities())) {
        hasError = true;
      }
      if (logMemberships(grouperProvisioningLists.getProvisioningMemberships())) {
        hasError = true;
      }
    }
    return hasError;
  }

  /**
   * log errors in entity
   * @param provisioningEntities
   * @return if error
   */
  private boolean logEntities(List<ProvisioningEntity> provisioningEntities) {
    
    boolean hasError = false;
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(provisioningEntities)) {
      if (logEntity(provisioningEntity)) {
        hasError = true;
      }
    }
    return hasError;
  }

  /**
   * log errors in entities
   * @param provisioningEntities
   * @return if error
   */
  private boolean logEntity(ProvisioningEntity provisioningEntity) {
    if (provisioningEntity != null && provisioningEntity.getException() != null) {
      LOG.error("Error in provisioner '" + this.getGrouperProvisioner().getConfigId() + "' - '" + this.getGrouperProvisioner().getInstanceId() + "' with entity: " + provisioningEntity, 
          provisioningEntity.getException());
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDiagnosticsContainer().isInDiagnostics()) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningDiagnosticsContainer().appendReportLineIfNotBlank("Error in entity: " + provisioningEntity + ", " + GrouperUtil.getFullStackTrace(provisioningEntity.getException()));
      }
      return true;
    }
    return false;
  }

  /**
   * log errors in groups
   * @param provisioningGroups
   * @return if has error
   */
  private boolean logGroups(List<ProvisioningGroup> provisioningGroups) {
    boolean hasError = false;
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(provisioningGroups)) {
      if (logGroup(provisioningGroup)) {
        hasError = true;
      }
    }
    return hasError;
  }

  /**
   * log errors in group
   * @param provisioningEntities
   */
  private boolean logGroup(ProvisioningGroup provisioningGroup) {
    if (provisioningGroup != null && provisioningGroup.getException() != null) {
      LOG.error("Error in provisioner '" + this.getGrouperProvisioner().getConfigId() + "' - '" 
          + this.getGrouperProvisioner().getInstanceId() + "' with group: " + provisioningGroup, 
          provisioningGroup.getException());
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDiagnosticsContainer().isInDiagnostics()) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningDiagnosticsContainer().appendReportLineIfNotBlank("Error in group: " + provisioningGroup + ", " + GrouperUtil.getFullStackTrace(provisioningGroup.getException()));
      }
      return true;
    }
    return false;
  }

  /**
   * log errors in object
   * @param provisioningObjects
   */
  private boolean logObjects(List<Object> provisioningObjects) {
    boolean hasError = false;
    for (Object provisioningObject : GrouperUtil.nonNull(provisioningObjects)) {
      if (logObject(provisioningObject)) {
        hasError = true;
      }
    }
    return hasError;
  }

  /**
   * log errors in group
   * @param provisioningEntities
   */
  private boolean logObject(Object provisioningObject) {
    
    if (provisioningObject instanceof ProvisioningGroup) {
      return logGroup((ProvisioningGroup)provisioningObject);
    }
    if (provisioningObject instanceof ProvisioningEntity) {
      return logEntity((ProvisioningEntity)provisioningObject);
    }
    if (provisioningObject instanceof ProvisioningMembership) {
      return logMembership((ProvisioningMembership)provisioningObject);
    }
    return false;
  }

  /**
   * log errors in membership
   * @param provisioningMemberships
   */
  private boolean logMemberships(List<ProvisioningMembership> provisioningMemberships) {
    
    boolean hasError = false;
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(provisioningMemberships)) {
      if (logMembership(provisioningMembership)) {
        hasError = true;
      }
    }
    return hasError;
  }

  /**
   * log errors in entities
   * @param provisioningEntities
   */
  private boolean logMembership(ProvisioningMembership provisioningMembership) {
    if (provisioningMembership != null && provisioningMembership.getException() != null) {
      LOG.error("Error with provisioner '" + this.getGrouperProvisioner().getConfigId() + "' - '" + this.getGrouperProvisioner().getInstanceId() + "' with membership: " + provisioningMembership, 
          provisioningMembership.getException());
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDiagnosticsContainer().isInDiagnostics()) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningDiagnosticsContainer().appendReportLineIfNotBlank("Error in membership: " + provisioningMembership + ", " + GrouperUtil.getFullStackTrace(provisioningMembership.getException()));
      }
      return true;
    }
    return false;
  }

  @Override
  public TargetDaoDeleteEntitiesResponse deleteEntities(
      TargetDaoDeleteEntitiesRequest targetDaoDeleteEntitiesRequest) {

    if (GrouperUtil.length(targetDaoDeleteEntitiesRequest.getTargetEntities()) == 0) {
      return new TargetDaoDeleteEntitiesResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteEntities(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoDeleteEntitiesResponse targetDaoDeleteEntitiesResponse = this.wrappedDao.deleteEntities(targetDaoDeleteEntitiesRequest);
        hasError = logEntities(targetDaoDeleteEntitiesRequest.getTargetEntities());
        for (ProvisioningEntity provisioningEntity : targetDaoDeleteEntitiesRequest.getTargetEntities()) { 
          if (provisioningEntity.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set deleted entity as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoDeleteEntitiesResponse;
      } catch (RuntimeException e) {
        hasError = true;
        for (ProvisioningEntity targetEntity : targetDaoDeleteEntitiesRequest.getTargetEntities()) { 
          
          if (targetEntity.getProvisioned() == null) {
            targetEntity.setProvisioned(false);
          }
          if (targetEntity.getException() == null) {
            GrouperUtil.injectInException(e, targetEntity.toString());
            targetEntity.setException(e);
          }
        }
        logEntities(targetDaoDeleteEntitiesRequest.getTargetEntities());
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "deleteEntities");
      }
      return new TargetDaoDeleteEntitiesResponse();
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteEntity(), false)) {
      for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(targetDaoDeleteEntitiesRequest.getTargetEntities())) {
        deleteEntity(new TargetDaoDeleteEntityRequest(provisioningEntity));
      }
      return new TargetDaoDeleteEntitiesResponse();
    }

    throw new RuntimeException("Dao cannot delete entity or entities");
  }


  @Override
  public TargetDaoInsertEntityResponse insertEntity(
      TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {

    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    if (targetEntity == null) {
      return new TargetDaoInsertEntityResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertEntity(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();
        TargetDaoInsertEntityResponse targetDaoInsertEntityResponse = this.wrappedDao.insertEntity(targetDaoInsertEntityRequest);
        hasError = logEntity(targetEntity);
        if (targetEntity.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set inserted entity as provisioned: " + this.wrappedDao);
        }
        return targetDaoInsertEntityResponse;
      } catch (RuntimeException e) {
        hasError = true;

        if (targetEntity.getProvisioned() == null) {
          targetEntity.setProvisioned(false);
        }
        if (targetEntity.getException() == null) {
          GrouperUtil.injectInException(e, targetEntity.toString());
          targetEntity.setException(e);
        }
        logEntity(targetEntity);
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "insertEntity");
      }
      return new TargetDaoInsertEntityResponse();
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertEntities(), false)) {
      this.insertEntities(new TargetDaoInsertEntitiesRequest(GrouperUtil.toList(targetEntity)));
      return new TargetDaoInsertEntityResponse();
    }

    throw new RuntimeException("Dao cannot insert entity or entities");
  }


  @Override
  public TargetDaoInsertEntitiesResponse insertEntities(
      TargetDaoInsertEntitiesRequest targetDaoInsertEntitiesRequest) {

    if (GrouperUtil.length(targetDaoInsertEntitiesRequest.getTargetEntityInserts()) == 0) {
      return new TargetDaoInsertEntitiesResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertEntities(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();
        TargetDaoInsertEntitiesResponse targetDaoInsertEntitiesResponse = this.wrappedDao.insertEntities(targetDaoInsertEntitiesRequest);
        hasError = logEntities(targetDaoInsertEntitiesRequest.getTargetEntityInserts());
        for (ProvisioningEntity provisioningEntity : targetDaoInsertEntitiesRequest.getTargetEntityInserts()) { 
          if (provisioningEntity.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set inserted entity as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoInsertEntitiesResponse;
      } catch (RuntimeException e) {
        hasError = true;
        for (ProvisioningEntity targetEntity : targetDaoInsertEntitiesRequest.getTargetEntityInserts()) { 
          
          if (targetEntity.getProvisioned() == null) {
            targetEntity.setProvisioned(false);
          }
          if (targetEntity.getException() == null) {
            GrouperUtil.injectInException(e, targetEntity.toString());
            targetEntity.setException(e);
          }
        }
        logEntities(targetDaoInsertEntitiesRequest.getTargetEntityInserts());
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "insertEntities");
      }
      return new TargetDaoInsertEntitiesResponse();
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertEntity(), false)) {
      for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(targetDaoInsertEntitiesRequest.getTargetEntityInserts())) {
        insertEntity(new TargetDaoInsertEntityRequest(provisioningEntity));
      }
      return new TargetDaoInsertEntitiesResponse();
    }

    throw new RuntimeException("Dao cannot insert entity or entities");
  }


  @Override
  public TargetDaoUpdateEntityResponse updateEntity(
      TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();
    
    if (targetEntity == null) {
      return new TargetDaoUpdateEntityResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateEntity(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoUpdateEntityResponse targetDaoUpdateEntityResponse = this.wrappedDao.updateEntity(targetDaoUpdateEntityRequest);
        hasError = logEntity(targetEntity);
        if (targetEntity.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set updateed entity as provisioned: " + this.wrappedDao);
        }
        return targetDaoUpdateEntityResponse;
      } catch (RuntimeException e) {
        hasError = true;

        if (targetEntity.getProvisioned() == null) {
          targetEntity.setProvisioned(false);
        }
        if (targetEntity.getException() == null) {
          GrouperUtil.injectInException(e, targetEntity.toString());
          targetEntity.setException(e);
        }
        
        setExceptionForMembershipsWhenGroupOrEntityAttributes(targetEntity, null, e);
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "updateEntity");
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateEntities(), false)) {
      this.updateEntities(new TargetDaoUpdateEntitiesRequest(GrouperUtil.toList(targetEntity)));
      return null;
    }

    throw new RuntimeException("Dao cannot update entity or entities");
  }


  private void setExceptionForMembershipsWhenGroupOrEntityAttributes(ProvisioningEntity targetEntity, ProvisioningGroup targetGroup, Exception e) {
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      
      Set<ProvisioningObjectChange> provisionObjectChanges = targetEntity.getInternal_objectChanges();
      for (ProvisioningObjectChange provisioningObjectChange: provisionObjectChanges) {
        if (provisioningObjectChange.getException() != null) {
          
          ProvisioningAttribute provisioningAttribute = targetEntity.getAttributes().get(provisioningObjectChange.getAttributeName());
          
          if (provisioningAttribute != null) {
            Map<Object, ProvisioningMembershipWrapper> valueToProvisioningMembershipWrapper = provisioningAttribute.getValueToProvisioningMembershipWrapper();
            
            if (valueToProvisioningMembershipWrapper != null) {
              
              
              ProvisioningMembershipWrapper provisioningMembershipWrapper = null;
              if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert || 
                  provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.update) {
                
                provisioningMembershipWrapper = valueToProvisioningMembershipWrapper.get(provisioningObjectChange.getNewValue());
              } else {
                provisioningMembershipWrapper = valueToProvisioningMembershipWrapper.get(provisioningObjectChange.getOldValue());
              }
              
              if (provisioningMembershipWrapper.getGrouperTargetMembership() != null) {
                provisioningMembershipWrapper.getGrouperTargetMembership().setException(e);
                provisioningMembershipWrapper.getGrouperTargetMembership().setProvisioned(false);
              } else {
                if (provisioningMembershipWrapper.getGcGrouperSyncMembership() != null) {
                  provisioningMembershipWrapper.getGcGrouperSyncMembership().setErrorMessage(GrouperUtil.getFullStackTrace(e));
                  provisioningMembershipWrapper.getGcGrouperSyncMembership().setErrorTimestamp(new Timestamp(System.currentTimeMillis()));
                }
                
              }
             
            }
          }
          
        }
        
      }
    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      
      Set<ProvisioningObjectChange> provisionObjectChanges = targetGroup.getInternal_objectChanges();
      for (ProvisioningObjectChange provisioningObjectChange: provisionObjectChanges) {
        if (provisioningObjectChange.getException() != null) {
          
          ProvisioningAttribute provisioningAttribute = targetGroup.getAttributes().get(provisioningObjectChange.getAttributeName());
          
          if (provisioningAttribute != null) {
            Map<Object, ProvisioningMembershipWrapper> valueToProvisioningMembershipWrapper = provisioningAttribute.getValueToProvisioningMembershipWrapper();
            
            if (valueToProvisioningMembershipWrapper != null) {
              
              ProvisioningMembershipWrapper provisioningMembershipWrapper = null;
              if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert || 
                  provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.update) {
                
                provisioningMembershipWrapper = valueToProvisioningMembershipWrapper.get(provisioningObjectChange.getNewValue());
              } else {
                provisioningMembershipWrapper = valueToProvisioningMembershipWrapper.get(provisioningObjectChange.getOldValue());
              }
              
              if (provisioningMembershipWrapper == null) {
                continue;
              }
              
              if (provisioningMembershipWrapper.getGrouperTargetMembership() != null) {
                provisioningMembershipWrapper.getGrouperTargetMembership().setException(e);
                provisioningMembershipWrapper.getGrouperTargetMembership().setProvisioned(false);
              } else {
                if (provisioningMembershipWrapper.getGcGrouperSyncMembership() != null) {
                  provisioningMembershipWrapper.getGcGrouperSyncMembership().setErrorMessage(GrouperUtil.getFullStackTrace(e));
                  provisioningMembershipWrapper.getGcGrouperSyncMembership().setErrorTimestamp(new Timestamp(System.currentTimeMillis()));
                }
                
              }
              
             
            }
          }
          
        }
        
      }
    }
    
  }
  
  @Override
  public TargetDaoUpdateEntitiesResponse updateEntities(
      TargetDaoUpdateEntitiesRequest targetDaoUpdateEntitiesRequest) {
    
    if (GrouperUtil.length(targetDaoUpdateEntitiesRequest.getTargetEntities()) == 0) {
      return new TargetDaoUpdateEntitiesResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateEntities(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();
        TargetDaoUpdateEntitiesResponse targetDaoUpdateEntitiesResponse = this.wrappedDao.updateEntities(targetDaoUpdateEntitiesRequest);
        hasError = logEntities(targetDaoUpdateEntitiesRequest.getTargetEntities());
        for (ProvisioningEntity provisioningEntity : targetDaoUpdateEntitiesRequest.getTargetEntities()) { 
          if (provisioningEntity.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set updated entity as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoUpdateEntitiesResponse;
      } catch (RuntimeException e) {
        hasError = true;

        for (ProvisioningEntity targetEntity : targetDaoUpdateEntitiesRequest.getTargetEntities()) { 
          
          if (targetEntity.getProvisioned() == null) {
            targetEntity.setProvisioned(false);
          }
          if (targetEntity.getException() == null) {
            GrouperUtil.injectInException(e, targetEntity.toString());
            targetEntity.setException(e);
          }
          
          setExceptionForMembershipsWhenGroupOrEntityAttributes(targetEntity, null, e);
        }
        
        logEntities(targetDaoUpdateEntitiesRequest.getTargetEntities());
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "updateEntities");
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateEntity(), false)) {
      for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(targetDaoUpdateEntitiesRequest.getTargetEntities())) {
        updateEntity(new TargetDaoUpdateEntityRequest(provisioningEntity));
      }
      return null;
    }

    throw new RuntimeException("Dao cannot update entity or entities");
  }


  @Override
  public TargetDaoDeleteMembershipResponse deleteMembership(
      TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
    ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest.getTargetMembership();
    
    if (targetMembership == null) {
      return new TargetDaoDeleteMembershipResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteMembership(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();
        TargetDaoDeleteMembershipResponse targetDaoDeleteMembershipResponse = this.wrappedDao.deleteMembership(targetDaoDeleteMembershipRequest);
        hasError = logMembership(targetMembership);
        if (targetMembership.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set deleted membership as provisioned: " + this.wrappedDao);
        }
        return targetDaoDeleteMembershipResponse;
      } catch (RuntimeException e) {
        hasError = true;

        if (targetMembership.getProvisioned() == null) {
          targetMembership.setProvisioned(false);
        }
        if (targetMembership.getException() == null) {
          GrouperUtil.injectInException(e, targetMembership.toString());
          targetMembership.setException(e);
        }
        logMembership(targetMembership);
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "deleteMembership");
      }
      return new TargetDaoDeleteMembershipResponse();
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteMemberships(), false)) {
      this.deleteMemberships(new TargetDaoDeleteMembershipsRequest(GrouperUtil.toList(targetMembership)));
      return new TargetDaoDeleteMembershipResponse();
    }

    throw new RuntimeException("Dao cannot delete membership or memberships");

  }


  @Override
  public TargetDaoDeleteGroupsResponse deleteGroups(
      TargetDaoDeleteGroupsRequest targetDaoDeleteGroupsRequest) {

    if (GrouperUtil.length(targetDaoDeleteGroupsRequest.getTargetGroups()) == 0) {
      return new TargetDaoDeleteGroupsResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteGroups(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();
        TargetDaoDeleteGroupsResponse targetDaoDeleteGroupsResponse = this.wrappedDao.deleteGroups(targetDaoDeleteGroupsRequest);
        hasError = logGroups(targetDaoDeleteGroupsRequest.getTargetGroups());
        for (ProvisioningGroup provisioningGroup : targetDaoDeleteGroupsRequest.getTargetGroups()) { 
          if (provisioningGroup.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set deleted group as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoDeleteGroupsResponse;
      } catch (RuntimeException e) {
        hasError = true;
        for (ProvisioningGroup targetGroup : targetDaoDeleteGroupsRequest.getTargetGroups()) { 
          
          if (targetGroup.getProvisioned() == null) {
            targetGroup.setProvisioned(false);
          }
          if (targetGroup.getException() == null) {
            GrouperUtil.injectInException(e, targetGroup.toString());
            targetGroup.setException(e);
          }
        }
        logGroups(targetDaoDeleteGroupsRequest.getTargetGroups());
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "deleteGroups");
      }
      return new TargetDaoDeleteGroupsResponse();
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteGroup(), false)) {
      for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetDaoDeleteGroupsRequest.getTargetGroups())) {
        deleteGroup(new TargetDaoDeleteGroupRequest(provisioningGroup));
      }
      return new TargetDaoDeleteGroupsResponse();
    }

    throw new RuntimeException("Dao cannot delete group or groups");
    
  }


  @Override
  public TargetDaoInsertMembershipResponse insertMembership(
      TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();
    
    if (targetMembership == null) {
      return new TargetDaoInsertMembershipResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertMembership(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();
        TargetDaoInsertMembershipResponse targetDaoInsertMembershipResponse = this.wrappedDao.insertMembership(targetDaoInsertMembershipRequest);
        hasError = logMembership(targetMembership);
        if (targetMembership.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set inserted membership as provisioned: " + this.wrappedDao);
        }
        return targetDaoInsertMembershipResponse;
      } catch (RuntimeException e) {
        hasError = true;

        if (targetMembership.getProvisioned() == null) {
          targetMembership.setProvisioned(false);
        }
        if (targetMembership.getException() == null) {
          GrouperUtil.injectInException(e, targetMembership.toString());
          targetMembership.setException(e);
        }
        logMembership(targetMembership);
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "insertMembership");
      }
      return new TargetDaoInsertMembershipResponse();
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertMemberships(), false)) {
      this.insertMemberships(new TargetDaoInsertMembershipsRequest(GrouperUtil.toList(targetMembership)));
      return new TargetDaoInsertMembershipResponse();
    }

    throw new RuntimeException("Dao cannot insert membership or memberships");
  }


  @Override
  public TargetDaoInsertMembershipsResponse insertMemberships(
      TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {
    
    if (GrouperUtil.length(targetDaoInsertMembershipsRequest.getTargetMemberships()) == 0) {
      return new TargetDaoInsertMembershipsResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertMemberships(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();
        TargetDaoInsertMembershipsResponse targetDaoInsertMembershipsResponse = this.wrappedDao.insertMemberships(targetDaoInsertMembershipsRequest);
        hasError = logMemberships(targetDaoInsertMembershipsRequest.getTargetMemberships());
        for (ProvisioningMembership provisioningMembership : targetDaoInsertMembershipsRequest.getTargetMemberships()) { 
          if (provisioningMembership.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set inserted membership as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoInsertMembershipsResponse;
      } catch (RuntimeException e) {
        hasError = true;
        for (ProvisioningMembership targetMembership : targetDaoInsertMembershipsRequest.getTargetMemberships()) { 
          
          if (targetMembership.getProvisioned() == null) {
            targetMembership.setProvisioned(false);
          }
          if (targetMembership.getException() == null) {
            GrouperUtil.injectInException(e, targetMembership.toString());
            targetMembership.setException(e);
          }
        }
        logMemberships(targetDaoInsertMembershipsRequest.getTargetMemberships());
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "insertMemberships");
      }
      return new TargetDaoInsertMembershipsResponse();
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertMembership(), false)) {
      for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(targetDaoInsertMembershipsRequest.getTargetMemberships())) {
        insertMembership(new TargetDaoInsertMembershipRequest(provisioningMembership));
      }
      return new TargetDaoInsertMembershipsResponse();
    }

    throw new RuntimeException("Dao cannot insert membership or memberships");
  }


  @Override
  public TargetDaoUpdateMembershipResponse updateMembership(
      TargetDaoUpdateMembershipRequest targetDaoUpdateMembershipRequest) {
    ProvisioningMembership targetMembership = targetDaoUpdateMembershipRequest.getTargetMembership();
    
    if (targetMembership == null) {
      return new TargetDaoUpdateMembershipResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateMembership(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();
        TargetDaoUpdateMembershipResponse targetDaoUpdateMembershipResponse = this.wrappedDao.updateMembership(targetDaoUpdateMembershipRequest);
        hasError = logMembership(targetDaoUpdateMembershipRequest.getTargetMembership());
        if (targetMembership.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set updated membership as provisioned: " + this.wrappedDao);
        }
        return targetDaoUpdateMembershipResponse;
      } catch (RuntimeException e) {
        hasError = true;

        if (targetMembership.getProvisioned() == null) {
          targetMembership.setProvisioned(false);
        }
        if (targetMembership.getException() == null) {
          GrouperUtil.injectInException(e, targetMembership.toString());
          targetMembership.setException(e);
        }
        logMembership(targetDaoUpdateMembershipRequest.getTargetMembership());
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "methodName");
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateMemberships(), false)) {
      this.updateMemberships(new TargetDaoUpdateMembershipsRequest(GrouperUtil.toList(targetMembership)));
      return null;
    }

    throw new RuntimeException("Dao cannot update membership or memberships");
  }

  @Override
  public TargetDaoUpdateMembershipsResponse updateMemberships(
      TargetDaoUpdateMembershipsRequest targetDaoUpdateMembershipsRequest) {
    
    if (GrouperUtil.length(targetDaoUpdateMembershipsRequest.getTargetMemberships()) == 0) {
      return new TargetDaoUpdateMembershipsResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateMemberships(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();
        TargetDaoUpdateMembershipsResponse targetDaoUpdateMembershipsResponse = this.wrappedDao.updateMemberships(targetDaoUpdateMembershipsRequest);
        hasError = logMemberships(targetDaoUpdateMembershipsRequest.getTargetMemberships());
        for (ProvisioningMembership provisioningMembership : targetDaoUpdateMembershipsRequest.getTargetMemberships()) { 
          if (provisioningMembership.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set updated membership as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoUpdateMembershipsResponse;
      } catch (RuntimeException e) {
        hasError = true;

        for (ProvisioningMembership targetMembership : targetDaoUpdateMembershipsRequest.getTargetMemberships()) { 
          
          if (targetMembership.getProvisioned() == null) {
            targetMembership.setProvisioned(false);
          }
          if (targetMembership.getException() == null) {
            GrouperUtil.injectInException(e, targetMembership.toString());
            targetMembership.setException(e);
          }
        }
        logMemberships(targetDaoUpdateMembershipsRequest.getTargetMemberships());
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "updateMemberships");
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateMembership(), false)) {
      for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(targetDaoUpdateMembershipsRequest.getTargetMemberships())) {
        updateMembership(new TargetDaoUpdateMembershipRequest(provisioningMembership));
      }
      return null;
    }

    throw new RuntimeException("Dao cannot update membership or memberships");
  }


  @Override
  public TargetDaoSendEntityChangesToTargetResponse sendEntityChangesToTarget(
      TargetDaoSendEntityChangesToTargetRequest targetDaoSendEntityChangesToTargetRequest) {
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanSendEntityChangesToTarget(), false)) {
      
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoSendEntityChangesToTargetResponse targetDaoSendEntityChangesToTargetResponse = this.wrappedDao.sendEntityChangesToTarget(targetDaoSendEntityChangesToTargetRequest);
        if (logEntities(targetDaoSendEntityChangesToTargetRequest.getTargetEntityInserts())) {
          hasError = true;
        }
        if (logEntities(targetDaoSendEntityChangesToTargetRequest.getTargetEntityUpdates())) {
          hasError = true;
        }
        if (logEntities(targetDaoSendEntityChangesToTargetRequest.getTargetEntityDeletes())) {
          hasError = true;
        }
        return targetDaoSendEntityChangesToTargetResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "sendEntityChangesToTarget");
      }
      
    }
    if (GrouperUtil.length(targetDaoSendEntityChangesToTargetRequest.getTargetEntityDeletes()) > 0){
      List<ProvisioningEntity> targetEntityDeletes = targetDaoSendEntityChangesToTargetRequest.getTargetEntityDeletes();
      TargetDaoDeleteEntitiesRequest targetDaoDeleteEntitiesRequest = new TargetDaoDeleteEntitiesRequest();
      targetDaoDeleteEntitiesRequest.setTargetEntities(targetEntityDeletes);
      this.deleteEntities(targetDaoDeleteEntitiesRequest);
    }

    if (GrouperUtil.length(targetDaoSendEntityChangesToTargetRequest.getTargetEntityInserts()) > 0) {
      List<ProvisioningEntity> targetEntityInserts = targetDaoSendEntityChangesToTargetRequest.getTargetEntityInserts();
      
      this.insertEntities(new TargetDaoInsertEntitiesRequest(targetEntityInserts));
    }
    if (GrouperUtil.length(targetDaoSendEntityChangesToTargetRequest.getTargetEntityUpdates()) > 0) {
      List<ProvisioningEntity> targetEntityUpdates = targetDaoSendEntityChangesToTargetRequest.getTargetEntityUpdates();
      
      this.updateEntities(new TargetDaoUpdateEntitiesRequest(targetEntityUpdates));
    }
    return null;
  }


  @Override
  public TargetDaoSendMembershipChangesToTargetResponse sendMembershipChangesToTarget(
      TargetDaoSendMembershipChangesToTargetRequest targetDaoSendMembershipChangesToTargetRequest) {

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanSendMembershipChangesToTarget(), false)) {
      
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();

        TargetDaoSendMembershipChangesToTargetResponse targetDaoSendMembershipChangesToTargetResponse = this.wrappedDao.sendMembershipChangesToTarget(targetDaoSendMembershipChangesToTargetRequest);
        if (logMemberships(targetDaoSendMembershipChangesToTargetRequest.getTargetMembershipInserts())) {
          hasError = true;
        }
        if (logMemberships(targetDaoSendMembershipChangesToTargetRequest.getTargetMembershipUpdates())) {
          hasError = true;
        }
        if (logMemberships(targetDaoSendMembershipChangesToTargetRequest.getTargetMembershipDeletes())) {
          hasError = true;
        }
        
        Collection<List<ProvisioningMembership>> provisioningMembershipsLists = targetDaoSendMembershipChangesToTargetRequest.getTargetMembershipReplaces().values();
        
        List<ProvisioningMembership> provisioningMembershipsToLog = new ArrayList<ProvisioningMembership>();
        for (List<ProvisioningMembership> provisioningMemberships: provisioningMembershipsLists) {
          provisioningMembershipsToLog.addAll(provisioningMemberships);
        }
        
        if (logMemberships(provisioningMembershipsToLog)) {
          hasError = true;
        }
        return targetDaoSendMembershipChangesToTargetResponse;
      } catch (RuntimeException e) {
        hasError = true;
        throw e;
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "sendMembershipChangesToTarget");
      }

    }
    {
      List<ProvisioningMembership> targetMembershipDeletes = targetDaoSendMembershipChangesToTargetRequest.getTargetMembershipDeletes();
      TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest = new TargetDaoDeleteMembershipsRequest();
      targetDaoDeleteMembershipsRequest.setTargetMemberships(targetMembershipDeletes);
      this.deleteMemberships(targetDaoDeleteMembershipsRequest);
    }

    {
      List<ProvisioningMembership> targetMembershipInserts = targetDaoSendMembershipChangesToTargetRequest.getTargetMembershipInserts();
      
      this.insertMemberships(new TargetDaoInsertMembershipsRequest(targetMembershipInserts));
    }
    {
      List<ProvisioningMembership> targetMembershipUpdates = targetDaoSendMembershipChangesToTargetRequest.getTargetMembershipUpdates();
      
      this.updateMemberships(new TargetDaoUpdateMembershipsRequest(targetMembershipUpdates));
    }
    {
      Map<ProvisioningGroup, List<ProvisioningMembership>> targetMembershipReplaces = targetDaoSendMembershipChangesToTargetRequest.getTargetMembershipReplaces();
      
      for (ProvisioningGroup targetGroup: targetMembershipReplaces.keySet()) {
        
        this.replaceGroupMemberships(new TargetDaoReplaceGroupMembershipsRequest(targetGroup, targetMembershipReplaces.get(targetGroup)));
      }
      
      
    }
    return null;

  }

  @Override
  public TargetDaoReplaceGroupMembershipsResponse replaceGroupMemberships(TargetDaoReplaceGroupMembershipsRequest targetDaoReplaceGroupMembershipsRequest) {
    
    
    if (GrouperUtil.length(targetDaoReplaceGroupMembershipsRequest.getTargetMemberships()) == 0) {
      return new TargetDaoReplaceGroupMembershipsResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanReplaceGroupMemberships(), false)) {
      boolean hasError = false;
      boolean commandLogStarted = false;
      try {
        commandLogStarted = commandLogStartLoggingIfConfigured();
        TargetDaoReplaceGroupMembershipsResponse targetDaoReplaceGroupMembershipsResponse = this.wrappedDao.replaceGroupMemberships(targetDaoReplaceGroupMembershipsRequest);
        hasError = logMemberships(targetDaoReplaceGroupMembershipsRequest.getTargetMemberships());
        for (ProvisioningMembership provisioningMembership : targetDaoReplaceGroupMembershipsRequest.getTargetMemberships()) { 
          if (provisioningMembership.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set updated membership as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoReplaceGroupMembershipsResponse;
      } catch (RuntimeException e) {
        hasError = true;
        for (ProvisioningMembership targetMembership : targetDaoReplaceGroupMembershipsRequest.getTargetMemberships()) { 
          
          if (targetMembership.getProvisioned() == null) {
            targetMembership.setProvisioned(false);
          }
          if (targetMembership.getException() == null) {
            GrouperUtil.injectInException(e, targetMembership.toString());
            targetMembership.setException(e);
          }
        }
        logMemberships(targetDaoReplaceGroupMembershipsRequest.getTargetMemberships());
      } finally {
        commandLogFinallyBlock(commandLogStarted, hasError, "replaceGroupMemberships");
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateMembership(), false)) {
      for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(targetDaoReplaceGroupMembershipsRequest.getTargetMemberships())) {
        updateMembership(new TargetDaoUpdateMembershipRequest(provisioningMembership));
      }
      return null;
    }

    throw new RuntimeException("Dao cannot update membership or memberships");
    
  }
  
  



}
