package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.profile.provider.BaseSAMLProfileRequestContext;
import edu.internet2.middleware.subject.Subject;

public abstract class BaseDataConnectorTest extends GrouperTest {

  public static final String TEST_PATH = "/test/edu/internet2/middleware/grouper/shibboleth/dataConnector/";

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
    correctAttributesChildStem.setAttribute(BaseGrouperDataConnector.PARENT_STEM_NAME_ATTR, "parentStem");

    // custom list
    GroupType type = GroupType.createType(grouperSession, "groupType");
    type.addAttribute(grouperSession, "attr1", AccessPrivilege.VIEW, AccessPrivilege.UPDATE, false);
    type.addList(grouperSession, "customList", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    Field customList = FieldFinder.find("customList", true);

    // group A
    groupA = StemHelper.addChildGroup(this.parentStem, "groupA", "Group A");
    groupA.addMember(SubjectTestHelper.SUBJ0);
    groupA.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN);
    correctAttributesA = new AttributeMap();
    correctAttributesA.setAttribute("extension", "groupA");
    correctAttributesA.setAttribute("displayExtension", "Group A");
    correctAttributesA.setAttribute("name", "parentStem:groupA");
    correctAttributesA.setAttribute("displayName", "Parent Stem:Group A");
    correctAttributesA.setAttribute(BaseGrouperDataConnector.PARENT_STEM_NAME_ATTR, "parentStem");

    // group B
    groupB = StemHelper.addChildGroup(this.parentStem, "groupB", "Group B");
    groupB.setDescription("Group B Description");
    groupB.addType(type);
    groupB.setAttribute("attr1", "value1", false);
    groupB.addMember(SubjectTestHelper.SUBJ1);
    groupB.addMember(groupA.toSubject());
    groupB.addMember(SubjectTestHelper.SUBJ2, customList);
    groupB.store();

    correctAttributesB = new AttributeMap();
    correctAttributesB.setAttribute("extension", "groupB");
    correctAttributesB.setAttribute("displayExtension", "Group B");
    correctAttributesB.setAttribute("name", "parentStem:groupB");
    correctAttributesB.setAttribute("displayName", "Parent Stem:Group B");
    correctAttributesB.setAttribute("description", "Group B Description");
    correctAttributesB.setAttribute(BaseGrouperDataConnector.PARENT_STEM_NAME_ATTR, "parentStem");
    correctAttributesB.setAttribute("attr1", "value1");

    // group C
    groupC = StemHelper.addChildGroup(this.childStem, "groupC", "Group C");

    correctAttributesC = new AttributeMap();
    correctAttributesC.setAttribute("extension", "groupC");
    correctAttributesC.setAttribute("displayExtension", "Group C");
    correctAttributesC.setAttribute("name", "parentStem:childStem:groupC");
    correctAttributesC.setAttribute("displayName", "Parent Stem:Child Stem:Group C");
    correctAttributesC.setAttribute(BaseGrouperDataConnector.PARENT_STEM_NAME_ATTR, "parentStem:childStem");

    memberSubj0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
    memberSubj1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, false);
    memberSubj2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, false);
    memberSubj3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, false);
    memberAll = MemberFinder.findBySubject(grouperSession, SubjectFinder.findAllSubject(), false);
  }

  public static ShibbolethResolutionContext getShibContext(String principal) {
    BaseSAMLProfileRequestContext attributeRequestContext = new BaseSAMLProfileRequestContext();
    attributeRequestContext.setPrincipalName(principal);
    return new ShibbolethResolutionContext(attributeRequestContext);
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

    public void setAttribute(String name, String... values) {
      if (values == null) {
        return;
      }
      ArrayList<String> list = new ArrayList<String>();
      for (String value : values) {
        list.add(value);
      }
      BasicAttribute attr = new BasicAttribute(name);
      attr.setValues(list);
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
          correctValueMap.put(Integer.valueOf(correctValue.hashCode()), correctValue);
        }
        Map<Integer, Object> currentValueMap = new TreeMap<Integer, Object>();
        for (Object currentValue : currentValues) {
          currentValueMap.put(Integer.valueOf(currentValue.hashCode()), currentValue);
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
      StringBuffer buffer = new StringBuffer();
      TreeSet<String> sortedKeys = new TreeSet<String>(map.keySet());
      for (String key : sortedKeys) {
        for (Object value : map.get(key).getValues()) {
          buffer.append(key + " : '" + value + "'\n");
        }
      }
      return buffer.toString();
    }
  }

}
