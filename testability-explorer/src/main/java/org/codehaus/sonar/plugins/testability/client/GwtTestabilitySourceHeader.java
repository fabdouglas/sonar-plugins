/*
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.codehaus.sonar.plugins.testability.client;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.codehaus.sonar.plugins.testability.client.webservices.WSTestabilityMetrics;
import org.sonar.gwt.ui.ViewerHeader;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;

public class GwtTestabilitySourceHeader extends ViewerHeader {

  public GwtTestabilitySourceHeader(Resource resource) {
    super(resource, new String[]{WSTestabilityMetrics.TESTABILITY_COST});
  }

  @Override
  protected void display(FlowPanel header, Resource resource) {
    HorizontalPanel panel = new HorizontalPanel();
    header.add(panel);
    Measure measure = resource.getMeasure(WSTestabilityMetrics.TESTABILITY_COST);
    if (measure == null) {
      addCell(panel, "Cost", "No data available");
    } else {
      addBigCell(panel, "Cost: " + measure.getFormattedValue());
    }
  }

}
