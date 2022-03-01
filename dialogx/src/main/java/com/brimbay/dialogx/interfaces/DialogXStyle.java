package com.brimbay.dialogx.interfaces;

import android.content.Context;

public interface DialogXStyle {
    int styleVer = 3;
    int BUTTON_OK = 1;
    int BUTTON_CANCEL = 2;
    int BUTTON_OTHER = 3;
    int SPACE = 4;
    int SPLIT = 5;

    int layout(boolean var1);

    int enterAnimResId();

    int exitAnimResId();

    int[] verticalButtonOrder();

    int[] horizontalButtonOrder();

    int splitWidthPx();

    int splitColorRes(boolean var1);

    DialogXStyle.BlurBackgroundSetting messageDialogBlurSettings();

    DialogXStyle.HorizontalButtonRes overrideHorizontalButtonRes();

    DialogXStyle.VerticalButtonRes overrideVerticalButtonRes();

    DialogXStyle.WaitTipRes overrideWaitTipRes();

    DialogXStyle.BottomDialogRes overrideBottomDialogRes();

    DialogXStyle.PopTipSettings popTipSettings();

    public interface PopTipSettings {
        int layout(boolean var1);

        DialogXStyle.PopTipSettings.ALIGN align();

        int enterAnimResId(boolean var1);

        int exitAnimResId(boolean var1);

        public static enum ALIGN {
            CENTER,
            TOP,
            BOTTOM,
            TOP_INSIDE,
            BOTTOM_INSIDE;

            private ALIGN() {
            }
        }
    }

    public interface BottomDialogRes {
        boolean touchSlide();

        int overrideDialogLayout(boolean var1);

        int overrideMenuDividerDrawableRes(boolean var1);

        int overrideMenuDividerHeight(boolean var1);

        int overrideMenuTextColor(boolean var1);

        float overrideBottomDialogMaxHeight();

        int overrideMenuItemLayout(boolean var1, int var2, int var3, boolean var4);

        int overrideSelectionMenuBackgroundColor(boolean var1);

        boolean selectionImageTint(boolean var1);

        int overrideSelectionImage(boolean var1, boolean var2);

        int overrideMultiSelectionImage(boolean var1, boolean var2);
    }

    public interface WaitTipRes {
        int overrideWaitLayout(boolean var1);

        int overrideRadiusPx();

        boolean blurBackground();

        int overrideBackgroundColorRes(boolean var1);

        int overrideTextColorRes(boolean var1);

        ProgressViewInterface overrideWaitView(Context var1, boolean var2);
    }

    public interface VerticalButtonRes {
        int overrideVerticalOkButtonBackgroundRes(int var1, boolean var2);

        int overrideVerticalCancelButtonBackgroundRes(int var1, boolean var2);

        int overrideVerticalOtherButtonBackgroundRes(int var1, boolean var2);
    }

    public interface HorizontalButtonRes {
        int overrideHorizontalOkButtonBackgroundRes(int var1, boolean var2);

        int overrideHorizontalCancelButtonBackgroundRes(int var1, boolean var2);

        int overrideHorizontalOtherButtonBackgroundRes(int var1, boolean var2);
    }

    public interface BlurBackgroundSetting {
        boolean blurBackground();

        int blurForwardColorRes(boolean var1);

        int blurBackgroundRoundRadiusPx();
    }
}
