package sunny.easychatwa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class SplashActivity extends AppCompatActivity {

    int SPLASH_DISPLAY_LENGTH = 5000;
    ImageView ivLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

//        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
//        ivLogo.startAnimation(hyperspaceJumpAnimation);


    }


}

