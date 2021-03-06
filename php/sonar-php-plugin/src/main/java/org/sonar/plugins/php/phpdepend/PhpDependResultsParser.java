/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Sonar PHP Plugin
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.phpdepend;

import static org.sonar.api.measures.CoreMetrics.CLASSES;
import static org.sonar.api.measures.CoreMetrics.COMMENT_LINES;
import static org.sonar.api.measures.CoreMetrics.COMPLEXITY;
import static org.sonar.api.measures.CoreMetrics.FILES;
import static org.sonar.api.measures.CoreMetrics.FUNCTIONS;
import static org.sonar.api.measures.CoreMetrics.LINES;
import static org.sonar.api.measures.CoreMetrics.NCLOC;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ResourceUtils;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.api.PhpFile;
import org.sonar.plugins.php.phpdepend.xml.ClassNode;
import org.sonar.plugins.php.phpdepend.xml.FileNode;
import org.sonar.plugins.php.phpdepend.xml.FunctionNode;
import org.sonar.plugins.php.phpdepend.xml.MethodNode;
import org.sonar.plugins.php.phpdepend.xml.MetricsNode;
import org.sonar.plugins.php.phpdepend.xml.PackageNode;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

/**
 * The PhpDependResultsParser par pdepend reports files and associate measures with metrics and resources.
 */
public class PhpDependResultsParser implements BatchExtension {

  private static final Logger LOG = LoggerFactory.getLogger(PhpDependResultsParser.class);
  private static final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = { 1, 2, 4, 6, 8, 10, 12 };
  private static final Number[] CLASSES_DISTRIB_BOTTOM_LIMITS = { 0, 5, 10, 20, 30, 60, 90 };

  /**
   * The context.
   */
  private SensorContext context;

  /**
   * The metrics.
   */
  private Set<Metric> metrics;

  /**
   * The project.
   */
  private Project project;

  /**
   * Resources bag to store metrics and their values.
   */
  private ResourcesBag resourcesBag;

  private boolean measureUnitTests = true;

  /**
   * Instantiates a new php depend results parser.
   * 
   * @param config
   *          the config
   * @param context
   *          the context
   */
  public PhpDependResultsParser(Project project, SensorContext context) {
    this.project = project;
    this.context = context;
    this.resourcesBag = new ResourcesBag();
    this.metrics = getMetrics();
  }

  /**
   * Returns true if unit tests are counted when measuring complexity and LOC.
   * 
   * @return true if unit tests are counted when measuring complexity and LOC.
   */
  public boolean isMeasureUnitTests() {
    return measureUnitTests;
  }

  /**
   * Sets whether unit tests are counted when measuring complexity and LOC.
   * 
   * @param measureUnitTests
   *          true if unit tests are counted when measuring complexity and LOC.
   */
  public void setMeasureUnitTests(boolean measureUnitTests) {
    this.measureUnitTests = measureUnitTests;
  }

  /**
   * If the given value is not null, the metric, resource and value will be associated
   * 
   * @param file
   *          the file
   * @param metric
   *          the metric
   * @param value
   *          the value
   */
  private void addMeasure(PhpFile file, Metric metric, Double value) {
    if (value != null) {
      resourcesBag.add(value, metric, file);
    }
  }

  /**
   * Adds the measure if the given metrics isn't already present on this resource.
   * 
   * @param file
   * @param metric
   * @param value
   */
  private void addMeasureIfNecessary(PhpFile file, Metric metric, double value) {
    Double measure = resourcesBag.getMeasure(metric, file);
    if (measure == null || measure == 0) {
      resourcesBag.add(value, metric, file);
    }
  }

