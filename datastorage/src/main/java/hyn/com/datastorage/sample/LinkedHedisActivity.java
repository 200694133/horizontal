package hyn.com.datastorage.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import hyn.com.datastorage.db.DataStorageManager;
import hyn.com.datastorage.db.OrderStructureDataStorage;
import hyn.com.datastorage.R;
import hyn.com.datastorage.exception.ParseFailedException;
import hyn.com.datastorage.parser.StringParser;
import hyn.com.lib.TimeUtils;
import hyn.com.lib.TwoTuple;

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

    private OrderStructureDataStorage<String> leftLruHedisImpl;
    private OrderStructureDataStorage<String> rightLruHedisImpl;

    private final String TAG_LEFT = "left";
    private final String TAG_RIGHT = "right";
    private int event = 0;
    DataStorageManager hedisManager;
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
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                ThreeTuple<String,String,String> item = myAdapter1.getItemData(position);
//                try {
//                    ThreeTuple<String, String, String>  value = lruHedisImpl.get(item.secondValue);
//                    Log.d("Fast Get value "+value.thirdValue);
//                } catch (HedisException e) {
//                    e.printStackTrace();
//                }
            }
        });
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                ThreeTuple<String,String,String> item = myAdapter2.getItemData(position);
//                try {
//                    ThreeTuple<String, String, String>  value = baseHedisImpl.get(item.secondValue);
//                    Log.d("Normal Get value "+value.thirdValue);
//                } catch (HedisException e) {
//                    e.printStackTrace();
//                }
            }
        });

        hedisManager = DataStorageManager.getInstance(this);

        leftLruHedisImpl = hedisManager.getOrderStorage(this, TAG_LEFT, new StringParser(), OrderStructureDataStorage.OrderPolicy.LRU);
        rightLruHedisImpl = hedisManager.getOrderStorage(this, TAG_RIGHT, new StringParser(), OrderStructureDataStorage.OrderPolicy.LRU);
    }

