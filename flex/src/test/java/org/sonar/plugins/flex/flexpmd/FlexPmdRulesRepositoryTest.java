/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
package org.sonar.plugins.flex.flexpmd;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.flex.Flex;
import org.sonar.plugins.flex.flexpmd.xml.Ruleset;
import org.sonar.plugins.flex.flexpmd.xml.Rule;
import org.sonar.api.rules.ActiveRule;
import org.apache.commons.io.IOUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;

public class FlexPmdRulesRepositoryTest {

  private FlexPmdRulesRepository repository;

  @Before
  public void setup() {
    repository = new FlexPmdRulesRepository(Flex.INSTANCE);
  }

  @Test
  public void loadFlexRulesRepositoryAndCheckMandatoryFields() {
    List<org.sonar.api.rules.Rule> rules = repository.getInitialReferential();
    assertThat(rules.size(), is(82));
    for (org.sonar.api.rules.Rule rule : rules) {
      assertNotNull(rule.getKey());
      assertNotNull(rule.getDescription());
      assertNotNull(rule.getConfigKey());
      assertNotNull(rule.getName());
    }
  }

  @Test
  public void testBuiltInProfiles() {
    Map<String, String> defaults = repository.getBuiltInProfiles();
    assertThat(defaults.size(), is(1));
    assertNotNull(defaults.get("Default Flex Profile"));
  }


  private Ruleset createRuleSet(String path) throws IOException {
    InputStream input = getClass().getResourceAsStream(path);
    return repository.buildRuleSetFromXml(IOUtils.toString(input, "UTF-8"));
  }

  @Test
  public void testBuildRuleSetFromXml() throws IOException {
    Ruleset ruleSet = createRuleSet("/org/sonar/plugins/flex/flexpmd/flexpmd-import-configuration.xml");
    assertThat(ruleSet.getRules().size(), is(8));
  }

  @Test
  public void testBuildActiveRulesFromRuleSet() throws IOException {
    Ruleset ruleSet = createRuleSet("/org/sonar/plugins/flex/flexpmd/flexpmd-import-configuration.xml");
    List<ActiveRule> activeRules = repository.buildActiveRulesFromRuleSet(ruleSet, repository.getInitialReferential());
    assertThat(activeRules.size(), is(6));
  }

  @Test
  public void testBuildActiveRulesFromEmptyRuleSet() throws IOException {
    Ruleset ruleSet = new Ruleset();
    ruleSet.setRules(null);
    List<ActiveRule> activeRules = repository.buildActiveRulesFromRuleSet(ruleSet, repository.getInitialReferential());
    assertThat(activeRules.size(), is(0));

    ruleSet.setRules(new ArrayList<Rule>());
    activeRules = repository.buildActiveRulesFromRuleSet(ruleSet, repository.getInitialReferential());
    assertThat(activeRules.size(), is(0));
  }

