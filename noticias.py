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
firebase_json = os.getenv("FIREBASE_CREDENCIALES_JSON")
if not firebase_json:
    raise ValueError("La variable de entorno FIREBASE_CREDENTIALS_JSON no está definida")

# Convertir el string JSON a dict
cred_dict = json.loads(firebase_json)

# Inicializar Firebase con el dict
cred = credentials.Certificate(cred_dict)
firebase_admin.initialize_app(cred)
db = firestore.client()

# Referencia a la colección "noticias" donde guardo las noticias
noticias_ref = db.collection("noticias")

# -----------------------------
# CONFIGURACIÓN SCRAPING
# -----------------------------

# Página etoy scrapeando
URL = "https://efeagro.com/actualidad/"
# Palabras clave para filtrar
PALABRAS_CLAVE = [    # Clima y agua
    "sequía", "lluvia", "agua", "clima", "tiempo",
    # Precios y mercado
    "precios", "subida", "bajada", "mercado", "exportación", "importación",
    # Cosecha y cultivos
    "cosecha", "trigo", "cebada", "maíz", "remolacha", "alubias", "alubia", "cereal", "hortalizas", "legumbres",
    # Ayudas y subvenciones
    "ayudas", "subvención", "subvenciones", "inversiones", "apoyo",
    # Maquinaria y gestión agrícola
    "cosechadora", "tractor", "riego", "fertilizante", "semillas", "tecnología",
    # Problemas y alertas del sector
    "plaga", "enfermedad", "alerta", "restricción", "protesta"]

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
        publication = fecha_tag.text.strip() if fecha_tag else ""
        imagen_url = imagen_tag["data-src"] if imagen_tag else ""

        # Agrego todas las noticias sin filtrar
        noticias.append({
            # Lo que tengo entre comillas son los campos que están en la base de datos
            "titulo": titulo,
            "resumen": resumen,
            "link": link,
            "publication": publication,
            "imagen_url": imagen_url
        })

# Ordeno por fecha (más recientes primero) y selecciono las 5 mejores noticias
noticias.sort(key=lambda d: d.get("publication",""), reverse=True)

# Divido las noticias en relevantes y no relevantes
noticias_relevantes = [n for n in noticias if es_relevante(n["titulo"] + " " + n["resumen"])]
noticias_no_relevantes = [n for n in noticias if not es_relevante(n["titulo"] + " " + n["resumen"])]

# Cojo las MAX_NOTICIAS y las relleno con noticias no relevantes en caso de no completar con las relevantes
top_noticias = noticias_relevantes[:MAX_NOTICIAS]
faltantes = MAX_NOTICIAS - len(top_noticias)
if faltantes > 0:
    top_noticias += noticias_no_relevantes[:faltantes]

# -----------------------------
# SUBIR A FIREBASE
# -----------------------------
# Mantener histórico limitado: borro las que exceden el límite
docs = list(noticias_ref.stream())
if len(docs) >= MAX_NOTICIAS:
    # Ordeno por fecha y borro las más antiguas
    docs.sort(key=lambda d: d.to_dict().get("publication", ""), reverse=True)
    for doc in docs[MAX_NOTICIAS - len(top_noticias):]:
        doc.reference.delete()

# Subir nuevas noticias
for i, noticia in enumerate(top_noticias):
    noticias_ref.document(f"noticia_{i + 1}").set(noticia)

# Eliminación de documentos antiguos que excedan MAX_NOTICIAS
docs = list(noticias_ref.stream())  # refresco la lista después de subirla
if len(docs) > MAX_NOTICIAS:
    # Ordeno por fecha y borro los más antiguos
    docs.sort(key=lambda d: d.to_dict().get("publication", ""), reverse=True)
    for doc in docs[MAX_NOTICIAS:]:
        doc.reference.delete()

print(f"{len(top_noticias)} noticias actualizadas en Firebase correctamente!")


