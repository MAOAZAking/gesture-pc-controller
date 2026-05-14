import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo

class SmartKeyboardService : InputMethodService() {

    private var currentWord = StringBuilder()
    private var lastWord = ""
    private var currentLanguage = "es" // Toggle para es/en

    override fun onCreateInputView(): View {
        // Aquí inflarás el diseño XML de tu teclado (los botones)
        // val keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null)
        // return keyboardView
        return super.onCreateInputView()
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        // Se llama cuando el usuario toca un campo de texto
        currentWord.clear()
        lastWord = ""
    }

    // Método simulado que se llamaría cuando el usuario presiona una tecla en tu UI
    fun handleKeyPress(primaryCode: Int) {
        val inputConnection = currentInputConnection ?: return

        when (primaryCode) {
            KeyEvent.KEYCODE_DEL -> {
                // Borrar
                if (currentWord.isNotEmpty()) {
                    currentWord.deleteCharAt(currentWord.length - 1)
                    inputConnection.deleteSurroundingText(1, 0)
                }
            }
            KeyEvent.KEYCODE_SPACE -> {
                // Espacio presionado: Procesar la palabra
                inputConnection.commitText(" ", 1)
                processWord(currentWord.toString())
            }
            else -> {
                // Escribir letra
                val char = primaryCode.toChar()
                currentWord.append(char)
                inputConnection.commitText(char.toString(), 1)
            }
        }
    }

    private fun processWord(word: String) {
        val cleanWord = word.trim().lowercase()
        if (cleanWord.isNotEmpty()) {
            if (lastWord.isNotEmpty()) {
                // AQUÍ: Guardar la relación lastWord -> cleanWord
                // Ejemplo: saveToLocalDatabase(lastWord, cleanWord, currentLanguage)
            }
            lastWord = cleanWord
            currentWord.clear()
        }
    }
}