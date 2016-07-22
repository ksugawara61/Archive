package comlab.soft.db.practicebfinal;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * �f�[�^�x�[�X��񋟂���v���o�C�_
 *
 * @author g031h078
 *
 */
public class PracticeBfinalProvider extends ContentProvider {
    //�f�[�^�x�[�X��
    private final static String DB_NAME = /*"/data/data"
        + "/comlab.soft.db.practicebfinal/files/*/"practicebfinal.db";
    //�e�[�u����
    private final static String DB_TABLE = "practicebfinal";
    private final static int DB_VERSION = 1;	//�o�[�W����

    private SQLiteDatabase db;	//�f�[�^�x�[�X

    /**
     * �R���e���c�v���o�C�_�̏�����
     */
    @Override
    public boolean onCreate() {
        //�f�[�^�x�[�X�̐���
        DBHelper dbHelper = new DBHelper(getContext());
        db = dbHelper.getWritableDatabase();
        return (db != null);
    }

    /**
     * �f�[�^�x�[�X�̃N�G������
     *
     * @param uri			�f�[�^�x�[�X��Uri
     * @param columns		�J����
     * @param selection		where����
     * @param selectionArgs	where�����̃p�����[�^
     * @param sortOrder		sort����
     * @return recode		�N�G�������𖞂��������R�[�h
     */
    @Override
    public Cursor query(Uri uri, String[] columns, String selection,
            String[] selectionArgs, String sortOrder) {
        Cursor recode = db.query(DB_TABLE, columns, selection,
                selectionArgs, null, null, null);
        return recode;
    }

    /**
     * �f�[�^�x�[�X�̍X�V����
     *
     * @param uri			�f�[�^�x�[�X��Uri
     * @param values		�X�V���郌�R�[�h
     * @param selection		where����
     * @param selectionArgs	where�����̃p�����[�^
     * @return number		�X�V�������R�[�h��
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        int number = db.update(DB_TABLE, values, selection, selectionArgs);
        return number;
    }

    /**
     * �f�[�^�x�[�X�̑}������
     *
     * @param uri		�f�[�^�x�[�X��Uri
     * @param values	�}�����郌�R�[�h
     * @return null
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        db.insert(DB_TABLE, "", values);
        return null;
    }

    /**
     * �f�[�^�x�[�X�̍폜����
     *
     * @param uri			�f�[�^�x�[�X��Uri
     * @param selection		where����
     * @param selectionArgs	where�����̃p�����[�^
     * @return number		�폜�������R�[�h��
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int number = db.delete(DB_TABLE, selection, selectionArgs);
        return number;
    }

    /**
     * ��ʂ̎擾(���g�p)
     *
     * @param uri
     * @return null
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }

    /**
     * �f�[�^�x�[�X�w���p�[�̒�`
     */
    private static class DBHelper extends SQLiteOpenHelper {
        /**
         * �R���X�g���N�^
         *
         * @param context	�R���e�L�X�g
         */
        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        /**
         * �f�[�^�x�[�X�̐���
         *
         * @param db	�f�[�^�x�[�X
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table if not exists " +
                    DB_TABLE +
                    " (id text primary key, music text, " +
                    "artist text, album text, path text)");
        }

        /**
         * �f�[�^�x�[�X�̃A�b�v�O���[�h
         *
         * @param db �f�[�^�x�[�X
         * @param oldVersion �O��̃o�[�W����
         * @param newVersion �V�K�̃o�[�W����
         */
        @Override
        public void onUpgrade(SQLiteDatabase db,
                int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + DB_TABLE);
            onCreate(db);
        }
    }
}
