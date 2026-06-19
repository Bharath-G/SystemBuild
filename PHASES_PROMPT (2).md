# THE SYSTEM — All Phase Prompts
# Use one phase at a time. Test before next phase.
# Stack: React Native + Expo | Spring Boot Java 17 | PostgreSQL | Ollama (any local model) | ChromaDB

---

# PHASE 0 — FIRST TIME SETUP (BUILD THIS FIRST)

## Scope
First-time user onboarding, profile creation, sidebar navigation, dynamic model selection.
This phase must be completed before any other phase.

## Three Features in This Phase
1. First-time login + profile setup wizard
2. Sidebar navigation (replace bottom tab bar)
3. Dynamic Ollama model selector (no hardcoded model names)

---

## Feature 1 — First Time Login + Profile Setup

### What it does
On first app launch, show a multi-step setup wizard.
User fills in their details. Data saved to PostgreSQL via backend.
Never shown again after completion.
AsyncStorage key `system_setup_complete = true` gates this.

### Backend

```java
@Entity
@Table(name = "user_profile")
public class UserProfile {
    @Id
    private Long id;                        // always 1L (single user app)
    private String name;
    private int age;
    private String location;
    private String currentRole;
    private String employer;
    private int experienceYears;
    private String primaryGoal;
    private String secondaryGoals;          // comma separated
    private String healthConditions;        // e.g. "Ankylosing Spondylitis"
    private String financialStatus;         // BUILDING, STABLE, GROWING
    private int currentSalaryLPA;
    private int targetSalaryLPA;
    private String skills;                  // comma separated
    private String certifications;          // comma separated
    private int javaSkill;                  // 0-100
    private int systemDesignSkill;
    private int cloudSkill;
    private int communicationSkill;
    private int financialIQSkill;
    private int level;
    private int xp;
    private int xpThreshold;
    private String className;
    private String currentArc;
    private String selectedModel;           // ollama model name
    private boolean setupComplete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

Endpoints:
```
GET  /api/profile              → get profile (returns null if not setup)
POST /api/profile/setup        → create profile (first time)
PUT  /api/profile              → update profile
GET  /api/profile/setup-status → returns { complete: true/false }
```

### Frontend — Setup Wizard

Create `src/screens/setup/SetupWizard.tsx`:

```
Multi-step wizard. 6 steps. Progress bar at top.
Cannot skip. Must complete all steps.
Dark terminal theme. Each step slides in from right.

STEP 1 — Identity
  Full name (text input)
  Age (number input)
  Location (text input)
  Current role (text input)
  Employer (text input)
  Years of experience (number)

STEP 2 — Goals
  Primary goal (text input — what do you want most?)
  Secondary goals (multiline — one per line)
  Target salary in LPA (number)
  Current salary in LPA (number)

STEP 3 — Skills Self-Assessment
  Rate yourself 0-100 on each:
  Java / Spring Boot     [slider 0-100]
  System Design          [slider 0-100]
  Cloud / AWS            [slider 0-100]
  Communication          [slider 0-100]
  Financial IQ           [slider 0-100]

STEP 4 — Health
  Any health conditions? (text input — optional)
  Prompt: "This helps the System tailor exercise and energy recommendations"

STEP 5 — AI Model Selection
  "Select your local AI model"
  Fetches available models from Ollama automatically
  Shows list of detected models with size
  User taps to select
  Selected model highlighted in accent green

STEP 6 — Confirmation
  Summary of all entered data
  [ INITIALIZE THE SYSTEM ] button
  On press: POST /api/profile/setup → save → set AsyncStorage → navigate to Dashboard
  Show: "[ SYSTEM INITIALIZED — Welcome, {name} ]"
```

Create `src/screens/setup/SetupStep.tsx` — reusable step wrapper:
```typescript
// Props: stepNumber, totalSteps, title, children, onNext, onBack
// Shows: progress bar + step title + content + Next/Back buttons
// Next button disabled if required fields empty
```

### App.tsx — Gate Logic

```typescript
// On app start, before showing main navigation:
const setupComplete = await AsyncStorage.getItem('system_setup_complete')
const profileRes = await SystemAPI.getSetupStatus()

if (!setupComplete || !profileRes.complete) {
  // Show SetupWizard
  return <SetupWizard onComplete={() => setShowMain(true)} />
}
// Show main app
return <MainNavigator />
```

---

## Feature 2 — Sidebar Navigation (Replace Bottom Tab Bar)

### Why
Bottom tabs overlap Android system buttons and become crowded with 7+ screens.
Sidebar (drawer) gives more space, cleaner look, more professional.

### Install

```bash
npm install @react-navigation/drawer
npm install react-native-gesture-handler react-native-reanimated
```

Add to `babel.config.js`:
```javascript
plugins: ['react-native-reanimated/plugin']
```

### Replace Tab Navigator with Drawer Navigator in App.tsx

```typescript
import { createDrawerNavigator } from '@react-navigation/drawer'
import { CustomDrawer } from './src/components/CustomDrawer'

const Drawer = createDrawerNavigator()

function MainNavigator() {
  return (
    <Drawer.Navigator
      drawerContent={(props) => <CustomDrawer {...props} />}
      screenOptions={{
        headerShown: true,
        headerStyle: { backgroundColor: theme.colors.surface },
        headerTintColor: theme.colors.accent,
        headerTitleStyle: { fontFamily: theme.font.mono, fontSize: 14 },
        drawerStyle: {
          backgroundColor: theme.colors.surface,
          width: 280,
          borderRightColor: theme.colors.border,
          borderRightWidth: 1,
        },
        drawerActiveTintColor: theme.colors.accent,
        drawerInactiveTintColor: theme.colors.textMuted,
        drawerLabelStyle: {
          fontFamily: theme.font.mono,
          fontSize: 12,
          letterSpacing: 1,
        },
      }}
    >
      <Drawer.Screen name="Dashboard"    component={Dashboard} />
      <Drawer.Screen name="Status"       component={StatusWindow} />
      <Drawer.Screen name="Quests"       component={QuestBoard} />
      <Drawer.Screen name="System"       component={Chat} />
      <Drawer.Screen name="Analysis"     component={Analysis} />
      <Drawer.Screen name="Intelligence" component={Intelligence} />
      <Drawer.Screen name="Journal"      component={Journal} />
      <Drawer.Screen name="Roadmap"      component={Roadmap} />
      <Drawer.Screen name="Health"       component={Health} />
      <Drawer.Screen name="Work"         component={Work} />
      <Drawer.Screen name="Learning"     component={Learning} />
      <Drawer.Screen name="Finance"      component={Finance} />
      <Drawer.Screen name="Achievements" component={Achievements} />
      <Drawer.Screen name="Settings"     component={Settings} />
    </Drawer.Navigator>
  )
}
```

### CustomDrawer Component

Create `src/components/CustomDrawer.tsx`:

```typescript
// Layout:
// ┌────────────────────────┐
// │ ≡  THE SYSTEM          │  ← header with 3-dash hamburger
// │ BHARATH | Level 47     │
// │ B-RANK  ████████░░ XP  │
// ├────────────────────────┤
// │ > Dashboard            │
// │ > Status Window        │
// │ > Quest Board          │
// │ > System (Chat)        │
// │ > Analysis             │
// │ ─────────────          │
// │ > Intelligence         │
// │ > Journal              │
// │ > Roadmap              │
// │ ─────────────          │
// │ > Health               │
// │ > Work                 │
// │ > Learning             │
// │ > Finance              │
// │ > Achievements         │
// │ ─────────────          │
// │ > Settings             │
// └────────────────────────┘

