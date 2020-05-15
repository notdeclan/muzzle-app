package wtf.declan.muzzle.view.ui;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.WindowManager;

public class ConversationPopupActivity extends ConversationActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
        );

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity    = Gravity.TOP;
        layoutParams.dimAmount  = 0.25f;
        layoutParams.alpha      = 1f;

        getWindow().setAttributes(layoutParams);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        int screenWidth = displaymetrics.widthPixels;
        int screenHeight = displaymetrics.heightPixels;

        if(screenHeight > screenWidth) { // if portrait
            getWindow().setLayout((int) (screenWidth * 0.85), (int) (screenHeight * 0.5));
        } else { // if landscape
            getWindow().setLayout((int) (screenWidth * 0.7), (int) (screenHeight * 0.75));
        }

        super.onCreate(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        messageTextBox.requestFocus();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }


}
