package comlab.soft.db.practicebfinal;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * データベースを提供するプロバイダ
 *
 * @author g031h078
 *
 */
public class PracticeBfinalProvider extends ContentProvider {
    //データベース名
    private final static String DB_NAME = /*"/data/data"
        + "/comlab.soft.db.practicebfinal/files/*/"practicebfinal.db";
    //テーブル名
    private final static String DB_TABLE = "practicebfinal";
    private final static int DB_VERSION = 1;	//バージョン

    private SQLiteDatabase db;	//データベース

    /**
     * コンテンツプロバイダの初期化
     */
    @Override
    public boolean onCreate() {
        //データベースの生成
        DBHelper dbHelper = new DBHelper(getContext());
        db = dbHelper.getWritableDatabase();
        return (db != null);
    }

    /**
     * データベースのクエリ命令
     *
     * @param uri			データベースのUri
     * @param columns		カラム
     * @param selection		where条件
     * @param selectionArgs	where条件のパラメータ
     * @param sortOrder		sort条件
     * @return recode		クエリ条件を満たしたレコード
     */
    @Override
    public Cursor query(Uri uri, String[] columns, String selection,
            String[] selectionArgs, String sortOrder) {
        Cursor recode = db.query(DB_TABLE, columns, selection,
                selectionArgs, null, null, null);
        return recode;
    }

    /**
     * データベースの更新命令
     *
     * @param uri			データベースのUri
     * @param values		更新するレコード
     * @param selection		where条件
     * @param selectionArgs	where条件のパラメータ
     * @return number		更新したレコード数
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        int number = db.update(DB_TABLE, values, selection, selectionArgs);
        return number;
    }

    /**
     * データベースの挿入命令
     *
     * @param uri		データベースのUri
     * @param values	挿入するレコード
     * @return null
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        db.insert(DB_TABLE, "", values);
        return null;
    }

    /**
     * データベースの削除命令
     *
     * @param uri			データベースのUri
     * @param selection		where条件
     * @param selectionArgs	where条件のパラメータ
     * @return number		削除したレコード数
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int number = db.delete(DB_TABLE, selection, selectionArgs);
        return number;
    }

    /**
     * 種別の取得(未使用)
     *
     * @param uri
     * @return null
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }

    /**
     * データベースヘルパーの定義
     */
    private static class DBHelper extends SQLiteOpenHelper {
        /**
         * コンストラクタ
         *
         * @param context	コンテキスト
         */
        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        /**
         * データベースの生成
         *
         * @param db	データベース
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table if not exists " +
                    DB_TABLE +
                    " (id text primary key, music text, " +
                    "artist text, album text, path text)");
        }

        /**
         * データベースのアップグレード
         *
         * @param db データベース
         * @param oldVersion 前回のバージョン
         * @param newVersion 新規のバージョン
         */
        @Override
        public void onUpgrade(SQLiteDatabase db,
                int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + DB_TABLE);
            onCreate(db);
        }
    }
}
