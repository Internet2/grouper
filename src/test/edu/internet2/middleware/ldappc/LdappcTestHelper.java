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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapContext;

import junit.framework.Assert;

import org.apache.directory.shared.ldap.entry.Entry;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.entry.Modification;
import org.apache.directory.shared.ldap.entry.ModificationOperation;
import org.apache.directory.shared.ldap.entry.Value;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.apache.directory.shared.ldap.util.AttributeUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceListener;
import org.opensaml.util.resource.PropertyReplacementResourceFilter;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.util.DatatypeHelper;
import org.openspml.v2.msg.Marshallable;
import org.openspml.v2.msg.XMLMarshaller;
import org.openspml.v2.msg.XMLUnmarshaller;
import org.openspml.v2.util.xml.UnknownSpml2TypeException;
import org.slf4j.Logger;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.util.IgnoreRequestIDDifferenceListener;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.vt.middleware.ldap.Ldap;

public class LdappcTestHelper {

  private static final Logger LOG = GrouperUtil.getLogger(LdappcTestHelper.class);

  /**
   * Rewrite the given string containing macros of the form ${key} with the properties
   * from the given property file.
   * 
   * @param ldif
   * @param propertiesFile
   * @return
   * @throws IOException
   * @throws ResourceException
   */
  public static String applyFilter(String ldif, File propertiesFile) throws IOException,
      ResourceException {
    if (propertiesFile == null) {
      return ldif;
    }

    PropertyReplacementResourceFilter filter = new PropertyReplacementResourceFilter(
        propertiesFile);
    return DatatypeHelper.inputstreamToString(filter
        .applyFilter(new ByteArrayInputStream(ldif.getBytes())), null);
  }

  /**
   * Get a map with keys objectclass names and values the names of the attributes that are
   * present for each object of the given objectclass.
   * 
   * @param ldif
   * @return
   * @throws NamingException
   */
  public static Map<String, Collection<String>> buildObjectlassAttributeMap(
      Collection<LdifEntry> ldifEntries) throws NamingException {
    Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();

    for (LdifEntry ldifEntry : ldifEntries) {
      Set<String> objectclasses = new HashSet<String>();
      Set<String> attributeIds = new HashSet<String>();

      if (!ldifEntry.isEntry()) {
        LOG.trace("Unable to parse LdifEntry as an Entry {}", ldifEntry);
        return null;
      }
      Entry entry = ldifEntry.getEntry();

      Iterator<EntryAttribute> iterator = entry.iterator();
      while (iterator.hasNext()) {
        EntryAttribute entryAttribute = iterator.next();
        String entryAttributeId = entryAttribute.getId();
        if (entryAttributeId.equalsIgnoreCase("objectclass")) {
          Iterator<Value<?>> values = entryAttribute.getAll();
          while (values.hasNext()) {
            Value<?> value = values.next();
            if (value.getString().equals("top")) {
              continue;
            }
            objectclasses.add(value.getString());
          }
        }
        attributeIds.add(entryAttributeId);
      }
      for (String objectclass : objectclasses) {
        map.put(objectclass, attributeIds);
      }
    }
    return map;
  }

  /**
   * see {@link #buildObjectlassAttributeMap(Collection)}
   * 
   * @param ldif
   * @return
   * @throws NamingException
   */
  public static Map<String, Collection<String>> buildObjectlassAttributeMap(String ldif)
      throws NamingException {
    LdifReader reader = new LdifReader();
    return buildObjectlassAttributeMap(reader.parseLdif(ldif));
  }

  /**
   * Destroy everything under the given base.
   * 
   * @param baseDn
   * @param ldap
   * @throws NamingException
   */
  public static void deleteChildren(String baseDn, Ldap ldap) throws NamingException {
    List<String> toDelete = LdapUtil.getChildDNs(baseDn, ldap);
    for (String dn : toDelete) {
      LOG.info("delete '{}'", dn);
      ldap.delete(LdapUtil.escapeForwardSlash(dn));
    }
  }

