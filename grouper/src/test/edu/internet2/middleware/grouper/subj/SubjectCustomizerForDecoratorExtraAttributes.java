package edu.internet2.middleware.grouper.subj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.permissions.PermissionResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * add attributes securely to the subject
 * @author mchyzer
 *
 */
public class SubjectCustomizerForDecoratorExtraAttributes extends SubjectCustomizerBase {

  /** stem name of the permission resources which represent columns in the attribute table */
  public static final String PERMISSIONS_STEM_NAME = "subjectAttributes:permissions:columnNames";

  /** privileged employee group name */
  public static final String PRIVILEGED_ADMIN_GROUP_NAME = "etc:privilegedAdmin";

  /** source id we care about */
  private static final String SOURCE_ID = "jdbc";

  /** subjectAttributes:permissions */
  public static final String SUBJECT_ATTRIBUTES_PERMISSIONS_ATTRIBUTE_DEF = "subjectAttributes:permissions";

  
  /**
   * @see SubjectCustomizer#decorateSubjects(GrouperSession, Set, Collection)
   */
  @Override
  public Set<Subject> decorateSubjects(GrouperSession grouperSession,
      final Set<Subject> subjects, final Collection<String> attributeNamesRequested) {

    //nothing to do if no results or no attributes
    if (GrouperUtil.length(subjects) == 0 || GrouperUtil.length(attributeNamesRequested) == 0) {
      return subjects;
    }
    
    final Subject subjectChecking = grouperSession.getSubject();
    
    //do all this as admin
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {

        //see if admin
        boolean isAdmin = new MembershipFinder().assignCheckSecurity(false).addGroup(PRIVILEGED_ADMIN_GROUP_NAME)
          .addSubject(theGrouperSession.getSubject()).hasMembership();
        
        //see which attributes the user has access to based on permissions
        PermissionResult permissionResult = null;
        
        //only need to check permissions if not admin
        if (!isAdmin) {
          
          Stem permissionsStem = StemFinder.findByName(theGrouperSession, PERMISSIONS_STEM_NAME, true);
          
          permissionResult = new PermissionFinder().addAction("read").addPermissionDef(SUBJECT_ATTRIBUTES_PERMISSIONS_ATTRIBUTE_DEF)
            .addSubject(subjectChecking).assignPermissionNameFolder(permissionsStem)
            .assignPermissionNameFolderScope(Scope.ONE).findPermissionResult();
          
        }
        
        //see which columns the user can see
        Set<String> columnsSet = isAdmin ? new HashSet<String>(attributeNamesRequested) 
            : permissionResult.permissionNameExtensions();

        //intersect the columns the user can see with the ones requested
        columnsSet.retainAll(attributeNamesRequested);

        if (GrouperUtil.length(columnsSet) == 0) {
          return null;
        }

        List<String> columns = new ArrayList<String>(columnsSet);

        //TODO add batching
        
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
        sql.append(" from testgrouper_subj_attr where subject_id in( ");
        sql.append(HibUtils.convertToInClauseForSqlStatic(subjectIds));
        sql.append(")");
        
        //get the results from the DB
        List<String[]> dbResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(), GrouperUtil.toListObject(subjectIds.toArray()));
        
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
        return null;
      }
    });
    
    return subjects;
  }
  
}
