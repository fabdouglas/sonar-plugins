/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
 *
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

package org.sonar.plugins.jlint;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Java;
import org.sonar.api.rules.*;
import org.sonar.plugins.jlint.xml.JlintFilter;

import java.util.*;

public class JlintRulesRepository extends AbstractRulesRepository<Java, JlintRulePriorityMapper>
    implements ConfigurationImportable, ConfigurationExportable {

  public JlintRulesRepository(Java language) {
    super(language, new JlintRulePriorityMapper());
  }

  @Override
  public String getRepositoryResourcesBase() {
    return "org/sonar/plugins/jlint";
  }

  public List<Rule> parseReferential(String fileContent) {
    return new StandardRulesXmlParser().parse(fileContent);
  }

  public List<RulesProfile> getProvidedProfiles() {
    //TODO: Change profile name to appropriate value
    RulesProfile profile = new RulesProfile(RulesProfile.SONAR_WAY_FINDBUGS_NAME, Java.KEY);
    List<Rule> rules = getInitialReferential();
    ArrayList<ActiveRule> activeRules = new ArrayList<ActiveRule>();
    for (Rule rule : rules) {
      activeRules.add(new ActiveRule(profile, rule, null));
    }
    profile.setActiveRules(activeRules);
    return Arrays.asList(profile);
  }

  public String exportConfiguration(RulesProfile activeProfile) {
    JlintFilter filter = JlintFilter.fromActiveRules(activeProfile.getActiveRulesByPlugin(JlintPlugin.KEY), getRulePriorityMapper());
    JlintConfiguration jconfig = new JlintConfiguration(filter);

    String xml = jconfig.toXml();
    return addHeaderToXml(xml);
  }

  private static String addHeaderToXml(String xmlModules) {
    String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!-- Generated by Sonar -->\n";
    return header + xmlModules;
  }

  public List<ActiveRule> importConfiguration(String xml, List<Rule> rules) {
    //JlintFilter filter = JlintFilter.fromXml(xml);

    JlintFilter filter = new JlintConfiguration().fromXml(xml);

    Set<ActiveRule> result = new HashSet<ActiveRule>();

    for (Map.Entry<String, RulePriority> categoryLevel : filter.getCategoryLevels(getRulePriorityMapper()).entrySet()) {
      completeActiveRulesByCategory(result, rules, categoryLevel.getKey(), categoryLevel.getValue());
    }

    for (Map.Entry<String, RulePriority> codeLevel : filter.getCodeLevels(getRulePriorityMapper()).entrySet()) {
      completeActiveRulesByCode(result, rules, codeLevel.getKey(), codeLevel.getValue());
    }

    for (Map.Entry<String, RulePriority> patternLevel : filter.getPatternLevels(getRulePriorityMapper()).entrySet()) {
      completeActiveRulesByPattern(result, rules, patternLevel.getKey(), patternLevel.getValue());
    }

    return new ArrayList<ActiveRule>(result);
  }

  private void completeActiveRulesByCategory(Set<ActiveRule> result, List<Rule> rules, String jlintCategory, RulePriority level) {
    for (Rule rule : rules) {
      String sonarCateg = Category.jlintToSonar(jlintCategory);

      if (sonarCateg != null && rule.getName().startsWith(sonarCateg)) {
        result.add(new ActiveRule(null, rule, level));
      }
    }
  }

  private void completeActiveRulesByCode(Set<ActiveRule> result, List<Rule> rules, String jlintCode, RulePriority level) {
    for (Rule rule : rules) {
      if (rule.getKey().equals(jlintCode) || StringUtils.startsWith(rule.getKey(), jlintCode + "_")) {
        result.add(new ActiveRule(null, rule, level));
      }
    }
  }

  private void completeActiveRulesByPattern(Set<ActiveRule> result, List<Rule> rules, String jlintPattern, RulePriority level) {
    for (Rule rule : rules) {
      if (rule.getKey().equals(jlintPattern)) {
        result.add(new ActiveRule(null, rule, level));
      }
    }
  }

}
