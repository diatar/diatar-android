package diatar.eu;
import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.widget.*;
import android.graphics.drawable.*;
import eu.diatar.library.*;
import java.io.*;

public class G  //globals
{
	public static final boolean OPEN_DIA_BY_FILESELECTOR = false;
	public static final boolean OPEN_PIC_BY_FILESELECTOR = false;

	public static final String idBKCOLOR = "BkColor";
	public static final String idTXCOLOR = "ForeColor";
	public static final String idBLANKCOLOR = "BlankColor";
	public static final String idHIGHCOLOR = "HighColor";
	public static final String idDOWNWHEN = "NetWhen";
	public static final String idDOWNLASTDAY = "LastNetDay";
	public static final String idDIR = "LoadDir";
	public static final String idDTXINDEX = "DtxIndex";
	public static final String idENEKINDEX = "EnekIndex";
	public static final String idDIAINDEX = "DiaIndex";
	public static final String idFNAME = "DiaFname";
	public static final String idSAVEMODE = "Save";
	public static final String idCOLOR = "color";
	public static final String idISDIR = "isdir";
	public static final String idORIG = "orig";
	public static final String idINDEX = "idx";
	public static final String idIPCNT = "IpCnt";
	public static final String idIPADDR = "IpAddr";
	public static final String idIPPORT = "IpPort";
	public static final String idFONTSIZE = "FontSize";
	public static final String idTITLESIZE = "TitleSize";
	public static final String idLEFTINDENT = "LeftIndent";
	public static final String idSPACING = "spacing";
	public static final String idAUTOSIZE = "autosize";
	public static final String idVCENTER = "VCenter";
	public static final String idHCENTER = "HCenter";
	public static final String idUSEAKKORD = "UseAkkord";
	public static final String idUSEKOTTA = "UseKotta";
	public static final String idUSETITLE = "UseTitle";
	public static final String idCIM = "cim";
	public static final String idTXT = "txt";
	public static final String idSEL = "sel";
	public static final String idFTYPE = "FileType";
	public static final String idPICDIR = "PicDir";
	public static final String idBGMODE = "BgMode";
	public static final String idBLANKPIC = "BlankPic";
	public static final String idBPICUSE = "BPicUse";
	public static final String idPROPMODE = "PropMode";
	public static final String idCOUNT = "Count";
	public static final String idTYPE = "Type";
	public static final String idDTX2DIR = "Dtx2Dir";
	public static final String idKOTTAARANY = "KottaArany";
	public static final String idAKKORDARANY = "AkkordArany";
	public static final String idBACKTRANS = "BackTrans";
	public static final String idBLANKTRANS ="BlankTrans";
	public static final String idALWAYSON ="AlwaysOn";
	public static final String idISFULLSCR = "IsFullScr";
	public static final String idBORDERL = "BorderL";
	public static final String idBORDERT = "BorderT";
	public static final String idBORDERR = "BorderR";
	public static final String idBORDERB = "BorderB";
	public static final String idBOLDTEXT = "BoldText";
	public static final String idFINISHED = "Finished";
	public static final String idCHECKED = "Checked";
	public static final String idDELETED = "Deleted";
	public static final String idGROUP = "Group";
	public static final String idDELETABLE = "Deletable";
	public static final String idFDATE = "FDate";

	
	public static int sBkColor, sTxColor, sBlankColor,sHighColor;
	public static int sFontSize, sTitleSize;
	public static int sIndent, sSpacing;
	public static boolean sHCenter, sVCenter;
	public static int sDownWhen;
	public static long sDownLastDay;
	public static String sLoadDir;
	public static String sPicDir;
	public static int sIpCnt;
	public static String[] sIpAddr;
	public static int[] sIpPort;
	public static boolean sAutosize;
	public static boolean sUseAkkord, sUseTitle, sUseKotta;
	public static int sBgMode;
	public static String sBlankPic;
	public static String sDtx2Dir;
	public static int sKottaArany, sAkkordArany;
	public static int sBackTrans, sBlankTrans;
	public static boolean sIsFullScr;
	public static int sBorderL, sBorderT, sBorderR, sBorderB;
	public static boolean sBoldText;
	
	public static String sDiaFname;  //.dia fajl
	public static boolean sShowing;
	public static int sHighPos;
	public static Uri sLoadUri;
	
	public static boolean sAlwaysOn;
	
	public static void setIpCnt(int newval) {
		if (newval<0) newval=0;
		sIpCnt=newval;
		sIpAddr=null; sIpPort=null;
		if (newval>0) {
			sIpAddr = new String[newval];
			sIpPort = new int[newval];
		}
	}
	
