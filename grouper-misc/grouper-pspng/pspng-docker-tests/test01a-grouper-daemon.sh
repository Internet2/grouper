#!/bin/bash
#
# description: test01a - grouper daemon sanity check
# configs: posix-groups
#
# This test sanity checks the test harness with just the pspng-development image
# (with no pspng provisioners defined)
# This test creates some groups and monitors the changelog, but doesn't provision anything
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
test_start "$ME" "Pspng ($flavor): groups and membership changes without provisioning"

################
## CONFIGURE GROUPER
## This will populate and define GROUPER_CONFIG_DIR

create_grouper_daemon_config

################
## START DOCKER

start_docker "${ME}_$flavor"

test_subject_source

wait_for_grouper_daemon_to_be_running

create_test_folder

create_group1_and_group2

add_members_to_group1 banderson agasper bbrown705
await_changelog_catchup

add_group1_to_group2
await_changelog_catchup

wrap_up
assert_empty "$ERRORS" "Check for exceptions in grouper_error.log"
test_success