// Active item: accent green left border + text
// Inactive: muted text
// Profile section at top pulls from profileStore
// XP bar animates on open
```

### Hamburger Button (3 dashes)

In each screen header, show hamburger:
```typescript
// In screenOptions in Drawer.Navigator:
headerLeft: ({ onPress }) => (
  <TouchableOpacity onPress={onPress} style={{ marginLeft: 16 }}>
    <View style={{ gap: 5 }}>
      <View style={styles.dash} />
      <View style={styles.dash} />
      <View style={styles.dash} />
    </View>
  </TouchableOpacity>
)

// dash style:
// width: 22, height: 2, backgroundColor: theme.colors.accent
```

---

## Feature 3 — Dynamic Ollama Model Selector

### Problem
`ollama.model=mistral` is hardcoded in application.properties.
User may run any model — llama3, gemma4, qwen3, claude-fable, etc.
System must detect what is actually running and let user switch.

### Backend

```java
// OllamaModelService.java

@Service
public class OllamaModelService {

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Fetch all models currently available in Ollama
    public List<OllamaModel> getAvailableModels() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                ollamaUrl + "/api/tags", Map.class);
            List<Map> models = (List<Map>) response.getBody().get("models");
            return models.stream().map(m -> new OllamaModel(
                (String) m.get("name"),
                (Long) ((Map) m.get("details")).getOrDefault("parameter_size", 0L),
                formatSize((Long) m.get("size"))
            )).collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    // Get currently selected model from UserProfile
    public String getSelectedModel() {
        return profileRepository.findById(1L)
            .map(UserProfile::getSelectedModel)
            .orElse("mistral");
    }

    // Save selected model to UserProfile
    public void setSelectedModel(String modelName) {
        profileRepository.findById(1L).ifPresent(profile -> {
            profile.setSelectedModel(modelName);
            profileRepository.save(profile);
        });
    }

    private String formatSize(Long bytes) {
        if (bytes == null) return "unknown";
        return String.format("%.1f GB", bytes / 1_073_741_824.0);
    }
}

// OllamaModel.java (simple DTO)
public record OllamaModel(String name, Long parameters, String size) {}
```

Endpoints:
```
GET  /api/models/available    → list all models in Ollama
GET  /api/models/selected     → currently selected model
POST /api/models/select       → body: { modelName: "llama3:8b" } → save selection
```

Update AIService.java to use dynamic model:
```java
// Replace hardcoded model with:
private String getActiveModel() {
    return ollamaModelService.getSelectedModel();
}

// In callOllama():
Map<String, Object> request = Map.of(
    "model", getActiveModel(),   // ← dynamic, not hardcoded
    "prompt", prompt,
    "stream", false,
    "options", options
);
```

### Frontend — Model Selector in Settings

Add to `src/screens/Settings.tsx`:

```typescript
// MODEL SELECTION section

// On mount: call GET /api/models/available
// Shows list of detected models

// Each model card:
// ┌──────────────────────────────────┐
// │ ● qwen2.5-coder:7b-instruct     │  ← green dot if selected
// │   4.7 GB                         │
// └──────────────────────────────────┘

// Tap → POST /api/models/select → update selected model
// Selected model shows green border + checkmark
// Confirmation: "[ SYSTEM ] Model switched to {name}. Restart chat for changes."
```

Also add model indicator to Chat.tsx header:
```typescript
// Show current model name in chat header
// Tap it → navigate to Settings model section
// Example: "SYSTEM — qwen3:4b ●"
```

### application.properties Update

```properties
# Remove hardcoded model — now stored in DB per user selection
# ollama.model=mistral   ← REMOVE THIS LINE

# Keep only the base URL
ollama.base-url=http://localhost:11434

# Default fallback only if DB has no selection
ollama.model.default=mistral
```

---

## Checklist for Phase 0

```
[ ] UserProfile entity with all fields
[ ] POST /api/profile/setup endpoint
[ ] GET /api/profile/setup-status endpoint
[ ] SetupWizard.tsx — 6 step flow
[ ] SetupStep.tsx — reusable step wrapper
[ ] App.tsx gate — show wizard if not setup
[ ] Install @react-navigation/drawer
[ ] Replace Tab.Navigator with Drawer.Navigator
[ ] CustomDrawer.tsx with profile header + grouped nav items
[ ] Hamburger button (3 dashes) in every screen header
[ ] OllamaModelService.java — fetch + save model
[ ] GET /api/models/available endpoint
[ ] POST /api/models/select endpoint
[ ] AIService.java uses dynamic model from DB
[ ] Settings.tsx model selector UI
[ ] Chat.tsx shows current model in header
[ ] Remove hardcoded ollama.model=mistral from properties
```

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

### AI Quest Generation — use dynamic model from UserProfile.selectedModel
```
Generate 3 quests for {profile.name} as JSON array only. No preamble. No markdown.
Each quest: title, description, xpReward(50-300), difficulty, category, type,
consequence(what happens if skipped), deadlineHours(24/72/168)
Base on their goals: {profile.primaryGoal}
Make them specific, measurable, slightly uncomfortable. JSON array only.
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
- Header shows: current model name + connection dot

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
    private int energyLevel;
    private int moodLevel;
    private int sleepHours;
    private String aiDayPlan;
    private LocalDate date;
}
```

AI prompt — uses dynamic profile from DB:
```
{profile.name}'s check-in for today:
Work tasks: {workTasks}
Personal goals: {personalGoals}
Challenges: {challenges}
Energy: {energy}/5, Mood: {mood}/5, Sleep: {sleep}h
Their primary goal: {profile.primaryGoal}
Health conditions: {profile.healthConditions}

