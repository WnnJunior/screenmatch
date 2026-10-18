
package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
public class Principal {

    private final Scanner leitura = new Scanner(System.in);
    private final ConsumoApi consumo = new ConsumoApi();
    private final ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=beb0cfa5";

    private final SerieRepository repository;

    private List<Serie> series = new ArrayList<>();

    public Principal(SerieRepository serieRepository) {
        this.repository = serieRepository;
    }

    public void exibeMenu() {
        var opcao = -1;

        while (opcao != 0) {
            var menu = """
        1 - Buscar séries
        2 - Buscar episódios
        3 - Listar séries buscadas
        4 - Buscar série por título
        5 - Buscar séries por ator
        6 - top 5 series
        8 - Filtrar séries por temporadas e avaliação
        9 - Buscar episódios por trecho
        10 - Top 5 episódios de uma série
        11 - Buscar episódios a partir de uma data
        0 - Sair
        """;

            System.out.println(menu);

            if (!leitura.hasNextInt()) {
                System.out.println("Digite uma opção numérica válida.");
                leitura.nextLine();
                continue;
            }

            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 8:
                    filtrarSeriesPorTemporadaEAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    topEpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosDepoisDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();

        if (dados == null || dados.titulo() == null) {
            System.out.println("Não foi possível encontrar a série.");
            return;
        }

        Serie serie = new Serie(dados);

        repository.save(serie);

        System.out.println("Série salva com sucesso!");
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca:");

        var nomeSerie = leitura.nextLine();

        var json = consumo.obterDados(
                ENDERECO + nomeSerie.replace(" ", "+") + API_KEY
        );

        DadosSerie dados = conversor.obterDados(
                json, DadosSerie.class
        );

        return dados;
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();

        if (series.isEmpty()) {
            return;
        }

        System.out.println("Escolha uma série pelo nome:");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie =
                repository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();

            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(
                        ENDERECO
                                + serieEncontrada.getTitulo().replace(" ", "+")
                                + "&season=" + i
                                + API_KEY
                );

                DadosTemporada dadosTemporada = conversor.obterDados(
                        json, DadosTemporada.class
                );

                temporadas.add(dadosTemporada);
            }

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);

            repository.save(serieEncontrada);

            System.out.println("Episódios salvos com sucesso!");

        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void listarSeriesBuscadas() {
        series = repository.findAll();

        if (series.isEmpty()) {
            System.out.println("Nenhuma série foi cadastrada ainda.");
            return;
        }

        series.stream()
                .sorted(Comparator.comparing(Serie::getTitulo))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome:");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serieBuscada =
                repository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBuscada.isPresent()) {
            System.out.println("Dados da série: " + serieBuscada.get());
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void buscarSeriesPorAtor() {
        System.out.println("Qual o nome do ator para a busca?");
        var nomeAtor = leitura.nextLine();

        System.out.println("Avaliações a partir de qual valor?");
        var avaliacao = leitura.nextDouble();
        leitura.nextLine();

        List<Serie> seriesEncontradas =
                repository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(
                        nomeAtor, avaliacao
                );

        System.out.println("Séries em que " + nomeAtor + " trabalhou:");

        if (seriesEncontradas.isEmpty()) {
            System.out.println("Nenhuma série encontrada com esses critérios.");
        } else {
            seriesEncontradas.forEach(s ->
                    System.out.println(
                            s.getTitulo() + " - Avaliação: " + s.getAvaliacao()
                    )
            );
        }
    }

    private void buscarTop5Series() {
        List<Serie> serieTop = repository.findTop5ByOrderByAvaliacaoDesc();

        if (serieTop.isEmpty()) {
            System.out.println("Nenhuma série cadastrada no banco de dados.");
            return;
        }

        System.out.println("Top 5 séries mais bem avaliadas:");

        serieTop.forEach(s ->
                System.out.println(
                        s.getTitulo() + " - Avaliação: " + s.getAvaliacao()
                )
        );
    }
    private void filtrarSeriesPorTemporadaEAvaliacao() {
        System.out.println("Filtrar séries com até quantas temporadas?");
        var totalTemporadas = leitura.nextInt();
        leitura.nextLine();

        System.out.println("Com avaliação a partir de qual valor?");
        var avaliacao = leitura.nextDouble();
        leitura.nextLine();

        List<Serie> filtroSeries =
                repository.seriesPorTemporadaEAvaliacao(
                        totalTemporadas,
                        avaliacao
                );

        if (filtroSeries.isEmpty()) {
            System.out.println("Nenhuma série encontrada com esses critérios.");
            return;
        }

        System.out.println("*** Séries filtradas ***");

        filtroSeries.forEach(s ->
                System.out.println(
                        s.getTitulo() + " - Avaliação: " + s.getAvaliacao()
                )
        );
    }
    private void buscarEpisodioPorTrecho() {
        System.out.println("Qual trecho do nome do episódio deseja buscar?");
        var trechoEpisodio = leitura.nextLine();

        List<Episodio> episodiosEncontrados =
                repository.episodiosPorTrecho(trechoEpisodio);

        if (episodiosEncontrados.isEmpty()) {
            System.out.println("Nenhum episódio encontrado.");
            return;
        }

        episodiosEncontrados.forEach(e ->
                System.out.printf(
                        "Série: %s | Temporada: %s | Episódio: %s | Título: %s%n",
                        e.getSerie().getTitulo(),
                        e.getTemporada(),
                        e.getNumeroEpisodio(),
                        e.getTitulo()
                )
        );
    }
    private void topEpisodiosPorSerie() {
        System.out.println("Digite o nome da série:");
        var nomeSerie = leitura.nextLine();

        var serieBusca = repository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isEmpty()) {
            System.out.println("Série não encontrada!");
            return;
        }

        Serie serie = serieBusca.get();

        List<Episodio> topEpisodios =
                repository.topEpisodiosPorSerie(
                        serie,
                        org.springframework.data.domain.PageRequest.of(0, 5)
                );

        if (topEpisodios.isEmpty()) {
            System.out.println("Essa série ainda não possui episódios cadastrados.");
            return;
        }

        System.out.println("Top 5 episódios de " + serie.getTitulo() + ":");

        topEpisodios.forEach(e ->
                System.out.printf(
                        "Temporada %d - Episódio %d - %s - Avaliação: %.1f%n",
                        e.getTemporada(),
                        e.getNumeroEpisodio(),
                        e.getTitulo(),
                        e.getAvaliacao()
                )
        );
    }
    private void buscarEpisodiosDepoisDeUmaData() {
        System.out.println("Digite o nome da série:");
        var nomeSerie = leitura.nextLine();

        var serieBusca =
                repository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isEmpty()) {
            System.out.println("Série não encontrada!");
            return;
        }

        Serie serie = serieBusca.get();

        System.out.println("Digite o ano limite de lançamento:");
        var anoLancamento = leitura.nextInt();
        leitura.nextLine();

        List<Episodio> episodiosAno =
                repository.episodiosPorSerieEAno(serie, anoLancamento);

        if (episodiosAno.isEmpty()) {
            System.out.println("Nenhum episódio encontrado a partir desse ano.");
            return;
        }

        episodiosAno.forEach(System.out::println);
    }


}
