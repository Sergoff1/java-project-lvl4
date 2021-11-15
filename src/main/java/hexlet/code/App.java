package hexlet.code;

import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;

public final class App {

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "3000");
        return Integer.parseInt(port);
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(JavalinConfig::enableDevLogging);

        app.get("/", ctx -> ctx.result("Hello World"));

        return app;
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }
}
