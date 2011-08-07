/*
 * @author mchyzer
 * $Id: DummySubject.java,v 1.4 2009-09-02 05:57:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.helper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SubjectCaseInsensitiveMapImpl;
import edu.internet2.middleware.subject.provider.SubjectImpl;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;


/**
 *
 */
@SuppressWarnings("serial")
public class DummySubject extends SubjectImpl {

  /**
   * 
   * @param theSubjectId
   * @param sourceId
   */
  public DummySubject(String theSubjectId, String sourceId) {
    super(theSubjectId, theSubjectId, theSubjectId, SubjectTypeEnum.PERSON.getName(), sourceId, null);
  }
  
  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String)
   */
  public String getAttributeValue(String arg0) {
    return this.getId();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
   */
  public Set<String> getAttributeValues(String arg0) {
    
    Set<String> results =new HashSet<String>();
    results.add(this.getId());
    return results;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributes()
   */
  public Map<String, Set<String>> getAttributes() {
    Map<String, Set<String>> map = new  SubjectCaseInsensitiveMapImpl<String, Set<String>>();
    map.put("loginid", GrouperUtil.toSet(this.getId()));
    map.put("name", GrouperUtil.toSet(this.getId()));
    map.put("description", GrouperUtil.toSet(this.getId()));
    return map;
  }

}
