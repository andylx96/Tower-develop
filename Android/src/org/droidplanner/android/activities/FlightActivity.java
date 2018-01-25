package org.droidplanner.android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.droidplanner.android.OpenCVCam;
import org.droidplanner.android.OpenCvDroneCam;
import org.droidplanner.android.R;
import org.droidplanner.android.dialogs.DialogMaterialFragment;
import org.droidplanner.android.fragments.FlightDataFragment;
import org.droidplanner.android.fragments.WidgetsListFragment;
import org.droidplanner.android.fragments.actionbar.ActionBarTelemFragment;
import org.droidplanner.android.utils.Utils;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class FlightActivity extends DrawerNavigationUI implements SlidingUpPanelLayout.PanelSlideListener {

    private static final String EXTRA_IS_ACTION_DRAWER_OPENED = "extra_is_action_drawer_opened";
    private static final boolean DEFAULT_IS_ACTION_DRAWER_OPENED = true;

    private FlightDataFragment flightData;
    //edits
    private Button openCvButton, openCvDroneButton;
    private static String TAG = "OpenCV";



    static {
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCVLoaded");



        } else {
            Log.i(TAG, "OpenCv Not Loaded");
        }

    }
    //

    static {
//        System.loadLibrary("native-lib");
        System.loadLibrary("MyLibs");
//        System.loadLibrary("MyFaceLibs");
    }

    @Override
    public void onDrawerClosed() {
        super.onDrawerClosed();

        if (flightData != null)
            flightData.onDrawerClosed();
    }

    @Override
    public void onDrawerOpened() {
        super.onDrawerOpened();

        if (flightData != null)
            flightData.onDrawerOpened();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);
//edits
        openCvButton = (Button) findViewById(R.id.openCvButtonMain);
        openCvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FlightActivity.this, OpenCVCam.class));

                Mat matb = new Mat();
            }
        });


        openCvDroneButton = (Button) findViewById(R.id.openCvDroneButton);
        openCvDroneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FlightActivity.this, OpenCvDroneCam.class));
            }
        });

        //




        final FragmentManager fm = getSupportFragmentManager();

        //Add the flight data fragment
        flightData = (FlightDataFragment) fm.findFragmentById(R.id.flight_data_container);
        if (flightData == null) {
            Bundle args = new Bundle();
            args.putBoolean(FlightDataFragment.EXTRA_SHOW_ACTION_DRAWER_TOGGLE, true);

            flightData = new FlightDataFragment();
            flightData.setArguments(args);
            fm.beginTransaction().add(R.id.flight_data_container, flightData).commit();
        }

        // Add the telemetry fragment
        final int actionDrawerId = getActionDrawerId();
        WidgetsListFragment widgetsListFragment = (WidgetsListFragment) fm.findFragmentById(actionDrawerId);
        if (widgetsListFragment == null) {
            widgetsListFragment = new WidgetsListFragment();
            fm.beginTransaction()
                    .add(actionDrawerId, widgetsListFragment)
                    .commit();
        }

        boolean isActionDrawerOpened = DEFAULT_IS_ACTION_DRAWER_OPENED;
        if (savedInstanceState != null) {
            isActionDrawerOpened = savedInstanceState.getBoolean(EXTRA_IS_ACTION_DRAWER_OPENED, isActionDrawerOpened);
        }

        if (isActionDrawerOpened)
            openActionDrawer();
    }

    @Override
    protected void onToolbarLayoutChange(int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (flightData != null)
            flightData.updateActionbarShadow(bottom);
    }

    @Override
    protected void addToolbarFragment() {
        final int toolbarId = getToolbarId();
        final FragmentManager fm = getSupportFragmentManager();
        Fragment actionBarTelem = fm.findFragmentById(toolbarId);
        if (actionBarTelem == null) {
            actionBarTelem = new ActionBarTelemFragment();
            fm.beginTransaction().add(toolbarId, actionBarTelem).commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_IS_ACTION_DRAWER_OPENED, isActionDrawerOpened());
    }

    @Override
    public void onStart() {
        super.onStart();

        final Context context = getApplicationContext();
        //Show the changelog if this is the first time the app is launched since update/install
        if (Utils.getAppVersionCode(context) > mAppPrefs.getSavedAppVersionCode()) {
            DialogMaterialFragment changelog = new DialogMaterialFragment();
            changelog.show(getSupportFragmentManager(), "Changelog Dialog");

            mAppPrefs.updateSavedAppVersionCode(context);
        }
    }

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_toolbar;
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return R.id.navigation_flight_data;
    }

    @Override
    protected boolean enableMissionMenus() {
        return true;
    }

    @Override
    public void onPanelSlide(View view, float v) {
        final int bottomMargin = (int) getResources().getDimension(R.dimen.action_drawer_margin_bottom);

        //Update the bottom margin for the action drawer
        final View flightActionBar = ((ViewGroup) view).getChildAt(0);
        final int[] viewLocs = new int[2];
        flightActionBar.getLocationInWindow(viewLocs);
        updateActionDrawerBottomMargin(viewLocs[0] + flightActionBar.getWidth(), Math.max((int) (view.getHeight() * v), bottomMargin));
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        switch (newState) {
            case COLLAPSED:
            case HIDDEN:
                resetActionDrawerBottomMargin();
                break;

            case EXPANDED:
                //Update the bottom margin for the action drawer
                ViewGroup slidingPanel = (ViewGroup) ((ViewGroup) panel).getChildAt(1);
                final View flightActionBar = slidingPanel.getChildAt(0);
                final int[] viewLocs = new int[2];
                flightActionBar.getLocationInWindow(viewLocs);
                updateActionDrawerBottomMargin(viewLocs[0] + flightActionBar.getWidth(), slidingPanel.getHeight());
                break;
        }
    }

    private void updateActionDrawerBottomMargin(int rightEdge, int bottomMargin) {
        final ViewGroup actionDrawerParent = (ViewGroup) getActionDrawer();
        final View actionDrawer = ((ViewGroup) actionDrawerParent.getChildAt(1)).getChildAt(0);

        final int[] actionDrawerLocs = new int[2];
        actionDrawer.getLocationInWindow(actionDrawerLocs);

        if (actionDrawerLocs[0] <= rightEdge) {
            updateActionDrawerBottomMargin(bottomMargin);
        }
    }

    private int getActionDrawerBottomMargin() {
        final ViewGroup actionDrawerParent = (ViewGroup) getActionDrawer();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) actionDrawerParent.getLayoutParams();
        return lp.bottomMargin;
    }

    private void updateActionDrawerBottomMargin(int newBottomMargin) {
        final ViewGroup actionDrawerParent = (ViewGroup) getActionDrawer();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) actionDrawerParent.getLayoutParams();
        lp.bottomMargin = newBottomMargin;
        actionDrawerParent.requestLayout();
    }

    private void resetActionDrawerBottomMargin() {
        updateActionDrawerBottomMargin((int) getResources().getDimension(R.dimen.action_drawer_margin_bottom));
    }
}
