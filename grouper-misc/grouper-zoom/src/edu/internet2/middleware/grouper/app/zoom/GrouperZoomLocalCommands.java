/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.zoom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class GrouperZoomLocalCommands {

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
    String ignoreUserIds = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("zoom." + configId + ".ignoreUserIds");

    return GrouperClientUtils.splitTrimToSet(ignoreUserIds, ",");

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
   * @return the map of group extension to set of multikey with sourceId and subjectId
   */
  public static Map<String, Set<String>> groupsEmailsToProvision(String configId) {
    
    Map<String, Set<String>> result = new HashMap<String, Set<String>>();
    
    Map<String, Set<MultiKey>> groupsSourceIdsSubjectIdsToProvision = groupsSourceIdsSubjectIdsToProvision(configId);
    
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
   * @param sourceIdToSubjectIds
   */
  public static Map<MultiKey, String> convertSourceIdSubjectIdToEmail(final String configId,
      final Map<String, Set<String>> sourceIdToSubjectIds) {
    
    return (Map<MultiKey, String>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Map<MultiKey, String> sourceIdSubjectIdToEmail = new HashMap<MultiKey, String>();
        String subjectAttributeForZoomEmail = subjectAttributeForZoomEmail(configId);

        for (String sourceId : sourceIdToSubjectIds.keySet()) {  
          Set<String> subjectIds = sourceIdToSubjectIds.get(sourceId);
          Map<String, Subject> subjectIdToSubject = SubjectFinder.findByIds(subjectIds, sourceId);
          for (Subject subject : subjectIdToSubject.values()) {
            
            String email = subject.getAttributeValue(subjectAttributeForZoomEmail);
            
            // ignore if no attribute
            if (StringUtils.isBlank(email)) {
              continue;
            }
            
            MultiKey multiKey = new MultiKey(subject.getSourceId(), subject.getId());
            sourceIdSubjectIdToEmail.put(multiKey, email);
          }
        }
        return sourceIdSubjectIdToEmail;
      }
    });

  }

  /**
   * get all memberships to provision, filter out ones not in correct source
   * @param configId
   * @return the map of group extension to set of multikey with sourceId and subjectId
   */
  public static Map<String, Set<MultiKey>> groupsSourceIdsSubjectIdsToProvision(String configId) {
    
    Stem folderToProvision = folderToProvision(configId);
    
    String sql = "select gg.extension, subject_source, subject_id from grouper_memberships_lw_v gmlv, grouper_groups gg "
       + " where gmlv.list_name = 'members' and gmlv.group_id = gg.id and gg.parent_stem = '" + folderToProvision.getId() + "'";

    List<Object[]> rows = new GcDbAccess().sql(sql).selectList(Object[].class);
    
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
   * @param args
   */
  public static void main(String[] args) {

  }

}
