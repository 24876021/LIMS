package com.thematrix.labmanagement.common.utils;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

public class CodeGenerator {

    public static void main(String[] args) {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setOutputDir(System.getProperty("user.dir") + "/src/main/java")
                .setAuthor("ShuangTian")
                .setOpen(false)
                .setFileOverride(false)
                .setServiceName("%sService")
                .setIdType(IdType.AUTO)
                .setSwagger2(true);

        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDbType(DbType.MYSQL)
                .setUrl("jdbc:mysql://localhost:3306/shiyanshi?useSSL=false&serverTimezone=UTC")
                .setUsername("root")
                .setPassword("waly7948")
                .setDriverName("com.mysql.cj.jdbc.Driver");

        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig
                .setExclude("sys_user", "role", "authority", "user_role", "role_authority")
                .setNaming(NamingStrategy.underline_to_camel)
                .setColumnNaming(NamingStrategy.underline_to_camel)
                .setEntityLombokModel(true)
                .setRestControllerStyle(true)
                .setTablePrefix(new String[]{"tbl_"});

        PackageConfig packageConfig = new PackageConfig();
        packageConfig.setParent("com.thematrix.labmanagement")
                .setMapper("mapper")
                .setEntity("entity")
                .setController("controller")
                .setService("service")
                .setXml("mapper");

        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setXml(null)
                .setController("templates/controller.java.vm")
                .setEntity("templates/entity.java.vm")
                .setMapper("templates/mapper.java.vm");

        AutoGenerator autoGenerator = new AutoGenerator();
        autoGenerator.setGlobalConfig(globalConfig)
                .setDataSource(dataSourceConfig)
                .setStrategy(strategyConfig)
                .setPackageInfo(packageConfig)
                .setTemplate(templateConfig);

        autoGenerator.execute();
    }
}