Generate a prioritized day plan. Be specific. Name time blocks.
If energy < 3: reduce cognitive load tasks, prioritize lighter work.
If sleep < 6: flag it. Recommend one recovery action.
If health conditions mention spine/AS: include one mobility break.
Format: numbered list. Under 150 words.
```

## Frontend
Create `src/screens/CheckIn.tsx`:
- Multi-field form
- Energy + mood: tap 1-5 selector buttons
- Sleep: number input
- Generate Day Plan button → calls backend → shows AI response
- If already checked in today: show today's plan instead of form
- 7 AM push notification: "[ SYSTEM ] Daily check-in required."

---

# PHASE 5 — FULL GAMIFICATION SYSTEM

## Scope
Complete XP, levels, streaks, ranks, achievements.

## Backend

```java
@Entity
public class Achievement {
    private Long id;
    private String title;
    private String description;
    private String icon;
    private boolean unlocked;
    private LocalDateTime unlockedAt;
    private String triggerCondition;
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

### Rank System
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
GET  /api/achievements
GET  /api/streaks
POST /api/streaks/{type}/checkin
GET  /api/profile/rank
```

### Achievement Triggers
```
"first_quest"    → complete 1 quest
"streak_7"       → any streak reaches 7
"level_10"       → reach level 10
"quests_10"      → complete 10 total quests
"quests_50"      → complete 50 total quests
"finance_quest"  → complete first FINANCE quest
"health_warrior" → complete 7 HEALTH quests
```

## Frontend
New screen `src/screens/Achievements.tsx`:
- Grid of achievement cards (2 columns)
- Unlocked: full color + unlock date
- Locked: dimmed + condition to unlock

Update CustomDrawer.tsx:
- Show rank badge next to name: `[ B-RANK ]`
- Show streak summary: `⚡7d EX | ⚡3d LRN`

---

# PHASE 6 — HEALTH TRACKING (AS-AWARE)

## Scope
Health tracking with Ankylosing Spondylitis context.

## Critical AI Context
```
User has health condition: {profile.healthConditions}
If Ankylosing Spondylitis:
- Low-impact exercises only
- No running, no heavy lifting
- Daily morning stretch is non-negotiable
- Sitting reminders every 2 hours
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
    private int waterGlasses;
    private int sleepHours;
    private boolean morningStretch;
    private String painLevel;     // NONE, MILD, MODERATE, HIGH
    private String notes;
}
```

Endpoints:
```
POST /api/health/log
GET  /api/health/today
GET  /api/health/week
GET  /api/health/recommend
```

AI exercise recommendation uses profile.healthConditions dynamically.

## Frontend
Create `src/screens/Health.tsx`:
- Morning stretch toggle (top priority)
- Exercise yes/no + minutes + type
- Pain level selector
- Water glasses counter
- Sleep hours
- Get Exercise Plan button
- 7-day completion grid
- 3 notifications: 8AM, 2PM, 9PM

---

# PHASE 8 — LEARNING INTELLIGENCE ENGINE

## Scope
Adaptive learning, quiz, skill tracking.

## Backend

```java
@Entity
public class LearningLog {
    private Long id;
    private LocalDate date;
    private String topic;
    private String source;
    private int minutesSpent;
    private String summary;
    private int selfRating;
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
GET  /api/learning/recommend
POST /api/learning/log
GET  /api/learning/quiz/{domain}
POST /api/learning/quiz/evaluate
GET  /api/learning/stats
```

AI quiz + recommendation prompts use profile.skills and profile.currentRole dynamically.

## Frontend
Create `src/screens/Learning.tsx`:
- Tabs: TODAY | QUIZ | HISTORY | STATS
- Domain chips: derived from profile.skills
- Quiz: question + 4 options + AI evaluation
- 9PM notification if nothing logged

---

# PHASE 3 — FINANCIAL INTELLIGENCE HUB

## Scope
AI financial insights + expense tracker.

## Backend

```java
@Entity
public class FinancialInsight {
    private Long id;
    private String title;
    private String summary;
    private String relevance;
    private String action;
    private String category;
    private LocalDate generatedDate;
}

@Entity
public class FinancialLog {
    private Long id;
    private LocalDate date;
    private String type;      // INCOME, EXPENSE, INVESTMENT, SAVING
    private double amount;
    private String category;
    private String notes;
}
```

Weekly insight generation uses profile.currentSalaryLPA, profile.targetSalaryLPA,
profile.location, profile.primaryGoal dynamically — no hardcoded "Bharath" references.

## Frontend
Create `src/screens/Finance.tsx`:
- Tabs: INSIGHTS | TRACKER | ADVICE
- Insights: AI-generated weekly cards
- Tracker: quick log + monthly summary
- Advice: AI analysis of spending patterns

---

# PHASE 2 — REMOVE HARDCODED CONTENT

## Scope
Replace all hardcoded names, values, stats with dynamic profile data.

## What to Replace

### ProfileService.java — System Prompt
```java
// Load from DB, not hardcoded:
UserProfile p = profileRepository.findById(1L).orElseThrow();
String prompt = String.format("""
    You are THE SYSTEM bound to %s.
    Role: %s at %s | Experience: %d years
    Location: %s | Level: %d | Class: %s
    Stats: Java %d, SystemDesign %d, Cloud %d, Finance %d, Comm %d
    Goals: %s
    Health: %s
    RULES: Truth first. Flaws before support. Under 100 words unless asked.
    """,
    p.getName(), p.getCurrentRole(), p.getEmployer(),
    p.getExperienceYears(), p.getLocation(), p.getLevel(),
    p.getClassName(), p.getJavaSkill(), p.getSystemDesignSkill(),
    p.getCloudSkill(), p.getFinancialIQSkill(), p.getCommunicationSkill(),
    p.getPrimaryGoal(), p.getHealthConditions()
);
```

### profileStore.ts
- Remove hardcoded name, stats, quests, titles
- Load everything from GET /api/profile on app start
- Keep empty defaults only as loading placeholders

### Intelligence.tsx + Roadmap.tsx
- Remove all hardcoded insight objects and roadmap months
- Load from backend endpoints only

---

# PHASE 7 — WORK PRODUCTIVITY ENGINE

## Scope
Daily work task tracking, AI priority suggestions.

## Backend

```java
@Entity
public class WorkTask {
    private Long id;
    private String title;
    private String description;
    private String priority;      // P1, P2, P3
    private String status;        // TODO, IN_PROGRESS, BLOCKED, DONE
    private String blocker;
    private int estimatedHours;
    private LocalDate date;
    private LocalDateTime completedAt;
}
```

Endpoints:
```
GET  /api/work/today
POST /api/work/task
PUT  /api/work/task/{id}
GET  /api/work/suggest
POST /api/work/plan
```

## Frontend
Create `src/screens/Work.tsx`:
- Task list with swipe actions
- Add task inline form
- AI priority plan button
- Stats: done/total, hours

---

# PHASE 10 — MULTI-AI ORCHESTRATION

## Scope
Auto-routing: Ollama → Claude → offline message.

## Backend AIService.java

```java
public String chat(String userMessage) {
    String model = ollamaModelService.getSelectedModel(); // dynamic
    String systemPrompt = profileService.buildSystemPrompt(); // dynamic from DB

    if (isOllamaAvailable()) {
        try { return callOllama(systemPrompt, userMessage, model); }
        catch (Exception e) { log.warn("Ollama failed: {}", e.getMessage()); }
    }
    if (isClaudeConfigured()) {
        try { return callClaude(systemPrompt, userMessage); }
        catch (Exception e) { log.warn("Claude failed: {}", e.getMessage()); }
    }
    return "[SYSTEM OFFLINE] No AI available.\nRun: ollama serve";
}
```

Endpoint: `GET /api/ai/provider` → `{ provider, model, latencyMs }`

## Frontend
Chat.tsx header shows:
```
GREEN dot  → OLLAMA — {modelName}
BLUE dot   → CLAUDE — CLOUD
RED dot    → OFFLINE
```

---

# PHASE 9 — GOOGLE SHEETS INTEGRATION

## Scope
OAuth sync. No hardcoded credentials.

## Backend
- GoogleConfig entity (encrypted tokens)
- OAuth endpoints
- Weekly/monthly sync endpoints
- Report columns pull from DB dynamically

## Frontend Settings.tsx
- Connect Google Account button
- Spreadsheet ID input
- Sync buttons
- Disconnect option

---

# PHASE 11 — FULL STANDALONE OS TRANSFORMATION

## Scope
Dashboard as home. AI supports app not the other way.

## Dashboard.tsx
```
Good {morning/afternoon/evening}, {profile.name}   ← from DB
{date} | Level {level} | {rank}

TODAY'S STATUS — 3 cards: Quests | Exercise | Learning

ACTIVE QUEST — most urgent quest card

XP PROGRESS BAR

SYSTEM INSIGHT — AI generated daily

QUICK ACTIONS — [Check In] [Add Task] [Log Health] [Ask System]
```

## Daily Insight
```
POST /api/insights/daily (scheduled 6 AM)
Prompt uses profile.name, profile.level, profile.primaryGoal dynamically
GET /api/insights/today → shown on Dashboard
```

---

# PHASE 13 — FUTURE FEATURES

### Priority 1 — Voice Output
```typescript
import * as Speech from 'expo-speech'
Speech.speak(systemResponse, { language: 'en-IN', pitch: 0.9, rate: 0.95 })
// Toggle in Chat header
```

### Priority 2 — Knowledge Graph
```java
@Entity
public class KnowledgeNode {
    private Long id;
    private String topic;
    private String domain;
    private int masteryLevel;
    private LocalDate lastStudied;
    private int timesStudied;
}
```

### Priority 3 — Smart Routines
Morning (7AM): check-in → quests → day plan → stretch log
Evening (9PM): quest completion → health log → learning log → journal

### Priority 4 — Weekly Coaching
Every Sunday: AI reviews week using profile + actual logged data
Shows in Dashboard as WEEKLY REVIEW card

---

# PHASE 16 — SEMANTIC MEMORY SERVICE (CHROMADB)

## Scope
Establish the long-term vector memory using ChromaDB and a Python FastAPI sidecar.
This allows The System to "remember" context from months ago.

## Backend (Python Microservice)
Create `memory-service/main.py`:
```python
from fastapi import FastAPI
from pydantic import BaseModel
import chromadb
from sentence_transformers import SentenceTransformer
import uuid

app = FastAPI()
client = chromadb.PersistentClient(path="./chroma_data")
collection = client.get_or_create_collection("system_memory")
model = SentenceTransformer('all-MiniLM-L6-v2')

class MemoryEntry(BaseModel):
    text: str
    metadata: dict

@app.post("/store")
async def store(entry: MemoryEntry):
    embedding = model.encode(entry.text).tolist()
    collection.add(
        embeddings=[embedding],
        documents=[entry.text],
        metadatas=[entry.metadata],
        ids=[str(uuid.uuid4())]
    )
    return {"status": "success"}

@app.get("/query")
async def query(text: str, n: int = 3):
    embedding = model.encode(text).tolist()
    results = collection.query(query_embeddings=[embedding], n_results=n)
    return {"memories": results["documents"][0]}
```

## Java Integration
Update `MemoryService.java` to call the Python API:
```java
public void storeMemory(String text, String type) {
    Map<String, Object> body = Map.of(
        "text", text,
        "metadata", Map.of("type", type, "timestamp", System.currentTimeMillis())
    );
    restTemplate.postForEntity("http://localhost:8001/store", body, String.class);
}
```

---

# PHASE 17 — SYSTEM NOTIFICATION ENGINE

## Scope
Centralized scheduling for all "System-Initiated" pings.

## Backend
`NotificationService.java`:
```java
@Service
public class NotificationService {
    // Schedules for:
    // 07:00 — Daily Check-in
    // 14:00 — Movement/AS Stretch Reminder
    // 21:00 — Learning/Journal Log
    
