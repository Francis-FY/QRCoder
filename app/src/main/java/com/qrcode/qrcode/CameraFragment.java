package com.qrcode.qrcode;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.qrcode.qrcode.decode.DecodeThread;
import com.qrcode.qrcode.view.ScanFrameView;

import java.io.IOException;
import java.util.List;

/**
 * Created by FengYang on 2016-06-01.
 */

public class CameraFragment extends Fragment implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = "CameraFragment";
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private SurfaceView surfaceView;
    private ImageView flashSwitch;
    private ImageView selectImgBtn;
    private boolean isFlashOn = false;
    private ScanFrameView scanFrameView;
    private RelativeLayout relativeLayer;
    private View scanLine;
    private Rect frameRect;
    private Point screenSize;

    private DecodeThread decodeThread;

    private int state = 0;
    private boolean isComplete = true;
    private boolean running = true;
    private boolean isInitialize = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: //一次解析完成并返回
                    isComplete = true;
                    Result result = (Result) msg.obj;
                    if (result != null) {
                        if (state == 0) {
                            state = 1;
                            Intent intent = new Intent(CameraFragment.this.getContext(), ScanResultActivity.class);
                            intent.putExtra("result", result.getText());
                            startActivity(intent);
                        }
                    }
                    break;
                case 1: //开始一次解析
                    isComplete = false;
                    break;
                case 2: //获取权限成功
                    if (isInitialize) {
//                        initSurface();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.camera_fragment, container, false);
        surfaceView = (SurfaceView) v.findViewById(R.id.id_sufaceview);
        flashSwitch = (ImageView) v.findViewById(R.id.id_flash);
        selectImgBtn = (ImageView) v.findViewById(R.id.id_select_pic);
        scanFrameView = (ScanFrameView) v.findViewById(R.id.id_scanframeview);
        relativeLayer = (RelativeLayout) v.findViewById(R.id.id_relative_layer);

        frameRect = scanFrameView.getFrame();

        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenSize = new Point(screenWidth, screenHeight);

        scanLine = new View(getContext());
        scanLine.setBackgroundColor(Color.WHITE);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(frameRect.width(), dipToPx(1));
        layoutParams.topMargin = scanFrameView.getFrame().top;
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        scanLine.setLayoutParams(layoutParams);
        relativeLayer.addView(scanLine);
        initEvents();
//        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//            initSurface();
//        }
        isInitialize = true;
        mHolder = surfaceView.getHolder();
        checkAndRequestPermission();
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            Log.d("CameraFragment", "连接相机失败");
            e.printStackTrace();
        }
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        return v;
    }

//    private void initSurface() {
//        Log.d("SSS", "initSurface: ");
//        mHolder = surfaceView.getHolder();
//        getCamera();
//        mHolder.addCallback(this);
//        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        initPreview();
//    }

