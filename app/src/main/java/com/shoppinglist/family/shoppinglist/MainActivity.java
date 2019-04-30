package com.shoppinglist.family.shoppinglist;


import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.mbms.MbmsErrors;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    EditText LoginNameText;
    int counter;
    String team;
    int teamCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitializContenet();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.actionbar2,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_team:
                OpenAddGroupForm();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void OpenAddGroupForm() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View mview = getLayoutInflater().inflate(R.layout.newteam, null);
        final EditText teamName;
        teamName = (EditText) mview.findViewById(R.id.teamName);


        Button b = (Button) mview.findViewById(R.id.okTeamClick);

        builder.setView(mview);
        final AlertDialog dialog = builder.create();
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (teamName.getText().toString()==null)
                    Toast.makeText(MainActivity.this,"נא להכניס שם תקין",Toast.LENGTH_SHORT).show();
                else if (teamName.getText().toString().equals(""))
                    Toast.makeText(MainActivity.this,"נא להכניס שם תקין",Toast.LENGTH_SHORT).show();
                else
                    AddNewTeam(teamName.getText().toString());
                dialog.hide();
            }

        });
        dialog.show();
    }

    private void AddNewTeam(String s) {
        AddTOFireBaseIfNotExist(s);
    }
    private void AddTOFireBaseIfNotExist(final String inputname) {
        teamCount = 0;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Teams");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                teamCount=0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if ( !(snapshot.getValue() instanceof Long )) {
                        if (snapshot.getValue(String.class).toLowerCase().trim().equals(inputname.toLowerCase().trim())) {
                            Toast.makeText(MainActivity.this, "קבוצה זו קיימת כבר!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        teamCount++;
                    }
                }
                AddTeamToFireBase(inputname,teamCount+1);
                teamCount++;
                AddTeamCounterToFireBase();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this,"הייתה בעיה בגישה לDB",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void AddTeamCounterToFireBase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Teams/teamCount");
        myRef.setValue(teamCount);
    }

    private void AddTeamToFireBase(String inputname, int teamCount) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Teams/"+teamCount);
        myRef.push();
        myRef.setValue(inputname.toLowerCase().trim());
    }

    private void ReadFireBase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Teams");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getValue(String.class).toLowerCase().trim().equals(LoginNameText.getText().toString().toLowerCase().trim())) {
                        OpenTeamList();
                        return;
                    }
                }
                Toast.makeText(MainActivity.this,"לא נמצאה כזו קבוצה",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this,"הייתה בעיה בגישה לDB",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void OpenTeamList() {
        Intent i = new Intent(this,ListActivity.class);
        i.putExtra("team",LoginNameText.getText().toString());
        startActivity(i);
    }

    private void FireBase() {

    }

    private void InitializContenet() {
        LoginNameText = findViewById(R.id.LoginNameText);
        counter = 1;
    }

    public void LoginBtnOnClick(View view){
        if (HasLegalText()){
            Login();
        }
    }

    private void Login() {
        ReadFireBase();
    }

    private boolean HasLegalText() {
        if(LoginNameText.getText().toString()!=null){
            if (LoginNameText.getText().toString().trim()!=""){
                return true;
            }
        }
        return false;
    }

}
