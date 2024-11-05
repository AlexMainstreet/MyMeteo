import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WeatherService {
    private static final String API_KEY = "c5e64c1a-302b-468e-bc5e-93f7f0b2c92d";
    private static final String API_URL = "https://api.weather.yandex.ru/v2/forecast?lat=%s&lon=%s&limit=%d";
    private static final Logger logger = Logger.getLogger(WeatherService.class.getName());

    private static final List<Double> temperatureHistory = new ArrayList<>(); // Список для хранения температур

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        double latitude = 0.0; // широта, принудительное обнуление 0.0
        double longitude = 0.0; // долгота, принудительное обнуление 0.0
        int limit = 0; // количество дней для получения средней температуры, принудительное обнуление 0

        // Ввод широты с проверкой
        while (true) {
            System.out.print("Введите широту/latitude (-90.0000 до 90.0000) через точку: ");
            String input = scanner.next().replace(",", ".");
            if (isDouble(input)) {
                latitude = Double.parseDouble(input);
                if (latitude >= -90 && latitude <= 90) {
                    break;
                } else {
                    System.out.println("Ошибка: широта должна быть в диапазоне от -90.0000 до 90.0000");
                }
            } else {
                System.out.println("Ошибка: Пожалуйста, введите действительное число с точкой");
            }
        }

        // Ввод долготы с проверкой
        while (true) {
            System.out.print("Введите долготу/longitude (-180.0000 до 180.0000) через точку: ");
            String input = scanner.next().replace(",", ".");
            if (isDouble(input)) {
                longitude = Double.parseDouble(input);
                if (longitude >= -180 && longitude <= 180) {
                    break;
                } else {
                    System.out.println("Ошибка: долгота должна быть в диапазоне от -180.0000 до 180.0000");
                }
            } else {
                System.out.println("Ошибка: Пожалуйста, введите действительное число с точкой");
            }
        }

        // Ввод количества дней с проверкой
        while (true) {
            System.out.print("Введите количество дней для средней температуры (целое число больше 1): ");
            if (scanner.hasNextInt()) {
                limit = scanner.nextInt();
                if (limit > 1) {
                    break; // Допускаем только значения больше 1
                } else {
                    System.out.println("Ошибка: для вычисления средней температуры требуется количество дней больше 1");
                }
            } else {
                System.out.println("Ошибка: Пожалуйста, введите целое число");
                scanner.next(); // Очищаем ввод
            }
        }

        getWeather(latitude, longitude, limit);
        scanner.close();
    }

    // Метод для проверки, является ли строка числом
    public static boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void getWeather(double lat, double lon, int limit) {
        try {
            String urlString = String.format(API_URL, lat, lon, limit);
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Yandex-API-Key", API_KEY);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Обработка JSON-ответа
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                System.out.println("Ответ от сервиса: " + jsonResponse); // Выводим весь ответ в формате JSON

                // Извлекаем текущую температуру из объекта fact
                double currentTemp = jsonResponse.getAsJsonObject("fact").get("temp").getAsDouble();
                temperatureHistory.add(currentTemp); // Сохраняем температуру в историю
                System.out.println("Текущая температура: " + currentTemp + "°C");

                // Вычисляем среднюю температуру
                System.out.println("Средняя температура за период (" + limit + " записей): " + calculateAverageTemperature() + "°C");

            } else {
                System.out.println("Ошибка в запросе: " + responseCode);
            }

        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "Ошибка в URI: ", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Произошла ошибка: ", e);
        }
    }

    // Метод для вычисления средней температуры
    public static double calculateAverageTemperature() {
        if (temperatureHistory.isEmpty()) {
            return 0.0;
        }
        double total = 0;
        for (double temp : temperatureHistory) {
            total += temp;
        }
        return total / temperatureHistory.size();
    }
}