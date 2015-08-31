/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * 
 */
public class GroupHooksImpl3 extends GroupHooks {

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  public void groupPreUpdate(HooksContext hooksContext, HooksGroupBean preUpdateBean) { 
    Group group = preUpdateBean.getGroup(); 
    
    System.out.println("new name: " + group.getName());

    group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), group.getId(), true,new QueryOptions().secondLevelCache(false));
    
    System.out.println("old name: " + group.getName());
    
    
  } 

  
  /**
   * 
   */
  public GroupHooksImpl3() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
//    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
//        .assignName("test:someGroupOldName").save();
//    
//    group.setExtension("someGroupNewName");
//    group.store();

    String someName = "test:SOMEGROUPNEWNAME";

//    Group group = HibernateSession.byHqlStatic()
//        .createQuery("select theGroup from Group theGroup where lower(theGroup.nameDb) = :theName")
//        .setString("theName", StringUtils.defaultString(someName).toLowerCase()).uniqueResult(Group.class);
//
//    System.out.println(group);

    String parentFolderName = GrouperUtil.parentStemNameFromName(someName);
    
    Stem parentStem = StemFinder.findByName(grouperSession, parentFolderName, true);
    
    Group group = HibernateSession.byHqlStatic()
        .createQuery("select theGroup from Group theGroup "
            + "where lower(theGroup.extensionDb) = :theExtension "
            + "and theGroup.parentUuid = :stemUuid")
        .setString("theExtension", StringUtils.defaultString(GrouperUtil.extensionFromName(someName)).toLowerCase())
        .setString("stemUuid", parentStem.getId())
        .uniqueResult(Group.class);

   System.out.println(group);

    
  }

}
