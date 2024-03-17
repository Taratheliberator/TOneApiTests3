/**
 * Класс ApiTests содержит набор тестовых сценариев для проверки функциональности API.
 * Включает тесты на регистрацию и аутентификацию пользователя, добавление и удаление продуктов в/из корзины,
 * получение информации о продуктах и корзине, а также обработку запросов с недопустимыми методами или без аутентификации.

 * Тесты используют общие данные, такие как accessToken, имя и пароль зарегистрированного пользователя,
 * а также идентификаторы продуктов, для выполнения различных операций API. Данные инициализируются и используются
 * по мере необходимости в разных тестах для создания реалистичных тестовых сценариев, отражающих типичное взаимодействие с API.
 *
 * @author Винер Гиндуллин
 * @version 1.0
 * @since 17.03.2024
 */
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

public class ApiTests extends BaseTest {
    /**
     * Тестирование процесса регистрации пользователя.
     * Ожидается успешная регистрация с получением статуса ответа 201.
     */
    @Test(priority = 1)

    public void UserRegistrationTest() {
        registeredUsername = "user" + Instant.now().getEpochSecond();
        registeredPassword = "password";
        // Формируем JSON объект для тела запроса
        String requestBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", registeredUsername, registeredPassword);

        performRequestAndVerify("POST", "/register", requestBody, null, 201);
    }

    /**
     * Тестирование процесса аутентификации пользователя.
     * Ожидается успешная аутентификация с получением токена доступа и статуса ответа 200.
     */
    @Test(priority = 2)

    public void authenticateUser() {
        // Формируем JSON объект для тела запроса аутентификации
        String requestBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", registeredUsername, registeredPassword);

        // Отправляем POST запрос для аутентификации пользователя и получаем токен доступа
        Response response = performRequestAndVerify("POST", "/login", requestBody, null, 200);

        // Проверяем, что токен действительно получен и сохраняем его
        accessToken = response.path("access_token");
        assertNotNull(accessToken, "Access token should not be null");
    }

    /**
     * Тестирует получение списка продуктов и извлекает идентификатор первого продукта из списка.
     * Метод выполняет GET запрос к эндпоинту "/products", проверяет успешный ответ от сервера (200 OK),
     * убеждается в наличии непустого списка продуктов в теле ответа, и проверяет корректность структуры данных первого продукта в списке,
     * включая его идентификатор (id), имя (name), категорию (category), цену (price) и скидку (discount).
     * Идентификатор первого продукта в списке сохраняется для использования в последующих тестах, что позволяет
     * тестировать операции, требующие наличия существующего идентификатора продукта.
     *
     * @throws AssertionError если ответ от сервера не соответствует ожидаемому (например, если тело ответа пустое,
     *                        список продуктов отсутствует или не содержит ни одного продукта, или если структура данных продукта
     *                        не соответствует ожидаемой), или если извлеченный идентификатор продукта не является положительным числом.
     */
    @Test(priority = 3)

    public void GetListOfProductsTestAndExtractProductId() {
        // Выполняем GET запрос для получения списка продуктов
        Response response = performRequestAndVerify("GET", "/products", null, null, 200);

        // Убеждаемся, что тело ответа не пустое и содержит список продуктов
        assertNotNull(response.getBody(), "Response body should not be null");

        // Проверяем, что в ответе присутствует список продуктов и он не пуст
        assertFalse(response.jsonPath().getList("").isEmpty(), "Products list should not be empty");

        // Проверяем структуру и типы данных первого продукта в списке
        response.then().body("[0].id", instanceOf(Number.class)) // ID продукта должно быть числом
                .body("[0].name", instanceOf(String.class)) // Название продукта должно быть строкой
                .body("[0].category", instanceOf(String.class)) // Категория продукта должна быть строкой
                .body("[0].price", instanceOf(Number.class)) // Цена продукта должна быть числом
                .body("[0].discount", instanceOf(Number.class)); // Скидка продукта должна быть числом

        // Извлекаем и сохраняем productId из ответа API для использования в последующих тестах
        productId = response.then().extract().jsonPath().getInt("[0].id");

        // Проверяем, что извлеченный productId является положительным числом
        assertTrue(productId > 0, "Product ID should be positive");
    }

