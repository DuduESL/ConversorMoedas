import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final String API_URL = "https://open.er-api.com/v6/latest/";
    private static final Gson gson = new Gson();

    // Classe auxiliar para desserializar a resposta JSON
    private static class ExchangeRateResponse {
        public String result;
        @SerializedName("base_code")
        public String baseCode;
        public Map<String, Double> rates;
        public String error;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int escolha;

        do {
            exibirMenu();
            System.out.print("Escolha uma opção: ");
            escolha = scanner.nextInt();
            scanner.nextLine(); // Consumir a quebra de linha

            switch (escolha) {
                case 1:
                    realizarConversao(scanner, "BRL", "USD");
                    break;
                case 2:
                    realizarConversao(scanner, "BRL", "EUR");
                    break;
                case 3:
                    realizarConversao(scanner, "USD", "BRL");
                    break;
                case 4:
                    realizarConversao(scanner, "EUR", "BRL");
                    break;
                case 5:
                    realizarConversao(scanner, "USD", "EUR");
                    break;
                case 6:
                    realizarConversao(scanner, "EUR", "USD");
                    break;
                case 0:
                    System.out.println("Saindo do conversor.");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
            System.out.println(); // Adiciona uma linha em branco para melhor visualização
        } while (escolha != 0);

        scanner.close();
    }

    private static void exibirMenu() {
        System.out.println("--- Conversor de Moedas ---");
        System.out.println("1. Real (BRL) para Dólar Americano (USD)");
        System.out.println("2. Real (BRL) para Euro (EUR)");
        System.out.println("3. Dólar Americano (USD) para Real (BRL)");
        System.out.println("4. Euro (EUR) para Real (BRL)");
        System.out.println("5. Dólar Americano (USD) para Euro (EUR)");
        System.out.println("6. Euro (EUR) para Dólar Americano (USD)");
        System.out.println("0. Sair");
    }

    private static void realizarConversao(Scanner scanner, String moedaOrigem, String moedaDestino) {
        System.out.printf("Digite o valor em %s: ", moedaOrigem);
        double valorOrigem = scanner.nextDouble();
        scanner.nextLine(); // Consumir a quebra de linha

        try {
            double taxaCambio = obterTaxaCambio(moedaOrigem, moedaDestino);
            double valorDestino = valorOrigem * taxaCambio;
            System.out.printf("%.2f %s equivalem a %.2f %s%n", valorOrigem, moedaOrigem, valorDestino, moedaDestino);
        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao obter a taxa de câmbio: " + e.getMessage());
        }
    }

    private static double obterTaxaCambio(String moedaBase, String moedaDestino) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + moedaBase))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String responseBody = response.body();
            ExchangeRateResponse exchangeRateResponse = gson.fromJson(responseBody, ExchangeRateResponse.class);

            if (exchangeRateResponse.result.equals("success") && exchangeRateResponse.rates != null && exchangeRateResponse.rates.containsKey(moedaDestino)) {
                return exchangeRateResponse.rates.get(moedaDestino);
            } else if (exchangeRateResponse.error != null) {
                throw new IOException("Erro na API: " + exchangeRateResponse.error);
            } else {
                throw new IOException("Taxa de câmbio não encontrada para " + moedaDestino);
            }
        } else {
            throw new IOException("Erro na requisição HTTP: " + response.statusCode());
        }
    }
}