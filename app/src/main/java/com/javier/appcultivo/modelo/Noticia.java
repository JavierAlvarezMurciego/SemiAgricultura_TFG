package com.javier.appcultivo.modelo;

public class Noticia {
        private String titulo;
        private String resumen;
        private String link;
        private String publicacion;
        private String imagen_url;

        // Constructor vacío necesario para Firebase
        public Noticia() {}

        // Constructor completo que se puede poner opcional
        public Noticia(String titulo, String resumen, String link, String publicacion, String imagen_url) {
            this.titulo = titulo;
            this.resumen = resumen;
            this.link = link;
            this.publicacion = publicacion;
            this.imagen_url = imagen_url;
        }

        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public String getResumen() {
            return resumen;
        }

        public void setResumen(String resumen) {
            this.resumen = resumen;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getPublicacion() {
            return publicacion;
        }

        public void setPublicacion(String publicacion) {
            this.publicacion = publicacion;
        }

        public String getImagenUrl() {
            return imagen_url;
        }

        public void setImagenUrl(String imagen_url) {
            this.imagen_url = imagen_url;
        }

}
