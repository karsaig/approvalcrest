package com.github.karsaig.approvalcrest.model;

import java.io.InputStream;
import java.io.OutputStream;

public class ClosableFields {

	private InputStream input;
	private OutputStream output;
	
	public void setInput(InputStream input) {
		this.input = input;
	}
	
	public void setOutput(OutputStream output) {
		this.output = output;
	}
}
