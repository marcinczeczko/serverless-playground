package com.marcinczeczko.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

public class JsonSchemaGenerateTask extends DefaultTask {

  private final ConfigurableFileCollection compiledClasses = getProject().files();

  private final DirectoryProperty destinationDirectory = getProject().getObjects().directoryProperty();

  private final Property<String> modelsRelativePath = getProject().getObjects().property(String.class);

  @Input
  public ConfigurableFileCollection getCompiledClasses() {
    return compiledClasses;
  }

  @OutputDirectory
  public DirectoryProperty getDestinationDir() {
    return destinationDirectory;
  }

  public Property<String> getModelsRelativePath() {
    return modelsRelativePath;
  }

  @TaskAction
  void execute() {
    Reflections reflections = new Reflections(ConfigurationBuilder.build(getProject().getGroup(), getClassLoader()).setExpandSuperTypes(false));
    Set<Class<?>> schemaPojos = reflections.getTypesAnnotatedWith(JsonSchemaTitle.class);

    List<Map<String, String>> models = new ArrayList<>();
    schemaPojos.forEach(clazz -> {
          String schema = generateSchema(clazz);
          Map<String, String> model = new HashMap<>();
          model.put("name", schema.substring(0, schema.indexOf('.')));
          model.put("contentType", "application/json");
          model.put("schema", String.format("${file(%s/%s)}", modelsRelativePath.get(), schema));
          models.add(model);
        }
    );

    File outFile = new File(destinationDirectory.get().getAsFile(), "models.json");
    ObjectMapper objectMapper = new ObjectMapper();
    try (FileWriter writer = new FileWriter(outFile, true)) {
      writer.write(
          objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(models));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String generateSchema(Class<?> clazz) {
    File outFile = new File(destinationDirectory.get().getAsFile(), clazz.getSimpleName() + ".json");

    ObjectMapper objectMapper = new ObjectMapper();

    JsonSchemaConfig config = JsonSchemaConfig.vanillaJsonSchemaDraft4();
    JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(objectMapper, config);

    JsonNode jsonSchema = jsonSchemaGenerator.generateJsonSchema(clazz);

    try (FileWriter writer = new FileWriter(outFile, true)) {
      writer.write(
          objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonSchema));
    } catch (Exception e) {
      e.printStackTrace();
    }

    return outFile.getName();
  }

  private URLClassLoader getClassLoader() {
    Set<File> compiledClassPath = compiledClasses.getFiles();

    URL[] urls = compiledClassPath.stream().map(this::fileToUrl).collect(Collectors.toList()).toArray(new URL[0]);

    return new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
  }

  private URL fileToUrl(File file) {
    URL url = null;
    try {
      url = file.toURI().toURL();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return url;
  }
}
