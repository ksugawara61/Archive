package comlab.soft.db.practicebfinal;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

/**
 * サーバー処理を行うクラス
 *
 * @author g031h078
 *
 */
public class PracticeBfinalServer extends Thread {

    //ログ出力用タグ
    private final static String LOG_TAG =
        "PracticeBfinalServer";
    //改行文字
    private final static String	BR =
        System.getProperty("line.separator");

    private ServerSocket	server;	//サーバソケット
    private Socket			client;	//クライアントソケット
    private TextView	lblReceive;	//受信ラベル
    private Activity	activity;
    private static Handler handler ;	//ハンドラ

    //サービス接続の部品
    public PracticeBfinalMediaPlayer MP;	//サービスクラス
    private boolean mIsBound = false;		//サービスの接続状況
    private ServiceConnection connection = new ServiceConnection() {
        //サービスへのダイレクトアクセスを可能にする
        public void onServiceConnected(ComponentName classname, IBinder service) {
            MP = ((PracticeBfinalMediaPlayer.MPBinder)service).getService();
        }

        //サービスの初期化
        public void onServiceDisconnected(ComponentName classname) {
            MP = null;
        }
    };

    public PracticeBfinalServer (Handler hand, TextView view, Activity act) {
        handler = hand;
        lblReceive = view;
        activity = act;
    }

    /**
     * サーバソケットの生成
     */
    public void CreateServer() {
        try {
            //サーバソケットの生成
            server = new ServerSocket(8080);
            addText("接続待機中ポート>" + server.getLocalPort());
        } catch (Exception e) {
            addText("サーバ生成エラー");
            Log.i(LOG_TAG, e.getMessage());
        }
    }

    /**
     * サーバソケットのクローズ
     */
    public void CloseServer() {
        try {
            if (server != null) {
                server.close();
                server = null;
            }
            if (client != null) {
                client.close();
                client = null;
            }
        } catch (Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }
    }

    /**
     * サーバ処理の開始
     */
    public void run() {
        while (server != null) {
            try {
                //接続待機
                client = server.accept();

                //クライアントIPの出力
                String clientIP = client.getInetAddress().toString();
                addText("クライアントIP>" + clientIP);

                //サーバの受信処理
                ReceiveServer();
            } catch (Exception e) {
                try {
                    client.close();
                } catch (Exception ex) {
                }
                addText("クライアントとの接続が切断されました");
                Log.i(LOG_TAG, e.getMessage());
            }
        }
    }

    /**
     * リクエストを受信（サーバー側）
     */
    public void ReceiveServer() {
        try {
            //入力ストリームの生成
            InputStream in = null;

            while (client != null && client.isConnected()) {
                //入力ストリームの取得
                in = client.getInputStream();
                byte[] w = new byte[1024];
                //リクエストの受信
                int size = in.read(w);
                if (size <= 0) {
                    throw new Exception();
                }
                String event = new String(w, 0, 3, "Shift_JIS");
                String data = new String(w, 3, size - 3, "Shift_JIS").trim();

                ServerEvent(event, data);
            }
        } catch (Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }
    }

