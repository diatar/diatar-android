package diatar.eu.edlst;

import android.content.ClipData;
import android.widget.*;
import android.view.*;
import android.view.View.*;
import diatar.eu.*;
import android.graphics.*;

import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.RecyclerView;

public class EdLstAdapter extends RecyclerView.Adapter<EdLstAdapter.MyViewHolder>
{
	private static final String txtDBLUP = "\u2517";	//kerek: 239D, szogletes: 23A3, tabla: 2517
	private static final String txtDBLDN = "\u250F";	//kerek: 239B, szogletes: 23A1, tabla: 250F

	public static class MyViewHolder extends RecyclerView.ViewHolder {
		int mPos;
		DiaItem mDia;
		TextView mTvAdd, mTvDbl, mTvTxt, mTvOrder;
		CheckBox mCb;
		View mView;
		
		public MyViewHolder(View v) {
			super(v);
			mView=v;
			mTvAdd=v.findViewById(R.id.editemAdd);
			mTvDbl=v.findViewById(R.id.editemDbl);
			mTvTxt=v.findViewById(R.id.editemTxt);
			mTvOrder=v.findViewById(R.id.editemOrder);
			mCb=v.findViewById(R.id.editemSel);
		}
	}
	
	private EditActivity mCtx;

	private OnClickListener mAddClickListener =
	new OnClickListener() {
		@Override public void onClick(View v) { onAddAfter(v); }
	};
	private OnClickListener mSelClickListener =
	new OnClickListener() {
		@Override public void onClick(View v) { onSel(v); }
	};
	private OnClickListener mTxtClickListener =
	new OnClickListener() {
		@Override public void onClick(View v) { onEd(v); }
	};
	private OnClickListener mDblClickListener =
	new OnClickListener() {
		@Override public void onClick(View v) { onDbl((TextView)v); }
	};
	private OnLongClickListener mLongClickListener =
	new OnLongClickListener() {
		@Override public boolean onLongClick(View v) { onLong(v); return true; }
	};

	public EdLstAdapter(EditActivity ctx) {
		mCtx=ctx;
		deselectAll();
	}
	
	@Override
	public EdLstAdapter.MyViewHolder onCreateViewHolder(ViewGroup p, int vtype) {
		View v = LayoutInflater.from(p.getContext())
			.inflate(R.layout.edlstitem, p, false);
		MyViewHolder vh = new MyViewHolder(v);
		return vh;
	}

	@Override
	public int getItemCount() {
		int res = DiaItem.getCount();
		if (res<=0) res=1;
		return res;
	}

