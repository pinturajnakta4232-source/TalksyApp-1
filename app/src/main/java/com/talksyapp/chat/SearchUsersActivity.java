package com.talksyapp.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.talksyapp.chat.R;
import com.talksyapp.chat.adapters.UserAdapter;
import com.talksyapp.chat.models.User;
import com.talksyapp.chat.repositories.UserRepository;
import com.talksyapp.chat.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/** Real Firestore-backed user search. Results are never hard-coded. */
public class SearchUsersActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvUsers;
    private View progressBar, emptyState;

    private final UserRepository userRepository = new UserRepository();
    private final List<User> users = new ArrayList<>();
    private UserAdapter adapter;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);

        currentUid = FirebaseAuth.getInstance().getUid();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etSearch = findViewById(R.id.etSearch);
        rvUsers = findViewById(R.id.rvUsers);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);

        adapter = new UserAdapter(users, this::openChatWith);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            users.clear();
            adapter.notifyDataSetChanged();
            emptyState.setVisibility(View.GONE);
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        userRepository.searchUsers(query, currentUid, new UserRepository.UsersCallback() {
            @Override
            public void onSuccess(List<User> results) {
                progressBar.setVisibility(View.GONE);
                users.clear();
                users.addAll(results);
                adapter.notifyDataSetChanged();
                emptyState.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SearchUsersActivity.this, "Search failed. Check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChatWith(User user) {
        if (user.getUid() != null && user.getUid().equals(currentUid)) {
            // Users cannot start a chat with themselves.
            return;
        }
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.EXTRA_RECEIVER_ID, user.getUid());
        intent.putExtra(Constants.EXTRA_RECEIVER_NAME, user.getFullName());
        intent.putExtra(Constants.EXTRA_RECEIVER_PHOTO, user.getPhotoUrl());
        startActivity(intent);
        finish();
    }
}
