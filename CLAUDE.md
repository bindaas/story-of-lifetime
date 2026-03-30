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
src/main/java/com/rajivnarula/storyoflifetime/
  config/     ← AppConfig.java
  model/      ← WorldModel, request/response classes
  agent/      ← BaseAgent + 5 agent classes
  result/     ← AgentResult base + 5 result classes
  web/        ← StoryController.java
  Main.java
src/main/resources/prompts/          ← LLM prompt templates (txt files)
src/main/resources/static/           ← frontend (index.html, architecture.html aka "Behind the scenes")
src/main/resources/system.properties ← model, temperature, story length defaults
facts/                                ← start.txt, end.txt, facts.txt (CLI mode)
```

## Package conventions
- `config` — AppConfig only
- `model` — WorldModel, all request classes (FactGenerateRequest, StoryRequest, ExplainRequest), StoryResponse
- `agent` — BaseAgent (shared HTTP/prompt/cost logic) + 5 agent classes extending it
- `result` — AgentResult (shared token/cost/elapsed fields) + 5 result classes extending it
- `web` — StoryController only

## Environment variables
```
STORY_OF_LIFETIME_ANTHROPIC_API_KEY   ← primary API key (project-specific)
ANTHROPIC_API_KEY                     ← fallback if above is not set
```

## Agent pipeline
```
FactGeneratorAgent  (creativity · contradiction · world type)
         │
Human reviews & edits facts  ← human-in-the-loop checkpoint
         │  approved
PlannerAgent → CriticAgent → (replan if rejected, max 3 attempts)
                    │  approved
               WriterAgent → Story v1
                                 │  add more facts
                            WriterAgent → Story v2
                                              │  automatic
                                         ExplainerAgent (diff analysis)
```

## Agent classes and their files
All agents extend `agent.BaseAgent`. All results extend `result.AgentResult`.

| Class | Prompt file | Result class |
|-------|------------|--------------|
| agent/FactGeneratorAgent.java | prompts/factgenerator_prompt.txt | result/FactGeneratorResult.java |
| agent/PlannerAgent.java | prompts/planner_prompt.txt | result/PlannerResult.java |
| agent/CriticAgent.java  | prompts/critic_prompt.txt  | result/CriticResult.java  |
| agent/WriterAgent.java  | prompts/writer_prompt.txt  | result/WriterResult.java  |
| agent/ExplainerAgent.java | prompts/explainer_prompt.txt | result/ExplainerResult.java |

## BaseAgent (agent/BaseAgent.java)
Shared infrastructure for all agents:
- `OkHttpClient` (30/30/120s timeouts) — built once, shared via inheritance
- `loadPromptTemplate(String promptFile)` — loads from classpath
- `callClaude(String model, double temperature, int maxTokens, String prompt)` — single-turn API call
- `calculateCost(String model, int in, int out)` — opus/haiku/sonnet pricing
- `formatFacts(List<String> facts)` — numbered list formatter for prompt injection

## API endpoints
```
POST /api/generate-facts  ← runs FactGeneratorAgent, returns fact list for human review
POST /api/generate        ← runs Planner → Critic → Writer, returns StoryResponse
POST /api/explain         ← runs Explainer on v1 vs v2, returns explanation text
GET  /                    ← serves index.html
GET  /architecture.html   ← serves "Behind the scenes" doc (titled architecture.html for URL stability)
```

## World type
A single control that flows through both FactGeneratorAgent and WriterAgent:
- grounded    — real world, real rules (Schindler's List, Pursuit of Happyness)
- realistic   — heightened but believable (Forrest Gump, Slumdog Millionaire)
- fantastical — one impossible element (Harry Potter, Inception)
- outlandish  — different universe entirely (Game of Thrones, Star Trek, Dune)

## Prompt placeholders
- factgenerator_prompt.txt — `{{START_STATE}}`, `{{END_STATE}}`, `{{CREATIVITY}}`, `{{CONTRADICTION}}`, `{{WORLD_TYPE}}`
- planner_prompt.txt — `{{START_STATE}}`, `{{END_STATE}}`, `{{FACTS}}`, `{{FEEDBACK_SECTION}}`
- critic_prompt.txt  — `{{START_STATE}}`, `{{END_STATE}}`, `{{FACTS}}`, `{{OUTLINE}}`, `{{FEEDBACK_SECTION}}`
- writer_prompt.txt  — `{{START_STATE}}`, `{{END_STATE}}`, `{{FACTS}}`, `{{OUTLINE}}`, `{{WORLD_TYPE}}`, `{{STORY_LENGTH}}`
- explainer_prompt.txt — `{{START_STATE}}`, `{{END_STATE}}`, `{{ORIGINAL_FACTS}}`, `{{ADDITIONAL_FACTS}}`, `{{STORY_V1}}`, `{{STORY_V2}}`

## Key request/response classes
- FactGenerateRequest — startState, endState, creativity, contradiction, worldType
- StoryRequest — startState, endState, facts, plannerModel/Temp, criticModel/Temp, writerModel/Temp, storyLength, worldType
- StoryResponse — outline, story, criticDecisions/Reasons, per-agent token/cost metrics, plannerAccepted flag
- ExplainRequest — startState, endState, originalFacts, additionalFacts, storyV1, storyV2

## Configuration
All agent settings in `src/main/resources/system.properties`:
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

## Fact editing model
Before approval: full editorial control — edit, reorder, delete, add, or regenerate freely. After approval: facts become the v1 baseline and stay fixed. Additional facts for v2 are appended on top — the original approved facts cannot change.

## Human-in-the-loop
The FactGeneratorAgent produces facts but does NOT automatically feed them into the pipeline. The human reviews the facts in an editable textarea, can edit any of them, and must click Approve before the Planner runs. This is a deliberate design decision demonstrating the human-in-the-loop agent pattern.

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
All agents use these timeouts — do not reduce them:
- connect: 30s
- write: 30s
- read: 120s  ← Claude can take 60-90s for long generations

## Common tasks
**Add a new agent:** create `XxxAgent.java`, `XxxResult.java`, `prompts/xxx_prompt.txt`, add settings to `system.properties`, add endpoint to `StoryController.java`.

**Change a prompt:** edit the relevant `.txt` file in `src/main/resources/prompts/`.

**Change model or temperature:** edit `src/main/resources/system.properties`.

**Add a new world type:** add to the UI pills in `index.html`, add description to `factgenerator_prompt.txt` and `writer_prompt.txt`.

## UI conventions
- All action buttons use `btn-primary` (black) — Generate facts, Approve facts, Generate original story, Generate revised story, Add new facts
- Active pills (contradiction level) use amber `#854F0B` to match the Fact Generator card theme
- World type selector lives in the middle column inside the Fact Generator card — it is an active input, not informational
- Left column is purely informational — How it works prose, agent descriptions, no interactive controls
- Facts textarea (`factsCard`) is hidden on load, revealed only after Generate facts runs successfully, hidden again if regenerated
- Agent section (Planner/Critic/Writer cards + Generate button) is hidden until facts are approved
- Story Setup and Fact Generator cards are collapsible — they collapse automatically after facts are generated, can be re-expanded by clicking the header
- Facts card body collapses automatically after Approve is clicked — header stays visible with "Approved" badge
- `toggleSection(bodyId, chevronId)` and `collapseSection(bodyId, chevronId)` handle collapse logic
- Generate revised story button is disabled until the user has typed at least one additional fact
- Additional facts textarea starts empty (no default values) each time "Add new facts" is clicked

