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
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
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
    
    deleteUnresolvableMembers(grouperSession);
    
    //TODO go through all the members that are tagged as unresolvables and 
    // if they are now resolvable, clear the attributes
    
    
    
    return null;
  }
  
  private static void clearUnresolvableMetadataOnNowResolvedMembers() {
    
    String attributeDefId = UsduAttributeNames.retrieveAttributeDefNameBase().getId();
    
    
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
  
  
  @SuppressWarnings("unused")
  private void clearMetadataFromNowResolvedMembers() {
    //TODO implement after figuring out how to get members that have subjectResolution attributes assigned.
    
  }
  
  
  private void deleteUnresolvableMembers(GrouperSession grouperSession) {
    
    Set<Member> unresolvableMembers = USDU.getUnresolvableMembers(grouperSession, null);
    
    deleteUnresolvableMembers(unresolvableMembers);
    
  }
  
  private void deleteUnresolvableMembers(Set<Member> unresolvables) {
    
    Set<Field> fields = getMemberFields();
    
    // map to store source id to set of members to be deleted
    Map<String, Set<Member>> sourceIdToMembers = new HashMap<String, Set<Member>>();
    
    Set<Member> membersWithoutExplicitSourceConfiguration = new HashSet<Member>();
    
    for (Member member : unresolvables) {
      
      Set<Membership> memberships = getAllImmediateMemberships(member, fields);
      
      if (memberships.isEmpty()) {
        LOG.info("member_uuid='" + member.getUuid() + "' subject=" + member + " no_memberships");
        continue;
      }
      
      SubjectResolutionAttributeValue savedSubjectResolutionAttributeValue = saveSubjectResolutionAttributeValue(member);
      
      addUnresolvedMemberToCorrectSet(member, savedSubjectResolutionAttributeValue, sourceIdToMembers, membersWithoutExplicitSourceConfiguration);
      
      deleteUnresolvableMembers(sourceIdToMembers, membersWithoutExplicitSourceConfiguration);
      
    }
    
  }
  
  private static void deleteUnresolvableMembers(Map<String, Set<Member>> sourceIdToMembers, Set<Member> membersWithoutExplicitSourceConfiguration) {
    
    int globalMaxAllowed = GrouperConfig.retrieveConfig().propertyValueInt("usdu.failsafe.maxUnresolvableSubjects", 500);
    
    boolean globalRemoveUpToFailSafe = GrouperConfig.retrieveConfig().propertyValueBoolean("usdu.failsafe.removeUpToFailsafe", false);
    
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
          
          deleteUnresolvableMembers(unresolvableMembersForASource, maxUnresolvableSubjectsAllowed);
          
        }
        
      } else {
        deleteUnresolvableMembers(unresolvableMembersForASource, unresolvableMembersForASource.size());
      }
        
    }
    
    // let's take care of the sources that have not been configured explicitly
    if (membersWithoutExplicitSourceConfiguration.size() > globalMaxAllowed) {
      
      if (!globalRemoveUpToFailSafe) {
        LOG.info("For global (not explicitly defined sources) found "+membersWithoutExplicitSourceConfiguration.size()+"unresolvable members. max limit is "+globalMaxAllowed+". "
            + "usdu.failsafe.removeUpToFailsafe is set to false hence not going to delete any members.");
      } else {
        LOG.info("For global (not explicitly defined sources) found "+membersWithoutExplicitSourceConfiguration.size()+"unresolvable members. max limit is "+globalMaxAllowed+". "
            + "usdu.failsafe.removeUpToFailsafe is set to true hence not going to delete "+globalMaxAllowed+" members.");
        
        deleteUnresolvableMembers(membersWithoutExplicitSourceConfiguration, globalMaxAllowed);
        
      }
      
    } else {
      deleteUnresolvableMembers(membersWithoutExplicitSourceConfiguration, membersWithoutExplicitSourceConfiguration.size());
    }
    
  }
  
  private static void addUnresolvedMemberToCorrectSet(Member member, SubjectResolutionAttributeValue memberSubjectResolutionAttributeValue,
      Map<String, Set<Member>> sourceIdToMembers, Set<Member> membersWithoutExplicitSourceConfiguration) {
    
    int globalDeleteAfterDays = GrouperConfig.retrieveConfig().propertyValueInt("usdu.delete.ifAfterDays", 30);
    
    if (usduConfiguredSources.containsKey(member.getSubjectSourceId())) { // this source has been configured explicitly
      
      if (memberSubjectResolutionAttributeValue.getSubjectResolutionDaysUnresolved() > usduConfiguredSources.get(member.getSubjectSourceId()).getDeleteAfterDays()) {
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
  
  private static SubjectResolutionAttributeValue saveSubjectResolutionAttributeValue(Member member) {
    
    SubjectResolutionAttributeValue existingSubjectResolutionAttributeValue = UsduService.getSubjectResolutionAttributeValue(member);
    
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    Date currentDate = new Date();
    String curentDateString = dateFormat.format(currentDate);
    
    SubjectResolutionAttributeValue newValue = new SubjectResolutionAttributeValue();
    
    if (existingSubjectResolutionAttributeValue == null) { //this member has become unresolvable for the first time only
      
      newValue.setSubjectResolutionResolvableString(BooleanUtils.toStringTrueFalse(false));
      newValue.setSubjectResolutionDateLastResolvedString(curentDateString);
      newValue.setSubjectResolutionDaysUnresolvedString(String.valueOf(0));
      newValue.setSubjectResolutionLastCheckedString(curentDateString);
      
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
      
      newValue.setSubjectResolutionLastCheckedString(curentDateString);
      newValue.setSubjectResolutionDaysUnresolvedString(String.valueOf(days));
      
    }
    
    UsduService.saveOrUpldateSubjectResolutionAttributeValue(newValue, member);
    return newValue;
    
  }
  
  private static void deleteUnresolvableMembers(Set<Member> unresolvableMembers, int howMany) {
    
    int deletedCount = 0;
    
    for (Member member: unresolvableMembers) {
      
      if (deletedCount >= howMany) {
        break;
      }
      
      Set<Field> fields = getMemberFields();
      
      Set<Membership> memberships = getAllImmediateMemberships(member, fields);
      
      boolean deleted = false;
      
      for (Membership membership : memberships) {
    
        LOG.info("member_uuid='" + member.getUuid() + "' subject=" + member);
        if (membership.getList().getType().equals(FieldType.LIST)
            || membership.getList().getType().equals(FieldType.ACCESS)) {
          LOG.info(" group='" + membership.getOwnerGroup().getName());
        }
        if (membership.getList().getType().equals(FieldType.NAMING)) {
          LOG.info(" stem='" + membership.getOwnerStem().getName());
        }
        LOG.info(" list='" + membership.getList().getName() + "'");
        
        if (membership.getList().getType().equals(FieldType.LIST)) {
          USDU.deleteUnresolvableMember(membership.getMember(), membership.getOwnerGroup(), membership.getList());
          deleted = true;
        }
        if (membership.getList().getType().equals(FieldType.ACCESS)) {
          USDU.deleteUnresolvableMember(membership.getMember(), membership.getOwnerGroup(), getPrivilege(membership
              .getList()));
          deleted = true;
        }
        if (membership.getList().getType().equals(FieldType.NAMING)) {
          USDU.deleteUnresolvableMember(membership.getMember(), membership.getOwnerStem(), getPrivilege(membership
              .getList()));
          deleted = true;
        }
        
      }
      
      if (deleted) {
        deletedCount++;
      }
      
    }
    
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
