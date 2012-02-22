/*
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.JAXBUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import java.lang.reflect.Array;
import java.util.*;


/**
 * Property is a specific characteristic of an object. It may be considered
 * object "attribute" or "field". For example User has fullName property that
 * contains string value of user's full name.
 * <p/>
 * Properties may be single-valued or multi-valued
 * <p/>
 * Properties may contain primitive types or complex types (defined by XSD
 * schema)
 * <p/>
 * Property values are unordered, implementation may change the order of values
 * <p/>
 * Duplicate values of properties should be silently removed by implementations,
 * but clients must be able tolerate presence of duplicate values.
 * <p/>
 * Operations that modify the objects work with the granularity of properties.
 * They add/remove/replace the values of properties, but do not "see" inside the
 * property.
 * <p/>
 * Property is mutable.
 *
 * @author Radovan Semancik
 */
public class PrismProperty<V> extends Item<PrismPropertyValue<V>> {

    private static final Trace LOGGER = TraceManager.getTrace(PrismProperty.class);

    public PrismProperty(QName name) {
        super(name);
    }

    protected PrismProperty(QName name, PrismPropertyDefinition definition, PrismContext prismContext) {
        super(name, definition, prismContext);
    }

	/**
     * Returns applicable property definition.
     * <p/>
     * May return null if no definition is applicable or the definition is not
     * know.
     *
     * @return applicable property definition
     */
    public PrismPropertyDefinition getDefinition() {
        return (PrismPropertyDefinition) definition;
    }

    /**
     * Sets applicable property definition.
     *
     * @param definition the definition to set
     */
    public void setDefinition(PrismPropertyDefinition definition) {
        this.definition = definition;
    }

    /**
     * Returns property values.
     * <p/>
     * The values are returned as set. The order of values is not significant.
     *
     * @return property values
     */
    public List<PrismPropertyValue<V>> getValues() {
        return (List<PrismPropertyValue<V>>) super.getValues();
    }
    
    public PrismPropertyValue<V> getValue() {
        List<PrismPropertyValue<V>> values = getValues();
        if (values == null || values.isEmpty()) {
        	return null;
        }
        if (values.size() == 1) {
        	return values.get(0);
        }
        throw new IllegalStateException("Attempt to get a single value from a multi-valued property "+getName());
    }

    /**
     * Type override, also for compatibility.
     */
    public <T> Set<PrismPropertyValue<T>> getValues(Class<T> T) {
        return (Set) getValues();
    }

    public Collection<V> getRealValues() {
		Collection<V> realValues = new ArrayList<V>(getValues().size());
		for (PrismPropertyValue<V> pValue: getValues()) {
			realValues.add(pValue.getValue());
		}
		return realValues;
	}
    
    /**
     * Type override, also for compatibility.
     */
	public <T> Collection<T> getRealValues(Class<T> type) {
		Collection<T> realValues = new ArrayList<T>(getValues().size());
		for (PrismPropertyValue<V> pValue: getValues()) {
			realValues.add((T) pValue.getValue());
		}
		return realValues;
	}
	
	public V getRealValue() {
		return getValue().getValue();
	}
	
	/**
     * Type override, also for compatibility.
     */
	public <T> T getRealValue(Class<T> type) {
		return (T) getValue().getValue();
	}

	/**
     * Type override, also for compatibility.
     */
	public <T> T[] getRealValuesArray(Class<T> type) {
		Object valuesArrary = Array.newInstance(type, getValues().size());
		for (int j = 0; j < getValues().size(); ++j) {
			Object avalue = getValues().get(j).getValue();
			Array.set(valuesArrary, j, avalue);
		}
		return (T[]) valuesArrary;
	}

    /**
     * Type override, also for compatibility.
     */
    public <T> PrismPropertyValue<T> getValue(Class<T> T) {
    	if (getDefinition() != null) {
    		if (getDefinition().isMultiValue()) {
    			throw new IllegalStateException("Attempt to get single value from property " + name
                        + " with multiple values");
    		}
    	}
        if (getValues().size() > 1) {
            throw new IllegalStateException("Attempt to get single value from property " + name
                    + " with multiple values");
        }
        if (getValues().isEmpty()) {
            return null;
        }
        PrismPropertyValue<V> o = getValues().iterator().next();
        return (PrismPropertyValue<T>) o;
    }

    /**
     * Means as a short-hand for setting just a value for single-valued
     * attributes.
     * Will remove all existing values.
     * TODO
     */
    public void setValue(PrismPropertyValue<V> value) {
    	getValues().clear();
        addValue(value);
    }

