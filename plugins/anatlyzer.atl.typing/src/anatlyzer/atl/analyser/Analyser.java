package anatlyzer.atl.analyser;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.ResourceSet;

import anatlyzer.atl.analyser.namespaces.GlobalNamespace;
import anatlyzer.atl.graph.ErrorPathGenerator;
import anatlyzer.atl.graph.ProblemGraph;
import anatlyzer.atl.model.ATLModel;
import anatlyzer.atl.model.ErrorModel;
import anatlyzer.atl.model.TypingModel;
import anatlyzer.atlext.ATL.Unit;

public class Analyser {
	private GlobalNamespace	mm;
	private TypingModel	typ;
	private ATLModel	trafo;
	private ErrorModel	errors;
	
	private boolean doDependency = true;
	private ProblemGraph problemGraph;
	
	public static final String USE_THIS_MODULE_CLASS = "ThisModule";
	private int stage = 0;
	
	public Analyser(GlobalNamespace mm, ATLModel atlModel) throws IOException {
		this.mm    = mm;
		this.trafo = atlModel;
		this.typ   = atlModel.getTyping();
		this.errors = atlModel.getErrors();
	}
	
	public void setDoDependencyAnalysis(boolean doDependency) {
		this.doDependency = doDependency;
	}

	public void perform() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<?> result = executor.submit(new Runnable() {			
			@Override
			public void run() {
				AnalyserContext.setErrorModel(errors);
				AnalyserContext.setTypingModel(typ);				
				AnalyserContext.setGlobalNamespace(mm);
				
				stage++;
				
				List<? extends Unit> units = trafo.allObjectsOf(Unit.class);
				for (Unit unit : units) {
					new ExtendTransformation(trafo, mm, unit).perform();
					new TypeAnalysisTraversal(trafo, mm, unit).perform();
				}	
				
				stage++;
				if ( doDependency ) {
					problemGraph = new ErrorPathGenerator(trafo).perform();
				}
			}
		});

		try {
			// wait;
			result.get();
		} catch (Exception e) {
			throw new AnalyserInternalError(e, stage);
		} finally {			
			executor.shutdown();
		}
		
		
	}
	
	public ErrorModel getErrors() {
		return errors;
	}
	
	public TypingModel getTyping() {
		return typ;
	}

	public ProblemGraph getDependencyGraph() {
		return problemGraph;
	}

	public ATLModel getATLModel() {
		return trafo;
	}
}