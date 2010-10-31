/*
 * Copyright (C) 2010 Matthijs Galesloot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sonar.plugins.webscanner;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.webscanner.language.Html;
import org.sonar.plugins.webscanner.language.HtmlFile;

/**
 * @author Matthijs Galesloot
 */
public final class HtmlSourceImporter extends AbstractSourceImporter {

  private static final Logger LOG = LoggerFactory.getLogger(HtmlSourceImporter.class);

  public HtmlSourceImporter(Project project) {
    super(new Html(project));
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    new ProjectConfiguration(project).addSourceDir();

    super.analyse(project, context);
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return isEnabled(project) && getLanguage().equals(project.getLanguage());
  }

  @Override
  protected Resource<?> createResource(File file, List<File> sourceDirs, boolean unitTest) {
    LOG.debug("HtmlSourceImporter:" + file.getPath());
    return HtmlFile.fromIOFile(file, sourceDirs);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}