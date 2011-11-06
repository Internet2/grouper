package edu.internet2.middleware.grouper.subj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.membership.GroupMembershipResult;
import edu.internet2.middleware.grouper.membership.PermissionResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * add attributes securely to the subject
 * @author mchyzer
 *
 */
public class SubjectCustomizerForDecoratorTesting3 extends SubjectCustomizerBase {

  /** stem name of the permission resources which represent columns in the attribute table */
  private static final String PERMISSIONS_STEM_NAME = "subjectAttributes:permissions:columnNames";

  /** privileged employee group name */
  private static final String PRIVILEGED_ADMIN_GROUP_NAME = "etc:privilegedAdmin";

  /** source id we care about */
  private static final String SOURCE_ID = "jdbc";

  
  /**
   * @see SubjectCustomizer#decorateSubjects(GrouperSession, Set, Collection)
   */
  @Override
  public Set<Subject> decorateSubjects(GrouperSession grouperSession,
      Set<Subject> subjects, Collection<String> attributeNamesRequested) {

    //nothing to do if no results or no attributes
    if (GrouperUtil.length(subjects) == 0 || GrouperUtil.length(attributeNamesRequested) == 0) {
      return subjects;
    }
    
    //get results in one query
    GroupMembershipResult groupMembershipResult = calculateMemberships(subjects, IncludeGrouperSessionSubject.TRUE, 
        GrouperUtil.toSet(PRIVILEGED_ADMIN_GROUP_NAME));

    
    //see if the user is privileged
    boolean grouperSessionIsPrivileged = groupMembershipResult.hasMembership(PRIVILEGED_ADMIN_GROUP_NAME, grouperSession.getSubject());
    
    //if so, we are done, they can see stuff
    if (grouperSessionIsPrivileged) {
      return subjects;
    }

    //see which attributes the user has access to based on permissions
    PermissionResult permissionResult =  calculatePermissionsInStem(null, 
        IncludeGrouperSessionSubject.TRUE, PERMISSIONS_STEM_NAME, Scope.ONE);
    
    //see which columns the user can see
    Set<String> columnsSet = permissionResult.permissionNameExtensions(PERMISSIONS_STEM_NAME, grouperSession.getSubject(), Scope.ONE);
    //intersect the columns the user can see with the ones requested
    columnsSet.retainAll(attributeNamesRequested);

    if (GrouperUtil.length(columnsSet) == 0) {
      return subjects;
    }

    List<String> columns = new ArrayList<String>(columnsSet);

    //get the list of subject ids
    Set<String> subjectIds = new LinkedHashSet<String>();
    for (Subject subject : subjects) {
      if (StringUtils.equals(SOURCE_ID, subject.getSourceId())) {
        subjectIds.add(subject.getId());
      }
    }
    
    //get the results of these columns for these subjects (by id)
    //make query
    StringBuilder sql = new StringBuilder("select id, ");
    sql.append(GrouperUtil.join(columns.iterator(), ','));
    sql.append(" from subject_attribute_table where id in( ");
    sql.append(HibUtils.convertToInClauseForSqlStatic(subjectIds));
    sql.append(")");
    
    //get the results from the DB
    List<String[]> dbResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(), null);
    
    //index the results by id of row
    Map<String, String[]> dbResultLookup = new HashMap<String, String[]>();
    
    for(String[] row : dbResults) {
      dbResultLookup.put(row[0], row);
    }
    
    //loop through the subjects and match everything up
    for (Subject subject : subjects) {
      if (StringUtils.equals(SOURCE_ID, subject.getSourceId())) {
        String[] row = dbResultLookup.get(subject.getId());
        if (row != null) {
          //look through the attributes
          for (int i=0;i<columns.size();i++) {
            //add one to row index since first is id.  add if null or not, we need the attribute set
            subject.getAttributes().put(columns.get(0), GrouperUtil.toSet(row[i+1]));
          }
        }
      }
    }
    return subjects;
  }
  
}
