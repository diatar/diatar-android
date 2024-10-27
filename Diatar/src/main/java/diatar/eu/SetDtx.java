package diatar.eu;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import diatar.eu.utils.DownloadDtxList;
import diatar.eu.utils.DownloadFiles;
import diatar.eu.utils.DtxParams;

class DtxLstItem {
    public boolean mIsChecked;
    public boolean mIsDeleted;
    public boolean mIsGroup;
    public boolean mIsDeletable;
    public String mText;
    public String mFName;
    public String mFDate;
}

public class SetDtx  extends Activity {

    public final static String idFNAME = "FName";
    public final static String idFDATE = "FDate";

    private static final int REQUEST_IMPORT = 110;
    private RecyclerView mDtxLst;
    private DtxLstAdapter mDtxAdapter;
    private TextView mWaitLbl;

    private DownloadDtxList mDDL;
    private ArrayList<DtxLstItem> mDtxArr;

    @Override
    protected void onCreate(Bundle bd) {
        super.onCreate(bd);

        setContentView(R.layout.setdtx);
        setTitle("Énekrendek beállítása");
        mDtxLst=findViewById(R.id.setdtxLst);
        mWaitLbl=findViewById(R.id.setdtxWait);

        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mDtxLst.setLayoutManager(lm);

        SharedPreferences sp = getSharedPreferences("dtxs", Context.MODE_PRIVATE);
        mDtxArr = new ArrayList<>();
        DtxLstItem dli = new DtxLstItem();
        dli.mIsChecked=false;
        dli.mIsDeleted=false;
        dli.mIsDeletable=false;
        dli.mIsGroup=true;
        dli.mText="(saját)";
        mDtxArr.add(dli);
        boolean allchecked=true;
        File dir = new File(TxTar.Get().getDtx2Dir());
        String[] fl = dir.list();
        for (String fname : fl) {
            if (fname.endsWith(".dtx")) {
                DtxParams par=null;
                try {
                    par=DtxParams.calcParamsOf(new File(dir, fname));
                } catch (Exception e) {
                    TxTar.Msg(this, "Err: "+e.getLocalizedMessage());
                    continue;
                }
                DtxLstItem childitem = new DtxLstItem();
                childitem.mIsChecked=!sp.getString(par.fname(),"").equals("X");
                childitem.mIsDeleted=false;
                childitem.mIsDeletable=true;
                childitem.mIsGroup=false;
                childitem.mText=par.title();
                childitem.mFName=par.fname();
                allchecked=allchecked && childitem.mIsChecked;
                mDtxArr.add(childitem);
            }
        }
        dli.mIsChecked=allchecked;

        mDtxAdapter = new DtxLstAdapter(mDtxArr);
        mDtxLst.setAdapter(mDtxAdapter);
        mDtxLst.setEnabled(false);

        mDDL = new DownloadDtxList(this, false) {
            @Override
            public void Finished() {
                FillDtxLst();
            }
        };
        mDDL.execute();
    }

    public void FillDtxLst() {
        if (mDDL.fparams==null) {
            mWaitLbl.setText("Hálózati hiba...");
            return;
        }

        SharedPreferences sp = getSharedPreferences("dtxs", Context.MODE_PRIVATE);
        String prevgroup="\n";  //ez biztos nem lehet
        DtxLstItem dli, grpdli=null;
        boolean allchecked=true;
        for (DtxParams dp : mDDL.fparams) {
            if (!dp.group().equals(prevgroup)) {
                if (grpdli!=null) grpdli.mIsChecked=allchecked;
                grpdli = new DtxLstItem();
                grpdli.mIsChecked=false;
                grpdli.mIsDeleted=false;
                grpdli.mIsDeletable=false;
                grpdli.mIsGroup=true;
                grpdli.mText=dp.group();
                prevgroup=dp.group();
                mDtxArr.add(grpdli);
                mDtxAdapter.notifyItemInserted(mDtxArr.size()-1);
                allchecked=true;
            }
            dli = new DtxLstItem();
            dli.mIsChecked=!sp.getString(dp.fname(),"").equals("X");;
            dli.mIsDeleted=false;
            dli.mIsDeletable=false;
            dli.mIsGroup=false;
            dli.mText=dp.title();
            dli.mFName=dp.fname();
            dli.mFDate=dp.timestamp();
            mDtxArr.add(dli);
            mDtxAdapter.notifyItemInserted(mDtxArr.size()-1);
            allchecked=allchecked && dli.mIsChecked;
        }
        if (grpdli!=null) grpdli.mIsChecked=allchecked;
        mDtxLst.setEnabled(true);

        //mDtxLst.setVisibility(View.VISIBLE);
        mDtxLst.invalidate();
        mWaitLbl.setVisibility(View.GONE);
    }

