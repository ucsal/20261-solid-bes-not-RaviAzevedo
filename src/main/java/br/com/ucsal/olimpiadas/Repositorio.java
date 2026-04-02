package br.com.ucsal.olimpiadas;

import java.util.List;

public interface Repositorio<T> {
	void salvar(T entidade);
	List<T> buscarTodos();
	long proximoId();
}
