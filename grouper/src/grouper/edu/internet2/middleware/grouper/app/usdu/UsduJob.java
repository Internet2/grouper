package edu.internet2.middleware.grouper.app.usdu;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * usdu daemon
 */
@DisallowConcurrentExecution
public class UsduJob extends OtherJobBase {
  
  private static final Log LOG = GrouperUtil.getLog(UsduJob.class);
  
  /** map list names to corresponding privileges, a better way probably exists */
  private static Map<String, Privilege> list2priv = new HashMap<String, Privilege>();
  
  private static Map<String, UsduSource> usduConfiguredSources = new HashMap<String, UsduSource>();
  
  static {
    list2priv.put(Field.FIELD_NAME_ADMINS, AccessPrivilege.ADMIN);
    list2priv.put(Field.FIELD_NAME_OPTINS, AccessPrivilege.OPTIN);
    list2priv.put(Field.FIELD_NAME_OPTOUTS, AccessPrivilege.OPTOUT);
    list2priv.put(Field.FIELD_NAME_READERS, AccessPrivilege.READ);
    list2priv.put(Field.FIELD_NAME_UPDATERS, AccessPrivilege.UPDATE);
    list2priv.put(Field.FIELD_NAME_VIEWERS, AccessPrivilege.VIEW);
    list2priv.put(Field.FIELD_NAME_GROUP_ATTR_READERS, AccessPrivilege.GROUP_ATTR_READ);
    list2priv.put(Field.FIELD_NAME_GROUP_ATTR_UPDATERS, AccessPrivilege.GROUP_ATTR_UPDATE);
    list2priv.put(Field.FIELD_NAME_CREATORS, NamingPrivilege.CREATE);
    list2priv.put(Field.FIELD_NAME_STEM_ADMINS, NamingPrivilege.STEM_ADMIN);
    list2priv.put(Field.FIELD_NAME_STEM_ATTR_READERS, NamingPrivilege.STEM_ATTR_READ);
    list2priv.put(Field.FIELD_NAME_STEM_ATTR_UPDATERS, NamingPrivilege.STEM_ATTR_UPDATE);
    
    populateUsduConfiguredSources();
  }
  
  private static void populateUsduConfiguredSources() {
    
    Pattern usduSourceIdKey = Pattern.compile("^usdu\\.source\\.(\\w+)\\.sourceId$");
    
    SourceManager.getInstance().getSources();
    
    Map<String, String> propertiesMap = GrouperConfig.retrieveConfig().propertiesMap(usduSourceIdKey);
    
    for (Entry<String, String> entry: propertiesMap.entrySet()) {
          
      String property = entry.getKey();
      String sourceId = entry.getValue();
      
      try {
        SourceManager.getInstance().getSource(sourceId);
      } catch (SourceUnavailableException e) {
        throw new RuntimeException("source id: "+sourceId+" not found in configured subject sources. ");
      }
      
      Matcher matcher = usduSourceIdKey.matcher(property);
      
      if (matcher.matches()) {
        
        String label = matcher.group(1);
        
        int maxUnresolvableSubjects = GrouperConfig.retrieveConfig().propertyValueInt("usdu.source."+label+".failsafe.maxUnresolvableSubjects", 500);
        
        boolean removeUpToFailSafe = GrouperConfig.retrieveConfig().propertyValueBoolean("usdu.source."+label+".failsafe.removeUpToFailsafe", false);
        
        int deleteAfterDays = GrouperConfig.retrieveConfig().propertyValueInt("usdu.source."+label+".delete.ifAfterDays", 30);
        
        UsduSource source = new UsduSource();
        source.setSourceId(sourceId);
        source.setSourceLabel(label);
        source.setMaxUnresolvableSubjects(maxUnresolvableSubjects);
        source.setDeleteAfterDays(deleteAfterDays);
        source.setRemoveUpToFailsafe(removeUpToFailSafe);
        
        usduConfiguredSources.put(sourceId, source);
      }
      
    }
    
  }
  
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    if (!UsduSettings.usduEnabled()) {
      LOG.info("usdu.enable is set to false. not going to run usdu daemon.");
      return null;
    }
    
