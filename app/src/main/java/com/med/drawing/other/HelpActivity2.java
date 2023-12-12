package com.med.drawing.other;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.med.drawing.R;


public class HelpActivity2 extends AppCompatActivity {

    Button btn_next;
    ImageView img_help;
    Animation pushanim;
    int selectedpos = 0;
    TextView text_help;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_help);
        this.pushanim = AnimationUtils.loadAnimation(this, R.anim.view_push);
        AdsManager.getInstance().loadBanner(HelpActivity2.this, getString(R.string.AdMob_Banner));

        this.text_help = (TextView) findViewById(R.id.txt_help);
        this.img_help = (ImageView) findViewById(R.id.ic_help);
        this.btn_next = (Button) findViewById(R.id.btn_next);
        final int[] iArr = {R.drawable.help1, R.drawable.help2, R.drawable.help3};
        final int[] iArr2 = {R.string.help1_1, R.string.help1_2, R.string.help1_3};
        this.img_help.setImageResource(iArr[0]);
        this.text_help.setText(iArr2[0]);
        this.btn_next.setOnClickListener(new View.OnClickListener() {
            @Override 
            public void onClick(View view) {
                view.startAnimation(HelpActivity2.this.pushanim);
                if (HelpActivity2.this.selectedpos < iArr.length - 1) {
                    HelpActivity2.this.selectedpos++;
                } else {
                    HelpActivity2.this.selectedpos = 0;
                }
                HelpActivity2.this.img_help.setImageResource(iArr[HelpActivity2.this.selectedpos]);
                HelpActivity2.this.text_help.setText(iArr2[HelpActivity2.this.selectedpos]);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        BackScreen();
    }

    private void BackScreen() {
        finish();
        AppConstants.overridePendingTransitionExit(this);
    }
}
