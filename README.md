# Olimpiada de Xadrez - Refatoracao SOLID

## Sobre o projeto

Sistema de quiz de xadrez onde participantes respondem questoes de multipla escolha (A-E) sobre posicoes de tabuleiro em notacao FEN.

## Mudancas realizadas

### Problema do codigo original

A classe `App.java` concentrava todas as responsabilidades do sistema. Ela tinha:

- 4 listas estaticas (`participantes`, `provas`, `questoes`, `tentativas`) fazendo o papel de banco de dados
- 4 contadores estaticos de ID (`proximoParticipanteId`, `proximaProvaId`, etc.)
- Todos os metodos eram `static`
- O metodo `calcularNota()` ficava dentro da App
- O metodo `imprimirTabuleiroFen()` ficava dentro da App
- A busca por ID era feita inline com `stream().anyMatch()` espalhada pelo codigo

Ou seja, se eu precisasse mudar a forma de armazenar dados, calcular nota ou exibir o tabuleiro, teria que mexer direto na `App.java`, arriscando quebrar tudo.

### O que foi criado

Foram criados 13 arquivos novos alem dos que ja existiam:

**Interfaces:**
- `Repositorio.java` - interface generica com `salvar()`, `buscarTodos()` e `proximoId()`
- `ParticipanteRepositorio.java` - estende Repositorio, adiciona `buscarPorId()`
- `ProvaRepositorio.java` - estende Repositorio, adiciona `buscarPorId()`
- `QuestaoRepositorio.java` - estende Repositorio, adiciona `buscarPorProvaId()`
- `TentativaRepositorio.java` - estende Repositorio (sem metodo extra)
- `CalculadoraNota.java` - interface com metodo `calcular(Tentativa)`
- `TabuleiroExibidor.java` - interface com metodo `exibir(String fen)`

**Implementacoes:**
- `ParticipanteRepositorioMemoria.java` - armazena participantes em ArrayList
- `ProvaRepositorioMemoria.java` - armazena provas em ArrayList
- `QuestaoRepositorioMemoria.java` - armazena questoes em ArrayList, filtra por provaId
- `TentativaRepositorioMemoria.java` - armazena tentativas em ArrayList
- `CalculadoraNotaSimples.java` - conta acertos percorrendo a lista de respostas
- `TabuleiroConsoleExibidor.java` - imprime tabuleiro FEN no console (mesma logica do original)

### O que mudou na App.java

**Antes:**
```java
public class App {
    static long proximoParticipanteId = 1;
    static final List<Participante> participantes = new ArrayList<>();
    // ... tudo estatico, tudo junto

    public static void main(String[] args) {
        seed();
        // menu direto no main
    }

    static void cadastrarParticipante() {
        p.setId(proximoParticipanteId++);
        participantes.add(p);  // acesso direto a lista
    }

    public static int calcularNota(Tentativa t) { ... }
    static void imprimirTabuleiroFen(String fen) { ... }
}
```

**Depois:**
```java
public class App {
    private final ParticipanteRepositorio participanteRepo;
    private final ProvaRepositorio provaRepo;
    private final QuestaoRepositorio questaoRepo;
    private final TentativaRepositorio tentativaRepo;
    private final CalculadoraNota calculadora;
    private final TabuleiroExibidor tabuleiroExibidor;

    public App(ParticipanteRepositorio participanteRepo, ...) {
        this.participanteRepo = participanteRepo;
        // dependencias injetadas pelo construtor
    }

    public static void main(String[] args) {
        var participanteRepo = new ParticipanteRepositorioMemoria();
        // instancia as implementacoes e passa pro construtor
        var app = new App(participanteRepo, ...);
        app.seed();
        app.executar();
    }

    void cadastrarParticipante() {
        participanteRepo.salvar(p);  // usa o repositorio
    }
}
```

