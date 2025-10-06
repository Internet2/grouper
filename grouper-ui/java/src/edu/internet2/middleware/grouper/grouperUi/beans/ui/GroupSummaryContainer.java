package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.provisioning.GuiGrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.misc.CompositeType;

public class GroupSummaryContainer {
  
  private boolean moreDetails = false;
  
  public boolean isMoreDetails() {
    return moreDetails;
  }
  
  public void setMoreDetails(boolean moreDetails) {
    this.moreDetails = moreDetails;
  }
  
  private boolean canRead = false;
  
  
  public boolean isCanRead() {
    return canRead;
  }
  
  public void setCanRead(boolean canRead) {
    this.canRead = canRead;
  }
  private int directMembersCount;
  
  private int totalMembersCount;
  
  private int notGroupMembersCount;
  
  private int directGroupMembersCount;

  private int groupAsMemberCount;
  
  private Set<GuiGroup> directGroupMembers = new HashSet<>(); 

  private Set<GuiGroup> groupsWhereTheCurrentGroupIsMemberOf = new HashSet<>(); 
  
  private boolean isAttestation;
  
  private boolean isComposite;
  
  private int compositeSize;
  
  private Set<GuiGroup> composites = new HashSet<>();
  
  private int provisioningAssignmentCount;

  private List<GuiGrouperProvisioningAttributeValue> guiGrouperProvisioningAttributeValues;

  private GuiGroup compositeLeftGroup;

  private GuiGroup compositeRightGroup;

  private CompositeType compositeType;

  private String attestationDateCertified;

  private int attributeAssignmentsCount;

  private int rulesCount;

  private int rulesCountWhereGroupIsUsed;

  private int newMembershipsInTheLastMonth;

  private int membershipsRemovedInTheLastMonth;

  private int auditsInTheLastMonth;

  private int configurationUsedCount;

  private int abacScriptedGroupDependenciesCount;

  private Set<GuiGroup> abacScriptedGroupDependencies;

  private int nonGroupTotalPrivilegesCount;

  private int totalPrivilegesCount;

  private int directPrivilegesCount;

  private int directGroupPrivilegesCount;

  private Set<GuiGroup> directGroupPrivilegesGroups;

  private int countOfWhereGroupIsBeingUsedInPrivileges;

  private Set<GuiGroup> groupsWhereGroupIsBeingUsedInPrivileges;

  
  public int getDirectMembersCount() {
    return directMembersCount;
  }

  
  public void setDirectMembersCount(int directMembersCount) {
    this.directMembersCount = directMembersCount;
  }
  
  
  
  public int getTotalMembersCount() {
    return totalMembersCount;
  }


  
  public void setTotalMembersCount(int totalMembersCount) {
    this.totalMembersCount = totalMembersCount;
  }
  
  
  
  public Set<GuiGroup> getGroupsWhereTheCurrentGroupIsMemberOf() {
    return groupsWhereTheCurrentGroupIsMemberOf;
  }


  
  public void setGroupsWhereTheCurrentGroupIsMemberOf(Set<GuiGroup> groupsWhereTheCurrentGroupIsMemberOf) {
    this.groupsWhereTheCurrentGroupIsMemberOf = groupsWhereTheCurrentGroupIsMemberOf;
  }


  public int getNotGroupMembersCount() {
    return notGroupMembersCount;
  }


  
  public void setNotGroupMembersCount(int notGroupMembersCount) {
    this.notGroupMembersCount = notGroupMembersCount;
  }


  
  public int getDirectGroupMembersCount() {
    return directGroupMembersCount;
  }

  
  public void setDirectGroupMembersCount(int directGroupMembersCount) {
    this.directGroupMembersCount = directGroupMembersCount;
  }

  
  public Set<GuiGroup> getDirectGroupMembers() {
    return directGroupMembers;
  }

  
  public void setDirectGroupMembers(Set<GuiGroup> directGroupMembers) {
    this.directGroupMembers = directGroupMembers;
  }
  
  
  public int getGroupAsMemberCount() {
    return groupAsMemberCount;
  }


  
  public void setGroupAsMemberCount(int groupAsMemberCount) {
    this.groupAsMemberCount = groupAsMemberCount;
  }


  public boolean isAttestation() {
    return isAttestation;
  }

  
  public void setAttestation(boolean isAttestation) {
    this.isAttestation = isAttestation;
  }

  
  public boolean isComposite() {
    return isComposite;
  }

  
  public void setComposite(boolean isComposite) {
    this.isComposite = isComposite;
  }

  
  public int getCompositeSize() {
    return compositeSize;
  }

  
  public void setCompositeSize(int compositeSize) {
    this.compositeSize = compositeSize;
  }

  
  public Set<GuiGroup> getComposites() {
    return composites;
  }

  
  public void setComposites(Set<GuiGroup> composites) {
    this.composites = composites;
  }

  
  public int getProvisioningAssignmentCount() {
    return provisioningAssignmentCount;
  }

  
  public void setProvisioningAssignmentCount(int provisioningAssignmentCount) {
    this.provisioningAssignmentCount = provisioningAssignmentCount;
  }

  public void setGuiGrouperProvisioningAttributeValues(
      List<GuiGrouperProvisioningAttributeValue> guiGrouperProvisioningAttributeValues) {
    this.guiGrouperProvisioningAttributeValues = guiGrouperProvisioningAttributeValues;
  }
  
  public List<GuiGrouperProvisioningAttributeValue> getGuiGrouperProvisioningAttributeValues() {
    return guiGrouperProvisioningAttributeValues;
  }


