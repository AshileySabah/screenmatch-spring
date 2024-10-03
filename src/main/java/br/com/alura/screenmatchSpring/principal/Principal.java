package br.com.alura.screenmatchSpring.principal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import br.com.alura.screenmatchSpring.model.DadosEpisodio;
import br.com.alura.screenmatchSpring.model.DadosSerie;
import br.com.alura.screenmatchSpring.model.DadosTemporadas;
import br.com.alura.screenmatchSpring.model.Episodio;
import br.com.alura.screenmatchSpring.service.ConsumoApi;
import br.com.alura.screenmatchSpring.service.ConverteDados;

public class Principal {
        private Scanner scanner = new Scanner(System.in);
        private final String ENDERECO = "https://omdbapi.com/?t=";
        private final String APIKEY = "&apikey=6585022c";
        private ConsumoApi consumoApi = new ConsumoApi();
        private ConverteDados conversor = new ConverteDados();
        private String json;


    public void exibeMenu() {
        System.out.println("Digite o nome da série:");
        var nomeSerie = scanner.nextLine();
        json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + APIKEY);
        DadosSerie dadosSerie = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dadosSerie);

        List<DadosTemporadas> temporadas = new ArrayList<DadosTemporadas>();
        for (int i = 0; i <= dadosSerie.totalTemporadas(); i++) {
        	json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + APIKEY);
        	DadosTemporadas dadosTemporadas = conversor.obterDados(json, DadosTemporadas.class);
        	temporadas.add(dadosTemporadas);
        }
        temporadas.forEach(System.out::println);

        temporadas.forEach(temporada -> temporada.episodios().forEach(episodio -> System.out.println(episodio.titulo())));

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(temporada -> temporada.episodios().stream())
                .collect(Collectors.toList());

        System.out.println("Top 5 episódios");
        dadosEpisodios.stream()
                .filter(episodio -> !episodio.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(temporada -> temporada.episodios().stream().map(dadosEpisodio -> new Episodio(temporada.numero(), dadosEpisodio)))
                .collect(Collectors.toList());
        episodios.forEach(System.out::println);

        System.out.println("A partir de que ano você deseja ver os episódios?");
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
