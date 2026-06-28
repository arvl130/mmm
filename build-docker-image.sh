#! /bin/sh -e

CURRENT_BRANCH="$(git branch --show-current)"
CURRENT_COMMIT_HASH="$(git rev-parse HEAD)"
ENV_FILE="$(realpath .env)"
IMAGE_PATH="ghcr.io/arvl130/mmm"

echo "Current branch: $CURRENT_BRANCH"
echo "Current commit: $CURRENT_COMMIT_HASH"
echo "Current env file: $ENV_FILE"

set -a
. $ENV_FILE
set +a

BRANCH_BASED_TAG="$IMAGE_PATH:$CURRENT_BRANCH"
COMMIT_BASED_TAG="$IMAGE_PATH:$CURRENT_COMMIT_HASH"

docker buildx build \
  --attest type=provenance,mode=min,inline-only=true \
  -t "$COMMIT_BASED_TAG" \
  -t "$BRANCH_BASED_TAG" \
  .

printf '%s\n\t$ %s\n' \
  "Push the new image with this command:" \
  "docker push $IMAGE_PATH --all-tags"