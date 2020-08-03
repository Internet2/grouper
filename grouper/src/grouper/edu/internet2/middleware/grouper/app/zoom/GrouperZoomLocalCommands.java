/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.zoom;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.UnresolvableSubject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;


/**
 *
 */
public class GrouperZoomLocalCommands {

  /**
   * configId of zoom external system to cache of sourceId, subjectId, to email
   */
  private static Map<String, ExpirableCache<MultiKey, String>> subjectToEmailCache = new HashMap<String, ExpirableCache<MultiKey, String>>();

  /**
   * 
   * @param configId (of zoom external system)
   * @return the cache
   */
  private static ExpirableCache<MultiKey, String> subjectToEmailCache(String configId) {

    ExpirableCache<MultiKey, String> subjectToEmailCacheForConfigId = subjectToEmailCache.get(configId);
    
    if (subjectToEmailCacheForConfigId == null) {
      
      int cacheForMinutes = GrouperLoaderConfig.retrieveConfig().propertyValueInt("zoom." + configId + ".subjectCacheMinutes", 8*60);

      subjectToEmailCacheForConfigId = new ExpirableCache<MultiKey, String>(cacheForMinutes);
      
      subjectToEmailCache.put(configId, subjectToEmailCacheForConfigId);
      
    }
    
    return subjectToEmailCacheForConfigId;
    
  }

  /**
   * configId cache from email to subject source id and subject id
   */
  private static Map<String, ExpirableCache<String, MultiKey>> emailToSubjectCache = new HashMap<String, ExpirableCache<String, MultiKey>>();

  /**
   * 
   * @param configId (of zoom external system)
   * @return the cache
   */
  private static ExpirableCache<String, MultiKey> emailToSubjectCache(String configId) {

    ExpirableCache<String, MultiKey> emailToSubjectCacheForConfigId = emailToSubjectCache.get(configId);
    
    if (emailToSubjectCacheForConfigId == null) {

      int cacheForMinutes = GrouperLoaderConfig.retrieveConfig().propertyValueInt("zoom." + configId + ".subjectCacheMinutes", 8*60);

      emailToSubjectCacheForConfigId = new ExpirableCache<String, MultiKey>(cacheForMinutes);
      
      emailToSubjectCache.put(configId, emailToSubjectCacheForConfigId);
      
    }
    
    return emailToSubjectCacheForConfigId;
    
  }


  /**
   * 
   */
  public GrouperZoomLocalCommands() {
  }
  
  /**
   * 
   * @param configId 
   * @return the folder name
   */
  public static String folderNameToProvision(String configId) {
    return GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("zoom." + configId + ".folderToProvision");
  }
  
  /**
   * 
   * @param configId 
   * @return the group name
   */
  public static String groupNameToDeleteUsers(String configId) {
    return GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("zoom." + configId + ".groupNameToDeleteUsers");
  }
  
  /**
   * 
   * @param configId 
   * @return if delete
   */
  public static boolean deleteInTargetIfDeletedInGrouper(String configId) {
    return GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("zoom." + configId + ".deleteInTargetIfDeletedInGrouper", true);
  }
  
  /**
   * sources for subjects
   * @param configId 
   * @return the config sources for subjects
   */
  public static Set<String> configSourcesForSubjects(String configId) {
  
    //# put the comma separated list of sources to send to duo
    //grouperDuo.sourcesForSubjects = someSource
    String sourcesForSubjectsString = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("zoom." + configId + ".sourcesForSubjects");
    
    return GrouperClientUtils.splitTrimToSet(sourcesForSubjectsString, ",");

  }

  /**
   * user ids to ignore if they are in a group (e.g. admin accounts)
   * @param configId 
   * @return user ids to ignore
   */
  public static Set<String> configIgnoreUserIds(String configId) {

    //  # ignore user ids in zoom (dont remove them) e.g. admin ids
    //  # {valueType: "string", multiple: true}
    String ignoreUserIds = GrouperLoaderConfig.retrieveConfig().propertyValueString("zoom." + configId + ".ignoreUserIds");

    return GrouperUtil.nonNull(GrouperClientUtils.splitTrimToSet(ignoreUserIds, ","));

  }
  
