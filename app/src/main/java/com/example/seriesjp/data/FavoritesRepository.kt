package com.example.seriesjp.data

import android.util.Log
import com.example.seriesjp.model.Favoritos
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class FavoritesRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
    }

    fun addFavorite(userId: String, favorito: Favoritos, onComplete: (Boolean) -> Unit) {
        firestore.collection("usuarios")
            .document(userId)
            .collection("favoritos")
            .document(favorito.id)
            .set(favorito)
            .addOnSuccessListener {
                Log.d("Repo", "Favorito agregado: ${favorito.titulo}")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("Repo", "Error agregando favorito", e)
                onComplete(false)
            }
    }

    fun getFavorites(userId: String, onResult: (List<Favoritos>) -> Unit) {
        firestore.collection("usuarios")
            .document(userId)
            .collection("favoritos")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.map { it.toObject(Favoritos::class.java) }
                onResult(list)
            }
            .addOnFailureListener { e ->
                Log.e("Repo", "Error obteniendo favoritos", e)
                onResult(emptyList())
            }
    }
}