As principais mudancas concretas:
- Removidas as 4 listas estaticas e os 4 contadores de ID da App
- Todos os metodos deixaram de ser `static`
- `calcularNota()` foi extraido pra `CalculadoraNotaSimples`
- `imprimirTabuleiroFen()` foi extraido pra `TabuleiroConsoleExibidor`
- `participantes.add(p)` virou `participanteRepo.salvar(p)`
- `questoes.stream().filter(...)` virou `questaoRepo.buscarPorProvaId(provaId)`
- `participantes.stream().anyMatch(...)` virou `participanteRepo.buscarPorId(id) == null`
- O loop do menu foi extraido pro metodo `executar()`

### Onde cada principio SOLID foi aplicado

**S - Responsabilidade Unica:** cada classe passou a ter uma unica funcao. Antes a App fazia persistencia + calculo + exibicao + menu. Agora cada repositorio cuida do seu armazenamento, a CalculadoraNotaSimples cuida do calculo e o TabuleiroConsoleExibidor cuida da exibicao.

**O - Aberto/Fechado:** pra trocar o armazenamento de memoria pra banco de dados, basta criar uma nova classe tipo `ParticipanteRepositorioBanco` implementando `ParticipanteRepositorio`. Nao precisa alterar a App nem nenhuma classe existente.

**L - Substituicao de Liskov:** qualquer implementacao pode substituir outra sem quebrar o sistema. Se trocar `CalculadoraNotaSimples` por uma `CalculadoraNotaPonderada`, o programa continua funcionando normalmente porque a App so conhece a interface `CalculadoraNota`.

**I - Segregacao de Interfaces:** as interfaces sao pequenas e focadas. `CalculadoraNota` so tem `calcular()`, `TabuleiroExibidor` so tem `exibir()`. A interface `Repositorio<T>` tem o basico e cada repositorio especifico adiciona so o que precisa (ex: `QuestaoRepositorio` adiciona `buscarPorProvaId()`).

**D - Inversao de Dependencia:** a App depende das interfaces, nao das classes concretas. So o metodo `main()` conhece as implementacoes (`ParticipanteRepositorioMemoria`, `CalculadoraNotaSimples`, etc). O resto do codigo usa as interfaces.

## Estrutura final

```
src/main/java/br/com/ucsal/olimpiadas/
├── App.java                            -> classe principal (menu)
├── Participante.java                   -> modelo (sem alteracao)
├── Prova.java                          -> modelo (sem alteracao)
├── Questao.java                        -> modelo (sem alteracao)
├── Resposta.java                       -> modelo (sem alteracao)
├── Tentativa.java                      -> modelo (sem alteracao)
├── Repositorio.java                    -> interface generica [NOVO]
├── ParticipanteRepositorio.java        -> interface [NOVO]
├── ParticipanteRepositorioMemoria.java -> implementacao [NOVO]
├── ProvaRepositorio.java               -> interface [NOVO]
├── ProvaRepositorioMemoria.java        -> implementacao [NOVO]
├── QuestaoRepositorio.java             -> interface [NOVO]
├── QuestaoRepositorioMemoria.java      -> implementacao [NOVO]
├── TentativaRepositorio.java           -> interface [NOVO]
├── TentativaRepositorioMemoria.java    -> implementacao [NOVO]
├── CalculadoraNota.java                -> interface [NOVO]
├── CalculadoraNotaSimples.java         -> implementacao [NOVO]
├── TabuleiroExibidor.java              -> interface [NOVO]
└── TabuleiroConsoleExibidor.java       -> implementacao [NOVO]
```

## Comportamento

Todas as funcionalidades originais foram mantidas sem alteracao:
- Cadastro de participantes
- Cadastro de provas
- Cadastro de questoes com 5 alternativas (A-E)
- Aplicacao de prova com exibicao do tabuleiro FEN
- Listagem de tentativas com nota
- Dados pre-carregados (seed) com questao de mate em 1

Nenhum framework externo foi adicionado. So Java puro + JUnit (que ja existia).
