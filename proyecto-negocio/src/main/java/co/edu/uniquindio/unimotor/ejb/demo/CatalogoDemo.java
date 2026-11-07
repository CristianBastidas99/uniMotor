package co.edu.uniquindio.unimotor.ejb.demo;

import co.edu.uniquindio.unimotor.entidades.TipoCombustible;
import co.edu.uniquindio.unimotor.entidades.TipoTrasmision;
import co.edu.uniquindio.unimotor.entidades.TipoVehiculo;

/**
 * Catálogo de datos reales y coherentes de Colombia usado para generar datos
 * de demostración (personas, ciudades, modelos de vehículos, características,
 * descripciones, preguntas y calificaciones).
 */
public final class CatalogoDemo {

    private CatalogoDemo() {
    }

    // ------------------------------------------------------------------
    // Clases anidadas
    // ------------------------------------------------------------------

    /** Ciudad colombiana con su departamento y un peso relativo según su población. */
    public static final class CiudadDemo {
        /** Nombre de la ciudad. */
        public final String nombre;
        /** Departamento al que pertenece. */
        public final String departamento;
        /** Peso relativo (población aproximada) para el sorteo ponderado. */
        public final int peso;

        /**
         * Crea una ciudad de demostración.
         * @param nombre nombre de la ciudad
         * @param departamento departamento
         * @param peso peso relativo
         */
        public CiudadDemo(String nombre, String departamento, int peso) {
            this.nombre = nombre;
            this.departamento = departamento;
            this.peso = peso;
        }
    }

    /** Modelo de vehículo realmente vendido en Colombia. */
    public static final class ModeloDemo {
        /** Marca del vehículo. */
        public final String marca;
        /** Nombre del modelo. */
        public final String nombre;
        /** Tipo de vehículo. */
        public final TipoVehiculo tipo;
        /** Precio en COP de una unidad nueva del año actual. */
        public final double precioNuevo;
        /** Cilindraje mínimo en cc (0 en eléctricos). */
        public final int cilindrajeMin;
        /** Cilindraje máximo en cc (0 en eléctricos). */
        public final int cilindrajeMax;
        /** Número de puertas (0 en motos). */
        public final int puertas;
        /** Combustibles disponibles para el modelo. */
        public final TipoCombustible[] combustibles;
        /** Transmisiones disponibles para el modelo. */
        public final TipoTrasmision[] transmisiones;
        /** Primer año de venta en Colombia. */
        public final int anioDesde;
        /** Popularidad relativa de 1 a 10. */
        public final int peso;

        /**
         * Crea un modelo de demostración.
         * @param marca marca
         * @param nombre nombre del modelo
         * @param tipo tipo de vehículo
         * @param precioNuevo precio nuevo en COP
         * @param cilindrajeMin cilindraje mínimo
         * @param cilindrajeMax cilindraje máximo
         * @param puertas número de puertas
         * @param combustibles combustibles posibles
         * @param transmisiones transmisiones posibles
         * @param anioDesde primer año de venta
         * @param peso popularidad relativa
         */
        public ModeloDemo(String marca, String nombre, TipoVehiculo tipo, double precioNuevo,
                int cilindrajeMin, int cilindrajeMax, int puertas, TipoCombustible[] combustibles,
                TipoTrasmision[] transmisiones, int anioDesde, int peso) {
            this.marca = marca;
            this.nombre = nombre;
            this.tipo = tipo;
            this.precioNuevo = precioNuevo;
            this.cilindrajeMin = cilindrajeMin;
            this.cilindrajeMax = cilindrajeMax;
            this.puertas = puertas;
            this.combustibles = combustibles;
            this.transmisiones = transmisiones;
            this.anioDesde = anioDesde;
            this.peso = peso;
        }
    }

    /** Característica opcional de un vehículo con su probabilidad de aparecer. */
    public static final class CaracteristicaDemo {
        /** Nombre de la característica. */
        public final String nombre;
        /** Tipos de vehículo a los que aplica. */
        public final TipoVehiculo[] aplicaA;
        /** Probabilidad de 0 a 1 de que un vehículo aplicable la tenga. */
        public final double probabilidad;

        /**
         * Crea una característica de demostración.
         * @param nombre nombre
         * @param aplicaA tipos a los que aplica
         * @param probabilidad probabilidad entre 0 y 1
         */
        public CaracteristicaDemo(String nombre, TipoVehiculo[] aplicaA, double probabilidad) {
            this.nombre = nombre;
            this.aplicaA = aplicaA;
            this.probabilidad = probabilidad;
        }
    }

    /** Color de vehículo con su peso relativo de frecuencia. */
    public static final class ColorDemo {
        /** Nombre del color. */
        public final String nombre;
        /** Peso relativo de frecuencia. */
        public final int peso;

        /**
         * Crea un color de demostración.
         * @param nombre nombre del color
         * @param peso peso relativo
         */
        public ColorDemo(String nombre, int peso) {
            this.nombre = nombre;
            this.peso = peso;
        }
    }

    /** Pregunta típica de un comprador con sus posibles respuestas del vendedor. */
    public static final class PreguntaDemo {
        /** Texto de la pregunta. */
        public final String pregunta;
        /** Respuestas posibles del vendedor. */
        public final String[] respuestas;
        /** Tipos a los que aplica; null significa todos los tipos. */
        public final TipoVehiculo[] aplicaA;

