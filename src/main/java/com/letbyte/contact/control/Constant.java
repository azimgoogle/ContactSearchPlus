package com.letbyte.contact.control;

import com.letbyte.contact.data.model.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nuc on 7/11/2015.
 */
public interface Constant {

    String EMPTY_STRING = "";
    String COMMA_STRING = "";
    int DISPLAY_NAME = 0;
    int PHONE_NUMBER = 1;
    int MAIL_ADDRESS = 2;
    int ADDRESS = 3;
    int NOTES = 4;
    int ORGANIZATION = 5;
    int RELATION = 6;

    int REQUESTCODE_SETTINGS = 10;
    String PHONE_NUMBER_REG_EX = "[^0-9]+";//\\d+
    Map<Long, Integer> cIDArrayListIndexMap = new HashMap<>();
    List<Contact> contactModelList = new ArrayList<>();

    String DEVICE_ID = "device_id";
    String BOOT_SYNCED = "boot_synced";
    String SYNCED = "synced";
    String TASK = "task";
    String SYNC = "sync";
    String CONFIG = "config";
    String CODE = "code";
    int SUCCESS = 1;
    String BFILTER_BY_NUMBER = "filter_by_number";
    String BNUMBER = "number";
    String BEMAIL = "email";
    String BADDRESS = "address";
    String BNOTES = "notes";
    String BORGANIZATION = "org";
    String BRELATION = "relation";
}