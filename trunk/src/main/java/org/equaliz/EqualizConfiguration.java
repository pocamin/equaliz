package org.equaliz;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.map.IdentityMap;

@SuppressWarnings("unchecked")
public class EqualizConfiguration<T> {

	private static Set<Class<?>> simpleClass = new HashSet<Class<?>>();
	static {
		simpleClass.add(String.class);
		simpleClass.add(Class.class);
		simpleClass.add(long.class);
		simpleClass.add(int.class);
		simpleClass.add(char.class);
		simpleClass.add(double.class);
		simpleClass.add(float.class);
		simpleClass.add(Long.class);
		simpleClass.add(Integer.class);
		simpleClass.add(Character.class);
		simpleClass.add(Double.class);
		simpleClass.add(Float.class);
		simpleClass.add(Date.class);
	}

	private static Set<Class<?>> notSupportedClass = new HashSet<Class<?>>();
	static {
		notSupportedClass.add(Map.class);
	}

	public Map<Class<?>, EqualizConfiguration<?>> innerConfigurator = new HashMap<Class<?>, EqualizConfiguration<?>>();

	public Map<String, ImprovedCollection<?>> innerImprovedCollections = new HashMap<String, ImprovedCollection<?>>();
	public Map<ImprovedCollection<?>, Class<?>> innerImprovedCollectionsReturnType = new IdentityMap();

	public <V> EqualizConfiguration<V> getInner(Class<V> classe) {
		if (innerConfigurator.containsKey(classe)) {
			return (EqualizConfiguration<V>) innerConfigurator.get(classe);
		}

		EqualizConfiguration<V> eq = new EqualizConfiguration<V>(classe);
		innerConfigurator.put(classe, eq);
		return eq;
	}

	private MethodInterceptor collectionInterceptor = new MethodInterceptor() {

		@Override
		public Object intercept(Object arg0, Method arg1, Object[] arg2, MethodProxy arg3) throws Throwable {
			if (arg1.getName().equals("with")) {

				Class returnType = innerImprovedCollectionsReturnType.get(arg0);

				if (isNotSupported(returnType)) {
					throw new EqualizException(EqualizException.NOT_SUPPORTED_CLASS, arg1.getReturnType().getName());
				}
				if (isCollection(returnType)) {
					throw new EqualizException(EqualizException.SUBCOLLECTION_ACCES_ERROR);
				}
				if (isSimpleClass(returnType)) {
					return null;
				}
				return getInner(returnType).with();
			}
			throw new EqualizException(EqualizException.COLLECTION_ACCES_ERROR);
		}

	};

	private MethodInterceptor interceptor = new MethodInterceptor() {
		@Override
		public Object intercept(Object arg0, Method arg1, Object[] arg2, MethodProxy arg3) throws Throwable {
			checkedProperties.add(getPropertyFromMethod(arg1));
			if (isNotSupported(arg1.getReturnType())) {
				throw new EqualizException(EqualizException.NOT_SUPPORTED_CLASS, arg1.getReturnType().getName());
			}
			if (isCollection(arg1.getReturnType())) {
				return returnCollection(arg1);
			}
			if (isSimpleClass(arg1.getReturnType())) {
				return null;
			}

			return getInner(arg1.getReturnType()).with();
		}

		/**
		 * @param arg1
		 * @return
		 */
		private Collection returnCollection(Method arg1) {
			ImprovedCollection newCollection = innerImprovedCollections.get(getPropertyFromMethod(arg1));
			if (newCollection == null) {
				newCollection =
						(ImprovedCollection) Enhancer
								.create(arg1.getReturnType(), new Class[] {ImprovedCollection.class}, collectionInterceptor);
				innerImprovedCollections.put(getPropertyFromMethod(arg1), newCollection);

				if (arg1.getGenericReturnType() != null) {
					Class classe = ((Class) ((java.lang.reflect.ParameterizedType) arg1.getGenericReturnType()).getActualTypeArguments()[0]);
					if (classe != null) {
						innerImprovedCollectionsReturnType.put(newCollection, classe);
					} else {
						throw new EqualizException(EqualizException.ONLY_GENERIC_COLLECTION_OF_CONCRETE, getPropertyFromMethod(arg1));
					}
				} else {
					throw new EqualizException(EqualizException.ONLY_GENERIC_COLLECTION_OF_CONCRETE, getPropertyFromMethod(arg1));
				}

			}

			return newCollection;
		}

		private String getPropertyFromMethod(Method arg1) {

			if (arg1.getName().startsWith("get")) {
				return lower(arg1.getName().substring(3));
			}
			if (arg1.getName().startsWith("is")) {
				return lower(arg1.getName().substring(2));
			}

			throw new EqualizException(EqualizException.INVALID_METHOD, arg1.getName());
		}

		private String lower(String string) {

			return string.substring(0, 1).toLowerCase() + string.substring(1);
		}
	};

	private Class<T> classe;
	private T enhancedObject;

	private Collection<String> checkedProperties;

	/**
	 * @param classe
	 */
	public EqualizConfiguration(Class<T> classe) {
		this.classe = classe;
		checkedProperties = new ArrayList<String>();
		createProxy();
	}

