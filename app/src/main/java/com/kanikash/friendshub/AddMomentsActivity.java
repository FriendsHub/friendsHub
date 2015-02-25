package com.kanikash.friendshub;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kanikash.friendshub.Models.Place;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        place = new Place();

        // We don't need action bar here
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_moments);

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
        btSave = (Button) findViewById(R.id.btSave);
        btCancel = (Button) findViewById(R.id.btCancel);

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

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
        Uri takenPhotoUri = getPhotoFileUri(photoFileName);
        // by this point we have photo on disk
        Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        takenImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        byte[] imageByteData = bos.toByteArray();

        // TODO: Scale the image:
        photoFile = new ParseFile(photoFileName, imageByteData);
        photoFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(AddMomentsActivity.this,
                            "Error saving: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                } else {
                    addPhotoToPlace(photoFile);
                }
            }
        });
        // Load the taken image into a preview
        pivPreview.setParseFile(photoFile);
        pivPreview.loadInBackground();
    }

    private void addPhotoToPlace(ParseFile photoFile) {
        place.setPhotoFile(photoFile);
    }

    public void onSave(View view) {
        place.setCaption(etCaption.getText().toString());
        place.setAuthor(ParseUser.getCurrentUser());
        place.setLocation(geoPoint);

        place.saveInBackground( new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    AddMomentsActivity.this.setResult(Activity.RESULT_OK);
                    AddMomentsActivity.this.finish();
                }
                else {
                    Toast.makeText(AddMomentsActivity.this.getApplicationContext(),
                            "Error saving: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void onCancel(View view) {
        this.setResult(Activity.RESULT_CANCELED);
        this.finish();
    }

}
