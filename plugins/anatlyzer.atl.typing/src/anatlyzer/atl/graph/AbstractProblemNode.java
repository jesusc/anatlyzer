package anatlyzer.atl.graph;

import anatlyzer.atl.analyser.generators.ErrorSlice;
import anatlyzer.atl.errors.Problem;

public abstract class AbstractProblemNode<P extends Problem> extends AbstractDependencyNode implements ProblemNode {

	protected P problem;
	private ErrorSlice	errorSlice;
	
	public AbstractProblemNode(P p) {
		this.problem = p;
	}
	
	@Override
	public ErrorSlice getErrorSlice() {
		if ( errorSlice == null ) {
			errorSlice = new ErrorSlice(null);			
			this.genErrorSlice(errorSlice);
		}
		return errorSlice;
	}
	
}
