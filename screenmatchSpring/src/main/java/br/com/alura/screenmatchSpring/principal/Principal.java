package br.com.alura.screenmatchSpring.principal;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import br.com.alura.screenmatchSpring.model.DadosSerie;
import br.com.alura.screenmatchSpring.model.DadosTemporadas;
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
    }
}