  /**
   * 
   * @param configId 
   * @return the folder name
   */
  public static String subjectAttributeForZoomEmail(String configId) {
    return GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("zoom." + configId + ".subjectAttributeForZoomEmail");
  }

  /**
   * 
   * @param configId
   * @return the stem that has groups
   */
  public static Stem folderToProvision(String configId) {
    
    final String folderNameToProvision = folderNameToProvision(configId);
    
    Stem result = (Stem)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Stem parent = StemFinder.findByName(grouperSession, folderNameToProvision, true);
        return parent;
      }
    });
    
    return result;
  }
  
  /**
   * 
   * @param configId
   * @return the group extensions to provision
   */
  public static Set<String> groupExtensionsToProvision(final String configId) {
    
    Set<String> result = (Set<String>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        Stem parent = folderToProvision(configId);

        Set<Group> groups = new GroupFinder().assignParentStemId(parent.getId()).assignStemScope(Scope.ONE).findGroups();
        
        Set<String> extensions = new TreeSet<String>();
        
        for (Group group : GrouperUtil.nonNull(groups)) {
          extensions.add(group.getExtension());
        }
        
        return extensions;
      }
    });
    return result;
  }

  /**
   * get all memberships to provision by email
   * @param configId
   * @return the map of group extension to set of emails
   */
  public static Map<String, Set<String>> groupsEmailsToProvision(String configId) {

    String folderNameToProvision = folderNameToProvision(configId);
    return groupsEmailsFromFolder(configId, folderNameToProvision);
  }

  /**
   * get all memberships to provision by email
   * @param configId
   * @param folderName
   * @return the map of group extension to set of emails
   */
  public static Map<String, Set<String>> groupsEmailsFromFolder(String configId, String folderName) {
    
    Map<String, Set<String>> result = new HashMap<String, Set<String>>();
    
    Stem stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), folderName, false);
    
    if (stem == null) {
      return result;
    }
    
    Map<String, Set<MultiKey>> groupsSourceIdsSubjectIdsToProvision = groupsSourceIdsSubjectIdsToProvision(configId, stem.getId());
    
    Map<String, Set<String>> sourceIdToSubjectIds = convertGroupExtensionSourceIdSubjectIdToSourceIdToSubjectIds(configId,
        groupsSourceIdsSubjectIdsToProvision);

    Map<MultiKey, String> sourceIdSubjectIdToEmail = convertSourceIdSubjectIdToEmail(configId, sourceIdToSubjectIds);

    for (String groupExtension : groupsSourceIdsSubjectIdsToProvision.keySet()) {
      Set<MultiKey> sourceIdsSubjectIds = groupsSourceIdsSubjectIdsToProvision.get(groupExtension);
      Set<String> emails = new HashSet<String>();
      result.put(groupExtension, emails);
      for (MultiKey sourceIdSubjectId : sourceIdsSubjectIds) {
        String email = sourceIdSubjectIdToEmail.get(sourceIdSubjectId);
        emails.add(email);
      }
    }

    return result;
  }

  /**
   * get all memberships from group by email
   * @param configId
   * @param groupName
   * @return the set of emails
   */
  public static Set<String> groupEmailsFromGroup(String configId, String groupName) {
    
    Set<String> result = new HashSet<String>();
    
    Set<MultiKey> sourceIdsSubjectIds = groupSourceIdsSubjectIds(configId, groupName);
    
    Map<String, Set<String>> sourceIdToSubjectIds = convertSourceIdSubjectIdToSourceIdToSubjectIds(configId,
        sourceIdsSubjectIds);

    Map<MultiKey, String> sourceIdSubjectIdToEmail = convertSourceIdSubjectIdToEmail(configId, sourceIdToSubjectIds);

    for (MultiKey sourceIdSubjectId : sourceIdsSubjectIds) {
      String email = sourceIdSubjectIdToEmail.get(sourceIdSubjectId);
      result.add(email);
    }

    return result;
  }


  /**
   * @param configId
   * @param groupsSourceIdsSubjectIdsToProvision
   * @return the map
   */
  public static Map<String, Set<String>> convertGroupExtensionSourceIdSubjectIdToSourceIdToSubjectIds(String configId,
      Map<String, Set<MultiKey>> groupsSourceIdsSubjectIdsToProvision) {

    // first lets resolve the subjects
    Map<String, Set<String>> sourceIdToSubjectIds = new HashMap<String, Set<String>>();
    
    for (String groupExtension : groupsSourceIdsSubjectIdsToProvision.keySet()) {
      Set<MultiKey> sourceIdsSubjectIds = groupsSourceIdsSubjectIdsToProvision.get(groupExtension);
      for (MultiKey sourceIdSubjectId : sourceIdsSubjectIds) {
        String sourceId = (String)sourceIdSubjectId.getKey(0);
        String subjectId = (String)sourceIdSubjectId.getKey(1);
        
        Set<String> subjectIds = sourceIdToSubjectIds.get(sourceId);
        if (subjectIds == null) {
          subjectIds = new HashSet<String>();
          sourceIdToSubjectIds.put(sourceId, subjectIds);
        }
        subjectIds.add(subjectId);
      }
    }
    return sourceIdToSubjectIds;
  }

  /**
   * @param configId
   * @param sourceIdsSubjectIds
   * @return the map
   */
  public static Map<String, Set<String>> convertSourceIdSubjectIdToSourceIdToSubjectIds(String configId,
      Set<MultiKey> sourceIdsSubjectIds) {

    // first lets resolve the subjects
    Map<String, Set<String>> sourceIdToSubjectIds = new HashMap<String, Set<String>>();
    
    for (MultiKey sourceIdSubjectId : sourceIdsSubjectIds) {
      String sourceId = (String)sourceIdSubjectId.getKey(0);
      String subjectId = (String)sourceIdSubjectId.getKey(1);
      
      Set<String> subjectIds = sourceIdToSubjectIds.get(sourceId);
      if (subjectIds == null) {
        subjectIds = new HashSet<String>();
        sourceIdToSubjectIds.put(sourceId, subjectIds);
      }
      subjectIds.add(subjectId);
    }
    return sourceIdToSubjectIds;
  }

  /**
   * input sourceId to subjectIds, return sourceId/subjectId multikey to email
   * @param configId
   * @param sourceIdToSubjectIdsInput
   * @return map of sourceId/subjectId to email
   */
  public static Map<MultiKey, String> convertSourceIdSubjectIdToEmail(final String configId,
      final Map<String, Set<String>> sourceIdToSubjectIdsInput) {
    
    Map<MultiKey, String> result = new HashMap<MultiKey, String>();
    
    if (GrouperUtil.length(sourceIdToSubjectIdsInput) == 0) {
      return result;
    }
    
    final Map<String, Set<String>> sourceIdToSubjectIdsNotInCache = new HashMap<String, Set<String>>();

    final ExpirableCache<MultiKey, String> thisSubjectToEmailCache = subjectToEmailCache(configId);
    final ExpirableCache<String, MultiKey> thisEmailToSubjectCache = emailToSubjectCache(configId);

    for (String sourceId : sourceIdToSubjectIdsInput.keySet()) {
      Set<String> subjectIds = sourceIdToSubjectIdsInput.get(sourceId);
      for (String subjectId : GrouperUtil.nonNull(subjectIds)) {
        final MultiKey sourceIdSubjectId = new MultiKey(sourceId, subjectId);
        String email = thisSubjectToEmailCache.get(sourceIdSubjectId);
        if (!StringUtils.isBlank(email)) {
          result.put(sourceIdSubjectId, email);
        } else {
          Set<String> subjectIdsNotInCache = sourceIdToSubjectIdsNotInCache.get(sourceId);
          if (subjectIdsNotInCache == null) {
            subjectIdsNotInCache = new HashSet<String>();
            sourceIdToSubjectIdsNotInCache.put(sourceId, subjectIdsNotInCache);
          }
          subjectIdsNotInCache.add(subjectId);
        }
      }
    }

    if (sourceIdToSubjectIdsNotInCache.size() > 0) {
      Map<MultiKey, String> sourceIdSubjectIdToEmailFromSubjectApi =  (Map<MultiKey, String>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Map<MultiKey, String> sourceIdSubjectIdToEmail = new HashMap<MultiKey, String>();
          String subjectAttributeForZoomEmail = subjectAttributeForZoomEmail(configId);
  
          for (String sourceId : sourceIdToSubjectIdsNotInCache.keySet()) {  
            Set<String> subjectIds = sourceIdToSubjectIdsNotInCache.get(sourceId);
            Map<String, Subject> subjectIdToSubject = SubjectFinder.findByIds(subjectIds, sourceId);
            for (Subject subject : subjectIdToSubject.values()) {
              
              String email = subject.getAttributeValue(subjectAttributeForZoomEmail);
              
              // ignore if no attribute
              if (StringUtils.isBlank(email)) {
                continue;
              }
              
              MultiKey multiKey = new MultiKey(subject.getSourceId(), subject.getId());
              sourceIdSubjectIdToEmail.put(multiKey, email);
              
              thisEmailToSubjectCache.put(email, multiKey);
              thisSubjectToEmailCache.put(multiKey, email);
              
            }
          }
          return sourceIdSubjectIdToEmail;
        }
      });
      result.putAll(sourceIdSubjectIdToEmailFromSubjectApi);
    }
    
    return result;
  }

  /**
   * 
   * @param folderName
   * @return the group extensions
   */
  public static Set<String> groupExtensionsInFolder(String folderName) {
    
    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignName(folderName).save();
    
    Set<String> extensions = new HashSet<String>();
    
    for (Group group : GrouperUtil.nonNull(new GroupFinder().assignParentStemId(stem.getId()).assignStemScope(Scope.ONE).findGroups())) {
      extensions.add(group.getExtension());
    }
    
    return extensions;
  }
  
  /**
   * input sourceId to subjectIds, return sourceId/subjectId multikey to email
   * @param configId
   * @param emailsInput
   * @return map of email to sourceId/subjectId 
   */
  public static Map<String, MultiKey> convertEmailToSourceIdSubjectId(final String configId,
      final Collection<String> emailsInput) {
    
    Map<String, MultiKey> result = new HashMap<String, MultiKey>();
    
    if (GrouperUtil.length(emailsInput) == 0) {
      return result;
    }
    
    final Set<String> emailsNotInCache = new HashSet<String>();
    
    final ExpirableCache<MultiKey, String> thisSubjectToEmailCache = subjectToEmailCache(configId);
    final ExpirableCache<String, MultiKey> thisEmailToSubjectCache = emailToSubjectCache(configId);

    for (String email : emailsInput) {
      
      MultiKey sourceIdSubjectId = thisEmailToSubjectCache.get(email);
      
      if (sourceIdSubjectId != null) {
        result.put(email, sourceIdSubjectId);
      } else {
        emailsNotInCache.add(email);
      }
    }

    if (emailsNotInCache.size() > 0) {
      Map<String, MultiKey> emailToSourceIdSubjectIdFromSubjectApi =  (Map<String, MultiKey>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

          Map<String, MultiKey> emailToSourceIdSubjectId = new HashMap<String, MultiKey>();
          
          for (String sourceId : configSourcesForSubjects(configId)) {

            Map<String, Subject> emailToSubject = SubjectFinder.findByIdentifiers(emailsNotInCache, sourceId);
            
            for (String email : emailToSubject.keySet()) {
              
              Subject subject = emailToSubject.get(email);
              MultiKey sourceIdSubjectId = new MultiKey(subject.getSourceId(), subject.getId());
              emailToSourceIdSubjectId.put(email, sourceIdSubjectId);
              
              emailsNotInCache.remove(email);
              
              thisEmailToSubjectCache.put(email, sourceIdSubjectId);
              thisSubjectToEmailCache.put(sourceIdSubjectId, email);

            }
            
          }
          
          return emailToSourceIdSubjectId;
        }
      });
      result.putAll(emailToSourceIdSubjectIdFromSubjectApi);
    }
    
    return result;
  }

  /**
   * get all memberships to provision, filter out ones not in correct source
   * @param configId
   * @param stemId
   * @return the map of group extension to set of multikey with sourceId and subjectId
   */
  public static Map<String, Set<MultiKey>> groupsSourceIdsSubjectIdsToProvision(String configId, String stemId) {
    
    String sql = "select gg.extension, subject_source, subject_id from grouper_memberships_lw_v gmlv, grouper_groups gg "
       + " where gmlv.list_name = 'members' and gmlv.group_id = gg.id and gg.parent_stem = ?";

    List<Object[]> rows = new GcDbAccess().sql(sql).addBindVar(stemId).selectList(Object[].class);
    
    Set<String> sources = configSourcesForSubjects(configId);
    
    Map<String, Set<MultiKey>> result = new HashMap<String, Set<MultiKey>>();
    
    for (Object[] row : GrouperUtil.nonNull(rows)) {
      String extension = (String)row[0];
      String subjectSource = (String)row[1];
      String subjectId = (String)row[2];
      if (!sources.contains(subjectSource)) {
        continue;
      }
      Set<MultiKey> listForGroup = result.get(extension);
      if (listForGroup == null) {
        listForGroup = new HashSet<MultiKey>();
        result.put(extension, listForGroup);
      }
      listForGroup.add(new MultiKey(subjectSource, subjectId));
    }
    return result;
  }
  
  /**
   * get all memberships, filter out ones not in correct source
   * @param configId
   * @param groupName
   * @return the map of group extension to set of multikey with sourceId and subjectId
   */
  public static Set<MultiKey> groupSourceIdsSubjectIds(String configId, String groupName) {

    String sql = "select subject_source, subject_id from grouper_memberships_lw_v gmlv "
       + " where gmlv.list_name = 'members' and gmlv.group_name = ?";

    List<Object[]> rows = new GcDbAccess().sql(sql).addBindVar(groupName).selectList(Object[].class);
    
    Set<String> sources = configSourcesForSubjects(configId);
    
    Set<MultiKey> result = new HashSet<MultiKey>();
    
    for (Object[] row : GrouperUtil.nonNull(rows)) {
      String subjectSource = (String)row[0];
      String subjectId = (String)row[1];
      if (!sources.contains(subjectSource)) {
        continue;
      }
      result.add(new MultiKey(subjectSource, subjectId));
    }
    return result;
  }
  
  /**
   * get all memberships to provision, filter out ones not in correct source
   * @param configId
   * @param groupExtensionParam
   * @param sourceIdParam 
   * @param subjectIdParam 
   * @return true if membership exists
   */
  public static boolean groupSourceIdSubjectIdToProvision(String configId, 
      String groupExtensionParam, String sourceIdParam, String subjectIdParam) {

    Set<String> sources = configSourcesForSubjects(configId);
    
    if (!sources.contains(sourceIdParam)) {
      return false;
    }

    Stem folderToProvision = folderToProvision(configId);
    
    if (StringUtils.isBlank(groupExtensionParam) || groupExtensionParam.contains(":")) {
      throw new RuntimeException("Invalid group extension '" + groupExtensionParam + "'");
    }
    
    String sql = "select gg.extension, subject_source, subject_id from grouper_memberships_lw_v gmlv, grouper_groups gg "
       + " where gmlv.list_name = 'members' and gmlv.group_id = gg.id "
       + " and gmlv.group_name = ? and gmlv.subject_source = ? and gmlv.subject_id = ?";

    List<Object[]> rows = new GcDbAccess().sql(sql)
        .addBindVar(folderToProvision.getName() + ":" + groupExtensionParam)
        .addBindVar(sourceIdParam).addBindVar(subjectIdParam).selectList(Object[].class);

    return GrouperUtil.length(rows) > 0;
  }
  
  /**
   * see if subject should be deleted
   * @param configId
   * @param sourceIdParam 
   * @param subjectIdParam 
   * @return true if membership exists
   */
  public static boolean groupSourceIdSubjectIdToDelete(String configId, 
      String sourceIdParam, String subjectIdParam) {

    Set<String> sources = configSourcesForSubjects(configId);
    
    if (!sources.contains(sourceIdParam)) {
      return false;
    }

    String groupName = groupNameToDeleteUsers(configId);
    
    if (StringUtils.isBlank(groupName) || !groupName.contains(":")) {
      throw new RuntimeException("Invalid group name '" + groupName + "'");
    }
    
    String sql = "select gg.extension, subject_source, subject_id from grouper_memberships_lw_v gmlv, grouper_groups gg "
       + " where gmlv.list_name = 'members' and gmlv.group_id = gg.id "
       + " and gmlv.group_name = ? and gmlv.subject_source = ? and gmlv.subject_id = ?";

    List<Object[]> rows = new GcDbAccess().sql(sql)
        .addBindVar(groupName)
        .addBindVar(sourceIdParam).addBindVar(subjectIdParam).selectList(Object[].class);

    return GrouperUtil.length(rows) > 0;
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {

  }


  /**
   * @param groupSyncFolder
   * @param groupsInGrouperToDelete
   */
  public static void deleteGroupExtensionsInFolder(String groupSyncFolder,
      Set<String> groupsInGrouperToDelete) {
    
    for (String extension : GrouperUtil.nonNull(groupsInGrouperToDelete)) {
      Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupSyncFolder + ":" + extension, false);
      group.delete();
    }
    
  }


  /**
   * @param groupSyncFolder
   * @param groupsInGrouperToAdd
   */
  public static void createGroupExtensionsInFolder(String groupSyncFolder,
      Set<String> groupsInGrouperToAdd) {
  
    for (String extension : GrouperUtil.nonNull(groupsInGrouperToAdd)) {
      new GroupSave(GrouperSession.staticGrouperSession()).assignName(groupSyncFolder + ":" + extension).save();
    }
  }


  /**
   * @param configId
   * @param groupSyncFolder
   * @param grouperGroupExtension
   * @param emailToAddToGrouper
   * @param zoomUserId
   * @return true if added, false if not
   * @throws SubjectNotFoundException
   */
  public static boolean addMembership(String configId, String groupSyncFolder,
      String grouperGroupExtension, String emailToAddToGrouper, String zoomUserId) {
    
    MultiKey sourceIdSubjectId = emailToSubjectCache(configId).get(emailToAddToGrouper);
    if (sourceIdSubjectId == null) {
      Map<String, MultiKey> emailToSourceIdSubjectId = convertEmailToSourceIdSubjectId(configId, GrouperUtil.toSet(emailToAddToGrouper));
      sourceIdSubjectId = GrouperUtil.nonNull(emailToSourceIdSubjectId).get(emailToAddToGrouper);
    }

    if (sourceIdSubjectId == null) {
      Set<String> configIgnoreUserIds = configIgnoreUserIds(configId);
      if (configIgnoreUserIds.contains(emailToAddToGrouper) || configIgnoreUserIds.contains(zoomUserId)) {
        return false;
      }
      throw new SubjectNotFoundException(emailToAddToGrouper);
    }
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupSyncFolder + ":" + grouperGroupExtension, true);
    Subject subject = SubjectFinder.findByIdAndSource((String)sourceIdSubjectId.getKey(1), (String)sourceIdSubjectId.getKey(0), true);
    return group.addMember(subject, false);
  }

  /**
   * @param configId
   * @param groupSyncFolder
   * @param grouperGroupExtension
   * @param emailToAddToGrouper
   * @param zoomUserId
   * @return true if added, false if not
   */
  public static boolean removeMembership(String configId, String groupSyncFolder,
      String grouperGroupExtension, String emailToAddToGrouper, String zoomUserId) {
    
    MultiKey sourceIdSubjectId = emailToSubjectCache(configId).get(emailToAddToGrouper);
    if (sourceIdSubjectId == null) {
      Map<String, MultiKey> emailToSourceIdSubjectId = convertEmailToSourceIdSubjectId(configId, GrouperUtil.toSet(emailToAddToGrouper));
      sourceIdSubjectId = GrouperUtil.nonNull(emailToSourceIdSubjectId).get(emailToAddToGrouper);
    }

    if (sourceIdSubjectId == null) {
      return false;
    }
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupSyncFolder + ":" + grouperGroupExtension, true);
    Subject subject = SubjectFinder.findByIdAndSource((String)sourceIdSubjectId.getKey(1), (String)sourceIdSubjectId.getKey(0), true);
    return group.deleteMember(subject, false);
  }

}
