package org.equaliz;

import java.util.Collection;

public interface ImprovedCollection<E> extends Collection<E> {

	public E with();

	public String getPropertyName();
}
