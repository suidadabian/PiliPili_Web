package com.pilipili.controller;

import com.google.gson.stream.JsonWriter;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.pilipili.Code;
import com.pilipili.Util;
import com.pilipili.model.User;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class LoginController extends Controller {
    private static final String LOGIN_FIELD_ACCOUNT = "account";
    private static final String LOGIN_FIELD_PASSWORD = "password";
    private static final String REGISTERED_FIELD_ACCOUNT = "account";
    private static final String REGISTERED_FIELD_PASSWORD = "password";
    private static final String REGISTERED_FIELD_USERNAME = "username";

    /**
     * 登录接口
     */
    public void login() throws IOException {
        Map<String, String> body = Util.parsePostRequestBody(HttpKit.readData(getRequest()));
        String account = body.get(LOGIN_FIELD_ACCOUNT);
        String password = body.get(LOGIN_FIELD_PASSWORD);

        if (account == null || password == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        User user = User.dao.findFirst("select * from user where account = ?", account);
        if (user == null) {
            renderText(Util.generateErrorJson(Code.NO_ACCOUNT));
            return;
        }
        if (user.get("password").equals(password)) {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            jsonWriter.beginObject()
                    .name("code").value(Code.SUCCESS)
                    .name("data");
            User.toJson(user, jsonWriter)
                    .endObject();
            jsonWriter.flush();
            renderText(stringWriter.toString());
            jsonWriter.close();
        } else {
            renderText(Util.generateErrorJson(Code.PASSWORD_ERROR));
        }
    }

    /**
     * 注册接口
     */
    public void registered() throws IOException {
        Map<String, String> body = Util.parsePostRequestBody(HttpKit.readData(getRequest()));
        String account = body.get(REGISTERED_FIELD_ACCOUNT);
        String password = body.get(REGISTERED_FIELD_PASSWORD);
        String username = body.get(REGISTERED_FIELD_USERNAME);

        if (account == null || password == null || username == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        User checkUser = User.dao.findFirst("select * from user where account = ? or name = ?", account, username);
        if (checkUser != null) {
            renderText(Util.generateErrorJson(checkUser.getAccount().equals(account) ? Code.ACCOUNT_EXIST : Code.USERNAME_EXIST));
            return;
        }

        new User().setAccount(account).setPassword(password).setName(username).save();
        User user = User.dao.findFirst("select * from user where account = ?", account);
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.beginObject()
                .name("code").value(Code.SUCCESS)
                .name("data");
        User.toJson(user, jsonWriter)
                .endObject();
        jsonWriter.flush();
        renderText(stringWriter.toString());
        jsonWriter.close();
    }
}
