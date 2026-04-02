package br.com.ucsal.olimpiadas;

import java.util.Scanner;

public class App {

	private final ParticipanteRepositorio participanteRepo;
	private final ProvaRepositorio provaRepo;
	private final QuestaoRepositorio questaoRepo;
	private final TentativaRepositorio tentativaRepo;
	private final CalculadoraNota calculadora;
	private final TabuleiroExibidor tabuleiroExibidor;

	private final Scanner in = new Scanner(System.in);

	public App(ParticipanteRepositorio participanteRepo,
			   ProvaRepositorio provaRepo,
			   QuestaoRepositorio questaoRepo,
			   TentativaRepositorio tentativaRepo,
			   CalculadoraNota calculadora,
			   TabuleiroExibidor tabuleiroExibidor) {
		this.participanteRepo = participanteRepo;
		this.provaRepo = provaRepo;
		this.questaoRepo = questaoRepo;
		this.tentativaRepo = tentativaRepo;
		this.calculadora = calculadora;
		this.tabuleiroExibidor = tabuleiroExibidor;
	}

	public static void main(String[] args) {
		var participanteRepo = new ParticipanteRepositorioMemoria();
		var provaRepo = new ProvaRepositorioMemoria();
		var questaoRepo = new QuestaoRepositorioMemoria();
		var tentativaRepo = new TentativaRepositorioMemoria();
		var calculadora = new CalculadoraNotaSimples();
		var tabuleiroExibidor = new TabuleiroConsoleExibidor();

		var app = new App(participanteRepo, provaRepo, questaoRepo,
				tentativaRepo, calculadora, tabuleiroExibidor);

		app.seed();
		app.executar();
	}

	void executar() {
		while (true) {
			System.out.println("\n=== OLIMPÍADA DE QUESTÕES (V1) ===");
			System.out.println("1) Cadastrar participante");
			System.out.println("2) Cadastrar prova");
			System.out.println("3) Cadastrar questão (A–E) em uma prova");
			System.out.println("4) Aplicar prova (selecionar participante + prova)");
			System.out.println("5) Listar tentativas (resumo)");
			System.out.println("0) Sair");
			System.out.print("> ");

			switch (in.nextLine()) {
			case "1" -> cadastrarParticipante();
			case "2" -> cadastrarProva();
			case "3" -> cadastrarQuestao();
			case "4" -> aplicarProva();
			case "5" -> listarTentativas();
			case "0" -> {
				System.out.println("tchau");
				return;
			}
			default -> System.out.println("opção inválida");
			}
		}
	}

	void cadastrarParticipante() {
		System.out.print("Nome: ");
		var nome = in.nextLine();

		System.out.print("Email (opcional): ");
		var email = in.nextLine();

		if (nome == null || nome.isBlank()) {
			System.out.println("nome inválido");
			return;
		}

		var p = new Participante();
		p.setNome(nome);
		p.setEmail(email);

		participanteRepo.salvar(p);
		System.out.println("Participante cadastrado: " + p.getId());
	}

	void cadastrarProva() {
		System.out.print("Título da prova: ");
		var titulo = in.nextLine();

		if (titulo == null || titulo.isBlank()) {
			System.out.println("título inválido");
			return;
		}

		var prova = new Prova();
		prova.setTitulo(titulo);

		provaRepo.salvar(prova);
		System.out.println("Prova criada: " + prova.getId());
	}

	void cadastrarQuestao() {
		if (provaRepo.buscarTodos().isEmpty()) {
			System.out.println("não há provas cadastradas");
			return;
		}

		var provaId = escolherProva();
		if (provaId == null)
			return;

		System.out.println("Enunciado:");
		var enunciado = in.nextLine();

		var alternativas = new String[5];
		for (int i = 0; i < 5; i++) {
			char letra = (char) ('A' + i);
			System.out.print("Alternativa " + letra + ": ");
			alternativas[i] = letra + ") " + in.nextLine();
		}

		System.out.print("Alternativa correta (A–E): ");
		char correta;
		try {
			correta = Questao.normalizar(in.nextLine().trim().charAt(0));
		} catch (Exception e) {
			System.out.println("alternativa inválida");
			return;
		}

		var q = new Questao();
		q.setProvaId(provaId);
		q.setEnunciado(enunciado);
		q.setAlternativas(alternativas);
		q.setAlternativaCorreta(correta);

		questaoRepo.salvar(q);

		System.out.println("Questão cadastrada: " + q.getId() + " (na prova " + provaId + ")");
	}

