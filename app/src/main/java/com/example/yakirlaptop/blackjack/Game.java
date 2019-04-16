package com.example.yakirlaptop.blackjack;

import android.content.Context;
import android.support.annotation.NonNull;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;



public class Game {
    private Button newGame;
    private Button stay;
    private Button hit;
    private Button logOut;
    private TextView playerText;
    private TextView dealerText;
    private TextView gameBet;
    private EditText bet;
    private Context context;
    private final String[] suits = {"♠", "♣", "♥", "♦"};
    private final String[] value = {"A", "K", "Q", "J", "10", "9", "8", "7", "6", "5", "4", "3", "2"};
    private boolean gameOver, playerWon,gameStarted;
    private ArrayList<Card> dealer;
    private ArrayList<Card> player;
    private ArrayList<Card> deck;
    private int dScore, pScore;
    private long currentPoints,adminPoints;
    private int currentBet;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public Game(Button newGame, Button stay, Button hit,Button logOut, TextView playerText, TextView dealerText, TextView gameBet, EditText bet, final Context context) {
        gameOver = false;
        playerWon = false;
        dealer = new ArrayList<>();
        player = new ArrayList<>();
        deck = new ArrayList<>();
        dScore = 0;
        pScore = 0;
        currentBet = 0;
        this.newGame = newGame;
        this.stay = stay;
        this.hit = hit;
        this.logOut = logOut;
        this.playerText = playerText;
        this.dealerText = dealerText;
        this.gameBet = gameBet;
        this.bet = bet;
        this.context = context;

    }

    public void stay() {
        gameOver = true;
        checkEnd();
        showStatus();
    }

    public void hit() {
        player.add(deck.remove(0));
        checkEnd();
        showStatus();
    }

