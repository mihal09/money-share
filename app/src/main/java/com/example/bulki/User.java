package com.example.bulki;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class User {
    int id;
    String name;
    int balance;
    int currentBalance;

    private String PriceToString(int balance){
        int decimal = Math.abs(balance)%100;
        return (balance<0 ? "-":"") + Math.abs(balance)/100+","+ (decimal<10 ? "0"+decimal:decimal) +"zł";
    }
    @Override public String toString() {
        return name+": "+ PriceToString(balance);
    }
    User(int id, String name, int balance, int current_balance){
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.currentBalance = current_balance;
    }
    boolean canAfford(int price){
        return (balance+300>=price);
    }

    private void changeMoney(int amount, String description){
        try {
            String path = "http://niepolecam.cba.pl/bulki/changeMoney.php?id="+Integer.toString(id)+"&v="+Integer.toString(amount)+"&d="+description;
            Log.d("sciezka", path);
            URL url = new URL(path);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.getInputStream();
        }  catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
        }
    }
    void addMoney(int amount, String description){
        balance += amount;
        changeMoney(amount, description);
    }

    void removeMoney(int amount, String description){
        balance -= amount;
        changeMoney(-amount, description);
    }
}
