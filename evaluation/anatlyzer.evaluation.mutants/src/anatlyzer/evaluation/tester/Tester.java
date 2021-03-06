package anatlyzer.evaluation.tester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.m2m.atl.core.ATLCoreException;
import org.eclipse.m2m.atl.core.ModelFactory;
import org.eclipse.m2m.atl.core.emf.EMFModel;
import org.eclipse.m2m.atl.core.emf.EMFModelFactory;
import org.eclipse.m2m.atl.core.emf.EMFReferenceModel;
import org.eclipse.m2m.atl.engine.compiler.CompileTimeError;
import org.eclipse.m2m.atl.engine.compiler.atl2006.Atl2006Compiler;
import org.eclipse.m2m.atl.engine.parser.AtlParser;

import transML.exceptions.transException;
import transML.utils.transMLProperties;
import transML.utils.modeling.EMFUtils;
import transML.utils.modeling.TrafoEngine;
import transML.utils.solver.FactorySolver;
import transML.utils.solver.SolverWrapper;
import witness.generator.MetaModel;
import anatlyzer.atl.analyser.Analyser;
import anatlyzer.atl.analyser.AnalysisResult;
import anatlyzer.atl.analyser.batch.RuleConflictAnalysis.OverlappingRules;
import anatlyzer.atl.analyser.namespaces.GlobalNamespace;
import anatlyzer.atl.editor.builder.AnalyserExecutor;
import anatlyzer.atl.editor.builder.AnalyserExecutor.AnalyserData;
import anatlyzer.atl.editor.witness.EclipseUseWitnessFinder;
import anatlyzer.atl.errors.Problem;
import anatlyzer.atl.errors.ProblemStatus;
import anatlyzer.atl.errors.atl_error.BindingPossiblyUnresolved;
import anatlyzer.atl.errors.atl_error.BindingWithoutRule;
import anatlyzer.atl.errors.atl_error.ConflictingRuleSet;
import anatlyzer.atl.model.ATLModel;
import anatlyzer.atl.util.ATLSerializer;
import anatlyzer.atl.util.ATLUtils;
import anatlyzer.atl.util.ATLUtils.ModelInfo;
import anatlyzer.atl.util.AnalyserUtils;
import anatlyzer.atl.util.AnalyserUtils.CannotLoadMetamodel;
import anatlyzer.atl.util.AnalyserUtils.IAtlFileLoader;
import anatlyzer.atl.util.AnalyserUtils.PreconditionParseError;
import anatlyzer.atlext.ATL.Module;
import anatlyzer.evaluation.instrumentation.PossiblyUnresolvedBindingInstrumenter;
import anatlyzer.evaluation.models.FullModelGenerationStrategy;
import anatlyzer.evaluation.models.LiteModelGenerationStrategy;
import anatlyzer.evaluation.models.ModelGenerationStrategy;
import anatlyzer.evaluation.mutators.AbstractMutator;
import anatlyzer.evaluation.mutators.creation.BindingCreationMutator;
import anatlyzer.evaluation.mutators.creation.InElementCreationMutator;
import anatlyzer.evaluation.mutators.creation.OutElementCreationMutator;
import anatlyzer.evaluation.mutators.deletion.ArgumentDeletionMutator;
import anatlyzer.evaluation.mutators.deletion.BindingDeletionMutator;
import anatlyzer.evaluation.mutators.deletion.FilterDeletionMutator;
import anatlyzer.evaluation.mutators.deletion.HelperContextDeletionMutator;
import anatlyzer.evaluation.mutators.deletion.HelperDeletionMutator;
import anatlyzer.evaluation.mutators.deletion.InElementDeletionMutator;
import anatlyzer.evaluation.mutators.deletion.OutElementDeletionMutator;
import anatlyzer.evaluation.mutators.deletion.ParameterDeletionMutator;
import anatlyzer.evaluation.mutators.deletion.ParentRuleDeletionMutator;
import anatlyzer.evaluation.mutators.deletion.RuleDeletionMutator;
import anatlyzer.evaluation.mutators.deletion.VariableDeletionMutator;
import anatlyzer.evaluation.mutators.modification.ParentRuleModificationMutator;
import anatlyzer.evaluation.mutators.modification.PrimitiveValueModificationMutator;
import anatlyzer.evaluation.mutators.modification.feature.BindingModificationMutator;
import anatlyzer.evaluation.mutators.modification.feature.NavigationModificationMutator;
import anatlyzer.evaluation.mutators.modification.invocation.CollectionOperationModificationMutator;
import anatlyzer.evaluation.mutators.modification.invocation.HelperOperationModificationMutator;
import anatlyzer.evaluation.mutators.modification.invocation.IteratorModificationMutator;
import anatlyzer.evaluation.mutators.modification.invocation.OperatorModificationMutator;
import anatlyzer.evaluation.mutators.modification.invocation.PredefinedOperationModificationMutator;
import anatlyzer.evaluation.mutators.modification.type.ArgumentModificationMutator;
import anatlyzer.evaluation.mutators.modification.type.CollectionModificationMutator;
import anatlyzer.evaluation.mutators.modification.type.HelperContextModificationMutator;
import anatlyzer.evaluation.mutators.modification.type.HelperReturnModificationMutator;
import anatlyzer.evaluation.mutators.modification.type.InElementModificationMutator;
import anatlyzer.evaluation.mutators.modification.type.OutElementModificationMutator;
import anatlyzer.evaluation.mutators.modification.type.ParameterModificationMutator;
import anatlyzer.evaluation.mutators.modification.type.VariableModificationMutator;
import anatlyzer.evaluation.raw.MUData;
import anatlyzer.evaluation.raw.MUProblem;
import anatlyzer.evaluation.raw.MUTransformation;
import anatlyzer.evaluation.report.Report;
import anatlyzer.ui.actions.CheckRuleConflicts;
import anatlyzer.ui.util.AtlEngineUtils;