    LOG.info("Going to mark members as deleted.");
    long deletedMembers = deleteUnresolvableMembers(grouperSession);
    otherJobInput.getHib3GrouperLoaderLog().store();
    
    long nowResolvedMembers = clearMetadataFromNowResolvedMembers(grouperSession);
    otherJobInput.getHib3GrouperLoaderLog().store();
    
    otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Marked " + deletedMembers + " members deleted. Cleared subject resolution attributes from "+nowResolvedMembers +" members");
    
    LOG.info("UsduJob finished successfully.");
    
    return null;
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    runDaemonStandalone();
  }
  
  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    String jobName = "OTHER_JOB_usduDaemon";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new UsduJob().run(otherJobInput);
  }
  
  
  /**
   * clear attributes from members who have become resolvable again.
   * @param grouperSession
   * @return
   */
  private long clearMetadataFromNowResolvedMembers(GrouperSession grouperSession) {
   
    Set<Member> members = new MemberFinder()
      .assignAttributeCheckReadOnAttributeDef(false)
      .assignNameOfAttributeDefName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_RESOLVABLE)
      .addAttributeValuesOnAssignment("false")
      .findMembers();
    
    long resolvableMembers = 0; 
    
    for (Member member: members) {
      if (USDU.isMemberResolvable(grouperSession, member)) {
        UsduService.deleteAttributeAssign(member);
        resolvableMembers++;
      }
    }
    
    return resolvableMembers;
    
  }
  
  
  /**
   * delete unresolvable members
   * @param grouperSession
   * @return number of members marked as deleted
   */
  private long deleteUnresolvableMembers(GrouperSession grouperSession) {
    
    Set<Member> unresolvableMembers = USDU.getUnresolvableMembers(grouperSession, null);
    
    // map to store source id to set of members to be deleted
    Map<String, Set<Member>> sourceIdToMembers = new HashMap<String, Set<Member>>();
    
    // store members for which sources have not been configured
    Set<Member> membersWithoutExplicitSourceConfiguration = new HashSet<Member>();
    
    populateUnresolvableMembersConfig(unresolvableMembers, sourceIdToMembers, membersWithoutExplicitSourceConfiguration);
    
    return deleteUnresolvableMembers(sourceIdToMembers, membersWithoutExplicitSourceConfiguration);
    
  }
  
  /**
   * delete unresolvable members
   * @param unresolvables
   */
  private void populateUnresolvableMembersConfig(Set<Member> unresolvables, Map<String, Set<Member>> sourceIdToMembers, 
      Set<Member> membersWithoutExplicitSourceConfiguration) {
    
    Set<Field> fields = getMemberFields();
    Set<Member> unresolvablesWithMemberships = new HashSet<Member>();
    
    for (Member member : unresolvables) {
      
      Set<Membership> memberships = getAllImmediateMemberships(member, fields);
      
      if (memberships.isEmpty()) {
        LOG.info("member_uuid='" + member.getUuid() + "' subject=" + member + " no_memberships");
        continue;
      }
      unresolvablesWithMemberships.add(member);
    }
    
    if (unresolvablesWithMemberships.size() == 0) {
      return;
    }

    AttributeAssignValueFinder attributeAssignValueFinder = new AttributeAssignValueFinder();
    
    for (Member member : unresolvablesWithMemberships) {
      
      attributeAssignValueFinder.addOwnerMemberIdOfAssignAssign(member.getId());
    }
    
    attributeAssignValueFinder.addAttributeDefNameId(UsduAttributeNames.retrieveAttributeDefNameBase().getId());
    AttributeAssignValueFinderResult attributeAssignValueFinderResult = attributeAssignValueFinder.findAttributeAssignValuesResult();
    
    for (Member member : unresolvablesWithMemberships) {
      
      SubjectResolutionAttributeValue savedSubjectResolutionAttributeValue = saveSubjectResolutionAttributeValue(member, attributeAssignValueFinderResult);
      
      addUnresolvedMemberToCorrectSet(member, savedSubjectResolutionAttributeValue, sourceIdToMembers, membersWithoutExplicitSourceConfiguration);
      
    }
    
  }
  
  private long deleteUnresolvableMembers(Map<String, Set<Member>> sourceIdToMembers, Set<Member> membersWithoutExplicitSourceConfiguration) {
    
    int globalMaxAllowed = GrouperConfig.retrieveConfig().propertyValueInt("usdu.failsafe.maxUnresolvableSubjects", 500);
    
    boolean globalRemoveUpToFailSafe = GrouperConfig.retrieveConfig().propertyValueBoolean("usdu.failsafe.removeUpToFailsafe", false);
    
    long deletedCount = 0;
    
    // now we need to decide if we need to delete unresolvable members and how many
    for (String sourceId: sourceIdToMembers.keySet()) {
      
      UsduSource source = usduConfiguredSources.get(sourceId);
      
      Set<Member> unresolvableMembersForASource = sourceIdToMembers.get(sourceId);
      int maxUnresolvableSubjectsAllowed = source.getMaxUnresolvableSubjects();
      boolean removeUpToFailsafe = source.isRemoveUpToFailsafe();
      
      if (unresolvableMembersForASource.size() > maxUnresolvableSubjectsAllowed) {
        
        if (!removeUpToFailsafe) {
          LOG.info("For source id "+sourceId+" found "+unresolvableMembersForASource.size()+"unresolvable members. max limit is "+maxUnresolvableSubjectsAllowed+". "
              + "removeUpToFailsafe is set to false hence not going to delete any members.");
        } else {
          LOG.info("For source id "+sourceId+" found "+unresolvableMembersForASource.size()+"unresolvable members. max limit is "+maxUnresolvableSubjectsAllowed+". "
              + "removeUpToFailsafe is set to true hence going to delete "+maxUnresolvableSubjectsAllowed+" members.");
          
          deletedCount += deleteUnresolvableMembers(unresolvableMembersForASource, maxUnresolvableSubjectsAllowed);
          
        }
        
      } else {
        deletedCount += deleteUnresolvableMembers(unresolvableMembersForASource, unresolvableMembersForASource.size());
      }
        
    }
    
    // let's take care of the sources that have not been configured explicitly
    if (membersWithoutExplicitSourceConfiguration.size() > globalMaxAllowed) {
      
      if (!globalRemoveUpToFailSafe) {
        LOG.info("For global (not explicitly defined sources) found "+membersWithoutExplicitSourceConfiguration.size()+"unresolvable members. max limit is "+globalMaxAllowed+". "
            + "usdu.failsafe.removeUpToFailsafe is set to false hence not going to delete any members.");
      } else {
        LOG.info("For global (not explicitly defined sources) found "+membersWithoutExplicitSourceConfiguration.size()+"unresolvable members. max limit is "+globalMaxAllowed+". "
            + "usdu.failsafe.removeUpToFailsafe is set to true hence going to delete "+globalMaxAllowed+" members.");
        
        deletedCount += deleteUnresolvableMembers(membersWithoutExplicitSourceConfiguration, globalMaxAllowed);
        
      }
      
    } else {
      deletedCount += deleteUnresolvableMembers(membersWithoutExplicitSourceConfiguration, membersWithoutExplicitSourceConfiguration.size());
    }
    
    return deletedCount;
    
  }
  
  private void addUnresolvedMemberToCorrectSet(Member member, SubjectResolutionAttributeValue memberSubjectResolutionAttributeValue,
      Map<String, Set<Member>> sourceIdToMembers, Set<Member> membersWithoutExplicitSourceConfiguration) {
    
    int globalDeleteAfterDays = GrouperConfig.retrieveConfig().propertyValueInt("usdu.delete.ifAfterDays", 30);
    
    if (usduConfiguredSources.containsKey(member.getSubjectSourceId())) { // this source has been configured explicitly
            
      if ( memberSubjectResolutionAttributeValue.getSubjectResolutionDaysUnresolved() > usduConfiguredSources.get(member.getSubjectSourceId()).getDeleteAfterDays()) {
        Set<Member> membersPerSource = sourceIdToMembers.get(member.getSubjectSourceId());
        if (membersPerSource == null) {
          membersPerSource = new HashSet<Member>();
        }
        
        membersPerSource.add(member);
        sourceIdToMembers.put(member.getSubjectSourceId(), membersPerSource);
      }
      
    } else if (memberSubjectResolutionAttributeValue.getSubjectResolutionDaysUnresolved() > globalDeleteAfterDays) {
      membersWithoutExplicitSourceConfiguration.add(member);
    }
    
  }
  
  private SubjectResolutionAttributeValue saveSubjectResolutionAttributeValue(Member member, AttributeAssignValueFinderResult attributeAssignValueFinderResult) {
    
    SubjectResolutionAttributeValue existingSubjectResolutionAttributeValue = UsduService.getSubjectResolutionAttributeValue(member, attributeAssignValueFinderResult);
    
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    Date currentDate = new Date();
    String curentDateString = dateFormat.format(currentDate);
    
    SubjectResolutionAttributeValue newValue = new SubjectResolutionAttributeValue();
    
    AuditEntry auditEntry = null;
    
    if (existingSubjectResolutionAttributeValue == null) { //this member has become unresolvable for the first time only
      
      newValue.setSubjectResolutionResolvableString(BooleanUtils.toStringTrueFalse(false));
      newValue.setSubjectResolutionDateLastResolvedString(curentDateString);
      newValue.setSubjectResolutionDaysUnresolvedString(String.valueOf(0L));
      newValue.setSubjectResolutionDateLastCheckedString(curentDateString);
      newValue.setMember(member);
      
      auditEntry = new AuditEntry(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_MEMBER_ADD);
      
      auditEntry.setDescription("Subject with id: " + member.getSubjectId() + " is being marked as unresolvable on "+currentDate);
      
    } else {
      
      String dateLastResolvedString = existingSubjectResolutionAttributeValue.getSubjectResolutionDateLastResolvedString();
      Date dateLastResolved = null;
      try {
        dateLastResolved = dateFormat.parse(dateLastResolvedString);
      } catch (ParseException e) {
        throw new RuntimeException(dateLastResolvedString+" is not a valid yyyy/MM/dd format");
      }
      
      long diff = currentDate.getTime() - dateLastResolved.getTime();
      
      long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
      
      newValue.setSubjectResolutionDateLastCheckedString(curentDateString);
      newValue.setSubjectResolutionDaysUnresolvedString(String.valueOf(days));
      newValue.setMember(member);
      
      auditEntry = new AuditEntry(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_MEMBER_UPDATE);
      
      auditEntry.setDescription("Subject with id: " + member.getSubjectId() + "; updating subject resolution attributes on "+currentDate);
      
    }
    
    UsduService.markMemberAsUnresolved(newValue, member);
    
    auditEntry.assignStringValue(auditEntry.getAuditType(), "ownerMemberId", member.getUuid());
    auditEntry.assignStringValue(auditEntry.getAuditType(), "ownerSourceId", member.getSubjectSourceId());
    auditEntry.assignStringValue(auditEntry.getAuditType(), "ownerSubjectId", member.getSubjectId());
    
    final AuditEntry AUDIT_ENTRY = auditEntry;
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
        AUDIT_ENTRY.saveOrUpdate(true);
        return null;
      }
    });
    
    return newValue;
    
  }
  
  public static long deleteUnresolvableMembers(Set<Member> unresolvableMembers, int howMany) {
    
    long deletedCount = 0;
    
    for (final Member member: unresolvableMembers) {
      
      if (deletedCount >= howMany) {
        LOG.info("Total: "+unresolvableMembers.size()+" unresolvable members, deleted: "+deletedCount);
        break;
      }
      
      Set<Field> fields = getMemberFields();
      
      Set<Membership> memberships = getAllImmediateMemberships(member, fields);
      
      for (final Membership membership : memberships) {
    
        LOG.info("member_uuid='" + member.getUuid() + "' subject=" + member);
        if (membership.getList().getType().equals(FieldType.LIST)
            || membership.getList().getType().equals(FieldType.ACCESS)) {
          LOG.info(" group='" + membership.getOwnerGroup().getName());
        }
        if (membership.getList().getType().equals(FieldType.NAMING)) {
          LOG.info(" stem='" + membership.getOwnerStem().getName());
        }
        LOG.info(" list='" + membership.getList().getName() + "'");
        
        HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
          public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
            
            if (membership.getList().getType().equals(FieldType.LIST)) {
              USDU.deleteUnresolvableMember(membership.getMember(), membership.getOwnerGroup(), membership.getList());
            }
            
            if (membership.getList().getType().equals(FieldType.ACCESS)) {
              USDU.deleteUnresolvableMember(membership.getMember(), membership.getOwnerGroup(), getPrivilege(membership.getList()));
            }
            
            if (membership.getList().getType().equals(FieldType.NAMING)) {
              USDU.deleteUnresolvableMember(membership.getMember(), membership.getOwnerStem(), getPrivilege(membership.getList()));
            }
            
            return null;
          }
        });
                
      }
      
      UsduService.markMemberAsDeleted(member);
      
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
        public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
          
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.USDU_MEMBER_DELETE);
          auditEntry.assignStringValue(auditEntry.getAuditType(), "memberId", member.getUuid());
          auditEntry.assignStringValue(auditEntry.getAuditType(), "sourceId", member.getSubjectSourceId());
          auditEntry.assignStringValue(auditEntry.getAuditType(), "subjectId", member.getSubjectId());
          auditEntry.setDescription("Deleted source id: " + member.getSubjectSourceId() + ", subject id: "+member.getSubjectId()+", name: "+member.getName()+", description: " + member.getDescription());
          
          auditEntry.saveOrUpdate(true);
          
          return null;
        }
      });
            
      deletedCount++;
      
    }
    
    return deletedCount;
    
  }
  
  /**
   * Get fields of which a subject might be a member. Includes all fields of
   * type FieldType.LIST, FieldType.ACCESS, and FieldType.NAMING.
   * 
   * @return set of fields
   * @throws SchemaException
   */
  protected static Set<Field> getMemberFields() throws SchemaException {

    Set<Field> listFields = new LinkedHashSet<Field>();
    for (Object field : FieldFinder.findAllByType(FieldType.LIST)) {
      listFields.add((Field) field);
    }
    for (Object field : FieldFinder.findAllByType(FieldType.ACCESS)) {
      listFields.add((Field) field);
    }
    for (Object field : FieldFinder.findAllByType(FieldType.NAMING)) {
      listFields.add((Field) field);
    }
    return listFields;
  }
  
  /**
   * Get memberships for a member for the given fields.
   * 
   * @param member
   * @param fields
   *          a set of 'list' fields
   * @return a set of memberships
   * @throws SchemaException
   */
  protected static Set<Membership> getAllImmediateMemberships(Member member, Set<Field> fields) throws SchemaException {

    Set<Membership> memberships = new LinkedHashSet<Membership>();
    for (Field field : fields) {
      
      Set<Object[]> rows = new MembershipFinder()
        .addMemberId(member.getId()).addField(field).assignEnabled(null).assignMembershipType(MembershipType.IMMEDIATE)
        .findMembershipsMembers();
      for (Object[] row : GrouperUtil.nonNull(rows)) {
        memberships.add((Membership) row[0]);
      }
    }
    return memberships;
  }
  
  /**
   * Map fields to privileges.
   * 
   * @param field
   * @return the privilege matching the given field or null
   */
  protected static Privilege getPrivilege(Field field) {

    return list2priv.get(field.getName());
  }

}
