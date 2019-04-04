package com.marcinczeczko.jsonschema;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

public class JsonSchemaPlugin implements Plugin<Project> {

  public static final String GENERATE_SCHEMA_TASK = "genJsonSchema";
  public static final String CLEAN_SCHEMA_TASK = "cleanGenJsonSchema";
  public static final String TASK_GROUP_NAME = "JsonSchema";

  @Override
  public void apply(Project project) {
    // apply plugin: 'java'
    project.getPlugins().apply(JavaPlugin.class);

    // classes.dependsOn genJsonSchema
    project.getTasksByName(JavaPlugin.CLASSES_TASK_NAME, true)
        .forEach(task -> task.dependsOn(GENERATE_SCHEMA_TASK));

    JsonSchemaExtension jsonSchemaExtension = project.getExtensions().create(JsonSchemaExtension.NAME, JsonSchemaExtension.class, project);
    JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

    project.getTasks().register(GENERATE_SCHEMA_TASK, JsonSchemaGenerateTask.class, task -> {
      task.setGroup(TASK_GROUP_NAME);
      task.setDescription("Generates Json Schema");
      task.getModelsRelativePath().set(jsonSchemaExtension.getModelsRelativePath());
      task.getDestinationDir().set(jsonSchemaExtension.getDestinationDirectory().dir(jsonSchemaExtension.getModelsRelativePath()));
      task.getCompiledClasses().setFrom(javaConvention.getSourceSets().findByName(SourceSet.MAIN_SOURCE_SET_NAME).getRuntimeClasspath());
    });

    // clean.dependsOn cleanGenJsonSchema (automatically generated clean task based on @Output annotations)
    project.getTasks().findByName(BasePlugin.CLEAN_TASK_NAME).dependsOn(CLEAN_SCHEMA_TASK);

    project.getPluginManager().apply(JsonSchemaPlugin.class);
  }
}
