package com.pilipili.model;

import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.generator.Generator;
import com.jfinal.plugin.druid.DruidPlugin;
import com.pilipili.PiliPiliConfig;

import javax.sql.DataSource;

public class PiliPiliGenerator {
    public static DataSource getDataSource() {
        PropKit.use("pilipili_config.txt");
        DruidPlugin druidPlugin = PiliPiliConfig.createDruidPlugin();
        druidPlugin.start();
        return druidPlugin.getDataSource();
    }

    public static void main(String[] args) {
        String baseModelPackageName = "com.pilipili.model.base";
        String baseModelOutputDir = PathKit.getWebRootPath() + "/../src/com/pilipili/model/base";
        String modelPackageName = "com.pilipili.model";
        String modelOutputDir = baseModelOutputDir + "/..";

        Generator generator = new Generator(getDataSource(), baseModelPackageName, baseModelOutputDir, modelPackageName, modelOutputDir);
        generator.setGenerateChainSetter(true);
        generator.setGenerateDaoInModel(true);
        generator.setGenerateDataDictionary(true);
        generator.generate();
    }
}
