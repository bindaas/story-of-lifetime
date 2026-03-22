#!/bin/bash
set -e

PROJECT_ID=$(gcloud config get-value project)
IMAGE="gcr.io/$PROJECT_ID/story-of-lifetime"
SERVICE="story-of-lifetime"
REGION="us-central1"

echo "=== Story of a Lifetime — Cloud Run Deploy ==="
echo "Project : $PROJECT_ID"
echo "Image   : $IMAGE"
echo "Region  : $REGION"
echo ""

echo "--- Step 1: Building Docker image ---"
docker build --platform linux/amd64 -t $IMAGE .

echo ""
echo "--- Step 2: Pushing image to Google Container Registry ---"
docker push $IMAGE

echo ""
echo "--- Step 3: Deploying to Cloud Run ---"
gcloud run deploy $SERVICE \
    --image $IMAGE \
    --platform managed \
    --region $REGION \
    --allow-unauthenticated \
    --set-secrets="ANTHROPIC_API_KEY=ANTHROPIC_API_KEY:latest" \
    --memory 512Mi \
    --timeout 120

echo ""
echo "--- Deployment complete ---"
echo "URL: $(gcloud run services describe $SERVICE \
    --platform managed \
    --region $REGION \
    --format='value(status.url)')"