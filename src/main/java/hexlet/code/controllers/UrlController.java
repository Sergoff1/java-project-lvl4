package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class UrlController {
    public static Handler listUrls = ctx -> {

        List<Url> urls = new QUrl()
                .orderBy().id.asc()
                .findList();

        ctx.attribute("urls", urls);
        ctx.render("urls/index.html");
    };

    public static Handler showUrl = ctx -> {

    };

    public static Handler createUrl = ctx -> {
        try {
            String authorityUrl = new URL(ctx.formParam("url")).getAuthority();

            boolean urlExist = new QUrl()
                    .name.equalTo(authorityUrl)
                    .exists();

            if (urlExist) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flash-type", "info");
            } else {
                new Url(authorityUrl).save();

                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("flash-type", "success");
            }
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }
        ctx.redirect("/urls");
    };
}
