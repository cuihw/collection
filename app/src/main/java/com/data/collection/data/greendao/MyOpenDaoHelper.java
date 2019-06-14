package com.data.collection.data.greendao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.database.Database;

public class MyOpenDaoHelper extends DaoMaster.OpenHelper {
    private static final String TAG = "MyOpenDaoHelper";

    public MyOpenDaoHelper(Context context, String name) {
        super(context, name);
    }

    public MyOpenDaoHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        if (oldVersion == 1 && newVersion == 2) {
            // alter table 表名 ADD 字段 类型 NOT NULL Default 0
            db.execSQL("alter table CHECK_POINT ADD IS_UPLOADED INTEGER NOT NULL Default 0");
        }


    }
}
