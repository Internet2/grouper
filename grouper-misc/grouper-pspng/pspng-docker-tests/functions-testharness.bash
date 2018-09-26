# Goal: These are utility functions for running docker-based integration tests
# Reality: There are some grouper-specific tests that need to move from here
#          to functions-pspng-testing.bash


_D=$(dirname "${BASH_SOURCE[0]}")
. "$_D/functions.bash"

export API=/opt/grouper/grouper.apiBinary
export GSH=gsh_

PATH="$(absolute_dir "$_D")/scripts:$PATH"

# read_test_config_files
# Defines: $flavor from argument
# Pull-in shell functions from test-config.<flavor> for generating config and verifying provisioning
#   Config Building: output_log4j_properties, output_grouper_loader_properties
#   Hooks: test_is_starting, test_is_ending
#   Provisioning verification: validate_provisioning <group> <correct members (comma-separated, alphabetical)>
#
function read_test_config_files()
{
  flavor=${1:?Usage: initialize_test_config <test-config flavor>}
  [ -z "${T:-}" ] && tempdir_into_T

  if [ -r test-config.$flavor ]; then
    . test-config.$flavor
  elif [ -r $flavor ]; then
    . $flavor
  else
    fail "Could not find test-config.$flavor or $flavor"
  fi
}

# This defines GROUPER_CONFIG_DIR and fills it with 
# the files that should overwrite the default apiBinary/conf files
#
# This uses the following functions to build config files:
#   output_grouper_loader_properties
#   output_log4j_properties
#   output_sources_xml
#   tweak_grouper_config "$GROUPER_CONFIG_DIR"
create_grouper_daemon_config() 
{
  [ -z "${T:-}" ] && \
	fail "initialize_test_config must be used before setup_daemon_config"

  export GROUPER_CONFIG_DIR="$T/grouper-config"; mkdir "$GROUPER_CONFIG_DIR"

  if type -t output_grouper_loader_properties >/dev/null 
  then
    output_grouper_loader_properties \
	> "$GROUPER_CONFIG_DIR/grouper-loader.properties"
    echo "changeLog.changeLogTempToChangeLog.quartz.cron = 0/5 * * * * ?" \
	>> "$GROUPER_CONFIG_DIR/grouper-loader.properties"
  fi

  if type -t output_log4j_properties > /dev/null 
  then
    output_log4j_properties > "$GROUPER_CONFIG_DIR/log4j.properties"
  fi

  if type -t output_sources_xml > /dev/null 
  then
    output_sources_xml > "$GROPUER_CONFIG_DIR/sources.xml"
  fi

  if type -t tweak_grouper_config > /dev/null 
  then
    tweak_grouper_config "$GROUPER_CONFIG_DIR"
  fi

  export GROUPER_DAEMON_CONFIG_HASH=$(hash_directory_contents "$GROUPER_CONFIG_DIR")
  echo "Grouper config has hash: $GROUPER_DAEMON_CONFIG_HASH"
}


# This is a function that uses DOCKER_COMPOSE_CMD to run a command in grouper-daemon container
run_in_grouper_daemon()
{
  echo "$(date) Running command in daemon container: ${@}" 1>&2
  ${DOCKER_COMPOSE_CMD:?start_docker needs to be called first} exec -T grouper-daemon "${@}"
  local s=$?
  if [ $s -ne 0 ]; then
    echo "$(date) Command failed in daemon container: ${@}" 1>&2
    return $s
  fi
}


