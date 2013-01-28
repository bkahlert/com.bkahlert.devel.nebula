package com.bkahlert.devel.nebula.colors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

;

public class ColorUtilsTest {

	private RGB colorBlack;
	private RGB colorWhite;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		colorBlack = new RGB(0, 0, 0);
		colorWhite = new RGB(1, 1, 1);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {

	}

	/**
	 * Test method for
	 * {@link de.fu_berlin.inf.dpp.util.ColorUtils#addLightness(org.eclipse.swt.graphics.RGB, float)}
	 * .
	 */
	@Test
	public void testAddLightnessRGBFloat() {
		RGB colorTest = new RGB(0.5, 0.5, 0.5);
		RGB shouldBeWhite = ColorUtils.addLightness(colorTest, +1);
		RGB shouldBeBlack = ColorUtils.addLightness(colorTest, -1);
		RGB shouldBeSame = ColorUtils.addLightness(colorTest, 0);

		assertTrue("should be White", shouldBeWhite.equals(colorWhite));
		assertTrue("should be Black", shouldBeBlack.equals(colorBlack));
		assertTrue("should be Same", shouldBeSame.equals(colorTest));
	}

	/**
	 * Test method for
	 * {@link de.fu_berlin.inf.dpp.util.ColorUtils#addLightness(org.eclipse.swt.graphics.RGB, float)}
	 * .
	 */
	@Test(expected = IndexOutOfBoundsException.class)
	public void testAddLightnessRGBFloat2() {
		ColorUtils.addLightness(new RGB(0.5, 0.5, 0.5), -2);
	}

	/**
	 * Test method for
	 * {@link de.fu_berlin.inf.dpp.util.ColorUtils#addLightness(org.eclipse.swt.graphics.Color, float)}
	 * .
	 */
	@Test
	public void testAddLightnessColorFloat() {
		// TODO
		// how to get device?
		// its the same like RGB testing
	}

	@Test(expected = IllegalArgumentException.class)
	public void testScaleLightnessWithInvalidRange() {
		ColorUtils.scaleLightnessBy(new RGB(0.5, 0.5, 0.5), -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testScaleSaturationWithInvalidRange() {
		ColorUtils.scaleSaturationBy(new RGB(0.5, 0.5, 0.5), -1);
	}

	/**
	 * Test method for
	 * {@link de.fu_berlin.inf.dpp.util.ColorUtils#scaleColorBy(org.eclipse.swt.graphics.RGB, float)}
	 * .
	 */
	@Test
	public void testScaleColorByRGBFloat() {

		RGB colorTest = new RGB(0.5, 0.5, 0.5);
		RGB shouldBeBlack = ColorUtils.scaleLightnessBy(colorTest, 0);
		RGB shouldBeSame = ColorUtils.scaleLightnessBy(colorTest, 1);
		RGB shouldBeBrighter = ColorUtils.scaleLightnessBy(colorTest, 2);

		assertTrue("should be Black", shouldBeBlack.equals(colorBlack));
		assertTrue("should be the Same", shouldBeSame.equals(colorTest));
		assertTrue("should be brighter",
				shouldBeBrighter.toClassicRGB().red > 128);
	}

	/**
	 * Test method for
	 * {@link de.fu_berlin.inf.dpp.util.ColorUtils#scaleColorBy(org.eclipse.swt.graphics.Color, float)}
	 * .
	 */
	@Test
	public void testScaleColorByColorFloat() {
		// TODO
		// how to get device?
		// its the same like RGB testing
	}

	@Test
	public void testGetBestComplementColorHLS() {
		assertNotNull(ColorUtils.getBestComplementColorHLS(null));
		assertNotNull(ColorUtils
				.getBestComplementColorHLS(new LinkedList<HLS>()));

		assertEquals(
				0.5,
				ColorUtils.getBestComplementColorHLS(
						Arrays.asList(new HLS(0, 0.5, 0.5),
								new HLS(1, 0.5, 0.5))).getHue(), 0.05);

		assertEquals(
				0.5,
				ColorUtils.getBestComplementColorHLS(
						Arrays.asList(new HLS(1, 0.5, 0.5),
								new HLS(1, 0.5, 0.5))).getHue(), 0.05);

		assertEquals(
				0.5,
				ColorUtils.getBestComplementColorHLS(
						Arrays.asList(new HLS(0, 0.5, 0.5),
								new HLS(0, 0.5, 0.5))).getHue(), 0.05);

		assertEquals(
				0.5,
				ColorUtils.getBestComplementColorHLS(
						Arrays.asList(new HLS(0.2, 0.5, 0.5), new HLS(0.8, 0.5,
								0.5))).getHue(), 0.05);

		assertEquals(
				0.8,
				ColorUtils.getBestComplementColorHLS(
						Arrays.asList(new HLS(0.2, 0.5, 0.5), new HLS(0.4, 0.5,
								0.5))).getHue(), 0.05);

	}
}
