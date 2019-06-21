
package io.swagger.codegen.languages;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.SupportingFile;
import io.swagger.codegen.languages.features.BeanValidationFeatures;
import io.swagger.codegen.languages.features.GzipTestFeatures;
import io.swagger.codegen.languages.features.LoggingTestFeatures;
import io.swagger.codegen.languages.features.UseGenericResponseFeatures;
import io.swagger.models.Operation;

public class JavaCXFClientCodegen extends AbstractJavaCodegen
        implements BeanValidationFeatures, UseGenericResponseFeatures, GzipTestFeatures, LoggingTestFeatures {

private static final Logger LOGGER = LoggerFactory.getLogger(JavaCXFClientCodegen.class);

    /**
     * Name of the sub-directory in "src/main/resource" where to find the
     * Mustache template for the JAX-RS Codegen.
     */
    protected static final String JAXRS_TEMPLATE_DIRECTORY_NAME = "JavaJaxRS";

    public static final String JSON_MAPPING_MODE = "jsonMapping";
    
    protected boolean useBeanValidation = false;
    
    protected boolean useGenericResponse = false;

    protected boolean useGzipFeatureForTests = false;

    protected boolean useLoggingFeatureForTests = false;

    protected boolean useJackson = true;
    
    protected boolean useJSR367 = false;
    
    public JavaCXFClientCodegen()
    {
        super();

        supportsInheritance = true;

        sourceFolder = "src/gen/java";
        invokerPackage = "io.swagger.api";
        artifactId = "swagger-jaxrs-client";
        dateLibrary = "legacy"; //TODO: add joda support to all jax-rs

        apiPackage = "io.swagger.api";
        modelPackage = "io.swagger.model";

        outputFolder = "generated-code/JavaJaxRS-CXF";

        // clear model and api doc template as this codegen
        // does not support auto-generated markdown doc at the moment
        //TODO: add doc templates
        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");


        typeMapping.put("date", "LocalDate");

        importMapping.put("LocalDate", "org.joda.time.LocalDate");

        embeddedTemplateDir = templateDir = JAXRS_TEMPLATE_DIRECTORY_NAME + File.separator + "cxf";

        cliOptions.add(CliOption.newBoolean(USE_BEANVALIDATION, "Use BeanValidation API annotations"));

        cliOptions.add(CliOption.newBoolean(USE_GZIP_FEATURE_FOR_TESTS, "Use Gzip Feature for tests"));
        cliOptions.add(CliOption.newBoolean(USE_LOGGING_FEATURE_FOR_TESTS, "Use Logging Feature for tests"));

        cliOptions.add(CliOption.newBoolean(USE_GENERIC_RESPONSE, "Use generic response"));
        
        CliOption jsonMode = new CliOption(JSON_MAPPING_MODE, "Option. The Json mapping to use");        
        Map<String, String> jsonMappingModeOption = new HashMap<>();
        jsonMappingModeOption.put("jackson", "Use the jackson framework");
        jsonMappingModeOption.put("jsonb", "Use JSR-367 Json-B annotations");
        jsonMode.setEnum(jsonMappingModeOption);
        cliOptions.add(jsonMode);
    }


    @Override
    public void processOpts()
    {
        super.processOpts();

        if (additionalProperties.containsKey(USE_BEANVALIDATION)) {
            boolean useBeanValidationProp = convertPropertyToBooleanAndWriteBack(USE_BEANVALIDATION);
            this.setUseBeanValidation(useBeanValidationProp);
        }
        
        if (additionalProperties.containsKey(USE_GENERIC_RESPONSE)) {
            this.setUseGenericResponse(convertPropertyToBoolean(USE_GENERIC_RESPONSE));
        }

        if (useGenericResponse) {
            writePropertyBack(USE_GENERIC_RESPONSE, useGenericResponse);
        }
        
        if ("jackson".equals(additionalProperties.get(JSON_MAPPING_MODE)) || !additionalProperties.containsKey(JSON_MAPPING_MODE)) {
            writePropertyBack("jackson", true);
            writePropertyBack("jsr367", false);
            this.useJackson = true;
            this.useJSR367 = false;
        } else if ("jsr367".equals(additionalProperties.get(JSON_MAPPING_MODE)) || !additionalProperties.containsKey(JSON_MAPPING_MODE)) {
          writePropertyBack("jackson", false);
          writePropertyBack("jsr367", true);
          this.useJackson = false;
          this.useJSR367 = true;
      }
        
        
        this.setUseGzipFeatureForTests(convertPropertyToBooleanAndWriteBack(USE_GZIP_FEATURE_FOR_TESTS));
        this.setUseLoggingFeatureForTests(convertPropertyToBooleanAndWriteBack(USE_LOGGING_FEATURE_FOR_TESTS));


        supportingFiles.clear(); // Don't need extra files provided by AbstractJAX-RS & Java Codegen

        writeOptional(outputFolder, new SupportingFile("pom.mustache", "", "pom.xml"));

    }

    @Override
    public String getName()
    {
        return "jaxrs-cxf-client";
    }


    @Override
    public CodegenType getTag()
    {
        return CodegenType.CLIENT;
    }

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
        super.addOperationToGroup(tag, resourcePath, operation, co, operations);
        co.subresourceOperation = !co.path.isEmpty();
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        model.imports.remove("ApiModelProperty");
        model.imports.remove("ApiModel");
        model.imports.remove("JsonSerialize");
        model.imports.remove("ToStringSerializer");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        objs = super.postProcessOperations(objs);
        return AbstractJavaJAXRSServerCodegen.jaxrsPostProcessOperations(objs);
    }

    @Override
    public String getHelp()
    {
        return "Generates a Java JAXRS Client based on Apache CXF framework.";
    }

    @Override
    public void setUseBeanValidation(boolean useBeanValidation) {
        this.useBeanValidation = useBeanValidation;
    }

    @Override
    public void setUseGzipFeatureForTests(boolean useGzipFeatureForTests) {
        this.useGzipFeatureForTests = useGzipFeatureForTests;
    }

    @Override
    public void setUseLoggingFeatureForTests(boolean useLoggingFeatureForTests) {
        this.useLoggingFeatureForTests = useLoggingFeatureForTests;
    }

    @Override
    public void setUseGenericResponse(boolean useGenericResponse) {
        this.useGenericResponse = useGenericResponse;
    }

}
