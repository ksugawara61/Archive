package comlab.soft.db.practicebfinal;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 *
 * @author g031h078
 *
 */
public class PracticeBfinalActivity extends Activity
implements View.OnClickListener {

    //ログ出力用タグ
    private final static String LOG_TAG =
        "PracticeBfinalActivity";
    private final static int REQUEST_TEXT = 0;
    private final String PATH = "/mnt/sdcard/music/";
    private File dir = new File(PATH);

    //レイアウトの部品
    private RadioGroup	radioGroup;	//ラジオグループ
    private EditText	edtIP;		//IPアドレス用エディットテキスト
    private EditText	edtReq;		//リクエスト用エディットテキスト
    private CheckBox	mode;		//モード選択用チェックボタン
    private TextView	lblReceive;	//受信ラベル

    //ソケット通信の部品
    private String			IP;			//IPアドレス

    private final static Handler handler = new Handler();	//ハンドラ
    private PracticeBfinalServer Server;
    private PracticeBfinalClient Client;

    /**
     * アクティビティ生成時に呼ばれるメソッド
     * アプリの初期化
     *
     * @param icicle
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //レイアウトの生成
        LinearLayout layout = new LinearLayout(this);
        layout.setBackgroundColor(Color.rgb(255, 255, 255));
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout Clayout = new LinearLayout(this);
        Clayout.setBackgroundColor(Color.rgb(255, 255, 255));
        Clayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout Blayout = new LinearLayout(this);
        Blayout.setBackgroundColor(Color.rgb(255, 255, 255));
        Blayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout Rlayout = new LinearLayout(this);
        Rlayout.setBackgroundColor(Color.rgb(255, 255, 255));
        Rlayout.setOrientation(LinearLayout.HORIZONTAL);

        //CheckBox の作成
        mode = new CheckBox(this);
        mode.setText("ローカルモード");
        mode.setTextColor(Color.rgb( 0, 0, 0));
        setLLParams(mode,
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(mode);

        //エディットテキストの生成
        edtIP = new EditText(this);
        edtIP.setText("", EditText.BufferType.NORMAL);
        edtIP.setWidth(400);
        setLLParams(edtIP);
        Clayout.addView(edtIP);

        //ボタンの生成
        Clayout.addView(makeButton(0, "接続", 100));
        layout.addView(Clayout);

        layout.addView(makeButton(1, "切断", 500));

        //ラジオグループの生成
        radioGroup = new RadioGroup(this);
        radioGroup.addView(makeRadio(1, "曲名"));
        radioGroup.addView(makeRadio(2, "アーティスト名"));
        radioGroup.addView(makeRadio(3, "アルバム名"));
        radioGroup.check(1);
        setLLParams(radioGroup);
        layout.addView(radioGroup);

        edtReq = new EditText(this);
        edtReq.setText("", EditText.BufferType.NORMAL);
        edtReq.setWidth(400);
        setLLParams(edtReq);
        Rlayout.addView(edtReq);

        Rlayout.addView(makeButton(2, "送信", 100));
        layout.addView(Rlayout);
        layout.addView(makeButton(3, "再生", 500));

        Blayout.addView(makeButton(4, "前の曲", 150));
        Blayout.addView(makeButton(5, "一時停止", 190));
        Blayout.addView(makeButton(6, "次の曲", 150));
        layout.addView(Blayout);

        layout.addView(makeButton(7, "停止", 500));
        layout.addView(makeButton(8, "データベースの更新", 500));

        //受信ラベルの生成
        lblReceive = new TextView(this);
        lblReceive.setId(1);
        lblReceive.setText("");
        lblReceive.setTextSize(16.0f);
        lblReceive.setTextColor(Color.rgb(0, 0, 0));
        setLLParams(lblReceive,
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(lblReceive);

        //スクロールビューの生成
        ScrollView scrollView=new ScrollView(this);
        scrollView.addView(layout);
        setContentView(scrollView);

        Server = new PracticeBfinalServer(handler, lblReceive, this);
        Client = new PracticeBfinalClient(handler, lblReceive, this);

        //サーバーの生成
        Server.CreateServer();

        //インテントの生成
        Intent intent = new Intent(this,
                comlab.soft.db.practicebfinal.PracticeBfinalMediaPlayer.class);
        this.startService(intent);
        Server.doBindService(intent);

        //サーバー処理の開始
        Server.start();
    }

    /**
     * アプリの停止
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    /**
     * ボタンクリックイベントの処理
     *
     * @param view ビュー
     */
    public void onClick(View view) {
        //ボタンの idを取得
        int id = view.getId();

        //接続ボタンを押した場合
        if (id == 0) {
            IP = edtIP.getText().toString();

            (new Thread() {public void run() {
                Client.connect(IP, 8080);
            }}).start();
        }

        //切断ボタンが押された場合
        if (id == 1) {
            Client.SendClient("cut");
            Client.CloseClient(false);
            //ノティフィケーションマネージャの取得
            NotificationManager nm;
            nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            //ノティフィケーションのキャンセル
            nm.cancel(0);
        }

        //曲のリクエストボタンが押された場合
        if (id == 2) {
            int radioID = radioGroup.getCheckedRadioButtonId();
            if (mode.isChecked()) {
                localRequest(radioID, edtReq.getText().toString());
            } else {
                String column = new String();

                if (radioID == 1) {
                    column = "mus";
                } else if (radioID == 2) {
                    column = "art";
                } else if (radioID == 3) {
                    column = "alb";
                }

                Client.SendClient("req" + column + edtReq.getText().toString());
            }

            edtReq.setText("", TextView.BufferType.NORMAL);
        }

        //再生ボタンが押された場合
        if (id == 3) {
            if (mode.isChecked()) {
                Server.MP.unPause();
            } else {
                Client.SendClient("pla");
            }
        }

        //前の曲ボタンが押された場合
        if (id == 4) {
            if (mode.isChecked()) {
                Server.MP.prevSound();
            } else {
                Client.SendClient("pre");
            }
        }

        //一時停止ボタンが押された場合
        if (id == 5) {
            if (mode.isChecked()) {
                Server.MP.pause();
            } else {
                Client.SendClient("pau");
            }
        }

        //次の曲ボタンが押された場合
        if (id == 6) {
            if (mode.isChecked()) {
                Server.MP.nextSound();
            } else {
                Client.SendClient("nex");
            }
        }


        //停止ボタンが押された場合
        if (id == 7) {
            if (mode.isChecked()) {
                Server.MP.stopSound();
            } else {
                Client.SendClient("sto");
            }
            //ノティフィケーションマネージャの取得
            NotificationManager nm;
            nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            //ノティフィケーションのキャンセル
            nm.cancel(0);

        }

        //データベースの更新ボタンが押された場合
        if (id == 8) {
            if (dir.exists()) {
                new PracticeBfinalFile(this, dir);

                //インテントの生成
                Intent intent = new Intent(this,
                        comlab.soft.db.practicebfinal.PracticeBfinalMediaPlayer.class);
                this.stopService(intent);
                Server.doUnbindService();

                this.startService(intent);
                Server.doBindService(intent);
            } else {
                //ディレクトリが存在しない場合アプリケーションを終了
                showDialog(this, "エラー",
                        "指定されたパスが存在ないためデータベースを更新できません");
            }
        }
    }

    /**
     * ソケットの切断
     */
    private void disconnect() {
        //インテントの生成
        Intent intent = new Intent(this,
                comlab.soft.db.practicebfinal.PracticeBfinalMediaPlayer.class);
        this.stopService(intent);
        Server.doUnbindService();
        try {
            Server.CloseServer();
            Client.CloseClient(true);
        } catch (Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }
    }

    /**
     * ローカルでのリクエスト
     *
     * @param search	検索方法
     * @param request	リクエスト
     */
    private void localRequest(int search, String request) {
        String column = new String();

        if (search == 1) {
            column = "music";
        } else if (search == 2) {
            column = "artist";
        } else if (search == 3) {
            column = "album";
        }

      //コンテンツプロバイダが提供するデータベースを示す
        Uri uri =
            Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
        //リクエストに該当するレコードの取得
        Cursor c = this.getContentResolver().query(uri,
                new String[]{"id", "music", "artist", "album"},
                column + " like '%" + request + "%'", null, null);

        if (c.getCount() > 0) {
            String[]	ids = new String[1024];
            String[] 	musics = new String[1024];
            String[] 	artists = new String[1024];
            String[] 	albums = new String[1024];
            int count = 0;

            c.moveToFirst();
            do {
                ids[count] = c.getString(0);
                musics[count] = c.getString(1);
                artists[count] = c.getString(2);
                albums[count] = c.getString(3);
                count++;
            } while (c.moveToNext());

            //インテントの生成
            Intent intent = new Intent(this,
                    comlab.soft.db.practicebfinal.PracticeBfinalList.class);

            //インテントにパラメータを付与
            intent.putExtra("num", count);
            intent.putExtra("id", ids);
            intent.putExtra("music", musics);
            intent.putExtra("artist", artists);
            intent.putExtra("album", albums);

            //アクティビティの表示
            this.startActivityForResult(intent, REQUEST_TEXT);
        }
    }

    /**
     * アクティビティ呼び出し結果の取得
     *
     * @param requestCode	リクエストコード
     * @param resultCode	結果コード
     * @param intent		インテント
     */
    @Override
    protected void onActivityResult(int requestCode,
            int resultCode, Intent intent) {
        if (requestCode == REQUEST_TEXT && resultCode == RESULT_OK) {
            //インテントからパラメータの取得
            String id = new String();
            String music = new String();
            String artist = new String();
            String album = new String();

            //Bundleオブジェクトを所得する
            Bundle extras = intent.getExtras();

            if (extras != null) {
                id = extras.getString("id");
                music = extras.getString("music");
                artist = extras.getString("artist");
                album = extras.getString("album");
            }

            if (mode.isChecked()){
                //コンテンツプロバイダが提供するデータベースを示す
                Uri uri =
                    Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
                Cursor c = this.getContentResolver().query(uri,
                        new String[]{"id", "music", "artist", "album", "path"},
                        "id = " + id, null, null);

                if (c.getCount() == 1) {
                    c.moveToFirst();

                    //データベースから取得したパスにファイルが実際に存在するかどうか
                    File file = new File(c.getString(4));
                    if (file.exists()) {
                        Server.MP.playSound(Integer.parseInt(c.getString(0)), c.getString(1),
                                c.getString(2), c.getString(3), c.getString(4));
                    }
                }
            } else {
                Client.SendClient("sta" + id);
                showNotification(this, R.drawable.icon,
                        music + " - " + artist,
                        music,
                        artist + " - " + album);
            }
        }
    }

    /**
     * ボタンの生成
     *
     * @param id		ボタンのID
     * @param text		ボタンに表示するテキスト
     * @param width		ボタンの幅
     * @return button	生成されたボタン
     */
    private Button makeButton(int id, String text, int width) {
        Button button = new Button(this);
        button.setId(id);
        button.setWidth(width);
        button.setText(text);
        button.setOnClickListener(this);
        setLLParams(button);
        return button;
    }

    /**
     * ラジオボタンの生成
     *
     * @param id		ラジオボタンのID
     * @param text		ラジオボタンに表示するテキスト
     * @return radio	生成されたラジオボタン
     */
    private RadioButton makeRadio(int id, String text) {
        RadioButton radio = new RadioButton(this);
        radio.setId(id);
        radio.setHeight(10);
        radio.setText(text);
        radio.setTextColor(Color.rgb(0, 0, 0));

        return radio;
    }

    /**
     * ライナーレイアウトのパラメータ指定
     *
     * @param view レイアウトを設定するビュー
     */
    private static void setLLParams(View view) {
        view.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
    }

    /**
     * ライナーレイアウトのパラメータ指定
     *
     * @param view		レイアウトを設定するビュー
     * @param width		幅
     * @param height	高さ
     */
    public static void setLLParams(View view, int width, int height) {
        view.setLayoutParams(new LinearLayout.LayoutParams(width, height));
    }

    /**
     * ダイアログの表示
     *
     * @param activity アクティビティ
     * @param title タイトル
     * @param text テキスト
     * @param error エラーかどうか
     */
     private static void showDialog(final Activity activity,
             String title, String text) {
         AlertDialog.Builder ad = new AlertDialog.Builder(activity);
         ad.setTitle(title);
         ad.setMessage(text);
         ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 activity.setResult(Activity.RESULT_OK);
             }
         });
         ad.create();
         ad.show();
     }

     /**
      * ノティフィケーションの表示
      *
      * @param context コンテキスト
      * @param iconID アイコンID
      * @param ticker ティッカーテキスト
      * @param title タイトル
      * @param message メッセージ
      */
     private void showNotification(Context context,
         int iconID, String ticker, String title, String message) {
         // ノティフィケーションマネージャの取得
         NotificationManager nm;
         nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

         //ノティフィケーションオブジェクトの生成
         Notification notification =
             new Notification(iconID, ticker, System.currentTimeMillis());
         PendingIntent intent = PendingIntent.getActivity(context, iconID,
                 new Intent(),0);

         notification.setLatestEventInfo(context, title, message, intent);

         // ノティフィケーションのキャンセル
         nm.cancel(0);

         //ノティフィケーションの表示
         nm.notify(0, notification);
     }
}