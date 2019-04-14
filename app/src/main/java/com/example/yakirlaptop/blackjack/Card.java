package com.example.yakirlaptop.blackjack;

public class Card {
    public String suit = "";
    public String value = "";

    public Card(String suit,String value){
        this.suit = suit;
        this.value = value;
    }
    public Card(Card c){
        this.suit = c.suit;
        this.value = c.value;
    }
    public static int getNumVal(Card c){
        if (c.value=="A")
            return 1;
        if(c.value=="K"||c.value=="Q"||c.value=="J")
            return 10;
        return Integer.parseInt(c.value);
    }
    public String toString(){
        return value+" "+suit;
    }
}
