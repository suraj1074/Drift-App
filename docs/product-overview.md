# Drift — Product Overview

## One-liner

An AI companion that remembers what you care about and won't let it silently slip away.

## The Problem

People don't lack ambition — they lack follow-through. They start books, projects, fitness routines, and financial plans, then quietly abandon them. Existing tools manage tasks but don't manage attention over time. Nothing notices when you stop. Nothing asks why.

## Core Loop

1. **Brain Dump** — User says whatever's on their mind (text or voice). No forms, no categories.
2. **Set Goals** — User defines what success looks like this week/month.
3. **Daily Focus** — AI picks 1-2 things that matter most today, based on goals, deadlines, and what's drifting.
4. **Drift Detection** — System tracks what hasn't been touched and nudges before things silently die.
5. **Weekly Mirror** — Factual reflection: what you said mattered vs what you actually did.
6. **Conscious Abandonment** — When something's been ignored too long, the app asks: continue, pause, or let go?

## Prototype Screens

1. **Brain Dump** — Text input to dump thoughts
2. **My Goals** — Set week/month goals
3. **Today's Focus** — AI-picked 1-2 actions for today
4. **My Stuff** — Everything dumped, with drift indicators (days since last touched)
5. **Push Notifications** — Daily focus + drift nudges

## Architecture

```
Flutter App  ←→  FastAPI Backend  ←→  Supabase (DB)
                      ↓
                 OpenAI API (AI thinking)
                      ↓
                 FCM (push notifications)
```

## Cost (Prototype)

- Supabase: Free tier (500MB DB, 50k auth users)
- Render/Railway: Free tier for backend
- FCM: Free, unlimited
- OpenAI: ~$1-2 for testing volume
- Google Play: Already have developer account

Total: ~$0-2/month
