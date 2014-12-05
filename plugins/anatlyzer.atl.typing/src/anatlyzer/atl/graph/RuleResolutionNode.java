package anatlyzer.atl.graph;

import anatlyzer.atl.analyser.generators.CSPModel;
import anatlyzer.atl.analyser.generators.ErrorSlice;
import anatlyzer.atl.analyser.generators.GraphvizBuffer;
import anatlyzer.atl.analyser.generators.OclGenerator;
import anatlyzer.atl.analyser.generators.TransformationSlice;
import anatlyzer.atl.model.TypeUtils;
import anatlyzer.atl.util.ATLUtils;
import anatlyzer.atlext.ATL.Binding;
import anatlyzer.atlext.OCL.OclExpression;

public class RuleResolutionNode extends AbstractDependencyNode implements ConstraintNode {

	private Binding	binding;

	public RuleResolutionNode(Binding atlBinding) {
		this.binding = atlBinding;
	}
	
	@Override
	public void genErrorSlice(ErrorSlice slice) {
		for(DependencyNode n : dependencies) {
			n.genErrorSlice(slice);
		}				
	}

	@Override
	public void genGraphviz(GraphvizBuffer gv) {
		super.genGraphviz(gv);
		gv.addNode(this, OclGenerator.gen(binding.getValue()) + 
				": " + TypeUtils.typeToString(ATLUtils.getSourceType(binding)) +"\\nresolvedBy", leadsToExecution);
	}


	@Override
	public OclExpression genCSP(CSPModel model) {
		throw new UnsupportedOperationException(binding.getLocation());
	}
	
	@Override
	public void genTransformationSlice(TransformationSlice slice) {
		for(DependencyNode n : dependencies) {
			n.genTransformationSlice(slice);
		}						
	}
	
}
