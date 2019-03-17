package net.keyanjie.android_style_transfer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.nie.ngallerylibrary.GalleryViewPager;
import com.nie.ngallerylibrary.ScalePageTransformer;
import com.nie.ngallerylibrary.adater.MyPageradapter;

import java.util.ArrayList;
import java.util.List;

public class ChooseStyleActivity extends AppCompatActivity {

    private GalleryViewPager mViewPager;
    private SimpleAdapter mPagerAdapter;
    private View next;
    private View back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_style);

        back = findViewById(R.id.back2);
        next = findViewById(R.id.next2);

        Intent intent = getIntent();
        final String strUri = intent.getStringExtra("uri");

        mViewPager = (GalleryViewPager) findViewById(R.id.viewpager);//找到这个控件
        mViewPager.setPageTransformer(true, new ScalePageTransformer());//设置PageTransformer,本库只有一个ScalePageTransformer,如果这个ScalePageTransformer满足不了您的需求,您可以自己写一个PageTransformer
        findViewById(R.id.root).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mViewPager.dispatchTouchEvent(event);
            }
        });//找到这个父控件设置他的listener

        mPagerAdapter = new SimpleAdapter(this);//初始化adapter
        mViewPager.setAdapter(mPagerAdapter);//设置adapter

        initData();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = mViewPager.getCurrentItem();
                if (pos == 9) {
                    Toast.makeText(
                            ChooseStyleActivity.this,
                            "这是我画的，不可以选这个！",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent next_intent = new Intent(getApplicationContext(), CompleteActivity.class);
                    next_intent.putExtra("uri", strUri);
                    next_intent.putExtra("style", pos);
                    startActivity(next_intent);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void initData() {
        List<Integer> list = new ArrayList<>();
        list.add(R.drawable.style_cubist);
        list.add(R.drawable.style_feathers);
        list.add(R.drawable.style_ink);
        list.add(R.drawable.style_la_muse);
        list.add(R.drawable.style_mosaic);
        list.add(R.drawable.style_scream);
        list.add(R.drawable.style_denoised_starry);
        list.add(R.drawable.style_udnie);
        list.add(R.drawable.style_wave);
        list.add(R.drawable.keyanjie);


        //设置OffscreenPageLimit
        mViewPager.setOffscreenPageLimit(Math.min(list.size(), 5));
        mPagerAdapter.addAll(list);
    }

    public class SimpleAdapter extends MyPageradapter {

        private final List<Integer> mList;
        private final Context mContext;

        public SimpleAdapter(Context context) {
            mList = new ArrayList<>();
            mContext = context;
        }

        public void addAll(List<Integer> list) {
            mList.addAll(list);
            notifyDataSetChanged();
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup container) {

            ImageView imageView = null;
            if (convertView == null) {
                imageView = new ImageView(mContext);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setTag(position);
            imageView.setImageResource(mList.get(position));

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((mViewPager.getCurrentItem()) == position) {
                        Toast.makeText(mContext, "点击的位置是:::" + position, Toast.LENGTH_SHORT).show();
                        Log.e("pos_onclick", "" + position);
                    }
                }
            });
            return imageView;
        }


        @Override
        public int getCount() {
            return mList.size();
        }
    }
}
