package diatar.eu;

import java.util.*;
import android.content.*;
import android.widget.*;
import java.io.*;
import android.app.*;
import android.os.*;

import diatar.eu.utils.DtxParams;

public class TxTar {
	private static TxTar instance = null;

	//fajlok, dtx nevek, rovid nevek, csoport
	private DtxParams[] mDtxLst;
	//diak szama inkrementalisan:
	//  egy kotet minden enekhez az utana kov. poz.
	private Short[] scnts;
	public static int CurrKotet;
	public static String progdir, docdir, appspecdir, privatedir;
	
	private TxTar(Context ctx) { LoadNames(ctx); }

	public boolean hasFile(String fname) {
		for (DtxParams p : mDtxLst)
			if (p.fname().equals(fname)) return true;
		return false;
	}
//////////////////////

	public static TxTar Create(Context ctx) {
		if (instance==null) {
			progdir=ctx.getFilesDir().getAbsolutePath();
			File fp = new File(progdir,"private");
			fp.mkdir();
			privatedir=fp.toString();
			File f = new File(
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
				, "diatar/"
			);
			docdir=f.getAbsolutePath();
			appspecdir=ctx.getExternalFilesDir(null).toString();
			instance=new TxTar(ctx);
		}
		return instance;
	}
	
	public static TxTar Get() { return instance; }

/////////////////////

	public static void Msg(Context ctx, String txt, int msec) {
		Toast.makeText(ctx,txt,msec).show();
	}

	public static void Msg(Context ctx, String txt) { Msg(ctx,txt,Toast.LENGTH_SHORT); }

	private static AlertDialog adlg;
	
	public static void OkBox(Context ctx, String txt, String title) {
		AlertDialog.Builder bd = new AlertDialog.Builder(ctx);
		if (title!=null) bd.setTitle(title);
		bd.setMessage(txt);
		bd.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int id) {
				adlg.dismiss();
			}
		});
		adlg = bd.create();
		adlg.show();
	}
/////////////////////

	public DtxParams[] getDtxLst() { return mDtxLst; }

	public Short[] getCnts() { return scnts; }
	
	public int getCount() {
		if (scnts==null || scnts.length<=0) return 0;
		return scnts[scnts.length-1];
	}

	public int[] NextDia(int ex,int dx) {
		int[] res = new int[2];
		res[0]=ex; res[1]=dx;
		if (ex<0 || ex>=scnts.length) return res;
		int nvszak = scnts[ex]-(ex>0 ? scnts[ex-1] : 0);
		dx++;
		if (dx>=nvszak) {
			ex++;
			if (ex>=scnts.length) return res;
			dx=0;
		}
		res[0]=ex; res[1]=dx;
		return res;
	}
	
	public int[] PrevDia(int ex,int dx) {
		int[] res = new int[2];
		res[0]=ex; res[1]=dx;
		if (ex<0 || ex>=scnts.length) return res;
		dx--;
		if (dx<0) {
			ex--;
			if (ex<0) return res;
			dx=(scnts[ex]-1-(ex>0 ? scnts[ex-1] : 0));
		}
		res[0]=ex; res[1]=dx;
		return res;
	}
	
