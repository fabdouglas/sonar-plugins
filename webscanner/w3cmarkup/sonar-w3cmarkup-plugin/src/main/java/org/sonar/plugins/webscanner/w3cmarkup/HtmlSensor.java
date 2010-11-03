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

package org.sonar.plugins.webscanner.w3cmarkup;

import java.io.File;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.webscanner.Configuration;
import org.sonar.plugins.webscanner.ProjectConfiguration;
import org.sonar.plugins.webscanner.html.FileSet;
import org.sonar.plugins.webscanner.html.HtmlScanner;
import org.sonar.plugins.webscanner.language.Html;
import org.sonar.plugins.webscanner.language.HtmlFile;
import org.sonar.plugins.webscanner.language.HtmlProperties;
import org.sonar.plugins.webscanner.markup.MarkupMessage;
import org.sonar.plugins.webscanner.markup.MarkupReport;
import org.sonar.plugins.webscanner.markup.MarkupValidator;

/**
 * @author Matthijs Galesloot
 * @since 0.1
 */
public final class HtmlSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(HtmlSensor.class);

  private final RulesProfile profile;
  private final RuleFinder ruleFinder;
  private final MavenSession session;

  public HtmlSensor(MavenSession session, RulesProfile profile, RuleFinder ruleFinder) {
    LOG.info("Profile: " + profile.getName());
    this.session = session;
    this.profile = profile;
    this.ruleFinder = ruleFinder;
  }

  private void prepareHtml(File htmlDir) {
    HtmlScanner htmlScanner = new HtmlScanner();
    htmlScanner.prepare(htmlDir);
  }

  /**
   * Find W3C Validation Markup reports in the source tree and save violations to Sonar. The Markup reports have file extension .mur.
   */
  public void analyse(Project project, SensorContext sensorContext) {

    configureProxy();

    ProjectConfiguration projectConfiguration = new ProjectConfiguration(project);
    projectConfiguration.addSourceDir();

    int numValid = 0;
    int numFiles = 0;

    MessageFilter messageFilter = new MessageFilter(project.getProperty(HtmlProperties.EXCLUDE_VIOLATIONS));

    for (File sourceDir : projectConfiguration.getSourceDirs()) {
      if ( !sourceDir.exists()) {
        LOG.error("Missing HTML directory: " + sourceDir.getPath());
        continue;
      }

      LOG.info("HTML Dir:" + sourceDir);

      prepareHtml(sourceDir);

      MarkupValidator markupValidator = new MarkupValidator();
      markupValidator.validateFiles(sourceDir);

      Collection<File> files = FileSet.getReportFiles(sourceDir, MarkupReport.REPORT_SUFFIX);

      for (File reportFile : files) {
        MarkupReport report = MarkupReport.fromXml(reportFile);

        numFiles++;
        if (report.isValid()) {
          numValid++;
        }

        // derive name of resource from name of report
        File file = new File(StringUtils.substringBefore(report.getReportFile().getPath(), MarkupReport.REPORT_SUFFIX));
        HtmlFile resource = HtmlFile.fromIOFile(file, project.getFileSystem().getSourceDirs());

        // save errors
        for (MarkupMessage error : report.getErrors()) {
          if (messageFilter.accept(error)) {
            addViolation(sensorContext, resource, error, true);
          }
        }
        // save warnings
        for (MarkupMessage warning : report.getWarnings()) {
          if (messageFilter.accept(warning)) {
            addViolation(sensorContext, resource, warning, false);
          }
        }
      }
    }

    double percentageValid = numFiles > 0 ? (double) (numFiles - numValid) / numFiles : 100;
    sensorContext.saveMeasure(HtmlMetrics.W3C_MARKUP_VALIDITY, percentageValid);
  }

  private void configureProxy() {
    Settings settings = session.getSettings();
    if (settings.getActiveProxy() != null) {
      LOG.info("configure proxy...");
      Configuration.setProxyHost(settings.getActiveProxy().getHost());
      Configuration.setProxyPort(settings.getActiveProxy().getPort());
    }
  }

  private String makeIdentifier(String idString) {
    int id = NumberUtils.toInt(idString, -1);
    if (id >= 0) {
      return String.format("%03d", id);
    } else {
      return idString;
    }
  }

  private void addViolation(SensorContext sensorContext, HtmlFile resource, MarkupMessage message, boolean error) {
    String ruleKey = makeIdentifier(message.getMessageId());
    Rule rule = ruleFinder.findByKey(MarkupRuleRepository.REPOSITORY_KEY, ruleKey);
    if (rule != null) {
      Violation violation = Violation.create(rule, resource).setLineId(message.getLine());
      violation.setMessage((error ? "" : "Warning: ") + message.getMessage());
      sensorContext.saveViolation(violation);
      LOG.debug(resource.getName() + ": " + message.getMessageId() + ":" + message.getMessage());
    } else {
      LOG.warn("Could not find Markup Rule " + message.getMessageId() + ", Message = " + message.getMessage());
    }
  }

  /**
   * This sensor only executes on Web projects with W3C Markup rules.
   */
  public boolean shouldExecuteOnProject(Project project) {
    return isEnabled(project) && Html.KEY.equals(project.getLanguage().getKey()) && hasMarkupRules();
  }

  private boolean hasMarkupRules() {
    for (ActiveRule activeRule : profile.getActiveRules()) {
      if (MarkupRuleRepository.REPOSITORY_KEY.equals(activeRule.getRepositoryKey())) {
        return true;
      }
    }
    return false;
  }

  private boolean isEnabled(Project project) {
    return project.getConfiguration().getBoolean(CoreProperties.CORE_IMPORT_SOURCES_PROPERTY,
        CoreProperties.CORE_IMPORT_SOURCES_DEFAULT_VALUE);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