  public void setCompositeLeftGroup(GuiGroup leftGroup) {
    this.compositeLeftGroup = leftGroup;
  }

  
  public GuiGroup getCompositeLeftGroup() {
    return compositeLeftGroup;
  }
  
  public void setCompositeRightGroup(GuiGroup rightGroup) {
    this.compositeRightGroup = rightGroup;
  }

  
  public GuiGroup getCompositeRightGroup() {
    return compositeRightGroup;
  }


  public void setCompositeType(CompositeType compositeType) {
    this.compositeType = compositeType;
  }

  public CompositeType getCompositeType() {
    return compositeType;
  }


  public void setAttestationDateCertified(String attestationDateCertified) {
    this.attestationDateCertified = attestationDateCertified;
  }
  
  public String getAttestationDateCertified() {
    return attestationDateCertified;
  }


  public void setAttributeAssignmentsCount(int attributeAssignmentsCount) {
    this.attributeAssignmentsCount = attributeAssignmentsCount;
  }

  public int getAttributeAssignmentsCount() {
    return attributeAssignmentsCount;
  }


  public void setRulesCount(int rulesCount) {
    this.rulesCount = rulesCount;
  }
  
  public int getRulesCount() {
    return rulesCount;
  }


  public void setRulesCountWhereGroupIsUsed(int rulesCountWhereGroupIsUsed) {
    this.rulesCountWhereGroupIsUsed = rulesCountWhereGroupIsUsed;
  }
  
  public int getRulesCountWhereGroupIsUsed() {
    return rulesCountWhereGroupIsUsed;
  }


  public void setNewMembershipsInTheLastMonth(int newMembershipsInTheLastMonth) {
    this.newMembershipsInTheLastMonth = newMembershipsInTheLastMonth;
  }
  
  public int getNewMembershipsInTheLastMonth() {
    return newMembershipsInTheLastMonth;
  }


  public void setMembershipsRemovedInTheLastMonth(int membershipsRemovedInTheLastMonth) {
    this.membershipsRemovedInTheLastMonth = membershipsRemovedInTheLastMonth;
  }
  
  public int getMembershipsRemovedInTheLastMonth() {
    return membershipsRemovedInTheLastMonth;
  }


  public void setAuditsInTheLastMonth(int auditsInTheLastMonth) {
    this.auditsInTheLastMonth = auditsInTheLastMonth; 
  }


  
  public int getAuditsInTheLastMonth() {
    return auditsInTheLastMonth;
  }


  public void setConfigurationUsedCount(int configurationUsedCount) {
    this.configurationUsedCount = configurationUsedCount;
  }

  public int getConfigurationUsedCount() {
    return configurationUsedCount;
  }


  public void setAbacScriptedGroupDependenciesCount(int abacScriptedGroupDependenciesCount) {
    this.abacScriptedGroupDependenciesCount = abacScriptedGroupDependenciesCount;
  }

  public int getAbacScriptedGroupDependenciesCount() {
    return abacScriptedGroupDependenciesCount;
  }


  public void setAbacScriptedGroupDependencies(Set<GuiGroup> abacScriptedGroupDependencies) {
    this.abacScriptedGroupDependencies = abacScriptedGroupDependencies;
  }

  public Set<GuiGroup> getAbacScriptedGroupDependencies() {
    return abacScriptedGroupDependencies;
  }


  public void setNonGroupTotalPrivilegesCount(int nonGroupTotalPrivilegesCount) {
    this.nonGroupTotalPrivilegesCount = nonGroupTotalPrivilegesCount;
  }

  public int getNonGroupTotalPrivilegesCount() {
    return nonGroupTotalPrivilegesCount;
  }


  public void setTotalPrivilegesCount(int totalPrivilegesCount) {
    this.totalPrivilegesCount = totalPrivilegesCount;
  }


  
  public int getTotalPrivilegesCount() {
    return totalPrivilegesCount;
  }


  public void setDirectPrivilegesCount(int directPrivilegesCount) {
    this.directPrivilegesCount = directPrivilegesCount;
  }

  public int getDirectPrivilegesCount() {
    return directPrivilegesCount;
  }


  public void setDirectGroupPrivilegesCount(int directGroupPrivilegesCount) {
    this.directGroupPrivilegesCount = directGroupPrivilegesCount;
  }


  
  public int getDirectGroupPrivilegesCount() {
    return directGroupPrivilegesCount;
  }

  public Set<GuiGroup> getDirectGroupPrivilegesGroups() {
    return directGroupPrivilegesGroups;
  }
  
  public void setDirectGroupPrivilegesGroups(Set<GuiGroup> directGroupPrivilegesGroups) {
    this.directGroupPrivilegesGroups = directGroupPrivilegesGroups;
  }


  public void setCountOfWhereGroupIsBeingUsedInPrivileges(int countOfWhereGroupIsBeingUsedInPrivileges) {
    this.countOfWhereGroupIsBeingUsedInPrivileges = countOfWhereGroupIsBeingUsedInPrivileges;
  }


  
  public int getCountOfWhereGroupIsBeingUsedInPrivileges() {
    return countOfWhereGroupIsBeingUsedInPrivileges;
  }


  
  public Set<GuiGroup> getGroupsWhereGroupIsBeingUsedInPrivileges() {
    return groupsWhereGroupIsBeingUsedInPrivileges;
  }


  
  public void setGroupsWhereGroupIsBeingUsedInPrivileges(Set<GuiGroup> groupsWhereGroupIsBeingUsedInPrivileges) {
    this.groupsWhereGroupIsBeingUsedInPrivileges = groupsWhereGroupIsBeingUsedInPrivileges;
  }

  
  
}
