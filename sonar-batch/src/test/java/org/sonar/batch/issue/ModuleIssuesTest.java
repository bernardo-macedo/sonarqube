/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.batch.issue;

import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.RulesBuilder;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.MessageException;
import org.sonar.core.issue.DefaultIssue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ModuleIssuesTest {

  static final RuleKey SQUID_RULE_KEY = RuleKey.of("squid", "AvoidCycle");
  static final String SQUID_RULE_NAME = "Avoid Cycle";

  @Mock
  IssueCache cache;

  @Mock
  Project project;

  @Mock
  IssueFilters filters;

  ActiveRulesBuilder activeRulesBuilder = new ActiveRulesBuilder();
  RulesBuilder ruleBuilder = new RulesBuilder();

  ModuleIssues moduleIssues;

  @Before
  public void setUp() {
    when(project.getAnalysisDate()).thenReturn(new Date());
    when(project.getEffectiveKey()).thenReturn("org.apache:struts-core");
    when(project.getRoot()).thenReturn(project);
  }

  @Test
  public void fail_on_unknown_rule() {
    initModuleIssues();
    DefaultIssue issue = new DefaultIssue().setRuleKey(SQUID_RULE_KEY);

    try {
      moduleIssues.initAndAddIssue(issue);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(MessageException.class);
    }

    verifyZeroInteractions(cache);
  }

  @Test
  public void fail_if_rule_has_no_name_and_issue_has_no_message() {
    ruleBuilder.add(RuleKey.of("squid", "AvoidCycle"));
    initModuleIssues();
    DefaultIssue issue = new DefaultIssue().setRuleKey(SQUID_RULE_KEY).setMessage("");

    try {
      moduleIssues.initAndAddIssue(issue);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(MessageException.class);
    }

    verifyZeroInteractions(cache);
  }

  @Test
  public void ignore_null_active_rule() {
    ruleBuilder.add(SQUID_RULE_KEY).setName(SQUID_RULE_NAME);
    initModuleIssues();

    DefaultIssue issue = new DefaultIssue().setRuleKey(SQUID_RULE_KEY);
    boolean added = moduleIssues.initAndAddIssue(issue);

    assertThat(added).isFalse();
    verifyZeroInteractions(cache);
  }

  @Test
  public void ignore_null_rule_of_active_rule() {
    ruleBuilder.add(SQUID_RULE_KEY).setName(SQUID_RULE_NAME);
    activeRulesBuilder.create(SQUID_RULE_KEY).activate();
    initModuleIssues();

    DefaultIssue issue = new DefaultIssue().setRuleKey(SQUID_RULE_KEY);
    boolean added = moduleIssues.initAndAddIssue(issue);

    assertThat(added).isFalse();
    verifyZeroInteractions(cache);
  }

  @Test
  public void add_issue_to_cache() {
    ruleBuilder.add(SQUID_RULE_KEY).setName(SQUID_RULE_NAME);
    activeRulesBuilder.create(SQUID_RULE_KEY).setSeverity(Severity.INFO).activate();
    initModuleIssues();

    Date analysisDate = new Date();
    when(project.getAnalysisDate()).thenReturn(analysisDate);

    DefaultIssue issue = new DefaultIssue()
      .setKey("ABCDE")
      .setRuleKey(SQUID_RULE_KEY)
      .setSeverity(Severity.CRITICAL);
    when(filters.accept(issue)).thenReturn(true);

    boolean added = moduleIssues.initAndAddIssue(issue);

    assertThat(added).isTrue();
    ArgumentCaptor<DefaultIssue> argument = ArgumentCaptor.forClass(DefaultIssue.class);
    verify(cache).put(argument.capture());
    assertThat(argument.getValue().severity()).isEqualTo(Severity.CRITICAL);
    assertThat(argument.getValue().creationDate()).isEqualTo(DateUtils.truncate(analysisDate, Calendar.SECOND));
  }

  @Test
  public void use_severity_from_active_rule_if_no_severity_on_issue() {
    ruleBuilder.add(SQUID_RULE_KEY).setName(SQUID_RULE_NAME);
    activeRulesBuilder.create(SQUID_RULE_KEY).setSeverity(Severity.INFO).activate();
    initModuleIssues();

    Date analysisDate = new Date();
    when(project.getAnalysisDate()).thenReturn(analysisDate);

    DefaultIssue issue = new DefaultIssue().setRuleKey(SQUID_RULE_KEY).setSeverity(null);
    when(filters.accept(issue)).thenReturn(true);
    moduleIssues.initAndAddIssue(issue);

    ArgumentCaptor<DefaultIssue> argument = ArgumentCaptor.forClass(DefaultIssue.class);
    verify(cache).put(argument.capture());
    assertThat(argument.getValue().severity()).isEqualTo(Severity.INFO);
    assertThat(argument.getValue().creationDate()).isEqualTo(DateUtils.truncate(analysisDate, Calendar.SECOND));
  }

  @Test
  public void use_rule_name_if_no_message() {
    ruleBuilder.add(SQUID_RULE_KEY).setName(SQUID_RULE_NAME);
    activeRulesBuilder.create(SQUID_RULE_KEY).setSeverity(Severity.INFO).setName(SQUID_RULE_NAME).activate();
    initModuleIssues();

    Date analysisDate = new Date();
    when(project.getAnalysisDate()).thenReturn(analysisDate);

    DefaultIssue issue = new DefaultIssue()
      .setKey("ABCDE")
      .setRuleKey(SQUID_RULE_KEY)
      .setSeverity(Severity.CRITICAL)
      .setMessage("");
    when(filters.accept(issue)).thenReturn(true);

    boolean added = moduleIssues.initAndAddIssue(issue);

    assertThat(added).isTrue();
    ArgumentCaptor<DefaultIssue> argument = ArgumentCaptor.forClass(DefaultIssue.class);
    verify(cache).put(argument.capture());
    assertThat(argument.getValue().message()).isEqualTo("Avoid Cycle");
  }

  @Test
  public void add_deprecated_violation() {
    ruleBuilder.add(SQUID_RULE_KEY).setName(SQUID_RULE_NAME);
    activeRulesBuilder.create(SQUID_RULE_KEY).setSeverity(Severity.INFO).activate();
    initModuleIssues();

    org.sonar.api.rules.Rule rule = org.sonar.api.rules.Rule.create("squid", "AvoidCycle", "Avoid Cycle");
    Resource resource = File.create("org/struts/Action.java").setEffectiveKey("struts:src/org/struts/Action.java");
    Violation violation = new Violation(rule, resource);
    violation.setLineId(42);
    violation.setSeverity(RulePriority.CRITICAL);
    violation.setMessage("the message");

    when(filters.accept(any(DefaultIssue.class))).thenReturn(true);

    boolean added = moduleIssues.initAndAddViolation(violation);
    assertThat(added).isTrue();

    ArgumentCaptor<DefaultIssue> argument = ArgumentCaptor.forClass(DefaultIssue.class);
    verify(cache).put(argument.capture());
    DefaultIssue issue = argument.getValue();
    assertThat(issue.severity()).isEqualTo(Severity.CRITICAL);
    assertThat(issue.line()).isEqualTo(42);
    assertThat(issue.message()).isEqualTo("the message");
    assertThat(issue.key()).isNotEmpty();
    assertThat(issue.ruleKey().toString()).isEqualTo("squid:AvoidCycle");
    assertThat(issue.componentKey()).isEqualTo("struts:src/org/struts/Action.java");
    assertThat(issue.projectKey()).isEqualTo("org.apache:struts-core");
  }

  @Test
  public void filter_issue() {
    ruleBuilder.add(SQUID_RULE_KEY).setName(SQUID_RULE_NAME);
    activeRulesBuilder.create(SQUID_RULE_KEY).setSeverity(Severity.INFO).activate();
    initModuleIssues();

    DefaultIssue issue = new DefaultIssue()
      .setKey("ABCDE")
      .setRuleKey(SQUID_RULE_KEY)
      .setSeverity(Severity.CRITICAL);

    when(filters.accept(issue)).thenReturn(false);

    boolean added = moduleIssues.initAndAddIssue(issue);

    assertThat(added).isFalse();
    verifyZeroInteractions(cache);
  }

  /**
   * Every rules and active rules has to be added in builders before creating ModuleIssues
   */
  private void initModuleIssues() {
    moduleIssues = new ModuleIssues(activeRulesBuilder.build(), ruleBuilder.build(), cache, project, filters);
  }

}
