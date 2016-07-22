package comlab.soft.db.practicebfinal;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * リスト表示するクラス
 *
 * @author g031h078
 *
 */
public class PracticeBfinalList extends Activity
implements AdapterView.OnItemClickListener {
    private String[] Getids;
    private String[] Getmusics;
    private String[] Getartists;
    private String[] Getalbums;

    private class Music {
        private String music;
        private String artist;
        private String album;

        public Music(String music, String artist, String album) {
            this.music = music;
            this.artist = artist;
            this.album = album;
        }

        public String getMusic() {
            return music;
        }

        public String getArtist() {
            return artist;
        }

        public String getAlbum() {
            return album;
        }
    }

    private static class ViewHolder {
        TextView Music;
        TextView ArtandAlb;
    }

    private class ListMusicAdapter extends BaseAdapter {
        private Context context;
        private List<Music> list;

        public ListMusicAdapter(Context context) {
            super();
            this.context = context;
            list = new ArrayList<Music>();
            int num = 0;

            //インテントからのパラメータ取得
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                num = extras.getInt("num");
                Getids = extras.getStringArray("id");
                Getmusics = extras.getStringArray("music");
                Getartists = extras.getStringArray("artist");
                Getalbums = extras.getStringArray("album");
            }

            for (int i = 0; i < num; i++) {
                list.add(new Music(Getmusics[i], Getartists[i], Getalbums[i]));
            }
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {

            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            Music music = (Music)getItem(position);
            ViewHolder holder;

            if (view == null) {
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                view = layout;

                holder = new ViewHolder();
                holder.Music = new TextView(context);
                holder.ArtandAlb = new TextView(context);
                layout.addView(holder.Music);
                layout.addView(holder.ArtandAlb);

                view.setTag(holder);
            } else {
                holder = (ViewHolder)view.getTag();
            }

            holder.Music.setText(music.getMusic());
            holder.ArtandAlb.setText(music.getArtist()
                    + " - " + music.getAlbum());

            return view;
        }
    }


    /**
     * アクティビティ生成時に呼ばれるメソッド
     * アプリの初期化
     *
     * @param icicle
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        //リストビューの生成
        ListView lv = new ListView(this);
        setContentView(lv);

        lv.setOnItemClickListener(this);
        lv.setAdapter(new ListMusicAdapter(this));
    }

    /**
     * アイテムのクリックイベント
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view,
            int position, long id) {
        //インテントの生成
        Intent intent = new Intent();
        //インテントパラメータの付与
        intent.putExtra("id", Getids[position]);
        intent.putExtra("music", Getmusics[position]);
        intent.putExtra("artist", Getartists[position]);
        intent.putExtra("album", Getalbums[position]);

        setResult(Activity.RESULT_OK, intent);

        //アクティビティの終了
        finish();
    }
}
