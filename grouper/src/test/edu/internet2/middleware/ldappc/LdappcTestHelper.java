/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
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

package edu.internet2.middleware.ldappc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;

import junit.framework.Assert;

import org.apache.directory.shared.ldap.entry.Entry;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.entry.Value;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceListener;
import org.openspml.v2.msg.Marshallable;
import org.openspml.v2.msg.XMLMarshaller;
import org.openspml.v2.msg.XMLUnmarshaller;
import org.openspml.v2.util.xml.UnknownSpml2TypeException;
import org.slf4j.Logger;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.util.IgnoreRequestIDDifferenceListener;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.bean.LdapResult;
import edu.vt.middleware.ldap.ldif.LdifResult;

public class LdappcTestHelper {

  private static final Logger LOG = GrouperUtil.getLogger(LdappcTestHelper.class);

  public static Map<LdapDN, Entry> buildLdapEntryMap(List<LdifEntry> ldifEntries) throws NamingException {

    Map<LdapDN, Entry> map = new HashMap<LdapDN, Entry>();

    for (LdifEntry ldifEntry : ldifEntries) {
      Entry entry = ldifEntry.getEntry();
      if (entry.contains("objectclass", "top")) {
        entry.remove("objectclass", "top");
      }
      map.put(ldifEntry.getDn(), entry);
    }

    return map;
  }

  public static void deleteChildren(String baseDn, Ldap ldap) throws NamingException {
    List<String> toDelete = getChildDNs(baseDn, ldap);
    for (String dn : toDelete) {
      LOG.info("delete '{}'", dn);
      ldap.delete(dn);
    }
  }

  /**
   * Return a list of child DNs under the given DN, in (reverse) order suitable for
   * deletion.
   * 
   * @param name
   *          the top level DN
   * @return
   * @throws NamingException
   */
  public static List<String> getChildDNs(String name, Ldap ldap) throws NamingException {
    ArrayList<String> tree = new ArrayList<String>();

    Iterator<SearchResult> searchResults = ldap.searchAttributes(name, new BasicAttributes("objectclass", null),
        new String[] {});
    LdapResult ldapResult = new LdapResult(searchResults);
    for (LdapEntry ldapEntry : ldapResult.getEntries()) {
      tree.addAll(getChildDNs(ldapEntry.getDn(), ldap));
      tree.add(ldapEntry.getDn());
    }

    return tree;
  }

  /**
   * Return an LDIF representation of the entire DIT.
   * 
   * @return
   * @throws NamingException
   */
  public static String getCurrentLdif(String baseDn, Ldap ldap) throws NamingException {

    StringBuffer ldif = new StringBuffer();

    List<String> currentDns = getChildDNs(baseDn, ldap);

    for (String currentDn : currentDns) {
      Attributes attributes = ldap.getAttributes(currentDn);
      LdapEntry ldapEntry = new LdapEntry();
      ldapEntry.setDn(currentDn);
      ldapEntry.setLdapAttributes(new LdapAttributes(attributes));
      LdifResult ldifResult = new LdifResult(ldapEntry);
      ldif.append(ldifResult.toLdif());
    }

    LOG.debug("current ldif {}\n{}", ldap.getLdapConfig().getLdapUrl(), ldif.toString());
    return ldif.toString();
  }

  public static void loadLdif(File file, Ldap ldap) throws NamingException {

    LdifReader ldifReader = new LdifReader(file);
    for (LdifEntry entry : ldifReader) {
      Attributes attributes = new BasicAttributes(true);
      for (EntryAttribute entryAttribute : entry.getEntry()) {
        BasicAttribute attribute = new BasicAttribute(entryAttribute.getId());
        Iterator<Value<?>> values = entryAttribute.getAll();
        while (values.hasNext()) {
          attribute.add(values.next().get());
        }
        attributes.put(attribute);
      }
      LOG.debug("creating '" + entry.getDn().toString() + " " + attributes);
      ldap.create(entry.getDn().toString(), attributes);
    }
  }

  public static String readFile(File file) {

    StringBuffer buffer = new StringBuffer();

    try {
      BufferedReader in = new BufferedReader(new FileReader(file));
      String str;
      while ((str = in.readLine()) != null) {
        buffer.append(str);
      }
      in.close();
    } catch (IOException e) {
      Assert.fail("An error occurred : " + e.getMessage());
    }
    return buffer.toString();
  }

  public static Object readSpml(XMLUnmarshaller u, File file) {
    try {
      String xml = readFile(file);
      return u.unmarshall(xml);
    } catch (UnknownSpml2TypeException e) {
      e.printStackTrace();
      Assert.fail("An error occurred : " + e.getMessage());
    }
    return null;
  }

