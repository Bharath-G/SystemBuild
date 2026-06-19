# THE SYSTEM — All Phase Prompts
# Use one phase at a time. Test before next phase.
# Stack: React Native + Expo | Spring Boot Java 17 | PostgreSQL | Ollama Mistral 7B | ChromaDB

---

# PHASE 12 — FIX #1: MISSIONS BROKEN

## Scope
Fix Request Missions functionality. Do not touch other features.

## Tasks

### Backend
1. Add GET `/api/quests/active` — returns List<Quest> where completed=false
2. Add POST `/api/quests/generate` — calls Ollama to generate 3 quests as JSON, saves to DB
3. Add POST `/api/quests/{id}/complete` — sets completed=true, adds xpReward to profile
4. Add GET `/api/quests/completed` — returns completed quests

### Quest.java entity must have:
```java
@Entity
public class Quest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private int xpReward;
    private String difficulty;   // EASY, MEDIUM, HARD, LEGENDARY
    private String category;     // CAREER, FINANCE, HEALTH, SKILL, KNOWLEDGE
    private String type;         // DAILY, WEEKLY, MILESTONE
    private boolean completed;
    private String consequence;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
```

### AI Quest Generation Prompt (use in QuestService.java):
```
Generate 3 quests for Bharath as JSON array only. No preamble. No markdown.
Each quest: title, description, xpReward(50-300), difficulty, category, type, consequence, deadlineHours(24/72/168)
Base on his goals: AWS SAA cert, NeoBank completion, start SIP, LinkedIn visibility.
Make them specific, measurable, slightly uncomfortable.
```

### Frontend
In `QuestBoard.tsx`:
1. On mount: call `SystemAPI.getActiveQuests()` → populate store
2. Complete button: call `SystemAPI.completeQuest(id)` → update local state → show XP toast
3. Generate button: call `SystemAPI.generateQuests()` → append to quest list
4. Show loading state during generation
5. Empty state when no quests: show "Generate Quests" button prominently

### Test Checklist
```
[ ] Active quests load on app open
[ ] Complete button marks quest done + shows XP gained
[ ] Generate produces 3 new quests from AI
[ ] Completed quests appear in completed tab
[ ] XP updates in profileStore after completion
```

---

# PHASE 12 — FIX #3: UI/UX MODERNIZATION

## Scope
Make app feel premium. No logic changes. UI and animations only.

## Global Rules
- Font: SpaceMono for all system text
- Background: #0A0A0F, Surface: #111118, Accent: #00FF88
- No rounded corners above 12px
- No gradients, no blur
- Animations: stat bars, XP counter, level flash, cursor blink

## Per-Screen Changes

### StatusWindow.tsx
- Animate XP bar fill on mount (Animated.timing, 1200ms)
- Pulse accent color when XP within 200 of threshold
- Each stat bar tappable → Modal with description + next milestone
- Titles shown as glowing badge chips (border: 1px accent, background: accentDim)
- Arc name shown at bottom: `[ GCC INITIATE ARC — ACTIVE ]`

