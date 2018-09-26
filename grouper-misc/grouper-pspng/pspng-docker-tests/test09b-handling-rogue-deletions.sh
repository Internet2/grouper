#!/bin/bash
#
# description: test09b - tests that see how incremental sync works after a rogue
# actor has already removed a membership from ldap
#
# configs: ldap-attributes, posix-groups, goun-groups, posix-groups-bushy, posix-groups-bushy-dnsearch
#
# This test hacks a removal of a member from a group and makes sure that the incremental sync
# doesn't fail completely with attribute-does-not-exist errors
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
test_start "$ME" "Pspng ($flavor): incremental provisioning after rogue member removals"

################
## CONFIGURE GROUPER
## This will populate and define GROUPER_CONFIG_DIR

create_grouper_daemon_config

run_after_parent_test test02a-basic-provisioning.sh

################
## START DOCKER

start_docker "${ME}_$flavor" 

init_test_scenario_variables
wait_for_grouper_daemon_to_be_running


#pre-remove a user from group1 outside of grouper (rogue ldap additions)
log_always "Removing member the wrong way (directly in ldap)"
directly_remove_member "$GROUP1_NAME" agasper

# This is what we expect in our kludged group memberships
validate_provisioning "$GROUP1_NAME" "banderson,bbrown705"
validate_provisioning "$GROUP2_NAME" "agasper,banderson,bbrown705"


# Now, remove agasper from group1 within grouper (the proper way)
log_always "Removing member the right way (in grouper)"
remove_members_from_group1 agasper 

#make sure everything is right
await_changelog_catchup

#make sure everything has converged to the proper state
validate_provisioning "$GROUP1_NAME" "banderson,bbrown705"
validate_provisioning "$GROUP2_NAME" "banderson,bbrown705"

#make sure extra groups were not provisioned
validate_deprovisioning "$UNPROVISIONED_GROUP_NAME"
wrap_up
assert_empty "$ERRORS" "Check for exceptions in grouper_error.log"
test_success
