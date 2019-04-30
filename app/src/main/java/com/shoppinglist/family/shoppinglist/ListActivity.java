package com.shoppinglist.family.shoppinglist;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.mbms.MbmsErrors;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    LinearLayout ScrollLayout;

    int ShowSettings;  // 1- show all items.   2- show just no-mark items.
    final static int ShowAll =1;
    final static int ShowNoMark =2;

    int counter;
    String team;
    int internalCounter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        InitializContenet();
    }

    private void InitializContenet() {
        ScrollLayout = findViewById(R.id.ScrollLayout);
        ShowSettings=1;
        counter = 1;
        team = getIntent().getStringExtra("team");
        ReadFromDB();
    }

    private void ReadFromDB() {
        ReadFireBaseCounter();
    }

    private void ReadFireBaseCounter() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Lists/"+team+"/counter");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (dataSnapshot.getValue()!= null){
                    int count = dataSnapshot.getValue(Integer.class);
                    counter = count;
                }
                else{
                    counter = 1;
                }
                ReadFireBaseItemList();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ListActivity.this,"הייתה בעיה בגישה לDB",Toast.LENGTH_LONG).show();
            }
        });
    }


    private void ReadFireBaseItemList() {
        internalCounter=0;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Lists/"+team);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                ScrollLayout.removeAllViews();
                 for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if ( !(snapshot.getValue() instanceof Long )) {
                            Item i = snapshot.getValue(Item.class);
                            BuildItemViewFromDb(i);
                            internalCounter++;
                        }
                 }
                boolean needToShowAllItem = NeedToShowAllItems();
                for (int j=0;j<ScrollLayout.getChildCount();j++) {
                    View v =((LinearLayout)((LinearLayout)ScrollLayout.getChildAt(j)).getChildAt(1)).getChildAt(1);
                    if(((CheckBox)v).isChecked()  && !needToShowAllItem){
                        ScrollLayout.getChildAt(j).setVisibility(View.GONE);
                    }

                }
                if (internalCounter==0) HandleNoItemsOnDB();
                Toast.makeText(ListActivity.this,"קליטת פריטים התבצעה בהצלחה",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ListActivity.this,"הייתה בעיה בגישה לDB",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void HandleNoItemsOnDB() {
        counter=1;
        WriteToDB("Lists/"+team+"/counter",counter);
    }

    private void WriteToDB(String path, Object item) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(path);
        myRef.push();
        myRef.setValue(item);
    }

    private void RemoveFromDb(String path){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(path);
        myRef.setValue(null);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.actionbar1,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_contact:
                PopUpNewItemDialog();
                break;
            case R.id.action_remove_Marked:
                RemoveMarkedItems();
                break;
            case R.id.action_show_noMark:
                ShowSettings = ShowNoMark;
                FilterList();
                break;
            case R.id.action_show_all:
                ShowSettings = ShowAll;
                FilterList();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void FilterList() {
        if (NeedToShowAllItems()) {
            for (int i = 0; i < ScrollLayout.getChildCount(); i++) {
                View v = ScrollLayout.getChildAt(i);
                v.setVisibility(View.VISIBLE);
            }
        }
        else{
            for(int i=0;i<ScrollLayout.getChildCount();i++){
                View v =((LinearLayout)((LinearLayout)ScrollLayout.getChildAt(i)).getChildAt(1)).getChildAt(1);
                if(((CheckBox)v).isChecked()){
                    ScrollLayout.getChildAt(i).setVisibility(View.GONE);
                }
            }
        }

    }

    private boolean NeedToShowAllItems() {
        if (ShowSettings==ShowAll)   // 1- show all items.   2- show just no-mark items.
            return true;
        else
            return false;
    }

    private void RemoveMarkedItems() {
        ArrayList<View> list =new ArrayList<>();
        for(int i=0;i<ScrollLayout.getChildCount();i++){
            View v =((LinearLayout)((LinearLayout)ScrollLayout.getChildAt(i)).getChildAt(1)).getChildAt(1);
            if(!((CheckBox)v).isChecked()){
                list.add((LinearLayout)ScrollLayout.getChildAt(i));
            }
            else{
                RemoveFromDb("Lists/"+team+"/"+((TextView)((LinearLayout)ScrollLayout.getChildAt(i)).getChildAt(4)).getText().toString());
            }
        }
        ScrollLayout.removeAllViews();
        for (View v:list) {
            ScrollLayout.addView(v);
        }
    }

    private void PopUpNewItemDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
        View mview = getLayoutInflater().inflate(R.layout.newitem, null);
        final EditText itemName;
        itemName = (EditText) mview.findViewById(R.id.itemName);


        Button b = (Button) mview.findViewById(R.id.okClick);

        builder.setView(mview);
        final AlertDialog dialog = builder.create();
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemName.getText().toString()==null)
                    Toast.makeText(ListActivity.this,"נא להכניס שם פרטי תקין",Toast.LENGTH_SHORT).show();
                else if (itemName.getText().toString().equals(""))
                    Toast.makeText(ListActivity.this,"נא להכניס שם פרטי תקין",Toast.LENGTH_SHORT).show();
                else
                    AddNewItemList(itemName.getText().toString());
                dialog.hide();
            }

        });
        dialog.show();
    }

     void AddNewItemList(String itemList) {
        String[] items = itemList.split(" |,");
        for (String item:items) {
            AddNewItemToListView(BuildItemView(item,false,counter));
            Item i = new Item(false,item,counter);

            WriteToDB("Lists/"+team+"/"+counter,i);
            IncreasCounter();
            WriteToDB("Lists/"+team+"/counter",counter);
        }

    }

    private void IncreasCounter() {
        counter++;
    }


    private void BuildItemViewFromDb(Item i) {
        AddNewItemToListView(BuildItemView(i.getName(),i.getIsChecked(),i.getId()));
    }

    private View BuildItemView(String itemName,boolean isChecked,int countFromItem){
        View item,itemH,text,checkBox,id;

        item = new LinearLayout(this);
        ((LinearLayout)item).setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,185));
        ((LinearLayout) item).setOrientation(LinearLayout.VERTICAL);

        item = AddSpace(item);

        itemH = new LinearLayout(this);
        ((LinearLayout)itemH).setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,185));
        ((LinearLayout) itemH).setOrientation(LinearLayout.HORIZONTAL);

        text = new TextView(this);
        ((TextView) text).setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,100));
        ((TextView) text).setText(itemName);
        ((TextView) text).setTextSize(25);

        checkBox = new CheckBox(this);
        ((CheckBox) checkBox).setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,100));
        ((CheckBox) checkBox).setChecked(isChecked);
        ((CheckBox) checkBox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View textViewID = ((LinearLayout)view.getParent().getParent()).getChildAt(4);
                View textView = ((LinearLayout)view.getParent()).getChildAt(0);
                int id = Integer.parseInt(((TextView) textViewID).getText().toString());
                String text = ((TextView) textView).getText().toString();
                Item i = new Item(((CheckBox)view).isChecked(),text,id);
                WriteToDB("Lists/"+team+"/"+id,i);
                FilterList();


            }
        });


        ((LinearLayout) itemH).addView(text);
        ((LinearLayout) itemH).addView(checkBox);

        ((LinearLayout) item).addView(itemH);

        item = AddSpace(item);

        item = AddseparateLine(item);

        id = new TextView(this);
        if (countFromItem!=0)
            ((TextView) id).setText(""+countFromItem);
        else
            ((TextView) id).setText(""+counter);
        ((TextView) id).setVisibility(View.GONE);

        ((LinearLayout) item).addView(id);

        return item;

    }
    private void AddNewItemToListView(View view){
        ScrollLayout.addView(view);
    }

    private View AddseparateLine(View view) {
        View space;

        space = new View (this);
        space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,5));
        space.setBackgroundColor(Color.GRAY);

        ((LinearLayout) view).addView(space);
        return view;
    }

    private View AddSpace(View view) {
        View space;

        space = new View (this);
        space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,40));
        ((LinearLayout) view).addView(space);
        return view;
    }
}
