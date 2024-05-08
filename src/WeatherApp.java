import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

// busca dados climáticos da API - a lógica do backend é buscar os últimos dados climáticos da API externa e retornar
// para o usuário por meio da interface gráfica
public class WeatherApp {
    // buscar os dados da localização dada
    @SuppressWarnings("unchecked")
    public static JSONObject getWeatherData(String locationName) {
        // buscar as coordenadas do local usando a API de geolocalização
        JSONArray locationData = getLocationData(locationName);

        // extrair latitude o longitude
        assert locationData != null;
        JSONObject location = (JSONObject) locationData.getFirst();
//        System.out.println(location);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // construir a requisição à API com as coordenadas do local
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=America%2FSao_Paulo";

//        JSONObject weatherData = null;
        try {
            // chamar a API e obter a resposta
            HttpURLConnection conn = fetchApiResponse(urlString);

            // checar o status da resposta
            // 200  quer dizer que deu certo
//            assert conn != null;
            assert conn != null;
            if (conn.getResponseCode() != 200) {
                System.out.println("Erro: Não foi possível conectar à API (conn.getResponse)");
                return null;
            }

            // armazene os resultados do json
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }

            // fechar o scanner
            scanner.close();

            //fechar a conexão com a url
            conn.disconnect();

            // converter os dados
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            // recuperar dados a cada hora
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");
            //queremos os dados da hora atual
            // então precisamos do índice da hora atual
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);


            // recuperar a temperatura
            JSONArray temperaturData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperaturData.get(index);


            // recuperar o código de tempo
            JSONArray weathercode = (JSONArray) hourly.get("weather_code");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            // obter a humidade
            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            // obter a velocidade do vento
            JSONArray windspeedData = (JSONArray) hourly.get("wind_speed_10m");
            double windspeed = (double) windspeedData.get(index);

            // construir o objeto json de clima que acessaremos no frontend
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);

            return weatherData;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static JSONArray getLocationData(String locationName){
        // substitui espaçoes no nome do local por '+' para satisfazer o formato de requisição da API
        locationName = locationName.replaceAll(" ", "+");

        // montar a URL da API com os parâmetros do local
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name="+
                locationName + "&count=10&language=pt&format=json";


        try {
            // chama a API e obtém ums resposta
            HttpURLConnection conn = fetchApiResponse(urlString);

            // checa o status da resposta
            assert conn != null;
            if(conn.getResponseCode() != 200){
                System.out.println("Erro: não foi possível conectar à API");
                return null;
            } else {
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                while(scanner.hasNext()){
                    resultJson.append(scanner.nextLine());
                }

                // fecha o scanner
                scanner.close();

                // fecha a conexão com a URL
                conn.disconnect();

                // transforna a string JSON em um objeto JSON
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                // obtém os dados de localização que a API gerou pelo nome da localização
                return (JSONArray) resultsJsonObj.get("results");
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        // não encontrou ao local
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString) throws URISyntaxException{
        try{
            // tenta criar a conexão
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // configura o método de requisição para get
            conn.setRequestMethod("GET");

            // conectar à API
            conn.connect();
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // não conseguiu fechar a conexão
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();

        // iterar sobre a lista de tempo a ver qual bate com nosso tempo atual
        for(int i = 0; i < timeList.size(); i++){
            String time = (String) timeList.get(i);
            if(time.equalsIgnoreCase(currentTime)){
                // retorne o índice
                return i;
            }
        }

        return 0;
    }

    public static String getCurrentTime(){
        LocalDateTime currentDateTime = LocalDateTime.now();

        // formatar a data para 2023-09-02T00:00 (assim como está na API)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");


        // formata e imprime a hora e data atuais
        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;
    }

    private static String convertWeatherCode(long weathercode){
        String weatherCondition = "";
        if(weathercode == 0L){
            weatherCondition = "Limpo";
        } else if (weathercode <= 3L && weathercode > 0L){
            weatherCondition = "Nublado";
        } else if ((weathercode >= 51L && weathercode <= 67L)
                ||(weathercode >= 80L && weathercode <= 99L)) {
            weatherCondition = "Chuvoso";
        } else if (weathercode >= 71L && weathercode <= 77L){
            weatherCondition = "Neve";
        }

        return weatherCondition;
    }
}

