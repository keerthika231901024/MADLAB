package com.yours.engine

import com.yours.data.db.PersonalDataEntity
import com.yours.data.repository.DataRepository

enum class ChatAction { NONE, STORED, RETRIEVED, DELETED, REFUSED }

data class ChatResult(
    val response: String,
    val action: ChatAction = ChatAction.NONE
)

class ChatEngine(private val repository: DataRepository) {

    // ── Greetings ─────────────────────────────────────────────────────────────
    private val greetingWords = setOf(
        "hi", "hello", "hey", "yo", "sup", "howdy",
        "good morning", "good evening", "good afternoon", "good night",
        "namaste", "vanakkam", "hii", "helo", "hai", "greetings"
    )

    private val greetingReplies = listOf(
        "Hey there! 👋 I'm Yours — your personal memory keeper.\nTell me anything you want to remember and I'll keep it safe!",
        "Hello! 😊 Great to see you!\nShare something with me and I'll remember it for you.",
        "Hi! I'm Yours. 🌟\nYou can tell me things like:\n\"My Aadhaar is 1234 5678 9012\"\n\"Ravi's birthday is March 15\"",
        "Hey! 😄 I'm here and ready to remember things for you.\nWhat would you like to tell me?"
    )

    // ── Store patterns (key is group 1, value is group 2) ────────────────────
    private val storePatterns = listOf(
        Regex("""my ([\w\s'.,-]+?) is ([\w\s'.,:@#/-]+)""", RegexOption.IGNORE_CASE),
        Regex("""my ([\w\s'.,-]+?) are ([\w\s'.,:@#/-]+)""", RegexOption.IGNORE_CASE),
        Regex("""remember (?:that )?my ([\w\s'.,-]+?) is ([\w\s'.,:@#/-]+)""", RegexOption.IGNORE_CASE),
        Regex("""save (?:my )?([\w\s'.,-]+?) as ([\w\s'.,:@#/-]+)""", RegexOption.IGNORE_CASE),
        Regex("""store (?:my )?([\w\s'.,-]+?) as ([\w\s'.,:@#/-]+)""", RegexOption.IGNORE_CASE),
        Regex("""note (?:that )?(?:my )?([\w\s'.,-]+?) is ([\w\s'.,:@#/-]+)""", RegexOption.IGNORE_CASE),
        Regex("""([\w\s'.,-]+?)'s (birthday|number|address|email|phone) is ([\w\s'.,:@#/-]+)""", RegexOption.IGNORE_CASE)
    )

    // ── Query patterns (key is group 1) ──────────────────────────────────────
    private val queryPatterns = listOf(
        Regex("""what(?:'s| is) my ([\w\s'.,-]+?)\??$""", RegexOption.IGNORE_CASE),
        Regex("""(?:tell|show|give) me (?:my )?([\w\s'.,-]+?)\??$""", RegexOption.IGNORE_CASE),
        Regex("""remind me (?:of |about )?(?:my )?([\w\s'.,-]+?)\??$""", RegexOption.IGNORE_CASE),
        Regex("""do you (?:know|remember|have) (?:my )?([\w\s'.,-]+?)\??$""", RegexOption.IGNORE_CASE),
        Regex("""what(?:'s| is) ([\w\s'.,-]+?)'s (birthday|number|address|email|phone)\??""", RegexOption.IGNORE_CASE)
    )

    // ── Delete patterns ───────────────────────────────────────────────────────
    private val deletePatterns = listOf(
        Regex("""(?:forget|delete|remove) (?:my )?([\w\s'.,-]+)""", RegexOption.IGNORE_CASE),
        Regex("""(?:clear|erase) (?:my )?([\w\s'.,-]+)""", RegexOption.IGNORE_CASE)
    )

    // ── List-all keywords ─────────────────────────────────────────────────────
    private val listAllPhrases = listOf(
        "what did i save", "what do you know", "show all", "list all",
        "what have i told", "show everything", "what do you remember",
        "show my data", "all my data", "everything i saved", "what i saved"
    )

