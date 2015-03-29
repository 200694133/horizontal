package com.hyn.hedis.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hyn.hedis.BaseSetHedisImpl;
import com.hyn.hedis.LinkedHashMapHedisDataBaseHelper;
import com.hyn.hedis.LruSetHedis;
import com.hyn.hedis.OrderPolicy;
import com.hyn.hedis.SetHedis;
import com.hyn.hedis.StringParser;
import com.hyn.hedis.exception.HedisException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import hyn.com.lib.ThreeTuple;
import hyn.com.lib.TimeUtils;
import hyn.com.lib.TwoTuple;
import hyn.com.lib.android.Log;
import hyn.com.storage.R;

/**
 * Created by hanyanan on 2015/3/19.
 */
public class LinkedHedisActivity extends ActionBarActivity {
    private ListView listView1, listView2;
    private EditText putEdit;
    private EditText trimSizeEdit;
    private EditText trimCountEdit;
    private TextView tipView;
    private MyAdapter myAdapter1 = new MyAdapter();
    private MyAdapter myAdapter2 = new MyAdapter();

    private SetHedis<String> mBaseSetHedisImpl;


    private final String TAG_FAST = "fast";
    private int event = 0;
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.linked_hedis);

        putEdit = (EditText) findViewById(R.id.put_body);
        trimSizeEdit = (EditText) findViewById(R.id.trim_size);
        trimCountEdit = (EditText) findViewById(R.id.trim_count);
        tipView = (TextView) findViewById(R.id.result);

        listView1 = (ListView) findViewById(R.id.listview1);
        listView2 = (ListView) findViewById(R.id.listview2);
        listView1.setAdapter(myAdapter1);
        listView2.setAdapter(myAdapter2);

