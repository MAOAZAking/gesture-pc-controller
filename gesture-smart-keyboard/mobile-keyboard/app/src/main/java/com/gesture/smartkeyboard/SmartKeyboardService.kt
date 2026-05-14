package com.gesture.smartkeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.LinearLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SmartKeyboardService : InputMethodService() {

    private lateinit var suggestionBar: TextView
    private var lastWord = ""
    private var currentLanguage = "es" // Puede cambiarse a "en"
    private val database = FirebaseDatabase.getInstance().getReference("ngrams")

    override fun onCreateInputView(): View {
        // Inflamos el diseño que creamos en XML
        val layout = layoutInflater.inflate(R.layout.keyboard_view, null) as LinearLayout
        suggestionBar = layout.findViewById(R.id.suggestion_bar)
        
        // Listener para cambiar de idioma si se toca la barra (opcional)
        suggestionBar.setOnClickListener {
            currentLanguage = if (currentLanguage == "es") "en" else "es"
            suggestionBar.text = "Idioma: ${currentLanguage.uppercase()}"
        }
        
        return layout
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        lastWord = ""
        suggestionBar.text = "Esperando entrada..."
    }

    // Esta función se llama cada vez que el usuario termina una palabra
    private fun fetchPredictions(word: String) {
        val cleanWord = word.trim().lowercase()
        if (cleanWord.isEmpty()) return

        // Consultamos Firebase: ngrams -> idioma -> palabra_actual
        database.child(currentLanguage).child(cleanWord)
            .orderByValue() // Ordenar por frecuencia
            .limitToLast(3) // Traer las 3 más usadas
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val suggestions = mutableListOf<String>()
                    for (child in snapshot.children) {
                        child.key?.let { suggestions.add(it) }
                    }
                    
                    // Invertimos para que la más frecuente salga primero
                    suggestions.reverse()
                    
                    // Actualizamos la UI del teclado
                    if (suggestions.isNotEmpty()) {
                        suggestionBar.text = suggestions.joinToString("   |   ")
                    } else {
                        suggestionBar.text = "..."
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    suggestionBar.text = "Error de conexión"
                }
            })
    }

    // Función que el programador usará para insertar texto desde los gestos
    fun onGestureWordDetected(detectedWord: String) {
        val ic = currentInputConnection ?: return
        
        // 1. Insertar la palabra detectada
        ic.commitText("$detectedWord ", 1)
        
        // 2. Buscar qué palabra sigue según la base de datos
        fetchPredictions(detectedWord)
        
        // 3. Guardar como última palabra para el contexto
        lastWord = detectedWord
    }
}