    public void onImport(View v) {
        Intent it = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        it.addCategory(Intent.CATEGORY_OPENABLE);
        it.setType("*/*");
        startActivityForResult(it, REQUEST_IMPORT);
    }

    public void onOk(View v) {
        int idx = 0;
        Intent it = new Intent();
        SharedPreferences sp = getSharedPreferences("dtxs", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        File dir = new File(TxTar.Get().getDtx2Dir());
        for (DtxLstItem dli : mDtxArr) {
            if (dli.mIsGroup) continue;
            if (dli.mIsDeleted) {
                ed.remove(dli.mFName);
                File f = new File(dir, dli.mFName);
                f.delete();
                continue;
            }
            if (!dli.mIsChecked) {
                ed.putString(dli.mFName, "X");
                //TODO: delete common dtx file
                continue;
            }
            if (dli.mIsDeletable) continue;  //sajat fajl, nem kell letolteni
            if (TxTar.Get().hasFile(dli.mFName) && sp.getString(dli.mFName,"").equals(dli.mFDate)) continue;
            String numstr = Integer.toString(++idx);
            it.putExtra(idFNAME+numstr, dli.mFName);
            it.putExtra(idFDATE+numstr, dli.mFDate);
        }
        ed.commit();

        setResult(RESULT_OK, it);
        finish();
    }

    public void onCancel(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_IMPORT || data == null) return;
        Uri uri = data.getData();
        String fname = "???";
        //String fpath = "???";
        Cursor cr = getContentResolver().query(uri,null,null,null,null,null);
        try {
            if (cr!=null && cr.moveToFirst()) {
                //fpath = cr.getString(0); //cr.getString(cr.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                int idx = cr.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx>=0) fname = cr.getString(idx);
            }
        } finally {
            cr.close();
        }

        if (!fname.toLowerCase().endsWith(".dtx")) {
            TxTar.Msg(this, fname+"\nNem .DTX kiterjesztésű fájl!");
            return;
        }
        File destfile = new File(TxTar.Get().getDtx2Dir(), fname);
        BufferedInputStream bin = null;
        BufferedOutputStream bout = null;
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
        try {
            //FileInputStream sin = new FileInputStream(FileChooser.getPath(this, uri));
            InputStream sin = getContentResolver().openInputStream(uri);
            FileOutputStream sout = new FileOutputStream(destfile);
            bin = new BufferedInputStream(sin);
            bout = new BufferedOutputStream(sout);
            byte[] buf = new byte[1024];
            while (bin.read(buf) != -1) bout.write(buf);
        } catch (Exception e) {
            TxTar.Msg(this, e.getLocalizedMessage());
            return;
        } finally {
            try {
                if (bin != null) bin.close();
                if (bout != null) bout.close();
            } catch (Exception e) {
                TxTar.Msg(this, e.getLocalizedMessage());
            }
        }

        DtxParams par=null;
        try {
            par=DtxParams.calcParamsOf(destfile);
        } catch(Exception e) {
            TxTar.Msg(this, e.getLocalizedMessage());
        }
        if (par==null || par.title().isEmpty()) {
            TxTar.Msg(this, fname+"\nA fájl nem tűnik énektárnak.");
            return;
        }
        int idx=1;
        boolean uj=true;
        while (idx<mDtxArr.size()) {
            DtxLstItem r = mDtxArr.get(idx);
            if (r.mIsGroup) break;
            if (r.mFName.equals(fname)) {
                uj=false;
                break;
            }
        }
        if (uj) {
            DtxLstItem childitem = new DtxLstItem();
            childitem.mIsChecked = true;
            childitem.mIsDeleted = false;
            childitem.mIsDeletable = true;
            childitem.mIsGroup = false;
            childitem.mText = par.title();
            childitem.mFName = par.fname();
            mDtxArr.add(idx, childitem);
            mDtxAdapter.notifyItemInserted(idx);
        } else {
            DtxLstItem r = mDtxArr.get(idx);
            r.mIsDeleted=false;
            r.mIsChecked=true;
            mDtxAdapter.notifyItemChanged(idx);
        }
        mDtxLst.scrollToPosition(idx);
    }
}

class DtxLstAdapter extends RecyclerView.Adapter<DtxLstAdapter.ViewHolder> {

    public static ArrayList<DtxLstItem> localDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox mCheckbox;
        private final TextView mTxt;
        private final TextView mTrash;

