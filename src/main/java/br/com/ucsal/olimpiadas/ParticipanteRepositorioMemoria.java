package br.com.ucsal.olimpiadas;

import java.util.ArrayList;
import java.util.List;

public class ParticipanteRepositorioMemoria implements ParticipanteRepositorio {

	private final List<Participante> participantes = new ArrayList<>();
	private long proximoId = 1;

	@Override
	public void salvar(Participante participante) {
		participante.setId(proximoId++);
		participantes.add(participante);
	}

	@Override
	public List<Participante> buscarTodos() {
		return participantes;
	}

	@Override
	public Participante buscarPorId(long id) {
		return participantes.stream()
				.filter(p -> p.getId() == id)
				.findFirst()
				.orElse(null);
	}

	@Override
	public long proximoId() {
		return proximoId;
	}
}
