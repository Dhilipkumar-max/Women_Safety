# Architectural Blueprint: Groq AI Integration for SafeHer

This document outlines the strategic integration of Groq's LPU™ (Language Processing Unit) technology into the SafeHer ecosystem to solve critical latency bottlenecks in emergency response.

## 1. Identified Bottlenecks & AI Solutions

| Component | Current Bottleneck | Groq AI Solution | Impact |
| :--- | :--- | :--- | :--- |
| **Threat Assessment** | Static "if/else" logic in `calculateInteractiveThreatScore` | **Neural Emergency Orchestration (NEO)**: Real-time contextual analysis of sensor data. | Eliminates false positives from exercise; detects subtle danger patterns. |
| **Voice Triggers** | Simple keyword matching in `VoiceTriggerManager` | **Contextual Acoustic Intelligence**: Analyzing ambient audio transcripts for intent/stress. | Triggers SOS even without exact "Help me" phrase (e.g., "Stop it", "Let go"). |
| **Emergency Reporting** | Basic log entries | **Automated Incident Synthesis**: Generating detailed, professional reports for law enforcement. | Faster response times with high-fidelity information. |

---

## 2. Proposed Architecture

We will implement a **Groq Gateway** service using the `Llama-3-70b` model for complex reasoning and `Llama-3-8b` for ultra-low latency real-time monitoring.

### File Placement Strategy
- `app/src/main/java/com/example/service/GroqAiService.kt`: Core API client for Groq.
- `app/src/main/java/com/example/data/AiModels.kt`: Data classes for AI requests/responses.
- `app/src/main/java/com/example/viewmodel/SafetyViewModel.kt`: Integration point for NEO logic.

---

## 3. Implementation: Neural Threat Engine

This implementation replaces the static 75% threshold with a dynamic, multi-modal assessment.

### [NEW] GroqAiService.kt
```kotlin
class GroqAiService(private val apiKey: String) {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    suspend fun analyzeThreat(
        heartRate: Int,
        motion: String,
        audioSnippet: String
    ): ThreatAnalysis {
        val prompt = """
            Analyze for danger (SafeHer App):
            - Heart Rate: $heartRate bpm
            - Motion: $motion
            - Ambient Audio Context: "$audioSnippet"

            Return JSON: { "threatScore": 0-100, "reason": "...", "action": "ALERT|MONITOR|IGNORE" }
        """.trimIndent()

        // Groq API Call logic here...
    }
}
```

---

## 4. Production-Ready Deployment Plan

1.  **Phase 1: Shadow Mode**: Deploy Groq analysis in parallel with existing logic to log "AI vs Heuristic" accuracy without changing UI behavior.
2.  **Phase 2: Active Triggering**: Shift the primary emergency trigger to the Groq NEO engine.
3.  **Phase 3: Edge Pre-processing**: Implement local NLP filtering to only call Groq when significant stress or keywords are detected, optimizing token usage.
