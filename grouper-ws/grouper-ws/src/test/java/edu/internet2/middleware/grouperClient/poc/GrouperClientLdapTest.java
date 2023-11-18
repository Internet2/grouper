/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.poc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouperClient.GrouperClient;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
public class GrouperClientLdapTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperClientLdapTest("testPennnameToPennid"));
  }
  
  /**
   * 
   */
  @Override
  protected void setUp() {
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.operationName.0", "pennnameToPennid");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.ldapName.0", "ou=pennnames");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.returningAttributes.0", "pennid");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.returningAttributeLabels.0", "pennid:$space$");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.matchingAttributes.0", "pennname");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.matchingAttributeLabels.0", "pennnameToDecode");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.resultType.0", "STRING");

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.operationName.1", "pennidToPennname");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.ldapName.1", "ou=pennnames");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.matchingAttributes.1", "pennid");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.matchingAttributeLabels.1", "pennidToDecode");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.returningAttributes.1", "pennname");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.returningAttributeLabels.1", "pennname:$space$");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.resultType.1", "STRING");

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.operationName.2", "hasMemberLdap");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.ldapName.2", "ou=groups");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.matchingAttributes.2", "cn, hasMember");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.matchingAttributeLabels.2", "groupName, pennnameToCheck");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.returningAttributes.2", "cn");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.returningAttributeLabels.2", "hasMember:$space$");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.resultType.2", "BOOLEAN");

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.operationName.3", "getMembersLdap");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.ldapName.3", "ou=groups");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.matchingAttributes.3", "cn");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.matchingAttributeLabels.3", "groupName");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.returningAttributes.3", "hasMember");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.returningAttributeLabels.3", "");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("ldapSearchAttribute.resultType.3", "STRING_LIST");

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.ldap.user.prefix", "uid=");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.ldap.user.suffix", ",ou=entities,dc=upenn,dc=edu");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.ldap.user.label", "kerberosPrincipal");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.output.version", "1.4.0");

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("encrypt.key", "sdfklj24lkj34lk34");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("encrypt.disableExternalFileLookup", "false");

  }

  /**
   * 
   */
  @Override
  protected void tearDown() {
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().clear();
    
  }

  /**
   * @param name
   */
  public GrouperClientLdapTest(String name) {
    super(name);
  }

  /**
   * note: this will only work at penn
   */
  public void testEncrypt() {
    
    InputStream systemIn = System.in;
    PrintStream systemOut = System.out;

    System.setIn(new ByteArrayInputStream("abc\n".getBytes())); 
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    
    try {
      
      GrouperClient.main(GrouperClientUtils.splitTrim("--operation=encryptPassword --dontMask=true", " "));
      System.out.flush();
      String encrypted = new String(baos.toByteArray());
      
      System.setIn(systemIn);
      System.setOut(systemOut);
      
      Pattern pattern = Pattern.compile("^.*Encrypted password: (.*)$", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(encrypted);
      
      assertTrue(encrypted, matcher.matches());
      
      String encryptedPass = GrouperClientUtils.trim(matcher.group(1));
      
      assertEquals("/SXb449hv2b1eM4XYbB72g==", encryptedPass);
      
    } finally {
      System.setIn(systemIn);
      System.setOut(systemOut);
    }
    
  }
  
  
  /**
   * note: this will only work at penn
   */
  public void testPennnameToPennid() {
    
    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    
    try {
      
      GrouperClient.main(GrouperClientUtils.splitTrim("--operation=pennnameToPennid --pennnameToDecode=mchyzer", " "));
      System.out.flush();
      String pennid = new String(baos.toByteArray());
      
      System.setOut(systemOut);
      
      Pattern pattern = Pattern.compile("^pennid: (.*)$", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(pennid);
      
      assertTrue(pennid, matcher.matches());
      
      String thePennid = GrouperClientUtils.trim(matcher.group(1));
      
      assertEquals("10021368", thePennid);
      
    } finally {
      System.setOut(systemOut);
    }
    
  }
  
  /**
   * note: this will only work at penn
   */
  public void testPennnameToPennidNotFound() {
    
    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    
    try {
      
      GrouperClient.main(GrouperClientUtils.splitTrim("--operation=pennnameToPennid --pennnameToDecode=x1x1x1x1", " "));
      System.out.flush();
      String pennid = new String(baos.toByteArray());
      
      System.setOut(systemOut);
      
      Pattern pattern = Pattern.compile("^pennid: (.*)$", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(pennid);
      
      assertTrue(pennid, matcher.matches());
      
      String thePennid = GrouperClientUtils.trimToEmpty(matcher.group(1));
      
      assertEquals("", thePennid);
      
    } finally {
      System.setOut(systemOut);
    }
    
  }

  /**
   * note: this will only work at penn
   */
  public void testPennidToPennname() {
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    
    try {
      
      GrouperClient.main(GrouperClientUtils.splitTrim("--operation=pennidToPennname --pennidToDecode=10021368", " "));
      System.out.flush();
      String pennname = new String(baos.toByteArray());
      
      System.setOut(systemOut);
      
      Pattern pattern = Pattern.compile("^pennname: (.*)$", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(pennname);
      
      assertTrue(pennname, matcher.matches());
      
      String thePennname = GrouperClientUtils.trim(matcher.group(1));
      
      assertEquals("mchyzer", thePennname);
      
    } finally {
      System.setOut(systemOut);
    }
    
  }

  /**
   * note: this will only work at penn
   */
  public void testIsInGroup() {
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    
    try {
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=hasMemberLdap --groupName=penn:isc:ait:apps:fast:pennCommunity --pennnameToCheck=mchyzer", " "));
      System.out.flush();
      String pennname = new String(baos.toByteArray());
      
      System.setOut(systemOut);
      
      Pattern pattern = Pattern.compile("^hasMember: (.*)$", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(pennname);
      
      assertTrue(pennname, matcher.matches());
      
      String thePennname = GrouperClientUtils.trim(matcher.group(1));
      
      assertEquals("true", thePennname);
      
    } finally {
      System.setOut(systemOut);
    }
    
  }

  /**
   * note: this will only work at penn
   */
  public void testIsInGroupUserNotFound() {
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    
    try {
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=hasMemberLdap --groupName=penn:isc:ait:apps:fast:pennCommunity --pennnameToCheck=x1x1x1x1", " "));
      System.out.flush();
      String pennname = new String(baos.toByteArray());
      
      System.setOut(systemOut);
      
      Pattern pattern = Pattern.compile("^hasMember: (.*)$", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(pennname);
      
      assertTrue(pennname, matcher.matches());
      
      String thePennname = GrouperClientUtils.trim(matcher.group(1));
      
      assertEquals("false", thePennname);
      
    } finally {
      System.setOut(systemOut);
    }
    
  }

  /**
   * note: this will only work at penn
   */
  public void testIsInGroupGroupNotFound() {
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    
    try {
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=hasMemberLdap --groupName=penn:isc:ait:apps:fast:x1x1x1x1x1 --pennnameToCheck=mchyzer", " "));
      System.out.flush();
      String pennname = new String(baos.toByteArray());
      
      System.setOut(systemOut);
      
      Pattern pattern = Pattern.compile("^hasMember: (.*)$", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(pennname);
      
      assertTrue(pennname, matcher.matches());
      
      String thePennname = GrouperClientUtils.trim(matcher.group(1));
      
      assertEquals("false", thePennname);
      
    } finally {
      System.setOut(systemOut);
    }
    
  }

  /**
   * note: this will only work at penn
   */
  public void testNotInGroup() {
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    
    try {
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=hasMemberLdap --groupName=penn:community:faculty --pennnameToCheck=mchyzer", " "));
      System.out.flush();
      String pennname = new String(baos.toByteArray());
      
      System.setOut(systemOut);
      
      Pattern pattern = Pattern.compile("^hasMember: (.*)$", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(pennname);
      
      assertTrue(pennname, matcher.matches());
      
      String thePennname = GrouperClientUtils.trim(matcher.group(1));
      
      assertEquals("false", thePennname);
      
    } finally {
      System.setOut(systemOut);
    }
    
  }

  /**
   * note: this will only work at penn
   */
  public void testPennidToPennnameNotFound() {
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    
    try {
      
      GrouperClient.main(GrouperClientUtils.splitTrim("--operation=pennidToPennname --pennidToDecode=1010101", " "));
      System.out.flush();
      String pennname = new String(baos.toByteArray());
      
      System.setOut(systemOut);
      
      Pattern pattern = Pattern.compile("^pennname: (.*)$", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(pennname);
      
      assertTrue(pennname, matcher.matches());
      
      String thePennname = GrouperClientUtils.trimToEmpty(matcher.group(1));
      
      assertEquals("", thePennname);
      
    } finally {
      System.setOut(systemOut);
    }
    
  }
  
  /**
   * note: this will only work at penn
   */
  public void testGetMembersLdap() {
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    
    try {
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getMembersLdap --groupName=penn:isc:ait:apps:fast:pennCommunity", " "));
      System.out.flush();
      String output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
      
      assertTrue(output, output.contains("mchyzer"));
      assertTrue(output, output.contains("harveycg"));
            
    } finally {
      System.setOut(systemOut);
    }
    
  }

  /**
   * note: this will only work at penn
   */
  public void testGetMembersLdapGroupNotExist() {
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    
    try {
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getMembersLdap --groupName=penn:isc:ait:apps:fast:x1x1x1x1", " "));
      System.out.flush();
      String output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
      
      assertEquals(output, "", GrouperClientUtils.trimToEmpty(output));
            
    } finally {
      System.setOut(systemOut);
    }
    
  }
  
  /**
   * note: this will only work at penn
   */
  public void testGetMembersLdapFile() {
    
    File file = new File("f:/temp/groupList.txt");
    if (file.exists()) {
      assertTrue(file.delete());
    }
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    
    try {
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getMembersLdap --groupName=penn:isc:ait:apps:fast:pennCommunity --saveResultsToFile=f:/temp/groupList.txt", " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertEquals(output, "", GrouperClientUtils.trimToEmpty(output));
      
      String fileContents = GrouperClientUtils.readFileIntoString(file);
      
      assertTrue(fileContents, fileContents.contains("mchyzer"));
      assertTrue(fileContents, fileContents.contains("harveycg"));

    } finally {
      System.setOut(systemOut);
      if (file.exists()) {
        assertTrue(file.delete());
      }
    }
  }
}
