/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package edu.internet2.middleware.ldappc.spml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.openspml.v2.msg.Marshallable;
import org.openspml.v2.util.Spml2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.shibboleth.attributeDefinition.LdapDnPSOIdentifierAttributeDefinition;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.SPMLDataConnector;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.BaseProvisioningTest;
import edu.internet2.middleware.ldappc.LdappcTestHelper;
import edu.internet2.middleware.ldappc.LdappcConfig.GroupDNStructure;
import edu.internet2.middleware.ldappc.spml.definitions.PSODefinition;
import edu.internet2.middleware.ldappc.spml.definitions.TargetDefinition;
import edu.internet2.middleware.ldappc.spml.provider.LdapTargetProvider;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethAttributeResolver;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.pool.LdapPool;

public abstract class BasePSPProvisioningTest extends BaseProvisioningTest {

  /** logger */
  protected static final Logger LOG = LoggerFactory.getLogger(BasePSPProvisioningTest.class);

  /** Test SPML request ID. */
  public static final String REQUESTID_TEST = "REQUESTID_TEST";

  /** The Spring id of the Shibboleth Attribute Resolver. */
  public static final String SPRING_ID_ATTRIBUTE_RESOLVER = "grouper.AttributeResolver";

  /** The Spring id of the SPMLDataConnector. */
  public static final String SPRING_ID_SPML_DC = "SpmlDataConnector";

  /** The ldap pool. */
  protected LdapPool<Ldap> ldapPool;

  /** The PSP. */
  protected PSP psp;

  protected Group groupA;

  protected Group groupB;

  protected Stem edu;

  public BasePSPProvisioningTest(String name, String confDir) {
    super(name, confDir);
  }

  public void setUp() {
    super.setUp();

    try {
      PSPOptions pspOptions = new PSPOptions(null);
      pspOptions.setConfDir(GrouperUtil.fileFromResourceName(confDir).getAbsolutePath());
      psp = PSP.getPSP(pspOptions);

      TargetDefinition ldapTargetDef = psp.getTargetDefinitions().get(
          "ldap");
      LdapTargetProvider ldapTargetProvider = (LdapTargetProvider) ldapTargetDef
          .getProvider();

      ldapPool = ldapTargetProvider.getLdapPool();
      ldap = ldapPool.checkOut();

      LdappcTestHelper.deleteChildren(base, ldap);

    } catch (Exception e) {
      e.printStackTrace();
      fail("An error occurred : " + e);
    }

    // setup grouper
    GrouperSession.startRootSession();
    Stem root = StemHelper.findRootStem(GrouperSession.staticGrouperSession());
    edu = StemHelper.addChildStem(root, "edu", "education");

    groupA = StemHelper.addChildGroup(edu, "groupA", "Group A");
    groupA.addMember(SubjectTestHelper.SUBJ0);

    groupB = StemHelper.addChildGroup(edu, "groupB", "Group B");
    groupB.addMember(SubjectTestHelper.SUBJ1);
    groupB.setDescription("descriptionB");
    groupB.store();
  }

  public void tearDown() {

    try {
      LdappcTestHelper.deleteChildren(base, ldap);
      LOG.debug("closing ldap connection");
      ldap.close();
      LOG.debug("checking in ldap");
      ldapPool.checkIn(ldap);
      LOG.debug("closing ldap pool");
      ldapPool.close();
      LOG.debug("finished");

    } catch (Exception e) {
      fail("An error occurred : " + e);
      e.printStackTrace();
    }

    super.tearDown();
  }

  public void verifyLdif(String pathToCorrectFile) {
    super.verifyLdif(pathToCorrectFile, getAllReferenceNames());
  }

  public void verifyLdif(String pathToCorrectFile, String base) {
    super.verifyLdif(pathToCorrectFile, getAllReferenceNames(), base);
  }

  public void verifySpml(Marshallable testObject, String correctXMLFileName) {
    LdappcTestHelper.verifySpml(psp.getXMLMarshaller(), psp
        .getXmlUnmarshaller(), testObject, LdappcTestHelper.getFile(correctXMLFileName), false, propertiesFile);
  }

  public ShibbolethAttributeResolver getAttributeResolver() {
    return (ShibbolethAttributeResolver) psp.getApplicationContext().getBean(SPRING_ID_ATTRIBUTE_RESOLVER);
  }

  public SPMLDataConnector getSpmlDataConnector() {
    return (SPMLDataConnector) this.getAttributeResolver().getServiceContext().getBean(SPRING_ID_SPML_DC);
  }

  public List<String> getAllReferenceNames() {
    ArrayList<String> referenceNames = new ArrayList<String>();
    for (TargetDefinition targetDefinitions : psp.getTargetDefinitions().values()) {
      for (PSODefinition psoDefinition : targetDefinitions.getPsoDefinitions()) {
        referenceNames.addAll(psoDefinition.getReferenceNames());
      }
    }
    return referenceNames;
  }

  public void makeGroupDNStructureFlat() {
    ShibbolethAttributeResolver AR = this.getAttributeResolver();
    LdapDnPSOIdentifierAttributeDefinition lad = (LdapDnPSOIdentifierAttributeDefinition) AR.getAttributeDefinitions()
        .get("group-dn");
    lad.setStructure(GroupDNStructure.flat);
  }

  public void setUpCourseTest() {
    Stem courses = this.edu.addChildStem("courses", "Courses");

    Stem spring = courses.addChildStem("spring", "Spring");
    Stem fall = courses.addChildStem("fall", "Fall");

    Group springCourseA = spring.addChildGroup("courseA", "Course A");
    springCourseA.addMember(SubjectTestHelper.SUBJ0);
    springCourseA.addMember(SubjectTestHelper.SUBJ1);

    Group springCourseB = spring.addChildGroup("courseB", "Course B");
    springCourseB.addMember(SubjectTestHelper.SUBJ1);

    Group fallCourseA = fall.addChildGroup("courseA", "Course A");
    fallCourseA.addMember(SubjectTestHelper.SUBJ0);
    fallCourseA.addMember(SubjectTestHelper.SUBJ1);

    Group fallCourseB = fall.addChildGroup("courseB", "Course B");
    fallCourseB.addMember(SubjectTestHelper.SUBJ1);
  }

  protected void verifySpmlWrite(Marshallable testObject, String correctXMLFileName) {

    File file = GrouperUtil.fileFromResourceName(correctXMLFileName);
    if (file != null) {
      throw new RuntimeException("File already exists " + correctXMLFileName);
    }

    // TODO make configurable, probably an env variable
    String PARENT = System.getProperty("user.dir") + "/src/test/resources";

    String newFilePath = PARENT + File.separator + correctXMLFileName;
    try {
      String xml = testObject.toXML(psp.getXMLMarshaller());

      Properties props = new Properties();
      props.load(new FileInputStream(propertiesFile));

      xml = xml.replace(props.getProperty("base"), "${base}");
      xml = xml.replace("<dsml:value>" + props.getProperty("groupObjectClass") + "</dsml:value>", "<dsml:value>${groupObjectClass}</dsml:value>");
      xml = xml.replaceAll("requestID='2.*'", "requestID='REQUEST_ID'");

      LOG.debug("writing new corrext XML file '{}'", newFilePath);
      BufferedWriter out = new BufferedWriter(new FileWriter(newFilePath));
      out.write(xml);
      out.close();

    } catch (Spml2Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    verifySpml(testObject, correctXMLFileName);
  }

}
