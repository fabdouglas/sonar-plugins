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

package org.sonar.plugins.webscanner.toetstool.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;
import org.sonar.plugins.webscanner.toetstool.xml.ToetstoolReport;

public class ToetstoolXmlTest {

  @Test
  @Ignore
  public void testReport() throws FileNotFoundException {
    InputStream input = getClass().getResourceAsStream("toetstool-report1.xml");
    ToetstoolReport toetstool = ToetstoolReport.fromXml(input);
    assertNotNull(toetstool);
    assertEquals("test-url-1", toetstool.getReport().getUrl());
    assertNotNull(toetstool.getReport().getCounters().getError());
    assertTrue(toetstool.getReport().getGuidelines().size() > 20);
  }
}