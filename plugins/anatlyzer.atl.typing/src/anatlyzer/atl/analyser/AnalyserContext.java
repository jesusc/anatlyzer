package anatlyzer.atl.analyser;

import anatlyzer.atl.model.ErrorModel;
import anatlyzer.atl.model.TypingModel;

public class AnalyserContext {
	private static ThreadLocal<TypingModel> typingModelTL = new ThreadLocal<TypingModel>();
	private static ThreadLocal<ErrorModel>  errorModelTL  = new ThreadLocal<ErrorModel>();
	private static ThreadLocal<EcoreTypeConverter> converterTL  = new ThreadLocal<EcoreTypeConverter>();
	private static boolean	isVarDclInferencePreferred = true;
	private static boolean	isOclStrict = true;
	
	public static void setTypingModel(TypingModel value) {
		typingModelTL.set(value);
		converterTL.set(new EcoreTypeConverter(value));
	}
	
	public static TypingModel getTypingModel() {
		return typingModelTL.get();
	}
	
	public static void setErrorModel(ErrorModel value) {
		errorModelTL.set(value);
	}
	
	public static ErrorModel getErrorModel() {
		return errorModelTL.get();
	}
	
	public static EcoreTypeConverter getConverter() {
		return converterTL.get();
	}

	public static boolean isVarDclInferencePreferred() {
		return isVarDclInferencePreferred ;
	}

	public static boolean isOclStrict() {
		return isOclStrict ;
	}
}