    /**
     * Тест добавления нового продукта через API.
     * Этот тест формирует JSON объект для тела запроса и отправляет POST-запрос на эндпоинт "/products".
     * Тест проверяет, что запрос на добавление нового продукта обрабатывается корректно,
     * и в ответ сервер возвращает статус код 201, что означает успешное создание ресурса.
     */
    @Test(priority = 4)

    public void AddNewProductTest() {

        // Формируем JSON объект для тела запроса на добавление нового продукта
        String requestBody = "{\"name\":\"New Product\",\"category\":\"Electronics\",\"price\":12.99,\"discount\":5}";
        performRequestAndVerify("POST", "/products", requestBody, accessToken, 201);

    }

    /**
     * Тестирование попытки добавления нового продукта с методом, который не разрешен API.
     * Тест формирует JSON объект для тела запроса и пытается отправить POST-запрос на эндпоинт "/products",
     * но без предоставления токена доступа. Это имитирует ситуацию, когда использование метода недопустимо.
     * * Ожидается, что API вернет статус код 405, указывающий на то, что метод запроса не разрешен.
     */
    @Test(priority = 5)

    public void AddNewProductWithNotAllowedMethodTest() {
        String requestBody = "{\"name\":\"New Product\",\"category\":\"Electronics\",\"price\":12.99,\"discount\":5}";
        performRequestAndVerify("POST", "/products", requestBody, null, 405);
    }

    /**
     * Тестирование получения информации о продукте по его ID.
     * Метод выполняет GET запрос к API для получения данных о конкретном продукте,
     * используя сохраненный ранее productId. Проверяется, что ответ содержит корректные
     * данные о продукте, включая его ID, имя, категорию, цену и скидку.
     * Все эти поля должны соответствовать ожидаемым типам данных.
     * Ожидается, что сервер вернет статусный код 200 (OK) и корректную информацию о продукте.
     */
    @Test(priority = 6)

    public void GetProductInformationTest() {
        // Формируем путь для запроса
        String path = "/products/" + productId;

        // Выполняем запрос и проверяем ответ
        Response response = performRequestAndVerify("GET", path, null, null, 200);

        // Добавляем проверки для структуры и типов данных возвращаемого продукта
        response.then().body("[0].id", instanceOf(Number.class)) // ID продукта должно быть числом
                .body("[0].name", instanceOf(String.class)) // Название продукта должно быть строкой
                .body("[0].category", instanceOf(String.class)) // Категория продукта должна быть строкой
                .body("[0].price", instanceOf(Number.class)) // Цена продукта должна быть числом
                .body("[0].discount", instanceOf(Number.class)); // Скидка продукта должна быть числом
    }

    /**
     * Тестирование ответа API на запрос информации о несуществующем продукте.
     * Этот тест пытается получить информацию о продукте, используя ID, который
     * гарантированно не существует в базе данных. Проверяется, что API корректно
     * обрабатывает такой запрос, возвращая статусный код 404 (Not Found), указывая на то,
     * что запрашиваемый ресурс не был найден.
     */
    @Test(priority = 7)

    public void GetNonexistentProductInformationTest() {
        int nonexistentProductId = 99999; // значение, гарантированно отсутствующее в базе
        String path = "/products/" + nonexistentProductId;
        performRequestAndVerify("GET", path, null, null, 404); // Проверяем, что сервер возвращает статус 404 Not Found
    }

    /**
     * Тестирование обновления информации о продукте.
     * Метод отправляет PUT запрос с обновленными данными продукта на эндпоинт "/products/{productId}".
     * Ожидается, что API успешно обработает запрос и вернет статусный код 200 (OK),
     * подтверждая успешное обновление информации о продукте. Данные для обновления
     * включают новое имя, категорию, цену и скидку продукта.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого или
     *                        если обновление информации о продукте не происходит.
     */
    @Test(priority = 8)

