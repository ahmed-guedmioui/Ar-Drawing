package com.ardrawing.sketchtrace.util.other;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.ardrawing.sketchtrace.R;


public class HelpActivity extends AppCompatActivity {

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
        this.text_help = (TextView) findViewById(R.id.txt_help);
        this.img_help = (ImageView) findViewById(R.id.ic_help);
        this.btn_next = (Button) findViewById(R.id.btn_next);
        final int[] iArr = {R.drawable.h1, R.drawable.h2, R.drawable.h3, R.drawable.h4, R.drawable.h5};
        final int[] iArr2 = {R.string.help1, R.string.help2, R.string.help3, R.string.help4, R.string.help5};
        this.img_help.setImageResource(iArr[0]);
        this.text_help.setText(iArr2[0]);
        this.btn_next.setOnClickListener(new View.OnClickListener() {
            @Override 
            public void onClick(View view) {
                view.startAnimation(HelpActivity.this.pushanim);
                if (HelpActivity.this.selectedpos < iArr.length - 1) {
                    HelpActivity.this.selectedpos++;
                } else {
                    HelpActivity.this.selectedpos = 0;
                }
                HelpActivity.this.img_help.setImageResource(iArr[HelpActivity.this.selectedpos]);
                HelpActivity.this.text_help.setText(iArr2[HelpActivity.this.selectedpos]);
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
