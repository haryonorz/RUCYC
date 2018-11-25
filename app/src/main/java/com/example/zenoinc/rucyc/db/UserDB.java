package com.example.zenoinc.rucyc.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;
import java.util.List;

public class UserDB {
    private final String DB_NAME = "rucyc" ;
    private Context context;

    public UserDB(Context context){
        this.context = context;
    }

    private UserDao setupDb(){
        DaoMaster.DevOpenHelper masterHelper = new DaoMaster.DevOpenHelper(this.context, DB_NAME, null);
        SQLiteDatabase db = masterHelper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);
        DaoSession masterSession=master.newSession();
        return masterSession.getUserDao();
    }

    public User getUser(){
        UserDao ud = setupDb();

        List<User> users = ud.queryBuilder().list();

        if(users.size() != 0)
            return users.get(0);
        return null;
    }

    public User getUser(String uid){
        UserDao ud = setupDb();
        List<User> users = ud.queryBuilder().where(UserDao.Properties.User_id.eq(uid)).list();

        if(users.size() != 0)
            return users.get(0);
        return null;
    }

    public void insertNewUserDB(String uid, String fname, String lname, String email, String password,
                                String token){
        UserDao ud = setupDb();
        User uo = new User();
        uo.setUser_id(uid);
        uo.setFname(fname);
        uo.setLname(lname);
        uo.setEmail(email);
        uo.setPassword(password);
        uo.setToken(token);
        uo.setFilled(false);
        uo.setStatus("incomplete");
        ud.insert(uo);
    }

    //problem decimal weight height
    public void updateProfileUserDB(String fname, String lname, String email, Date birthdate,
                                    String weight, String height, String gender, String address, int province,
                                    int city, int district){
        UserDao ud = setupDb();
        User uo = getUser();
        uo.setFname(fname);
        uo.setLname(lname);
        uo.setEmail(email);
        uo.setBirthdate(birthdate);
        uo.setWeight(weight);
        uo.setHeight(height);
        uo.setGender(gender);
        uo.setAddress(address);
        uo.setProvince(province);
        uo.setCity(city);
        uo.setDistrict(district);
        ud.update(uo);
    }

    public void updatePassword(String password){
        UserDao ud = setupDb();
        User uo = getUser();
        uo.setPassword(password);
        ud.update(uo);
    }

    public void updateFillUserDB(Date birthdate, String weight, String height, String gender, String address, int province, int city,
                                 int district){
        UserDao ud = setupDb();
        User uo = getUser();
        uo.setPhoto(null);
        uo.setBirthdate(birthdate);
        uo.setWeight(weight);
        uo.setHeight(height);
        uo.setGender(gender);
        uo.setAddress(address);
        uo.setProvince(province);
        uo.setCity(city);
        uo.setDistrict(district);
        uo.setFilled(true);
        uo.setStatus("new");
        ud.update(uo);
    }

    //auth user liat
    public void updateInformationUserDB(int photo, Date birthdate, String weight, String height, String gender,
                                        String address, int province, int city, int district, boolean filled, String status,
                                        String token){
        UserDao ud = setupDb();
        User uo = getUser();
        uo.setPhoto(photo);
        uo.setBirthdate(birthdate);
        uo.setWeight(weight);
        uo.setHeight(height);
        uo.setGender(gender);
        uo.setAddress(address);
        uo.setProvince(province);
        uo.setCity(city);
        uo.setDistrict(district);
        uo.setFilled(filled);
        uo.setStatus(status);
        uo.setToken(token);
        ud.update(uo);
    }

    public void updateFilledStatus(boolean filled){
        UserDao ud = setupDb();
        User uo = getUser();
        uo.setFilled(filled);
        ud.update(uo);
    }

    public void updatePhotoUserDB(int photo){
        UserDao ud = setupDb();
        User uo = getUser();
        uo.setPhoto(photo);
        ud.update(uo);
    }

    public void updateStatusUserDB(String status){
        UserDao ud = setupDb();
        User uo = getUser();
        uo.setStatus(status);
        ud.update(uo);
    }

    public void updateTokenUserDB(String token){
        UserDao ud = setupDb();
        User uo = getUser();
        uo.setToken(token);
        ud.update(uo);
    }

    public void removeAll(){
        setupDb().deleteAll();
    }
}
