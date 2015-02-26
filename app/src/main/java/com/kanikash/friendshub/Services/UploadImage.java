package com.kanikash.friendshub.Services;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.kanikash.friendshub.Models.Place;
import com.kanikash.friendshub.R;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;

import bolts.Task;

public class UploadImage extends IntentService {
    public static final String ACTION = "com.kanikash.friendshub.Services.UploadImage";

    public UploadImage() {
        super("image-upload-service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String imgPath = intent.getStringExtra("image_path");
        final String imgCaption = intent.getStringExtra("image_caption");
        Location location = intent.getParcelableExtra("location");
        int display_width = intent.getIntExtra("display_width", 0);
        int display_height = intent.getIntExtra("display_height", 0);
        final ParseGeoPoint geoPoint = (location != null) ? new ParseGeoPoint(location.getLatitude(), location.getLongitude()) : null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(Uri.parse(imgPath).getPath(), options);
        options.inSampleSize = CalculateInSampleSize(options, display_width, display_height);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(Uri.parse(imgPath).getPath(), options);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap bitmapR = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        bitmap = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmapR.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bitmapR.recycle();
        bitmapR = null;
        byte[] imageByteData = bos.toByteArray();

        final ParseFile file = new ParseFile("MomentsPhoto.jpg", imageByteData);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    Log.i("INFO", "image upload failed");
                    Intent in = new Intent(ACTION);
                    in.putExtra("resultCode", Activity.RESULT_CANCELED);
                    in.putExtra("resultValue", "Image upload failed");
                    // Fire the broadcast with intent packaged
                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(in);
                } else {
                    Place place = new Place();
                    place.setPhotoFile(file);
                    place.setCaption(imgCaption);
                    place.setLocation(geoPoint);
                    place.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.i("INFO", "data creation failed");
                                Intent in = new Intent(ACTION);
                                in.putExtra("resultCode", Activity.RESULT_CANCELED);
                                in.putExtra("resultValue", "Data upload failed");
                                // Fire the broadcast with intent packaged
                                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(in);
                            } else {
                                Intent in = new Intent(ACTION);
                                in.putExtra("resultCode", Activity.RESULT_OK);
                                in.putExtra("resultValue", "Data upload successful");
                                // Fire the broadcast with intent packaged
                                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(in);
                                // Broadcast the failure message
                            }
                        }
                    });
                    // Broadcast the failure message
                }
            }
        });
    }

    private  BitmapFactory.Options GetBitmapOptionsOfImageAsync()
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // The result will be null because InJustDecodeBounds == true.
        Bitmap result=  BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.ic_launcher, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        return options;
    }

    public static int CalculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        reqWidth = reqWidth - 200;
        reqHeight = reqHeight - 200;
        // Raw height and width of image
        float height = options.outHeight;
        float width = options.outWidth;
        double inSampleSize = 1D;

        if (height > reqHeight || width > reqWidth)
        {
            int halfHeight = (int)(height / 2);
            int halfWidth = (int)(width / 2);

            // Calculate a inSampleSize that is a power of 2 - the decoder will use a value that is a power of two anyway.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth)
            {
                inSampleSize *= 2;
            }
        }
        return (int)inSampleSize;
    }
}
