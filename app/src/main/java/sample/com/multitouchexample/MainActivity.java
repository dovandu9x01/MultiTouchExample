package sample.com.multitouchexample;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import sample.com.multitouchexample.view.CustomTouchView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.layout_content);
        CustomTouchView customTouchView1 = new CustomTouchView(this, BitmapFactory.decodeResource(getResources(), R.mipmap.image_1));
        CustomTouchView customTouchView2 = new CustomTouchView(this, BitmapFactory.decodeResource(getResources(), R.mipmap.image_2));
        customTouchView1.setBringToFront(true);
        customTouchView2.setBringToFront(true);

        frameLayout.addView(customTouchView1);
        frameLayout.addView(customTouchView2);
    }
}
