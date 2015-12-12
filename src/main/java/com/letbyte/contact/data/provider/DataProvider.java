package com.letbyte.contact.data.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;


public class DataProvider {

    public static abstract class Entry implements android.provider.BaseColumns {
        private static final String TABLE_SUGGESTION = "keyword_suggestion_table";

        public static final String KEYWORD = "keyword";
        public static final String WEIGHT = "weight";
    }

    private Context mContext;

    private DataProvider(Context context) {
        this.mContext = context;
    }

    private static DataProvider sData;
    private final int CALL_LOOG_DISPLAY_LIMIT = 100;

    public synchronized static DataProvider onProvider(Context context) {
        return (sData = sData == null ? new DataProvider(context) : sData);
    }

    //Can maintain 100 rows only
    public boolean updateOrInsertSearchHints(CharSequence searchString) {
        if(searchString == null)
            throw  new NullPointerException();
        String keyword = searchString.toString();
        String query = "UPDATE "+Entry.TABLE_SUGGESTION+" SET " + Entry.WEIGHT + " = " + Entry.WEIGHT +
                " + 1 WHERE "+Entry.KEYWORD + " = '"+keyword+"'";
        String[] whereArg = new String[]{keyword};
        LetSQLite sqLite = LetSQLite.onSQLite(mContext);
        long affectedRowCount = sqLite.rawQueryWithAffectedRowCount(query, whereArg);
        /*ContentValues cv1 = new ContentValues();
        cv1.put(Entry.WEIGHT, Entry.WEIGHT + " + 1");
        boolean isUpdated = sqLite.update(Entry.TABLE_SUGGESTION, cv1, Entry.KEYWORD + " = ?",
                new String[]{keyword});*/
        if(affectedRowCount != 0)
            return true;
        /*if(isUpdated)
            return true;*/
        ContentValues cv = new ContentValues();
        cv.put(Entry.KEYWORD, keyword);
        cv.put(Entry.WEIGHT, 1);
        return sqLite.insert(Entry.TABLE_SUGGESTION, null, cv);
    }

    public Cursor getSuggesationHint() {
        return getSuggesationHint(null);
    }

    public Cursor getSuggesationHint(String query) {
        String table = Entry.TABLE_SUGGESTION;
        String[] projection = new String[]{Entry._ID, Entry.KEYWORD};
        String where = query == null || query.length() < 1 ? null : Entry.KEYWORD + " LIKE '%"+ query +"%'";
        String[] whereArg = null;
        String orderBy = Entry.KEYWORD + " ASC, " + Entry.WEIGHT + " DESC";
        String limit = " 100";
        return LetSQLite.onSQLite(mContext).query(table, projection, where, whereArg, null, orderBy, limit);
    }

    public void importDatabase() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();


            String currentDBPath = "//data//"+ mContext.getPackageName() +"//databases//let.db";
            String backupDBPath = "let.db";
            File currentDB = new File(data, currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            FileChannel src = new FileInputStream(currentDB).getChannel();
            FileChannel dst = new FileOutputStream(backupDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            Toast.makeText(mContext.getApplicationContext(), backupDB.toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(mContext.getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }


    private static final class LetSQLite extends SQLiteOpenHelper {
        private static String DB_LET = "let.db";
        private static int DB_VERSION = 1;

        private static Context sContext;
        private static LetSQLite sLetSQLite;

        private LetSQLite(Context context) {
            super(context, DB_LET, null, DB_VERSION);
            sContext = context;
        }

        public synchronized static LetSQLite onSQLite(Context context) {
            return (sLetSQLite = sLetSQLite == null ? new LetSQLite(context) : sLetSQLite);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);/*
            if (!db.isReadOnly()) {
                // Enable foreign key constraints
                db.execSQL("PRAGMA foreign_keys=ON;");
            }*/
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            String createSuggestionTable = String.format(
                    "create table if not exists %s (" +
                            "%s INTEGER primary key NOT NULL, " +
                            "%s text not null unique, " +
                            "%s INTEGER not null DEFAULT 1" +
                            ")",
                    Entry.TABLE_SUGGESTION,
                    Entry._ID,
                    Entry.KEYWORD,
                    Entry.WEIGHT
            );

            db.beginTransaction();
            try {
                db.execSQL(createSuggestionTable);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        private boolean insert(String table, String nullColumnHack, ContentValues values) {
            boolean isSuccess;
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            try {
                isSuccess = db.insert(table, nullColumnHack, values) != -1;
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            return isSuccess;
        }

        private boolean update(String table, ContentValues values, String whereClause, String[] whereArgs) {
            boolean isSuccess;
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            try {
                isSuccess = db.update(table, values, whereClause, whereArgs) > 0;
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            return isSuccess;
        }

        private long rawQueryWithAffectedRowCount(String rawUpdateQuery, String[] arg) {
            long affectedRowCount = 0;
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            try {
                SQLiteStatement st = db.compileStatement(rawUpdateQuery);
                /*int length = arg.length;
                for(int I = 1; I <= length; I++) {
                    st.bindString(I, arg[I]);
                }*/
                affectedRowCount = st.executeUpdateDelete();
                /*db.rawQuery(rawUpdateQuery, arg);
                Cursor cursor = db.rawQuery("SELECT changes() AS affected_row_count", null);
                if(cursor != null) {
                    if(cursor.moveToFirst()) {
                        affectedRowCount = cursor.getLong(cursor.getColumnIndex("affected_row_count"));
                    }
                    cursor.close();
                }*/
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            return affectedRowCount;
        }

        private Cursor query(String table, String[] projectionIn, String selection, String[] selectionArgs, String groupBy, String sortOrder, String limit) {
            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(table);
            Cursor cursor;
            SQLiteDatabase db = getReadableDatabase();
            db.beginTransaction();
            try {
                cursor = builder.query(db, projectionIn, selection, selectionArgs, groupBy, null, sortOrder, limit);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            return cursor;
        }

        private boolean delete(String table, String whereClause, String[] whereArgs) {
            boolean isSuccess;
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            try {
                isSuccess = db.delete(table, whereClause, whereArgs) > 0;
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            return isSuccess;
        }

        private Cursor rawQuery(String query) {
            Cursor cursor;
            SQLiteDatabase db = getReadableDatabase();
            db.beginTransaction();
            try {
                cursor = db.rawQuery(query, null);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            return cursor;
        }
    }
}
