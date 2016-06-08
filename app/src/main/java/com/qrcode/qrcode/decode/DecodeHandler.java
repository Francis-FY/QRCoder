package com.qrcode.qrcode.decode;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.qrcode.qrcode.BuildConfig;
import com.qrcode.qrcode.utils.Utils;

import java.util.Hashtable;

/**
 * Created by FengYang on 2016-06-03.
 */
public class DecodeHandler extends Handler {
    private final MultiFormatReader multiFormatReader = new MultiFormatReader();
    private Camera.Size size;
    private Rect rect;
    private Handler handler;
    private int[] rgb;
    private Point screenSize;

    DecodeHandler(Camera.Size size,Point screenSize, Rect rect, Handler handler, Hashtable hints) {
        multiFormatReader.setHints(hints);
        this.size = size;
        this.rect = rect;
        this.handler = handler;
        rgb = new int[size.width * size.height];
        this.screenSize = screenSize;

        int left = rect.left*size.height/screenSize.x;
        int right = rect.right*size.height/screenSize.x;
        int top = rect.top*size.width/screenSize.y;
        int bottom = rect.bottom*size.width/screenSize.y;
        this.rect = new Rect(left,top,right,bottom);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                decode((byte[]) msg.obj);
                break;
            default:
                break;
        }
        super.handleMessage(msg);
    }

    private void decode(byte[] data) {
        Utils.decodeYUV420SP(rgb, data, size.width, size.height);
        Bitmap bitmap = Bitmap.createBitmap(rgb, size.width, size.height, Bitmap.Config.RGB_565);
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        Bitmap bmp = Bitmap.createBitmap(bitmap, rect.top, rect.left, rect.width(), rect.height(), matrix, false);
        int[] pixels = new int[rect.width() * rect.height()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        RGBLuminanceSource source = new RGBLuminanceSource(bmp.getWidth(), bmp.getHeight(), pixels);
        if (!bmp.isRecycled()) {
            bmp.recycle();
            bmp = null;
        }
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        System.gc();

        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result rawResult = null;
        try {
            rawResult = multiFormatReader.decodeWithState(binaryBitmap);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } finally {
            multiFormatReader.reset();
        }
        Message message = handler.obtainMessage(0, rawResult);
        message.sendToTarget();
    }
}
