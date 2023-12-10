package com.beloo.chipslayoutmanager.sample.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.beloo.widget.chipslayoutmanager.BuildConfig;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import com.beloo.chipslayoutmanager.sample.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private Drawer drawer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        toolbar.setTitle(getString(R.string.app_name_and_version, BuildConfig.VERSION_NAME));

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, ItemsFragment.newInstance())
                    .commit();
        }

        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawer = new DrawerBuilder(this)
                .withToolbar(toolbar)
                .addDrawerItems(new PrimaryDrawerItem().withName(R.string.main).withIdentifier(1))
                .addDrawerItems(new PrimaryDrawerItem().withName(R.string.bottom_sheet).withIdentifier(2))
                .withOnDrawerItemClickListener(this::onDrawerItemClickListener)
                .build();
    }

    private boolean onDrawerItemClickListener(View view, int position, IDrawerItem drawerItem) {
        int id = (int) drawerItem.getIdentifier();
        switch (id) {
            case 1:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, ItemsFragment.newInstance())
                        .commit();
                drawer.closeDrawer();
                break;
            case 2:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, BottomSheetFragment.newInstance())
                        .commit();
                drawer.closeDrawer();
                break;
        }
        return true;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }
}
