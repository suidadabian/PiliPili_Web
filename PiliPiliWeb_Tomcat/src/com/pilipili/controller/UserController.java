package com.pilipili.controller;

import com.google.gson.stream.JsonWriter;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.pilipili.Code;
import com.pilipili.Util;
import com.pilipili.model.User;

import java.io.IOException;
import java.io.StringWriter;

public class UserController extends Controller {
    private static final String GET_USER_PARA_ID = "id";

    public void getUser() throws IOException {
        Long id = getParaToLong(GET_USER_PARA_ID);

        if (id == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        User user = User.dao.findFirst("select * from user where id = ?", id);
        renderText(generateUserJson(user));
    }

    public void modifyUserInfo() throws IOException {
        User user = User.fromJson(HttpKit.readData(getRequest()));

        if (user == null || User.dao.findById(user.getId()) == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        if (User.dao.findFirst("select * from user where name = ?", user.getName()) != null) {
            renderText(Util.generateErrorJson(Code.USERNAME_EXIST));
            return;
        }

        user.update();
        renderText(generateUserJson(user));
    }

    public static String generateUserJson(User user) throws IOException {
        if (user == null) {
            return null;
        }

        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.beginObject()
                .name("code").value(Code.SUCCESS)
                .name("data");
        User.toJson(user, jsonWriter)
                .endObject();
        jsonWriter.flush();
        String json = stringWriter.toString();
        jsonWriter.close();
        return json;
    }
}
