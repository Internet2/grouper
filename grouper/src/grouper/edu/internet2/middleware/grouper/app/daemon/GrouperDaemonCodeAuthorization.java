/**
 * Copyright 2026 Internet2
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
package edu.internet2.middleware.grouper.app.daemon;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadataType;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Decides who may add, edit or delete a daemon configuration from the UI.
 * <p>
 * The daemon screens are open to members of uiV2.admin.daemonJobs.must.be.in.group, who are not
 * meant to be sysadmins.  Daemons run in a root grouper session, so a daemon type that carries
 * code (a GSH or SQL script, a compiled template, a file in the container, an arbitrary job or
 * change log consumer class) or runs an arbitrary SQL query on a configured database lets whoever
 * writes its config act as GrouperSystem.
 * </p>
 * <p>
 * Rule (user decision 2026-11-10, replaces the earlier list of wheel-only types, which kept missing
 * types that run root code from config: ESB filters, notification templates, the incremental
 * loader, subject change SQL, change log to messaging, report destinations, the instrumentation
 * URL, messaging listener classes): members of the daemon group manage existing daemons but do not
 * author them.  They may view, run, enable and disable every daemon, and edit only its schedule
 * (quartzCron) and enabled setting, but not to a new expression (2026-11-12).  Adding a daemon, changing any other setting, and deleting a
 * daemon are wheel or root only.
 * </p>
 * <p>
 * Called from GrouperDaemonConfiguration insertConfig, editConfig and deleteConfig when fromUi is
 * true.  API and GSH callers (fromUi false) are not changed.  The subject is the one of the static
 * grouper session (the UI starts it as the logged-in user); no session means no.
 * </p>
 * <p>
 * Types and classes are matched by name, not by class literal, so the same file compiles on every
 * release from 5.0.0 on, including releases which lack some of these types.
 * </p>
 */
public class GrouperDaemonCodeAuthorization {

  /**
   * message shown on the daemon screen when a non sysadmin is refused.  plain text, not
   * externalized, so the patch does not need a grouper.text file
   */
  public static final String NOT_ALLOWED_MESSAGE = "Only a Grouper sysadmin (wheel or root) can add or delete a "
      + "daemon, or change settings other than its schedule, since daemons run as GrouperSystem";

  /**
   * the only settings (suffixes) a non sysadmin may change on an existing daemon
   */
  private static final Set<String> delegateEditableSuffixes = Collections.unmodifiableSet(
      new HashSet<String>(java.util.Arrays.asList("quartzCron", "enabled")));

  /**
   * simple class names of the daemon config types that only wheel or root may add, edit or
   * delete.  a subclass of one of these is also wheel only
   */
  private static final Set<String> wheelOnlyDaemonTypeSimpleNames;

  /**
   * job, consumer or publisher classes that run a script, SQL or a sync from a SQL query.  a
   * class-valued field of any daemon type set to one of these is wheel only, so a crafted post
   * cannot turn an ordinary daemon into one of these
   */
  private static final Set<String> wheelOnlyClassValues;

  /** simple class name of the notification daemon config type */
  private static final String NOTIFICATION_TYPE_SIMPLE_NAME = "GrouperDaemonOtherJobNotificationConfiguration";

  /** suffix of the notification population type, sqlQuery runs a SQL query */
  private static final String NOTIFICATION_POPULATION_TYPE_SUFFIX = "populationType";

  /** population type of a notification daemon which runs a SQL query */
  private static final String NOTIFICATION_POPULATION_TYPE_SQL = "sqlQuery";

