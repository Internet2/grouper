/**
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
 */
package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.opensaml.util.resource.FilesystemResource;
import org.opensaml.util.resource.Resource;
import org.opensaml.util.resource.ResourceException;
import org.springframework.context.support.GenericApplicationContext;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabel;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;
import edu.internet2.middleware.shibboleth.common.profile.provider.BaseSAMLProfileRequestContext;
import edu.internet2.middleware.subject.Subject;

public abstract class BaseDataConnectorTest extends GrouperTest {

  public static final String TEST_PATH = "/test/";

  protected Group groupA;

  protected Group groupB;

  protected Group groupC;

  protected Stem root;

  protected Stem parentStem;

  protected Stem childStem;

  protected GrouperSession grouperSession;

  protected AttributeMap correctAttributesA;

  protected AttributeMap correctAttributesB;

  protected AttributeMap correctAttributesC;

  protected Member memberSubj0;

  protected Member memberSubj1;

  protected Member memberSubj2;

  protected Member memberSubj3;

  protected Member memberAll;

  protected AttributeMap correctAttributesParentStem;

  protected AttributeMap correctAttributesChildStem;

  public BaseDataConnectorTest(String name) {
    super(name);
  }

  public void setUp() {

    super.setUp();

    grouperSession = SessionHelper.getRootSession();

    root = StemHelper.findRootStem(grouperSession);

    // parent stem
    parentStem = StemHelper.addChildStem(root, "parentStem", "Parent Stem");
    correctAttributesParentStem = new AttributeMap();
    correctAttributesParentStem.setAttribute("extension", "parentStem");
    correctAttributesParentStem.setAttribute("displayExtension", "Parent Stem");
    correctAttributesParentStem.setAttribute("name", "parentStem");
    correctAttributesParentStem.setAttribute("displayName", "Parent Stem");

    // child stem
    childStem = StemHelper.addChildStem(parentStem, "childStem", "Child Stem");
    correctAttributesChildStem = new AttributeMap();
    correctAttributesChildStem.setAttribute("extension", "childStem");
    correctAttributesChildStem.setAttribute("displayExtension", "Child Stem");
    correctAttributesChildStem.setAttribute("name", "parentStem:childStem");
    correctAttributesChildStem.setAttribute("displayName", "Parent Stem:Child Stem");

    // group A
    groupA = StemHelper.addChildGroup(this.parentStem, "groupA", "Group A");
    groupA.addMember(SubjectTestHelper.SUBJ0);
    groupA.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN);
    correctAttributesA = new AttributeMap();
    correctAttributesA.setAttribute("extension", "groupA");
    correctAttributesA.setAttribute("displayExtension", "Group A");
    correctAttributesA.setAttribute("name", "parentStem:groupA");
    correctAttributesA.setAttribute("displayName", "Parent Stem:Group A");

    // group B
    groupB = StemHelper.addChildGroup(this.parentStem, "groupB", "Group B");
    groupB.setDescription("Group B Description");
    groupB.addMember(SubjectTestHelper.SUBJ1);
    groupB.addMember(groupA.toSubject());
    groupB.store();

    correctAttributesB = new AttributeMap();
    correctAttributesB.setAttribute("extension", "groupB");
    correctAttributesB.setAttribute("displayExtension", "Group B");
    correctAttributesB.setAttribute("name", "parentStem:groupB");
    correctAttributesB.setAttribute("displayName", "Parent Stem:Group B");
    correctAttributesB.setAttribute("description", "Group B Description");
    correctAttributesB.setAttribute("attr1", "value1");
  
    // group C
    groupC = StemHelper.addChildGroup(this.childStem, "groupC", "Group C");

    correctAttributesC = new AttributeMap();
    correctAttributesC.setAttribute("extension", "groupC");
    correctAttributesC.setAttribute("displayExtension", "Group C");
    correctAttributesC.setAttribute("name", "parentStem:childStem:groupC");
    correctAttributesC.setAttribute("displayName", "Parent Stem:Child Stem:Group C");

