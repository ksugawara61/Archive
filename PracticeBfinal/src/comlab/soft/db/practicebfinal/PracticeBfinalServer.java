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
 * �T�[�o�[�������s���N���X
 *
 * @author g031h078
 *
 */
public class PracticeBfinalServer extends Thread {

    //���O�o�͗p�^�O
    private final static String LOG_TAG =
        "PracticeBfinalServer";
    //���s����
    private final static String	BR =
        System.getProperty("line.separator");

    private ServerSocket	server;	//�T�[�o�\�P�b�g
    private Socket			client;	//�N���C�A���g�\�P�b�g
    private TextView	lblReceive;	//��M���x��
    private Activity	activity;
    private static Handler handler ;	//�n���h��

    //�T�[�r�X�ڑ��̕��i
    public PracticeBfinalMediaPlayer MP;	//�T�[�r�X�N���X
    private boolean mIsBound = false;		//�T�[�r�X�̐ڑ���
    private ServiceConnection connection = new ServiceConnection() {
        //�T�[�r�X�ւ̃_�C���N�g�A�N�Z�X���\�ɂ���
        public void onServiceConnected(ComponentName classname, IBinder service) {
            MP = ((PracticeBfinalMediaPlayer.MPBinder)service).getService();
        }

        //�T�[�r�X�̏�����
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
     * �T�[�o�\�P�b�g�̐���
     */
    public void CreateServer() {
        try {
            //�T�[�o�\�P�b�g�̐���
            server = new ServerSocket(8080);
            addText("�ڑ��ҋ@���|�[�g>" + server.getLocalPort());
        } catch (Exception e) {
            addText("�T�[�o�����G���[");
            Log.i(LOG_TAG, e.getMessage());
        }
    }

    /**
     * �T�[�o�\�P�b�g�̃N���[�Y
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
     * �T�[�o�����̊J�n
     */
    public void run() {
        while (server != null) {
            try {
                //�ڑ��ҋ@
                client = server.accept();

                //�N���C�A���gIP�̏o��
                String clientIP = client.getInetAddress().toString();
                addText("�N���C�A���gIP>" + clientIP);

                //�T�[�o�̎�M����
                ReceiveServer();
            } catch (Exception e) {
                try {
                    client.close();
                } catch (Exception ex) {
                }
                addText("�N���C�A���g�Ƃ̐ڑ����ؒf����܂���");
                Log.i(LOG_TAG, e.getMessage());
            }
        }
    }

    /**
     * ���N�G�X�g����M�i�T�[�o�[���j
     */
    public void ReceiveServer() {
        try {
            //���̓X�g���[���̐���
            InputStream in = null;

            while (client != null && client.isConnected()) {
                //���̓X�g���[���̎擾
                in = client.getInputStream();
                byte[] w = new byte[1024];
                //���N�G�X�g�̎�M
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
     * ���N�G�X�g����
     *
     * @param event	���N�G�X�g
     * @param data	�f�[�^
     */
    private void ServerEvent(String event, String data) {
        //���N�G�X�g���Ȃ̃��N�G�X�g�̏ꍇ
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

            //�R���e���c�v���o�C�_���񋟂���f�[�^�x�[�X������
            Uri uri =
                Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
            //���N�G�X�g�ɊY�����郌�R�[�h�̎擾
            Cursor c = activity.getContentResolver().query(uri,
                    new String[]{"id", "music", "artist", "album"},
                    column + " like '%" + name + "%'", null, null);

            //�擾�������R�[�h0�ȏォ�ǂ���
            if (c.getCount() > 0) {
                c.moveToFirst();
                String sender = "req";
                do {
                    sender = sender + c.getString(0) + "'" + c.getString(1) +
                    "'" + c.getString(2) + "'" + c.getString(3) + BR;
                } while(c.moveToNext());
                //���R�[�h���̑��M
                SendServer(sender);
            } else {
                //������Ȃ��������Ƃ𑗐M
                SendServer("not");
            }
        } else

        if (event.equals("sta")) {
            //�R���e���c�v���o�C�_���񋟂���f�[�^�x�[�X������
            Uri uri =
                Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
            Cursor c = activity.getContentResolver().query(uri,
                    new String[]{"id", "music", "artist", "album", "path"},
                    "id = " + data, null, null);

            if (c.getCount() == 1) {
                c.moveToFirst();

                //�f�[�^�x�[�X����擾�����p�X�Ƀt�@�C�������ۂɑ��݂��邩�ǂ���
                File music = new File(c.getString(4));
                if (music.exists()) {
                    MP.playSound(Integer.parseInt(c.getString(0)), c.getString(1),
                            c.getString(2), c.getString(3), c.getString(4));
                } else {
                    //������Ȃ��������Ƃ𑗐M
                    SendServer("not");
                }
            }
        }

        //���N�G�X�g���Đ��̏ꍇ
        if (event.equals("pla")) {
            MP.unPause();
        }

        //���N�G�X�g���O�̋Ȃ̏ꍇ
        if (event.equals("pre")) {
            MP.prevSound();

            //�R���e���c�v���o�C�_���񋟂���f�[�^�x�[�X������
            Uri uri =
                Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
            Cursor c = activity.getContentResolver().query(uri,
                    new String[]{"id", "music", "artist", "album", "path"},
                    "id = " + MP.ID, null, null);

            c.moveToFirst();
            SendServer("cha" + c.getString(1) + "'" + c.getString(2) +
                    "'" + c.getString(3));
        }

        //���N�G�X�g���ꎞ��~�̏ꍇ
        if (event.equals("pau")) {
            MP.pause();
        }

        //���N�G�X�g�����̋Ȃ̏ꍇ
        if (event.equals("nex")) {
            MP.nextSound();

            //�R���e���c�v���o�C�_���񋟂���f�[�^�x�[�X������
            Uri uri =
                Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
            Cursor c = activity.getContentResolver().query(uri,
                    new String[]{"id", "music", "artist", "album", "path"},
                    "id = " + MP.ID, null, null);

            c.moveToFirst();
            SendServer("cha" + c.getString(1) + "'" + c.getString(2) +
                    "'" + c.getString(3));
        }

        //���N�G�X�g����~�̏ꍇ
        if (event.equals("sto")) {
            MP.stopSound();
        }

        //���N�G�X�g���ؒf�̏ꍇ
        if (event.equals("cut")) {
            try {
                client.close();
                client = null;
                addText("�N���C�A���g�Ƃ̐ڑ�����������܂���");
            } catch (Exception e) {
                Log.i(LOG_TAG, e.getMessage());
            }
        }
    }

    /**
     * ���N�G�X�g�𑗐M�i�T�[�o�[���j
     *
     * @param data		���M�f�[�^
     */
    private void SendServer(String data) {
        try {
            //�o�̓X�g���[���̎擾
            OutputStream out = client.getOutputStream();

            //���N�G�X�g���擾���A�o�C�g�z��֕ϊ�
            byte[] w = data.getBytes("Shift_JIS");
            //�o�C�g�z��̏�������
            out.write(w);
            //�o�C�g�z��𖾎��I�ɑ��M
            out.flush();
        } catch (Exception e) {
            addText("���M���s�i�T�[�o�[�j");

            Log.i(LOG_TAG, e.getMessage());
        }
    }

    /**
     * �T�[�r�X�Ƃ̐ڑ�
     *
     * @param intent	�C���e���g
     */
    public void doBindService(Intent intent) {
        //�T�[�r�X�֐ڑ�����
        activity.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /**
     * �T�[�r�X�Ƃ̐ڑ�������
     */
    public void doUnbindService() {
        if (mIsBound) {
            //�R�l�N�V�����̉���
            activity.unbindService(connection);
        }
        mIsBound = false;
    }

    /**
     * ��M�e�L�X�g�̒ǉ�
     *
     * @param text	��M����e�L�X�g
     */
    private void addText(final String text) {
        handler.post(new Runnable() {
            public void run() {
                lblReceive.setText(text + BR + lblReceive.getText());
            }
        });
    }
}
