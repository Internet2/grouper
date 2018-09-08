#!/bin/bash
#
# description: test03a - using morphString to encrypt credentials
# configs: ldap-attributes
#
# This test does some basic provisioning (like test02*), but with a 
# morphString-protected ldap password
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


# Note that the test is starting, saves start-time, etc
test_start "$ME" "Pspng ($flavor): morphString password encryption"

# This switches ldap properties to use morphString
#
tweak_grouper_config() {
  local DIR="${1:?USAGE: tweak_grouper_config <directory>}"

  cat <<EOF > $DIR/morphString.properties

encrypt.key = abc123456789
encrypt.disableExternalFileLookup=false
EOF

  # This came from using the above morphString.properties and 
  # /opt/java/bin/java -jar /opt/grouper/grouper.apiBinary/lib/grouper/morphString.jar
  echo 'WodH8pSuV9MqORn/BZ9qpw==' > $DIR/password.morph

  sed -i .bak 's:secret:/opt/grouper/grouper.apiBinary/conf/password.morph:' $DIR/grouper-loader.properties  
}



################
## CONFIGURE GROUPER
## This will populate and define GROUPER_CONFIG_DIR, and will use tweak_grouper_config 
## defined above

create_grouper_daemon_config

################
## START DOCKER

start_docker "${ME}_$flavor"


wait_for_grouper_daemon_to_be_running

create_test_folder

mark_test_folder_for_provisioning

create_group1_and_group2

add_members_to_group1 banderson agasper bbrown705
await_changelog_catchup

validate_provisioning "$GROUP1_NAME" "agasper,banderson,bbrown705"

add_group1_to_group2
await_changelog_catchup

validate_provisioning "$GROUP2_NAME" "agasper,banderson,bbrown705"

#make sure extra groups were not provisioned
validate_deprovisioning "$UNPROVISIONED_GROUP_NAME"
wrap_up
assert_empty "$ERRORS" "Check for exceptions in grouper_error.log"
test_success

