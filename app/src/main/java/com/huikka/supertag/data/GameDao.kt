package com.huikka.supertag.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.huikka.supertag.data.model.Game
import com.huikka.supertag.data.model.Player
import kotlinx.coroutines.tasks.await

class GameDao {

    private var db = Firebase.firestore
    private fun getGameFromDocument(doc: DocumentSnapshot): Game? {
        return doc.toObject(Game::class.java)
    }

    private suspend fun getGameDocumentById(id: String): DocumentSnapshot? {
        val gameRef = db.collection("games").whereEqualTo("id", id).get().await()
        if (gameRef.isEmpty) {
            return null
        }
        return gameRef.documents[0]
    }

    suspend fun checkGameExists(id: String): Boolean {
        return getGameDocumentById(id) != null
    }

    suspend fun removeChaser(id: String, gameId: String): Error? {
        val doc = getGameDocumentById(gameId) ?: return Error("Game not found")
        val game = getGameFromDocument(doc)
        val chasers = game?.chasers
        chasers?.forEach {
            if (it.id == id) {
                doc.reference.update(
                    "chasers",
                    FieldValue.arrayRemove(
                        it
                    )
                )
            }
        }
        return null
    }

    suspend fun addChaser(player: Player, gameId: String): Error? {
        val doc = getGameDocumentById(gameId) ?: return Error("Game not found")
        doc.reference.update(
            "chasers",
            FieldValue.arrayUnion(
                player
            )
        ).await()
        return null
    }

    suspend fun createGame(game: Game): Error? {
        var err: Error? = null
        db.collection("games").add(game).addOnFailureListener {
            err = Error(it)
        }.await()
        return err
    }
}