  /**
   * Destroy everything under the given base.
   * 
   * @param base
   * @param ldapContext
   * @throws Exception
   */
  public static void deleteChildren(String base, LdapContext ldapContext)
      throws Exception {
    List<String> toDelete = LdapUtil.getChildDNs(base, ldapContext);
    for (String dn : toDelete) {
      LOG.info("delete " + dn);
      ldapContext.destroySubcontext(dn);
    }
  }

  /**
   * Return an LDIF representation of the entire DIT.
   * 
   * @return
   * @throws NamingException
   */
  public static String getCurrentLdif(String baseDn, Ldap ldap) throws NamingException {
    return getCurrentLdif(baseDn, null, ldap);
  }

  /**
   * see {@link #getCurrentLdif(String, String[], LdapContext)}
   * 
   * @param baseDn
   * @param ldapContext
   * @return
   * @throws NamingException
   */
  public static String getCurrentLdif(String baseDn, LdapContext ldapContext)
      throws NamingException {
    return getCurrentLdif(baseDn, null, ldapContext);
  }

  /**
   * Return an LDIF representation of the entire DIT.
   * 
   * @return
   * @throws NamingException
   */
  public static String getCurrentLdif(String baseDn, String[] attrIds,
      LdapContext ldapContext) throws NamingException {

    StringBuffer ldif = new StringBuffer();

    List<String> currentDns = LdapUtil.getChildDNs(baseDn, ldapContext);

    for (String currentDn : currentDns) {
      ldif.append("dn: " + currentDn + "\n");
      Attributes attributes = ldapContext.getAttributes(currentDn, attrIds);
      ldif.append(LdapUtil.getLdif(attributes));
      ldif.append("\n");
    }

    return ldif.toString();
  }

  /**
   * Return an LDIF representation of the entire DIT.
   * 
   * @return
   * @throws NamingException
   */
  public static String getCurrentLdif(String baseDn, String[] attrIds, Ldap ldap)
      throws NamingException {

    StringBuffer ldif = new StringBuffer();

    List<String> currentDns = LdapUtil.getChildDNs(baseDn, ldap);

    for (String currentDn : currentDns) {
      ldif.append("dn: " + currentDn + "\n");
      Attributes attributes = LdapUtil.searchAttributes(ldap, LdapUtil
          .escapeForwardSlash(currentDn), attrIds);
      ldif.append(LdapUtil.getLdif(attributes));
      ldif.append("\n");
    }

    return ldif.toString();
  }

  /**
   * Return the file from the given object's classloader.
   * 
   * @param object
   *          the parent
   * @param fileName
   *          the file name
   * @return the File
   */
  public static File getFile(Object object, String fileName) {
    try {
      URL url = object.getClass().getResource(fileName);
      if (url == null) {
        throw new LdappcException("File not found : " + fileName);
      }
      return new File(url.toURI());
    } catch (URISyntaxException e) {
      e.printStackTrace();
      throw new LdappcException("An error occurred : " + e.getMessage());
    }
  }

  /**
   * Create entries read from the given ldif file.
   * 
   * @param file
   * @param ldap
   * @throws NamingException
   */
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

  /**
   * Create entries read from the given ldif file.
   * 
   * @param file
   * @param ldap
   * @throws NamingException
   */
  public static void loadLdif(File file, LdapContext ldapContext) throws Exception {

    loadLdif(file, null, ldapContext);
  }

  /**
   * Create entries read from the given ldif file after replacing macros.
   * 
   * @param ldifFile
   * @param replacementPropertiesFile
   * @param ldapContext
   * @throws Exception
   */
  public static void loadLdif(File ldifFile, File replacementPropertiesFile,
      LdapContext ldapContext) throws Exception {
    loadLdif(new FileInputStream(ldifFile), replacementPropertiesFile, ldapContext);
  }

  public static void loadLdif(File ldifFile, File replacementPropertiesFile, Ldap ldap)
      throws Exception {
    loadLdif(new FileInputStream(ldifFile), replacementPropertiesFile, ldap);
  }

  public static void loadLdif(String ldif, File replacementPropertiesFile,
      LdapContext ldapContext) throws Exception {
    loadLdif(new ByteArrayInputStream(ldif.getBytes()), replacementPropertiesFile,
        ldapContext);
  }

