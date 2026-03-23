# Story of a Lifetime — Claude Code Context

## What this project is
A multi-agent AI web application that generates a life story from a start state to an end state, guided by an evolving set of human-provided facts. Built in Java with Spring Boot and the Anthropic Claude API.

## Build and run
```bash
mvn clean package        # compile and package
mvn spring-boot:run      # run locally on http://localhost:8080
./deploy.sh              # build Docker image and deploy to Google Cloud Run
```

## Project type
- Java 17, Spring Boot 3.2, Maven
- Single-module project — no submodules
- REST API backend + single-page HTML frontend (no React, no Node)

## Key directories
```
src/main/java/com/rajivnarula/storyoflifetime/   ← all Java source files
src/main/resources/prompts/                       ← LLM prompt templates (txt files)
src/main/resources/static/                        ← frontend (index.html only)
src/main/resources/system.properties             ← model, temperature, story length defaults
facts/                                            ← start.txt, end.txt, facts.txt (CLI mode)
```

## Environment variables
```
STORY_OF_LIFETIME_ANTHROPIC_API_KEY   ← primary API key (project-specific)
ANTHROPIC_API_KEY                     ← fallback if above is not set
```

## Agent pipeline
The app runs a multi-agent loop on every generation request:
1. **PlannerAgent** — builds a 5-7 milestone story outline
2. **CriticAgent** — approves or rejects the outline (max 3 attempts)
3. **WriterAgent** — generates full prose from the approved outline
4. **ExplainerAgent** — (v2 only) explains what changed between v1 and v2

Each agent has its own class, prompt file, result class, and config settings.

## Agent classes and their files
| Class | Prompt file | Result class |
|-------|------------|--------------|
| PlannerAgent.java | prompts/planner_prompt.txt | PlannerResult.java |
| CriticAgent.java  | prompts/critic_prompt.txt  | CriticResult.java  |
| WriterAgent.java  | prompts/writer_prompt.txt  | WriterResult.java  |
| ExplainerAgent.java | prompts/explainer_prompt.txt | ExplainerResult.java |

## API endpoints
```
POST /api/generate   ← runs Planner → Critic → Writer, returns StoryResponse
POST /api/explain    ← runs Explainer on v1 vs v2, returns explanation text
```

## Configuration
All agent settings live in `src/main/resources/system.properties`:
```properties
planner.model=claude-sonnet-4-6
planner.temperature=0.3
critic.model=claude-sonnet-4-6
critic.temperature=0.1
critic.max_attempts=3
writer.model=claude-opus-4-6
writer.temperature=0.8
explainer.model=claude-sonnet-4-6
explainer.temperature=0.3
writer.story_length=medium   # short | medium | long
claude.max_tokens=2048
```

Settings can also be overridden per-request from the browser UI (except explainer which always uses system.properties defaults).

## Prompt placeholders
Prompt templates use `{{PLACEHOLDER}}` syntax. Each agent fills these in at runtime:

- `planner_prompt.txt` — `{{START_STATE}}`, `{{END_STATE}}`, `{{FACTS}}`, `{{FEEDBACK_SECTION}}`
- `critic_prompt.txt`  — `{{START_STATE}}`, `{{END_STATE}}`, `{{FACTS}}`, `{{OUTLINE}}`, `{{FEEDBACK_SECTION}}`
- `writer_prompt.txt`  — `{{START_STATE}}`, `{{END_STATE}}`, `{{FACTS}}`, `{{OUTLINE}}`, `{{STORY_LENGTH}}`
- `explainer_prompt.txt` — `{{START_STATE}}`, `{{END_STATE}}`, `{{ORIGINAL_FACTS}}`, `{{ADDITIONAL_FACTS}}`, `{{STORY_V1}}`, `{{STORY_V2}}`

## Facts are append-only
The core design constraint: facts in `facts/facts.txt` (CLI) or the browser textarea can only be added to, never removed or edited. New facts must be reconciled with all previous ones by the agent.

## Deployment
Hosted on Google Cloud Run:
- URL: https://story-of-lifetime-ii73f77uuq-uc.a.run.app
- Project: story-of-lifetime
- Region: us-central1
- API key stored as Google Secret Manager secret named `ANTHROPIC_API_KEY`
- Injected into Cloud Run as env var `STORY_OF_LIFETIME_ANTHROPIC_API_KEY`

To redeploy after changes:
```bash
./deploy.sh
```

## OkHttp timeouts
All agents use these timeouts on their HTTP client — do not reduce them:
- connect: 30s
- write: 30s
- read: 120s  ← Claude can take 60-90s for long generations

## Common tasks
**Add a new agent:** create `XxxAgent.java`, `XxxResult.java`, `prompts/xxx_prompt.txt`, add settings to `system.properties`, wire into `StoryController.java`.

**Change a prompt:** edit the relevant `.txt` file in `src/main/resources/prompts/` — no recompile needed if using `mvn spring-boot:run` with DevTools, otherwise `mvn clean package` first.

**Change model or temperature:** edit `src/main/resources/system.properties` — no recompile needed at runtime, but requires repackage if changed before startup.
