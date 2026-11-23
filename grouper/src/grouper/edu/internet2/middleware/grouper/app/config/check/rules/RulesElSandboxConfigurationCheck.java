package edu.internet2.middleware.grouper.app.config.check.rules;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.rules.RuleElSysadminCheck;
import edu.internet2.middleware.grouper.rules.RuleThen;
import edu.internet2.middleware.grouper.rules.RuleThenEnum;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * GRP-7596: with rules.allowUnrestrictedEl off (the default), rule if/then expression language and
 * send-email rules with more than the predefined variables run in the restricted JEXL sandbox.  Warn
 * when rules exist whose expressions use something the sandbox refuses (constructors, class objects,
 * the group/member/membership/subject/rule objects), since they fail every time they fire.  The fix is
 * to set the property (if a sysadmin wrote them and they are wanted) or to change or delete the rules.
 *
 * The syntax test is a heuristic (RuleElSysadminCheck.usesSandboxRefusedSyntax), so a rule it does not
 * list can still fail; the runtime error names the property too.
 */
public class RulesElSandboxConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * cap on how many rule ids to list in the finding message
   */
  private static final int MAX_RULES_LISTED = 20;

  @Override
  public String getName() {
    return "rulesElSandbox";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    // switch on: nothing is sandboxed that a sysadmin wrote
    if (RuleElSysadminCheck.allowUnrestrictedEl()) {
      return results;
    }

    // rule assign id (the rule) of every rule whose expression would be refused, in order
    Set<String> refusedRuleIds = new LinkedHashSet<String>();

    // if/then EL: every value of those two attributes, with the rule they belong to
    String elSql = "select gaa_el.owner_attribute_assign_id, gaav_el.value_string "
        + " from grouper_attribute_assign gaa_el "
        + " join grouper_attribute_def_name gadn_el on gadn_el.id = gaa_el.attribute_def_name_id "
        + " join grouper_attribute_assign_value gaav_el on gaav_el.attribute_assign_id = gaa_el.id "
        + " where gadn_el.name in (?, ?) and gaav_el.value_string is not null and gaa_el.enabled = 'T' "
        + " order by gaa_el.owner_attribute_assign_id";
    List<Object[]> elRows = new GcDbAccess().sql(elSql).addBindVar(RuleUtils.ruleIfConditionElName())
        .addBindVar(RuleUtils.ruleThenElName()).selectList(Object[].class);
    for (Object[] row : elRows) {
      if (row[1] != null && RuleElSysadminCheck.usesSandboxRefusedSyntax(String.valueOf(row[1]))) {
        refusedRuleIds.add(String.valueOf(row[0]));
      }
    }

    // send-email rules: recipient, subject and body.  Only ones that are not predefined-only run
    // unrestricted when the switch is on, so only those are listed.
    String emailSql = "select gaa_then.owner_attribute_assign_id, gadn_arg.name, gaav_arg.value_string "
        + " from grouper_attribute_assign gaa_then "
        + " join grouper_attribute_def_name gadn_then on gadn_then.id = gaa_then.attribute_def_name_id "
        + " join grouper_attribute_assign_value gaav_then on gaav_then.attribute_assign_id = gaa_then.id "
        + " join grouper_attribute_assign gaa_arg on gaa_arg.owner_attribute_assign_id = gaa_then.owner_attribute_assign_id "
        + " join grouper_attribute_def_name gadn_arg on gadn_arg.id = gaa_arg.attribute_def_name_id "
        + " join grouper_attribute_assign_value gaav_arg on gaav_arg.attribute_assign_id = gaa_arg.id "
        + " where gadn_then.name = ? and lower(gaav_then.value_string) = 'sendemail' and gaa_then.enabled = 'T' "
        + " and gadn_arg.name in (?, ?, ?) "
        + " order by gaa_then.owner_attribute_assign_id";
    List<Object[]> emailRows = new GcDbAccess().sql(emailSql).addBindVar(RuleUtils.ruleThenEnumName())
        .addBindVar(RuleUtils.ruleThenEnumArg0Name()).addBindVar(RuleUtils.ruleThenEnumArg1Name())
        .addBindVar(RuleUtils.ruleThenEnumArg2Name()).selectList(Object[].class);

    // rebuild each rule's then from its rows, then decide per rule
    String currentRuleId = null;
    RuleThen ruleThen = null;
    for (int i = 0; i <= emailRows.size(); i++) {
      Object[] row = i < emailRows.size() ? emailRows.get(i) : null;
      String ruleId = row == null ? null : String.valueOf(row[0]);
      if (currentRuleId != null && !currentRuleId.equals(ruleId)) {
        if (RuleElSysadminCheck.sendEmailRunsUnrestricted(ruleThen)
            && (RuleElSysadminCheck.usesSandboxRefusedSyntax(ruleThen.getThenEnumArg0())
                || RuleElSysadminCheck.usesSandboxRefusedSyntax(ruleThen.getThenEnumArg1())
                || RuleElSysadminCheck.usesSandboxRefusedSyntax(ruleThen.getThenEnumArg2()))) {
          refusedRuleIds.add(currentRuleId);
        }
      }
      if (row == null) {
        break;
      }
      if (!ruleId.equals(currentRuleId)) {
        currentRuleId = ruleId;
        ruleThen = new RuleThen(null, RuleThenEnum.sendEmail.name(), null, null, null);
      }
      String argName = String.valueOf(row[1]);
      String value = row[2] == null ? null : String.valueOf(row[2]);
      if (RuleUtils.ruleThenEnumArg0Name().equals(argName)) {
        ruleThen.setThenEnumArg0(value);
      } else if (RuleUtils.ruleThenEnumArg1Name().equals(argName)) {
        ruleThen.setThenEnumArg1(value);
      } else {
        ruleThen.setThenEnumArg2(value);
      }
    }

    if (refusedRuleIds.isEmpty()) {
      return results;
    }

    StringBuilder listed = new StringBuilder();
    int count = 0;
    for (String ruleId : refusedRuleIds) {
      if (++count > MAX_RULES_LISTED) {
        listed.append(", and ").append(refusedRuleIds.size() - MAX_RULES_LISTED).append(" more");
        break;
      }
      if (listed.length() > 0) {
        listed.append(", ");
      }
      listed.append(ruleId);
    }

    results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
        ConfigFileName.GROUPER_PROPERTIES, RuleElSysadminCheck.ALLOW_UNRESTRICTED_EL_PROPERTY, "false",
        refusedRuleIds.size() + " rule(s) use expression language that the restricted sandbox does not allow "
            + "(a constructor, a class object, or the group, member, membership, subject or rule objects), so "
            + "they fail when they fire.  Rule ids: " + listed + ".",
        "If a Grouper sysadmin wrote these rules and they are wanted, set "
            + RuleElSysadminCheck.ALLOW_UNRESTRICTED_EL_PROPERTY + " = true.  Otherwise change or delete them."));

    return results;
  }

}
