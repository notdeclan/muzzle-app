package wtf.declan.muzzle.view.helpers;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import wtf.declan.muzzle.view.adapters.ConversationListAdapter;
import wtf.declan.muzzle.view.callbacks.RecipientSwipeCallback;


public class ConversationListTouchHelper extends ItemTouchHelper.SimpleCallback {

    private RecipientSwipeCallback listener;

    public ConversationListTouchHelper(RecipientSwipeCallback listener) {
        super(0, ItemTouchHelper.LEFT);
        this.listener = listener;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            final View visibleView = ((ConversationListAdapter.ConversationHolder) viewHolder).getVisibleView();
            getDefaultUIUtil().onSelected(visibleView);
        }
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {

        final View visibleView = ((ConversationListAdapter.ConversationHolder) viewHolder).getVisibleView();

        getDefaultUIUtil().onDrawOver(c, recyclerView, visibleView, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final View visibleView = ((ConversationListAdapter.ConversationHolder) viewHolder).getVisibleView();
        getDefaultUIUtil().clearView(visibleView);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {

        final View visibleView = ((ConversationListAdapter.ConversationHolder) viewHolder).getVisibleView();

        getDefaultUIUtil().onDraw(c, recyclerView, visibleView, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        ConversationListAdapter.ConversationHolder holder = (ConversationListAdapter.ConversationHolder) viewHolder;
        listener.onConversationSwiped(viewHolder.getAdapterPosition(), holder.getConversation());
    }

}