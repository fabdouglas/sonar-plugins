/*
 * Sonar Xml Plugin
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

package org.sonar.plugins.xml.checks;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.SonarException;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.xml.AbstractWebPluginTester;
import org.sonar.plugins.xml.rules.DefaultXmlProfile;
import org.sonar.plugins.xml.rules.XmlMessagesRepository;
import org.sonar.plugins.xml.rules.XmlRulesRepository;
import org.sonar.plugins.xml.rules.XmlSchemaMessagesRepository;

public abstract class AbstractCheckTester extends AbstractWebPluginTester {

  private ProfileDefinition profileDefinition;
  private String repositoryKey;

  protected class SimpleRuleFinder implements RuleFinder {

    private final RulesProfile profile;

    public SimpleRuleFinder() {
      ValidationMessages validationMessages = ValidationMessages.create();
      profile = profileDefinition.createProfile(validationMessages);
      assertEquals(0, validationMessages.getErrors().size());
      assertEquals(0, validationMessages.getWarnings().size());
    }

    public Rule find(RuleQuery query) {
      return null;
    }

    public Collection<Rule> findAll(RuleQuery query) {
      return null;
    }

    public Rule findByKey(String repositoryKey, String key) {
      ActiveRule activeRule = profile.getActiveRuleByConfigKey(repositoryKey, key);
      assertNotNull(activeRule);
      return activeRule.getRule();
    }
  }

  @Before
  public void setProfileDefinition() {
    setProfileDefinition(new DefaultXmlProfile(new XmlRulesRepository(new AnnotationRuleParser()),
        new XmlMessagesRepository(), new XmlSchemaMessagesRepository()));
    setRepositoryKey("Xml");
  }

  protected ProfileDefinition getProfileDefinition() {
    return profileDefinition;
  }

  protected void setProfileDefinition(ProfileDefinition profileDefinition) {
    this.profileDefinition = profileDefinition;
  }

  protected void setRepositoryKey(String repositoryKey) {
    this.repositoryKey = repositoryKey;
  }

  protected String getRepositoryKey() {
    if (repositoryKey == null) {
      repositoryKey = "Web";
    }
    return repositoryKey;
  }

  private Rule getRule(String ruleKey, Class<? extends AbstractPageCheck> checkClass) {

    AnnotationRuleParser parser = new AnnotationRuleParser();
    List<Rule> rules = parser.parse(getRepositoryKey(), Arrays.asList(new Class[] { checkClass }));
    for (Rule rule : rules) {
      if (rule.getKey().equals(ruleKey)) {
        return rule;
      }
    }
    return null;
  }

  public XmlSourceCode parseAndCheck(Reader reader, Class<? extends AbstractPageCheck> checkClass, String... params) {

    return parseAndCheck(reader, null, null, checkClass, params);
  }

  public XmlSourceCode parseAndCheck(Reader reader, java.io.File file,
      String code, Class<? extends AbstractPageCheck> checkClass, String... params) {

    AbstractPageCheck check = instantiateCheck(checkClass, params);

    XmlSourceCode xmlSourceCode = new XmlSourceCode(new File(file == null ? "test" : file.getPath()), file);
    xmlSourceCode.setCode(code);

    check.validate(xmlSourceCode);

    return xmlSourceCode;
  }

  protected AbstractPageCheck instantiateCheck(Class<? extends AbstractPageCheck> checkClass, String... params) {
    try {
      AbstractPageCheck check = checkClass.newInstance();

      Rule rule = getRule(checkClass.getSimpleName(), checkClass);
      assertNotNull("Could not find rule", rule);
      check.setRule(rule);
      configureDefaultParams(check, rule);

      for (int i = 0; i < params.length / 2; i++) {
        BeanUtils.setProperty(check, params[i * 2], params[i * 2 + 1]);
        assertNotNull(BeanUtils.getProperty(check, params[i * 2]));
      }
      return check;
    } catch (IllegalAccessException e) {
      throw new SonarException(e);
    } catch (InstantiationException e) {
      throw new SonarException(e);
    } catch (InvocationTargetException e) {
      throw new SonarException(e);
    } catch (NoSuchMethodException e) {
      throw new SonarException(e);
    }
  }

  private void configureDefaultParams(AbstractPageCheck check, Rule rule) {
    ValidationMessages validationMessages = ValidationMessages.create();
    RulesProfile rulesProfile = getProfileDefinition().createProfile(validationMessages);

    ActiveRule activeRule = rulesProfile.getActiveRule(rule);

    assertNotNull("Could not find activeRule", activeRule);

    try {
      if (activeRule.getActiveRuleParams() != null) {
        for (ActiveRuleParam param : activeRule.getActiveRuleParams()) {
          Object value = PropertyUtils.getProperty(check, param.getRuleParam().getKey());
          if (value instanceof Integer) {
            value = Integer.parseInt(param.getValue());
          } else {
            value = param.getValue();
          }
          PropertyUtils.setProperty(check, param.getRuleParam().getKey(), value);
        }
      }
    } catch (IllegalAccessException e) {
      throw new SonarException(e);
    } catch (InvocationTargetException e) {
      throw new SonarException(e);
    } catch (NoSuchMethodException e) {
      throw new SonarException(e);
    }
  }
}