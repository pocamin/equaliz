package org.equaliz;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Limitation :
 * <ul>
 * <li>Cannot equaliz final class</li>
 * <li>Cannot equaliz non generic collections</li>
 * </ul>
 * @author benjamin Leroux
 */
public class Equaliz {
	public static Map<Class<?>, EqualizConfiguration<?>> savedConfigurator = new HashMap<Class<?>, EqualizConfiguration<?>>();

	/**
	 * Remove all Equaliz configuration
	 */
	protected static void clearEqualizConfig() {
		savedConfigurator.clear();
	}

	@SuppressWarnings("unchecked")
	public static <T> EqualizConfiguration<T> get(Class<T> classe) {
		if (savedConfigurator.containsKey(classe)) {
			return (EqualizConfiguration<T>) savedConfigurator.get(classe);
		}

		EqualizConfiguration<T> eq = new EqualizConfiguration<T>(classe);
		savedConfigurator.put(classe, eq);
		return eq;

	}

	public static <T> T with(Class<T> classe) {
		return get(classe).with();
	}

	/**
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static boolean equals(Object p1, Object p2) {
		return get(p1.getClass()).equals(p1, p2);
	}

	/**
	 * @param p1
	 */
	public static int hashCode(Object p1) {
		return get(p1.getClass()).hashCode(p1);
	}

	public static <T> T clone(T o) {
		return get(o.getClass()).clone(o);
	}

	/**
	 * @param c
	 */
	public static <T> T withElementOf(Collection<T> c) {
		ImprovedCollection<T> improvedCollection = (ImprovedCollection<T>) c;
		return improvedCollection.with();
	}

}
