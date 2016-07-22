package comlab.soft.db.practicebfinal;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

/**
 * クライアント処理を行うクラス
 *
 * @author g031h078
 *
 */
public class PracticeBfinalClient {

    //ログ出力用タグ
    private final static String LOG_TAG =
        "PracticeBfinalClient";
    //改行文字
    private final static String	BR =
        System.getProperty("line.separator");
    private final static int REQUEST_TEXT = 0;

    private Socket		socket;		//ソケット
    private String[]	ids = new String[1024];
    private String[] 	musics = new String[1024];
    private String[] 	artists = new String[1024];
    private String[] 	albums = new String[1024];
    private int 		count = 0;

    private TextView	lblReceive;	//受信ラベル
    private Activity	activity;
    private static Handler handler ;	//ハンドラ

    public PracticeBfinalClient (Handler hand, TextView view, Activity act) {
        handler = hand;
        lblReceive = view;
        activity = act;
    }

    /**
     * ソケットのクローズ
     *
     * @param close		アクティビティの終了かどうか
     */
    public void CloseClient(boolean close) {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
                if (close == false) {
                    addText("サーバーとの接続を解除しました");
                }
            }
        } catch (Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }
    }

    /**
     * ソケットの接続
     *
     * @param ip	IPアドレス
     * @param port	ポート番号
     */
    public void connect(String ip, int port) {
        if (socket == null) {
            try {
                InputStream in = null;
                byte[] w = new byte[131072];

                //ソケット結合
                addText("サーバー接続中");
                socket = new Socket(ip, port);
                //入力ストリームの取得
                in = socket.getInputStream();
                addText("サーバー接続完了");


                //受信ループ
                while (socket != null && socket.isConnected()) {
                    //サーバからデータの受信
                    int size = in.read(w);
                    if (size <= 0) {
                        continue;
                    }

                    ClientEvent(w, size);
                }
            } catch (Exception e) {
                addText("サーバーとの接続に失敗しました");
                Log.i(LOG_TAG, e.getMessage());
            }
        } else {
            addText("すでに接続されています");
        }
    }

    /**
     * リクエスト処理
     *
     * @param w		リクエストのバイトデータ
     * @param size	バイトデータの大きさ
     * @throws Exception
     */
    private void ClientEvent(byte[] w, int size) throws Exception {
        String event = new String(w, 0, 3, "Shift_JIS");

        //曲の一覧を取得できたかどうか
        if (event.equals("req")) {
            //音楽ファイルをレコードごとに切り取り
            String recode[] = new String(w, 3, size - 3, "Shift_JIS").split(BR);
            int recodelen = recode.length;

            for (int i = 0; i < recodelen; i++) {
                //レコードをテーブルごとに切り取り
                String table[] = recode[i].split("'");

                ids[count] = table[0].trim();
                musics[count] = table[1].trim();
                artists[count] = table[2].trim();
                albums[count] = table[3].trim();
                count++;
            }

            //リストの表示
            ListIndication();
        } else if (event.equals("not")) {
            addText("見つかりませんでした");
        } else if (event.equals("cha")) {
            String info[] = new String(w, 3, size -3, "Shift_JIS").split("'");
            showNotification(activity, R.drawable.icon,
                    info[0] + " - " + info[1],
                    info[0],
                    info[1] + " - " + info[2]);
        }
    }

    /**
     * リクエストを送信（クライアント側）
     *
     * @param str	送信データ
     */
    public void SendClient(String str) {
        if (socket != null && socket.isConnected()) {
            try {
                //出力ストリームの取得
                OutputStream out = socket.getOutputStream();

                str = str.replaceAll("'", "\"");
                //リクエストを取得し、バイト配列へ変換
                byte[] w = str.getBytes("Shift_JIS");
                //バイト配列の書き込み
                out.write(w);
                //バイト配列を明示的に送信
                out.flush();
            } catch (Exception e) {
                addText("送信失敗（クライアント）");
                Log.i(LOG_TAG, e.getMessage());
            }
        } else {
            addText("サーバーに接続されていません");
        }
    }

    /**
     * リストの表示
     */
    private void ListIndication() {
        if (socket != null && socket.isConnected()) {
            if (count != 0) {
                //インテントの生成
                Intent intent = new Intent(activity,
                        comlab.soft.db.practicebfinal.PracticeBfinalList.class);

                //インテントにパラメータを付与
                intent.putExtra("num", count);
                intent.putExtra("id", ids);
                intent.putExtra("music", musics);
                intent.putExtra("artist", artists);
                intent.putExtra("album", albums);

                //アクティビティの表示
                activity.startActivityForResult(intent, REQUEST_TEXT);

                //リストの初期化
                musics = new String[1024];
                artists = new String[1024];
                albums = new String[1024];
                count = 0;
            }
        }
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
        nm = (NotificationManager)activity.getSystemService(Context.NOTIFICATION_SERVICE);

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