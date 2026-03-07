# Setup

## Open the Project

Open the `Drift-App` folder in Android Studio. Gradle sync should run automatically.

## OpenAI API Key (Optional)

For AI-powered daily focus, set your key in `AiService.kt`:

```kotlin
AiService.getInstance().apiKey = "sk-your-key-here"
```

Without it, the app uses a simple fallback that picks the most stale item. Fine for initial testing.

## Run

Build and run on your device or emulator. The app starts on the "Today" screen.

## Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (needs device/emulator)
./gradlew connectedAndroidTest

# Coverage report
./gradlew jacocoTestReport
# Report at app/build/reports/jacoco/index.html
```

## Still To Do

- [ ] Firebase Cloud Messaging for push notifications (daily focus + drift nudges)
- [ ] FastAPI backend for server-side AI calls and scheduled drift detection
- [ ] Supabase integration for cloud data sync
- [ ] Onboarding flow (first-time brain dump + goal setting)
- [ ] Weekly reflection screen
- [ ] Conscious abandonment prompts (continue / pause / let go after X days idle)
- [ ] Voice input for brain dumps
