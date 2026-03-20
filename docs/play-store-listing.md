# Drift — Play Store Listing

## App Name
**Drift — Focus on What Matters**

(30 characters — Play Store limit is 30)

## Short Description
AI picks your top 2 priorities. Catches what's drifting before it dies.

(76 characters — Play Store limit is 80)

## Full Description

Stop letting things silently slip away.

You don't need another to-do list. You need something that remembers what you committed to, notices when things start drifting, and helps you decide — consciously — what to keep, pause, or let go.

Drift is your AI-powered clarity companion.

HOW IT WORKS

→ Brain Dump
Just say what's on your mind. No forms, no categories. Tasks, goals, half-baked ideas — dump it all. The AI understands context and organizes it for you.

→ Daily Focus
Every morning, Drift picks the 1-2 things that matter most today — based on your goals and what's been slipping. It makes the decision so you don't have to.

→ Drift Detection
Haven't touched something in a while? Drift notices. It nudges you — not as a nag, but as a thoughtful prompt: "You said this mattered. Still true?"

→ Conscious Abandonment
Nothing just fades away. When something's been ignored, you choose: continue, pause, or let go. Things end intentionally, not by neglect.

→ Set Goals
Define what would make this week, month, or quarter a win. Your goals become the lens for everything else.

WHAT MAKES DRIFT DIFFERENT

• Two things, not twenty — the app decides, you act
• Tracks commitments over weeks and months, not just today
• Drift detection catches what's slipping before it dies
• AI that feels like a thoughtful friend, not a task manager
• Messy input welcome — no structure required

It's like having a friend with perfect memory and zero judgment.

Built for people who start things but struggle to finish them. For anyone whose important-but-not-urgent tasks always get pushed. For anyone who wants clarity on what actually deserves their attention.

---

## Graphics Specifications

All graphics use the Drift brand palette:
- Primary: #6C63FF (Drift purple) / #5B5BD6 (softer indigo in-app)
- Accent: #F5A623 (warm amber)
- Background: #FCFBF8 (warm off-white)
- Text: #2C2C2C (soft black)
- Error/Drift warning: #D4634B (warm red)
- Light lavender: #EEEDFF (card backgrounds)

### 1. App Icon (512 × 512 px)

Required: High-res icon for Play Store listing.

Design spec (matches existing adaptive icon):
- Background: Solid #6C63FF (Drift purple)
- Foreground elements (white on purple):
  - Inner ring (focus/attention circle)
  - Center dot (the "now")
  - Drift trail arc (gentle motion curve, top-right)
  - Small trailing dot at 60% opacity (something drifting away)
- Shape: Let Google Play apply its rounded mask (provide as full square with safe zone)
- No text on the icon

Export the existing adaptive icon at 512×512. The vector is already defined in:
`app/src/main/res/drawable/ic_drift_foreground.xml`

To generate the 512px PNG:
1. Open Android Studio → Resource Manager → right-click the launcher icon → Export
2. Or render the vector manually:
   - 512×512 canvas
   - Fill background: #6C63FF
   - Scale the vector paths proportionally (the viewportWidth/Height is 108, icon safe zone is the center 66dp)

### 2. Feature Graphic (1024 × 500 px)

Required: Banner shown at top of Play Store listing.

Design spec:
- Background: Gradient from #5B5BD6 (left) to #6C63FF (right), or solid #6C63FF
- Center text (white, clean sans-serif like Inter or Google Sans):
  - Line 1: "Two things. Not twenty." (large, ~40px)
  - Line 2: "AI-powered focus for what actually matters." (smaller, ~20px, 80% opacity)
- Subtle Drift icon mark in bottom-right corner at ~15% opacity as watermark
- Keep it minimal — no phone mockups, no busy graphics

### 3. Phone Screenshots (1080 × 1920 px or 1080 × 2400 px)

Required: 2-8 screenshots. Recommended: 5-6.

Frame each screenshot with:
- Background color extending behind the phone frame
- Caption text above or below the phone frame
- Consistent style across all screenshots

#### Screenshot 1 — Today Screen (Hero)
- Caption: "Your AI picks what matters today"
- Show: Today screen with greeting, 2 focus cards with action + why + linked goal, and the "My Goals" card at bottom
- Background tint: Light lavender (#EEEDFF)

#### Screenshot 2 — Brain Dump
- Caption: "Dump everything. AI sorts it out."
- Show: Brain Dump screen with sample text in the input field, "Dump it ✨" button visible
- Background tint: Warm off-white (#FCFBF8)

#### Screenshot 3 — Drift Detection
- Caption: "Nothing silently slips away"
- Show: My Stuff screen with items showing drift indicators — one item with "⚠️ Drifting — 7 days" in red, another with "✓ Touched today", action chips visible
- Background tint: Light warm red (#FFF0ED)

#### Screenshot 4 — Goals
- Caption: "Set your lens. Week, month, quarter."
- Show: Goals screen with horizon chips (Week selected), a goal typed in, and 1-2 active goals in cards below
- Background tint: Light lavender (#EEEDFF)

#### Screenshot 5 — Conscious Abandonment
- Caption: "Continue, pause, or let go — you choose"
- Show: My Stuff screen zoomed into the action chips row: 👆 Touch, ✓ Done, 💤 Pause, 👋 Let go
- Background tint: Warm amber (#FFF3E0)

---

## How to Create the Graphics

Since you can't generate images directly in code, here are your options:

### Option A: Figma (recommended)
1. Create a new Figma file with frames for each asset size
2. Use the color palette above
3. Use Google Sans or Inter for typography
4. Export as PNG at 1x

### Option B: Canva
1. Use custom dimensions for each asset
2. Set brand colors manually
3. Export as PNG

### Option C: Android Studio + Screenshots
1. Run the app on an emulator (Pixel 7 or similar)
2. Populate with sample data that looks good
3. Take screenshots via emulator toolbar
4. Frame them using a tool like https://screenshots.pro or https://mockuphone.com
5. Add captions in Figma/Canva

### Sample Data for Screenshots
To get good-looking screenshots, seed the app with this data before capturing:

Brain dump text:
"I need to file my taxes before the deadline, finish reading Atomic Habits, get back into running — maybe 3 times a week, call mom this weekend, and start that side project I keep thinking about"

Goals:
- "File taxes and run 3 times" (week)
- "Finish Atomic Habits" (month)
- "Launch side project MVP" (quarter)

This will give you realistic focus cards, drift indicators, and goal displays.
