package com.polyjoe.DiaVetito;

import android.graphics.*;
import eu.diatar.library.*;

public class DiaPicBase extends DiaBase
{
	protected Bitmap bmp;
	
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
	
	public void loadBmp(RecPicBase rec) {
		try {
			bmp = BitmapFactory.decodeStream(rec.getInputStream());
		} catch (Exception e) {
			bmp=null;
		}
	}
	
	public void loadRescaledBmp(RecPicBase rec, int scrW, int scrH) {
		try {
			final BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(rec.getInputStream(), null, opt);

			// Calculate inSampleSize
			opt.inSampleSize = calcScale(opt, scrW, scrH);

			// Decode bitmap with inSampleSize set
			opt.inJustDecodeBounds = false;
			bmp = BitmapFactory.decodeStream(rec.getInputStream(), null, opt);
		} catch (Exception e) {
			bmp=null;
		}
	}
	
	public void drawCenterBmp(Canvas canvas) {
		if (bmp==null) return;
		canvas.drawBitmap(bmp,
			(getWidth(canvas)-bmp.getWidth())/2f,
			(getHeight(canvas)-bmp.getHeight())/2,
			null);
	}
	
	public void drawZoomBmp(Canvas canvas, boolean isometric) {
		if (bmp==null) return;
		Rect sr = new Rect();
		Rect dr = new Rect();
		sr.set(0,0,bmp.getWidth(),bmp.getHeight());
		dr.set(0,0,getWidth(canvas),getHeight(canvas));
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
				(getWidth(canvas)-dr.width())/2,
				(getHeight(canvas)-dr.height())/2);
		}
		canvas.drawBitmap(bmp,sr,dr,null);
	}
	
	public void drawCascadeBmp(Canvas canvas) {
		if (bmp==null) return;
		int cw = getWidth(canvas), ch = getHeight(canvas);
		int bw = bmp.getWidth(), bh = bmp.getHeight();
		int y = 0;
		do {
			int x = 0;
			do {
				canvas.drawBitmap(bmp,x,y,null);
				x+=bw;
			} while(x<cw);
			y+=bh;
		} while(y<ch);
	}
	
	public void drawMirrorBmp(Canvas canvas) {
		if (bmp==null) return;
		Bitmap bmps[] = new Bitmap[4];
		int cw = getWidth(canvas), ch = getHeight(canvas);
		int bw = bmp.getWidth(), bh = bmp.getHeight();
		Matrix mx =new Matrix();
		bmps[0]=bmp;
		mx.setScale(1f,-1f,bw/2f,bh/2f);
		bmps[2]=Bitmap.createBitmap(bmp,0,0,bw,bh,mx,false);
		mx.setScale(-1f,1f,bw/2f,bh/2f);
		bmps[1]=Bitmap.createBitmap(bmp,0,0,bw,bh,mx,false);
		bmps[3]=Bitmap.createBitmap(bmps[2],0,0,bw,bh,mx,false);
		int sc = 0;
		int y = 0;
		do {
			sc&=2;
			int x = 0;
			do {
				canvas.drawBitmap(bmps[sc],x,y,null);
				sc^=1;
				x+=bw;
			} while(x<cw);
			sc^=2;
			y+=bh;
		} while(y<ch);
	}
}
