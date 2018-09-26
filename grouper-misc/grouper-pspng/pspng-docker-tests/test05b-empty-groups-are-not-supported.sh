#!/bin/bash
#
# description: test05b - empty-groups-are-not-supported
# configs: posix-groups
#
# This test takes verifies that empty posix groups are not created before members are added
# when supportsEmptyGroups is false
#

set -o errexit
set -o pipefail
set -o nounset

ME=$(basename "$0")

# Functions for test harness: read_test_config_files, test_start, start_docker, etc
# (This also pulls in functions.bash (log_always, temp-file functions, etc)
. "$(dirname "${BASH_SOURCE[0]}")/functions-pspng-testing.bash"


###
# Pull-in shell functions for generating config and verifying provisioning
# These are usually specific for different provisioning endpoints
# And the same 
#   Config Building: output_log4j_properties, output_grouper_loader_properties
#   Hooks: test_is_starting, test_is_ending
#   Provisioning verification: validate_provisioning <group> <correct members (comma-separated, alphabetical)>
# Defines: $flavor
read_test_config_files "${1:-posix-groups}"


# Tell the provisioner that empty groups are not supported
tweak_grouper_config() {
  echo "changeLog.consumer.pspng1.supportsEmptyGroups = false" >>  "$GROUPER_CONFIG_DIR/grouper-loader.properties"
}


# Note that the test is starting, saves start-time, etc
test_start "$ME" "Pspng ($flavor): testing that empty groups are not created"

################
## CONFIGURE GROUPER
## This will populate and define GROUPER_CONFIG_DIR

create_grouper_daemon_config

################
## START DOCKER

start_docker "${ME}_$flavor"


wait_for_grouper_daemon_to_be_running

create_test_folder

mark_test_folder_for_provisioning

create_group1_and_group2

# Check that no empty Group1 was provisioned
  await_changelog_catchup

  x=$(run_in_grouper_daemon myldapsearch -b dc=example,dc=edu "(&(objectclass=posixgroup)(cn=posix-$GROUP1_NAME))" memberUid)
  assert_empty "$x" "Posix group should not be found before members were added when empty groups are not supported"


add_members_to_group1 banderson agasper bbrown705
await_changelog_catchup

validate_provisioning "$GROUP1_NAME" "agasper,banderson,bbrown705"

add_group1_to_group2
await_changelog_catchup

validate_provisioning "$GROUP2_NAME" "agasper,banderson,bbrown705"

test_step "Emptying test groups to make sure ldap groups that require members are deleted"
run_in_grouper_daemon del-member --group "$GROUP1_NAME" banderson agasper bbrown705
await_changelog_catchup

for GROUP in "$GROUP1_NAME" "$GROUP2_NAME"
do
  x=$(run_in_grouper_daemon myldapsearch -b dc=example,dc=edu "(&(objectclass=posixgroup)(cn=posix-$GROUP))" memberUid | run_in_grouper_daemon noAttributeLabels)
  assert_empty "$x" "Posix group should not be found after members were all removed when empty groups are not supported"
done

#make sure extra groups were not provisioned
validate_deprovisioning "$UNPROVISIONED_GROUP_NAME"
wrap_up
assert_empty "$ERRORS" "Check for exceptions in grouper_error.log"
test_success

