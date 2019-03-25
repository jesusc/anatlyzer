package anatlyzer.ocl.emf;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ocl.pivot.CollectionKind;

import anatlyzer.atl.model.TypeUtils;
import anatlyzer.atl.types.BagType;
import anatlyzer.atl.types.CollectionType;
import anatlyzer.atl.types.OrderedSetType;
import anatlyzer.atl.types.SequenceType;
import anatlyzer.atl.types.SetType;
import anatlyzer.atl.witness.ConstraintSatisfactionChecker.IOCLDialectTransformer;
import anatlyzer.atlext.ATL.Unit;
import anatlyzer.atlext.OCL.Attribute;
import anatlyzer.atlext.OCL.CollectionOperationCallExp;
import anatlyzer.atlext.OCL.Iterator;
import anatlyzer.atlext.OCL.IteratorExp;
import anatlyzer.atlext.OCL.NavigationOrAttributeCallExp;
import anatlyzer.atlext.OCL.OCLFactory;
import anatlyzer.atlext.OCL.OclModelElement;
import anatlyzer.atlext.OCL.OclType;
import anatlyzer.atlext.OCL.Operation;
import anatlyzer.atlext.OCL.OperationCallExp;
import anatlyzer.atlext.OCL.PropertyCallExp;
import anatlyzer.atlext.OCL.SetExp;
import anatlyzer.atlext.processing.AbstractVisitor;

public class EMFOCL2UseFixer {

	public static class Pre extends AbstractVisitor implements IOCLDialectTransformer { 
		
		@Override
		public void adapt(Unit u) {
			startVisiting(u);
		}
	
		@Override
		public void inIteratorExp(IteratorExp self) {
			// In EMF/OCL collect flattens automatically
			if ( self.getName().equals("collect") ) {
				CollectionOperationCallExp flatten = OCLFactory.eINSTANCE.createCollectionOperationCallExp();
				flatten.setOperationName("flatten");
				EcoreUtil.replace(self, flatten);
				flatten.setSource(self);
			} 
		}
		
		// oclAsSet
		@Override
		public void inOperationCallExp(OperationCallExp self) {
			if ( self.getOperationName().equals("oclContainer")) {
				// Map to refImmediateComposite() because it is what we use internally
				self.setOperationName("refImmediateComposite");
			} else if ( self.getOperationName().equals("oclAsSet") ) {
				self.setOperationName("asSet");
			}
		}
	}

	public static class Post extends AbstractVisitor implements IOCLDialectTransformer { 
	
		private Map<EObject, CollectionKind> collectionAttr = new HashMap<>();
		private static final Map<String, CollectionKind> COL_MAPPING = new HashMap<String, CollectionKind>();
		static {
			COL_MAPPING.put("collect", CollectionKind.BAG);
		}
		
		@Override
		public void adapt(Unit u) {
			startVisiting(u);
		}
	
		@Override
		public void inAttribute(Attribute self) {
			CollectionKind kind = getColAttr(self.getInitExpression());
			if ( kind != null && self.getType() instanceof anatlyzer.atlext.OCL.CollectionType ) {
				OclType nested = ((anatlyzer.atlext.OCL.CollectionType) self.getType()).getElementType();
				self.setType(toATLCollectionType(kind, nested));
			}
		}
		
		@Override
		public void inOperation(Operation self) {			
			CollectionKind kind = getColAttr(self.getBody());
			if ( kind != null && self.getReturnType() instanceof anatlyzer.atlext.OCL.CollectionType ) {
				OclType nested = ((anatlyzer.atlext.OCL.CollectionType) self.getReturnType()).getElementType();
				self.setReturnType(toATLCollectionType(kind, nested));
			}
		}
		
		@Override
		public void inNavigationOrAttributeCallExp(NavigationOrAttributeCallExp self) {
			if ( self.getInferredType() instanceof CollectionType ) {
				// TODO: Ask Martin about this, it seems that associations in USE are always SET
				// collectionAttr.put(self, toCollectionKind((CollectionType) self.getInferredType()));
				setColAttr(self, CollectionKind.SET);
			}
		}

		@Override
		public void inCollectionOperationCallExp(CollectionOperationCallExp self) {
			handleCollectionKindMapping(self, self.getOperationName());
		}
		
		@Override
		public void inIteratorExp(IteratorExp self) {
			handleCollectionKindMapping(self, self.getName());
		}

		private void handleCollectionKindMapping(PropertyCallExp self, String name) {
			if ( COL_MAPPING.containsKey(name) ) {
				setColAttr(self, COL_MAPPING.get(name));
			} else {
				// by default we just propagate
				setColAttr(self, getColAttr(self.getSource()));
			}
		}
		

		// oclAsSet
		@Override
		public void inOperationCallExp(OperationCallExp self) {
			// oclAsSet is valid in a non-collection
			if ( self.getOperationName().equals("asSet") && ! TypeUtils.isCollection(self.getSource().getInferredType())) {
				SetExp set = OCLFactory.eINSTANCE.createSetExp();
				EcoreUtil.replace(self, set);			
				set.getElements().add(self.getSource());
			} else if ( self.getOperationName().equals("allInstances") ) {
				setColAttr(self, CollectionKind.SET);
			}
		}
		
		/**
		 * EMF OCL manual: 3.12. OclElement
		 * "The type OclElement is the implicit supertype of any user-defined type that has no explicit supertypes.
		 *  Operations defined for OclElement"
		 */
		@Override
		public void inOclModelElement(OclModelElement self) {
			// This is not available in USE, but OclElement conforms to OclAny, so let's use OclAny
			if ( self.getName().equals("OclElement")) {
				EcoreUtil.replace(self, OCLFactory.eINSTANCE.createOclAnyType());
			}
		}
		
		private CollectionKind toCollectionKind(CollectionType c) {
			if ( c instanceof SequenceType ) 
				return CollectionKind.SEQUENCE;
			else if ( c instanceof SetType ) 
				return CollectionKind.SET;
			else if ( c instanceof OrderedSetType ) 
				return CollectionKind.ORDERED_SET;
			else if ( c instanceof BagType ) 
				return CollectionKind.BAG;
			else
				throw new UnsupportedOperationException("Not supported " + c);
		}

		private OclType toATLCollectionType(CollectionKind kind, OclType nested) {
			anatlyzer.atlext.OCL.CollectionType t;
			switch (kind) {
			case SET: 
				t = OCLFactory.eINSTANCE.createSetType();
				break;
			case SEQUENCE: 
				t = OCLFactory.eINSTANCE.createSequenceType();
				break;
			case BAG: 
				t = OCLFactory.eINSTANCE.createBagType();
				break;
			case ORDERED_SET: 
				t = OCLFactory.eINSTANCE.createOrderedSetType();
				break;
			default:
				throw new IllegalStateException();
			}

			t.setElementType(nested);
			return t;
		}


		private CollectionKind getColAttr(EObject self) {
			return collectionAttr.get(self);
		}
		
		private void setColAttr(EObject self, CollectionKind kind) {
			collectionAttr.put(self, kind);
		}
	}

	
}