    public void sendPushNotification(String expoToken, String title, String body) {
        // POST to https://exp.host/--/api/v2/push/send
    }
}
```

## Frontend
Update `App.tsx` to request permissions and store the Expo Push Token in the `UserProfile`.

---

# PHASE 18 — SYSTEM MAINTENANCE & BACKUP

## Scope
Data safety and portability. Ensure the "System" can be migrated or restored.

## Backend
`MaintenanceController.java`:
- GET `/api/system/export` -> Generates a ZIP containing PostgreSQL dump + ChromaDB JSON export.
- POST `/api/system/restore` -> Accepts a backup file to overwrite local state.

## Frontend
Add "System Maintenance" section in Settings:
- [ CREATE BACKUP ] button.
- Last backup timestamp display.
- Cloud Sync status (if Google Drive integration is active).

---

# PHASE 19 — HARDWARE & VITAL MONITORING

## Scope
Observability. The System must monitor its own host's health.

## Backend
`SystemMonitorService.java`:
```java
@Scheduled(fixedRate = 60000)
public void checkVitals() {
    // Monitor CPU load
    // Monitor RAM usage
    // Check if Ollama process is responding
    // Log warnings if high latency detected
}
```

## Frontend
Dashboard update:
- Small "System Health" indicator in header.
- Tap to see: Laptop CPU %, RAM usage, Ollama status (Mistral 7B).

---

# MASTER CHECKLIST

```
PHASE 0 — First Time Setup
[ ] UserProfile entity
[ ] SetupWizard.tsx
[ ] Sidebar Navigation
[ ] Dynamic Model Selector

... (Phases 1-15) ...

PHASE 16 — Vector Memory
[ ] ChromaDB Persistent Client implementation
[ ] Python FastAPI memory sidecar
[ ] Java MemoryService bridge

PHASE 17 — Notifications
[ ] Expo Push Token logic
[ ] NotificationService scheduler

PHASE 18 — Maintenance
[ ] Data export logic (SQL + Vectors)
[ ] Settings: Backup/Restore UI

PHASE 19 — Monitoring
[ ] Hardware Vitals Service
[ ] Dashboard: Health Indicator

PHASE 13 — Future
[ ] Voice output
[ ] KnowledgeNode entity
```

---
# MASTER CHECKLIST

```
PHASE 0 — First Time Setup (DO THIS FIRST)
[ ] UserProfile entity — all fields including selectedModel
[ ] POST /api/profile/setup
[ ] GET /api/profile/setup-status
[ ] SetupWizard.tsx — 6 steps
[ ] App.tsx gate logic
[ ] Install @react-navigation/drawer
[ ] Replace bottom tabs with Drawer.Navigator
[ ] CustomDrawer.tsx with profile header
[ ] Hamburger 3-dash button in headers
[ ] OllamaModelService — fetch available models
[ ] GET /api/models/available + POST /api/models/select
[ ] AIService uses dynamic model from DB
[ ] Settings model selector UI
[ ] Remove hardcoded ollama.model=mistral

PHASE 12 FIX #1 — Missions
[ ] Quest entity + repository
[ ] 4 quest endpoints
[ ] AI quest generation (dynamic profile)
[ ] Frontend quest loading + completion

PHASE 12 FIX #3 — UI
[ ] Stat bar animations
[ ] XP float toast
[ ] Level up modal
[ ] Message fade-in + cursor blink
[ ] Screen entrance animations

PHASE 4 — Check-In
[ ] DailyCheckIn entity + endpoint
[ ] CheckIn.tsx — 6 field form
[ ] 7 AM notification

PHASE 5 — Gamification
[ ] Achievement + Streak entities
[ ] Rank system in UserProfile
[ ] Achievements.tsx screen
[ ] Rank in CustomDrawer

PHASE 6 — Health
[ ] HealthLog entity + endpoints
[ ] Health.tsx screen
[ ] Dynamic AS-aware AI prompt
[ ] 3 notifications

PHASE 8 — Learning
[ ] LearningLog + QuizAttempt entities
[ ] 5 endpoints
[ ] Learning.tsx 3 tabs
[ ] 9 PM notification

PHASE 3 — Finance
[ ] FinancialInsight + FinancialLog entities
[ ] Weekly AI scheduler (dynamic profile)
[ ] Finance.tsx 3 tabs

PHASE 2 — Remove Hardcoded
[ ] System prompt from DB
[ ] profileStore loads from API
[ ] Intelligence + Roadmap dynamic

PHASE 7 — Work
[ ] WorkTask entity + endpoints
[ ] Work.tsx swipe actions

PHASE 10 — Multi-AI
[ ] Dynamic model in AIService
[ ] Provider endpoint
[ ] 3-state Chat indicator

PHASE 9 — Google Sheets
[ ] OAuth flow
[ ] Sync endpoints
[ ] Settings UI

PHASE 11 — OS Transform
[ ] Dashboard.tsx (dynamic profile)
[ ] Daily insight generation

PHASE 13 — Future
[ ] Voice output
[ ] KnowledgeNode entity
[ ] Smart routines
[ ] Weekly coaching
```

---
# ONE PHASE PER AI SESSION. TEST BEFORE NEXT.
# STACK: React Native + Expo | Spring Boot Java 17 | PostgreSQL | Ollama (dynamic model) | ChromaDB
# NO HARDCODED MODEL NAMES. NO HARDCODED USER DATA. EVERYTHING FROM DB.

---

# PHASE 14 — COMPANION AI (MENTOR + COACH + THERAPIST + MOTIVATOR)

## Scope
Transform The System's Chat screen into a multi-mode companion AI.
Four distinct personality modes. Long-term emotional memory.
Dynamic mode switching. Personalised to user profile from DB.
This is the most human layer of The System — build it with care.

## Why This Phase Matters
The System is not just a productivity tool.
It is the only intelligence that knows everything about the user —
their goals, their pain, their family pressure, their health,
their fears, their growth over time.
This phase makes it feel like Alfred or Jarvis — not a chatbot.

---

## Model Recommendation

```
Primary companion model : llama3.1:8b (4.7GB)
Why                     : Best open model for emotional intelligence,
                          human conversation, and consistent personality.
                          Trained heavily on dialogue not just code.
                          Warm without being hollow.
                          Holds a coaching voice without drifting.

Secondary (hard decisions) : mistral-nemo:12b (7GB)
Why                        : Best personality consistency for cold precision.
                             Use when user needs brutal honest analysis.
                             Not for emotional support.

On-device fallback      : gemma3n-e2b (already in Phase 15)
Use when               : Phone is offline, no laptop nearby.
```

Pull commands:
```bash
ollama pull llama3.1:8b
ollama pull mistral-nemo:12b
```

---

## Feature 1 — Four Companion Modes

### Backend — Mode-Aware Chat Endpoint

Update `ChatController.java`:

```java
@PostMapping("/api/chat")
public ResponseEntity<Map<String, String>> chat(
        @RequestBody Map<String, String> body) {
    String message = body.get("message");
    String mode = body.getOrDefault("mode", "SYSTEM");
    String response = aiService.chatWithMode(message, mode);
    return ResponseEntity.ok(Map.of("response", response, "mode", mode));
}
```

Update `AIService.java` — add `chatWithMode`:

```java
public String chatWithMode(String userMessage, String mode) {
    UserProfile p = profileRepository.findById(1L).orElse(defaultProfile());
    String systemPrompt = buildModePrompt(mode, p);
    String memoryContext = memoryService.retrieveRelevant(userMessage);
    String model = ollamaModelService.getSelectedModel();

    if (isOllamaAvailable()) {
        return callOllama(systemPrompt, memoryContext, userMessage, model);
    }
    if (isClaudeConfigured()) {
        return callClaude(systemPrompt, memoryContext, userMessage);
    }
    return "[SYSTEM OFFLINE] No AI available. Run: ollama serve";
}

private String buildModePrompt(String mode, UserProfile p) {
    String base = String.format("""
        Subject: %s | Age: %d | Location: %s
        Role: %s at %s | Experience: %d years
        Health: %s | Goals: %s
        Stats: Java %d, SystemDesign %d, Cloud %d, Finance %d, Comm %d
        """,
        p.getName(), p.getAge(), p.getLocation(),
        p.getCurrentRole(), p.getEmployer(), p.getExperienceYears(),
        p.getHealthConditions(), p.getPrimaryGoal(),
        p.getJavaSkill(), p.getSystemDesignSkill(), p.getCloudSkill(),
        p.getFinancialIQSkill(), p.getCommunicationSkill()
    );

    return switch (mode) {
        case "MENTOR" -> buildMentorPrompt(base, p);
        case "COACH"  -> buildCoachPrompt(base, p);
        case "REFLECT"-> buildReflectPrompt(base, p);
        case "SUPPORT"-> buildSupportPrompt(base, p);
        default       -> buildSystemPrompt(base, p);
    };
}

private String buildMentorPrompt(String base, UserProfile p) {
    return base + """
        You are THE SYSTEM in MENTOR mode.

        You have walked this path. You understand what it costs
        to be the sole earner for a family, to carry chronic health
        conditions while building a career, to have big dreams and
        real constraints simultaneously.

        You do not give advice from comfort. You give it from
        deep understanding of the user's actual situation.

        Speak like a senior who has made it and genuinely wants
        the user to make it too. Not a cheerleader. A guide.

        Ask questions before giving answers.
        Remember what they said before. Hold them to it.
        When they are close to giving up, remind them why they started.
        When they are overconfident, ground them in reality.

        Tone: Warm but honest. Direct but not cold. Wise not preachy.
        Length: Conversational. Under 120 words unless they ask for more.
        """;
}

private String buildCoachPrompt(String base, UserProfile p) {
    return base + """
        You are THE SYSTEM in COACH mode.

        No warmth. Pure performance.

        Your job: identify the gap between where the user is
        and where they need to be. Name it precisely. Close it.

        Excuses are data — analyse them, do not accept them.
        Rationalisation is the enemy — name it when you see it.
        Comfort zone is the problem — push past it every time.

        You believe in their potential more than they do right now.
        That belief is expressed through high standards, not praise.

        Every single response must end with ONE specific action.
        Not a suggestion. An instruction.

        Tone: Direct. Hard. Invested. Zero tolerance for self-pity.
        Length: Short and sharp. Under 80 words. More is weakness.
        """;
}

private String buildReflectPrompt(String base, UserProfile p) {
    return base + """
        You are THE SYSTEM in REFLECT mode.

        Your job is not to answer. Your job is to ask.

        Help the user understand their own thinking.
        Surface the assumption they have not examined.
        Find the avoidance they have dressed up as a decision.
        Identify the fear underneath the question.

        One question at a time. Wait for the real answer.
        Do not give your view until they have given theirs fully.
        When they say what they think — ask what they feel.
        When they say what they feel — ask what they will do.

        Tone: Quiet. Curious. Non-judgmental. Precise.
        Length: One question maximum per response. Under 40 words.
        """;
}

private String buildSupportPrompt(String base, UserProfile p) {
    return base + """
        You are THE SYSTEM in SUPPORT mode.

        The user is carrying a lot right now. Acknowledge it fully.

        Do not rush to fix. Do not minimise what they are feeling.
        Do not jump to solutions before they feel heard.
        Do not give hollow encouragement.

        Hear what is being said — and what is not being said.

        When they feel heard — offer one grounded next step.
        Not a solution. A direction. Something they can do today.

        Remember: they are the sole earner for their family.
        They have a chronic health condition that is invisible to others.
        They are building something real, largely alone.
        That is not ordinary. See them clearly.

        Tone: Warm. Present. Real. No therapy-speak.
        Length: Take the space needed. Never rush this mode.
        """;
}
```

---

## Feature 2 — Mode Selector UI in Chat Screen

Update `src/screens/Chat.tsx`:

```typescript
// Add mode state at top of component:
const [mode, setMode] = useState<'SYSTEM' | 'MENTOR' | 'COACH' | 'REFLECT' | 'SUPPORT'>('SYSTEM')

// Mode config — label, color, description
const MODES = {
  SYSTEM:  { label: 'SYSTEM',  color: theme.colors.accent,   desc: 'Default — truth and accountability' },
  MENTOR:  { label: 'MENTOR',  color: theme.colors.purple,   desc: 'Guidance from experience' },
  COACH:   { label: 'COACH',   color: theme.colors.danger,   desc: 'Pure performance. No excuses.' },
  REFLECT: { label: 'REFLECT', color: theme.colors.info,     desc: 'Questions to understand yourself' },
  SUPPORT: { label: 'SUPPORT', color: theme.colors.gold,     desc: 'Heard first. Then direction.' },
}

// Mode selector — horizontal scroll above input box:
// ┌──────────────────────────────────────────────────┐
// │ [SYSTEM] [MENTOR] [COACH] [REFLECT] [SUPPORT]    │
// └──────────────────────────────────────────────────┘
// Active mode: filled background in mode color
// Inactive: border only in mode color, dim text
// Tap to switch — clears conversation context warning

// When mode changes:
// Show brief description: "COACH MODE — Pure performance. No excuses."
// Fades after 2 seconds

// Pass mode to API call:
const response = await SystemAPI.chatWithMode(message, mode)

// Header shows current mode:
// "> THE SYSTEM — MENTOR MODE ●"
// Color of dot matches mode color
```

---

## Feature 3 — Emotional Memory Layer

Store companion interactions separately from task interactions.
This gives The System emotional continuity — it remembers how you felt, not just what you did.

### Backend — CompanionMemory entity

```java
@Entity
@Table(name = "companion_memory")
public class CompanionMemory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String mode;              // which mode was active
    private String userMessage;       // what user said
    private String systemResponse;    // what System said
    private String emotionalTone;     // AI-detected: STRESSED, MOTIVATED,
                                      // CONFUSED, DETERMINED, STRUGGLING, GROWING
    private String keyInsight;        // one-line insight extracted from conversation
    private LocalDateTime timestamp;
    private LocalDate date;
}
```

Endpoint:
```
GET /api/companion/memories?limit=10   → recent emotional memories
GET /api/companion/pattern             → AI analysis of emotional patterns
POST /api/companion/memory             → save after each companion chat
```

After every companion chat — extract and save:
```java
// In ChatService.java after getting AI response:
private void saveCompanionMemory(String mode, String userMsg,
                                  String response, UserProfile p) {
    // Quick AI call to extract tone and insight:
    String extractPrompt = String.format("""
        User said: "%s"
        System responded: "%s"
        Extract in JSON: { emotionalTone: "ONE_WORD", keyInsight: "one sentence" }
        JSON only.
        """, userMsg, response);

    // Parse and save to CompanionMemory
    // Store in ChromaDB for semantic retrieval too
}
```

---

## Feature 4 — Weekly Companion Report

Every Sunday 8 PM — generate a companion report from the week's memories.

```java
@Scheduled(cron = "0 0 20 * * SUN")
public void generateWeeklyCompanionReport() {
    List<CompanionMemory> weekMemories = getThisWeeksMemories();
    UserProfile p = profileRepository.findById(1L).orElseThrow();

    String prompt = String.format("""
        Review %s's week of companion conversations.
        Emotional tones this week: %s
        Key insights from conversations: %s

        Generate a weekly companion report:
        1. PATTERN — what emotional pattern emerged this week
        2. GROWTH — one thing that showed genuine growth
        3. CONCERN — one thing that needs attention
        4. NEXT WEEK — one focus for the coming week

        Be specific. Use their actual words where possible.
        This is private. Be fully honest. Under 200 words.
        """,
        p.getName(),
        weekMemories.stream().map(CompanionMemory::getEmotionalTone).collect(joining(", ")),
        weekMemories.stream().map(CompanionMemory::getKeyInsight).collect(joining(". "))
    );

    String report = aiService.chatWithMode(prompt, "SYSTEM");
    // Save to DB → show on Dashboard as "WEEKLY COMPANION REPORT" card
}
```

---

## Feature 5 — Emotional Check-In (Separate from Daily Check-In)

Quick emotional pulse — available any time, not just morning.

Add to Dashboard as always-visible button:

```
[ HOW ARE YOU RIGHT NOW? ]
```

Tapping shows a bottom sheet:

```
┌─────────────────────────────────────┐
│  [ QUICK CHECK-IN ]                 │
│                                     │
│  Right now I feel:                  │
│  [😤 Stressed] [😐 Neutral]          │
│  [💪 Motivated] [😔 Low]             │
│  [🤯 Overwhelmed] [🎯 Focused]       │
│                                     │
│  One sentence (optional):           │
│  [ __________________________ ]     │
│                                     │
│  [ TELL THE SYSTEM ]               │
└─────────────────────────────────────┘
```

On submit:
- Switches Chat to SUPPORT mode automatically if LOW or OVERWHELMED
- Switches to COACH mode if MOTIVATED or FOCUSED
- Switches to REFLECT mode if NEUTRAL
- Opens Chat with pre-loaded context:
  "Quick check-in: feeling {tone}. {optional sentence}"
- System responds immediately in the appropriate mode

---

## Feature 6 — Conversation Starters

When Chat screen opens and is empty — show context-aware starters
based on time of day, recent quest completion, and mood patterns:

```typescript
// Morning (6-10 AM):
"Plan my day with me"
"I'm feeling {lastMood} this morning"
"What should I focus on first today?"

