#!/usr/bin/env bash
# Toggle an injected fault.
#
#   scripts/fault.sh <service> <fault-id> on|off ["change description"]
#   scripts/fault.sh <service> list
#
# Examples:
#   scripts/fault.sh inventory missing-index on "cac V12__cleanup_unused_indexes"
#   scripts/fault.sh inventory missing-index off
#   scripts/fault.sh order list
#
# The optional change description is logged by the service (logger "deployments")
# as if a deploy/config change had happened. That is evidence the agent may find.
set -euo pipefail

declare -A PORTS=([order]=8081 [inventory]=8082 [notification]=8083)

service="${1:?usage: fault.sh <order|inventory|notification> <fault-id|list> [on|off] [change]}"
port="${PORTS[$service]:?unknown service '$service' (use order, inventory or notification)}"
base="http://localhost:${port}/admin/faults"

if [[ "${2:-}" == "list" ]]; then
  curl -sS "$base"; echo
  exit 0
fi

fault="${2:?missing fault id}"
case "${3:?missing on|off}" in
  on)  action=enable ;;
  off) action=disable ;;
  *)   echo "third argument must be on or off" >&2; exit 1 ;;
esac

args=()
if [[ -n "${4:-}" ]]; then
  args=(--data-urlencode "change=$4")
fi

curl -sS -f -X POST -G "${base}/${fault}/${action}" "${args[@]}"
echo
