package org.equaliz;

import java.text.MessageFormat;

public class EqualizException extends RuntimeException {

	private static final long serialVersionUID = -3817616161785105418L;

	public static final String INVALID_BEAN = "Equalizer only support simple POJO with default constructor. Class is : {0}";
	public static final String INVALID_METHOD = "Could only use getter and setter method. Method is : {0}";
	public static final String INVALID_PROPERTY = "Could not retrieve property : {0}";
	protected static final String NOT_SUPPORTED_CLASS = "Not supported Class : {0}";
	public static final String COLLECTION_ACCES_ERROR = "Could not access collection element within equaliz";
	protected static final String ONLY_GENERIC_COLLECTION_OF_CONCRETE = "Could only equaliz generic collection of concrete object";
	protected static final String SUBCOLLECTION_ACCES_ERROR = "Could not acces collection of collection";

	public EqualizException(String message, Object... args) {
		super(MessageFormat.format(message, args));
	}

}