//    SetHedis.OnEntryRemovedListener<String> listener = new SetHedis.OnEntryRemovedListener<String>(){
//        final List<String> removed = new LinkedList<>();
//        @Override
//        public void onEntryRemoved(String tag, String key, String oldValue) {
//            String item = key + "\t"+oldValue;
//            Log.d("Remove item "+item);
//        }
//    };

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

        final List<TwoTuple<String,String>> prev = new ArrayList<>();
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
            for (TwoTuple<String, String> twoTuple : load) {
                leftLruHedisImpl.put(twoTuple.firstValue, twoTuple.secondValue, t1 + 50 * 1000L);
            }
        } catch (ParseFailedException e) {
            e.printStackTrace();
        }
        long t2 = TimeUtils.getCurrentWallClockTime();

        final List<TwoTuple<String,String>> prev1 = new ArrayList<>();

        long t3 = TimeUtils.getCurrentWallClockTime();
        try {
            for(TwoTuple<String,String> twoTuple : load) {
                rightLruHedisImpl.put(twoTuple.firstValue, twoTuple.secondValue, t3 + 50 * 1000L);
            }
        } catch (ParseFailedException e) {
            e.printStackTrace();
        }
        long t4 = TimeUtils.getCurrentWallClockTime();

        tipView.setText("Left Cost "+(t4-t3)+"\tRight Cost "+(t2-t1)+"\nLeft Size : "+leftLruHedisImpl.size()+"\t,\tCount: "+leftLruHedisImpl.count()
        +"\nRight Size: "+rightLruHedisImpl.size()+"\t,\tCount: "+rightLruHedisImpl.count());

        myAdapter1.setData(prev);
        myAdapter1.notifyDataSetChanged();
        myAdapter2.setData(prev1);
        myAdapter2.notifyDataSetChanged();
    }

    public void getAll(View view){
        List<TwoTuple<String, String>> out = new ArrayList<>();
        long t1 = TimeUtils.getCurrentWallClockTime();
        try {
            out = leftLruHedisImpl.getAll();
        } catch (ParseFailedException e) {
            e.printStackTrace();
        }
        long t2 = TimeUtils.getCurrentWallClockTime();

        List<TwoTuple<String, String>> out1 = new ArrayList<>();
        long t3 = TimeUtils.getCurrentWallClockTime();
        try {
            out1 = rightLruHedisImpl.getAll();
        } catch (ParseFailedException e) {
            e.printStackTrace();
        }
        long t4 = TimeUtils.getCurrentWallClockTime();
        tipView.setText("Left Cost "+(t4-t3)+"\tRight Cost "+(t2-t1)+"\nLeft Size : "+leftLruHedisImpl.size()+"\t,\tCount: "+leftLruHedisImpl.count()
                +"\nRight Size: "+rightLruHedisImpl.size()+"\t,\tCount: "+rightLruHedisImpl.count());

        myAdapter1.setData(out);
        myAdapter1.notifyDataSetChanged();
        myAdapter2.setData(out1);
        myAdapter2.notifyDataSetChanged();
    }

    public void trimSize(View view){
        if(trimSizeEdit.getText() == null || trimSizeEdit.getText().toString() == null) return ;
        String content = trimSizeEdit.getText().toString();
        if(TextUtils.isEmpty(content)) return ;
        int size = Integer.parseInt(content);
        long t1 = TimeUtils.getCurrentWallClockTime();
        leftLruHedisImpl.trimToSize(size);
        long t2 = TimeUtils.getCurrentWallClockTime();

        long t3 = TimeUtils.getCurrentWallClockTime();
        rightLruHedisImpl.trimToSize(size);
        long t4 = TimeUtils.getCurrentWallClockTime();

        tipView.setText("Left Cost "+(t4-t3)+"\tRight Cost "+(t2-t1)+"\nLeft Size : "+leftLruHedisImpl.size()+"\t,\tCount: "+leftLruHedisImpl.count()
                +"\nRight Size: "+rightLruHedisImpl.size()+"\t,\tCount: "+rightLruHedisImpl.count());
    }

    public void trimCount(View view){
        if(trimCountEdit.getText() == null || trimCountEdit.getText().toString() == null) return ;
        String content = trimCountEdit.getText().toString();
        if(TextUtils.isEmpty(content)) return ;
        String prev = null;
        int size = Integer.parseInt(content);

        long t1 = TimeUtils.getCurrentWallClockTime();
        leftLruHedisImpl.trimToCount(size);
        long t2 = TimeUtils.getCurrentWallClockTime();

        long t3 = TimeUtils.getCurrentWallClockTime();
        rightLruHedisImpl.trimToCount(size);
        long t4 = TimeUtils.getCurrentWallClockTime();

        tipView.setText("Left Cost "+(t4-t3)+"\tRight Cost "+(t2-t1)+"\nLeft Size : "+leftLruHedisImpl.size()+"\t,\tCount: "+leftLruHedisImpl.count()
                +"\nRight Size: "+rightLruHedisImpl.size()+"\t,\tCount: "+rightLruHedisImpl.count());
    }



    public void getPage(View view){
//        List<String> all = new ArrayList<>();
//        Random random = new Random();
//        int count = Math.abs(random.nextInt() % 100)+10;
//        int pageOffset = Math.abs(random.nextInt()%20);
//        Log.d("getPage page "+pageOffset+"\t count "+count);
//        //getPage(int pageIndex, int pageCount, OrderPolicy orderPolicy)
//        long t1 = TimeUtils.getCurrentWallClockTime();
//        try {
//            List<ThreeTuple<String, String, String>> res = lruHedisImpl.getPage(pageOffset, count);
//            if(null != res){
//                for(ThreeTuple<String, String, String> tuple : res){
//                    if(null == tuple) continue;
//                    all.add(tuple.thirdValue);
//                }
//            }
//        } catch (HedisException e) {
//            e.printStackTrace();
//        }
//        long t2 = TimeUtils.getCurrentWallClockTime();
//        myAdapter1.setData(all);
//        myAdapter1.notifyDataSetChanged();
//        tipView.setText("Cost "+(t2-t1)+"\tCount:"+ lruHedisImpl.count()+"\tSize:"+ lruHedisImpl.size());
    }

    public void getSize(View view){
        String all = null;
        long t1 = TimeUtils.getCurrentWallClockTime();
        all = ""+ leftLruHedisImpl.size();
        long t2 = TimeUtils.getCurrentWallClockTime();


        long t3 = TimeUtils.getCurrentWallClockTime();
        all = ""+ rightLruHedisImpl.size();
        long t4 = TimeUtils.getCurrentWallClockTime();


        tipView.setText("Left Cost "+(t4-t3)+"\tRight Cost "+(t2-t1)+"\nLeft Size : "+leftLruHedisImpl.size()+"\t,\tCount: "+leftLruHedisImpl.count()
                +"\nRight Size: "+rightLruHedisImpl.size()+"\t,\tCount: "+rightLruHedisImpl.count());
    }

    public void getCount(View view){
        String all = null;
        long t1 = TimeUtils.getCurrentWallClockTime();
        all = ""+ leftLruHedisImpl.count();
        long t2 = TimeUtils.getCurrentWallClockTime();


        long t3 = TimeUtils.getCurrentWallClockTime();
        all = ""+ rightLruHedisImpl.count();
        long t4 = TimeUtils.getCurrentWallClockTime();


        tipView.setText("Left Cost "+(t4-t3)+"\tRight Cost "+(t2-t1)+"\nLeft Size : "+leftLruHedisImpl.size()+"\t,\tCount: "+leftLruHedisImpl.count()
                +"\nRight Size: "+rightLruHedisImpl.size()+"\t,\tCount: "+rightLruHedisImpl.count());
    }

    public void clear(View view) {
        String all = null;
        long t1 = TimeUtils.getCurrentWallClockTime();
        leftLruHedisImpl.clear();
        long t2 = TimeUtils.getCurrentWallClockTime();


        long t3 = TimeUtils.getCurrentWallClockTime();
        rightLruHedisImpl.clear();
        long t4 = TimeUtils.getCurrentWallClockTime();


        tipView.setText("Left Cost "+(t4-t3)+"\tRight Cost "+(t2-t1)+"\nLeft Size : "+leftLruHedisImpl.size()+"\t,\tCount: "+leftLruHedisImpl.count()
                +"\nRight Size: "+rightLruHedisImpl.size()+"\t,\tCount: "+rightLruHedisImpl.count());
    }























    private class MyAdapter extends BaseAdapter {
        final List<TwoTuple<String, String>> data = new ArrayList<>();
        public MyAdapter(){

        }
        public void setData(TwoTuple<String, String> ... data){
            this.data.clear();
            if(null != data && data.length > 0) {
                for(TwoTuple<String, String> s:data)
                    this.data.add(s);
            }
        }

        public void setData(List<TwoTuple<String, String>> data){
            this.data.clear();
            if(null != data) this.data.addAll(data);
        }

        public TwoTuple<String, String> getItemData(int position){
            if(data.size() <= position) return null;
            return data.get(position);
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
            TwoTuple<String, String> item = data.get(position);
            if(null != data.get(position)) tv.setText(""+position+"\t"+item.firstValue+"\t"+item.secondValue);
            tv.setSingleLine();
            return tv;
        }
    }
}
