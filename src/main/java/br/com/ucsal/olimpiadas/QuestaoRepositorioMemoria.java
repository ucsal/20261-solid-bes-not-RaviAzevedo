package br.com.ucsal.olimpiadas;

import java.util.ArrayList;
import java.util.List;

public class QuestaoRepositorioMemoria implements QuestaoRepositorio {

	private final List<Questao> questoes = new ArrayList<>();
	private long proximoId = 1;

	@Override
	public void salvar(Questao questao) {
		questao.setId(proximoId++);
		questoes.add(questao);
	}

	@Override
	public List<Questao> buscarTodos() {
		return questoes;
	}

	@Override
	public List<Questao> buscarPorProvaId(long provaId) {
		return questoes.stream()
				.filter(q -> q.getProvaId() == provaId)
				.toList();
	}

	@Override
	public long proximoId() {
		return proximoId;
	}
}
