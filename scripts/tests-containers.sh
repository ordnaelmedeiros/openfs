set -eu

(cd server; mvn clean test -Dtest.containers.enabled=true)
