package com.letbyte.callblock.control;

/**
 * Created by nuc on 7/11/2015.
 */
public interface Constant {

    /*Runtime permission request codes*/
    int REQUEST_CODE_PERMISSION_READ_CALL_LOG = 111;


    String SERVICE_CODE_KEY = "SERVICE_CODE_KEY";
    String IS_SERVICE_CODE_GENERATION_SUCCESS_KEY = "IS_SERVICE_CODE_GENERATION_SUCCESS_KEY";
    String SERVICE_CODE_ITERATOR_INDEX_KEY = "SERVICE_CODE_ITERATOR_INDEX_KEY";
    String SHARED_PREFERENCE_NAME_KEY = "MY_SP";

    int MILLI = 1000;
    int SECOND = 1 * MILLI;
    int MINUTE = 60 * SECOND;
    int HOUR = 60 * MINUTE;
    int FEED_SYNC_DELAY = MINUTE;


    String BORN = "born";

    int ACTIVITY_REQUEST = 1;

    String EMPTY = "";

    enum Task {
        LOGIN, SYNC, ACK
    }

    enum Code {
        SUCCESS, FAILED
    }


    String WORD = "word";


    String TAG_SEP = " ";

    String NAME = "name";
    String NUMBER = "number";
    String EMAIL = "email";
    String PASSWORD = "password";

    String ID = "id";
    String TIME = "time";
    String VALUE = "value";
    String TYPE = "type";
    String CONTENT = "content";
    String TAG = "tag";
    String LEVEL = "level";
    String RANK = "rank";

    String SYNC_ID = "sync_id";
    String SYNC_TIME = "sync_time";
    String SYNCED = "synced";
    String MARKED = "marked";
    String TRACK_VALUE = "track_value";


    String TYPE_SYNC = "sync";
    String CODE = "code";
    String MODE = "mode";
    String POST = "post";

    /*note information*/




    int LOGIN_SUCCESS = 1;
    int LOGIN_FAILED = -1;

    int SYNC_SUCCESS = 2;
    int SYNC_FAILED = -2;
}