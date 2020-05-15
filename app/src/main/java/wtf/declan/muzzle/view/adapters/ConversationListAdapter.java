package wtf.declan.muzzle.view.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import wtf.declan.muzzle.R;
import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.data.db.entities.ConversationWithMessages;
import wtf.declan.muzzle.data.db.entities.MessageEntity;
import wtf.declan.muzzle.recipient.Recipient;
import wtf.declan.muzzle.recipient.RecipientFactory;
import wtf.declan.muzzle.view.callbacks.RecipientClickCallback;


public class ConversationListAdapter extends ListAdapter<ConversationWithMessages, ConversationListAdapter.ConversationHolder> {

    private Context context;

    private RecipientClickCallback recipientClickListener;

    public ConversationListAdapter(Context context, RecipientClickCallback recipientClickListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.recipientClickListener = recipientClickListener;
    }

    private static DiffUtil.ItemCallback<ConversationWithMessages> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ConversationWithMessages>() {
                @Override
                public boolean areItemsTheSame(ConversationWithMessages oldMessage, ConversationWithMessages newMessage) {
                    return oldMessage.getConversation().getId() == newMessage.getConversation().getId();
                }

                @Override
                public boolean areContentsTheSame(ConversationWithMessages oldMessage, ConversationWithMessages newMessage) {
                    return oldMessage.getConversation().getUpdated().equals(newMessage.getConversation().getUpdated());
                }
            };


    @NonNull
    @Override
    public ConversationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater  inflater    =   LayoutInflater.from(parent.getContext());
        View            view        =   inflater.inflate(R.layout.conversation_list_item_layout, parent, false);

        return new ConversationHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationHolder holder, int position) {
        // get Message object from position in view
        ConversationWithMessages    conversationWithMessages    =   getItem(position);
        ConversationEntity          conversation                =   conversationWithMessages.getConversation();
        List<MessageEntity>         messages                    =   conversationWithMessages.getMessages();

        final Recipient recipient = RecipientFactory.getRecipientFromNumber(context, conversation.getNumber());

        // set the sender address, message, and time
        holder.senderContact.setText(recipient.getDisplayName());

        if(messages.size() > 0) {
            MessageEntity lastMessage;
            if((lastMessage = messages.get(0)) != null) {
                holder.message.setText(lastMessage.getBody());
                holder.time.setText(getFormattedDate(lastMessage.getDate().getTime()));
            }
        } else {
            holder.message.setText("No messages");
        }

        holder.senderImage.setImageDrawable(recipient.getIcon());
        holder.setRecipient(recipient);
        holder.setConversation(conversation);
        // set bold text if unread
        if (!conversation.isRead()) {
            holder.senderContact.setTypeface(holder.senderContact.getTypeface(), Typeface.BOLD);
            holder.message.setTypeface(holder.message.getTypeface(), Typeface.BOLD);
            holder.time.setTypeface(holder.time.getTypeface(), Typeface.BOLD);
        } else {
            holder.senderContact.setTypeface(null, Typeface.NORMAL);
            holder.message.setTypeface(null, Typeface.NORMAL);
            holder.time.setTypeface(null, Typeface.NORMAL);
        }
    }

    public void remove(int position) {
        notifyItemRemoved(position);
    }

    private String getFormattedDate(long milliSeconds)
    {
        final Calendar  message    = Calendar.getInstance();
        final Calendar  now        = Calendar.getInstance();

        message.setTimeInMillis(milliSeconds);
        now.setTimeInMillis(System.currentTimeMillis());

        final String    format;
        final boolean   sameYear    = message.get(Calendar.YEAR) == now.get(Calendar.YEAR);

        if(message.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) && sameYear) {
            // if same day and year
            format = "HH:mm"; // 14:12
        } else if(message.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR) && sameYear) {
            // if same week and year
            format = "HH:mm EEE"; // 14:12 Tue
        } else if(sameYear) {
            // if same year
            format = "d MMM"; // 24 Jan
        } else {
            // if not same day, week, or year
            format = "d MMM y"; // 24 Jan 2019
        }

        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(message.getTime());
    }


    public class ConversationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView senderImage;
        private final TextView senderContact;
        private final TextView message;
        private final TextView time;
        private Recipient recipient;

        private RelativeLayout swipeView, visibleView;

        private ConversationEntity conversation;

        ConversationHolder(@NonNull View itemView) {
            super(itemView);

            senderImage = itemView.findViewById(R.id.recipient_icon);
            senderContact = itemView.findViewById(R.id.recipient_name);
            message = itemView.findViewById(R.id.message_content);
            time = itemView.findViewById(R.id.time);

            swipeView = itemView.findViewById(R.id.swipe_view);
            visibleView = itemView.findViewById(R.id.visible_view);
            visibleView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (recipientClickListener != null) {
                recipientClickListener.onConversationClick(
                        recipient
                );
            }
        }

        void setRecipient(Recipient recipient) {
            this.recipient = recipient;
        }

        void setConversation(ConversationEntity conversation) {
            this.conversation = conversation;
        }

        public ConversationEntity getConversation() {
            return this.conversation;
        }

        public RelativeLayout getSwipeView() {
            return swipeView;
        }

        public RelativeLayout getVisibleView() {
            return visibleView;
        }
    }
}
