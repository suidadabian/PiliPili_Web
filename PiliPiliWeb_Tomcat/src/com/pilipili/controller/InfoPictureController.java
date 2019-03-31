package com.pilipili.controller;

import com.google.gson.stream.JsonWriter;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Page;
import com.pilipili.Code;
import com.pilipili.Util;
import com.pilipili.model.InfoPicture;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class InfoPictureController extends Controller {
    private static final String GET_INFO_PICTURES_PARA_PAGE_NO = "pageNo";
    private static final String GET_INFO_PICTURES_PARA_PAGE_SIZE = "pageSize";

    private static final String GET_USER_INFO_PICTURES_PARA_PAGE_NO = "pageNo";
    private static final String GET_USER_INFO_PICTURES_PARA_PAGE_SIZE = "pageSize";
    private static final String GET_USER_INFO_PICTURES_PARA_USER_ID = "userId";
    private static final String GET_USER_INFO_PICTURE_PARA_SELF = "self";

    private static final String GET_RECOMMEND_INFO_PICTURES_PARA_INFO_PICTURE_ID = "infoPictureId";
    private static final int GET_RECOMMEND_INFO_PICTURES_MAX_NUMBER = 5;

    private static final String GET_INFO_PICTURE_ID = "id";

    private static final String DELETE_INFO_PICTURE_ID = "id";
    private static final String DELETE_INFO_PICTURE_USER_ID = "userId";

    public void getInfoPictures() throws IOException {
        Integer pageNo = getParaToInt(GET_INFO_PICTURES_PARA_PAGE_NO);
        Integer pageSize = getParaToInt(GET_INFO_PICTURES_PARA_PAGE_SIZE);

        if (pageNo == null || pageSize == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        Page<InfoPicture> infoPicturePage = InfoPicture.dao.paginate(pageNo, pageSize, "select *", "from infopicture where share = ? order by date desc", InfoPicture.SHARE_TRUE);
        renderText(generateInfoPicturesJson(infoPicturePage.getList()));
    }

    public void getUserInfoPictures() throws IOException {
        Integer pageNo = getParaToInt(GET_USER_INFO_PICTURES_PARA_PAGE_NO);
        Integer pageSize = getParaToInt(GET_USER_INFO_PICTURES_PARA_PAGE_SIZE);
        Long userId = getParaToLong(GET_USER_INFO_PICTURES_PARA_USER_ID);
        Boolean self = getParaToBoolean(GET_USER_INFO_PICTURE_PARA_SELF);

        if (pageNo == null || pageSize == null || userId == null || self == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        Page<InfoPicture> infoPicturePage;
        if (self) {
            infoPicturePage = InfoPicture.dao.paginate(pageNo, pageSize, "select *", "from infopicture where userId = ? order by date desc", userId);
        } else {
            infoPicturePage = InfoPicture.dao.paginate(pageNo, pageSize, "select *", "from infopicture where userId = ? and share = ? order by date desc", userId, InfoPicture.SHARE_TRUE);
        }
        renderText(generateInfoPicturesJson(infoPicturePage.getList()));
    }

    public void getRecommendInfoPictures() throws IOException {
        Long infoPictureId = getParaToLong(GET_RECOMMEND_INFO_PICTURES_PARA_INFO_PICTURE_ID);

        if (infoPictureId == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        List<InfoPicture> infoPictures = InfoPicture.dao.find("select * from infopicture where id <> " + infoPictureId + " and share = " + InfoPicture.SHARE_TRUE);
        List<InfoPicture> recommendInfoPictures = new ArrayList<>();
        if (infoPictures.size() <= GET_RECOMMEND_INFO_PICTURES_MAX_NUMBER) {
            recommendInfoPictures.addAll(infoPictures);
            Collections.shuffle(recommendInfoPictures);
        } else {
            Collections.shuffle(recommendInfoPictures);
            while (recommendInfoPictures.size() > GET_RECOMMEND_INFO_PICTURES_MAX_NUMBER) {
                recommendInfoPictures.remove(recommendInfoPictures.size() - 1);
            }
        }

        renderText(generateInfoPicturesJson(recommendInfoPictures));
    }

    public void getInfoPicture() throws IOException {
        Long id = getParaToLong(GET_INFO_PICTURE_ID);

        if (id == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        InfoPicture infoPicture = InfoPicture.dao.findById(id);

        if (infoPicture == null) {
            renderText(Util.generateErrorJson(Code.NO_INFO_PICTURE));
            return;
        }

        renderText(generateInfoPictureJson(infoPicture));
    }

    public void uploadInfoPicture() throws IOException {
        InfoPicture infoPicture = InfoPicture.fromJson(HttpKit.readData(getRequest()));

        if (infoPicture == null || infoPicture.getId() != 0) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        if (infoPicture.getUserId() == null || infoPicture.getUrl() == null
                || infoPicture.getTitle() == null || infoPicture.getShare() == null
                || infoPicture.getDeletehash() == null || infoPicture.getIntro() == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        if (infoPicture.getDate() == null) {
            infoPicture.setDate(new Date(System.currentTimeMillis()));
        }

        infoPicture.setId(null);
        infoPicture.save();
        renderText(generateInfoPictureJson(infoPicture));
    }

    public void deleteInfoPicture() throws IOException {
        Long id = getParaToLong(DELETE_INFO_PICTURE_ID);
        Long userId = getParaToLong(DELETE_INFO_PICTURE_USER_ID);

        if (id == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        InfoPicture infoPicture = InfoPicture.dao.findById(id);

        if (infoPicture == null) {
            renderText(Util.generateErrorJson(Code.NO_INFO_PICTURE));
            return;
        }

        if (!infoPicture.getUserId().equals(userId)) {
            renderText(Util.generateErrorJson(Code.NO_AUTHORITY));
            return;
        }

        infoPicture.delete();
        renderText(generateInfoPictureJson(infoPicture));
    }

    private String generateInfoPicturesJson(List<InfoPicture> infoPictures) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.beginObject()
                .name("code").value(Code.SUCCESS)
                .name("data")
                .beginArray();
        for (InfoPicture infoPicture : infoPictures) {
            InfoPicture.toJson(infoPicture, jsonWriter);
        }
        jsonWriter.endArray()
                .endObject();
        jsonWriter.flush();
        String json = stringWriter.toString();
        jsonWriter.close();
        return json;
    }

    private String generateInfoPictureJson(InfoPicture infoPicture) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.beginObject()
                .name("code").value(Code.SUCCESS)
                .name("data");
        InfoPicture.toJson(infoPicture, jsonWriter)
                .endObject();
        jsonWriter.flush();
        String json = stringWriter.toString();
        jsonWriter.close();
        return json;
    }
}
