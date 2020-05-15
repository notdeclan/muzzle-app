package wtf.declan.muzzle.view.callbacks;

import wtf.declan.muzzle.data.db.entities.ConversationEntity;

public interface RecipientSwipeCallback {
    void onConversationSwiped(int adapterPosition, ConversationEntity conversationEntity);
}
