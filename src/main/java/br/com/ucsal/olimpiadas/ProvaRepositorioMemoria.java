package br.com.ucsal.olimpiadas;

import java.util.ArrayList;
import java.util.List;

public class ProvaRepositorioMemoria implements ProvaRepositorio {

	private final List<Prova> provas = new ArrayList<>();
	private long proximoId = 1;

	@Override
	public void salvar(Prova prova) {
		prova.setId(proximoId++);
		provas.add(prova);
	}

	@Override
	public List<Prova> buscarTodos() {
		return provas;
	}

	@Override
	public Prova buscarPorId(long id) {
		return provas.stream()
				.filter(p -> p.getId() == id)
				.findFirst()
				.orElse(null);
	}

	@Override
	public long proximoId() {
		return proximoId;
	}
}
