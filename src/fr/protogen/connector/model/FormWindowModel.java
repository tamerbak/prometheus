/**
 * 
 */
package fr.protogen.connector.model;

import java.util.List;

import fr.protogen.engine.control.ui.MtmBlock;
import fr.protogen.engine.utils.UIControlsLine;

/**
 * @author developer
 *
 */
public class FormWindowModel {
	private List<MtmBlock> mtmBlocks;
	private UIControlsLine controlLines;
	

	/**
	 * @return the mtmBlocks
	 */
	public List<MtmBlock> getMtmBlocks() {
		return mtmBlocks;
	}

	/**
	 * @param mtmBlocks
	 *            the mtmBlocks to set
	 */
	public void setMtmBlocks(List<MtmBlock> mtmBlocks) {
		this.mtmBlocks = mtmBlocks;
	}

	/**
	 * @return the controlLines
	 */
	public UIControlsLine getControlLines() {
		return controlLines;
	}

	/**
	 * @param controlLines
	 *            the controlLines to set
	 */
	public void setControlLines(UIControlsLine controlLines) {
		this.controlLines = controlLines;
	}
}
