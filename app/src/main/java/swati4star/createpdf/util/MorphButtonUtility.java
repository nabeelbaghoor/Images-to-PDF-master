package swati4star.createpdf.util;

import android.app.Activity;
import android.widget.FrameLayout;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;

import com.dd.morphingbutton.MorphingButton;

import swati4star.createpdf.R;

//import android.support.annotation.ColorRes;
//import android.support.annotation.DimenRes;

public class MorphButtonUtility {

    private final Activity mActivity;

    public MorphButtonUtility(Activity activity) {
        mActivity = activity;
    }

    public int integer() {
        return mActivity.getResources().getInteger(R.integer.mb_animation);
    }

    private int dimen(@DimenRes int resId) {
        return (int) mActivity.getResources().getDimension(resId);
    }

    private int color(@ColorRes int resId) {
        return mActivity.getResources().getColor(resId);
    }

    public void morphToSquare(final MorphingButton btnMorph, int duration) {
        MorphingButton.Params square = defaultButton(duration);
        String text = btnMorph.getText().toString().isEmpty() ?
                mActivity.getString(R.string.create_pdf) :
                btnMorph.getText().toString();
        square.color(color(R.color.mb_blue));
        square.colorPressed(color(R.color.mb_blue_dark));
        square.text(text);
        btnMorph.morph(square);
    }

    public void morphToSuccess(final MorphingButton btnMorph) {
        MorphingButton.Params circle = MorphingButton.Params.create()
                .duration(integer())
                .cornerRadius(dimen(R.dimen.mb_height_56))
                .width(dimen(R.dimen.mb_height_56))
                .height(dimen(R.dimen.mb_height_56))
                .color(color(R.color.mb_green))
                .colorPressed(color(R.color.mb_green_dark))
                .icon(R.drawable.ic_check_white_24dp);
        btnMorph.morph(circle);
    }

    public void morphToGrey(final MorphingButton btnMorph, int duration) {
        MorphingButton.Params square = defaultButton(duration);
        square.color(color(R.color.mb_gray));
        square.colorPressed(color(R.color.mb_gray));
        square.text(btnMorph.getText().toString());
        btnMorph.morph(square);
    }

    private MorphingButton.Params defaultButton(int duration) {
        return MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius(dimen(R.dimen.mb_corner_radius_2))
                .width(FrameLayout.LayoutParams.MATCH_PARENT)
                .height(FrameLayout.LayoutParams.WRAP_CONTENT);
    }
}
