package com.stellariver.milky.demo.infrastructure.database;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Date;

public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "gmtCreate", Date.class, new Date());
        this.strictInsertFill(metaObject, "gmtModified", Date.class, new Date());
        this.setFieldValByName("deleted", 0L, metaObject);
        this.setFieldValByName("version", 0L, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        metaObject.setValue("gmtModified", null);
        this.strictUpdateFill(metaObject, "gmtModified", Date.class, new Date());
    }

}
