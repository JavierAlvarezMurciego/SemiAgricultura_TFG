from datetime import datetime
from pathlib import Path

import requests
import unicodedata
import re
from bs4 import BeautifulSoup
import os
import firebase_admin
from firebase_admin import credentials, firestore

# Ruta al archivo JSON en la carpeta credenciales
ruta_credenciales =  os.path.join('credenciales',"firebase_clave.json")

# Inicializar Firebase
cred = credentials.Certificate(ruta_credenciales)
firebase_admin.initialize_app(cred)
db = firestore.client()

def limpiar_nombre(nombre):
    nombre = unicodedata.normalize('NFKD', nombre).encode('ascii', 'ignore').decode('utf-8')
    nombre = re.sub(r'[^a-zA-Z0-9\s]', '', nombre)
    return nombre.strip()

def parsear_precio(texto):
    texto = texto.replace('€', '').replace(',', '.').strip()
    if texto.lower() in ['s/c', '']:
        return 'S/C'
    try:
        return float(texto)
    except ValueError:
        return 'S/C'

def obtener_url_actual_y_fecha():
    url_base = 'https://www.lonjadeleon.es/category/cotizaciones/cereales/'
    response = requests.get(url_base)
    soup = BeautifulSoup(response.text, 'html.parser')

    enlace = soup.find('a', href=lambda x: x and 'lonja-de-cereal-' in x)
    if not enlace or not enlace.get('href'):
        return None, None

    url = enlace['href']

    # Extraer fecha del enlace
    patron = r'(\d{2})-(\d{2})-(\d{4})'
    match = re.search(patron, url)
    if match:
        dia, mes, anio = match.groups()
        fecha = f"{dia}-{mes}-{anio}"
    else:
        fecha = datetime.now().strftime('%d-%m-%Y')

    return url, fecha

def extraer_precios_y_fecha(url):
    response = requests.get(url)
    soup = BeautifulSoup(response.text, 'html.parser')

    cultivos_data = []
    tabla = soup.find('table')
    if not tabla:
        return cultivos_data

    filas = tabla.find_all('tr')
    for fila in filas[1:]:
        columnas = fila.find_all('td')
        if len(columnas) >= 3:
            nombre = columnas[0].get_text(strip=True)
            precio_anterior = parsear_precio(columnas[1].get_text())
            precio_actual = parsear_precio(columnas[2].get_text())

            cultivo = {
                'nombre': nombre,
                'precio_anterior': precio_anterior,
                'precio_actual': precio_actual
            }
            cultivos_data.append(cultivo)
    return cultivos_data

def subir_cotizaciones_por_fecha_cereales(fecha, cultivos):
    doc_ref = (
        db.collection('cereales')
        .document('cotizaciones_por_fecha')
        .collection('fechas')
        .document(fecha)
    )

    data_para_fecha = {}
    for cultivo in cultivos:
        nombre = cultivo['nombre']
        if not nombre:
            continue

        data_para_fecha[nombre] = {
            'precio_anterior': cultivo.get('precio_anterior'),
            'precio_actual': cultivo.get('precio_actual'),
        }

    doc_ref.set(data_para_fecha)
    print(f"Subido en 'cereales → cotizaciones_por_fecha → fechas → {fecha}'")

def main():
    url_actual, fecha = obtener_url_actual_y_fecha()
    if not url_actual:
        print("No se pudo encontrar la URL de la lonja actual.")
        return

    print(f"Extrayendo datos de: {url_actual}")
    cultivos = extraer_precios_y_fecha(url_actual)
    if cultivos:
        subir_cotizaciones_por_fecha_cereales(fecha, cultivos)
    else:
        print("No se encontraron datos para subir.")

if __name__ == '__main__':
    main()