package ch.orbitsapps.imative;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.*;

public class MainActivity extends AppCompatActivity {

    private static final int PERM_REQ_OPEN_CODE = 777;
    private static final int PERM_REQ_SAVE_CODE = 77;
    private static final int CHOOSE_REQ_CODE = 888;
    private Button btnOpen, btnApply, btnSave;
    private ImageView imgView;
    private Spinner filterChoice;
    private EditText filterSetting;
    private Uri openUri;
    private IFilter filter = null;
    private ImageData imgData = null;
    private Bitmap img = null;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnOpen = findViewById(R.id.button_open_image);
        btnApply = findViewById(R.id.button_apply);
        btnSave = findViewById(R.id.button_save_image);
        imgView = findViewById(R.id.image_display);
        filterChoice = findViewById(R.id.filter_choice);
        filterSetting = findViewById(R.id.filter_setting);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(R.layout.dialog_progress);
        progressDialog = builder.create();
        progressDialog.hide();

        AddButtonListeners();
    }

    private void AddButtonListeners() {
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnOpenImage();
            }
        });

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnApply();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnSaveImage();
            }
        });
    }

    private void ShowPermissionToast() {
        Toast.makeText(this,"Please grant storage access", Toast.LENGTH_SHORT).show();
    }

    private void ShowErrorToast() {
        Toast.makeText(this,"Error :(", Toast.LENGTH_SHORT).show();
    }

    private void ShowSaveToast() {
        Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show();
    }

    private void OnOpenImage() {
        if(GetPermission(PERM_REQ_OPEN_CODE)) {
            ChooseImage();
        } else {
            ShowPermissionToast();
        }
    }

    private void OnSaveImage() {
        if(GetPermission(PERM_REQ_SAVE_CODE)) {
            try {
                Uri uri = CreateSaveFile();
                SaveImage(uri);
            } catch (Exception ex) {
                ShowErrorToast();
            }
        } else {
            ShowPermissionToast();
        }
    }

    private Uri CreateSaveFile() {
        ContentValues values = new ContentValues();
        String filename = System.currentTimeMillis() + ".jpg";
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DESCRIPTION, "hello");
        if (Build.VERSION.SDK_INT >= 29) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Imative");
        }
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
    }

    private void SaveImage(Uri uri) throws FileNotFoundException {
        OutputStream os;
        if(Build.VERSION.SDK_INT >= 29) {
            os = getContentResolver().openOutputStream(uri);
            img.compress(Bitmap.CompressFormat.JPEG,100,os);
        } else {
            File album = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"Imative");
            album.mkdirs();
            File photo = new File(album.getPath(),System.currentTimeMillis() + ".jpg");
            os = new FileOutputStream(photo);
            img.compress(Bitmap.CompressFormat.JPEG,100,os);
            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(photo);
            scanIntent.setData(contentUri);
            sendBroadcast(scanIntent);
        }
        ShowSaveToast();
    }

    private boolean GetPermission(int code) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},code);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERM_REQ_OPEN_CODE:
                for(int i = 0; i < permissions.length; ++i) {
                    if(permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED) ChooseImage();
                    else if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) ShowPermissionToast();
                }
                break;
            case PERM_REQ_SAVE_CODE:
                for(int i = 0; i < permissions.length; ++i) {
                    if(permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED) CreateSaveFile();
                    else if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) ShowPermissionToast();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void ChooseImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i,"Select image"),CHOOSE_REQ_CODE);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final @Nullable Intent data) {
        if(requestCode == CHOOSE_REQ_CODE && resultCode == RESULT_OK && data != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Uri chosen_uri = data.getData();
                    if(chosen_uri != null) {
                        openUri = chosen_uri;
                        PrepareImage();
                        imgView.post(new Runnable() {
                            @Override
                            public void run() {
                                imgView.setImageURI(chosen_uri);
                            }
                        });
                    }
                }
            }).start();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void OnApply() {
        InitFilter();
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    imgData = filter.applyTo(imgData)[0];
                    imgData.applyToImage(img);

                    imgView.post(new Runnable() {
                        @Override
                        public void run() {
                            imgView.setImageBitmap(img);
                        }
                    });
                } catch (Exception ex){
                    ShowErrorToast();
                }
                imgView.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.hide();
                    }
                });
            }
        }).start();
    }

    private void PrepareImage() {
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inMutable = true;
            img = BitmapFactory.decodeStream(getContentResolver().openInputStream(openUri),null,opt);
            if(img == null) throw new Exception("Image was null");
            imgData = new ImageData(img);
        } catch (Exception ex) {
            ShowErrorToast();
        }
    }

    private void InitFilter() {
        int setting;
        if(filterSetting.getText().toString().isEmpty()) setting = 40;
        else setting = Integer.parseInt(filterSetting.getText().toString());
        switch ((byte)filterChoice.getSelectedItemId()) {
            case 0:
                filter = new CarreFilter(setting);
                break;
            case 1:
                filter = new CannyFilter();
                break;
            case 2:
                filter = new SobelOperator(SobelOperator.Mode.Absolute,true);
                break;
            case 3:
                filter = new GaussFilter(setting);
                break;
            default:
                filter = new CarreFilter(10);
                break;
        }
    }
}
