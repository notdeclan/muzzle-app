package wtf.declan.muzzle.view.adapters;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.app.ShareCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;

import wtf.declan.muzzle.BR;
import wtf.declan.muzzle.R;
import wtf.declan.muzzle.data.db.entities.MessageEntity;
import wtf.declan.muzzle.recipient.Recipient;
import wtf.declan.muzzle.view.ui.ConversationActivity;


public class ConversationAdapter extends ListAdapter<MessageEntity, ConversationAdapter.MessageViewHolder> {

    private static final String TAG = ConversationAdapter.class.getSimpleName();

    private Recipient recipient;

    public ConversationAdapter(Recipient recipient) {
        super(DIFF_CALLBACK);
        this.recipient  = recipient;
    }

    private static DiffUtil.ItemCallback<MessageEntity> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<MessageEntity>() {
            @Override
            public boolean areItemsTheSame(MessageEntity oldMessage, MessageEntity newMessage) {
                return oldMessage.getId() == newMessage.getId();
            }

            @Override
            public boolean areContentsTheSame(MessageEntity oldMessage, MessageEntity newMessage) {
                return oldMessage.getBody().equals(newMessage.getBody())
                        && oldMessage.getMessageType().equals(newMessage.getMessageType())
                        && oldMessage.isSent() == newMessage.isSent()
                        && oldMessage.isDelivered() == newMessage.isDelivered()
                        && oldMessage.isFailed() == newMessage.isFailed();
            }
    };

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater      layoutInflater      = LayoutInflater.from(parent.getContext());
        ViewDataBinding     viewDataBinding     = DataBindingUtil.inflate(
                layoutInflater,
                getLayoutForType(viewType),
                parent,
                false
        );

        return new MessageViewHolder(recipient, viewDataBinding);
    }


    @Override
    public int getItemViewType(int position) {
        MessageEntity message = getItem(position);
        if(message != null) {
            // If message failed
            if(message.isFailed()) {
                return 2;
            }

            // If message is still sending
            if(!message.isSent() && message.isOutbox()) {
                return 3;
            }

            // if message is received then 1 else if message is outbox then 0
            return message.isInbox() && !message.isOutbox() ? 1 : 0;
        }

        return -1;
    }

    private @LayoutRes int getLayoutForType(int type) {
        switch (type) {
            case 0:
                return R.layout.message_sent_layout;
            case 1:
                return R.layout.message_received_layout;
            case 2:
                return R.layout.message_failed_layout;
            case 3:
                return R.layout.message_pending_layout;
            default:
                throw new IllegalArgumentException("Unsupported MessageType");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bindTo(getItem(position));
    }


    static class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        private final Recipient   recipient;
        private final ViewDataBinding binding;

        private MessageEntity messageEntity;

        MessageViewHolder(Recipient recipient, ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding    = binding;
            this.recipient  = recipient;

            binding.getRoot().setClickable(true);
            binding.getRoot().setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            showDialog(view.getContext());
            return true;
        }

        void showDialog(Context context) {
            String[]    items = {"Details", "Copy Text", "Share"};
            AlertDialog alert = new AlertDialog.Builder(context)
                    .setCancelable(true)
                    .setItems(items, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                handleDetails(context);
                                break;
                            case 1:
                                handleCopy(context);
                                break;
                            case 2:
                                handleShare(context);
                                break;
                        }
                    }).create();
            alert.show();
        }

        void handleDetails(Context context) {
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            stringBuilder.append(
                    detailRow("Date", getFormattedDate(messageEntity.getDate()))
            );
            stringBuilder.append("\n\n");
            stringBuilder.append(
                    detailRow("Message Type", messageEntity.getMessageType().getFriendly())
            );

            if(messageEntity.isFailed()) {
                stringBuilder.append("\n\n");
                stringBuilder.append(
                        detailRow("Delivery Status", "Failed to send")
                );
            } else if (messageEntity.isDelivered()) {
                stringBuilder.append("\n\n");
                stringBuilder.append(
                        detailRow("Delivery Status", "Delivered")
                );
            }

            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setTitle("Message Details")
                    .setMessage(stringBuilder)
                    .create();

            alertDialog.show();
        }

        CharSequence detailRow(String prefix, String value) {
            SpannableString string = new SpannableString(prefix + "\n" + value);
            string.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, prefix.length(),0);
            return string;
        }

        void handleCopy(Context context) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

            if(clipboard != null) {
                clipboard.setPrimaryClip(
                        ClipData.newPlainText("Message Text", messageEntity.getBody())
                );

                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Failed to get Clipboard Service");
            }
        }

        void handleShare(Context context) {
            Intent  shareIntent = new Intent(context, ConversationAdapter.class)
                    .setAction(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, messageEntity.getBody());

            context.startActivity(Intent.createChooser(shareIntent, "Share Via"));
        }


        void bindTo(MessageEntity messageEntity) {
            this.messageEntity = messageEntity;

            binding.setVariable(BR.recipient, recipient);
            binding.setVariable(BR.message, messageEntity);
            binding.executePendingBindings();
        }

        String getFormattedDate(Date date)
        {
            String              dateFormat  = "HH:mm:ss z dd/MM/yyyy";
            SimpleDateFormat    formatter   = new SimpleDateFormat(dateFormat);

            return formatter.format(date.getTime());
        }
    }
}
