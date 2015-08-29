package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.ui.main.page_transformers.DepthPageTransformer;

public class ExceptionTypeChooserActivity extends AppCompatActivity {
    private ExceptionTypePagerAdapter pagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception_type_chooser);

        pagerAdapter = new ExceptionTypePagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.exception_type_pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageTransformer(true, new DepthPageTransformer());
    }
}
