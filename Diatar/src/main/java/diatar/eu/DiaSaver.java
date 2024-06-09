package diatar.eu;

import java.io.*;

public class DiaSaver
{
	private BufferedWriter bw;
	
	public String Save(String fname) {
		try {
			FileOutputStream fs = new FileOutputStream(fname);
			bw = new BufferedWriter(new OutputStreamWriter(fs,"UTF-8"));
			bw.write("[main]\n");
			SaveProp(DiaItem.sCommonProps);
			
			bw.write("diaszam="+DiaItem.getCount()+"\n");
			
			DiaItem d = DiaItem.sFirst;
			int cnt = 0;
			while(d!=null) {
				cnt++;
				bw.write("["+cnt+"]\n");
				if (d.mTipus==d.ditSEPAR) {
					bw.write("separator="+d.mKnev+"\n");
				} else if (d.mTipus==d.ditDTX) {
					String s =
						SaveStr(d.mKnev,"kotet","")
						+SaveStr(d.mVnev,"enek","")
						+SaveStr(d.mSnev,"versszak","")
						+SaveStr(d.mSid,"id","")
						+SaveProp(d.mProps);
					if (!s.isEmpty()) return s;
				} else if (d.mTipus==d.ditTXT) {
					bw.write("caption="+d.mKnev+"\n");
					bw.write("lines="+d.mTxt.length+"\n");
					for (int i=0; i<d.mTxt.length; i++) {
						bw.write("line"+i+"="+d.mTxt[i]+"\n");
						String s=SaveProp(d.mProps);
						if (!s.isEmpty()) return s;
					}
				} else if (d.mTipus==d.ditPIC) {
					String s =
						SaveStr(d.mKnev+d.mVnev,"kep","")
						+SaveProp(d.mProps);
					if (!s.isEmpty()) return s;
				}
				d=d.mNext;
			}
			bw.close();
		} catch(IOException e) {
			return "Fájl mentési hiba!\n"+e.getLocalizedMessage();
		}
		return "";
	}
	
	private String SaveInt(int val, String key, int defval) {
		if (val==defval) return "";
		String s = key+"="+val+"\n";
		try {
			bw.write(s);
		} catch(IOException e) {
			return "Fájl mentési hiba!\n"+e.getLocalizedMessage();
		}
		return "";
	}

	private String SaveColor(int val, String key) {
		if ((val&0xFF000000)==0) return "";
		return SaveInt(val&0x00FFFFFF, key, -1);
	}
	
	private String SaveStr(String val, String key, String defval) {
		if (val==defval) return "";
		String s = key+"="+val+"\n";
		try {
			bw.write(s);
		} catch(IOException e) {
			return "Fájl mentési hiba!\n"+e.getLocalizedMessage();
		}
		return "";
	}

	private String SaveBool(boolean val, String key, boolean defval) {
		return SaveInt(val ? 1 : 0, key, defval ? 1 : 0);
	}
	
	private String SaveProp(DiaProp prop) {
		return
		 SaveColor(prop.mBkColor,"bkcolor")
		+SaveColor(prop.mTxColor,"txcolor")
		+SaveColor(prop.mHiColor,"hicolor")
		+SaveColor(prop.mBlankColor,"offcolor")
		+SaveStr(prop.mFontName,"fontname","")
		+SaveInt(prop.mFontSize,"fontsize",-1)
		+SaveInt(prop.mTitleSize,"titlesize",-1)
		+SaveInt(prop.mIndent,"indent",-1)
		+SaveInt(prop.mSpacing,"spacing",-1)
		+SaveInt(prop.mFontBold,"fontbold",prop.b3UNUSED)
		+SaveInt(prop.mHCenter,"hcenter",prop.b3UNUSED)
		+SaveInt(prop.mVCenter,"vcenter",prop.b3UNUSED)
		+SaveBool(prop.mDblDia, "dbldia", false)
		;
	}
}
