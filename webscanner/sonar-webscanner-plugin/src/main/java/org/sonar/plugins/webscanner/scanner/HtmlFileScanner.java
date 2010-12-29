/*
 * Sonar Webscanner Plugin
 * Copyright (C) 2010 Matthijs Galesloot
 * dev@sonar.codehaus.org
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

package org.sonar.plugins.webscanner.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Logger;

/**
 * Scanner for html files.
 *
 * @author Matthijs Galesloot
 * @since 0.1
 *
 */
public final class HtmlFileScanner {

  private static final Logger LOG = Logger.getLogger(HtmlFileScanner.class);

  private final HtmlFileVisitor visitor;

  public HtmlFileScanner(HtmlFileVisitor visitor) {
    this.visitor = visitor;
  }

  protected List<File> randomSubsetFiles(List<File> files, Integer amount) {
    List<File> newCollection = new ArrayList<File>();
    for (int i = 0; i < amount && i < files.size(); i++) {
      newCollection.add(files.get(i));
    }
    return newCollection;
  }

  public void validateFiles(List<File> files, Integer nrOfSamples) {

    if (nrOfSamples != null && nrOfSamples > 0) {
      files = randomSubsetFiles(files, nrOfSamples);
    }

    int n = 0;
    for (File file : files) {
      // skip analysis if the report already exists
      File reportFile = visitor.reportFile(file);
      if (!reportFile.exists()) {
        if (n++ > 0) {
          visitor.waitBetweenValidationRequests();
        }
        LOG.debug("Validating " + file.getPath() + "...");
        visitor.validateFile(file);
      }
    }
  }

  public static Collection<File> getReportFiles(File htmlFolder, final String reportXml) {
    @SuppressWarnings("unchecked")
    Collection<File> reportFiles = FileUtils.listFiles(htmlFolder, new IOFileFilter() {
  
      public boolean accept(File file) {
        return file.getName().endsWith(reportXml);
      }
  
      public boolean accept(File dir, String name) {
        return name.endsWith(reportXml);
      }
    }, new IOFileFilter() {
  
      public boolean accept(File file) {
        return true;
      }
  
      public boolean accept(File dir, String name) {
        return true;
      }
    });
  
    return reportFiles;
  }
}