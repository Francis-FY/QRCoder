package com.qrcode.qrcode;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by FengYang on 2016-06-04.
 */
public class ScanResultActivity extends AppCompatActivity {

    private TextView resultText;
    private TextView resultType;
    private ImageView typeImg;
    private Button btn;
    private boolean isUrl;
    private String result;

    private static final String URL_REG = "^((https|http|ftp|rtsp|mms)?:\\/\\/)[^\\s]+";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.activity_scan_result);

        resultText = (TextView) findViewById(R.id.id_scan_result);
        resultType = (TextView) findViewById(R.id.id_result_type_text);
        typeImg = (ImageView) findViewById(R.id.id_type_image);
        btn = (Button) findViewById(R.id.id_result_btn);

        result = getIntent().getStringExtra("result");
        if (isUrl = result.matches(URL_REG)) {
            resultType.setText(R.string.result_type_url);
            btn.setText("访问网址");
            typeImg.setImageResource(R.mipmap.url_icon);
        } else {
            resultType.setText(R.string.result_type_text);
            btn.setText("复制");
            typeImg.setImageResource(R.mipmap.text_icon);
        }
        resultText.setText(toSBC(result));
        initEvent();
    }

    public void goBack(View view) {
        this.finish();
    }

    private void initEvent(){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isUrl){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(result));
                    startActivity(intent);
                }else{
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("text",result);
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(ScanResultActivity.this, "复制文本成功", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    //将半角字符转换为全角字符
    public String toSBC(String input) {
        char c[] = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == ' ') {
                c[i] = '\u3000';
            } else if (c[i] < '\177') {
                c[i] = (char) (c[i] + 65248);
            }
        }
        return new String(c);
    }
}
