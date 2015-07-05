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
package org.sonar.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.Map;
import org.sonar.api.server.ServerSide;
import org.sonar.db.Dao;
import org.sonar.db.Database;
import org.sonar.db.DbSession;
import org.sonar.db.MyBatis;
import org.sonar.db.activity.ActivityDao;
import org.sonar.db.component.ComponentIndexDao;
import org.sonar.db.component.ComponentLinkDao;
import org.sonar.db.component.ResourceDao;
import org.sonar.db.component.SnapshotDao;
import org.sonar.db.compute.AnalysisReportDao;
import org.sonar.db.dashboard.DashboardDao;
import org.sonar.db.dashboard.WidgetDao;
import org.sonar.db.dashboard.WidgetPropertyDao;
import org.sonar.db.debt.CharacteristicDao;
import org.sonar.db.event.EventDao;
import org.sonar.db.issue.ActionPlanDao;
import org.sonar.db.issue.IssueChangeDao;
import org.sonar.db.issue.IssueDao;
import org.sonar.db.issue.IssueFilterDao;
import org.sonar.db.loadedtemplate.LoadedTemplateDao;
import org.sonar.db.measure.MeasureDao;
import org.sonar.db.permission.PermissionTemplateDao;
import org.sonar.db.property.PropertiesDao;
import org.sonar.db.purge.PurgeDao;
import org.sonar.db.qualitygate.QualityGateConditionDao;
import org.sonar.db.qualityprofile.QualityProfileDao;
import org.sonar.db.source.FileSourceDao;
import org.sonar.db.user.AuthorDao;
import org.sonar.db.user.AuthorizationDao;
import org.sonar.db.user.GroupMembershipDao;
import org.sonar.db.user.RoleDao;
import org.sonar.db.user.UserGroupDao;
import org.sonar.server.component.db.ComponentDao;
import org.sonar.server.measure.custom.persistence.CustomMeasureDao;
import org.sonar.server.metric.persistence.MetricDao;
import org.sonar.server.qualityprofile.db.ActiveRuleDao;
import org.sonar.server.rule.db.RuleDao;
import org.sonar.server.user.db.GroupDao;
import org.sonar.server.user.db.UserDao;

/**
 * Facade for all db components, mainly DAOs
 */
@ServerSide
public class DbClient {

  private final Database db;
  private final MyBatis myBatis;
  private final RuleDao ruleDao;
  private final ActiveRuleDao activeRuleDao;
  private final QualityProfileDao qualityProfileDao;
  private final CharacteristicDao debtCharacteristicDao;
  private final LoadedTemplateDao loadedTemplateDao;
  private final PropertiesDao propertiesDao;
  private final ComponentDao componentDao;
  private final SnapshotDao snapshotDao;
  private final ResourceDao resourceDao;
  private final MeasureDao measureDao;
  private final MetricDao metricDao;
  private final ActivityDao activityDao;
  private final AuthorizationDao authorizationDao;
  private final UserDao userDao;
  private final GroupDao groupDao;
  private final UserGroupDao userGroupDao;
  private final GroupMembershipDao groupMembershipDao;
  private final RoleDao roleDao;
  private final PermissionTemplateDao permissionTemplateDao;
  private final IssueDao issueDao;
  private final IssueFilterDao issueFilterDao;
  private final IssueChangeDao issueChangeDao;
  private final ActionPlanDao actionPlanDao;
  private final AnalysisReportDao analysisReportDao;
  private final DashboardDao dashboardDao;
  private final WidgetDao widgetDao;
  private final WidgetPropertyDao widgetPropertyDao;
  private final FileSourceDao fileSourceDao;
  private final AuthorDao authorDao;
  private final ComponentIndexDao componentIndexDao;
  private final ComponentLinkDao componentLinkDao;
  private final EventDao eventDao;
  private final PurgeDao purgeDao;
  private final CustomMeasureDao customMeasureDao;
  private final QualityGateConditionDao gateConditionDao;

  public DbClient(Database db, MyBatis myBatis, Dao... daos) {
    this.db = db;
    this.myBatis = myBatis;

    Map<Class, Dao> map = new IdentityHashMap<>();
    for (Dao dao : daos) {
      map.put(dao.getClass(), dao);
    }
    ruleDao = getDao(map, RuleDao.class);
    activeRuleDao = getDao(map, ActiveRuleDao.class);
    debtCharacteristicDao = getDao(map, CharacteristicDao.class);
    qualityProfileDao = getDao(map, QualityProfileDao.class);
    loadedTemplateDao = getDao(map, LoadedTemplateDao.class);
    propertiesDao = getDao(map, PropertiesDao.class);
    componentDao = getDao(map, ComponentDao.class);
    snapshotDao = getDao(map, SnapshotDao.class);
    resourceDao = getDao(map, ResourceDao.class);
    measureDao = getDao(map, MeasureDao.class);
    metricDao = getDao(map, MetricDao.class);
    customMeasureDao = getDao(map, CustomMeasureDao.class);
    activityDao = getDao(map, ActivityDao.class);
    authorizationDao = getDao(map, AuthorizationDao.class);
    userDao = getDao(map, UserDao.class);
    groupDao = getDao(map, GroupDao.class);
    userGroupDao = getDao(map, UserGroupDao.class);
    groupMembershipDao = getDao(map, GroupMembershipDao.class);
    roleDao = getDao(map, RoleDao.class);
    permissionTemplateDao = getDao(map, PermissionTemplateDao.class);
    issueDao = getDao(map, IssueDao.class);
    issueFilterDao = getDao(map, IssueFilterDao.class);
    issueChangeDao = getDao(map, IssueChangeDao.class);
    actionPlanDao = getDao(map, ActionPlanDao.class);
    analysisReportDao = getDao(map, AnalysisReportDao.class);
    dashboardDao = getDao(map, DashboardDao.class);
    widgetDao = getDao(map, WidgetDao.class);
    widgetPropertyDao = getDao(map, WidgetPropertyDao.class);
    fileSourceDao = getDao(map, FileSourceDao.class);
    authorDao = getDao(map, AuthorDao.class);
    componentIndexDao = getDao(map, ComponentIndexDao.class);
    componentLinkDao = getDao(map, ComponentLinkDao.class);
    eventDao = getDao(map, EventDao.class);
    purgeDao = getDao(map, PurgeDao.class);
    gateConditionDao = getDao(map, QualityGateConditionDao.class);
  }