    // ── General knowledge patterns ─────────────────────────────────────────────
    // Uses (?!my) to NOT refuse "what is MY ..."
    private val generalKnowledgePatterns = listOf(
        Regex("""^who is (?!my)([\w\s]+)""", RegexOption.IGNORE_CASE),
        Regex("""^who was ([\w\s]+)""", RegexOption.IGNORE_CASE),
        Regex("""^what is the (?!my)([\w\s]+)""", RegexOption.IGNORE_CASE),
        Regex("""^what are the ([\w\s]+)""", RegexOption.IGNORE_CASE),
        Regex("""^where is (?!my)([\w\s]+)""", RegexOption.IGNORE_CASE),
        Regex("""^when (?:did|was) ([\w\s]+)""", RegexOption.IGNORE_CASE),
        Regex("""^how (?:many|much|do|does|did) (?!i|my)([\w\s]+)""", RegexOption.IGNORE_CASE),
        Regex("""capital of ([\w\s]+)""", RegexOption.IGNORE_CASE),
        Regex("""(?:cm|chief minister|prime minister|president|governor) of ([\w\s]+)""", RegexOption.IGNORE_CASE),
        Regex("""^(?:weather|temperature|forecast)\b""", RegexOption.IGNORE_CASE),
        Regex("""^(?:news|cricket|football|score)\b""", RegexOption.IGNORE_CASE),
        Regex("""population of ([\w\s]+)""", RegexOption.IGNORE_CASE),
        Regex("""^which is the ([\w\s]+)""", RegexOption.IGNORE_CASE)
    )

    private val refusalReplies = listOf(
        "I only know what YOU tell me! 😄\nI can't answer general questions — I'm your personal memory keeper, not a search engine.",
        "That's beyond my knowledge! 🤷\nI only store and recall YOUR personal info. Ask me something you've saved!",
        "I'm not Google! 😄\nI only know what you've shared with me. Try telling me something personal to remember.",
        "Hmm, I can't help with that. 🙅\nI'm designed to remember only YOUR personal information — not public knowledge."
    )

    // ── Main process function ─────────────────────────────────────────────────
    suspend fun process(input: String): ChatResult {
        val trimmed = input.trim()
        val lower = trimmed.lowercase()

        // 1. Greeting
        if (isGreeting(lower)) {
            return ChatResult(greetingReplies.random())
        }

        // 2. List all
        if (listAllPhrases.any { lower.contains(it) }) {
            return handleListAll()
        }

        // 3. Delete
        val deleteKey = extractDeleteKey(lower)
        if (deleteKey != null) {
            return handleDelete(deleteKey)
        }

        // 4. Store
        val storeData = extractStoreData(trimmed)
        if (storeData != null) {
            return handleStore(storeData.first, storeData.second, storeData.first)
        }

        // 5. General knowledge refusal (before query)
        if (isGeneralKnowledge(lower)) {
            return ChatResult(refusalReplies.random(), ChatAction.REFUSED)
        }

        // 6. Query
        val queryKey = extractQueryKey(lower)
        val isQuestion = lower.contains("?") || lower.startsWith("what") ||
            lower.startsWith("tell") || lower.startsWith("remind") ||
            lower.startsWith("show") || lower.startsWith("give") ||
            lower.startsWith("do you")

        if (isQuestion) {
            return handleQuery(queryKey ?: lower)
        }

        // 7. Fuzzy search entire input
        val results = repository.search(lower)
        if (results.isNotEmpty()) {
            return ChatResult(formatResults(results), ChatAction.RETRIEVED)
        }

        // 8. Fallback
        return ChatResult(
            "I'm not sure I understood that. 🤔\n\n" +
            "Try saying:\n" +
            "• \"My phone number is 9876543210\"\n" +
            "• \"What is my phone number?\"\n" +
            "• \"Show all my data\"\n" +
            "• \"Forget my phone number\"",
            ChatAction.NONE
        )
    }

    // ── Helper: Greeting ──────────────────────────────────────────────────────
    private fun isGreeting(input: String): Boolean {
        val cleaned = input.trimEnd('!', '.', ' ')
        return greetingWords.any { cleaned == it || cleaned.startsWith("$it ") }
    }

