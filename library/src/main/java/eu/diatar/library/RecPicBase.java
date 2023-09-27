package eu.diatar.library;
import java.io.*;

public class RecPicBase extends RecBase
{
	public RecPicBase(int recsize) {
		setMaxlen(recsize);
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(buf,8,getMaxlen()-8);
	}
	
	public void setExt(String ext) {
		if (getMaxlen()<8) return;
		int extlen=ext.length();
		if (extlen>7) extlen=7;
		buf[0]=(byte)extlen;
		for (int i=0; i<extlen; i++)
			buf[1+i]=(byte)ext.charAt(i);
	}
	
	public String loadFile(String fname) {
		try {
			File fn = new File(fname);
			if (!fn.isFile() || !fn.exists())
				return "Fájl nem található: "+fname;
			int flen = (int)fn.length();
			setMaxlen(8+flen); setLen(8+flen);
			String ext = fn.getName();
			ext=ext.substring(ext.lastIndexOf('.')+1);
			setExt(ext);
			FileInputStream fis = new FileInputStream(fn);
			fis.read(buf,8,flen);
		} catch(Exception e) {
			return "Képfájl hiba: "+e.getLocalizedMessage();
		}
		return "";
	}
}