  /**
   * Collects the given class measures and launches {@see #collectFunctionMeasures(MethodNode, PhpFile)} for all its descendant.
   * 
   * @param file
   *          the php related file
   * @param classNode
   *          representing the class in the report file
   * @param methodComplexityDistribution
   */
  private void collectClassMeasures(ClassNode classNode, PhpFile file, RangeDistributionBuilder methodComplexityDistribution) {
    addMeasureIfNecessary(file, LINES, classNode.getLinesNumber());
    addMeasureIfNecessary(file, COMMENT_LINES, classNode.getCommentLineNumber());
    addMeasureIfNecessary(file, NCLOC, classNode.getCodeLinesNumber());
    // Adds one class to this file
    addMeasure(file, CLASSES, 1.0);
    // for all methods in this class.
    List<MethodNode> methodes = classNode.getMethodes();
    if (methodes != null && !methodes.isEmpty()) {
      for (MethodNode methodNode : methodes) {
        collectMethodMeasures(methodNode, file);
        methodComplexityDistribution.add(methodNode.getComplexity());
      }
    }
  }

  /**
   * Collects the given function measures.
   * 
   * @param file
   *          the php related file
   * @param functionNode
   *          representing the class in the report file
   * @param methodComplexityDistribution
   */
  private void collectFunctionsMeasures(FunctionNode functionNode, PhpFile file, RangeDistributionBuilder methodComplexityDistribution) {
    addMeasureIfNecessary(file, LINES, functionNode.getLinesNumber());
    addMeasureIfNecessary(file, COMMENT_LINES, functionNode.getCommentLineNumber());
    addMeasureIfNecessary(file, NCLOC, functionNode.getCodeLinesNumber());
    // Adds one class to this file
    addMeasure(file, FUNCTIONS, 1.0);
    addMeasure(file, COMPLEXITY, functionNode.getComplexity());
    methodComplexityDistribution.add(functionNode.getComplexity());
  }

