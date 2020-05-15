package wtf.declan.muzzle.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import wtf.declan.muzzle.BR;
import wtf.declan.muzzle.R;
import wtf.declan.muzzle.recipient.Recipient;
import wtf.declan.muzzle.view.callbacks.RecipientClickCallback;

/**
 * Adapter is reasonable for displaying contacts inside the ContactListFragment
 */
public class ContactListAdapter extends ListAdapter<Recipient, ContactListAdapter.ContactHolder> {

    private static DiffUtil.ItemCallback<Recipient> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Recipient>() {
                @Override
                public boolean areItemsTheSame(Recipient oldRecipient, Recipient newRecipient) {
                    return oldRecipient.getContactUri() == newRecipient.getContactUri();
                }

                @Override
                public boolean areContentsTheSame(Recipient oldRecipient, Recipient newRecipient) {
                    return oldRecipient.getNumber().equals(newRecipient.getNumber()) &&
                            oldRecipient.getDisplayName().equals(newRecipient.getDisplayName());
                }
            };

    private RecipientClickCallback recipientClickCallback;

    public ContactListAdapter(RecipientClickCallback recipientClickCallback) {
        super(DIFF_CALLBACK);
        this.recipientClickCallback = recipientClickCallback;
    }


    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater      layoutInflater      = LayoutInflater.from(parent.getContext());
        ViewDataBinding     viewDataBinding     = DataBindingUtil.inflate(
                layoutInflater,
                R.layout.contact_list_item_layout,
                parent,
                false
        );

        return new ContactHolder(viewDataBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactHolder holder, int position) {
        holder.bindTo(getItem(position));
    }

    public class ContactHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ViewDataBinding viewDataBinding;

        ContactHolder(@NonNull ViewDataBinding viewDataBinding) {
            super(viewDataBinding.getRoot());

            this.viewDataBinding = viewDataBinding;
            this.viewDataBinding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (recipientClickCallback != null) {
                recipientClickCallback.onConversationClick(
                    getItem(getAdapterPosition())
                );
            }
        }

        void bindTo(Recipient recipient) {
            viewDataBinding.setVariable(BR.recipient, recipient);
            viewDataBinding.executePendingBindings();
        }
    }
}
