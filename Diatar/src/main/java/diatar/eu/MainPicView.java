package diatar.eu;

import android.graphics.*;
import java.io.*;

public class MainPicView extends DiaViewBase
{
	private Bitmap mBmp;
	private String mFname;

	public MainPicView(MainActivity ma, String fname) {
		super(ma);
		mFname=fname;
	}

	public void loadBmp(String fname) {
		try {
			mBmp = BitmapFactory.decodeStream(new FileInputStream(fname));
		} catch (Exception e) {
			mBmp=null;
		}
	}

	protected int calcScale(BitmapFactory.Options opt, int scrW, int scrH) {
		int w = opt.outWidth, h = opt.outHeight;
		int scale = 1;
		if (h>scrH || w>scrW) {
			final int h2 = h / 2;
			final int w2 = w / 2;
			while (h2/scale >= scrH && w2/scale >= scrW)
				scale*=2;
		}
		return scale;
	}

	public void loadRescaledBmp(String fname, int scrW, int scrH) {
		try {
			final BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(fname, opt);

			// Calculate inSampleSize
			opt.inSampleSize = calcScale(opt, scrW, scrH);

			// Decode bitmap with inSampleSize set
			opt.inJustDecodeBounds = false;
			mBmp = BitmapFactory.decodeFile(fname, opt);
		} catch (Exception e) {
			mBmp=null;
		}
	}
	
	public void drawZoomBmp(Canvas canvas, boolean isometric) {
		if (mBmp==null) return;
		Rect sr = new Rect();
		Rect dr = new Rect();
		sr.set(0,0,mBmp.getWidth(),mBmp.getHeight());
		dr.set(0,0,canvas.getWidth(),canvas.getHeight());
		if (isometric) {
			if (sr.width()>0 && sr.height()>0) {
				int w = dr.width();
				int h = (sr.height()*dr.width())/sr.width();
				if (h>dr.height()) {
					h=dr.height();
					w=(sr.width()*dr.height())/sr.height();
				}
				dr.set(0,0,w,h);
			}
			dr.offset(
				(canvas.getWidth()-dr.width())/2,
				(canvas.getHeight()-dr.height())/2);
		}
		canvas.drawBitmap(mBmp,sr,dr,null);
	}
	
	@Override
	protected void drawDia(Canvas canvas) {
		if (mBmp==null)
			loadRescaledBmp(mFname,canvas.getWidth(),canvas.getHeight());

		canvas.drawColor(G.sBkColor);
		drawZoomBmp(canvas,true);
	}
}
