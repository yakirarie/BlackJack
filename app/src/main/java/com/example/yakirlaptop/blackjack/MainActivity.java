package com.example.yakirlaptop.blackjack;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

// BlackJack by Yakir Arie
public class MainActivity extends AppCompatActivity {
    private long backPressedTime;
    private Toast backToasty;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    boolean isNew;
    private GoogleSignInClient mGoogleSignInClient;
    Game game;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //interact with gui
        TextView playerText = findViewById(R.id.player_textView);
        TextView dealerText = findViewById(R.id.dealer_textView);
        TextView gameBet = findViewById(R.id.game_bet);
        Button newGame = findViewById(R.id.newGame_button);
        Button hit = findViewById(R.id.hit_button);
        Button stay = findViewById(R.id.stay_button);
        Button logOut = findViewById(R.id.log_out_button);
        Button adminButton = findViewById(R.id.admin_button);
        EditText betPlace = findViewById(R.id.place_bet);
        betPlace.setText("0");
        pointsRecieve();
        game = new Game(newGame, stay, hit,logOut, playerText, dealerText,gameBet,betPlace,this);

        //hide not-in-use buttons
        hit.setVisibility(View.GONE);
        stay.setVisibility(View.GONE);
        adminButton.setVisibility(View.GONE);
        if (mAuth.getCurrentUser().getUid().equals("BNSWHHuqwZWgXt3wZZV6gs3Gefw1"))
            adminButton.setVisibility(View.VISIBLE);

        //code for new game button
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                game.newGame();
            }
        });

        //code for hit button
        hit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                game.hit();
            }
        });

        //code for stay button
        stay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                game.stay();
            }
        });

        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                game.admin();
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                mGoogleSignInClient.signOut();
                finish();
                Intent intent = new Intent(getApplicationContext(), GoogleSignInActivity.class);
                startActivity(intent);
            }
        });

    }

    public void pointsRecieve() {
        final CollectionReference users = db.collection("Users");
        Map<String, Object> data = new HashMap<>();
        data.put("points", 6000);
        isNew = getIntent().getBooleanExtra("isNew", false);
        if (isNew) {
            users.document("" + mAuth.getCurrentUser().getDisplayName()).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Toasty.success(getApplicationContext(), R.string.new_user_points, Toasty.LENGTH_LONG).show();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toasty.error(getApplicationContext(), "Error writing document - " + e.getMessage(), Toasty.LENGTH_LONG).show();
                        }
                    });
        }
    }


    @Override
    public void onBackPressed() {

        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            if (game.isGameStarted())
                game.losePoints();
            backToasty.cancel();
            super.onBackPressed();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else {
            backToasty = Toasty.info(getApplicationContext(), "לחץ שוב כדי לצאת", Toasty.LENGTH_LONG);
            backToasty.show();

        }
        backPressedTime = System.currentTimeMillis();
    }


}