//        mBaseSetHedisImpl = new BaseSetHedisImpl<String>(this,TAG_FAST,
//                LinkedHashMapHedisDataBaseHelper.getInstance(this)
//                ,new StringParser(), OrderPolicy.Size, true);
        mBaseSetHedisImpl = new LruSetHedis<String>(
                LinkedHashMapHedisDataBaseHelper.getInstance(this)
                ,new StringParser(),TAG_FAST);
        mBaseSetHedisImpl.setRemoveListener(listener);
    }

    SetHedis.OnEntryRemovedListener<String> listener = new SetHedis.OnEntryRemovedListener<String>(){
        final List<String> removed = new LinkedList<>();
        @Override
        public void onEntryRemoved(String tag, String key, String oldValue) {
            String item = key + "\t"+oldValue;
            removed.add(0, item);
            myAdapter2.setData(removed);
            myAdapter2.notifyDataSetChanged();
        }
    };
    public String getKey(){
        String key = ""+event;
        event+=1;
        return key;
    }
    public void put(View view){
        if(putEdit.getText() == null || putEdit.getText().toString() == null) return ;
        String content = putEdit.getText().toString();
        if(TextUtils.isEmpty(content)) return ;
        Random random = new Random();

        final List<String> prev = new ArrayList<>();
        final List<TwoTuple<String,String>> load = new ArrayList<>();
        for(int i =0 ;i<500;++i){
            String key = getKey();
            int count = Math.abs(random.nextInt()%8) + 2;
            String body = "";
            for(int m =0;m<count;++m){
                body = body + content;
            }
            load.add(new TwoTuple<String, String>(key, body));
        }
        long t1 = TimeUtils.getCurrentWallClockTime();
        try {
            for(TwoTuple<String,String> twoTuple : load) {
                String p = mBaseSetHedisImpl.replace(twoTuple.firstValue, twoTuple.secondValue, 30 * 1000);
                if(null != p )prev.add(p);
            }
        } catch (HedisException e) {
            e.printStackTrace();
        }
        long t2 = TimeUtils.getCurrentWallClockTime();
        myAdapter1.setData(prev);
        myAdapter1.notifyDataSetChanged();


        tipView.setText("Cost "+(t2-t1)+"\tCount:"+ mBaseSetHedisImpl.count()+"\tSize:"+ mBaseSetHedisImpl.size());
    }

    public void getAll(View view){
        long t1 = TimeUtils.getCurrentWallClockTime();
        List<ThreeTuple<String, String, String>>  all = null;
        List<String> out = new ArrayList<>();
        try {
            all = mBaseSetHedisImpl.getAll();
            if(null != all) {
                for (ThreeTuple<String, String, String> item : all) {
                    if(null == item) continue;
                    out.add(item.thirdValue);
                }
            }
        } catch (HedisException e) {
            e.printStackTrace();
        }
        long t2 = TimeUtils.getCurrentWallClockTime();
        myAdapter1.setData(out);
        myAdapter1.notifyDataSetChanged();

        tipView.setText("Cost "+(t2-t1)+"\tCount:"+ mBaseSetHedisImpl.count()+"\tSize:"+ mBaseSetHedisImpl.size());
    }

    public void trimSize(View view){
        if(trimSizeEdit.getText() == null || trimSizeEdit.getText().toString() == null) return ;
        String content = trimSizeEdit.getText().toString();
        if(TextUtils.isEmpty(content)) return ;
        String key = getKey();
        String prev = null;
        int size = Integer.parseInt(content);
        long t1 = TimeUtils.getCurrentWallClockTime();
        mBaseSetHedisImpl.trimToSize(size);
        long t2 = TimeUtils.getCurrentWallClockTime();
        myAdapter1.setData(prev);
        myAdapter1.notifyDataSetChanged();

        tipView.setText("Cost "+(t2-t1)+"\tCount:"+ mBaseSetHedisImpl.count()+"\tSize:"+ mBaseSetHedisImpl.size());
    }

    public void trimCount(View view){
        if(trimCountEdit.getText() == null || trimCountEdit.getText().toString() == null) return ;
        String content = trimCountEdit.getText().toString();
        if(TextUtils.isEmpty(content)) return ;
        String prev = null;
        int size = Integer.parseInt(content);
        long t1 = TimeUtils.getCurrentWallClockTime();
        mBaseSetHedisImpl.trimToCount(size);
        long t2 = TimeUtils.getCurrentWallClockTime();
        myAdapter1.setData(prev);
        myAdapter1.notifyDataSetChanged();

        tipView.setText("Cost "+(t2-t1)+"\tCount:"+ mBaseSetHedisImpl.count()+"\tSize:"+ mBaseSetHedisImpl.size());
    }



    public void getPage(View view){
        List<String> all = new ArrayList<>();
        Random random = new Random();
        int count = Math.abs(random.nextInt() % 100)+10;
        int pageOffset = Math.abs(random.nextInt()%20);
        Log.d("getPage page "+pageOffset+"\t count "+count);
        //getPage(int pageIndex, int pageCount, OrderPolicy orderPolicy)
        long t1 = TimeUtils.getCurrentWallClockTime();
        try {
            List<ThreeTuple<String, String, String>> res = mBaseSetHedisImpl.getPage(pageOffset, count);
            if(null != res){
                for(ThreeTuple<String, String, String> tuple : res){
                    if(null == tuple) continue;
                    all.add(tuple.thirdValue);
                }
            }
        } catch (HedisException e) {
            e.printStackTrace();
        }
        long t2 = TimeUtils.getCurrentWallClockTime();
        myAdapter1.setData(all);
        myAdapter1.notifyDataSetChanged();
        tipView.setText("Cost "+(t2-t1)+"\tCount:"+ mBaseSetHedisImpl.count()+"\tSize:"+ mBaseSetHedisImpl.size());
    }

    public void getSize(View view){
        String all = null;
        long t1 = TimeUtils.getCurrentWallClockTime();
        all = ""+ mBaseSetHedisImpl.size();
        long t2 = TimeUtils.getCurrentWallClockTime();
        myAdapter1.setData(all);
        myAdapter1.notifyDataSetChanged();

        tipView.setText("Cost "+(t2-t1)+"\tCount:"+ mBaseSetHedisImpl.count()+"\tSize:"+ mBaseSetHedisImpl.size());
    }

    public void getCount(View view){
        String all = null;
        long t1 = TimeUtils.getCurrentWallClockTime();
        all = ""+ mBaseSetHedisImpl.count();
        long t2 = TimeUtils.getCurrentWallClockTime();
        myAdapter1.setData(all);
        myAdapter1.notifyDataSetChanged();

        tipView.setText("Cost "+(t2-t1)+"\tCount:"+ mBaseSetHedisImpl.count()+"\tSize:"+ mBaseSetHedisImpl.size());
    }

























    private class MyAdapter extends BaseAdapter {
        final List<String> data = new ArrayList<>();
        public MyAdapter(){

        }
        public void setData(String ... data){
            this.data.clear();
            if(null != data && data.length > 0) {
                for(String s:data)
                    this.data.add(s);
            }
        }
        public void setData(List<String> data){
            this.data.clear();
            if(null != data) this.data.addAll(data);
        }
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = new TextView(LinkedHedisActivity.this);
            tv.setMaxWidth(300);
            if(null != data.get(position)) tv.setText(""+position+"\t"+data.get(position));
            tv.setSingleLine();
            return tv;
        }
    }
}
