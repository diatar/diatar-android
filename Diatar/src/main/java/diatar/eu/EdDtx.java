package diatar.eu;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.content.*;
import android.widget.AdapterView.*;
import android.view.*;

import diatar.eu.utils.DtxParams;

public class EdDtx extends Activity
{
	private Spinner mDtxLst, mEnekLst;
	private ListView mDiaLst;
	private DiaAdapter mDiaAdapter;
	public boolean mMultiSelect;
	
	private int mDtxIdx, mEnekIdx, mDiaIdx;
	
	@Override
	public void onCreate(Bundle bd) {
		super.onCreate(bd);
		
		setContentView(R.layout.eddtx);
		setTitle("Énektár");
		mDtxLst=findViewById(R.id.eddtxDtxLst);
		mEnekLst=findViewById(R.id.eddtxEnekLst);
		mDiaLst=findViewById(R.id.eddtxDiaLst);
		
		if (bd==null) {
			Intent it = getIntent();
			mDtxIdx=it.getIntExtra(G.idDTXINDEX,0);
			mEnekIdx=it.getIntExtra(G.idENEKINDEX,0);
			mDiaIdx=it.getIntExtra(G.idDIAINDEX,-1);
		} else {
			mDtxIdx=bd.getInt(G.idDTXINDEX);
			mEnekIdx=bd.getInt(G.idENEKINDEX);
			mDiaIdx=bd.getInt(G.idDIAINDEX,-1);
		}
		mMultiSelect=(mDiaIdx<0);
		
		fillDtxLst();
		mDtxLst.setSelection(mDtxIdx);
		fillEnekLst();
		mEnekLst.setSelection(mEnekIdx);
		fillDiaLst();
		if (bd!=null && mMultiSelect) {
			for (int i=0; i<mDiaAdapter.getCount(); i++)
				mDiaAdapter.setSelected(i,bd.getBoolean(G.idSEL+i,false));
		} else {
			mDiaAdapter.setSelected(mDiaIdx,true);
		}
		
		mDtxLst.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View v, int pos, long id) {
					int p=mDtxLst.getSelectedItemPosition();
					if (p==mDtxIdx) return;
					mDtxIdx=p; mEnekIdx=0;
					fillEnekLst();
					fillDiaLst();
				}
				public void onNothingSelected(AdapterView<?> arg0) {}
			});
		mEnekLst.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View v, int pos, long id) {
					int p=mEnekLst.getSelectedItemPosition();
					if (p==mEnekIdx) return;
					mEnekIdx=p;
					fillDiaLst();
				}
				public void onNothingSelected(AdapterView<?> arg0) {}
			});
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(G.idDTXINDEX,mDtxIdx);
		outState.putInt(G.idENEKINDEX,mEnekIdx);
		if (mMultiSelect)
			for (int i=0; i<mDiaAdapter.getCount(); i++) {
				if (mDiaAdapter.isSelected(i))
					outState.putBoolean(G.idSEL+i,true);
			}
		else
			outState.putInt(G.idDIAINDEX,mDiaIdx);
	}
	
	public void onCancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	public void onOk(View v) {
		finishOk(mDiaIdx);
	}
	
	public void finishOk(int selpos) {
		Intent it = new Intent();
		it.putExtra(G.idDTXINDEX,mDtxIdx);
		it.putExtra(G.idENEKINDEX,mEnekIdx);
		if (mMultiSelect)
			it.putExtra(G.idSEL,mDiaAdapter.getSelArray());
		else
			it.putExtra(G.idDIAINDEX,selpos);
		setResult(RESULT_OK,it);
		finish();
	}
	
	private void fillDtxLst() {
		DtxParams[] dtxlst = TxTar.Get().getDtxLst();
		String[] namearr = new String[dtxlst.length];
		int idx=0;
		for (DtxParams p : dtxlst) namearr[idx++]=p.title();
		ArrayAdapter<String> adp =
			new ArrayAdapter<String>(
				this,
				android.R.layout.simple_spinner_item,
				namearr
			);
		mDtxLst.setAdapter(adp);
	}
	
	private void fillEnekLst() {
		ArrayAdapter<String> adp =
			new ArrayAdapter<String>(
				this,
				android.R.layout.simple_spinner_item,
				TxTar.Get().getEnekLst(this,mDtxIdx)
			);
		mEnekLst.setAdapter(adp);
	}
	
	private void fillDiaLst() {
		TxTar Dtx = TxTar.Get();
		String[] cim = Dtx.getVersszakLst(this,mDtxIdx,mEnekIdx);
		String[] sor = new String[cim.length];
		for (int i=0; i<sor.length; i++) {
			String[] xxx = Dtx.getDiaTxt(this,mDtxIdx,mEnekIdx,i);
			sor[i]="("+Dtx.deFormat(xxx[0])+")";
		}
		
		mDiaAdapter = new DiaAdapter(this,cim,sor);
		mDiaLst.setAdapter(mDiaAdapter);
		mDiaLst.setOnItemClickListener(mDiaAdapter);
	}
	
	////////////////
	
	private class DiaAdapter extends BaseAdapter implements OnItemClickListener
	{
		private String[] mCim, mSor;
		private boolean[] mSel;
		private EdDtx mCtx;
		
		public DiaAdapter(EdDtx ctx, String[] cim, String[] sor) {
			super();
			mCtx=ctx; mCim=cim; mSor=sor;
			mSel = new boolean[mCim.length];
		}
		
		public boolean isSelected(int pos) {
			if (pos<0 || pos>=mSel.length) return false;
			return mSel[pos];
		}
		
		public void setSelected(int pos, boolean sel) {
			if (pos>=0 && pos <mSel.length) mSel[pos]=sel;
			//mDiaLst.invalidate();
		}
		
		public boolean[] getSelArray() { return mSel; }
		
		public int getCount() {
			return mCim.length;
		}

		public Object getItem(int arg) {
			return null;
		}

		public long getItemId(int pos) {
			return pos;
		}

		public View getView(int pos, View cv, ViewGroup parent) {
			if (cv==null) {
				LayoutInflater li = (LayoutInflater)mCtx.getSystemService(mCtx.LAYOUT_INFLATER_SERVICE);
				cv = li.inflate(R.layout.eddtxitem,parent,false);
			}
			
			TextView tv = cv.findViewById(R.id.eddtxSel);
			setCheck(tv,isSelected(pos));
			
			tv=cv.findViewById(R.id.eddtxCim);
			tv.setText(mCim[pos]);
			tv=cv.findViewById(R.id.eddtxSor);
			tv.setText(mSor[pos]);

			return cv;
		}
		
		private void setCheck(TextView tv, boolean chk) {
			if (chk) {
				tv.setText("\u2713");
				tv.setTextColor(0xFF007F00);
			} else {
				tv.setText("\u25fd");
				tv.setTextColor(0xFF000000);
			}
		}
		
		@Override
		public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
			if (!mCtx.mMultiSelect) {
				mCtx.finishOk(pos);
				return;
			}
			boolean b = !isSelected(pos);
			setSelected(pos,b);
			TextView tv = v.findViewById(R.id.eddtxSel);
			setCheck(tv,b);
		}
	}
}
