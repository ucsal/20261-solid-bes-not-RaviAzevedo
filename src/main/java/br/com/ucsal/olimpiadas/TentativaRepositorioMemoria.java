package br.com.ucsal.olimpiadas;

import java.util.ArrayList;
import java.util.List;

public class TentativaRepositorioMemoria implements TentativaRepositorio {

	private final List<Tentativa> tentativas = new ArrayList<>();
	private long proximoId = 1;

	@Override
	public void salvar(Tentativa tentativa) {
		tentativa.setId(proximoId++);
		tentativas.add(tentativa);
	}

	@Override
	public List<Tentativa> buscarTodos() {
		return tentativas;
	}

	@Override
	public long proximoId() {
		return proximoId;
	}
}
