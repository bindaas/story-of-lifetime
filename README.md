# Story of a Lifetime

An AI Agent application that generates a coherent life story from a fixed start state to a fixed end state, incorporating an evolving set of human-provided facts. Built in Java using the Anthropic Claude API.

---

## What This Project Demonstrates

This project is a practical demonstration of core AI Agent concepts:

- **Goal-directed planning** — the agent always knows where it needs to end up
- **World model** — a shared memory of the current story state, accepted facts, and outline
- **Multi-agent pipeline** — a Planner, Critic, and Writer each with a single responsibility
- **Replanning** — when new facts arrive, the agent reconciles them with existing facts and revises its plan
- **Append-only environment** — facts are immutable and sequential; new facts must be reconciled, never override old ones

---

## The Story Problem

Given:
- A **start state** — e.g. *"John was born in an orphanage in Texas"*
- An **end state** — e.g. *"John was confirmed as Health Minister for the new government in India"*
- A **fact file** — a human-provided, append-only list of facts about John's life

The agent generates a plausible, coherent story connecting start to end, honouring every fact in sequence.

When new facts are added to the fact file, the agent detects changes, replans, and produces a revised story — making visible exactly where and why the story diverged.

---

## Agent Architecture

```
Inputs: Start state + End state + Fact file (append-only)
         │
         ▼
    [ Planner ]  ──── builds a story outline (5–7 milestones)
         │
         ▼
    [ Critic  ]  ──── checks feasibility, flags contradictions
         │ (reject → back to Planner, max 3 retries)
         ▼
    [ Writer  ]  ──── generates full prose story from approved outline
         │
         ▼
    [ World Model ] ── stores state, passed into every agent on each run
         │
         ▼
    Output: Story v1 (5 facts) → Story v2 (5+3 facts) + diff explanation
```

---

## Build Phases

The project is built incrementally — each phase is independently runnable:

| Phase | What it does | Status |
|-------|-------------|--------|
| 1 | Verify connection to Claude API | Done |
| 2 | Writer in isolation — hardcoded inputs, one story | Done |
| 3 | World Model — load start, end, facts from files | Done |
| 4 | Planner added — outline first, then Writer | Upcoming |
| 5 | Critic added — feedback loop, reject and replan | Upcoming |
| 6 | Replan trigger — detect new facts, regenerate story | Upcoming |
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

Add this to your `~/.zshrc` to make it permanent:
```bash
echo 'export ANTHROPIC_API_KEY=your_key_here' >> ~/.zshrc
source ~/.zshrc
```

**3. Build and run**
```bash
mvn clean compile
mvn exec:java
```

---

## Project Structure

```
story-of-lifetime/
├── pom.xml
├── README.md
├── facts/
│   ├── start.txt                 ← start state of the story
│   ├── end.txt                   ← end state of the story
│   └── facts.txt                 ← append-only fact file (you edit this)
└── src/
    └── main/
        ├── resources/
        │   ├── system.properties ← model, temperature, story length
        │   └── prompts/
        │       └── writer_prompt.txt
        └── java/
            └── com/
                └── rajivnarula/
                    └── storyoflifetime/
                        ├── Main.java
                        ├── AppConfig.java        ← reads system.properties
                        ├── WorldModel.java        ← reads facts/ directory
                        ├── WriterAgent.java       ← generates prose
                        ├── PlannerAgent.java      (Phase 4)
                        └── CriticAgent.java       (Phase 5)
```

---

## Configuration

All tuning is done in `src/main/resources/system.properties` — no recompile needed:

```properties
# Claude model to use
# Options: claude-opus-4-6, claude-sonnet-4-6, claude-haiku-4-5-20251001
claude.model=claude-opus-4-6

# Maximum tokens in the response
claude.max_tokens=2048

# Temperature: 0.0 (deterministic) to 1.0 (most creative)
writer.temperature=0.8

# Story length: short (2-3 paragraphs), medium (4-6), long (8-10)
writer.story_length=medium
```

---

## Fact File Format

Facts are plain text, one per line, in `facts/facts.txt`. They are **append-only** — never edit or remove a previous fact, only add new ones at the bottom.

```
John has a rich uncle in Houston who takes an interest in him at age 7
John shows exceptional talent in golf from age 9
John's uncle sponsors his education at a boarding school in the UK
John studies medicine at University College London
John befriends an Indian diplomat named Rajan Mehta during his residency
```

---

## Experimenting Without Touching Code

| What you want to try | What you change |
|---|---|
| Different Claude model | `claude.model` in system.properties |
| More creative story | `writer.temperature` in system.properties |
| Longer / shorter story | `writer.story_length` in system.properties |
| Different prompt style | `src/main/resources/prompts/writer_prompt.txt` |
| Different character / scenario | `facts/start.txt` and `facts/end.txt` |
| Add new facts | Append lines to `facts/facts.txt` |

---

## Key Design Decisions

**Facts are immutable.** Once a fact is written, it cannot be changed or removed. New facts must be reconciled with all previous facts. This mirrors how real environments work — the agent cannot un-invent the past.

**Agents have single responsibilities.** The Planner never writes prose. The Writer never judges feasibility. The Critic never generates content. This separation makes each agent's behavior inspectable and testable in isolation.

**The World Model is the connective tissue.** It holds current story state and is passed into every agent on every run. It is what makes v2 a coherent revision of v1, not a fresh start.

**Config and prompts live outside code.** Model choice, temperature, story length, and prompt wording are all in files — you can experiment freely without recompiling.

---

## Author

Rajiv Narula — [@bindaas](https://github.com/bindaas)
