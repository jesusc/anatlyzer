package anatlyzer.atl.explanations;

import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import anatlyzer.atl.analyser.batch.PossibleInvariantViolationNode;
import anatlyzer.atl.analyser.batch.invariants.IInvariantNode;
import anatlyzer.atl.analyser.generators.GraphvizBuffer;
import anatlyzer.atl.util.ATLSerializer;
import anatlyzer.atl.witness.IWitnessModel;
import anatlyzer.atl.witness.IWitnessVisualizer;
import anatlyzer.atl.witness.WitnessUtil;

public class InvariantExplanationDialog extends SimpleExplanationDialog {

	public static final int SHOW_REWRITING_TREE = 1024;
	private PossibleInvariantViolationNode node;

	public InvariantExplanationDialog(Shell parentShell, PossibleInvariantViolationNode node) {
		super(parentShell, new InvariantExplanation(node));
		this.node = node;
	}

	public static class InvariantExplanation implements AtlProblemExplanation {
		private PossibleInvariantViolationNode node;

		public InvariantExplanation(PossibleInvariantViolationNode node) {
			this.node = node;
		}

		@Override
		public void setMarker(IMarker marker) {
			throw new IllegalStateException();
		}

		@Override
		public boolean isApplicable() {
			return true;
		}

		@Override
		public void setDetailedProblemDescription(StyledText text) {
			text.setText("This is the generated pre-condition: \n\n" + ATLSerializer.serialize(node.getPrecondition()));
		}

		@Override
		public void setAdditionalInfo(Composite composite) {
			
			IWitnessModel witness = node.getWitness();
			if (witness == null ) {
				Label l = new Label(composite, SWT.NONE);
				l.setText("No witness is available.");
				return;
			}
			
			IWitnessVisualizer visualizer = WitnessUtil.getWitnessVisualizer(witness);
			if (visualizer == null ) {
				Label l = new Label(composite, SWT.NONE);
				l.setText("No visualizer is available.");
				return;
			}
			
			visualizer.render(composite);
		}
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		createButton(parent, SHOW_REWRITING_TREE,
				"Show rewriting tree", false);
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		if ( buttonId == SHOW_REWRITING_TREE ) { 
			showTree();
			cancelPressed();			
		}
		super.buttonPressed(buttonId);
	}

	private void showTree() {
		GraphvizBuffer<IInvariantNode> gv = new GraphvizBuffer<IInvariantNode>();
		this.node.genGraphviz(gv);
		
		
	    String tempDir = System.getProperty("java.io.tmpdir");		
	    String output  = tempDir + "/output.dot";
	    
		String s = gv.getText();
		try {
			FileWriter fw = new FileWriter(output);
			fw.append(s);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Runtime.getRuntime().exec("xdot " + output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
		
	}
}
