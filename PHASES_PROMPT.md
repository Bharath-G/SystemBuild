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