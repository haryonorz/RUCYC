package com.example.zenoinc.rucyc.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "USER".
*/
public class UserDao extends AbstractDao<User, Long> {

    public static final String TABLENAME = "USER";

    /**
     * Properties of entity User.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "ID");
        public final static Property User_id = new Property(1, String.class, "user_id", false, "USER_ID");
        public final static Property Fname = new Property(2, String.class, "fname", false, "FNAME");
        public final static Property Lname = new Property(3, String.class, "lname", false, "LNAME");
        public final static Property Email = new Property(4, String.class, "email", false, "EMAIL");
        public final static Property Password = new Property(5, String.class, "password", false, "PASSWORD");
        public final static Property Token = new Property(6, String.class, "token", false, "TOKEN");
        public final static Property Photo = new Property(7, Integer.class, "photo", false, "PHOTO");
        public final static Property Birthdate = new Property(8, java.util.Date.class, "birthdate", false, "BIRTHDATE");
        public final static Property Weight = new Property(9, String.class, "weight", false, "WEIGHT");
        public final static Property Height = new Property(10, String.class, "height", false, "HEIGHT");
        public final static Property Gender = new Property(11, String.class, "gender", false, "GENDER");
        public final static Property Address = new Property(12, String.class, "address", false, "ADDRESS");
        public final static Property Province = new Property(13, Integer.class, "province", false, "PROVINCE");
        public final static Property City = new Property(14, Integer.class, "city", false, "CITY");
        public final static Property District = new Property(15, Integer.class, "district", false, "DISTRICT");
        public final static Property Filled = new Property(16, boolean.class, "filled", false, "FILLED");
        public final static Property Status = new Property(17, String.class, "status", false, "STATUS");
    }


    public UserDao(DaoConfig config) {
        super(config);
    }
    
    public UserDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"USER\" (" + //
                "\"ID\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"USER_ID\" TEXT NOT NULL ," + // 1: user_id
                "\"FNAME\" TEXT NOT NULL ," + // 2: fname
                "\"LNAME\" TEXT NOT NULL ," + // 3: lname
                "\"EMAIL\" TEXT NOT NULL ," + // 4: email
                "\"PASSWORD\" TEXT NOT NULL ," + // 5: password
                "\"TOKEN\" TEXT NOT NULL ," + // 6: token
                "\"PHOTO\" INTEGER," + // 7: photo
                "\"BIRTHDATE\" INTEGER," + // 8: birthdate
                "\"WEIGHT\" TEXT," + // 9: weight
                "\"HEIGHT\" TEXT," + // 10: height
                "\"GENDER\" TEXT," + // 11: gender
                "\"ADDRESS\" TEXT," + // 12: address
                "\"PROVINCE\" INTEGER," + // 13: province
                "\"CITY\" INTEGER," + // 14: city
                "\"DISTRICT\" INTEGER," + // 15: district
                "\"FILLED\" INTEGER NOT NULL ," + // 16: filled
                "\"STATUS\" TEXT NOT NULL );"); // 17: status
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"USER\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, User entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getUser_id());
        stmt.bindString(3, entity.getFname());
        stmt.bindString(4, entity.getLname());
        stmt.bindString(5, entity.getEmail());
        stmt.bindString(6, entity.getPassword());
        stmt.bindString(7, entity.getToken());
 
        Integer photo = entity.getPhoto();
        if (photo != null) {
            stmt.bindLong(8, photo);
        }
 
        java.util.Date birthdate = entity.getBirthdate();
        if (birthdate != null) {
            stmt.bindLong(9, birthdate.getTime());
        }
 
        String weight = entity.getWeight();
        if (weight != null) {
            stmt.bindString(10, weight);
        }
 
        String height = entity.getHeight();
        if (height != null) {
            stmt.bindString(11, height);
        }
 
        String gender = entity.getGender();
        if (gender != null) {
            stmt.bindString(12, gender);
        }
 
        String address = entity.getAddress();
        if (address != null) {
            stmt.bindString(13, address);
        }
 
        Integer province = entity.getProvince();
        if (province != null) {
            stmt.bindLong(14, province);
        }
 
        Integer city = entity.getCity();
        if (city != null) {
            stmt.bindLong(15, city);
        }
 
        Integer district = entity.getDistrict();
        if (district != null) {
            stmt.bindLong(16, district);
        }
        stmt.bindLong(17, entity.getFilled() ? 1L: 0L);
        stmt.bindString(18, entity.getStatus());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, User entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getUser_id());
        stmt.bindString(3, entity.getFname());
        stmt.bindString(4, entity.getLname());
        stmt.bindString(5, entity.getEmail());
        stmt.bindString(6, entity.getPassword());
        stmt.bindString(7, entity.getToken());
 
        Integer photo = entity.getPhoto();
        if (photo != null) {
            stmt.bindLong(8, photo);
        }
 
        java.util.Date birthdate = entity.getBirthdate();
        if (birthdate != null) {
            stmt.bindLong(9, birthdate.getTime());
        }
 
        String weight = entity.getWeight();
        if (weight != null) {
            stmt.bindString(10, weight);
        }
 
        String height = entity.getHeight();
        if (height != null) {
            stmt.bindString(11, height);
        }
 
        String gender = entity.getGender();
        if (gender != null) {
            stmt.bindString(12, gender);
        }
 
        String address = entity.getAddress();
        if (address != null) {
            stmt.bindString(13, address);
        }
 
        Integer province = entity.getProvince();
        if (province != null) {
            stmt.bindLong(14, province);
        }
 
        Integer city = entity.getCity();
        if (city != null) {
            stmt.bindLong(15, city);
        }
 
        Integer district = entity.getDistrict();
        if (district != null) {
            stmt.bindLong(16, district);
        }
        stmt.bindLong(17, entity.getFilled() ? 1L: 0L);
        stmt.bindString(18, entity.getStatus());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public User readEntity(Cursor cursor, int offset) {
        User entity = new User( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // user_id
            cursor.getString(offset + 2), // fname
            cursor.getString(offset + 3), // lname
            cursor.getString(offset + 4), // email
            cursor.getString(offset + 5), // password
            cursor.getString(offset + 6), // token
            cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7), // photo
            cursor.isNull(offset + 8) ? null : new java.util.Date(cursor.getLong(offset + 8)), // birthdate
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // weight
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // height
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // gender
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // address
            cursor.isNull(offset + 13) ? null : cursor.getInt(offset + 13), // province
            cursor.isNull(offset + 14) ? null : cursor.getInt(offset + 14), // city
            cursor.isNull(offset + 15) ? null : cursor.getInt(offset + 15), // district
            cursor.getShort(offset + 16) != 0, // filled
            cursor.getString(offset + 17) // status
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, User entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUser_id(cursor.getString(offset + 1));
        entity.setFname(cursor.getString(offset + 2));
        entity.setLname(cursor.getString(offset + 3));
        entity.setEmail(cursor.getString(offset + 4));
        entity.setPassword(cursor.getString(offset + 5));
        entity.setToken(cursor.getString(offset + 6));
        entity.setPhoto(cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7));
        entity.setBirthdate(cursor.isNull(offset + 8) ? null : new java.util.Date(cursor.getLong(offset + 8)));
        entity.setWeight(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setHeight(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setGender(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setAddress(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setProvince(cursor.isNull(offset + 13) ? null : cursor.getInt(offset + 13));
        entity.setCity(cursor.isNull(offset + 14) ? null : cursor.getInt(offset + 14));
        entity.setDistrict(cursor.isNull(offset + 15) ? null : cursor.getInt(offset + 15));
        entity.setFilled(cursor.getShort(offset + 16) != 0);
        entity.setStatus(cursor.getString(offset + 17));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(User entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(User entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(User entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
