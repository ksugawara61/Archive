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
 * �v���[���[�T�[�r�X
 *
 * @author g031h078
 *
 */
public class PracticeBfinalMediaPlayer extends Service
implements MediaPlayer.OnCompletionListener {
    //���O�o�͗p�^�O
    private final static String LOG_TAG =
        "PracticeBfinalMediaPlayer";

    public MediaPlayer player;	//���y�v���[���[
    private final IBinder mbinder = new MPBinder();	//�o�C���_�[�̐���
    public int ID;		//�Đ����鉹�y��ID
    private int NUM;	//���y�t�@�C���̐�

    /**
     * �T�[�r�X�̊J�n���ɌĂ΂�郁�\�b�h
     */
    @Override
    public void onStart(Intent intent, int StartID) {
        //�R���e���c�v���o�C�_���񋟂���f�[�^�x�[�X������
        Uri uri =
            Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
        Cursor c = this.getContentResolver().query(uri,
                new String[]{"id"}, null, null, null);
        //���y�t�@�C�������擾
        NUM = c.getCount();
    }

    /**
     * �T�[�r�X������ɌĂ΂�郁�\�b�h
     */
    @Override
    public void onDestroy() {
        //�T�E���h�̒�~
        stopSound();
    }

    /**
     * �T�[�r�X�ւ̒ʐM�`�����l����߂�
     *
     * @param intent	�C���e���g
     * @return mbinder	���������o�C���_�[
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mbinder;
    }

    /**
     * �T�E���h�̍Đ�
     *
     * @param id		�Đ����鉹�y��ID
     * @param music		�Đ����鉹�y��
     * @param artist	�Đ����鉹�y�̉̎薼
     * @param album		�Đ����鉹�y�̃A���o����
     * @param path		�Đ����鉹�y�̃p�X
     */
    public void playSound(int id, String music, String artist,
            String album, String path) {
        ID = id;

        //�O�̏�񂪎c���Ă���ꍇ�폜
        if (player != null) {
            player.stop();
            player.release();
        }

        //�t�@�C���v���C���[�̐���
        Uri.Builder builder = new Uri.Builder();
        builder.path(path);
        builder.scheme("file");

        //�m�e�B�t�B�P�[�V�����̕\��
        showNotification(this,R.drawable.icon,
            music + " - " + artist,
            music,
            artist + " - " + album);

        player = MediaPlayer.create(this, builder.build());

        //�v���C���[�̊J�n
        player.start();
        player.setOnCompletionListener(this);
    }

    /**
     * �T�E���h�̈ꎞ��~
     */
    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    /**
     * �T�E���h�̍ĊJ
     */
    public void unPause() {
        if (player != null) {
            player.start();
        }
    }

    /**
     * �T�E���h��O�̋Ȃ�
     */
    public void prevSound() {
        if (player != null) {
            //�R���e���c�v���o�C�_���񋟂���f�[�^�x�[�X������
            Uri uri =
                Uri.parse("content://comlab.soft.db.practicebfinalprovider/");

            //ID���t�@�C���̐擪�Ȃ�Ō����
            if (--ID == -1) {
                int last = NUM - 1;
                Cursor c = this.getContentResolver().query(uri,
                        new String[]{"music", "artist", "album", "path"},
                        "id = " + last, null, null);

                c.moveToFirst();

                //�f�[�^�x�[�X����擾�����p�X�Ƀt�@�C�������ۂɑ��݂��邩�ǂ���
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

                //�f�[�^�x�[�X����擾�����p�X�Ƀt�@�C�������ۂɑ��݂��邩�ǂ���
                File music = new File(c.getString(3));
                if (music.exists()) {
                    playSound(ID, c.getString(0), c.getString(1),
                            c.getString(2), c.getString(3));
                }
            }
        }
    }

    /**
     * �T�E���h�����̋Ȃ�
     */
    public void nextSound() {
        if (player != null) {
            //�R���e���c�v���o�C�_���񋟂���f�[�^�x�[�X������
            Uri uri =
                Uri.parse("content://comlab.soft.db.practicebfinalprovider/");

            //ID���t�@�C���̍Ō�Ȃ�0�ɖ߂�
            if (++ID == NUM) {
                Cursor c = this.getContentResolver().query(uri,
                        new String[]{"music", "artist", "album", "path"},
                        "id = " + 0, null, null);

                c.moveToFirst();

                //�f�[�^�x�[�X����擾�����p�X�Ƀt�@�C�������ۂɑ��݂��邩�ǂ���
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

                //�f�[�^�x�[�X����擾�����p�X�Ƀt�@�C�������ۂɑ��݂��邩�ǂ���
                File music = new File(c.getString(3));
                if (music.exists()) {
                    playSound(ID, c.getString(0), c.getString(1),
                            c.getString(2), c.getString(3));
                }
            }
        }
    }

    /**
     * �T�E���h�̒�~
     */
    public void stopSound() {
        try {
            if (player != null) {
                player.stop();
                player.setOnCompletionListener(null);
                player.release();
                player = null;
            }

            //�m�e�B�t�B�P�[�V�����}�l�[�W���̎擾
            NotificationManager nm;
            nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            //�m�e�B�t�B�P�[�V�����̃L�����Z��
            nm.cancel(0);
        } catch (Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }
    }

    /**
     * �T�E���h�Đ��I�����ɌĂ΂��
     *
     * @param mediaPlayer	���f�B�A�v���C���[
     */
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            //�R���e���c�v���o�C�_���񋟂���f�[�^�x�[�X������
            Uri uri =
                Uri.parse("content://comlab.soft.db.practicebfinalprovider/");

            //ID���t�@�C���̍Ō�Ȃ�0�ɖ߂�
            if (++ID == NUM) {
                Cursor c = this.getContentResolver().query(uri,
                        new String[]{"music", "artist", "album", "path"},
                        "id = " + 0, null, null);

                c.moveToFirst();

                //�f�[�^�x�[�X����擾�����p�X�Ƀt�@�C�������ۂɑ��݂��邩�ǂ���
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

                //�f�[�^�x�[�X����擾�����p�X�Ƀt�@�C�������ۂɑ��݂��邩�ǂ���
                File music = new File(c.getString(3));
                if (music.exists()) {
                    playSound(ID, c.getString(0), c.getString(1),
                            c.getString(2), c.getString(3));
                }
            }
        }
    }

    /**
     * �T�[�r�X�ɐڑ����邽�߂̃o�C���_�[�̐���
     */
    public class MPBinder extends Binder {
        //�T�[�r�X�̎擾
        PracticeBfinalMediaPlayer getService() {
            return PracticeBfinalMediaPlayer.this;
        }
    }

    /**
     * �m�e�B�t�B�P�[�V�����̕\��
     *
     * @param context �R���e�L�X�g
     * @param iconID �A�C�R��ID
     * @param ticker �e�B�b�J�[�e�L�X�g
     * @param title �^�C�g��
     * @param message ���b�Z�[�W
     */
    private void showNotification(Context context,
        int iconID, String ticker, String title, String message) {
        // �m�e�B�t�B�P�[�V�����}�l�[�W���̎擾
        NotificationManager nm;
        nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        //�m�e�B�t�B�P�[�V�����I�u�W�F�N�g�̐���
        Notification notification =
            new Notification(iconID, ticker, System.currentTimeMillis());
        PendingIntent intent = PendingIntent.getActivity(context, iconID,
                new Intent(),0);

        notification.setLatestEventInfo(context, title, message, intent);

        // �m�e�B�t�B�P�[�V�����̃L�����Z��
        nm.cancel(0);

        //�m�e�B�t�B�P�[�V�����̕\��
        nm.notify(0, notification);
    }
}
