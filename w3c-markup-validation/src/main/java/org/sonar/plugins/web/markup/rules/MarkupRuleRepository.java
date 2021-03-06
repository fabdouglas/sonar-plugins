/*
 * Sonar W3C Markup Validation Plugin
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
package org.sonar.plugins.web.markup.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.RuleRepository;
import org.sonar.plugins.web.api.WebConstants;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * MarkupRuleRepository provides a repository of the W3C Markup rules.
 *
 * @author Matthijs Galesloot
 * @since 1.0
 */
public final class MarkupRuleRepository extends RuleRepository {

  @XStreamAlias("rule")
  public static class HtmlMarkupRule {

    private String explanation;
    private String key;
    private RulePriority priority;
    private String remark;

    public String getExplanation() {
      return explanation;
    }

    public String getKey() {
      return key;
    }

    public RulePriority getPriority() {
      return priority;
    }

    public String getRemark() {
      return remark;
    }

    public void setExplanation(String explanation) {
      this.explanation = explanation;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public void setPriority(RulePriority priority) {
      this.priority = priority;
    }

    public void setRemark(String remark) {
      this.remark = remark;
    }
  }

  @XStreamAlias("rules")
  public static class HtmlMarkupRules {

    @XStreamImplicit(itemFieldName = "rule")
    public List<HtmlMarkupRule> rules;
  }

  private static final String ALL_RULES = "org/sonar/plugins/web/markup/rules/rules.xml";
  public static final String REPOSITORY_KEY = "W3CMarkupValidation";

  public static final String REPOSITORY_NAME = "W3C Markup Validation";

  private static final int RULENAME_MAX_LENGTH = 192;

  public MarkupRuleRepository() {
    super(MarkupRuleRepository.REPOSITORY_KEY, WebConstants.LANGUAGE_KEY);
    setName(MarkupRuleRepository.REPOSITORY_NAME);
  }

  @Override
  public List<Rule> createRules() {
    List<Rule> rules = new ArrayList<Rule>();

    XStream xstream = new XStream();
    xstream.setClassLoader(getClass().getClassLoader());
    xstream.processAnnotations(HtmlMarkupRules.class);
    HtmlMarkupRules markupRules = (HtmlMarkupRules) xstream.fromXML(MarkupRuleRepository.class.getClassLoader().getResourceAsStream(ALL_RULES));
    for (HtmlMarkupRule htmlMarkupRule : markupRules.rules) {
      Rule rule = Rule.create(REPOSITORY_KEY, htmlMarkupRule.getKey(),
          StringUtils.abbreviate(htmlMarkupRule.getRemark(), RULENAME_MAX_LENGTH));
      if (htmlMarkupRule.getExplanation() != null) {
        rule.setDescription(StringEscapeUtils.escapeHtml(htmlMarkupRule.getExplanation()));
      }
      rule.setSeverity(htmlMarkupRule.getPriority());
      rules.add(rule);
    }
    return rules;
  }
}
