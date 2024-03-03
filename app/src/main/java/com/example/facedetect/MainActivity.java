package com.example.facedetect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button cameraBtn;
    TextView textView;
    ImageView imageView;

    private final static int REQUEST_IMAGE_CAPTURE = 124;

    InputImage firebaseVision;
    FaceDetector faceDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBtn = findViewById(R.id.cameraBtn);
        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);

        FirebaseApp.initializeApp(this);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFile();
            }
        });
        Toast.makeText(this, "App is Started!", Toast.LENGTH_SHORT).show();
    }

    private void openFile() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }else{
            Toast.makeText(this, "Failed to Capture Image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bundle bundle = data.getExtras();
        Bitmap bitmap = (Bitmap) bundle.get("data");

        faceDetectionProcess(bitmap);
        Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
    }

    private void faceDetectionProcess(Bitmap bitmap) {
        textView.setText("Processing Image...");
        final StringBuilder builder = new StringBuilder();

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .enableTracking().build();

        faceDetector = FaceDetection.getClient(options);

        Task<List<Face>> result = faceDetector.process(image).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
            @Override
            public void onSuccess(List<Face> faces) {
                if(faces.size() != 0){
                    if(faces.size() == 1){
                        builder.append(faces.size()+" Face Detecteed\n\n");
                    }else if(faces.size() > 1){
                        builder.append(faces.size()+" Faces Detecteed\n\n");
                    }
                }

                for(Face face : faces){
                    int id = face.getTrackingId();
                    //Tilting and Rotating Probability
//                    float rotX = face.getHeadEulerAngleX();
                    float rotY = face.getHeadEulerAngleY();
                    float rotZ = face.getHeadEulerAngleZ();

                    builder.append("1. Face Tracking Id["+id+"]\n");
                    builder.append("2. Head Rotation to Right ["+
                            String.format("%.2f",rotY)+"deg.]\n");
                    builder.append("3. Head Tilted Sideways ["+
                            String.format("%.2f",rotZ)+" deg.]\n");

                    //Smiling Probability
                    if(face.getSmilingProbability() > 0){
                        float smilingProbability = face.getSmilingProbability();
                        builder.append("4. Smiling Probability ["+
                                String.format(".2f",smilingProbability)+" ]\n");
                    }

                    //Left Eye Opened Probability
                    if(face.getLeftEyeOpenProbability() > 0){
                        float leftEye = face.getLeftEyeOpenProbability();
                        builder.append("5. Left Opened Probability ["+
                                String.format(".2f",leftEye)+"]\n");
                    }

                    //Right Eye Opened Probability
                    if(face.getRightEyeOpenProbability() > 0){
                        float rightEye = face.getLeftEyeOpenProbability();
                        builder.append("6. Right Opened Probability ["+
                                String.format(".2f",rightEye)+"]\n");
                    }
                    builder.append("\n");
                }

                showDetection("Face Detection",builder,true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                StringBuilder builder1 = new StringBuilder();
                builder1.append("Sorry! There is an error");
                showDetection("Face Detection",builder1,false);
            }
        });
    }

    private void showDetection(String title, StringBuilder builder, boolean success) {
        textView.setText(null);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.append(builder);
        if(success == true){
            if(builder.length() > 0){
                if(title.substring(0,title.indexOf(' ')).equalsIgnoreCase("OCR")){
                    textView.append("\n(Hold the text to copy it!)");
                }else{
                    textView.append("(Hold the text to copy  it!)");
                }

                textView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(title, builder);
                        clipboard.setPrimaryClip(clip);
                        return true;
                    }
                });
            }else{
                textView.append(title.substring(0,title.indexOf(' '))+"Failed to find Anything!");
            }
        }
    }
}