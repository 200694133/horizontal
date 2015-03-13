package com.hyn.hedis.sample;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.hyn.hedis.ObjectParser;
import com.hyn.hedis.QueueHedis;
import com.hyn.hedis.QueueHedisImpl;
import com.hyn.hedis.exception.HedisException;

import java.util.Collection;
import java.util.Random;

import hyn.com.storage.R;

public class QueueHedisActivity extends ActionBarActivity {
    TextView tv;
    QueueHedisImpl queueHedis ;
    private static final int MAX = 7;
    private static final String KEY = "test_queue";
    Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_hedis);
        tv = (TextView) this.findViewById(R.id.tv);
        queueHedis = new QueueHedisImpl(this);
    }

    public void pushHead(View view){
        int length = Math.abs(random.nextInt() % MAX) + 2;
        if(length == 0) length=1;
        String in = "";
        for(int i =0;i<length;++i){
            in = in+length;
        }
        queueHedis.pushHead(KEY, in, parser, 100000);
        updateData();
    }

    public void pushTail(View view){
        int length = Math.abs(random.nextInt() % MAX) + 2;
        if(length == 0) length=1;
        String in = "";
        for(int i =0;i<length;++i){
            in = in+length;
        }
        queueHedis.pushTail(KEY, in, parser, 100000);
        updateData();
    }

    public void trimSize50(View view){
        queueHedis.trimSizeSilence(KEY, 50);
        updateData();
    }

    public void trimCount5(View view){
        queueHedis.trimCountSilence(KEY, 5);
        updateData();
    }

    public void deleteHead(View view){
        queueHedis.deleteFirst(KEY);
        updateData();
    }

    public void deleteTail(View view){
        queueHedis.deleteTail(KEY);
        updateData();
    }

    public void updateData(){
        String out = "";
        try {
            Collection<String> data = queueHedis.getAll(KEY,parser);
            for(String s : data){
                out = out + s +"\t\t\t"+s.getBytes().length+"\n";
            }
        } catch (HedisException e) {
            e.printStackTrace();
        }
        out = out+"\nAll "+out.getBytes().length;
        tv.setText(out);
    }

    private ObjectParser<String> parser = new ObjectParser<String>() {
        @Override
        public byte[] transferToBlob(String object) {
            return object.getBytes();
        }

        @Override
        public String transferToObject(byte[] blob) throws HedisException {
            return new String(blob);
        }
    };
}
