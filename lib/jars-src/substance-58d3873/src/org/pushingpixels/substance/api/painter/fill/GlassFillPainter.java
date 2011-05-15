/*
 * Copyright (c) 2005-2010 Substance Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of Substance Kirill Grouchnikov nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package org.pushingpixels.substance.api.painter.fill;

import java.awt.Color;

import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.internal.utils.SubstanceColorUtilities;

/**
 * Fill painter that returns images with classic appearance. This class is part
 * of officially supported API.
 * 
 * @author Kirill Grouchnikov
 */
public class GlassFillPainter extends StandardFillPainter {
	@Override
	public String getDisplayName() {
		return "Glass";
	}

	@Override
	public Color getTopFillColor(SubstanceColorScheme fillScheme) {
		return SubstanceColorUtilities.getInterpolatedColor(super
				.getBottomFillColor(fillScheme), super
				.getMidFillColorTop(fillScheme), 0.6);
	}

	@Override
	public Color getMidFillColorTop(SubstanceColorScheme fillScheme) {
		return SubstanceColorUtilities.getInterpolatedColor(this
				.getTopFillColor(fillScheme), super
				.getMidFillColorTop(fillScheme), 0.8);
	}

	@Override
	public Color getMidFillColorBottom(SubstanceColorScheme fillScheme) {
		return super.getMidFillColorTop(fillScheme);
	}

	@Override
	public Color getBottomFillColor(SubstanceColorScheme fillScheme) {
		return SubstanceColorUtilities.getInterpolatedColor(this
				.getMidFillColorBottom(fillScheme), super
				.getBottomFillColor(fillScheme), 0.7);
	}
}