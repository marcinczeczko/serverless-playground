package com.marcinczeczko.jsonschema;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

public class JsonSchemaExtension {

  public static final String NAME = "jsonSchema";
  private static final String DEST_DIR_CONVENTION = "models";

  private DirectoryProperty destinationDirectory;

  private Property<String> modelsRelativePath;

  public JsonSchemaExtension(Project project) {
    this.destinationDirectory = project.getObjects().directoryProperty().convention(project.getLayout().getBuildDirectory());
    this.modelsRelativePath = project.getObjects().property(String.class).convention(DEST_DIR_CONVENTION);
  }

  public DirectoryProperty getDestinationDirectory() {
    return destinationDirectory;
  }

  public Property<String> getModelsRelativePath() {
    return modelsRelativePath;
  }
}
