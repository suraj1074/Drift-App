# Testing Flow — From Landing Page to App

## Overview

The goal is to get the app into the hands of early testers (friends) with minimal friction.

## The Flow

### 1. Discovery (Landing Page)
- User visits the Drift landing page
- Takes the 4-question quiz
- If the app is a good fit, they're prompted to enter their email
- Email is submitted to a Google Form → lands in a Google Sheet

### 2. You Get Notified
- Google Forms sends you an email notification for each new submission
- Check the Google Sheet for new signups

### 3. Add to Internal Testing
- Go to [Google Play Console](https://play.google.com/console)
- Navigate to your app → Testing → Internal testing
- Add the tester's email to the testers list (must be a Gmail or Google Workspace email)
- Copy the opt-in URL that Google Play generates for your internal test track

### 4. Send Welcome Email
- Email the tester with:
  - What Drift is (1-2 sentences)
  - The opt-in link (they must click this first)
  - Instructions: "Click the link above, accept the invite, then download Drift from the Play Store"
  - What to expect: "The app will ask you to dump what's on your mind and set a goal. It'll then tell you what to focus on each day."
  - How to give feedback: reply to the email, or a simple feedback form

### 5. Tester Experience
- Tester clicks opt-in link → accepts invite
- Downloads app from Play Store (it appears after accepting)
- Opens app, starts using it
- Gets push notifications for daily focus and drift nudges

### 6. Collect Feedback
- After 1-2 weeks, reach out personally
- Key questions:
  - Did you open the app more than once?
  - Did the daily focus feel useful or random?
  - Did any nudge actually make you do something?
  - What felt broken or confusing?
  - Would you keep using this?

## Notes

- Internal testing supports up to 100 testers
- Testers must use the same Google account you added
- App updates are available to testers within minutes of publishing
- No app review needed for internal testing track
