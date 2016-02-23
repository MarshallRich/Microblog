package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static HashMap<String, User> users = new HashMap<>();



    public static void main(String[] args) {
        Spark.init();

        Spark.get(
                "/",
                ((request, response) -> {

                    HashMap m = new HashMap();

                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = users.get(username);

                    if (user == null) {
                        return new ModelAndView(m, "index.html");
                    }
                    else{
                        m.put("name", user.name);
                        m.put("messages", user.messages);
                        return new ModelAndView(m, "messages.html");
                    }

                }),
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/create-user",
                ((request, response) -> {
                    String name = request.queryParams("loginName");
                    String password = request.queryParams("password");
                    User user = users.get(name);

                    if (user == null){
                        user = new User(name, password);
                        users.put(name, user);

                        Session session = request.session();
                        session.attribute("username", name);

                        response.redirect("/");
                    }

                    else if (password.equals(user.password)) {
                        Session session = request.session();
                        session.attribute("username", name);

                        response.redirect("/");
                    }

                    else {
                        Spark.halt(403);
                    }
                    return "";
                })
        );

        Spark.post(
                "/create-message",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = users.get(username);


                    String text = request.queryParams("enteredMessage");
                    Message message = new Message(text);
                    user.messages.add(message);
                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/delete-message",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = users.get(username);

                    int messageId = Integer.valueOf(request.queryParams("messageId")) - 1;

                    if (messageId + 1 <= user.messages.size()) {
                        user.messages.remove(messageId);
                    }

                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/edit-message",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = users.get(username);

                    int messageId = Integer.valueOf(request.queryParams("messageId")) - 1;

                    if (messageId + 1 <= user.messages.size()) {
                        Message newMessage = new Message(request.queryParams("newMessage"));

                        user.messages.set(messageId, newMessage);
                    }

                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/logout",
                ((request, response) ->{
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );
    }
}
