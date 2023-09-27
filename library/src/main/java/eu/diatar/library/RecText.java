package eu.diatar.library;
import java.nio.charset.*;

public class RecText extends RecBase
{
	public RecText(int recsize) { setMaxlen(recsize); }

	public String getLine(int lineidx) {
		StringBuffer s = new StringBuffer();
		int p=0;
		while (p<len && lineidx>0) {
			if (buf[p]==(byte)13) lineidx--;
			p++;
		}
		if (p<len && buf[p]==(byte)10) p++;
		if (p>=len) return "";
		int l=0;
		while (p+l<len) {
			if (buf[p+l]==(byte)13) break;
			l++;
		}
		try {
			return new String(buf,p,l,"UTF-8");
		} catch (Exception e) {
			return "";
		}
	}

	public int getLineCount() {
		if (len==0) return 0;
		int res=0;
		int p=len-1;
		while(p-->0) {
			if (buf[p]==(byte)13) res++;
		}
		if (res==0) res=1;
		return res;
	}

	public void NormalizeEOL() {
		boolean cr = false;
		for (int p=0; p<len; p++) {
			if (!cr && buf[p]==(byte)10)
				buf[p]=(byte)13;
			else
				cr=(buf[p]==(byte)13);
		}
	}
	
	public static RecText Create(String title, String body[]) {
		Charset cs = Charset.forName("UTF-8");
		byte[] btitle = title.getBytes(cs);
		int l = btitle.length+1;
		int blen = (body==null ? 0 : body.length);
		byte[][] bbody = null;
		if (blen>0) {
			bbody = new byte[body.length][];
			for (int i=0; i<blen; i++) {
				String s = body[i];
				bbody[i]=s.getBytes(cs);
				l+=bbody[i].length+1;
			}
		}
		RecText res = new RecText(l);
		int ix=0;
		res.buf[ix++]=13;  //scola line
		for (int i=0; i<btitle.length; i++) res.buf[ix++]=btitle[i];
		for (int r=0; r<blen; r++) {
			byte[] bb = bbody[r];
			res.buf[ix++]=13;
			for (int i=0; i<bb.length; i++) res.buf[ix++]=bb[i];
		}
		return res;
	}
}
