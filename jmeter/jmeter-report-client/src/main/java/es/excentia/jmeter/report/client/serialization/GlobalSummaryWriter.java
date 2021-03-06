/*
 * JMeter Report Client
 * Copyright (C) 2010 eXcentia
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

package es.excentia.jmeter.report.client.serialization;

import java.io.IOException;
import java.io.OutputStream;

import es.excentia.jmeter.report.client.data.GlobalSummary;

public class GlobalSummaryWriter extends BasicWriter<GlobalSummary> {

  public GlobalSummaryWriter(OutputStream os) {
    super(os);
  }

  @Override
  public void writeObjectToStream(GlobalSummary obj) throws IOException {

    writeString(obj.getTestDesc() == null ? "" : obj.getTestDesc());
    dos.writeLong(obj.getUsersLogged());
    dos.writeLong(obj.getTestDuration());

    dos.writeLong(obj.getRequestsTotal());
    dos.writeLong(obj.getRequestsOkTotal());
    dos.writeLong(obj.getRequestsErrorTotal());
    dos.writeDouble(obj.getRequestsErrorPercent());

    dos.writeDouble(obj.getRequestsOkPerMinute());
    dos.writeDouble(obj.getRequestsOkPerMinuteAndUser());

    dos.writeDouble(obj.getRequestsResponseTimeOkAvg());
    dos.writeDouble(obj.getRequestsResponseTimeOkAvgDev());
    dos.writeDouble(obj.getRequestsResponseTimeOkAvgDevPercent());

    dos.writeDouble(obj.getRequestsBytesOkAvg());
    dos.writeDouble(obj.getRequestsBytesOkAvgDev());
    dos.writeDouble(obj.getRequestsBytesOkAvgDevPercent());

    dos.writeLong(obj.getTransTotal());
    dos.writeLong(obj.getTransOkTotal());
    dos.writeLong(obj.getTransErrorTotal());
    dos.writeDouble(obj.getTransErrorPercent());

    dos.writeDouble(obj.getTransOkPerMinute());
    dos.writeDouble(obj.getTransOkPerMinuteAndUser());

    dos.writeDouble(obj.getTransResponseTimeOkAvg());
    dos.writeDouble(obj.getTransResponseTimeOkAvgDev());
    dos.writeDouble(obj.getTransResponseTimeOkAvgDevPercent());

    dos.writeDouble(obj.getTransBytesOkAvg());
    dos.writeDouble(obj.getTransBytesOkAvgDev());
    dos.writeDouble(obj.getTransBytesOkAvgDevPercent());

    writeString(obj.getSlowestTransName());
    dos.writeDouble(obj.getSlowestTransResponseTimeOkAvg());
    dos.writeDouble(obj.getSlowestTransBytesOkAvgDevPercent());

    writeString(obj.getMostUnstableTransName());
    dos.writeDouble(obj.getMostUnstableTransResponseTimeOkAvgDevPercent());
    dos.writeDouble(obj.getMostUnstableTransBytesOkAvgDevPercent());

    writeStringList(obj.getTransOrder());
    writeMapLong(obj.getTransMapOkTotal());
    writeMapLong(obj.getTransMapErrorTotal());

    writeMapDouble(obj.getTransMapResponseTimeOkAvg());
    writeMapDouble(obj.getTransMapResponseTimeOkAvgDev());
    writeMapDouble(obj.getTransMapResponseTimeOkAvgDevPercent());

    writeMapDouble(obj.getTransMapBytesOkAvg());
    writeMapDouble(obj.getTransMapBytesOkAvgDev());
    writeMapDouble(obj.getTransMapBytesOkAvgDevPercent());

  }

}
