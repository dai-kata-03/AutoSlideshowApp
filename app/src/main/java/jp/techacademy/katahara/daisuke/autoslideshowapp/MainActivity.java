package jp.techacademy.katahara.daisuke.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    Button mnextButton;
    Button mprevButton;
    Button mplayButton;
    TextView mtextView2;

    Timer mTimer;
    Handler mHandler = new Handler();

    Boolean autoplay = false; // スライドショー機能が選択されているかどうかのチェック

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        // Android 5桂以下の場合
        } else {
            getContentInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentInfo();
                }
                break;
        }
    }

    private void getContentInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        final Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null  // ソート (null = ソートなし)
        );

        mtextView2 = (TextView) findViewById(R.id.textView2);

        if (cursor.moveToFirst()) {

                // ボタンの初期化
                mnextButton = (Button) findViewById(R.id.nextButton);
                mprevButton = (Button) findViewById(R.id.prevButton);
                mplayButton = (Button) findViewById(R.id.playButton);

                // 一番最初の画像を表示（初期表示）
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
                imageVIew.setImageURI(imageUri);

        } else {
          // 画像データが存在しない場合
          mtextView2.setText("画像データが1枚も存在しません。");
          cursor.close();
        }

        // "進む"ボタンが押された際の処理
        mnextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (autoplay) {
                    // スライドショー起動時は処理なし
                } else {


                    // 画像ファイルの一番最後まで来ていた場合は、一番最初に戻る
                    if (cursor.moveToNext() == false) {
                        cursor.moveToFirst();
                    }

                    // 次の画像の表示
                    int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    Long id = cursor.getLong(fieldIndex);
                    Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
                    imageVIew.setImageURI(imageUri);
                }
            }
        });

        // "戻る"ボタンが押された際の処理
        mprevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (autoplay) {
                // スライドショー起動時は処理なし
                } else {

                // 画像ファイルの一番最初まで来ていた場合は、一番最後に戻る
                if (cursor.moveToPrevious() == false) {
                    cursor.moveToLast();
                }

                // 次の画像の表示
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
                imageVIew.setImageURI(imageUri);
                }
            }
        });

        // "再生 / 停止"ボタンが押された際の処理
        mplayButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (autoplay) {
                    autoplay = false;
                    mTimer.cancel();
                } else {

                    Log.d("DEBUG", "POINT 01");

                    // スライドショーの開始
                    autoplay = true;
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {

                        @Override
                        public void run() {

                            // 画像ファイルの一番最後まで来ていた場合は、一番最初に戻る
                            if (cursor.moveToNext() == false) {
                                cursor.moveToFirst();
                            }

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    // indexからIDを取得し、そのIDから画像のURIを取得する
                                    int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                                    Long id = cursor.getLong(fieldIndex);
                                    Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                                    ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
                                    imageVIew.setImageURI(imageUri);

                                    Log.d("ANDROID", "URI:" + imageUri.toString()); // 確認のためにログ出力しておく
                                }
                            });
                        }
                    }, 0, 2000);

                }

            }
        });

    }
}
