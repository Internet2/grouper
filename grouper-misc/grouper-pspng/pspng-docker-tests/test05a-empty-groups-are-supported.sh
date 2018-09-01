#!/bin/bash
#
# description: test05a - empty-groups-are-supported
# configs: posix-groups
#
# This test takes verifies that empty posix groups are created before members are added
# and that an empty posix group remains after all the members are removed
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


# Note that the test is starting, saves start-time, etc
test_start "$ME" "Pspng ($flavor): testing that empty groups are created"

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

# Check that an empty Group1 was provisioned
  await_changelog_catchup
  validate_provisioning "$GROUP1_NAME" ""

add_members_to_group1 banderson agasper bbrown705
await_changelog_catchup

validate_provisioning "$GROUP1_NAME" "agasper,banderson,bbrown705"

add_group1_to_group2
await_changelog_catchup

validate_provisioning "$GROUP2_NAME" "agasper,banderson,bbrown705"

test_step "Emptying test groups to make sure ldap groups that do not require members remain and are empty"
run_in_grouper_daemon del-member --group "$GROUP1_NAME" banderson agasper bbrown705
await_changelog_catchup

validate_provisioning "$GROUP1_NAME" ""
validate_provisioning "$GROUP2_NAME" ""

wrap_up
assert_empty "$ERRORS" "Check for exceptions in grouper_error.log"
test_success
