/*
 * Ada Sonar Plugin
 * Copyright (C) 2010 Akram Ben Aissi
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

package org.sonar.plugins.ada.core;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.Phase;
import org.sonar.api.resources.Project;

import org.sonar.plugins.ada.Ada;

/**
 * The AdaSourceImporter is in charge of analysing and loading valid ada files. All source files under source folder and test source folder
 * will be imported. The extension will only execute on ada projects
 */
@Phase(name = Phase.Name.PRE)
public class AdaSourceImporter extends AbstractSourceImporter {

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(AdaSourceImporter.class);

  /**
   * Instantiates the source importer.
   */
  public AdaSourceImporter(Project project) {
    super(Ada.INSTANCE);
  }

  protected AdaFile createResource(File file, List<File> sourceDirs, boolean unitTest) {
    String absolutePath = file.getAbsolutePath();
    LOG.debug("Creating ada file " + absolutePath);
    return file != null ? AdaFile.fromIOFile(file, sourceDirs, unitTest) : null;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
