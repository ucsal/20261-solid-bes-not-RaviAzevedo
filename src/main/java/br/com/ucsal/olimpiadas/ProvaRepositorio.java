package br.com.ucsal.olimpiadas;

public interface ProvaRepositorio extends Repositorio<Prova> {
	Prova buscarPorId(long id);
}
