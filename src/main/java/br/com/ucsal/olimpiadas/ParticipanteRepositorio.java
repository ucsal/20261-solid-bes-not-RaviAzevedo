package br.com.ucsal.olimpiadas;

public interface ParticipanteRepositorio extends Repositorio<Participante> {
	Participante buscarPorId(long id);
}
