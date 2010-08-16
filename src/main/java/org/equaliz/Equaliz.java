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

	public static ThreadLocal<Map<Class<?>, EqualizConfiguration<?>>> savedConfigurators =
			new ThreadLocal<Map<Class<?>, EqualizConfiguration<?>>>();

	@SuppressWarnings("unchecked")
	private static <T> EqualizConfiguration<T> get(Class<T> classe) {

		if (savedConfigurators.get() == null) {
			savedConfigurators.set(new HashMap<Class<?>, EqualizConfiguration<?>>());
		}

		if (savedConfigurators.get().containsKey(classe)) {
			return (EqualizConfiguration<T>) savedConfigurators.get().get(classe);
		}

		EqualizConfiguration<T> eq = new EqualizConfiguration<T>(classe);
		savedConfigurators.get().put(classe, eq);
		return eq;

	}

	public static <T> T with(Class<T> classe) {
		return get(classe).with();
	}

	/**
	 * @param c
	 */
	public static <T> T withElementOf(Collection<T> c) {
		ImprovedCollection<T> improvedCollection = (ImprovedCollection<T>) c;
		return improvedCollection.with();
	}

	public static <T> Equalizer createEqualizer(Class<T> classe) {
		if (savedConfigurators.get() == null) {
			throw new IllegalAccessError("Should not create equalizer before definiting it");
		}

		EqualizConfiguration<?> conf = savedConfigurators.get().get(classe);
		if (conf == null) {
			throw new IllegalAccessError("Should not create equalizer before definiting it");
		}

		savedConfigurators.get().remove(classe);

		return conf.createEqualizer();
	}

}
