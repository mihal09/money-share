package com.example.bulki;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {
    private ListView list ;
    private ArrayAdapter<String> adapter ;
    private EditText amountText;
    private ArrayList<Integer> activeUsersList;
    private ArrayList<User> usersList = new ArrayList<User>();

    int getValue(){
//        return 3;
        return (int)(Double.parseDouble(amountText.getText().toString())*100);
    }

    ArrayList<User> getUserData(){
        ArrayList<User> result = new ArrayList<User>();
        String response_string="";
        try {
            URL url = new URL("http://niepolecam.cba.pl/bulki/users.php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try{
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                response_string = stringBuilder.toString();
                bufferedReader.close();
//                Log.d("API: ", stringBuilder.toString());
            }
            finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
        }

        try {
            JSONObject jObject = new JSONObject(response_string);
            JSONArray usersJSONArray = jObject.getJSONArray("users");
            for (int i = 0; i < usersJSONArray.length(); i++) {
                JSONObject row = usersJSONArray.getJSONObject(i);
                User user = new User(Integer.valueOf(row.getString("id")),row.getString("name"), row.getInt("balance"), row.getInt("currentBalance"));
                result.add(user);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    void createPanel(){
        usersList = getUserData();
        ArrayList<String> usersStringList = new ArrayList<String>();
        for (int i = 0; i < usersList.size(); i++) {
            usersStringList.add(usersList.get(i).toString());
        }
        adapter = new ArrayAdapter<String>(this, R.layout.row, usersStringList);
        list.setAdapter(adapter);
    }
    void refreshPanel(){
        usersList = getUserData();
        ArrayList<String> usersStringList = new ArrayList<String>();
        for (int i = 0; i < usersList.size(); i++) {
            usersStringList.add(usersList.get(i).toString());
        }
        adapter.clear();
        adapter.addAll(usersStringList);
        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //siec
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        //listview
        list = (ListView) findViewById(R.id.userListView);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        list.setItemsCanFocus(false);
        activeUsersList = new ArrayList<Integer>();

        //dodanie uzytkownikow
//        String users[] = {"Michał", "Paweł", "Marcin"};
//        ArrayList<String> usersStringList = new ArrayList<String>();
//        for (int i = 0; i < users.length; i++) {
//            usersList.add(new User(users[i],0,0));
//        }
        createPanel();

        amountText = (EditText)findViewById(R.id.amountText);
        final TextView activeUserTextView = (TextView)findViewById(R.id.activeUserTextView);
        final EditText decriptionText = (EditText)findViewById(R.id.descriptionText);

        //przyciski
        final ImageButton addButton = findViewById(R.id.addButton);
        final ImageButton removeButton = findViewById(R.id.removeButton);
        final ImageButton refreshButton = findViewById(R.id.refreshButton);

        if(usersList.isEmpty())
            refreshButton.setVisibility(View.VISIBLE);

        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int usersCount = activeUsersList.size();
                if(usersCount==0)
                    return;
                int amount = getValue();
               // amount = (int)Math.floor((double)amount / usersCount);
                for(int index : activeUsersList){
                    User user = usersList.get(index);
                    user.addMoney(amount, decriptionText.getText().toString());
                }
                refreshPanel();
            }
        });


        removeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int usersCount = activeUsersList.size();
                Log.d("uzytkownicy:", activeUsersList.toString());
                if(usersCount==0)
                    return;
                int amount = getValue();
                amount = (int)Math.ceil((double)amount / usersCount);
                Boolean canEveryoneAfford = true;
                for(int index : activeUsersList){
                    User user = usersList.get(index);
                    if(!user.canAfford(amount)) {
                        canEveryoneAfford = false;
                        break;
                    }
                }
                if(!canEveryoneAfford) {
                    Log.d("Usuwanie pieniedzy", "Komuś brakuje");
                    return;
                }
                for(int index : activeUsersList){
                    User user = usersList.get(index);
                    user.removeMoney(amount, decriptionText.getText().toString());
                }

                refreshPanel();
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refreshPanel();
//                if(!usersList.isEmpty())
//                    refreshButton.setVisibility(View.INVISIBLE);
            }
        });


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id_long) {
                if(activeUsersList.size()==0) {
                    addButton.setVisibility(View.VISIBLE);
                    removeButton.setVisibility(View.VISIBLE);
                    amountText.setVisibility(View.VISIBLE);
                }
                final String item = (String) parent.getItemAtPosition(position);
                int id = (int)id_long;
                Log.d("id",Integer.toString(id));
                User user = usersList.get(position);
                Integer userId = usersList.indexOf(user);
                if(!list.isItemChecked(id)) {
                    if(activeUsersList.contains(userId))
                        activeUsersList.remove(userId);
                }
                else{
                    if(!activeUsersList.contains(userId))
                        activeUsersList.add(userId);
                }

//                if(!activeUsersList.isEmpty())
//                    Log.d("XD",activeUsersList.toString());


                if(activeUsersList.isEmpty())
                    activeUserTextView.setText("Wybierz użytkowników");
                else
                    activeUserTextView.setText("Ilośc wybranych użytkowników: "+ activeUsersList.size());
               // list.setItemChecked(id,true);
            }

        });
    }
}
