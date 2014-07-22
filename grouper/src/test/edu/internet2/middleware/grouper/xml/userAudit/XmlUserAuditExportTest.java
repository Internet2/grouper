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
/*
 * @author mchyzer
 * $Id: XmlUserAuditExportTest.java,v 1.3 2009-11-09 03:12:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.xml.userAudit;

import java.io.File;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class XmlUserAuditExportTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new XmlUserAuditExportTest("testExportImport"));
  }
  
  /**
   * @param name
   */
  public XmlUserAuditExportTest(String name) {
    super(name);
  }

  /**
   * test an export and import
   */
  public void testExportImport() {
    
    File file = new File("testUserAuditExport.xml");
    GrouperUtil.deleteFile(file);
    try {
      
      //generate some audit records
      GrouperSession grouperSession = SessionHelper.getRootSession();
      Stem root = StemHelper.findRootStem(grouperSession);
      Stem edu = StemHelper.addChildStem(root, "edu", "education");
      StemHelper.addChildGroup(edu, "i2", "internet2");

      HibernateSession.bySqlStatic().executeSql("update grouper_audit_entry set logged_in_member_id = 'abc'");
      
      //export
      XmlUserAuditExport.writeUserAudits(file);
      
      //import
      new XmlUserAuditImport().readUserAudits(file);
      
      //delete types and audits, try again
      HibernateSession.byHqlStatic().createQuery("delete from AuditEntry").executeUpdate();
      HibernateSession.byHqlStatic().createQuery("delete from AuditType").executeUpdate();

      //import
      new XmlUserAuditImport().readUserAudits(file);

      //see if all have abc
      int abcRecords = (Integer)HibernateSession.bySqlStatic().select(int.class, 
          "select count(*) from grouper_audit_entry where logged_in_member_id = 'abc'");
      
      //2 at least, one for add stem, one for add group
      assertTrue("Need to find some abc's: " + abcRecords, abcRecords >= 2);
      
    } finally {
      GrouperUtil.deleteFile(file);
    }
    
  }
  
}
