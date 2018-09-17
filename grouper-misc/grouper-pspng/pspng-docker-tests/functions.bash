# Shell utilities, particularly logging, resource-finding, temporary files

trap 'echo "[ERROR] Error occurred at $BASH_SOURCE:$LINENO command: $BASH_COMMAND"' ERR

# Resolve a directory to an absolute path
absolute_dir() {
  local dir="${1:?USAGE absolute_dir <a directory>}"
  local result=$(cd "$dir"; pwd -P)

  # echo "absolute_dir: $dir :: $result" 1>&2

  echo "$result"
}

# Print "<date> Message" to stderr.
# Note, that this assumes printf-style arguments
log_always() {
  local fmt="$1"; shift
  printf "$(date "+%F %H:%M:%S") [%d] $fmt\\n" $$ "$@" 1>&2
}

# convert the log level to upper-case
# FYI, these are the allowed log levels: INFO, DEBUG, TRACE 
#    (WARN & ERROR are always enabled)
export LOG_LEVEL=$(echo ${LOG_LEVEL:-WARN} | tr a-z A-Z)

# Logging errors is always enabled
is_error_logging_enabled() {
  echo yes
}

# Log an error
log_error() {
  if [ -n "$(is_error_logging_enabled)" ]; then
    # If a file has been defined to capture errors, put message in there in addition to stderr
    if [ -n "${ERROR_LOG_FILE:-}" ]; then
      printf "$@" >> $ERROR_LOG_FILE
    fi
    
    log_always ERROR:"$@"
  fi
}

# Log and error and exit with a failure
fail() {
  log_error "$@"
  log_error "Exiting"
  exit 1
}

#Logging warnings is always enabled
is_warn_logging_enabled() {
  echo yes
}

# Log a warning
log_warn() {
   if [ -n "$(is_warn_logging_enabled)" ]; then
     log_always WARN:"$@"
   fi
}


# Determine if info-level logging is enabled
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

# Determine if debug-level logging is enabled
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
     [ -n "$cmd" ] || continue

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

# Make sure do_cleanup will be called on exit
function schedule_cleanup
{
  if [ "${CLEANUP_IS_SCHEDULED:-no}" != yes ]; then
    declare -a FILES_TO_CLEANUP
    declare -a CLEANUP_COMMANDS
    trap 'S=$?; echo "=====CLEANUP STARTING (S=$S) =====" 1>&2; do_cleanup $S; exit $S' INT TERM EXIT
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
  CLEANUP_COMMANDS=("${CLEANUP_COMMANDS[@]-}" "$*" )
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
  T=$(mktemp -d "/tmp/${ME}_tmp_$(date +%m%d).XXXXXXXXXX")
  if [ -z "$T" ]; then echo "Fatal error. Could not create temporary directory." 1>&2 ; exit 1; fi
  cleanup_files_on_exit "$T"
}

function hash_directory_contents
{
  local dir="${1:?USAGE: hash_directory_contents <directory>}"
  (cd $dir; find . -type f -print0 | sort -z | xargs -0 shasum | shasum | awk '{print $1}')
}
