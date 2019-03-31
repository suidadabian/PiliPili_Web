package com.pilipili.controller;

import com.google.gson.stream.JsonWriter;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Page;
import com.pilipili.Code;
import com.pilipili.Util;
import com.pilipili.model.Comment;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

public class CommentController extends Controller {
    private static final String GET_COMMENTS_PARA_PAGE_NO = "pageNo";
    private static final String GET_COMMENTS_PARA_PAGE_SIZE = "pageSize";
    private static final String GET_COMMENTS_PARA_INFO_PICTURE_ID = "infoPictureId";

    private static final String GET_COMMENT_PARA_ID = "id";

    public void getComments() throws IOException {
        Integer pageNo = getParaToInt(GET_COMMENTS_PARA_PAGE_NO);
        Integer pageSize = getParaToInt(GET_COMMENTS_PARA_PAGE_SIZE);
        Long infoPictureId = getParaToLong(GET_COMMENTS_PARA_INFO_PICTURE_ID);

        if (pageNo == null || pageSize == null || infoPictureId == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        Page<Comment> commentPage = Comment.dao.paginate(pageNo, pageSize, "select *", "from comment where infoPictureId = ? order by date desc", infoPictureId);
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.beginObject()
                .name("code").value(Code.SUCCESS)
                .name("data")
                .beginArray();
        for (Comment comment : commentPage.getList()) {
            Comment.toJson(comment, jsonWriter);
        }
        jsonWriter.endArray()
                .endObject();
        jsonWriter.flush();
        renderText(stringWriter.toString());
        jsonWriter.close();
    }

    public void getComment() throws IOException {
        Long id = getParaToLong(GET_COMMENT_PARA_ID);

        if (id == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        Comment comment = Comment.dao.findById(id);

        if (comment == null) {
            renderText(Util.generateErrorJson(Code.NO_COMMENT));
            return;
        }

        renderText(generateCommentJson(comment));
    }

    public void sendComment() throws IOException {
        Comment comment = Comment.fromJson(HttpKit.readData(getRequest()));

        if (comment == null || comment.getId() != 0) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        if (comment.getUserId() == null || comment.getInfoPictureId() == null || comment.getComment() == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        if (comment.getDate() == null) {
            comment.setDate(new Date(System.currentTimeMillis()));
        }

        comment.setId(null);
        comment.save();
        renderText(generateCommentJson(comment));
    }

    private String generateCommentJson(Comment comment) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.beginObject()
                .name("code").value(Code.SUCCESS)
                .name("data");
        Comment.toJson(comment, jsonWriter)
                .endObject();
        jsonWriter.flush();
        String json = stringWriter.toString();
        jsonWriter.close();
        return json;
    }

}
