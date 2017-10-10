package net.alexandroid.utils.exoplayerlibrary.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import net.alexandroid.utils.exoplayerlibrary.R;

import java.util.ArrayList;
import java.util.Arrays;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        serRecyclerView();
    }

    private void serRecyclerView() {

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<VideoItem> list = getVideoItemsList();

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(list);
        getLifecycle().addObserver(adapter);
        recyclerView.setAdapter(adapter);
    }

    @NonNull
    private ArrayList<VideoItem> getVideoItemsList() {
        return new ArrayList<>(Arrays.asList(
                new VideoItem("http://ynethd-i.akamaihd.net/i/cdnwiz/1017/091017_yedioth_baby_fix_bP3D0Kgw_800.mp4/master.m3u8",
                        "https://images1.ynet.co.il/PicServer5/2017/10/10/8082267/80822570100292640360no.jpg"),
                new VideoItem("http://ynethd-i.akamaihd.net/i/cdnwiz/1017/0710171807_SWISS_GIANTS_7jHXuKJW_800.mp4/master.m3u8",
                        "https://images1.ynet.co.il/PicServer5/2017/10/08/8076970/80760114998850640360no.jpg"),
                new VideoItem("http://ynethd-i.akamaihd.net/i/cdnwiz/1017/031017_yediot_batito_amazonas_3_Coqt1YHG_800.mp4/master.m3u8",
                        "https://images1.ynet.co.il/PicServer5/2017/10/08/8077083/shofar.jpg"),
                new VideoItem("http://ynethd-i.akamaihd.net/i/cdnwiz/1017/081017_meteor_rXwj2aTq_800.mp4/master.m3u8",
                        "https://images1.ynet.co.il/PicServer5/2017/10/08/8077142/807703311823487640360no.jpg"),
                new VideoItem("http://ynethd-i.akamaihd.net/i/cdnwiz/1017/arnav_fpo8g7XF_800.mp4/master.m3u8",
                        "https://images1.ynet.co.il/PicServer5/2017/10/07/8075646/80756450991498640360no.jpg")));
    }
}