public class Tester {
	
	private String atlFile;            // name of original transformation
	private EMFModel atlModel;         // model of the original transformation
	private GlobalNamespace namespace; // meta-models used by the transformation (union of inputMetamodels and outputMetamodels)
	private List<String> inputMetamodels  = new ArrayList<String>(); // input metamodels (IN)
	private List<String> outputMetamodels = new ArrayList<String>(); // output metamodels (OUT)
    private HashMap<String, ModelInfo> aliasToPaths = new HashMap<String, ModelInfo>();
	private ResourceSet rs;
	private Report report;
	
	private MUData rawData;
	private MUTransformation currentMutant; // The mutant being evaluated (if != null)
	private long initTime;
	
	private ModelGenerationStrategy.STRATEGY modelGenerationStrategy;
	
	// temporal folders
	private String folderMutants;
	private String folderModels;
	private String folderTemp;

	// configuration options
	private boolean generateMutants = false;
	private boolean generateTestModels = false;
	private boolean alwaysCheckRuleConflicts = true;
	
	/**
	 * @param trafo transformation to be used in the evaluation
	 * @param temporalFolder temporal folder used to store the generated mutants and input test models
	 * @param strategy model generation strategy (Lite by default) 
	 * @throws ATLCoreException 
	 * @throws transException 
	 */
	public Tester (String trafo, String temporalFolder) throws ATLCoreException, transException { this (trafo, temporalFolder, ModelGenerationStrategy.STRATEGY.Lite);	}
	public Tester (String trafo, String temporalFolder, ModelGenerationStrategy.STRATEGY strategy) throws ATLCoreException, transException {
		this.rs       = new ResourceSetImpl();
		this.report   = new Report();
		this.atlFile  = trafo;
		this.atlModel = this.loadTransformationModel(trafo);
		this.loadMetamodelsFromTransformation();
		this.modelGenerationStrategy = strategy;
		// initialize temporal folders
		this.folderMutants = temporalFolder + "mutants" + File.separator;
		this.folderModels  = temporalFolder + "testmodels" + File.separator;
		this.folderTemp    = temporalFolder + "temp" + File.separator;
	}
	
	public void setGenerateMutants(boolean optionGenerateNewMutants) {
		this.generateMutants = optionGenerateNewMutants;
	}
	
	public void setGenerateTestModels(boolean optionGenerateTestModels) {
		this.generateTestModels = optionGenerateTestModels;
	}


