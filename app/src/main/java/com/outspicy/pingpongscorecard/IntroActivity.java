package com.outspicy.pingpongscorecard;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro2 {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String firstTutorialText = "While playing, press the score to give a point";

        String secondTutorialText = "Pressing the \"score - 1\" button will take away a point";

        String thirdTutorialText = "This bar represents the current server";

        String fourthTutorialText = "Pressing the server toggle will change who is serving";

        String fifthTutorialText = "Use this toggle to switch between 21 and 11 point games";

        String sixthTutorialText = "When a player wins press the winning score again to start a new game";

        addSlide(AppIntroFragment.newInstance("Tutorial", firstTutorialText, R.drawable.tutorial_score, Color.parseColor("#26a69a")));
        addSlide(AppIntroFragment.newInstance("Tutorial", secondTutorialText, R.drawable.tutorial_decrement, Color.parseColor("#5c6bc0")));
        addSlide(AppIntroFragment.newInstance("Tutorial", thirdTutorialText, R.drawable.tutorial_bar, Color.parseColor("#263238")));
        addSlide(AppIntroFragment.newInstance("Almost done", fourthTutorialText, R.drawable.tutorial_server, Color.parseColor("#26a69a")));
        addSlide(AppIntroFragment.newInstance("Almost...", fifthTutorialText, R.drawable.tutorial_game_toggle, Color.parseColor("#5c6bc0")));
        addSlide(AppIntroFragment.newInstance("Ok, last one", sixthTutorialText, R.drawable.tutorial_win, Color.parseColor("#26a69a")));

        // SHOW or HIDE the statusbar
        showStatusBar(true);
        showSkipButton(true);

        // Edit the color of the nav bar on Lollipop+ devices
        //setNavBarColor(Color.parseColor("#3F51B5"))

        // Turn vibration on and set intensity
        // NOTE: you will need to ask VIBRATE permission in Manifest if you haven't already
        //setVibrate(true);
        //setVibrateIntensity(30);

        // Animations -- use only one of the below. Using both could cause errors.
        //setFadeAnimation(); // OR
        //setZoomAnimation(); // OR
        //setFlowAnimation(); // OR
        //setSlideOverAnimation(); // OR
        setDepthAnimation(); // OR
        //setCustomTransformer(yourCustomTransformer);

        // Permissions -- takes a permission and slide number
        //askForPermissions(new String[]{Manifest.permission.CAMERA}, 3);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        finish();

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
