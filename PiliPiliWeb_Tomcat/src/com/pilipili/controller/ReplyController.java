package com.pilipili.controller;

import com.google.gson.stream.JsonWriter;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Page;
import com.pilipili.Code;
import com.pilipili.Util;
import com.pilipili.model.Comment;
import com.pilipili.model.Reply;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

public class ReplyController extends Controller {
    private static final String GET_REPLIES_PARA_PAGE_NO = "pageNo";
    private static final String GET_REPLIES_PARA_PAGE_SIZE = "pageSize";
    private static final String GET_REPLIES_PARA_COMMENT_ID = "commentId";

    public void getReplies() throws IOException {
        Integer pageNo = getParaToInt(GET_REPLIES_PARA_PAGE_NO);
        Integer pageSize = getParaToInt(GET_REPLIES_PARA_PAGE_SIZE);
        Long commentId = getParaToLong(GET_REPLIES_PARA_COMMENT_ID);

        if (pageNo == null || pageSize == null || commentId == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        Page<Reply> replyPage = Reply.dao.paginate(pageNo, pageSize, "select *", "from reply where commentId = ? order by date desc", commentId);
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.beginObject()
                .name("code").value(Code.SUCCESS)
                .name("data")
                .beginArray();
        for (Reply reply : replyPage.getList()) {
            Reply.toJson(reply, jsonWriter);
        }
        jsonWriter.endArray()
                .endObject();
        jsonWriter.flush();
        renderText(stringWriter.toString());
        jsonWriter.close();
    }

    public void sendReply() throws IOException {
        Reply reply = Reply.fromJson(HttpKit.readData(getRequest()));

        if (reply == null || reply.getId() != 0) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        if (reply.getUserId() == null || reply.getCommentId() == null || reply.getReply() == null) {
            renderText(Util.generateErrorJson(Code.PARA_ERROR));
            return;
        }

        if (reply.getDate() == null) {
            reply.setDate(new Date(System.currentTimeMillis()));
        }


        Comment comment = Comment.dao.findById(reply.getCommentId());
        comment.setReplyNum(comment.getReplyNum() + 1).update();
        reply.setId(null);
        reply.save();

        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.beginObject()
                .name("code").value(Code.SUCCESS)
                .name("data");
        Reply.toJson(reply, jsonWriter)
                .endObject();
        jsonWriter.flush();
        renderText(stringWriter.toString());
        jsonWriter.close();
    }

}
