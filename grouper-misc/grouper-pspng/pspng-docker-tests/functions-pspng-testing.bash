# These are utilities specific for Grouper/PSPNG testing


. "$(dirname "${BASH_SOURCE[0]}")/functions-testharness.bash"


init_test_scenario_variables() 
{
  export TEST_FOLDER_TOP=${TEST_FOLDER_TOP:-parentFolder}
  export TEST_FOLDER=${TEST_FOLDER:-${TEST_FOLDER_TOP}:provisionedFolder}

  export GROUP1_NAME=${GROUP1_NAME:-${TEST_FOLDER}:group1}
  export GROUP2_NAME=${GROUP2_NAME:-${TEST_FOLDER}:group2}
  export UNPROVISIONED_GROUP_NAME=${UNPROVISIONED_GROUP_NAME:-${TEST_FOLDER_TOP}:unprovisioned-group}

  #Remove everything up to the last colon
  export GROUP1_EXTENSION=$(sed 's/.*://' <<<"$GROUP1_NAME")
  export GROUP2_EXTENSION=$(sed 's/.*://' <<<"$GROUP2_NAME")
  export UNPROVISIONED_GROUP_EXTENSION=$(sed 's/.*://' <<<"$UNPROVISIONED_GROUP_NAME")

  export PROVISIONER1_NAME=${PROVISIONER1_NAME:-pspng1}
}


wait_for_grouper_daemon_to_be_running()
{
  GROUPER_LOG_LINE_NUMBER=0

  test_step "Waiting for daemons to start"
  run_in_grouper_daemon await-ldap-server || test_failure "LDAP server never started"
  run_in_grouper_daemon await-mysql-server || test_failure "MySql server never started"
  run_in_grouper_daemon await-grouper-loader || test_failure "Grouper loader never started"

  test_is_starting

  test_step "Waiting for changelog processing to complete (so changelog consumer will be initialized (so it will definitely see test-folder event))"
  run_in_grouper_daemon await-changelog-completion --log_line_number $GROUPER_LOG_LINE_NUMBER --time_limit_secs 90
  GROUPER_LOG_LINE_NUMBER=$(run_in_grouper_daemon cat $API/logs/grouper_error.log | wc -l)
}

create_test_folder() 
{
  init_test_scenario_variables

  test_step "Creating test folder(s)"
  run_in_grouper_daemon create-folder --folder_name "$TEST_FOLDER_TOP"
  run_in_grouper_daemon create-folder --folder_name "$TEST_FOLDER"
}


mark_test_folder_for_provisioning()
{
  await_changelog_catchup "so pspng can create provision_to attributes"

  test_step "Marking test folders for provisioning"
  run_in_grouper_daemon add-attribute --folder $TEST_FOLDER --attribute_name etc:pspng:provision_to --attribute_value $PROVISIONER1_NAME
}


create_group1_and_group2() 
{
  init_test_scenario_variables

  test_step "Creating group 1: $GROUP1_NAME"
  run_in_grouper_daemon create-group --group_name ${GROUP1_NAME}
  test_step "Creating group  2: $GROUP2_NAME"
  run_in_grouper_daemon create-group --group_name ${GROUP2_NAME}

  test_step "Creating unprovisioned group: $UNPROVISIONED_GROUP_NAME"
  run_in_grouper_daemon create-group --group_name ${UNPROVISIONED_GROUP_NAME}

  test_step "Adding member to unprovisioned group $UNPROVISIONED_GROUP_NAME: agasper"
  run_in_grouper_daemon add-member --group $UNPROVISIONED_GROUP_NAME agasper
}

add_members_to_group1()
{
  local members=${*:?Usage: add_members_to_group <space-separated list of members>}
  init_test_scenario_variables

  test_step "Adding members to group $GROUP1_NAME: $members"
  run_in_grouper_daemon add-member --group $GROUP1_NAME $members
}
  
add_group1_to_group2()
{
  init_test_scenario_variables

  test_step "Adding group1 ($GROUP1_NAME) to group2 ($GROUP2_NAME)"
  run_in_grouper_daemon add-nested-group --group $GROUP2_EXTENSION $GROUP1_EXTENSION
}

remove_members_from_group1()
{
  local members=${*:?Usage: remove_members_from_group1 <space-separated list of members>}
  init_test_scenario_variables

  test_step "Removing members from group $GROUP1_NAME: $members"
  run_in_grouper_daemon del-member --group $GROUP1_NAME $members
}

remove_members_from_group2()
{
  local members=${*:?Usage: remove_members_from_group2 <space-separated list of members>}
  init_test_scenario_variables

  test_step "Removing members from group $GROUP2_NAME: $members"
  run_in_grouper_daemon del-member --group $GROUP2_NAME $members
}

await_changelog_catchup()
{
  local message="${1:-after ${TEST_STEP:-}}"

  test_step "Waiting for changelog processing to complete ($message)"
  run_in_grouper_daemon await-changelog-completion --log_line_number $GROUPER_LOG_LINE_NUMBER --time_limit_secs 90
  GROUPER_LOG_LINE_NUMBER=$(run_in_grouper_daemon cat $API/logs/grouper_error.log | wc -l)
}

await_full_sync()
{
  local message="${1:-after ${TEST_STEP:-}}"

  test_step "Waiting for full-sync cycle to complete ($message)"
  run_in_grouper_daemon await-full-sync-cycle --log_line_number $GROUPER_LOG_LINE_NUMBER --time_limit_secs 90
  GROUPER_LOG_LINE_NUMBER=$(run_in_grouper_daemon cat $API/logs/grouper_error.log | wc -l)
}