function run_after_parent_test()
{
  local parent_test="${1:?USAGE: run_after_parent_test <parent test>}"

  # Check to see if our parent test has already run by looking for its history file
  # If the history file doesn't exist, then parent test hasn't run
  local parent_test_file="$TEST_HISTORY_DIR/${parent_test}.${GROUPER_DAEMON_CONFIG_HASH}"
  if [ ! -r "$parent_test_file" ]; then
    log_always "Parent test hasn't run yet: Running $parent_test $flavor"
    KEEP_VOLUMES=yes $_D/$parent_test $flavor 2>&1 | sed "s/^/($parent_test)/"
    log_always "$parent_test has completed. $TEST_NAME will start if it was successful"
  fi
  
  # Fail if we can't find the parent's test file
  [ -r "$parent_test_file" ] || fail "Unable to find $parent_test_file even after running $parent_test"
  
  # Fail if our parent test failed
  local parent_result=$(grep_ RESULT= "$parent_test_file" | sed 's/.*=//' | sed 's/ //g')
  [ "$parent_result" != SUCCESS ] && fail "$TEST_NAME relies on $parent_test which failed"
  
  PARENT_VOLUME_SUFFIX=$(grep_ VOLUME_SUFFIX= "$parent_test_file" | sed 's/.*=//' | sed 's/ //g' )
  [ -n "$PARENT_VOLUME_SUFFIX" ] || fail "$parent_test file did not specify VOLUME_SUFFIX: $parent_test_file"

  log_always "Parent test succeeded. Starting $TEST_NAME now"
}


## RUN THE CONTAINER in the background and schedule its destruction for when we exit
## This defines DOCKER_COMPOSE_CMD to run docker-compose within the test's environment
## This also invokes test_is_starting once everything is running
##
## This also takes care of java debugging setup if DEBUG=yes (using GROUPER_LOADER_DEBUG_PORT)
function start_docker()
{
  local test_name_and_flavor="${1:?USAGE: start_docker <test name and flavor>}"

  [ -z "${GROUPER_CONFIG_DIR:-}" ] && fail "create_grouper_daemon_config must be run before start_docker"
  export DOCKER_COMPOSE_PROJECT=${test_name_and_flavor}.$$
  export VOLUME_SUFFIX=$DOCKER_COMPOSE_PROJECT

  test_start "$TEST_NAME" "$TEST_DESCRIPTION" 
  
  PARENT_VOLUME_SUFFIX=${PARENT_VOLUME_SUFFIX:-template}

  test_step "Clone docker data images from $PARENT_VOLUME_SUFFIX for suffix $DOCKER_COMPOSE_PROJECT"
  clone-data-templates --source_suffix ${PARENT_VOLUME_SUFFIX} --volume_suffix $DOCKER_COMPOSE_PROJECT

  # Write out some information about our Test
  export MY_TEST_HISTORY_FILE=$TEST_HISTORY_DIR/${TEST_NAME}.${GROUPER_DAEMON_CONFIG_HASH}
  cat <<EOF > $MY_TEST_HISTORY_FILE
NAME=$TEST_NAME
FLAVOR=$flavor
CONFIG_HASH=$GROUPER_DAEMON_CONFIG_HASH
VOLUME_SUFFIX=$DOCKER_COMPOSE_PROJECT
EOF

  local _D=$(absolute_dir $(dirname "${BASH_SOURCE[0]}"))

  # Defines 
  export DOCKER_COMPOSE_CMD="docker-compose --project-name $DOCKER_COMPOSE_PROJECT --file $_D/docker-compose/docker-compose.yml"

  test_step "Running containers"
  $DOCKER_COMPOSE_CMD up --detach

  add_cleanup_command cleanup_docker 
  $DOCKER_COMPOSE_CMD ps | sed 's/^/Docker containers running: /'

  if [ ${GROUPER_LOADER_DEBUG_PORT:-0} -ne 0 ]; then
    while [ -z "$(run_in_grouper_daemon  netstat -an | grep_ LISTEN | grep_ :8000)" ]; do
      echo "Waiting for java to be listening on debugging port"
      sleep 2
    done

    while [ -z "$(run_in_grouper_daemon netstat -an | grep_ ESTABLISHED | grep_ :8000)" ]; do
      echo "Java is waiting for a connection to debugging port 8000"
      sleep 2
    done
  fi

  test_is_starting
}


