# THE SYSTEM — All Phase Prompts
# Use one phase at a time. Test before next phase.
# Stack: React Native + Expo | Spring Boot Java 25 | PostgreSQL | Ollama (any local model) | ChromaDB

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
## User Email Configuration

The OAuth flow must ask the user for their Google account email before
initiating the OAuth redirect. This email is used only for display
purposes (showing which account is connected) and for pre-filling the
OAuth consent screen hint parameter.
DO NOT hardcode any email addresses or credentials.
All OAuth tokens stored encrypted in PostgreSQL.
### Settings Screen — Email Input Flow

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

GOOGLE SHEETS SYNC
Step 1 — Enter your Google account email:

[ email input field ]

placeholder: "yourname@gmail.com"

validation: must contain @ and .

[ CONNECT THIS ACCOUNT ] button
Step 2 — After email entered:

Call GET /api/google/auth-url?hint={email}

Open returned URL in device browser

User completes Google OAuth consent

Browser redirects back with auth code

App exchanges code for tokens
Step 3 — Connected state shows:

"Connected as: {email}"     ← stored in GoogleConfig.userEmail

[ SYNC THIS WEEK ]

[ SYNC THIS MONTH ]

[ DISCONNECT ]
### Backend — GoogleConfig entity add one field

```java
private String userEmail;    // stored after user enters it in Step 1
                             // never used for auth — display only
```

### Backend — auth URL endpoint accepts hint

```java
@GetMapping("/api/google/auth-url")
public ResponseEntity<Map<String, String>> getAuthUrl(
        @RequestParam(required = false) String hint) {
    String url = googleOAuthService.buildAuthUrl(hint);
    // hint passed as login_hint parameter to Google OAuth
    // pre-fills which account to use on consent screen
    return ResponseEntity.ok(Map.of("url", url));
}
```

### Validation Rules
- Email field is required before OAuth button activates
- Email is saved to GoogleConfig.userEmail on successful OAuth completion
- If user disconnects and reconnects: email field is pre-filled with last used email
- DO NOT use email for any authentication logic — only for display and OAuth hint
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
