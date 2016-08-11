/**
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouper.subj.decoratorExamples;

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
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.permissions.PermissionResult;
import edu.internet2.middleware.grouper.subj.SubjectCustomizer;
import edu.internet2.middleware.grouper.subj.SubjectCustomizerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectImpl;

/**
 * add attributes securely to the subject
 * @author mchyzer
 *
 */
public class SubjectCustomizerForDecoratorExtraAttributes extends SubjectCustomizerBase {

  /** stem name of the permission resources which represent columns in the attribute table */
  public static final String PERMISSIONS_STEM_NAME = "subjectAttributes:permissions:columnNames";

  /** privileged employee group name
   * @return the group name
   */
  public static String PRIVILEGED_ADMIN_GROUP_NAME() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":privilegedAdmin";
  }

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
        boolean isAdmin = new MembershipFinder().assignCheckSecurity(false).addGroup(PRIVILEGED_ADMIN_GROUP_NAME())
          .addSubject(subjectChecking).hasMembership();
        
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

        List<Subject> subjectList = new ArrayList<Subject>(subjects);
        
        int batchSize = 180;
        int numberOfBatches = GrouperUtil.batchNumberOfBatches(subjectList, batchSize);
        
        subjects.clear();

        //batch these since you dont know if there will be more than 180 to resolve
        for (int i=0; i<numberOfBatches; i++) {
          
          List<Subject> subjectsBatch = GrouperUtil.batchList(subjectList, batchSize, i);
          
          //get the list of subject ids
          Set<String> subjectIds = new LinkedHashSet<String>();
          for (Subject subject : subjectsBatch) {
            if (StringUtils.equals(SOURCE_ID, subject.getSourceId())) {
              subjectIds.add(subject.getId());
            }
          }
          
          //get the results of these columns for these subjects (by id)
          //make query
          StringBuilder sql = new StringBuilder("select subject_id, ");
          sql.append(GrouperUtil.join(columns.iterator(), ','));
          sql.append(" from testgrouper_subj_attr where subject_id in( ");
          sql.append(HibUtils.convertToInClauseForSqlStatic(subjectIds));
          sql.append(")");
          
          //get the results from the DB
          List<String[]> dbResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(), GrouperUtil.toListObject(subjectIds.toArray()));
          
          //index the results by id of row
          Map<String, String[]> dbResultLookup = new HashMap<String, String[]>();
          
          Set<String> subjectIdsRelated = new HashSet<String>();
          
          for(String[] row : dbResults) {
            dbResultLookup.put(row[0], row);
            subjectIdsRelated.add(row[0]);
          }
          
          {
            //copy over the subjects since we need new objects...
            for (Subject subject : subjectsBatch) {
              
              //if it is a subject impl or doesnt have new attributes, we are all good...
              if (StringUtils.equals("jdbc", subject.getSourceId()) && subjectIdsRelated.contains(subject.getId())) {
                
                if (!(subject instanceof SubjectImpl)) {
                  subject = new SubjectImpl(subject.getId(), subject.getName(), 
                      subject.getDescription(), subject.getTypeName(), subject.getSourceId(), 
                      subject.getAttributes(false));
                }
                String[] row = dbResultLookup.get(subject.getId());
                //shouldnt be null at this point
                if (row != null) {
                  //look through the attributes
                  for (int j=0;j<columns.size();j++) {
                    
                    //add one to row index since first is id.  add if null or not, we need the attribute set
                    String columnName = columns.get(j);
                    String value = row[j+1];

                    //this should return a modifiable map of attributes for us to work with
                    subject.getAttributes(false).put(columnName, GrouperUtil.toSet(value));
                  }
                }
              }
              
              subjects.add(subject);
              
            }
          
          }
          
        }
        return null;
      }
    });
    
    return subjects;
  }
  
}
