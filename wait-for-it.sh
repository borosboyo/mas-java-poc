#!/usr/bin/env sh
# wait-for-it.sh: Wait until a host:port are available
# Usage: wait-for-it.sh host:port -- command args

set -e

host="$1"
shift
port="$1"
shift

while ! nc -z "$host" "$port"; do
  echo "Waiting for $host:$port..."
  sleep 1
done

exec "$@"
