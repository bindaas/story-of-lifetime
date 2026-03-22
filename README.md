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

| Phase | What it does |
|-------|-------------|
| 1 | Verify connection to Claude API |
| 2 | Writer in isolation — hardcoded inputs, one story |
| 3 | World Model — load start, end, facts from files |
| 4 | Planner added — outline first, then Writer |
| 5 | Critic added — feedback loop, reject and replan |
| 6 | Replan trigger — detect new facts, regenerate story |
| 7 | Diff and explainability — what changed and why |

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

Add this to your `~/.zshrc` or `~/.bashrc` to make it permanent.

**3. Build**
```bash
mvn clean compile
```

**4. Run**
```bash
mvn exec:java
```

---

## Project Structure

```
story-of-lifetime/
├── pom.xml
├── README.md
├── facts/
│   └── facts_v1.txt          # Your fact file (added in Phase 3)
└── src/
    └── main/
        └── java/
            └── com/
                └── rajivnarula/
                    └── storyoflifetime/
                        ├── Main.java
                        ├── WorldModel.java       (Phase 3)
                        ├── PlannerAgent.java     (Phase 4)
                        ├── CriticAgent.java      (Phase 5)
                        └── WriterAgent.java      (Phase 2)
```

---

## Fact File Format

Facts are plain text, one per line, numbered in sequence. They are **append-only** — never edit or remove a previous fact, only add new ones at the bottom.

```
1. John has a rich uncle in Houston
2. John shows exceptional talent in golf aged 9
3. Uncle sponsors John's education abroad
4. John studies medicine in the UK
5. John befriends an Indian diplomat during his studies
```

---

## Key Design Decisions

**Facts are immutable.** Once a fact is written, it cannot be changed or removed. New facts must be reconciled with all previous facts. This mirrors how real environments work — the agent cannot un-invent the past.

**Agents have single responsibilities.** The Planner never writes prose. The Writer never judges feasibility. The Critic never generates content. This separation makes each agent's behavior inspectable and testable in isolation.

**The World Model is the connective tissue.** It holds current story state and is passed into every agent on every run. It is what makes v2 a coherent revision of v1, not a fresh start.

---

## Author

Rajiv Narula — [@bindaas](https://github.com/bindaas)