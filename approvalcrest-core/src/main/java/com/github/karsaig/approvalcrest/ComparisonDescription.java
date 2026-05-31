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

import java.util.List;

import com.github.karsaig.approvalcrest.matcher.machinereadable.AliasTracker;
import com.github.karsaig.approvalcrest.matcher.machinereadable.IgnoredFieldsTracker;
import com.github.karsaig.approvalcrest.matcher.machinereadable.SortedFieldsTracker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.hamcrest.StringDescription;

/**
 * {@link StringDescription} which holds the mismatch message along with the actual and expected Json representation.
 */
public class ComparisonDescription extends StringDescription {
	private static final Gson OUTPUT_GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private String actual;
	private String expected;
	private String differencesMessage;
	private boolean comparisonFailure;
	private boolean machineReadable;
	private String approvedFilePath;
	private String testInfo;
	private IgnoredFieldsTracker ignoredFieldsTracker;
	private AliasTracker aliasTracker;
	private SortedFieldsTracker sortedFieldsTracker;
	private String note;

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

	public void setTestInfo(String testInfo) {
		this.testInfo = testInfo;
	}

	public void setIgnoredFieldsTracker(IgnoredFieldsTracker tracker) {
		this.ignoredFieldsTracker = tracker;
	}

	public void setAliasTracker(AliasTracker tracker) {
		this.aliasTracker = tracker;
	}

	public void setSortedFieldsTracker(SortedFieldsTracker tracker) {
		this.sortedFieldsTracker = tracker;
	}

	public void setTypesIgnoredConfigured(boolean typesIgnoredConfigured) {
		// Legacy compatibility — builds the note string
		if (typesIgnoredConfigured) {
			setNote("Type-based ignoring is configured but field-level tracking is not available for it.");
		}
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String toFailureMessage(String reason) {
		if (machineReadable) {
			return buildMachineReadableMessage(reason);
		}
		return (isNotBlank(reason) ? reason + "\n" : "") + getDifferencesMessage()
				+ "\n[AI tip] Re-run with system property fmAI=true for structured, machine-readable output.";
	}

	private String buildMachineReadableMessage(String reason) {
		JsonObject root = new JsonObject();
		root.addProperty("failureType", "MISMATCH");
		if (testInfo != null) {
			root.addProperty("test", testInfo);
		}
		if (approvedFilePath != null) {
			root.addProperty("approvedFile", approvedFilePath);
			root.addProperty("action", "Set system property fMUInPlace=true and re-run to update the approved file");
		}
		if (expected != null) {
			root.addProperty("expected", expected);
		}
		if (actual != null) {
			root.addProperty("actual", actual);
		}
		root.add("ignoredFields", buildIgnoredFieldsArray());
		root.add("aliasedFields", buildAliasedFieldsArray());
		root.add("sortedFields", buildSortedFieldsArray());
		if (note != null) {
			root.addProperty("note", note);
		}
		return OUTPUT_GSON.toJson(root);
	}

	private JsonArray buildIgnoredFieldsArray() {
		JsonArray arr = new JsonArray();
		if (ignoredFieldsTracker != null && !ignoredFieldsTracker.isEmpty()) {
			for (IgnoredFieldsTracker.IgnoredField field : ignoredFieldsTracker.getFields()) {
				JsonObject entry = new JsonObject();
				entry.addProperty("path", field.getPath());
				entry.addProperty("reason", field.getReason().name());
				if (field.getPattern() != null) {
					entry.addProperty("pattern", field.getPattern());
				}
				if (field.getCauses() != null && !field.getCauses().isEmpty()) {
					JsonArray causes = new JsonArray();
					for (String cause : field.getCauses()) {
						causes.add(cause);
					}
					entry.add("causes", causes);
				}
				arr.add(entry);
			}
		}
		return arr;
	}

	private JsonArray buildAliasedFieldsArray() {
		JsonArray arr = new JsonArray();
		if (aliasTracker != null && !aliasTracker.isEmpty()) {
			for (AliasTracker.AliasedField field : aliasTracker.getFields()) {
				JsonObject entry = new JsonObject();
				entry.addProperty("path", field.getPath());
				entry.addProperty("originalValue", field.getOriginalValue());
				entry.addProperty("alias", field.getAlias());
				arr.add(entry);
			}
		}
		return arr;
	}

	private JsonArray buildSortedFieldsArray() {
		JsonArray arr = new JsonArray();
		if (sortedFieldsTracker != null && !sortedFieldsTracker.isEmpty()) {
			for (SortedFieldsTracker.SortedField field : sortedFieldsTracker.getFields()) {
				JsonObject entry = new JsonObject();
				entry.addProperty("path", field.getPath());
				entry.addProperty("reason", field.getReason().name());
				if (field.getPattern() != null) {
					entry.addProperty("pattern", field.getPattern());
				}
				arr.add(entry);
			}
		}
		return arr;
	}
}