  public static void loadLdif(String ldif, File replacementPropertiesFile, Ldap ldap)
      throws Exception {
    loadLdif(new ByteArrayInputStream(ldif.getBytes()), replacementPropertiesFile, ldap);
  }

  public static void loadLdif(InputStream ldif, File replacementPropertiesFile,
      LdapContext ldapContext) throws Exception {

    LdifReader ldifReader = null;
    if (replacementPropertiesFile != null) {
      PropertyReplacementResourceFilter prf = new PropertyReplacementResourceFilter(
          replacementPropertiesFile);
      ldifReader = new LdifReader(prf.applyFilter(ldif));
    } else {
      ldifReader = new LdifReader(ldif);
    }

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
      ldapContext.createSubcontext(entry.getDn().toString(), attributes);
    }
  }

  public static void loadLdif(InputStream ldif, File replacementPropertiesFile, Ldap ldap)
      throws Exception {

    LdifReader ldifReader = null;
    if (replacementPropertiesFile != null) {
      PropertyReplacementResourceFilter prf = new PropertyReplacementResourceFilter(
          replacementPropertiesFile);
      ldifReader = new LdifReader(prf.applyFilter(ldif));
    } else {
      ldifReader = new LdifReader(ldif);
    }

    for (LdifEntry entry : ldifReader) {
      Attributes attributes = new BasicAttributes(true);
      if (entry.isChangeAdd()) {
        for (EntryAttribute entryAttribute : entry.getEntry()) {
          BasicAttribute attribute = new BasicAttribute(entryAttribute.getId());
          Iterator<Value<?>> values = entryAttribute.getAll();
          while (values.hasNext()) {
            attribute.add(values.next().get());
          }
          attributes.put(attribute);
        }
        LOG.debug("creating '" + entry.getDn().toString() + " " + attributes);
        ldap.create(LdapUtil.escapeForwardSlash(entry.getDn().toString()), attributes);
      } else if (entry.isChangeModify()) {
        // nice, ApacheDS really makes this easy, maybe 0 == 0 next time.
        List<ModificationItem> mods = new ArrayList<ModificationItem>();
        for (Modification modification : entry.getModificationItems()) {
          if (modification.getOperation().equals(ModificationOperation.ADD_ATTRIBUTE)) {
            mods.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, AttributeUtils
                .toAttribute(modification.getAttribute())));
          } else if (modification.getOperation().equals(
              ModificationOperation.REMOVE_ATTRIBUTE)) {
            mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, AttributeUtils
                .toAttribute(modification.getAttribute())));
          } else if (modification.getOperation().equals(
              ModificationOperation.REPLACE_ATTRIBUTE)) {
            mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, AttributeUtils
                .toAttribute(modification.getAttribute())));
          }
        }
        LOG.debug("modifying '" + entry.getDn().toString() + " " + mods);
        ldap.modifyAttributes(LdapUtil.escapeForwardSlash(entry.getDn().toString()), mods
            .toArray(new ModificationItem[] {}));
      } else {
        throw new RuntimeException("Unhandled entry type : " + entry.getChangeType());
      }
    }
  }

  /**
   * Normalize values as DNs for every attribute of the given Entry which matches a given
   * attribute name. Probably this method should use the ApacheDS Normalization.
   * 
   * @param entry
   * @param attributeNames
   * @throws NamingException
   */
  public static void normalizeDNValues(Entry entry, Collection<String> dnAttributeNames)
      throws NamingException {
    Iterator<EntryAttribute> iterator = entry.iterator();
    while (iterator.hasNext()) {
      Set<String> toAdd = new HashSet<String>();
      Set<String> toRemove = new HashSet<String>();
      EntryAttribute entryAttribute = iterator.next();
      if (dnAttributeNames.contains(entryAttribute.getId())) {
        Iterator<Value<?>> valueIterator = entryAttribute.getAll();
        while (valueIterator.hasNext()) {
          Value<?> value = valueIterator.next();
          String oldValue = value.getString();
          String newValue = new LdapDN(value.get().toString()).toNormName();
          if (!oldValue.equals(newValue)) {
            toRemove.add(value.getString());
            toAdd.add(new LdapDN(value.get().toString()).toNormName());
          }
        }
      }
      if (!toAdd.isEmpty()) {
        entryAttribute.add(toAdd.toArray(new String[] {}));
      }
      if (!toRemove.isEmpty()) {
        entryAttribute.remove(toRemove.toArray(new String[] {}));
      }
    }
  }

  /**
   * see {@link #normalizeDNValues(Entry, Collection)}
   * 
   * @param ldifEntries
   * @param dnAttributeNames
   * @throws NamingException
   */
  public static void normalizeDNValues(Collection<LdifEntry> ldifEntries,
      Collection<String> dnAttributeNames) throws NamingException {
    for (LdifEntry ldifEntry : ldifEntries) {
      if (ldifEntry.isEntry()) {
        normalizeDNValues(ldifEntry.getEntry(), dnAttributeNames);
      }
    }
  }

  /**
   * Remove from the entry any attribute which is not contained in the given collection of
   * attribute names.
   * 
   * @param entry
   * @param attributeNamesToKeep
   * @throws NamingException
   */
  public static void purgeAttributes(Entry entry, Collection<String> attributeNamesToKeep)
      throws NamingException {
    if (attributeNamesToKeep == null) {
      return;
    }

    Set<String> attrNames = new HashSet<String>();
    for (String attributeName : attributeNamesToKeep) {
      attrNames.add(attributeName.toLowerCase());
    }

    List<EntryAttribute> entryAttributesToRemove = new ArrayList<EntryAttribute>();
    Iterator<EntryAttribute> iterator = entry.iterator();
    while (iterator.hasNext()) {
      EntryAttribute entryAttribute = iterator.next();
      if (!attrNames.contains(entryAttribute.getId().toLowerCase())) {
        entryAttributesToRemove.add(entryAttribute);
      }
    }
    for (EntryAttribute entryAttributeToRemove : entryAttributesToRemove) {
      entry.remove(entryAttributeToRemove);
    }
  }

  /**
   * Remove attributes from the entry which are not in the supplied map.
   * 
   * see {@link #purgeAttributes(Entry, Collection)}.
   * 
   * see {@link #buildObjectlassAttributeMap(BufferedReader)}
   * 
   * @param ldifEntries
   * @param objectclassAttributeMap
   * @throws NamingException
   */
  public static void purgeAttributes(Collection<LdifEntry> ldifEntries,
      Map<String, Collection<String>> objectclassAttributeMap) throws NamingException {
    if (objectclassAttributeMap == null) {
      return;
    }
    for (LdifEntry ldifEntry : ldifEntries) {
      if (ldifEntry.isEntry()) {
        Set<String> attributeNamesToKeep = new HashSet<String>();
        for (String objectclass : objectclassAttributeMap.keySet()) {
          if (ldifEntry.getEntry().hasObjectClass(objectclass)) {
            attributeNamesToKeep.addAll(objectclassAttributeMap.get(objectclass));
          }
        }
        purgeAttributes(ldifEntry.getEntry(), attributeNamesToKeep);
      }
    }
  }

  /**
   * Remove any attribute whose name is "objectclass" and value is "top".
   * 
   * @param ldifEntries
   * @throws NamingException
   */
  public static void purgeObjectclassTop(Collection<LdifEntry> ldifEntries)
      throws NamingException {
    for (LdifEntry ldifEntry : ldifEntries) {
      if (ldifEntry.isEntry()) {
        Entry entry = ldifEntry.getEntry();
        if (entry.contains("objectclass", "top")) {
          entry.remove("objectclass", "top");
        }
      }
    }
  }

  /**
   * Return the contents of the given file as a string.
   * 
   * @param file
   * @return
   */
  public static String readFile(File file) {

    StringBuffer buffer = new StringBuffer();

    try {
      BufferedReader in = new BufferedReader(new FileReader(file));
      String str;
      while ((str = in.readLine()) != null) {
        buffer.append(str + System.getProperty("line.separator"));
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

  /**
   * Sort the given entries by DN or string LDIF representation.
   * 
   * @param ldifEntries
   * @return
   */
  public static List<LdifEntry> sortLdif(Collection<LdifEntry> ldifEntries) {

    ArrayList<LdifEntry> list = new ArrayList<LdifEntry>(ldifEntries);
    list.trimToSize();

    Collections.sort(list, new Comparator() {

      public int compare(Object o1, Object o2) {
        // first compare by DN
        int c = (((LdifEntry) o1).getDn()).compareTo(((LdifEntry) o2).getDn());
        if (c != 0) {
          return c;
        }
        // then compare by "ldif"
        return ((LdifEntry) o1).toString().compareTo(((LdifEntry) o2).toString());
      }
    });

    return list;
  }

  public static void verifyLdif(String correctLdif, String currentLdif,
      boolean purgeAttributes) throws NamingException, FileNotFoundException,
      IOException, ResourceException {
    verifyLdif(correctLdif, currentLdif, null, purgeAttributes);
  }

  public static void verifyLdif(String correctLdif, String currentLdif,
      File propertiesFile, boolean purgeAttributes) throws NamingException,
      FileNotFoundException, IOException, ResourceException {
    verifyLdif(correctLdif, currentLdif, propertiesFile, null, purgeAttributes);
  }

  public static void verifyLdif(String correctLdif, File propertiesFile,
      Collection<String> normalizeDnAttributes, String base, LdapContext ldapContext,
      boolean purgeAttributes) throws IOException, ResourceException, NamingException {

    // replace macros
    String filteredCorrectLdif = LdappcTestHelper
        .applyFilter(correctLdif, propertiesFile);

    // get attribute ids to request
    String[] requestedAttributes = null;
    Map<String, Collection<String>> map = LdappcTestHelper
        .buildObjectlassAttributeMap(filteredCorrectLdif);
    if (map != null) {
      Set<String> attrIds = new HashSet<String>();
      for (Collection<String> values : map.values()) {
        attrIds.addAll(values);
      }
      requestedAttributes = attrIds.toArray(new String[] {});
    }

    // get current ldif using requested attribute ids
    String currentLdif = LdappcTestHelper.getCurrentLdif(base, requestedAttributes,
        ldapContext);

    // verify ldif
    LdappcTestHelper.verifyLdif(correctLdif, currentLdif, propertiesFile,
        normalizeDnAttributes, purgeAttributes);
  }

  public static void verifyLdif(String correctLdif, File propertiesFile,
      Collection<String> normalizeDnAttributes, String base, Ldap ldap,
      boolean purgeAttributes) throws IOException, ResourceException, NamingException {

    // replace macros
    String filteredCorrectLdif = LdappcTestHelper
        .applyFilter(correctLdif, propertiesFile);

    // get attribute ids to request
    String[] requestedAttributes = null;
    Map<String, Collection<String>> map = LdappcTestHelper
        .buildObjectlassAttributeMap(filteredCorrectLdif);
    if (map != null) {
      Set<String> attrIds = new HashSet<String>();
      for (Collection<String> values : map.values()) {
        attrIds.addAll(values);
      }
      requestedAttributes = attrIds.toArray(new String[] {});
    }

    // get current ldif using requested attribute ids
    String currentLdif = LdappcTestHelper.getCurrentLdif(base, requestedAttributes, ldap);

    // verify ldif
    LdappcTestHelper.verifyLdif(correctLdif, currentLdif, propertiesFile,
        normalizeDnAttributes, purgeAttributes);
  }

  public static void verifyLdif(String correctLdif, String currentLdif,
      File propertiesFile, Collection<String> normalizeDnAttributes,
      boolean purgeAttributes) throws NamingException, FileNotFoundException,
      IOException, ResourceException {
    InputStream correct = new ByteArrayInputStream(correctLdif.getBytes());
    InputStream current = new ByteArrayInputStream(currentLdif.getBytes());
    verifyLdif(correct, current, propertiesFile, normalizeDnAttributes, purgeAttributes);
  }

  public static void verifyLdif(File correctFile, File currentFile, File propertiesFile,
      Collection<String> normalizeDnAttributes, boolean purgeAttributes)
      throws FileNotFoundException, IOException, ResourceException, NamingException {
    InputStream correct = new FileInputStream(correctFile);
    InputStream current = new FileInputStream(currentFile);
    verifyLdif(correct, current, propertiesFile, normalizeDnAttributes, purgeAttributes);
  }

  public static void verifyLdif(InputStream correct, InputStream current,
      File propertiesFile, Collection<String> normalizeDnAttributes,
      boolean purgeAttributes) throws FileNotFoundException, IOException,
      ResourceException, NamingException {
    String correctLdif;
    String currentLdif;
    if (propertiesFile != null) {
      // replace macros
      PropertyReplacementResourceFilter filter = new PropertyReplacementResourceFilter(
          propertiesFile);
      correctLdif = DatatypeHelper.inputstreamToString(filter.applyFilter(correct), null);
      currentLdif = DatatypeHelper.inputstreamToString(filter.applyFilter(current), null);
    } else {
      correctLdif = DatatypeHelper.inputstreamToString(correct, null);
      currentLdif = DatatypeHelper.inputstreamToString(current, null);
    }

    // the ApacheDS reader
    LdifReader reader = new LdifReader();

    Collection<LdifEntry> correctEntries = reader.parseLdif(correctLdif);
    Collection<LdifEntry> currentEntries = reader.parseLdif(currentLdif);

    // remove objectclass: top
    purgeObjectclassTop(correctEntries);
    purgeObjectclassTop(currentEntries);

    if (purgeAttributes) {
      Map<String, Collection<String>> objectClassAttributeMap = buildObjectlassAttributeMap(correctEntries);
      if (objectClassAttributeMap != null) {
        purgeAttributes(correctEntries, objectClassAttributeMap);
        purgeAttributes(currentEntries, objectClassAttributeMap);
      }
    }

    // normalize dn values
    if (normalizeDnAttributes != null) {
      normalizeDNValues(correctEntries, normalizeDnAttributes);
      normalizeDNValues(currentEntries, normalizeDnAttributes);
    }

    verifyLdif(correctEntries, currentEntries);
  }

  /**
   * Verify that the given ldif entries are equal.
   * 
   * @param correctEntries
   * @param currentEntries
   */
  public static void verifyLdif(Collection<LdifEntry> correctEntries,
      Collection<LdifEntry> currentEntries) {

    List<LdifEntry> correctList = LdappcTestHelper.sortLdif(correctEntries);
    List<LdifEntry> currentList = LdappcTestHelper.sortLdif(currentEntries);

    Assert.assertEquals(correctList.size(), currentList.size());

    for (int i = 0; i < correctList.size(); i++) {
      Assert.assertEquals(correctList.get(i), currentList.get(i));
    }

    for (int i = 0; i < currentList.size(); i++) {
      Assert.assertEquals(currentList.get(i), correctList.get(i));
    }
  }

  public static Marshallable verifySpml(XMLMarshaller m, XMLUnmarshaller u,
      Marshallable testObject, File correctXMLFile) {
    return verifySpml(m, u, testObject, correctXMLFile, false);
  }

  public static Marshallable verifySpml(XMLMarshaller m, XMLUnmarshaller u,
      Marshallable testObject, File correctXMLFile, boolean testEquality) {

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
      DetailedDiff marshallingDiff = new DetailedDiff(new Diff(testXML,
          unmarshalledTestXML));
      Assert.assertTrue(marshallingDiff.identical());

      // ignore requestID, must test similar not identical
      DifferenceListener ignoreRequestID = new IgnoreRequestIDDifferenceListener();

      // test testXML against correctXML
      Diff correctDiff = new Diff(new FileReader(correctXMLFile), new StringReader(
          testXML));
      correctDiff.overrideDifferenceListener(ignoreRequestID);
      DetailedDiff correctDetailedDiff = new DetailedDiff(correctDiff);
      if (!correctDetailedDiff.getAllDifferences().isEmpty()) {
        LOG.debug("differences '{}'", correctDetailedDiff.getAllDifferences());
        LOG.debug("diff '{}'", correctDetailedDiff.toString());
      }
      Assert.assertTrue(correctDetailedDiff.getAllDifferences().isEmpty());
      Assert.assertTrue(correctDetailedDiff.similar());

      // test unmarshalledXML against correctXML
      Diff unmarshalledDiff = new Diff(new FileReader(correctXMLFile), new StringReader(
          unmarshalledTestXML));
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
