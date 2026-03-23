# Story of a Lifetime

An AI Agent web application that generates a coherent life story from a fixed start state to a fixed end state, guided by an evolving set of human-provided facts. Built in Java with Spring Boot and the Anthropic Claude API.

---

## Live Demo

[https://story-of-lifetime-ii73f77uuq-uc.a.run.app](https://story-of-lifetime-ii73f77uuq-uc.a.run.app)

---

## What This Project Demonstrates

- **Goal-directed planning** — the agent always knows where it needs to end up
- **World model** — shared memory of story state, accepted facts, and outline
- **Multi-agent pipeline** — Planner, Critic, and Writer each with a single responsibility
- **Agent loop** — the Critic can reject the Planner's outline and force a replan, up to 3 times
- **Replanning** — when new facts arrive the agent reconciles and revises its plan
- **Append-only environment** — facts are immutable; new facts are reconciled, never override old ones
- **Cost tracking** — token usage and cost tracked separately per agent per request

---

## The Story Problem

Given a **start state**, an **end state**, and a **fact file**, the agent generates a plausible life story connecting them. When new facts are added, the agent replans and produces a revised story — making visible exactly where and why the narrative diverged.

**Triggering a Critic rejection:**
Add a contradictory fact to the facts box — e.g. *"John's uncle goes bankrupt when John is 8 and can no longer support him."* This forces the Planner to find a new path (golf scholarship, bursary, etc.) and makes the agent loop visible.

---

## Agent Architecture

```
Browser form (start + end + facts + per-agent settings)
         │
         ▼
  StoryController  (POST /api/generate)
         │
         ▼
    [ Planner ]  ── builds story outline (5-7 milestones)
         │
         ▼
    [ Critic  ]  ── checks feasibility and fact consistency
         │
    ┌────┴─────────────────┐
  approve                reject (with reason)
    │                       │
    ▼                       ▼
[ Writer ]           Planner retries with
generates prose      critic feedback attached
                     (max 3 attempts)
         │
         ▼
  JSON response → browser renders outline + critic log + story + cost breakdown
```

---

## Build Phases

| Phase | What was built | Status |
|-------|---------------|--------|
| 1 | Verify Claude API connection | Done |
| 2 | Writer in isolation — hardcoded inputs | Done |
| 3 | World Model — config files, prompt templates | Done |
| 3b | Spring Boot web app, single page UI | Done |
| 3c | Cloud Run deployment, permanent public URL | Done |
| 4 | Planner agent — separate model + cost per agent | Done |
| 5 | Critic agent — full Planner→Critic→Writer loop | Done |
| 6 | Replan trigger — add facts, v1 vs v2 side by side | Upcoming |
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

This project uses a dedicated API key separate from other projects — usage and costs are tracked independently.

Create a project-specific key at [console.anthropic.com/settings/keys](https://console.anthropic.com/settings/keys), name it `story-of-lifetime`, then add it to your shell:

```bash
echo 'export STORY_OF_LIFETIME_ANTHROPIC_API_KEY=your_key_here' >> ~/.zshrc
source ~/.zshrc
```

The app checks for `STORY_OF_LIFETIME_ANTHROPIC_API_KEY` first, then falls back to `ANTHROPIC_API_KEY`.

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
├── Dockerfile
├── deploy.sh                          ← one command Cloud Run deploy
├── facts/
│   ├── start.txt                      ← start state (CLI mode)
│   ├── end.txt                        ← end state (CLI mode)
│   └── facts.txt                      ← append-only fact file (CLI mode)
└── src/
    └── main/
        ├── resources/
        │   ├── system.properties      ← model, temperature, limits per agent
        │   ├── prompts/
        │   │   ├── planner_prompt.txt
        │   │   ├── critic_prompt.txt
        │   │   └── writer_prompt.txt
        │   └── static/
        │       └── index.html         ← single page frontend
        └── java/com/rajivnarula/storyoflifetime/
            ├── Main.java              ← Spring Boot entry point
            ├── StoryController.java   ← orchestrates Planner→Critic→Writer loop
            ├── StoryRequest.java      ← JSON request from browser
            ├── StoryResponse.java     ← JSON response to browser
            ├── AppConfig.java         ← reads system.properties
            ├── WorldModel.java        ← story state (file or form input)
            ├── PlannerAgent.java      ← builds story outline
            ├── PlannerResult.java     ← Planner output + token metrics
            ├── CriticAgent.java       ← approves or rejects outline
            ├── CriticResult.java      ← Critic decision + token metrics
            ├── WriterAgent.java       ← generates prose from outline
            └── WriterResult.java      ← Writer output + token metrics
```

---

## Configuration

Edit `src/main/resources/system.properties` — no recompile needed:

```properties
# Planner — logical, low temperature
planner.model=claude-sonnet-4-6
planner.temperature=0.3

# Critic — analytical, very low temperature
critic.model=claude-sonnet-4-6
critic.temperature=0.1
critic.max_attempts=3

# Writer — creative, higher temperature
writer.model=claude-opus-4-6
writer.temperature=0.8

# short (2-3 paragraphs) | medium (4-6) | long (8-10)
writer.story_length=medium

claude.max_tokens=2048
```

All agent settings can also be overridden per-request from the browser UI.

---

## What Appears in the UI After Generation

1. **Cost table** — tokens and cost broken down by Planner, Critic, and Writer
2. **Critic decision log** — each attempt shown as approved (green) or rejected (red) with the specific reason
3. **Approved outline** — the Planner's final accepted milestones in purple
4. **Generated story** — the Writer's prose in full

---

## Experimenting Without Touching Code

| What you want to try | Where to change it |
|---|---|
| Different model per agent | Model dropdowns in UI |
| More/less creative output | Temperature sliders in UI |
| Longer/shorter story | Length pills in UI |
| Force a Critic rejection | Add a contradictory fact to the facts box |
| Change prompt style | `src/main/resources/prompts/*.txt` |
| Change defaults | `src/main/resources/system.properties` |

---

## Deploying to Google Cloud Run

**One-time setup:**
```bash
gcloud services enable run.googleapis.com secretmanager.googleapis.com \
    containerregistry.googleapis.com cloudbuild.googleapis.com

echo -n "$STORY_OF_LIFETIME_ANTHROPIC_API_KEY" | gcloud secrets create ANTHROPIC_API_KEY \
    --data-file=- --replication-policy="automatic"

gcloud secrets add-iam-policy-binding ANTHROPIC_API_KEY \
    --member="serviceAccount:$(gcloud projects describe $(gcloud config get-value project) \
    --format='value(projectNumber)')-compute@developer.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"
```

**Every deployment:**
```bash
./deploy.sh
```

---

## Key Design Decisions

**Facts are append-only.** Once written, facts cannot be changed or removed. The agent must reconcile new facts with all previous ones — mirroring how real environments work.

**Agents have single responsibilities.** Planner never writes prose. Writer never judges feasibility. Critic never generates content. Clean separation makes each agent inspectable and testable in isolation.

**The Critic loop is the heart of the agent.** A pipeline executes in sequence. An agent evaluates and decides its next step. The Critic's reject-and-replan cycle is what crosses that line.

**Cost is tracked per agent.** Every API call returns token usage. Each agent accumulates its own cost independently — making the tradeoffs between model choices visible and measurable.

**Config and prompts live outside code.** Model, temperature, story length, and prompt wording are all in files — experiment freely without recompiling.

---

## Author

Rajiv Narula — [@bindaas](https://github.com/bindaas)