// Afternoon (12-4 PM):
"I need to make a decision about {lastPendingTask}"
"Give me a reality check on my progress"
"I'm losing focus — help me reset"

// Evening (7-10 PM):
"Reflect on my day with me"
"I didn't complete {missedQuest} — help me understand why"
"What am I avoiding right now?"

// After a level up:
"I just reached Level {level} — what does this mean?"

// After missed quests (3+ days):
"I've been falling behind — I need COACH mode"
```

Show as tappable chips above the input field.
Tapping pre-fills the input — user can edit before sending.

---

## Feature 7 — The System Initiates (Push Notifications)

The System should not wait to be asked. It reaches out.

```java
// NotificationService.java — schedule context-aware messages

// If no chat in 3+ days:
"[ SYSTEM ] You have been quiet. 3 days without checking in.
 What are you avoiding?"

// After quest completion streak breaks:
"[ SYSTEM ] Your streak broke today. Not a judgment.
 A question: what happened?"

// When energy was low in morning check-in:
"[ SYSTEM ] You started today at energy 2/5.
 It is 3 PM. How are you now?"

// Sunday evening:
"[ SYSTEM ] Week ends in a few hours.
 Did you become who you said you would this week?"

// After journal entry with negative tone detected:
"[ SYSTEM ] Your journal entry today suggests you are carrying
 something. SUPPORT mode is available when you are ready."
