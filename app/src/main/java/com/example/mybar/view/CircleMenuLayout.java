package com.example.mybar.view;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktc.ktcbar.R;
import com.ktc.ktcbar.activity.TouMingActivity;
import com.ktc.ktcbar.adapter.MyGridSmallViewAdpter;
import com.ktc.ktcbar.application.App;
import com.ktc.ktcbar.constant.Constants;
import com.ktc.ktcbar.entity.SeekBarTag;
import com.ktc.ktcbar.getappinfo.entity.AppEntity;
import com.ktc.ktcbar.getappinfo.utils.Constant;
import com.ktc.ktcbar.windowManager.KTCWindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liujian on 2019/03/19.
 */
public class CircleMenuLayout extends FrameLayout implements VerticalSeekBar.SlideChangeListener, View.OnClickListener {


    public static final int STATUS_MENU_EMPTY = 0;
    public static final int STATUS_MENU_ONE = 1;
    public static final int STATUS_MENU_TWO = 2;
    public static final int TAG_BUTTON_NONE = -1;//no button is selected
    public static final int TAG_BUTTON_ZERO = 0;
    public static final int TAG_BUTTON_ONE = 1;
    public static final int TAG_BUTTON_TWO = 2;
    public static final int TAG_BUTTON_THREE = 3;
    public static final int TAG_BUTTON_FOUR = 4;
    public static final int TAG_BUTTON_FIVE = 5;
    public static final int TAG_BUTTON_CENTER = 6;
    private static final int AUTO_DISMISS_TIME = 30 * 1000;

    private static boolean isInMenuTwo = false;
    private Context mContext;
    private OnMenuOperationListener menuOperationListener;
    private View menuView;
    private ImageView[] imageViews = new ImageView[7];
    private int[] imageIds = {R.id.iv_1, R.id.iv_2, R.id.iv_3, R.id.iv_4, R.id.iv_5, R.id.iv_6, R.id.iv_7};
    private int[] drawables_one = {R.drawable.ic_home_nor, R.drawable.ic_back_nor, R.drawable.ic_postil_nor,
            R.drawable.ic_source_nor, R.drawable.ic_tool_nor/*R.drawable.ic_course_nor*/, R.drawable.ic_halfscreen_nor, R.drawable.switch_one_selector};
    private int[] drawables_two = {R.drawable.ic_screenshots_nor, R.drawable.ic_clean_nor, R.drawable.ic_add_nor,
            R.drawable.ic_brightness_nor, R.drawable.ic_volume_nor, R.drawable.ic_lock_screen_nor, R.drawable.switch_two_selector};
    private VerticalSeekBar verticalSeekBar;
    private TextView tvProgress;
    private LinearLayout llSeekBar;
    private GridView gvAddBar;
    public int status = 1;
    public boolean isShowApps = false;
    private WindowManager mWindowManager;
    private PropertyValuesHolder translationX[] = new PropertyValuesHolder[6];
    private PropertyValuesHolder translationY[] = new PropertyValuesHolder[6];
    private ObjectAnimator objectAnimator[] = new ObjectAnimator[6];
    private ObjectAnimator scaleAnimator;
    private WindowManager.LayoutParams mParams;
    private static final int PROGRESS_GONE = 0;
    private static final int PROGRESS_LEFT = 1;
    private static final int PROGRESS_RIGHT = 2;
    private RelativeLayout.LayoutParams progressParams, menuParams, appsParams;
    private List<SeekBarTag> seekTags;
    private List<SeekBarTag> appsTags;
    public int selectedTag;
    private RelativeLayout rlMenu;

    private String TAG = "CircleMenuLayout";

    private MyGridSmallViewAdpter myGridViewAdpter;

    private List<AppEntity> AppFromLocalEntities = new ArrayList<AppEntity>();

    public CircleMenuLayout(Context context) {
        this(context, null);
    }

    public CircleMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Initial layout
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public CircleMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initView();
        mHandler.postDelayed(autoDismissRun, AUTO_DISMISS_TIME);
        showAnimation();

//        AppEntity ap2 = new AppEntity();
//        Drawable drawable = this.getResources().getDrawable(R.drawable.ic_launcher_background);
//        ap2.setImage(drawable);
//        ap2.setAppName("sdfs");
//        ap1.setAppName("xxx");
        //        AppFromLocalEntities.add(ap2);
//        AppEntity ap1 = new AppEntity();
//        Drawable drawable2 = this.getResources().getDrawable(R.drawable.ic_add_pre);
//        ap1.setImage(drawable2);
//
//        for (int i = 0; i < 5; i++){
//            AppFromLocalEntities.add(ap1);
//        }

