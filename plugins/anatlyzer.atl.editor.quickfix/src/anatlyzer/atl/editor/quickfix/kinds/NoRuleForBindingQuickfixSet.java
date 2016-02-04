package anatlyzer.atl.editor.quickfix.kinds;

import anatlyzer.atl.editor.quickfix.AbstractQuickfixSet;
import anatlyzer.atl.editor.quickfix.AtlProblemQuickfix;
import anatlyzer.atl.editor.quickfix.errors.NoRuleForBindingQuickfix_AddRule;
import anatlyzer.atl.editor.quickfix.errors.NoRuleForBindingQuickfix_RemoveBinding;
import anatlyzer.atl.editor.quickfix.errors.NoRuleForBinding_FilterBinding;
import anatlyzer.atl.editor.quickfix.errors.NoRuleForBinding_Precondition;

public class NoRuleForBindingQuickfixSet extends AbstractQuickfixSet  {
		
	@Override
	public AtlProblemQuickfix[] getPossibleQuickfixes() {
		return new AtlProblemQuickfix[] {
				new NoRuleForBindingQuickfix_AddRule(),
				new NoRuleForBindingQuickfix_RemoveBinding(),
				new NoRuleForBinding_FilterBinding(),
				new NoRuleForBinding_Precondition()
		};
	}
}
