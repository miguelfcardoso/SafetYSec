package pt.isec.a2022143267.safetysec.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pt.isec.a2022143267.safetysec.model.Rule
import pt.isec.a2022143267.safetysec.model.TimeWindow

/**
 * Repository for Rule operations with Firestore
 */
class RuleRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Create a new rule
     */
    suspend fun createRule(rule: Rule): Result<String> {
        return try {
            val docRef = firestore.collection("rules").document()
            val ruleWithId = rule.copy(id = docRef.id)
            docRef.set(ruleWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing rule
     */
    suspend fun updateRule(rule: Rule): Result<Unit> {
        return try {
            firestore.collection("rules")
                .document(rule.id)
                .set(rule)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a rule
     */
    suspend fun deleteRule(ruleId: String): Result<Unit> {
        return try {
            firestore.collection("rules")
                .document(ruleId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggle rule enabled/disabled
     */
    suspend fun toggleRule(ruleId: String, enabled: Boolean): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "isEnabled" to enabled,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            firestore.collection("rules")
                .document(ruleId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get rules for a protected user (real-time)
     */
    fun getRulesForProtected(protectedId: String): Flow<List<Rule>> = callbackFlow {
        val listener = firestore.collection("rules")
            .whereEqualTo("protectedId", protectedId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val rules = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Rule::class.java)
                }?.toList() ?: emptyList()  // Force new list

                android.util.Log.d("RuleRepository", "Rules updated: ${rules.size} total, ${rules.count { it.isEnabled }} enabled")
                rules.forEach {
                    android.util.Log.d("RuleRepository", "  - ${it.name}: enabled=${it.isEnabled}")
                }

                trySend(rules)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get rules created by a monitor (real-time)
     */
    fun getRulesForMonitor(monitorId: String): Flow<List<Rule>> = callbackFlow {
        val listener = firestore.collection("rules")
            .whereEqualTo("monitorId", monitorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val rules = snapshot?.documents?.mapNotNull {
                    it.toObject(Rule::class.java)
                } ?: emptyList()

                trySend(rules)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Create a time window
     */
    suspend fun createTimeWindow(timeWindow: TimeWindow): Result<String> {
        return try {
            val docRef = firestore.collection("time_windows").document()
            val windowWithId = timeWindow.copy(id = docRef.id)
            docRef.set(windowWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get time windows for a protected user
     */
    fun getTimeWindows(protectedId: String): Flow<List<TimeWindow>> = callbackFlow {
        val listener = firestore.collection("time_windows")
            .whereEqualTo("protectedId", protectedId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val windows = snapshot?.documents?.mapNotNull {
                    it.toObject(TimeWindow::class.java)
                } ?: emptyList()

                trySend(windows)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Delete a time window
     */
    suspend fun deleteTimeWindow(windowId: String): Result<Unit> {
        return try {
            firestore.collection("time_windows")
                .document(windowId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

