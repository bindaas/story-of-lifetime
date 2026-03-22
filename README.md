# Story of a Lifetime

An AI Agent web application that generates a coherent life story from a fixed start state to a fixed end state, guided by an evolving set of human-provided facts. Built in Java with Spring Boot and the Anthropic Claude API.

---

## What This Project Demonstrates

- **Goal-directed planning** — the agent always knows where it needs to end up
- **World model** — shared memory of story state, accepted facts, and outline
- **Multi-agent pipeline** — Planner, Critic, and Writer each with a single responsibility
- **Replanning** — when new facts arrive the agent reconciles and revises its plan
- **Append-only environment** — facts are immutable; new facts are reconciled, never override old ones

---

## The Story Problem

Given a **start state**, an **end state**, and a **fact file**, the agent generates a plausible life story connecting them. When new facts are added, the agent replans and produces a revised story — making visible exactly where and why the narrative diverged.

---

## Agent Architecture

```
Browser form (start + end + facts + settings)
         │
         ▼
  StoryController  (HTTP POST /api/generate)
         │
         ▼
    [ Planner ]  ──── builds story outline         (Phase 4)
         │
         ▼
    [ Critic  ]  ──── checks feasibility            (Phase 5)
         │
         ▼
    [ Writer  ]  ──── generates full prose story
         │
         ▼
  JSON response → browser renders story
```

---

## Build Phases

