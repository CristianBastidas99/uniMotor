package co.edu.uniquindio.unimotor.web.rest.dto;

/** Resumen de un vehículo para listados. */
public class VehiculoResumenDTO {

	private Long id;
	private String titulo;
	private String marca;
	private String modelo;
	private int anio;
	private Double precio;
	private int kilometros;
	private String tipo;
	private String condicion;
	private String combustible;
	private String transmision;
	private String ciudad;
	private String fotoPrincipal;
	private int numeroBusquedas;
	private boolean activo;
	private boolean vendido;
	private String fechaPublicacion;
	private String url;

	/** Constructor vacío requerido por JSON-B. */
	public VehiculoResumenDTO() {
	}

	/** @return valor de {@code id} */
	public Long getId() {
		return id;
	}

	/** @param id nuevo valor de {@code id} */
	public void setId(Long id) {
		this.id = id;
	}

	/** @return valor de {@code titulo} */
	public String getTitulo() {
		return titulo;
	}

	/** @param titulo nuevo valor de {@code titulo} */
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	/** @return valor de {@code marca} */
	public String getMarca() {
		return marca;
	}

	/** @param marca nuevo valor de {@code marca} */
	public void setMarca(String marca) {
		this.marca = marca;
	}

	/** @return valor de {@code modelo} */
	public String getModelo() {
		return modelo;
	}

	/** @param modelo nuevo valor de {@code modelo} */
	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	/** @return valor de {@code anio} */
	public int getAnio() {
		return anio;
	}

	/** @param anio nuevo valor de {@code anio} */
	public void setAnio(int anio) {
		this.anio = anio;
	}

	/** @return valor de {@code precio} */
	public Double getPrecio() {
		return precio;
	}

	/** @param precio nuevo valor de {@code precio} */
	public void setPrecio(Double precio) {
		this.precio = precio;
	}

	/** @return valor de {@code kilometros} */
	public int getKilometros() {
		return kilometros;
	}

	/** @param kilometros nuevo valor de {@code kilometros} */
	public void setKilometros(int kilometros) {
		this.kilometros = kilometros;
	}

	/** @return valor de {@code tipo} */
	public String getTipo() {
		return tipo;
	}

	/** @param tipo nuevo valor de {@code tipo} */
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	/** @return valor de {@code condicion} */
	public String getCondicion() {
		return condicion;
	}

	/** @param condicion nuevo valor de {@code condicion} */
	public void setCondicion(String condicion) {
		this.condicion = condicion;
	}

	/** @return valor de {@code combustible} */
	public String getCombustible() {
		return combustible;
	}

	/** @param combustible nuevo valor de {@code combustible} */
	public void setCombustible(String combustible) {
		this.combustible = combustible;
	}

	/** @return valor de {@code transmision} */
	public String getTransmision() {
		return transmision;
	}

	/** @param transmision nuevo valor de {@code transmision} */
	public void setTransmision(String transmision) {
		this.transmision = transmision;
	}

	/** @return valor de {@code ciudad} */
	public String getCiudad() {
		return ciudad;
	}

	/** @param ciudad nuevo valor de {@code ciudad} */
	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}

	/** @return valor de {@code fotoPrincipal} */
	public String getFotoPrincipal() {
		return fotoPrincipal;
	}

	/** @param fotoPrincipal nuevo valor de {@code fotoPrincipal} */
	public void setFotoPrincipal(String fotoPrincipal) {
		this.fotoPrincipal = fotoPrincipal;
	}

	/** @return valor de {@code numeroBusquedas} */
	public int getNumeroBusquedas() {
		return numeroBusquedas;
	}

	/** @param numeroBusquedas nuevo valor de {@code numeroBusquedas} */
	public void setNumeroBusquedas(int numeroBusquedas) {
		this.numeroBusquedas = numeroBusquedas;
	}

	/** @return valor de {@code activo} */
	public boolean isActivo() {
		return activo;
	}

	/** @param activo nuevo valor de {@code activo} */
	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	/** @return valor de {@code vendido} */
	public boolean isVendido() {
		return vendido;
	}

	/** @param vendido nuevo valor de {@code vendido} */
	public void setVendido(boolean vendido) {
		this.vendido = vendido;
	}

	/** @return valor de {@code fechaPublicacion} */
	public String getFechaPublicacion() {
		return fechaPublicacion;
	}

	/** @param fechaPublicacion nuevo valor de {@code fechaPublicacion} */
	public void setFechaPublicacion(String fechaPublicacion) {
		this.fechaPublicacion = fechaPublicacion;
	}

	/** @return valor de {@code url} */
	public String getUrl() {
		return url;
	}

	/** @param url nuevo valor de {@code url} */
	public void setUrl(String url) {
		this.url = url;
	}
}