        /**
         * Crea una pregunta de demostración.
         * @param pregunta texto de la pregunta
         * @param respuestas respuestas posibles
         * @param aplicaA tipos a los que aplica, o null para todos
         */
        public PreguntaDemo(String pregunta, String[] respuestas, TipoVehiculo[] aplicaA) {
            this.pregunta = pregunta;
            this.respuestas = respuestas;
            this.aplicaA = aplicaA;
        }
    }

    /** Comentario de calificación a un vendedor con su puntaje en estrellas. */
    public static final class ComentarioDemo {
        /** Estrellas de 1 a 5. */
        public final int estrellas;
        /** Texto del comentario. */
        public final String texto;

        /**
         * Crea un comentario de demostración.
         * @param estrellas puntaje de 1 a 5
         * @param texto texto del comentario
         */
        public ComentarioDemo(int estrellas, String texto) {
            this.estrellas = estrellas;
            this.texto = texto;
        }
    }

    // ------------------------------------------------------------------
    // Ayudantes privados de construcción
    // ------------------------------------------------------------------

    /** Construye un arreglo de tipos de vehículo. */
    private static TipoVehiculo[] tv(TipoVehiculo... t) {
        return t;
    }

    /** Construye un arreglo de combustibles. */
    private static TipoCombustible[] cb(TipoCombustible... c) {
        return c;
    }

    /** Construye un arreglo de transmisiones. */
    private static TipoTrasmision[] tr(TipoTrasmision... t) {
        return t;
    }

    /** Crea un modelo de carro. */
    private static ModeloDemo carro(String marca, String nombre, double precio, int ccMin, int ccMax,
            int puertas, TipoCombustible[] comb, TipoTrasmision[] trans, int desde, int peso) {
        return new ModeloDemo(marca, nombre, TipoVehiculo.CARRO, precio, ccMin, ccMax, puertas, comb, trans, desde, peso);
    }

    /** Crea un modelo de motocicleta. */
    private static ModeloDemo moto(String marca, String nombre, double precio, int ccMin, int ccMax,
            TipoTrasmision[] trans, int desde, int peso) {
        return new ModeloDemo(marca, nombre, TipoVehiculo.MOTOCICLETA, precio, ccMin, ccMax, 0,
                cb(TipoCombustible.GASOLINA), trans, desde, peso);
    }

    /** Crea un modelo de camioneta. */
    private static ModeloDemo camioneta(String marca, String nombre, double precio, int ccMin, int ccMax,
            TipoCombustible[] comb, TipoTrasmision[] trans, int desde, int peso) {
        return new ModeloDemo(marca, nombre, TipoVehiculo.CAMIONETA, precio, ccMin, ccMax, 4, comb, trans, desde, peso);
    }

    /** Genera nombres de archivo de foto con formato prefijo-NN.jpg. */
    private static String[] generarFotos(String prefijo, int cantidad) {
        String[] fotos = new String[cantidad];
        for (int i = 0; i < cantidad; i++) {
            fotos[i] = String.format("%s-%02d.jpg", prefijo, i + 1);
        }
        return fotos;
    }

    // ------------------------------------------------------------------
    // Personas y contacto
    // ------------------------------------------------------------------

    /** Nombres masculinos comunes en Colombia. */
    public static final String[] NOMBRES_HOMBRE = {
        "Juan", "Carlos", "Andrés", "Sebastián", "Camilo", "Santiago", "Daniel", "David", "Felipe", "Jhon",
        "Luis", "Jorge", "Miguel", "Alejandro", "Mateo", "Nicolás", "Julián", "Diego", "Cristian", "Óscar",
        "Fernando", "Mauricio", "Alberto", "Ricardo", "Hernán", "Germán", "Esteban", "Johan", "Brayan", "Wilson",
        "Édgar", "Héctor", "Iván", "Jairo", "Víctor", "Álvaro", "Gustavo", "Rafael", "Samuel", "Tomás",
        "Andrey", "Yeison", "Leonardo", "Sergio"
    };

    /** Nombres femeninos comunes en Colombia. */
    public static final String[] NOMBRES_MUJER = {
        "María", "Laura", "Valentina", "Daniela", "Carolina", "Paula", "Natalia", "Andrea", "Camila", "Juliana",
        "Sofía", "Isabella", "Luisa", "Sara", "Diana", "Paola", "Claudia", "Mónica", "Liliana", "Marcela",
        "Katherine", "Johana", "Alejandra", "Luz", "Ángela", "Viviana", "Catalina", "Estefanía", "Manuela", "Lina",
        "Yurany", "Sandra", "Patricia", "Gloria", "Adriana", "Tatiana", "Verónica", "Silvia", "Melissa", "Jessica",
        "Gabriela", "Mariana", "Susana", "Érika"
    };