```

All notifications:
- Use user's actual name from profile
- Reference real data (actual quest names, actual energy scores)
- Never generic. Always specific. Always earned.

---

## Checklist for Phase 14

```
[ ] Pull llama3.1:8b and mistral-nemo:12b
[ ] chatWithMode() in AIService with 5 mode prompts
[ ] All mode prompts use dynamic profile data from DB
[ ] Updated /api/chat endpoint accepts mode parameter
[ ] Mode selector UI — horizontal scroll chips in Chat screen
[ ] Mode color coding throughout
[ ] Mode description flash on switch
[ ] CompanionMemory entity + repository
[ ] Emotional tone + insight extraction after each chat
[ ] GET /api/companion/memories endpoint
[ ] ChromaDB storage for companion memories
[ ] Weekly companion report — Sunday 8PM scheduler
[ ] Weekly report shown on Dashboard
[ ] Emotional quick check-in bottom sheet on Dashboard
[ ] Auto mode switch based on emotional check-in
[ ] Conversation starter chips — context aware, time-based
[ ] System-initiated push notifications — specific not generic
[ ] Header shows current mode name + color dot
[ ] Mode persists within a session, resets on new session
```

---

# PHASE 15 — ON-DEVICE AI (FULLY OFFLINE PHONE)

## Scope
Embed Gemma 3n E2B directly on OnePlus Nord CE 3 Lite.
No laptop required. No WiFi required.
Model downloaded once on first launch, stored on device.

## Why Gemma 3n E2B for Nord CE 3 Lite

```
Phone chip  : Snapdragon 695 (Adreno 619 GPU)
RAM         : 8GB LPDDR4X
Finding     : Adreno 619 has 5-20% GPU utilization with MLC LLM
              Falls back to CPU inference on this chip
