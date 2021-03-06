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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.sonar.plugins.testability.client.model.MethodTestabilityCostData;
import org.codehaus.sonar.plugins.testability.client.model.MethodTestabilityCostDataDecoder;
import org.codehaus.sonar.plugins.testability.client.model.MethodTestabilityCostDataDecoderImpl;
import org.codehaus.sonar.plugins.testability.client.model.ViolationCostDetail;
import org.codehaus.sonar.plugins.testability.client.webservices.WSTestabilityMetrics;

import org.sonar.gwt.ui.SourcePanel;
import org.sonar.wsclient.gwt.AbstractCallback;
import org.sonar.wsclient.gwt.Sonar;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

public class GwtTestabilitySourcePanel extends SourcePanel {

  private static final String COMPLEXITY_TITLE = "CC";
  private static final String LOD_TITLE = "Lod";
  private static final String LINE_TITLE = "Line";

  private MethodTestabilityCostData costData;

  public MethodTestabilityCostData getMethodTestabilityCostData() {
    if (this.costData == null) {
      this.costData = new MethodTestabilityCostData();
    }
    return this.costData;
  }

  public GwtTestabilitySourcePanel(Resource resource) {
    super(resource);
    loadTestabilityCosts(resource);
  }

  private void loadTestabilityCosts(final Resource resource) {
    ResourceQuery query = ResourceQuery.createForResource(resource, WSTestabilityMetrics.METHOD_DETAILS_COST);
    Sonar.getInstance().find(query, new AbstractCallback<Resource>(){
      @Override
      protected void doOnResponse(Resource result) {
        MethodTestabilityCostDataDecoder decoder = new MethodTestabilityCostDataDecoderImpl();
        setMethodTestabilityCostData(decoder.decode(resource.getMeasure(WSTestabilityMetrics.METHOD_DETAILS_COST).getData()));
        setStarted();
      }
    });
  }
  
  @Override
  protected List<Row> decorateLine(int index, String source) {
    List<Row> response;
    if (index == 0) {
      response = Arrays.asList(new Row(LINE_TITLE, LOD_TITLE, COMPLEXITY_TITLE, ""));
    } else {
      response = decorateSourceLine(index, source);
    }
    return response;
  }

  private List<Row> decorateSourceLine(int index, String source) {
    List<Row> rows;
    if (hasCost(index)) {
      rows = decorateSourceLineWithCosts(index, source);
    } else {
      rows = Arrays.asList(new Row(index, source));
    }
    return rows;
  }

  private List<Row> decorateSourceLineWithCosts(int lineIndex, String source) {
    List<Row> rows;
    if (hasLineViolationsCost(lineIndex)) {
      rows = getViolationCostRows(lineIndex, source);
    } else {
      rows = Arrays.asList((Row)new HasCostDataRow(getMethodTestabilityCostData().getMethodCostOfLine(lineIndex), lineIndex, source));
    }
    return rows;
  }

  private List<Row> getViolationCostRows(int lineIndex, String source) {
    List<Row> rows;
    rows = new ArrayList<Row>();
    rows.add(new Row().setLineIndex(lineIndex, "red").setSource(source, "red").unsetValue());
    List<ViolationCostDetail> violationsOfLine = getMethodTestabilityCostData().getViolationsOfLine(lineIndex);
    for (ViolationCostDetail violationCostDetail : violationsOfLine) {
      rows.add(new HasCostDataRow(violationCostDetail, lineIndex, violationCostDetail.getReason()));
    }
    return rows;
  }

  public void setMethodTestabilityCostData(MethodTestabilityCostData methodTestabilityCostData) {
    this.costData = methodTestabilityCostData;
  }
  
  private boolean hasCost(int lineIndex) {
    return hasLineViolationsCost(lineIndex) || getMethodTestabilityCostData().getMethodCostOfLine(lineIndex) != null;
  }
  
  private boolean hasLineViolationsCost(int lineIndex) {
    List<ViolationCostDetail> violationsOfLine = getMethodTestabilityCostData().getViolationsOfLine(lineIndex);
    return violationsOfLine != null && violationsOfLine.size() > 0;
  }

}
