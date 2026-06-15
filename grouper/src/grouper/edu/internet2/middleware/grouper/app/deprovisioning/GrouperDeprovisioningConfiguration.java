/**
 * Copyright 2018 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.type.StringType;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * configuration on an object
 */
public class GrouperDeprovisioningConfiguration {

  /**
   * we want to retain the last emailed date and last certified date
   */
  public void clearOutConfigurationButLeaveMetadata() {
    //lets see if certified or emailed?
    if (this.newConfig == null 
        || (this.newConfig.getCertifiedDate() == null
        && this.newConfig.getLastEmailedDate() == null)) {
      this.setNewConfig(null);
    } else {
      GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValueNew = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValueNew.setCertifiedDate(this.newConfig.getCertifiedDate());
      grouperDeprovisioningAttributeValueNew.setLastEmailedDate(this.newConfig.getLastEmailedDate());
      grouperDeprovisioningAttributeValueNew.setDeprovision(false);
      grouperDeprovisioningAttributeValueNew.setAffiliationString(this.newConfig.getAffiliationString());
      grouperDeprovisioningAttributeValueNew.setDirectAssignment(false);
      this.setNewConfig(this.newConfig);
      this.newConfig = grouperDeprovisioningAttributeValueNew;
    }

  }
  
  /**
   * grouper deprovisioning overall configuration
   */
  private GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = null;
  
  /**
   * @return the grouperDeprovisioningOverallConfiguration
   */
  public GrouperDeprovisioningOverallConfiguration getGrouperDeprovisioningOverallConfiguration() {
    return this.grouperDeprovisioningOverallConfiguration;
  }
  
  /**
   * @param grouperDeprovisioningOverallConfiguration1 the grouperDeprovisioningOverallConfiguration to set
   */
  public void setGrouperDeprovisioningOverallConfiguration(
      GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration1) {
    this.grouperDeprovisioningOverallConfiguration = grouperDeprovisioningOverallConfiguration1;
  }

  /**
   * if retrieve configs all at once, put the inherited config here
   */
  private GrouperDeprovisioningConfiguration inheritedConfig = null;
  
  /**
   * if retrieve configs all at once, put the inherited config here
   * @return the inheritedConfig
   */
  public GrouperDeprovisioningConfiguration getInheritedConfig() {
    return this.inheritedConfig;
  }
  
  /**
   * if retrieve configs all at once, put the inherited config here
   * @param inheritedConfig1 the inheritedConfig to set
   */
  public void setInheritedConfig(GrouperDeprovisioningConfiguration inheritedConfig1) {
    this.inheritedConfig = inheritedConfig1;
  }

  /**
   * base existing attribute assign for this configuration
   */
  private AttributeAssign attributeAssignBase = null;
  
  /**
   * base existing attribute assign for this configuration
   * @return the attribute assign
   */
  public AttributeAssign getAttributeAssignBase() {
    return this.attributeAssignBase;
  }

  /**
   * base existing attribute assign for this configuration
   * @param attributeAssignBase1
   */
  public void setAttributeAssignBase(AttributeAssign attributeAssignBase1) {
    this.attributeAssignBase = attributeAssignBase1;
  }

  /**
   * Stem that is the inherited owner
   */
  private Stem inheritedOwner;

  
  /**
   * @return the inheritedOwner
   */
  public Stem getInheritedOwner() {
    return this.inheritedOwner;
  }

  
  /**
   * @param inheritedOwner1 the inheritedOwner to set
   */
  public void setInheritedOwner(Stem inheritedOwner1) {
    this.inheritedOwner = inheritedOwner1;
  }

  /**
   * original config in the database
   */
  private GrouperDeprovisioningAttributeValue originalConfig;
  
  
  /**
   * @return the originalConfig
   */
  public GrouperDeprovisioningAttributeValue getOriginalConfig() {
    return this.originalConfig;
  }

  
  /**
   * @param originalConfig1 the originalConfig to set
   */
  public void setOriginalConfig(GrouperDeprovisioningAttributeValue originalConfig1) {
    this.originalConfig = originalConfig1;
  }

  /**
   * new config after calculations
   */
  private GrouperDeprovisioningAttributeValue newConfig;
  
  /**
   * new config after calculations
   * @return the newConfig
   */
  public GrouperDeprovisioningAttributeValue getNewConfig() {
    return this.newConfig;
  }

  
  /**
   * new config after calculations
   * @param newConfig1 the newConfig to set
   */
  public void setNewConfig(GrouperDeprovisioningAttributeValue newConfig1) {
    this.newConfig = newConfig1;
  }
  
