package com.programming.kantech.deliveryservice.app.driver.views.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;
import com.programming.kantech.deliveryservice.app.utils.Constants;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by patrick keogh on 2017-09-14.
 * NOT BEING USED YET*********************
 *
 */

public class Activity_Photo extends AppCompatActivity {

    private Driver mDriver;

    // Firebase references
    private StorageReference mDriverPhotoStorageRef;
    private DatabaseReference mDriverRef;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.iv_driver_photo)
    ImageView mImageView;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mProgressBar;

    @BindView(R.id.layout_photo_uploading)
    LinearLayout mLayoutLoading;

    @BindView(R.id.layout_photo_showing)
    LinearLayout mLayoutShowing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_photo);

        if (savedInstanceState != null) {

            if (savedInstanceState.containsKey(Constants.STATE_INFO_DRIVER)) {
                mDriver = savedInstanceState.getParcelable(Constants.STATE_INFO_DRIVER);
            }

        } else {
            mDriver = getIntent().getParcelableExtra(Constants.EXTRA_DRIVER);
        }

        ButterKnife.bind(this);

        if (mDriver == null) {
            throw new IllegalArgumentException("Must pass EXTRA_DRIVER");
        } else {
            if (!Objects.equals(mDriver.getThumbUrl(), "")) {

                //Log.i(Constants.LOG_TAG, "Load the photo with Picasso:" + mDriver.getPhotoUrl());

                Glide.with(Activity_Photo.this).load(mDriver.getThumbUrl()).dontAnimate().into(mImageView);
            }


        }

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        ActionBar mActionBar = this.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setTitle("Driver Photo");
        }

        mDriverPhotoStorageRef = FirebaseStorage.getInstance().getReference().child(Constants.FIREBASE_NODE_DRIVER_PHOTOS);
        mDriverRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_DRIVERS).child(mDriver.getUid());
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_SELECT_PHOTO && resultCode == RESULT_OK) {
            Log.i(Constants.LOG_TAG, "We got a photo:");

            Uri selectedImageUri = data.getData();


            // Get a reference to store file at driver_photos/<FILENAME>
            StorageReference photoRef = mDriverPhotoStorageRef.child(selectedImageUri.getLastPathSegment());


            mLayoutShowing.setVisibility(View.GONE);
            mLayoutLoading.setVisibility(View.VISIBLE);

            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setCustomMetadata("uid", mDriver.getUid())
                    .build();

            // Upload file to Firebase Storage

            photoRef.putFile(selectedImageUri, metadata)
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            // When the image has successfully uploaded, we get its download URL
                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            //final StorageMetadata metadata = taskSnapshot.getMetadata();

                            //metadata.getName();

                            // Set the download URL to the message box, so that the user can send it to the database
                            assert downloadUrl != null;

                            //final StorageReference thumbRef = mDriverPhotoStorageRef.child("thumb_" + metadata.getName());

                            //Log.i(Constants.LOG_TAG, "ThumbUrl:" + thumbRef.toString());

                            //thumbRef.getDownloadUrl()

                            mLayoutLoading.setVisibility(View.GONE);
                            mLayoutShowing.setVisibility(View.VISIBLE);

                            mDriver.setPhotoUrl(downloadUrl.toString());
                            //mDriver.setThumbUrl(uri.toString());
                            mDriverRef.setValue(mDriver);
                            //Log.i(Constants.LOG_TAG, "Photo was saved to firebase storage:" + mDriver.getPhotoUrl());

                            Glide.with(Activity_Photo.this)
                                    .load(mDriver.getPhotoUrl())
                                    .error(R.drawable.ic_menu_drive)
                                    .placeholder(R.drawable.ic_attach_money)
                                    .dontAnimate().into(mImageView);


//                            final Handler handler = new Handler();
//                            handler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    thumbRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                        @Override
//                                        public void onSuccess(Uri uri) {
//
//                                            Uri thumbUrl = uri;
//
//                                            Log.i(Constants.LOG_TAG, "ThumbUrl:" + thumbUrl);
//                                            mLayoutLoading.setVisibility(View.GONE);
//                                            mLayoutShowing.setVisibility(View.VISIBLE);
//
//                                            mDriver.setPhotoUrl(downloadUrl.toString());
//                                            mDriver.setThumbUrl(uri.toString());
//                                            mDriverRef.setValue(mDriver);
//                                            Log.i(Constants.LOG_TAG, "Photo was saved to firebase storage:" + mDriver.getPhotoUrl());
//
//                                            Glide.with(Activity_Photo.this).load(mDriver.getThumbUrl()).error(R.drawable.ic_menu_drive).placeholder(R.drawable.ic_attach_money).dontAnimate().into(mImageView);
//
//
//                                        }
//                                    }).addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception exception) {
//                                            Log.i(Constants.LOG_TAG, "We got an error getting the thumb:" + exception.toString());
//                                        }
//                                    });
//                                }
//                            }, 5000);


                        }


                    });

        }
    }

    @OnClick(R.id.btn_driver_photo_upload)
    public void select_photo() {

//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/jpeg");
//        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//        startActivityForResult(Intent.createChooser(intent, "Complete action using"), Constants.REQUEST_CODE_SELECT_PHOTO);

    }


}
