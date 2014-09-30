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
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.xml.importXml;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportMain;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestXml.java,v 1.2 2009-03-24 17:12:08 mchyzer Exp $
 * @since   1.1.0
 */
public class XmlLegacyTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new XmlLegacyTest("testFullExportFullImportCustomTypes"));
  }
  
  /** */
  private static final Log LOG = GrouperUtil.getLog(XmlLegacyTest.class);

  /**
   * 
   * @param name
   */
  public XmlLegacyTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testFullExportWithEscapedCharactersAndFullImportFullStem() {
    LOG.info("testFullExportWithEscapedCharactersAndFullImportFullStem");
    try {
      // Populate Registry And Verify
      R     r   = R.populateRegistry(4, 1, 0);
      // For Later Validation
      String  amp   = "ampersand - &";
      String  apos  = "apostrophe - '";
      String  gt    = "greater than - >";
      String  lt    = "less than - <";
      // Set and verify unsightly values
      Stem  nsA = assertFindStemByName( r.rs, "i2:a" );
      nsA.setDisplayExtension(amp);
      nsA.store();
      assertStemDisplayExtension(nsA, amp);

      Stem  nsB = assertFindStemByName( r.rs, "i2:b" );
      nsB.setDisplayExtension(apos);
      nsB.store();
      assertStemDisplayExtension(nsB, apos);

      Stem  nsC = assertFindStemByName( r.rs, "i2:c" );
      nsC.setDisplayExtension(gt);
      nsC.store();
      assertStemDisplayExtension(nsC, gt);

      Stem  nsD = assertFindStemByName( r.rs, "i2:d" );
      nsD.setDisplayExtension(lt);
      nsD.store();
      assertStemDisplayExtension(nsD, lt);

      Group gAA = assertFindGroupByName( r.rs, "i2:a:a" );
      gAA.setDisplayExtension(amp);
      gAA.store();
      assertGroupDisplayExtension(gAA, amp);

      Group gBA = assertFindGroupByName( r.rs, "i2:b:a" );
      gBA.setDisplayExtension(apos);
      gBA.store();
      assertGroupDisplayExtension(gBA, apos);

      Group gCA = assertFindGroupByName( r.rs, "i2:c:a" );
      gCA.setDisplayExtension(gt);
      gCA.store();
      assertGroupDisplayExtension(gCA, gt);

      Group gDA = assertFindGroupByName( r.rs, "i2:d:a" );
      gDA.setDisplayExtension(lt);
      gDA.store();
      assertGroupDisplayExtension(gDA, lt);

      r.rs.stop();

      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExportMain xmlExportMain = new XmlExportMain();
      xmlExportMain.writeAllTables(w, "a string");
      
      String xml = w.toString();
      s.stop();

      // Reset And Verify
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertDoNotFindStemByName( s, "i2:a" );
      s.stop();

      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImportMain xmlImportMain = new XmlImportMain();
      
      
      xmlImportMain.setRecordReport(true);

      xmlImportMain.processXml(xml);
      s.stop();

      // Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );

      nsA = assertFindStemByName(s, "i2:a");
      assertStemDisplayExtension(nsA, amp);

      nsB = assertFindStemByName(s, "i2:b");
      assertStemDisplayExtension(nsB, apos);

      nsC = assertFindStemByName(s, "i2:c");
      assertStemDisplayExtension(nsC, gt);

      nsD = assertFindStemByName(s, "i2:d");
      assertStemDisplayExtension(nsD, lt);

      gAA = assertFindGroupByName(s, "i2:a:a");
      assertGroupDisplayExtension(gAA, amp);

      gBA = assertFindGroupByName(s, "i2:b:a");
      assertGroupDisplayExtension(gBA, apos);

      gCA = assertFindGroupByName(s, "i2:c:a");
      assertGroupDisplayExtension(gCA, gt);

      gDA = assertFindGroupByName(s, "i2:d:a");
      assertGroupDisplayExtension(gDA, lt);

      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFullExportWithEscapedCharactersAndFullImportFullStem()


  /**
   * 
   */
  public void testFullExportFullImportCustomTypes() {
    LOG.info("testFullExportFullImportCustomTypes");
    try {
      // Populate Registry And Verify
      R         r     = R.populateRegistry(1, 1, 1);
      Group     gA    = assertFindGroupByName( r.rs, "i2:a:a" );
      Subject   subjA = r.getSubject("a");
      GroupType type  = GroupType.createType(r.rs, "custom type");
      AttributeDefName attr  = type.addAttribute(r.rs, "custom attribute", false);
      Field     list  = type.addList(r.rs, "custom list", AccessPrivilege.READ, AccessPrivilege.UPDATE);
      gA.addType( type );
      gA.setAttribute( attr.getLegacyAttributeName(true), attr.getLegacyAttributeName(true) );

      gA.addMember( subjA, list );
      r.rs.stop();
  
      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();

      XmlExportMain xmlExportMain = new XmlExportMain();
      xmlExportMain.writeAllTables(w, "a string");
      
      String xml = w.toString();

      s.stop();
  
      // Reset And Verify
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertDoNotFindGroupByName( s, "i2:a:a" );
      s.stop();
  
      // Add Subjects
      r = R.populateRegistry(0, 0, 1);
      subjA = r.getSubject("a");
      r.rs.stop();
  
      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImportMain xmlImportMain = new XmlImportMain();
      xmlImportMain.setRecordReport(true);
      xmlImportMain.processXml(xml);

      s.stop();
  
      // Verify
      s     = GrouperSession.start( SubjectFinder.findRootSubject() );
      gA    = assertFindGroupByName( s, "i2:a:a" );
      type  = assertFindGroupType("custom type");
      list  = assertFindField("custom list");
      assertGroupHasType( gA, type, true );
      assertGroupAttribute( gA, attr.getLegacyAttributeName(true), attr.getLegacyAttributeName(true) );
      assertGroupHasMember( gA, subjA, list, true );
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFullExportFullImportCustomTypes()

  /**
   * 
   */
  public void testFullExportFullImportFullAccessPrivs() {
    LOG.info("testFullExportFullImportFullAccessPrivs");
    try {
      // Populate Registry And Verify
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = assertFindGroupByName( r.rs, "i2:a:a" );
      gA.grantPriv( SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN );
      gA.grantPriv( SubjectFinder.findAllSubject(), AccessPrivilege.OPTOUT );
      gA.revokePriv( SubjectFinder.findAllSubject(), AccessPrivilege.READ );
      gA.revokePriv( SubjectFinder.findAllSubject(), AccessPrivilege.VIEW );
      boolean has_a   = gA.hasAdmin( SubjectFinder.findAllSubject() );
      boolean has_oi  = gA.hasOptin( SubjectFinder.findAllSubject() );
      boolean has_oo  = gA.hasOptout( SubjectFinder.findAllSubject() );
      boolean has_u   = gA.hasUpdate( SubjectFinder.findAllSubject() );
      boolean has_r   = gA.hasRead( SubjectFinder.findAllSubject() );
      boolean has_v   = gA.hasView( SubjectFinder.findAllSubject() );
      r.rs.stop();
  
      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExportMain xmlExportMain = new XmlExportMain();
      xmlExportMain.setIncludeComments(true);
      xmlExportMain.writeAllTables(w, "a string");
      
      String xml = w.toString();
      s.stop();
  
      // Reset And Verify
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertDoNotFindGroupByName( s, "i2:a:a" );
      s.stop();
  
      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImportMain xmlImportMain = new XmlImportMain();
      xmlImportMain.setRecordReport(true);
      
      xmlImportMain.processXml(xml);
      s.stop();
  
      // Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
      gA  = assertFindGroupByName( s, "i2:a:a" );
      assertGroupHasAdmin( gA, SubjectFinder.findAllSubject(), has_a );
      assertGroupHasOptin( gA, SubjectFinder.findAllSubject(), has_oi );
      assertGroupHasOptout( gA, SubjectFinder.findAllSubject(), has_oo );
      assertGroupHasRead( gA, SubjectFinder.findAllSubject(), has_r ); // added by default
      assertGroupHasUpdate( gA, SubjectFinder.findAllSubject(), has_u );
      assertGroupHasView( gA, SubjectFinder.findAllSubject(), has_v ); // added by default
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFullExportFullImportFullAccessPrivs()

  /**
   * 
   */
  public void testFullExportFullImportFullGroup() {
    LOG.info("testFullExportFullImportFullGroup");
    try {
      // Populate Registry And Verify
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = assertFindGroupByName( r.rs, "i2:a:a" );
      // For Later Validation
      boolean has_a   = gA.hasAdmin( SubjectFinder.findAllSubject() );
      boolean has_oi  = gA.hasOptin( SubjectFinder.findAllSubject() );
      boolean has_oo  = gA.hasOptout( SubjectFinder.findAllSubject() );
      boolean has_r   = gA.hasRead( SubjectFinder.findAllSubject() );
      boolean has_u   = gA.hasUpdate( SubjectFinder.findAllSubject() );
      boolean has_v   = gA.hasView( SubjectFinder.findAllSubject() );
      Subject val_c   = gA.getCreateSubject();
      Date    val_ct  = gA.getCreateTime();
      String  val_d   = gA.getDescription();
      String  val_de  = gA.getDisplayExtension();
      String  val_dn  = gA.getDisplayName();
      String  val_e   = gA.getExtension();
      String  val_n   = gA.getName();
      String  val_u   = gA.getUuid();
      r.rs.stop();
  
      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExportMain xmlExportMain = new XmlExportMain();
      xmlExportMain.writeAllTables(w, "a string");
      
      String xml = w.toString();
      s.stop();
  
      // Reset And Verify
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertDoNotFindGroupByName( s, "i2:a:a" );
      s.stop();
  
      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImportMain xmlImportMain = new XmlImportMain();
      
      xmlImportMain.setRecordReport(true);

      xmlImportMain.processXml(xml);
      s.stop();
  
      // Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
      gA  = assertFindGroupByName( s, "i2:a:a" );
      assertGroupHasAdmin( gA, SubjectFinder.findAllSubject(), has_a );
      assertGroupHasOptin( gA, SubjectFinder.findAllSubject(), has_oi );
      assertGroupHasOptout( gA, SubjectFinder.findAllSubject(), has_oo );
      assertGroupHasRead( gA, SubjectFinder.findAllSubject(), has_r );
      assertGroupHasUpdate( gA, SubjectFinder.findAllSubject(), has_u );
      assertGroupHasView( gA, SubjectFinder.findAllSubject(), has_v );
      assertGroupCreateSubject( gA, val_c );
      assertGroupCreateTime( gA, val_ct );
      assertGroupDescription( gA, val_d );
      assertGroupDisplayExtension( gA, val_de );
      assertGroupDisplayName( gA, val_dn );
      assertGroupExtension( gA, val_e );
      assertGroupName( gA, val_n );
      assertGroupUuid( gA, val_u );
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFullExportFullImportFullGroup()

  /**
   * 
   */
  public void testFullExportFullImportFullGroupInternational() {
    LOG.info("testFullExportFullImportFullGroupInternational");
    try {
      // Populate Registry And Verify
      R     r   = R.populateRegistry(1, 1, 0);
      GrouperSession grouperSession = GrouperSession.startRootSession();
      Group gA = new GroupSave(grouperSession).assignGroupNameToEdit("tést:àGroup").assignName("tést:àGroup")
        .assignDisplayName("tést:àGroup").assignDescription("tést:àGroup").assignCreateParentStemsIfNotExist(true).save();
      
      // For Later Validation
      boolean has_a   = gA.hasAdmin( SubjectFinder.findAllSubject() );
      boolean has_oi  = gA.hasOptin( SubjectFinder.findAllSubject() );
      boolean has_oo  = gA.hasOptout( SubjectFinder.findAllSubject() );
      boolean has_r   = gA.hasRead( SubjectFinder.findAllSubject() );
      boolean has_u   = gA.hasUpdate( SubjectFinder.findAllSubject() );
      boolean has_v   = gA.hasView( SubjectFinder.findAllSubject() );
      Subject val_c   = gA.getCreateSubject();
      Date    val_ct  = gA.getCreateTime();
      String  val_d   = gA.getDescription();
      String  val_de  = gA.getDisplayExtension();
      String  val_dn  = gA.getDisplayName();
      String  val_e   = gA.getExtension();
      String  val_n   = gA.getName();
      String  val_u   = gA.getUuid();
      r.rs.stop();
  
      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExportMain xmlExportMain = new XmlExportMain();
      xmlExportMain.setIncludeComments(true);
      xmlExportMain.writeAllTables(w, "a string");
      
      String xml = w.toString();
      
      s.stop();
  
      // Reset And Verify
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertDoNotFindGroupByName( s, "tést:àGroup" );
      s.stop();
  
      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImportMain xmlImportMain = new XmlImportMain();
      
      xmlImportMain.setRecordReport(true);

      xmlImportMain.processXml(xml);
      s.stop();
  
      // Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
      gA  = assertFindGroupByName( s, "tést:àGroup" );
      assertGroupHasAdmin( gA, SubjectFinder.findAllSubject(), has_a );
      assertGroupHasOptin( gA, SubjectFinder.findAllSubject(), has_oi );
      assertGroupHasOptout( gA, SubjectFinder.findAllSubject(), has_oo );
      assertGroupHasRead( gA, SubjectFinder.findAllSubject(), has_r );
      assertGroupHasUpdate( gA, SubjectFinder.findAllSubject(), has_u );
      assertGroupHasView( gA, SubjectFinder.findAllSubject(), has_v );
      assertGroupCreateSubject( gA, val_c );
      assertGroupCreateTime( gA, val_ct );
      assertGroupDescription( gA, val_d );
      assertGroupDisplayExtension( gA, val_de );
      assertGroupDisplayName( gA, val_dn );
      assertGroupExtension( gA, val_e );
      assertGroupName( gA, val_n );
      assertGroupUuid( gA, val_u );
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFullExportFullImportFullGroupInternational()

  /**
   * 
   */
  public void testFullExportFullImportFullMemberships() {
    LOG.info("testFullExportFullImportFullMemberships");
    try {
      // Populate Registry And Verify
      R       r     = R.populateRegistry(1, 4, 2);
      Group   gA    = assertFindGroupByName( r.rs, "i2:a:a" );
      Group   gB    = assertFindGroupByName( r.rs, "i2:a:b" );
      Group   gC    = assertFindGroupByName( r.rs, "i2:a:c" );
      Group   gD    = assertFindGroupByName( r.rs, "i2:a:d" );
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      gA.addMember( subjA );
      gB.addMember( subjB );
      gC.addMember( gA.toSubject() );
      gD.addCompositeMember( CompositeType.UNION, gA, gB );
      boolean has_a_a = gA.hasMember(subjA);
      boolean has_a_b = gA.hasMember(subjB);
      boolean has_a_A = gA.hasMember( gA.toSubject() );
      boolean has_a_B = gA.hasMember( gB.toSubject() );
      boolean has_a_C = gA.hasMember( gC.toSubject() );
      boolean has_a_D = gA.hasMember( gD.toSubject() );
      boolean has_b_a = gB.hasMember(subjA);
      boolean has_b_b = gB.hasMember(subjB);
      boolean has_b_A = gB.hasMember( gA.toSubject() );
      boolean has_b_B = gB.hasMember( gB.toSubject() );
      boolean has_b_C = gB.hasMember( gC.toSubject() );
      boolean has_b_D = gB.hasMember( gD.toSubject() );
      boolean has_c_a = gC.hasMember(subjA);
      boolean has_c_b = gC.hasMember(subjB);
      boolean has_c_A = gC.hasMember( gA.toSubject() );
      boolean has_c_B = gC.hasMember( gB.toSubject() );
      boolean has_c_C = gC.hasMember( gC.toSubject() );
      boolean has_c_D = gC.hasMember( gD.toSubject() );
      boolean has_d_a = gD.hasMember(subjA);
      boolean has_d_b = gD.hasMember(subjB);
      boolean has_d_A = gD.hasMember( gA.toSubject() );
      boolean has_d_B = gD.hasMember( gB.toSubject() );
      boolean has_d_C = gD.hasMember( gC.toSubject() );
      boolean has_d_D = gD.hasMember( gD.toSubject() );
      r.rs.stop();
  
      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExportMain xmlExportMain = new XmlExportMain();
      xmlExportMain.writeAllTables(w, "a string");
      
      String xml = w.toString();
      s.stop();
  
      // Reset And Verify
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertDoNotFindGroupByName( s, "i2:a:a" );
      assertDoNotFindGroupByName( s, "i2:a:b" );
      assertDoNotFindGroupByName( s, "i2:a:c" );
      assertDoNotFindGroupByName( s, "i2:a:d" );
      s.stop();
  
      // Add Subjects
      r = R.populateRegistry(0, 0, 2);
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
      r.rs.stop();
  
      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImportMain xmlImportMain = new XmlImportMain();
      
      xmlImportMain.setRecordReport(true);

      xmlImportMain.processXml(xml);
      s.stop();
  
      // Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
      gA  = assertFindGroupByName( s, "i2:a:a" );
      gB  = assertFindGroupByName( s, "i2:a:b" );
      gC  = assertFindGroupByName( s, "i2:a:c" );
      gD  = assertFindGroupByName( s, "i2:a:d" );
      assertGroupHasMember( gA, subjA, has_a_a );
      assertGroupHasMember( gA, subjB, has_a_b );
      assertGroupHasMember( gA, gA.toSubject(), has_a_A );
      assertGroupHasMember( gA, gB.toSubject(), has_a_B );
      assertGroupHasMember( gA, gC.toSubject(), has_a_C );
      assertGroupHasMember( gA, gD.toSubject(), has_a_D );
      assertGroupHasMember( gB, subjA, has_b_a );
      assertGroupHasMember( gB, subjB, has_b_b );
      assertGroupHasMember( gB, gA.toSubject(), has_b_A );
      assertGroupHasMember( gB, gB.toSubject(), has_b_B );
      assertGroupHasMember( gB, gC.toSubject(), has_b_C );
      assertGroupHasMember( gB, gD.toSubject(), has_b_D );
      assertGroupHasMember( gC, subjA, has_c_a );
      assertGroupHasMember( gC, subjB, has_c_b );
      assertGroupHasMember( gC, gA.toSubject(), has_c_A );
      assertGroupHasMember( gC, gB.toSubject(), has_c_B );
      assertGroupHasMember( gC, gC.toSubject(), has_c_C );
      assertGroupHasMember( gC, gD.toSubject(), has_c_D );
      assertGroupHasMember( gD, subjA, has_d_a );
      assertGroupHasMember( gD, subjB, has_d_b );
      assertGroupHasMember( gD, gA.toSubject(), has_d_A );
      assertGroupHasMember( gD, gB.toSubject(), has_d_B );
      assertGroupHasMember( gD, gC.toSubject(), has_d_C );
      assertGroupHasMember( gD, gD.toSubject(), has_d_D );
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFullExportFullImportFullMemberships()

  /**
   * 
   */
  public void testFullExportFullImportFullNamingPrivs() {
    LOG.info("testFullExportFullImportFullNamingPrivs");
    try {
      // Populate Registry And Verify
      R     r   = R.populateRegistry(1, 0, 0);
      Stem  nsA = assertFindStemByName( r.rs, "i2:a" );
      nsA.grantPriv( SubjectFinder.findAllSubject(), NamingPrivilege.CREATE );
      nsA.grantPriv( SubjectFinder.findAllSubject(), NamingPrivilege.STEM );
      boolean has_c = nsA.hasCreate( SubjectFinder.findAllSubject() );
      boolean has_s = nsA.hasStem( SubjectFinder.findAllSubject() );
      r.rs.stop();
  
      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExportMain xmlExportMain = new XmlExportMain();
      xmlExportMain.writeAllTables(w, "a string");
      
      String xml = w.toString();
      s.stop();
  
      // Reset And Verify
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertDoNotFindStemByName( s, "i2:a" );
      s.stop();
  
      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImportMain xmlImportMain = new XmlImportMain();
      
      xmlImportMain.setRecordReport(true);

      xmlImportMain.processXml(xml);
      s.stop();
  
      // Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
      nsA = assertFindStemByName( s, "i2:a" );
      assertStemHasCreate( nsA, SubjectFinder.findAllSubject(), has_c );
      assertStemHasStem( nsA, SubjectFinder.findAllSubject(), has_s );
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFullExportFullImportFullNamingPrivs()

  /**
   * 
   */
  public void testFullExportFullImportFullStem() {
    LOG.info("testFullExportFullImportFullStem");
    try {
      // Populate Registry And Verify
      R     r   = R.populateRegistry(1, 0, 0);
      Stem  nsA = assertFindStemByName( r.rs, "i2:a" );
      // For Later Validation
      boolean has_c   = nsA.hasCreate( SubjectFinder.findAllSubject() );
      boolean has_s   = nsA.hasStem( SubjectFinder.findAllSubject() );
      Subject val_c   = nsA.getCreateSubject();
      Date    val_ct  = nsA.getCreateTime();
      String  val_d   = nsA.getDescription();
      String  val_de  = nsA.getDisplayExtension();
      String  val_dn  = nsA.getDisplayName();
      String  val_e   = nsA.getExtension();
      String  val_n   = nsA.getName();
      String  val_u   = nsA.getUuid();
      r.rs.stop();
  
      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExportMain xmlExportMain = new XmlExportMain();
      xmlExportMain.writeAllTables(w, "a string");
      
      String xml = w.toString();
      s.stop();
  
      // Reset And Verify
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertDoNotFindStemByName( s, "i2:a" );
      s.stop();
  
      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImportMain xmlImportMain = new XmlImportMain();
      
      xmlImportMain.setRecordReport(true);

      xmlImportMain.processXml(xml);
      s.stop();
  
      // Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
      nsA = assertFindStemByName( s, "i2:a" );
      assertStemHasCreate( nsA, SubjectFinder.findAllSubject(), has_c );
      assertStemHasStem( nsA, SubjectFinder.findAllSubject(), has_s );
      assertStemCreateSubject( nsA, val_c );
      assertStemCreateTime( nsA, val_ct );
      assertStemDescription( nsA, val_d );
      assertStemDisplayExtension( nsA, val_de );
      assertStemDisplayName( nsA, val_dn );
      assertStemExtension( nsA, val_e );
      assertStemName( nsA, val_n );
      assertStemUuid( nsA, val_u );
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFullExportFullImportFullStem()

  /**
   * 
   */
  public void testUpdateOkAccessPrivsInAddMode() {
    LOG.info("testUpdateOkAccessPrivsInAddMode");
    try {
      // Populate Registry And Verify
      R       r     = R.populateRegistry(1, 2, 0);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      String  nameA = gA.getName();
      gA.grantPriv( SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN );
      // Make sure no exception is thrown due to gB not existing when updating
      gB.grantPriv( SubjectFinder.findAllSubject(), AccessPrivilege.GROUP_ATTR_READ );
      r.rs.stop();
  
      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExportMain xmlExportMain = new XmlExportMain();
      xmlExportMain.writeAllTables(w, "a string");
      
      String xml = w.toString();
      s.stop();
  
      // Reset 
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
  
      // Install Subjects and partial registry
      r = R.populateRegistry(1, 1, 0);
      gA = assertFindGroupByName(r.rs, nameA, "recreate");
      // Now grant an added priv
      gA.grantPriv( SubjectFinder.findAllSubject(), AccessPrivilege.GROUP_ATTR_READ );
      r.rs.stop();
  
      // Import 
      s                   = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImportMain xmlImportMain = new XmlImportMain();
      
      xmlImportMain.setRecordReport(true);

      xmlImportMain.processXml(xml);
      s.stop();
  
      // Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
      gA  = assertFindGroupByName(s, nameA);
      // Should have
      assertGroupHasOptin( gA, SubjectFinder.findAllSubject(), true );
      // Should still have
      assertGroupHasGroupAttrRead( gA, SubjectFinder.findAllSubject(), true );
      s.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testUpdateOkAccessPrivsInAddMode()

  /**
   * 
   */
  public void testUpdateOkDoNotAddMissingGroups() {
    LOG.info("testUpdateOkDoNotAddMissingGroups");
    try {
      // Export - Setup
      R       r     = R.populateRegistry(1, 2, 0);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      String  nameA = gA.getName();
      String  nameB = gB.getName();
      assertFindGroupByName(r.rs, nameA, "setup");
      assertFindGroupByName(r.rs, nameB, "setup");
      // These are to make sure no exception is thrown due to gB not existing when updating
      gB.addMember( SubjectFinder.findRootSubject() );
      gB.grantPriv( SubjectFinder.findAllSubject(), AccessPrivilege.GROUP_ATTR_READ );
      r.rs.stop();
  
      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExportMain xmlExportMain = new XmlExportMain();
      xmlExportMain.writeAllTables(w, "a string");
      
      String xml = w.toString();
      s.stop();
  
      // Reset
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
  
      // Install Subjects and partial registry
      r = R.populateRegistry(1, 1, 0);
      assertFindGroupByName(r.rs, nameA, "recreate");
      assertDoNotFindGroupByName(r.rs, nameB, "recreate");
      r.rs.stop();
  
      // Update
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImportMain xmlImportMain = new XmlImportMain();
      
      xmlImportMain.setRecordReport(true);

      xmlImportMain.processXml(xml);
      s.stop();
  
      // Import - Verify
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertFindGroupByName(s, nameA, "update");
      assertFindGroupByName(s, nameB, "update");
      s.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testUpdateOkDoNotAddMissingGroups()

  /**
   * 
   */
  public void testUpdateOkDoNotAddMissingStems() {
    LOG.info("testUpdateOkDoNotAddMissingStems");
    try {
      // Export - Setup
      R       r     = R.populateRegistry(2, 0, 0);
      Stem    nsA   = r.getStem("a");
      Stem    nsB   = r.getStem("b");
      String  nameA = nsA.getName();
      String  nameB = nsB.getName();
      assertFindStemByName(r.rs, nameA, "setup");
      assertFindStemByName(r.rs, nameB, "setup");
      r.rs.stop();
  
      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExportMain xmlExportMain = new XmlExportMain();
      xmlExportMain.writeAllTables(w, "a string");
      
      String xml = w.toString();
      s.stop();
  
      // Reset
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
  
      // Install Subjects and partial registry
      r = R.populateRegistry(1, 0, 0);
      assertFindStemByName(r.rs, nameA, "recreate");
      assertDoNotFindStemByName(r.rs, nameB, "recreate");
      r.rs.stop();
  
      // Update
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImportMain xmlImportMain = new XmlImportMain();
      
      xmlImportMain.setRecordReport(true);

      xmlImportMain.processXml(xml);
      s.stop();
  
      // Import - Verify
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertFindStemByName(s, nameA, "update");
      assertFindStemByName(s, nameB, "update");
      s.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testUpdateOkDoNotAddMissingStems()

//  public void testUpdateOkDoNotUpdateGroupAttrs() {
//    LOG.info("testUpdateOkDoNotUpdateGroupAttrs");
//    try {
//      // Export - Setup
//      R       r     = R.populateRegistry(1, 2, 0);
//      Group   gA    = r.getGroup("a", "a");
//      String  nameA = gA.getName();
//      String  orig  = gA.getDescription();
//      gA.setDescription(nameA);
//      gA.store();
//      assertGroupDescription(gA, nameA);
//      r.rs.stop();
//  
//      // Export
//      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
//      Writer          w         = new StringWriter();
//      XmlExporter     exporter  = new XmlExporter(s, new Properties());
//      exporter.export(w);
//      String          xml       = w.toString();
//      s.stop();
//  
//      // Reset
//      RegistryReset.reset();
//  
//      // Install Subjects and partial registry
//      r = R.populateRegistry(1, 1, 0);
//      assertFindGroupByName(r.rs, nameA, "recreate");
//      r.rs.stop();
//  
//      // Update
//      s = GrouperSession.start( SubjectFinder.findRootSubject() );
//      XmlImporter importer  = new XmlImporter(s, new Properties());
//      importer.update( XmlReader.getDocumentFromString(xml) );
//      s.stop();
//  
//      // Import - Verify
//      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
//      gA = assertFindGroupByName(s, nameA, "update");
//      assertGroupDescription(gA, orig);
//      s.stop();
//    }
//    catch (Exception e) {
//      unexpectedException(e);
//    }
//  } // public void testUpdateOkDoNotUpdateGroupAttrs()
//
//  public void testUpdateOkDoNotUpdateStemAttrs() {
//    LOG.info("testUpdateOkDoNotUpdateStemAttrs");
//    try {
//      // Export - Setup
//      R       r     = R.populateRegistry(2, 0, 0);
//      Stem    nsA   = r.getStem("a");
//      String  nameA = nsA.getName();
//      String  orig  = nsA.getDescription();
//      nsA.setDescription(nameA);
//      nsA.store();
//  
//      assertStemDescription(nsA, nameA);
//      r.rs.stop();
//  
//      // Export
//      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
//      Writer          w         = new StringWriter();
//      XmlExporter     exporter  = new XmlExporter(s, new Properties());
//      exporter.export(w);
//      String          xml       = w.toString();
//      s.stop();
//  
//      // Reset
//      RegistryReset.reset();
//  
//      // Install Subjects and partial registry
//      r = R.populateRegistry(1, 0, 0);
//      assertFindStemByName(r.rs, nameA, "recreate");
//      r.rs.stop();
//  
//      // Update
//      s = GrouperSession.start( SubjectFinder.findRootSubject() );
//      Properties  custom  = new Properties();
//      custom.setProperty("import.data.update-attributes", "false");
//      XmlImporter importer  = new XmlImporter(s, custom);
//      importer.update( XmlReader.getDocumentFromString(xml) );
//      s.stop();
//  
//      // Import - Verify
//      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
//      nsA = assertFindStemByName(s, nameA, "update");
//      assertStemDescription(nsA, orig);
//      s.stop();
//    }
//    catch (Exception e) {
//      unexpectedException(e);
//    }
//  } // public void testUpdateOkDoNotUpdateStemAttrs()
//
  /**
   * 
   */
  public void testGroupCreateDescription() {
    LOG.info("testGroupCreateDescription");
    try {
      String val_d = "Description of Group";
      
      // Populate Registry And Verify
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = assertFindGroupByName( r.rs, "i2:a:a" );
      
      // Set description      
      gA.setDescription(val_d);
      gA.store();
      
      // Stop session
      r.rs.stop();
  
      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExportMain xmlExportMain = new XmlExportMain();
      xmlExportMain.writeAllTables(w, "a string");
      
      String xml = w.toString();
      s.stop();
  
      // Reset And Verify
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertDoNotFindGroupByName( s, "i2:a:a" );
      s.stop();
  
      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImportMain xmlImportMain = new XmlImportMain();
      
      xmlImportMain.setRecordReport(true);

      xmlImportMain.processXml(xml);
      s.stop();
  
      // Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
      gA  = assertFindGroupByName( s, "i2:a:a" );     
      assertGroupDescription( gA, val_d );      
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGroupCreateDescription()

  /**
   * 
   */
  public void testGroupUpdateDescription() {
    LOG.info("testGroupUpdateDescription");
    try {
      String val_d = "Description of Group";
      
      // Populate Registry And Verify
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = assertFindGroupByName( r.rs, "i2:a:a" );
      
      // Stop session
      r.rs.stop();
      
      // Export
      GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
      Writer          w         = new StringWriter();
      XmlExportMain xmlExportMain = new XmlExportMain();
      xmlExportMain.writeAllTables(w, "a string");
      
      String xml = w.toString();
      s.stop();

      // Reset And Verify
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      assertDoNotFindGroupByName(s, "i2:a:a");
      s.stop();

      // Import 
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      XmlImportMain xmlImportMain = new XmlImportMain();
      
      xmlImportMain.setRecordReport(true);

      xmlImportMain.processXml(xml);
      s.stop();

      // Verify that there is no description
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      gA = assertFindGroupByName(s, "i2:a:a");
      assertEquals(GrouperConfig.EMPTY_STRING, gA.getDescription());
      s.stop();

      // Set description
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      gA = assertFindGroupByName(s, "i2:a:a");
      gA.setDescription(val_d);
      gA.store();
      s.stop();

      // Export
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      w         = new StringWriter();
      xmlExportMain = new XmlExportMain();
      xmlExportMain.writeAllTables(w, "a string");
      
      xml = w.toString();
      s.stop();

      // Import 
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      xmlImportMain = new XmlImportMain();
      
      xmlImportMain.setRecordReport(true);

      xmlImportMain.processXml(xml);
      s.stop();

      // Verify description
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      gA = assertFindGroupByName(s, "i2:a:a");
      assertGroupDescription(gA, val_d);
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGroupUpdateDescription()
  
  /**
   * 
   */
  public void testStemCreateDescription() {
    LOG.info("testStemCreateDescription");
    try {
      String val_d = "Description of Stem";

      // Populate Registry And Verify
      R r = R.populateRegistry(2, 0, 0);
      Stem nsA = assertFindStemByName(r.rs, "i2:a");

      // Set description     
      nsA.setDescription(val_d);
      nsA.store();

      // Stop session
      r.rs.stop();

      // Export
      GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
      Writer          w         = new StringWriter();
      XmlExportMain xmlExportMain = new XmlExportMain();
      xmlExportMain.writeAllTables(w, "a string");
      
      String xml = w.toString();
      s.stop();

      // Reset And Verify
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      assertDoNotFindStemByName(s, "i2:a");
      s.stop();

      // Import 
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      XmlImportMain xmlImportMain = new XmlImportMain();
      
      xmlImportMain.setRecordReport(true);

      xmlImportMain.processXml(xml);
      s.stop();

      // Verify
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      nsA = assertFindStemByName(s, "i2:a");
      assertStemDescription(nsA, val_d);
      s.stop();
    } catch (Exception e) {
      T.e(e);
    }
  } // public void testStemCreateDescription()

  /**
   * 
   */
  public void testStemUpdateDescription() {
    LOG.info("testStemUpdateDescription");
    try {
      String val_d = "Description of Stem";

      // Populate Registry And Verify
      R r = R.populateRegistry(2, 0, 0);
      Stem nsA = assertFindStemByName(r.rs, "i2:a");

      // Stop session
      r.rs.stop();

      // Export
      GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
      Writer          w         = new StringWriter();
      XmlExportMain xmlExportMain = new XmlExportMain();
      xmlExportMain.writeAllTables(w, "a string");
      
      String xml = w.toString();

      s.stop();

      // Reset And Verify
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      assertDoNotFindStemByName(s, "i2:a");
      s.stop();

      // Import 
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      XmlImportMain xmlImportMain = new XmlImportMain();
      
      xmlImportMain.setRecordReport(true);

      xmlImportMain.processXml(xml);
      s.stop();

      // Verify that there is no description
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      nsA = assertFindStemByName(s, "i2:a");
      assertEquals(GrouperConfig.EMPTY_STRING, nsA.getDescription());
      s.stop();

      // Set description
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      nsA = assertFindStemByName(s, "i2:a");
      nsA.setDescription(val_d);
      nsA.store();
      s.stop();

      // Export
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      w         = new StringWriter();
      xmlExportMain = new XmlExportMain();
      xmlExportMain.writeAllTables(w, "a string");
      
      xml = w.toString();
      s.stop();

      // Import 
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      xmlImportMain = new XmlImportMain();
      
      xmlImportMain.setRecordReport(true);

      xmlImportMain.processXml(xml);
      s.stop();

      // Verify
      s = GrouperSession.start(SubjectFinder.findRootSubject());
      nsA = assertFindStemByName(s, "i2:a");
      assertStemDescription(nsA, val_d);
      s.stop();
    } catch (Exception e) {
      T.e(e);
    }
  } // public void testStemUpdateDescription()


} // public class TestXml0

