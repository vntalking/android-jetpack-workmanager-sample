package com.example.workmanager;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;

import java.util.List;

import androidx.work.Data;
import androidx.work.WorkInfo;

public class MainActivity extends AppCompatActivity {
    private BlurViewModel mViewModel;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private Button mGoButton, mOutputButton, mCancelButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the ViewModel
        mViewModel = ViewModelProviders.of(this).get(BlurViewModel.class);

        // Get all of the Views
        mImageView = findViewById(R.id.image_view);
        mProgressBar = findViewById(R.id.progress_bar);
        mGoButton = findViewById(R.id.go_button);
        mOutputButton = findViewById(R.id.see_file_button);
        mCancelButton = findViewById(R.id.cancel_button);

        // Image uri should be stored in the ViewModel; put it there then display
        Intent intent = getIntent();
        String imageUriExtra = intent.getStringExtra(Constants.KEY_IMAGE_URI);
        mViewModel.setImageUri(imageUriExtra);
        if (mViewModel.getImageUri() != null) {
            Glide.with(this).load(mViewModel.getImageUri()).into(mImageView);
        }

        // Setup blur image file button
        mGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewModel.applyBlur(MainActivity.this.getBlurLevel());
            }
        });

        // Setup view output image file button
        mOutputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri currentUri = mViewModel.getOutputUri();
                if (currentUri != null) {
                    Intent actionView = new Intent(Intent.ACTION_VIEW, currentUri);
                    if (actionView.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                        MainActivity.this.startActivity(actionView);
                    }
                }
            }
        });

        // Hookup the Cancel button
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewModel.cancelWork();
            }
        });


        // Show work status
        mViewModel.getOutputWorkInfo().observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(@Nullable List<WorkInfo> listOfWorkInfo) {

                // Note that these next few lines grab a single WorkInfo if it exists
                // This code could be in a Transformation in the ViewModel; they are included here
                // so that the entire process of displaying a WorkInfo is in one location.

                // If there are no matching work info, do nothing
                if (listOfWorkInfo == null || listOfWorkInfo.isEmpty()) {
                    return;
                }

                // We only care about the one output status.
                // Every continuation has only one worker tagged TAG_OUTPUT
                WorkInfo workInfo = listOfWorkInfo.get(0);

                boolean finished = workInfo.getState().isFinished();
                if (!finished) {
                    MainActivity.this.showWorkInProgress();
                } else {
                    MainActivity.this.showWorkFinished();

                    // Normally this processing, which is not directly related to drawing views on
                    // screen would be in the ViewModel. For simplicity we are keeping it here.
                    Data outputData = workInfo.getOutputData();

                    String outputImageUri =
                            outputData.getString(Constants.KEY_IMAGE_URI);

                    // If there is an output file show "See File" button
                    if (!TextUtils.isEmpty(outputImageUri)) {
                        mViewModel.setOutputUri(outputImageUri);
                        mOutputButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private void showWorkInProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
        mCancelButton.setVisibility(View.VISIBLE);
        mGoButton.setVisibility(View.GONE);
        mOutputButton.setVisibility(View.GONE);
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private void showWorkFinished() {
        mProgressBar.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);
        mGoButton.setVisibility(View.VISIBLE);
    }

    /**
     * Get the blur level from the radio button as an integer
     * @return Integer representing the amount of times to blur the image
     */
    private int getBlurLevel() {
        RadioGroup radioGroup = findViewById(R.id.radio_blur_group);

        switch(radioGroup.getCheckedRadioButtonId()) {
            case R.id.radio_blur_lv_1:
                return 1;
            case R.id.radio_blur_lv_2:
                return 2;
            case R.id.radio_blur_lv_3:
                return 3;
        }

        return 1;
    }
}
