set -eu

docker build -f Dockerfile.native -t openfs-native .
docker run -it --rm \
  -p 8082:8082 \
  -p 8083:8083 \
  openfs-native
