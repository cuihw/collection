package com.data.collection.data.greendao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.database.Database;

public class MyOpenDaoHelper extends DaoMaster.OpenHelper {
    private static final String TAG = "MyOpenDaoHelper";
    Context context;

    public MyOpenDaoHelper(Context context, String name) {
        super(context, name);
        this.context = context;
    }

    public MyOpenDaoHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
        this.context = context;
    }

    public void initData(){
        // 是否切换项目，如果切换项目，那么删除原来的数据。
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);


        if (oldVersion == 1 && newVersion == 2) {
            // alter table 表名 ADD 字段 类型 NOT NULL Default 0
            upgrade1To2(db);
        }
        if (oldVersion == 2 && newVersion == 3) {
            // alter table 表名 ADD 字段 类型 NOT NULL Default 0
            upgrade2To3(db);
        }
    }

    private void upgrade1To2(Database db) {
        db.execSQL("alter table CHECK_POINT ADD IS_UPLOADED INTEGER NOT NULL Default 0");
    }


    private void upgrade2To3(Database db) {
        db.execSQL("alter table CHECK_POINT ADD PROJECT_ID TEXT");
    }
}
