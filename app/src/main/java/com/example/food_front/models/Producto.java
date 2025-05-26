package com.example.food_front.models;

public class Producto {
    private int idProducto;
    private String nombre;
    private String descripcion;
    private double precio;
    private String imagenUrl;

    // Constructor
    public Producto(int idProducto, String nombre, String descripcion, double precio, String imagenUrl) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        // Verifica si la URL de la imagen es null y maneja esto adecuadamente
        this.imagenUrl = (imagenUrl != null) ? imagenUrl : ""; // Asignar una cadena vacía si es null
    }

    // Método para obtener el ID
    public int getIdProducto() {
        return idProducto; // Este es el método que necesitas
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

}
