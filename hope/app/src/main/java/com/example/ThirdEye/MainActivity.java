package com.example.ThirdEye;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.StateCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener, ValueEventListener {

    //new
    private DatabaseReference mDatabase;
// ...

    TextToSpeech t1;
    float Value;
    int i=1;
    int myname=0;
    String Finalname;
    Button button;
    Button btnUpload;


    TextureView textureView;
    private static final SparseIntArray ORIENTATIONS =  new SparseIntArray();

    private SensorManager mSensorManager;
    private Sensor mCompass;
    private TextView mTextView;

    StorageReference mStorageRef;
    private Uri imguri;

    static{
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }

    private String cameraID;
    CameraDevice cameraDevice;
    CameraCaptureSession cameraCaptureSession;
    CaptureRequest captureRequest;
    CaptureRequest.Builder captureRequestBuilder;

    private Size imageDimension;
    private ImageReader imageReader;

    private File file;
    Handler mBackgroundHandler;
    HandlerThread mBackgroundThread;
    boolean outputs=false;
    TextView txtResult;
    ImageView btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        mDatabase=FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String myvariablefinal=dataSnapshot.child("TxtoSp").getValue().toString();
                if(outputs)
                {
                    t1.speak(myvariablefinal, TextToSpeech.QUEUE_FLUSH, null);
                }else
                {
                    outputs=true;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDatabase = FirebaseDatabase.getInstance().getReference();


        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mTextView = (TextView) findViewById(R.id.sensorvalue);
        btnUpload = (Button)findViewById(R.id.btnupload);
        mStorageRef=FirebaseStorage.getInstance().getReference("Images");

        textureView = (TextureView)findViewById(R.id.textureview);
        button = (Button)findViewById(R.id.button_capture);
        textureView.setSurfaceTextureListener(textureLinstener);
        btnSpeak = (ImageView)findViewById(R.id.btnSpeak);
        txtResult =(TextView)findViewById(R.id.txvResult);



//
//        RelativeLayout rlayout = (RelativeLayout) findViewById(R.id.mainLayout);
//        rlayout.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                try {
//                    takePicure();
//                } catch (CameraAccessException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                button.performClick();
                startLoop(1);

            }
        }, 5000);

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//      Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));

        } else {
            //deprecated in API 26
            v.vibrate(500);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t1.speak("Welcome , What Are You Searching", TextToSpeech.QUEUE_FLUSH, null);
                //takePicure();
                startLoop(1);

            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadData();
            }
        });

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }
    private String getExtension(Uri uri)
    {
        ContentResolver cr=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }
    private void uploadData() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Bitmap bitmap = null;

        File dir = new File(Environment.getExternalStorageDirectory()+"/VirtualEye/");
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                StorageReference mountainImagesRef = storageRef.child("/Images/"+children[i]);
                bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/VirtualEye/"+children[i]);
                if(bitmap==null)
                    continue;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = mountainImagesRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(MainActivity.this, "Uploading...", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
    }

    private void startLoop(final int m) {
        if(m!=9) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("i",""+i);
                    try {
                        takePicure();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    startLoop(m+1);
                }
            }, 5000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            imguri=data.getData();
            //img.setImageURI(imguri);
        }
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtResult.setText(result.get(0));
                    mDatabase.child("SptoTx").setValue(result.get(0));


                }
                break;
            }
        }
    }
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if(requestCode == 101){
                if(grantResults[0]==PackageManager.PERMISSION_DENIED){
                    Toast.makeText(getApplicationContext(), "Sorry.. camera Permission is necessary", Toast.LENGTH_SHORT).show();
                }

            }
    }

    TextureView.SurfaceTextureListener textureLinstener = new TextureView.SurfaceTextureListener(){

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened( CameraDevice camera) {
            cameraDevice = camera;
            try {
                createCamerapreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void createCamerapreview() throws CameraAccessException {
        SurfaceTexture texture  = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
        Surface surface = new Surface(texture);
        captureRequestBuilder =cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);

        cameraDevice.createCaptureSession(Arrays.asList(surface), new StateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession session) {
                if(cameraDevice==null)
                {
                    return;
                }
                cameraCaptureSession =  session;
                updatePreview();
            }

            @Override
            public void onConfigureFailed(CameraCaptureSession session) {
                Toast.makeText(MainActivity.this, "Configuration Changed", Toast.LENGTH_SHORT).show();
            }
        },null);
    }

    private void updatePreview() {

        if(cameraDevice == null)
        {
            return;
        }
        captureRequestBuilder.set(captureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),null,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() throws CameraAccessException {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        cameraID =manager.getCameraIdList()[0];
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        imageDimension  =map.getOutputSizes(SurfaceTexture.class)[0];

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},101);
            return;
        }

        File dir = new File(Environment.getExternalStorageDirectory()+"/VirtualEye/");
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }

        }
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "VirtualEye");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        manager.openCamera(cameraID,stateCallback,null);
    }

    private void takePicure() throws CameraAccessException
    {
        myname++;

        if(cameraDevice==null){
            return;
        }
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
        Size[] jpegSizes = null;
        jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

        int width = 640;
        int height = 480;

        if(jpegSizes != null && jpegSizes.length>0){
            width = jpegSizes[0].getWidth();
            height = jpegSizes[0].getHeight();
        }

        ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.JPEG,5);
        List<Surface> outputsurface = new ArrayList<>(2);
        outputsurface.add(reader.getSurface());

        outputsurface.add(new Surface(textureView.getSurfaceTexture()));

        final CaptureRequest.Builder captureBuilder =cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(reader.getSurface());
        captureBuilder.set(CaptureRequest.CONTROL_MODE,CameraMetadata.CONTROL_MODE_AUTO);


        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation));

       // Long tsLong = System.currentTimeMillis()/1000;
       // String ts = tsLong.toString();
        if(myname==2)
        {
            Finalname="F1";
        }else if (myname==4)
        {
            Finalname="F2";
        }else if(myname==6)
        {
            Finalname="R1";
        }else if (myname==8)
        {
            Finalname="R2";
        }else if(myname==10)
        {
            Finalname="B1";
        }else if (myname==12) {
            Finalname="B2";
        }else if(myname==14)
        {
            Finalname="L1";
        }else if (myname==16) {
            Finalname="L2";
        }

        file = new File(Environment.getExternalStorageDirectory()+"/VirtualEye/"+ Finalname +".jpeg");

        ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image =null;

                image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                try {
                    Log.d("TAG", "Camera image listener wrote to file: " + Environment.getExternalStorageDirectory());
                    save(bytes);
                    Log.d("TAG", "Error to " + Environment.getExternalStorageDirectory());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if(image!=null)
                    {
                        image.close();
                    }
                }
            }
        };

        reader.setOnImageAvailableListener(readerListener,mBackgroundHandler);

        final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(CameraCaptureSession session,CaptureRequest request,TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                try {
                    createCamerapreview();
                }
                catch (CameraAccessException e){
                    e.printStackTrace();
                }
            }
        };

        cameraDevice.createCaptureSession(outputsurface,new CameraCaptureSession.StateCallback(){

            @Override
            public void onConfigured(CameraCaptureSession session) {
                try {
                    session.capture(captureBuilder.build(),captureListener,mBackgroundHandler);
                }catch (CameraAccessException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onConfigureFailed(CameraCaptureSession session) {

            }
        },mBackgroundHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_NORMAL);
        if(textureView.isAvailable()){
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        else{
            textureView.setSurfaceTextureListener(textureLinstener);
        }
    }

    private void save(byte[] bytes) throws IOException {
        Log.d("TAG", "Error in file");
        OutputStream outputStream = null;
        Log.d("TAG", "Error in outputstream");
        outputStream = new FileOutputStream(file);
        Log.d("TAG", "stream created: ");
        outputStream.write(bytes);
        Log.d("TAG", "stream written: ");
        outputStream.close();
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    protected void onPause() {
        try {
            stopBackGroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onPause();

        mSensorManager.unregisterListener(this);

    }

    protected void stopBackGroundThread() throws InterruptedException {
        mBackgroundThread.quitSafely();
        mBackgroundThread.join();
        mBackgroundThread = null;
        mBackgroundHandler= null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Value = Math.round(event.values[0]);
        // The other values provided are:
        //  float pitch = event.values[1];
        //  float roll = event.values[2];
        mTextView.setText("Degree : " + Float.toString(Value));
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
