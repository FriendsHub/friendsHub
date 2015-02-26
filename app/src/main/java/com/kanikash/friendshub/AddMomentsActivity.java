package com.kanikash.friendshub;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kanikash.friendshub.Models.Place;
import com.kanikash.friendshub.Services.UploadImage;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


public class AddMomentsActivity extends ActionBarActivity {
    private static final int CAMERA_IMPLICIT_INTENT_REQUEST_CODE = 1;
    private static final String TAG = AddMomentsActivity.class.getSimpleName();
    public String photoFileName = "MomentsPhoto.jpg";

    private Place place;
    private ParseFile photoFile;
    private Location loc;
    private ParseGeoPoint geoPoint;

    private EditText etCaption;
    private Button btSave;
    private ParseImageView pivPreview;
    private Button btCancel;
    private int width;
    private int height;
    private Bitmap bitmapR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        place = new Place();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_moments);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setTitle("Moment");
        loc = getIntent().getParcelableExtra("Location");
        if(loc != null)
            geoPoint = new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
        else
            geoPoint = null;
        // call implicit intent to capture photo
        capturePhoto();
        setupViews();
    }

    private void capturePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName)); // set the image file name
        // Start the image capture intent to take photo
        startActivityForResult(intent, CAMERA_IMPLICIT_INTENT_REQUEST_CODE);
    }

    private void setupViews() {
        etCaption = (EditText) findViewById(R.id.etCaption);
        pivPreview = (ParseImageView) findViewById(R.id.pivPreview);
        //btSave = (Button) findViewById(R.id.btSave);
        Display mDisplay = getWindowManager().getDefaultDisplay();
        width  = mDisplay.getWidth();
        height = mDisplay.getHeight();
        //btCancel = (Button) findViewById(R.id.btCancel);

    }

    // Returns the Uri for a photo stored on disk given the fileName
    public Uri getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_moments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_IMPLICIT_INTENT_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                handlePhotoResult();
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handlePhotoResult() {
        // Load the taken image into a preview
        // Display image in the image preview
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(getPhotoFileUri(photoFileName).getPath(), options);
        options.inSampleSize = CalculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(getPhotoFileUri(photoFileName).getPath(), options);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        bitmapR = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        bitmap = null;
        pivPreview.setImageBitmap(bitmapR);
        /*pivPreview.setParseFile(photoFile);
        pivPreview.loadInBackground();*/
    }

    public void onCancel(View view) {
        this.setResult(Activity.RESULT_CANCELED);
        this.finish();
    }

    public void onSave(MenuItem mi) {
        bitmapR.recycle();
        bitmapR = null;
        Uri takenPhotoUri = getPhotoFileUri(photoFileName);
        // Create the service Intent
        Intent i = new Intent(this, UploadImage.class);
        i.putExtra("image_path", takenPhotoUri.toString());
        i.putExtra("image_caption", etCaption.getText().toString());
        i.putExtra("location", loc);
        i.putExtra("display_width", width);
        i.putExtra("display_height", height);
        startService(i);

        this.finish();
    }

    public static int CalculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
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