# Stop and clean up the containers if everything was successful
#   KEEP_VM=on-failure [default]: containers are kept when tests fail, otherwise they're deleted
#   KEEP_VM=yes: containers are kept
#   KEEP_VM=no: Always delete the containers
#
# If STOP_JAVA=yes, then java/grouper-daemon is killed even if container is kept around
#
function cleanup_docker()
{
  local CLEAN=
  case ${KEEP_VM:-on-failure} in
    yes)
      CLEAN=no
      ;;
    on-failure)
      if [ $EXIT_CODE -eq 0 ]; then
        CLEAN=yes
      else
        CLEAN=no
      fi
      ;;
    no)
      CLEAN=yes
      ;;
  esac

  if [ -n "${TEST_DIR:-}" ]; then
    log_always "Saving grouper_error.log to $TEST_DIR"
    docker cp $($DOCKER_COMPOSE_CMD ps -q grouper-daemon):/opt/grouper/grouper.apiBinary/logs/grouper_error.log "${TEST_DIR}"
  fi

  if [ "$CLEAN" = yes ]; then
    log_always "Cleaning up containers"
    $DOCKER_COMPOSE_CMD rm --stop --force 
    $DOCKER_COMPOSE_CMD down 
    if [ "${KEEP_VOLUMES:-}" != yes ]; then
      docker volume ls | grep_ -e "${VOLUME_SUFFIX}" | awk '{print $NF}' | xargs docker volume rm
    fi
  else
    if [ "${STOP_JAVA:-}" = yes ]; then
      log_always "Stopping java in grouper-daemon container: pkill -f java"
      run_in_grouper_daemon pkill -f java
    fi

    log_always "Not cleaning up container. Helpful docker commands:"
    echo "echo CLEAN; $DOCKER_COMPOSE_CMD rm --stop --force" 1>&2
    echo "echo SHELL; $DOCKER_COMPOSE_CMD exec grouper-daemon bash" 1>&2
    echo "echo GSH; $DOCKER_COMPOSE_CMD exec grouper-daemon /opt/grouper/grouper.apiBinary/bin/gsh" 1>&2
    echo "echo LOG; $DOCKER_COMPOSE_CMD exec -T grouper-daemon cat $API/logs/grouper_error.log </dev/null | less -In" 1>&2
  fi
}

