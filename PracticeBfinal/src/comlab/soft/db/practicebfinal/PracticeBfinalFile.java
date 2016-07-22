package comlab.soft.db.practicebfinal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.nio.channels.FileChannel;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

/**
 * データベースの更新を行うクラス
 *
 * @author g031h078
 *
 */
public class PracticeBfinalFile {
    //ログ出力用タグ
    private final static String LOG_TAG =
        "PracticeBfinalFile";
    private int id = 0;
    private Activity activity;

    public PracticeBfinalFile(Activity act, File dir) {
        activity = act;

        //コンテンツプロバイダが提供するデータベースを示す
        Uri uri =
            Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
        //データベースの初期化
        activity.getContentResolver().delete(uri, null, null);

        writeFiles(dir);
    }

    /**
     * 音楽ファイルをデータベースに格納
     *
     * @param dir	ディレクトリ
     */
    private void writeFiles(File dir) {
        //ディレクトリ内のディレクトリ
        File[] dirs = dir.listFiles();
        //ディレクトリ内の音楽ファイル
        File[] music = dir.listFiles(new MusicFileFilter());

        //ディレクトリ内の探索
        int dirlen = dirs.length;
        for (int i = 0; i < dirlen; i++) {
            //ディレクトリを再帰的に呼び出し
            if (dirs[i].isDirectory()) {
                writeFiles(dirs[i]);
            }
        }

        //音楽ファイルをデータベースに格納
        int musiclen = music.length;
        for (int i = 0; i < musiclen; i++) {
            try {
                writeDB(music[i], id);
                id++;
            } catch (Exception e) {
                Log.i(LOG_TAG, e.getMessage());
            }
        }
    }

    /**
     * データベースへの書き込み
     *
     * @param file	書き込むファイル
     * @param id	ID
     * @throws Exception
     */
    private void writeDB(File file, int id) throws Exception {
        //コンテンツプロバイダが提供するデータベースを示す
        Uri uri =
            Uri.parse("content://comlab.soft.db.practicebfinalprovider/");

        FileInputStream in = new FileInputStream(file);
        FileChannel channel = in.getChannel();
        channel.position(channel.size() - 128);
        byte[] w = new byte[128];

        in.read(w);

        String music = new String(w, 3, 30, "Shift_JIS").replaceAll("'", "\"");
        String artist = new String(w, 33, 30, "Shift_JIS").replaceAll("'", "\"");
        String album = new String(w, 63, 30, "Shift_JIS").replaceAll("'", "\"");
        Log.i(LOG_TAG, music.trim());
        Log.i(LOG_TAG, artist.trim());
        Log.i(LOG_TAG, album.trim());
        Log.i(LOG_TAG, file.getAbsolutePath());

        //コンテンツプロバイダが提供するデータベースへのアクセス
        ContentValues values = new ContentValues();

        //レコードに格納するパラメータを付与
        values.put("id", String.valueOf(id));
        values.put("music", music.trim());
        values.put("artist", artist.trim());
        values.put("album", album.trim());
        values.put("path", file.getAbsolutePath());

        //IDが一致した場合更新
        int colNum =
            activity.getContentResolver().update(uri, values, "id =" + id, null);
        //一致しない場合挿入
        if (colNum == 0) {
            activity.getContentResolver().insert(uri, values);
        }
    }

    /**
     * 音楽ファイルを取得
     */
    public class MusicFileFilter implements FilenameFilter {
        //フィルタ対象文字列
        private final String FILTER_KEYWORD = ".mp3";

        public boolean accept(File dir, String filename) {
            //ファイル拡張子が .mp3であればtrueを返す
            if (filename.endsWith(FILTER_KEYWORD) == true) {
                return true;
            }

            return false;
        }
    }
}
