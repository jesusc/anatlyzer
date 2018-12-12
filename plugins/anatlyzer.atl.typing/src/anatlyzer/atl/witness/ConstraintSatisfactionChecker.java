package anatlyzer.atl.witness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.resource.Resource;

import analyser.atl.problems.IDetectedProblem;
import anatlyzer.atl.analyser.Analyser;
import anatlyzer.atl.analyser.AnalysisResult;
import anatlyzer.atl.analyser.ExtendTransformation.IEOperationHandler;
import anatlyzer.atl.analyser.IAnalyserResult;
import anatlyzer.atl.analyser.generators.ErrorSlice;
import anatlyzer.atl.analyser.generators.OclSlice;
import anatlyzer.atl.analyser.namespaces.GlobalNamespace;
import anatlyzer.atl.errors.ProblemStatus;
import anatlyzer.atl.model.ATLModel;
import anatlyzer.atl.util.ATLCopier;
import anatlyzer.atl.util.ATLUtils;
import anatlyzer.atl.util.AnalyserUtils;
import anatlyzer.atl.witness.IWitnessFinder.WitnessGenerationMode;
import anatlyzer.atlext.ATL.ATLFactory;
import anatlyzer.atlext.ATL.ContextHelper;
import anatlyzer.atlext.ATL.Helper;
import anatlyzer.atlext.ATL.Library;
import anatlyzer.atlext.ATL.Module;
import anatlyzer.atlext.ATL.StaticHelper;
import anatlyzer.atlext.ATL.Unit;
import anatlyzer.atlext.OCL.OCLFactory;
import anatlyzer.atlext.OCL.OclExpression;
import anatlyzer.atlext.OCL.OclFeatureDefinition;
import anatlyzer.atlext.OCL.OclModel;
import anatlyzer.atlext.OCL.Operation;
import anatlyzer.atlext.OCL.OperatorCallExp;

/**
 * This class implements a wrapper over the AnATLyzer machinery intended
 * to allow the user to ask the constraint solver if a set of expressions
 * is satisfiable. 
 * 
 * @author jesus
 */
public class ConstraintSatisfactionChecker {

	private List<OclExpression> expressions = new ArrayList<OclExpression>();
	private HashMap<String, Resource> namesToResources = new HashMap<String, Resource>();
	private IWitnessFinder finder;
	private ProblemStatus finderResult;
	private ATLModel model;
	private Library library;
	
	public ConstraintSatisfactionChecker(List<OclExpression> expressions) {
		this.expressions.addAll(expressions);
	}
	
	public ConstraintSatisfactionChecker(Library lib) {
		this.library = lib;
	}

	public static ConstraintSatisfactionChecker withExpr(OclExpression expr) {
		return new ConstraintSatisfactionChecker(Collections.singletonList(expr));
	}
	

	public static ConstraintSatisfactionChecker withLibrary(Library lib) {
		return new ConstraintSatisfactionChecker(lib);
	}

	public static ConstraintSatisfactionChecker withExpr(List<OclExpression> expr) {
		return new ConstraintSatisfactionChecker(expr);
	}
	
	public ConstraintSatisfactionChecker configureMetamodel(String mmName, Resource mmResource) {
		namesToResources.put(mmName, mmResource);
		return this;
	}
	
	public ConstraintSatisfactionChecker withFinder(IWitnessFinder finder) {
		this.finder = finder;
		return this;
	}
	
	public ConstraintSatisfactionChecker check() {
		if ( this.finder == null )
			throw new IllegalStateException();
		
		Unit unit = constructTransformation();
		
		// Configure the analysis
		GlobalNamespace mm = new GlobalNamespace(namesToResources.values(), namesToResources, false);
		ATLModel model = new ATLModel();
		model.add(unit);
		this.model = model;
		
		Analyser analyser = new Analyser(mm, model);
		// This is to make sure that we do not inline eOperations which have already been handled by the OCL translator
		analyser.withEOperationHandler(new IEOperationHandler() {			
			@Override
			public boolean handle(Unit unit, EClass c, EOperation op) { return true; }		
			@Override
			public boolean canHandle(EClass c, EOperation op) { return true; }
		});
		
		analyser.perform();
		
		// Configure the finder
		this.finderResult = finder.find(new ConstraintSatisfactionProblem(), new AnalysisResult(analyser));

		return this;
	}

	public ProblemStatus getFinderResult() {
		return finderResult;
	}

	public IWitnessModel getWitnessModel() {
		return finder.getFoundWitnessModel();
	}
	