    /** Apellidos comunes en Colombia. */
    public static final String[] APELLIDOS = {
        "García", "Rodríguez", "Martínez", "López", "González", "Gómez", "Hernández", "Pérez", "Sánchez", "Ramírez",
        "Torres", "Díaz", "Vargas", "Rojas", "Moreno", "Jiménez", "Castro", "Ortiz", "Gutiérrez", "Suárez",
        "Muñoz", "Álvarez", "Romero", "Ruiz", "Mendoza", "Aguirre", "Ospina", "Restrepo", "Henao", "Vélez",
        "Cardona", "Zapata", "Giraldo", "Arias", "Orozco", "Salazar", "Quintero", "Bedoya", "Montoya", "Londoño",
        "Duque", "Marín", "Correa", "Cano", "Franco", "Valencia", "Betancur", "Gaviria", "Mejía", "Uribe",
        "Echeverri", "Jaramillo", "Hoyos", "Patiño", "Ríos", "Parra", "Cárdenas", "Medina", "Herrera", "Peña",
        "Cortés", "Ramos", "Acosta", "Pineda", "Bermúdez", "Cruz", "Guerrero", "Navarro", "Delgado", "Cifuentes",
        "Vega", "Silva", "Cabrera", "Bautista", "Barrera", "Ardila", "Camacho", "Mora", "Prieto", "Rincón",
        "Soto", "Lozano", "Forero", "Galvis", "Becerra", "Arango", "Ceballos", "Naranjo", "Osorio", "Tamayo"
    };

    /** Dominios de correo electrónico frecuentes. */
    public static final String[] DOMINIOS_EMAIL = {
        "gmail.com", "hotmail.com", "outlook.com", "yahoo.com", "hotmail.es", "gmail.com", "outlook.es", "live.com"
    };

    /** Prefijos reales de telefonía celular en Colombia. */
    public static final String[] PREFIJOS_CELULAR = {
        "300", "301", "302", "304", "305", "310", "311", "312", "313", "314", "315", "316", "317", "318", "319",
        "320", "321", "322", "323", "324", "350", "351"
    };

    /** Tipos de vía para armar direcciones. */
    public static final String[] TIPOS_VIA = {
        "Calle", "Carrera", "Avenida", "Transversal", "Diagonal"
    };

    /** Ciudades con su departamento y peso aproximado a la población. */
    public static final CiudadDemo[] CIUDADES = {
        new CiudadDemo("Bogotá D.C.", "Bogotá", 100),
        new CiudadDemo("Medellín", "Antioquia", 35),
        new CiudadDemo("Cali", "Valle del Cauca", 30),
        new CiudadDemo("Barranquilla", "Atlántico", 17),
        new CiudadDemo("Bucaramanga", "Santander", 8),
        new CiudadDemo("Cartagena", "Bolívar", 13),
        new CiudadDemo("Pereira", "Risaralda", 14),
        new CiudadDemo("Manizales", "Caldas", 10),
        new CiudadDemo("Armenia", "Quindío", 14),
        new CiudadDemo("Calarcá", "Quindío", 5),
        new CiudadDemo("Ibagué", "Tolima", 7),
        new CiudadDemo("Villavicencio", "Meta", 7),
        new CiudadDemo("Santa Marta", "Magdalena", 7),
        new CiudadDemo("Pasto", "Nariño", 5),
        new CiudadDemo("Neiva", "Huila", 5),
        new CiudadDemo("Montería", "Córdoba", 5),
        new CiudadDemo("Popayán", "Cauca", 4),
        new CiudadDemo("Tunja", "Boyacá", 3),
        new CiudadDemo("Valledupar", "Cesar", 4),
        new CiudadDemo("Envigado", "Antioquia", 6),
        new CiudadDemo("Bello", "Antioquia", 6),
        new CiudadDemo("Itagüí", "Antioquia", 5),
        new CiudadDemo("Soacha", "Cundinamarca", 6),
        new CiudadDemo("Dosquebradas", "Risaralda", 8)
    };

    // ------------------------------------------------------------------
    // Modelos
    // ------------------------------------------------------------------

