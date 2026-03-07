# Drift — App

An AI companion that remembers what you care about and won't let it silently slip away.

## Tech Stack

- **App:** Kotlin + Jetpack Compose (Android)
- **Backend:** Python (FastAPI)
- **Database:** Supabase (Postgres)
- **AI:** OpenAI API
- **Notifications:** Firebase Cloud Messaging (FCM)
- **Hosting:** Render or Railway (free tier)

## Project Structure

```
Drift-App/
├── app/              # Android app (Kotlin + Jetpack Compose)
├── backend/          # FastAPI backend
├── docs/             # Product docs, flows, decisions
└── README.md
```

## Getting Started

### Android App
1. Open Android Studio → File → New Project → Empty Activity (Compose)
2. Package name: `com.drift.app`
3. Min SDK: 26
4. Copy the source files from `app/` into your project

### Backend
```bash
cd backend
pip install -r requirements.txt
uvicorn main:app --reload
```

## Distribution

Using Google Play Internal Testing track. See `docs/testing-flow.md` for the full process.

## App Icon

A purple circle with a white ring, center dot, and a fading trail. The ring is focus. The dot is now. The trail is something starting to drift away. That's the whole app in one image — stay centered, catch what's slipping.
