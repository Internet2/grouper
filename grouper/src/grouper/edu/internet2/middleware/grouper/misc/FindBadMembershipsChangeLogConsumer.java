package edu.internet2.middleware.grouper.misc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningProcessingResult;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvent;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventType;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class FindBadMembershipsChangeLogConsumer extends EsbListenerBase {
  
  private static final Log LOG = GrouperUtil.getLog(FindBadMembershipsChangeLogConsumer.class);

  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public void disconnect() {
    // nothing
  }
  
  @Override
  public ProvisioningSyncConsumerResult dispatchEventList(
      List<EsbEventContainer> esbEventContainers,
      GrouperProvisioningProcessingResult grouperProvisioningProcessingResult) {

    ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();
    
    Map<String, Set<Composite>> findAsFactorOrHasMemberOfFactorResults = new HashMap<String, Set<Composite>>();

    for (EsbEventContainer esbEventContainer : esbEventContainers) {

      EsbEventType esbEventType = esbEventContainer.getEsbEventType();
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();

      if (esbEventType == EsbEventType.MEMBERSHIP_ADD || esbEventType == EsbEventType.MEMBERSHIP_DELETE) {
        
        if (esbEvent.getSourceId().equals("g:gsa")) {
          continue;
        }
        
        String memberId = esbEvent.getMemberId();
        
        if (findAsFactorOrHasMemberOfFactorResults.get(esbEvent.getGroupId()) == null) {
          findAsFactorOrHasMemberOfFactorResults.put(esbEvent.getGroupId(), GrouperDAOFactory.getFactory().getComposite().findAsFactorOrHasMemberOfFactor(esbEvent.getGroupId()));
        }
        
        Set<Composite> composites = findAsFactorOrHasMemberOfFactorResults.get(esbEvent.getGroupId());
        for (Composite composite : composites) {
          Group owner = GrouperDAOFactory.getFactory().getGroup().findByUuid(composite.getFactorOwnerUuid(), true);
          Group left = GrouperDAOFactory.getFactory().getGroup().findByUuid(composite.getLeftFactorUuid(), true);
          Group right = GrouperDAOFactory.getFactory().getGroup().findByUuid(composite.getRightFactorUuid(), true);
          
          boolean ownerHasMember = hasMember(owner.getUuid(), memberId);
          boolean compositeShouldHaveMember = false;

          // check to see if the composite *should* have the member
          if (composite.getType().equals(CompositeType.UNION) && 
              (hasMember(right.getUuid(), memberId) || hasMember(left.getUuid(), memberId))) {
            compositeShouldHaveMember = true;
          } else if (composite.getType().equals(CompositeType.INTERSECTION) && 
              (hasMember(right.getUuid(), memberId) && hasMember(left.getUuid(), memberId))) {
            compositeShouldHaveMember = true;
          } else if (composite.getType().equals(CompositeType.COMPLEMENT) && 
              (!hasMember(right.getUuid(), memberId) && hasMember(left.getUuid(), memberId))) {
            compositeShouldHaveMember = true;
          }
          
          // fix the composite membership if necessary
          if (compositeShouldHaveMember && !ownerHasMember) {
            Membership ms = Composite.internal_createNewCompositeMembershipObject(owner.getUuid(), memberId, composite.getUuid());
            LOG.warn("Adding composite membership for groupName=" + owner.getName() + ", groupId=" + owner.getUuid() + ", memberId=" + memberId + ", compositeId=" + composite.getUuid());
            GrouperDAOFactory.getFactory().getMembership().save(ms);
            this.getEsbConsumer().getChangeLogProcessorMetadata().getHib3GrouperLoaderLog().addInsertCount(1);
          } else if (!compositeShouldHaveMember && ownerHasMember) {
            Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
              owner.getUuid(), memberId, Group.getDefaultList(), MembershipType.COMPOSITE.getTypeString(), true, false);
            LOG.warn("Deleting composite membership for groupName=" + owner.getName() + ", groupId=" + owner.getUuid() + ", memberId=" + memberId + ", compositeId=" + composite.getUuid());
            ms.delete();
            this.getEsbConsumer().getChangeLogProcessorMetadata().getHib3GrouperLoaderLog().addDeleteCount(1);
          }
        }
      }
    }

    provisioningSyncConsumerResult.setLastProcessedSequenceNumber(esbEventContainers.get(esbEventContainers.size()-1).getSequenceNumber());
    return provisioningSyncConsumerResult;
  }
  
  private static boolean hasMember(String groupId, String memberId) {
    Set<Membership> mships = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndMemberAndField(
        groupId, memberId, Group.getDefaultList(), true);
    
    if (mships.size() > 0) {
      return true;
    }
    
    return false;
  }
}