    memberSubj0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
    memberSubj1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, false);
    memberSubj2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, false);
    memberSubj3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, false);
    memberAll = MemberFinder.findBySubject(grouperSession, SubjectFinder.findAllSubject(), false);

    AttributeDef attributeDef = parentStem.addChildAttributeDef("attrDef", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.setAssignToStem(true);
    attributeDef.setAssignToMember(true);
    attributeDef.setMultiValued(true);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();

    parentStem.addChildAttributeDefName(attributeDef, "mailAlternateAddress", "mailAlternateAddress");
  }

  public static ShibbolethResolutionContext getShibContext(String principal) {
    BaseSAMLProfileRequestContext attributeRequestContext = new BaseSAMLProfileRequestContext();
    attributeRequestContext.setPrincipalName(principal);
    return new ShibbolethResolutionContext(attributeRequestContext);
  }

  public static GenericApplicationContext createSpringContext(String... configs) throws ResourceException {

    GenericApplicationContext gContext = new GenericApplicationContext();
    SpringConfigurationUtils.populateRegistry(gContext, getResources(null, configs));
    gContext.refresh();
    gContext.registerShutdownHook();

    return gContext;
  }

  /**
   * Returns {@link Resource}s with the given names. If the path is {@code null}, resources will be found using the
   * classpath.
   * 
   * @param path the directory containing resources
   * @param resourceNames the names of the resource files
   * @return the resources
   * @throws ResourceException if an error occurs loading the resource
   * @throws IllegalArgumentException if the resources are not files or are not readable
   */
  public static List<Resource> getResources(String path, String... resourceNames) throws ResourceException {
    ArrayList<Resource> resources = new ArrayList<Resource>();
    for (String resourceName : resourceNames) {
      File file = null;
      if (path == null) {
        file = GrouperUtil.fileFromResourceName(resourceName);
      } else {
        file = new File(path + System.getProperty("file.separator") + resourceName);
      }
      if (file == null) {
        throw new IllegalArgumentException("Unable to find file '" + resourceName + "'.");
      }
      if (!file.isFile() || !file.canRead()) {
        throw new IllegalArgumentException("Unable to read file '" + resourceName + "'.");
      }
      resources.add(new FilesystemResource(file.getAbsolutePath()));
    }
    return resources;
  }

  public class AttributeMap {

    private Map<String, BaseAttribute> map;

    public AttributeMap() {
      this.map = new HashMap<String, BaseAttribute>();
    }

    public AttributeMap(Map<String, BaseAttribute> map) {
      this.map = map;
    }

    public Map<String, BaseAttribute> getMap() {
      return map;
    }

    public void addAttribute(String name, Member... members) {
      if (members == null) {
        return;
      }
      Set<Member> list = new LinkedHashSet<Member>();
      for (Member member : members) {
        list.add(member);
      }
      BaseAttribute attr = null;
      if (map.containsKey(name)) {
        attr = map.get(name);
      } else {
        attr = new BasicAttribute(name);
        map.put(name, attr);
      }
      attr.getValues().addAll(list);
    }

    public void addAttribute(String name, Group... groups) {
      if (groups == null) {
        return;
      }
      Set<Group> list = new LinkedHashSet<Group>();
      for (Group group : groups) {
        list.add(group);
      }
      BaseAttribute attr = null;
      if (map.containsKey(name)) {
        attr = map.get(name);
      } else {
        attr = new BasicAttribute(name);
        map.put(name, attr);
      }
      attr.getValues().addAll(list);
    }
    
    public void setAttribute(String name) {
      BasicAttribute attr = new BasicAttribute(name);      
      map.put(attr.getId(), attr);
    }

    public void setAttribute(String name, String... values) {
      if (values == null) {
        return;
      }
      ArrayList<String> list = new ArrayList<String>();
      for (String value : values) {
        if (value != null) {
          list.add(value);
        }
      }
      if (list.isEmpty()) {
        return;
      }
      BasicAttribute attr = new BasicAttribute(name);
      attr.setValues(list);
      map.put(attr.getId(), attr);
    }

    public void setAttribute(ChangeLogLabel changeLogLabel, ChangeLogEntry changeLogEntry) {

      String value = changeLogEntry.retrieveValueForLabel(changeLogLabel);

      BasicAttribute attr = new BasicAttribute(changeLogLabel.name());
      attr.getValues().add(value);
      map.put(attr.getId(), attr);
    }

    public void setAttribute(String name, Member... members) {
      if (members == null) {
        return;
      }
      Set<Member> list = new LinkedHashSet<Member>();
      for (Member member : members) {
        list.add(member);
      }
      BasicAttribute attr = new BasicAttribute(name);
      attr.setValues(list);
      map.put(attr.getId(), attr);
    }

    public void setAttribute(String name, Group... groups) {
      if (groups == null) {
        return;
      }
      Set<Group> list = new LinkedHashSet<Group>();
      for (Group group : groups) {
        list.add(group);
      }
      BasicAttribute attr = new BasicAttribute(name);
      attr.setValues(list);
      map.put(attr.getId(), attr);
    }

    public void setAttribute(String name, Subject... subjects) {
      if (subjects == null) {
        return;
      }
      Set<Subject> list = new LinkedHashSet<Subject>();
      for (Subject subject : subjects) {
        list.add(subject);
      }
      BasicAttribute attr = new BasicAttribute(name);
      attr.setValues(list);
      map.put(attr.getId(), attr);
    }

    public void setAttribute(String name, GroupType... groupTypes) {
      if (groupTypes == null) {
        return;
      }
      Set<GroupType> list = new LinkedHashSet<GroupType>();
      for (GroupType groupType : groupTypes) {
        list.add(groupType);
      }
      BasicAttribute attr = new BasicAttribute(name);
      attr.setValues(list);
      map.put(attr.getId(), attr);
    }

    public void setAttribute(BaseAttribute baseAttribute) {
      if (baseAttribute == null) {
        return;
      }
      map.put(baseAttribute.getId(), baseAttribute);
    }

    /**
     * Attempt to evaluate equals() for an arbitrary BaseAttribute<Type>.
     */
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (!(o instanceof AttributeMap)) {
        return false;
      }

      final AttributeMap that = (AttributeMap) o;

      if (!this.getMap().equals(that.getMap())) {
        return false;
      }

      TreeSet<String> allKeys = new TreeSet<String>();
      allKeys.addAll(this.getMap().keySet());
      allKeys.addAll(that.getMap().keySet());

      for (String key : allKeys) {
        BaseAttribute correctAttribute = this.getMap().get(key);
        BaseAttribute currentAttribute = that.getMap().get(key);
        if (correctAttribute == null && currentAttribute == null) {
          continue;
        }
        if (correctAttribute == null) {
          return false;
        }
        if (currentAttribute == null) {
          return false;
        }

        Collection<?> correctValues = correctAttribute.getValues();
        Collection<?> currentValues = currentAttribute.getValues();

        if (correctValues.size() != currentValues.size()) {
          return false;
        }
        // "sort" values, even if they are not comparable (?)
        Map<Integer, Object> correctValueMap = new TreeMap<Integer, Object>();
        for (Object correctValue : correctValues) {
          if (correctValue == null) {
            correctValueMap.put(0, correctValue);
          } else {
            correctValueMap.put(Integer.valueOf(correctValue.hashCode()), correctValue);
          }
        }
        Map<Integer, Object> currentValueMap = new TreeMap<Integer, Object>();
        for (Object currentValue : currentValues) {
          if (currentValue == null) {
            currentValueMap.put(0, currentValue);
          } else {
            currentValueMap.put(Integer.valueOf(currentValue.hashCode()), currentValue);
          }
        }

        ArrayList<Object> correctValueList = new ArrayList<Object>();
        for (Integer k : correctValueMap.keySet()) {
          correctValueList.add(correctValueMap.get(k));
        }
        ArrayList<Object> currentValueList = new ArrayList<Object>();
        for (Integer k : currentValueMap.keySet()) {
          currentValueList.add(currentValueMap.get(k));
        }

        if (!correctValueList.equals(currentValueList)) {
          return false;
        }
      }

      return true;
    }

    public String toString() {
      if (map == null) {
        return null;
      }
      StringBuffer buffer = new StringBuffer("\n");
      TreeSet<String> sortedKeys = new TreeSet<String>(map.keySet());
      for (String key : sortedKeys) {
        BaseAttribute baseAttribute = map.get(key);
        if (baseAttribute.getValues().isEmpty()) {
          buffer.append(key + " : ''\n");
        } else {
          for (Object value : map.get(key).getValues()) {
            buffer.append(key + " : '" + value + "'\n");
          }
        }
      }
      return buffer.toString();
    }
  }

}
