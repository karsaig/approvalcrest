package com.github.karsaig.json.ignore;

import static com.fasterxml.jackson.core.util.VersionUtil.parseVersion;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public abstract class AbstractFieldIgnoringModule extends Module {
    private static final String APPROVAL_CREST_VERSION = "0.1";
    private static final String GROUP_ID = "com.github.karsaig.approvalcrest.providers.jackson";
    private static final String JACKSON_VERSION_AS_ARTIFACT_ID = "2.6.3";

    private static final Version VERSION = parseVersion(APPROVAL_CREST_VERSION, GROUP_ID, JACKSON_VERSION_AS_ARTIFACT_ID);

    private final BeanSerializerModifier beanSerializerModifier;
    private final String moduleName;

    public AbstractFieldIgnoringModule(BeanSerializerModifier beanSerializerModifier, String moduleName) {
        this.beanSerializerModifier = beanSerializerModifier;
        this.moduleName = moduleName;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public Version version() {
        return VERSION;
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addBeanSerializerModifier(beanSerializerModifier);
    }
}
