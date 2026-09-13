package com.talksyapp.chat.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.talksyapp.chat.R;
import com.talksyapp.chat.adapters.MessageAdapter;
import com.talksyapp.chat.models.Message;
import com.talksyapp.chat.repositories.ChatRepository;
import com.talksyapp.chat.repositories.UserRepository;
import com.talksyapp.chat.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * THE CORE FEATURE: real-time one-to-one chat.
 *
 * Two phones running this same app, logged in as two different real
 * Firebase Authentication users, exchange messages purely through
 * Cloud Firestore documents and a live snapshot listener — there is
 * no polling and no local/mock message store anywhere in this class.
 */
public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private View emptyState;
    private TextView tvReceiverName, tvReceiverStatus;
    private CircleImageView ivReceiverPhoto;

    private final ChatRepository chatRepository = new ChatRepository();
    private final UserRepository userRepository = new UserRepository();
    private final List<Message> messages = new ArrayList<>();
    private MessageAdapter adapter;

    private ListenerRegistration messagesListener;
    private ListenerRegistration receiverListener;

    private String currentUid;
    private String receiverUid;
    private String receiverName;
    private String receiverPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        currentUid = FirebaseAuth.getInstance().getUid();
        receiverUid = getIntent().getStringExtra(Constants.EXTRA_RECEIVER_ID);
        receiverName = getIntent().getStringExtra(Constants.EXTRA_RECEIVER_NAME);
        receiverPhotoUrl = getIntent().getStringExtra(Constants.EXTRA_RECEIVER_PHOTO);

        if (currentUid == null || receiverUid == null) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvReceiverName = findViewById(R.id.tvReceiverName);
        tvReceiverStatus = findViewById(R.id.tvReceiverStatus);
        ivReceiverPhoto = findViewById(R.id.ivReceiverPhoto);
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        emptyState = findViewById(R.id.emptyState);

        tvReceiverName.setText(receiverName);
        Glide.with(this).load(receiverPhotoUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .into(ivReceiverPhoto);

        adapter = new MessageAdapter(messages, currentUid);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());

        // Make sure the conversation document exists before we start
        // listening/sending, so both phones resolve to the same thread.
        chatRepository.ensureConversation(currentUid, receiverUid, new ChatRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                listenToMessages();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ChatActivity.this, "Could not start conversation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenToReceiverStatus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (messagesListener != null) messagesListener.remove();
        if (receiverListener != null) receiverListener.remove();
    }

    /** Live Firestore snapshot listener — the heart of the real-time chat. */
    private void listenToMessages() {
        messagesListener = chatRepository.listenToMessages(currentUid, receiverUid,
                new ChatRepository.MessagesCallback() {
                    @Override
                    public void onMessagesChanged(List<Message> updated) {
                        messages.clear();
                        messages.addAll(updated);
                        adapter.notifyDataSetChanged();
                        emptyState.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
                        if (!messages.isEmpty()) {
                            rvMessages.scrollToPosition(messages.size() - 1);
                        }
                        chatRepository.markMessagesAsRead(currentUid, receiverUid);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(ChatActivity.this, "Connection lost. Retrying...", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void listenToReceiverStatus() {
        receiverListener = userRepository.listenToUser(receiverUid, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(com.talksyapp.chat.models.User user) {
                if (user == null) return;
                tvReceiverStatus.setText(user.isOnline()
                        ? getString(R.string.online) : getString(R.string.offline));
            }

            @Override
            public void onError(Exception e) {
                // Non-fatal — status simply won't update.
            }
        });
    }

    private void sendMessage() {
        String text = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        if (TextUtils.isEmpty(text)) return;

        btnSend.setEnabled(false);
        chatRepository.sendMessage(currentUid, receiverUid, text, new ChatRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                etMessage.setText("");
                btnSend.setEnabled(true);
            }

            @Override
            public void onError(Exception e) {
                btnSend.setEnabled(true);
                Toast.makeText(ChatActivity.this, "Message failed to send. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
