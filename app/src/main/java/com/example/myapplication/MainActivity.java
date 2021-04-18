package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    FirebaseFirestore db;
    Button nameButton;
    Spinner stateSpinner, citySpinner;
    List<String> stateArray;
    HashMap<String, Object> mainMap = new HashMap<String, Object>();
    HashMap<String, ArrayList> stateMap;
    ArrayAdapter<String> stateAdapter, cityAdapter;
    Query userProfileRef;
    String userDocumentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nameButton = findViewById(R.id.nameButton);
        stateSpinner = findViewById(R.id.stateSpinner);
        citySpinner = findViewById(R.id.citySpinner);
        db = FirebaseFirestore.getInstance();

        setSpinner();

    }

    private void setSpinner() {
        String TAG = "spinner";
        String KEY_CITY = "City";
        final DocumentReference sfDocRef= db.collection("Main").document(KEY_CITY);
        db
                .runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        sfDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                        mainMap = (HashMap<String, Object>) document.getData();
                                        stateMap = new HashMap<String, ArrayList>();
                                        stateMap = (HashMap<String, ArrayList>) document.getData().get(KEY_CITY);

                                        stateArray = new ArrayList<>(stateMap.keySet());
                                        stateAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, stateArray);
                                        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        stateSpinner.setAdapter(stateAdapter);
                                        String spinnerSelectedState = stateSpinner.getSelectedItem().toString();
                                        cityAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, new ArrayList<>(stateMap.get(spinnerSelectedState)));
                                        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        citySpinner.setAdapter(cityAdapter);




                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            }

                        });
                        return null;
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");
                        getData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                    }
                });


    }

    private void getData() {
        String TAG = "customer profile";
        userProfileRef=db.collection("Customer")
                .whereEqualTo("mobileNumber", 123456);

        userProfileRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                userDocumentID = document.getId();
                                Toast.makeText(getApplicationContext(), document.getData().get("currentState").toString(), Toast.LENGTH_SHORT).show();

                                String temp = document.getData().get("firstName").toString() + " " + document.getData().get("lastName").toString() + "\n+91" + document.getData().get("mobileNumber").toString() + "\n" + document.getData().get("currentCity").toString();
                                nameButton.setText(temp);
                                String stateName=document.getData().get("currentState").toString();
                                String cityName=document.getData().get("currentCity").toString();

                                stateSpinner.setSelection(stateAdapter.getPosition(stateName));
//                                Log.d(TAG,stateSpinner.getSelectedItem().toString());
//                                String spinnerSelectedState = stateSpinner.getSelectedItem().toString();
                                cityAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, new ArrayList<>(stateMap.get(stateName)));
                                cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                citySpinner.setAdapter(cityAdapter);
                                Log.d(TAG, "onComplete: "+cityName+cityAdapter.getPosition(cityName));
                                citySpinner.setSelection(cityAdapter.getPosition(cityName));

                                HashMap<String, ArrayList> finalStateMap = stateMap;
                                stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                                                String spinnerSelectedState = stateSpinner.getSelectedItem().toString();

                                        cityAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, new ArrayList<>(finalStateMap.get(parent.getItemAtPosition(position).toString())));
                                        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        citySpinner.setAdapter(cityAdapter);
                                        citySpinner.setSelection(cityAdapter.getPosition(cityName));
//                                        Map<String, Object> data = new HashMap<>();
//                                        data.put("currentCity", citySpinner.getSelectedItem().toString());
//                                        data.put("currentState", stateSpinner.getSelectedItem().toString());
//
//                                        db.collection("Customer").document(userDocumentID).set(data, SetOptions.merge());
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                        String spinnerSelectedState = stateSpinner.getSelectedItem().toString();
                                        cityAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, new ArrayList<>(finalStateMap.get(spinnerSelectedState)));
                                        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        citySpinner.setAdapter(cityAdapter);
                                        citySpinner.setSelection(cityAdapter.getPosition(cityName));
                                    }
                                });
                                citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        Map<String, Object> data = new HashMap<>();
                                        data.put("currentCity", citySpinner.getSelectedItem().toString());
                                        data.put("currentState", stateSpinner.getSelectedItem().toString());

                                        db.collection("Customer").document(userDocumentID).set(data, SetOptions.merge());
                                    }
                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });



                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });




    }

}