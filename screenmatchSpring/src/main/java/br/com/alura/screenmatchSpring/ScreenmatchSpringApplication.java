package br.com.alura.screenmatchSpring;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import br.com.alura.screenmatchSpring.model.DadosEpisodio;
import br.com.alura.screenmatchSpring.model.DadosSerie;
import br.com.alura.screenmatchSpring.service.ConsumoApi;
import br.com.alura.screenmatchSpring.service.ConverteDados;

@SpringBootApplication
public class ScreenmatchSpringApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchSpringApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ConsumoApi consumoApi = new ConsumoApi();
		ConverteDados conversor = new ConverteDados();
		String json;

		// Série
		json = consumoApi.obterDados("https://www.omdbapi.com/?t=gilmore+girls&apikey=6585022c");
		DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
		System.out.println(dados);

		// Episódio
		json = consumoApi.obterDados("https://omdbapi.com/?t=gilmore+girls&season=1&episode=2&apikey=6585022c");
		DadosEpisodio dadosEpisodio = conversor.obterDados(json, DadosEpisodio.class);
		System.out.println(dados);
	}
}