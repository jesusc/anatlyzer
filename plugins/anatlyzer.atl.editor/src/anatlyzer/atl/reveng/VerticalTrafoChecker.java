package anatlyzer.atl.reveng;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;

import anatlyzer.atl.model.ATLModel;
import anatlyzer.atl.types.Metaclass;
import anatlyzer.atl.util.ATLUtils;
import anatlyzer.atlext.ATL.MatchedRule;
import anatlyzer.atlext.processing.AbstractVisitor;

/**
 * 
 * An analysis to determine whether or not the transformation
 * is a vertical transformation: abstraction or refinement.
 * 
 * @author jesus
 *
 */
public class VerticalTrafoChecker extends AbstractVisitor {

	protected ATLModel model;
	protected HashMap<EClass, List<MatchedRule>> rulesByInputType = new HashMap<EClass, List<MatchedRule>>();
	protected HashMap<EClass, List<MatchedRule>> rulesByOutputType = new HashMap<EClass, List<MatchedRule>>();
	
	protected HashMap<EClass, MatchedRule> rulesOneToN = new HashMap<EClass, MatchedRule>();

	protected HashSet<MatchedRule> oneToOne = new HashSet<MatchedRule>();
	
	public VerticalTrafoChecker(ATLModel model) {
		this.model = model;
	}
	
	public Result perform() {
		startVisiting(model.getRoot());
		
		
		if ( rulesOneToN.size() > 0 ) {
			System.out.println("May be 1:N");
			printRules(rulesOneToN.values(), System.out);
			return new ResultOneToN(new HashSet<MatchedRule>(rulesOneToN.values()));
		}
		
		return checkAbstractionRefinement();
	}
	
	private Result checkAbstractionRefinement() {
		HashSet<MatchedRule> refinements  = new HashSet<MatchedRule>();
		HashSet<MatchedRule> abstractions = new HashSet<MatchedRule>();
		
		
		for (Entry<EClass, List<MatchedRule>> e1 : rulesByInputType.entrySet()) {
			// Two rules with the same input type (should have a filter)
			// and (probably) different output types or initializations
			// => Refinement
			//    Example: Transition -> Generator, Transition -> Terminator 
			if ( e1.getValue().size() > 1 ) {
				// Mark as refinement
				refinements.addAll(e1.getValue());
			}
		}
		
		for (Entry<EClass, List<MatchedRule>> e1 : rulesByOutputType.entrySet()) {
			// Two rules with the same output type 
			// (and possibly different input types, but this is checked above)
			// => Abstraction
			//    Example: Generator -> Transition, Terminator-> Transition
			if ( e1.getValue().size() > 1 ) {
				// Mark as abstractions
				abstractions.addAll(e1.getValue());
			}
		}
		
		
		// Possibility 1: Check that refinement and abstraction are disjunct!
		
		// Possibility 2: Check that there are either refinment or abstractions
		if ( abstractions.size() > 0 && refinements.isEmpty() ) {
			System.out.println("Abstraction.");
			printRules(abstractions, System.out);
			return new ResultAbstraction(abstractions);
		} else if ( refinements.size() > 0 && abstractions.isEmpty() ){
			System.out.println("Refinement.");
			printRules(refinements, System.out);
			return new ResultRefinement(refinements);
		} else if ( refinements.size() > 0 && abstractions.size() > 0 ) {
			System.out.println("NO abstraction or refinement.");
			refinements.retainAll(abstractions);
			printRules(refinements, System.out);
			return new ResultOther(refinements);
		} else {
			System.out.println("One-to-one.");
			return new ResultOneToOne(new HashSet<MatchedRule>());			
		}
		
		
		// TODO: How to handle supertype relationships?
		
	}

	private static void printRules(Collection<MatchedRule> set, PrintStream out) {
		for (MatchedRule matchedRule : set) {
			out.println("  - " + matchedRule.getName() + " (" + matchedRule.getLocation() + ")");
		}
	}

	@Override
	public void inMatchedRule(MatchedRule self) {
		if ( ! ATLUtils.isOneOneRule(self) ) 
			throw new UnsupportedOperationException("Don't know how to handle rules with more than one input pattern");
		
		Metaclass src = ATLUtils.getInPatternType(self);
		if ( self.getActionBlock() != null ) {
			// So far nothing...
			return;
		}
		
		if ( self.getOutPattern().getElements().size() == 1 ) {
			add(rulesByInputType, src.getKlass(), self);
			add(rulesByOutputType, ((Metaclass) self.getOutPattern().getElements().get(0).getInferredType()).getKlass(), self);

			oneToOne.add(self);
		} else {
			rulesOneToN.put(src.getKlass(), self);
		}
	}

	private void add(HashMap<EClass, List<MatchedRule>> rules, EClass klass, MatchedRule rule) {
		if ( ! rules.containsKey(klass) ) {
			rules.put(klass, new ArrayList<MatchedRule>());
		}
		
		rules.get(klass).add(rule);
	}
	
	public static abstract class Result {
		private Set<MatchedRule> rules;
		public Result(Set<MatchedRule> rules) {
			this.rules = rules;
		}
		
		public abstract String getTrafoType();
		public void printInfo(OutputStream stream) {
			PrintStream out = new PrintStream(stream);
			out.println(getTrafoType());
			printRules(rules, out);
		}
	}
	
	public class ResultAbstraction extends Result { 
		public ResultAbstraction(Set<MatchedRule> rules) {
			super(rules);
		}
		public String getTrafoType() { return "Abstraction"; }
	}
	public class ResultRefinement extends Result { 
		public ResultRefinement(Set<MatchedRule> rules) {
			super(rules);
		}
		public String getTrafoType() { return "Refinement"; }
	}
	public class ResultOneToOne extends Result { 
		public ResultOneToOne(Set<MatchedRule> rules) {
			super(rules);
		}
		public String getTrafoType() { return "One-to-one"; }
	}
	public class ResultOneToN extends Result { 
		public ResultOneToN(Set<MatchedRule> rules) {
			super(rules);
		}
		public String getTrafoType() { return "One-to-N (maybe)"; }
	}
	public class ResultOther extends Result { 
		public ResultOther() {
			super(new HashSet<MatchedRule>());
		}
		public ResultOther(Set<MatchedRule> rules) {
			super(rules);
		}
		public String getTrafoType() { return "Unknown"; }
	}

}

