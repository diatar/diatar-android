package diatar.eu.edlst;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class EdLstCallback extends ItemTouchHelper.Callback
{
	private EdLstAdapter mAdapter;
	
	public EdLstCallback(EdLstAdapter adapter) {
		mAdapter=adapter;
	}
	
	@Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }
	
	@Override
    public int getMovementFlags(RecyclerView rv, RecyclerView.ViewHolder vh) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }
	
	@Override
    public boolean onMove(RecyclerView rv,
						  RecyclerView.ViewHolder source, 
						  RecyclerView.ViewHolder target) {
        mAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder vh, int dir) {
        mAdapter.onItemDismiss(vh.getAdapterPosition());
    }
	
	@Override
	public void onSelectedChanged(RecyclerView.ViewHolder vh, int action) {
		super.onSelectedChanged(vh,action);
		if (action==ItemTouchHelper.ACTION_STATE_IDLE) return;
		mAdapter.onSelected(vh,true);
	}
	
	@Override
	public void clearView(RecyclerView rv, RecyclerView.ViewHolder vh) {
		super.clearView(rv,vh);
		mAdapter.onSelected(vh,false);
	}
}
