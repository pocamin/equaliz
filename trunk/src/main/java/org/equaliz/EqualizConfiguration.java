package org.equaliz;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.commons.collections.map.IdentityMap;

@SuppressWarnings("unchecked")
public class EqualizConfiguration<T> {
	private static int classNumber = 0;

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

	/**
	 * @return
	 */
	public synchronized Equalizer createEqualizer() {
		ClassPool pool = ClassPool.getDefault();
		pool.insertClassPath(new ClassClassPath(classe));
		CtClass evalClass = pool.makeClass(classe.getCanonicalName() + "_eq1_" + classNumber++);
		try {
			evalClass.addMethod(CtNewMethod.make(createEqualsMethod(), evalClass));
			evalClass.addMethod(CtNewMethod.make(createhashCodeMethod(), evalClass));
			evalClass.addInterface(pool.get(Equalizer.class.getCanonicalName()));

			return (Equalizer) evalClass.toClass().newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException("Could not create equalizer", e);
		}
	}

	public String createhashCodeMethod() throws SecurityException, NoSuchMethodException {
		StringBuilder builder = new StringBuilder();
		builder.append("public int hashCode(Object p1){");
		builder.append("int hashCode = 0;");
		builder.append(classe.getCanonicalName() + " p1d = (" + classe.getCanonicalName() + ")p1;");

		createPropertiesHashCoder(builder, "p1d", this);
		builder.append("return hashCode;");
		builder.append("}");
		return builder.toString();
	}

	/**
	 * @param builder
	 * @param string
	 * @param equalizConfiguration
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	private void createPropertiesHashCoder(StringBuilder builder, String current, EqualizConfiguration<T> currentConfig)
			throws SecurityException, NoSuchMethodException {
		int i = 0;
		builder.append("if (" + current + " != null ){");
		for (String property : currentConfig.checkedProperties) {

			String next = current + "_" + i;
			String getterMethod = "get" + property.substring(0, 1).toUpperCase() + property.substring(1);
			Class subClasse = currentConfig.classe.getMethod(getterMethod).getReturnType();

			builder.append(subClasse.getCanonicalName() + " " + next + " = " + current + "." + getterMethod + "();");
			builder.append("if (" + next + " != null){");

			if (isSimpleClass(subClasse)) {
				builder.append("hashCode += " + next + ".hashCode();");
			} else if (isCollection(subClasse)) {
				createPropertiesHashCoderForCollection(builder, currentConfig, property, next);
			} else {
				createPropertiesHashCoder(builder, next, currentConfig.getInner(subClasse));
			}

			builder.append("}");

			i++;
		}
		builder.append("} ");
	}

	/**
	 * @param builder
	 * @param currentConfig
	 * @param property
	 * @param next
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	private void createPropertiesHashCoderForCollection(StringBuilder builder, EqualizConfiguration<T> currentConfig, String property,
			String next) throws SecurityException, NoSuchMethodException {
		Class collectionReturnType = innerImprovedCollectionsReturnType.get(innerImprovedCollections.get(property));
		// Convert to array
		String nextArray = next + "a";
		builder.append(collectionReturnType.getCanonicalName() + "[] " + nextArray + " = new " + collectionReturnType.getCanonicalName() + "["
				+ next + ".size()];");
		builder.append(next + ".toArray(" + nextArray + ");");

		// create hashcode
		builder.append("for(int i = 0; i < " + nextArray + ".length; i++){");
		String nextArrayContent = next + "ac";
		builder.append(collectionReturnType.getCanonicalName() + " " + nextArrayContent + " = " + nextArray + "[i];");

		if (isSimpleClass(collectionReturnType)) {
			builder.append("hashCode += " + nextArrayContent + ".hashCode();");
		} else {
			createPropertiesHashCoder(builder, nextArrayContent, currentConfig.getInner(collectionReturnType));
		}
		builder.append("}");
	}

	public String createEqualsMethod() throws SecurityException, NoSuchMethodException {
		StringBuilder builder = new StringBuilder();
		builder.append("public boolean equals(Object p1, Object p2){");

		builder.append(classe.getCanonicalName() + " p1d = (" + classe.getCanonicalName() + ")p1;");
		builder.append(classe.getCanonicalName() + " p2d = (" + classe.getCanonicalName() + ")p2;");

		createPropertiesComparator(builder, "p1d", "p2d", this);
		builder.append("return true;");
		builder.append("}");
		return builder.toString();
	}

	/**
	 * @param builder
	 * @param currentLeft
	 * @param currentRight
	 * @param currentConfig
	 * @throws NoSuchMethodException
	 */
	private void createPropertiesComparator(StringBuilder builder, String currentLeft, String currentRight, EqualizConfiguration currentConfig)
			throws NoSuchMethodException {

		int i = 0;
		builder.append("if (!(" + currentLeft + " == null && " + currentRight + " == null)){");
		for (String property : (Collection<String>) currentConfig.checkedProperties) {

			String nextLeft = currentLeft + "_" + i;
			String nextRight = currentRight + "_" + i;
			String getterMethod = "get" + property.substring(0, 1).toUpperCase() + property.substring(1);
			Class subClasse = currentConfig.classe.getMethod(getterMethod).getReturnType();

			builder.append(subClasse.getCanonicalName() + " " + nextLeft + " = " + currentLeft + "." + getterMethod + "();");
			builder.append(subClasse.getCanonicalName() + " " + nextRight + " = " + currentRight + "." + getterMethod + "();");
			builder.append("if ((" + nextLeft + " == null && " + nextRight + " != null) || (" + nextLeft + " != null && " + nextRight
					+ " == null)) return false;");
			if (isSimpleClass(subClasse)) {
				builder.append("if (!" + nextLeft + ".equals(" + nextRight + ")) return false;");
			} else if (isCollection(subClasse)) {
				createPropertiesComparatorForCollection(builder, currentConfig, property, nextLeft, nextRight);
			} else {
				createPropertiesComparator(builder, nextLeft, nextRight, currentConfig.getInner(subClasse));
			}

			i++;
		}
		builder.append("} else {");
		builder.append("if (" + currentLeft + " == null || " + currentRight + " == null) return false;");
		builder.append("}");
	}

