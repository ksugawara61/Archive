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
 * �N���C�A���g�������s���N���X
 *
 * @author g031h078
 *
 */
public class PracticeBfinalClient {

    //���O�o�͗p�^�O
    private final static String LOG_TAG =
        "PracticeBfinalClient";
    //���s����
    private final static String	BR =
        System.getProperty("line.separator");
    private final static int REQUEST_TEXT = 0;

    private Socket		socket;		//�\�P�b�g
    private String[]	ids = new String[1024];
    private String[] 	musics = new String[1024];
    private String[] 	artists = new String[1024];
    private String[] 	albums = new String[1024];
    private int 		count = 0;

    private TextView	lblReceive;	//��M���x��
    private Activity	activity;
    private static Handler handler ;	//�n���h��

    public PracticeBfinalClient (Handler hand, TextView view, Activity act) {
        handler = hand;
        lblReceive = view;
        activity = act;
    }

    /**
     * �\�P�b�g�̃N���[�Y
     *
     * @param close		�A�N�e�B�r�e�B�̏I�����ǂ���
     */
    public void CloseClient(boolean close) {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
                if (close == false) {
                    addText("�T�[�o�[�Ƃ̐ڑ����������܂���");
                }
            }
        } catch (Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }
    }

    /**
     * �\�P�b�g�̐ڑ�
     *
     * @param ip	IP�A�h���X
     * @param port	�|�[�g�ԍ�
     */
    public void connect(String ip, int port) {
        if (socket == null) {
            try {
                InputStream in = null;
                byte[] w = new byte[131072];

                //�\�P�b�g����
                addText("�T�[�o�[�ڑ���");
                socket = new Socket(ip, port);
                //���̓X�g���[���̎擾
                in = socket.getInputStream();
                addText("�T�[�o�[�ڑ�����");


                //��M���[�v
                while (socket != null && socket.isConnected()) {
                    //�T�[�o����f�[�^�̎�M
                    int size = in.read(w);
                    if (size <= 0) {
                        continue;
                    }

                    ClientEvent(w, size);
                }
            } catch (Exception e) {
                addText("�T�[�o�[�Ƃ̐ڑ��Ɏ��s���܂���");
                Log.i(LOG_TAG, e.getMessage());
            }
        } else {
            addText("���łɐڑ�����Ă��܂�");
        }
    }

    /**
     * ���N�G�X�g����
     *
     * @param w		���N�G�X�g�̃o�C�g�f�[�^
     * @param size	�o�C�g�f�[�^�̑傫��
     * @throws Exception
     */
    private void ClientEvent(byte[] w, int size) throws Exception {
        String event = new String(w, 0, 3, "Shift_JIS");

        //�Ȃ̈ꗗ���擾�ł������ǂ���
        if (event.equals("req")) {
            //���y�t�@�C�������R�[�h���Ƃɐ؂���
            String recode[] = new String(w, 3, size - 3, "Shift_JIS").split(BR);
            int recodelen = recode.length;

            for (int i = 0; i < recodelen; i++) {
                //���R�[�h���e�[�u�����Ƃɐ؂���
                String table[] = recode[i].split("'");

                ids[count] = table[0].trim();
                musics[count] = table[1].trim();
                artists[count] = table[2].trim();
                albums[count] = table[3].trim();
                count++;
            }

            //���X�g�̕\��
            ListIndication();
        } else if (event.equals("not")) {
            addText("������܂���ł���");
        } else if (event.equals("cha")) {
            String info[] = new String(w, 3, size -3, "Shift_JIS").split("'");
            showNotification(activity, R.drawable.icon,
                    info[0] + " - " + info[1],
                    info[0],
                    info[1] + " - " + info[2]);
        }
    }

    /**
     * ���N�G�X�g�𑗐M�i�N���C�A���g���j
     *
     * @param str	���M�f�[�^
     */
    public void SendClient(String str) {
        if (socket != null && socket.isConnected()) {
            try {
                //�o�̓X�g���[���̎擾
                OutputStream out = socket.getOutputStream();

                str = str.replaceAll("'", "\"");
                //���N�G�X�g���擾���A�o�C�g�z��֕ϊ�
                byte[] w = str.getBytes("Shift_JIS");
                //�o�C�g�z��̏�������
                out.write(w);
                //�o�C�g�z��𖾎��I�ɑ��M
                out.flush();
            } catch (Exception e) {
                addText("���M���s�i�N���C�A���g�j");
                Log.i(LOG_TAG, e.getMessage());
            }
        } else {
            addText("�T�[�o�[�ɐڑ�����Ă��܂���");
        }
    }

    /**
     * ���X�g�̕\��
     */
    private void ListIndication() {
        if (socket != null && socket.isConnected()) {
            if (count != 0) {
                //�C���e���g�̐���
                Intent intent = new Intent(activity,
                        comlab.soft.db.practicebfinal.PracticeBfinalList.class);

                //�C���e���g�Ƀp�����[�^��t�^
                intent.putExtra("num", count);
                intent.putExtra("id", ids);
                intent.putExtra("music", musics);
                intent.putExtra("artist", artists);
                intent.putExtra("album", albums);

                //�A�N�e�B�r�e�B�̕\��
                activity.startActivityForResult(intent, REQUEST_TEXT);

                //���X�g�̏�����
                musics = new String[1024];
                artists = new String[1024];
                albums = new String[1024];
                count = 0;
            }
        }
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
        nm = (NotificationManager)activity.getSystemService(Context.NOTIFICATION_SERVICE);

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