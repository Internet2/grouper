#!/bin/bash
#
# description: test07d - tests that full-sync cleans up extra groups when there are more than 50 provisioned groups
#   
# configs: ldap-attributes, posix-groups, goun-groups, posix-groups-bushy, posix-groups-bushy-dnsearch
#
# Full-Sync Group-Cleanup had a bug where it did not correctly break the provisioned groups
# into bite-sized chunks. This test exercises that bug and verifies that it is fixed.
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

tweak_grouper_config() {
  add_full_sync_starter 
}


# Note that the test is starting, saves start-time, etc
test_start "$ME" "Pspng ($flavor): provisioning groups and membership changes"

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

run_in_grouper_daemon create-groups --member banderson $(for ((i=1;i<=250;i++)); do echo $TEST_FOLDER:group-$i; done)

#make sure everything is right
await_changelog_catchup

# check provisioning of a couple groups
validate_provisioning "$TEST_FOLDER:group-1" banderson
validate_provisioning "$TEST_FOLDER:group-70" banderson

test_step "Waiting for at least one full-sync process to start and stop"
await_full_sync

CLEANUP_LINES=$(run_in_grouper_daemon egrep_ 'groups that we should delete|values that should be purged' /opt/grouper/grouper.apiBinary/logs/grouper_error.log)
assert_not_empty "$CLEANUP_LINES" "Expected to see some group-cleanup lines"

CLEANUP_LINES_NOT_ZERO_GROUPS=$(egrep_ -v "There are 0 " <<<"$CLEANUP_LINES")
assert_empty "$CLEANUP_LINES_NOT_ZERO_GROUPS" "Did not expect to see any groups get cleaned up"

# TODO: Should try to create an extra group and make sure cleanup happens



#make sure extra groups were not provisioned
validate_deprovisioning "$UNPROVISIONED_GROUP_NAME"
wrap_up
assert_empty "$ERRORS" "Check for exceptions in grouper_error.log"
test_success