### QuestBoard.tsx
- Overdue quests: red left border (borderLeftWidth: 3, borderLeftColor: danger)
- Urgent (<6h): amber left border
- Complete button: press animation (scale 0.95 → 1.0)
- XP toast on complete: floating `+150 XP` text animates upward and fades
- Level up: full screen flash (#00FF88 at 30% opacity, 300ms) + modal

### Chat.tsx
- Blinking cursor during AI response: `_` character toggling every 500ms
- System messages fade in (opacity 0 → 1, 200ms)
- Add copy-on-long-press for any message
- Smooth scroll to bottom after each message

### All Screens
- Add subtle entrance animation (translateY: 20 → 0, opacity 0 → 1, 300ms) on mount
- Loading states: skeleton placeholders not spinners
- Empty states: monospace message + action button, no illustrations

### LevelUpModal Component (create new)
```typescript
// Shown when XP crosses threshold
// Full screen overlay, accent color flash
// Shows: [ LEVEL UP ] in large mono font
// New level number
// Cold System message: "Level {n} reached. Adequate. Continue."
// Dismiss after 3 seconds or on tap
```

---

# PHASE 4 — DAILY CHECK-IN SYSTEM

## Scope
Morning check-in screen + AI day plan generation.

## Backend
Add POST `/api/checkin` — saves check-in, returns AI-generated day plan:

```java
@Entity
public class DailyCheckIn {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String workTasks;
    private String personalGoals;
    private String challenges;
    private int energyLevel;      // 1-5
    private int moodLevel;        // 1-5
    private int sleepHours;
    private String aiDayPlan;     // stored after generation
    private LocalDate date;
}
```

AI prompt for day plan generation:
```
Bharath's check-in for today:
Work tasks: {workTasks}
Personal goals: {personalGoals}
Challenges: {challenges}
Energy: {energy}/5, Mood: {mood}/5, Sleep: {sleep}h

Generate a prioritized day plan. Be specific. Name time blocks.
If energy < 3: reduce cognitive load tasks, prioritize lighter work.
If sleep < 6: flag it. Recommend one recovery action.
Format: numbered list. Under 150 words.
```

## Frontend
Create `src/screens/CheckIn.tsx`:

```
Layout:
[ DAILY CHECK-IN — {date} ]

Work tasks today? (multiline input)
Personal goals today? (multiline input)  
Any challenges? (multiline input)
Energy level: [1] [2] [3] [4] [5]  (tap to select)
Mood: [1] [2] [3] [4] [5]
Sleep hours: (number input)

[ GENERATE DAY PLAN ] button

─────────────────────────
> SYSTEM DAY PLAN
{AI response shown below}
─────────────────────────
```

- Check-in auto-prompts at 7:00 AM via expo-notifications
- If already checked in today: show today's plan instead of form
- Store check-in date in AsyncStorage to avoid duplicate prompts

Add to navigation as first tab or modal on app open if no check-in today.

---

# PHASE 5 — FULL GAMIFICATION SYSTEM

## Scope
Complete XP, levels, streaks, ranks, achievements.

## Backend

### New Entities

```java
@Entity
public class Achievement {
    private Long id;
    private String title;
    private String description;
    private String icon;          // text emoji or icon name
    private boolean unlocked;
    private LocalDateTime unlockedAt;
    private String triggerCondition; // "level_10", "streak_7", "quests_50"
}

@Entity  
public class Streak {
    private Long id;
    private String type;          // EXERCISE, LEARNING, WAKEUP, PRODUCTIVITY
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastChecked;
}
```

### Rank System (save in UserProfile)
```
E-Rank  : Level 1-9    "Initiating..."
D-Rank  : Level 10-19  "Disciplined"
C-Rank  : Level 20-34  "Performer"
B-Rank  : Level 35-49  "Builder"
A-Rank  : Level 50-74  "Architect"
S-Rank  : Level 75-99  "Elite"
SS-Rank : Level 100+   "Transcendent"
```

### Endpoints
```
GET  /api/achievements          → all achievements + unlock status
GET  /api/streaks               → all streak types + current values
POST /api/streaks/{type}/checkin → increment streak for today
GET  /api/profile/rank          → current rank + level + XP
```

### Achievement Triggers (check after every quest complete)
```java
// In QuestService.completeQuest():
achievementService.checkAndUnlock(profile);

// Conditions to check:
"first_quest"    → complete 1 quest
"streak_7"       → any streak reaches 7
"level_10"       → reach level 10
"quests_10"      → complete 10 total quests
"quests_50"      → complete 50 total quests
"finance_quest"  → complete first FINANCE category quest
"health_warrior" → complete 7 HEALTH quests
```

## Frontend

### New Screen: `src/screens/Achievements.tsx`
```
Layout:
[ ACHIEVEMENTS ] — {unlocked}/{total}

Progress bar: overall completion %

Grid of achievement cards (2 columns):
  Unlocked: full color, show unlock date
  Locked: dimmed, show condition to unlock

Each card: icon + title + description
```

### Update StatusWindow.tsx
- Add RANK badge next to level: `[ B-RANK ]` in accent color
- Add streak row below stats: `EXERCISE ⚡7d | LEARNING ⚡3d | WAKEUP ⚡12d`
- Rank up triggers same animation as level up

### Add Achievements to navigation tab
```typescript
Achievements: { active: 'trophy', inactive: 'trophy-outline' }
```

---

# PHASE 6 — HEALTH TRACKING (AS-AWARE)

## Scope
Health tracking with Ankylosing Spondylitis context. Daily exercise, mobility, nutrition, sleep.

## Critical Context for AI Prompts
```
User has Ankylosing Spondylitis (chronic spinal inflammation).
Requirements:
- Daily mobility and stretching is non-negotiable
- Low-impact exercises only (walking, swimming, yoga, stretching)
- No high-impact (running, heavy lifting, jumping)
- Sitting for long periods worsens condition — movement reminders essential
- Morning stiffness is common — gentle warm-up before any activity
```

## Backend

```java
@Entity
public class HealthLog {
    private Long id;
    private LocalDate date;
    private boolean exerciseDone;
    private int exerciseMinutes;
    private String exerciseType;
    private int waterGlasses;       // target: 8
    private int sleepHours;
    private boolean morningStretch; // AS-specific
    private String painLevel;       // NONE, MILD, MODERATE, HIGH
    private String notes;
}
```

Endpoints:
```
POST /api/health/log        → save today's health log
GET  /api/health/today      → get today's log
GET  /api/health/week       → get last 7 days
GET  /api/health/recommend  → AI generates exercise plan for today
```

AI exercise recommendation prompt:
```
Bharath has Ankylosing Spondylitis. Low-impact only. No running, no heavy lifting.
Today's pain level: {painLevel}. Sleep: {sleep}h. Last exercise: {lastExercise}.
Generate a 15-20 minute mobility routine for today.
Include: warm-up, 3-4 AS-friendly exercises, cool down.
Be specific with duration per exercise. Under 120 words.
```

## Frontend

Create `src/screens/Health.tsx`:
```
Layout:
[ HEALTH LOG — {date} ]

Morning stretch done? [YES] [NO]     ← AS priority #1
Exercise today? [YES] [NO]
  If YES: minutes + type inputs
Pain level: [NONE] [MILD] [MODERATE] [HIGH]
Water glasses: [+] {count} [-]
Sleep hours: (number)

[ GET TODAY'S EXERCISE PLAN ] → calls /api/health/recommend

Weekly summary: 7-day grid showing exercise completion
Streak: "Exercise streak: 5 days ⚡"
```

Notifications:
- 8:00 AM: "Morning mobility reminder — 10 minutes. Your spine requires it."
- 2:00 PM: "You have been sitting for hours. Stand and stretch. 5 minutes."
- 9:00 PM: "Log today's health data."

---

# PHASE 8 — LEARNING INTELLIGENCE ENGINE

## Scope
Adaptive learning system: recommendations, quiz, skill tracking, XP for learning.

## Backend

```java
@Entity
public class LearningLog {
    private Long id;
    private LocalDate date;
    private String topic;
    private String source;       // book, video, docs, practice
    private int minutesSpent;
    private String summary;      // what was learned
    private int selfRating;      // 1-5 how well understood
}

@Entity
public class QuizAttempt {
    private Long id;
    private String domain;
    private String question;
    private String userAnswer;
    private String correctAnswer;
    private boolean correct;
    private LocalDateTime attemptedAt;
}
```

Endpoints:
```
GET  /api/learning/recommend     → AI recommends what to learn today
POST /api/learning/log           → save what was learned
GET  /api/learning/quiz/{domain} → AI generates 3 quiz questions
POST /api/learning/quiz/evaluate → AI evaluates answer, returns feedback + XP
GET  /api/learning/stats         → total hours, topics covered, weak areas
```

Learning domains for Bharath:
```
JAVA, SPRING_BOOT, KAFKA, SYSTEM_DESIGN, AWS, 
MICROSERVICES, AI_ENGINEERING, COMMUNICATION, FINANCE, LEADERSHIP
```

AI quiz prompt:
```
Generate 1 quiz question for Bharath on {domain}.
Difficulty: {difficulty based on his stat level}.
Format JSON: { question, optionA, optionB, optionC, optionD, correct, explanation }
JSON only. No preamble.
```

AI evaluation prompt:
```
Question: {question}
Bharath's answer: {answer}
Correct answer: {correct}
Was he correct? Give brief feedback. If wrong: explain why + what to study.
Award XP: correct=50, partially correct=25, wrong=10 (for trying).
Under 80 words.
```

## Frontend

Create `src/screens/Learning.tsx`:
```
Layout:
[ LEARNING CENTER ]

Tabs: TODAY | QUIZ | HISTORY | STATS

TODAY tab:
  "What did you learn today?" (multiline input)
  [ LOG LEARNING ] button
  ─────────────────
  > SYSTEM RECOMMENDATION
  [ GET TODAY'S RECOMMENDATION ] → calls /api/learning/recommend

QUIZ tab:
  Domain selector: (horizontal scroll chips)
  JAVA | SPRING | KAFKA | AWS | SYSTEM DESIGN | ...
  [ START QUIZ ] → loads 1 question
  Question display
  4 option buttons (A/B/C/D)
  Submit → shows AI evaluation + XP earned

STATS tab:
  Total learning hours this week
  Top domain: {domain}
  Weakest domain: {domain} → [ Practice Now ]
  Learning streak: X days
```

If no learning logged by 9 PM: push notification
"Nothing learned today. Unacceptable. 20 minutes on {recommended topic}."

---

# PHASE 3 — FINANCIAL INTELLIGENCE HUB

## Scope
Market insights, wealth concepts, AI-generated financial guidance. No trading features.

## Backend

```java
@Entity
public class FinancialInsight {
    private Long id;
    private String title;
    private String summary;
    private String relevance;    // why it matters to Bharath
    private String action;       // what to do
    private String category;     // CAREER, INVESTING, SAVINGS, MARKET, TECH
    private LocalDate generatedDate;
}

@Entity
public class FinancialLog {
    private Long id;
    private LocalDate date;
    private String type;         // INCOME, EXPENSE, INVESTMENT, SAVING
    private double amount;
    private String category;
    private String notes;
}
```

Endpoints:
```
GET  /api/finance/insights          → get this week's AI insights
POST /api/finance/insights/generate → AI generates 5 new insights
POST /api/finance/log               → log income/expense
GET  /api/finance/summary           → monthly summary
GET  /api/finance/advice            → AI advice based on logs
```

### Weekly Insight Generation (scheduled Monday 7 AM)
```java
@Scheduled(cron = "0 0 7 * * MON")
public void generateWeeklyInsights() {
    String prompt = """
        Generate 5 financial insights relevant to Bharath.
        Context: 26yo, Chennai, ₹X LPA salary, sole earner, no SIP yet,
        building emergency fund, target home ownership.
        Include: one investing concept, one career-finance link,
        one Chennai real estate fact, one wealth habit, one market trend.
        Format JSON array: [{title, summary, relevance, action, category}]
        JSON only.
        """;
    // call AI, parse JSON, save to DB
}
```

## Frontend

Create `src/screens/Finance.tsx`:
```
Layout:
[ FINANCIAL INTELLIGENCE ]

Tabs: INSIGHTS | TRACKER | ADVICE

INSIGHTS tab:
  Weekly AI-generated cards
  Each card: title + summary + WHY THIS MATTERS + ACTION STEP
  Refresh button: generate new insights

TRACKER tab:
  Quick log: [INCOME] [EXPENSE] [SIP] [SAVINGS] buttons
  Amount input + category + notes
  This month summary:
    Income: ₹X | Expenses: ₹X | Saved: ₹X | Savings rate: X%

ADVICE tab:
  [ GET FINANCIAL ADVICE ] → AI analyses logs + gives specific advice
  AI response shown in System terminal style
```

Hardcoded starting insights (shown before first AI generation):
```
1. Starting SIP at 26 vs 30 — 4-year compounding gap costs ~₹1.1Cr by 55
2. Java + Kafka + Cloud at GCCs: ₹20-35 LPA (you are below this)
3. Freelance Java/Spring Boot: ₹1,500-4,500/hour on Upwork
4. Nifty 50 index fund: 12% avg CAGR historically — best start for beginners
5. Emergency fund target: 6 months expenses before aggressive investing
```

---

# PHASE 2 — REMOVE HARDCODED CONTENT

## Scope
Replace all static strings with AI-generated dynamic content. No UI changes.

## What to Replace

### In profileStore.ts
Remove hardcoded `quests` array — quests must come from backend only.
Keep hardcoded `stats` and `titles` as defaults until backend loads.

### In Intelligence.tsx
Remove hardcoded insight objects.
On mount: call `GET /api/intelligence/insights`.
If empty: call `POST /api/intelligence/generate` automatically.

### In Roadmap.tsx
Remove hardcoded roadmap months.
Call `GET /api/roadmap` — backend returns AI-generated roadmap based on current stats.

Roadmap generation prompt:
```
Generate Bharath's 12-month roadmap as JSON.
Current stats: Java 78, SystemDesign 52, Cloud 30, Finance 61, Comm 43.
Goals: AWS SAA, job switch ₹18-25 LPA, start SIP, NeoBook live.
Format: [{month, focus, milestones:[], xpReward}]
4 phases covering next 12 months. Be specific with timelines.
JSON only.
```

### In ProfileService.java
System prompt must dynamically inject current stats from DB, not hardcoded values:
```java
public String buildSystemPrompt() {
    UserProfile profile = profileRepository.findById(1L).orElse(defaultProfile());
    return String.format("""
        You are THE SYSTEM bound to %s.
        Level: %d | Class: %s | Arc: %s
        Stats: Java %d, SystemDesign %d, Cloud %d, Finance %d, Comm %d
        Goals: %s
        RULES: Truth first. Flaws before support. Under 100 words unless asked.
        """,
        profile.getName(), profile.getLevel(), profile.getClassName(),
        profile.getCurrentArc(), profile.getJavaStat(), profile.getSystemDesignStat(),
        profile.getCloudStat(), profile.getFinanceStat(), profile.getCommStat(),
        profile.getGoalsSummary()
    );
}
```

---

# PHASE 7 — WORK PRODUCTIVITY ENGINE

## Scope
Daily work task tracking, blocker detection, AI priority suggestions.

## Backend

```java
@Entity
public class WorkTask {
    private Long id;
    private String title;
    private String description;
    private String priority;      // P1, P2, P3
    private String status;        // TODO, IN_PROGRESS, BLOCKED, DONE
    private String blocker;       // null if not blocked
    private int estimatedHours;
    private LocalDate date;
    private LocalDateTime completedAt;
}
```

Endpoints:
```
GET  /api/work/today          → today's tasks
POST /api/work/task           → add task
PUT  /api/work/task/{id}      → update status/blocker
GET  /api/work/suggest        → AI suggests priorities for today
POST /api/work/plan           → AI generates work plan from task list
```

AI work planning prompt:
```
Bharath's tasks today: {taskList}
Energy level: {energy}/5
Suggest priority order. Flag any tasks that need more than estimated time.
If any task is blocked, suggest workaround.
If workload is light (< 4 hours): recommend one learning task to fill time.
Under 120 words.
```

## Frontend

Create `src/screens/Work.tsx`:
```
Layout:
[ WORK BOARD — {date} ]

[ + ADD TASK ] button → inline form (title, priority, hours)

Task list:
  Each task: title | priority badge | status | hours
  Swipe right → mark DONE (XP awarded)
  Swipe left → mark BLOCKED (ask for blocker description)
  Tap → expand → edit

[ GET AI PRIORITY PLAN ] → calls /api/work/suggest

Bottom stats:
  Tasks: X done / Y total | Estimated: Xh | Actual: Yh
```

If all tasks done before 5 PM:
Automatically suggest: "Workload complete. Recommended: 1 hour AWS SAA study."

---

# PHASE 10 — MULTI-AI ORCHESTRATION

## Scope
Automatic routing between Ollama (local) → Claude → fallback message. No user intervention.

## Backend — AIService.java Rewrite

```java
@Service
public class AIService {

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${anthropic.api.key:}")
    private String claudeKey;

    @Value("${ollama.model:mistral}")
    private String ollamaModel;

    private final RestTemplate restTemplate = new RestTemplate();

    public enum AIProvider { OLLAMA, CLAUDE, NONE }

    public String chat(String userMessage) {
        String systemPrompt = profileService.buildSystemPrompt();
        String context = memoryService.retrieveRelevant(userMessage);

        // Try Ollama first (local, free, fast when warm)
        if (isOllamaAvailable()) {
            try {
                return callOllama(systemPrompt, context, userMessage);
            } catch (Exception e) {
                log.warn("Ollama failed, trying Claude: {}", e.getMessage());
            }
        }

        // Try Claude (online, best quality)
        if (isClaudeConfigured()) {
            try {
                return callClaude(systemPrompt, context, userMessage);
            } catch (Exception e) {
                log.warn("Claude failed: {}", e.getMessage());
            }
        }

        // Both offline
        return "[SYSTEM OFFLINE] No AI provider available.\n" +
               "Start Ollama: ollama serve\n" +
               "Or configure Claude API key in settings.";
    }

    public AIProvider getCurrentProvider() {
        if (isOllamaAvailable()) return AIProvider.OLLAMA;
        if (isClaudeConfigured()) return AIProvider.CLAUDE;
        return AIProvider.NONE;
    }

    private boolean isOllamaAvailable() {
        try {
            ResponseEntity<String> r = restTemplate.getForEntity(
                ollamaUrl + "/api/tags", String.class);
            return r.getStatusCode().is2xxSuccessful();
        } catch { return false; }
    }

    private boolean isClaudeConfigured() {
        return claudeKey != null && !claudeKey.isEmpty();
    }

    private String callOllama(String system, String ctx, String msg) {
        // existing implementation with options
    }

    private String callClaude(String system, String ctx, String msg) {
        // existing Claude API implementation
    }
}
```

Add endpoint to expose current provider to frontend:
```
GET /api/ai/provider → returns { provider: "OLLAMA"|"CLAUDE"|"NONE", latencyMs: 234 }
```

## Frontend
Update the connection dot in Chat.tsx:
```typescript
// 3 states instead of 2:
GREEN  → OLLAMA (local, fast)
BLUE   → CLAUDE (cloud, online)
RED    → OFFLINE (no AI)

// Label shows: "OLLAMA — LOCAL" or "CLAUDE — CLOUD" or "SYSTEM OFFLINE"
```

---

# PHASE 9 — GOOGLE SHEETS INTEGRATION

## Scope
OAuth Google Sheets sync for weekly/monthly reports. No hardcoded credentials.

## Backend

Add dependency to pom.xml:
```xml
<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client</artifactId>
    <version>2.2.0</version>
</dependency>
<dependency>
    <groupId>com.google.apis</groupId>
    <artifactId>google-api-services-sheets</artifactId>
    <version>v4-rev20230815-2.0.0</version>
</dependency>
```

```java
@Entity
public class GoogleConfig {
    private Long id;
    private String accessToken;     // encrypted
    private String refreshToken;    // encrypted
    private String spreadsheetId;
    private LocalDateTime tokenExpiry;
}
```

Endpoints:
```
GET  /api/google/auth-url        → returns OAuth URL for user to visit
POST /api/google/callback         → receives code, exchanges for tokens
GET  /api/google/status          → connected or not
POST /api/google/sync/weekly     → write weekly report to Sheets
POST /api/google/sync/monthly    → write monthly report to Sheets
DELETE /api/google/disconnect    → revoke + delete tokens
```

Weekly report columns:
```
Date | Quests Completed | XP Earned | Level | Exercise Done | 
Learning Hours | Sleep Avg | Water Avg | Work Tasks Done | Journal Entries | Mood Avg
```

## Frontend

Add to Settings.tsx (new section):
```
GOOGLE SHEETS SYNC

[ CONNECT GOOGLE ACCOUNT ] → opens OAuth URL in browser
Status: Connected as {email} / Not connected

If connected:
  Spreadsheet ID: (text input — user pastes their sheet ID)
  [ SYNC THIS WEEK ] button
  [ SYNC THIS MONTH ] button
  [ DISCONNECT ] button

Instructions shown:
  1. Create a Google Sheet
  2. Copy the Sheet ID from URL
  3. Paste above and tap Save
```

DO NOT hardcode any email addresses or credentials.
All OAuth tokens stored encrypted in PostgreSQL.

---

# PHASE 11 — FULL STANDALONE OS TRANSFORMATION

## Scope
Dashboard screen as home. AI supports app, not the other way around.

## New Dashboard Screen

Create `src/screens/Dashboard.tsx` — make this the FIRST tab:

```
Layout:
─────────────────────────────────
  Good morning, Bharath.         (time-based greeting)
  {date} | Level 47 | B-Rank
─────────────────────────────────
  TODAY'S STATUS
  ┌─────────┬─────────┬─────────┐
  │ Quests  │Exercise │Learning │
  │  2/5    │  ✓ Done │ 0h      │
  └─────────┴─────────┴─────────┘

  ACTIVE QUEST (most urgent)
  [ quest card — compact ]

  XP PROGRESS
  ████████░░  4700/5000 XP

  SYSTEM INSIGHT (AI-generated, refreshed daily)
  "> {one sentence insight relevant to today}"

  QUICK ACTIONS
  [Check In] [Add Task] [Log Health] [Ask System]
─────────────────────────────────
```

## Navigation Update

New tab order:
```
Dashboard | Status | Quests | System | Intel | More(→ Health, Work, Learning, Finance, Journal, Roadmap, Achievements, Settings)
```

Put Health, Work, Learning, Finance, Journal, Roadmap, Achievements, Settings inside a "More" screen to reduce tab bar crowding:

```typescript
// src/screens/More.tsx
// Simple list of remaining screens with icons
// Tap any → navigate to that screen
// Use @react-navigation/stack for nested navigation
```

## Daily System Insight Generation
```
Scheduled: every morning 6 AM
POST /api/insights/daily
Prompt: "Generate one sentence insight for Bharath today.
Based on his level {level}, recent quest completions {count},
upcoming: {nextDeadline}. Be direct. Be specific. Under 20 words."
Store in DB, return via GET /api/insights/today
```

---

# PHASE 13 — FUTURE FEATURES

## Implement These in Priority Order

### Priority 1 — Voice Input in Chat
```typescript
// Use expo-speech for text-to-speech (System reads its responses)
import * as Speech from 'expo-speech';

// In Chat.tsx, after system message received:
Speech.speak(systemResponse, {
  language: 'en-IN',
  pitch: 0.9,        // slightly lower, more authoritative
  rate: 0.95,
});

// Add toggle: [🔊 VOICE ON/OFF] in Chat header
```

### Priority 2 — Personal Knowledge Graph
```java
// Track connections between topics Bharath learns
@Entity
public class KnowledgeNode {
    private Long id;
    private String topic;
    private String domain;
    private int masteryLevel;    // 0-100
    private List<String> relatedTopics;
    private LocalDate lastStudied;
    private int timesStudied;
}
// Visualize in Learning screen as a simple list sorted by mastery
```

### Priority 3 — Smart Routines
```
Morning routine template (7 AM check-in triggers):
1. Check-in form
2. Review today's quests
3. View AI day plan
4. Log morning stretch (AS-specific)

Evening routine template (9 PM notification):
1. Mark quest completions
2. Log health data
3. Log learning
4. Quick journal entry
5. View tomorrow's preparation
```

### Priority 4 — Predictive Coaching
```
Every Sunday: AI generates weekly review
POST /api/coaching/weekly-review

Prompt:
"Review Bharath's week:
Quests: {completed}/{total}
XP: {earned}
Exercise: {days}/7
Learning: {hours}h
Mood avg: {mood}/5

What pattern do you see? What needs to change next week?
Name one thing he did well. Name one critical gap.
Be direct. Under 150 words."

Display in Dashboard as "WEEKLY REVIEW" card
```

### Priority 5 — Calendar Intelligence (future)
```
When Google Calendar integration is added:
- Pull today's meetings
- Inject into day plan: "You have 3 meetings. Deep work window: 2-5 PM."
- Suggest quest scheduling around calendar
```

---

# MASTER FILE CHECKLIST

Use this to track implementation:

```
PHASE 12 FIX #1 — Missions
[ ] Quest entity + repository
[ ] 4 quest endpoints
[ ] AI quest generation
[ ] Frontend quest loading + completion

PHASE 12 FIX #3 — UI
[ ] Stat bar animations
[ ] XP float toast
[ ] Level up modal
[ ] Message fade-in
[ ] Screen entrance animations

PHASE 4 — Check-In
[ ] DailyCheckIn entity + endpoint
[ ] CheckIn.tsx screen
[ ] 7 AM notification

PHASE 5 — Gamification
[ ] Achievement entity + endpoints
[ ] Streak entity + endpoints
[ ] Rank system
[ ] Achievements.tsx screen

PHASE 6 — Health
[ ] HealthLog entity + endpoints
[ ] Health.tsx screen
[ ] AS-aware exercise AI
[ ] 3 health notifications

PHASE 8 — Learning
[ ] LearningLog + QuizAttempt entities
[ ] 5 learning endpoints
[ ] Learning.tsx with 3 tabs
[ ] 9 PM learning notification

PHASE 3 — Finance
[ ] FinancialInsight + FinancialLog entities
[ ] Weekly AI insight scheduler
[ ] Finance.tsx with 3 tabs

PHASE 2 — Remove Hardcoded
[ ] Remove hardcoded quests from store
[ ] Dynamic Intelligence screen
[ ] Dynamic Roadmap
[ ] Dynamic system prompt

PHASE 7 — Work
[ ] WorkTask entity + endpoints
[ ] Work.tsx with swipe actions

PHASE 10 — Multi-AI
[ ] AIService provider routing
[ ] /api/ai/provider endpoint
[ ] 3-state indicator in Chat.tsx

PHASE 9 — Google Sheets
[ ] OAuth flow
[ ] GoogleConfig entity (encrypted tokens)
[ ] Sync endpoints
[ ] Settings screen section

PHASE 11 — OS Transform
[ ] Dashboard.tsx
[ ] More.tsx (nested nav)
[ ] Daily insight generation

PHASE 13 — Future
[ ] Voice output in Chat
[ ] KnowledgeNode entity
[ ] Smart routine notifications
[ ] Weekly coaching review
```

---
# ONE PHASE PER AI SESSION. TEST BEFORE NEXT.
# STACK: React Native + Expo | Spring Boot Java 17 | PostgreSQL | Ollama Mistral 7B | ChromaDB