  /**
   * Collect the fiven php file measures and launches {@see #collectClassMeasures(ClassNode, PhpFile)} for all its descendant. Indeed even
   * if it's not a good practice it isn't illegal to have more than one public class in one php file.
   * 
   * @param file
   *          the php file
   * @param fileNode
   *          the node representing the file in the report file.
   */
  private void collectFileMeasures(FileNode fileNode, PhpFile file) {
    addMeasure(file, LINES, fileNode.getLinesNumber());
    addMeasure(file, CoreMetrics.NCLOC, fileNode.getCodeLinesNumber());
    addMeasure(file, CoreMetrics.COMMENT_LINES, fileNode.getCommentLineNumber());
    // Adds one file to this php file
    addMeasure(file, CoreMetrics.FILES, 1.0);
    // for all class in this file
    RangeDistributionBuilder classComplexityDistribution = new RangeDistributionBuilder(CoreMetrics.CLASS_COMPLEXITY_DISTRIBUTION,
        CLASSES_DISTRIB_BOTTOM_LIMITS);
    RangeDistributionBuilder methodComplexityDistribution = new RangeDistributionBuilder(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION,
        FUNCTIONS_DISTRIB_BOTTOM_LIMITS);
    if (fileNode.getClasses() != null) {
      for (ClassNode classNode : fileNode.getClasses()) {
        collectClassMeasures(classNode, file, methodComplexityDistribution);
        classComplexityDistribution.add(classNode.getComplexity());
      }// for all class in this file
    }
    if (fileNode.getFunctions() != null) {
      for (FunctionNode funcNode : fileNode.getFunctions()) {
        collectFunctionsMeasures(funcNode, file, methodComplexityDistribution);
      }
    }
    // String fileName = fileNode.getFileName();
    context.saveMeasure(file, classComplexityDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
    context.saveMeasure(file, methodComplexityDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
  }

  /**
   * Collect function measures.
   * 
   * @param file
   *          the file
   * @param methodNode
   *          the method node
   */
  private void collectMethodMeasures(MethodNode methodNode, PhpFile file) {
    // Adds one method to this file
    addMeasure(file, CoreMetrics.FUNCTIONS, 1.0);
    addMeasure(file, CoreMetrics.COMPLEXITY, methodNode.getComplexity());
  }

  /**
   * Collect measures.
   * 
   * @param reportXml
   *          the report xml
   * @throws FileNotFoundException
   *           the file not found exception
   * @throws ParseException
   *           the parse exception
   */
  protected void collectMeasures(File reportXml) throws FileNotFoundException, ParseException {
    MetricsNode metricsNode = getMetrics(reportXml);
    List<FileNode> files = metricsNode.getFiles();
    for (FileNode fileNode : files) {
      String fileName = fileNode.getFileName();
      PhpFile currentResourceFile = PhpFile.getInstance(project).fromAbsolutePath(fileName, project);
      if (currentResourceFile != null) {
        if (measureUnitTests || !ResourceUtils.isUnitTestClass(currentResourceFile)) {
          collectFileMeasures(fileNode, currentResourceFile);
        }
      } else {
        LOG.warn("The following file doesn't belong to current project sources or tests : " + fileName);
      }
    }
    saveMeasures();
  }

  /**
   * Gets the metrics.
   * 
   * @return the metrics
   */
  private Set<Metric> getMetrics() {
    Set<Metric> metricsNode = new HashSet<Metric>();
    metricsNode.add(LINES);
    metricsNode.add(NCLOC);
    metricsNode.add(FUNCTIONS);
    metricsNode.add(COMMENT_LINES);
    metricsNode.add(FILES);
    metricsNode.add(COMPLEXITY);
    metricsNode.add(CLASSES);
    return metricsNode;
  }

  /**
   * Gets the metrics.
   * 
   * @param report
   *          the report
   * @return the metrics
   */
  private MetricsNode getMetrics(File report) {
    InputStream inputStream = null;
    try {
      XStream xstream = new XStream();
      // Migration Sonar 2.2
      xstream.setClassLoader(getClass().getClassLoader());
      xstream.processAnnotations(MetricsNode.class);
      xstream.processAnnotations(PackageNode.class);
      xstream.processAnnotations(FileNode.class);
      xstream.processAnnotations(ClassNode.class);
      xstream.processAnnotations(FunctionNode.class);
      xstream.processAnnotations(MethodNode.class);
      inputStream = new FileInputStream(report);
      return (MetricsNode) xstream.fromXML(inputStream);
    } catch (XStreamException e) {
      throw new SonarException("PDepend report isn't valid: " + report.getName(), e);
    } catch (IOException e) {
      throw new SonarException("Can't read report : " + report.getName(), e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  /**
   * Parses the pdepend report file.
   */
  public void parse(File reportXml) {
    // If no files can be found, plugin will stop normally only logging the
    // error
    if ( !reportXml.exists()) {
      LOG.error("Result file not found : " + reportXml.getAbsolutePath() + ". Plugin will stop");
      return;
    }
    try {
      LOG.info("Collecting measures...");
      collectMeasures(reportXml);
    } catch (Exception e) {
      LOG.error("Report file is invalid or can't be found, plugin will stop.", e);
      throw new SonarException(e);
    }
  }

  /**
   * Saves on measure in the context. One value is associated with a metric and a resource.
   * 
   * @param resource
   *          Can be a PhpFile or a PhpPackage
   * @param metric
   *          the metric evaluated
   * @param measure
   *          the corresponding value
   */
  private void saveMeasure(PhpFile resource, Metric metric, Double measure) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Saving " + metric.getName() + " for resource " + resource.getKey() + " with value " + measure);
    }
    context.saveMeasure(resource, metric, measure);
  }

  /**
   * Saves all the measure contained in the resourceBag used for this analysis.
   * 
   * @throws ParseException
   */
  private void saveMeasures() {
    LOG.info("Saving measures...");
    for (PhpFile resource : resourcesBag.getResources()) {
      for (Metric metric : resourcesBag.getMetrics(resource)) {
        if (metrics.contains(metric)) {
          Double measure = resourcesBag.getMeasure(metric, resource);
          saveMeasure(resource, metric, measure);
        }
      }
    }
  }

}