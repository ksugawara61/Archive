package comlab.soft.db.practicebfinal;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 *
 * @author g031h078
 *
 */
public class PracticeBfinalActivity extends Activity
implements View.OnClickListener {

    //���O�o�͗p�^�O
    private final static String LOG_TAG =
        "PracticeBfinalActivity";
    private final static int REQUEST_TEXT = 0;
    private final String PATH = "/mnt/sdcard/music/";
    private File dir = new File(PATH);

    //���C�A�E�g�̕��i
    private RadioGroup	radioGroup;	//���W�I�O���[�v
    private EditText	edtIP;		//IP�A�h���X�p�G�f�B�b�g�e�L�X�g
    private EditText	edtReq;		//���N�G�X�g�p�G�f�B�b�g�e�L�X�g
    private CheckBox	mode;		//���[�h�I��p�`�F�b�N�{�^��
    private TextView	lblReceive;	//��M���x��

    //�\�P�b�g�ʐM�̕��i
    private String			IP;			//IP�A�h���X

    private final static Handler handler = new Handler();	//�n���h��
    private PracticeBfinalServer Server;
    private PracticeBfinalClient Client;

    /**
     * �A�N�e�B�r�e�B�������ɌĂ΂�郁�\�b�h
     * �A�v���̏�����
     *
     * @param icicle
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //���C�A�E�g�̐���
        LinearLayout layout = new LinearLayout(this);
        layout.setBackgroundColor(Color.rgb(255, 255, 255));
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout Clayout = new LinearLayout(this);
        Clayout.setBackgroundColor(Color.rgb(255, 255, 255));
        Clayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout Blayout = new LinearLayout(this);
        Blayout.setBackgroundColor(Color.rgb(255, 255, 255));
        Blayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout Rlayout = new LinearLayout(this);
        Rlayout.setBackgroundColor(Color.rgb(255, 255, 255));
        Rlayout.setOrientation(LinearLayout.HORIZONTAL);

        //CheckBox �̍쐬
        mode = new CheckBox(this);
        mode.setText("���[�J�����[�h");
        mode.setTextColor(Color.rgb( 0, 0, 0));
        setLLParams(mode,
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(mode);

        //�G�f�B�b�g�e�L�X�g�̐���
        edtIP = new EditText(this);
        edtIP.setText("", EditText.BufferType.NORMAL);
        edtIP.setWidth(400);
        setLLParams(edtIP);
        Clayout.addView(edtIP);

        //�{�^���̐���
        Clayout.addView(makeButton(0, "�ڑ�", 100));
        layout.addView(Clayout);

        layout.addView(makeButton(1, "�ؒf", 500));

        //���W�I�O���[�v�̐���
        radioGroup = new RadioGroup(this);
        radioGroup.addView(makeRadio(1, "�Ȗ�"));
        radioGroup.addView(makeRadio(2, "�A�[�e�B�X�g��"));
        radioGroup.addView(makeRadio(3, "�A���o����"));
        radioGroup.check(1);
        setLLParams(radioGroup);
        layout.addView(radioGroup);

        edtReq = new EditText(this);
        edtReq.setText("", EditText.BufferType.NORMAL);
        edtReq.setWidth(400);
        setLLParams(edtReq);
        Rlayout.addView(edtReq);

        Rlayout.addView(makeButton(2, "���M", 100));
        layout.addView(Rlayout);
        layout.addView(makeButton(3, "�Đ�", 500));

        Blayout.addView(makeButton(4, "�O�̋�", 150));
        Blayout.addView(makeButton(5, "�ꎞ��~", 190));
        Blayout.addView(makeButton(6, "���̋�", 150));
        layout.addView(Blayout);

        layout.addView(makeButton(7, "��~", 500));
        layout.addView(makeButton(8, "�f�[�^�x�[�X�̍X�V", 500));

        //��M���x���̐���
        lblReceive = new TextView(this);
        lblReceive.setId(1);
        lblReceive.setText("");
        lblReceive.setTextSize(16.0f);
        lblReceive.setTextColor(Color.rgb(0, 0, 0));
        setLLParams(lblReceive,
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(lblReceive);

        //�X�N���[���r���[�̐���
        ScrollView scrollView=new ScrollView(this);
        scrollView.addView(layout);
        setContentView(scrollView);

        Server = new PracticeBfinalServer(handler, lblReceive, this);
        Client = new PracticeBfinalClient(handler, lblReceive, this);

        //�T�[�o�[�̐���
        Server.CreateServer();

        //�C���e���g�̐���
        Intent intent = new Intent(this,
                comlab.soft.db.practicebfinal.PracticeBfinalMediaPlayer.class);
        this.startService(intent);
        Server.doBindService(intent);

        //�T�[�o�[�����̊J�n
        Server.start();
    }

    /**
     * �A�v���̒�~
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    /**
     * �{�^���N���b�N�C�x���g�̏���
     *
     * @param view �r���[
     */
    public void onClick(View view) {
        //�{�^���� id���擾
        int id = view.getId();

        //�ڑ��{�^�����������ꍇ
        if (id == 0) {
            IP = edtIP.getText().toString();

            (new Thread() {public void run() {
                Client.connect(IP, 8080);
            }}).start();
        }

        //�ؒf�{�^���������ꂽ�ꍇ
        if (id == 1) {
            Client.SendClient("cut");
            Client.CloseClient(false);
            //�m�e�B�t�B�P�[�V�����}�l�[�W���̎擾
            NotificationManager nm;
            nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            //�m�e�B�t�B�P�[�V�����̃L�����Z��
            nm.cancel(0);
        }

        //�Ȃ̃��N�G�X�g�{�^���������ꂽ�ꍇ
        if (id == 2) {
            int radioID = radioGroup.getCheckedRadioButtonId();
            if (mode.isChecked()) {
                localRequest(radioID, edtReq.getText().toString());
            } else {
                String column = new String();

                if (radioID == 1) {
                    column = "mus";
                } else if (radioID == 2) {
                    column = "art";
                } else if (radioID == 3) {
                    column = "alb";
                }

                Client.SendClient("req" + column + edtReq.getText().toString());
            }

            edtReq.setText("", TextView.BufferType.NORMAL);
        }

        //�Đ��{�^���������ꂽ�ꍇ
        if (id == 3) {
            if (mode.isChecked()) {
                Server.MP.unPause();
            } else {
                Client.SendClient("pla");
            }
        }

        //�O�̋ȃ{�^���������ꂽ�ꍇ
        if (id == 4) {
            if (mode.isChecked()) {
                Server.MP.prevSound();
            } else {
                Client.SendClient("pre");
            }
        }

        //�ꎞ��~�{�^���������ꂽ�ꍇ
        if (id == 5) {
            if (mode.isChecked()) {
                Server.MP.pause();
            } else {
                Client.SendClient("pau");
            }
        }

        //���̋ȃ{�^���������ꂽ�ꍇ
        if (id == 6) {
            if (mode.isChecked()) {
                Server.MP.nextSound();
            } else {
                Client.SendClient("nex");
            }
        }


        //��~�{�^���������ꂽ�ꍇ
        if (id == 7) {
            if (mode.isChecked()) {
                Server.MP.stopSound();
            } else {
                Client.SendClient("sto");
            }
            //�m�e�B�t�B�P�[�V�����}�l�[�W���̎擾
            NotificationManager nm;
            nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            //�m�e�B�t�B�P�[�V�����̃L�����Z��
            nm.cancel(0);

        }

        //�f�[�^�x�[�X�̍X�V�{�^���������ꂽ�ꍇ
        if (id == 8) {
            if (dir.exists()) {
                new PracticeBfinalFile(this, dir);

                //�C���e���g�̐���
                Intent intent = new Intent(this,
                        comlab.soft.db.practicebfinal.PracticeBfinalMediaPlayer.class);
                this.stopService(intent);
                Server.doUnbindService();

                this.startService(intent);
                Server.doBindService(intent);
            } else {
                //�f�B���N�g�������݂��Ȃ��ꍇ�A�v���P�[�V�������I��
                showDialog(this, "�G���[",
                        "�w�肳�ꂽ�p�X�����݂Ȃ����߃f�[�^�x�[�X���X�V�ł��܂���");
            }
        }
    }

    /**
     * �\�P�b�g�̐ؒf
     */
    private void disconnect() {
        //�C���e���g�̐���
        Intent intent = new Intent(this,
                comlab.soft.db.practicebfinal.PracticeBfinalMediaPlayer.class);
        this.stopService(intent);
        Server.doUnbindService();
        try {
            Server.CloseServer();
            Client.CloseClient(true);
        } catch (Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }
    }

    /**
     * ���[�J���ł̃��N�G�X�g
     *
     * @param search	�������@
     * @param request	���N�G�X�g
     */
    private void localRequest(int search, String request) {
        String column = new String();

        if (search == 1) {
            column = "music";
        } else if (search == 2) {
            column = "artist";
        } else if (search == 3) {
            column = "album";
        }

      //�R���e���c�v���o�C�_���񋟂���f�[�^�x�[�X������
        Uri uri =
            Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
        //���N�G�X�g�ɊY�����郌�R�[�h�̎擾
        Cursor c = this.getContentResolver().query(uri,
                new String[]{"id", "music", "artist", "album"},
                column + " like '%" + request + "%'", null, null);

        if (c.getCount() > 0) {
            String[]	ids = new String[1024];
            String[] 	musics = new String[1024];
            String[] 	artists = new String[1024];
            String[] 	albums = new String[1024];
            int count = 0;

            c.moveToFirst();
            do {
                ids[count] = c.getString(0);
                musics[count] = c.getString(1);
                artists[count] = c.getString(2);
                albums[count] = c.getString(3);
                count++;
            } while (c.moveToNext());

            //�C���e���g�̐���
            Intent intent = new Intent(this,
                    comlab.soft.db.practicebfinal.PracticeBfinalList.class);

            //�C���e���g�Ƀp�����[�^��t�^
            intent.putExtra("num", count);
            intent.putExtra("id", ids);
            intent.putExtra("music", musics);
            intent.putExtra("artist", artists);
            intent.putExtra("album", albums);

            //�A�N�e�B�r�e�B�̕\��
            this.startActivityForResult(intent, REQUEST_TEXT);
        }
    }

    /**
     * �A�N�e�B�r�e�B�Ăяo�����ʂ̎擾
     *
     * @param requestCode	���N�G�X�g�R�[�h
     * @param resultCode	���ʃR�[�h
     * @param intent		�C���e���g
     */
    @Override
    protected void onActivityResult(int requestCode,
            int resultCode, Intent intent) {
        if (requestCode == REQUEST_TEXT && resultCode == RESULT_OK) {
            //�C���e���g����p�����[�^�̎擾
            String id = new String();
            String music = new String();
            String artist = new String();
            String album = new String();

            //Bundle�I�u�W�F�N�g����������
            Bundle extras = intent.getExtras();

            if (extras != null) {
                id = extras.getString("id");
                music = extras.getString("music");
                artist = extras.getString("artist");
                album = extras.getString("album");
            }

            if (mode.isChecked()){
                //�R���e���c�v���o�C�_���񋟂���f�[�^�x�[�X������
                Uri uri =
                    Uri.parse("content://comlab.soft.db.practicebfinalprovider/");
                Cursor c = this.getContentResolver().query(uri,
                        new String[]{"id", "music", "artist", "album", "path"},
                        "id = " + id, null, null);

                if (c.getCount() == 1) {
                    c.moveToFirst();

                    //�f�[�^�x�[�X����擾�����p�X�Ƀt�@�C�������ۂɑ��݂��邩�ǂ���
                    File file = new File(c.getString(4));
                    if (file.exists()) {
                        Server.MP.playSound(Integer.parseInt(c.getString(0)), c.getString(1),
                                c.getString(2), c.getString(3), c.getString(4));
                    }
                }
            } else {
                Client.SendClient("sta" + id);
                showNotification(this, R.drawable.icon,
                        music + " - " + artist,
                        music,
                        artist + " - " + album);
            }
        }
    }

    /**
     * �{�^���̐���
     *
     * @param id		�{�^����ID
     * @param text		�{�^���ɕ\������e�L�X�g
     * @param width		�{�^���̕�
     * @return button	�������ꂽ�{�^��
     */
    private Button makeButton(int id, String text, int width) {
        Button button = new Button(this);
        button.setId(id);
        button.setWidth(width);
        button.setText(text);
        button.setOnClickListener(this);
        setLLParams(button);
        return button;
    }

    /**
     * ���W�I�{�^���̐���
     *
     * @param id		���W�I�{�^����ID
     * @param text		���W�I�{�^���ɕ\������e�L�X�g
     * @return radio	�������ꂽ���W�I�{�^��
     */
    private RadioButton makeRadio(int id, String text) {
        RadioButton radio = new RadioButton(this);
        radio.setId(id);
        radio.setHeight(10);
        radio.setText(text);
        radio.setTextColor(Color.rgb(0, 0, 0));

        return radio;
    }

    /**
     * ���C�i�[���C�A�E�g�̃p�����[�^�w��
     *
     * @param view ���C�A�E�g��ݒ肷��r���[
     */
    private static void setLLParams(View view) {
        view.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
    }

    /**
     * ���C�i�[���C�A�E�g�̃p�����[�^�w��
     *
     * @param view		���C�A�E�g��ݒ肷��r���[
     * @param width		��
     * @param height	����
     */
    public static void setLLParams(View view, int width, int height) {
        view.setLayoutParams(new LinearLayout.LayoutParams(width, height));
    }

    /**
     * �_�C�A���O�̕\��
     *
     * @param activity �A�N�e�B�r�e�B
     * @param title �^�C�g��
     * @param text �e�L�X�g
     * @param error �G���[���ǂ���
     */
     private static void showDialog(final Activity activity,
             String title, String text) {
         AlertDialog.Builder ad = new AlertDialog.Builder(activity);
         ad.setTitle(title);
         ad.setMessage(text);
         ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 activity.setResult(Activity.RESULT_OK);
             }
         });
         ad.create();
         ad.show();
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