  /**
   * Store the configuration, serialized per owner so concurrent saves cannot create duplicate
   * single-assign metadata assignments.  A second save (double-submit, or another UI/WS/daemon node)
   * blocks on the SELECT FOR UPDATE below until the first save commits, then sees its committed
   * metadata and updates rather than inserting a duplicate.
   * @return how many changes made
   */
  public int storeConfiguration() {

    final int[] changeCountHolder = new int[]{0};

    // READ_WRITE_NEW (not USE_EXISTING) so each owner's save commits and releases its owner-row
    // lock immediately.  The deprovisioning full-sync daemon saves many owners in one outer
    // transaction; joining it would hold every owner lock until the whole run committed.
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {

            // serialize concurrent saves for this owner.  any other save of this owner's
            // deprovisioning configuration blocks here until this transaction commits.
            GrouperDeprovisioningConfiguration.this.lockOwnerRowForUpdate();

            changeCountHolder[0] = GrouperDeprovisioningConfiguration.this.storeConfigurationHelper();

            return null;
          }
        });

    return changeCountHolder[0];
  }

  /**
   * Acquire a row-level lock on the owner (group/stem/attributeDef) of this configuration via
   * SELECT FOR UPDATE, so concurrent deprovisioning-config saves of the same owner are serialized.
   * This is the same idiom GrouperLoader.claimJobIfNotRunning uses to serialize across nodes.
   */
  private void lockOwnerRowForUpdate() {

    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration =
        this.getGrouperDeprovisioningOverallConfiguration();
    if (grouperDeprovisioningOverallConfiguration == null) {
      return;
    }

    GrouperObject originalOwner = grouperDeprovisioningOverallConfiguration.getOriginalOwner();
    if (originalOwner == null) {
      return;
    }

    String tableName = null;
    String ownerId = null;
    if (originalOwner instanceof Group) {
      tableName = Group.TABLE_GROUPER_GROUPS;
      ownerId = ((Group)originalOwner).getId();
    } else if (originalOwner instanceof Stem) {
      tableName = Stem.TABLE_GROUPER_STEMS;
      ownerId = ((Stem)originalOwner).getId();
    } else if (originalOwner instanceof AttributeDef) {
      tableName = AttributeDef.TABLE_GROUPER_ATTRIBUTE_DEF;
      ownerId = ((AttributeDef)originalOwner).getId();
    } else {
      return;
    }

    if (StringUtils.isBlank(ownerId)) {
      return;
    }

    HibernateSession.bySqlStatic().listSelect(String.class,
        "select id from " + tableName + " where id = ? for update",
        GrouperUtil.toListObject(ownerId),
        HibUtils.listType(StringType.INSTANCE));
  }

  /**
   * Do the actual store of the configuration.  Always invoked under the per-owner lock acquired by
   * {@link #storeConfiguration()}.
   * @return how many changes made
   */
  private int storeConfigurationHelper() {

    try {

      // belt-and-suspenders: collapse any duplicate metadata assignment-on-assignment rows that
      // predate this fix (e.g. created by an earlier concurrent save).  The lock above prevents new
      // duplicates; this heals existing ones so the deprovisioning read path does not break
      // (AttributeAssignValueFinder throws when an owner+attributeDefName appears twice).
      this.removeDuplicateMetadataAssignments();

      GrouperDeprovisioningAttributeValue newConfig = this.getNewConfig();
      GrouperDeprovisioningAttributeValue originalConfig = this.getOriginalConfig();
  
      if (newConfig == null
          && originalConfig == null) {
        return 0;
      }
  
      if (newConfig == null) {
        this.getAttributeAssignBase().deleteAndStore();
        return 1;
      }
  
      if (originalConfig == null) {
        originalConfig = new GrouperDeprovisioningAttributeValue();
        originalConfig.setGrouperDeprovisioningConfiguration(this);
        this.setOriginalConfig(originalConfig);
      }
      
      //make sure things are set to null if default
      newConfig.flattenValues();
      originalConfig.flattenValues();
      
      int[] changeCount = new int[]{0};
  
      //it has both new and old, lets compare and do the saves
      updateAttribute( 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAllowAddsWhileDeprovisioned(), 
          originalConfig.getAllowAddsWhileDeprovisionedString(),
          newConfig.getAllowAddsWhileDeprovisionedString(), changeCount);
      originalConfig.setAllowAddsWhileDeprovisionedString(newConfig.getAllowAddsWhileDeprovisionedString());
  
      updateAttribute( 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoChangeLoader(), 
          originalConfig.getAutoChangeLoaderString(),
          newConfig.getAutoChangeLoaderString(), changeCount);
      originalConfig.setAutoChangeLoaderString(newConfig.getAutoChangeLoaderString());
  
      updateAttribute( 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoSelectForRemoval(),
          originalConfig.getAutoselectForRemovalString(),
          newConfig.getAutoselectForRemovalString(), changeCount);
      originalConfig.setAutoselectForRemovalString(newConfig.getAutoselectForRemovalString());
  
      updateAttribute( 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision(), 
          originalConfig.getDeprovisionString(),
          newConfig.getDeprovisionString(), changeCount);
      originalConfig.setDeprovisionString(newConfig.getDeprovisionString());
  
      updateAttribute( 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment(), 
          originalConfig.getDirectAssignmentString(),
          newConfig.getDirectAssignmentString(), changeCount);
      originalConfig.setDirectAssignmentString(newConfig.getDirectAssignmentString());
  
      updateAttribute( 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailAddresses(), 
          originalConfig.getEmailAddressesString(),
          newConfig.getEmailAddressesString(), changeCount);
      originalConfig.setEmailAddressesString(newConfig.getEmailAddressesString());
  
      updateAttribute( 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailBody(), 
          originalConfig.getEmailBodyString(),
          newConfig.getEmailBodyString(), changeCount);
      originalConfig.setEmailBodyString(newConfig.getEmailBodyString());
  
      updateAttribute( 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameInheritedFromFolderId(), 
          originalConfig.getInheritedFromFolderIdString(),
          newConfig.getInheritedFromFolderIdString(), changeCount);
      originalConfig.setInheritedFromFolderIdString(newConfig.getInheritedFromFolderIdString());
  
      updateAttribute( 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup(), 
          originalConfig.getMailToGroupString(),
          newConfig.getMailToGroupString(), changeCount);
      originalConfig.setMailToGroupString(newConfig.getMailToGroupString());
  
      updateAttribute( 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation(), 
          originalConfig.getAffiliationString(),
          newConfig.getAffiliationString(), changeCount);
      originalConfig.setAffiliationString(newConfig.getAffiliationString());
  
      updateAttribute( 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameSendEmail(), 
          originalConfig.getSendEmailString(),
          newConfig.getSendEmailString(), changeCount);
      originalConfig.setSendEmailString(newConfig.getSendEmailString());
  
      updateAttribute( 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameShowForRemoval(), 
          originalConfig.getShowForRemovalString(),
          newConfig.getShowForRemovalString(), changeCount);
      originalConfig.setShowForRemovalString(newConfig.getShowForRemovalString());
  
      updateAttribute( 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameStemScope(), 
          originalConfig.getStemScopeString(),
          newConfig.getStemScopeString(), changeCount);
      originalConfig.setStemScopeString(newConfig.getStemScopeString());
  
      updateAttribute( 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameCertifiedMillis(),
          originalConfig.getCertifiedMillisString(),
          newConfig.getCertifiedMillisString(), changeCount);
      originalConfig.setCertifiedMillisString(newConfig.getCertifiedMillisString());
  
      updateAttribute( 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameLastEmailedDate(),
          originalConfig.getLastEmailedDateString(),
          newConfig.getLastEmailedDateString(), changeCount);
      originalConfig.setLastEmailedDateString(newConfig.getLastEmailedDateString());
      return changeCount[0];
    } finally {
      if (this.getGrouperDeprovisioningOverallConfiguration() != null 
          && this.getGrouperDeprovisioningOverallConfiguration().getOriginalOwner() != null) {
                
        GrouperObject originalOwner = this.getGrouperDeprovisioningOverallConfiguration().getOriginalOwner();
        GrouperDeprovisioningOverallConfiguration.cacheClear(originalOwner);
        
        //clear the entire cache because children derive their values from root and store them in cache
        if (originalOwner instanceof Stem && ((Stem) originalOwner).isRootStem()) {
          GrouperDeprovisioningOverallConfiguration.cacheClear();
        }
        
      }      
    }

  }

  /**
   * 
   * @return true if has attributes (direct or inherited) in the database
   */
  public boolean isHasDatabaseAttributes() {
    return this.originalConfig != null;
  }
  
  /**
   * 
   * @return true if has attributes (direct or inherited) in the database
   */
  public boolean isHasDatabaseConfiguration() {
    if (!this.isHasDatabaseAttributes()) {
      return false;
    }
    return this.originalConfig.isDirectAssignment() || !StringUtils.isBlank(this.originalConfig.getInheritedFromFolderIdString());
  }
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this);
    try {
      // Bypass privilege checks.  If the group is loaded it is viewable.
      toStringBuilder
        .append( "hasDatabaseConfiguration", this.isHasDatabaseConfiguration());
      toStringBuilder
      .append( "hasDatabaseAttributes", this.isHasDatabaseAttributes());
      
      if (this.inheritedOwner != null) {
        toStringBuilder.append("inheritedOwner", this.inheritedOwner.getName());
      }
      if (this.inheritedConfig != null) {
        toStringBuilder.append("inheritedConfig", "exists");
      }
      if (this.attributeAssignBase != null) {
        toStringBuilder.append("attributeAssignBase", this.attributeAssignBase.getId());
      }
      if (this.originalConfig != null) {
        toStringBuilder.append("originalConfig", this.originalConfig);
      }
      if (this.newConfig != null) {
        toStringBuilder.append("newConfig", this.newConfig);
      }
        
    } catch (Exception e) {
      //ignore, did all we could
    }
    return toStringBuilder.toString();

  }

  /**
   * update an attribute if it needs it
   * @param attributeDefName
   * @param originalValue
   * @param newValue
   * @param changeCount
   */
  private void updateAttribute(
      AttributeDefName attributeDefName, String originalValue, String newValue, int[] changeCount) {
    
    // not changed
    if (StringUtils.equals(StringUtils.trimToNull(originalValue), StringUtils.trimToNull(newValue))) {
      return;
    }
    
    changeCount[0]++;
    if (newValue == null) {
      if (this.getAttributeAssignBase() != null) {

        // remove attribute and value
        this.getAttributeAssignBase().getAttributeDelegate().removeAttribute(attributeDefName);
        
      }
    } else {
      
      //if there is not base assign, then add one
      if (this.getAttributeAssignBase() == null) {
        GrouperObject grouperObject = this.getGrouperDeprovisioningOverallConfiguration().getOriginalOwner();
        AttributeAssignable attributeAssignable = (AttributeAssignable)grouperObject;
        AttributeAssign attributeAssign = attributeAssignable.getAttributeDelegate().addAttribute(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
        this.setAttributeAssignBase(attributeAssign);
      }
      
      this.getAttributeAssignBase().getAttributeValueDelegate().assignValue(attributeDefName.getName(), newValue);
    }
  }

  /**
   * Collapse any duplicate metadata assignment-on-assignment rows on the base deprovisioning
   * assignment.  The deprovisioning metadata attributes are single-assign, so there should be at
   * most one assignment per attributeDefName on the base assign.  Concurrent saves can violate that
   * (see storeConfiguration), and the read path then fails.  Keep the newest assignment per
   * attributeDefName (by created-on) and delete the rest, mirroring the maintenance daemon's
   * GrouperDaemonDeleteMultipleCorruption.fixAssigns behavior so the data self-heals on every save.
   */
  private void removeDuplicateMetadataAssignments() {

    AttributeAssign attributeAssignBase = this.getAttributeAssignBase();
    if (attributeAssignBase == null) {
      return;
    }

    Set<AttributeAssign> metadataAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign()
        .findByOwnerAttributeAssignId(attributeAssignBase.getId(), new QueryOptions().secondLevelCache(false));

    if (GrouperUtil.length(metadataAttributeAssigns) < 2) {
      return;
    }

    // group the metadata assignments by their attributeDefName
    Map<String, List<AttributeAssign>> attributeDefNameIdToAttributeAssigns = new HashMap<String, List<AttributeAssign>>();
    for (AttributeAssign metadataAttributeAssign : metadataAttributeAssigns) {
      String attributeDefNameId = metadataAttributeAssign.getAttributeDefNameId();
      List<AttributeAssign> attributeAssignsForName = attributeDefNameIdToAttributeAssigns.get(attributeDefNameId);
      if (attributeAssignsForName == null) {
        attributeAssignsForName = new ArrayList<AttributeAssign>();
        attributeDefNameIdToAttributeAssigns.put(attributeDefNameId, attributeAssignsForName);
      }
      attributeAssignsForName.add(metadataAttributeAssign);
    }

    for (String attributeDefNameId : attributeDefNameIdToAttributeAssigns.keySet()) {

      List<AttributeAssign> attributeAssignsForName = attributeDefNameIdToAttributeAssigns.get(attributeDefNameId);
      if (GrouperUtil.length(attributeAssignsForName) < 2) {
        continue;
      }

      // keep the newest (largest created-on), delete the rest
      AttributeAssign attributeAssignToKeep = null;
      for (AttributeAssign attributeAssign : attributeAssignsForName) {
        if (attributeAssignToKeep == null) {
          attributeAssignToKeep = attributeAssign;
          continue;
        }
        Long createdOnDb = attributeAssign.getCreatedOnDb();
        Long createdOnDbToKeep = attributeAssignToKeep.getCreatedOnDb();
        if (createdOnDb != null && (createdOnDbToKeep == null || createdOnDb > createdOnDbToKeep)) {
          attributeAssignToKeep = attributeAssign;
        }
      }

      for (AttributeAssign attributeAssign : attributeAssignsForName) {
        if (StringUtils.equals(attributeAssign.getId(), attributeAssignToKeep.getId())) {
          continue;
        }
        attributeAssign.delete();
      }
    }
  }


}
