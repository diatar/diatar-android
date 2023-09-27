package com.polyjoe.DiaVetito;

import android.graphics.*;

public class DiaPic extends DiaPicBase
{

	@Override
	public void OnDrawClipped(Canvas canvas)
	{
		canvas.drawColor(mTcp.G.BkColor);
		drawZoomBmp(canvas,true);
	}
}
