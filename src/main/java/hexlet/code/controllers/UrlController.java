package hexlet.code.controllers;

import io.javalin.http.Handler;

public final class UrlController {
    public static Handler listUrls = ctx -> {

        ctx.render("urls/index.html");
    };

    public static Handler showUrl = ctx -> {

    };

    public static Handler createUrl = ctx -> {

    };
}
