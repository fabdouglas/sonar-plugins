/*
 * Sonar Clover Plugin
 * Copyright (C) 2008 SonarSource
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

package org.sonar.plugins.clover.it;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.ResourceQuery;

public abstract class AbstractIT {

  protected static Sonar sonar;

  @BeforeClass
  public static void buildServer() {
    sonar = Sonar.create("http://localhost:9000");
  }

  protected abstract String getProjectKey();

  protected String getProfile() {
    return "turbo";
  }

  @Test
  public void projectIsAnalyzed() {
    assertThat("Version", sonar.find(new ResourceQuery(getProjectKey())).getVersion(), is("1.0-SNAPSHOT"));
    assertThat("Profile", getProjectMeasure("profile").getData(), is(getProfile()));
  }

  protected Measure getProjectMeasure(String metricKey) {
    return sonar.find(ResourceQuery.createForMetrics(getProjectKey(), metricKey)).getMeasure(metricKey);
  }

  protected Measure getMeasure(String resourceKey, String metricKey) {
    return sonar.find(ResourceQuery.createForMetrics(getProjectKey() + ":" + resourceKey, metricKey)).getMeasure(metricKey);
  }

}