  public Database database() {
    return db;
  }

  public DbSession openSession(boolean batch) {
    return myBatis.openSession(batch);
  }

  public RuleDao ruleDao() {
    return ruleDao;
  }

  public ActiveRuleDao activeRuleDao() {
    return activeRuleDao;
  }

  public IssueDao issueDao() {
    return issueDao;
  }

  public IssueFilterDao issueFilterDao() {
    return issueFilterDao;
  }

  public IssueChangeDao issueChangeDao() {
    return issueChangeDao;
  }

  public QualityProfileDao qualityProfileDao() {
    return qualityProfileDao;
  }

  public CharacteristicDao debtCharacteristicDao() {
    return debtCharacteristicDao;
  }

  public LoadedTemplateDao loadedTemplateDao() {
    return loadedTemplateDao;
  }

  public PropertiesDao propertiesDao() {
    return propertiesDao;
  }

  public ComponentDao componentDao() {
    return componentDao;
  }

  public SnapshotDao snapshotDao() {
    return snapshotDao;
  }

  public ResourceDao resourceDao() {
    return resourceDao;
  }

  public MeasureDao measureDao() {
    return measureDao;
  }

  public MetricDao metricDao() {
    return metricDao;
  }

  public CustomMeasureDao customMeasureDao() {
    return customMeasureDao;
  }

  public ActivityDao activityDao() {
    return activityDao;
  }

  public AuthorizationDao authorizationDao() {
    return authorizationDao;
  }

  public UserDao userDao() {
    return userDao;
  }

  public GroupDao groupDao() {
    return groupDao;
  }

  public UserGroupDao userGroupDao() {
    return userGroupDao;
  }

  public GroupMembershipDao groupMembershipDao() {
    return groupMembershipDao;
  }

  public RoleDao roleDao() {
    return roleDao;
  }

  public PermissionTemplateDao permissionTemplateDao() {
    return permissionTemplateDao;
  }

  public ActionPlanDao actionPlanDao() {
    return actionPlanDao;
  }

  public AnalysisReportDao analysisReportDao() {
    return analysisReportDao;
  }

  public DashboardDao dashboardDao() {
    return dashboardDao;
  }

  public WidgetDao widgetDao() {
    return widgetDao;
  }

  public WidgetPropertyDao widgetPropertyDao() {
    return widgetPropertyDao;
  }

  public FileSourceDao fileSourceDao() {
    return fileSourceDao;
  }

  public AuthorDao authorDao() {
    return authorDao;
  }

  public ComponentIndexDao componentIndexDao() {
    return componentIndexDao;
  }

  public ComponentLinkDao componentLinkDao() {
    return componentLinkDao;
  }

  public EventDao eventDao() {
    return eventDao;
  }

  public PurgeDao purgeDao() {
    return purgeDao;
  }

  public QualityGateConditionDao gateConditionDao() {
    return gateConditionDao;
  }

  private <K> K getDao(Map<Class, Dao> map, Class<K> clazz) {
    return (K) map.get(clazz);
  }

  /**
   * Create a PreparedStatement for SELECT requests with scrolling of results
   */
  public final PreparedStatement newScrollingSelectStatement(Connection connection, String sql) {
    int fetchSize = database().getDialect().getScrollDefaultFetchSize();
    return newScrollingSelectStatement(connection, sql, fetchSize);
  }

  /**
   * Create a PreparedStatement for SELECT requests with scrolling of results row by row (only one row
   * in memory at a time)
   */
  public final PreparedStatement newScrollingSingleRowSelectStatement(Connection connection, String sql) {
    int fetchSize = database().getDialect().getScrollSingleRowFetchSize();
    return newScrollingSelectStatement(connection, sql, fetchSize);
  }

  private PreparedStatement newScrollingSelectStatement(Connection connection, String sql, int fetchSize) {
    try {
      PreparedStatement stmt = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      stmt.setFetchSize(fetchSize);
      return stmt;
    } catch (SQLException e) {
      throw new IllegalStateException("Fail to create SQL statement: " + sql, e);
    }
  }
}