//    private void initPreview() {
//        if(mCamera == null){
//            return;
//        }
//        mCamera.setDisplayOrientation(90);
//        try {
//            mCamera.setPreviewDisplay(mHolder);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        mCamera.setPreviewCallback(this);
//        initCamera();
//        mCamera.startPreview();
//
//        Camera.Size size = mCamera.getParameters().getPreviewSize();
//        decodeThread = new DecodeThread(size, screenSize, handler, frameRect);
//        decodeThread.start();
//    }

    @Override
    public void onStart() {
        super.onStart();
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, frameRect.width() - dipToPx(1));
        translateAnimation.setRepeatCount(Animation.INFINITE);
        translateAnimation.setRepeatMode(Animation.REVERSE);
        translateAnimation.setDuration(3000);
        translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        scanLine.startAnimation(translateAnimation);
    }

    @Override
    public void onResume() {
        state = 0;
        super.onResume();
    }

    @Override
    public void onPause() {
        if (isFlashOn) {
            turnLightOff();
        }
        super.onPause();
    }

    private void initEvents() {
        flashSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFlashOn) {
                    turnLightOff();
                } else {
                    turnLightOn();
                }
            }
        });

        selectImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, 0);
            }
        });
    }

    public void turnLightOn() {
        if (isFlashOn) return;
        if (mCamera == null) {
            Toast.makeText(getContext(), "开启闪光灯失败", Toast.LENGTH_SHORT).show();
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            Toast.makeText(getContext(), "开启闪光灯失败", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes == null) {
            Toast.makeText(getContext(), "开启闪光灯失败", Toast.LENGTH_SHORT).show();
            return;
        }
        String flashMode = parameters.getFlashMode();
        if (!Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                isFlashOn = true;
                flashSwitch.setImageResource(R.mipmap.flash_on);
            }
        }
    }

    public void turnLightOff() {
        if (!isFlashOn) return;
        if (mCamera == null) {
            Toast.makeText(getContext(), "关闭闪光灯失败", Toast.LENGTH_SHORT).show();
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            Toast.makeText(getContext(), "关闭闪光灯失败", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        String flashMode = parameters.getFlashMode();
        // Check if camera flash exists
        if (flashModes == null) {
            Toast.makeText(getContext(), "关闭闪光灯失败", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
            // Turn off the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                isFlashOn = false;
                flashSwitch.setImageResource(R.mipmap.flash_off);
            }
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            Log.d(TAG, "surfaceCreated: Created");
            if (mCamera == null) {
                new AlertDialog.Builder(getContext()).setMessage("打开相机失败,请检查是否授予权限！").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Process.killProcess(Process.myPid());
                    }
                }).setCancelable(false).create().show();
            } else {
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.setPreviewCallback(this);
                initCamera();
                mCamera.startPreview();

                Camera.Size size = mCamera.getParameters().getPreviewSize();
                decodeThread = new DecodeThread(size, screenSize, handler, frameRect);
                decodeThread.start();
            }
        } catch (IOException e) {
            Log.d("surfaceCreated", "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d(TAG, "surfaceChanged: ");
        if (mCamera == null) {
            new AlertDialog.Builder(getContext()).setMessage("打开相机失败,请检查是否授予权限！").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Process.killProcess(Process.myPid());
                }
            }).setCancelable(false).create().show();
        } else {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    initCamera();
                    mCamera.cancelAutoFocus();
                }

            });
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed: ");
        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
//            mCamera.release();
        } catch (Exception e) {
        }
    }

    private void initCamera() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
        mCamera.setParameters(parameters);
    }

    private int dipToPx(int dip) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (running && isComplete) {
            Message message = decodeThread.getHandler().obtainMessage(0, data);
            message.sendToTarget();
            handler.sendEmptyMessage(1);
        }

    }

    public void toggleRunning() {
        if (running) {
            running = false;
        } else {
            running = true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0)
            return;
        switch (requestCode) {
            case 0:
                startCorpActivity(data.getData());
                break;
            case 1:
                Bitmap bitmap = data.getParcelableExtra("data");
                parseBitmap(bitmap);
                break;
            default:
                break;
        }
    }

    private void parseBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            if (BuildConfig.DEBUG) Log.d("CameraFragment", "回收");
            bitmap = null;
            System.gc();
        }

        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result rawResult = null;
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        try {
            rawResult = multiFormatReader.decodeWithState(binaryBitmap);
            Message message = handler.obtainMessage(0);
            message.obj = rawResult;
            handler.sendMessage(message);
        } catch (NotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "解析图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCorpActivity(Uri data) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(data, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);

        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        startActivityForResult(intent, 1);
    }


    private boolean checkAndRequestPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (this.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            } else {
                this.requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "授权成功", Toast.LENGTH_SHORT).show();
                    Message message = Message.obtain(handler, 2);
                    message.sendToTarget();
                } else {
                    Toast.makeText(getContext(), "授权失败", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}


