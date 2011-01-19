/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.kuali.rice.core.config.Config;
import org.kuali.rice.core.config.ConfigContext;
import org.kuali.rice.core.database.platform.DatabasePlatform;
import org.kuali.rice.core.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.util.RiceConstants;
import org.kuali.rice.kew.actionitem.ActionItem;
import org.kuali.rice.kew.mail.service.impl.CustomizableActionListEmailServiceImpl;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.springframework.jdbc.core.JdbcTemplate;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * send daily email reminders if the user hasnt received an email in a day
 */
public class CustomActionListEmailService extends CustomizableActionListEmailServiceImpl {

  /**
   * logger
   */
  private static final Log LOG = GrouperClientUtils.retrieveLog(CosignAuthenticationService.class);

  /**
   * @see org.kuali.rice.kew.mail.service.impl.ActionListEmailServiceImpl#sendDailyReminder()
   */
  @Override
  public void sendDailyReminder() {
    
    if (sendActionListEmailNotification()) {

      //true this up if didnt get set from spring
      if (GrouperClientUtils.isBlank(this.getDeploymentEnvironment())) {
        this.setDeploymentEnvironment(ConfigContext.getCurrentContextConfig().getProperty(Config.ENVIRONMENT));
      }
      
      DatabasePlatform databasePlatform = (DatabasePlatform)GlobalResourceLoader.getService(RiceConstants.DB_PLATFORM);
      DataSource dataSource = KEWServiceLocator.getDataSource();
      List<String> principalIds = new JdbcTemplate(dataSource).queryForList("select principal_id from daily_action_send_v", String.class);
      
      Collection<Person> users = new ArrayList<Person>();
      
      for (String principalId : principalIds) {
        try {
          users.add(KIMServiceLocator.getPersonService().getPerson(principalId));
        } catch (Exception e) {
          LOG.error("error retrieving workflow user with ID: "
              + principalId);
        }
      }

      for (Person user : users) {

        try {
          Collection actionItems = getActionListService().getActionList(user.getPrincipalId(), null);
          if (actionItems != null && actionItems.size() > 0) {
              sendPeriodicReminder(user, actionItems,
                KEWConstants.EMAIL_RMNDR_DAY_VAL);
          }
        } catch (Exception e) {
          LOG.error(
              "Error sending daily action list reminder to user: "
                  + user.getEmailAddressUnmasked(), e);
        }
      }

    }
    
    LOG.debug("Daily action list emails sent successful");
    
  }

  /**
   * @see org.kuali.rice.kew.mail.service.impl.CustomizableActionListEmailServiceImpl#sendImmediateReminder(org.kuali.rice.kim.bo.Person, org.kuali.rice.kew.actionitem.ActionItem)
   */
  @Override
  public void sendImmediateReminder(Person user, ActionItem actionItem) {
    
    //true this up if didnt get set from spring
    if (GrouperClientUtils.isBlank(this.getDeploymentEnvironment())) {
      this.setDeploymentEnvironment(ConfigContext.getCurrentContextConfig().getProperty(Config.ENVIRONMENT));
    }

    super.sendImmediateReminder(user, actionItem);
  }

}
