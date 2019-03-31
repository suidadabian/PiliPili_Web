package com.pilipili.controller;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Page;
import com.pilipili.Code;
import com.pilipili.Util;
import com.pilipili.model.LightPicture;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

public class LightPictureController extends Controller {
    private static final String GET_LIGHT_PICTURES_PARA_PAGE_NO = "pageNo";
    private static final String GET_LIGHT_PICTURES_PARA_PAGE_SIZE = "pageSize";

    private static final String GET_USER_LIGHT_PICTURES_PARA_PAGE_NO = "pageNo";
    private static final String GET_USER_LIGHT_PICTURES_PARA_PAGE_SIZE = "pageSize";
    private static final String GET_USER_LIGHT_PICTURES_PARA_USER_ID = "userId";
    private static final String GET_USER_LIGHT_PICTURES_PARA_SELF = "self";

    private static final String GET_LIGHT_PICTURE_PARA_ID = "id";

    private static final String DELETE_LIGHT_PICTURE_PARA_ID = "id";
    private static final String DELETE_LIGHT_PICTURE_PARA_USER_ID = "userId";

    public void getLightPictures() throws IOException {
        Integer pageNo = getParaToInt(GET_LIGHT_PICTURES_PARA_PAGE_NO);
        Integer pageSize = getParaToInt(GET_LIGHT_PICTURES_PARA_PAGE_SIZE);

        if (pageNo == null || pageSize == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        Page<LightPicture> lightPicturePage = LightPicture.dao.paginate(pageNo, pageSize, "select *", "from lightpicture where share = ? order by date desc", LightPicture.SHARE_TRUE);
        renderText(generateLightPicturesJson(lightPicturePage.getList()));
    }

    public void getUserLightPictures() throws IOException {
        Integer pageNo = getParaToInt(GET_USER_LIGHT_PICTURES_PARA_PAGE_NO);
        Integer pageSize = getParaToInt(GET_USER_LIGHT_PICTURES_PARA_PAGE_SIZE);
        Long userId = getParaToLong(GET_USER_LIGHT_PICTURES_PARA_USER_ID);
        Boolean self = getParaToBoolean(GET_USER_LIGHT_PICTURES_PARA_SELF);

        if (pageNo == null || pageSize == null || userId == null || self == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        Page<LightPicture> lightPicturePage;
        if (self) {
            lightPicturePage = LightPicture.dao.paginate(pageNo, pageSize, "select *", "from lightpicture where userId = ? order by date desc", userId);
        } else {
            lightPicturePage = LightPicture.dao.paginate(pageNo, pageSize, "select *", "from lightpicture where userId = ? and share = ? order by date desc", userId, LightPicture.SHARE_TRUE);
        }
        renderText(generateLightPicturesJson(lightPicturePage.getList()));
    }

    public void getLightPicture() throws IOException {
        Long id = getParaToLong(GET_LIGHT_PICTURE_PARA_ID);

        if (id == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        LightPicture lightPicture = LightPicture.dao.findById(id);

        if (lightPicture == null) {
            renderText(Util.generateErrorJson(Code.NO_LIGHT_PICTURE));
            return;
        }

        renderText(generateLightPictureJson(lightPicture));
    }

    public void uploadLightPicture() throws IOException {
        LightPicture lightPicture = LightPicture.fromJson(HttpKit.readData(getRequest()));

        if (lightPicture == null || lightPicture.getId() != 0) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        if (lightPicture.getUserId() == null || lightPicture.getUrl() == null
                || lightPicture.getScale() == null || lightPicture.getShare() == null
                || lightPicture.getDeletehash() == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        if (lightPicture.getDate() == null) {
            lightPicture.setDate(new Date(System.currentTimeMillis()));
        }

        lightPicture.setId(null);
        lightPicture.save();
        renderText(generateLightPictureJson(lightPicture));
    }

    public void deleteLightPicture() throws IOException {
        Long id = getParaToLong(DELETE_LIGHT_PICTURE_PARA_ID);
        Long userId = getParaToLong(DELETE_LIGHT_PICTURE_PARA_USER_ID);

        if (id == null || userId == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        LightPicture lightPicture = LightPicture.dao.findById(id);

        if (lightPicture == null) {
            renderText(Util.generateErrorJson(Code.NO_LIGHT_PICTURE));
            return;
        }

        if (!lightPicture.getUserId().equals(userId)) {
            renderText(Util.generateErrorJson(Code.NO_AUTHORITY));
        }

        lightPicture.delete();
        renderText(generateLightPictureJson(lightPicture));
    }

    private String generateLightPicturesJson(List<LightPicture> lightPictures) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.beginObject()
                .name("code").value(Code.SUCCESS)
                .name("data")
                .beginArray();
        for (LightPicture lightPicture : lightPictures) {
            LightPicture.toJson(lightPicture, jsonWriter);
        }
        jsonWriter.endArray()
                .endObject();
        jsonWriter.flush();
        String json = stringWriter.toString();
        jsonWriter.close();
        return json;
    }

    private String generateLightPictureJson(LightPicture lightPicture) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.beginObject()
                .name("code").value(Code.SUCCESS)
                .name("data");
        LightPicture.toJson(lightPicture, jsonWriter)
                .endObject();
        jsonWriter.flush();
        String json = stringWriter.toString();
        jsonWriter.close();
        return json;
    }
}
