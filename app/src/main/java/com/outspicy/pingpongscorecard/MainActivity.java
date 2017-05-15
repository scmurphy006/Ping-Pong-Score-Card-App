package com.outspicy.pingpongscorecard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity{

    /**Declarations for the string finals that are used for storing game prefs**/
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String SCORE_LEFT = "ScoreLeft";
    public static final String SCORE_RIGHT = "ScoreRight";
    public static final String WINS_LEFT = "WinsLeft";
    public static final String WINS_RIGHT = "WinsRight";
    public static final String CURRENT_SERVER = "CurrentServer";
    public static final String CURRENT_MODE = "CurrentMode";
    public static final String CURRENT_SERVE_MODE = "CurrentServeMode";

    private static final String TAG = R.class.getSimpleName();

    /**Player 0 score**/
    private int mScoreLeft = 0;

    /**Player 0 Wins**/
    private int mWinsLeft = 0;

    /**Player 1 Score**/
    private int mScoreRight = 0;

    /**Player 1 Wins**/
    private  int mWinsRight = 0;

    /**21 or 11 point games**/
    private int mGameMode = 21;

    /**mode 21 = 5, mode 11 = 2**/
    private int mServeMode = 5;

    /**0 = left, 1 = right**/
    private int mCurrentServer = 0;

    /** Handles playback of all the sound files */
    private MediaPlayer mMediaPlayer;

    /** Handles audio focus when playing a sound file */
    private AudioManager mAudioManager;

    /**
     * This listener gets triggered whenever the audio focus changes
     * (i.e., we gain or lose audio focus because of another app or device).
     */
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                // The AUDIOFOCUS_LOSS_TRANSIENT case means that we've lost audio focus for a
                // short amount of time. The AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK case means that
                // our app is allowed to continue playing sound but at a lower volume. We'll treat
                // both cases the same way because our app is playing short sound files.

                // Pause playback and reset player to the start of the file. That way, we can
                // play the word from the beginning when we resume playback.
                mMediaPlayer.pause();
                mMediaPlayer.seekTo(0);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // The AUDIOFOCUS_GAIN case means we have regained focus and can resume playback.
                mMediaPlayer.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // The AUDIOFOCUS_LOSS case means we've lost audio focus and
                // Stop playback and clean up resources
                releaseMediaPlayer();
            }
        }
    };

    private SharedPreferences game_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {

                    //  Launch app intro
                    Intent i = new Intent(MainActivity.this, IntroActivity.class);
                    startActivity(i);

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();
                }
            }
        });

        // Start the thread
        t.start();

        /**getting shared prefs if there are any, if not, then use default values**/
        game_state = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mScoreLeft = game_state.getInt(SCORE_LEFT, 0);
        mScoreRight = game_state.getInt(SCORE_RIGHT, 0);
        mWinsLeft = game_state.getInt(WINS_LEFT, 0);
        mWinsRight = game_state.getInt(WINS_RIGHT, 0);
        mCurrentServer = game_state.getInt(CURRENT_SERVER, 0);
        mGameMode = game_state.getInt(CURRENT_MODE, 21);
        mServeMode = game_state.getInt(CURRENT_SERVE_MODE, 5);

        /**Setting the current server color to indicate which player is serving**/
        final int primaryDarkColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        final int servingColor = ContextCompat.getColor(this, R.color.colorServe);

        /**init the Player Header text view so the background color can be changed**/
        final TextView textPlayer1 = (TextView) findViewById(R.id.text_left);
        final TextView textPlayer2 = (TextView) findViewById(R.id.text_right);

        /**init teh audio manager**/
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayer = MediaPlayer.create(this, R.raw.chime);

        /**initially player 0 is serving**/
        textPlayer1.setBackgroundColor(servingColor);
        textPlayer2.setBackgroundColor(primaryDarkColor);

        /**onCreate toggle for which player is serving(this might leave if I can just use the onResume for everything)**/
        ToggleButton serveToggle = (ToggleButton) findViewById(R.id.toggle_serve);
        serveToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onResumes toggle");
                if (isChecked) {
                    mCurrentServer = 0;
                    textPlayer2.setBackgroundColor(servingColor);
                    textPlayer1.setBackgroundColor(primaryDarkColor);
                } else {
                    mCurrentServer = 1;
                    textPlayer1.setBackgroundColor(servingColor);
                    textPlayer2.setBackgroundColor(primaryDarkColor);
                }
                mMediaPlayer.start();
            }
        });

        /**A check to make sure that the serveBar is over the correct player**/
        if (mCurrentServer == 1 && !serveToggle.isChecked()){
            serveToggle.toggle();
        }
        else if (mCurrentServer == 0 && serveToggle.isChecked()){
            serveToggle.toggle();
        }

        /**Updating the score and wins sets them to the initial 0,0**/
        updateScore();
        updateWins();
    }

    public void incrementScoreLeft(View view){
        ToggleButton serveToggle = (ToggleButton) findViewById(R.id.toggle_serve);
        if (mScoreLeft >= mGameMode && mScoreLeft >= mScoreRight + 2){
            mWinsLeft++;

            if (mWinsLeft >= 2){
                resetSet(view);
            }

            if (!serveToggle.isChecked()){
                //serveToggle.toggle();
                setServer(0);
                //Log.d(TAG, "1st Serve Toggle ");
            }

            resetGame(view);
        } else{
            mScoreLeft++;
            //if left gets a point and takes the advantage then setSetver as right(1)
            if (mScoreLeft >= (mGameMode - 1) && mScoreLeft > mScoreRight){
                setServer(1);
                Log.d(TAG, "Left takes the advantage");
            }else if ((mScoreLeft + mScoreRight)% mServeMode == 0){
                    //serveToggle.toggle();
                //Log.d(TAG, "2 Serve Toggle ");
                    if (mCurrentServer == 0){
                        setServer(1);
                    }
                    else {
                        setServer(0);
                    }

                    if (leftHasAdvantage()){
                        setServer(1);
                        Log.d(TAG, "1st left if ");
                    }else if(rightHasAdvantage() && mScoreLeft != mScoreRight){
                        setServer(0);
                        Log.d(TAG, "2nd left if ");
                    }else if (mScoreLeft >= (mGameMode - 1) && mScoreLeft == mScoreRight){
                        setServer(0);
                        Log.d(TAG, "3rd left if ");
                    }
            }
        }

        updateScore();
        updateWins();
    }

    public void incrementScoreRight(View view){
        ToggleButton serveToggle = (ToggleButton) findViewById(R.id.toggle_serve);
        if (mScoreRight >= mGameMode && mScoreRight >= mScoreLeft + 2){
            mWinsRight++;

            if (mWinsRight >= 2){
                resetSet(view);
            }

            if (serveToggle.isChecked()){
                //serveToggle.toggle();
                setServer(0);
                //Log.d(TAG, "3 Serve Toggle ");
            }

            resetGame(view);

        } else{
            mScoreRight++;
            //if right gets a point and takes the advantage then setSetver as left(0)
            if (mScoreRight >= (mGameMode - 1) && mScoreRight > mScoreLeft){
                setServer(0);
                //Log.d(TAG, "Right takes the advantage");
            }else if ((mScoreLeft + mScoreRight)% mServeMode == 0 ){
                    //serveToggle.toggle();
               // Log.d(TAG, "4 Serve Toggle ");
                    if (mCurrentServer == 0){
                        setServer(1);
                    }
                    else {
                        setServer(0);
                    }

                    if (rightHasAdvantage()){
                        setServer(0);
                        Log.d(TAG, "1st right if ");
                    }else if(leftHasAdvantage() && mScoreLeft != mScoreRight){
                        setServer(1);
                        Log.d(TAG, "2nd right if ");
                    }else if (mScoreRight >= (mGameMode - 1) && mScoreLeft == mScoreRight){
                        setServer(1);
                        Log.d(TAG, "3rd right if ");
                    }
            }
        }

        updateScore();
        updateWins();
    }

    public void decrementScoreLeft(View view){
        ToggleButton serveToggle = (ToggleButton) findViewById(R.id.toggle_serve);

        if ((mScoreLeft + mScoreRight)% mServeMode == 0){
            serveToggle.toggle();
        }

        if (mScoreLeft > 0){
            mScoreLeft--;
        }else{
            return;
        }

        updateScore();
    }

    public void decrementScoreRight(View view){
        ToggleButton serveToggle = (ToggleButton) findViewById(R.id.toggle_serve);

        if ((mScoreLeft + mScoreRight) % mServeMode == 0){
            serveToggle.toggle();
        }

        if (mScoreRight > 0){
            mScoreRight--;
        }else{
            return;
        }

        updateScore();
    }

    public void resetSet(View view){
        mWinsLeft = 0;
        mWinsRight = 0;
        mScoreLeft = 0;
        mScoreRight = 0;

        updateWins();
        updateScore();
    }

    public void resetGame(View view){
        mScoreLeft = 0;
        mScoreRight = 0;

        updateWins();
        updateScore();
    }

    public boolean leftHasAdvantage(){
        if(mScoreLeft >= (mGameMode - 1) && mScoreLeft > mScoreRight){
            Log.d(TAG, "left score: " + mScoreLeft + "|| right score: " + mScoreRight);
            return true;
        }
        return false;
    }

    public boolean rightHasAdvantage(){
        if (mScoreRight >= (mGameMode - 1) && mScoreRight > mScoreLeft){
            Log.d(TAG, "left score: " + mScoreLeft + "|| right score: " + mScoreRight);
            return true;
        }
        return false;
    }

    public void setServer(int server){
        Log.d(TAG, "mCurrentServer: " + mCurrentServer);
        ToggleButton serveToggle = (ToggleButton) findViewById(R.id.toggle_serve);
        if(server == 0 && serveToggle.isChecked()){
            serveToggle.toggle();
            Log.d(TAG, "5 Serve Toggle ");
        } else if (server == 1 && !serveToggle.isChecked()){
            serveToggle.toggle();
            Log.d(TAG, "6 Serve Toggle ");
        }
        mCurrentServer = server;
        Log.d(TAG, "Server after setServe: " + mCurrentServer);
    }

    public void updateScore(){
        Button leftScoreButton = (Button) findViewById(R.id.button_score_left);
        leftScoreButton.setText("" + mScoreLeft);

        Button rightScoreButton = (Button) findViewById(R.id.button_score_right);
        rightScoreButton.setText("" + mScoreRight);
    }

    public void updateWins(){
        TextView leftWinsTextView = (TextView) findViewById(R.id.text_left);
        leftWinsTextView.setText("Player 1: " + mWinsLeft);

        TextView rightWinsTextView = (TextView) findViewById(R.id.text_right);
        rightWinsTextView .setText("Player 2: " + mWinsRight);
    }

    @Override
    public void onStop() {
        super.onStop();

        /**onStop, save the current game Prefs**/
        SharedPreferences.Editor editor = game_state.edit();
        editor.putInt(SCORE_LEFT, mScoreLeft);
        editor.putInt(SCORE_RIGHT, mScoreRight);
        editor.putInt(WINS_LEFT, mWinsLeft);
        editor.putInt(WINS_RIGHT, mWinsRight);
        editor.putInt(CURRENT_SERVER, mCurrentServer);
        editor.putInt(CURRENT_MODE, mGameMode);
        editor.putInt(CURRENT_SERVE_MODE, mServeMode);

        //Log.d(TAG, "Server after onStop: " + mCurrentServer);

        // When the activity is stopped, release the media player resources because we won't
        // be playing any more sounds.
        releaseMediaPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**onResume we are checking to see if there are any saved game prefs and if so setting the appropriate values
         * this may replace the iteration of this in the onCreate**/

        mCurrentServer = game_state.getInt(CURRENT_SERVER, 0);

        Log.d(TAG, "Server onResume: " + mCurrentServer);

        final int primaryDarkColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        final int servingColor = ContextCompat.getColor(this, R.color.colorServe);

        final TextView textPlayer1 = (TextView) findViewById(R.id.text_left);
        final TextView textPlayer2 = (TextView) findViewById(R.id.text_right);

        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayer = MediaPlayer.create(this, R.raw.chime);

        textPlayer1.setBackgroundColor(servingColor);
        textPlayer2.setBackgroundColor(primaryDarkColor);

        /**Toggle for Manually changing which player is serving**/
        ToggleButton resumeServeToggle = (ToggleButton) findViewById(R.id.toggle_serve);
        resumeServeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onResumes toggle");
                if (isChecked) {
                    mCurrentServer = 1;
                    textPlayer2.setBackgroundColor(servingColor);
                    textPlayer1.setBackgroundColor(primaryDarkColor);
                } else {
                    mCurrentServer = 0;
                    textPlayer1.setBackgroundColor(servingColor);
                    textPlayer2.setBackgroundColor(primaryDarkColor);
                }
                mMediaPlayer.start();
            }
        });

        /**Toggle for the game mode toggle. Checked means 11 point game. Unchecked means 21 point game.**/
        ToggleButton resumeGameModeToggle = (ToggleButton) findViewById(R.id.toggle_game_mode);
        resumeGameModeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onResumes toggle");
                if (isChecked) {
                    mGameMode = 11;
                    mServeMode = 2;
                } else {
                    mGameMode = 21;
                    mServeMode = 5;
                }
            }
        });

        if (resumeServeToggle.isChecked()){
            setServer(1);

            if (mCurrentServer == 1) {
                textPlayer2.setBackgroundColor(servingColor);
                textPlayer1.setBackgroundColor(primaryDarkColor);
            } else {
                textPlayer1.setBackgroundColor(servingColor);
                textPlayer2.setBackgroundColor(primaryDarkColor);
            }
        }
    }

    /**
     * Clean up the media player by releasing its resources.
     */
    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (mMediaPlayer != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mMediaPlayer.release();

            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            mMediaPlayer = null;

            // Regardless of whether or not we were granted audio focus, abandon it. This also
            // unregisters the AudioFocusChangeListener so we don't get anymore callbacks.
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }
}