    /** Modelos reales vendidos en Colombia (carros, motos y camionetas). */
    public static final ModeloDemo[] MODELOS = {
        // Chevrolet
        carro("Chevrolet", "Spark GT", 62_000_000, 1200, 1200, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO), 2011, 6),
        carro("Chevrolet", "Onix", 78_000_000, 1000, 1200, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2020, 8),
        carro("Chevrolet", "Onix Sedán", 96_000_000, 1000, 1000, 4, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.AUTOMATICO), 2020, 5),
        carro("Chevrolet", "Tracker", 120_000_000, 1200, 1200, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.AUTOMATICO), 2021, 7),
        carro("Chevrolet", "Captiva", 135_000_000, 1500, 1500, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.AUTOMATICO), 2007, 4),
        carro("Chevrolet", "Joy", 62_000_000, 1400, 1400, 4, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO), 2020, 5),
        // Renault
        carro("Renault", "Logan", 66_000_000, 1600, 1600, 4, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2007, 8),
        carro("Renault", "Sandero", 70_000_000, 1600, 1600, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2008, 7),
        carro("Renault", "Sandero Stepway", 80_000_000, 1600, 1600, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2010, 6),
        carro("Renault", "Duster", 105_000_000, 1600, 2000, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2012, 7),
        carro("Renault", "Kwid", 54_000_000, 1000, 1000, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO), 2018, 6),
        carro("Renault", "Kardian", 100_000_000, 1000, 1000, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2025, 3),
        // Mazda
        carro("Mazda", "Mazda 2", 76_000_000, 1500, 1500, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2009, 7),
        carro("Mazda", "Mazda 3", 110_000_000, 2000, 2500, 4, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2010, 8),
        carro("Mazda", "CX-30", 140_000_000, 2000, 2500, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.AUTOMATICO), 2020, 6),
        carro("Mazda", "CX-5", 175_000_000, 2000, 2500, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.AUTOMATICO), 2013, 5),
        // Kia
        carro("Kia", "Picanto", 62_000_000, 1000, 1250, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2012, 7),
        carro("Kia", "Rio", 85_000_000, 1400, 1600, 4, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2008, 6),
        carro("Kia", "Sportage", 160_000_000, 2000, 2000, 5, cb(TipoCombustible.GASOLINA, TipoCombustible.HIBRIDO), tr(TipoTrasmision.AUTOMATICO), 2011, 6),
        carro("Kia", "Seltos", 125_000_000, 1600, 2000, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.AUTOMATICO), 2021, 5),
        carro("Kia", "Sonet", 85_000_000, 1500, 1500, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2022, 5),
        // Toyota
        carro("Toyota", "Corolla", 120_000_000, 1800, 2000, 4, cb(TipoCombustible.GASOLINA, TipoCombustible.HIBRIDO), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2008, 7),
        carro("Toyota", "Corolla Cross", 160_000_000, 1800, 2000, 5, cb(TipoCombustible.GASOLINA, TipoCombustible.HIBRIDO), tr(TipoTrasmision.AUTOMATICO), 2022, 6),
        carro("Toyota", "Yaris", 85_000_000, 1500, 1500, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2007, 5),
        carro("Toyota", "Raize", 98_000_000, 1200, 1200, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.AUTOMATICO), 2022, 4),
        // Nissan
        carro("Nissan", "Versa", 88_000_000, 1600, 1600, 4, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2012, 6),
        carro("Nissan", "March", 62_000_000, 1600, 1600, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2012, 5),
        carro("Nissan", "Kicks", 120_000_000, 1600, 1600, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.AUTOMATICO), 2018, 5),
        // Suzuki
        carro("Suzuki", "Swift", 78_000_000, 1200, 1200, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2008, 5),
        carro("Suzuki", "Vitara", 120_000_000, 1400, 1600, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.AUTOMATICO), 2016, 4),
        // Hyundai
        carro("Hyundai", "Accent", 80_000_000, 1400, 1600, 4, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2008, 5),
        carro("Hyundai", "Creta", 125_000_000, 1500, 1500, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.AUTOMATICO), 2017, 6),
        carro("Hyundai", "Grand i10", 64_000_000, 1200, 1200, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2014, 6),
        carro("Hyundai", "Tucson", 165_000_000, 2000, 2000, 5, cb(TipoCombustible.GASOLINA, TipoCombustible.HIBRIDO), tr(TipoTrasmision.AUTOMATICO), 2010, 5),
        // Ford
        carro("Ford", "Territory", 140_000_000, 1500, 1500, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.AUTOMATICO), 2023, 3),
        // Volkswagen
        carro("Volkswagen", "Polo", 85_000_000, 1000, 1600, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2018, 4),
        carro("Volkswagen", "T-Cross", 125_000_000, 1000, 1000, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.AUTOMATICO), 2020, 4),
        carro("Volkswagen", "Nivus", 120_000_000, 1000, 1000, 5, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.AUTOMATICO), 2022, 3),
        // BYD
        carro("BYD", "Dolphin", 115_000_000, 0, 0, 5, cb(TipoCombustible.ELECTRICO), tr(TipoTrasmision.AUTOMATICO), 2024, 5),
        carro("BYD", "Dolphin Mini", 80_000_000, 0, 0, 5, cb(TipoCombustible.ELECTRICO), tr(TipoTrasmision.AUTOMATICO), 2024, 4),
        carro("BYD", "Song Plus", 175_000_000, 0, 1500, 5, cb(TipoCombustible.HIBRIDO, TipoCombustible.ELECTRICO), tr(TipoTrasmision.AUTOMATICO), 2024, 3),
        // Motos
        moto("Yamaha", "NMAX 155", 14_500_000, 155, 155, tr(TipoTrasmision.AUTOMATICO), 2017, 8),
        moto("Yamaha", "FZ 150", 11_000_000, 149, 149, tr(TipoTrasmision.MECANICO), 2015, 7),
        moto("Yamaha", "XTZ 125", 10_000_000, 125, 125, tr(TipoTrasmision.MECANICO), 2010, 6),
        moto("Yamaha", "MT-03", 24_000_000, 321, 321, tr(TipoTrasmision.MECANICO), 2016, 3),
        moto("AKT", "NKD 125", 6_000_000, 125, 125, tr(TipoTrasmision.MECANICO), 2012, 9),
        moto("AKT", "TTR 200", 10_500_000, 200, 200, tr(TipoTrasmision.MECANICO), 2015, 5),
        moto("Bajaj", "Pulsar NS 160", 11_500_000, 160, 160, tr(TipoTrasmision.MECANICO), 2016, 7),
        moto("Bajaj", "Boxer CT100", 5_800_000, 102, 102, tr(TipoTrasmision.MECANICO), 2008, 9),
        moto("Honda", "CB 190R", 12_500_000, 184, 184, tr(TipoTrasmision.MECANICO), 2018, 6),
        moto("Honda", "XR 150L", 10_500_000, 149, 149, tr(TipoTrasmision.MECANICO), 2014, 6),
        moto("Suzuki", "Gixxer 150", 10_800_000, 155, 155, tr(TipoTrasmision.MECANICO), 2017, 5),
        moto("Suzuki", "GN 125", 7_000_000, 125, 125, tr(TipoTrasmision.MECANICO), 2008, 5),
        // Camionetas
        camioneta("Toyota", "Hilux", 205_000_000, 2400, 2800, cb(TipoCombustible.DISEL), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2005, 9),
        camioneta("Toyota", "Fortuner", 270_000_000, 2700, 2800, cb(TipoCombustible.DISEL, TipoCombustible.GASOLINA), tr(TipoTrasmision.AUTOMATICO), 2012, 5),
        camioneta("Toyota", "Prado", 340_000_000, 2700, 2800, cb(TipoCombustible.DISEL, TipoCombustible.GASOLINA), tr(TipoTrasmision.AUTOMATICO), 2010, 4),
        camioneta("Chevrolet", "D-Max", 160_000_000, 1900, 3000, cb(TipoCombustible.DISEL), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2012, 6),
        camioneta("Nissan", "Frontier", 180_000_000, 2300, 2500, cb(TipoCombustible.DISEL), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2005, 5),
        camioneta("Ford", "Ranger", 195_000_000, 2000, 3200, cb(TipoCombustible.DISEL), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2012, 6),
        camioneta("Volkswagen", "Amarok", 230_000_000, 2000, 3000, cb(TipoCombustible.DISEL), tr(TipoTrasmision.AUTOMATICO), 2012, 3),
        camioneta("Renault", "Duster Oroch", 105_000_000, 1600, 2000, cb(TipoCombustible.GASOLINA), tr(TipoTrasmision.MECANICO, TipoTrasmision.AUTOMATICO), 2016, 4)
    };

    // ------------------------------------------------------------------
    // Características, colores
    // ------------------------------------------------------------------

    /** Características opcionales con los tipos a los que aplican. */
    public static final CaracteristicaDemo[] CARACTERISTICAS = {
        new CaracteristicaDemo("Aire acondicionado", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA), 0.90),
        new CaracteristicaDemo("Cámara de reversa", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA), 0.50),
        new CaracteristicaDemo("Sensores de parqueo", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA), 0.40),
        new CaracteristicaDemo("Sunroof", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA), 0.12),
        new CaracteristicaDemo("Tracción 4x4", tv(TipoVehiculo.CAMIONETA), 0.75),
        new CaracteristicaDemo("Bluetooth", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA, TipoVehiculo.MOTOCICLETA), 0.65),
        new CaracteristicaDemo("Pantalla táctil", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA), 0.55),
        new CaracteristicaDemo("Android Auto / Apple CarPlay", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA), 0.40),
        new CaracteristicaDemo("Rines de lujo", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA), 0.35),
        new CaracteristicaDemo("Vidrios eléctricos", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA), 0.90),
        new CaracteristicaDemo("Airbags", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA), 0.90),
        new CaracteristicaDemo("Frenos ABS", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA, TipoVehiculo.MOTOCICLETA), 0.70),
        new CaracteristicaDemo("Control crucero", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA), 0.25),
        new CaracteristicaDemo("Sillas en cuero", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA), 0.25),
        new CaracteristicaDemo("Cierre centralizado", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA), 0.85),
        new CaracteristicaDemo("Luces LED", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA, TipoVehiculo.MOTOCICLETA), 0.45),
        new CaracteristicaDemo("Baúl para moto", tv(TipoVehiculo.MOTOCICLETA), 0.30),
        new CaracteristicaDemo("Alarma", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA, TipoVehiculo.MOTOCICLETA), 0.50),
        new CaracteristicaDemo("Parrilla trasera", tv(TipoVehiculo.MOTOCICLETA), 0.40),
        new CaracteristicaDemo("Tablero digital", tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA, TipoVehiculo.MOTOCICLETA), 0.45)
    };

    /** Colores de vehículo con su peso de frecuencia. */
    public static final ColorDemo[] COLORES = {
        new ColorDemo("Blanco", 24),
        new ColorDemo("Gris", 20),
        new ColorDemo("Negro", 18),
        new ColorDemo("Plata", 14),
        new ColorDemo("Rojo", 8),
        new ColorDemo("Azul", 6),
        new ColorDemo("Beige", 2),
        new ColorDemo("Verde", 2),
        new ColorDemo("Naranja", 1),
        new ColorDemo("Amarillo", 1),
        new ColorDemo("Vinotinto", 3),
        new ColorDemo("Marrón", 1)
    };

    // ------------------------------------------------------------------
    // Textos
    // ------------------------------------------------------------------

    /**
     * Plantillas de descripción para vehículos usados. Marcadores: marca, modelo,
     * anio, km, color y ciudad entre llaves.
     */
    public static final String[] DESCRIPCIONES = {
        "{marca} {modelo} {anio}, único dueño, {km} km, todos los mantenimientos en concesionario. Color {color}, en {ciudad}.",
        "Vendo {marca} {modelo} modelo {anio} en excelente estado, con {km} km reales. Papeles al día y listo para traspaso en {ciudad}.",
        "{marca} {modelo} {anio} color {color}. Recorrido {km} km, siempre guardado en garaje. Se recibe a prueba mecánica.",
        "Vehículo en perfecto estado, {marca} {modelo} {anio}, {km} km. Soat y tecnomecánica vigentes. Ubicado en {ciudad}.",
        "Se vende {marca} {modelo} {anio}, segundo dueño, {km} km, mantenimientos al día con facturas. Negociable.",
        "{marca} {modelo} {anio} {color}, impecable por dentro y por fuera. {km} km, llantas nuevas. Escucho ofertas serias.",
        "Por viaje al exterior vendo mi {marca} {modelo} {anio}. Tiene {km} km, nunca ha chocado. Ciudad: {ciudad}.",
        "{marca} {modelo} {anio}, carro de familia muy bien cuidado, {km} km. Todo en regla, sin deudas ni multas.",
        "Vendo {marca} {modelo} {anio} color {color}, {km} km. Primera mano en {ciudad}, revisión técnica completa recién hecha.",
        "{marca} {modelo} {anio} con {km} km, sin choques ni pintura. Se entrega con kit de carretera y llave de repuesto.",
        "Oportunidad: {marca} {modelo} {anio}, {km} km, motor y caja en perfecto estado. Recibo vehículo como parte de pago.",
        "{marca} {modelo} {anio}, uso particular, {km} km. Cuidado con esmero, siempre con repuestos originales. {ciudad}.",
        "Vendo por cambio de vehículo mi {marca} {modelo} {anio}, color {color}. Tiene {km} km y todos los papeles al día.",
        "{marca} {modelo} {anio} en {ciudad}. {km} km, excelente consumo de combustible, listo para estrenar. Muy buen precio.",
        "Se vende {marca} {modelo} {anio}, {km} km, única mano, color {color}. Se permite revisión en taller de confianza.",
        "{marca} {modelo} {anio}, {km} km. Mantenimiento preventivo cada 5.000 km, con hoja de vida completa. Traspaso incluido.",
        "Vendo {marca} {modelo} {anio} en muy buen estado general, {km} km. Pintura original, tapicería impecable. {ciudad}.",
        "{marca} {modelo} {anio} color {color}, {km} km. Ideal para uso diario, económico y confiable. Acepto crédito o contado.",
        "Excelente {marca} {modelo} {anio}, recorrido {km} km. Revisado y listo para entregar. Disponible para ver en {ciudad}.",
        "Mi {marca} {modelo} {anio} busca nuevo dueño. {km} km, sin detalles mecánicos, con Soat y revisión vigentes.",
        "{marca} {modelo} {anio}, {km} km, color {color}. Un solo propietario, nunca fue taxi ni servicio público.",
        "Vendo {marca} {modelo} {anio} por no usarlo, muy bien cuidado. {km} km, documentos al día, ubicado en {ciudad}.",
        "{marca} {modelo} {anio} con {km} km, motor sin fugas, frenos y suspensión revisados. Precio negociable.",
        "Se vende {marca} {modelo} {anio} {color}. {km} km reales, certificables. Escucho propuestas, también permuta."
    };

    /**
     * Plantillas de descripción exclusivas para vehículos 0 km. Marcadores: marca,
     * modelo, anio, color y ciudad entre llaves.
     */
    public static final String[] DESCRIPCIONES_NUEVO = {
        "{marca} {modelo} {anio} 0 km, color {color}. Entrega inmediata en {ciudad} con garantía de fábrica.",
        "Estrena tu {marca} {modelo} {anio}. Vehículo nuevo, sin matrícula, con garantía del fabricante y facilidades de financiación.",
        "{marca} {modelo} {anio} nuevo, color {color}. Disponible en {ciudad}, con entrega inmediata y primer mantenimiento incluido.",
        "Vehículo 0 km {marca} {modelo} {anio}. Garantía de 3 años y asistencia en carretera. Consulta por planes de crédito.",
        "{marca} {modelo} {anio}, unidad nueva en {ciudad}. Aprovecha nuestro plan de financiación y recibimos tu vehículo actual.",
        "Nuevo {marca} {modelo} {anio} color {color}, listo para entrega. Incluye matrícula, Soat y kit de carretera.",
        "Lanzamiento {marca} {modelo} {anio} 0 km. Pregunta por la promoción del mes y agenda tu prueba de manejo en {ciudad}.",
        "{marca} {modelo} {anio} sin estrenar, color {color}. Con garantía total de fábrica y red de servicio a nivel nacional."
    };

    /** Preguntas frecuentes de compradores con sus posibles respuestas. */
    public static final PreguntaDemo[] PREGUNTAS = {
        new PreguntaDemo("¿El precio es negociable?",
            new String[] {"Sí, hay un pequeño margen de negociación para quien venga a verlo.",
                "El precio es casi fijo, pero podemos conversar si viene con el dinero listo.",
                "Poco negociable, ya está muy bien de precio."}, null),
        new PreguntaDemo("¿Tiene el SOAT y la tecnomecánica vigentes?",
            new String[] {"Sí, ambos están vigentes y al día.",
                "El SOAT está vigente y la tecnomecánica se renueva antes de la entrega."}, null),
        new PreguntaDemo("¿Recibe vehículo como parte de pago?",
            new String[] {"Sí, recibo vehículo de menor valor como parte de pago.",
                "Por ahora solo vendo de contado o con crédito, no recibo permutas.",
                "Depende del modelo y año del vehículo, cuénteme cuál tiene."}, null),
        new PreguntaDemo("¿Cuántos dueños ha tenido?",
            new String[] {"Soy el único dueño desde que se compró.",
                "Ha tenido dos dueños, yo soy el segundo.",
                "Es de primera mano, con todos los papeles a mi nombre."}, null),
        new PreguntaDemo("¿Se puede ver este fin de semana?",
            new String[] {"Claro, el sábado en la mañana estoy disponible. Escríbame para coordinar.",
                "Sí, el domingo en la tarde podemos encontrarnos para que lo vea.",
                "Entre semana después de las 5 p. m. me queda más fácil, ¿le sirve?"}, null),
        new PreguntaDemo("¿Tiene deudas, multas o comparendos pendientes?",
            new String[] {"No, está al día y sin multas ni comparendos.",
                "Cero deudas. Puede verificarlo en el Runt antes de comprar."}, null),
        new PreguntaDemo("¿Aceptan que lo revise un mecánico de mi confianza?",
            new String[] {"Por supuesto, puede traer a su mecánico cuando quiera.",
                "Sí, sin problema. Lo importante es que quede tranquilo con la compra."}, null),
        new PreguntaDemo("¿Ha tenido choques o ha sido repintado?",
            new String[] {"No, nunca ha tenido choques. Pintura original.",
                "Solo un detalle leve en el parachoques, ya reparado, nada estructural.",
                "Sin choques. Se puede revisar con medidor de pintura."}, null),
        new PreguntaDemo("¿Me puede enviar más fotos?",
            new String[] {"Claro, le escribo y le mando más fotos y un video.",
                "Con gusto, deme su contacto y se las envío por WhatsApp."}, null),
        new PreguntaDemo("¿Tiene los mantenimientos al día?",
            new String[] {"Sí, todos los mantenimientos están al día y tengo las facturas.",
                "Los hice en el taller autorizado y cuento con el registro completo."}, null),
        new PreguntaDemo("¿Está disponible?",
            new String[] {"Sí, sigue disponible. ¿Quiere agendar una visita?",
                "Disponible. Cualquier duda con gusto se la resuelvo."}, null),
        new PreguntaDemo("¿Qué incluye el traspaso y quién paga los gastos?",
            new String[] {"El traspaso lo hacemos juntos en el tránsito, los gastos se dividen entre los dos.",
                "Los gastos de traspaso corren por cuenta del comprador."}, null),
        new PreguntaDemo("¿Aceptan crédito o financiación?",
            new String[] {"Sí, se acepta crédito bancario, nos ponemos de acuerdo con el trámite.",
                "Prefiero de contado, pero si el crédito es aprobado de una vez no hay problema."}, null),
        new PreguntaDemo("¿Cuál es el último precio?",
            new String[] {"El último precio es el publicado menos un pequeño ajuste si viene a verlo.",
                "Ya tiene el mejor precio del mercado, pero hablemos en persona."}, null),
        new PreguntaDemo("¿Tiene llaves de repuesto y manuales?",
            new String[] {"Sí, entrego dos llaves y los manuales originales.",
                "Tengo ambas llaves y el manual del propietario."}, null),
        new PreguntaDemo("¿Qué consumo de combustible tiene?",
            new String[] {"Es muy económico, en ciudad rinde bastante bien.",
                "El consumo es el normal para el modelo, muy aceptable."}, null),
        new PreguntaDemo("¿Está disponible para prueba de manejo?",
            new String[] {"Sí, con gusto lo prueba, solo traiga su licencia de conducción.",
                "Claro, la prueba de manejo es sin compromiso."}, null),
        new PreguntaDemo("¿Tiene garantía?",
            new String[] {"Si es nuevo tiene la garantía de fábrica; si es usado se vende en el estado en que está.",
                "Cuenta con la garantía del fabricante vigente según el año del vehículo."}, null),
        new PreguntaDemo("¿En qué ciudad está matriculado?",
            new String[] {"Está matriculado en la misma ciudad donde se encuentra.",
                "Tiene matrícula de otra ciudad, pero el traslado de cuenta lo hacemos sin problema."}, null),
        new PreguntaDemo("¿Tiene pico y placa?",
            new String[] {"Depende de la ciudad y el último dígito de la placa, le confirmo el dígito.",
                "Sí, tiene restricción un día a la semana según la ciudad."}, null),
        new PreguntaDemo("¿Por qué lo vende?",
            new String[] {"Estoy cambiando de vehículo por uno más grande.",
                "Por viaje, ya no lo voy a utilizar.",
                "Compré otro y no necesito dos."}, null),
        new PreguntaDemo("¿Las llantas están en buen estado?",
            new String[] {"Están en muy buen estado, con más de la mitad de vida útil.",
                "Son nuevas, las cambié hace poco."}, tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA)),
        new PreguntaDemo("¿El aire acondicionado funciona bien?",
            new String[] {"Sí, enfría perfectamente.",
                "Funciona muy bien, se le hizo mantenimiento recientemente."}, tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA)),
        new PreguntaDemo("¿Cuántas llaves tiene y trae control de alarma?",
            new String[] {"Tiene dos llaves con control de alarma.",
                "Incluye una llave con control y otra de repuesto."}, tv(TipoVehiculo.CARRO, TipoVehiculo.CAMIONETA)),
        new PreguntaDemo("¿La tracción 4x4 funciona bien?",
            new String[] {"Sí, la 4x4 funciona perfecto, se ha probado en trocha.",
                "Todo el sistema de tracción está revisado y sin fallas."}, tv(TipoVehiculo.CAMIONETA)),
        new PreguntaDemo("¿Se ha usado para carga o trabajo pesado?",
            new String[] {"No, solo uso familiar y de paseo.",
                "Algo de uso en finca, pero siempre con mantenimiento al día."}, tv(TipoVehiculo.CAMIONETA)),
        new PreguntaDemo("¿Incluye casco?",
            new String[] {"Sí, incluyo casco certificado y chaleco reflectivo.",
                "Incluye el casco que he usado, en buen estado."}, tv(TipoVehiculo.MOTOCICLETA)),
        new PreguntaDemo("¿Tiene todos los papeles de la moto al día, incluida la tarjeta de propiedad?",
            new String[] {"Sí, tarjeta de propiedad, SOAT y revisión al día.",
                "Todo en regla, sin prenda y sin embargos."}, tv(TipoVehiculo.MOTOCICLETA)),
        new PreguntaDemo("¿La moto tiene alguna caída o golpe?",
            new String[] {"No, nunca se ha caído. Solo el desgaste normal de uso.",
                "Un rayón leve en el guardabarros, nada más."}, tv(TipoVehiculo.MOTOCICLETA)),
        new PreguntaDemo("¿Cuándo fue el último cambio de aceite y kit de arrastre?",
            new String[] {"El aceite se cambió hace poco y el kit de arrastre está en buen estado.",
                "Hice el cambio de aceite hace un mes; el kit de arrastre lo cambié recientemente."}, tv(TipoVehiculo.MOTOCICLETA))
    };

    /** Comentarios de calificación a vendedores, en su mayoría positivos. */
    public static final ComentarioDemo[] COMENTARIOS = {
        new ComentarioDemo(5, "Excelente vendedor, todo tal como lo describió. Muy honesto y puntual en la entrega."),
        new ComentarioDemo(5, "Una compra muy tranquila. El vehículo estaba impecable y los papeles perfectos."),
        new ComentarioDemo(5, "Muy amable y transparente. Recomendado al cien por ciento."),
        new ComentarioDemo(5, "Gran atención, resolvió todas mis dudas y el traspaso fue rápido."),
        new ComentarioDemo(5, "Totalmente confiable. El carro estaba como en las fotos. Volvería a comprarle."),
        new ComentarioDemo(5, "Vendedor serio y cumplido. Nos pusimos de acuerdo fácil en el precio."),
        new ComentarioDemo(5, "Todo perfecto, desde el primer mensaje hasta la entrega. Muy recomendado."),
        new ComentarioDemo(5, "Excelente trato y mucha disposición para que mi mecánico revisara el vehículo."),
        new ComentarioDemo(5, "Súper amable, el negocio fue claro y sin sorpresas. Gracias."),
        new ComentarioDemo(4, "Buen vendedor, el vehículo estaba en muy buen estado. Solo demoró un poco con los papeles."),
        new ComentarioDemo(4, "Buena experiencia en general. Responde rápido y es amable."),
        new ComentarioDemo(4, "Todo bien con la compra, el precio fue justo. Recomendado."),
        new ComentarioDemo(4, "Cumplió con lo prometido. Pequeños detalles menores, pero nada grave."),
        new ComentarioDemo(4, "Persona seria y respetuosa. La entrega se hizo en el día acordado."),
        new ComentarioDemo(4, "Buen trato y buena comunicación. El carro tal como se describía."),
        new ComentarioDemo(4, "Muy buena atención, aunque tardó en responder algunos mensajes."),
        new ComentarioDemo(4, "Negocio claro y rápido. Lo recomiendo."),
        new ComentarioDemo(3, "El vehículo estaba bien, pero tenía un par de detalles que no mencionó en el aviso."),
        new ComentarioDemo(3, "Atención aceptable. El precio final fue más alto de lo esperado."),
        new ComentarioDemo(3, "Cumplió, aunque la comunicación fue lenta y llegó tarde a la cita."),
        new ComentarioDemo(3, "Experiencia regular: el carro es bueno, pero la entrega de papeles se demoró bastante."),
        new ComentarioDemo(2, "El estado del vehículo no era el que describía en el aviso. Esperaba algo mejor."),
        new ComentarioDemo(2, "Poca disposición para negociar y respuestas tardías. No fue una buena experiencia."),
        new ComentarioDemo(1, "No coincidió lo publicado con la realidad. No lo recomiendo."),
        new ComentarioDemo(5, "Gracias por la amabilidad y la honestidad. Un vendedor de los que da gusto encontrar."),
        new ComentarioDemo(5, "Muy buena experiencia, vehículo en excelentes condiciones y trato cordial.")
    };

    // ------------------------------------------------------------------
    // Fotos
    // ------------------------------------------------------------------

    /** Nombres de archivo de fotos de carros, de carro-01.jpg a carro-30.jpg. */
    public static final String[] FOTOS_CARRO = generarFotos("carro", 30);

    /** Nombres de archivo de fotos de motos, de moto-01.jpg a moto-12.jpg. */
    public static final String[] FOTOS_MOTO = generarFotos("moto", 12);

    /** Nombres de archivo de fotos de camionetas, de camioneta-01.jpg a camioneta-08.jpg. */
    public static final String[] FOTOS_CAMIONETA = generarFotos("camioneta", 8);

    /**
     * Devuelve las fotos disponibles para un tipo de vehículo.
     * @param tipo tipo de vehículo (CARRO, MOTOCICLETA o CAMIONETA)
     * @return arreglo con los nombres de archivo de las fotos
     * @throws IllegalArgumentException si el tipo no tiene fotos de demostración
     */
    public static String[] fotosDe(TipoVehiculo tipo) {
        if (tipo == TipoVehiculo.CARRO) {
            return FOTOS_CARRO;
        }
        if (tipo == TipoVehiculo.MOTOCICLETA) {
            return FOTOS_MOTO;
        }
        if (tipo == TipoVehiculo.CAMIONETA) {
            return FOTOS_CAMIONETA;
        }
        throw new IllegalArgumentException("Tipo de vehículo sin fotos de demostración: " + tipo);
    }
}
