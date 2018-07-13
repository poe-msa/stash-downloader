#!/bin/bash

set -e

docker build -t gcr.io/${PROJECT_NAME}/${DOCKER_IMAGE}:$TRAVIS_COMMIT .

gcloud auth activate-service-account --key-file $GOOGLE_DEPLOY_CREDENTIALS

gcloud --quiet config set project $PROJECT_NAME
gcloud --quiet config set compute/zone ${CLOUDSDK_COMPUTE_ZONE}

gcloud auth configure-docker --quiet

docker push gcr.io/${PROJECT_NAME}/${DOCKER_IMAGE}:$TRAVIS_COMMIT
gcloud beta compute instances update-container $SERVICE_INSTANCE_NAME \
    --container-image gcr.io/${PROJECT_NAME}/${DOCKER_IMAGE}:$TRAVIS_COMMIT \
    --container-env-file ./deploy/deploy.env