# Drift App — Progress

## What's Built

### Android App (Kotlin + Jetpack Compose)
- Full Gradle project setup (compileSdk 34, minSdk 26)
- Custom Drift purple theme matching the brand
- Adaptive launcher icon (focus ring + drift trail)
- 4 screens:
  - **Today** — Shows AI-generated daily focus as structured cards (action + why + linked goal), with "parked for now" section for deprioritized items
  - **Brain Dump** — Free-form text input, sent to backend for AI-powered parsing into structured items with categories
  - **Goals** — Set goals with time horizons (week/month/quarter), view and complete active goals
  - **My Stuff** — All active items with drift indicators (days since last touched), actions: touched it / done / let go / pause
- Bottom navigation between Today, Dump, and My Stuff
- Room database (SQLite) for local storage of items, goals, and focus cache
- Focus caching: only calls backend on new day or when items/goals change, with manual refresh button
- Network security config allowing cleartext to local dev server
- Graceful fallback to local logic when backend is unreachable

### Tests
- Unit tests (AiServiceTest): fallback parsing, fallback focus logic, network failure handling
- Instrumented tests (DriftDatabaseTest): 13 tests covering insert, retrieve, status changes, drift detection, ordering, goal separation
- JaCoCo code coverage configured (v0.8.12)

### Docs
- `docs/product-overview.md` — Core loop, screens, architecture, cost breakdown
- `docs/testing-flow.md` — Full flow from landing page signup to Play Store internal testing to feedback collection
- `SETUP.md` — How to run, test, and what's still to do

## Architecture
```
Android App ←→ FastAPI Backend ←→ Gemini 2.5 Flash (free tier)
    ↓
Room DB (local cache + items + goals)
```

## What's Not Done Yet
- Firebase Cloud Messaging for push notifications (daily focus + drift nudges)
- Onboarding flow (first-time brain dump + goal setting)
- Weekly reflection screen
- Conscious abandonment prompts (auto-prompt after X days idle)
- Voice input for brain dumps
- Backend deployment (currently local only)
- Supabase integration for cloud data sync across devices
- Play Store internal testing track setup
