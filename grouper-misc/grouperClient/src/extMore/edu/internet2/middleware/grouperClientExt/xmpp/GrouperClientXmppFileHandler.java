/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClientExt.xmpp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * handler for events and saves to file
 */
public class GrouperClientXmppFileHandler implements GrouperClientXmppHandler {

  /**
   * @see edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppHandler#handleAll(GrouperClientXmppJob, String, String, List)
   */
  // @Override
  public void handleAll(GrouperClientXmppJob grouperClientXmppJob, String groupName,
      String groupExtension,
      List<GrouperClientXmppSubject> newSubjectList) {
    handleList(grouperClientXmppJob, newSubjectList);
  }

  /**
   * 
   * @param grouperClientXmppJob
   * @param subjects
   */
  private void handleList(GrouperClientXmppJob grouperClientXmppJob, List<GrouperClientXmppSubject> subjects) {
    String pre = "";
    if (!GrouperClientUtils.isBlank(grouperClientXmppJob.getFilePrefix())) {
      pre = GrouperClientUtils.readFileIntoString(new File(grouperClientXmppJob.getFilePrefix()));
    }
    String post = "";
    if (!GrouperClientUtils.isBlank(grouperClientXmppJob.getFileSuffix())) {
      post = GrouperClientUtils.readFileIntoString(new File(grouperClientXmppJob.getFileSuffix()));
    }
    StringBuilder result = new StringBuilder(pre);
    List<String> resultList = new ArrayList<String>();
    for (GrouperClientXmppSubject xmppSubject : GrouperClientUtils.nonNull(subjects)) {
      String outputTemplate = grouperClientXmppJob.getIteratorEl();
      outputTemplate = GrouperClientUtils.substituteCommonVars(outputTemplate);
      Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
      substituteMap.put("grouperClientUtils", new GrouperClientUtils());
      substituteMap.put("subject", xmppSubject);
      String output = GrouperClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
      resultList.add(output);
    }
    //sort results
    Collections.sort(resultList);
    for (String resultItem : resultList) {
      result.append(resultItem);
    }
    result.append(post);
    GrouperClientUtils.saveStringIntoFile(new File(grouperClientXmppJob.getTargetFile()), result.toString());
    
  }

  /**
   * @see edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppHandler#handleIncremental(GrouperClientXmppJob, String, String, List, List, GrouperClientXmppSubject, String)
   */
  // @Override
  public void handleIncremental(GrouperClientXmppJob grouperClientXmppJob, String groupName,
      String groupExtension, 
      List<GrouperClientXmppSubject> newSubjectList, List<GrouperClientXmppSubject> previousSubjectList,
      GrouperClientXmppSubject changeSubject, String action) {
    handleList(grouperClientXmppJob, newSubjectList);
  }
}
