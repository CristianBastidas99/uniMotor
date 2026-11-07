package co.edu.uniquindio.unimotor.ejb.demo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import co.edu.uniquindio.unimotor.entidades.TipoVehiculo;

/**
 * Reparte las fotos de demostración entre los vehículos de forma pareja. Por
 * cada tipo de vehículo mantiene una cola barajada con las fotos del catálogo;
 * toma las fotos de la cola y, cuando se agota, la baraja de nuevo. Así todas
 * las fotos se usan casi el mismo número de veces y un vehículo nunca recibe la
 * misma foto dos veces.
 */
public class RepartidorFotos {

	private final Random random;

	private final Map<TipoVehiculo, Deque<String>> colas = new EnumMap<TipoVehiculo, Deque<String>>(
			TipoVehiculo.class);

	private final Map<String, Integer> usos = new LinkedHashMap<String, Integer>();

	/**
	 * Crea el repartidor.
	 *
	 * @param random generador de números aleatorios (con semilla fija para que el
	 *               reparto sea reproducible)
	 */
	public RepartidorFotos(Random random) {
		this.random = random;
		TipoVehiculo[] tipos = { TipoVehiculo.CARRO, TipoVehiculo.MOTOCICLETA, TipoVehiculo.CAMIONETA };
		for (TipoVehiculo tipo : tipos) {
			String[] fotos = CatalogoDemo.fotosDe(tipo);
			if (fotos != null) {
				for (String foto : fotos) {
					usos.put(foto, 0);
				}
			}
		}
	}

	/**
	 * Entrega las siguientes fotos de un tipo de vehículo, todas distintas entre
	 * sí.
	 *
	 * @param tipo     tipo de vehículo
	 * @param cantidad número de fotos pedidas
	 * @return lista con {@code cantidad} nombres de archivo distintos
	 * @throws IllegalArgumentException si el catálogo del tipo tiene menos fotos
	 *                                  que las pedidas
	 */
	public List<String> siguientes(TipoVehiculo tipo, int cantidad) {
		String[] catalogo = CatalogoDemo.fotosDe(tipo);
		if (catalogo == null || catalogo.length < cantidad) {
			throw new IllegalArgumentException("No hay suficientes fotos para el tipo " + tipo);
		}
		Deque<String> cola = colas.get(tipo);
		if (cola == null) {
			cola = new ArrayDeque<String>();
			colas.put(tipo, cola);
		}
		List<String> resultado = new ArrayList<String>(cantidad);
		List<String> aplazadas = new ArrayList<String>();
		while (resultado.size() < cantidad) {
			if (cola.isEmpty()) {
				rebarajar(cola, catalogo);
			}
			String foto = cola.pollFirst();
			if (resultado.contains(foto)) {
				aplazadas.add(foto);
			} else {
				resultado.add(foto);
				Integer actual = usos.get(foto);
				usos.put(foto, actual == null ? 1 : actual + 1);
			}
		}
		for (int i = aplazadas.size() - 1; i >= 0; i--) {
			cola.addFirst(aplazadas.get(i));
		}
		return resultado;
	}

	/**
	 * Rellena la cola con las fotos del catálogo en orden aleatorio.
	 *
	 * @param cola     cola a rellenar
	 * @param catalogo fotos del tipo
	 */
	private void rebarajar(Deque<String> cola, String[] catalogo) {
		List<String> lista = new ArrayList<String>(Arrays.asList(catalogo));
		Collections.shuffle(lista, random);
		cola.addAll(lista);
	}

	/**
	 * Indica cuántas veces se ha entregado cada foto.
	 *
	 * @return copia del mapa nombre de foto &rarr; número de usos
	 */
	public Map<String, Integer> getUsos() {
		return new LinkedHashMap<String, Integer>(usos);
	}

}