    public void UpdateProductInformationTest() {
        String requestBody = "{\"name\":\"Updated Product Name\",\"category\":\"Electronics\",\"price\":15.99,\"discount\":8}";
        performRequestAndVerify("PUT", "/products/" + productId, requestBody, accessToken, 200);
    }

    /**
     * Тестирование попытки обновления информации о продукте с использованием неразрешенного метода.
     * Метод отправляет PUT запрос для обновления продукта без необходимого токена аутентификации,
     * что не разрешено API. API должно ответить с кодом статуса 405 (Method Not Allowed),
     * указывая на то, что хотя метод (PUT) может быть допустим в целом, его использование
     * не разрешено без аутентификации или для конкретного запрашиваемого ресурса.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого 405
     */
    @Test(priority = 9)

    public void UpdateProductWithNotAllowedMethodTest() {
        String requestBody = "{\"name\":\"Updated Product Name\",\"category\":\"Electronics\",\"price\":15.99,\"discount\":8}";
        performRequestAndVerify("PUT", "/products/" + productId, requestBody, null, 405);
    }

    /**
     * Тестирование удаления продукта через API.
     * Метод отправляет DELETE запрос на эндпоинт "/products/{productId}" с использованием
     * аутентификационного токена для удаления продукта, указанного переменной productId.
     * Ожидается, что API успешно обработает запрос, удалит продукт и вернет
     * статусный код 200 (OK), подтверждая успешное выполнение операции удаления.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого 200
     */
    @Test(priority = 10)

    public void DeleteProductTest() {
        performRequestAndVerify("DELETE", "/products/" + productId, null, accessToken, 200);
    }

    /**
     * Тестирование попытки удаления продукта с использованием неразрешённого метода.
     * Этот тест выполняет DELETE запрос на эндпоинт "/products/{productId}" без аутентификационного токена,
     * что предполагается быть недопустимым в данном контексте API. Ожидается, что сервер отклонит запрос,
     * возвращая статусный код 405 (Method Not Allowed), указывая на то, что выполнение
     * операции удаления не разрешено без аутентификации или данное действие в целом недопустимо.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого 405,
     *                        что указывало бы на некорректную обработку запроса со стороны API.
     */
    @Test(priority = 11)

    public void DeleteProductWithNotAllowedMethodTest() {
        performRequestAndVerify("DELETE", "/products/" + productId, null, null, 405);
    }

    /**
     * Тестирование получения информации о корзине покупок.
     * Метод выполняет аутентификацию пользователя и отправляет GET запрос на эндпоинт "/cart",
     * ожидая получить информацию о содержимом корзины пользователя включая общую стоимость и скидку.
     * Проверяется, что ответ содержит корректные поля total_price и total_discount с числовыми значениями,
     * а также проверяется структура и типы данных каждого продукта в корзине, подтверждая наличие товаров,
     * их идентификаторы, названия, категории, цены и скидки.
     *
     * @throws AssertionError если ответ API не содержит ожидаемую структуру данных или типы данных
     *                        не соответствуют ожидаемым.
     */
    @Test(priority = 14)

    public void GetShoppingCartTest() {
        authenticateUser();
        Response response = performRequestAndVerify("GET", "/cart", null, accessToken, 200);

        // Проверяем, что ответ содержит поля total_price и total_discount с числовыми значениями
        response.then()
                .body("total_price", instanceOf(Number.class))
                .body("total_discount", instanceOf(Number.class));

        // Проверяем структуру и типы данных для каждого продукта в списке "cart"

        response.then().body("cart.size()", greaterThan(0)) // Проверяем, что в корзине есть товары
                .body("cart.id", everyItem(instanceOf(Number.class))) // Каждый id должен быть числом
                .body("cart.name", everyItem(instanceOf(String.class))) // Каждое имя должно быть строкой
                .body("cart.category", everyItem(instanceOf(String.class))) // Каждая категория должна быть строкой
                .body("cart.price", everyItem(instanceOf(Number.class))) // Каждая цена должна быть числом
                .body("cart.discount", everyItem(instanceOf(Number.class))); // Каждая скидка должна быть числом
    }

