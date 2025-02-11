/**
 * Copyright (C) 2009-2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.identity.organization.model.organization.impl;

import org.bonitasoft.studio.identity.organization.model.organization.Group;
import org.bonitasoft.studio.identity.organization.model.organization.OrganizationPackage;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Group</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.bonitasoft.studio.organization.model.organization.impl.GroupImpl#getDisplayName <em>Display Name</em>}</li>
 *   <li>{@link org.bonitasoft.studio.organization.model.organization.impl.GroupImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.bonitasoft.studio.organization.model.organization.impl.GroupImpl#getIconName <em>Icon Name</em>}</li>
 *   <li>{@link org.bonitasoft.studio.organization.model.organization.impl.GroupImpl#getIconPath <em>Icon Path</em>}</li>
 *   <li>{@link org.bonitasoft.studio.organization.model.organization.impl.GroupImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.bonitasoft.studio.organization.model.organization.impl.GroupImpl#getParentPath <em>Parent Path</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GroupImpl extends EObjectImpl implements Group {
	/**
     * The default value of the '{@link #getDisplayName() <em>Display Name</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getDisplayName()
     * @generated
     * @ordered
     */
	protected static final String DISPLAY_NAME_EDEFAULT = null;

	/**
     * The cached value of the '{@link #getDisplayName() <em>Display Name</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getDisplayName()
     * @generated
     * @ordered
     */
	protected String displayName = DISPLAY_NAME_EDEFAULT;

	/**
     * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getDescription()
     * @generated
     * @ordered
     */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
     * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getDescription()
     * @generated
     * @ordered
     */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
     * The default value of the '{@link #getIconName() <em>Icon Name</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getIconName()
     * @generated
     * @ordered
     */
	protected static final String ICON_NAME_EDEFAULT = null;

	/**
     * The cached value of the '{@link #getIconName() <em>Icon Name</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getIconName()
     * @generated
     * @ordered
     */
	protected String iconName = ICON_NAME_EDEFAULT;

	/**
     * The default value of the '{@link #getIconPath() <em>Icon Path</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getIconPath()
     * @generated
     * @ordered
     */
	protected static final String ICON_PATH_EDEFAULT = null;

	/**
     * The cached value of the '{@link #getIconPath() <em>Icon Path</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getIconPath()
     * @generated
     * @ordered
     */
	protected String iconPath = ICON_PATH_EDEFAULT;

	/**
     * The default value of the '{@link #getName() <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getName()
     * @generated
     * @ordered
     */
	protected static final String NAME_EDEFAULT = null;

	/**
     * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getName()
     * @generated
     * @ordered
     */
	protected String name = NAME_EDEFAULT;

	/**
     * The default value of the '{@link #getParentPath() <em>Parent Path</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getParentPath()
     * @generated
     * @ordered
     */
	protected static final String PARENT_PATH_EDEFAULT = null;

	/**
     * The cached value of the '{@link #getParentPath() <em>Parent Path</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getParentPath()
     * @generated
     * @ordered
     */
	protected String parentPath = PARENT_PATH_EDEFAULT;

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected GroupImpl() {
        super();
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
	protected EClass eStaticClass() {
        return OrganizationPackage.Literals.GROUP;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
    public String getDisplayName() {
        return displayName;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
    public void setDisplayName(String newDisplayName) {
        String oldDisplayName = displayName;
        displayName = newDisplayName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, OrganizationPackage.GROUP__DISPLAY_NAME, oldDisplayName, displayName));
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
    public String getDescription() {
        return description;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
    public void setDescription(String newDescription) {
        String oldDescription = description;
        description = newDescription;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, OrganizationPackage.GROUP__DESCRIPTION, oldDescription, description));
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
    public String getIconName() {
        return iconName;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
    public void setIconName(String newIconName) {
        String oldIconName = iconName;
        iconName = newIconName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, OrganizationPackage.GROUP__ICON_NAME, oldIconName, iconName));
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
    public String getIconPath() {
        return iconPath;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
    public void setIconPath(String newIconPath) {
        String oldIconPath = iconPath;
        iconPath = newIconPath;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, OrganizationPackage.GROUP__ICON_PATH, oldIconPath, iconPath));
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
    public String getName() {
        return name;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, OrganizationPackage.GROUP__NAME, oldName, name));
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
    public String getParentPath() {
        return parentPath;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
    public void setParentPath(String newParentPath) {
        String oldParentPath = parentPath;
        parentPath = newParentPath;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, OrganizationPackage.GROUP__PARENT_PATH, oldParentPath, parentPath));
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case OrganizationPackage.GROUP__DISPLAY_NAME:
                return getDisplayName();
            case OrganizationPackage.GROUP__DESCRIPTION:
                return getDescription();
            case OrganizationPackage.GROUP__ICON_NAME:
                return getIconName();
            case OrganizationPackage.GROUP__ICON_PATH:
                return getIconPath();
            case OrganizationPackage.GROUP__NAME:
                return getName();
            case OrganizationPackage.GROUP__PARENT_PATH:
                return getParentPath();
        }
        return super.eGet(featureID, resolve, coreType);
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
	public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case OrganizationPackage.GROUP__DISPLAY_NAME:
                setDisplayName((String)newValue);
                return;
            case OrganizationPackage.GROUP__DESCRIPTION:
                setDescription((String)newValue);
                return;
            case OrganizationPackage.GROUP__ICON_NAME:
                setIconName((String)newValue);
                return;
            case OrganizationPackage.GROUP__ICON_PATH:
                setIconPath((String)newValue);
                return;
            case OrganizationPackage.GROUP__NAME:
                setName((String)newValue);
                return;
            case OrganizationPackage.GROUP__PARENT_PATH:
                setParentPath((String)newValue);
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
            case OrganizationPackage.GROUP__DISPLAY_NAME:
                setDisplayName(DISPLAY_NAME_EDEFAULT);
                return;
            case OrganizationPackage.GROUP__DESCRIPTION:
                setDescription(DESCRIPTION_EDEFAULT);
                return;
            case OrganizationPackage.GROUP__ICON_NAME:
                setIconName(ICON_NAME_EDEFAULT);
                return;
            case OrganizationPackage.GROUP__ICON_PATH:
                setIconPath(ICON_PATH_EDEFAULT);
                return;
            case OrganizationPackage.GROUP__NAME:
                setName(NAME_EDEFAULT);
                return;
            case OrganizationPackage.GROUP__PARENT_PATH:
                setParentPath(PARENT_PATH_EDEFAULT);
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
            case OrganizationPackage.GROUP__DISPLAY_NAME:
                return DISPLAY_NAME_EDEFAULT == null ? displayName != null : !DISPLAY_NAME_EDEFAULT.equals(displayName);
            case OrganizationPackage.GROUP__DESCRIPTION:
                return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
            case OrganizationPackage.GROUP__ICON_NAME:
                return ICON_NAME_EDEFAULT == null ? iconName != null : !ICON_NAME_EDEFAULT.equals(iconName);
            case OrganizationPackage.GROUP__ICON_PATH:
                return ICON_PATH_EDEFAULT == null ? iconPath != null : !ICON_PATH_EDEFAULT.equals(iconPath);
            case OrganizationPackage.GROUP__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case OrganizationPackage.GROUP__PARENT_PATH:
                return PARENT_PATH_EDEFAULT == null ? parentPath != null : !PARENT_PATH_EDEFAULT.equals(parentPath);
        }
        return super.eIsSet(featureID);
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	@Override
	public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuilder result = new StringBuilder(super.toString());
        result.append(" (displayName: ");
        result.append(displayName);
        result.append(", description: ");
        result.append(description);
        result.append(", iconName: ");
        result.append(iconName);
        result.append(", iconPath: ");
        result.append(iconPath);
        result.append(", name: ");
        result.append(name);
        result.append(", parentPath: ");
        result.append(parentPath);
        result.append(')');
        return result.toString();
    }

} //GroupImpl
