package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {

    private static Javalin app;
    private static String baseUrl;
    private static Url existingUrl;
    private static Transaction transaction;
    private static MockWebServer mockWebServer;

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();

        baseUrl = "http://localhost:" + port;

        existingUrl = new Url("https://ru.hexlet.io");
        existingUrl.save();

        mockWebServer = new MockWebServer();

        String expected = Files.readString(Paths.get("src", "test", "resources", "expected", "github"));

        mockWebServer.enqueue(new MockResponse().setBody(expected));

        mockWebServer.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();

        mockWebServer.shutdown();
    }

    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }

    @Nested
    class RootTest {

        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest.get(baseUrl).asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Бесплатно проверяйте сайты на SEO пригодность");
        }
    }

    @Nested
    class UrlTest {

        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains(existingUrl.getName());
            assertThat(body).contains("10/10/2021 03:03");
        }

        @Test
        void testShowUrl() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/" + existingUrl.getId())
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains(existingUrl.getName());
//            assertThat(body).contains("Живое онлайн сообщество");
        }

        @Test
        void testCreateUrl() {
            String inputUrl = "https://yandex.ru/search/?lr=13321&text=java";
            String normalizedUrl = "https://yandex.ru";
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains(normalizedUrl);
            assertThat(body).contains("Страница успешно добавлена");

            Url actualArticle = new QUrl()
                    .name.equalTo(normalizedUrl)
                    .findOne();

            assertThat(actualArticle).isNotNull();
        }

        @Test
        void testCreateExistingUrl() {
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", existingUrl.getName())
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> response = Unirest
                    .get(baseUrl)
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("Страница уже существует");
        }

        @Test
        void testCreateInvalidUrl() {
            String inputUrl = "Qwerty123";
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).doesNotContain(inputUrl);
            assertThat(body).contains("Некорректный URL");

            Url actualArticle = new QUrl()
                    .name.equalTo(inputUrl)
                    .findOne();

            assertThat(actualArticle).isNull();
        }

        @Test
        void testCheckUrl() {
            String gitHubDescription = "GitHub is where over 73 million developers shape the future of software.";
            String gitHubTitle = "GitHub: Where the world builds software · GitHub";
            String gitHubH1 = "Where the world builds software";

            String mockUrl = mockWebServer.url("/").toString();

            HttpResponse<String> response = Unirest.get(mockUrl)
                    .asString();

            assertThat(response.getStatus()).isEqualTo(200);
        }
    }
}