  /*@Ignore
  public void shouldBuilModuleTreeFromXmlInUtf8() throws IOException {
  InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/flex/flexpmd/test_xml_utf8.xml");
  Ruleset ruleset = repository.buildRuleSetFromXml(IOUtils.toString(input, "UTF-8"));

  Rule rule1 = ruleset.getRules().get(0);
  assertThat(rule1.getRef(), is("rulesets/coupling.xml/CouplingBetweenObjects"));
  assertThat(rule1.getProperties().get(0).getValue(), is("\u00E9"));
  } */

/*


@Test
public void shouldBuilXmlFromModuleTree() throws IOException, SAXException {
Ruleset ruleset = buildModuleTreeFixture();
String xml = repository.buildXmlFromModuleTree(ruleset);
assertXmlAreSimilar(xml, "test_module_tree.xml");
}


@Test
public void shouldImportConfiguration() throws IOException {
final List<org.sonar.api.rules.Rule> inputRules = buildRulesFixture();
List<ActiveRule> activeRulesExpected = buildActiveRulesFixture(inputRules);

InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/flex/flexpmd/test_module_tree.xml");
List<ActiveRule> results = repository.importConfiguration(IOUtils.toString(input), inputRules);

assertThat(results.size(), is(activeRulesExpected.size()));
assertActiveRulesAreEquals(results, activeRulesExpected);
}

@Test
public void shouldImportPmdLevelsAsSonarLevels() throws IOException {
InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/flex/flexpmd/PmdRulesRepositoryTest/shouldImportPmdLevelsAsSonarLevels.xml");
final List<org.sonar.api.rules.Rule> rules = buildRulesFixture();
List<ActiveRule> results = repository.importConfiguration(IOUtils.toString(input), rules);

assertThat(results.size(), is(3));
assertThat(results.get(0).getPriority(), is(RulePriority.MAJOR));
assertThat(results.get(1).getPriority(), is(RulePriority.MINOR));
assertThat(results.get(2).getPriority(), is(RulePriority.INFO));
}

@Test
public void shouldImportWithDefaultRuleLevelWhenNoExplicitPriority() throws IOException {
InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/flex/flexpmd/PmdRulesRepositoryTest/shouldImportWithDefaultRuleLevelWhenNoExplicitPriority.xml");
final List<org.sonar.api.rules.Rule> rules = buildRulesFixture();
List<ActiveRule> results = repository.importConfiguration(IOUtils.toString(input), rules);

assertThat(results.size(), is(1));
assertThat(results.get(0).getPriority(), is(RulePriority.MAJOR));
}

@Test
public void shouldImportConfigurationContainingDataToExcludeWithoutException() throws IOException {
shouldImportConfiguration("test_xml_with_data_to_exclude.xml");
}

// See http://jira.codehaus.org/browse/XSTR-448 for details
@Test(expected = ConversionException.class)
public void shouldFailToImportConfigurationContainingClassParamBecauseOfXStreamLimitation() throws IOException {
shouldImportConfiguration("test_xml_with_class_param.xml");
}

@Test
public void shouldBuildActiveRulesFromModuleTree() {
final List<org.sonar.api.rules.Rule> inputRules = buildRulesFixture();
List<ActiveRule> activeRulesExpected = buildActiveRulesFixture(inputRules);

List<ActiveRule> activeRules = new ArrayList<ActiveRule>();
Ruleset ruleset = buildModuleTreeFixture();
repository.buildActiveRulesFromRuleSet(ruleset, activeRules, inputRules);

assertThat(activeRulesExpected.size(), is(activeRules.size()));
assertActiveRulesAreEquals(activeRulesExpected, activeRules);
}

@Ignore
public void shouldExportConfiguration() throws IOException, SAXException {
List<ActiveRule> activeRulesExpected = buildActiveRulesFixture(buildRulesFixture());
RulesProfile activeProfile = new RulesProfile();
activeProfile.setActiveRules(activeRulesExpected);
String xml = repository.exportConfiguration(activeProfile);
assertXmlAreSimilar(xml, "test_xml_complete.xml");
}

@Test
public void shouldAddHeaderToXml() throws IOException, SAXException {
String xml = repository.addHeaderToXml("<ruleset/>");
assertXmlAreSimilar(xml, "test_header.xml");
}

@Test
public void shouldBuildOnlyOneModuleWhenNoPmdActiveRules() {
org.sonar.api.rules.Rule rule1 = new org.sonar.api.rules.Rule();
rule1.setPluginName("not-a-flexpmd-plugin");
ActiveRule activeRule1 = new ActiveRule(null, rule1, RulePriority.CRITICAL);
org.sonar.api.rules.Rule rule2 = new org.sonar.api.rules.Rule();
rule2.setPluginName("not-a-flexpmd-plugin");
ActiveRule activeRule2 = new ActiveRule(null, rule1, RulePriority.CRITICAL);

Ruleset tree = repository.buildModuleTree(Arrays.asList(activeRule1, activeRule2));
assertThat(tree.getRules().size(), is(0));
}

@Test
public void shouldBuildOnlyOneModuleWhenNoActiveRules() {
Ruleset tree = repository.buildModuleTree(Collections.<ActiveRule>emptyList());
assertThat(tree.getRules().size(), is(0));
}

@Ignore
public void shouldBuildTwoModulesEvenIfSameTwoRulesActivated() {
org.sonar.api.rules.Rule dbRule1 = new org.sonar.api.rules.Rule();
dbRule1.setPluginName(CoreProperties.PMD_PLUGIN);
dbRule1.setConfigKey("rulesets/coupling.xml/CouplingBetweenObjects");
ActiveRule activeRule1 = new ActiveRule(null, dbRule1, RulePriority.CRITICAL);
org.sonar.api.rules.Rule dbRule2 = new org.sonar.api.rules.Rule();
dbRule2.setPluginName(CoreProperties.PMD_PLUGIN);
dbRule2.setConfigKey("rulesets/coupling.xml/CouplingBetweenObjects");
ActiveRule activeRule2 = new ActiveRule(null, dbRule2, RulePriority.CRITICAL);

Ruleset tree = repository.buildModuleTree(Arrays.asList(activeRule1, activeRule2));
assertThat(tree.getRules().size(), is(2));

Rule rule1 = tree.getRules().get(0);
assertThat(rule1.getRef(), is("rulesets/coupling.xml/CouplingBetweenObjects"));

Rule rule2 = tree.getRules().get(1);
assertThat(rule2.getRef(), is("rulesets/coupling.xml/CouplingBetweenObjects"));
}

// ------------------------------------------------------------------------
// -- Private methods
// ------------------------------------------------------------------------

private void assertXmlAreSimilar(String xml, String xmlFileToFind) throws IOException, SAXException {
XMLUnit.setIgnoreWhitespace(true);
InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/flex/flexpmd/" + xmlFileToFind);
String xmlToFind = IOUtils.toString(input);
Diff diff = XMLUnit.compareXML(xml, xmlToFind);
assertTrue(diff.toString(), diff.similar());
}

private Ruleset buildModuleTreeFixture() {
Ruleset ruleset = new Ruleset();
ruleset.setDescription("Sonar PMD rules");

Rule rule1 = new Rule("rulesets/coupling.xml/CouplingBetweenObjects", "2");
rule1.addProperty(new Property("threshold", "20"));
ruleset.addRule(rule1);

Rule rule2 = new Rule("rulesets/coupling.xml/ExcessiveImports", "3");
rule2.addProperty(new Property("max", "30"));
ruleset.addRule(rule2);

Rule rule3 = new Rule("rulesets/design.xml/UseNotifyAllInsteadOfNotify", "4");
ruleset.addRule(rule3);

return ruleset;
}

private List<org.sonar.api.rules.Rule> buildRulesFixture() {
final org.sonar.api.rules.Rule rule1 = new org.sonar.api.rules.Rule("Coupling Between Objects", "CouplingBetweenObjects",
    "rulesets/coupling.xml/CouplingBetweenObjects", null, CoreProperties.PMD_PLUGIN, null);
RuleParam ruleParam1 = new RuleParam(rule1, "threshold", null, "i");
rule1.setParams(Arrays.asList(ruleParam1));

final org.sonar.api.rules.Rule rule2 = new org.sonar.api.rules.Rule("Excessive Imports", "ExcessiveImports",
    "rulesets/coupling.xml/ExcessiveImports", null, CoreProperties.PMD_PLUGIN, null);
RuleParam ruleParam2 = new RuleParam(rule2, "max", null, "i");
rule2.setParams(Arrays.asList(ruleParam2));

final org.sonar.api.rules.Rule rule3 = new org.sonar.api.rules.Rule("Use Notify All Instead Of Notify", "UseNotifyAllInsteadOfNotify",
    "rulesets/design.xml/UseNotifyAllInsteadOfNotify", null, CoreProperties.PMD_PLUGIN, null);

final org.sonar.api.rules.Rule rule4 = new org.sonar.api.rules.Rule("Class names should always begin with an upper case character.", "ClassNamingConventions",
    "rulesets/naming.xml/ClassNamingConventions", null, CoreProperties.PMD_PLUGIN, null);

return Arrays.asList(rule1, rule2, rule3, rule4);
}

private List<ActiveRule> buildActiveRulesFixture(List<org.sonar.api.rules.Rule> rules) {
List<ActiveRule> activeRules = new ArrayList<ActiveRule>();

ActiveRule activeRule1 = new ActiveRule(null, rules.get(0), RulePriority.CRITICAL);
activeRule1.setActiveRuleParams(Arrays.asList(new ActiveRuleParam(activeRule1, rules.get(0).getParams().get(0), "20")));
activeRules.add(activeRule1);

ActiveRule activeRule2 = new ActiveRule(null, rules.get(1), RulePriority.MAJOR);
activeRule2.setActiveRuleParams(Arrays.asList(new ActiveRuleParam(activeRule2, rules.get(1).getParams().get(0), "30")));
activeRules.add(activeRule2);

ActiveRule activeRule3 = new ActiveRule(null, rules.get(2), RulePriority.MINOR);
activeRules.add(activeRule3);

return activeRules;

}

private void assertActiveRulesAreEquals(List<ActiveRule> activeRules1, List<ActiveRule> activeRules2) {
for (int i = 0; i < activeRules1.size(); i++) {
  ActiveRule activeRule1 = activeRules1.get(i);
  ActiveRule activeRule2 = activeRules2.get(i);
  assertTrue(activeRule1.getRule().equals(activeRule2.getRule()));
  assertTrue(activeRule1.getPriority().equals(activeRule2.getPriority()));
  assertEquals(activeRule1.getActiveRuleParams().size(), (activeRule2.getActiveRuleParams().size()));

  for (int j = 0; j < activeRule1.getActiveRuleParams().size(); j++) {
    ActiveRuleParam activeRuleParam1 = activeRule1.getActiveRuleParams().get(j);
    ActiveRuleParam activeRuleParam2 = activeRule2.getActiveRuleParams().get(j);
    assertTrue(activeRuleParam1.getRuleParam().equals(activeRuleParam2.getRuleParam())
        && activeRuleParam1.getValue().equals(activeRuleParam2.getValue()));
  }
}
}

public void shouldImportConfiguration(String configurationFile) throws IOException {
final List<org.sonar.api.rules.Rule> inputRules = buildRulesFixture();
List<ActiveRule> activeRulesExpected = buildActiveRulesFixture(inputRules);

InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/flex/flexpmd/" + configurationFile);
List<ActiveRule> results = repository.importConfiguration(IOUtils.toString(input), inputRules);

assertThat(results.size(), is(activeRulesExpected.size()));
assertActiveRulesAreEquals(results, activeRulesExpected);
}
*/
}