	/**
	 * @param builder
	 * @param currentConfig
	 * @param property
	 * @param nextLeft
	 * @param nextRight
	 * @throws NoSuchMethodException
	 */
	private void createPropertiesComparatorForCollection(StringBuilder builder, EqualizConfiguration currentConfig, String property,
			String nextLeft, String nextRight) throws NoSuchMethodException {
		Class collectionReturnType = innerImprovedCollectionsReturnType.get(innerImprovedCollections.get(property));
		builder.append("if (" + nextLeft + ".size() !=" + nextRight + ".size()) return false;");

		// Convert to array
		String nextLeftArray = nextLeft + "a";
		String nextRightArray = nextRight + "b";
		builder.append(collectionReturnType.getCanonicalName() + "[] " + nextLeftArray + " = new " + collectionReturnType.getCanonicalName()
				+ "[" + nextLeft + ".size()];");
		builder.append(collectionReturnType.getCanonicalName() + "[] " + nextRightArray + " = new " + collectionReturnType.getCanonicalName()
				+ "[" + nextRight + ".size()];");
		builder.append(nextRight + ".toArray(" + nextRightArray + ");");
		builder.append(nextLeft + ".toArray(" + nextLeftArray + ");");

		// compare arrays element
		builder.append("for(int i = 0; i < " + nextLeftArray + ".length; i++){");
		String nextLeftArrayContent = nextLeft + "ac";
		String nextRightArrayContent = nextRight + "bc";
		builder.append(collectionReturnType.getCanonicalName() + " " + nextLeftArrayContent + " = " + nextLeftArray + "[i];");
		builder.append(collectionReturnType.getCanonicalName() + " " + nextRightArrayContent + " = " + nextRightArray + "[i];");

		if (isSimpleClass(collectionReturnType)) {
			builder.append("if (!" + nextLeftArrayContent + ".equals(" + nextRightArrayContent + ")) return false;");
		} else {
			createPropertiesComparator(builder, nextLeftArrayContent, nextRightArrayContent, currentConfig.getInner(collectionReturnType));
		}

		builder.append("}");
	}
}
