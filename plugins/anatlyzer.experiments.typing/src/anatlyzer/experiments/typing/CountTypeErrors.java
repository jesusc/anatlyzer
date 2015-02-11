package anatlyzer.experiments.typing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import anatlyzer.atl.analyser.Analyser;
import anatlyzer.atl.editor.builder.AnalyserExecutor.AnalyserData;
import anatlyzer.atl.editor.builder.AnalyserExecutor.CannotLoadMetamodel;
import anatlyzer.examples.api.ErrorReport;
import anatlyzer.experiments.extensions.IExperiment;

public class CountTypeErrors extends AbstractATLExperiment implements IExperiment {

	List<AnalyserData> allData = new ArrayList<AnalyserData>();
	
	public CountTypeErrors() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void perform(IResource resource) {
		AnalyserData data;
		try {
			data = executeAnalyser(resource);
			if ( data == null )
				return;

			allData.add(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	@Override
	public void printResult(PrintStream out) {
		
		out.println("Transformations - Detail");
		for(AnalyserData data : allData) {
			out.println(data.getAnalyser().getATLModel().getFileLocations().get(0));
			
			Analyser analyser = data.getAnalyser();
			
			ByteArrayOutputStream outS = new ByteArrayOutputStream();
			String[] fileLocations = new String[analyser.getATLModel().getFileLocations().size()];
			int i = 0;
			for (String eclipseLocation : analyser.getATLModel().getFileLocations()) {
				fileLocations[i] = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(eclipseLocation)).getRawLocation().toPortableString();			
				i++;
			}
			ErrorReport.printStatistics(data.getAnalyser(), fileLocations, outS);
			ErrorReport.printErrorsByType(data.getAnalyser(), outS);
			
			out.print(outS.toString());
			
			// out.println(data.getProblems())
		}
	}

}