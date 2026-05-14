############################################################
############################NOTE############################
############################################################

# Para ejecutar descargar las librerias necesarias con este comando
# pip install pynput
# pip install firebase-admin pynput


# To run, download the necessary libraries with this command
# pip install pynput
# pip install firebase-admin pynput

import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
from pynput import keyboard
import re

# 1. CONFIGURACIÓN DE FIREBASE
cred = credentials.Certificate("serviceAccountKey.json") # El archivo que descargaste
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://gesturekeyboard-pccontroller-default-rtdb.firebaseio.com/'
})

buffer_teclas = []
ultima_palabra = ""
idioma_actual = "es"

def limpiar_palabra(palabra):
    return re.sub(r'[^a-zA-ZáéíóúÁÉÍÓÚñÑ]', '', palabra).lower()

def guardar_en_nube(word1, word2, lang):
    if not word1 or not word2: return
    
    # Referencia en la base de datos: ngrams/es/palabra1/palabra2
    ref = db.reference(f'ngrams/{lang}/{word1}/{word2}')
    
    # Leemos la frecuencia actual y sumamos 1
    frecuencia = ref.get()
    if frecuencia is None:
        ref.set(1)
    else:
        ref.set(frecuencia + 1)
    print(f"Subido a la nube: {word1} -> {word2}")

def al_presionar(tecla):
    global buffer_teclas, ultima_palabra
    try:
        if tecla == keyboard.Key.space or tecla == keyboard.Key.enter:
            palabra_actual = limpiar_palabra("".join(buffer_teclas))
            if palabra_actual:
                guardar_en_nube(ultima_palabra, palabra_actual, idioma_actual)
                ultima_palabra = palabra_actual
            buffer_teclas.clear()
        elif tecla == keyboard.Key.backspace:
            if buffer_teclas: buffer_teclas.pop()
        elif hasattr(tecla, 'char') and tecla.char is not None:
            buffer_teclas.append(tecla.char)
    except: pass

print("Rastreador conectado a Firebase. Escribe algo...")
with keyboard.Listener(on_press=al_presionar) as listener:
    listener.join()