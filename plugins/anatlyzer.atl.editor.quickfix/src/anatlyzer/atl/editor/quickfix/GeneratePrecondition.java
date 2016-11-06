package anatlyzer.atl.editor.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;

import anatlyzer.atl.analyser.AnalysisResult;
import anatlyzer.atl.errors.atl_error.LocalProblem;
import anatlyzer.atl.graph.ErrorPathGenerator;
import anatlyzer.atl.graph.ProblemPath;
import anatlyzer.atl.quickfixast.InDocumentSerializer;
import anatlyzer.atl.quickfixast.QuickfixApplication;
import anatlyzer.atl.util.ATLSerializer;
import anatlyzer.atlext.OCL.OclExpression;

public abstract class GeneratePrecondition extends AbstractAtlQuickfix {

	private Class<?> applicableClass;
	private ProblemPath path;

	public GeneratePrecondition(Class<?> applicableClass) {
		this.applicableClass = applicableClass;
	}

	@Override
	public boolean isApplicable(IMarker marker) throws CoreException {
		boolean typeOk = checkProblemType(marker, applicableClass);
		if ( typeOk ) {
			setErrorMarker(marker);
			this.path = getProblemPath();
			return ! this.path.getExecutionNodes().isEmpty();
		}
		return false;
	}

	public ProblemPath getProblemPath() throws CoreException {
		if ( path != null )
			return path;
		
		LocalProblem p = (LocalProblem) getProblem();		
		AnalysisResult result = getAnalysisResult();
		
		ErrorPathGenerator pathgen = new ErrorPathGenerator(result.getAnalyser());		
		ProblemPath path = pathgen.generatePath(p);
		
		return path;
	}
	
	@Override public void resetCache() { 
		this.path = null;		
	}
	
	@Override
	public QuickfixApplication getQuickfixApplication() throws CoreException {
		OclExpression expr = getProblemPath().getWeakestPrecondition();
		
		if ( expr == null ) {
			MessageDialog.openWarning(null, "Error", "Dead code. Could not create a path");
			new QuickfixApplication(this); // does nothing
		}
		
		QuickfixApplication qfa = new QuickfixApplication(this);
		qfa.addCommentBefore(getATLModel().getRoot(), () -> {
			String pre = ATLSerializer.serialize(expr);
			pre = pre.replace("\n", "\n-- ");			
			return "-- @pre " + pre + "\n";
		});
		
		// This code was used to generate the pre-condition in USE format
		/*
		ErrorSlice slice = path.getErrorSlice(result.getAnalyser());
		String info = slice.toOneLineString();
		
		QuickfixApplication qfa = new QuickfixApplication(this);
		qfa.addCommentBefore(getATLModel().getRoot(), () -> {
			USEConstraint useConstraint = USESerializer.retypeAndGenerate(expr, path);	
			String pre = useConstraint.asString();
			pre = pre.replace("\n", "\n-- ");			
			return "-- @pre " + pre + "\n" + "-- @footprint " + info;
		});
		*/
		
		return qfa;
	}
	
	@Override
	public void apply(IDocument document) {
		try {
			QuickfixApplication qfa = getQuickfixApplication();
			new InDocumentSerializer(qfa, document).serialize();		
		} catch (CoreException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public String getDisplayString() {
		return "Generate precondition";
	}


	@Override public Image getImage() {
		return QuickfixImages.precondition.createImage();
	}
}
