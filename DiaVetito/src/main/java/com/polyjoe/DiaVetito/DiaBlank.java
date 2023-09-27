package com.polyjoe.DiaVetito;

import android.graphics.*;
import eu.diatar.library.*;

public class DiaBlank extends DiaPicBase
{
	@Override
	public void OnDrawClipped(Canvas canvas) {
		canvas.drawColor(mTcp.G.BlankColor);
		if (mTcp.G.IsBlankPic && mTcp.G.ShowBlankPic) {
			switch(mTcp.G.BgMode) {
				case RecPic.bgCENTER:
				default:
					drawCenterBmp(canvas);
					break;
				case RecPic.bgFULL:
					drawZoomBmp(canvas,false);
					break;
				case RecPic.bgZOOM:
					drawZoomBmp(canvas,true);
					break;
				case RecPic.bgCASCADE:
					drawCascadeBmp(canvas);
					break;
				case RecPic.bgMIRROR:
					drawMirrorBmp(canvas);
					break;
			}
		}
	}
}
