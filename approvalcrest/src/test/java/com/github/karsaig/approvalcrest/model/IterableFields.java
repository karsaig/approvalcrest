package com.github.karsaig.approvalcrest.model;

import java.util.Collection;

import com.github.karsaig.approvalcrest.model.cyclic.Two;

public class IterableFields {

	private Iterable<Throwable> ones;
	private Collection<Two> twos;
	
	public void setOnes(Iterable<Throwable> ones) {
		this.ones = ones;
	}
	
	public void setTwos(Collection<Two> twos) {
		this.twos = twos;
	}
}
