/*
 * @author mchyzer
 * $Id: XmlUserAuditExportTest.java,v 1.1 2009-03-31 06:58:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.xml.userAudit;

import java.io.File;

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

      //export
      XmlUserAuditExport.writeUserAudits(file);
      
      //import
      new XmlUserAuditImport().readUserAudits(file);
      
      //delete types and audits, try again
      HibernateSession.byHqlStatic().createQuery("delete from AuditEntry").executeUpdate();
      HibernateSession.byHqlStatic().createQuery("delete from AuditType").executeUpdate();

      //import
      new XmlUserAuditImport().readUserAudits(file);

    } finally {
      GrouperUtil.deleteFile(file);
    }
    
  }
  
}
