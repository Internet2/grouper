#!/bin/bash
#
# description: test14a - cache-size reading
# configs: ldap-attributes
#
# This test makes sure that cache parameters specified in grouper-loader.properties are used properly
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

# Some unique values so we can find them in logs
GROUPER_SUBJECT_CACHE_SIZE=${GROUPER_SUBJECT_CACHE_SIZE:-111666} #grouperSubjectCacheSize
GROUPER_GROUP_CACHE_SIZE=${GROUPER_GROUP_CACHE_SIZE:-111777}   #grouperGroupCacheSize
TARGET_SYSTEM_USER_CACHE_SIZE=${TARGET_SYSTEM_USER_CACHE_SIZE:-111888} #targetSystemUserCacheSize
TARGET_SYSTEM_GROUP_CACHE_SIZE=${TARGET_SYSTEM_GROUP_CACHE_SIZE:-111999}   #targetSystemGroupCacheSize

# Note that the test is starting, saves start-time, etc
test_start "$ME" "Pspng ($flavor): cache configuration"

# This adds known cache settings to grouper-loader.properties
#
tweak_grouper_config() {
  local DIR="${1:?USAGE: tweak_grouper_config <directory>}"

  echo "changeLog.consumer.pspng1.grouperSubjectCacheSize=$GROUPER_SUBJECT_CACHE_SIZE" >> $DIR/grouper-loader.properties
  echo "changeLog.consumer.pspng1.grouperGroupCacheSize=$GROUPER_GROUP_CACHE_SIZE" >> $DIR/grouper-loader.properties
  echo "changeLog.consumer.pspng1.targetSystemUserCacheSize=$GROUPER_SUBJECT_CACHE_SIZE" >> $DIR/grouper-loader.properties
  echo "changeLog.consumer.pspng1.targetSystemGroupCacheSize=$GROUPER_GROUP_CACHE_SIZE" >> $DIR/grouper-loader.properties
}



################
## CONFIGURE GROUPER
## This will populate and define GROUPER_CONFIG_DIR, and will use tweak_grouper_config 
## defined above

create_grouper_daemon_config

################
## START DOCKER

start_docker "${ME}_$flavor"

# make sure PSPNG has been initialized (by a change in grouper creating a changelog event)

create_test_folder
await_changelog_catchup

# Make sure logs indicate right Group and Subject cache sizes were used

GSIZE=$(run_in_grouper_daemon bash -c "egrep 'Setting grouperGroupCacheSize to'  $API/logs/grouper_error.log | head -1 | awk '{print \$NF}'")
SSIZE=$(run_in_grouper_daemon bash -c "egrep 'Setting grouperSubjectCacheSize to'  $API/logs/grouper_error.log | head -1 | awk '{print \$NF}'")

assert_equals "$SUBJECT_CACHE_SIZE" "$SSIZE" "grouperSubjectCacheSize"
assert_equals "$GROUP_CACHE_SIZE" "$GSIZE" "grouperGroupCacheSize"

wrap_up
assert_empty "$ERRORS" "Check for exceptions in grouper_error.log"
test_success