        initData();
    }

    private int startX, startY, startRawX, startRawY, curTag;
    private boolean moveFlag, isLongFlag;
    private Handler mHandler = new Handler();
    private OnTouchListener mViewOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = (int) event.getX();
                    startY = (int) event.getY();
                    startRawX = (int) event.getRawX();
                    startRawY = (int) event.getRawY();
                    moveFlag = false;
                    isLongFlag = false;
                    curTag = (int) view.getTag();
                    if (curTag == TAG_BUTTON_CENTER) {
                        mHandler.postDelayed(longClickRun, 500);
                    }
                    changeBackgroundPress(curTag);
                    mHandler.removeCallbacks(autoDismissRun);
//                    mHandler.removeCallbacks(autoExitRun);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int rawx = (int) event.getRawX();
                    int rawy = (int) event.getRawY();
                    int length = (int) Math.sqrt((rawx - startRawX) * (rawx - startRawX) + (rawy - startRawY) * (rawy - startRawY));
                    if (length > 60) {
                        mHandler.removeCallbacks(longClickRun);
                        moveFlag = true;
                        int tag1 = (int) view.getTag();
                        move(tag1, rawx - startX, rawy - startY);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mHandler.removeCallbacks(longClickRun);
                    Log.i(TAG, "onTouch: -----status: " + status);
                    if (status == STATUS_MENU_EMPTY) {
//                        mHandler.postDelayed(autoExitRun, AUTO_DISMISS_TIME);
                    } else {
                        mHandler.postDelayed(autoDismissRun, AUTO_DISMISS_TIME);
                    }
                    if (moveFlag || isLongFlag) {
                        changeBackgroundNormal();
                        break;
                    }
                    OnButtonClick(curTag);
                    break;
            }
            return true;
        }
    };

    private Runnable longClickRun = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(longClickRun);
            if (status != STATUS_MENU_EMPTY) {
                status = STATUS_MENU_EMPTY;
                switchMenu(status);
            } else {
                if (menuOperationListener != null) {
                    menuOperationListener.onCenterLongClick(status);
                }
            }
            isLongFlag = true;
        }
    };