	public static void Load(Context mainctx) {
		SharedPreferences sp = mainctx.getSharedPreferences("settings",Context.MODE_PRIVATE);
		sBkColor=sp.getInt(idBKCOLOR,0xFFFFFFFF);
		sTxColor=sp.getInt(idTXCOLOR,0xFF000000);
		sBlankColor=sp.getInt(idBLANKCOLOR,0xFF000000);
		sHighColor=sp.getInt(idHIGHCOLOR,0xFF0000FF);
		sDownWhen=sp.getInt(idDOWNWHEN,0);
		sDownLastDay=sp.getLong(idDOWNLASTDAY,0);
		sLoadDir=sp.getString(idDIR,"/");
		sPicDir=sp.getString(idPICDIR,"/");
		setIpCnt(sp.getInt(idIPCNT,0));
		for (int i=0; i<sIpCnt; i++) {
			sIpAddr[i]=sp.getString(idIPADDR+i,"1.1.1.1");
			sIpPort[i]=sp.getInt(idIPPORT+i,1024);
		}
		sFontSize=sp.getInt(idFONTSIZE,70);
		sTitleSize=sp.getInt(idTITLESIZE,12);
		sIndent=sp.getInt(idLEFTINDENT,2);
		sSpacing=sp.getInt(idSPACING,0);
		sAutosize=sp.getBoolean(idAUTOSIZE,true);
		sVCenter=sp.getBoolean(idVCENTER,true);
		sHCenter=sp.getBoolean(idHCENTER,false);
		sUseAkkord=sp.getBoolean(idUSEAKKORD,false);
		sUseKotta=sp.getBoolean(idUSEKOTTA,true);
		sUseTitle=sp.getBoolean(idUSETITLE,true);
		sBgMode=sp.getInt(idBGMODE,RecPic.bgZOOM);
		sBlankPic=sp.getString(idBLANKPIC,"");
		sDtx2Dir=sp.getString(idDTX2DIR,"");
		sKottaArany=sp.getInt(idKOTTAARANY,100);
		sAkkordArany=sp.getInt(idAKKORDARANY,100);
		sBackTrans=sp.getInt(idBACKTRANS,0);
		sBlankTrans=sp.getInt(idBLANKTRANS,0);
		sAlwaysOn=sp.getBoolean(idALWAYSON,false);
		sIsFullScr=sp.getBoolean(idISFULLSCR,false);
		sBorderL=sp.getInt(idBORDERL, 0);
		sBorderT=sp.getInt(idBORDERT, 0);
		sBorderR=sp.getInt(idBORDERR, 0);
		sBorderB=sp.getInt(idBORDERB, 0);
		sBoldText=sp.getBoolean(idBOLDTEXT,false);
	}
	
	public static void Save(Context mainctx) {
		SharedPreferences sp = mainctx.getSharedPreferences("settings",Context.MODE_PRIVATE);
		SharedPreferences.Editor spe = sp.edit();
		spe.putInt(idBKCOLOR,sBkColor);
		spe.putInt(idTXCOLOR,sTxColor);
		spe.putInt(idBLANKCOLOR,sBlankColor);
		spe.putInt(idHIGHCOLOR,sHighColor);
		spe.putInt(idDOWNWHEN,sDownWhen);
		spe.putLong(idDOWNLASTDAY,sDownLastDay);
		spe.putString(idDIR,sLoadDir);
		spe.putString(idPICDIR,sPicDir);
		spe.putInt(idIPCNT,sIpCnt);
		for (int i=0; i<sIpCnt; i++) {
			spe.putString(idIPADDR+i,sIpAddr[i]);
			spe.putInt(idIPPORT+i,sIpPort[i]);
		}
		spe.putInt(idFONTSIZE,sFontSize);
		spe.putInt(idTITLESIZE,sTitleSize);
		spe.putInt(idLEFTINDENT,sIndent);
		spe.putInt(idSPACING,sSpacing);
		spe.putBoolean(idAUTOSIZE,sAutosize);
		spe.putBoolean(idVCENTER,sVCenter);
		spe.putBoolean(idHCENTER,sHCenter);
		spe.putBoolean(idUSEAKKORD,sUseAkkord);
		spe.putBoolean(idUSEKOTTA,sUseKotta);
		spe.putBoolean(idUSETITLE,sUseTitle);
		spe.putInt(idBGMODE,sBgMode);
		spe.putString(idBLANKPIC,sBlankPic);
		spe.putString(idDTX2DIR,sDtx2Dir);
		spe.putInt(idKOTTAARANY,sKottaArany);
		spe.putInt(idAKKORDARANY,sAkkordArany);
		spe.putInt(idBACKTRANS,sBackTrans);
		spe.putInt(idBLANKTRANS,sBlankTrans);
		spe.putBoolean(idALWAYSON,sAlwaysOn);
		spe.putBoolean(idISFULLSCR,sIsFullScr);
		spe.putInt(idBORDERL, sBorderL);
		spe.putInt(idBORDERT, sBorderT);
		spe.putInt(idBORDERR, sBorderR);
		spe.putInt(idBORDERB, sBorderB);
		spe.putBoolean(idBOLDTEXT,sBoldText);
		
		spe.apply();
	}
	
	public static String getVersion(Context ctx) {
		String res = "???";
		try {
			PackageInfo pi = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(),0);
			res = pi.versionName+" ("+pi.versionCode+")";
		} catch (Exception e) {}
		return res;
	}
	
	static public int getColorOfBtn(Button b) {
		return 0xFF000000 | ((ColorDrawable)b.getBackground()).getColor();
	}
	
	static public void setColorOfBtn(Button btn, int color) {
		color|=0xFF000000;
		btn.setBackgroundColor(color);
		btn.setTextColor(ColorDlg.getComplementer(color));
		btn.setText(String.format("#%06X",(color&0x00FFFFFF)));
	}
	
	static public String dirOf(String fname) {
		if (fname==null || fname.isEmpty()) return fname;
		File fn = new File(fname);
		if (fn.isDirectory()) return fname;
		return fn.getParent()+File.separator;
	}
	
	static public String nameOf(String fname) {
		if (fname==null || fname.isEmpty()) return fname;
		File fn = new File(fname);
		if (fn.isDirectory()) return "";
		return fn.getName();
	}
}
