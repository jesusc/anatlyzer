/**
 */
package anatlyzer.atl.errors.atl_error.impl;

import anatlyzer.atl.errors.atl_error.AtlErrorPackage;
import anatlyzer.atl.errors.atl_error.BindingResolution;
import anatlyzer.atl.errors.atl_error.BindingWithResolvedByIncompatibleRule;
import anatlyzer.atl.errors.atl_error.ResolvedRuleInfo;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Binding With Resolved By Incompatible Rule</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link anatlyzer.atl.errors.atl_error.impl.BindingWithResolvedByIncompatibleRuleImpl#getRightType <em>Right Type</em>}</li>
 *   <li>{@link anatlyzer.atl.errors.atl_error.impl.BindingWithResolvedByIncompatibleRuleImpl#getTargetType <em>Target Type</em>}</li>
 *   <li>{@link anatlyzer.atl.errors.atl_error.impl.BindingWithResolvedByIncompatibleRuleImpl#getRules <em>Rules</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class BindingWithResolvedByIncompatibleRuleImpl extends BindingProblemImpl implements BindingWithResolvedByIncompatibleRule {
	/**
	 * The cached value of the '{@link #getRightType() <em>Right Type</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRightType()
	 * @generated
	 * @ordered
	 */
	protected EClass rightType;

	/**
	 * The cached value of the '{@link #getTargetType() <em>Target Type</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTargetType()
	 * @generated
	 * @ordered
	 */
	protected EClass targetType;

	/**
	 * The cached value of the '{@link #getRules() <em>Rules</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRules()
	 * @generated
	 * @ordered
	 */
	protected EList<ResolvedRuleInfo> rules;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BindingWithResolvedByIncompatibleRuleImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return AtlErrorPackage.Literals.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRightType() {
		if (rightType != null && rightType.eIsProxy()) {
			InternalEObject oldRightType = (InternalEObject)rightType;
			rightType = (EClass)eResolveProxy(oldRightType);
			if (rightType != oldRightType) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RIGHT_TYPE, oldRightType, rightType));
			}
		}
		return rightType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass basicGetRightType() {
		return rightType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRightType(EClass newRightType) {
		EClass oldRightType = rightType;
		rightType = newRightType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RIGHT_TYPE, oldRightType, rightType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTargetType() {
		if (targetType != null && targetType.eIsProxy()) {
			InternalEObject oldTargetType = (InternalEObject)targetType;
			targetType = (EClass)eResolveProxy(oldTargetType);
			if (targetType != oldTargetType) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__TARGET_TYPE, oldTargetType, targetType));
			}
		}
		return targetType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass basicGetTargetType() {
		return targetType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTargetType(EClass newTargetType) {
		EClass oldTargetType = targetType;
		targetType = newTargetType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__TARGET_TYPE, oldTargetType, targetType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<ResolvedRuleInfo> getRules() {
		if (rules == null) {
			rules = new EObjectContainmentEList<ResolvedRuleInfo>(ResolvedRuleInfo.class, this, AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RULES);
		}
		return rules;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RULES:
				return ((InternalEList<?>)getRules()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RIGHT_TYPE:
				if (resolve) return getRightType();
				return basicGetRightType();
			case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__TARGET_TYPE:
				if (resolve) return getTargetType();
				return basicGetTargetType();
			case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RULES:
				return getRules();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RIGHT_TYPE:
				setRightType((EClass)newValue);
				return;
			case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__TARGET_TYPE:
				setTargetType((EClass)newValue);
				return;
			case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RULES:
				getRules().clear();
				getRules().addAll((Collection<? extends ResolvedRuleInfo>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RIGHT_TYPE:
				setRightType((EClass)null);
				return;
			case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__TARGET_TYPE:
				setTargetType((EClass)null);
				return;
			case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RULES:
				getRules().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RIGHT_TYPE:
				return rightType != null;
			case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__TARGET_TYPE:
				return targetType != null;
			case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RULES:
				return rules != null && !rules.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == BindingResolution.class) {
			switch (derivedFeatureID) {
				case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RIGHT_TYPE: return AtlErrorPackage.BINDING_RESOLUTION__RIGHT_TYPE;
				case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__TARGET_TYPE: return AtlErrorPackage.BINDING_RESOLUTION__TARGET_TYPE;
				case AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RULES: return AtlErrorPackage.BINDING_RESOLUTION__RULES;
				default: return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == BindingResolution.class) {
			switch (baseFeatureID) {
				case AtlErrorPackage.BINDING_RESOLUTION__RIGHT_TYPE: return AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RIGHT_TYPE;
				case AtlErrorPackage.BINDING_RESOLUTION__TARGET_TYPE: return AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__TARGET_TYPE;
				case AtlErrorPackage.BINDING_RESOLUTION__RULES: return AtlErrorPackage.BINDING_WITH_RESOLVED_BY_INCOMPATIBLE_RULE__RULES;
				default: return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
	}

} //BindingWithResolvedByIncompatibleRuleImpl
