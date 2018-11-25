package com.example.greendao_generator;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Schema;

public class MainGenerator {
    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1, "com.example.zenoinc.rucyc.db");
        Entity user = schema.addEntity("User");
        user.addLongProperty("id").primaryKey().autoincrement();
        user.addStringProperty("user_id").notNull();
        user.addStringProperty("fname").notNull();
        user.addStringProperty("lname").notNull();
        user.addStringProperty("email").notNull();
        user.addStringProperty("password").notNull();
        user.addStringProperty("token").notNull();
        user.addIntProperty("photo");
        user.addDateProperty("birthdate");
        user.addStringProperty("weight");
        user.addStringProperty("height");
        user.addStringProperty("gender");
        user.addStringProperty("address");
        user.addIntProperty("province");
        user.addIntProperty("city");
        user.addIntProperty("district");
        user.addBooleanProperty("filled").notNull();
        user.addStringProperty("status").notNull();
        new DaoGenerator().generateAll(schema, "../app/src/main/java");
    }
}
