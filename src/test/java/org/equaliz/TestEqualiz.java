package org.equaliz;

import static org.equaliz.Equaliz.with;
import static org.equaliz.Equaliz.withElementOf;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.equaliz.model.SimplePojo;
import org.equaliz.model.SimplePojo2;
import org.junit.Test;

public class TestEqualiz {

	@Test
	public void testEqualizerSimpleEquals() {

		with(SimplePojo.class).getName();
		Equalizer equalizer = Equaliz.createEqualizer(SimplePojo.class);

		SimplePojo p1 = new SimplePojo("test");
		SimplePojo p2 = new SimplePojo("test");
		Assert.assertTrue(equalizer.equals(p1, p2));

		p2.setName("testFail");
		Assert.assertFalse(equalizer.equals(p1, p2));

	}

	@Test
	public void testEqualizerInnerEquals() {

		with(SimplePojo.class).getName();
		with(SimplePojo.class).getInner().getName();
		Equalizer equalizer = Equaliz.createEqualizer(SimplePojo.class);

		SimplePojo p1 = new SimplePojo("test");
		SimplePojo p2 = new SimplePojo("test");
		p1.setInner(new SimplePojo2("tata"));
		p2.setInner(new SimplePojo2("tata"));

		Assert.assertTrue(equalizer.equals(p1, p2));

		p2.setInner(new SimplePojo2("tati"));
		Assert.assertFalse(equalizer.equals(p1, p2));

	}

	@Test
	public void testEqualizerSimpleCollectionEquals() {

		with(SimplePojo.class).getCollectionOfString();
		Equalizer equalizer = Equaliz.createEqualizer(SimplePojo.class);

		Collection<String> c1 = new ArrayList<String>();
		c1.add("test");
		c1.add("test2");

		Collection<String> c2 = new ArrayList<String>();
		c2.add("test");
		c2.add("test2");

		SimplePojo p1 = new SimplePojo();
		SimplePojo p2 = new SimplePojo();

		p1.setCollectionOfString(c1);
		p2.setCollectionOfString(c2);

		Assert.assertTrue(equalizer.equals(p1, p2));

		c2.clear();
		c2.add("test");
		c2.add("testfail");
		Assert.assertFalse(equalizer.equals(p1, p2));
		Assert.assertFalse(equalizer.equals(p1, p2));
	}

	@Test
	public void testEqualizerComplexeCollectionEquals() {

		withElementOf(with(SimplePojo.class).getCollectionOfSimplePojo2()).getName();
		Equalizer equalizer = Equaliz.createEqualizer(SimplePojo.class);

		Collection<SimplePojo2> c1 = new ArrayList<SimplePojo2>();
		c1.add(new SimplePojo2("test1"));
		c1.add(new SimplePojo2("test2"));

		Collection<SimplePojo2> c2 = new ArrayList<SimplePojo2>();
		c2.add(new SimplePojo2("test1"));
		c2.add(new SimplePojo2("test2"));

		SimplePojo p1 = new SimplePojo();
		SimplePojo p2 = new SimplePojo();
		p1.setCollectionOfSimplePojo2(c1);
		p2.setCollectionOfSimplePojo2(c2);

		Assert.assertTrue(equalizer.equals(p1, p2));

		c2.clear();
		c2.add(new SimplePojo2("test1"));
		c2.add(new SimplePojo2("test3"));
		Assert.assertFalse(equalizer.equals(p1, p2));
	}

	@Test
	public void testEqualizerSimpleHashCode() {

		with(SimplePojo.class).getName();
		Equalizer equalizer = Equaliz.createEqualizer(SimplePojo.class);

		SimplePojo p1 = new SimplePojo("test");
		Assert.assertEquals(equalizer.hashCode(p1), p1.getName().hashCode());
	}

	@Test
	public void testEqualizerInnerHashCode() {

		with(SimplePojo.class).getName();
		with(SimplePojo.class).getInner().getName();
		Equalizer equalizer = Equaliz.createEqualizer(SimplePojo.class);

		SimplePojo p1 = new SimplePojo("test");
		p1.setInner(new SimplePojo2("tata"));

		Assert.assertEquals(equalizer.hashCode(p1), p1.getName().hashCode() + p1.getInner().getName().hashCode());
	}

	@Test
	public void testEqualizerSimpleCollectionHashCode() {
		with(SimplePojo.class).getCollectionOfString();
		Equalizer equalizer = Equaliz.createEqualizer(SimplePojo.class);

		Collection<String> c1 = new ArrayList<String>();
		c1.add("test");
		c1.add("test2");
		SimplePojo p1 = new SimplePojo();

		p1.setCollectionOfString(c1);
		Assert.assertEquals(equalizer.hashCode(p1), "test".hashCode() + "test2".hashCode());
	}

	@Test
	public void testEqualizerComplexeCollectionHashCode() {
		withElementOf(with(SimplePojo.class).getCollectionOfSimplePojo2()).getName();
		Equalizer equalizer = Equaliz.createEqualizer(SimplePojo.class);

		Collection<SimplePojo2> c1 = new ArrayList<SimplePojo2>();
		c1.add(new SimplePojo2("test1"));
		c1.add(new SimplePojo2("test2"));

		SimplePojo p1 = new SimplePojo();
		p1.setCollectionOfSimplePojo2(c1);

		Assert.assertEquals(equalizer.hashCode(p1), "test1".hashCode() + "test2".hashCode());
	}

}