	public T with() {
		return enhancedObject;
	}

	private void createProxy() {
		enhancedObject = (T) Enhancer.create(classe, interceptor);
	}

	/**
	 * @param p1
	 * @param p2
	 * @return
	 */
	public boolean equals(Object p1, Object p2) {
		for (String property : checkedProperties) {
			try {
				Object value1 = PropertyUtils.getProperty(p1, property);
				Object value2 = PropertyUtils.getProperty(p2, property);
				if (value1 == null && value2 == null) {
					continue;
				} else if (value1 == null || value2 == null) {
					return false;
				} else if (isSimpleClass(value1.getClass())) {
					if (!value1.equals(value2)) {
						return false;
					}
				} else if (isCollection(value1.getClass())) {
					if (!improvedCollectionEquals(property, (Collection<?>) value1, (Collection<?>) value2)) {
						return false;
					}
				} else {
					return getInner(value1.getClass()).equals(value1, value2);
				}

			}
			catch (Exception e) {
				throw new EqualizException(EqualizException.INVALID_PROPERTY, property);
			}

		}
		return true;
	}

	/**
	 * @param property
	 * @param value1
	 * @param value2
	 */
	private boolean improvedCollectionEquals(String property, Collection c1, Collection c2) {
		if (c1.size() != c2.size()) {
			return false;
		}

		if (c1.size() > 0) {
			// get the first object
			Object firstObject = c1.iterator().next();
			if (isSimpleClass(firstObject.getClass())) {
				if (!new ArrayList(c1).equals(new ArrayList(c2))) {
					return false;
				}
			} else {
				EqualizConfiguration ec = innerConfigurator.get(firstObject.getClass());
				Iterator ic1 = c1.iterator();
				Iterator ic2 = c2.iterator();
				while (ic1.hasNext()) {
					if (!ec.equals(ic1.next(), ic2.next())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * @param class1
	 * @return
	 */
	private boolean isSimpleClass(Class<? extends Object> class1) {
		if (simpleClass.contains(class1)) {
			return true;
		}
		if (Enum.class.isAssignableFrom(class1)) {
			return true;
		}
		return simpleClass.contains(class1);
	}

	/**
	 * @param p1
	 * @return
	 */
	public int hashCode(Object p1) {
		int code = 0;
		for (String property : checkedProperties) {
			try {
				Object p = PropertyUtils.getProperty(p1, property);
				if (p != null) {
					if (isSimpleClass(p.getClass())) {
						code += p.hashCode();
					} else if (isCollection(p.getClass())) {
						code += getHashCodeForCollection((Collection<?>) p);
					} else {
						code += getInner(p.getClass()).hashCode(p);
					}
				}
			}
			catch (Exception e) {
				throw new EqualizException(EqualizException.INVALID_PROPERTY, property);
			}
		}

		return code;
	}

	public <U> U clone(U o) {
		if (o.equals(null)) {
			return null;
		}

		U clonedObject;
		try {
			clonedObject = (U) o.getClass().newInstance();
		}
		catch (Exception e) {
			throw new EqualizException(EqualizException.INVALID_BEAN, o.getClass().toString());
		}

		for (String property : checkedProperties) {
			try {
				Object p = PropertyUtils.getProperty(o, property);
				if (p != null) {
					if (isSimpleClass(p.getClass())) {
						BeanUtils.setProperty(clonedObject, property, p);
					} else if (isCollection(p.getClass())) {
						BeanUtils.setProperty(clonedObject, property, cloneCollection((Collection<?>) p));
					} else {
						BeanUtils.setProperty(clonedObject, property, getInner(p.getClass()).clone(p));
					}
				}
			}
			catch (Exception e) {
				throw new EqualizException(EqualizException.INVALID_PROPERTY, property);
			}
		}

		return clonedObject;
	}

	/**
	 * @param p
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private Collection cloneCollection(Collection<?> fromCollection) throws InstantiationException, IllegalAccessException {
		Collection clonedCollection = fromCollection.getClass().newInstance();
		for (Object o : fromCollection) {
			if (o == null) {
				clonedCollection.add(null);
			} else if (isSimpleClass(o.getClass())) {
				clonedCollection.add(o);
			} else {
				clonedCollection.add(getInner(o.getClass()).clone(o));
			}

		}
		return clonedCollection;
	}

	/**
	 * @param p
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private int getHashCodeForCollection(Collection<?> fromCollection) {
		int hashCode = 0;
		for (Object o : fromCollection) {
			if (isSimpleClass(o.getClass())) {
				hashCode += o.hashCode();
			} else {
				hashCode += getInner(o.getClass()).hashCode(o);
			}
		}
		return hashCode;
	}

	private boolean isCollection(Class<?> returnType) {
		return Collection.class.isAssignableFrom(returnType);
	}

	private boolean isNotSupported(Class<?> returnType) {
		for (Class classe : notSupportedClass) {
			if (classe.isAssignableFrom(returnType)) {
				return true;
			}
		}
		return false;
	}
}
