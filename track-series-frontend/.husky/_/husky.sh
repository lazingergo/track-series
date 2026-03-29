#!/bin/sh
if [ -z "$husky_skip_init" ]; then
  debug () {
    [ "$HUSKY_DEBUG" = "1" ] && echo "husky (debug) - $1"
  }

  readonly hook_name="$(basename -- "$0")"
  debug "running $hook_name"

  if [ -f "$(dirname -- "$0")/package.json" ]; then
    debug "sourcing from $(dirname -- "$0")/package.json"
  fi
fi
