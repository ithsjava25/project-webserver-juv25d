# ADR-002: IP-based Request Filtering

**Date:** 2026-02-25
**Status:** Accepted
**Deciders:** Team juv25d
**Technical:** - PR #59 - Add IP filter to request pipeline

---

## Context

We want to restrict access to the HTTP server based on the client IP address.
This should happen early in the request, before any route/handler/static file logic.

The initial implementation should be simple, easy to configure and safe to use during group development.

---

## Decision

We implement an IP filter as a global filter in the server pipeline.

The filter supports three modes:

- **Whitelist mode:** only IPs explicitly listed are allowed.
- **Blacklist mode:** all IPs are allowed except those explicitly listed.
- **Open mode:** if both whitelist and blacklist are empty, all requests are allowed (useful during development)

The initial implementation uses **exact IP address matching**.

---

## Rationale

The design prioritizes:
- clear and predictable behavior
- simple configuration
- low friction for development environments

Whitelist and blacklist are intentionally treated as separate configuration approaches.
Using both simultaneously can be ambiguous, so the project can either document precedence rules or enforce a single-mode configuration in future iterations.

---

## Consequences

### Positive
- Centralized access control early in the pipeline
- Easy to enable/disable restrictions via configuration
- Works well for group development (open mode)

### Trade-offs
- Exact IP matching only.
- Potential ambiguity if both lists are configured

---

## Alternatives Considered

### Enforce single mode (whitelist or blacklist)
**Pros:** clear API contract
**Cons:** less flexible during experimentation
**Status:** considered for future refinement if the team wants stricter validation

---
