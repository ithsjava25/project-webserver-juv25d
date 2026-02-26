# Architectural Decision Records (ADR)

This directory contains all Architectural Decision Records for the JavaHttpServer project.

## What is an ADR?

An ADR is a document that captures an important architectural decision made along with its context and consequences. This helps the team:

- Understand why certain design choices were made
- Onboard new team members faster
- Avoid repeating past discussions
- Track the evolution of the system

## ADR Format

We use the format described in [Joel Parker Henderson's ADR repository](https://github.com/joelparkerhenderson/architecture-decision-record).

Each ADR includes:
- **Context:** Why the decision is needed
- **Decision:** What choice was made
- **Consequences:** Trade-offs, pros, and cons

## Creating a New ADR

1. Copy `TEMPLATE.md` to a new file named `ADR-XXX-brief-title.md` (e.g., `ADR-001-use-maven-for-build.md`)
2. Fill in all sections of the template
3. Discuss with the team before marking as "Accepted"
4. Commit the ADR to the repository

## Naming Convention

- Files are named: `ADR-XXX-descriptive-kebab-case-title.md`
- XXX is a zero-padded sequential number (001, 002, etc.)
- Titles should be brief but descriptive

## ADR Status

An ADR can have one of the following statuses:

- **Proposed:** Under discussion
- **Accepted:** Decision has been made and is active
- **Deprecated:** No longer relevant but kept for historical context
- **Superseded:** Replaced by another ADR (reference the new ADR)

## Index of ADRs

| ADR | Title | Status | Date |
|-----|-------|--------|------|
| [001](ADR-001-static-file-serving-architecture.md) | Static File Serving Architecture | Accepted | 2026-02-11 |
| [002](ADR-002-ip-based-request-filtering.md) | IP-based Request Filtering | Accepted | 2026-02-25 |

---

## Best Practices

### When to Create an ADR

Create an ADR when:
- Making a significant architectural choice
- Choosing between multiple viable technical solutions
- Making a decision that will be hard to reverse
- Implementing a pattern that the whole team should follow
- Resolving a technical dispute

### When NOT to Create an ADR

Don't create ADRs for:
- Minor code style preferences (use linting/formatting tools)
- Trivial implementation details
- Temporary workarounds
- Decisions that are easily reversible

### Writing Good ADRs

**DO:**
- Be specific and concrete
- Include timestamps for time-sensitive information
- Explain the "why" clearly
- Consider alternatives thoroughly
- Keep it focused on one decision

**DON'T:**
- Change existing ADRs (amend or create new ones instead)
- Make them too long (aim for 1-2 pages)
- Skip the "Consequences" section
- Leave out the context

---

## Resources

- [ADR GitHub Organization](https://adr.github.io/)
- [Joel Parker Henderson's ADR repo](https://github.com/joelparkerhenderson/architecture-decision-record)
- [Documenting Architecture Decisions by Michael Nygard](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
