set -eu

docker build -f Dockerfile.jvm -t openfs-jvm .
docker run -it --rm \
  -p 8082:8082 \
  -p 8083:8083 \
  openfs-jvm
