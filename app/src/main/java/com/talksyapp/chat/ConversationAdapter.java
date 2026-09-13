package com.talksyapp.chat.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.talksyapp.chat.R;
import com.talksyapp.chat.models.Conversation;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    private final List<Conversation> conversations;
    private final OnConversationClickListener listener;

    public ConversationAdapter(List<Conversation> conversations, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.tvName.setText(conversation.getOtherUserName());
        holder.tvLastMessage.setText(
                conversation.getLastMessage() == null || conversation.getLastMessage().isEmpty()
                        ? "Say hello 👋" : conversation.getLastMessage());

        if (conversation.getLastMessageTime() != null) {
            holder.tvTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(conversation.getLastMessageTime()));
        } else {
            holder.tvTime.setText("");
        }

        Glide.with(holder.itemView.getContext())
                .load(conversation.getOtherUserPhotoUrl())
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .into(holder.ivPhoto);

        holder.onlineDot.setVisibility(conversation.isOtherUserOnline() ? View.VISIBLE : View.GONE);

        if (conversation.getUnreadCount() > 0) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(conversation.getUnreadCount()));
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onConversationClick(conversation));
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivPhoto;
        View onlineDot;
        TextView tvName, tvLastMessage, tvTime, tvUnreadCount;

        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            onlineDot = itemView.findViewById(R.id.onlineDot);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
        }
    }
}