| Phase | What it does | Status |
|-------|-------------|--------|
| 1 | Verify Claude API connection | Done |
| 2 | Writer in isolation — hardcoded inputs | Done |
| 3 | World Model — load inputs from config files | Done |
| 3b | Web application — Spring Boot + single page UI | Done |
| 4 | Planner added — outline before prose | Upcoming |
| 5 | Critic added — feedback loop, reject and replan | Upcoming |
| 6 | Replan trigger — detect new facts, regenerate | Upcoming |
| 7 | Diff and explainability — what changed and why | Upcoming |

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Anthropic API key — get one at [console.anthropic.com](https://console.anthropic.com)

---

## Setup

**1. Clone the repository**
```bash
git clone https://github.com/bindaas/story-of-lifetime.git
cd story-of-lifetime
```

**2. Set your API key**
```bash
export ANTHROPIC_API_KEY=your_key_here
```

Add to `~/.zshrc` to make it permanent:
```bash
echo 'export ANTHROPIC_API_KEY=your_key_here' >> ~/.zshrc
source ~/.zshrc
```

**3. Build and run**
```bash
mvn clean package
mvn spring-boot:run
```

**4. Open in browser**
```
http://localhost:8080
```

---

## Project Structure

```
story-of-lifetime/
├── pom.xml
├── README.md
├── facts/
│   ├── start.txt                      ← start state (CLI mode)
│   ├── end.txt                        ← end state (CLI mode)
│   └── facts.txt                      ← append-only fact file (CLI mode)
└── src/
    └── main/
        ├── resources/
        │   ├── system.properties      ← model, temperature, story length defaults
        │   ├── prompts/
        │   │   └── writer_prompt.txt  ← prompt template with placeholders
        │   └── static/
        │       └── index.html         ← single page frontend
        └── java/com/rajivnarula/storyoflifetime/
            ├── Main.java              ← Spring Boot entry point
            ├── StoryController.java   ← HTTP POST /api/generate
            ├── StoryRequest.java      ← JSON request payload
            ├── AppConfig.java         ← reads system.properties
            ├── WorldModel.java        ← story state (file or form)
            └── WriterAgent.java       ← generates prose via Claude API
```

---

## Configuration

Edit `src/main/resources/system.properties` — no recompile needed for defaults:

```properties
# Options: claude-opus-4-6, claude-sonnet-4-6, claude-haiku-4-5-20251001
claude.model=claude-sonnet-4-6

claude.max_tokens=2048

# 0.0 (deterministic) → 1.0 (most creative)
writer.temperature=0.8

# short (2-3 paragraphs) | medium (4-6) | long (8-10)
writer.story_length=medium
```

All settings can also be overridden per-request from the browser UI.

---

## Experimenting Without Touching Code

| What you want to try | Where to change it |
|---|---|
| Different Claude model | Model dropdown in UI |
| More/less creative story | Temperature slider in UI |
| Longer/shorter story | Length pills in UI |
| Different prompt style | `src/main/resources/prompts/writer_prompt.txt` |
| Change default settings | `src/main/resources/system.properties` |

---

## Key Design Decisions

**Facts are append-only.** Once written, facts cannot be changed or removed. The agent must reconcile new facts with all previous ones — mirroring how real environments work.

**Agents have single responsibilities.** Planner never writes prose. Writer never judges feasibility. Critic never generates content. Clean separation makes each agent inspectable and testable in isolation.

**The World Model is the connective tissue.** Passed into every agent on every run — it is what makes story v2 a coherent revision of v1, not a fresh start.

**Config and prompts live outside code.** Model, temperature, story length, and prompt wording are all in files — experiment freely without recompiling.

---

## Author

Rajiv Narula — [@bindaas](https://github.com/bindaas)

---

## Deploying to Google Cloud Run

### Prerequisites
- Docker installed on your machine
- Google Cloud CLI installed (`brew install --cask google-cloud-sdk`)
- Google Cloud project set up with billing enabled

### One-time setup

**Enable required APIs:**
```bash
gcloud services enable run.googleapis.com
gcloud services enable secretmanager.googleapis.com
gcloud services enable containerregistry.googleapis.com
gcloud services enable cloudbuild.googleapis.com
```

**Store your Anthropic API key as a secret:**
```bash
echo -n "$ANTHROPIC_API_KEY" | gcloud secrets create ANTHROPIC_API_KEY \
    --data-file=- \
    --replication-policy="automatic"
```

**Grant Cloud Run access to the secret:**
```bash
# Get your project number first
gcloud projects describe $(gcloud config get-value project) --format="value(projectNumber)"

# Then grant access (replace YOUR_PROJECT_NUMBER)
gcloud secrets add-iam-policy-binding ANTHROPIC_API_KEY \
    --member="serviceAccount:YOUR_PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"
```

### Deploy

**Step 1 — Build and push the Docker image:**
```bash
PROJECT_ID=$(gcloud config get-value project)
docker build -t gcr.io/$PROJECT_ID/story-of-lifetime .
docker push gcr.io/$PROJECT_ID/story-of-lifetime
```

**Step 2 — Deploy to Cloud Run:**
```bash
gcloud run deploy story-of-lifetime \
    --image gcr.io/$PROJECT_ID/story-of-lifetime \
    --platform managed \
    --region us-central1 \
    --allow-unauthenticated \
    --set-secrets="ANTHROPIC_API_KEY=ANTHROPIC_API_KEY:latest" \
    --memory 512Mi \
    --timeout 120
```

**Step 3 — Get your public URL:**
```bash
gcloud run services describe story-of-lifetime \
    --platform managed \
    --region us-central1 \
    --format="value(status.url)"
```

Open that URL in your browser — your app is live.

### Redeploying after code changes

Every time you make changes, repeat Steps 1 and 2:
```bash
PROJECT_ID=$(gcloud config get-value project)
docker build -t gcr.io/$PROJECT_ID/story-of-lifetime .
docker push gcr.io/$PROJECT_ID/story-of-lifetime
gcloud run deploy story-of-lifetime \
    --image gcr.io/$PROJECT_ID/story-of-lifetime \
    --platform managed \
    --region us-central1 \
    --allow-unauthenticated \
    --set-secrets="ANTHROPIC_API_KEY=ANTHROPIC_API_KEY:latest" \
    --memory 512Mi \
    --timeout 120
```

### Cost

Cloud Run charges per request — the first 2 million requests/month are free. At demo/learning usage you will pay nothing for the infrastructure. You only pay for Anthropic API calls.
