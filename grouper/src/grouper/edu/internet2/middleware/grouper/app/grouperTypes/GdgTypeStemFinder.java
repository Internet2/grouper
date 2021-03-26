package edu.internet2.middleware.grouper.app.grouperTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils; 

public class GdgTypeStemFinder {
  
  private Stem stem;
  
  private String stemId;
  
  private String stemName;
  
  private String type;
  
  private boolean runAsRoot;
  
  /**
   * if null (default) retrieve direct and indirect assignments, if true then only retrieve direct assignments,
   * if false only retrieve indirect assignments
   */
  private Boolean directAssignment;
  
  public GdgTypeStemFinder assignStem(Stem stem) {
    this.stem = stem;
    return this;
  }
  
  public GdgTypeStemFinder assignStemId(String stemId) {
    this.stemId = stemId;
    return this;
  } 
  
  public GdgTypeStemFinder assignStemName(String stemName) {
    this.stemName = stemName;
    return this;
  }
  
  public GdgTypeStemFinder assignType(String type) {
    this.type = type;
    return this;
  }
  
  public GdgTypeStemFinder assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }
  
  public GdgTypeStemFinder assignDirectAssignment(Boolean directAssignment) {
    this.directAssignment = directAssignment;
    return this;
  }
  
  public GrouperObjectTypesAttributeValue findGdgTypeStemAssignment() {
    
    Set<GrouperObjectTypesAttributeValue> gdgTypeGroupAssignments = this.findGdgTypeStemAssignments();

    return GrouperUtil.setPopOne(gdgTypeGroupAssignments);
  }
  
  
  @SuppressWarnings("unchecked")
  public Set<GrouperObjectTypesAttributeValue> findGdgTypeStemAssignments() {
   
    Subject SUBJECT_IN_SESSION = GrouperSession.staticGrouperSession().getSubject();
    
    Set<GrouperObjectTypesAttributeValue> gdgTypeGroupAssignments = (Set<GrouperObjectTypesAttributeValue>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        if (stem == null && !StringUtils.isBlank(stemId)) {
          stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemId, false, new QueryOptions().secondLevelCache(false));
        }
        
        if (stem == null && !StringUtils.isBlank(stemName)) {
          stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), stemName, false, new QueryOptions().secondLevelCache(false));
        }
        
        GrouperUtil.assertion(stem != null,  "Stem not found");
        
        if (!runAsRoot) {
          if (!stem.canHavePrivilege(SUBJECT_IN_SESSION, NamingPrivilege.STEM_ADMIN.getName(), false)) {
            throw new RuntimeException("Subject '" + SubjectUtils.subjectToString(SUBJECT_IN_SESSION) 
              + "' cannot ADMIN stem '" + stem.getName() + "'");
          }
        }
        
        if (StringUtils.isNotBlank(type) && !GrouperObjectTypesSettings.getObjectTypeNames().contains(type)) {
          throw new RuntimeException("type must be one of the valid types ["+GrouperUtil.collectionToString(GrouperObjectTypesSettings.getObjectTypeNames()) + "]");
        }
        
        Set<GrouperObjectTypesAttributeValue> result = new HashSet<GrouperObjectTypesAttributeValue>();
        
        if (StringUtils.isNotBlank(type)) {
          GrouperObjectTypesAttributeValue typesAttributeValue = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(stem, type);
          if (typesAttributeValue != null) {            
            result.add(typesAttributeValue);
          }
        } else {
          List<GrouperObjectTypesAttributeValue> grouperObjectTypesAttributeValues = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValues(stem);
          result.addAll(GrouperUtil.nonNull(grouperObjectTypesAttributeValues));
        }
        
        if (directAssignment == null) {
          return result;
        } else if (directAssignment) {
          return result.stream().filter(attributeValue -> attributeValue.isDirectAssignment()).collect(Collectors.toSet());
        } else {
          return result.stream().filter(attributeValue -> !attributeValue.isDirectAssignment()).collect(Collectors.toSet());
        }
        
      }});
    
    return gdgTypeGroupAssignments;
  }

}
