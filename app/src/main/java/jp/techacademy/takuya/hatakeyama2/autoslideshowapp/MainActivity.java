package jp.techacademy.takuya.hatakeyama2.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    Cursor cursor;
    ImageView imageView;
    Button nextButton;
    Button backButton;
    Button startStopButton;
    List<Uri> uriList = new ArrayList<>();
    int listSize;
    int currentListIndex = -1;
    Timer mTimer;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                readPictures();
                findViews();
                setListeners();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    private void readPictures() {
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );
        if (cursor.moveToFirst()) {
            do {
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                uriList.add(imageUri);
            } while (cursor.moveToNext());
        } else {
            Toast.makeText(this, R.string.cannot_get_data, Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        listSize = uriList.size();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    readPictures();
                    findViews();
                    setListeners();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void findViews() {
        imageView = (ImageView) findViewById(R.id.imageview);
        nextButton = (Button) findViewById(R.id.next_button);
        backButton = (Button) findViewById(R.id.back_button);
        startStopButton = (Button) findViewById(R.id.start_stop_button);
    }

    private void setListeners() {
        nextButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        startStopButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.next_button:
                setNextIndex();
                imageView.setImageURI(uriList.get(currentListIndex));
                break;
            case R.id.back_button:
                setBeforeIndex();
                imageView.setImageURI(uriList.get(currentListIndex));
                break;
            case R.id.start_stop_button:
                if (mTimer == null) {
                    startStopButton.setText(R.string.stop_button_text);
                    nextButton.setEnabled(false);
                    backButton.setEnabled(false);
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    setNextIndex();
                                    imageView.setImageURI(uriList.get(currentListIndex));
                                }
                            });
                        }
                    }, 0, 2000);
                } else {
                    stopAutoSlideshow();
                    nextButton.setEnabled(true);
                    backButton.setEnabled(true);
                }
        }
    }

    private void setNextIndex() {
        if (++currentListIndex == listSize) {
            currentListIndex = 0;
        }
    }

    private void setBeforeIndex() {
        if (--currentListIndex <= -1) {
            currentListIndex = listSize - 1;
        }
    }

    private void stopAutoSlideshow() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            startStopButton.setText(R.string.start_button_text);
        }
    }
}