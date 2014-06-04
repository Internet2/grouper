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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.validator.EmailValidator;
import org.apache.log4j.Logger;
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


/**
 * send daily email reminders if the user hasnt received an email in a day
 */
public class CustomActionListEmailService extends CustomizableActionListEmailServiceImpl {

  /**
   * logger
   */
  private static final Logger LOG = Logger.getLogger(CustomActionListEmailService.class);

  /**
   * @see org.kuali.rice.kew.mail.service.impl.ActionListEmailServiceImpl#sendDailyReminder()
   */
  @Override
  public void sendDailyReminder() {
    
    Map<String, Object> logMap = new LinkedHashMap<String, Object>();

    try {
    
      boolean sendActionListEmailNotification = sendActionListEmailNotification();
  
      logMap.put("sendActionListEmailNotification", sendActionListEmailNotification);
      
      if (sendActionListEmailNotification) {
  
        //true this up if didnt get set from spring
        if (GrouperClientUtils.isBlank(this.getDeploymentEnvironment())) {
          this.setDeploymentEnvironment(ConfigContext.getCurrentContextConfig().getProperty(Config.ENVIRONMENT));
        }
        
        DatabasePlatform databasePlatform = (DatabasePlatform)GlobalResourceLoader.getService(RiceConstants.DB_PLATFORM);
        DataSource dataSource = KEWServiceLocator.getDataSource();
        List<String> principalIds = new JdbcTemplate(dataSource).queryForList("select principal_id from daily_action_send_v", String.class);
        
        logMap.put("principalIdCount", GrouperClientUtils.length(principalIds));
        
        Collection<Person> users = new ArrayList<Person>();

        for (String principalId : principalIds) {
          try {
            Person person = KIMServiceLocator.getPersonService().getPerson(principalId);
            
            if (person == null) {
              LOG.error("person is null: " + principalId);
              logMap.put("errorRetrieve_" + principalId, "null user");
              continue;
            }
            
            if (GrouperClientUtils.isBlank(person.getEmailAddressUnmasked())) {
              LOG.error("email is null: " + principalId);
              logMap.put("errorRetrieve_" + principalId, "null email");
              continue;
            }

            if(!EmailValidator.getInstance().isValid(person.getEmailAddressUnmasked())) {
              LOG.error("email is invalid: " + principalId + ", " + person.getEmailAddressUnmasked());
              logMap.put("errorRetrieve_" + principalId, "invalid email: " + person.getEmailAddressUnmasked());
              continue;
            }

            users.add(person);
          } catch (Exception e) {
            LOG.error("error retrieving workflow user with ID: "
                + principalId);
            logMap.put("errorRetrieve_" + principalId, e.getMessage());
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
            logMap.put("errorSend_" + user.getPrincipalId(), user.getEmailAddressUnmasked() + ": " + e.getMessage());
          }
        }
  
      }
      
      logMap.put("sendActionListEmailSuccess", true);
    } catch (RuntimeException re) {
      logMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperClientUtils.MapToString(logMap));
      }
    }
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
