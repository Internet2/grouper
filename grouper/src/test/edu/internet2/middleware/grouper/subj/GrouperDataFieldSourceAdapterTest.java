package edu.internet2.middleware.grouper.subj;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob;
import edu.internet2.middleware.grouper.dataField.GrouperPrivacyRealmConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.testing.GrouperTestBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import junit.textui.TestRunner;

public class GrouperDataFieldSourceAdapterTest extends GrouperTest {
  
  public GrouperDataFieldSourceAdapterTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperDataFieldSourceAdapterTest("testGetSubject"));
  }
  
  public void testGetSubject() {
    
    setupData();
    
    Subject subject = SubjectFinder.findByIdAndSource("test.subject.0", "dataFieldSubjectSource", true);
    System.out.println("subject: "+subject);
    
    subject = SubjectFinder.findByIdentifierAndSource("id.test.subject.0", "dataFieldSubjectSource", true);
    System.out.println("subject: "+subject);
    
    Set<Subject> subjects = SubjectFinder.findAll("test 0", "dataFieldSubjectSource");
    
    //verify subjects name is not null
    for (Subject s : subjects) {
      System.out.println("subject in loop: " + s);
      assertNotNull(s.getName());
    }
    
    GrouperSession.callbackGrouperSessionBySubjectId("test.subject.1", "jdbc", new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Subject subject6 = SubjectFinder.findByIdAndSource("test.subject.6", "dataFieldSubjectSource", true);
        assertEquals("my name is test.subject.6", subject6.getName());
        
        String namePublic = subject6.getAttributeValue("namePublic");
        assertNull(namePublic);
        // should be able to see private name
        String namePrivate = subject6.getAttributeValue("namePrivate");
        assertEquals("my name is test.subject.6", namePrivate);
        return null;
      }
    });
    
    GrouperSession.callbackGrouperSessionBySubjectId("test.subject.0", "jdbc", new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Subject subject6 = SubjectFinder.findByIdAndSource("test.subject.6", "dataFieldSubjectSource", true);
        assertNull(subject6.getName());
        
        String namePublic = subject6.getAttributeValue("namePublic");
        assertNull(namePublic);
        // should not be able to see private name
        String namePrivate = subject6.getAttributeValue("namePrivate");
        assertNull(namePrivate);
        return null;
      }
    });
    
  }
  
  /**
   * make sure subject source cache is not caching data field subjects
   */
  public void testGetSubjectCache() {
    
    setupData();
    
    GrouperSession.startRootSession();
    assertEquals("my name is test.subject.6", SubjectFinder.findByIdAndSource("test.subject.6", "dataFieldSubjectSource", true).getAttributes().get("nameprivate").iterator().next());
    
    GrouperSession.start(SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true));
    assertEquals(0, GrouperUtil.length(SubjectFinder.findByIdAndSource("test.subject.6", "dataFieldSubjectSource", true).getAttributes().get("nameprivate")));
    
  }
  
  public void setupData() {
    
    try {      
      synchronized (GrouperTestBase.class) {
        GrouperSession grouperSession = GrouperSession.startRootSession();
        
        //TODO: add to DDL
        if (!GrouperDdlUtils.assertTableThere(true, "subject_base_v")) {
          new GcDbAccess().sql(""" 
              CREATE VIEW subject_base_v
                   AS SELECT subjectid AS id,
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
              """).executeSql();
        }
        
        if (!GrouperDdlUtils.assertTableThere(true, "subject_v")) {
          new GcDbAccess().sql(""" 
              create view subject_v as 
              select sbv.id, 
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
              """).executeSql();
        }
        
        //TODO: take the following sql out once Shilen's code is in
        
        String _sql = """
            INSERT INTO public.grouper_members (id,subject_id,subject_source,subject_type,hibernate_version_number,subject_identifier0,subject_identifier1,subject_identifier2,id_index,internal_id,email0,sort_string0,sort_string1,sort_string2,sort_string3,sort_string4,search_string0,search_string1,search_string2,search_string3,search_string4,"name",description,context_id,subject_resolution_deleted,subject_resolution_resolvable,subject_resolution_eligible) VALUES
       ('8d498acb78f54c789d4229daa805d1a0','test.subject.0','dataFieldSubjectSource','person',0,'',NULL,NULL,999901,999901,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'My name is test.subject.0','My description is test.subject.0','f3f6ba364aa448b58c11677c9081e7b0','F','T','T')
            """;
//        new GcDbAccess().sql(_sql).executeSql();
        
        Group viewersGroup = new GroupSave(grouperSession).assignName("test:dataFieldPrivateViewers").assignCreateParentStemsIfNotExist(true).save();
        Group updatersGroup = new GroupSave(grouperSession).assignName("test:dataFieldPrivateUpdaters").assignCreateParentStemsIfNotExist(true).save();
        Group readersGroup = new GroupSave(grouperSession).assignName("test:dataFieldPrivateReaders").assignCreateParentStemsIfNotExist(true).save();
        
        readersGroup.addMember(updatersGroup.toSubject());
        
        readersGroup.addMember(SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true));
        readersGroup.addMember(SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true));
        
        //rum change_log temp daemon
        GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog", false);
        GrouperDataEngine.clearHighestLevelCache();
        
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.privateConfigId.privacyRealmName").value("privateConfigId").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.privateConfigId.privacyRealmPublic").value("false").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.privateConfigId.privacyRealmAuthenticated").value("false").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.privateConfigId.privacyRealmSysadminsCanView").value("true").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.privateConfigId.privacyRealmViewersGroupName").value(viewersGroup.getName()).store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.privateConfigId.privacyRealmUpdatersGroupName").value(updatersGroup.getName()).store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.privateConfigId.privacyRealmReadersGroupName").value(readersGroup.getName()).store();
        
        GrouperPrivacyRealmConfig privacyRealmConfigPrivate = new GrouperPrivacyRealmConfig();
        privacyRealmConfigPrivate.readFromConfig("privateConfigId");
        
        
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.publicConfigId.privacyRealmName").value("publicConfigId").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.publicConfigId.privacyRealmPublic").value("true").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.publicConfigId.privacyRealmAuthenticated").value("false").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.publicConfigId.privacyRealmSysadminsCanView").value("true").store();
        
        GrouperPrivacyRealmConfig privacyRealmConfigPublic = new GrouperPrivacyRealmConfig();
        privacyRealmConfigPublic.readFromConfig("publicConfigId");
        
        
        for (String configId: new String[] {"namePublic", "namePrivate", "lfNamePublic", "lfNamePrivate", "netIdPublic", "netIdPrivate", "emailPublic", 
            "emailPrivate", "descriptionPublic", "descriptionPrivate"}) {
          
          String privacyRealm = configId.endsWith("Public") ? "publicConfigId": "privateConfigId";
          String fieldDataUseValue = configId.startsWith("netId") ? "informational": "identifier";
          
          new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField."+configId+".fieldAliases").value(configId).store();
          new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField."+configId+".fieldPrivacyRealm").value(privacyRealm).store();
          new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField."+configId+".descriptionHtml").value("<b>description html </b>").store();
          new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField."+configId+".fieldDataAssignableTo").value("individuals").store();
          new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField."+configId+".fieldDataUse").value(fieldDataUseValue).store();
          
        }
        
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.dataFieldSource.name").value("dataFieldSource").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.dataFieldSource.subjectSource").value("true").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.dataFieldSource.subjectSourceId").value("dataFieldSubjectSource").store();
        
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerConfigId").value("dataFieldSource").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryType").value("sql").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQuerySqlConfigId").value("grouper").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQuerySqlQuery")
          .value("""
              SELECT id as subject_id, name_public, name_private, lfname_public, lfname_private, loginid_public, loginid_private, 
              description_public, description_private, email_public, email_private
                  FROM subject_v
              """).store();

        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataStructure").value("attribute").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQuerySubjectIdAttribute").value("subject_id").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQuerySubjectIdType").value("subjectId").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQuerySubjectSourceId").value("dataFieldSubjectSource").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryNumberOfDataFields").value("10").store();
        
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.0.providerDataFieldConfigId").value("namePublic").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.0.providerDataFieldMappingType").value("attribute").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.0.providerDataFieldAttribute").value("name_public").store();
        
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.1.providerDataFieldConfigId").value("namePrivate").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.1.providerDataFieldMappingType").value("attribute").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.1.providerDataFieldAttribute").value("name_private").store();
        
        // lf name public
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.2.providerDataFieldConfigId").value("lfNamePublic").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.2.providerDataFieldMappingType").value("attribute").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.2.providerDataFieldAttribute").value("lfname_public").store();
        
        // lf name private
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.3.providerDataFieldConfigId").value("lfNamePrivate").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.3.providerDataFieldMappingType").value("attribute").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.3.providerDataFieldAttribute").value("lfname_private").store();
        
        // net id public
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.4.providerDataFieldConfigId").value("netIdPublic").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.4.providerDataFieldMappingType").value("attribute").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.4.providerDataFieldAttribute").value("loginid_public").store();
        
        // net id private
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.5.providerDataFieldConfigId").value("netIdPrivate").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.5.providerDataFieldMappingType").value("attribute").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.5.providerDataFieldAttribute").value("loginid_private").store();
        
        // email public
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.6.providerDataFieldConfigId").value("emailPublic").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.6.providerDataFieldMappingType").value("attribute").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.6.providerDataFieldAttribute").value("email_public").store();
        
        // email private
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.7.providerDataFieldConfigId").value("emailPrivate").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.7.providerDataFieldMappingType").value("attribute").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.7.providerDataFieldAttribute").value("email_private").store();
        
        // description public
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.8.providerDataFieldConfigId").value("descriptionPublic").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.8.providerDataFieldMappingType").value("attribute").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.8.providerDataFieldAttribute").value("description_public").store();
        
        // description private
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.9.providerDataFieldConfigId").value("descriptionPrivate").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.9.providerDataFieldMappingType").value("attribute").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.dataFieldSourceQuery.providerQueryDataField.9.providerDataFieldAttribute").value("description_private").store();
        
        
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.adapterClass").value("edu.internet2.middleware.grouper.subj.GrouperDataFieldSourceAdapter").store();;
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.0.name").value("name").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.0.translationType").value("dataFieldPrivacyTarget").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.1.name").value("namePrivate").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.1.privacyAttributeName").value("name").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.1.privacyDataFieldSource").value("true").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.1.privacyPriority").value("1").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.1.sourceAttribute").value("namePrivate").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.1.translationType").value("sourceAttribute").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.10.formatToLowerCase").value("false").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.10.name").value("emailPrivate").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.10.privacyAttributeName").value("email").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.10.privacyDataFieldSource").value("true").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.10.privacyPriority").value("1").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.10.sourceAttribute").value("emailPrivate").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.10.translationType").value("sourceAttribute").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.11.name").value("emailPublic").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.11.privacyAttributeName").value("email").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.11.privacyDataFieldSource").value("true").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.11.privacyPriority").value("2").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.11.sourceAttribute").value("emailPublic").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.11.translationType").value("sourceAttribute").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.12.name").value("description").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.12.translationType").value("dataFieldPrivacyTarget").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.13.name").value("descriptionPrivate").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.13.privacyAttributeName").value("description").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.13.privacyDataFieldSource").value("true").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.13.privacyPriority").value("1").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.13.sourceAttribute").value("descriptionPrivate").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.13.translationType").value("sourceAttribute").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.14.name").value("descriptionPublic").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.14.privacyAttributeName").value("description").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.14.privacyDataFieldSource").value("true").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.14.privacyPriority").value("2").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.14.sourceAttribute").value("descriptionPublic").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.14.translationType").value("sourceAttribute").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.2.name").value("namePublic").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.2.privacyAttributeName").value("name").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.2.privacyDataFieldSource").value("true").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.2.privacyPriority").value("2").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.2.sourceAttribute").value("namePublic").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.2.translationType").value("sourceAttribute").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.3.name").value("lfName").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.3.translationType").value("dataFieldPrivacyTarget").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.4.name").value("lfNamePrivate").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.4.privacyAttributeName").value("lfName").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.4.privacyDataFieldSource").value("true").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.4.privacyPriority").value("1").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.4.sourceAttribute").value("lfNamePrivate").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.4.translationType").value("sourceAttribute").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.5.name").value("lfNamePublic").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.5.privacyAttributeName").value("lfName").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.5.privacyDataFieldSource").value("true").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.5.privacyPriority").value("2").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.5.sourceAttribute").value("lfNamePublic").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.5.translationType").value("sourceAttribute").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.6.name").value("netId").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.6.translationType").value("dataFieldPrivacyTarget").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.7.name").value("netIdPrivate").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.7.privacyAttributeName").value("netId").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.7.privacyDataFieldSource").value("true").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.7.privacyPriority").value("1").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.7.sourceAttribute").value("netIdPrivate").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.7.translationType").value("sourceAttribute").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.8.name").value("netIdPublic").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.8.privacyAttributeName").value("netId").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.8.privacyDataFieldSource").value("true").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.8.privacyPriority").value("2").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.8.sourceAttribute").value("netIdPublic").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.8.translationType").value("sourceAttribute").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.9.name").value("email").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.9.translationType").value("dataFieldPrivacyTarget").store();

        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.6.subjectIdentifier").value("true").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.7.subjectIdentifier").value("true").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.8.subjectIdentifier").value("true").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.9.subjectIdentifier").value("true").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.10.subjectIdentifier").value("true").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.attribute.11.subjectIdentifier").value("true").store();
        
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.id").value("dataFieldSubjectSource").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.name").value("data field subject source").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.numberOfAttributes").value("15").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.param.Description_AttributeType.value").value("description").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.param.Name_AttributeType.value").value("name").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.param.emailAttributeName.value").value("email").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.param.netId.value").value("netId").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.param.subjectIdentifierAttribute0.value").value("netId").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.searchAttribute.0.attributeName").value("descriptionPrivate").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.searchAttribute.1.attributeName").value("descriptionPublic").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.searchAttributeCount").value("2").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.sortAttribute.0.attributeName").value("lfNamePrivate").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.sortAttribute.1.attributeName").value("lfNamePublic").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.sortAttributeCount").value("2").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.types").value("person").store();
        
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.g_gsa.param.sortAttribute1.value").value("displayExtension").store();
        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.g_gsa.param.searchAttribute1.value").value("searchAttribute0").store();
        
//        new GrouperDbConfig().configFileName("subject.properties").propertyName("subjectApi.source.dataFieldSubjectSource.extraAttributesFromSource").value("person").store();

        new GrouperDbConfig().configFileName("grouper.properties").propertyName("internalSubjects.searchAttribute1.el").value("${subject.name},${subject.id}").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("internalSubjects.sortAttribute1.el").value("${subject.name}").store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("security.member.sort.string0.allowOnlyGroup").value(readersGroup.getName()).store();
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("security.member.search.string0.allowOnlyGroup").value(readersGroup.getName()).store();
        

        new GrouperDbConfig().configFileName("grouper-ui.properties").propertyName("grouper.ui.authentication.sourceIds").value("jdbc,g:isa").store();

        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataFieldSourceFull.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob").store();
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataFieldSourceFull.dataProviderConfigId").value("dataFieldSource").store();
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataFieldSourceFull.quartzCron").value("0 0 5 * * ?").store();

        
        // load data
        SubjectConfig.clearCache();
        GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataFieldSourceFull");
      }
    } catch (Exception e) {
      System.out.println("error: " + e);
    }
    
  }

}
