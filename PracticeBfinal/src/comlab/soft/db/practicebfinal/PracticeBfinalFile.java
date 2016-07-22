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
 * �f�[�^�x�[�X�̍X�V���s���N���X
 *
 * @author g031h078
 *
 */
public class PracticeBfinalFile {
    //���O�o�͗p�^�O
    private final static String LOG_TAG =
        "PracticeBfinalFile";
    private int id = 0;
    private Activity activity;

    public PracticeBfinalFile(Activity act, File dir) {
        activity = act;

        //�R���e���c�v���o�C�_���񋟂���f�[�^�x�[�X������
        Uri uri =
            Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
        //�f�[�^�x�[�X�̏�����
        activity.getContentResolver().delete(uri, null, null);

        writeFiles(dir);
    }

    /**
     * ���y�t�@�C�����f�[�^�x�[�X�Ɋi�[
     *
     * @param dir	�f�B���N�g��
     */
    private void writeFiles(File dir) {
        //�f�B���N�g�����̃f�B���N�g��
        File[] dirs = dir.listFiles();
        //�f�B���N�g�����̉��y�t�@�C��
        File[] music = dir.listFiles(new MusicFileFilter());

        //�f�B���N�g�����̒T��
        int dirlen = dirs.length;
        for (int i = 0; i < dirlen; i++) {
            //�f�B���N�g�����ċA�I�ɌĂяo��
            if (dirs[i].isDirectory()) {
                writeFiles(dirs[i]);
            }
        }

        //���y�t�@�C�����f�[�^�x�[�X�Ɋi�[
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
     * �f�[�^�x�[�X�ւ̏�������
     *
     * @param file	�������ރt�@�C��
     * @param id	ID
     * @throws Exception
     */
    private void writeDB(File file, int id) throws Exception {
        //�R���e���c�v���o�C�_���񋟂���f�[�^�x�[�X������
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

        //�R���e���c�v���o�C�_���񋟂���f�[�^�x�[�X�ւ̃A�N�Z�X
        ContentValues values = new ContentValues();

        //���R�[�h�Ɋi�[����p�����[�^��t�^
        values.put("id", String.valueOf(id));
        values.put("music", music.trim());
        values.put("artist", artist.trim());
        values.put("album", album.trim());
        values.put("path", file.getAbsolutePath());

        //ID����v�����ꍇ�X�V
        int colNum =
            activity.getContentResolver().update(uri, values, "id =" + id, null);
        //��v���Ȃ��ꍇ�}��
        if (colNum == 0) {
            activity.getContentResolver().insert(uri, values);
        }
    }

    /**
     * ���y�t�@�C�����擾
     */
    public class MusicFileFilter implements FilenameFilter {
        //�t�B���^�Ώە�����
        private final String FILTER_KEYWORD = ".mp3";

        public boolean accept(File dir, String filename) {
            //�t�@�C���g���q�� .mp3�ł����true��Ԃ�
            if (filename.endsWith(FILTER_KEYWORD) == true) {
                return true;
            }

            return false;
        }
    }
}
