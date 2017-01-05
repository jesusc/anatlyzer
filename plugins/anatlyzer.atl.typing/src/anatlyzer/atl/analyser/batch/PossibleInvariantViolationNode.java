package anatlyzer.atl.analyser.batch;

import java.util.HashSet;
import java.util.function.Function;

import analyser.atl.problems.IDetectedProblem;
import anatlyzer.atl.analyser.Analyser;
import anatlyzer.atl.analyser.IAnalyserResult;
import anatlyzer.atl.analyser.batch.invariants.IInvariantNode;
import anatlyzer.atl.analyser.batch.invariants.InvariantGraphGenerator;
import anatlyzer.atl.analyser.generators.CSPModel;
import anatlyzer.atl.analyser.generators.ErrorSlice;
import anatlyzer.atl.analyser.generators.TransformationSlice;
import anatlyzer.atl.errors.ProblemStatus;
import anatlyzer.atl.errors.atl_error.LocalProblem;
import anatlyzer.atl.graph.AbstractDependencyNode;
import anatlyzer.atl.graph.GraphNode;
import anatlyzer.atl.graph.IPathVisitor;
import anatlyzer.atl.model.ATLModel;
import anatlyzer.atl.types.Metaclass;
import anatlyzer.atl.util.ATLCopier;
import anatlyzer.atl.util.ATLSerializer;
import anatlyzer.atl.util.ATLUtils;
import anatlyzer.atl.witness.IWitnessModel;
import anatlyzer.atlext.ATL.OutPatternElement;
import anatlyzer.atlext.ATL.StaticHelper;
import anatlyzer.atlext.OCL.IteratorExp;
import anatlyzer.atlext.OCL.OCLFactory;
import anatlyzer.atlext.OCL.OclExpression;
import anatlyzer.atlext.OCL.OclModel;
import anatlyzer.atlext.OCL.OclModelElement;
import anatlyzer.atlext.OCL.OperationCallExp;
import anatlyzer.atlext.OCL.OperatorCallExp;
import anatlyzer.atlext.OCL.VariableDeclaration;

// Probably there is no need to inherit from AbstractDependencyNode
public class PossibleInvariantViolationNode extends AbstractDependencyNode implements IDetectedProblem {

	private OclExpression expr;
	private ATLModel model;
	private IAnalyserResult result;
	private ProblemStatus status = ProblemStatus.WITNESS_REQUIRED;
	private String invName;
	private IInvariantNode invNode;
	private IWitnessModel witness;

	public PossibleInvariantViolationNode(StaticHelper helper, ATLModel model, IAnalyserResult result) {
		this.invName = ATLUtils.getHelperName(helper);
		this.expr = ATLUtils.getHelperBody(helper);
		this.model = model;
		this.result = result; 
	}

	@Override
	public OclExpression genCSP(CSPModel model, GraphNode previous) {
//		CSPModel cspmodel = new CSPModel();
//		cspmodel.initWithoutThisModuleContext();
//
//		return cspmodel.negateExpression( getInvariantNode().genExpr(cspmodel) );
		return getPrecondition((pre) -> {
			OperatorCallExp negate = OCLFactory.eINSTANCE.createOperatorCallExp();
			negate.setOperationName("not");
			negate.setSource(pre);
			return negate;
		});
	}

	public OclExpression getPrecondition() {
		return getPrecondition(exp -> exp);
	}
	
	protected OclExpression getPrecondition(Function<OclExpression, OclExpression> negator) {
		CSPModel cspmodel = new CSPModel();
		cspmodel.initWithoutThisModuleContext();

		HashSet<OutPatternElement> targets = new HashSet<OutPatternElement>();
		// getInvariantNode().getTargetObjectsInBinding(targets);

		if ( targets.size() > 0 ) {
			OutPatternElement ope = targets.iterator().next();
			
			OclModelElement m = OCLFactory.eINSTANCE.createOclModelElement();
			OclModel model = OCLFactory.eINSTANCE.createOclModel();
			model.setName("TGT");
			m.setName(ope.getType().getName());
			m.setModel(model);
			m.setInferredType(ope.getInferredType());
			
			OperationCallExp op = OCLFactory.eINSTANCE.createOperationCallExp();
			op.setOperationName("allInstances");
			op.setSource(m);
		
			IteratorExp exists = cspmodel.createExists(op, ope.getVarName());
			cspmodel.addToScope(ope, exists.getIterators().get(0));

			exists.setBody( negator.apply(getInvariantNode().genExpr(cspmodel)) );
			return exists;
		}
		
		return negator.apply( getInvariantNode().genExpr(cspmodel) );		
	}
	
	private IInvariantNode getInvariantNode() {
		if ( invNode == null)
			this.invNode = new InvariantGraphGenerator(this.model).replace(expr);		
		return this.invNode;
	}

	@Override
	public void genErrorSlice(ErrorSlice slice) {
		HashSet<OutPatternElement> targets = new HashSet<OutPatternElement>();
		getInvariantNode().getTargetObjectsInBinding(targets);
		targets.forEach(o -> {
			slice.addTargetMetaclassNeededInError(((Metaclass) o.getInferredType()).getKlass());
		});
		
		getInvariantNode().genErrorSlice(slice);
	}

	@Override
	public void genTransformationSlice(TransformationSlice slice) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isProblemInPath(LocalProblem lp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isExpressionInPath(OclExpression expr) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isVarRequiredByErrorPath(VariableDeclaration v) {		
		throw new IllegalStateException();
	}

	@Override
	public OclExpression genWeakestPrecondition(CSPModel model) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void bottomUp(IPathVisitor visitor) {
		boolean b = visitor.visitProblem(this);
		if ( b ) followDepending(node -> node.bottomUp(visitor));
	}

	// Implementation of IDetectedProblem
	
	@Override
	public ErrorSlice getErrorSlice(IAnalyserResult result) {
		ErrorSlice slice = new ErrorSlice(result, ATLUtils.getSourceMetamodelNames(result.getATLModel()), this);
		this.genErrorSlice(slice);
		return slice;
	}

	@Override
	public OclExpression getWitnessCondition() {
		// Similar to rule analysis
		CSPModel model = new CSPModel();
		IteratorExp ctx = model.createThisModuleContext();
		model.setThisModuleVariable(ctx.getIterators().get(0));
		
		OclExpression theCondition = this.genCSP(model, null);
		ctx.setBody(theCondition);
		return ctx;
	}

	public ProblemStatus getAnalysisResult() {
		return status;
	}

	public void setAnalysisResult(ProblemStatus result2, IWitnessModel iWitnessModel) {
		this.status = result2;
		this.witness = iWitnessModel;
	}

	public IWitnessModel getWitness() {
		return witness;
	}
	
	public String getInvName() {
		return this.invName;
	}
	
}