	public void runEvaluation() throws transException, ATLCoreException, IOException {
		runEvaluation(new NullProgressMonitor());
	}
	
	
	/**
	 * It runs the evaluation, consisting on three steps:
	 * (1) generation of mutants of the original (correct) transformation
	 * (2) generation of input tests models
	 * (3) evaluation (for each mutant, check if the result of the anatlyzer is correct)  
	 * @throws transException 
	 * @throws ATLCoreException 
	 * @throws IOException 
	 */
	public void runEvaluation(IProgressMonitor monitor) throws transException, ATLCoreException, IOException {
		rawData = new MUData();
		initTime = System.currentTimeMillis();
		
		if ( generateTestModels ) {
			monitor.beginTask("Generating test models", IProgressMonitor.UNKNOWN);
			this.generateTestModels ();
			monitor.done();
		}
		
		// if the original transformation has no errors, perform the evaluation
		this.evaluateTransformation(atlFile);
		String error = "";
		if      (report.getAnatlyserNotifiesError(atlFile))   error = "The evaluation CANNOT be performed\nbecause the anatlyser detected an error in the transformation:\n\n"+report.getAnatlyserError(atlFile);
		else if (report.getAnatlyserDoesNotFinish(atlFile))   error = "The evaluation CANNOT be performed\nbecause the anatlyser raised an exception when analysing the transformation:\n\n"+report.getAnatlyserError(atlFile);
		else if (report.getExecutionRaisesException(atlFile)) error = "The evaluation CANNOT be performed\nbecause the transformation fails at runtime:\n\n"+report.getExecutionError(atlFile);
		else if (report.getExecutionYieldsIllTarget(atlFile)) error = "The evaluation CANNOT be performed\nbecause the transformation yields an incorrect target model:\n\n"+report.getExecutionError(atlFile);
		
		if (error.isEmpty()) {
			report.clear();
			
			if ( generateMutants ) {
				monitor.beginTask("Generating mutants", IProgressMonitor.UNKNOWN);
				this.generateMutants ();
				monitor.done();
			}
			
			this.evaluate(monitor);

			// Save the raw data
			rawData.setElapsedTime(getElapsedTime());
			rawData.save(atlFile.replace(".atl", ".expdata"));

			// Save the report as was done originally
//			this.deleteDirectory(this.folderTemp, true); // delete temporal folder
			this.printReport(URI.createURI(atlFile).trimSegments(1).toString());
			try {
				this.printReport(); // be aware that this may provoke an exception if the editor is closed...
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		
		// otherwise, return an error
		else report.printError(error);
	}
	
	/**
	 * It prints the result of the evaluation to the console.
	 */
	public void printReport () {
		report.print();
	}
	
	/**
	 * It prints the result of the evaluation to an excel file.
	 * @param String folder
	 */
	public void printReport (String folder) {
		report.print(folder);
	}
	
	/**
	 * It returns a report with the result of the evaluation.
	 */
	public Report getReport () {
		return report;
	}
	
	/**
	 * It generates mutants of a transformation.
	 * @throws transException 
	 * @throws ATLCoreException 
	 */
	/*private*/public void generateMutants () throws transException {
		MetaModel iMetaModel, oMetaModel;
		/*
		try {
			// TODO: consider several input/output metamodels
			String immname = this.namespace.getLogicalNamesToMetamodels().get(this.inputMetamodels.get (0)).getURI().path();
			String ommname = this.namespace.getLogicalNamesToMetamodels().get(this.outputMetamodels.get(0)).getURI().path();
			iMetaModel = new MetaModel(immname);
			oMetaModel = new MetaModel(ommname);
		} 
		catch (transException e)   { throw new RuntimeException(e); }
		*/
		// jesusc: To avoid problem with paths, using the meta-models already loaded in the transformation namespace
		iMetaModel = new MetaModel(new ArrayList<EPackage>(this.namespace.getNamespace(this.inputMetamodels.get(0)).getLoadedPackages()));
		oMetaModel = new MetaModel(new ArrayList<EPackage>(this.namespace.getNamespace(this.outputMetamodels.get(0)).getLoadedPackages()));
		iMetaModel.setName (this.namespace.getNamespace(this.inputMetamodels.get (0)).getName());
		oMetaModel.setName (this.namespace.getNamespace(this.outputMetamodels.get(0)).getName());
		
		// create output folder
		this.deleteDirectory(this.folderMutants, true);
		this.createDirectory(this.folderMutants);
		
		// generate mutants
		AbstractMutator[] operators = {
				// deletion
				new ArgumentDeletionMutator(),
				new BindingDeletionMutator(),
				new FilterDeletionMutator(),
				new HelperDeletionMutator(),
				new InElementDeletionMutator(),
				new OutElementDeletionMutator(),
				new ParameterDeletionMutator(),
				new ParentRuleDeletionMutator(),
				new RuleDeletionMutator(),
				new VariableDeletionMutator(),
				new HelperContextDeletionMutator(),
				// type modification
				new InElementModificationMutator(),
				new OutElementModificationMutator(),
				new HelperReturnModificationMutator(), 
				new HelperContextModificationMutator(),
				new CollectionModificationMutator(),
				new ParameterModificationMutator(),
				new ArgumentModificationMutator(),
				new VariableModificationMutator(),
				// feature modification
				new NavigationModificationMutator(), 
				new BindingModificationMutator(),
				// invocation modification
				new CollectionOperationModificationMutator(),
				new OperatorModificationMutator(),
				new PredefinedOperationModificationMutator(),
				new HelperOperationModificationMutator(),
				new IteratorModificationMutator(),
				// other modifications
				new ParentRuleModificationMutator(),
				new PrimitiveValueModificationMutator(),
				// creation
				new BindingCreationMutator(),
				new InElementCreationMutator(),
				new OutElementCreationMutator(),
		}; 
		for (AbstractMutator operator : operators) 
			operator.generateMutants(atlModel, iMetaModel, oMetaModel, this.folderMutants);
	}
	
	/**
	 * It generates instances of the input metamodel.
	 * @throws transException 
	 * @throws IOException 
	 */
	private void generateTestModels () throws transException, IOException {
		String metamodelName = this.inputMetamodels.get(0);
		Resource resource    = this.namespace.getLogicalNamesToMetamodels().get(metamodelName);
		// TODO: consider several input models
		EPackage metamodel   = null;
		for (EObject obj : resource.getContents()) {
			if (obj instanceof EPackage && ((EPackage)obj).getName().equals(metamodelName)) {						
				metamodel = (EPackage)obj;
				break;
			}
		}
		if (metamodel==null) metamodel = (EPackage)resource.getContents().get(0); // TODO: fix
		if (metamodel.getNsURI()==null) metamodel.setNsURI(aliasToPaths.get(metamodel.getName()).getURIorPath());
		
		// create temporal and output folders
		this.deleteDirectory(this.folderTemp, true);
		this.deleteDirectory(this.folderModels, true);
		this.createDirectory(this.folderTemp);
		
		// build arrays with the name of classes and references; they will be used to generate 
		// the scope for the number of objects for each class, and the number of links for each
		// reference, which will be different in each generated model.
		List<String>     classes    = new ArrayList<String>();
		List<String>     references = new ArrayList<String>();
		List<EReference> auxref     = new ArrayList<EReference>();
		for (EClassifier classifier : metamodel.getEClassifiers()) {
			if (classifier instanceof EClass) {
				if (!((EClass)classifier).isAbstract()) 
					classes.add(classifier.getName());
				for (EReference ref : ((EClass)classifier).getEReferences()) {
					// optimization: do not consider opposite
					if (!auxref.contains(ref.getEOpposite()))
						references.add(((EClass)classifier).getName()+"."+ref.getName());
					auxref.add(ref);
				}
			}
		}
		
		// initialize parameters for model generation
		Properties properties = new Properties(); 
		saveTransMLProperties(properties);
		
		// load transformation preconditions (defined as comments annotated by @pre)
		Module       module        = new ATLModel(atlModel.getResource()).getModule();
		List<String> preconditions = new ArrayList<String>();
		String tag = "@pre_use_format";
		for (String s : module.getCommentsBefore()) {
			if (s.contains(tag)) 
				preconditions.add(s.substring( s.indexOf(tag)+tag.length()).trim());
		}
		
		// generate models
		SolverWrapper solver = FactorySolver.getInstance().createSolverWrapper();
		ModelGenerationStrategy modelGenerationStrategy =
				this.modelGenerationStrategy == ModelGenerationStrategy.STRATEGY.Full?
				new FullModelGenerationStrategy(classes, references) :
				new LiteModelGenerationStrategy(classes, references) ;
		for (Properties propertiesUse : modelGenerationStrategy) {
			try {
				saveTransMLProperties(propertiesUse);
				String model = solver.find(metamodel, preconditions); // Collections.<String>emptyList());
				System.out.println("generated model: " + ( model!=null? model : "NONE" ));
			}
			catch (transException e) {
				String error = e.getDetails().length>0? e.getDetails()[0] : e.getMessage();
				if (error.endsWith("\n")) error = error.substring(0, error.lastIndexOf("\n"));
				System.out.println("[ERROR] " + error); 
			}
		}
		
		// move generated models to output folder
		this.moveDirectory (this.folderTemp + "models", this.folderModels);
	}
	
	/**
	 * It stores the received properties object as a transML properties file. 
	 * @throws transException 
	 */
	private void saveTransMLProperties(Properties properties) throws transException {
		try {
			File file = new File(this.folderTemp, "transml.properties");
			FileOutputStream fileOut = new FileOutputStream(file);
    		properties.put("solver", "use");
			properties.put("solver.scope", "10");
	    	properties.put("temp", new File(this.folderTemp).getAbsolutePath()); 
			properties.store(fileOut, "--"); 
			fileOut.close();
		}
		catch (IOException e) { e.printStackTrace(); }
		transMLProperties.loadPropertiesFile(this.folderTemp);
	}
	
	/**
	 * For each transformation in folderTransformations, it checks whether its anatlysis yields a correct result. 
	 * @param monitor 
	 */
	private void evaluate (IProgressMonitor monitor) {
		File[] trafos = new File(this.folderMutants).listFiles(
			new FilenameFilter() {
				public boolean accept(File directory, String fileName) {
					return  fileName.endsWith(".atl") && 
							! (fileName.endsWith(".inst.atl")); // to avoid processing instrumented transformations which are only needed for execution 
				}
			});		
		
		monitor.beginTask("Evaluating mutants", trafos.length);
		
		for(int i = 0; i < trafos.length; i++) {
			File transformation = trafos[i];
			
			monitor.subTask("Evaluating " + transformation.getName() + " (" + i + "/" + trafos.length + "). Elapsed time: " + getElapsedTime() + " minutes");
			
			evaluateTransformation(transformation.getPath());
		
			monitor.worked(1);
			if ( monitor.isCanceled() ) {
				throw new EvaluationFinishedOnRequest();
			}
		}
	}
	
	/**
	 * It checks whether the result of anatlysing the received transformation is correct.
	 * This is checked by comparing the result of the anatlysis and the result of executing
	 * the transformation (in particular, whether the execution crashes or yields an output
	 * model that is not conformant to the output metamodel). Both the anatlysis and the
	 * execution should yield the same result (both successful or unsuccessful).
	 * @param transformation
	 */
	private void evaluateTransformation (String transformation) {		
		System.out.println("evaluating " + transformation + "...");
		
		currentMutant = new MUTransformation(transformation, transformation);
		rawData.addMutant(currentMutant);
		
		List<Problem> errorsAnatlyser = null;
		boolean errorsExecution = false;
		
		// initialize report
		report.addResult(transformation);

		// anatlyse transformation
		try {	
			errorsAnatlyser = this.typing(transformation);			
			errorsAnatlyser.forEach(p -> currentMutant.addProblem(new MUProblem(p)));
			
			if (!errorsAnatlyser.isEmpty()) 
				report.setAnatlyserError(transformation, errorsAnatlyser.stream().map(error -> error.getDescription() + " (" + error.getStatus() + ")").collect(Collectors.joining(";")) );
		}
		catch (Exception e) { 
			currentMutant.analysisError(e);
			report.setAnatlyserException(transformation, e.getMessage()); 
		}
		
		// execute transformation
		errorsExecution = this.executeTransformation(transformation);

		// if there are no execution errors, but the anatlyser reported the error "possibly unresolved binding", instrument the transformation to make it fail.
		// TODO: devolver lista de errores en vez de String (solo hago esto si ese es el unico error del anatlyzer)
		// TODO: where is the error description ??? => System.out.println("---->"+AtlErrorFactory.eINSTANCE.createAccessToUndefinedValue().getDescription()); // <-- this is null
	    if (!errorsExecution && errorsAnatlyser != null && needsInstrumentation(errorsAnatlyser)) {
			try {
				java.nio.file.Path transformation_path      = new File(transformation).toPath();
				java.nio.file.Path transformation_back_path = new File(transformation+".back").toPath();
				java.nio.file.Path transformation_inst_path = new File(transformation.replace(".atl", ".inst.atl")).toPath();
		    	// - instrument transformation
				IWorkspace workspace = ResourcesPlugin.getWorkspace();    
				IPath      location  = Path.fromOSString(transformation);
				IFile      ifile     = workspace.getRoot().getFileForLocation(location);
				ifile.refreshLocal(IResource.DEPTH_ZERO, null);
				AnalyserData result  = new AnalyserExecutor().exec(ifile, false);
				ATLModel   atlModel  = result.getATLModel();
				PossiblyUnresolvedBindingInstrumenter instrumenter = new PossiblyUnresolvedBindingInstrumenter();
				instrumenter.instrumentModel(atlModel);
				// - save instrumented transformation (use name of original transformation)
				Files.copy(transformation_path, transformation_back_path, StandardCopyOption.REPLACE_EXISTING);
				ATLSerializer.serialize(atlModel, transformation);
				if (compileTransformation(transformation, true)==false) return;
				// - execute instrumented transformation
				errorsExecution = this.executeTransformation(transformation);
				Files.copy  (transformation_path, transformation_inst_path, StandardCopyOption.REPLACE_EXISTING);
				Files.copy  (transformation_back_path, transformation_path, StandardCopyOption.REPLACE_EXISTING);
				Files.delete(transformation_back_path);
				if (compileTransformation(transformation, true)==false) return;
			} 
			catch (IOException | CoreException | CannotLoadMetamodel | PreconditionParseError e) { e.printStackTrace(); }
		}
	}

	private boolean needsInstrumentation(List<Problem> errorsAnatlyser) {
		return errorsAnatlyser.stream().anyMatch(p -> (p instanceof BindingPossiblyUnresolved) || (p instanceof BindingWithoutRule)); 
	}
	
	/**
	 * It compiles an atl transformation file. 
	 * @param atlTransformationFile
	 * @boolean forceCompilation false (by default, compile if no <atlTransformatinFile>.asm exists); true (compile anyway)  
	 * @return true if the compilation was successful, false if there were compilation errors.
	 */
	private boolean compileTransformation (String atlTransformationFile) { return compileTransformation(atlTransformationFile, false);	}
	private boolean compileTransformation (String atlTransformationFile, boolean forceCompilation) {
		CompileTimeError[] compileErrors         = {};
		String             asmTransformationFile = atlTransformationFile.replace(".atl", ".asm");
		
		if (!new File(asmTransformationFile).exists() || forceCompilation) {
			Atl2006Compiler compiler  = new Atl2006Compiler();
			FileInputStream trafoFile;
			try {
				trafoFile = new FileInputStream(new File(atlTransformationFile));
				compileErrors = compiler.compile(trafoFile, asmTransformationFile);
				trafoFile.close();
			} 
			catch (FileNotFoundException e) { return false; } 
			catch (IOException e)           { return false; }
		}
		
		return !(compileErrors!=null && compileErrors.length>0);
	}
	
	/**
	 * It loads a transformation as a model.
	 * @param atlTransformationFile 
	 * @return transformation model
	 * @throws ATLCoreException
	 */
	private EMFModel loadTransformationModel (String atlTransformationFile) throws ATLCoreException {
		ModelFactory      modelFactory = new EMFModelFactory();
		EMFReferenceModel atlMetamodel = (EMFReferenceModel)modelFactory.getBuiltInResource("ATL.ecore");
		AtlParser         atlParser    = new AtlParser();		
		EMFModel          atlModel     = (EMFModel)modelFactory.newModel(atlMetamodel);
		atlParser.inject (atlModel, atlTransformationFile);	
		atlModel.setIsTarget(true);				
		
//		// Should we want to serialize the model.
//		String injectedFile = "file:/" + atlTransformationFile + ".xmi";
//		IExtractor extractor = new EMFExtractor();
//		extractor.extract(atlModel, injectedFile);
		
		return atlModel;
	}
	
	/**
	 * It perform the type checking phase of a transformation. Method used by evaluateTransformation.
	 * @param atlTransformationFile
	 * @return list of confirmed problems
	 * @throws IOException
	 * @throws ATLCoreException
	 * @throws PreconditionParseError 
	 * @throws CannotLoadMetamodel 
	 * @throws CoreException 
	 */
	private List<Problem> typing (String atlTransformationFile) throws IOException, ATLCoreException, CoreException, CannotLoadMetamodel, PreconditionParseError {
		ProblemStatus status  = null;
		ArrayList<Problem> result = new ArrayList<Problem>();
		
		// the anatlyser needs to create the global namespace each time...
		/*
		ResourceSet               rs                      = new ResourceSetImpl();
		HashMap<String, Resource> logicalNamesToResources = new HashMap<String, Resource>();
		for (String metamodel : this.namespace.getLogicalNamesToMetamodels().keySet()) {
			Resource resource = this.namespace.getLogicalNamesToMetamodels().get(metamodel);
			Resource refresh  = rs.getResource(resource.getURI(), true);
			logicalNamesToResources.put(metamodel, refresh);
		}
		GlobalNamespace namespace = new GlobalNamespace(logicalNamesToResources.values(), logicalNamesToResources);
		*/
		
	
		// load transformation
		EMFModel  atlModel          = this.loadTransformationModel(atlTransformationFile);			
		ATLModel  atlTransformation = new ATLModel(atlModel.getResource());

		// The prepare method is in charge of setting up the meta-models but also
		// configuring libraries (indicated with @lib) and pre-conditions (@pre)
		GlobalNamespace namespace = AnalyserUtils.prepare(atlTransformation, new IAtlFileLoader() {			
			@Override
			public Resource load(IFile f) {
				EMFModel libModel = AtlEngineUtils.loadATLFile(f);
				return libModel.getResource();
			}
			
			@Override
			public Resource load(String text) {
				EMFModel libModel = AtlEngineUtils.loadATLText(text);
				return libModel.getResource();
			}
		});

		
		// anatlyse
		Analyser analyser = new Analyser(namespace, atlTransformation);
		analyser.perform();
		
		// build list of problems found 
		for (Problem error : analyser.getErrors().getAnalysis().getProblems())  {
			status = error.getStatus();
			// if needed, generate witness to confirm the problem
			if (status==ProblemStatus.WITNESS_REQUIRED) 
				try                 { status = new EclipseUseWitnessFinder().checkDiscardCause(false).find(error, new AnalysisResult(analyser)); error.setStatus(status); }
				catch (Exception e) { status = ProblemStatus.IMPL_INTERNAL_ERROR; }	
			// the problem has been confirmed
			if ( AnalyserUtils.isConfirmed(status) ) { 				
				result.add(error);
			}
			// if (status != ProblemStatus.ERROR_DISCARDED) problem += error.getDescription() + " (" + status + ") ;";
		}
		
		// if no problem was found, check rule conflicts
		// if (problem.isEmpty() || status == ProblemStatus.IMPL_INTERNAL_ERROR) {
		if ( result.isEmpty() || alwaysCheckRuleConflicts  ) {
			try { 
				List<OverlappingRules> rules = new CheckRuleConflicts().performAction(new AnalyserData(analyser), null);
				rules.stream().filter(or -> AnalyserUtils.isConfirmed(or.getAnalysisResult())).findAny().ifPresent(or -> {
					ConflictingRuleSet theError = or.createRuleSet();
					result.add(theError);
				});
			} catch ( Exception e ) {
				// An internal error occurred
				// Could be recorded as well, but it is likely due to another problems that have already been discovered
			}
			
			// if      (rules.stream().anyMatch(or -> or.getAnalysisResult() == ProblemStatus.ERROR_CONFIRMED))      problem = "CONFLICT: " + ProblemStatus.ERROR_CONFIRMED.getLiteral();
			// else if (rules.stream().anyMatch(or -> or.getAnalysisResult() == ProblemStatus.STATICALLY_CONFIRMED)) problem = "CONFLICT: " + ProblemStatus.STATICALLY_CONFIRMED.getLiteral();
			//for (OverlappingRules or : rules) System.out.println("------------->"+or.getAnalysisResult() );
		}
		
		return result;
	}
	
	/**
	 * It executes a transformation for all input models. Method used by evaluateTransformation.
	 * @param transformation Name of atl transformation file.
	 * @param exhaustive (OPTIONAL) If it is true, it will execute the transformation for all
	 *        input models. If it is false (by default) it will execute the transformation until 
	 *        it fails for some input model.
	 * @return true if error, false otherwise
	 * @throws IOException
	 * @throws ATLCoreException
	 */
	private boolean executeTransformation (String transformation) { return executeTransformation(transformation, false); }		
	private boolean executeTransformation (String transformation, boolean exhaustive) {
		boolean error = false;

		// name of input/output metamodels of the transformation
		// TODO: there may be several input/output metamodels
		String immAlias = this.inputMetamodels.get(0);
		String ommAlias = this.outputMetamodels.get(0);
		String aux      = URI.createFileURI(transformation).lastSegment();
		String oFolder  = aux.substring(0, aux.lastIndexOf("."));
		
		// compilation is performed when the mutant is generated, but just in case...
		if (compileTransformation(transformation)==false) return true;

		try {
			// load transformation
			String transformation_asm = transformation.replace(".atl", ".asm");
			TrafoEngine engine = new AnATLyzerATLEngine();
			engine.loadTransformation(transformation_asm);

			// obtain input test models 
			File[] inputModels = new File(folderModels).listFiles(
				new FilenameFilter() {
					public boolean accept(File directory, String fileName) {
						return fileName.endsWith(".model"); 
					}
				});
			
			// for each input test model
			for (File inputModel : inputModels) {
				
				// load input/output model
				String iModel  = inputModel.getPath();
				String oModel  = this.folderTemp + oFolder + File.separator + inputModel.getName(); // generate output model in temporal folder, because it will be deleted
				engine.loadSourcemodel(immAlias, iModel, aliasToPaths.get(immAlias).getURIorPath()); 
				engine.loadTargetmodel(ommAlias, oModel, aliasToPaths.get(ommAlias).getURIorPath());

				// execute transformation
				try {
					// check whether the transformation does not crash
					if (engine.execute()) {						
						// check whether the output model is conformant to the output metamodel
						// TODO: check OCL constraints of the meta-model as well						
						URI uri = URI.createFileURI(oModel);
						Resource resource = rs.getResource(uri, true);
						for (EObject eObject : resource.getContents()) {
							Diagnostic diagnostic = Diagnostician.INSTANCE.validate(eObject);
							if (diagnostic.getSeverity() != Diagnostic.OK) {
								error = report.setOutputError(transformation, ((BasicDiagnostic)diagnostic.getChildren().get(0)).getMessage(), inputModel.getName());
								currentMutant.setMutantError(diagnostic, inputModel.getName());
							}
						}
					}
					else {
						error = report.setExecutionError(transformation, "EXECUTION_ERROR", inputModel.getName());
						currentMutant.setMutantError("EXECUTION_ERROR", inputModel.getName());
					}
				}
				catch (transException e) { 
					error = report.setExecutionError(transformation, e.getDetails().length>0? e.getDetails()[0] : e.getMessage(), inputModel.getName()); 
					currentMutant.setMutantError( e.getDetails().length>0? e.getDetails()[0] : e.getMessage(), inputModel.getName());
				}
				
				if (error && !exhaustive) break;
			}
		}
		catch (transException e) { System.out.println("******** REVISE: EXECUTION_ERROR (" + transformation + ")"); }
		finally { deleteDirectory(oFolder, true); }
		
		return error;
	}
	
	/**
	 * It loads the metamodels used by the transformation. The path of the meta-models must
	 * be defined as comments (starting by -- @path) at the beginning of the transformation.
	 * @return
	 */
	private void loadMetamodelsFromTransformation() throws transException {
		/*
		HashMap<String, Resource> logicalNamesToResources = new HashMap<String, Resource>();
		ArrayList<Resource>       resources               = new ArrayList<Resource>();
		ATLModel                  wrapper                 = new ATLModel(atlModel.getResource());
		List<Module>              modules                 = (List<Module>)wrapper.allObjectsOf(Module.class);
		
		for (Module module : modules) {
			
			// obtain path of meta-models from the transformation
			for (String comment : module.getCommentsBefore()) {
				comment = comment.trim();
				if (comment.startsWith("-- @path")) {
					comment = comment.substring(8).trim();
					String[] path = comment.split("=");
					try {
						String uri = path[1].trim();
						this.loadMetamodel(uri);
						Resource r = rs.getResource(URI.createFileURI(uri), true);
						resources.add(r);
						logicalNamesToResources.put(path[0].trim(), r);
					}
					catch (Exception e) {
						throw new transException(transException.ERROR.FILE_NOT_FOUND, path[1].trim());
					}
				}
			}
			
			// obtain metamodel of IN models of the transformation
			for (OclModel model : module.getInModels()) {
				String metamodel = model.getMetamodel().getName();
				if (!logicalNamesToResources.containsKey(metamodel))
					throw new transException(transException.ERROR.GENERIC_ERROR, "Path of metamodel "+metamodel+" not found");
				else this.inputMetamodels.add(metamodel);
			}
			
			// obtain metamodel of OUT models of the transformation
			for (OclModel model : module.getOutModels()) {
				String metamodel = model.getMetamodel().getName();
				if (!logicalNamesToResources.containsKey(metamodel))
					throw new transException(transException.ERROR.GENERIC_ERROR, "Path of metamodel "+metamodel+" not found");
				else this.outputMetamodels.add(metamodel);
			}
		}
		
		this.namespace = new GlobalNamespace(resources, logicalNamesToResources);
		*/
		
		// register ecore factory
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		
		try {
			ATLModel tmpAtlModel = new ATLModel(atlModel.getResource());
			this.namespace = AnalyserUtils.prepare(tmpAtlModel, new IAtlFileLoader() {			
				@Override
				public Resource load(IFile f) {
					EMFModel libModel = AtlEngineUtils.loadATLFile(f);
					return libModel.getResource();
				}

				@Override
				public Resource load(String text) {
					EMFModel libModel = AtlEngineUtils.loadATLText(text);
					return libModel.getResource();
				}
			});
			
			for (ModelInfo info : ATLUtils.getModelInfo(tmpAtlModel)) {
				if (info.isInput()) 
					 this.inputMetamodels.add (info.getMetamodelName());
				else this.outputMetamodels.add(info.getMetamodelName());
				aliasToPaths.put(info.getMetamodelName(), info);

				// register metamodel
				List<EPackage> metamodels = EMFUtils.loadEcoreMetamodel(info.getURIorPath());
		        for (EPackage p: metamodels) {
		        	if (p.getNsURI()!=null && !p.getNsURI().equals("")) rs.getPackageRegistry().put(p.getNsURI(), p);
		        	if (p.getName().equals(info.getMetamodelName()))    rs.getPackageRegistry().put(info.getMetamodelName(), p);
		        	
		        	// assign instance class name to data types (it is empty in uml/kermeta meta-models)
		        	for (EClassifier classifier : p.getEClassifiers())
		        		if (classifier instanceof EDataType)
		        			if (((EDataType)classifier).getInstanceClassName() == null)
		        				if      (classifier.getName().equals("String"))  ((EDataType)classifier).setInstanceClassName("java.lang.String");
		        				else if (classifier.getName().equals("Integer")) ((EDataType)classifier).setInstanceClassName("java.lang.Integer");
		        				else if (classifier.getName().equals("Boolean")) ((EDataType)classifier).setInstanceClassName("java.lang.Boolean");
		        }
			}
		} 
		catch (CoreException | CannotLoadMetamodel | PreconditionParseError e ) {
			throw new transException(transException.ERROR.GENERIC_ERROR, e.getMessage());
		}
	}
	
	/**
	 * It deletes a directory.
	 * @param folder name of directory
	 * @param recursive it deletes the subdirectories recursively
	 */
	private void deleteDirectory (String directory, boolean recursive) {
		File folder = new File(directory);
		if (folder.exists())
			for (File file : folder.listFiles()) {				
				if (file.isDirectory()) deleteDirectory(file.getPath(), recursive);
				file.delete();
			}
		folder.delete();
	}
	
	/**
	 * It creates a directory.
	 * @param folder name of directory
	 */
	private void createDirectory (String directory) {
		File folder = new File(directory);
		while (!folder.exists()) 
			folder.mkdir();
	}
	
	/**
	 * It moves a source directory to a target directory.
	 * @param sourceDirectory
	 * @param targetDirectory
	 * @throws IOException
	 */
	private void moveDirectory (String sourceDirectory, String targetDirectory) throws IOException {
		File source = new File(sourceDirectory);
		File target = new File(targetDirectory);
		Files.move(source.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE);
	}
	
	/**
	 * Computes the time in minutes since the evaluation started.
	 */
	public long getElapsedTime() {
		long diff = System.currentTimeMillis() - initTime;
		return diff / (1000 * 60);
	}
	
}