  static {

    // daemon types which carry code or run SQL
    Set<String> types = new HashSet<String>();

    // gsh, sql, compiledJava, or a file in the container
    types.add("GrouperDaemonOtherJobScriptConfiguration");
    // change log consumer that runs a script
    types.add("GrouperDaemonChangeLogScriptConfiguration");
    // generic other job, any class which extends OtherJobBase
    types.add("GrouperDaemonOtherJobConfiguration");
    // generic change log consumer, any consumer class
    types.add("GrouperDaemonChangeLogConsumerConfiguration");
    // SQL query daemons, on any configured database including the Grouper one
    types.add("GrouperDaemonOtherJobCsvReportConfiguration");
    types.add("GrouperDaemonOtherJobSyncToGrouperFromSqlConfiguration");
    types.add("GrouperDaemonOtherJobSftpToSqlConfiguration");
    types.add("GrouperDaemonOtherJobLdapToSqlConfiguration");
    types.add("GrouperDaemonOtherJobTableSyncConfiguration");
    wheelOnlyDaemonTypeSimpleNames = Collections.unmodifiableSet(types);

    // the classes the above types identify themselves by
    Set<String> classes = new HashSet<String>();
    classes.add("edu.internet2.middleware.grouper.app.loader.OtherJobScript");
    classes.add("edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript");
    classes.add("edu.internet2.middleware.grouper.app.reports.GrouperCsvReportJob");
    classes.add("edu.internet2.middleware.grouper.app.syncToGrouper.SyncToGrouperFromSqlDaemon");
    classes.add("edu.internet2.middleware.grouper.app.sqlSync.GrouperSftpToSqlJob");
    classes.add("edu.internet2.middleware.grouper.app.ldapToSql.LdapToSqlSyncDaemon");
    classes.add("edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob");
    wheelOnlyClassValues = Collections.unmodifiableSet(classes);
  }

  /**
   * whether a subject may add, edit or delete the wheel only daemon types
   * @param subject null means no
   * @return true if wheel or root
   */
  public static boolean isSubjectAllowedToEditWheelOnlyDaemons(Subject subject) {
    return subject != null && PrivilegeHelper.isWheelOrRoot(subject);
  }

  /**
   * whether the subject of the current static grouper session may add, edit or delete the wheel
   * only daemon types.  decided from the session, never from the request.  no session means no
   * @return true if wheel or root
   */
  public static boolean isCurrentSessionAllowedToEditWheelOnlyDaemons() {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    return grouperSession != null && isSubjectAllowedToEditWheelOnlyDaemons(grouperSession.getSubject());
  }

  /**
   * whether a daemon config type is wheel only to add, edit or delete (the type itself, or a
   * superclass, is in the list)
   * @param grouperDaemonConfiguration
   * @return true if wheel only
   */
  public static boolean isWheelOnlyDaemonType(GrouperDaemonConfiguration grouperDaemonConfiguration) {
    return grouperDaemonConfiguration != null && isTypeOrSuperclassNamed(grouperDaemonConfiguration, wheelOnlyDaemonTypeSimpleNames);
  }

  /**
   * whether a class name is a job, consumer or publisher class that runs a script or SQL
   * @param className
   * @return true if wheel only
   */
  public static boolean isWheelOnlyClassValue(String className) {
    return className != null && wheelOnlyClassValues.contains(className.trim());
  }

  /**
   * whether this daemon config, with the values it has now (posted values for an insert or edit,
   * the saved values for a delete), is wheel only: its type is in the list, it is a notification
   * daemon which runs a SQL query (now or before this edit), or one of its class-valued fields
   * names a script or SQL class
   * @param grouperDaemonConfiguration
   * @return true if only wheel or root may add, edit or delete it
   */
  public static boolean isWheelOnly(GrouperDaemonConfiguration grouperDaemonConfiguration) {

    if (grouperDaemonConfiguration == null) {
      return false;
    }

    if (isWheelOnlyDaemonType(grouperDaemonConfiguration)) {
      return true;
    }

    // a notification daemon can run a sql query, check the value in the form and the saved one,
    // so that a delegate cannot edit the query of a sql notification by flipping the type
    if (isTypeOrSuperclassNamed(grouperDaemonConfiguration, Collections.singleton(NOTIFICATION_TYPE_SIMPLE_NAME))) {
      GrouperConfigurationModuleAttribute populationTypeAttribute = grouperDaemonConfiguration.retrieveAttributes().get(NOTIFICATION_POPULATION_TYPE_SUFFIX);
      if (populationTypeAttribute != null
          && StringUtils.equals(NOTIFICATION_POPULATION_TYPE_SQL, StringUtils.trim(populationTypeAttribute.getValueOrExpressionEvaluation()))) {
        return true;
      }
      if (StringUtils.equals(NOTIFICATION_POPULATION_TYPE_SQL, StringUtils.trim(savedValue(grouperDaemonConfiguration,
          grouperDaemonConfiguration.getConfigItemPrefix() + NOTIFICATION_POPULATION_TYPE_SUFFIX)))) {
        return true;
      }
    }

    // a class-valued field (e.g. the job class or the change log publisher class) which names a
    // script or sql class makes the daemon one of the wheel only kinds once saved
    for (GrouperConfigurationModuleAttribute attribute : grouperDaemonConfiguration.retrieveAttributes().values()) {
      if (isClassValued(attribute) && isWheelOnlyClassValue(attribute.getValueOrExpressionEvaluation())) {
        return true;
      }
    }
    return false;
  }

