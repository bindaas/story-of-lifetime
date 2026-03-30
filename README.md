# Story of a Lifetime

A multi-agent AI web application that generates a life story from a fixed start state to a fixed end state, guided by an evolving set of human-provided facts. Built in Java with Spring Boot and the Anthropic Claude API.

---

## Live Demo

[https://story-of-lifetime-ii73f77uuq-uc.a.run.app](https://story-of-lifetime-ii73f77uuq-uc.a.run.app)

**Behind the scenes:** [/architecture.html](https://story-of-lifetime-ii73f77uuq-uc.a.run.app/architecture.html) — how it works, design decisions, how it was built

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
| 6 | Replan trigger — v1 vs v2 side by side | Done |
| 7 | Explainer agent — diff and explainability | Done |
| 8 | Fact Generator agent — human-in-the-loop | Done |
| 9 | World type — grounded to outlandish | Done |
| 10 | UI restructure — three column layout, world type as input, creativity derived from world type, progressive reveal, consistent button colours | Done |
| 11 | UI polish — collapsible setup cards, button parity, additional facts UX, "Behind the scenes" page reorder with anchor links | Done |
| 12 | Unit tests — 40 tests across 7 classes, spring-boot-starter-test | Done |
| 13 | Refactor — layered packages (config/model/agent/result/web), BaseAgent, AgentResult base class | Done |

---

## Agent Pipeline

```
FactGeneratorAgent  (creativity · contradiction · world type)
         │
         ▼
Human reviews & edits facts  ← human-in-the-loop checkpoint
         │  approved
         ▼
PlannerAgent  →  CriticAgent  →  (replan if rejected)
                      │  approved
                      ▼
                 WriterAgent  →  Story v1
                                    │  add more facts
                                    ▼
                              WriterAgent  →  Story v2
                                                 │  automatic
                                                 ▼
                                           ExplainerAgent
```

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
├── CLAUDE.md                              ← Claude Code context
├── Dockerfile
├── deploy.sh                              ← one command Cloud Run deploy
├── facts/
│   ├── start.txt
│   ├── end.txt
│   └── facts.txt
└── src/
    └── main/
        ├── resources/
        │   ├── system.properties
        │   ├── prompts/
        │   │   ├── factgenerator_prompt.txt
        │   │   ├── planner_prompt.txt
        │   │   ├── critic_prompt.txt
        │   │   ├── writer_prompt.txt
        │   │   └── explainer_prompt.txt
        │   └── static/
        │       ├── index.html
        │       └── architecture.html
        └── java/com/rajivnarula/storyoflifetime/
            ├── Main.java
            ├── config/
            │   └── AppConfig.java
            ├── model/
            │   ├── WorldModel.java
            │   ├── FactGenerateRequest.java
            │   ├── StoryRequest.java / StoryResponse.java
            │   └── ExplainRequest.java
            ├── agent/
            │   ├── BaseAgent.java          ← shared HTTP, prompt loading, cost calc
            │   ├── FactGeneratorAgent.java
            │   ├── PlannerAgent.java
            │   ├── CriticAgent.java
            │   ├── WriterAgent.java
            │   └── ExplainerAgent.java
            ├── result/
            │   ├── AgentResult.java        ← shared token/cost/elapsed fields
            │   ├── FactGeneratorResult.java
            │   ├── PlannerResult.java
            │   ├── CriticResult.java
            │   ├── WriterResult.java
            │   └── ExplainerResult.java
            └── web/
                └── StoryController.java
```

---

## Configuration

Edit `src/main/resources/system.properties` — no recompile needed:

```properties
factgenerator.model=claude-sonnet-4-6
factgenerator.temperature=0.9
planner.model=claude-sonnet-4-6
planner.temperature=0.3
critic.model=claude-sonnet-4-6
critic.temperature=0.1
critic.max_attempts=3
writer.model=claude-opus-4-6
writer.temperature=0.8
explainer.model=claude-sonnet-4-6
explainer.temperature=0.3
writer.story_length=medium
claude.max_tokens=2048
```

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

## Author

Rajiv Narula — [@bindaas](https://github.com/bindaas)
