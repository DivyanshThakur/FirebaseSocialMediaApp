package com.example.firebasesocialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewPostsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private FirebaseAuth firebaseAuth;

    private ListView viewPostsListView;
    private ArrayList<String> usernames;
    private ArrayAdapter adapter;
    private TextView txtDescription;
    private ImageView sentPostImageView;
    private ArrayList<DataSnapshot> dataSnapshots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);

        firebaseAuth = FirebaseAuth.getInstance();

        sentPostImageView = findViewById(R.id.sentPostImageView);
        txtDescription = findViewById(R.id.description);

        viewPostsListView = findViewById(R.id.viewPostsListView);
        usernames = new ArrayList<>();
        dataSnapshots = new ArrayList<>();

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames);
        viewPostsListView.setAdapter(adapter);

        viewPostsListView.setOnItemClickListener(this);
        viewPostsListView.setOnItemLongClickListener(this);

        FirebaseDatabase.getInstance().getReference().child("my_users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("received_posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                dataSnapshots.add(dataSnapshot);
                String fromWhomUsers = (String) dataSnapshot.child("fromWhom").getValue();
                usernames.add(fromWhomUsers);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                int i = 0;

                for (DataSnapshot snapshot : dataSnapshots) {
                    if (snapshot.getKey().equals(dataSnapshot.getKey())) {
                        dataSnapshots.remove(i);
                        usernames.remove(i);

                    }
                    i++;

                }

                adapter.notifyDataSetChanged();
                sentPostImageView.setImageResource(R.drawable.placeholder);
                txtDescription.setText("");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        DataSnapshot myDataSnapShots = dataSnapshots.get(i);
        String imageDownloadLink = (String) myDataSnapShots.child("imageLink").getValue();

        Picasso.get().load(imageDownloadLink).into(sentPostImageView);

        // I am talking about the code below
        txtDescription.setText((String)myDataSnapShots.child("des").getValue());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //continue with delete

                        FirebaseStorage.getInstance().getReference()
                                .child("my_images").child((String)
                                dataSnapshots.get(i).child("imageIdentifier")
                                        .getValue()).delete();

                        FirebaseDatabase.getInstance().getReference()
                                .child("my_users").child(FirebaseAuth.getInstance().getCurrentUser()
                                .getUid()).child("received_posts")
                                .child(dataSnapshots.get(i).getKey())
                                .removeValue();

                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do nothing
            }
        }).setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        return false;
    }
}
