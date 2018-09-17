export API=/opt/grouper/grouper.apiBinary
export GSH=gsh_

# Print "Date Message" to stderr.
# Note, that this assumes printf-style arguments
log_always() {
  local fmt="$1"; shift
  printf "$(date "+%F %H:%M:%S") [%d] $fmt\\n" $$ "$@" 1>&2
}

# convert the log level to upper-case
export LOG_LEVEL=$(echo ${LOG_LEVEL:-WARN} | tr a-z A-Z)

# Logging errors
is_error_logging_enabled() {
  echo yes
}

log_error() {
  if [ -n "$(is_error_logging_enabled)" ]; then
    # If a file has been defined to capture errors, put message in there in addition to stderr
    if [ -n "${ERROR_LOG_FILE:-}" ]; then
      printf "$@" >> $ERROR_LOG_FILE
    fi
    
    log_always ERROR:"$@"
  fi
}

fail() {
  log_error "$@"
  log_error "Exiting"
  exit 1
}

function fail_usage()
{
  if [ $# -gt 0 ]; then
    log_error "$@"
  fi

  [ -n "${USAGE:-}" ] && fail "$USAGE"

  type -t flags_help >/dev/null  && flags_help
  exit 1
}

#Logging warnings
is_warn_logging_enabled() {
  echo yes
}

log_warn() {
   if [ -n "$(is_warn_logging_enabled)" ]; then
     log_always WARN:"$@"
   fi
}


is_info_logging_enabled() {
  if [ $LOG_LEVEL = INFO -o $LOG_LEVEL = DEBUG -o $LOG_LEVEL = TRACE ]; then
    echo yes
  fi
}

# Log an info-level message
log_info() {
  if [ -n "$(is_info_logging_enabled)" ]; then
    log_always INFO:"$@"
  fi
}

is_debug_logging_enabled() {
  if [ $LOG_LEVEL = DEBUG -o $LOG_LEVEL = TRACE ]; then
    echo yes
  fi
}

# Log a debug message
log_debug() {
  if [ -n "$(is_debug_logging_enabled)" ]; then
    log_always DEBUG:"$@"
  fi
}

is_trace_logging_enabled() {
  if [ $LOG_LEVEL = TRACE ]; then
    echo yes
  fi
}

# Log a trace message when LOG_LEVEL is low enough
log_trace() {
  if [ -n "$(is_trace_logging_enabled)" ]; then
    log_always TRACE:"$@"
  fi
}

# Run the cleanup commands, passing EXIT_CODE as an environment variable
function do_cleanup
{
   export EXIT_CODE="${1:-0}"
   trap - DEBUG

   for cmd in "${CLEANUP_COMMANDS[@]-}"; do
     [ -z "$cmd" ] && continue

     if [ ${NO_CLEANUP:-no} = yes ]; then
       log_warn "Not running cleanup command: '$cmd'"
     else
       log_debug "Running cleanup command: '$cmd'"
       eval "$cmd"
     fi
   done

   for f in "${FILES_TO_CLEANUP[@]-}"; do
     if [ -z "$f" ]; then continue; fi; #skip empty files
     if [ ${KEEP_TEMP:-no} = yes -o ${NO_CLEANUP:-no} = yes ]; then
        log_warn "Not deleting '$f'"
     else
        log_debug "Deleting '$f'"
        rm -rf "$f"*
     fi
   done
}

function schedule_cleanup
{
  if [ "${CLEANUP_IS_SCHEDULED:-no}" != yes ]; then
    declare -a FILES_TO_CLEANUP
    declare -a CLEANUP_COMMANDS
    trap 'S=$?; do_cleanup $S; exit $S' INT TERM EXIT
  fi
  CLEANUP_IS_SCHEDULED=yes
}

# This adds the files mentioned as arguments to the cleanup process
function cleanup_files_on_exit
{
  schedule_cleanup
  local f
  for f in "$@"; do
    FILES_TO_CLEANUP=( "${FILES_TO_CLEANUP[@]-}" "$f" )
  done
}

# This adds the command provided as an argument to the cleanup process
function add_cleanup_command
{
  schedule_cleanup
  CLEANUP_COMMANDS=("${CLEANUP_COMMANDS[@]-}" "$1" )
}


## Create a temporary file and assign it to T
##  (installs trap to delete ${T}* on exit)

function tempfile_into_T
{
  local ME=$(basename "${0#-}")
  T=$(mktemp "${TMPDIR:-/tmp}/${ME}_tmp_$(date +%m%d).XXXXXXXXXX")
  if [ -z "$T" ]; then log_error "Fatal error. Could not create temporary file." ; exit 1; fi
  cleanup_files_on_exit "$T"
}

function tempdir_into_T
{
  local ME=$(basename "${0#-}")
  T=$(mktemp -d "${TMPDIR:-/tmp}/${ME}_tmp_$(date +%m%d).XXXXXXXXXX")
  if [ -z "$T" ]; then echo "Fatal error. Could not create temporary directory." 1>&2 ; exit 1; fi
  cleanup_files_on_exit "$T"
}


# function to manage status printing and timing out
function check_progress_timer()
{
  local time_limit_secs=120

  if [ $1 = --time_limit_secs ]; then
    time_limit_secs=$2
    shift; shift
  fi
   
  local progress="${1:-}"
  local message="${2:-}"

  if [ -z "${CHECK_PROGRESS_TIMER_INITIALIZED:-}" ]; then
    CHECK_PROGRESS_TIMER_INITIALIZED=yes
    LAST_MESSAGE=""
    TASK_START_TIME=
    LAST_PROGRESS=nothing
    LAST_PROGRESS_TIME=
  fi

  # Is this the first progress update for the current message/task?
  if [ "$LAST_MESSAGE" != "$message" ]; then
     echo ""
     echo -n "$message..."
     LAST_MESSAGE="$message"

     TASK_START_TIME=$(date +%s)
     LAST_PROGRESS="$progress"
     LAST_PROGRESS_TIME=$(date +%s)
  elif [ "${LAST_PROGRESS}" != "$progress" ]; then
    # Progress has changed
    LAST_PROGRESS="$progress"
    LAST_PROGRESS_TIME=$(date +%s)
  fi

  local time_since_task_started=$(( $(date +%s) - TASK_START_TIME ))
  local time_since_progress=$(( $(date +%s) - LAST_PROGRESS_TIME ))

  if [ $time_since_progress -lt $time_limit_secs ]; then
    if [ $((time_since_task_started % 5)) -eq 0 ]; then
      echo "[$progress]."
    else
      echo -n .
    fi
  else
    echo ""
    echo "Timeout: $message"
    echo "grouper_error lines:"
    tail -n +$GROUPER_ERROR_LINE_NUMBER $API/logs/grouper_error.log
    exit 1
  fi
}
