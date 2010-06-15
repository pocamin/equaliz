package org.equaliz.model;

import java.util.Collection;

public class SimplePojo {
	String name;
	SimplePojo2 inner;
	Collection<String> collectionOfString;
	Collection<SimplePojo2> collectionOfSimplePojo2;

	public SimplePojo() {
		super();
	}

	public SimplePojo(String name) {
		this.name = name;
	}

	/**
	 * @return the collectionOfSimplePojo2
	 */
	public Collection<SimplePojo2> getCollectionOfSimplePojo2() {
		return collectionOfSimplePojo2;
	}

	/**
	 * @param collectionOfSimplePojo2 the collectionOfSimplePojo2 to set
	 */
	public void setCollectionOfSimplePojo2(Collection<SimplePojo2> collectionOfSimplePojo2) {
		this.collectionOfSimplePojo2 = collectionOfSimplePojo2;
	}

	/**
	 * @return the c
	 */
	public Collection<String> getCollectionOfString() {
		return collectionOfString;
	}

	/**
	 * @param c the c to set
	 */
	public void setCollectionOfString(Collection<String> c) {
		this.collectionOfString = c;
	}

	/**
	 * @return the inner
	 */
	public SimplePojo2 getInner() {
		return inner;
	}

	/**
	 * @param inner the inner to set
	 */
	public void setInner(SimplePojo2 inner) {
		this.inner = inner;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