//    private Runnable autoExitRun = new Runnable() {
//        @Override
//        public void run() {
//            Log.i(TAG, "run: ----------autoExitRun");
//            mHandler.removeCallbacks(autoExitRun);
//            if (status == STATUS_MENU_EMPTY) {
//                if (menuOperationListener != null) {
//                    menuOperationListener.onCenterLongClick(status);
//                }
//            }
//        }
//    };

    private Runnable autoDismissRun = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(autoDismissRun);
            status = STATUS_MENU_EMPTY;
            switchMenu(status);
        }
    };

    private void changeBackgroundPress(int tag) {
        changeBackgroundNormal();
        int resId = R.drawable.ic_home_pre;
        switch (tag) {
            case TAG_BUTTON_ZERO:
                resId = status == STATUS_MENU_ONE ? R.drawable.ic_home_pre : R.drawable.ic_screenshots_pre;
                break;
            case TAG_BUTTON_ONE:
                resId = status == STATUS_MENU_ONE ? R.drawable.ic_back_pre : R.drawable.ic_clean_pre;
                break;
            case TAG_BUTTON_TWO:
                resId = status == STATUS_MENU_ONE ? R.drawable.ic_postil_pre : R.drawable.ic_add_pre;
                break;
            case TAG_BUTTON_THREE:
                resId = status == STATUS_MENU_ONE ? R.drawable.ic_source_pre : R.drawable.ic_brightness_pre;
                break;
            case TAG_BUTTON_FOUR:
                resId = status == STATUS_MENU_ONE ? R.drawable.ic_tool_pre : R.drawable.ic_volume_pre;
                break;
            case TAG_BUTTON_FIVE:
                resId = status == STATUS_MENU_ONE ? R.drawable.ic_halfscreen_pre : R.drawable.ic_lock_screen_pre;
                break;
            case TAG_BUTTON_CENTER:
                resId = status == STATUS_MENU_ONE ? R.drawable.switch_one_pre : R.drawable.switch_two_pre;
                break;
        }
        if (tag != TAG_BUTTON_CENTER) {
            imageViews[tag].setImageResource(resId);
        } else {
            if (status != STATUS_MENU_EMPTY) {
                imageViews[tag].setImageResource(resId);
            }
        }
    }

    private void changeBackgroundNormal() {
        imageViews[TAG_BUTTON_ZERO].setImageResource(status == STATUS_MENU_ONE ? R.drawable.ic_home_nor : R.drawable.ic_screenshots_nor);
        imageViews[TAG_BUTTON_ONE].setImageResource(status == STATUS_MENU_ONE ? R.drawable.ic_back_nor : R.drawable.ic_clean_nor);
        imageViews[TAG_BUTTON_TWO].setImageResource(status == STATUS_MENU_ONE ? R.drawable.ic_postil_nor : R.drawable.ic_add_nor);
        imageViews[TAG_BUTTON_THREE].setImageResource(status == STATUS_MENU_ONE ? R.drawable.ic_source_nor : R.drawable.ic_brightness_nor);
        imageViews[TAG_BUTTON_FOUR].setImageResource(status == STATUS_MENU_ONE ? R.drawable.ic_tool_nor : R.drawable.ic_volume_nor);
        imageViews[TAG_BUTTON_FIVE].setImageResource(status == STATUS_MENU_ONE ? R.drawable.ic_halfscreen_nor : R.drawable.ic_lock_screen_nor);
        if (status == STATUS_MENU_EMPTY) {
            imageViews[TAG_BUTTON_CENTER].setImageResource(R.drawable.circle_nor);
        } else if (status == STATUS_MENU_ONE) {
            imageViews[TAG_BUTTON_CENTER].setImageResource(R.drawable.switch_one_nor);
        } else if (status == STATUS_MENU_TWO) {
            imageViews[TAG_BUTTON_CENTER].setImageResource(R.drawable.switch_two_nor);
        }
    }

    private void clearSeekbar() {
        if (selectedTag != TAG_BUTTON_NONE) {
            imageViews[selectedTag].setSelected(false);
        }
        selectedTag = TAG_BUTTON_NONE;
        llSeekBar.setVisibility(GONE);
        changeBackgroundNormal();
    }

    private void clearAppsBar() {
        if (selectedTag != TAG_BUTTON_NONE) {
            imageViews[selectedTag].setSelected(false);
        }
//        gvAddBar.setVisibility(GONE);
        changeBackgroundNormal();
    }

    private void OnButtonClick(int tag) {
        if (tag == TAG_BUTTON_CENTER) {
            boolean show = false;
            if (status == STATUS_MENU_ONE) {
                status = STATUS_MENU_EMPTY;
            } else if (status == STATUS_MENU_TWO) {
                status = STATUS_MENU_ONE;
            } else {
                status = STATUS_MENU_ONE;
                show = true;
            }
            switchMenu(status);
            if (show) {
                resetButtons();
            }
        } else {
            int curx = 0;
            if (mParams != null) {
                curx = mParams.x;
            }
            if (isBtnShowSeekBar(status, tag)) {
                if (imageViews[tag].isSelected()) {
                    imageViews[tag].setSelected(false);
                    changeBackgroundNormal();
                    llSeekBar.setVisibility(GONE);
                } else {
                    changeBackgroundNormal();
                    changeBackgroundPress(tag);
                    if (tag >= 2) {
                        showProgressBar(PROGRESS_RIGHT);
                    } else {
                        showProgressBar(PROGRESS_LEFT);
                    }
                    if (selectedTag != TAG_BUTTON_NONE) {
                        imageViews[selectedTag].setSelected(false);
                    }
                    imageViews[tag].setSelected(true);
                    selectedTag = tag;
                }
            } else {
                clearSeekbar();
            }

            if (isAddApps(status, tag)) {
                if (isShowApps) {
                    isShowApps = false;
                    changeBackgroundNormal();
                    gvAddBar.setVisibility(GONE);
                } else {
                    changeBackgroundNormal();
                    changeBackgroundPress(tag);
                    showAppsBar();
                    isShowApps = true;
                }
            } else {
                clearAppsBar();
            }

            if (menuOperationListener != null) {
                int locationMode = Constants.LOCATION_LEFT;
                if (curx > 0) {
                    locationMode = Constants.LOCATION_RIGHT;
                }
                menuOperationListener.onMenuItemClick(status, tag, locationMode);
            }
        }
    }

    private void initView() {
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        menuView = mInflater.inflate(R.layout.circle_menu_layout, null);
        addView(menuView);
        rlMenu = (RelativeLayout) menuView.findViewById(R.id.rl_menu);
        llSeekBar = (LinearLayout) menuView.findViewById(R.id.ll_seek_bar);
        llSeekBar.getBackground().setAlpha(25);
        llSeekBar.setVisibility(GONE);

        gvAddBar = (GridView) menuView.findViewById(R.id.gr_apps_bar);
//        gvAddBar.setVisibility(GONE);
        verticalSeekBar = (VerticalSeekBar) menuView.findViewById(R.id.seek_bar);
        verticalSeekBar.setOnSlideChangeListener(this);
        verticalSeekBar.setMaxProgress(100);
        verticalSeekBar.setOrientation(0);
        //verticalSeekBar.setThumbSizePx(14, 7);
        verticalSeekBar.setmInnerProgressWidthPx(2);

        tvProgress = (TextView) findViewById(R.id.tv_progress);
        tvProgress.setText(verticalSeekBar.getProgress() + "");

        for (int i = 0; i < imageViews.length; i++) {
            imageViews[i] = menuView.findViewById(imageIds[i]);
            imageViews[i].setTag(i);
            imageViews[i].setOnTouchListener(mViewOnTouchListener);
        }
    }

    public void switchMenu(int state) {
//        mHandler.removeCallbacks(autoExitRun);

        clearSeekbar();
        if (state == STATUS_MENU_EMPTY) {
            for (int i = 0; i < 6; i++) {
                imageViews[i].setVisibility(GONE);
            }
            gvAddBar.setVisibility(GONE);
            isShowApps = false;
            isInMenuTwo = false;
            App.getInstance().destoryActivity(Constant.TouMingActivityName);
            imageViews[6].setImageResource(R.drawable.circle_selector);

            try {
                KTCWindowManager.showAgainCircleLayout(mContext);
            } catch (Exception e) {
                e.printStackTrace();
            }

//            mHandler.postDelayed(autoExitRun, AUTO_DISMISS_TIME);
        } else if (state == STATUS_MENU_ONE) {
            if (!isInMenuTwo){
                KTCWindowManager.resetLocation(mContext);
            }
            for (int i = 0; i < imageViews.length; i++) {
                imageViews[i].setVisibility(VISIBLE);
                imageViews[i].setImageResource(drawables_one[i]);
            }
            gvAddBar.setVisibility(GONE);
            isShowApps = false;
            isInMenuTwo = false;
            App.getInstance().destoryActivity(Constant.TouMingActivityName);
        } else if (state == STATUS_MENU_TWO) {
            isInMenuTwo = true;
            for (int i = 0; i < imageViews.length; i++) {
                imageViews[i].setVisibility(VISIBLE);
                imageViews[i].setImageResource(drawables_two[i]);
            }
        }
    }


    private void showAnimation() {
        int duration = 500;

        PropertyValuesHolder scaleValueAnimatorX = PropertyValuesHolder.ofFloat("scaleX", 0.9f, 1.0f);
        PropertyValuesHolder scaleValueAnimatorY = PropertyValuesHolder.ofFloat("scaleY", 0.9f, 1.0f);

        scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(imageViews[6], scaleValueAnimatorX, scaleValueAnimatorY);
        scaleAnimator.setDuration(500);
        scaleAnimator.setInterpolator(new AnticipateOvershootInterpolator());
        scaleAnimator.start();

        PropertyValuesHolder alphaAnimator0 = PropertyValuesHolder.ofFloat("alpha", 0, 1);

        translationX[0] = PropertyValuesHolder.ofFloat("translationX", -136);
        translationY[0] = PropertyValuesHolder.ofFloat("translationY", -68);
        objectAnimator[0] = ObjectAnimator.ofPropertyValuesHolder(imageViews[0], translationX[0], translationY[0], alphaAnimator0);
        objectAnimator[0].setStartDelay(0);
        objectAnimator[0].setDuration(duration);
        objectAnimator[0].start();

        translationX[1] = PropertyValuesHolder.ofFloat("translationX", -136);
        translationY[1] = PropertyValuesHolder.ofFloat("translationY", 68);
        objectAnimator[1] = ObjectAnimator.ofPropertyValuesHolder(imageViews[1], translationX[1], translationY[1], alphaAnimator0);
        objectAnimator[1].setStartDelay(0);
        objectAnimator[1].setDuration(duration);
        objectAnimator[1].start();

        translationX[2] = PropertyValuesHolder.ofFloat("translationX", 0);
        translationY[2] = PropertyValuesHolder.ofFloat("translationY", 136);
        objectAnimator[2] = ObjectAnimator.ofPropertyValuesHolder(imageViews[2], translationX[2], translationY[2], alphaAnimator0);
        objectAnimator[2].setStartDelay(0);
        objectAnimator[2].setDuration(duration);
        objectAnimator[2].start();

        translationX[3] = PropertyValuesHolder.ofFloat("translationX", 136);
        translationY[3] = PropertyValuesHolder.ofFloat("translationY", 68);
        objectAnimator[3] = ObjectAnimator.ofPropertyValuesHolder(imageViews[3], translationX[3], translationY[3], alphaAnimator0);
        objectAnimator[3].setStartDelay(0);
        objectAnimator[3].setDuration(duration);
        objectAnimator[3].start();

        translationX[4] = PropertyValuesHolder.ofFloat("translationX", 136);
        translationY[4] = PropertyValuesHolder.ofFloat("translationY", -68);
        objectAnimator[4] = ObjectAnimator.ofPropertyValuesHolder(imageViews[4], translationX[4], translationY[4], alphaAnimator0);
        objectAnimator[4].setStartDelay(0);
        objectAnimator[4].setDuration(duration);
        objectAnimator[4].start();

        translationX[5] = PropertyValuesHolder.ofFloat("translationX", 0);
        translationY[5] = PropertyValuesHolder.ofFloat("translationY", -136);
        objectAnimator[5] = ObjectAnimator.ofPropertyValuesHolder(imageViews[5], translationX[5], translationY[5], alphaAnimator0);
        objectAnimator[5].setStartDelay(0);
        objectAnimator[5].setDuration(duration);
        objectAnimator[5].start();
    }

    private void resetButtons() {
        int duration = 500;

        objectAnimator[0].end();
        objectAnimator[1].end();
        objectAnimator[2].end();
        objectAnimator[3].end();
        objectAnimator[4].end();
        objectAnimator[5].end();

        objectAnimator[0].setStartDelay(0);
        objectAnimator[1].setStartDelay(0);
        objectAnimator[2].setStartDelay(0);
        objectAnimator[3].setStartDelay(0);
        objectAnimator[4].setStartDelay(0);
        objectAnimator[5].setStartDelay(0);

        objectAnimator[0].setDuration(duration);
        objectAnimator[1].setDuration(duration);
        objectAnimator[2].setDuration(duration);
        objectAnimator[3].setDuration(duration);
        objectAnimator[4].setDuration(duration);
        objectAnimator[5].setDuration(duration);

        objectAnimator[0].start();
        objectAnimator[1].start();
        objectAnimator[2].start();
        objectAnimator[3].start();
        objectAnimator[4].start();

        objectAnimator[5].start();
        scaleAnimator.start();
    }

    public void changeMenu(int mode) {
        if (mode <= STATUS_MENU_TWO && mode >= STATUS_MENU_EMPTY) {
            status = mode;
            switchMenu(status);
        }
    }

    public void destroyAnimation() {
        for (int i = 0; i < translationX.length; i++) {
            imageViews[i].clearAnimation();
            objectAnimator[i].cancel();
        }
//        mHandler.removeCallbacks(autoExitRun);
        mHandler.removeCallbacks(autoDismissRun);
        scaleAnimator.cancel();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_menu_main:
                ShakeAnimation.mNeedShake = false;
                myGridViewAdpter.notifyDataSetChanged();
                initData();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
//        if (gvAddBar.getVisibility() == VISIBLE) {
//            return true;
//        }
//        if (action == MotionEvent.ACTION_OUTSIDE) {
//            Log.i(TAG, "dispatchTouchEvent: -------dispatchTouchEvent------MotionEvent.ACTION_OUTSIDE");
//            removeCircleMenu();
//            return true;
//        }
        if (action == MotionEvent.ACTION_OUTSIDE) {
            Log.i(TAG, "dispatchTouchEvent: -------dispatchTouchEvent------MotionEvent.ACTION_OUTSIDE");
            ShakeAnimation.mNeedShake = false;
            ShakeAnimation.mNeedShake = false;
            myGridViewAdpter.notifyDataSetChanged();
            initData();
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    private void removeCircleMenu() {
        KTCWindowManager.removeCircleLayout(mContext);
    }

    @Override
    public void onStart(VerticalSeekBar slideView, int progress) {

    }

    @Override
    public void onStop(VerticalSeekBar slideView, int progress) {

    }

    @Override
    public void onProgress(VerticalSeekBar slideView, int progress) {
        tvProgress.setText(progress + "");
        if (menuOperationListener != null) {
            menuOperationListener.onProgressChange(status, selectedTag, progress);
        }
    }

    /**
     * SeekBar is set according to the volume or brightness of the system
     *
     * @param progress volume or brightness
     */
    public void setSeekBarProgress(int progress) {
        Log.e("CircleMenuLayout", "KtcDisplayManager light progress " + progress);
        verticalSeekBar.setProgress(progress);
        tvProgress.setText(progress + "");
    }

    public void setParams(WindowManager.LayoutParams mParams) {
        this.mParams = mParams;
    }

    //0(0,34),1(0,102),2(68,138),3(136,102),4(136,34),5(68,0),6(68,68)
    private void move(int tag, int x, int y) {
        switch (tag) {
            case TAG_BUTTON_ZERO:
                x = x - 0;
                y = y - 68;
                break;
            case TAG_BUTTON_ONE:
                x = x - 0;
                y = y - 204;
                break;
            case TAG_BUTTON_TWO:
                x = x - 136;
                y = y - 276;
                break;
            case TAG_BUTTON_THREE:
                x = x - 276;
                y = y - 206;
                break;
            case TAG_BUTTON_FOUR:
                x = x - 276;
                y = y - 68;
                break;
            case TAG_BUTTON_FIVE:
                x = x - 136;
                y = y - 0;
                break;
            case TAG_BUTTON_CENTER:
                if (status != STATUS_MENU_EMPTY) {
                    x = x - 136;
                    y = y - 136;
                }
                break;
        }
        move(x, y);
    }

    private void move(int x, int y) {
        if (mParams != null && this != null) {
            // mParams.x = x - (3840 - getMeasuredWidth()) / 2;
            //mParams.y = y - (2160 - getMeasuredHeight()) / 2;
            mParams.x = x;
            mParams.y = y;
            //Log.e("huangjing","x:" + mParams.x + "y:" +   mParams.y);
            try {
                mWindowManager.updateViewLayout(com.ktc.ktcbar.view.CircleMenuLayout.this, mParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Set the display status of the progress bar, and adjust the width of the layout file when it
     * is displayed on the left
     *
     * @param pos 0、hide；1、show on the left；2、show on the right
     */
    private void showProgressBar(int pos) {
        if (pos == PROGRESS_LEFT) {
            progressParams = new RelativeLayout.LayoutParams(50, 300);
            progressParams.addRule(RelativeLayout.CENTER_VERTICAL);
            progressParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            llSeekBar.setLayoutParams(progressParams);
            llSeekBar.setVisibility(VISIBLE);
        } else if (pos == PROGRESS_RIGHT) {
            progressParams = new RelativeLayout.LayoutParams(50, 300);
            progressParams.addRule(RelativeLayout.CENTER_VERTICAL);
            progressParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            llSeekBar.setLayoutParams(progressParams);
            llSeekBar.setVisibility(VISIBLE);
        } else if (pos == PROGRESS_GONE) {
            llSeekBar.setVisibility(GONE);
        }
    }

    private void showAppsBar() {
        appsParams = new RelativeLayout.LayoutParams(600, 120);
//        appsParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        appsParams.topMargin = 480;
//        appsParams.addRule(RelativeLayout.BELOW,R.id.rl_menu);
        gvAddBar.setLayoutParams(appsParams);
        gvAddBar.setVisibility(VISIBLE);
    }

    public void initData() {
        Log.i(TAG, "initData: --------");
        AppFromLocalEntities = Constant.getSmallAppFromLocalEntities();
//        gridView.setSelector(new ColorDrawable(0x20000000));
        myGridViewAdpter = new MyGridSmallViewAdpter(mContext, AppFromLocalEntities);
        gvAddBar.setAdapter(myGridViewAdpter);
        gvAddBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.CUPCAKE)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (AppFromLocalEntities.get(position).getAppPackageName() != null) {
                    //通过包名，启动第三方app
                    Intent LaunchIntent = mContext.getPackageManager().getLaunchIntentForPackage(AppFromLocalEntities.get(position).getAppPackageName());
                    Log.e("zlong", "AppFromLocalEntities.get(position).getAppPackageName()=" + AppFromLocalEntities.get(position).getAppPackageName());
                    mContext.startActivity(LaunchIntent);
//                    gv_app.setVisibility(View.GONE);
                } else {
                    Log.i(TAG, "onItemClick:AppPackageName() == null ");
                    if (AppFromLocalEntities.get(position).isCanAddApp()) {
                        Intent intent = new Intent(mContext, TouMingActivity.class);
                        mContext.startActivity(intent);
                    }
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(mContext, "package not find", Toast.LENGTH_SHORT).show();
//                        }
//                    }).start();
                }
            }
        });

        gvAddBar.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                ShakeAnimation.mNeedShake = true;
                myGridViewAdpter.notifyDataSetChanged();
                return true;
            }
        });
    }


    /**
     * The default location for seekBar when called out
     *
     * @param seekTags        The location where seekBar can be called out
     * @param appsTags        The location where addBar can be called out
     * @param showLeftSeekBar Whether the seekBar needs to be displayed on the left
     *                        yes: the menu show in the middle
     */
    public void setSeekBarTags(List<SeekBarTag> seekTags, List<SeekBarTag> appsTags, boolean showLeftSeekBar) {
        this.seekTags = seekTags;
        this.appsTags = appsTags;
        if (showLeftSeekBar) {
            menuParams = new RelativeLayout.LayoutParams(550, 550);
            menuParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            rlMenu.setLayoutParams(menuParams);
        } else {
            menuParams = new RelativeLayout.LayoutParams(550, 550);
            menuParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            rlMenu.setLayoutParams(menuParams);
        }
    }

    /**
     * Determine whether the currently clicked button pops up seekBar
     *
     * @param status
     * @param tag
     * @return
     */
    private boolean isBtnShowSeekBar(int status, int tag) {
        if (seekTags != null && seekTags.size() > 0) {
            return seekTags.contains(new SeekBarTag(status, tag));
        }
        return false;
    }

    /**
     * Determine whether the currently clicked button pops up apps
     *
     * @param status
     * @param tag
     * @return
     */
    private boolean isAddApps(int status, int tag) {
        if (appsTags != null && appsTags.size() > 0) {
            return appsTags.contains(new SeekBarTag(status, tag));
        }
        return false;
    }

    /**
     * the callback interface for the click event
     *
     * @param menuOperationListener
     */
    public void setMenuOperationListener(OnMenuOperationListener menuOperationListener) {
        this.menuOperationListener = menuOperationListener;
    }

    public interface OnMenuOperationListener {

        /**
         * the click event for the buttons
         *
         * @param status Current menu status  0: menu hidden; 1: Menu 1; 2. Menu 2;
         * @param tag    buttons  6、center button； 0~5、side buttons；
         */
        void onMenuItemClick(int status, int tag, int locationMode);

        /**
         * @param status the status before long click the center button，0: menu hidden; 1: Menu 1;
         *               2. Menu 2;
         */
        void onCenterLongClick(int status);

        /**
         * Returns the current sliding progress of the seekBar
         *
         * @param status   which page
         * @param tag      which button
         * @param progress current sliding progress of the seekBar
         */
        void onProgressChange(int status, int tag, int progress);
    }
}
