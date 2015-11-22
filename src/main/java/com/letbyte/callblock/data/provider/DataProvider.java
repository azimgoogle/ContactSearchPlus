package com.letbyte.callblock.data.provider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;

import com.letbyte.callblock.control.Util;
import com.letbyte.callblock.data.model.Block;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class DataProvider {

    private static abstract class Entry implements android.provider.BaseColumns {
        private static final String TABLE_BLOCK = "block";

        private static final String ID = "_id";
        private static final String TIME = "time";
        private static final String DISPLAY_NAME = "display_name";
        private static final String NUMBER = "number";
    }

    private Context context;

    private DataProvider(Context context) {
        this.context = context;
    }

    private static DataProvider sData;

    public synchronized static DataProvider onProvider(Context context) {
        return (sData = sData == null ? new DataProvider(context) : sData);
    }

    public void deleteBlock(Block block) {
        String table = Entry.TABLE_BLOCK;

        String selection = Entry.NUMBER + " = ?";
        String[] selectionArgs = new String[]{block.getNumber()};

        LetSQLite.onSQLite(context).delete(table, selection, selectionArgs);
    }

    public List<Block> getContacts() {
        List<Block> blocks = null;

        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " COLLATE NOCASE ASC";
        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Contacts._ID},
                ContactsContract.Contacts.HAS_PHONE_NUMBER + " = ?", new String[]{"1"}, sortOrder);


        if (cursor != null && cursor.moveToFirst()) {
            blocks = new ArrayList<>();

            final int contactIDIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID);
            final int displayNameIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);

            do {

                Block block = new Block();
                blocks.add(block);

                String displayName = cursor.getString(displayNameIndex);

                //          block.setNumber(number);
                block.setDisplayName(displayName);

            } while (cursor.moveToNext());

        }

        if (cursor != null) {
            cursor.close();
        }

        return blocks == null ? new ArrayList<Block>() : blocks;
    }

    public List<Block> getCallLogs(Activity activity, int permissionRequestCode) {
        List<Block> blocks = null;

        String srtOrder = CallLog.Calls.DATE + " DESC";
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    permissionRequestCode);

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CALL_LOG)) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CALL_LOG}, permissionRequestCode);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CALL_LOG}, permissionRequestCode);
            }

            return null;
        }

        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, srtOrder);

        if (cursor != null && cursor.moveToFirst()) {
            blocks = new ArrayList<>();

            final int contactIDIndex = cursor.getColumnIndexOrThrow(CallLog.Calls._ID);
            final int displayNameIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME);
            final int callTypeIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE);

            do {

                Block block = new Block();
                blocks.add(block);

                String displayName = cursor.getString(displayNameIndex);

                block.setDisplayName(displayName);

            } while (cursor.moveToNext());

        }

        if (cursor != null) {
            cursor.close();
        }

        return blocks == null ? new ArrayList<Block>() : blocks;
    }

    public String isNumberBlocked(String number) {
        String displayName = null;
        String query;
        if (number.length() > 7) {//Assumed that international number's digit is at least of 8 digit
            query = "SELECT " + Entry.DISPLAY_NAME + ", " + Entry.NUMBER + " FROM " +
                    Entry.TABLE_BLOCK + " WHERE ( LENGTH(REPLACE(" + Entry.NUMBER + ", '" + number +
                    "', '')) < 4 OR LENGTH(REPLACE('" + number + "', " + Entry.NUMBER + ", '')) < 4 ) LIMIT 1";
        } else {//Particularly for operator annoying numbers like, 4000, 5555
            query = "SELECT " + Entry.DISPLAY_NAME + ", " + Entry.NUMBER + " FROM " +
                    Entry.TABLE_BLOCK + " WHERE (" + Entry.NUMBER + " = " + number + ") LIMIT 1";
        }
        //Keep track of CC so that for each row only one of abpove OR condition will excute
        //And that will ensure more certainity
        Cursor cursor = LetSQLite.onSQLite(context).rawQuery(query);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                final int displayNameIndex = cursor.getColumnIndex(Entry.DISPLAY_NAME);
                final int numberIndex = cursor.getColumnIndex(Entry.NUMBER);
                displayName = cursor.getString(displayNameIndex);
                //If no name available then send number
                if (displayName == null || displayName.length() < 1) {
                    displayName = cursor.getString(numberIndex);
                }
            }
            cursor.close();
        }
        return displayName;
    }


    public synchronized List<Block> getBlocks() {
        String table = Entry.TABLE_BLOCK;

        String[] projectionIn = new String[]{
                Entry.ID,
                Entry.TIME,
                Entry.DISPLAY_NAME,
                Entry.NUMBER
        };

        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String sortOrder = null;

        String limit = null;

        Cursor cursor = LetSQLite.onSQLite(context).query(table, projectionIn, selection, selectionArgs, groupBy, sortOrder, limit);

        List<Block> blocks = null;

        if (cursor != null && cursor.moveToFirst()) {
            blocks = new ArrayList<>();
            do {

                Block block = new Block();
                blocks.add(block);

                int id = cursor.getInt(cursor.getColumnIndexOrThrow(Entry.ID));
                long time = cursor.getLong(cursor.getColumnIndexOrThrow(Entry.TIME));
                String number = cursor.getString(cursor.getColumnIndexOrThrow(Entry.NUMBER));
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(Entry.DISPLAY_NAME));

                block.setId(id);
                block.setTime(time);
                block.setNumber(number);
                block.setDisplayName(displayName);

            } while (cursor.moveToNext());

        }

        if (cursor != null) {
            cursor.close();
        }

        return blocks == null ? new ArrayList<Block>() : blocks;
    }


    public synchronized <T> boolean iu(T... t) {

        if (t == null || t.length == 0) return false;

        for (int i = 0; i < t.length; i++) {
            if (Block.class.isInstance(t[i])) {
                iuBlock((Block) t[i]);
            }
        }

        return true;
    }

    private synchronized boolean iuBlock(Block block) {

        String displayName = ContentResolver.onResolver(context).getNameByContactNumber(block.getNumber());
        block.setDisplayName(displayName);

        boolean isSuccess;

        String selection = Entry.NUMBER + " = ?";
        String[] selectionArgs = new String[]{block.getNumber()};

        int id = LetSQLite.onSQLite(context).getId(null, Entry.TABLE_BLOCK, selection, selectionArgs);

        if (id == 0) {

            id = LetSQLite.onSQLite(context).buildId(Entry.TABLE_BLOCK);

            block.setId(id);
            block.setTime(Util.getCurrentTime());

            ContentValues values = new ContentValues();
            values.put(Entry.ID, block.getId());
            values.put(Entry.TIME, block.getTime());
            values.put(Entry.NUMBER, block.getNumber());
            values.put(Entry.DISPLAY_NAME, block.getDisplayName());

            isSuccess = LetSQLite.onSQLite(context).insert(Entry.TABLE_BLOCK, null, values);

        } else {

            block.setId(id);
            block.setTime(Util.getCurrentTime());

            ContentValues values = new ContentValues();
            values.put(Entry.TIME, block.getTime());
            values.put(Entry.NUMBER, block.getNumber());
            values.put(Entry.DISPLAY_NAME, block.getDisplayName());

            String whereClause = Entry.ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(block.getId())};

            isSuccess = LetSQLite.onSQLite(context).update(Entry.TABLE_BLOCK, values, whereClause, whereArgs);
        }

        return isSuccess;
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
            super.onOpen(db);
            if (!db.isReadOnly()) {
                // Enable foreign key constraints
                db.execSQL("PRAGMA foreign_keys=ON;");
            }
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            String createBlock = String.format(
                    "create table %s (" +
                            "%s int not null primary key, " +
                            "%s int not null, " +
                            "%s text, " +
                            "%s text" +
                            ")",
                    Entry.TABLE_BLOCK,
                    Entry.ID,
                    Entry.TIME,
                    Entry.NUMBER,
                    Entry.DISPLAY_NAME
            );

            db.beginTransaction();
            try {
                db.execSQL(createBlock);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        private int getId(SQLiteDatabase db, String table, String selection, String[] selectionArgs) {
            int id = 0;
            db = db == null ? getReadableDatabase() : db;
            db.beginTransaction();
            try {
                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setTables(table);
                String[] projectionIn = new String[]{DataProvider.Entry.ID};
                Cursor cursor = builder.query(db, projectionIn, selection, selectionArgs, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(DataProvider.Entry.ID));
                }
                cursor.close();
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            return id;
        }


        private int buildId(String table) {
            int id = 1;
            String[] columns = new String[]{DataProvider.Entry.ID};
            String sortOrder = DataProvider.Entry._ID + " ASC";
            Cursor cursor = query(table, columns, null, null, null, sortOrder, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        int key = cursor.getInt(cursor.getColumnIndexOrThrow(DataProvider.Entry.ID));
                        if (key - id > 0) break;
                        id++;
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }

            return id;
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

    /**
     * Android content data
     */
    private static final class ContentResolver {
        private static Context sContext;
        private static ContentResolver sContentResolver;

        private ContentResolver(Context context) {
            sContext = context;
        }

        public synchronized static ContentResolver onResolver(Context context) {
            return (sContentResolver = sContentResolver == null ? new ContentResolver(context) : sContentResolver);
        }

        /**
         * @param number
         * @return Number if display name is not available, otherwise return number
         */
        private synchronized String getNameByContactNumber(String number) {

            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

            Cursor cursor = sContext.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

            if (cursor == null) return null;

            String displayName = null;

            if (cursor.moveToFirst()) {
                displayName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }

            cursor.close();

            return displayName == null ? number : displayName;
        }
    }
}
