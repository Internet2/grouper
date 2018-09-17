#!/bin/bash
#
# description: test09a - tests that see how incremental sync works after a rogue
# actor has already put a membership into place
#
# configs: ldap-attributes, posix-groups, goun-groups, posix-groups-bushy, posix-groups-bushy-dnsearch
#
# This test hacks an extra member into a group and makes sure that the incremental sync
# doesn't fail completely with attribute-already-exists errors
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
test_start "$ME" "Pspng ($flavor): incremental provisioning after rogue member additions"

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

#pre-add some users to group1 and group2 outside of grouper (rogue ldap additions)
log_always "Adding members the wrong way (directly in ldap)
directly_add_member "$GROUP1_NAME" kwhite
directly_add_member "$GROUP1_NAME" ddavis
directly_add_member "$GROUP2_NAME" ddavis

# This is what we expect in our kludged group memberships
validate_provisioning "$GROUP1_NAME" "agasper,banderson,bbrown705,ddavis,kwhite"
validate_provisioning "$GROUP2_NAME" "agasper,banderson,bbrown705,ddavis"


# Now, add kwhite and ddavis to the groups in grouper (the proper way)
log_always "Adding members the right way (in grouper)"
add_members_to_group1 kwhite ddavis

#make sure everything is right
await_changelog_catchup

#make sure everything has converged to the proper state
validate_provisioning "$GROUP1_NAME" "agasper,banderson,bbrown705,ddavis,kwhite"
validate_provisioning "$GROUP2_NAME" "agasper,banderson,bbrown705,ddavis,kwhite"

#make sure extra groups were not provisioned
validate_deprovisioning "$UNPROVISIONED_GROUP_NAME"
wrap_up
assert_empty "$ERRORS" "Check for exceptions in grouper_error.log"
test_success
