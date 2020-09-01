package com.example.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trivia.data.AnswerListAsyncResponse;
import com.example.trivia.data.QuestionBank;
import com.example.trivia.model.Question;

import org.w3c.dom.Text;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String MESSAGE_ID = "message_prefs";
    private TextView questionText;
    private TextView questionCounterTextView;
    private TextView currentScoreText;
    private TextView highestScoreText;
    private Button trueButton;
    private Button falseButton;
    private Button shareButton;
    private ImageButton nextButton;
    private ImageButton previousButton;
    private int questionCounter=0;
    private int currentScore=0;
    private int highestScore=0;
    private List<Question> questionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         questionText=findViewById(R.id.question_text_id);
         questionCounterTextView=findViewById(R.id.counter_text_id);
         currentScoreText=findViewById(R.id.current_score_id);
         highestScoreText=findViewById(R.id.highest_score_id);
         trueButton=findViewById(R.id.true_button_id);
         falseButton=findViewById(R.id.false_button_id);
         nextButton=findViewById(R.id.next_button_id);
         previousButton=findViewById(R.id.prev_button_id);
         shareButton=findViewById(R.id.share_button_id);

         trueButton.setOnClickListener(this);
         falseButton.setOnClickListener(this);
         nextButton.setOnClickListener(this);
         previousButton.setOnClickListener(this);
         shareButton.setOnClickListener(this);

        questionList = new QuestionBank().getQuestions(new AnswerListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<Question> questionArrayList) {

                questionCounterTextView.setText((questionCounter+1)+"/"+questionArrayList.size());
                questionText.setText(questionArrayList.get(questionCounter).getAnswer());
                Log.d("Inside", "processFinished: "+questionArrayList);
            }
        });

        SharedPreferences getShareData=getSharedPreferences(MESSAGE_ID,MODE_PRIVATE);
        String value= getShareData.getString("message","Highest score");
        highestScore=getShareData.getInt("highest score",0);
        highestScoreText.setText(value);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.true_button_id:
                correctAnswer(true);
                updateQuestion();
                break;
            case R.id.false_button_id:
                correctAnswer(false);
                updateQuestion();
                break;
            case R.id.next_button_id:
                    goNext();
                break;
            case R.id.prev_button_id:
                questionCounter--;
                if(questionCounter<0) questionCounter=questionList.size()-1;
                updateQuestion();
                break;
            case R.id.share_button_id:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Playing Trivia");
                intent.putExtra(Intent.EXTRA_TEXT,"Highest score is:" + highestScore);
                startActivity(intent);
                break;
        }
        currentScoreText.setText(MessageFormat.format("Current score:{0}", String.valueOf(currentScore)));
        highestScoreText.setText(MessageFormat.format("Highest score:{0}", String.valueOf(highestScore)));
    }

    private void correctAnswer(boolean userChoice) {
        boolean answer=questionList.get(questionCounter).isAnswerTrue();
        int toastMessageId=0;
        if(answer==userChoice) {
            toastMessageId = R.string.correct_text;
            fadeView();
            currentScore++;
            if(highestScore<currentScore) {
                highestScore = currentScore;
            }
        }
        else {
            toastMessageId = R.string.wrong_text;
            shakeAnimation();
            if(currentScore>0) currentScore--;
        }
        Toast.makeText(MainActivity.this,toastMessageId,Toast.LENGTH_SHORT).show();
    }

    private void updateQuestion() {
        questionText.setText(questionList.get(questionCounter).getAnswer());
        questionCounterTextView.setText((questionCounter+1)+"/"+questionList.size());
    }

    private void goNext(){
        questionCounter=(questionCounter+1)%questionList.size();
        updateQuestion();
    }

    private void fadeView(){
        final CardView cardView=findViewById(R.id.cardView);
        AlphaAnimation alphaAnimation=new AlphaAnimation(1.0f,0.0f);

        alphaAnimation.setDuration(300);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        cardView.setAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.GREEN);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
                goNext();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void shakeAnimation(){
        Animation shake = AnimationUtils.loadAnimation(MainActivity.this,R.anim.shake_animation);
        final CardView cardview=findViewById(R.id.cardView);
        cardview.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardview.setCardBackgroundColor(Color.RED);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardview.setCardBackgroundColor(Color.WHITE );
                goNext();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        //writing to disk

        SharedPreferences sharedPreferences=getSharedPreferences(MESSAGE_ID,MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("message","Highest score:"+Integer.toString(highestScore));
        editor.putInt("highest score",highestScore);
        editor.apply();
    }
}