    /**
     * リクエスト処理
     *
     * @param event	リクエスト
     * @param data	データ
     */
    private void ServerEvent(String event, String data) {
        //リクエストが曲のリクエストの場合
        if (event.equals("req")) {
            String column = data.substring(0, 3);
            if (column.equals("mus")) {
                column = "music";
            } else if (column.equals("art")) {
                column = "artist";
            } else if (column.equals("alb")) {
                column = "album";
            }

            String name = data.substring(3);

            //コンテンツプロバイダが提供するデータベースを示す
            Uri uri =
                Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
            //リクエストに該当するレコードの取得
            Cursor c = activity.getContentResolver().query(uri,
                    new String[]{"id", "music", "artist", "album"},
                    column + " like '%" + name + "%'", null, null);

            //取得したレコード0個以上かどうか
            if (c.getCount() > 0) {
                c.moveToFirst();
                String sender = "req";
                do {
                    sender = sender + c.getString(0) + "'" + c.getString(1) +
                    "'" + c.getString(2) + "'" + c.getString(3) + BR;
                } while(c.moveToNext());
                //レコード情報の送信
                SendServer(sender);
            } else {
                //見つからなかったことを送信
                SendServer("not");
            }
        } else

        if (event.equals("sta")) {
            //コンテンツプロバイダが提供するデータベースを示す
            Uri uri =
                Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
            Cursor c = activity.getContentResolver().query(uri,
                    new String[]{"id", "music", "artist", "album", "path"},
                    "id = " + data, null, null);

            if (c.getCount() == 1) {
                c.moveToFirst();

                //データベースから取得したパスにファイルが実際に存在するかどうか
                File music = new File(c.getString(4));
                if (music.exists()) {
                    MP.playSound(Integer.parseInt(c.getString(0)), c.getString(1),
                            c.getString(2), c.getString(3), c.getString(4));
                } else {
                    //見つからなかったことを送信
                    SendServer("not");
                }
            }
        }

        //リクエストが再生の場合
        if (event.equals("pla")) {
            MP.unPause();
        }

        //リクエストが前の曲の場合
        if (event.equals("pre")) {
            MP.prevSound();

            //コンテンツプロバイダが提供するデータベースを示す
            Uri uri =
                Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
            Cursor c = activity.getContentResolver().query(uri,
                    new String[]{"id", "music", "artist", "album", "path"},
                    "id = " + MP.ID, null, null);

            c.moveToFirst();
            SendServer("cha" + c.getString(1) + "'" + c.getString(2) +
                    "'" + c.getString(3));
        }

        //リクエストが一時停止の場合
        if (event.equals("pau")) {
            MP.pause();
        }

        //リクエストが次の曲の場合
        if (event.equals("nex")) {
            MP.nextSound();

            //コンテンツプロバイダが提供するデータベースを示す
            Uri uri =
                Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
            Cursor c = activity.getContentResolver().query(uri,
                    new String[]{"id", "music", "artist", "album", "path"},
                    "id = " + MP.ID, null, null);

            c.moveToFirst();
            SendServer("cha" + c.getString(1) + "'" + c.getString(2) +
                    "'" + c.getString(3));
        }

        //リクエストが停止の場合
        if (event.equals("sto")) {
            MP.stopSound();
        }

        //リクエストが切断の場合
        if (event.equals("cut")) {
            try {
                client.close();
                client = null;
                addText("クライアントとの接続が解除されました");
            } catch (Exception e) {
                Log.i(LOG_TAG, e.getMessage());
            }
        }
    }

    /**
     * リクエストを送信（サーバー側）
     *
     * @param data		送信データ
     */
    private void SendServer(String data) {
        try {
            //出力ストリームの取得
            OutputStream out = client.getOutputStream();

            //リクエストを取得し、バイト配列へ変換
            byte[] w = data.getBytes("Shift_JIS");
            //バイト配列の書き込み
            out.write(w);
            //バイト配列を明示的に送信
            out.flush();
        } catch (Exception e) {
            addText("送信失敗（サーバー）");

            Log.i(LOG_TAG, e.getMessage());
        }
    }

    /**
     * サービスとの接続
     *
     * @param intent	インテント
     */
    public void doBindService(Intent intent) {
        //サービスへ接続する
        activity.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /**
     * サービスとの接続を解除
     */
    public void doUnbindService() {
        if (mIsBound) {
            //コネクションの解除
            activity.unbindService(connection);
        }
        mIsBound = false;
    }

    /**
     * 受信テキストの追加
     *
     * @param text	受信するテキスト
     */
    private void addText(final String text) {
        handler.post(new Runnable() {
            public void run() {
                lblReceive.setText(text + BR + lblReceive.getText());
            }
        });
    }
}
