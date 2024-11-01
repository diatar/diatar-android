package diatar.eu;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.content.*;
import diatar.eu.edlst.*;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public class EditActivity extends Activity
	implements PopupMenu.OnMenuItemClickListener
{
	private EdLstAdapter mLstAdapter;
	private RecyclerView mLst;
	private ItemTouchHelper mTouchHelper;
	
	@Override
    protected void onCreate(Bundle bd)
    {
        super.onCreate(bd);

		setContentView(R.layout.edit);
		setMyTitle();
		
		mLst=findViewById(R.id.edLst);
		mLst.setHasFixedSize(true);
		mLst.setLayoutManager(new LinearLayoutManager(this));
		mLstAdapter=new EdLstAdapter(this);
		mLst.setAdapter(mLstAdapter);
		ItemTouchHelper.Callback cb = new EdLstCallback(mLstAdapter);
		mTouchHelper = new ItemTouchHelper(cb);
		mTouchHelper.attachToRecyclerView(mLst);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inf = getMenuInflater();
		inf.inflate(R.menu.edoptionsmenu,menu);

		return true;
	}
	
	void setMyTitle() {
		setTitle("DIA: "+(G.sDiaFname==null || G.sDiaFname.isEmpty() ? "új" : G.sDiaFname));
	}
	
	public void onClose(View v) {
		setResult(RESULT_OK);
		finish();
	}
	
	public void onSave(View v) {
		doSave();
	}
	
	public void onTouchOrder(RecyclerView.ViewHolder vh) {
		mTouchHelper.startDrag(vh);
	}
	
	///////////////////
	
	static private int actitem;
	
	private static final int REQUEST_ADD_DTX = 201;
	private static final int REQUEST_ADD_MANUAL = 202;
	private static final int REQUEST_ADD_SEP = 203;
	private static final int REQUEST_ED_DTX = 204;
	private static final int REQUEST_ED_MANUAL = 205;
	private static final int REQUEST_ED_SEP = 206;
	private static final int REQUEST_FILESAVE = 207;
	private static final int REQUEST_ADD_PIC = 208;
	private static final int REQUEST_ED_PIC = 209;
	
	private static final int REQUEST_COMMONPROP = 1001;
	private static final int REQUEST_PROP = 1002;
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()) {
			case R.id.mnCommonProps:
				optCommonProps();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		Intent it;
		switch(item.getItemId()) {
		case R.id.mnEdAddDtx:
			it = new Intent(this, EdDtx.class);
			startActivityForResult(it,REQUEST_ADD_DTX);
			return true;
		case R.id.mnEdAddManual:
			it = new Intent(this, EdManualText.class);
			startActivityForResult(it,REQUEST_ADD_MANUAL);
			return true;
		case R.id.mnEdAddSep:
			it = new Intent(this, EdSep.class);
			startActivityForResult(it,REQUEST_ADD_SEP);
			return true;
		case R.id.mnEdAddPic:
			it = new Intent(this, FileSelectorActivity.class);
			it.putExtra(G.idDIR,G.sPicDir);
			it.putExtra(G.idFTYPE,FileSelectorActivity.ftPIC);
			startActivityForResult(it,REQUEST_ADD_PIC);
			return true;
		case R.id.mnEdLongDel:
			posDel(actitem);
			return true;
		case R.id.mnEdLongProp:
			DiaItem d = DiaItem.getByPos(actitem);
			it = new Intent(this,SetProj.class);
			Bundle bd = new Bundle();
			d.mProps.toBundle(bd,0);
			it.replaceExtras(bd);
			startActivityForResult(it,REQUEST_PROP);
			return true;
		}
		return false;
	}
	
	/////////////////
	
	private void optCommonProps() {
		Intent it = new Intent(this,SetProj.class);
		DiaProp dp = DiaItem.getCommonProps();
		Bundle bd = new Bundle();
		dp.toBundle(bd,0);
		it.replaceExtras(bd);
		startActivityForResult(it,REQUEST_COMMONPROP);
	}
	
	/////////////////
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode==REQUEST_ADD_SEP) reqAddSep(resultCode, data);
		if (requestCode==REQUEST_ADD_DTX) reqAddDtx(resultCode, data);
		if (requestCode==REQUEST_ADD_MANUAL) reqAddManual(resultCode, data);
		if (requestCode==REQUEST_ADD_PIC) reqAddPic(resultCode, data);
		if (requestCode==REQUEST_ED_DTX) reqEdDtx(resultCode, data);
		if (requestCode==REQUEST_ED_MANUAL) reqEdManual(resultCode, data);
		if (requestCode==REQUEST_ED_SEP) reqEdSep(resultCode, data);
		if (requestCode==REQUEST_ED_PIC) reqEdPic(resultCode, data);
		if (requestCode==REQUEST_FILESAVE) reqFilesave(resultCode, data);
		if (requestCode==REQUEST_COMMONPROP) reqCommonprop(resultCode, data);
		if (requestCode==REQUEST_PROP) reqProp(resultCode, data);
	}
	
	private void reqAddSep(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		String s = data.getStringExtra(G.idTXT);
		if (s.isEmpty()) return;
		DiaItem dact = DiaItem.getByPos(actitem);
		DiaItem dnew = new DiaItem(DiaItem.ditSEPAR);
		dnew.mKnev=s;
		dnew.InsertMe(dact);
		mLstAdapter.notifyDataSetChanged();
		mLst.invalidate();
	}
	
	private void reqAddDtx(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		int dx = data.getIntExtra(G.idDTXINDEX,0);
		int ex = data.getIntExtra(G.idENEKINDEX,0);
		boolean[] diak = data.getBooleanArrayExtra(G.idSEL);
		TxTar Dtx = TxTar.Get();
		String knev = Dtx.getDtxLst()[dx].title();
		String enev = Dtx.getEnekLst(this,dx)[ex];
		String[] vnevek = Dtx.getVersszakLst(this,dx,ex);
		DiaItem dact = DiaItem.getByPos(actitem);
		
		ArrayList<String> idlst = new ArrayList<String>();
		ArrayList<Integer> vxlst = new ArrayList<Integer>();
		ArrayList<Integer> sxlst = new ArrayList<Integer>();
		TxTar.Get().getIdList(null,dx,idlst,vxlst,sxlst);
		int ididx=vxlst.size()-1;
		while (ididx>=0 && vxlst.get(ididx)!=ex) ididx--;
		
		int i = diak.length;
		while(i-->0) {
			if (!diak[i]) continue;
			DiaItem dnew = new DiaItem(DiaItem.ditDTX);
			dnew.mKnev=knev; dnew.mKotet=dx;
			dnew.mVnev=enev; dnew.mVers=ex;
			dnew.mSnev=vnevek[i]; dnew.mVszak=i;
			//id megkereses
			while (ididx>=0 && vxlst.get(ididx)==ex && sxlst.get(ididx)>i) ididx--;
			if (ididx>=0 && vxlst.get(ididx)==ex && sxlst.get(ididx)==i)
				dnew.mSid=idlst.get(ididx);
			
			//beillesztes
			dnew.InsertMe(dact);
			dact=dnew;
		}
		mLstAdapter.notifyDataSetChanged();
		mLst.invalidate();
	}
	
	private void reqEdDtx(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		DiaItem d = DiaItem.getByPos(actitem);
		if (d==null) return;
		d.mKotet=data.getIntExtra(G.idDTXINDEX,0);
		d.mVers=data.getIntExtra(G.idENEKINDEX,0);
		d.mVszak=data.getIntExtra(G.idDIAINDEX,0);
		TxTar Dtx = TxTar.Get();
		d.mKnev=Dtx.getDtxLst()[d.mKotet].title();
		d.mVnev=Dtx.getEnekLst(this,d.mKotet)[d.mVers];
		String[] vnevek = Dtx.getVersszakLst(this,d.mKotet,d.mVers);
		d.mSnev=vnevek[d.mVszak];
		mLstAdapter.notifyDataSetChanged();
	}
	
	private void reqEdSep(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		DiaItem d = DiaItem.getByPos(actitem);
		if (d==null) return;
		String s = data.getStringExtra(G.idTXT);
		if (s.isEmpty()) return;
		d.mKnev=s;
		mLstAdapter.notifyDataSetChanged();
	}
	
	private void reqAddPic(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		String fn = data.getStringExtra(G.idFNAME);
		if (fn.isEmpty()) return;
		String dn = data.getStringExtra(G.idDIR);
		if (dn.isEmpty()) return;
		DiaItem dact = DiaItem.getByPos(actitem);
		DiaItem dnew = new DiaItem(DiaItem.ditPIC);
		dnew.mKnev=dn; dnew.mVnev=fn;
		dnew.InsertMe(dact);
		mLstAdapter.notifyDataSetChanged();
		mLst.invalidate();
		G.sPicDir=dn;
		G.Save(this);
	}
	
	private void reqEdPic(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		DiaItem d = DiaItem.getByPos(actitem);
		if (d==null) return;
		String fn = data.getStringExtra(G.idFNAME);
		if (fn.isEmpty()) return;
		String dn = data.getStringExtra(G.idDIR);
		if (dn.isEmpty()) return;
		d.mKnev=dn; d.mVnev=fn;
		mLstAdapter.notifyDataSetChanged();
		G.sPicDir=dn;
		G.Save(this);
	}

	private void reqAddManual(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		String cim = data.getStringExtra(G.idCIM);
		String txt = data.getStringExtra(G.idTXT);
		if (cim==null||txt==null) return;
		DiaItem dact = DiaItem.getByPos(actitem);
		DiaItem dnew = new DiaItem(DiaItem.ditTXT);
		dnew.mKnev=cim;
		dnew.mTxt=txt.split("\n");
		dnew.InsertMe(dact);
		mLstAdapter.notifyDataSetChanged();
		mLst.invalidate();
	}

	private void reqEdManual(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		DiaItem d = DiaItem.getByPos(actitem);
		if (d==null) return;
		String cim = data.getStringExtra(G.idCIM);
		String txt = data.getStringExtra(G.idTXT);
		if (cim==null||txt==null) return;
		d.mKnev=cim;
		d.mTxt=txt.split("\n");
		mLstAdapter.notifyDataSetChanged();
	}
	
	private void reqFilesave(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		String dir=data.getStringExtra(G.idDIR);
		String fname=data.getStringExtra(G.idFNAME);
		DiaSaver ds = new DiaSaver();
		String res = ds.Save(dir+fname);
		if (res.isEmpty()) {
			res=fname+" mentve.";
			G.sLoadDir=dir;
			G.sDiaFname=FileSelectorActivity.SubDiaExt(fname);
			setMyTitle();
		}
		TxTar.Msg(this,res);
	}
	
	private void reqCommonprop(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		DiaProp dp = DiaItem.getCommonProps();
		dp.fromBundle(data.getExtras(),0);
	}
	
	private void reqProp(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		DiaItem d = DiaItem.getByPos(actitem);
		d.mProps.fromBundle(data.getExtras(),0);
	}
	
	///////////////////
	
	public boolean posDel(int p) {
		DiaItem d = DiaItem.getByPos(p);
		if (d==null) return false;
		d.RemoveMe();
		return true;
	}
	
	public boolean posAdd(int beforepos, View v) {
		actitem=beforepos;
		PopupMenu pm = new PopupMenu(this,v);
		pm.inflate(R.menu.edaddmenu);
		pm.setOnMenuItemClickListener(this);
		pm.show();
		return true;
	}
	
	public void posEd(int p) {
		actitem=p;
		DiaItem d = DiaItem.getByPos(p);
		if (d==null) return;
		if (d.mTipus==d.ditSEPAR) {
			Intent it = new Intent(this,EdSep.class);
			it.putExtra(G.idTXT,d.mKnev);
			it.putExtra(G.idINDEX,p);
			startActivityForResult(it,REQUEST_ED_SEP);
			return;
		}
		if (d.mTipus==d.ditDTX) {
			Intent it = new Intent(this,EdDtx.class);
			it.putExtra(G.idDTXINDEX,d.mKotet);
			it.putExtra(G.idENEKINDEX,d.mVers);
			it.putExtra(G.idDIAINDEX,d.mVszak);
			it.putExtra(G.idINDEX,p);
			startActivityForResult(it,REQUEST_ED_DTX);
			return;
		}
		if (d.mTipus==d.ditPIC) {
			Intent it = new Intent(this,FileSelectorActivity.class);
			it.putExtra(G.idDIR,d.mKnev);
			it.putExtra(G.idFTYPE,FileSelectorActivity.ftPIC);
			it.putExtra(G.idFNAME,d.mVnev);
			it.putExtra(G.idINDEX,p);
			startActivityForResult(it,REQUEST_ED_PIC);
			return;
		}
		if (d.mTipus== DiaItem.ditTXT) {
			Intent it = new Intent(this,EdManualText.class);
			it.putExtra(G.idCIM,d.mKnev);
			it.putExtra(G.idTXT,String.join("\n",d.mTxt));
			startActivityForResult(it,REQUEST_ED_MANUAL);
			return;
		}
	}

	public boolean posLong(int pos, View v) {
		actitem=pos;
		PopupMenu pm = new PopupMenu(this,v);
		pm.inflate(R.menu.edlongmenu);
		pm.setOnMenuItemClickListener(this);
		pm.show();
		return true;
	}
	
	protected void doSave() {
		Intent it = new Intent(this,FileSelectorActivity.class);
		it.putExtra(G.idDIR,G.sLoadDir);
		it.putExtra(G.idFNAME,G.sDiaFname);
		it.putExtra(G.idSAVEMODE,true);
		startActivityForResult(it,REQUEST_FILESAVE);
	}
	
}