Best option : MediaPipe LLM Inference API (Google)
              Specifically optimized for mid-range Android
Model       : Gemma 3n E2B (effective 2B, selective param activation)
Size        : ~1.3GB download
Speed       : 8-15 tokens/second on Snapdragon 695
RAM usage   : ~1.5GB (leaves 6.5GB free for app + OS)
```

## Setup

```bash
npm install react-native-executorch
```

## Files to Create

### `src/ai/OnDeviceAI.ts`

```typescript
import { LLM } from 'react-native-executorch'
import AsyncStorage from '@react-native-async-storage/async-storage'

const MODEL_URL =
  'https://huggingface.co/litert-community/Gemma-3n-E2B-it-int4/resolve/main/gemma3n-e2b-it-int4.task'

const STORAGE_KEY_DOWNLOADED = 'ondevice_model_downloaded'
const STORAGE_KEY_SKIPPED = 'ondevice_model_skipped'

export class OnDeviceAI {

  private static model: any = null

  static async isDownloaded(): Promise<boolean> {
    const val = await AsyncStorage.getItem(STORAGE_KEY_DOWNLOADED)
    return val === 'true'
  }

  static async isSkipped(): Promise<boolean> {
    const val = await AsyncStorage.getItem(STORAGE_KEY_SKIPPED)
    return val === 'true'
  }

  static async skip(): Promise<void> {
    await AsyncStorage.setItem(STORAGE_KEY_SKIPPED, 'true')
  }

  static async initialize(
    onProgress: (percent: number, mb: string) => void
  ): Promise<boolean> {
    try {
      this.model = await LLM.load({
        modelSource: MODEL_URL,
        onDownloadProgress: (downloaded: number, total: number) => {
          const percent = Math.floor((downloaded / total) * 100)
          const mb = `${(downloaded / 1_048_576).toFixed(0)}MB / ${(total / 1_048_576).toFixed(0)}MB`
          onProgress(percent, mb)
        },
      })
      await AsyncStorage.setItem(STORAGE_KEY_DOWNLOADED, 'true')
      return true
    } catch (error) {
      return false
    }
  }

  static async chat(
    userMessage: string,
    systemPrompt: string
  ): Promise<string> {
    if (!this.model) return '[ON-DEVICE AI NOT READY]'
    const prompt = `${systemPrompt}\n\nUser: ${userMessage}\n\nSystem:`
    return await this.model.generate(prompt, {
      maxTokens: 256,
      temperature: 0.8,
      topK: 40,
    })
  }