    public void addValues(Collection<PrismPropertyValue<V>> pValuesToAdd) {
    	for (PrismPropertyValue<V> pValue: pValuesToAdd) {
    		addValue(pValue);
    	}
    }

    public void addValue(PrismPropertyValue<V> pValueToAdd) {
    	Iterator<PrismPropertyValue<V>> iterator = getValues().iterator();
    	while (iterator.hasNext()) {
    		PrismPropertyValue<V> pValue = iterator.next();
    		if (pValue.equalsRealValue(pValueToAdd)) {
    			LOGGER.warn("Adding value to property "+getName()+" that already exists (overwriting), value: "+pValueToAdd);
    			iterator.remove();
    		}
    	}
    	pValueToAdd.setParent(this);
    	getValues().add(pValueToAdd);
    }

    public boolean deleteValues(Collection<PrismPropertyValue<V>> pValuesToDelete) {
        boolean changed = false;
    	for (PrismPropertyValue<V> pValue: pValuesToDelete) {
            if (!changed) {
    		    changed = deleteValue(pValue);
            } else {
                deleteValue(pValue);
            }
    	}
        return changed;
    }

    public boolean deleteValue(PrismPropertyValue<V> pValueToDelete) {
    	Iterator<PrismPropertyValue<V>> iterator = getValues().iterator();
    	boolean found = false;
    	while (iterator.hasNext()) {
    		PrismPropertyValue<V> pValue = iterator.next();
    		if (pValue.equalsRealValue(pValueToDelete)) {
    			iterator.remove();
    			pValue.setParent(null);
    			found = true;
    		}
    	}
    	if (!found) {
    		LOGGER.warn("Deleting value of property "+getName()+" that does not exist (skipping), value: "+pValueToDelete);
    	}

        return found;
    }

    public void replaceValues(Collection<PrismPropertyValue<V>> valuesToReplace) {
    	getValues().clear();
        addValues(valuesToReplace);
    }

    public boolean hasValue(PrismPropertyValue<V> value) {
        return super.hasValue(value);
    }

    public boolean hasRealValue(PrismPropertyValue<V> value) {
        for (PrismPropertyValue<V> propVal : getValues()) {
            if (propVal.equalsRealValue(value)) {
                return true;
            }
        }

        return false;
    }
    
    public Class<V> getValueClass() {
    	if (getDefinition() != null) {
    		return getDefinition().getTypeClass();
    	}
    	if (!getValues().isEmpty()) {
    		PrismPropertyValue<V> firstPVal = getValues().get(0);
    		if (firstPVal != null) {
    			V firstVal = firstPVal.getValue();
    			if (firstVal != null) {
    				return (Class<V>) firstVal.getClass();
    			}
    		}
    	}
    	// TODO: How to determine value class?????
    	return PrismConstants.DEFAULT_VALUE_CLASS;
    }
    
	void applyDefinition(ItemDefinition definition) {
		if (!(definition instanceof PrismPropertyDefinition)) {
			throw new IllegalArgumentException("Cannot apply "+definition+" to property");
		}
		super.applyDefinition(definition);
	}
	
    @Override
	public PropertyDelta<V> createDelta(PropertyPath path) {
		return new PropertyDelta<V>(path, getDefinition());
	}

//    @Override
//    public void serializeToDom(Node parentNode) throws SchemaException {
//        serializeToDom(parentNode, null, null, false);
//    }

	public void serializeToDom(Node parentNode, PrismPropertyDefinition propDef, Set<PrismPropertyValue<V>> alternateValues,
            boolean recordType) throws SchemaException {

        if (propDef == null) {
            propDef = getDefinition();
        }

        Collection<PrismPropertyValue<V>> serializeValues = getValues();
        if (alternateValues != null) {
            serializeValues = alternateValues;
        }

        for (PrismPropertyValue<V> val : serializeValues) {
            // If we have a definition then try to use it. The conversion may be more realiable
            // Otherwise the conversion will be governed by Java type
            QName xsdType = null;
            if (propDef != null) {
                xsdType = propDef.getTypeName();
            }
            try {
                XmlTypeConverter.appendBelowNode(val.getValue(), xsdType, getName(), parentNode, recordType);
            } catch (SchemaException e) {
                throw new SchemaException(e.getMessage() + ", while converting " + propDef.getTypeName(), e);
            }
        }
    }