	void aplicarProva() {
		if (participanteRepo.buscarTodos().isEmpty()) {
			System.out.println("cadastre participantes primeiro");
			return;
		}
		if (provaRepo.buscarTodos().isEmpty()) {
			System.out.println("cadastre provas primeiro");
			return;
		}

		var participanteId = escolherParticipante();
		if (participanteId == null)
			return;

		var provaId = escolherProva();
		if (provaId == null)
			return;

		var questoesDaProva = questaoRepo.buscarPorProvaId(provaId);

		if (questoesDaProva.isEmpty()) {
			System.out.println("esta prova não possui questões cadastradas");
			return;
		}

		var tentativa = new Tentativa();
		tentativa.setParticipanteId(participanteId);
		tentativa.setProvaId(provaId);

		System.out.println("\n--- Início da Prova ---");

		for (var q : questoesDaProva) {
			System.out.println("\nQuestão #" + q.getId());
			System.out.println(q.getEnunciado());

			System.out.println("Posição inicial:");
			tabuleiroExibidor.exibir(q.getFenInicial());

			for (var alt : q.getAlternativas()) {
			    System.out.println(alt);
			}

			System.out.print("Sua resposta (A–E): ");
			char marcada;
			try {
				marcada = Questao.normalizar(in.nextLine().trim().charAt(0));
			} catch (Exception e) {
				System.out.println("resposta inválida (marcando como errada)");
				marcada = 'X';
			}

			var r = new Resposta();
			r.setQuestaoId(q.getId());
			r.setAlternativaMarcada(marcada);
			r.setCorreta(q.isRespostaCorreta(marcada));

			tentativa.getRespostas().add(r);
		}

		tentativaRepo.salvar(tentativa);

		int nota = calculadora.calcular(tentativa);
		System.out.println("\n--- Fim da Prova ---");
		System.out.println("Nota (acertos): " + nota + " / " + tentativa.getRespostas().size());
	}

	void listarTentativas() {
		System.out.println("\n--- Tentativas ---");
		for (var t : tentativaRepo.buscarTodos()) {
			System.out.printf("#%d | participante=%d | prova=%d | nota=%d/%d%n", t.getId(), t.getParticipanteId(),
					t.getProvaId(), calculadora.calcular(t), t.getRespostas().size());
		}
	}

	Long escolherParticipante() {
		System.out.println("\nParticipantes:");
		for (var p : participanteRepo.buscarTodos()) {
			System.out.printf("  %d) %s%n", p.getId(), p.getNome());
		}
		System.out.print("Escolha o id do participante: ");

		try {
			long id = Long.parseLong(in.nextLine());
			if (participanteRepo.buscarPorId(id) == null) {
				System.out.println("id inválido");
				return null;
			}
			return id;
		} catch (Exception e) {
			System.out.println("entrada inválida");
			return null;
		}
	}

	Long escolherProva() {
		System.out.println("\nProvas:");
		for (var p : provaRepo.buscarTodos()) {
			System.out.printf("  %d) %s%n", p.getId(), p.getTitulo());
		}
		System.out.print("Escolha o id da prova: ");

		try {
			long id = Long.parseLong(in.nextLine());
			if (provaRepo.buscarPorId(id) == null) {
				System.out.println("id inválido");
				return null;
			}
			return id;
		} catch (Exception e) {
			System.out.println("entrada inválida");
			return null;
		}
	}

	void seed() {
		var prova = new Prova();
		prova.setTitulo("Olimpíada 2026 • Nível 1 • Prova A");
		provaRepo.salvar(prova);

		var q1 = new Questao();
		q1.setProvaId(prova.getId());

		q1.setEnunciado("""
				Questão 1 — Mate em 1.
				É a vez das brancas.
				Encontre o lance que dá mate imediatamente.
				""");

		q1.setFenInicial("6k1/5ppp/8/8/8/7Q/6PP/6K1 w - - 0 1");

		q1.setAlternativas(new String[] { "A) Qh7#", "B) Qf5#", "C) Qc8#", "D) Qh8#", "E) Qe6#" });

		q1.setAlternativaCorreta('C');

		questaoRepo.salvar(q1);
	}
}
