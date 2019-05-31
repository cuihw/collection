package com.data.collection.data.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "MESSAGE_DATA".
*/
public class MessageDataDao extends AbstractDao<MessageData, String> {

    public static final String TABLENAME = "MESSAGE_DATA";

    /**
     * Properties of entity MessageData.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, String.class, "id", true, "ID");
        public final static Property Title = new Property(1, String.class, "title", false, "TITLE");
        public final static Property Content = new Property(2, String.class, "content", false, "CONTENT");
        public final static Property Type = new Property(3, String.class, "type", false, "TYPE");
        public final static Property Read_at = new Property(4, String.class, "read_at", false, "READ_AT");
        public final static Property Create_time = new Property(5, String.class, "create_time", false, "CREATE_TIME");
        public final static Property Pusher = new Property(6, String.class, "pusher", false, "PUSHER");
        public final static Property IsUploaded = new Property(7, boolean.class, "isUploaded", false, "IS_UPLOADED");
    }


    public MessageDataDao(DaoConfig config) {
        super(config);
    }
    
    public MessageDataDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"MESSAGE_DATA\" (" + //
                "\"ID\" TEXT PRIMARY KEY NOT NULL ," + // 0: id
                "\"TITLE\" TEXT," + // 1: title
                "\"CONTENT\" TEXT," + // 2: content
                "\"TYPE\" TEXT," + // 3: type
                "\"READ_AT\" TEXT," + // 4: read_at
                "\"CREATE_TIME\" TEXT," + // 5: create_time
                "\"PUSHER\" TEXT," + // 6: pusher
                "\"IS_UPLOADED\" INTEGER NOT NULL );"); // 7: isUploaded
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"MESSAGE_DATA\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, MessageData entity) {
        stmt.clearBindings();
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(2, title);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(3, content);
        }
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(4, type);
        }
 
        String read_at = entity.getRead_at();
        if (read_at != null) {
            stmt.bindString(5, read_at);
        }
 
        String create_time = entity.getCreate_time();
        if (create_time != null) {
            stmt.bindString(6, create_time);
        }
 
        String pusher = entity.getPusher();
        if (pusher != null) {
            stmt.bindString(7, pusher);
        }
        stmt.bindLong(8, entity.getIsUploaded() ? 1L: 0L);
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, MessageData entity) {
        stmt.clearBindings();
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(2, title);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(3, content);
        }
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(4, type);
        }
 
        String read_at = entity.getRead_at();
        if (read_at != null) {
            stmt.bindString(5, read_at);
        }
 
        String create_time = entity.getCreate_time();
        if (create_time != null) {
            stmt.bindString(6, create_time);
        }
 
        String pusher = entity.getPusher();
        if (pusher != null) {
            stmt.bindString(7, pusher);
        }
        stmt.bindLong(8, entity.getIsUploaded() ? 1L: 0L);
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    @Override
    public MessageData readEntity(Cursor cursor, int offset) {
        MessageData entity = new MessageData( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // title
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // content
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // type
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // read_at
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // create_time
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // pusher
            cursor.getShort(offset + 7) != 0 // isUploaded
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, MessageData entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setTitle(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setContent(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setType(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setRead_at(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setCreate_time(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setPusher(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setIsUploaded(cursor.getShort(offset + 7) != 0);
     }
    
    @Override
    protected final String updateKeyAfterInsert(MessageData entity, long rowId) {
        return entity.getId();
    }
    
    @Override
    public String getKey(MessageData entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(MessageData entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}