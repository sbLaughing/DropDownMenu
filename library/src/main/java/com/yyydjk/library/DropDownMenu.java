package com.yyydjk.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


/**
 * Created by dongjunkun on 2015/6/17.
 */
public class DropDownMenu extends LinearLayout {

    private int menuBackgroundColor;
    private int underlineColor;
    private int underlineHeight;
    //顶部菜单布局
    private LinearLayout tabMenuView;
    //底部容器，包含popupMenuViews，maskView
    private FrameLayout containerView;
    //弹出菜单父布局
    private FrameLayout popupMenuViews;
    //遮罩半透明View，点击可关闭DropDownMenu
    private View maskView;
    //tabMenuView里面选中的tab位置，-1表示未选中
    private int current_tab_position = -1;

    //分割线颜色
    private int dividerColor = 0xffcccccc;
    //tab选中颜色
    private int textSelectedColor = 0xff890c85;
    //tab未选中颜色
    private int textUnselectedColor = 0xff111111;
    //遮罩颜色
    private int maskColor = 0x88888888;
    //tab字体大小
    private int menuTextSize = 14;

    //tab选中图标
    private int menuSelectedIcon;
    //tab未选中图标
    private int menuUnselectedIcon;

    private int mDividerWidth = 1;
    private int mDividerMarginVertial;

    private float menuHeighPercent = 0.5f;
    private FrameLayout.LayoutParams tabLayoutParams;
    private  LinearLayout.LayoutParams dividerLayoutParams;

    private View childView;


    public DropDownMenu(Context context) {
        super(context, null);
    }

