package comlab.soft.db.practicebfinal;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * プレーヤーサービス
 *
 * @author g031h078
 *
 */
public class PracticeBfinalMediaPlayer extends Service
implements MediaPlayer.OnCompletionListener {
    //ログ出力用タグ
    private final static String LOG_TAG =
        "PracticeBfinalMediaPlayer";

    public MediaPlayer player;	//音楽プレーヤー
    private final IBinder mbinder = new MPBinder();	//バインダーの生成
    public int ID;		//再生する音楽のID
    private int NUM;	//音楽ファイルの数

    /**
     * サービスの開始時に呼ばれるメソッド
     */
    @Override
    public void onStart(Intent intent, int StartID) {
        //コンテンツプロバイダが提供するデータベースを示す
        Uri uri =
            Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
        Cursor c = this.getContentResolver().query(uri,
                new String[]{"id"}, null, null, null);
        //音楽ファイル数を取得
        NUM = c.getCount();
    }

    /**
     * サービス解放時に呼ばれるメソッド
     */
    @Override
    public void onDestroy() {
        //サウンドの停止
        stopSound();
    }

    /**
     * サービスへの通信チャンネルを戻す
     *
     * @param intent	インテント
     * @return mbinder	生成したバインダー
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mbinder;
    }

    /**
     * サウンドの再生
     *
     * @param id		再生する音楽のID
     * @param music		再生する音楽名
     * @param artist	再生する音楽の歌手名
     * @param album		再生する音楽のアルバム名
     * @param path		再生する音楽のパス
     */
    public void playSound(int id, String music, String artist,
            String album, String path) {
        ID = id;

        //前の情報が残っている場合削除
        if (player != null) {
            player.stop();
            player.release();
        }

        //ファイルプレイヤーの生成
        Uri.Builder builder = new Uri.Builder();
        builder.path(path);
        builder.scheme("file");

        //ノティフィケーションの表示
        showNotification(this,R.drawable.icon,
            music + " - " + artist,
            music,
            artist + " - " + album);

        player = MediaPlayer.create(this, builder.build());

        //プレイヤーの開始
        player.start();
        player.setOnCompletionListener(this);
    }

    /**
     * サウンドの一時停止
     */
    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    /**
     * サウンドの再開
     */
    public void unPause() {
        if (player != null) {
            player.start();
        }
    }

    /**
     * サウンドを前の曲へ
     */
    public void prevSound() {
        if (player != null) {
            //コンテンツプロバイダが提供するデータベースを示す
            Uri uri =
                Uri.parse("content://comlab.soft.db.practicebfinalprovider/");

            //IDがファイルの先頭なら最後尾に
            if (--ID == -1) {
                int last = NUM - 1;
                Cursor c = this.getContentResolver().query(uri,
                        new String[]{"music", "artist", "album", "path"},
                        "id = " + last, null, null);

                c.moveToFirst();

                //データベースから取得したパスにファイルが実際に存在するかどうか
                File music = new File(c.getString(3));
                if (music.exists()) {
                    playSound(last, c.getString(0), c.getString(1),
                            c.getString(2), c.getString(3));
                }
            } else{
                Cursor c = this.getContentResolver().query(uri,
                        new String[]{"music", "artist", "album", "path"},
                        "id = " + ID, null, null);

                c.moveToFirst();

                //データベースから取得したパスにファイルが実際に存在するかどうか
                File music = new File(c.getString(3));
                if (music.exists()) {
                    playSound(ID, c.getString(0), c.getString(1),
                            c.getString(2), c.getString(3));
                }
            }
        }
    }

    /**
     * サウンドを次の曲へ
     */
    public void nextSound() {
        if (player != null) {
            //コンテンツプロバイダが提供するデータベースを示す
            Uri uri =
                Uri.parse("content://comlab.soft.db.practicebfinalprovider/");

            //IDがファイルの最後なら0に戻る
            if (++ID == NUM) {
                Cursor c = this.getContentResolver().query(uri,
                        new String[]{"music", "artist", "album", "path"},
                        "id = " + 0, null, null);

                c.moveToFirst();

                //データベースから取得したパスにファイルが実際に存在するかどうか
                File music = new File(c.getString(3));
                if (music.exists()) {
                    playSound(0, c.getString(0), c.getString(1),
                            c.getString(2), c.getString(3));
                }
            } else{
                Cursor c = this.getContentResolver().query(uri,
                        new String[]{"music", "artist", "album", "path"},
                        "id = " + ID, null, null);

                c.moveToFirst();

                //データベースから取得したパスにファイルが実際に存在するかどうか
                File music = new File(c.getString(3));
                if (music.exists()) {
                    playSound(ID, c.getString(0), c.getString(1),
                            c.getString(2), c.getString(3));
                }
            }
        }
    }

    /**
     * サウンドの停止
     */
    public void stopSound() {
        try {
            if (player != null) {
                player.stop();
                player.setOnCompletionListener(null);
                player.release();
                player = null;
            }

            //ノティフィケーションマネージャの取得
            NotificationManager nm;
            nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            //ノティフィケーションのキャンセル
            nm.cancel(0);
        } catch (Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }
    }

    /**
     * サウンド再生終了時に呼ばれる
     *
     * @param mediaPlayer	メディアプレイヤー
     */
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            //コンテンツプロバイダが提供するデータベースを示す
            Uri uri =
                Uri.parse("content://comlab.soft.db.practicebfinalprovider/");

            //IDがファイルの最後なら0に戻る
            if (++ID == NUM) {
                Cursor c = this.getContentResolver().query(uri,
                        new String[]{"music", "artist", "album", "path"},
                        "id = " + 0, null, null);

                c.moveToFirst();

                //データベースから取得したパスにファイルが実際に存在するかどうか
                File music = new File(c.getString(3));
                if (music.exists()) {
                    playSound(0, c.getString(0), c.getString(1),
                            c.getString(2), c.getString(3));
                }
            } else{
                Cursor c = this.getContentResolver().query(uri,
                        new String[]{"music", "artist", "album", "path"},
                        "id = " + ID, null, null);

                c.moveToFirst();

                //データベースから取得したパスにファイルが実際に存在するかどうか
                File music = new File(c.getString(3));
                if (music.exists()) {
                    playSound(ID, c.getString(0), c.getString(1),
                            c.getString(2), c.getString(3));
                }
            }
        }
    }

    /**
     * サービスに接続するためのバインダーの生成
     */
    public class MPBinder extends Binder {
        //サービスの取得
        PracticeBfinalMediaPlayer getService() {
            return PracticeBfinalMediaPlayer.this;
        }
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
