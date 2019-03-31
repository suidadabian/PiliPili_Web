package com.pilipili;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;
import com.pilipili.controller.CommentController;
import com.pilipili.controller.InfoPictureController;
import com.pilipili.controller.LightPictureController;
import com.pilipili.controller.LoginController;
import com.pilipili.controller.ReplyController;
import com.pilipili.controller.UserController;
import com.pilipili.model._MappingKit;

public class PiliPiliConfig extends JFinalConfig {
    public void configConstant(Constants me) {
        PropKit.use("pilipili_config.txt");
        me.setDevMode(PropKit.getBoolean("devMode", false));
    }

    public void configRoute(Routes me) {
        me.add("/user", UserController.class);
        me.add("/login", LoginController.class);
        me.add("/infoPicture", InfoPictureController.class);
        me.add("/lightPicture", LightPictureController.class);
        me.add("/comment", CommentController.class);
        me.add("/reply", ReplyController.class);
    }

    public void configEngine(Engine me) {

    }

    public static DruidPlugin createDruidPlugin() {
        return new DruidPlugin(PropKit.get("jdbcUrl"), PropKit.get("user"), PropKit.get("password").trim());
    }

    public void configPlugin(Plugins me) {
        DruidPlugin dp = createDruidPlugin();
        me.add(dp);

        ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);
        _MappingKit.mapping(arp);
        me.add(arp);
    }

    public void configInterceptor(Interceptors me) {

    }

    public void configHandler(Handlers me) {

    }
}