    public void newGame() {
        final DocumentReference docRef = db.collection("Users").document(mAuth.getCurrentUser().getDisplayName());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    currentPoints = (long) documentSnapshot.get("points");
                    adminPoints = currentPoints;

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(context, "שגיאה " + e.getMessage(), Toasty.LENGTH_LONG).show();
            }
        });
        if (bet.getText().length() == 0 || Integer.parseInt(bet.getText().toString())<=0) {
            Toasty.error(context, R.string.bet_error, Toasty.LENGTH_LONG).show();
        } else
            checkBetFireStore();
    }

    private void start() {
        gameStarted = true;
        gameOver = false;
        playerWon = false;
        dealer.removeAll(dealer);
        player.removeAll(player);
        deck.addAll(createDeck());
        shuffleDeck();
        dealer.add(deck.remove(0));
        dealer.add(deck.remove(0));
        player.add(deck.remove(0));
        player.add(deck.remove(0));
    }

    public void showStatus() {
        gameBet.setText("המשחק על "+currentBet+" נקודות");
        String dealerCardString = "";
        for (int i = 0; i < dealer.size(); i++) {
            dealerCardString += dealer.get(i).toString();
        }
        String playerCardString = "";
        for (int i = 0; i < player.size(); i++) {
            playerCardString += player.get(i).toString();
        }
        updateScores();
        dealerText.setText("יד של המחשב: " +"\n" +dealerCardString +"\n" + "ניקוד של המחשב: " +dScore);
        playerText.setText("יד שלך: " +"\n" + playerCardString +"\n"+ "ניקוד שלך: "  + pScore);

        if (gameOver) {
            if (playerWon) {
                winPoints();
            } else {
                if (pScore == dScore) {
                    Toasty.warning(context, "תיקו ", Toasty.LENGTH_LONG).show();
                    logOut.setVisibility(View.VISIBLE);
                    bet.setVisibility(View.VISIBLE);
                    bet.setText("0");
                    currentBet = Integer.parseInt(bet.getText().toString());
                    newGame.setVisibility(View.VISIBLE);
                    hit.setVisibility(View.GONE);
                    stay.setVisibility(View.GONE);
                } else {
                    losePoints();
                }
            }
        }

    }

    public void checkEnd() {
        updateScores();

        if (gameOver) {
            while (dScore < pScore && dScore <= 21 && pScore <= 21) {
                dealer.add(deck.remove(0));
                updateScores();
            }
        }

        if (dScore > 21) {
            gameStarted = false;
            playerWon = true;
            gameOver = true;
        } else if (pScore > 21) {
            gameStarted = false;
            playerWon = false;
            gameOver = true;
        } else if (gameOver) {
            if (dScore < pScore)
                playerWon = true;
            else
                playerWon = false;
        }
    }

    public void updateScores() {
        dScore = getScore(dealer);
        pScore = getScore(player);
    }

    public int getScore(ArrayList<Card> array) {
        int score = 0;
        boolean hasAce = false;
        for (int i = 0; i < array.size(); i++) {
            score += Card.getNumVal(array.get(i));
            if (array.get(i).value == "A")
                hasAce = true;
        }
        if (hasAce && score + 10 <= 21)
            return score + 10;
        return score;

    }

    public ArrayList<Card> createDeck() {
        ArrayList<Card> deck = new ArrayList<>();
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < suits.length; j++) {
                Card c = new Card(suits[j], value[i]);
                deck.add(c);
            }
        }
        return deck;
    }

    public void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = (int) Math.floor(Math.random() * deck.size());
            Card temp = new Card(deck.get(i));
            deck.set(i, deck.get(j));
            deck.set(j, temp);

        }

    }

  public void checkBetFireStore(){
      currentBet = Integer.parseInt(bet.getText().toString());
      final DocumentReference docRef = db.collection("Users").document(mAuth.getCurrentUser().getDisplayName());
      docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
          @Override
          public void onSuccess(DocumentSnapshot documentSnapshot) {
              if((long)documentSnapshot.get("points")<currentBet){
                  currentPoints = (long)documentSnapshot.get("points");
                  Toasty.error(context, "אין לך מספיק נקודות בחשבון! כרגע יש לך - "+currentPoints, Toasty.LENGTH_LONG).show();

              }
              else{
                  logOut.setVisibility(View.GONE);
                  bet.setVisibility(View.GONE);
                  newGame.setVisibility(View.GONE);
                  stay.setVisibility(View.VISIBLE);
                  hit.setVisibility(View.VISIBLE);
                  start();
                  showStatus();
              }

          }
      }).addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
              Toasty.error(context, "שגיאה " + e.getMessage(), Toasty.LENGTH_LONG).show();

          }
      });


  }

    public void losePoints(){
        final CollectionReference users = db.collection("Users");
        Map<String, Object> data = new HashMap<>();
        data.put("points", currentPoints - currentBet);
        users.document("" + mAuth.getCurrentUser().getDisplayName()).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (currentBet>0&&currentPoints>0) {
                        Toasty.warning(context, "הפסדת " + currentBet +  " נקודות, סהכ - "+(currentPoints-currentBet), Toasty.LENGTH_LONG).show();
                        logOut.setVisibility(View.VISIBLE);
                        bet.setVisibility(View.VISIBLE);
                        bet.setText("0");
                        currentBet = Integer.parseInt(bet.getText().toString());
                        newGame.setVisibility(View.VISIBLE);
                        hit.setVisibility(View.GONE);
                        stay.setVisibility(View.GONE);
                    }
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toasty.error(context, "Error writing document - " + e.getMessage(), Toasty.LENGTH_LONG).show();
                        }
                    });

    }
    public void winPoints(){
        final CollectionReference users = db.collection("Users");
        Map<String, Object> data = new HashMap<>();
        data.put("points", currentPoints + currentBet);
        users.document("" + mAuth.getCurrentUser().getDisplayName()).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toasty.success(context,  "הרווחת " + currentBet +  " נקודות, סהכ - "+(currentPoints+currentBet), Toasty.LENGTH_LONG).show();
                logOut.setVisibility(View.VISIBLE);
                bet.setVisibility(View.VISIBLE);
                bet.setText("0");
                currentBet = Integer.parseInt(bet.getText().toString());
                newGame.setVisibility(View.VISIBLE);
                hit.setVisibility(View.GONE);
                stay.setVisibility(View.GONE);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toasty.error(context, "Error writing document - " + e.getMessage(), Toasty.LENGTH_LONG).show();
                    }
                });

    }

    public void admin() {
        final CollectionReference users = db.collection("Users");
        Map<String, Object> data = new HashMap<>();
        data.put("points", adminPoints + 1000);
        users.document("" + mAuth.getCurrentUser().getDisplayName()).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                adminPoints +=1000;
                Toasty.success(context,  "הרווחת " + 1000 +  " נקודות, סהכ - "+adminPoints, Toasty.LENGTH_LONG).show();

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toasty.error(context, "Error writing document - " + e.getMessage(), Toasty.LENGTH_LONG).show();
                    }
                });
    }

    public boolean isGameStarted(){
        return gameStarted;
    }
}