  public static void verifyLdif(File correctLdifFile, File currentLdifFile) throws NamingException {
    LdifReader reader = new LdifReader();

    Map<LdapDN, Entry> correctMap = buildLdapEntryMap(reader.parseLdifFile(correctLdifFile.getAbsolutePath()));
    Map<LdapDN, Entry> currentMap = buildLdapEntryMap(reader.parseLdifFile(currentLdifFile.getAbsolutePath()));

    verifyLdif(correctMap, currentMap);
  }

  public static void verifyLdif(File correctLdifFile, String currentLdif) throws NamingException {
    LdifReader reader = new LdifReader();

    Map<LdapDN, Entry> correctMap = buildLdapEntryMap(reader.parseLdifFile(correctLdifFile.getAbsolutePath()));
    Map<LdapDN, Entry> currentMap = buildLdapEntryMap(reader.parseLdif(currentLdif));

    verifyLdif(correctMap, currentMap);
  }

  public static void verifyLdif(Map<LdapDN, Entry> correctMap, Map<LdapDN, Entry> currentMap) {
    for (LdapDN correctDn : correctMap.keySet()) {
      Assert.assertEquals("correct ", correctMap.get(correctDn), currentMap.get(correctDn));
    }
    for (LdapDN currentDn : currentMap.keySet()) {
      Assert.assertEquals("current", correctMap.get(currentDn), currentMap.get(currentDn));
    }
  }

  public static Marshallable verifySpml(XMLMarshaller m, XMLUnmarshaller u, Marshallable testObject, File correctXMLFile) {
    return verifySpml(m, u, testObject, correctXMLFile, false);
  }

  public static Marshallable verifySpml(XMLMarshaller m, XMLUnmarshaller u, Marshallable testObject,
      File correctXMLFile, boolean testEquality) {

    try {
      String testXML = testObject.toXML(m);

      Marshallable unmarshalledObject = u.unmarshall(testXML);

      String unmarshalledTestXML = unmarshalledObject.toXML(m);

      String correctXML = readFile(correctXMLFile);

      Marshallable unmarshalledFromCorrectXMLFile = u.unmarshall(correctXML);

      if (LOG.isDebugEnabled()) {
        LOG.debug("current:\n{}", testXML);
        LOG.debug("unmarshalled:\n{}", unmarshalledTestXML);
        LOG.debug("correct:\n{}", correctXML);
      }

      // test objects
      if (testEquality) {
        Assert.assertEquals(testObject, unmarshalledObject);
        Assert.assertEquals(unmarshalledFromCorrectXMLFile, testObject);
      }

      // TODO test marshalling and unmarshalling objects
      // OCEtoMarshallableAdapter does not have an equals() method

      // test marshalling and unmarshalling xml
      DetailedDiff marshallingDiff = new DetailedDiff(new Diff(testXML, unmarshalledTestXML));
      Assert.assertTrue(marshallingDiff.identical());

      // ignore requestID, must test similar not identical
      DifferenceListener ignoreRequestID = new IgnoreRequestIDDifferenceListener();

      // test testXML against correctXML
      Diff correctDiff = new Diff(new FileReader(correctXMLFile), new StringReader(testXML));
      correctDiff.overrideDifferenceListener(ignoreRequestID);
      DetailedDiff correctDetailedDiff = new DetailedDiff(correctDiff);
      if (!correctDetailedDiff.getAllDifferences().isEmpty()) {
        LOG.debug("differences '{}'", correctDetailedDiff.getAllDifferences());
        LOG.debug("diff '{}'", correctDetailedDiff.toString());
      }
      Assert.assertTrue(correctDetailedDiff.getAllDifferences().isEmpty());
      Assert.assertTrue(correctDetailedDiff.similar());

      // test unmarshalledXML against correctXML
      Diff unmarshalledDiff = new Diff(new FileReader(correctXMLFile), new StringReader(unmarshalledTestXML));
      unmarshalledDiff.overrideDifferenceListener(ignoreRequestID);
      DetailedDiff unmarshalledDetailedDiff = new DetailedDiff(unmarshalledDiff);
      if (!unmarshalledDetailedDiff.getAllDifferences().isEmpty()) {
        LOG.debug("differences '{}'", unmarshalledDetailedDiff.getAllDifferences());
        LOG.debug("diff '{}'", unmarshalledDetailedDiff.toString());
      }
      Assert.assertTrue(unmarshalledDetailedDiff.getAllDifferences().isEmpty());
      Assert.assertTrue(unmarshalledDetailedDiff.similar());

      return unmarshalledObject;

    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("An error occurred : " + e.getMessage());
      return null;
    }
  }
}