  /**
   * check an insert or edit from the UI.  For a non sysadmin: an insert is refused; an edit is allowed
   * only if, compared to what is saved, nothing but the schedule (quartzCron) or enabled setting would
   * change (every field the edit would save or remove, and any other saved key under the daemon's
   * prefix, which an edit removes).  On refusal an error is added and the caller saves nothing.
   * @param grouperDaemonConfiguration with the posted values populated
   * @param isInsert true for insert, false for edit
   * @param errorsToDisplay the error is added here
   * @return true if allowed
   */
  public static boolean checkInsertOrEditFromUi(GrouperDaemonConfiguration grouperDaemonConfiguration,
      boolean isInsert, List<String> errorsToDisplay) {

    if (isCurrentSessionAllowedToEditWheelOnlyDaemons()) {
      return true;
    }

    boolean allowed = !isInsert && grouperDaemonConfiguration != null
        && onlyDelegateEditableSettingsChange(grouperDaemonConfiguration);

    if (!allowed && errorsToDisplay != null) {
      errorsToDisplay.add(NOT_ALLOWED_MESSAGE);
    }
    return allowed;
  }

  /**
   * @param grouperDaemonConfiguration posted values populated
   * @return true if the edit changes only quartzCron or enabled
   */
  static boolean onlyDelegateEditableSettingsChange(GrouperDaemonConfiguration grouperDaemonConfiguration) {
    String prefix = grouperDaemonConfiguration.getConfigItemPrefix();
    Set<String> attributePropertyNames = new HashSet<String>();
    for (java.util.Map.Entry<String, GrouperConfigurationModuleAttribute> entry : grouperDaemonConfiguration.retrieveAttributes().entrySet()) {
      GrouperConfigurationModuleAttribute attribute = entry.getValue();
      String propertyName = GrouperUtil.stripSuffix(attribute.getFullPropertyName(), ".elConfig");
      attributePropertyNames.add(propertyName);
      if (delegateEditableSuffixes.contains(entry.getKey())) {
        // the schedule and enabled setting may change, but not to a new expression: an
        // expression would be saved as <key>.elConfig, which the config loader evaluates with
        // static class access in every JVM.  Releases without GRP-7386 still take the
        // expression language checkbox from the request, so check it here too.  Keeping an
        // expression a sysadmin saved (unchanged) is fine
        if (attribute.isExpressionLanguage()
            && !StringUtils.equals(valueTheEditWouldSave(attribute), savedValueForCompare(grouperDaemonConfiguration, propertyName))) {
          return false;
        }
        continue;
      }
      // a password field comes back to the form as a placeholder; posting the placeholder keeps the saved value
      if (attribute.isPassword() && StringUtils.equals(edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate.ESCAPED_PASSWORD,
          StringUtils.trim(attribute.getValue()))) {
        continue;
      }
      if (!StringUtils.equals(valueTheEditWouldSave(attribute), savedValueForCompare(grouperDaemonConfiguration, propertyName))) {
        return false;
      }
    }
    // an edit also removes every other saved key under the prefix
    if (!StringUtils.isBlank(prefix)) {
      for (String key : GrouperUtil.nonNull(grouperDaemonConfiguration.retrieveConfigurationKeysByPrefix(prefix))) {
        String propertyName = GrouperUtil.stripSuffix(key, ".elConfig");
        if (attributePropertyNames.contains(propertyName)
            || (propertyName.startsWith(prefix) && delegateEditableSuffixes.contains(propertyName.substring(prefix.length())))) {
          continue;
        }
        if (savedValueForCompare(grouperDaemonConfiguration, propertyName) != null) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * what editConfig would save for this field: null if it would be removed, "EL:" + script for an
   * expression, else the value
   * @param attribute
   * @return the value
   */
  private static String valueTheEditWouldSave(GrouperConfigurationModuleAttribute attribute) {
    if (!(attribute.isHasValue() && attribute.getConfigItemMetadata() != null
        && attribute.getConfigItemMetadata().isSaveToDb() && attribute.isShow())) {
      return null;
    }
    String value = attribute.isExpressionLanguage() ? "EL:" + StringUtils.trimToEmpty(attribute.getExpressionLanguageScript())
        : StringUtils.trimToNull(attribute.getValue());
    return value;
  }

  /**
   * the saved value in the same form as valueTheEditWouldSave
   * @param grouperDaemonConfiguration
   * @param propertyName without .elConfig
   * @return the value or null
   */
  private static String savedValueForCompare(GrouperDaemonConfiguration grouperDaemonConfiguration, String propertyName) {
    String el = StringUtils.trimToNull(savedValue(grouperDaemonConfiguration, propertyName + ".elConfig"));
    if (el != null) {
      return "EL:" + el;
    }
    return StringUtils.trimToNull(savedValue(grouperDaemonConfiguration, propertyName));
  }

  /**
   * check a delete from the UI.  only wheel or root may delete a daemon
   * @param grouperDaemonConfiguration the saved daemon
   * @throws InsufficientPrivilegeException if not allowed
   */
  public static void assertDeleteFromUiAllowed(GrouperDaemonConfiguration grouperDaemonConfiguration) {
    if (isCurrentSessionAllowedToEditWheelOnlyDaemons()) {
      return;
    }
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    throw new InsufficientPrivilegeException(NOT_ALLOWED_MESSAGE + ", subject: "
        + (grouperSession == null ? null : GrouperUtil.subjectToString(grouperSession.getSubject()))
        + ", daemon config: " + (grouperDaemonConfiguration == null ? null : grouperDaemonConfiguration.getClass().getSimpleName()
        + ", " + grouperDaemonConfiguration.getConfigId()));
  }

  /**
   * whether an attribute holds a class name
   * @param attribute
   * @return true if valueType class
   */
  private static boolean isClassValued(GrouperConfigurationModuleAttribute attribute) {
    return attribute != null && attribute.getConfigItemMetadata() != null
        && attribute.getConfigItemMetadata().getValueType() == ConfigItemMetadataType.CLASS;
  }

  /**
   * the saved value of a property in the daemon's config file (database or files), or null
   * @param grouperDaemonConfiguration
   * @param propertyName
   * @return the value
   */
  private static String savedValue(GrouperDaemonConfiguration grouperDaemonConfiguration, String propertyName) {
    if (StringUtils.isBlank(propertyName)) {
      return null;
    }
    // the raw saved value from the config's properties (files and database), not propertyValueString:
    // that refuses keys ending in .elConfig and evaluates expressions, and the edit compares raw values
    return grouperDaemonConfiguration.getConfigFileName().getConfig().properties().getProperty(propertyName);
  }

  /**
   * whether the class of the object, or a superclass, has one of these simple names
   * @param object
   * @param simpleNames
   * @return true if so
   */
  private static boolean isTypeOrSuperclassNamed(Object object, Set<String> simpleNames) {
    Class<?> theClass = object.getClass();
    while (theClass != null && theClass != Object.class) {
      if (simpleNames.contains(theClass.getSimpleName())) {
        return true;
      }
      theClass = theClass.getSuperclass();
    }
    return false;
  }

}