        public ViewHolder(View v) {
            super(v);
            mCheckbox = v.findViewById(R.id.setdtxCheckbox);
            mTxt = v.findViewById(R.id.setdtxText);
            mTrash = v.findViewById(R.id.setdtxTrash);
        }

        public boolean IsChecked() { return mCheckbox.isChecked(); }
        public void setChecked(boolean checked) {
            mCheckbox.setChecked(checked);
            if (checked) setDeleted(false);
        }
        public boolean IsDeleted() { return (mCheckbox.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) != 0; }
        public void setDeleted(boolean deleted) {
            if (deleted) {
                mTxt.setPaintFlags(mCheckbox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                setChecked(false);
            } else {
                mTxt.setPaintFlags(mCheckbox.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
            //mTxt.invalidate();
        }
        public boolean IsGroup() {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)mCheckbox.getLayoutParams();
            return (lp.leftMargin == 0);
        }
        public void setGroup(boolean group) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)mCheckbox.getLayoutParams();
            lp.leftMargin = (group ? 0 : 50);
            mCheckbox.setLayoutParams(lp);
            mTxt.setTypeface(null, group ? Typeface.BOLD : Typeface.NORMAL);
        }
        public boolean isDeletable() {
            return (mTrash.getVisibility()==View.VISIBLE);
        }
        public void setDeletable(boolean deletable) {
            mTrash.setVisibility(deletable ? View.VISIBLE : View.GONE);
        }
    }

    public DtxLstAdapter(ArrayList<DtxLstItem> dataset) {
        localDataset=dataset;
    }

    public void onCheckboxClick(ViewHolder vh) {
        boolean checked = vh.mCheckbox.isChecked();
        int idx = vh.getAdapterPosition(), cnt = localDataset.size();
        DtxLstItem item = localDataset.get(idx);
        if (item.mIsChecked==checked) return;
        item.mIsChecked=checked;
        if (checked) item.mIsDeleted=false;
        notifyItemChanged(idx);
        if (item.mIsGroup) {
            while (++idx < cnt) {
                item = localDataset.get(idx);
                if (item.mIsGroup) break;
                if (item.mIsChecked != checked) {
                    item.mIsChecked = checked;
                    if (checked) item.mIsDeleted = false;
                    notifyItemChanged(idx);
                }
            }
            return;
        }
        //this is a child, modify group item
        int groupidx = idx;
        DtxLstItem groupitem;
        boolean allchecked=checked;
        //go back to group
        while(true) {
            if (--groupidx < 0) return; //no group???
            groupitem = localDataset.get(groupidx);
            if (groupitem.mIsGroup) break;
            allchecked=allchecked && groupitem.mIsChecked;
        }
        //go forward to end of group
        while (allchecked && ++idx<cnt) {
            item=localDataset.get(idx);
            if (item.mIsGroup) break;
            allchecked=allchecked && item.mIsChecked;
        }
        if (groupitem.mIsChecked!=allchecked) {
            groupitem.mIsChecked=allchecked;
            notifyItemChanged(groupidx);
        }
    }

    public void onTrashClick(ViewHolder vh) {
        int idx = vh.getAdapterPosition();
        DtxLstItem item = localDataset.get(idx);
        item.mIsDeleted=!item.mIsDeleted;
        boolean groupcanchange=false;
        if (item.mIsDeleted) {
            item.mIsChecked=false;
            groupcanchange=true;
        }
        notifyItemChanged(idx);
        if (!groupcanchange) return;
        while (true) {
            if (--idx<0) return; //no group???
            item = localDataset.get(idx);
            if (item.mIsGroup) break;
        }
        if (!item.mIsChecked) return; //no change
        item.mIsChecked=false;
        notifyItemChanged(idx);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.setdtxitem, viewGroup, false);
        //ViewGroup.LayoutParams lp = view.getLayoutParams();
        //lp.width = viewGroup.getMeasuredWidth();
        //lp.height = (int)(48* viewGroup.getContext().getResources().getDisplayMetrics().density + 0.5f);
        ViewHolder vh = new ViewHolder(view);
        vh.mCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckboxClick(vh);
            }
        });
        vh.mTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTrashClick(vh);
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        DtxLstItem dli = localDataset.get(position);
        viewHolder.setChecked(dli.mIsChecked);
        viewHolder.mTxt.setText(dli.mText);
        viewHolder.setGroup(dli.mIsGroup);
        viewHolder.setDeleted(dli.mIsDeleted);
        viewHolder.setDeletable(dli.mIsDeletable);
    }

    @Override
    public int getItemCount() {
        return localDataset.size();
    }
}
