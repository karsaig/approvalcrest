/*
 * Copyright 2013 Shazam Entertainment Limited
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.github.karsaig.approvalcrest;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.hamcrest.StringDescription;

/**
 * {@link StringDescription} which holds the mismatch message along with the actual and expected Json representation.
 */
public class ComparisonDescription extends StringDescription {
	private String actual;
	private String expected;
	private String differencesMessage;
	private boolean comparisonFailure;
	private boolean machineReadable;
	private String approvedFilePath;

	public String getActual() {
		return actual;
	}

	public String getExpected() {
		return expected;
	}

	public String getDifferencesMessage() {
		return differencesMessage;
	}

	public void setActual(String actual) {
		this.actual = actual;
	}

	public void setExpected(String expected) {
		this.expected = expected;
	}

	public void setDifferencesMessage(String differencesMessage) {
		this.differencesMessage = differencesMessage;
	}

	public void setComparisonFailure(boolean comparisonFailure) {
		this.comparisonFailure = comparisonFailure;
	}

	public boolean isComparisonFailure() {
		return comparisonFailure;
	}

	public boolean isMachineReadable() {
		return machineReadable;
	}

	public void setMachineReadable(boolean machineReadable) {
		this.machineReadable = machineReadable;
	}

	public void setApprovedFilePath(String approvedFilePath) {
		this.approvedFilePath = approvedFilePath;
	}

	public String toFailureMessage(String reason) {
		if (machineReadable) {
			return buildMachineReadableMessage(reason);
		}
		return (isNotBlank(reason) ? reason + "\n" : "") + getDifferencesMessage();
	}

	private String buildMachineReadableMessage(String reason) {
		StringBuilder sb = new StringBuilder();
		if (isNotBlank(reason)) {
			sb.append(reason).append("\n");
		}
		if (approvedFilePath != null) {
			sb.append("Approved file (expected): ").append(approvedFilePath).append("\n");
			sb.append("Tip: to update approved content with current actual, re-run with -DfileMatcherUpdateInPlace=true\n");
		} else if (expected != null) {
			sb.append("=== EXPECTED (full) ===\n").append(expected).append("\n=== END EXPECTED ===\n");
		}
		sb.append("\n");
		if (actual != null) {
			sb.append("=== ACTUAL (full) ===\n").append(actual).append("\n=== END ACTUAL ===");
		}
		return sb.toString();
	}
}