/////////////////////

	public String getDtx2Dir() {
		return privatedir;
		//if (G.sDtx2Dir.isEmpty()) return docdir;
		//return G.sDtx2Dir;
	}
	
	public void LoadNames(Context ctx) {
		ArrayList<String> flst = new ArrayList<String>();
		ArrayList<String> dlst = new ArrayList<String>();
		ArrayList<String> rlst = new ArrayList<String>();
		ArrayList<String> clst = new ArrayList<String>();
		ArrayList<Integer> sor = new ArrayList<Integer>();

		ArrayList<DtxParams> dtxlst = new ArrayList<>();
		ProgressDialog dlg=ProgressDialog.show(ctx,"Betöltés...","",true);
		SharedPreferences sp = ctx.getSharedPreferences("dtxs", Context.MODE_PRIVATE);
		for (int pass=1; pass<=2; pass++) {
			try {
				File dir = new File(pass==1 ? progdir : getDtx2Dir());
				dir.mkdirs();
				String[] fl = dir.list();
				for (String fname : fl) {
					if (fname.endsWith(".dtx")) {
						if (sp.getString(fname, "").equals("X")) continue;
						dlg.setMessage(fname);
						dtxlst.add(DtxParams.calcParamsOf(new File(dir,fname)));
					}
				}
			} catch (Exception e) {
				Msg(ctx,"Err: "+e.getLocalizedMessage());
			}
		}
		
		dlg.setMessage("Rendezés...");
		Collections.sort(dtxlst, new DtxParams.DtxComparator());

		if (dtxlst.isEmpty()) {
			dtxlst.add(new DtxParams("", "<nincsenek énektárak!>", "", "", 0, 0, ""));
		}

		dlg.dismiss();
		mDtxLst = new DtxParams[dtxlst.size()];
		dtxlst.toArray(mDtxLst);
	}
	
	public String[] getEnekLst(Context ctx, int kotet) {
		DtxParams par = mDtxLst[kotet];
		if (par.fname().isEmpty()) {
			String[] res = new String[1];
			res[0]="";
			return res;
		}
		ProgressDialog dlg = null;
		if (ctx!=null) dlg=ProgressDialog.show(ctx,"Éneklista...",par.title(),true);
		ArrayList<String> lst = new ArrayList<String>();
		ArrayList<Short> cntlst = new ArrayList<Short>();
		short scnt=0;
		try {
			File f = new File(progdir,par.fname());
			if (!f.exists()) f = new File(getDtx2Dir(),par.fname());
			FileInputStream fis = new FileInputStream(f);
			BufferedReader rd = new BufferedReader(new InputStreamReader(fis));
			String enek=""; boolean separ=false;
			String ln = rd.readLine();
			while(ln!=null) {
				if (ln.startsWith(">")) {
					if (enek.length()>0) {
						lst.add(separ ? "-- "+enek+" --" : enek);
						cntlst.add(scnt);
					}
					enek=ln.substring(1);
					separ=true; scnt++;
				} else if (ln.startsWith(" ")) {
					separ=false;
				} else if (ln.startsWith("/")) {
					if (!separ) scnt++;
					separ=false;
				}
				ln = rd.readLine();
			}
			if (enek.length()>0) {
				lst.add(separ ? "-- "+enek+" --" : enek);
				cntlst.add(scnt);
			}
		} catch (Exception e) {
			if (ctx!=null) Msg(ctx,"Err: "+e.getLocalizedMessage());
		}
		if (dlg!=null) dlg.dismiss();

		String[] res = new String[lst.size()];
		lst.toArray(res);
		scnts = new Short[cntlst.size()];
		cntlst.toArray(scnts);
		CurrKotet=kotet;
		return res;
	}
	
	public int Idx2Pos(int ex, int dx) {
		if (ex<0 || scnts==null || ex>=scnts.length) return 0;
		if (ex==0) return dx;
		return scnts[ex-1]+dx;
	}
	
	public int[] Pos2Idx(int pos) {
		int res[] = new int[2];
		res[0]=0; res[1]=0;
		if (pos<=0 || scnts.length<=0 || pos>=scnts[scnts.length-1]) return res;
		int p = Arrays.binarySearch(scnts,(short)pos);
		if (p>=0) { res[0]=p+1; return res; }
		p=-(p+1);
		res[0]=p; res[1]=pos;
		if (p>0) res[1]=pos-scnts[p-1];
		return res;
	}
	
	public String[] getVersszakLst(Context ctx, int kotet, int enek) {
		DtxParams par = mDtxLst[kotet];
		if (par.fname().isEmpty()) {
			String[] res = new String[1];
			res[0]="";
			return res;
		}
		ProgressDialog dlg = null;
		if (ctx!=null) dlg=ProgressDialog.show(ctx,"Versszaklista...",par.title(),true);
		ArrayList<String> lst = new ArrayList<String>();
		try {
			File f = new File(progdir,par.fname());
			if (!f.exists()) f = new File(getDtx2Dir(),par.fname());
			FileInputStream fis = new FileInputStream(f);
			BufferedReader rd = new BufferedReader(new InputStreamReader(fis));
			enek++;
			String ln = rd.readLine();
			while(ln!=null) {
				if (ln.startsWith(">")) {
					enek--;
					if (enek<0) break;
				} else if (enek==0 && ln.startsWith("/")) {
					lst.add(ln.substring(1));
				}
				ln = rd.readLine();
			}
			if (lst.size()<=0) lst.add("---");
		} catch (Exception e) {
			if (ctx!=null) Msg(ctx,"Err: "+e.getLocalizedMessage());
		}
		if (dlg!=null) dlg.dismiss();

		String[] res = new String[lst.size()];
		lst.toArray(res);
		return res;
	}
	
	public class HaromDia {
		String[] dia1,dia2,dia3;
	}
	
	public String[] getDiaTxt(Context ctx, int kx, int ex, int dx) {
		HaromDia res = getDia3(1,ctx,kx,ex,dx);
		if (res.dia1==null) {
			res.dia1=new String[1];
			res.dia1[0]="";
		}
		return res.dia1;
	}
	
	public HaromDia getDia3(int cnt,Context ctx, int kx, int ex, int dx) {
		HaromDia res = new HaromDia();
		if (kx<0 || kx>=mDtxLst.length) return res;
		DtxParams par = mDtxLst[kx];
		if (par.fname().isEmpty()) return res;

		ProgressDialog dlg = null;
		if (ctx!=null) dlg=ProgressDialog.show(ctx,"Dia...",par.title(),true);
		ArrayList<String> lst = new ArrayList<String>();
		try {
			File f = new File(progdir,par.fname());
			if (!f.exists()) f = new File(getDtx2Dir(),par.fname());
			FileInputStream fis = new FileInputStream(f);
			BufferedReader rd = new BufferedReader(new InputStreamReader(fis));
			ex++; dx++;
			boolean hasinput=false;
			String ln = rd.readLine();
			for (int i=0; i<cnt; i++) {
				lst.clear();
				while(ln!=null) {
					if (ln.startsWith(">")) {
						hasinput=false;
						ex--;
						if (ex<0) break;
					} else if (ex==0 || i>0) {
						ex=0;
						if (ln.startsWith("/")) {
							hasinput=true;
							dx--;
							if (dx<0) break;
						} else if (ln.startsWith(" ") && (dx==0 || !hasinput)) {
							dx=0;
							lst.add(ln.substring(1));
						}
					}
					ln = rd.readLine();
				}
				if (lst.size()<=0) lst.add("");
				String[] sa = new String[lst.size()];
				lst.toArray(sa);
				if (res.dia1==null) res.dia1=sa; else if (res.dia2==null) res.dia2=sa; else res.dia3=sa;
				ex=1; dx=1;
				if (ln==null) break;
			}
		} catch (Exception e) {
			if (ctx!=null) Msg(ctx,"Err: "+e.getLocalizedMessage());
		}
		if (dlg!=null) dlg.dismiss();

		return res;
	}
	
	public String getDiaTitle(Context ctx, int kx, int ex, int vx) {
		if (kx<0 || kx>=mDtxLst.length) return "?";
		String[] elst = getEnekLst(ctx,kx);
		String[] vlst = getVersszakLst(ctx,kx,ex);
		String res=mDtxLst[kx].nick()+": "+
			(ex<0 || ex>=elst.length ? "?" : elst[ex])+
			(vx<0 || vx>=vlst.length ? "?" :
				(vlst.length==1 && vlst[0]=="---" ? "" : "/"+vlst[vx]));
		return res;
	}
	
	private boolean isB, isIt, isUl;
	public String changeAttr(boolean newB, boolean newIt, boolean newUl) {
		StringBuilder sb = new StringBuilder();
		if (isB) sb.append("</b>");
		if (isIt) sb.append("</i>");
		if (isUl) sb.append("</u>");
		isB=newB; isIt=newIt; isUl=newUl;
		if (isB) sb.append("<b>");
		if (isIt) sb.append("<i>");
		if (isUl) sb.append("<u>");
		return sb.toString();
	}
	public String diaArrToString(String[] arr) {
		StringBuilder sb = new StringBuilder();
		String nl = "";
		
		isB=false; isIt=false; isUl=false;
		for (String line : arr) {
			sb.append(nl);
			boolean esc=false;
			int i=0, len=line.length();
			while (i<len) {
				char ch=line.charAt(i++);
				if (esc) {
					switch(ch) {
					case 'B': sb.append(changeAttr(true,isIt,isUl)); break;
					case 'b': sb.append(changeAttr(false,isIt,isUl)); break;
					case 'I': sb.append(changeAttr(isB,true,isUl)); break;
					case 'i': sb.append(changeAttr(isB,false,isUl)); break;
					case 'U': sb.append(changeAttr(isB,isIt,true)); break;
					case 'u': sb.append(changeAttr(isB,isIt,false)); break;
					case '-': sb.append("&shy;"); break;
					case '_': sb.append("&#8209;"); break;
					case '.': sb.append("&nbsp;"); break;
					case 'G':
					case 'K':
					case '?':
						while (i<len && line.charAt(i)!=';') i++;
						i++;
						break;
					case ' ':
					default:
						sb.append(ch);
						break;
					}
					esc=false;
				} else if (ch=='\\')
					esc=true;
				else
					sb.append(ch);
			}
			nl="<br/>";
		}
		
		return sb.toString();
	}
	
	public String deFormat(String src) {
		StringBuilder sb = new StringBuilder();
		boolean esc = false;
		int i = 0, len = src.length();
		while(i<len) {
			char ch = src.charAt(i++);
			if (esc) {
				esc=false;
				switch(ch) {
				case '.':
				case ' ':
					sb.append(' ');
					break;
				case '-':
				case '_':
					sb.append('-');
					break;
				case 'G':
				case 'K':
				case '?':
					while (i<len && src.charAt(i++)!=';');
					break;
				}
				continue;
			}
			esc=(ch=='\\');
			if (!esc) sb.append(ch);
		}
		return sb.toString();
	}
	
	public void getIdList(Context ctx, int kotet,
		ArrayList<String> idlst,
		ArrayList<Integer> vxlst,
		ArrayList<Integer> sxlst) {
		DtxParams par = mDtxLst[kotet];
		if (par.fname().isEmpty()) return;

		ProgressDialog dlg = null;
		if (ctx!=null) dlg=ProgressDialog.show(ctx,"ID lista...",par.title(),true);
		try {
			File f = new File(progdir,par.fname());
			if (!f.exists()) f = new File(getDtx2Dir(),par.fname());
			FileInputStream fis = new FileInputStream(f);
			BufferedReader rd = new BufferedReader(new InputStreamReader(fis));
			String ln;
			Integer vx=-1, sx=0;
			while((ln=rd.readLine())!=null) {
				if (ln.startsWith(">")) {
					vx++; sx=-1;
				} else if (ln.startsWith("/")) {
					sx++;
				} else if (ln.startsWith("#")) {
					idlst.add(ln.substring(1));
					vxlst.add(vx); sxlst.add(sx<0 ? 0 : sx);
				}
			}
		} catch (Exception e) {
			if (ctx!=null) Msg(ctx,"Err: "+e.getLocalizedMessage());
		}
		if (dlg!=null) dlg.dismiss();
	}
}

