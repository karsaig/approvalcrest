package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameContentAsApproved;

import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.Test.None;

public class ContentMatcherTests {

	@Test(expected = None.class)
	public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApproved() {
		String actual = "Example content";
		assertThat(actual, sameContentAsApproved());
	}

	@Test(expected = ComparisonFailure.class)
	public void shouldThrowAssertionErrorWhenContentDiffersFromApprovedContent() {
		String actual = "Modified content";
		assertThat(actual, sameContentAsApproved());
	}

	@Test(expected = None.class)
	public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueId() {
		String actual = "Content";
		assertThat(actual, sameContentAsApproved().withUniqueId("idTest"));
	}

	@Test(expected = None.class)
	public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileName() {
		String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
		assertThat(actual, sameContentAsApproved().withFileName("single-line"));
	}

	@Test(expected = None.class)
	public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndPathName() {
		String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
		assertThat(actual, sameContentAsApproved().withPathName("src/test/contents").withFileName("single-line-2"));
	}

	@Test(expected = None.class)
	public void shouldNotThrowAssertionErrorWhenMultiLineContentIsSameContentAsApprovedWithFileName() {
		String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\nMauris gravida varius dolor, vel imperdiet urna consectetur a.";
		assertThat(actual, sameContentAsApproved().withFileName("multi-line-no-empty-line"));
	}

	@Test(expected = None.class)
	public void shouldNotThrowAssertionErrorWhenMultiLineContentWithEmptyLineIsSameContentAsApprovedWithFileName() {
		String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\nMauris gravida varius dolor, vel imperdiet urna consectetur a.\n";
		assertThat(actual, sameContentAsApproved().withFileName("multi-line-with-empty-line"));
	}

	@Test(expected = None.class)
	public void shouldNotThrowAssertionErrorWhenLongContentIsSameContentAsApprovedWithFileName() {
		String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris gravida varius dolor, vel imperdiet urna consectetur a. Quisque a massa quis neque imperdiet mattis id nec augue. Praesent vitae odio in orci hendrerit pretium quis eu enim. Maecenas arcu urna, vestibulum at mauris sit amet, cursus mollis quam. Ut laoreet vestibulum nisi, in auctor tellus pharetra nec. Pellentesque vulputate lorem velit, sit amet placerat tortor blandit scelerisque. Ut eget risus ut magna faucibus aliquet. Sed semper lobortis nisi, eu egestas massa malesuada vitae. Aenean sagittis ultrices libero, sit amet tempor arcu eleifend et. Nulla facilisi. Proin sit amet ipsum ut est vestibulum mollis non viverra erat. Nunc libero velit, vestibulum sit amet lorem id, cursus hendrerit risus. Vivamus dignissim lacus sem, a eleifend sapien imperdiet eu. Integer rutrum pulvinar augue a ullamcorper. In lacinia feugiat dignissim. Sed sollicitudin orci sit amet leo ultricies gravida.\n\nNullam nec arcu blandit, lobortis velit a, placerat lorem. Phasellus vitae porttitor felis, in sodales est. Pellentesque venenatis turpis eu tortor lacinia iaculis. Aliquam lacus lectus, laoreet a odio sit amet, lacinia laoreet ex. Nullam tempor sapien vel tortor sollicitudin, eu facilisis tellus consequat. Integer neque arcu, tempus feugiat felis quis, porta consequat augue. Nulla sem lacus, euismod vitae pretium non, sagittis at massa. Fusce laoreet consectetur sapien elementum fringilla."
				+ "\n\nIn dignissim nisl enim, quis pellentesque purus tempor quis. Integer varius, sapien a commodo suscipit, libero nisi malesuada justo, in iaculis quam nisl vitae arcu. Quisque non semper leo. Quisque porta placerat finibus. Fusce tempor porta varius. Nullam ullamcorper vitae nisi in faucibus. Vestibulum eget dolor cursus, condimentum massa at, viverra turpis. Sed eu aliquam mauris, sit amet efficitur metus. Mauris ac sodales est."
				+ "\n\nInteger nunc neque, semper non nisi nec, vehicula sodales sem. Nullam dictum est eu porta tempus. Pellentesque malesuada convallis neque. Nunc ultricies faucibus leo, et aliquam purus imperdiet vel. Mauris nunc est, dignissim vel mi et, posuere ornare metus. Donec lectus arcu, consectetur sed iaculis sit amet, cursus non nunc. Etiam dictum justo at nisl laoreet, maximus pharetra elit imperdiet. Pellentesque vel diam at leo scelerisque porttitor nec ut purus. Ut cursus lobortis malesuada. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. In dapibus, dui eu posuere egestas, augue orci rutrum ex, non suscipit purus ligula at urna. Nunc vel arcu non mi molestie consectetur. Morbi dignissim magna at urna mattis gravida. Pellentesque placerat est lacus, sed varius orci tempor ac."
				+ "\n\nQuisque consectetur, sapien non aliquam luctus, est urna aliquam purus, eu feugiat diam ex in ex. Phasellus ut nibh vitae libero iaculis accumsan. Morbi vitae orci ut eros tempus rhoncus ut eget justo. Vestibulum a accumsan urna. Cras sit amet lectus sed eros sollicitudin maximus. Fusce at vulputate eros, in bibendum ipsum. Etiam vitae malesuada metus, ac mattis felis. Nullam in facilisis justo. Morbi elementum vestibulum eros in vehicula.";
		assertThat(actual, sameContentAsApproved().withFileName("long-content"));
	}
	
	@Test(expected = None.class)
	public void shouldNotThrowAssertionErrorWhenMultiLineContentWithEmptyLineAndWindowsNewLineIsSameContentAsApprovedWithFileName() {
		String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\r\nMauris gravida varius dolor, vel imperdiet urna consectetur a.\r\n";
		assertThat(actual, sameContentAsApproved().withFileName("multi-line-with-windows-newline"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionrWhenContentIsNull() {
		assertThat(null, sameContentAsApproved());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionrWhenContentIsANumber() {
		assertThat(1L, sameContentAsApproved());
	}
}
