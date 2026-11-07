package co.edu.uniquindio.unimotor.ejb.demo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import co.edu.uniquindio.unimotor.entidades.TipoVehiculo;

/**
 * Reparte las fotos de demostración entre los vehículos de forma pareja. Por
 * cada modelo mantiene una cola barajada con las fotos de ese modelo; toma las
 * fotos de la cola y, cuando se agota, la baraja de nuevo. Si el modelo tiene
 * menos fotos de las pedidas, completa con fotos de la misma marca y tipo y
 * después con fotos del tipo. Un vehículo nunca recibe la misma foto dos veces.
 */
public class RepartidorFotos {

	private final Random random;

	private final Map<String, Deque<String>> colas = new HashMap<String, Deque<String>>();

	private final Map<String, Integer> usos = new LinkedHashMap<String, Integer>();

	/**
	 * Crea el repartidor.
	 *
	 * @param random generador de números aleatorios (con semilla fija para que el
	 *               reparto sea reproducible)
	 */
	public RepartidorFotos(Random random) {
		this.random = random;
	}

	/**
	 * Entrega las siguientes fotos de un modelo, todas distintas entre sí.
	 *
	 * @param marca    marca del vehículo
	 * @param modelo   nombre del modelo
	 * @param tipo     tipo de vehículo
	 * @param cantidad número de fotos pedidas
	 * @return lista de enlaces distintos; puede tener menos de {@code cantidad}
	 *         elementos (o ninguno) si no hay suficientes fotos
	 */
	public List<String> siguientes(String marca, String modelo, TipoVehiculo tipo, int cantidad) {
		List<String> resultado = new ArrayList<String>(cantidad);
		List<String> propias = new ArrayList<String>(urls(FotosDemo.de(marca, modelo)));
		if (!propias.isEmpty()) {
			String clave = (marca == null ? "" : marca.trim().toLowerCase(Locale.ROOT)) + "|"
					+ (modelo == null ? "" : modelo.trim().toLowerCase(Locale.ROOT));
			Deque<String> cola = colas.get(clave);
			if (cola == null) {
				cola = new ArrayDeque<String>();
				colas.put(clave, cola);
			}
			List<String> aplazadas = new ArrayList<String>();
			int tope = Math.min(cantidad, propias.size());
			while (resultado.size() < tope) {
				if (cola.isEmpty()) {
					List<String> lista = new ArrayList<String>(propias);
					Collections.shuffle(lista, random);
					cola.addAll(lista);
				}
				String foto = cola.pollFirst();
				if (resultado.contains(foto)) {
					aplazadas.add(foto);
				} else {
					resultado.add(foto);
				}
			}
			for (int i = aplazadas.size() - 1; i >= 0; i--) {
				cola.addFirst(aplazadas.get(i));
			}
		}
		if (resultado.size() < cantidad) {
			completar(resultado, FotosDemo.deMarcaYTipo(marca, tipo), cantidad);
		}
		if (resultado.size() < cantidad) {
			completar(resultado, FotosDemo.deTipo(tipo), cantidad);
		}
		for (String foto : resultado) {
			Integer actual = usos.get(foto);
			usos.put(foto, actual == null ? 1 : actual + 1);
		}
		return resultado;
	}

	/** Enlaces distintos de una lista de fotos, en orden. */
	private static Set<String> urls(List<FotoDemo> fotos) {
		Set<String> r = new LinkedHashSet<String>();
		for (FotoDemo f : fotos) {
			r.add(f.getUrl());
		}
		return r;
	}

	/** Agrega fotos de una fuente (en orden aleatorio) sin repetir las ya elegidas. */
	private void completar(List<String> resultado, List<FotoDemo> fuente, int cantidad) {
		List<String> candidatas = new ArrayList<String>(urls(fuente));
		candidatas.removeAll(resultado);
		Collections.shuffle(candidatas, random);
		for (String url : candidatas) {
			if (resultado.size() >= cantidad) {
				break;
			}
			resultado.add(url);
		}
	}

	/**
	 * Indica cuántas veces se ha entregado cada foto.
	 *
	 * @return copia del mapa enlace de foto &rarr; número de usos
	 */
	public Map<String, Integer> getUsos() {
		return new LinkedHashMap<String, Integer>(usos);
	}

}
