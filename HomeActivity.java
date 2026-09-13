package com.talksyapp.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.talksyapp.chat.R;
import com.talksyapp.chat.adapters.ConversationAdapter;
import com.talksyapp.chat.models.Conversation;
import com.talksyapp.chat.models.User;
import com.talksyapp.chat.repositories.ChatRepository;
import com.talksyapp.chat.repositories.UserRepository;
import com.talksyapp.chat.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Real-time conversation list, driven entirely by a Firestore
 * snapshot listener on the conversations collection. No hard-coded
 * conversations are ever shown.
 */
public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvConversations;
    private View emptyState;
    private SwipeRefreshLayout swipeRefresh;

    private final ChatRepository chatRepository = new ChatRepository();
    private final UserRepository userRepository = new UserRepository();
    private final List<Conversation> conversations = new ArrayList<>();
    private ConversationAdapter adapter;
    private ListenerRegistration conversationsListener;

    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvConversations = findViewById(R.id.rvConversations);
        emptyState = findViewById(R.id.emptyState);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        adapter = new ConversationAdapter(conversations, conversation -> openChat(
                conversation.getOtherUserId(), conversation.getOtherUserName(), conversation.getOtherUserPhotoUrl()));
        rvConversations.setLayoutManager(new LinearLayoutManager(this));
        rvConversations.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> swipeRefresh.setRefreshing(false));

        findViewById(R.id.fabSearch).setOnClickListener(v ->
                startActivity(new Intent(this, SearchUsersActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        userRepository.setOnlineStatus(currentUid, true);
        listenToConversations();
    }

    @Override
    protected void onStop() {
        super.onStop();
        userRepository.setOnlineStatus(currentUid, false);
        if (conversationsListener != null) conversationsListener.remove();
    }

    private void listenToConversations() {
        conversationsListener = chatRepository.listenToConversations(currentUid,
                new ChatRepository.ConversationsCallback() {
                    @Override
                    public void onConversationsChanged(List<Conversation> updated) {
                        resolveOtherUsers(updated);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(HomeActivity.this, "Failed to load conversations", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Each conversation only stores participant UIDs, so we join in the other user's profile. */
    private void resolveOtherUsers(List<Conversation> updated) {
        conversations.clear();
        if (updated.isEmpty()) {
            adapter.notifyDataSetChanged();
            toggleEmptyState();
            return;
        }
        int[] remaining = {updated.size()};
        for (Conversation conversation : updated) {
            String otherUid = null;
            if (conversation.getParticipants() != null) {
                for (String uid : conversation.getParticipants()) {
                    if (!uid.equals(currentUid)) otherUid = uid;
                }
            }
            final String finalOtherUid = otherUid;
            if (finalOtherUid == null) {
                remaining[0]--;
                continue;
            }
            userRepository.getUser(finalOtherUid, new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    if (user != null) {
                        conversation.setOtherUserId(user.getUid());
                        conversation.setOtherUserName(user.getFullName());
                        conversation.setOtherUserPhotoUrl(user.getPhotoUrl());
                        conversation.setOtherUserOnline(user.isOnline());
                        conversations.add(conversation);
                    }
                    remaining[0]--;
                    if (remaining[0] <= 0) finishBinding();
                }

                @Override
                public void onError(Exception e) {
                    remaining[0]--;
                    if (remaining[0] <= 0) finishBinding();
                }
            });
        }
    }

    private void finishBinding() {
        conversations.sort((a, b) -> {
            if (a.getLastMessageTime() == null || b.getLastMessageTime() == null) return 0;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });
        adapter.notifyDataSetChanged();
        toggleEmptyState();
    }

    private void toggleEmptyState() {
        emptyState.setVisibility(conversations.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openChat(String uid, String name, String photoUrl) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.EXTRA_RECEIVER_ID, uid);
        intent.putExtra(Constants.EXTRA_RECEIVER_NAME, name);
        intent.putExtra(Constants.EXTRA_RECEIVER_PHOTO, photoUrl);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchUsersActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
