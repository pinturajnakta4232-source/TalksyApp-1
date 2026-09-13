package com.talksyapp.chat.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.talksyapp.chat.R;
import com.talksyapp.chat.models.Message;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final List<Message> messages;
    private final String currentUid;

    public MessageAdapter(List<Message> messages, String currentUid) {
        this.messages = messages;
        this.currentUid = currentUid;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return currentUid.equals(message.getSenderId()) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SENT) {
            return new SentViewHolder(inflater.inflate(R.layout.item_message_sent, parent, false));
        } else {
            return new ReceivedViewHolder(inflater.inflate(R.layout.item_message_received, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        String time = message.getTimestamp() != null
                ? new SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.getTimestamp())
                : "";

        if (holder instanceof SentViewHolder) {
            SentViewHolder h = (SentViewHolder) holder;
            h.tvMessage.setText(message.getText());
            h.tvTime.setText(time);
            h.tvStatus.setText(statusLabel(message.getStatus()));
        } else if (holder instanceof ReceivedViewHolder) {
            ReceivedViewHolder h = (ReceivedViewHolder) holder;
            h.tvMessage.setText(message.getText());
            h.tvTime.setText(time);
        }
    }

    private String statusLabel(String status) {
        if (status == null) return "Sent";
        switch (status) {
            case Message.STATUS_READ: return "Read";
            case Message.STATUS_DELIVERED: return "Delivered";
            default: return "Sent";
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvStatus;
        SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
