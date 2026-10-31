package Modelos;

import java.time.LocalDateTime;

public class Cliente {
    private int idCliente;
    private String nombre;
    private String telefono;
    private String email;
    private String direccion;
    private LocalDateTime fechaAlta;
    private boolean activo;
    private int saldoPuntos;
    private int totalCompras;
    private double totalGastado;
    private int antiguedadDias;
    
    public Cliente() {
    }
    
    public Cliente(int idCliente, String nombre, String telefono) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.telefono = telefono;
    }
    
    public int getIdCliente() {
        return idCliente;
    }
    
    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }
    
    public void setFechaAlta(LocalDateTime fechaAlta) {
        this.fechaAlta = fechaAlta;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public int getSaldoPuntos() {
        return saldoPuntos;
    }
    
    public void setSaldoPuntos(int saldoPuntos) {
        this.saldoPuntos = saldoPuntos;
    }
    
    public int getTotalCompras() {
        return totalCompras;
    }
    
    public void setTotalCompras(int totalCompras) {
        this.totalCompras = totalCompras;
    }
    
    public double getTotalGastado() {
        return totalGastado;
    }
    
    public void setTotalGastado(double totalGastado) {
        this.totalGastado = totalGastado;
    }
    
    public int getAntiguedadDias() {
        return antiguedadDias;
    }
    
    public void setAntiguedadDias(int antiguedadDias) {
        this.antiguedadDias = antiguedadDias;
    }
    
    public String getAntiguedadTexto() {
        if (antiguedadDias <= 0) {
            return "Nuevo";
        }
        int meses = antiguedadDias / 30;
        int dias = antiguedadDias % 30;
        if (meses == 0) {
            return dias + " día" + (dias != 1 ? "s" : "");
        } else if (dias == 0) {
            return meses + " mes" + (meses != 1 ? "es" : "");
        } else {
            return meses + " mes" + (meses != 1 ? "es" : "") + " y " + dias + " día" + (dias != 1 ? "s" : "");
        }
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}