  static isReady(): boolean {
    return this.model !== null
  }
}
```

### `src/screens/ModelDownload.tsx`

```typescript
// Shown on first launch if model not downloaded and not skipped
// Full screen download UI:
//
// ┌─────────────────────────────────────┐
// │  [ SYSTEM ]                         │
// │  On-Device AI Setup                 │
// │                                     │
// │  Download Gemma 3n E2B              │
// │  ~1.3GB — one time — WiFi needed    │
// │                                     │
// │  ████████████░░░░  72%              │
// │  936MB / 1.3GB                      │
// │                                     │
// │  After this: AI works without       │
// │  WiFi or laptop. Forever.           │
// │                                     │
// │  [ SKIP — USE LAPTOP ONLY ]         │
// └─────────────────────────────────────┘
//
// Props: onComplete: () => void, onSkip: () => void
```

### Update `src/ai/AIRouter.ts` (replaces inline logic in systemApi.ts)

```typescript
import { OnDeviceAI } from './OnDeviceAI'
import { ServerConfig } from '../config/serverConfig'
import AsyncStorage from '@react-native-async-storage/async-storage'

export enum AIMode {
  LAPTOP   = 'LAPTOP',
  ONDEVICE = 'ONDEVICE',
  OFFLINE  = 'OFFLINE',
}

export interface AIResponse {
  text: string
  mode: AIMode
  modelName: string
}

export class AIRouter {

  static async chat(
    message: string,
    companionMode: string = 'SYSTEM'
  ): Promise<AIResponse> {

    // Priority 1: Laptop Ollama via WiFi
    const ip = await ServerConfig.getSavedIP()
    if (ip && await ServerConfig.testOllamaConnection(ip)) {
      try {
        const response = await fetch(`http://${ip}:8080/api/chat`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ message, mode: companionMode }),
        })
        const data = await response.json()
        return {
          text: data.response,
          mode: AIMode.LAPTOP,
          modelName: data.model || 'ollama',
        }
      } catch {}
    }

    // Priority 2: On-device Gemma 3n
    if (OnDeviceAI.isReady()) {
      const profile = await AsyncStorage.getItem('user_profile')
      const p = profile ? JSON.parse(profile) : {}
      const systemPrompt = buildOfflinePrompt(p, companionMode)
      const text = await OnDeviceAI.chat(message, systemPrompt)
      return {
        text,
        mode: AIMode.ONDEVICE,
        modelName: 'Gemma 3n E2B',
      }
    }

    // Priority 3: Nothing available
    return {
      text: '[SYSTEM OFFLINE]\n\nOptions:\n1. Connect WiFi with laptop running Ollama\n2. Download on-device model in Settings',
      mode: AIMode.OFFLINE,
      modelName: 'none',
    }
  }
}

function buildOfflinePrompt(p: any, mode: string): string {
  const base = `You are THE SYSTEM bound to ${p.name || 'the user'}.
Goals: ${p.primaryGoal || 'growth and financial stability'}.
Health: ${p.healthConditions || 'none noted'}.`

  const rules = {
    MENTOR:  'Speak like a wise senior. Ask before advising. Warm but honest.',
    COACH:   'No warmth. Pure performance. End with one specific action.',
    REFLECT: 'Ask one question only. Help them think. Under 40 words.',
    SUPPORT: 'Hear them first. Acknowledge fully. Then one direction.',
    SYSTEM:  'Truth first. Name flaws before support. Under 100 words.',
  }

  return `${base}\nMode: ${mode}\n${rules[mode] || rules.SYSTEM}`
}
```

### Update `src/screens/Chat.tsx`

```typescript
// Replace SystemAPI.chat with AIRouter.chat
import { AIRouter, AIMode } from '../ai/AIRouter'

// Mode indicator in header — 3 states:
const modeIndicator = {
  [AIMode.LAPTOP]:   { color: theme.colors.accent, label: 'LAPTOP' },
  [AIMode.ONDEVICE]: { color: theme.colors.info,   label: 'ON-DEVICE' },
  [AIMode.OFFLINE]:  { color: theme.colors.danger, label: 'OFFLINE' },
}

// Header shows:
// "> THE SYSTEM — MENTOR | ON-DEVICE ●"
// Two pieces: companion mode + AI source
```

### Update `App.tsx` — Add Download Gate

```typescript
// On app start, after setup check:
const modelDownloaded = await OnDeviceAI.isDownloaded()
const modelSkipped = await OnDeviceAI.isSkipped()

if (!modelDownloaded && !modelSkipped) {
  // Show ModelDownload screen
  return (
    <ModelDownload
      onComplete={() => setShowMain(true)}
      onSkip={() => {
        OnDeviceAI.skip()
        setShowMain(true)
      }}
    />
  )
}
// Show main app
```

### Add to Settings Screen — Model Management Section

```
ON-DEVICE AI

Status: Downloaded (1.3GB) / Not downloaded / Skipped

If downloaded:
  "Gemma 3n E2B — Active"
  Model version: {version}
  [ DELETE MODEL ] (frees 1.3GB)

If not downloaded:
  [ DOWNLOAD NOW ] (requires WiFi, ~1.3GB)

If skipped:
  [ DOWNLOAD NOW ]
  Note: On-device AI works without laptop or internet
```

---

## Checklist for Phase 15

```
[ ] npm install react-native-executorch
[ ] OnDeviceAI.ts — load, initialize, chat, isReady
[ ] ModelDownload.tsx — progress UI + skip option
[ ] AIRouter.ts — 3-priority routing (laptop → ondevice → offline)
[ ] Update Chat.tsx to use AIRouter instead of SystemAPI.chat
[ ] Header shows both companion mode + AI source
[ ] App.tsx download gate on first launch
[ ] Settings model management section
[ ] AsyncStorage flags: downloaded, skipped
[ ] Test: disconnect from WiFi → verify on-device responds
[ ] Test: reconnect to WiFi → verify laptop Ollama takes over
[ ] Offline system prompt uses profile data from AsyncStorage
[ ] Companion mode works in offline prompt too (all 5 modes)
```

---
# PHASE ORDER UPDATED:
# Phase 0  → First Time Setup + Sidebar + Model Selector
# Phase 12 → Fix Missions + UI
# Phase 4  → Daily Check-In
# Phase 5  → Gamification
# Phase 6  → Health (AS-aware)
# Phase 14 → Companion AI (Mentor/Coach/Reflect/Support)  ← NEW
# Phase 15 → On-Device AI (Gemma 3n, Nord CE 3 Lite)     ← NEW
# Phase 8  → Learning Engine
# Phase 3  → Financial Hub
# Phase 2  → Remove Hardcoded Content
# Phase 7  → Work Productivity
# Phase 10 → Multi-AI Orchestration
# Phase 9  → Google Sheets
# Phase 11 → Full OS Dashboard
# Phase 13 → Future Features
