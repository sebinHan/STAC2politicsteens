package droidmentor.PoliticTeens_Client;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import droidmentor.PoliticTeens_Client.models.JungConPost;
import droidmentor.PoliticTeens_Client.models.JungPost;
import droidmentor.PoliticTeens_Client.models.UserLogin;

public class NewJungPostActivity extends BaseActivity2 {

    private static final String TAG = "NewJungPostActivity";
    private static final String REQUIRED = "입력해주세요";

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    private EditText mTitleField;
    private EditText mBodyField;
    private Button mSubmitButton;
    private Spinner mCategory;
//String color = "#" + dataSnapshot.child("club").child(user_club).child("just_color").getValue(String.class));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myjungdang_new_post);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        mTitleField = (EditText) findViewById(R.id.field_title);
        mBodyField = (EditText) findViewById(R.id.field_body);
        mCategory = (Spinner)findViewById(R.id.field_category);


        mSubmitButton = (Button) findViewById(R.id.fab_submit_post);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
    }

    private void submitPost() {
        final String title = mTitleField.getText().toString();
        final String body = mBodyField.getText().toString();
        final String categoryText = mCategory.getSelectedItem().toString();
        final int clubId = S.club_id;

        // Title is required
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(REQUIRED);
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(body)) {
            mBodyField.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String userId = getUid();
        Log.d("세빈2",userId);
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        UserLogin user = dataSnapshot.getValue(UserLogin.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(NewJungPostActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writeNewPost(userId, user.username, title, body, categoryText);
                            if(categoryText.equals("안건")){
                                writeMonthPost(userId, user.username, title, body, categoryText,S.jungColor);
                            }
                        }

                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                });
        // [END single_value_read]
    }

    private void setEditingEnabled(boolean enabled) {
        mTitleField.setEnabled(enabled);
        mBodyField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    // [START write_fan_out]
    private void writeNewPost(String userId, String username, String title, String body, String category) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("/"+S.club_id+"/").push().getKey();
        String keyTest = mDatabase.child("/"+S.club_id+"/").push().getKey();
        S.distinctJung = keyTest;
        Log.d("ㅂㅈㄷㅂㅈㄷ",keyTest);
        JungPost post = new JungPost(userId, username, title, body, category);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/jung_posts/" + key+ "/"+S.distinctJung +"/", postValues);
        childUpdates.put("/jung_user-posts/" + userId + "/"+S.distinctJung +"/" + key, postValues);

        /*
        *  String key = mDatabase.child("plaza_posts").push().getKey();
        Post post = new Post(userId, username, title, body);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/plaza_posts/" + key, postValues);
        childUpdates.put("/plaza_user-posts/" + userId + "/" + key, postValues);
        * */
        mDatabase.updateChildren(childUpdates);
    }

    private void writeMonthPost(String userId, String username, String title, String body, String category, String color) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("jung_all_posts").push().getKey();
        String keyTest = mDatabase.child("/"+S.club_id+"/").push().getKey();
        S.distinctJung = keyTest;
        Log.d("ㅂㅈㄷㅂㅈㄷ",keyTest);
        JungConPost post = new JungConPost(userId, username, title, body, category,color);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/jung_all_posts/" + key , postValues);
        /*
        *  String key = mDatabase.child("plaza_posts").push().getKey();
        Post post = new Post(userId, username, title, body);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/plaza_posts/" + key, postValues);
        childUpdates.put("/plaza_user-posts/" + userId + "/" + key, postValues);
        * */
        mDatabase.updateChildren(childUpdates);

    }

    // [END write_fan_out]
}