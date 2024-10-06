package diatar.eu.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.ListView;
import android.content.Context;

import java.util.ArrayList;

import diatar.eu.TxTar;

public class SelectForDownload {
    private final Context mCtx;
    private final DownloadDtxList mDtxLst;
    private boolean[] mChk;

    public SelectForDownload(Context mainctx, DownloadDtxList dtxlst) {
        mCtx=mainctx;
        mDtxLst=dtxlst;
    }
    public boolean Run() {
        if (mDtxLst.fparams==null) return false;
        int len = mDtxLst.fparams.length;
        if (len<=0) return false;
        String[] fn = new String[len+1];
        mChk = new boolean[len+1];
        fn[0]="[mind / egy se]";
        mChk[0]=true;
        TxTar Dtx = TxTar.Get();
        SharedPreferences sp = mCtx.getSharedPreferences("dtxs",Context.MODE_PRIVATE);
        boolean vanmitletolteni=false;
        for (int i=0; i<len; i++) {
            String fname = mDtxLst.fparams[i].fname();
            String fdate = mDtxLst.fparams[i].timestamp();
            fn[i+1]=fname;
            String fd = sp.getString(fname,"");
            mChk[i+1]=!fd.equals("X") && (!Dtx.hasFile(fname) || !fd.equals(fdate));
            if (mChk[i+1]) vanmitletolteni=true; else mChk[0]=false;
        }
        if (!vanmitletolteni) return false;

        Builder bld = new Builder(mCtx);
        bld.setPositiveButton("Letöltés", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int id) {
                //ok
                afterDlg();
            }});
        bld.setNegativeButton("Mégsem", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int id) {
                //cancel
            }});
        bld.setMultiChoiceItems(fn,mChk,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dlg, int ix, boolean on) {
                        AlertDialog ad = (AlertDialog)dlg;
                        ListView lv = ad.getListView();
                        if (ix>0) {
                            boolean b = true;
                            for (int i=1; i<mChk.length; i++) b = b && mChk[i];
                            mChk[0]=b;
                            lv.setItemChecked(0,b);
                        } else {
                            for (int i=1; i<mChk.length; i++)
                                if (mChk[i]!=on) {
                                    mChk[i]=on;
                                    lv.setItemChecked(i,on);
                                }
                        }
                    }
                });
        AlertDialog dlg = bld.create();
        dlg.setTitle("Énektár frissítések elérhetők");
        dlg.show();

        return true;
    }

    private void afterDlg() {
        int cnt=0;
        for (int i=1; i<mChk.length; i++) if (mChk[i]) cnt++;
        if (cnt<=0) {
            TxTar.Get().OkBox(mCtx,"Nem maradt letöltendő!","Internet");
            return;
        }
        DownloadFiles df = new DownloadFiles(mCtx);
        df.fnames = new ArrayList<String>(); df.fdates = new ArrayList<String>();
        for (int i=1; i<mChk.length; i++) {
            if (!mChk[i]) continue;
            df.fnames.add(mDtxLst.fparams[i-1].fname());
            df.fdates.add(mDtxLst.fparams[i-1].timestamp());
        }
        df.verbose=true;    //??
        df.execute();
    }
}
