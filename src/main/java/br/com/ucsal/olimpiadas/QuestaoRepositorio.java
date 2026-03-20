package br.com.ucsal.olimpiadas;

import java.util.List;

public interface QuestaoRepositorio extends Repositorio<Questao> {
	List<Questao> buscarPorProvaId(long provaId);
}