#
# test_start <TEST_NAME> <TEST_DESCRIPTION>
# This marks the start of the test. Defines a few variables
#   TEST_NAME, TEST_DESCRIPTION, TEST_START_EPOCH=now
function test_start()
{
  local USAGE="test_start <name> <brief description>"
  [ $# -eq 2 ] || fail "$USAGE"

  export TEST_NAME=${1}
  export TEST_DESCRIPTION=${2}
  export TEST_START_EPOCH=$(date +%s)

  # Where information (volume names, result goes)
  export TEST_HISTORY_DIR=${TEST_HISTORY_DIR:-$T/test_history.d}
  [ ! -d "$TEST_HISTORY_DIR" ] && mkdir -p "$TEST_HISTORY_DIR"

  echo ====================================================================
  echo ====================================================================
  echo ====================================================================
  log_always "TEST STARTING: $TEST_NAME/$TEST_DESCRIPTION"

  test_step "Starting"
}

#
# test_step_done
# This logs the current step is done and how long it took
function test_step_done()
{
  if [ -n "${TEST_STEP:-}" ]; then
    TEST_STEP_DURATION=$(( $(date +%s) - TEST_STEP_START_EPOCH ))
    log_always "==== STEP DONE: [$TEST_STEP_DURATION secs] $TEST_STEP"
    log_always "-----------------------------------------------------"
    if [ -n "${container:-}" ]; then
      docker exec -T $container bash -c "echo '==== STEP DONE: [$TEST_STEP_DURATION secs] $TEST_STEP' >> $API/logs/grouper_error.log"
      docker exec -T $container bash -c "echo '-----------------------------------------------------' >> $API/logs/grouper_error.log"
    fi
    [ -z "${TEST_DIR:-}" ] || echo "$(date) STEP DONE: $TEST_STEP" >> $TEST_DIR/test_steps
  fi
}

# test_step <TEST_STEP> 
# Logs that previous step is done and that a testing step is being started
#   Logs go to: Screen, TEST_DIR/test_steps, container's grouper_error.log
function test_step()
{
  test_step_done

  TEST_STEP="${1:?test_step <step description>}"
  TEST_STEP_START_EPOCH=$(date +%s)
  echo ==================================================================================
  log_always "===== STEP STARTING: $TEST_STEP"

  [ -z "${TEST_DIR:-}" ] || echo "$(date) STEP STARTING: $TEST_STEP" >> $TEST_DIR/test_steps

  if [ -n "${container:-}" ]; then
    docker exec -T $container bash -c "echo '==== STEP STARTING: $TEST_STEP' >> $API/logs/grouper_error.log"
  fi
}
  

# test_success
# Logs that entire test has succeed, prints duration and exits
function test_success()
{
  local TEST_DURATION=$(($(date +%s) - TEST_START_EPOCH))
  log_always "TEST SUCCEEDED [$TEST_DURATION secs]: $TEST_NAME/$TEST_DESCRIPTION"
  test_step "DONE: SUCCESS"

  echo "RESULT=SUCCESS" >> $MY_TEST_HISTORY_FILE
}

# test_failure <REASON>
# Logs that test has failed and exits with an error
function test_failure()
{
  local REASON="$*"

  echo "RESULT=FAILURE" >> $MY_TEST_HISTORY_FILE
  echo "FAILURE_REASON=$REASON" >> $MY_TEST_HISTORY_FILE

  [ ${KEEP_VM:-on-failure} = on-failure ] && export KEEP_VM=yes

  local TEST_DURATION=$(($(date +%s) - TEST_START_EPOCH))
  log_error "TEST FAILED [$TEST_DURATION secs]: $REASON"

  test_step "DONE: FAILURE"

  wrap_up

  exit 1
}

# assert_equals <expected> <actual> <message>
# Utility function to compare two values (ignoring whitespace differences)
#  If the values are different, test_failure is called with a detailed message
#  If the values are the same, <message> is logged as successful
function assert_equals()
{
  local usage="USAGE: assert_equals <expected> <actual> <message>"
  [ $# -eq 3 ] || fail "$usage"

  local expected=${1}
  local actual=${2}
  local message=${3}

  # Trim values
  expected=$(echo -n "$expected" | sed -e 's/^ *//' -e 's/ *$//')
  actual=$(echo -n "$actual" | sed -e 's/^ *//' -e 's/ *$//')

  if [ "$expected" != "$actual" ]; then
    test_failure "$message. Expected: '$expected' Actual: '$actual'"
  else
    log_always "***SUCCESSFUL CHECK: $message"
  fi
}

# assert_empty "$VARIABLE" <message>
# Utility function to print a message and fail if a variable is not empty.
function assert_empty() 
{
  local usage="USAGE: assert_empty <string> <message>"
  [ $# -eq 2 ] || fail_usage "$usage"

  local should_be_empty="${1}"
  local message="${2}"

  if [ -z "$should_be_empty" ]; then
    log_always "***SUCCESSFUL CHECK: $message"
  else
    test_failure "$message: Expected string to be empty, but it was not: '$should_be_empty'"
  fi
}
  
# assert_not_empty "$VARIABLE" <message>
# Utility function to print a message and fail if a variable is empty.
function assert_not_empty() 
{
  local usage="USAGE: assert_not_empty <string> <message>"
  [ $# -eq 2 ] || fail_usage "$usage"

  local should_not_be_empty="${1}"
  local message="${2}"

  if [ -n "$should_not_be_empty" ]; then
    log_always "***SUCCESSFUL CHECK: $message"
  else
    test_failure "$message: Expected string to not be empty, but it was"
  fi
}
  

# Testing is done. 
# This calls test_is_ending
# Defines ERRORS to be any errors that are in the grouper_errors.log
function wrap_up()
{
  test_is_ending

  ERRORS=$(run_in_grouper_daemon cat $API/logs/grouper_error.log \
        | grep_ -i exception \
        | egrep_ -iv 'Optimized, coalesced ldap provisioning failed|THIS WILL BE RETRIED|SqlExceptionHelper.logExceptions|Exception in list') 

  if type -t filter_wrap_up_errors >/dev/null 
  then
    ERRORS=$(wrap_up_filter_errors <<<"$ERRORS")
  else
    ERRORS=$(head -1 <<<"$ERRORS")
  fi
}