	private Unit constructTransformation() {
		// Library module = ATLFactory.eINSTANCE.createLibrary();
		Module module = ATLFactory.eINSTANCE.createModule();
		module.setName("inMemoryModule");
		
		int i = 0;
		for (Entry<String, Resource> entry : namesToResources.entrySet()) {
			OclModel inModel = OCLFactory.eINSTANCE.createOclModel();
			inModel.setName("IN" + i);
			OclModel mmModel = OCLFactory.eINSTANCE.createOclModel();
			mmModel.setName(entry.getKey());
			inModel.setMetamodel(mmModel);
			module.getInModels().add(inModel);			
			i++;
		}
		
		List<StaticHelper> helpers = expressions.stream().map(e -> {
			return createOperation("exp" + expressions.indexOf(e), (op) -> {
				op.setBody((OclExpression) ATLCopier.copySingleElement(e));
			});
		}).collect(Collectors.toList());
		
		
		// module.getHelpers().addAll(helpers);
		module.getElements().addAll(helpers);

		if ( library != null )
			module.getElements().addAll(library.getHelpers());
		
		return module;
	}

	private StaticHelper createOperation(String opName, Consumer<Operation> consumer) {
		StaticHelper h = ATLFactory.eINSTANCE.createStaticHelper();
		OclFeatureDefinition f = OCLFactory.eINSTANCE.createOclFeatureDefinition();		
		Operation op = OCLFactory.eINSTANCE.createOperation();
		op.setName(opName);
		op.setReturnType(OCLFactory.eINSTANCE.createBooleanType());
		f.setFeature(op);
		h.setDefinition(f);
		consumer.accept(op);
		return h;
	}

	private List<OclExpression> getExpressionsToBeChecked() {
		List<Helper> helpers = ATLUtils.getAllHelpers(model).stream().
				filter(h -> ! AnalyserUtils.isAddedEOperation(h)).
				filter(h -> ! isPropertyRepresentation(h)).
				map(h -> {
					if ( h instanceof ContextHelper ) {
						return AnalyserUtils.convertContextInvariant((ContextHelper) h);
					} else {
						return h;
					}
				}).
				collect(Collectors.toList());
		return helpers.stream().map(h -> ATLUtils.getBody(h)).collect(Collectors.toList());
	}
	

	private List<ContextHelper> getDerivedPropertiesOrOperations() {
		return ATLUtils.getAllHelpers(model).stream().			
				filter(h -> isPropertyRepresentation(h) || isOperationImplementation(h)).
				map(h -> (ContextHelper) h).
				collect(Collectors.toList());
	}
	
	
	private boolean isPropertyRepresentation(Helper h) {
		return h.getAnnotations().containsKey("DERIVED_PROPERTY");
	}

	private boolean isOperationImplementation(Helper h) {
		return h.getAnnotations().containsKey("OPERATION_IMPLEMENTATION");
	}

	public class ConstraintSatisfactionProblem implements IDetectedProblem {

		@Override
		public ErrorSlice getErrorSlice(IAnalyserResult result) {
			ErrorSlice slice = new ErrorSlice(result, namesToResources.keySet(), this);
			
			getExpressionsToBeChecked().forEach(e -> OclSlice.slice(slice, e));
			getDerivedPropertiesOrOperations().forEach(h -> {
				System.out.println("====> " + ATLUtils.getHelperName(h));
				OclSlice.slice(slice, ATLUtils.getHelperBody(h));
				slice.addHelper(h);	
			});
			
			return slice;
		}

		@Override
		public OclExpression getWitnessCondition() {
			List<OclExpression> exprs = getExpressionsToBeChecked();
			if ( exprs.size() == 1 ) 
				return exprs.get(0);
			
			OclExpression result = (OclExpression) ATLCopier.copySingleElement(exprs.get(0));
			for(int i = 1; i < exprs.size(); i++) {
				OclExpression exp = exprs.get(i);
				
				OperatorCallExp andOp = OCLFactory.eINSTANCE.createOperatorCallExp();
				andOp.setOperationName("and");
				andOp.setSource(result);
				// Needs to be copied because there will be other calls to getExpressionsToBeChecked
				// and adding exp to the "and" will change the helper body
				andOp.getArguments().add( (OclExpression) ATLCopier.copySingleElement(exp) );
				
				result = andOp;
			}
			
			return result;
		}

		@Override
		public boolean isExpressionInPath(OclExpression expr) {
			return false;
		}

		@Override
		public List<OclExpression> getFrameConditions() {
			return Collections.emptyList();
		}
		
	}


	
}