## Behind the scenes page (architecture.html)
- Filename stays `architecture.html` for URL stability; display title is "Behind the scenes"
- Link in index.html reads "Behind the scenes ↗"
- Section order: How this was built → Agent pipeline → Design decisions → Agents → World types → Google Cloud deployment → Git workflow → Testing → API endpoints → Cost model → Tech stack
- Each section has an `id` attribute for deep linking (e.g. `#how-it-was-built`, `#design-decisions`, `#agent-pipeline`, `#agents`, `#world-types`, `#gcp-deployment`, `#git-workflow`, `#testing`, `#api-endpoints`, `#cost-model`, `#tech-stack`)
- Table of contents at the top links to all sections

## Creativity is derived from world type
Creativity is no longer a user-facing control. It is derived automatically from world type in JS:
- grounded → low
- realistic → medium
- fantastical → high
- outlandish → high

The `creativityFromWorldType(worldType)` function in index.html handles this. The `/api/generate-facts` endpoint still accepts a `creativity` parameter — it is just now set automatically rather than by the user.

## Terminology
- "v1" is called "Original" in the UI (story column label, outline label, critic label)
- "v2" is called "Revised" in the UI
- Internal JS variables still use v1Data, v2StoryText etc. — only user-facing labels changed
- The output card section-label says "Your story" (v1) or "Original and revised" (v2) — never "Output"
- The cost table subsection is labeled "Model usage and cost" (rendered by costSectionLabel div)
- The Writer agent section in output is labeled "Story" (not "Story agent", not "Generated story")
- v1-label CSS: warm gray (#f1efe8/#444441) to match pipeline G node (Original story)
- v2-label CSS: purple (#EEEDFE/#534AB7) to match pipeline H node (Revised story)
- Explainer section is wrapped in a blue box (#e8f2fc / #b5d4f4) matching the pipeline Explainer node