    // ── Helper: Store extraction ──────────────────────────────────────────────
    private fun extractStoreData(input: String): Pair<String, String>? {
        for (pattern in storePatterns) {
            val match = pattern.find(input) ?: continue
            val groups = match.groupValues
            return when (groups.size) {
                4 -> { // Friend's <type> is <value>
                    val key = "${groups[1].trim()}'s ${groups[2].trim()}"
                    val value = groups[3].trim()
                    if (key.isNotEmpty() && value.isNotEmpty()) Pair(key, value) else null
                }
                3 -> {
                    val key = groups[1].trim()
                    val value = groups[2].trim()
                    if (key.isNotEmpty() && value.isNotEmpty() && key.length < 60) Pair(key, value) else null
                }
                else -> null
            }
        }
        return null
    }

    // ── Helper: Query extraction ──────────────────────────────────────────────
    private fun extractQueryKey(input: String): String? {
        for (pattern in queryPatterns) {
            val match = pattern.find(input) ?: continue
            val groups = match.groupValues
            return if (groups.size >= 3 && groups[2].isNotEmpty()) {
                "${groups[1].trim()}'s ${groups[2].trim()}"
            } else {
                groups[1].trim().removeSuffix("?")
            }
        }
        return null
    }

    // ── Helper: Delete extraction ─────────────────────────────────────────────
    private fun extractDeleteKey(input: String): String? {
        for (pattern in deletePatterns) {
            val match = pattern.find(input) ?: continue
            val key = match.groupValues[1].trim()
            if (key.isNotEmpty()) return key
        }
        return null
    }

    // ── Helper: General knowledge detection ──────────────────────────────────
    private fun isGeneralKnowledge(input: String): Boolean {
        return generalKnowledgePatterns.any { it.containsMatchIn(input) }
    }

    // ── Handlers ──────────────────────────────────────────────────────────────
    private suspend fun handleStore(key: String, value: String, rawKey: String): ChatResult {
        repository.saveData(key, value, rawKey)
        return ChatResult(
            "Got it! ✅ I've saved:\n📌 ${key.capitalizeFirst()} → $value\n\n🔒 Only you can access this.",
            ChatAction.STORED
        )
    }

    private suspend fun handleQuery(key: String): ChatResult {
        val results = repository.search(key)
        return if (results.isNotEmpty()) {
            ChatResult(formatResults(results), ChatAction.RETRIEVED)
        } else {
            ChatResult(
                "Hmm, I don't have anything saved for \"$key\" yet. 🤔\n\nTell me and I'll remember it!",
                ChatAction.NONE
            )
        }
    }

    private suspend fun handleListAll(): ChatResult {
        val all = repository.getAllDataSync()
        return if (all.isEmpty()) {
            ChatResult(
                "You haven't saved anything yet! 📝\n\nTry:\n\"My Aadhaar is 1234 5678 9012\"\n\"My friend Ravi's birthday is March 15\"",
                ChatAction.NONE
            )
        } else {
            val sb = StringBuilder("Here's everything I remember for you: 📋\n\n")
            all.forEachIndexed { i, item ->
                sb.append("${i + 1}. ${item.rawKey.capitalizeFirst()}: ${item.value}\n")
            }
            ChatResult(sb.trim().toString(), ChatAction.RETRIEVED)
        }
    }

    private suspend fun handleDelete(key: String): ChatResult {
        repository.deleteByTag(key)
        return ChatResult(
            "Done! 🗑️ I've forgotten your $key.",
            ChatAction.DELETED
        )
    }

    private fun formatResults(results: List<PersonalDataEntity>): String {
        return if (results.size == 1) {
            val item = results[0]
            "Here's what I have: 📌\n\n${item.rawKey.capitalizeFirst()}: ${item.value}"
        } else {
            val sb = StringBuilder("I found ${results.size} matching items: 📌\n\n")
            results.forEachIndexed { i, item ->
                sb.append("${i + 1}. ${item.rawKey.capitalizeFirst()}: ${item.value}\n")
            }
            sb.trim().toString()
        }
    }

    private fun String.capitalizeFirst(): String =
        replaceFirstChar { it.uppercase() }
}
