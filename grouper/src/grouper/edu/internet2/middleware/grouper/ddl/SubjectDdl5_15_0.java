package edu.internet2.middleware.grouper.ddl;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class SubjectDdl5_15_0 {
  
  static void addSubjectBaseView(DdlVersionBean ddlVersionBean, Database database) {
    
//    if (!SubjectDdl5_15_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
//      return;
//    }
    
    if (ddlVersionBean.didWeDoThis("v5_15_0_addSubjectBaseView", true)) {
      return;
    }
    
    String sql = """
        SELECT subjectid AS id,
          name,
          ( SELECT sa2.value
                 FROM subjectattribute sa2
                WHERE sa2.name = 'name' AND sa2.subjectid = s.subjectid) AS lfname,
          ( SELECT sa3.value
                 FROM subjectattribute sa3
                WHERE sa3.name = 'loginid' AND sa3.subjectid = s.subjectid) AS loginid,
          ( SELECT sa4.value
                 FROM subjectattribute sa4
                WHERE sa4.name = 'description' AND sa4.subjectid = s.subjectid) AS description,
          ( SELECT sa5.value
                 FROM subjectattribute sa5
                WHERE sa5.name = 'email' AND sa5.subjectid = s.subjectid) AS email
        FROM subject s
        """;
    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "subject_base_v", "subject base view",
        GrouperUtil.toSet("id", "name", "lfname", "loginid", "description", "email"),
        GrouperUtil.toSet("id: subject id",
            "name: subject name",
            "lfname: last first name",
            "loginid: login id",
            "description: subject description",
            "email: subject email"), sql);
    
  }
  
  static void addSubjectView(DdlVersionBean ddlVersionBean, Database database) {
    
//  if (!SubjectDdl5_15_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
//    return;
//  }
  
  if (ddlVersionBean.didWeDoThis("v5_15_0_addSubjectView", true)) {
    return;
  }
  
  String sql = """
      select sbv.id as id, 
        case when id in ('test.subject.6', 'test.subject.7') then null else name end as name_public, 
        name as name_private,
        case when id in ('test.subject.6', 'test.subject.7') then null else lfname end as lfname_public, 
        lfname as lfname_private,
        case when id in ('test.subject.7', 'test.subject.8') then null else loginid end as loginid_public, 
        loginid as loginid_private,
        case when id in ('test.subject.8', 'test.subject.9') then null else description end as description_public, 
        description as description_private,
        case when id in ('test.subject.5', 'test.subject.6') then null else email end as email_public, 
        email as email_private
      from subject_base_v sbv
      """;
  
  GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "subject_v", "subject view",
      GrouperUtil.toSet("id", "name_public", "name_private",
          "lfname_public", "lfname_private", "loginid_public", "loginid_private",
          "description_public", "description_private", "email_public", "email_private"),
      GrouperUtil.toSet("id: subject id", "name_public: public name", "name_private: private name",
          "lfname_public: last first name public", "lfname_private: last first name private", "loginid_public: login id public",
          "loginid_private: login id private",
          "description_public: public description", "description_private: private description", 
          "email_public: public email", "email_private: private email"),
      sql);
  
}

}