    public DropDownMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DropDownMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);

        //为DropDownMenu添加自定义属性
        menuBackgroundColor = 0xffffffff;
        underlineColor = 0xffcccccc;
        underlineHeight = dpTpPx(1);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DropDownMenu);
        underlineColor = a.getColor(R.styleable.DropDownMenu_ddunderlineColor, underlineColor);
        dividerColor = a.getColor(R.styleable.DropDownMenu_dddividerColor, dividerColor);
        textSelectedColor = a.getColor(R.styleable.DropDownMenu_ddtextSelectedColor, textSelectedColor);
        textUnselectedColor = a.getColor(R.styleable.DropDownMenu_ddtextUnselectedColor, textUnselectedColor);
        menuBackgroundColor = a.getColor(R.styleable.DropDownMenu_ddmenuBackgroundColor, menuBackgroundColor);
        maskColor = a.getColor(R.styleable.DropDownMenu_ddmaskColor, maskColor);
        menuTextSize = a.getDimensionPixelSize(R.styleable.DropDownMenu_ddmenuTextSize, menuTextSize);
        menuSelectedIcon = a.getResourceId(R.styleable.DropDownMenu_ddmenuSelectedIcon, menuSelectedIcon);
        menuUnselectedIcon = a.getResourceId(R.styleable.DropDownMenu_ddmenuUnselectedIcon, menuUnselectedIcon);
        menuHeighPercent = a.getFloat(R.styleable.DropDownMenu_ddmenuMenuHeightPercent,menuHeighPercent);
        mDividerWidth = a.getDimensionPixelOffset(R.styleable.DropDownMenu_ddDividerWidth,mDividerWidth);
        mDividerMarginVertial = a.getDimensionPixelOffset(R.styleable.DropDownMenu_ddDividerMarginVertial,dpTpPx(12));
        underlineHeight = a.getDimensionPixelOffset(R.styleable.DropDownMenu_ddunderlineHeight,underlineHeight);
        a.recycle();


        tabLayoutParams= new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tabLayoutParams.gravity = Gravity.CENTER;
        dividerLayoutParams = new LinearLayout.LayoutParams(mDividerWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        dividerLayoutParams.setMargins(0,mDividerMarginVertial,0,mDividerMarginVertial);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (getChildCount()!=1){
            throw new IllegalStateException("DropDownMenu can host only one direct child. "+getChildCount()+" child now.");
        }

        childView = getChildAt(0);
        removeView(childView);

        //初始化tabMenuView并添加到tabMenuView
        tabMenuView = new LinearLayout(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tabMenuView.setOrientation(HORIZONTAL);
        tabMenuView.setBackgroundColor(menuBackgroundColor);
        tabMenuView.setLayoutParams(params);
        addView(tabMenuView);

        //为tabMenuView添加下划线
        View underLine = new View(getContext());
        underLine.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, underlineHeight));
        underLine.setBackgroundColor(underlineColor);
        addView(underLine);

        //初始化containerView并将其添加到DropDownMenu
        containerView = new FrameLayout(getContext());
        containerView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        addView(containerView);
        containerView.addView(childView);
    }

    /**
     * 初始化DropDownMenu
     *
     * @param tabTexts
     * @param popupViews
     * @param contentView
     */
    public void setDropDownMenu(@NonNull List<String> tabTexts, @NonNull List<View> popupViews, @NonNull View contentView) {

        if (tabTexts.size() != popupViews.size()) {
            throw new IllegalArgumentException("params not match, tabTexts.size() should be equal popupViews.size()");
        }

        clearMenu();

        for (int i = 0; i < tabTexts.size(); i++) {
            addTab(tabTexts, i);
        }
//        containerView.addView(contentView, 0);

        maskView = new View(getContext());
        maskView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        maskView.setBackgroundColor(maskColor);
        maskView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
            }
        });
        containerView.addView(maskView);
        maskView.setVisibility(GONE);

        popupMenuViews = new FrameLayout(getContext());
        popupMenuViews.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (DeviceUtils.getScreenSize(getContext()).y*menuHeighPercent)));
        popupMenuViews.setVisibility(GONE);
        containerView.addView(popupMenuViews);

        for (int i = 0; i < popupViews.size(); i++) {
            popupViews.get(i).setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            popupMenuViews.addView(popupViews.get(i), i);
        }

    }

    private void addTab(@NonNull List<String> tabTexts, int i) {
        final TextView tab = new TextView(getContext());
        final FrameLayout tabContainer = new FrameLayout(getContext());

        tab.setSingleLine();
        tab.setEllipsize(TextUtils.TruncateAt.END);
        tab.setGravity(Gravity.CENTER);
        tab.setTextSize(TypedValue.COMPLEX_UNIT_PX,menuTextSize);
        tabContainer.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        tab.setTextColor(textUnselectedColor);
        tab.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(menuUnselectedIcon), null);
        tab.setText(tabTexts.get(i));
        tab.setPadding(dpTpPx(5), dpTpPx(12), dpTpPx(5), dpTpPx(12));
        //添加点击事件
        tabContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchMenu(v);
            }
        });
        tabContainer.addView(tab,tabLayoutParams);
        tabMenuView.addView(tabContainer);
        //添加分割线
        if (i < tabTexts.size() - 1) {
            View view = new View(getContext());
            view.setLayoutParams(dividerLayoutParams);
            view.setBackgroundColor(dividerColor);
            tabMenuView.addView(view);
        }
    }

    /**
     * 改变tab文字
     *
     * @param text
     */
    public void setTabText(String text) {
        if (current_tab_position != -1) {
            getTextViewByPosition(current_tab_position).setText(text);
        }
    }

    public void setTabClickable(boolean clickable) {
        for (int i = 0; i < tabMenuView.getChildCount(); i = i + 2) {
            tabMenuView.getChildAt(i).setClickable(clickable);
        }
    }


    /**
     * 清理菜单
     */
    public void clearMenu(){
        closeMenu();
        if (tabMenuView!=null) tabMenuView.removeAllViews();
        if (popupMenuViews!=null) popupMenuViews.removeAllViews();
        if (containerView!=null) containerView.removeAllViews();
        if (containerView!=null) containerView.addView(childView);
    }

    /**
     * 关闭菜单
     */
    public void closeMenu() {
        if (current_tab_position != -1) {
            TextView tv = getTextViewByPosition(current_tab_position);
            tv.setTextColor(textUnselectedColor);
            tv.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    getResources().getDrawable(menuUnselectedIcon), null);
            popupMenuViews.setVisibility(View.GONE);
            popupMenuViews.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_out));
            maskView.setVisibility(GONE);
            maskView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
            current_tab_position = -1;
        }

    }

    /**
     * DropDownMenu是否处于可见状态
     *
     * @return
     */
    public boolean isShowing() {
        return current_tab_position != -1;
    }

    /**
     * 切换菜单
     *
     * @param target
     */
    private void switchMenu(View target) {
        System.out.println(current_tab_position);
        for (int i = 0; i < tabMenuView.getChildCount(); i = i + 2) {
            if (target == tabMenuView.getChildAt(i)) {
                if (current_tab_position == i) {
                    closeMenu();
                } else {
                    if (current_tab_position == -1) {
                        popupMenuViews.setVisibility(View.VISIBLE);
                        popupMenuViews.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_in));
                        maskView.setVisibility(VISIBLE);
                        maskView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_in));
                        popupMenuViews.getChildAt(i / 2).setVisibility(View.VISIBLE);
                    } else {
                        popupMenuViews.getChildAt(i / 2).setVisibility(View.VISIBLE);
                    }
                    current_tab_position = i;
                    TextView tv = getTextViewByPosition(i);
                    tv.setTextColor(textSelectedColor);
                    tv.setCompoundDrawablesWithIntrinsicBounds(null, null,
                            getResources().getDrawable(menuSelectedIcon), null);
                }
            } else {
                TextView tv = getTextViewByPosition(i);
                tv.setTextColor(textUnselectedColor);
                tv.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        getResources().getDrawable(menuUnselectedIcon), null);
                popupMenuViews.getChildAt(i / 2).setVisibility(View.GONE);
            }
        }
    }

    public int dpTpPx(float value) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, dm) + 0.5);
    }

    private TextView getTextViewByPosition(int position){
        FrameLayout frameLayout = (FrameLayout) tabMenuView.getChildAt(position);
        return ((TextView) frameLayout.getChildAt(0));
    }
}
