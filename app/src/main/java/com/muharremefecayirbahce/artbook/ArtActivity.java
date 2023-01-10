package com.muharremefecayirbahce.artbook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.muharremefecayirbahce.artbook.databinding.ActivityArtBinding;

import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {

    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.equals("new")) {
            binding.artNameText.setText("");
            binding.artistNameText.setText("");
            binding.yearText.setText("");
            binding.button.setVisibility(View.VISIBLE);
            binding.imageView.setImageResource(R.drawable.selectimage);
        } else {
            int artId = intent.getIntExtra("artId", 0);
            binding.button.setVisibility(View.INVISIBLE);

            try {
                database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);
                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", new String[]{String.valueOf(artId)});
                int nameIx = cursor.getColumnIndex("artname");
                int artistIx = cursor.getColumnIndex("artistname");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()) {
                    binding.artNameText.setText(cursor.getString(nameIx));
                    binding.artistNameText.setText(cursor.getString(artistIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void save(View view) {
        String artName = binding.artNameText.getText().toString();
        String artistName = binding.artistNameText.getText().toString();
        String year = binding.yearText.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {
            database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)");
            String sqlString = "INSERT INTO arts (artname, artistname, year, image) VALUES (?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1, artName);
            sqLiteStatement.bindString(2, artistName);
            sqLiteStatement.bindString(3, year);
            sqLiteStatement.bindBlob(4, byteArray);
            sqLiteStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(ArtActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public void selectImage(View view) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", v -> permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)).show();
            } else {
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else {
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void registerLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent intentFromResult = result.getData();
                if (intentFromResult != null) {
                    Uri imageData = intentFromResult.getData();
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= 28) {
                            ImageDecoder.Source source = ImageDecoder.createSource(ArtActivity.this.getContentResolver(), imageData);
                            selectedImage = ImageDecoder.decodeBitmap(source);
                            binding.imageView.setImageBitmap(selectedImage);
                        } else {
                            selectedImage = android.provider.MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(), imageData);
                            binding.imageView.setImageBitmap(selectedImage);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            } else {
                Toast.makeText(ArtActivity.this, "Permission needed!", Toast.LENGTH_LONG).show();
            }
        });
    }
}