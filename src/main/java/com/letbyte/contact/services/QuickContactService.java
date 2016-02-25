package com.letbyte.contact.services;

import android.Manifest;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.letbyte.contact.R;
import com.letbyte.contact.control.Constant;
import com.letbyte.contact.drawable.CircleTransform;
import com.letbyte.contact.utility.ContactUtility;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Asus on 2/6/2016.
 */
public class QuickContactService extends Service {

    private WindowManager windowManager;
    private List<ImageView> avatarHeadHolder = new ArrayList<>(Constant.MAXIMUM_CONTACT_HEAD_COUNT);

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.hasExtra(getString(R.string.contact_head))) {
            ArrayList<Long> contactIDList = (ArrayList<Long>) intent.getExtras().getSerializable(getString(R.string.contact_head));

            if(contactIDList != null) {

                Uri imageUri;
                ImageView contactAvatar;

                for(long contactID : contactIDList) {
                    imageUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactID);

                    contactAvatar = new ImageView(getApplicationContext());
                    Picasso.with(getApplicationContext())
                            .load(imageUri).
                            transform(new CircleTransform())
                            .resize(100, 100)
                            .centerCrop()
                            .into(contactAvatar);
                    avatarHeadHolder.add(contactAvatar);

                    WindowManager.LayoutParams params= new WindowManager.LayoutParams(
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.TYPE_PHONE,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            PixelFormat.TRANSLUCENT);

                    params.gravity = Gravity.TOP | Gravity.LEFT;//Gravity.START
                    params.x = 0;
                    params.y = 100;
                    //Try to set generic above loop

                    //this code is for dragging the chat head...can we ignore new here TODO
                    contactAvatar.setOnTouchListener(new QCTouchListener(contactAvatar, params, contactID));
                    windowManager.addView(contactAvatar, params);
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (avatarHeadHolder != null) {
            for (ImageView chatHead : avatarHeadHolder) {
                windowManager.removeView(chatHead);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class QCTouchListener implements View.OnTouchListener, View.OnClickListener {
        private int initialX;
        private int initialY;
        private float initialTouchX;
        private float initialTouchY;
        private WindowManager.LayoutParams params;
        private ImageView contactAvatar;
        private long contactID;

        QCTouchListener(ImageView contactAvatar, WindowManager.LayoutParams params, long contactID) {
            this.contactAvatar = contactAvatar;
            this.params = params;
            this.contactID = contactID;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = params.x;
                    initialY = params.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    return true;
                case MotionEvent.ACTION_UP:
                    return true;
                case MotionEvent.ACTION_MOVE:
                    params.x = initialX
                            + (int) (event.getRawX() - initialTouchX);
                    params.y = initialY
                            + (int) (event.getRawY() - initialTouchY);
                    windowManager.updateViewLayout(contactAvatar, params);
                    return true;
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            makeCall(contactID);
        }

        private boolean makeCall(long contactID) {
            String number = new ContactUtility(getApplicationContext()).getNumberfromContactID(contactID);
            if (number == null)
                return false;
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (getActivity().checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }*/
            startActivity(intent);
            return true;
        }
    }

}
