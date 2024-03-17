import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

import static io.restassured.RestAssured.given;
/**
 * Базовый класс для API тестов.
 * Предоставляет настройки и утилиты для выполнения HTTP запросов с использованием RestAssured.
 */
public class BaseTest {

    protected String accessToken;
    protected String registeredUsername;
    protected String registeredPassword;
    protected int productId;
    /**
     * Инициализирует базовые настройки для всех тестов.
     * Устанавливает базовый URI сервиса для отправки HTTP запросов.
     */
    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "http://9b142cdd34e.vps.myjino.ru:49268";
    }
    /**
     * Выполняет HTTP запрос к заданному эндпоинту с указанным методом, телом запроса и токеном аутентификации.
     * Автоматически проверяет, что ответ от сервера соответствует ожидаемому статусному коду.
     *
     * @param method Метод HTTP запроса ("GET", "POST", "PUT", "DELETE").
     * @param path Путь к эндпоинту, к которому будет направлен запрос.
     * @param body Тело запроса в формате JSON. Если тело запроса не требуется, передать {@code null}.
     * @param accessToken Токен аутентификации для доступа к защищенным ресурсам.
     *                    Если аутентификация не требуется, передать {@code null}.
     * @param expectedStatusCode Ожидаемый статусный код HTTP ответа, который будет проверен после выполнения запроса.
     * @return Ответ ({@link Response}) от сервера для дальнейшей обработки или проверки.
     * @throws IllegalArgumentException если указан неизвестный или неподдерживаемый HTTP метод.
     */
    protected Response performRequestAndVerify(String method, String path, String body, String accessToken, int expectedStatusCode) {
        RequestSpecification request = given()
                .header("Content-Type", "application/json")
                .log().all();

        if (accessToken != null) {
            request.header("Authorization", "Bearer " + accessToken);
        }

        if (body != null) {
            request.body(body);
        }

        Response response;
        switch (method) {
            case "GET":
                response = request.when().get(path);
                break;
            case "POST":
                response = request.when().post(path);
                break;
            case "PUT":
                response = request.when().put(path);
                break;
            case "DELETE":
                response = request.when().delete(path);
                break;
            default:
                throw new IllegalArgumentException("Unsupported method: " + method);
        }

        response.then().log().all().statusCode(expectedStatusCode);

        return response;
    }
}