    /**
     * Serializes property to DOM or JAXB element(s).
     * <p/>
     * The property name will be used as an element QName.
     * The values will be in the element content. Single-value
     * properties will produce one element (on none), multi-valued
     * properies may produce several elements. All of the elements will
     * have the same QName.
     * <p/>
     * The property must have a definition (getDefinition() must not
     * return null).
     *
     * @param doc DOM Document
     * @return property serialized to DOM Element or JAXBElement
     * @throws SchemaException No definition or inconsistent definition
     */
    public List<Object> serializeToJaxb(Document doc) throws SchemaException {
        return serializeToJaxb(doc, null, null, false);
    }

    /**
     * Same as serializeToDom(Document doc) but allows external definition.
     * <p/>
     * Package-private. Useful for some internal calls inside schema processor.
     */
    List<Object> serializeToJaxb(Document doc, PrismPropertyDefinition propDef) throws SchemaException {
        // No need to record types, we have schema definition here
        return serializeToJaxb(doc, propDef, null, false);
    }

    /**
     * Same as serializeToDom(Document doc) but allows external definition.
     * <p/>
     * Allows alternate values.
     * Allows option to record type in the serialized output (using xsi:type)
     * <p/>
     * Package-private. Useful for some internal calls inside schema processor.
     */
    List<Object> serializeToJaxb(Document doc, PrismPropertyDefinition propDef, Set<PrismPropertyValue<V>> alternateValues,
            boolean recordType) throws SchemaException {


        // Try to locate definition
        List<Object> elements = new ArrayList<Object>();

        //check if the property has value..if not, return empty elemnts list..

        if (propDef == null) {
            propDef = getDefinition();
        }

        Collection<PrismPropertyValue<V>> serializeValues = getValues();
        if (alternateValues != null) {
            serializeValues = alternateValues;
        }


        for (PrismPropertyValue<V> val : serializeValues) {
            // If we have a definition then try to use it. The conversion may be more realiable
            // Otherwise the conversion will be governed by Java type
            QName xsdType = null;
            if (propDef != null) {
                xsdType = propDef.getTypeName();
                //FIXME: we do not want to send ignored attribute to the other layers..
                //but this place is maybe not suitable to skip the ignored property..
                if (propDef.isIgnored()) {
                    continue;
                }
            }

            try {
                elements.add(XmlTypeConverter.toXsdElement(val.getValue(), xsdType, getName(), doc, recordType));
            } catch (SchemaException e) {
                throw new SchemaException(e.getMessage() + ", while converting " + propDef.getTypeName(), e);
            }

        }
        return elements;
    }

	@Override
    public PrismProperty<V> clone() {
        PrismProperty<V> clone = new PrismProperty<V>(getName(), getDefinition(), prismContext);
        copyValues(clone);
        return clone;
    }

    protected void copyValues(PrismProperty<V> clone) {
        super.copyValues(clone);
        for (PrismPropertyValue<V> value : getValues()) {
            clone.addValue(value.clone());
        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }
	
	@Override
	protected ItemDelta fixupDelta(ItemDelta delta, Item otherItem, PropertyPath pathPrefix,
			boolean ignoreMetadata) {
		PrismPropertyDefinition def = getDefinition();
		if (def != null && def.isSingleValue() && !delta.isEmpty()) {
        	// Drop the current delta (it was used only to detect that something has changed
        	// Generate replace delta instead of add/delete delta
			PrismProperty<V> other = (PrismProperty<V>)otherItem;
			PropertyDelta<V> propertyDelta = (PropertyDelta<V>)delta; 
    		Collection<PrismPropertyValue<V>> replaceValues = new ArrayList<PrismPropertyValue<V>>(other.getValues().size());
            for (PrismPropertyValue<V> value : other.getValues()) {
            	replaceValues.add(value.clone());
            }
            propertyDelta.setValuesToReplace(replaceValues);
			return propertyDelta;
        } else {
        	return super.fixupDelta(delta, otherItem, pathPrefix, ignoreMetadata);
        }
	}

	@Override
    public String toString() {
        return getClass().getSimpleName() + "(" + DebugUtil.prettyPrint(getName()) + "):" + getValues();
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append(INDENT_STRING);
        }
        sb.append(getDebugDumpClassName()).append(": ").append(DebugUtil.prettyPrint(getName())).append(" = ");
        if (getValues() == null) {
            sb.append("null");
        } else {
            sb.append("[ ");
            for (Object value : getValues()) {
                sb.append(DebugUtil.prettyPrint(value));
                sb.append(", ");
            }
            sb.append(" ]");
        }
        if (getDefinition() != null) {
            sb.append(" def");
        }
        return sb.toString();
    }

    /**
     * Return a human readable name of this class suitable for logs.
     */
    @Override
    protected String getDebugDumpClassName() {
        return "PP";
    }

}
