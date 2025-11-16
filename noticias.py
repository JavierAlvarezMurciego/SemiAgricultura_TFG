# Descarga el contenido HTML de la página de noticias
import json

import requests
# Parsea el HTML y extrae titulares, enlaces, resúmenes
from bs4 import BeautifulSoup
# Normaliza los textos y eliminar acentos
import unicodedata
#Usa expresiones regulares si se necesita filtrar o limpiar texto
import firebase_admin
#Conecto con Firebase y guardo las noticias
from firebase_admin import credentials, firestore, storage
# Módulo estándar de Python que permite interactuar con el sistema de archivos y el sistema operativo
# En este script se usa para eliminar archivos temporales descargados (os.remove) y manejar rutas
import os
# Importa la clase 'bucket' de la librería de Google Cloud Storage
# Permite crear y manejar objetos dentro de Firebase Storage (subir archivos, asignar rutas, hacer públicos, etc.)
#from google.cloud.storage import bucket

# -----------------------------
# CONFIGURACIÓN FIREBASE
# -----------------------------
# Leer la variable de entorno con el JSON
'''firebase_json = os.getenv("FIREBASE_CREDENCIALES_JSON")
if not firebase_json:
    raise ValueError("La variable de entorno FIREBASE_CREDENTIALS_JSON no está definida")'''

ruta_json = os.path.join('credenciales',"firebase_clave.json")
# Abrir el archivo y cargar su contenido
with open(ruta_json, "r") as f:
    cred = json.load(f)  # json.load, no json.loads

# Convertir el string JSON a dict
#cred_dict = json.loads(firebase_json)
# Carga mi archivo de servicio de Firebase para autenticar
#cred_dict = json.loads(firebase_json)
cred = credentials.Certificate(ruta_json)
# Inicializa la conexión con Firebase
firebase_admin.initialize_app(cred,{
    "storageBucket": "SemiAgricultura.appspot.com"
})
# Obtengo el cliente de Firestore
db = firestore.client()
bucket = storage.bucket()
# Referencia a la colección "noticias" donde guardo las noticias
noticias_ref = db.collection("noticias")

# -----------------------------
# CONFIGURACIÓN SCRAPING
# -----------------------------

# Página etoy scrapeando
URL = "https://efeagro.com/actualidad/"
# Palabras clave para filtrar
PALABRAS_CLAVE = ["sequía", "precios", "clima", "ayudas", "cosecha", "agua", "lluvia",
            "tiempo", "subida", "bajada", "subvenciones", "subvención", "cereal","remolacha","alubias"
            ,"inversiones","cosechadora","cebada","trigo","alubia"]
# Límite de noticias que mantengo
MAX_NOTICIAS = 5

# -----------------------------
# FUNCIONES AUXILIARES
# -----------------------------
def limpiar_texto(texto):
    '''Normalizo el texto eliminando acentos y caracteres especiales y los convierto a minúsculas'''
    texto_limpio = unicodedata.normalize("NFKD", texto).encode("ASCII", "ignore").decode()
    return texto_limpio.lower()

def es_relevante(texto):
    '''
    Compruebo si el texto contiene alguna de las palabras clave definidas
    Devuelvo True si la noticia es relevante, False si no
    '''
    texto_limpio = limpiar_texto(texto)
    return any(k.lower() in texto_limpio for k in PALABRAS_CLAVE)

def subir_imagen_storage(url, nombre_archivo):
    """Descarga imagen de url y la sube a Firebase Storage"""
    if not url:
        return ""
    response = requests.get(url, stream=True)
    if response.status_code == 200:
        temp_path = f"/tmp/{nombre_archivo}"
        with open(temp_path, "wb") as f:
            for chunk in response.iter_content(1024):
                f.write(chunk)
        # Creo un "objeto blob" en Firebase Storage dentro de la carpeta 'noticias' con el nombre de archivo definido
        blob = bucket.blob(f"noticias/{nombre_archivo}")
        # Subo el archivo temporal que descargamos desde la web a Firebase Storage
        blob.upload_from_filename(temp_path)
        # Hago que la imagen sea pública para poder usar su URL directamente en la app
        blob.make_public()  # opcional, pero útil para mostrar la imagen sin autenticación
        # Elimino el archivo temporal local para no llenar el disco con descargas
        os.remove(temp_path)
        return blob.public_url
    return ""

# -----------------------------
# SCRAPING DE NOTICIAS
# -----------------------------

# Descargo la página de noticias
response = requests.get(URL)
# Convierto en un objeto BeautifulSoup para poder seleccionar elementos del HTML
soup = BeautifulSoup(response.content, "html.parser")

noticias = []

# Ajusto el selector según la estructura real de EFEAGRO
for item in soup.select("article"):  # Muchos artículos están dentro de <article>
    titulo_tag = item.find("h2")
    resumen_tag = item.find("p")
    link_tag = item.find("a")
    fecha_tag = item.find("span")
    imagen_tag = item.find("img")

    if titulo_tag and link_tag:
        titulo = titulo_tag.text.strip()
        resumen = resumen_tag.text.strip() if resumen_tag else ""
        link = link_tag["href"]
        publicacion = fecha_tag.text.strip() if fecha_tag else ""
        imagen_url = imagen_tag["data-src"] if imagen_tag else ""

        # Filtrar por keywords
        if es_relevante(titulo + " " + resumen):
            noticias.append({
                "titulo": titulo,
                "resumen": resumen,
                "link": link,
                "publicacion": publicacion,
                "imagen_url": imagen_url
            })

# Ordeno por fecha (más recientes primero) y selecciono las 5 mejores noticias
noticias.sort(key=lambda d: d.get("publicacion",""), reverse=True)
top_noticias = noticias[:5]

# -----------------------------
# SUBIR A FIREBASE
# -----------------------------
# Mantener histórico limitado: borro las que exceden el límite
docs = list(noticias_ref.stream())
if len(docs) >= MAX_NOTICIAS:
    # Ordeno por fecha y borro las más antiguas
    docs.sort(key=lambda d: d.get("publicacion", ""), reverse=True)
    for doc in docs[MAX_NOTICIAS - len(top_noticias):]:
        doc.reference.delete()

# Subir nuevas noticias
for i, noticia in enumerate(top_noticias):
    noticias_ref.document(f"noticia_{i + 1}").set(noticia)

print(f"{len(top_noticias)} noticias actualizadas en Firebase correctamente!")


