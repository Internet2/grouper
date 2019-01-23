#!/bin/bash
#
# description: test11c - Prefix that has regex-relevant characters
# configs: ldap-attributes
#
# This pspng config is to test https://bugs.internet2.edu/jira/browse/GRP-1911
# where the Prefix was "g|" which meant that all attributes were considered
# matching
# allProvisionedValuesPrefix=g| AND provisionedAttributeValueFormat = g|${group.name}
#
# This test makes sure all extra values are removed from the attribute
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
read_test_config_files "${1:-ldap-attributes}"

tweak_grouper_config() {
  DIR="$1"
  add_full_sync_starter 

  # Change the eduPersonEntitlement prefix so validate_provisioning works
  EDU_PERSON_ENTITLEMENT_PREFIX="g|"

  sed -i .bak 's:allProvisionedValuesPrefix.*:allProvisionedValuesPrefix=g|:' $DIR/grouper-loader.properties
  sed -i .bak 's:provisionedAttributeValueFormat.*:provisionedAttributeValueFormat=g|${group.name}:' $DIR/grouper-loader.properties
}


# Note that the test is starting, saves start-time, etc
test_start "$ME" "Pspng ($flavor): Ldap-Provisioning with full control of attribute"

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
add_members_to_group1 banderson agasper bbrown705
add_group1_to_group2

#make sure everything is right
await_changelog_catchup

validate_provisioning "$GROUP1_NAME" "agasper,banderson,bbrown705"
validate_provisioning "$GROUP2_NAME" "agasper,banderson,bbrown705"

test_step "Direct LDAP change: adding an attribute value (xyz:pdq) that does not start with g|"

user_dn=$(run_in_grouper_daemon bash -c "myldapsearch -b dc=example,dc=edu uid=agasper dn | noAttributeLabels --show-dn")
assert_not_empty "$user_dn" "User expected to exist: agasper"

LDIF="dn: $user_dn
add: eduPersonEntitlement
eduPersonEntitlement: xyz:pdq"

run_in_grouper_daemon myldapmodify <<< "$LDIF"

test_step "Waiting for at least one full-sync process to start and stop"
await_full_sync

test_step "Checking that xyz:pdq is still an attribute value"
x=$(run_in_grouper_daemon bash -c "myldapsearch -b "$user_dn" eduPersonEntitlement | grep_ xyz:pdq")

assert_not_empty "$x" "Expected xyz:pdq to remain an eduPersonEntitlement value"

#make sure extra groups were not provisioned
validate_deprovisioning "$UNPROVISIONED_GROUP_NAME"
wrap_up
assert_empty "$ERRORS" "Check for exceptions in grouper_error.log"
test_success
