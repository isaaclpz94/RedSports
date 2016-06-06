package com.example.usuario.redsports.splash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.usuario.redsports.Login;
import com.example.usuario.redsports.R;

import java.util.Locale;

/**
 * Created by USUARIO on 07/05/2016.
 */
public class SplashScreen extends AppCompatActivity {

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    /**
     * Called when the activity is first created.
     */
    Thread splashTread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        prefs = getSharedPreferences("idioma_preferences", Context.MODE_PRIVATE);
        String idioma = prefs.getString("idioma", "");
        switch (idioma) {
            case "es":
                Locale locale = new Locale("es");
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                SplashScreen.this.getApplicationContext().getResources().updateConfiguration(config, null);
                break;
            case "en":
                Locale locale2 = new Locale("en_US");
                Locale.setDefault(locale2);
                Configuration config2 = new Configuration();
                config2.locale = locale2;
                SplashScreen.this.getApplicationContext().getResources().updateConfiguration(config2, null);
                break;
            default:
                Locale locale3 = new Locale("es");
                Locale.setDefault(locale3);
                Configuration config3 = new Configuration();
                config3.locale = locale3;
                SplashScreen.this.getApplicationContext().getResources().updateConfiguration(config3, null);
                break;
        }

        StartAnimations();
    }

    private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.layout_splash);
        anim.reset();
        RelativeLayout l = (RelativeLayout) findViewById(R.id.lin_lay);
        assert l != null;
        l.clearAnimation();
        l.startAnimation(anim);

        Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.image_splash);
        anim2.reset();
        ImageView iv = (ImageView) findViewById(R.id.splash);
        iv.clearAnimation();
        iv.startAnimation(anim2);

        Animation anim3 = AnimationUtils.loadAnimation(this, R.anim.titulo_splash);
        anim3.reset();
        TextView tvTitulo = (TextView) findViewById(R.id.tvTituloSplash);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/RockSalt.ttf");
        tvTitulo.setTypeface(tf);
        tvTitulo.clearAnimation();
        tvTitulo.startAnimation(anim3);

        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    // Splash screen pause time
                    while (waited < 3500) {
                        sleep(100);
                        waited += 100;
                    }
                    Intent intent = new Intent(SplashScreen.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    finish();
                }

            }
        };
        splashTread.start();

    }

}