	@Override
	public void onBindViewHolder(final MyViewHolder vh, int pos) {
		DiaItem d = DiaItem.getByPos(pos);
		vh.mView.setTag((Integer)pos);
		vh.mPos=pos;
		boolean first = (vh.mDia==null);
		vh.mDia=d;
		if (d==null) {
			vh.mTvAdd.setVisibility(View.INVISIBLE);
			vh.mTvOrder.setVisibility(View.INVISIBLE);
			vh.mCb.setVisibility(View.INVISIBLE);
			vh.mTvDbl.setVisibility(View.INVISIBLE);
			vh.mTvTxt.setText("Üres lista. Katt ide!");
			vh.mView.setOnClickListener(mAddClickListener);
			return;
		}
		
		if (first) {
			vh.mTvAdd.setVisibility(View.VISIBLE);
			vh.mTvDbl.setVisibility(View.VISIBLE);
			vh.mTvOrder.setVisibility(View.VISIBLE);
			vh.mCb.setVisibility(View.VISIBLE);
			vh.mView.setOnClickListener(null);
			vh.mTvAdd.setOnClickListener(mAddClickListener);
			vh.mTvDbl.setOnClickListener(mDblClickListener);
			vh.mTvTxt.setOnClickListener(mTxtClickListener);
			vh.mTvTxt.setOnLongClickListener(mLongClickListener);
			vh.mTvOrder.setOnTouchListener(
			new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent evt) {
					if (MotionEventCompat.getActionMasked(evt) == MotionEvent.ACTION_DOWN)
						mCtx.onTouchOrder(vh);
					return false;
				}
				});
			vh.mCb.setChecked(d.mEdSelected);
			vh.mCb.setOnClickListener(mSelClickListener);
		}
		vh.mTvTxt.setText(d.getDiaTitle(false));
		if (d.mProps.mDblDia) {
			vh.mTvDbl.setText(txtDBLDN);
		} else {
			d=DiaItem.getByPos(pos-1);
			if (d!=null && d.mProps.mDblDia)
				vh.mTvDbl.setText(txtDBLUP);
			else
				vh.mTvDbl.setText("");
		}

		return;
	}
	
	//////////////////
	
	public void deselectAll() {
		DiaItem d = DiaItem.sFirst;
		while (d!=null) {
			d.mEdSelected=false;
			d=d.mNext;
		}
	}
	
	////////////////////
	
	private int getViewPos(View v) {
		View p=(View)(v.getParentForAccessibility());
		Integer I = (Integer)p.getTag();
		return (I==null ? 0 : I);
	}
	
	//public void onAddBefore(View v) {
	//	deselectAll();
	//	int p=getViewPos(v);
	//	mCtx.posAdd(p,v);
	//}
	
	public void onAddAfter(View v) {
		deselectAll();
		int p=getViewPos(v);
		mCtx.posAdd(p+1,v);
	}
	
	public void onSel(View v) {
		int p = getViewPos(v);
		DiaItem d = DiaItem.getByPos(p);
		if (d!=null) {
			d.mEdSelected=!d.mEdSelected;
			CheckBox cb = (CheckBox)v;
			cb.setChecked(d.mEdSelected);
		}
	}
	
	public void onEd(View v) {
		int p = getViewPos(v);
		mCtx.posEd(p);
	}

	public void onDbl(TextView v) {
		int p = getViewPos(v);
		DiaItem d = DiaItem.getByPos(p);
		if (d==null) return;	//to be safe
		if (d.mProps.mDblDia) {
			d.mProps.mDblDia=false;
			notifyItemChanged(p);
			if (p+1<getItemCount()) notifyItemChanged(p+1);
		} else if (p+1<getItemCount()) {
			if (p>0) {
				DiaItem dprev = DiaItem.getByPos(p-1);
				if (dprev==null) return;
				if (dprev.mProps.mDblDia) {
					dprev.mProps.mDblDia=false;
					notifyItemChanged(p);
					notifyItemChanged(p-1);
					return;
				}
			}
			DiaItem dnext = DiaItem.getByPos(p+1);
			if (dnext==null || dnext.mProps.mDblDia) return;
			d.mProps.mDblDia=true;
			notifyItemChanged(p);
			notifyItemChanged(p+1);
		}
	}

	public void onLong(View v) {
		int p = getViewPos(v);
		mCtx.posLong(p,v);
	}
	
	public void onItemDismiss(int pos) {
		DiaItem d = DiaItem.getByPos(pos);
		if (d==null) {
			notifyItemChanged(pos);
			return;
		}
		if (pos>0) {
			DiaItem dprev = DiaItem.getByPos(pos-1);
			if (dprev!=null && dprev.mProps.mDblDia) {
				dprev.mProps.mDblDia=false;
				notifyItemChanged(pos-1);
			}
		}
		if (d.mProps.mDblDia && pos+1<getItemCount()) {
			d.mProps.mDblDia=false;
			notifyItemChanged(pos + 1);
		}
		d.KillMe();
		notifyItemRemoved(pos);
	}
	
	public void onItemMove(int frompos, int topos) {
		DiaItem d = DiaItem.getByPos(frompos);
		if (d==null) return;
		if (d.mProps.mDblDia) {
			d.mProps.mDblDia=false;
			notifyItemChanged(frompos);
			if (frompos+1<getItemCount()) notifyItemChanged(frompos+1);
		} else if (frompos>0) {
			DiaItem dprev = DiaItem.getByPos(frompos-1);
			if (dprev!=null && dprev.mProps.mDblDia) {
				dprev.mProps.mDblDia=false;
				notifyItemChanged(frompos-1);
				notifyItemChanged(frompos);
			}
		}
		DiaItem dto = DiaItem.getByPos(topos);
		if (dto!=null && dto.mProps.mDblDia) {
			dto.mProps.mDblDia=false;
			notifyItemChanged(topos);
			if (topos+1<getItemCount()) notifyItemChanged(topos+1);
		} else if (topos>0) {
			DiaItem dprev = DiaItem.getByPos(topos-1);
			if (dprev!=null && dprev.mProps.mDblDia) {
				dprev.mProps.mDblDia=false;
				notifyItemChanged(topos-1);
				notifyItemChanged(topos);
			}
		}
		d.MoveMe(topos);
		notifyItemMoved(frompos,topos);
	}
	
	public void onSelected(RecyclerView.ViewHolder vh, boolean selected) {
		if (vh instanceof MyViewHolder) {
			View v = ((MyViewHolder)vh).mView;
			if (selected)
				v.setBackgroundColor(Color.LTGRAY);
			else
				v.setBackgroundColor(0);
		}
	}
}
