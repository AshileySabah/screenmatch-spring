package br.com.alura.screenmatch.principal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

public class Principal {
    private Scanner scanner = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConverteDados conversorDados = new ConverteDados();
    private String json;

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";

    public void exibeMenu(){
        // consultar série
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = scanner.nextLine();

        json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversorDados.obterDados(json, DadosSerie.class);

        System.out.println(dados);

        // obter todos os episódios de cada temporada
        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i<=dados.totalTemporadas(); i++){
            json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") +"&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversorDados.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }

        temporadas.forEach(System.out::println);

        temporadas.forEach(temporada -> temporada.episodios().forEach(episodio -> System.out.println(episodio.titulo())));

        // obter os 5 melhores episódios
        System.out.println("Top 5 episódios");

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(temporada -> temporada.episodios().stream())
                .collect(Collectors.toList());

        dadosEpisodios.stream()
                .filter(episodio -> !episodio.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(temporada -> temporada.episodios().stream()
                .map(dadosEpisodio -> new Episodio(temporada.numero(), dadosEpisodio)))
                .collect(Collectors.toList());

        episodios.forEach(System.out::println);

        // consultar por nome do episódio
        System.out.println("Digite o nome do episódio que deseja encontrar:");
        String trechoTitulo = scanner.nextLine();
        
        // retornar o primeiro match encontrado
        List<Episodio> episodiosBuscados = episodios.stream()
                .filter(episodio -> episodio.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .collect(Collectors.toList());

        Optional<Episodio> primeiraReferenciaEpisodioBuscado = episodiosBuscados.stream().findFirst();
        if(primeiraReferenciaEpisodioBuscado.isPresent()) {
                System.out.println("Episódio encontrado!");
                System.out.println(
                        "Episódio: " + primeiraReferenciaEpisodioBuscado.get().getTitulo() +
                        " (Temporada " + primeiraReferenciaEpisodioBuscado.get().getTemporada() + ")"
                );
        } else {
                System.out.println("Episódio não encontrado.");
        }

        // retornar os matches restantes
        if(episodiosBuscados.size() > 1) {
                System.out.println("Existem outros episódios com esse nome, deseja conferir? (S - Sim, N - Não)");
                if(scanner.nextLine().equalsIgnoreCase("S")) {
                        episodiosBuscados.stream()
                                .filter(episodioBuscado -> !episodioBuscado.getTitulo().equalsIgnoreCase(primeiraReferenciaEpisodioBuscado.get().getTitulo()))
                                .forEach(episodioBuscado -> 
                                        System.out.println(
                                                "Episódio: " + episodioBuscado.getTitulo() +
                                                " (Temporada " + episodioBuscado.getTemporada() + ")"
                                        )
                                );
                }
        }
        
        // obter média de avaliações das temporadas
        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(episodio -> episodio.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada, Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(avaliacoesPorTemporada);

        // estatíticas
        DoubleSummaryStatistics estatisticas = episodios.stream()
                .filter(episodio -> episodio.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("Média:" + estatisticas.getAverage());
        System.out.println("Nota do pior avaliado:" + estatisticas.getMin());
        System.out.println("Nota do melhor avaliado:" + estatisticas.getMax());
        System.out.println("Quantidade:" + estatisticas.getCount());

        // obter episódios a partir de determinado ano
        System.out.println("A partir de que ano você deseja ver os episódios? ");
        var ano = scanner.nextInt();
        scanner.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        episodios.stream()
                .filter(episodio -> episodio.getDataLancamento() != null && episodio.getDataLancamento().isAfter(dataBusca))
                .forEach(episodio -> System.out.println(
                        "Temporada:  " + episodio.getTemporada() +
                        " Episódio: " + episodio.getTitulo() +
                        " Data lançamento: " + episodio.getDataLancamento().format(formatador)
                ));
    }
}