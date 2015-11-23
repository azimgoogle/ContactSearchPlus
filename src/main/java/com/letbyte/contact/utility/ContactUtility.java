package com.letbyte.contact.utility;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.letbyte.contact.control.Constant;

public class ContactUtility {
	private Context mContext;
	
	public ContactUtility(Context context) {
		mContext = context;
	}

	public String getNumberfromContactID(long contactID) {
		Cursor cursor = null;
		String number = null;
		try {
			cursor = mContext.getContentResolver().query(Phone.CONTENT_URI, new String[]{Phone.NUMBER, Phone.IS_SUPER_PRIMARY}, Phone.CONTACT_ID + " = ?",
					new String[]{Constant.EMPTY_STRING + contactID}, null);
			if(cursor != null) {
				if(cursor.moveToFirst()) {
					final int numberColumn = cursor.getColumnIndex(Phone.NUMBER);
					final int superPrimaryColumn = cursor.getColumnIndex(Phone.IS_SUPER_PRIMARY);
					number = cursor.getString(numberColumn);
					for(; !cursor.isAfterLast(); cursor.moveToNext()) {
						if(cursor.getInt(superPrimaryColumn) != 0) {
							number = cursor.getString(numberColumn);
							break;
						}
					}
				}
				cursor.close();
			}
		} catch(Exception ex) {
			ex.toString();
		}
		return number;
	}
}
