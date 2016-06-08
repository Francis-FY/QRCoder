package com.qrcode.qrcode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Created by FengYang on 2016-06-01.
 */
public class GenerateFragment extends Fragment implements View.OnClickListener {
    private EditText mEditText;
    private Button mButton;
    private ImageView resultImage;
    private Bitmap mBitmap;
    private static final int QRCODE_WIDTH = 250;
    private boolean isGenerated = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.generate_fragment, container, false);
        mEditText = (EditText) v.findViewById(R.id.id_generate_text);
        mButton = (Button) v.findViewById(R.id.id_generate_btn);
        resultImage = (ImageView) v.findViewById(R.id.id_generated_qrcode);
        initEvent();
        return v;
    }

    private void initEvent() {
        mButton.setOnClickListener(this);
        resultImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                shareQrcode();
                return true;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.id_generate_btn:
                generateQrCode();
                break;
            default:
                break;
        }
    }

    private void shareQrcode() {
        if (isGenerated) {
            File saveFile = new File(getContext().getExternalFilesDir("qrcode"), "qrcode_tmp.jpg");
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(saveFile));
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, "分享到"));
        }
    }

    private void generateQrCode() {
        String text = mEditText.getText().toString();
        if (text == null || TextUtils.isEmpty(text)) {
            Toast.makeText(getContext(), "请输入文字以生成二维码", Toast.LENGTH_SHORT).show();
            return;
        }
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            Map hints = new Hashtable<DecodeHintType, Object>(3);
            Vector<BarcodeFormat> decodeFormats = new Vector<>();
            decodeFormats.add(BarcodeFormat.QR_CODE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            int width = dipToPx(QRCODE_WIDTH);
            String contents = new String(text.getBytes("UTF-8"), "ISO-8859-1");
            BitMatrix bitMatrix = multiFormatWriter.encode(contents, BarcodeFormat.QR_CODE, width, width, hints);
            int[] pixels = new int[width * width];
            for (int y = 0; y < width; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            if (mBitmap != null && !mBitmap.isRecycled()) {
                mBitmap.recycle();
                mBitmap = null;
            }
            mBitmap = Bitmap.createBitmap(width, width, Bitmap.Config.RGB_565);
            mBitmap.setPixels(pixels, 0, width, 0, 0, width, width);
            resultImage.setImageBitmap(mBitmap);
            try {
                File saveFile = new File(getContext().getExternalFilesDir("qrcode"), "qrcode_tmp.jpg");
                if (saveFile.exists()) {
                    saveFile.delete();
                }
                OutputStream os = new FileOutputStream(saveFile);
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();
                InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(),0);
                isGenerated = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "生成失败", Toast.LENGTH_SHORT).show();
            isGenerated = false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private int dipToPx(int dip) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f);
    }

}