    /**
     * Тестирование попытки получения информации о корзине покупок без аутентификации.
     * Метод отправляет GET запрос на эндпоинт "/cart" без аутентификационного токена,
     * ожидая, что доступ к содержимому корзины будет ограничен для неавторизованных пользователей.
     * Ожидается, что API вернет статусный код 401 (Unauthorized), указывая на то, что
     * доступ к запрашиваемой информации требует предварительной аутентификации пользователя.
     */
    @Test(priority = 15)

    public void GetShoppingCartWithoutAuthorizationTest() {
        performRequestAndVerify("GET", "/cart", null, null, 401);
    }

    /**
     * Тестирование добавления продукта в корзину покупок.
     * Метод сначала аутентифицирует пользователя, затем создает JSON тело запроса,
     * содержащее идентификатор продукта и количество добавляемых единиц этого продукта.
     * После этого отправляется POST запрос на эндпоинт "/cart" с указанным телом запроса и аутентификационным токеном.
     * Ожидается, что API успешно обработает запрос, добавит указанный продукт в корзину пользователя
     * и вернет статусный код 201 (Created), подтверждая успешное добавление продукта в корзину.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого 201,
     *                        что указывало бы на некорректную обработку запроса добавления продукта в корзину.
     */
    @Test(priority = 12)

    public void AddProductToCartTest() {
        // Создаем JSON тело запроса, используя переменную productId
        String requestBody = String.format("{\"product_id\": %d, \"quantity\": 2}", productId);
        authenticateUser();
        performRequestAndVerify("POST", "/cart", requestBody, accessToken, 201);
    }

    /**
     * Тестирование попытки добавления продукта в корзину без аутентификации.
     * Этот метод формирует JSON тело запроса для добавления определенного продукта (указанного через переменную productId)
     * в корзину, но отправляет POST запрос на эндпоинт "/cart" без аутентификационного токена.
     * Такой запрос должен быть отклонен API с возвращением статусного кода 401 (Unauthorized),
     * указывая на необходимость аутентификации пользователя для выполнения данной операции.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого 401,
     *                        что указывало бы на ошибку в механизме контроля доступа API.
     */
    @Test(priority = 13)

    public void AddProductToCartWithoutAuthorizationTest() {
        // Создаем JSON тело запроса, используя переменную productId
        String requestBody = String.format("{\"product_id\": %d, \"quantity\": 2}", productId);
        performRequestAndVerify("POST", "/cart", requestBody, null, 401);

    }

    /**
     * Тестирование удаления продукта из корзины покупок.
     * Метод сначала аутентифицирует пользователя, а затем отправляет DELETE запрос на эндпоинт "/cart/{productId}",
     * используя аутентификационный токен, для удаления конкретного продукта из корзины покупок.
     * Ожидается, что API успешно обработает запрос и вернет статусный код 200 (OK),
     * подтверждая успешное удаление продукта из корзины.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого 200,
     *                        что указывало бы на некорректную обработку запроса удаления продукта из корзины.
     */
    @Test(priority = 16)

    public void RemoveProductFromCartTest() {
        authenticateUser();
        performRequestAndVerify("DELETE", "/cart/" + productId, null, accessToken, 200);
    }

    /**
     * Тестирование попытки удаления продукта из корзины без аутентификации.
     * Этот метод отправляет DELETE запрос на эндпоинт "/cart/{productId}" без использования аутентификационного токена.
     * Такой запрос должен быть отклонен API с возвращением статусного кода 401 (Unauthorized),
     * указывая на то, что операция удаления продукта из корзины требует предварительной аутентификации пользователя.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого 401
     *
     */
    @Test(priority = 17)

    public void RemoveProductFromCartWithoutAuthorizationTest() {
        performRequestAndVerify("DELETE", "/cart/" + productId, null, null, 401);
    }

}
