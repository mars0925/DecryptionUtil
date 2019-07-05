package com.mars.decryptionutil;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import static com.mars.decryptionutil.DecryptionUtil.decrypt;
import static com.mars.decryptionutil.DecryptionUtil.getSHA;

public class MainActivity extends AppCompatActivity {
    private final String tag = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String result = decrypt("tcHWRSdoGyaP4dPq/lBguQ==","24drs_push_serv","transNoti");
        Log.e(tag , result + "");

        String resultSHA  = getSHA(result);
        Log.e(tag , resultSHA + "");
    }
}
